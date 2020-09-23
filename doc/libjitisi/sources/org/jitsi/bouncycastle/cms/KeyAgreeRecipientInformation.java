package org.jitsi.bouncycastle.cms;

import java.io.IOException;
import java.security.Key;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.util.List;
import org.jitsi.bouncycastle.asn1.ASN1OctetString;
import org.jitsi.bouncycastle.asn1.ASN1Sequence;
import org.jitsi.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.jitsi.bouncycastle.asn1.cms.KeyAgreeRecipientIdentifier;
import org.jitsi.bouncycastle.asn1.cms.KeyAgreeRecipientInfo;
import org.jitsi.bouncycastle.asn1.cms.OriginatorIdentifierOrKey;
import org.jitsi.bouncycastle.asn1.cms.OriginatorPublicKey;
import org.jitsi.bouncycastle.asn1.cms.RecipientEncryptedKey;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jitsi.bouncycastle.cms.jcajce.JceKeyAgreeAuthenticatedRecipient;
import org.jitsi.bouncycastle.cms.jcajce.JceKeyAgreeEnvelopedRecipient;

public class KeyAgreeRecipientInformation extends RecipientInformation {
    private ASN1OctetString encryptedKey;
    private KeyAgreeRecipientInfo info;

    KeyAgreeRecipientInformation(KeyAgreeRecipientInfo keyAgreeRecipientInfo, RecipientId recipientId, ASN1OctetString aSN1OctetString, AlgorithmIdentifier algorithmIdentifier, CMSSecureReadable cMSSecureReadable, AuthAttributesProvider authAttributesProvider) {
        super(keyAgreeRecipientInfo.getKeyEncryptionAlgorithm(), algorithmIdentifier, cMSSecureReadable, authAttributesProvider);
        this.info = keyAgreeRecipientInfo;
        this.rid = recipientId;
        this.encryptedKey = aSN1OctetString;
    }

    private SubjectPublicKeyInfo getPublicKeyInfoFromOriginatorId(OriginatorId originatorId) throws CMSException {
        throw new CMSException("No support for 'originator' as IssuerAndSerialNumber or SubjectKeyIdentifier");
    }

    private SubjectPublicKeyInfo getPublicKeyInfoFromOriginatorPublicKey(AlgorithmIdentifier algorithmIdentifier, OriginatorPublicKey originatorPublicKey) {
        return new SubjectPublicKeyInfo(algorithmIdentifier, originatorPublicKey.getPublicKey().getBytes());
    }

    private SubjectPublicKeyInfo getSenderPublicKeyInfo(AlgorithmIdentifier algorithmIdentifier, OriginatorIdentifierOrKey originatorIdentifierOrKey) throws CMSException, IOException {
        OriginatorPublicKey originatorKey = originatorIdentifierOrKey.getOriginatorKey();
        if (originatorKey != null) {
            return getPublicKeyInfoFromOriginatorPublicKey(algorithmIdentifier, originatorKey);
        }
        IssuerAndSerialNumber issuerAndSerialNumber = originatorIdentifierOrKey.getIssuerAndSerialNumber();
        return getPublicKeyInfoFromOriginatorId(issuerAndSerialNumber != null ? new OriginatorId(issuerAndSerialNumber.getName(), issuerAndSerialNumber.getSerialNumber().getValue()) : new OriginatorId(originatorIdentifierOrKey.getSubjectKeyIdentifier().getKeyIdentifier()));
    }

    static void readRecipientInfo(List list, KeyAgreeRecipientInfo keyAgreeRecipientInfo, AlgorithmIdentifier algorithmIdentifier, CMSSecureReadable cMSSecureReadable, AuthAttributesProvider authAttributesProvider) {
        ASN1Sequence recipientEncryptedKeys = keyAgreeRecipientInfo.getRecipientEncryptedKeys();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < recipientEncryptedKeys.size()) {
                RecipientEncryptedKey instance = RecipientEncryptedKey.getInstance(recipientEncryptedKeys.getObjectAt(i2));
                KeyAgreeRecipientIdentifier identifier = instance.getIdentifier();
                IssuerAndSerialNumber issuerAndSerialNumber = identifier.getIssuerAndSerialNumber();
                list.add(new KeyAgreeRecipientInformation(keyAgreeRecipientInfo, issuerAndSerialNumber != null ? new KeyAgreeRecipientId(issuerAndSerialNumber.getName(), issuerAndSerialNumber.getSerialNumber().getValue()) : new KeyAgreeRecipientId(identifier.getRKeyID().getSubjectKeyIdentifier().getOctets()), instance.getEncryptedKey(), algorithmIdentifier, cMSSecureReadable, authAttributesProvider));
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    public CMSTypedStream getContentStream(Key key, String str) throws CMSException, NoSuchProviderException {
        return getContentStream(key, CMSUtils.getProvider(str));
    }

    public CMSTypedStream getContentStream(Key key, Provider provider) throws CMSException {
        try {
            Recipient jceKeyAgreeEnvelopedRecipient;
            if (this.secureReadable instanceof CMSEnvelopedSecureReadable) {
                jceKeyAgreeEnvelopedRecipient = new JceKeyAgreeEnvelopedRecipient((PrivateKey) key);
            } else {
                Object jceKeyAgreeEnvelopedRecipient2 = new JceKeyAgreeAuthenticatedRecipient((PrivateKey) key);
            }
            if (provider != null) {
                jceKeyAgreeEnvelopedRecipient2.setProvider(provider);
                if (provider.getName().equalsIgnoreCase("SunJCE")) {
                    jceKeyAgreeEnvelopedRecipient2.setContentProvider((String) null);
                }
            }
            return getContentStream(jceKeyAgreeEnvelopedRecipient2);
        } catch (IOException e) {
            throw new CMSException("encoding error: " + e.getMessage(), e);
        }
    }

    /* access modifiers changed from: protected */
    public RecipientOperator getRecipientOperator(Recipient recipient) throws CMSException, IOException {
        return ((KeyAgreeRecipient) recipient).getRecipientOperator(this.keyEncAlg, this.messageAlgorithm, getSenderPublicKeyInfo(((KeyAgreeRecipient) recipient).getPrivateKeyAlgorithmIdentifier(), this.info.getOriginator()), this.info.getUserKeyingMaterial(), this.encryptedKey.getOctets());
    }
}
