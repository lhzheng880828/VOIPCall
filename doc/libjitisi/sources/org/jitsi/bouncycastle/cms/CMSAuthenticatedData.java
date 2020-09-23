package org.jitsi.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.security.NoSuchProviderException;
import java.security.Provider;
import org.jitsi.bouncycastle.asn1.ASN1Encodable;
import org.jitsi.bouncycastle.asn1.ASN1OctetString;
import org.jitsi.bouncycastle.asn1.ASN1Set;
import org.jitsi.bouncycastle.asn1.cms.AttributeTable;
import org.jitsi.bouncycastle.asn1.cms.AuthenticatedData;
import org.jitsi.bouncycastle.asn1.cms.CMSAttributes;
import org.jitsi.bouncycastle.asn1.cms.ContentInfo;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.cms.jcajce.JceAlgorithmIdentifierConverter;
import org.jitsi.bouncycastle.operator.DigestCalculatorProvider;
import org.jitsi.bouncycastle.operator.OperatorCreationException;
import org.jitsi.bouncycastle.util.Arrays;

public class CMSAuthenticatedData {
    /* access modifiers changed from: private */
    public ASN1Set authAttrs;
    ContentInfo contentInfo;
    private byte[] mac;
    private AlgorithmIdentifier macAlg;
    private OriginatorInformation originatorInfo;
    RecipientInformationStore recipientInfoStore;
    private ASN1Set unauthAttrs;

    public CMSAuthenticatedData(InputStream inputStream) throws CMSException {
        this(CMSUtils.readContentInfo(inputStream));
    }

    public CMSAuthenticatedData(InputStream inputStream, DigestCalculatorProvider digestCalculatorProvider) throws CMSException {
        this(CMSUtils.readContentInfo(inputStream), digestCalculatorProvider);
    }

    public CMSAuthenticatedData(ContentInfo contentInfo) throws CMSException {
        this(contentInfo, null);
    }

    public CMSAuthenticatedData(ContentInfo contentInfo, DigestCalculatorProvider digestCalculatorProvider) throws CMSException {
        this.contentInfo = contentInfo;
        AuthenticatedData instance = AuthenticatedData.getInstance(contentInfo.getContent());
        if (instance.getOriginatorInfo() != null) {
            this.originatorInfo = new OriginatorInformation(instance.getOriginatorInfo());
        }
        ASN1Set recipientInfos = instance.getRecipientInfos();
        this.macAlg = instance.getMacAlgorithm();
        this.authAttrs = instance.getAuthAttrs();
        this.mac = instance.getMac().getOctets();
        this.unauthAttrs = instance.getUnauthAttrs();
        CMSProcessableByteArray cMSProcessableByteArray = new CMSProcessableByteArray(ASN1OctetString.getInstance(instance.getEncapsulatedContentInfo().getContent()).getOctets());
        if (this.authAttrs == null) {
            this.recipientInfoStore = CMSEnvelopedHelper.buildRecipientInformationStore(recipientInfos, this.macAlg, new CMSAuthenticatedSecureReadable(this.macAlg, cMSProcessableByteArray));
        } else if (digestCalculatorProvider == null) {
            throw new CMSException("a digest calculator provider is required if authenticated attributes are present");
        } else {
            try {
                this.recipientInfoStore = CMSEnvelopedHelper.buildRecipientInformationStore(recipientInfos, this.macAlg, new CMSDigestAuthenticatedSecureReadable(digestCalculatorProvider.get(instance.getDigestAlgorithm()), cMSProcessableByteArray), new AuthAttributesProvider() {
                    public ASN1Set getAuthAttributes() {
                        return CMSAuthenticatedData.this.authAttrs;
                    }
                });
            } catch (OperatorCreationException e) {
                throw new CMSException("unable to create digest calculator: " + e.getMessage(), e);
            }
        }
    }

    public CMSAuthenticatedData(byte[] bArr) throws CMSException {
        this(CMSUtils.readContentInfo(bArr));
    }

    public CMSAuthenticatedData(byte[] bArr, DigestCalculatorProvider digestCalculatorProvider) throws CMSException {
        this(CMSUtils.readContentInfo(bArr), digestCalculatorProvider);
    }

    private byte[] encodeObj(ASN1Encodable aSN1Encodable) throws IOException {
        return aSN1Encodable != null ? aSN1Encodable.toASN1Primitive().getEncoded() : null;
    }

    public AttributeTable getAuthAttrs() {
        return this.authAttrs == null ? null : new AttributeTable(this.authAttrs);
    }

    public byte[] getContentDigest() {
        return this.authAttrs != null ? ASN1OctetString.getInstance(getAuthAttrs().get(CMSAttributes.messageDigest).getAttrValues().getObjectAt(0)).getOctets() : null;
    }

    public ContentInfo getContentInfo() {
        return this.contentInfo;
    }

    public byte[] getEncoded() throws IOException {
        return this.contentInfo.getEncoded();
    }

    public byte[] getMac() {
        return Arrays.clone(this.mac);
    }

    public String getMacAlgOID() {
        return this.macAlg.getObjectId().getId();
    }

    public byte[] getMacAlgParams() {
        try {
            return encodeObj(this.macAlg.getParameters());
        } catch (Exception e) {
            throw new RuntimeException("exception getting encryption parameters " + e);
        }
    }

    public AlgorithmIdentifier getMacAlgorithm() {
        return this.macAlg;
    }

    public AlgorithmParameters getMacAlgorithmParameters(String str) throws CMSException, NoSuchProviderException {
        return new JceAlgorithmIdentifierConverter().setProvider(str).getAlgorithmParameters(this.macAlg);
    }

    public AlgorithmParameters getMacAlgorithmParameters(Provider provider) throws CMSException {
        return new JceAlgorithmIdentifierConverter().setProvider(provider).getAlgorithmParameters(this.macAlg);
    }

    public OriginatorInformation getOriginatorInfo() {
        return this.originatorInfo;
    }

    public RecipientInformationStore getRecipientInfos() {
        return this.recipientInfoStore;
    }

    public AttributeTable getUnauthAttrs() {
        return this.unauthAttrs == null ? null : new AttributeTable(this.unauthAttrs);
    }
}
