package org.jitsi.impl.neomedia.transform.dtls;

import java.io.IOException;
import java.util.Hashtable;
import org.jitsi.bouncycastle.crypto.tls.Certificate;
import org.jitsi.bouncycastle.crypto.tls.CertificateRequest;
import org.jitsi.bouncycastle.crypto.tls.DefaultTlsServer;
import org.jitsi.bouncycastle.crypto.tls.DefaultTlsSignerCredentials;
import org.jitsi.bouncycastle.crypto.tls.ProtocolVersion;
import org.jitsi.bouncycastle.crypto.tls.TlsContext;
import org.jitsi.bouncycastle.crypto.tls.TlsFatalAlert;
import org.jitsi.bouncycastle.crypto.tls.TlsSRTPUtils;
import org.jitsi.bouncycastle.crypto.tls.TlsServerContext;
import org.jitsi.bouncycastle.crypto.tls.TlsSignerCredentials;
import org.jitsi.bouncycastle.crypto.tls.UseSRTPData;
import org.jitsi.util.Logger;

public class TlsServerImpl extends DefaultTlsServer {
    private static final Logger logger = Logger.getLogger(TlsServerImpl.class);
    private final CertificateRequest certificateRequest = new CertificateRequest(new short[]{(short) 1}, null);
    private int chosenProtectionProfile;
    private final DtlsPacketTransformer packetTransformer;
    private TlsSignerCredentials rsaSignerCredentials;

    public TlsServerImpl(DtlsPacketTransformer packetTransformer) {
        this.packetTransformer = packetTransformer;
    }

    public CertificateRequest getCertificateRequest() {
        return this.certificateRequest;
    }

    /* access modifiers changed from: 0000 */
    public int getChosenProtectionProfile() {
        return this.chosenProtectionProfile;
    }

    /* access modifiers changed from: 0000 */
    public TlsContext getContext() {
        return this.context;
    }

    private DtlsControlImpl getDtlsControl() {
        return this.packetTransformer.getDtlsControl();
    }

    /* access modifiers changed from: protected */
    public ProtocolVersion getMaximumVersion() {
        return ProtocolVersion.DTLSv10;
    }

    /* access modifiers changed from: protected */
    public ProtocolVersion getMinimumVersion() {
        return ProtocolVersion.DTLSv10;
    }

    /* access modifiers changed from: protected */
    public TlsSignerCredentials getRSASignerCredentials() throws IOException {
        if (this.rsaSignerCredentials == null) {
            DtlsControlImpl dtlsControl = getDtlsControl();
            this.rsaSignerCredentials = new DefaultTlsSignerCredentials(this.context, dtlsControl.getCertificate(), dtlsControl.getKeyPair().getPrivate());
        }
        return this.rsaSignerCredentials;
    }

    public Hashtable getServerExtensions() throws IOException {
        Hashtable serverExtensions = TlsServerImpl.super.getServerExtensions();
        if (TlsSRTPUtils.getUseSRTPExtension(serverExtensions) == null) {
            if (serverExtensions == null) {
                serverExtensions = new Hashtable();
            }
            UseSRTPData useSRTPData = TlsSRTPUtils.getUseSRTPExtension(this.clientExtensions);
            int chosenProtectionProfile = DtlsControlImpl.chooseSRTPProtectionProfile(useSRTPData.getProtectionProfiles());
            if (chosenProtectionProfile == 0) {
                TlsFatalAlert tfa = new TlsFatalAlert((short) 80);
                logger.error("No chosen SRTP protection profile!", tfa);
                throw tfa;
            }
            TlsSRTPUtils.addUseSRTPExtension(serverExtensions, new UseSRTPData(new int[]{chosenProtectionProfile}, useSRTPData.getMki()));
            this.chosenProtectionProfile = chosenProtectionProfile;
        }
        return serverExtensions;
    }

    public void init(TlsServerContext context) {
        TlsServerImpl.super.init(context);
    }

    public void notifyClientCertificate(Certificate clientCertificate) throws IOException {
        try {
            getDtlsControl().verifyAndValidateCertificate(clientCertificate);
        } catch (Exception e) {
            logger.error("Failed to verify and/or validate client certificate!", e);
            if (e instanceof IOException) {
                throw ((IOException) e);
            }
            throw new IOException(e);
        }
    }

    public void processClientExtensions(Hashtable clientExtensions) throws IOException {
        UseSRTPData useSRTPData = TlsSRTPUtils.getUseSRTPExtension(clientExtensions);
        if (useSRTPData == null) {
            String msg = "DTLS extended client hello does not include the use_srtp extension!";
            IOException ioe = new IOException(msg);
            logger.error(msg, ioe);
            throw ioe;
        } else if (DtlsControlImpl.chooseSRTPProtectionProfile(useSRTPData.getProtectionProfiles()) == 0) {
            TlsFatalAlert tfa = new TlsFatalAlert((short) 47);
            logger.error("No chosen SRTP protection profile!", tfa);
            throw tfa;
        } else {
            TlsServerImpl.super.processClientExtensions(clientExtensions);
        }
    }
}
