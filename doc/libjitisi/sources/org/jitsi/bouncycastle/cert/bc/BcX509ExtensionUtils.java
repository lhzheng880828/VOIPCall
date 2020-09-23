package org.jitsi.bouncycastle.cert.bc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.jitsi.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.jitsi.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.jitsi.bouncycastle.cert.X509ExtensionUtils;
import org.jitsi.bouncycastle.crypto.digests.SHA1Digest;
import org.jitsi.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.jitsi.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.jitsi.bouncycastle.operator.DigestCalculator;

public class BcX509ExtensionUtils extends X509ExtensionUtils {

    private static class SHA1DigestCalculator implements DigestCalculator {
        private ByteArrayOutputStream bOut;

        private SHA1DigestCalculator() {
            this.bOut = new ByteArrayOutputStream();
        }

        public AlgorithmIdentifier getAlgorithmIdentifier() {
            return new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1);
        }

        public byte[] getDigest() {
            byte[] toByteArray = this.bOut.toByteArray();
            this.bOut.reset();
            SHA1Digest sHA1Digest = new SHA1Digest();
            sHA1Digest.update(toByteArray, 0, toByteArray.length);
            toByteArray = new byte[sHA1Digest.getDigestSize()];
            sHA1Digest.doFinal(toByteArray, 0);
            return toByteArray;
        }

        public OutputStream getOutputStream() {
            return this.bOut;
        }
    }

    public BcX509ExtensionUtils() {
        super(new SHA1DigestCalculator());
    }

    public BcX509ExtensionUtils(DigestCalculator digestCalculator) {
        super(digestCalculator);
    }

    public AuthorityKeyIdentifier createAuthorityKeyIdentifier(AsymmetricKeyParameter asymmetricKeyParameter) throws IOException {
        return super.createAuthorityKeyIdentifier(SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(asymmetricKeyParameter));
    }

    public SubjectKeyIdentifier createSubjectKeyIdentifier(AsymmetricKeyParameter asymmetricKeyParameter) throws IOException {
        return super.createSubjectKeyIdentifier(SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(asymmetricKeyParameter));
    }
}
