package org.jitsi.impl.neomedia.transform.dtls;

import com.lti.utils.UnsignedUtils;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Map;
import org.jitsi.bouncycastle.asn1.x500.X500Name;
import org.jitsi.bouncycastle.asn1.x500.X500NameBuilder;
import org.jitsi.bouncycastle.asn1.x500.style.BCStyle;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.cert.X509v3CertificateBuilder;
import org.jitsi.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.jitsi.bouncycastle.crypto.Digest;
import org.jitsi.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.jitsi.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.jitsi.bouncycastle.crypto.tls.Certificate;
import org.jitsi.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.jitsi.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.jitsi.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.jitsi.bouncycastle.operator.bc.BcDefaultDigestProvider;
import org.jitsi.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.jitsi.impl.neomedia.AbstractRTPConnector;
import org.jitsi.service.neomedia.AbstractSrtpControl;
import org.jitsi.service.neomedia.DtlsControl;
import org.jitsi.service.neomedia.DtlsControl.Setup;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.SrtpControlType;
import org.jitsi.service.version.Version;
import org.jitsi.util.Logger;
import org.jitsi.util.StringUtils;

public class DtlsControlImpl extends AbstractSrtpControl<DtlsTransformEngine> implements DtlsControl {
    private static final char[] HEX_ENCODE_TABLE = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final long ONE_DAY = 86400000;
    static final int[] SRTP_PROTECTION_PROFILES = new int[]{1, 2};
    private static final Logger logger = Logger.getLogger(DtlsControlImpl.class);
    private final Certificate certificate;
    private AbstractRTPConnector connector;
    private boolean disposed = false;
    private final AsymmetricCipherKeyPair keyPair = generateKeyPair();
    private final String localFingerprint;
    private final String localFingerprintHashFunction;
    private Map<String, String> remoteFingerprints;
    private Setup setup;

    public DtlsControlImpl() {
        super(SrtpControlType.DTLS_SRTP);
        org.jitsi.bouncycastle.asn1.x509.Certificate x509Certificate = generateX509Certificate(generateCN(), this.keyPair);
        this.certificate = new Certificate(new org.jitsi.bouncycastle.asn1.x509.Certificate[]{x509Certificate});
        this.localFingerprintHashFunction = findHashFunction(x509Certificate);
        this.localFingerprint = computeFingerprint(x509Certificate, this.localFingerprintHashFunction);
    }

    static int chooseSRTPProtectionProfile(int... theirs) {
        int[] ours = SRTP_PROTECTION_PROFILES;
        if (theirs != null) {
            for (int their : theirs) {
                for (int our : ours) {
                    if (their == our) {
                        return their;
                    }
                }
            }
        }
        return 0;
    }

    public void cleanup() {
        super.cleanup();
        setConnector(null);
        synchronized (this) {
            this.disposed = true;
            notifyAll();
        }
    }

    private static final String computeFingerprint(org.jitsi.bouncycastle.asn1.x509.Certificate certificate, String hashFunction) {
        try {
            Digest digest = BcDefaultDigestProvider.INSTANCE.get(new DefaultDigestAlgorithmIdentifierFinder().find(hashFunction.toUpperCase()));
            byte[] in = certificate.getEncoded("DER");
            byte[] out = new byte[digest.getDigestSize()];
            digest.update(in, 0, in.length);
            digest.doFinal(out, 0);
            return toHex(out);
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            } else {
                logger.error("Failed to generate certificate fingerprint!", t);
                if (t instanceof RuntimeException) {
                    RuntimeException t3 = (RuntimeException) t;
                } else {
                    RuntimeException runtimeException = new RuntimeException(t);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public DtlsTransformEngine createTransformEngine() {
        DtlsTransformEngine transformEngine = new DtlsTransformEngine(this);
        transformEngine.setConnector(this.connector);
        transformEngine.setSetup(this.setup);
        return transformEngine;
    }

    private static X500Name generateCN() {
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        String applicationName = System.getProperty(Version.PNAME_APPLICATION_NAME);
        String applicationVersion = System.getProperty(Version.PNAME_APPLICATION_VERSION);
        StringBuilder cn = new StringBuilder();
        if (!StringUtils.isNullOrEmpty(applicationName, true)) {
            cn.append(applicationName);
        }
        if (!StringUtils.isNullOrEmpty(applicationVersion, true)) {
            if (cn.length() != 0) {
                cn.append(' ');
            }
            cn.append(applicationVersion);
        }
        if (cn.length() == 0) {
            cn.append(DtlsControlImpl.class.getName());
        }
        builder.addRDN(BCStyle.CN, cn.toString());
        return builder.build();
    }

    private static AsymmetricCipherKeyPair generateKeyPair() {
        RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
        generator.init(new RSAKeyGenerationParameters(new BigInteger("10001", 16), new SecureRandom(), 1024, 80));
        return generator.generateKeyPair();
    }

    private static org.jitsi.bouncycastle.asn1.x509.Certificate generateX509Certificate(X500Name subject, AsymmetricCipherKeyPair keyPair) {
        try {
            long now = System.currentTimeMillis();
            Date notBefore = new Date(now - ONE_DAY);
            Date notAfter = new Date(518400000 + now);
            X509v3CertificateBuilder builder = new X509v3CertificateBuilder(subject, BigInteger.valueOf(now), notBefore, notAfter, subject, SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(keyPair.getPublic()));
            AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
            return builder.build(new BcRSAContentSignerBuilder(sigAlgId, new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId)).build(keyPair.getPrivate())).toASN1Structure();
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            } else {
                logger.error("Failed to generate self-signed X.509 certificate", t);
                if (t instanceof RuntimeException) {
                    RuntimeException t3 = (RuntimeException) t;
                } else {
                    RuntimeException runtimeException = new RuntimeException(t);
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public AsymmetricCipherKeyPair getKeyPair() {
        return this.keyPair;
    }

    public boolean getSecureCommunicationStatus() {
        return false;
    }

    private static String findHashFunction(org.jitsi.bouncycastle.asn1.x509.Certificate certificate) {
        try {
            return BcDefaultDigestProvider.INSTANCE.get(new DefaultDigestAlgorithmIdentifierFinder().find(certificate.getSignatureAlgorithm())).getAlgorithmName().toLowerCase();
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            } else {
                logger.warn("Failed to find the hash function of the signature algorithm of a certificate!", t);
                if (t instanceof RuntimeException) {
                    RuntimeException t3 = (RuntimeException) t;
                } else {
                    RuntimeException runtimeException = new RuntimeException(t);
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public Certificate getCertificate() {
        return this.certificate;
    }

    public String getLocalFingerprint() {
        return this.localFingerprint;
    }

    public String getLocalFingerprintHashFunction() {
        return this.localFingerprintHashFunction;
    }

    public boolean requiresSecureSignalingTransport() {
        return true;
    }

    public void setConnector(AbstractRTPConnector connector) {
        if (this.connector != connector) {
            this.connector = connector;
            DtlsTransformEngine transformEngine = this.transformEngine;
            if (transformEngine != null) {
                transformEngine.setConnector(this.connector);
            }
        }
    }

    public void setRemoteFingerprints(Map<String, String> remoteFingerprints) {
        if (remoteFingerprints == null) {
            throw new NullPointerException("remoteFingerprints");
        }
        synchronized (this) {
            this.remoteFingerprints = remoteFingerprints;
            notifyAll();
        }
    }

    public void setSetup(Setup setup) {
        if (this.setup != setup) {
            this.setup = setup;
            DtlsTransformEngine transformEngine = this.transformEngine;
            if (transformEngine != null) {
                transformEngine.setSetup(this.setup);
            }
        }
    }

    public void start(MediaType mediaType) {
        DtlsTransformEngine transformEngine = (DtlsTransformEngine) getTransformEngine();
        if (transformEngine != null) {
            transformEngine.start(mediaType);
        }
    }

    private static String toHex(byte[] fingerprint) {
        if (fingerprint.length == 0) {
            throw new IllegalArgumentException("fingerprint");
        }
        char[] chars = new char[((fingerprint.length * 3) - 1)];
        int f = 0;
        int fLast = fingerprint.length - 1;
        int c = 0;
        while (f <= fLast) {
            int b = fingerprint[f] & UnsignedUtils.MAX_UBYTE;
            int i = c + 1;
            chars[c] = HEX_ENCODE_TABLE[b >>> 4];
            c = i + 1;
            chars[i] = HEX_ENCODE_TABLE[b & 15];
            if (f != fLast) {
                i = c + 1;
                chars[c] = ':';
            } else {
                i = c;
            }
            f++;
            c = i;
        }
        return new String(chars);
    }

    private void verifyAndValidateCertificate(org.jitsi.bouncycastle.asn1.x509.Certificate certificate) throws Exception {
        String remoteFingerprint;
        String hashFunction = findHashFunction(certificate);
        String fingerprint = computeFingerprint(certificate, hashFunction);
        synchronized (this) {
            if (this.disposed) {
                throw new IllegalStateException("disposed");
            }
            Map<String, String> remoteFingerprints = this.remoteFingerprints;
            if (remoteFingerprints == null) {
                throw new IOException("No fingerprints declared over the signaling path!");
            }
            remoteFingerprint = (String) remoteFingerprints.get(hashFunction);
        }
        if (remoteFingerprint == null) {
            throw new IOException("No fingerprint declared over the signaling path with hash function: " + hashFunction + "!");
        } else if (!remoteFingerprint.equals(fingerprint)) {
            throw new IOException("Fingerprint " + remoteFingerprint + " does not match the " + hashFunction + "-hashed certificate " + fingerprint + "!");
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean verifyAndValidateCertificate(Certificate certificate) throws Exception {
        try {
            org.jitsi.bouncycastle.asn1.x509.Certificate[] certificateList = certificate.getCertificateList();
            if (certificateList.length == 0) {
                throw new IllegalArgumentException("certificate.certificateList");
            }
            for (org.jitsi.bouncycastle.asn1.x509.Certificate x509Certificate : certificateList) {
                verifyAndValidateCertificate(x509Certificate);
            }
            return true;
        } catch (Exception e) {
            String message = "Failed to verify and/or validate a certificate offered over the media path against fingerprints declared over the signaling path!";
            String throwableMessage = e.getMessage();
            if (throwableMessage == null || throwableMessage.length() == 0) {
                logger.warn(message, e);
                return false;
            }
            logger.warn(message + " " + throwableMessage);
            return false;
        }
    }
}
