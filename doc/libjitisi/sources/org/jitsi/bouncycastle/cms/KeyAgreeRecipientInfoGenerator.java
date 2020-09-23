package org.jitsi.bouncycastle.cms;

import java.io.IOException;
import org.jitsi.bouncycastle.asn1.ASN1Encodable;
import org.jitsi.bouncycastle.asn1.ASN1EncodableVector;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.ASN1Sequence;
import org.jitsi.bouncycastle.asn1.DERNull;
import org.jitsi.bouncycastle.asn1.DEROctetString;
import org.jitsi.bouncycastle.asn1.cms.KeyAgreeRecipientInfo;
import org.jitsi.bouncycastle.asn1.cms.OriginatorIdentifierOrKey;
import org.jitsi.bouncycastle.asn1.cms.OriginatorPublicKey;
import org.jitsi.bouncycastle.asn1.cms.RecipientInfo;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jitsi.bouncycastle.operator.GenericKey;

public abstract class KeyAgreeRecipientInfoGenerator implements RecipientInfoGenerator {
    private ASN1ObjectIdentifier keyAgreementOID;
    private ASN1ObjectIdentifier keyEncryptionOID;
    private SubjectPublicKeyInfo originatorKeyInfo;

    protected KeyAgreeRecipientInfoGenerator(ASN1ObjectIdentifier aSN1ObjectIdentifier, SubjectPublicKeyInfo subjectPublicKeyInfo, ASN1ObjectIdentifier aSN1ObjectIdentifier2) {
        this.originatorKeyInfo = subjectPublicKeyInfo;
        this.keyAgreementOID = aSN1ObjectIdentifier;
        this.keyEncryptionOID = aSN1ObjectIdentifier2;
    }

    /* access modifiers changed from: protected */
    public OriginatorPublicKey createOriginatorPublicKey(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        return new OriginatorPublicKey(new AlgorithmIdentifier(subjectPublicKeyInfo.getAlgorithm().getAlgorithm(), DERNull.INSTANCE), subjectPublicKeyInfo.getPublicKeyData().getBytes());
    }

    public RecipientInfo generate(GenericKey genericKey) throws CMSException {
        OriginatorIdentifierOrKey originatorIdentifierOrKey = new OriginatorIdentifierOrKey(createOriginatorPublicKey(this.originatorKeyInfo));
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.keyEncryptionOID);
        aSN1EncodableVector.add(DERNull.INSTANCE);
        AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(this.keyEncryptionOID, DERNull.INSTANCE);
        AlgorithmIdentifier algorithmIdentifier2 = new AlgorithmIdentifier(this.keyAgreementOID, algorithmIdentifier);
        ASN1Sequence generateRecipientEncryptedKeys = generateRecipientEncryptedKeys(algorithmIdentifier2, algorithmIdentifier, genericKey);
        ASN1Encodable userKeyingMaterial = getUserKeyingMaterial(algorithmIdentifier2);
        if (userKeyingMaterial == null) {
            return new RecipientInfo(new KeyAgreeRecipientInfo(originatorIdentifierOrKey, null, algorithmIdentifier2, generateRecipientEncryptedKeys));
        }
        try {
            return new RecipientInfo(new KeyAgreeRecipientInfo(originatorIdentifierOrKey, new DEROctetString(userKeyingMaterial), algorithmIdentifier2, generateRecipientEncryptedKeys));
        } catch (IOException e) {
            throw new CMSException("unable to encode userKeyingMaterial: " + e.getMessage(), e);
        }
    }

    public abstract ASN1Sequence generateRecipientEncryptedKeys(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, GenericKey genericKey) throws CMSException;

    public abstract ASN1Encodable getUserKeyingMaterial(AlgorithmIdentifier algorithmIdentifier) throws CMSException;
}
