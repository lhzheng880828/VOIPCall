package org.jitsi.bouncycastle.openssl.jcajce;

import java.security.Provider;
import java.security.SecureRandom;
import org.jitsi.bouncycastle.jcajce.DefaultJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.JcaJceHelper;
import org.jitsi.bouncycastle.jcajce.NamedJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.ProviderJcaJceHelper;
import org.jitsi.bouncycastle.openssl.PEMEncryptor;
import org.jitsi.bouncycastle.openssl.PEMException;

public class JcePEMEncryptorBuilder {
    /* access modifiers changed from: private|final */
    public final String algorithm;
    /* access modifiers changed from: private */
    public JcaJceHelper helper = new DefaultJcaJceHelper();
    private SecureRandom random;

    public JcePEMEncryptorBuilder(String str) {
        this.algorithm = str;
    }

    public PEMEncryptor build(final char[] cArr) {
        if (this.random == null) {
            this.random = new SecureRandom();
        }
        final byte[] bArr = new byte[(this.algorithm.startsWith("AES-") ? 16 : 8)];
        this.random.nextBytes(bArr);
        return new PEMEncryptor() {
            public byte[] encrypt(byte[] bArr) throws PEMException {
                return PEMUtilities.crypt(true, JcePEMEncryptorBuilder.this.helper, bArr, cArr, JcePEMEncryptorBuilder.this.algorithm, bArr);
            }

            public String getAlgorithm() {
                return JcePEMEncryptorBuilder.this.algorithm;
            }

            public byte[] getIV() {
                return bArr;
            }
        };
    }

    public JcePEMEncryptorBuilder setProvider(String str) {
        this.helper = new NamedJcaJceHelper(str);
        return this;
    }

    public JcePEMEncryptorBuilder setProvider(Provider provider) {
        this.helper = new ProviderJcaJceHelper(provider);
        return this;
    }

    public JcePEMEncryptorBuilder setSecureRandom(SecureRandom secureRandom) {
        this.random = secureRandom;
        return this;
    }
}
