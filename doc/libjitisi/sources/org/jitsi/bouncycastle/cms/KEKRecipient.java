package org.jitsi.bouncycastle.cms;

import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;

public interface KEKRecipient extends Recipient {
    RecipientOperator getRecipientOperator(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, byte[] bArr) throws CMSException;
}
