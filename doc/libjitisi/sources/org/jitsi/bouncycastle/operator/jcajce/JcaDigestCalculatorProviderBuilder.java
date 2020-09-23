package org.jitsi.bouncycastle.operator.jcajce;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Provider;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.jcajce.DefaultJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.NamedJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.ProviderJcaJceHelper;
import org.jitsi.bouncycastle.operator.DigestCalculator;
import org.jitsi.bouncycastle.operator.DigestCalculatorProvider;
import org.jitsi.bouncycastle.operator.OperatorCreationException;

public class JcaDigestCalculatorProviderBuilder {
    /* access modifiers changed from: private */
    public OperatorHelper helper = new OperatorHelper(new DefaultJcaJceHelper());

    private class DigestOutputStream extends OutputStream {
        private MessageDigest dig;

        DigestOutputStream(MessageDigest messageDigest) {
            this.dig = messageDigest;
        }

        /* access modifiers changed from: 0000 */
        public byte[] getDigest() {
            return this.dig.digest();
        }

        public void write(int i) throws IOException {
            this.dig.update((byte) i);
        }

        public void write(byte[] bArr) throws IOException {
            this.dig.update(bArr);
        }

        public void write(byte[] bArr, int i, int i2) throws IOException {
            this.dig.update(bArr, i, i2);
        }
    }

    public DigestCalculatorProvider build() throws OperatorCreationException {
        return new DigestCalculatorProvider() {
            public DigestCalculator get(final AlgorithmIdentifier algorithmIdentifier) throws OperatorCreationException {
                try {
                    final DigestOutputStream digestOutputStream = new DigestOutputStream(JcaDigestCalculatorProviderBuilder.this.helper.createDigest(algorithmIdentifier));
                    return new DigestCalculator() {
                        public AlgorithmIdentifier getAlgorithmIdentifier() {
                            return algorithmIdentifier;
                        }

                        public byte[] getDigest() {
                            return digestOutputStream.getDigest();
                        }

                        public OutputStream getOutputStream() {
                            return digestOutputStream;
                        }
                    };
                } catch (GeneralSecurityException e) {
                    throw new OperatorCreationException("exception on setup: " + e, e);
                }
            }
        };
    }

    public JcaDigestCalculatorProviderBuilder setProvider(String str) {
        this.helper = new OperatorHelper(new NamedJcaJceHelper(str));
        return this;
    }

    public JcaDigestCalculatorProviderBuilder setProvider(Provider provider) {
        this.helper = new OperatorHelper(new ProviderJcaJceHelper(provider));
        return this;
    }
}
