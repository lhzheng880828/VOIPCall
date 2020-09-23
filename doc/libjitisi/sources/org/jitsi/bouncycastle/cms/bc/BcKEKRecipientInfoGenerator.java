package org.jitsi.bouncycastle.cms.bc;

import org.jitsi.bouncycastle.asn1.cms.KEKIdentifier;
import org.jitsi.bouncycastle.cms.KEKRecipientInfoGenerator;
import org.jitsi.bouncycastle.operator.bc.BcSymmetricKeyWrapper;

public class BcKEKRecipientInfoGenerator extends KEKRecipientInfoGenerator {
    public BcKEKRecipientInfoGenerator(KEKIdentifier kEKIdentifier, BcSymmetricKeyWrapper bcSymmetricKeyWrapper) {
        super(kEKIdentifier, bcSymmetricKeyWrapper);
    }

    public BcKEKRecipientInfoGenerator(byte[] bArr, BcSymmetricKeyWrapper bcSymmetricKeyWrapper) {
        this(new KEKIdentifier(bArr, null, null), bcSymmetricKeyWrapper);
    }
}
