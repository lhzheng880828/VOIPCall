package org.jitsi.impl.neomedia.transform.dtls;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import org.jitsi.bouncycastle.crypto.tls.Certificate;
import org.jitsi.bouncycastle.crypto.tls.CertificateRequest;
import org.jitsi.bouncycastle.crypto.tls.DefaultTlsClient;
import org.jitsi.bouncycastle.crypto.tls.DefaultTlsSignerCredentials;
import org.jitsi.bouncycastle.crypto.tls.ProtocolVersion;
import org.jitsi.bouncycastle.crypto.tls.TlsAuthentication;
import org.jitsi.bouncycastle.crypto.tls.TlsClientContext;
import org.jitsi.bouncycastle.crypto.tls.TlsContext;
import org.jitsi.bouncycastle.crypto.tls.TlsCredentials;
import org.jitsi.bouncycastle.crypto.tls.TlsFatalAlert;
import org.jitsi.bouncycastle.crypto.tls.TlsSRTPUtils;
import org.jitsi.bouncycastle.crypto.tls.TlsUtils;
import org.jitsi.bouncycastle.crypto.tls.UseSRTPData;
import org.jitsi.util.Logger;

public class TlsClientImpl extends DefaultTlsClient {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(TlsClientImpl.class);
    private final TlsAuthentication authentication = new TlsAuthenticationImpl();
    private int chosenProtectionProfile;
    private final byte[] mki = TlsUtils.EMPTY_BYTES;
    private final DtlsPacketTransformer packetTransformer;

    private class TlsAuthenticationImpl implements TlsAuthentication {
        private TlsCredentials clientCredentials;

        private TlsAuthenticationImpl() {
        }

        public TlsCredentials getClientCredentials(CertificateRequest certificateRequest) throws IOException {
            if (this.clientCredentials == null) {
                DtlsControlImpl dtlsControl = TlsClientImpl.this.getDtlsControl();
                this.clientCredentials = new DefaultTlsSignerCredentials(TlsClientImpl.this.context, dtlsControl.getCertificate(), dtlsControl.getKeyPair().getPrivate());
            }
            return this.clientCredentials;
        }

        public void notifyServerCertificate(Certificate serverCertificate) throws IOException {
            try {
                TlsClientImpl.this.getDtlsControl().verifyAndValidateCertificate(serverCertificate);
            } catch (Exception e) {
                TlsClientImpl.logger.error("Failed to verify and/or validate server certificate!", e);
                if (e instanceof IOException) {
                    throw ((IOException) e);
                }
                throw new IOException(e);
            }
        }
    }

    public TlsClientImpl(DtlsPacketTransformer packetTransformer) {
        this.packetTransformer = packetTransformer;
    }

    public synchronized TlsAuthentication getAuthentication() throws IOException {
        return this.authentication;
    }

    /* access modifiers changed from: 0000 */
    public int getChosenProtectionProfile() {
        return this.chosenProtectionProfile;
    }

    public Hashtable getClientExtensions() throws IOException {
        Hashtable clientExtensions = TlsClientImpl.super.getClientExtensions();
        if (TlsSRTPUtils.getUseSRTPExtension(clientExtensions) == null) {
            if (clientExtensions == null) {
                clientExtensions = new Hashtable();
            }
            TlsSRTPUtils.addUseSRTPExtension(clientExtensions, new UseSRTPData(DtlsControlImpl.SRTP_PROTECTION_PROFILES, this.mki));
        }
        return clientExtensions;
    }

    public ProtocolVersion getClientVersion() {
        return ProtocolVersion.DTLSv10;
    }

    /* access modifiers changed from: 0000 */
    public TlsContext getContext() {
        return this.context;
    }

    /* access modifiers changed from: private */
    public DtlsControlImpl getDtlsControl() {
        return this.packetTransformer.getDtlsControl();
    }

    public ProtocolVersion getMinimumVersion() {
        return ProtocolVersion.DTLSv10;
    }

    public void init(TlsClientContext context) {
        TlsClientImpl.super.init(context);
    }

    public void processServerExtensions(Hashtable serverExtensions) throws IOException {
        int chosenProtectionProfile = 0;
        UseSRTPData useSRTPData = TlsSRTPUtils.getUseSRTPExtension(serverExtensions);
        if (useSRTPData == null) {
            String msg = "DTLS extended server hello does not include the use_srtp extension!";
            IOException ioe = new IOException(msg);
            logger.error(msg, ioe);
            throw ioe;
        }
        if (useSRTPData.getProtectionProfiles().length == 1) {
            chosenProtectionProfile = DtlsControlImpl.chooseSRTPProtectionProfile(useSRTPData.getProtectionProfiles()[0]);
        }
        TlsFatalAlert tfa;
        if (chosenProtectionProfile == 0) {
            tfa = new TlsFatalAlert((short) 47);
            logger.error("No chosen SRTP protection profile!", tfa);
            throw tfa;
        } else if (Arrays.equals(useSRTPData.getMki(), this.mki)) {
            TlsClientImpl.super.processServerExtensions(serverExtensions);
            this.chosenProtectionProfile = chosenProtectionProfile;
        } else {
            tfa = new TlsFatalAlert((short) 47);
            logger.error("Server's MKI does not match the one offered by this client!", tfa);
            throw tfa;
        }
    }
}
