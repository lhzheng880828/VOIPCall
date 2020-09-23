package org.jitsi.bouncycastle.cms;

import org.jitsi.bouncycastle.asn1.DEROctetString;
import org.jitsi.bouncycastle.asn1.cms.KEKIdentifier;
import org.jitsi.bouncycastle.asn1.cms.KEKRecipientInfo;
import org.jitsi.bouncycastle.asn1.cms.RecipientInfo;
import org.jitsi.bouncycastle.operator.GenericKey;
import org.jitsi.bouncycastle.operator.OperatorException;
import org.jitsi.bouncycastle.operator.SymmetricKeyWrapper;

public abstract class KEKRecipientInfoGenerator implements RecipientInfoGenerator {
    private final KEKIdentifier kekIdentifier;
    protected final SymmetricKeyWrapper wrapper;

    protected KEKRecipientInfoGenerator(KEKIdentifier kEKIdentifier, SymmetricKeyWrapper symmetricKeyWrapper) {
        this.kekIdentifier = kEKIdentifier;
        this.wrapper = symmetricKeyWrapper;
    }

    public final RecipientInfo generate(GenericKey genericKey) throws CMSException {
        try {
            return new RecipientInfo(new KEKRecipientInfo(this.kekIdentifier, this.wrapper.getAlgorithmIdentifier(), new DEROctetString(this.wrapper.generateWrappedKey(genericKey))));
        } catch (OperatorException e) {
            throw new CMSException("exception wrapping content key: " + e.getMessage(), e);
        }
    }
}
