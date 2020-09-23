package org.jitsi.bouncycastle.cms;

import org.jitsi.bouncycastle.asn1.ASN1OctetString;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

public interface KeyAgreeRecipient extends Recipient {
    AlgorithmIdentifier getPrivateKeyAlgorithmIdentifier();

    RecipientOperator getRecipientOperator(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, SubjectPublicKeyInfo subjectPublicKeyInfo, ASN1OctetString aSN1OctetString, byte[] bArr) throws CMSException;
}
