package org.jitsi.bouncycastle.openssl.jcajce;

import java.security.Provider;
import org.jitsi.bouncycastle.jcajce.DefaultJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.JcaJceHelper;
import org.jitsi.bouncycastle.jcajce.NamedJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.ProviderJcaJceHelper;
import org.jitsi.bouncycastle.openssl.PEMDecryptor;
import org.jitsi.bouncycastle.openssl.PEMDecryptorProvider;
import org.jitsi.bouncycastle.openssl.PEMException;
import org.jitsi.bouncycastle.openssl.PasswordException;

public class JcePEMDecryptorProviderBuilder {
    /* access modifiers changed from: private */
    public JcaJceHelper helper = new DefaultJcaJceHelper();

    public PEMDecryptorProvider build(final char[] cArr) {
        return new PEMDecryptorProvider() {
            public PEMDecryptor get(final String str) {
                return new PEMDecryptor() {
                    public byte[] decrypt(byte[] bArr, byte[] bArr2) throws PEMException {
                        if (cArr == null) {
                            throw new PasswordException("Password is null, but a password is required");
                        }
                        return PEMUtilities.crypt(false, JcePEMDecryptorProviderBuilder.this.helper, bArr, cArr, str, bArr2);
                    }
                };
            }
        };
    }

    public JcePEMDecryptorProviderBuilder setProvider(String str) {
        this.helper = new NamedJcaJceHelper(str);
        return this;
    }

    public JcePEMDecryptorProviderBuilder setProvider(Provider provider) {
        this.helper = new ProviderJcaJceHelper(provider);
        return this;
    }
}
