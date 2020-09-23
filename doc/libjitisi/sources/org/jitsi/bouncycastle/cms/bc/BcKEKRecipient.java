package org.jitsi.bouncycastle.cms.bc;

import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.cms.CMSException;
import org.jitsi.bouncycastle.cms.KEKRecipient;
import org.jitsi.bouncycastle.crypto.CipherParameters;
import org.jitsi.bouncycastle.operator.OperatorException;
import org.jitsi.bouncycastle.operator.SymmetricKeyUnwrapper;
import org.jitsi.bouncycastle.operator.bc.BcSymmetricKeyUnwrapper;

public abstract class BcKEKRecipient implements KEKRecipient {
    private SymmetricKeyUnwrapper unwrapper;

    public BcKEKRecipient(BcSymmetricKeyUnwrapper bcSymmetricKeyUnwrapper) {
        this.unwrapper = bcSymmetricKeyUnwrapper;
    }

    /* access modifiers changed from: protected */
    public CipherParameters extractSecretKey(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, byte[] bArr) throws CMSException {
        try {
            return CMSUtils.getBcKey(this.unwrapper.generateUnwrappedKey(algorithmIdentifier2, bArr));
        } catch (OperatorException e) {
            throw new CMSException("exception unwrapping key: " + e.getMessage(), e);
        }
    }
}
