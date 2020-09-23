package org.jitsi.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;
import org.jitsi.bouncycastle.asn1.ASN1Set;
import org.jitsi.bouncycastle.asn1.cms.AuthEnvelopedData;
import org.jitsi.bouncycastle.asn1.cms.ContentInfo;
import org.jitsi.bouncycastle.asn1.cms.OriginatorInfo;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;

class CMSAuthEnvelopedData {
    private ASN1Set authAttrs;
    private AlgorithmIdentifier authEncAlg;
    ContentInfo contentInfo;
    private byte[] mac;
    private OriginatorInfo originator;
    RecipientInformationStore recipientInfoStore;
    private ASN1Set unauthAttrs;

    public CMSAuthEnvelopedData(InputStream inputStream) throws CMSException {
        this(CMSUtils.readContentInfo(inputStream));
    }

    public CMSAuthEnvelopedData(ContentInfo contentInfo) throws CMSException {
        this.contentInfo = contentInfo;
        AuthEnvelopedData instance = AuthEnvelopedData.getInstance(contentInfo.getContent());
        this.originator = instance.getOriginatorInfo();
        ASN1Set recipientInfos = instance.getRecipientInfos();
        this.authEncAlg = instance.getAuthEncryptedContentInfo().getContentEncryptionAlgorithm();
        this.recipientInfoStore = CMSEnvelopedHelper.buildRecipientInformationStore(recipientInfos, this.authEncAlg, new CMSSecureReadable() {
            public InputStream getInputStream() throws IOException, CMSException {
                return null;
            }
        });
        this.authAttrs = instance.getAuthAttrs();
        this.mac = instance.getMac().getOctets();
        this.unauthAttrs = instance.getUnauthAttrs();
    }

    public CMSAuthEnvelopedData(byte[] bArr) throws CMSException {
        this(CMSUtils.readContentInfo(bArr));
    }
}
