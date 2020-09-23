package org.jitsi.impl.neomedia.transform.dtls;

import com.lti.utils.UnsignedUtils;
import com.sun.media.format.WavAudioFormat;
import java.io.IOException;
import java.security.SecureRandom;
import org.jitsi.bouncycastle.crypto.tls.DTLSClientProtocol;
import org.jitsi.bouncycastle.crypto.tls.DTLSProtocol;
import org.jitsi.bouncycastle.crypto.tls.DTLSServerProtocol;
import org.jitsi.bouncycastle.crypto.tls.DTLSTransport;
import org.jitsi.bouncycastle.crypto.tls.DatagramTransport;
import org.jitsi.bouncycastle.crypto.tls.ProtocolVersion;
import org.jitsi.bouncycastle.crypto.tls.TlsClientContext;
import org.jitsi.bouncycastle.crypto.tls.TlsContext;
import org.jitsi.bouncycastle.crypto.tls.TlsFatalAlert;
import org.jitsi.bouncycastle.crypto.tls.TlsPeer;
import org.jitsi.bouncycastle.crypto.tls.TlsServerContext;
import org.jitsi.bouncycastle.crypto.tls.TlsUtils;
import org.jitsi.impl.neomedia.AbstractRTPConnector;
import org.jitsi.impl.neomedia.RawPacket;
import org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.WASAPI;
import org.jitsi.impl.neomedia.transform.SinglePacketTransformer;
import org.jitsi.impl.neomedia.transform.srtp.SRTCPTransformer;
import org.jitsi.impl.neomedia.transform.srtp.SRTPContextFactory;
import org.jitsi.impl.neomedia.transform.srtp.SRTPPolicy;
import org.jitsi.impl.neomedia.transform.srtp.SRTPTransformer;
import org.jitsi.service.neomedia.DtlsControl.Setup;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.util.Logger;

public class DtlsPacketTransformer extends SinglePacketTransformer {
    private static final long CONNECT_RETRY_INTERVAL = 500;
    private static final int CONNECT_TRIES = 3;
    static final int DTLS_RECORD_HEADER_LENGTH = 13;
    private static final int DTLS_TRANSPORT_RECEIVE_WAITMILLIS = -1;
    private static final Logger logger = Logger.getLogger(DtlsPacketTransformer.class);
    private final int componentID;
    /* access modifiers changed from: private */
    public Thread connectThread;
    private AbstractRTPConnector connector;
    private DatagramTransportImpl datagramTransport;
    private DTLSTransport dtlsTransport;
    private MediaType mediaType;
    private Setup setup;
    private SinglePacketTransformer srtpTransformer;
    private final DtlsTransformEngine transformEngine;

    public static boolean isDtlsRecord(byte[] buf, int off, int len) {
        if (len < 13) {
            return false;
        }
        switch (TlsUtils.readUint8(buf, off)) {
            case (short) 20:
            case WavAudioFormat.WAVE_FORMAT_DIGISTD /*21*/:
            case WavAudioFormat.WAVE_FORMAT_DIGIFIX /*22*/:
            case WASAPI.CLSCTX_ALL /*23*/:
                int major = buf[off + 1] & UnsignedUtils.MAX_UBYTE;
                int minor = buf[off + 2] & UnsignedUtils.MAX_UBYTE;
                ProtocolVersion version = null;
                if (major == ProtocolVersion.DTLSv10.getMajorVersion() && minor == ProtocolVersion.DTLSv10.getMinorVersion()) {
                    version = ProtocolVersion.DTLSv10;
                }
                if (version == null && major == ProtocolVersion.DTLSv12.getMajorVersion() && minor == ProtocolVersion.DTLSv12.getMinorVersion()) {
                    version = ProtocolVersion.DTLSv12;
                }
                if (version == null || TlsUtils.readUint16(buf, off + 11) + 13 > len) {
                    return false;
                }
                return true;
            default:
                return false;
        }
    }

    public DtlsPacketTransformer(DtlsTransformEngine transformEngine, int componentID) {
        this.transformEngine = transformEngine;
        this.componentID = componentID;
    }

    public synchronized void close() {
        setConnector(null);
        setMediaType(null);
    }

    private void closeDatagramTransport() {
        if (this.datagramTransport != null) {
            try {
                this.datagramTransport.close();
            } catch (IOException ioe) {
                logger.error("Failed to (properly) close " + this.datagramTransport.getClass(), ioe);
            }
            this.datagramTransport = null;
        }
    }

    private boolean enterRunInConnectThreadLoop(int i) {
        boolean z = false;
        if (i >= 0 && i <= 3) {
            Thread currentThread = Thread.currentThread();
            synchronized (this) {
                if (i > 0 && i < 2) {
                    boolean interrupted = false;
                    try {
                        wait(CONNECT_RETRY_INTERVAL);
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                    if (interrupted) {
                        currentThread.interrupt();
                    }
                }
                if (currentThread.equals(this.connectThread) && this.datagramTransport.equals(this.datagramTransport)) {
                    z = true;
                }
            }
        }
        return z;
    }

    /* access modifiers changed from: 0000 */
    public DtlsControlImpl getDtlsControl() {
        return getTransformEngine().getDtlsControl();
    }

    /* access modifiers changed from: 0000 */
    public DtlsTransformEngine getTransformEngine() {
        return this.transformEngine;
    }

    private boolean handleRunInConnectThreadException(IOException ioe, String msg, int i) {
        if (ioe instanceof TlsFatalAlert) {
            short alertDescription = ((TlsFatalAlert) ioe).getAlertDescription();
            if (alertDescription == (short) 10) {
                msg = msg + " Received fatal unexpected message.";
                if (i == 0 || !Thread.currentThread().equals(this.connectThread) || this.connector == null || this.mediaType == null) {
                    msg = msg + " Giving up after " + (3 - i) + " retries.";
                } else {
                    logger.error(msg + " Will retry.", ioe);
                    return true;
                }
            }
            msg = msg + " Received fatal alert " + alertDescription + ".";
        }
        logger.error(msg, ioe);
        return false;
    }

    private SinglePacketTransformer initializeSRTPTransformer(int srtpProtectionProfile, TlsContext tlsContext) {
        boolean rtcp;
        int cipher_key_length;
        int cipher_salt_length;
        int cipher;
        int auth_function;
        int auth_key_length;
        int RTP_auth_tag_length;
        int RTCP_auth_tag_length;
        SRTPContextFactory forwardSRTPContextFactory;
        SRTPContextFactory reverseSRTPContextFactory;
        switch (this.componentID) {
            case 1:
                rtcp = false;
                break;
            case 2:
                rtcp = true;
                break;
            default:
                throw new IllegalStateException("componentID");
        }
        switch (srtpProtectionProfile) {
            case 1:
                cipher_key_length = 16;
                cipher_salt_length = 14;
                cipher = 1;
                auth_function = 1;
                auth_key_length = 20;
                RTP_auth_tag_length = 10;
                RTCP_auth_tag_length = 10;
                break;
            case 2:
                cipher_key_length = 16;
                cipher_salt_length = 14;
                cipher = 1;
                auth_function = 1;
                auth_key_length = 20;
                RTCP_auth_tag_length = 10;
                RTP_auth_tag_length = 4;
                break;
            case 5:
                cipher_key_length = 0;
                cipher_salt_length = 0;
                cipher = 0;
                auth_function = 1;
                auth_key_length = 20;
                RTP_auth_tag_length = 10;
                RTCP_auth_tag_length = 10;
                break;
            case 6:
                cipher_key_length = 0;
                cipher_salt_length = 0;
                cipher = 0;
                auth_function = 1;
                auth_key_length = 20;
                RTCP_auth_tag_length = 10;
                RTP_auth_tag_length = 4;
                break;
            default:
                throw new IllegalArgumentException("srtpProtectionProfile");
        }
        Object keyingMaterial = tlsContext.exportKeyingMaterial("EXTRACTOR-dtls_srtp", null, (cipher_key_length + cipher_salt_length) * 2);
        byte[][] keyingMaterialValues = new byte[][]{new byte[cipher_key_length], new byte[cipher_key_length], new byte[cipher_salt_length], new byte[cipher_salt_length]};
        int keyingMaterialOffset = 0;
        for (Object keyingMaterialValue : keyingMaterialValues) {
            System.arraycopy(keyingMaterial, keyingMaterialOffset, keyingMaterialValue, 0, keyingMaterialValue.length);
            keyingMaterialOffset += keyingMaterialValue.length;
        }
        SRTPPolicy srtcpPolicy = new SRTPPolicy(cipher, cipher_key_length, auth_function, auth_key_length, RTCP_auth_tag_length, cipher_salt_length);
        SRTPPolicy srtpPolicy = new SRTPPolicy(cipher, cipher_key_length, auth_function, auth_key_length, RTP_auth_tag_length, cipher_salt_length);
        SRTPContextFactory clientSRTPContextFactory = new SRTPContextFactory(tlsContext instanceof TlsClientContext, client_write_SRTP_master_key, client_write_SRTP_master_salt, srtpPolicy, srtcpPolicy);
        SRTPContextFactory serverSRTPContextFactory = new SRTPContextFactory(tlsContext instanceof TlsServerContext, server_write_SRTP_master_key, server_write_SRTP_master_salt, srtpPolicy, srtcpPolicy);
        if (tlsContext instanceof TlsClientContext) {
            forwardSRTPContextFactory = clientSRTPContextFactory;
            reverseSRTPContextFactory = serverSRTPContextFactory;
        } else if (tlsContext instanceof TlsServerContext) {
            forwardSRTPContextFactory = serverSRTPContextFactory;
            reverseSRTPContextFactory = clientSRTPContextFactory;
        } else {
            throw new IllegalArgumentException("tlsContext");
        }
        if (rtcp) {
            return new SRTCPTransformer(forwardSRTPContextFactory, reverseSRTPContextFactory);
        }
        return new SRTPTransformer(forwardSRTPContextFactory, reverseSRTPContextFactory);
    }

    public RawPacket reverseTransform(RawPacket pkt) {
        byte[] buf = pkt.getBuffer();
        int off = pkt.getOffset();
        int len = pkt.getLength();
        if (isDtlsRecord(buf, off, len)) {
            boolean receive;
            synchronized (this) {
                if (this.datagramTransport == null) {
                    receive = false;
                } else {
                    this.datagramTransport.queueReceive(buf, off, len);
                    receive = true;
                }
            }
            if (!receive) {
                return null;
            }
            DTLSTransport dtlsTransport = this.dtlsTransport;
            if (dtlsTransport == null) {
                return null;
            }
            try {
                int delta = dtlsTransport.getReceiveLimit() - len;
                if (delta > 0) {
                    pkt.grow(delta);
                    buf = pkt.getBuffer();
                    off = pkt.getOffset();
                    len = pkt.getLength();
                } else if (delta < 0) {
                    pkt.shrink(-delta);
                    buf = pkt.getBuffer();
                    off = pkt.getOffset();
                    len = pkt.getLength();
                }
                int received = dtlsTransport.receive(buf, off, len, -1);
                if (received <= 0) {
                    return null;
                }
                delta = len - received;
                if (delta > 0) {
                    pkt.shrink(delta);
                }
                return null;
            } catch (IOException ioe) {
                logger.error("Failed to decode a DTLS record!", ioe);
                return null;
            }
        }
        SinglePacketTransformer srtpTransformer = this.srtpTransformer;
        if (srtpTransformer != null) {
            return srtpTransformer.reverseTransform(pkt);
        }
        return pkt;
    }

    /* access modifiers changed from: private */
    public void runInConnectThread(DTLSProtocol dtlsProtocol, TlsPeer tlsPeer, DatagramTransport datagramTransport) {
        boolean closeSRTPTransformer;
        DTLSTransport dtlsTransport = null;
        int srtpProtectionProfile = 0;
        TlsContext tlsContext = null;
        int i;
        if (dtlsProtocol instanceof DTLSClientProtocol) {
            DTLSClientProtocol dtlsClientProtocol = (DTLSClientProtocol) dtlsProtocol;
            TlsClientImpl tlsClient = (TlsClientImpl) tlsPeer;
            i = 2;
            while (i >= 0 && enterRunInConnectThreadLoop(i)) {
                try {
                    dtlsTransport = dtlsClientProtocol.connect(tlsClient, datagramTransport);
                    break;
                } catch (IOException ioe) {
                    if (!handleRunInConnectThreadException(ioe, "Failed to connect this DTLS client to a DTLS server!", i)) {
                        break;
                    }
                    i--;
                }
            }
            if (dtlsTransport != null) {
                srtpProtectionProfile = tlsClient.getChosenProtectionProfile();
                tlsContext = tlsClient.getContext();
            }
        } else if (dtlsProtocol instanceof DTLSServerProtocol) {
            DTLSServerProtocol dtlsServerProtocol = (DTLSServerProtocol) dtlsProtocol;
            TlsServerImpl tlsServer = (TlsServerImpl) tlsPeer;
            i = 2;
            while (i >= 0 && enterRunInConnectThreadLoop(i)) {
                try {
                    dtlsTransport = dtlsServerProtocol.accept(tlsServer, datagramTransport);
                    break;
                } catch (IOException ioe2) {
                    if (!handleRunInConnectThreadException(ioe2, "Failed to accept a connection from a DTLS client!", i)) {
                        break;
                    }
                    i--;
                }
            }
            if (dtlsTransport != null) {
                srtpProtectionProfile = tlsServer.getChosenProtectionProfile();
                tlsContext = tlsServer.getContext();
            }
        } else {
            throw new IllegalStateException("dtlsProtocol");
        }
        SinglePacketTransformer srtpTransformer = dtlsTransport == null ? null : initializeSRTPTransformer(srtpProtectionProfile, tlsContext);
        synchronized (this) {
            if (Thread.currentThread().equals(this.connectThread)) {
                if (datagramTransport.equals(this.datagramTransport)) {
                    this.dtlsTransport = dtlsTransport;
                    this.srtpTransformer = srtpTransformer;
                    notifyAll();
                }
            }
            closeSRTPTransformer = this.srtpTransformer != srtpTransformer;
        }
        if (closeSRTPTransformer && srtpTransformer != null) {
            srtpTransformer.close();
        }
    }

    /* access modifiers changed from: 0000 */
    public void setConnector(AbstractRTPConnector connector) {
        if (this.connector != connector) {
            this.connector = connector;
            DatagramTransportImpl datagramTransport = this.datagramTransport;
            if (datagramTransport != null) {
                datagramTransport.setConnector(connector);
            }
        }
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized void setMediaType(MediaType mediaType) {
        if (this.mediaType != mediaType) {
            if (this.mediaType != null) {
                stop();
            }
            this.mediaType = mediaType;
            if (this.mediaType != null) {
                start();
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void setSetup(Setup setup) {
        if (this.setup != setup) {
            this.setup = setup;
        }
    }

    private synchronized void start() {
        if (this.datagramTransport == null) {
            AbstractRTPConnector connector = this.connector;
            if (connector == null) {
                throw new NullPointerException("connector");
            }
            DTLSProtocol dtlsProtocolObj;
            TlsPeer tlsPeer;
            Setup setup = this.setup;
            SecureRandom secureRandom = new SecureRandom();
            if (Setup.ACTIVE.equals(setup)) {
                dtlsProtocolObj = new DTLSClientProtocol(secureRandom);
                tlsPeer = new TlsClientImpl(this);
            } else {
                dtlsProtocolObj = new DTLSServerProtocol(secureRandom);
                tlsPeer = new TlsServerImpl(this);
            }
            final DatagramTransportImpl datagramTransport = new DatagramTransportImpl(this.componentID);
            datagramTransport.setConnector(connector);
            Thread connectThread = new Thread() {
                public void run() {
                    try {
                        DtlsPacketTransformer.this.runInConnectThread(dtlsProtocolObj, tlsPeer, datagramTransport);
                    } finally {
                        if (Thread.currentThread().equals(DtlsPacketTransformer.this.connectThread)) {
                            DtlsPacketTransformer.this.connectThread = null;
                        }
                    }
                }
            };
            connectThread.setDaemon(true);
            connectThread.setName(DtlsPacketTransformer.class.getName() + ".connectThread");
            this.connectThread = connectThread;
            this.datagramTransport = datagramTransport;
            try {
                connectThread.start();
                if (!true) {
                    if (connectThread.equals(this.connectThread)) {
                        this.connectThread = null;
                    }
                    if (datagramTransport.equals(this.datagramTransport)) {
                        this.datagramTransport = null;
                    }
                }
                notifyAll();
            } catch (Throwable th) {
                if (!false) {
                    if (connectThread.equals(this.connectThread)) {
                        this.connectThread = null;
                    }
                    if (datagramTransport.equals(this.datagramTransport)) {
                        this.datagramTransport = null;
                    }
                }
            }
        } else if (this.connectThread == null && this.dtlsTransport == null) {
            logger.warn(getClass().getName() + " has been started but has failed to establish" + " the DTLS connection!");
        }
    }

    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:16:0x0020, B:25:0x0049] */
    /* JADX WARNING: Missing block: B:31:?, code skipped:
            notifyAll();
     */
    private synchronized void stop() {
        /*
        r4 = this;
        monitor-enter(r4);
        r1 = r4.connectThread;	 Catch:{ all -> 0x0050 }
        if (r1 == 0) goto L_0x0008;
    L_0x0005:
        r1 = 0;
        r4.connectThread = r1;	 Catch:{ all -> 0x0050 }
    L_0x0008:
        r1 = r4.dtlsTransport;	 Catch:{ all -> 0x0048 }
        if (r1 == 0) goto L_0x0014;
    L_0x000c:
        r1 = r4.dtlsTransport;	 Catch:{ IOException -> 0x0028 }
        r1.close();	 Catch:{ IOException -> 0x0028 }
    L_0x0011:
        r1 = 0;
        r4.dtlsTransport = r1;	 Catch:{ all -> 0x0048 }
    L_0x0014:
        r1 = r4.srtpTransformer;	 Catch:{ all -> 0x0048 }
        if (r1 == 0) goto L_0x0020;
    L_0x0018:
        r1 = r4.srtpTransformer;	 Catch:{ all -> 0x0048 }
        r1.close();	 Catch:{ all -> 0x0048 }
        r1 = 0;
        r4.srtpTransformer = r1;	 Catch:{ all -> 0x0048 }
    L_0x0020:
        r4.closeDatagramTransport();	 Catch:{ all -> 0x0053 }
        r4.notifyAll();	 Catch:{ all -> 0x0050 }
        monitor-exit(r4);
        return;
    L_0x0028:
        r0 = move-exception;
        r1 = logger;	 Catch:{ all -> 0x0048 }
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0048 }
        r2.<init>();	 Catch:{ all -> 0x0048 }
        r3 = "Failed to (properly) close ";
        r2 = r2.append(r3);	 Catch:{ all -> 0x0048 }
        r3 = r4.dtlsTransport;	 Catch:{ all -> 0x0048 }
        r3 = r3.getClass();	 Catch:{ all -> 0x0048 }
        r2 = r2.append(r3);	 Catch:{ all -> 0x0048 }
        r2 = r2.toString();	 Catch:{ all -> 0x0048 }
        r1.error(r2, r0);	 Catch:{ all -> 0x0048 }
        goto L_0x0011;
    L_0x0048:
        r1 = move-exception;
        r4.closeDatagramTransport();	 Catch:{ all -> 0x0058 }
        r4.notifyAll();	 Catch:{ all -> 0x0050 }
        throw r1;	 Catch:{ all -> 0x0050 }
    L_0x0050:
        r1 = move-exception;
        monitor-exit(r4);
        throw r1;
    L_0x0053:
        r1 = move-exception;
        r4.notifyAll();	 Catch:{ all -> 0x0050 }
        throw r1;	 Catch:{ all -> 0x0050 }
    L_0x0058:
        r1 = move-exception;
        r4.notifyAll();	 Catch:{ all -> 0x0050 }
        throw r1;	 Catch:{ all -> 0x0050 }
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.transform.dtls.DtlsPacketTransformer.stop():void");
    }

    public RawPacket transform(RawPacket pkt) {
        if (isDtlsRecord(pkt.getBuffer(), pkt.getOffset(), pkt.getLength())) {
            return pkt;
        }
        SinglePacketTransformer srtpTransformer = this.srtpTransformer;
        if (srtpTransformer != null) {
            return srtpTransformer.transform(pkt);
        }
        return pkt;
    }
}
