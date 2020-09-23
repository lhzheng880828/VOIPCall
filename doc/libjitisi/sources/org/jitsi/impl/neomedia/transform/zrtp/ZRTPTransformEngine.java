package org.jitsi.impl.neomedia.transform.zrtp;

import gnu.java.zrtp.ZRtp;
import gnu.java.zrtp.ZrtpCallback;
import gnu.java.zrtp.ZrtpCallback.EnableSecurity;
import gnu.java.zrtp.ZrtpCallback.Role;
import gnu.java.zrtp.ZrtpCodes.InfoEnrollment;
import gnu.java.zrtp.ZrtpCodes.MessageSeverity;
import gnu.java.zrtp.ZrtpCodes.WarningCodes;
import gnu.java.zrtp.ZrtpConfigure;
import gnu.java.zrtp.ZrtpConstants.SupportedAuthAlgos;
import gnu.java.zrtp.ZrtpConstants.SupportedSASTypes;
import gnu.java.zrtp.ZrtpConstants.SupportedSymAlgos;
import gnu.java.zrtp.ZrtpSrtpSecrets;
import gnu.java.zrtp.ZrtpStateClass.ZrtpStates;
import gnu.java.zrtp.utils.ZrtpFortuna;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Timer;
import java.util.TimerTask;
import org.jitsi.impl.neomedia.AbstractRTPConnector;
import org.jitsi.impl.neomedia.RawPacket;
import org.jitsi.impl.neomedia.transform.PacketTransformer;
import org.jitsi.impl.neomedia.transform.SinglePacketTransformer;
import org.jitsi.impl.neomedia.transform.srtp.SRTCPTransformer;
import org.jitsi.impl.neomedia.transform.srtp.SRTPContextFactory;
import org.jitsi.impl.neomedia.transform.srtp.SRTPPolicy;
import org.jitsi.impl.neomedia.transform.srtp.SRTPTransformer;
import org.jitsi.service.neomedia.SrtpControl.TransformEngine;
import org.jitsi.util.Logger;

public class ZRTPTransformEngine extends SinglePacketTransformer implements TransformEngine, ZrtpCallback {
    protected static final int ZRTP_PACKET_HEADER = 12;
    private static final Logger logger = Logger.getLogger(ZRTPTransformEngine.class);
    private String clientIdString = "GNU ZRTP4J 3.1.0";
    private boolean enableParanoidMode = false;
    private boolean enableZrtp = false;
    private boolean mitmMode = false;
    /* access modifiers changed from: private */
    public boolean muted = false;
    private int ownSSRC = 0;
    private SecurityEventManager securityEventManager = null;
    private short senderZrtpSeqNo = (short) 0;
    private SRTPTransformer srtpInTransformer = null;
    private SRTPTransformer srtpOutTransformer = null;
    private boolean started = false;
    private TimeoutProvider timeoutProvider = null;
    private ZRTCPTransformer zrtcpTransformer = null;
    private AbstractRTPConnector zrtpConnector = null;
    private ZRtp zrtpEngine = null;
    private long zrtpUnprotect;

    private class TimeoutProvider extends Thread {
        private boolean newTask = false;
        private long nextDelay = 0;
        private boolean stop = false;
        private final Object sync = new Object();

        public TimeoutProvider(String name) {
            super(name);
        }

        public synchronized void requestTimeout(long delay) {
            synchronized (this.sync) {
                this.nextDelay = delay;
                this.newTask = true;
                this.sync.notifyAll();
            }
        }

        public void stopRun() {
            synchronized (this.sync) {
                this.stop = true;
                this.sync.notifyAll();
            }
        }

        public void cancelRequest() {
            synchronized (this.sync) {
                this.newTask = false;
                this.sync.notifyAll();
            }
        }

        public void run() {
            while (!this.stop) {
                synchronized (this.sync) {
                    while (!this.newTask && !this.stop) {
                        try {
                            this.sync.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
                long currentTime = System.currentTimeMillis();
                long endTime = currentTime + this.nextDelay;
                synchronized (this.sync) {
                    while (currentTime < endTime) {
                        if (this.newTask && !this.stop) {
                            try {
                                this.sync.wait(endTime - currentTime);
                            } catch (InterruptedException e2) {
                            }
                            currentTime = System.currentTimeMillis();
                        }
                    }
                }
                if (this.newTask && !this.stop) {
                    this.newTask = false;
                    ZRTPTransformEngine.this.handleTimeout();
                }
            }
        }
    }

    public ZRTPTransformEngine() {
        byte[] random = new byte[2];
        ZrtpFortuna.getInstance().nextBytes(random);
        this.senderZrtpSeqNo = (short) random[0];
        this.senderZrtpSeqNo = (short) (this.senderZrtpSeqNo | (random[1] << 8));
        this.senderZrtpSeqNo = (short) (this.senderZrtpSeqNo & 32767);
    }

    public ZRTCPTransformer getRTCPTransformer() {
        if (this.zrtcpTransformer == null) {
            this.zrtcpTransformer = new ZRTCPTransformer();
        }
        return this.zrtcpTransformer;
    }

    public PacketTransformer getRTPTransformer() {
        return this;
    }

    public boolean initialize(String zidFilename, ZrtpConfigure config) {
        return initialize(zidFilename, true, config);
    }

    public boolean initialize(String zidFilename, boolean autoEnable) {
        return initialize(zidFilename, autoEnable, null);
    }

    public boolean initialize(String zidFilename) {
        return initialize(zidFilename, true, null);
    }

    /* JADX WARNING: Missing block: B:25:0x004f, code skipped:
            return r1;
     */
    public synchronized boolean initialize(java.lang.String r16, boolean r17, gnu.java.zrtp.ZrtpConfigure r18) {
        /*
        r15 = this;
        monitor-enter(r15);
        r11 = 0;
        r10 = org.jitsi.service.libjitsi.LibJitsi.getFileAccessService();	 Catch:{ all -> 0x0059 }
        if (r10 == 0) goto L_0x0010;
    L_0x0008:
        r1 = org.jitsi.service.fileaccess.FileCategory.PROFILE;	 Catch:{ Exception -> 0x0050 }
        r0 = r16;
        r11 = r10.getPrivatePersistentFile(r0, r1);	 Catch:{ Exception -> 0x0050 }
    L_0x0010:
        r14 = 0;
        if (r11 == 0) goto L_0x0017;
    L_0x0013:
        r14 = r11.getAbsolutePath();	 Catch:{ SecurityException -> 0x005c }
    L_0x0017:
        r13 = gnu.java.zrtp.zidfile.ZidFile.getInstance();	 Catch:{ all -> 0x0059 }
        r1 = r13.isOpen();	 Catch:{ all -> 0x0059 }
        if (r1 != 0) goto L_0x0081;
    L_0x0021:
        if (r14 != 0) goto L_0x0047;
    L_0x0023:
        r1 = "HOME";
        r12 = java.lang.System.getenv(r1);	 Catch:{ all -> 0x0059 }
        if (r12 == 0) goto L_0x0032;
    L_0x002b:
        r1 = r12.length();	 Catch:{ all -> 0x0059 }
        r2 = 1;
        if (r1 >= r2) goto L_0x006d;
    L_0x0032:
        r7 = "";
    L_0x0034:
        r1 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0059 }
        r1.<init>();	 Catch:{ all -> 0x0059 }
        r1 = r1.append(r7);	 Catch:{ all -> 0x0059 }
        r2 = ".GNUZRTP4J.zid";
        r1 = r1.append(r2);	 Catch:{ all -> 0x0059 }
        r14 = r1.toString();	 Catch:{ all -> 0x0059 }
    L_0x0047:
        r1 = r13.open(r14);	 Catch:{ all -> 0x0059 }
        if (r1 >= 0) goto L_0x0081;
    L_0x004d:
        r1 = 0;
    L_0x004e:
        monitor-exit(r15);
        return r1;
    L_0x0050:
        r9 = move-exception;
        r1 = logger;	 Catch:{ all -> 0x0059 }
        r2 = "Failed to create the zid file.";
        r1.warn(r2);	 Catch:{ all -> 0x0059 }
        goto L_0x0010;
    L_0x0059:
        r1 = move-exception;
    L_0x005a:
        monitor-exit(r15);
        throw r1;
    L_0x005c:
        r9 = move-exception;
        r1 = logger;	 Catch:{ all -> 0x0059 }
        r1 = r1.isDebugEnabled();	 Catch:{ all -> 0x0059 }
        if (r1 == 0) goto L_0x0017;
    L_0x0065:
        r1 = logger;	 Catch:{ all -> 0x0059 }
        r2 = "Failed to obtain the absolute path of the zid file.";
        r1.debug(r2, r9);	 Catch:{ all -> 0x0059 }
        goto L_0x0017;
    L_0x006d:
        r1 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0059 }
        r1.<init>();	 Catch:{ all -> 0x0059 }
        r1 = r1.append(r12);	 Catch:{ all -> 0x0059 }
        r2 = "/";
        r1 = r1.append(r2);	 Catch:{ all -> 0x0059 }
        r7 = r1.toString();	 Catch:{ all -> 0x0059 }
        goto L_0x0034;
    L_0x0081:
        if (r18 != 0) goto L_0x008d;
    L_0x0083:
        r8 = new gnu.java.zrtp.ZrtpConfigure;	 Catch:{ all -> 0x0059 }
        r8.<init>();	 Catch:{ all -> 0x0059 }
        r8.setStandardConfig();	 Catch:{ all -> 0x00c2 }
        r18 = r8;
    L_0x008d:
        r1 = r15.enableParanoidMode;	 Catch:{ all -> 0x0059 }
        if (r1 == 0) goto L_0x0098;
    L_0x0091:
        r1 = r15.enableParanoidMode;	 Catch:{ all -> 0x0059 }
        r0 = r18;
        r0.setParanoidMode(r1);	 Catch:{ all -> 0x0059 }
    L_0x0098:
        r1 = new gnu.java.zrtp.ZRtp;	 Catch:{ all -> 0x0059 }
        r2 = r13.getZid();	 Catch:{ all -> 0x0059 }
        r4 = r15.clientIdString;	 Catch:{ all -> 0x0059 }
        r6 = r15.mitmMode;	 Catch:{ all -> 0x0059 }
        r3 = r15;
        r5 = r18;
        r1.<init>(r2, r3, r4, r5, r6);	 Catch:{ all -> 0x0059 }
        r15.zrtpEngine = r1;	 Catch:{ all -> 0x0059 }
        r1 = r15.timeoutProvider;	 Catch:{ all -> 0x0059 }
        if (r1 != 0) goto L_0x00bc;
    L_0x00ae:
        r1 = new org.jitsi.impl.neomedia.transform.zrtp.ZRTPTransformEngine$TimeoutProvider;	 Catch:{ all -> 0x0059 }
        r2 = "ZRTP";
        r1.m2682init(r2);	 Catch:{ all -> 0x0059 }
        r15.timeoutProvider = r1;	 Catch:{ all -> 0x0059 }
        r1 = r15.timeoutProvider;	 Catch:{ all -> 0x0059 }
        r1.start();	 Catch:{ all -> 0x0059 }
    L_0x00bc:
        r0 = r17;
        r15.enableZrtp = r0;	 Catch:{ all -> 0x0059 }
        r1 = 1;
        goto L_0x004e;
    L_0x00c2:
        r1 = move-exception;
        r18 = r8;
        goto L_0x005a;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.transform.zrtp.ZRTPTransformEngine.initialize(java.lang.String, boolean, gnu.java.zrtp.ZrtpConfigure):boolean");
    }

    public void setStartMuted(boolean startMuted) {
        this.muted = startMuted;
        if (startMuted) {
            new Timer().schedule(new TimerTask() {
                public void run() {
                    ZRTPTransformEngine.this.muted = false;
                }
            }, 1500);
        }
    }

    public boolean getSecureCommunicationStatus() {
        return (this.srtpInTransformer == null && this.srtpOutTransformer == null) ? false : true;
    }

    public void startZrtp() {
        if (this.zrtpEngine != null) {
            this.zrtpEngine.startZrtpEngine();
            this.started = true;
            this.securityEventManager.securityNegotiationStarted();
        }
    }

    public void close() {
        stopZrtp();
    }

    public void stopZrtp() {
        if (this.zrtpEngine != null) {
            if (this.zrtpUnprotect < 10) {
                this.zrtpEngine.setRs2Valid();
            }
            this.zrtpEngine.stopZrtp();
            this.zrtpEngine = null;
            this.started = false;
        }
        if (this.srtpOutTransformer != null) {
            this.srtpOutTransformer.close();
            this.srtpOutTransformer = null;
        }
        if (this.srtpInTransformer != null) {
            this.srtpInTransformer.close();
            this.srtpOutTransformer = null;
        }
        if (this.zrtcpTransformer != null) {
            this.zrtcpTransformer.close();
            this.zrtcpTransformer = null;
        }
    }

    public void cleanup() {
        stopZrtp();
        if (this.timeoutProvider != null) {
            this.timeoutProvider.stopRun();
            this.timeoutProvider = null;
        }
    }

    public void setOwnSSRC(long ssrc) {
        this.ownSSRC = (int) (-1 & ssrc);
    }

    public RawPacket transform(RawPacket pkt) {
        if (ZrtpRawPacket.isZrtpData(pkt)) {
            return pkt;
        }
        if (this.enableZrtp && this.ownSSRC == 0) {
            this.ownSSRC = pkt.getSSRC();
        }
        if (this.srtpOutTransformer != null) {
            return this.srtpOutTransformer.transform(pkt);
        }
        return pkt;
    }

    public RawPacket reverseTransform(RawPacket pkt) {
        if (!(this.started || !this.enableZrtp || this.ownSSRC == 0)) {
            startZrtp();
        }
        if (ZrtpRawPacket.isZrtpData(pkt)) {
            if (this.enableZrtp && this.started) {
                ZrtpRawPacket zPkt = new ZrtpRawPacket(pkt);
                if (!zPkt.checkCrc()) {
                    this.securityEventManager.showMessage(MessageSeverity.Warning, EnumSet.of(WarningCodes.WarningCRCmismatch));
                } else if (zPkt.hasMagic()) {
                    this.zrtpEngine.processZrtpMessage(zPkt.readRegion((zPkt.getHeaderLength() - zPkt.getExtensionLength()) - 4, (zPkt.getExtensionLength() + 4) + zPkt.getPayloadLength()), zPkt.getSSRC());
                }
            }
            return null;
        } else if (this.srtpInTransformer == null) {
            return this.muted ? null : pkt;
        } else {
            RawPacket pkt2 = this.srtpInTransformer.reverseTransform(pkt);
            if (pkt2 != null && this.started && this.zrtpEngine.inState(ZrtpStates.WaitConfAck)) {
                this.zrtpEngine.conf2AckSecure();
            }
            if (pkt2 != null) {
                this.zrtpUnprotect++;
            }
            return pkt2;
        }
    }

    public boolean sendDataZRTP(byte[] data) {
        byte[] tmp = new byte[(data.length + 12)];
        System.arraycopy(data, 0, tmp, 12, data.length);
        ZrtpRawPacket packet = new ZrtpRawPacket(tmp, 0, tmp.length);
        packet.setSSRC(this.ownSSRC);
        short s = this.senderZrtpSeqNo;
        this.senderZrtpSeqNo = (short) (s + 1);
        packet.setSeqNum(s);
        packet.setCrc();
        try {
            this.zrtpConnector.getDataOutputStream().write(packet.getBuffer(), packet.getOffset(), packet.getLength());
            return true;
        } catch (IOException e) {
            logger.warn("Failed to send ZRTP data.");
            if (!logger.isDebugEnabled()) {
                return false;
            }
            logger.debug("Failed to send ZRTP data.", e);
            return false;
        }
    }

    public boolean srtpSecretsReady(ZrtpSrtpSecrets secrets, EnableSecurity part) {
        int cipher = 0;
        int authn = 0;
        int authKeyLen = 0;
        if (secrets.getAuthAlgorithm() == SupportedAuthAlgos.HS) {
            authn = 1;
            authKeyLen = 20;
        } else if (secrets.getAuthAlgorithm() == SupportedAuthAlgos.SK) {
            authn = 2;
            authKeyLen = 32;
        }
        if (secrets.getSymEncAlgorithm() == SupportedSymAlgos.AES) {
            cipher = 1;
        } else if (secrets.getSymEncAlgorithm() == SupportedSymAlgos.TwoFish) {
            cipher = 3;
        }
        SRTPContextFactory engine;
        SRTPPolicy sRTPPolicy;
        if (part == EnableSecurity.ForSender) {
            if (secrets.getRole() == Role.Initiator) {
                SRTPPolicy srtpPolicy = new SRTPPolicy(cipher, secrets.getInitKeyLen() / 8, authn, authKeyLen, secrets.getSrtpAuthTagLen() / 8, secrets.getInitSaltLen() / 8);
                engine = new SRTPContextFactory(true, secrets.getKeyInitiator(), secrets.getSaltInitiator(), srtpPolicy, srtpPolicy);
                this.srtpOutTransformer = new SRTPTransformer(engine);
                getRTCPTransformer().setSrtcpOut(new SRTCPTransformer(engine));
            } else {
                sRTPPolicy = new SRTPPolicy(cipher, secrets.getRespKeyLen() / 8, authn, authKeyLen, secrets.getSrtpAuthTagLen() / 8, secrets.getRespSaltLen() / 8);
                engine = new SRTPContextFactory(true, secrets.getKeyResponder(), secrets.getSaltResponder(), sRTPPolicy, sRTPPolicy);
                this.srtpOutTransformer = new SRTPTransformer(engine);
                getRTCPTransformer().setSrtcpOut(new SRTCPTransformer(engine));
            }
        } else if (part == EnableSecurity.ForReceiver) {
            if (secrets.getRole() == Role.Initiator) {
                sRTPPolicy = new SRTPPolicy(cipher, secrets.getRespKeyLen() / 8, authn, authKeyLen, secrets.getSrtpAuthTagLen() / 8, secrets.getRespSaltLen() / 8);
                engine = new SRTPContextFactory(false, secrets.getKeyResponder(), secrets.getSaltResponder(), sRTPPolicy, sRTPPolicy);
                this.srtpInTransformer = new SRTPTransformer(engine);
                getRTCPTransformer().setSrtcpIn(new SRTCPTransformer(engine));
                this.muted = false;
            } else {
                sRTPPolicy = new SRTPPolicy(cipher, secrets.getInitKeyLen() / 8, authn, authKeyLen, secrets.getSrtpAuthTagLen() / 8, secrets.getInitSaltLen() / 8);
                engine = new SRTPContextFactory(false, secrets.getKeyInitiator(), secrets.getSaltInitiator(), sRTPPolicy, sRTPPolicy);
                this.srtpInTransformer = new SRTPTransformer(engine);
                getRTCPTransformer().setSrtcpIn(new SRTCPTransformer(engine));
                this.muted = false;
            }
        }
        return true;
    }

    public void srtpSecretsOn(String c, String s, boolean verified) {
        if (this.securityEventManager != null) {
            this.securityEventManager.secureOn(c);
            if (s != null || !verified) {
                this.securityEventManager.showSAS(s, verified);
            }
        }
    }

    public void srtpSecretsOff(EnableSecurity part) {
        if (part == EnableSecurity.ForSender) {
            if (this.srtpOutTransformer != null) {
                this.srtpOutTransformer.close();
                this.srtpOutTransformer = null;
            }
        } else if (part == EnableSecurity.ForReceiver && this.srtpInTransformer != null) {
            this.srtpInTransformer.close();
            this.srtpInTransformer = null;
        }
        if (this.securityEventManager != null) {
            this.securityEventManager.secureOff();
        }
    }

    public int activateTimer(int time) {
        if (this.timeoutProvider != null) {
            this.timeoutProvider.requestTimeout((long) time);
        }
        return 1;
    }

    public int cancelTimer() {
        if (this.timeoutProvider != null) {
            this.timeoutProvider.cancelRequest();
        }
        return 1;
    }

    public void handleTimeout() {
        if (this.zrtpEngine != null) {
            this.zrtpEngine.processTimeout();
        }
    }

    public void sendInfo(MessageSeverity severity, EnumSet<?> subCode) {
        if (this.securityEventManager != null) {
            this.securityEventManager.showMessage(severity, subCode);
        }
    }

    public void zrtpNegotiationFailed(MessageSeverity severity, EnumSet<?> subCode) {
        if (this.securityEventManager != null) {
            this.securityEventManager.zrtpNegotiationFailed(severity, subCode);
        }
    }

    public void zrtpNotSuppOther() {
        if (this.securityEventManager != null) {
            this.securityEventManager.zrtpNotSuppOther();
        }
    }

    public void zrtpAskEnrollment(InfoEnrollment info) {
        if (this.securityEventManager != null) {
            this.securityEventManager.zrtpAskEnrollment(info);
        }
    }

    public void zrtpInformEnrollment(InfoEnrollment info) {
        if (this.securityEventManager != null) {
            this.securityEventManager.zrtpInformEnrollment(info);
        }
    }

    public void signSAS(byte[] sasHash) {
        if (this.securityEventManager != null) {
            this.securityEventManager.signSAS(sasHash);
        }
    }

    public boolean checkSASSignature(byte[] sasHash) {
        return this.securityEventManager != null ? this.securityEventManager.checkSASSignature(sasHash) : false;
    }

    public void setEnableZrtp(boolean onOff) {
        this.enableZrtp = onOff;
    }

    public boolean isEnableZrtp() {
        return this.enableZrtp;
    }

    public void SASVerified() {
        if (this.zrtpEngine != null) {
            this.zrtpEngine.SASVerified();
        }
        if (this.securityEventManager != null) {
            this.securityEventManager.setSASVerified(true);
        }
    }

    public void resetSASVerified() {
        if (this.zrtpEngine != null) {
            this.zrtpEngine.resetSASVerified();
        }
        if (this.securityEventManager != null) {
            this.securityEventManager.setSASVerified(false);
        }
    }

    public void requestGoClear() {
    }

    public void requestGoSecure() {
    }

    public void setAuxSecret(byte[] data) {
        if (this.zrtpEngine != null) {
            this.zrtpEngine.setAuxSecret(data);
        }
    }

    public void setClientId(String id) {
        this.clientIdString = id;
    }

    public String getHelloHash(int index) {
        return this.zrtpEngine != null ? this.zrtpEngine.getHelloHash(index) : new String();
    }

    public String[] getHelloHashSep(int index) {
        return this.zrtpEngine != null ? this.zrtpEngine.getHelloHashSep(index) : null;
    }

    public String getPeerHelloHash() {
        return this.zrtpEngine != null ? this.zrtpEngine.getPeerHelloHash() : new String();
    }

    public byte[] getMultiStrParams() {
        return this.zrtpEngine != null ? this.zrtpEngine.getMultiStrParams() : new byte[0];
    }

    public void setMultiStrParams(byte[] parameters) {
        if (this.zrtpEngine != null) {
            this.zrtpEngine.setMultiStrParams(parameters);
        }
    }

    public boolean isMultiStream() {
        return this.zrtpEngine != null ? this.zrtpEngine.isMultiStream() : false;
    }

    public void acceptEnrollment(boolean accepted) {
        if (this.zrtpEngine != null) {
            this.zrtpEngine.acceptEnrollment(accepted);
        }
    }

    public SupportedSASTypes getSasType() {
        return this.zrtpEngine != null ? this.zrtpEngine.getSasType() : null;
    }

    public byte[] getSasHash() {
        return this.zrtpEngine != null ? this.zrtpEngine.getSasHash() : null;
    }

    public boolean sendSASRelayPacket(byte[] sh, SupportedSASTypes render) {
        return this.zrtpEngine != null ? this.zrtpEngine.sendSASRelayPacket(sh, render) : false;
    }

    public boolean isMitmMode() {
        return this.mitmMode;
    }

    public void setMitmMode(boolean mitmMode) {
        this.mitmMode = mitmMode;
    }

    public void setParanoidMode(boolean yesNo) {
        this.enableParanoidMode = yesNo;
    }

    public boolean isParanoidMode() {
        return this.enableParanoidMode;
    }

    public boolean isEnrollmentMode() {
        return this.zrtpEngine != null ? this.zrtpEngine.isEnrollmentMode() : false;
    }

    public void setEnrollmentMode(boolean enrollmentMode) {
        if (this.zrtpEngine != null) {
            this.zrtpEngine.setEnrollmentMode(enrollmentMode);
        }
    }

    public boolean setSignatureData(byte[] data) {
        return this.zrtpEngine != null ? this.zrtpEngine.setSignatureData(data) : false;
    }

    public byte[] getSignatureData() {
        return this.zrtpEngine != null ? this.zrtpEngine.getSignatureData() : new byte[0];
    }

    public int getSignatureLength() {
        return this.zrtpEngine != null ? this.zrtpEngine.getSignatureLength() : 0;
    }

    public void handleGoClear() {
        this.securityEventManager.confirmGoClear();
    }

    public void setConnector(AbstractRTPConnector connector) {
        this.zrtpConnector = connector;
    }

    public void setUserCallback(SecurityEventManager ub) {
        this.securityEventManager = ub;
    }

    public boolean isStarted() {
        return this.started;
    }

    public SecurityEventManager getUserCallback() {
        return this.securityEventManager;
    }

    public byte[] getPeerZid() {
        return this.zrtpEngine != null ? this.zrtpEngine.getPeerZid() : null;
    }

    public int getNumberSupportedVersions() {
        return this.zrtpEngine != null ? this.zrtpEngine.getNumberSupportedVersions() : 0;
    }

    public int getCurrentProtocolVersion() {
        return this.zrtpEngine != null ? this.zrtpEngine.getCurrentProtocolVersion() : 0;
    }
}
