package org.jitsi.bouncycastle.cert.jcajce;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import org.jitsi.bouncycastle.asn1.ASN1OctetString;
import org.jitsi.bouncycastle.asn1.ASN1Primitive;
import org.jitsi.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.jitsi.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.jitsi.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jitsi.bouncycastle.cert.X509ExtensionUtils;
import org.jitsi.bouncycastle.operator.DigestCalculator;

public class JcaX509ExtensionUtils extends X509ExtensionUtils {

    private static class SHA1DigestCalculator implements DigestCalculator {
        private ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        private MessageDigest digest;

        public SHA1DigestCalculator(MessageDigest messageDigest) {
            this.digest = messageDigest;
        }

        public AlgorithmIdentifier getAlgorithmIdentifier() {
            return new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1);
        }

        public byte[] getDigest() {
            byte[] digest = this.digest.digest(this.bOut.toByteArray());
            this.bOut.reset();
            return digest;
        }

        public OutputStream getOutputStream() {
            return this.bOut;
        }
    }

    public JcaX509ExtensionUtils() throws NoSuchAlgorithmException {
        super(new SHA1DigestCalculator(MessageDigest.getInstance("SHA1")));
    }

    public JcaX509ExtensionUtils(DigestCalculator digestCalculator) {
        super(digestCalculator);
    }

    public static ASN1Primitive parseExtensionValue(byte[] bArr) throws IOException {
        return ASN1Primitive.fromByteArray(ASN1OctetString.getInstance(bArr).getOctets());
    }

    public AuthorityKeyIdentifier createAuthorityKeyIdentifier(PublicKey publicKey) {
        return super.createAuthorityKeyIdentifier(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
    }

    public AuthorityKeyIdentifier createAuthorityKeyIdentifier(X509Certificate x509Certificate) throws CertificateEncodingException {
        return super.createAuthorityKeyIdentifier(new JcaX509CertificateHolder(x509Certificate));
    }

    public SubjectKeyIdentifier createSubjectKeyIdentifier(PublicKey publicKey) {
        return super.createSubjectKeyIdentifier(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
    }

    public SubjectKeyIdentifier createTruncatedSubjectKeyIdentifier(PublicKey publicKey) {
        return super.createSubjectKeyIdentifier(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
    }
}
