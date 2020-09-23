package org.jitsi.bouncycastle.cms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.security.NoSuchProviderException;
import java.security.Provider;
import org.jitsi.bouncycastle.asn1.ASN1Encodable;
import org.jitsi.bouncycastle.asn1.ASN1EncodableVector;
import org.jitsi.bouncycastle.asn1.ASN1OctetStringParser;
import org.jitsi.bouncycastle.asn1.ASN1SequenceParser;
import org.jitsi.bouncycastle.asn1.ASN1Set;
import org.jitsi.bouncycastle.asn1.ASN1SetParser;
import org.jitsi.bouncycastle.asn1.DERSet;
import org.jitsi.bouncycastle.asn1.cms.AttributeTable;
import org.jitsi.bouncycastle.asn1.cms.EncryptedContentInfoParser;
import org.jitsi.bouncycastle.asn1.cms.EnvelopedDataParser;
import org.jitsi.bouncycastle.asn1.cms.OriginatorInfo;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.cms.jcajce.JceAlgorithmIdentifierConverter;

public class CMSEnvelopedDataParser extends CMSContentInfoParser {
    private boolean attrNotRead;
    private AlgorithmIdentifier encAlg;
    EnvelopedDataParser envelopedData;
    private OriginatorInformation originatorInfo;
    RecipientInformationStore recipientInfoStore;
    private AttributeTable unprotectedAttributes;

    public CMSEnvelopedDataParser(InputStream inputStream) throws CMSException, IOException {
        super(inputStream);
        this.attrNotRead = true;
        this.envelopedData = new EnvelopedDataParser((ASN1SequenceParser) this._contentInfo.getContent(16));
        OriginatorInfo originatorInfo = this.envelopedData.getOriginatorInfo();
        if (originatorInfo != null) {
            this.originatorInfo = new OriginatorInformation(originatorInfo);
        }
        ASN1Set instance = ASN1Set.getInstance(this.envelopedData.getRecipientInfos().toASN1Primitive());
        EncryptedContentInfoParser encryptedContentInfo = this.envelopedData.getEncryptedContentInfo();
        this.encAlg = encryptedContentInfo.getContentEncryptionAlgorithm();
        this.recipientInfoStore = CMSEnvelopedHelper.buildRecipientInformationStore(instance, this.encAlg, new CMSEnvelopedSecureReadable(this.encAlg, new CMSProcessableInputStream(((ASN1OctetStringParser) encryptedContentInfo.getEncryptedContent(4)).getOctetStream())));
    }

    public CMSEnvelopedDataParser(byte[] bArr) throws CMSException, IOException {
        this(new ByteArrayInputStream(bArr));
    }

    private byte[] encodeObj(ASN1Encodable aSN1Encodable) throws IOException {
        return aSN1Encodable != null ? aSN1Encodable.toASN1Primitive().getEncoded() : null;
    }

    public AlgorithmIdentifier getContentEncryptionAlgorithm() {
        return this.encAlg;
    }

    public String getEncryptionAlgOID() {
        return this.encAlg.getAlgorithm().toString();
    }

    public byte[] getEncryptionAlgParams() {
        try {
            return encodeObj(this.encAlg.getParameters());
        } catch (Exception e) {
            throw new RuntimeException("exception getting encryption parameters " + e);
        }
    }

    public AlgorithmParameters getEncryptionAlgorithmParameters(String str) throws CMSException, NoSuchProviderException {
        return new JceAlgorithmIdentifierConverter().setProvider(str).getAlgorithmParameters(this.encAlg);
    }

    public AlgorithmParameters getEncryptionAlgorithmParameters(Provider provider) throws CMSException {
        return new JceAlgorithmIdentifierConverter().setProvider(provider).getAlgorithmParameters(this.encAlg);
    }

    public OriginatorInformation getOriginatorInfo() {
        return this.originatorInfo;
    }

    public RecipientInformationStore getRecipientInfos() {
        return this.recipientInfoStore;
    }

    public AttributeTable getUnprotectedAttributes() throws IOException {
        if (this.unprotectedAttributes == null && this.attrNotRead) {
            ASN1SetParser unprotectedAttrs = this.envelopedData.getUnprotectedAttrs();
            this.attrNotRead = false;
            if (unprotectedAttrs != null) {
                ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
                while (true) {
                    ASN1Encodable readObject = unprotectedAttrs.readObject();
                    if (readObject == null) {
                        break;
                    }
                    aSN1EncodableVector.add(((ASN1SequenceParser) readObject).toASN1Primitive());
                }
                this.unprotectedAttributes = new AttributeTable(new DERSet(aSN1EncodableVector));
            }
        }
        return this.unprotectedAttributes;
    }
}
