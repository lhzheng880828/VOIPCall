package org.jitsi.bouncycastle.pkcs.jcajce;

import java.security.PrivateKey;
import org.jitsi.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.jitsi.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfoBuilder;

public class JcaPKCS8EncryptedPrivateKeyInfoBuilder extends PKCS8EncryptedPrivateKeyInfoBuilder {
    public JcaPKCS8EncryptedPrivateKeyInfoBuilder(PrivateKey privateKey) {
        super(PrivateKeyInfo.getInstance(privateKey.getEncoded()));
    }
}
