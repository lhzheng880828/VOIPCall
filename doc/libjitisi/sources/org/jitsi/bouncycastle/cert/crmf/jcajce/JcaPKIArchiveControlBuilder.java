package org.jitsi.bouncycastle.cert.crmf.jcajce;

import java.security.PrivateKey;
import javax.security.auth.x500.X500Principal;
import org.jitsi.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.jitsi.bouncycastle.asn1.x500.X500Name;
import org.jitsi.bouncycastle.asn1.x509.GeneralName;
import org.jitsi.bouncycastle.cert.crmf.PKIArchiveControlBuilder;

public class JcaPKIArchiveControlBuilder extends PKIArchiveControlBuilder {
    public JcaPKIArchiveControlBuilder(PrivateKey privateKey, X500Principal x500Principal) {
        this(privateKey, X500Name.getInstance(x500Principal.getEncoded()));
    }

    public JcaPKIArchiveControlBuilder(PrivateKey privateKey, X500Name x500Name) {
        this(privateKey, new GeneralName(x500Name));
    }

    public JcaPKIArchiveControlBuilder(PrivateKey privateKey, GeneralName generalName) {
        super(PrivateKeyInfo.getInstance(privateKey.getEncoded()), generalName);
    }
}
