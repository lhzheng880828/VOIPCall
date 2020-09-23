package org.jitsi.bouncycastle.openssl.jcajce;

import java.security.PrivateKey;
import org.jitsi.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.jitsi.bouncycastle.openssl.PKCS8Generator;
import org.jitsi.bouncycastle.operator.OutputEncryptor;
import org.jitsi.bouncycastle.util.io.pem.PemGenerationException;

public class JcaPKCS8Generator extends PKCS8Generator {
    public JcaPKCS8Generator(PrivateKey privateKey, OutputEncryptor outputEncryptor) throws PemGenerationException {
        super(PrivateKeyInfo.getInstance(privateKey.getEncoded()), outputEncryptor);
    }
}
