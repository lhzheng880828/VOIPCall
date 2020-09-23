package org.jitsi.bouncycastle.cms;

import java.io.IOException;
import java.security.Key;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import org.jitsi.bouncycastle.asn1.ASN1OctetString;
import org.jitsi.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.jitsi.bouncycastle.asn1.cms.KeyTransRecipientInfo;
import org.jitsi.bouncycastle.asn1.cms.RecipientIdentifier;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.cms.jcajce.JceKeyTransAuthenticatedRecipient;
import org.jitsi.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;

public class KeyTransRecipientInformation extends RecipientInformation {
    private KeyTransRecipientInfo info;

    KeyTransRecipientInformation(KeyTransRecipientInfo keyTransRecipientInfo, AlgorithmIdentifier algorithmIdentifier, CMSSecureReadable cMSSecureReadable, AuthAttributesProvider authAttributesProvider) {
        super(keyTransRecipientInfo.getKeyEncryptionAlgorithm(), algorithmIdentifier, cMSSecureReadable, authAttributesProvider);
        this.info = keyTransRecipientInfo;
        RecipientIdentifier recipientIdentifier = keyTransRecipientInfo.getRecipientIdentifier();
        if (recipientIdentifier.isTagged()) {
            this.rid = new KeyTransRecipientId(ASN1OctetString.getInstance(recipientIdentifier.getId()).getOctets());
            return;
        }
        IssuerAndSerialNumber instance = IssuerAndSerialNumber.getInstance(recipientIdentifier.getId());
        this.rid = new KeyTransRecipientId(instance.getName(), instance.getSerialNumber().getValue());
    }

    public CMSTypedStream getContentStream(Key key, String str) throws CMSException, NoSuchProviderException {
        return getContentStream(key, CMSUtils.getProvider(str));
    }

    public CMSTypedStream getContentStream(Key key, Provider provider) throws CMSException {
        try {
            Recipient jceKeyTransEnvelopedRecipient;
            if (this.secureReadable instanceof CMSEnvelopedSecureReadable) {
                jceKeyTransEnvelopedRecipient = new JceKeyTransEnvelopedRecipient((PrivateKey) key);
            } else {
                Object jceKeyTransEnvelopedRecipient2 = new JceKeyTransAuthenticatedRecipient((PrivateKey) key);
            }
            if (provider != null) {
                jceKeyTransEnvelopedRecipient2.setProvider(provider);
                if (provider.getName().equalsIgnoreCase("SunJCE")) {
                    jceKeyTransEnvelopedRecipient2.setContentProvider((String) null);
                }
            }
            return getContentStream(jceKeyTransEnvelopedRecipient2);
        } catch (IOException e) {
            throw new CMSException("encoding error: " + e.getMessage(), e);
        }
    }

    /* access modifiers changed from: protected */
    public RecipientOperator getRecipientOperator(Recipient recipient) throws CMSException {
        return ((KeyTransRecipient) recipient).getRecipientOperator(this.keyEncAlg, this.messageAlgorithm, this.info.getEncryptedKey().getOctets());
    }
}
