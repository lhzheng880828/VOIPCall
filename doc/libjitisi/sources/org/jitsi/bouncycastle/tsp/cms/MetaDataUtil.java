package org.jitsi.bouncycastle.tsp.cms;

import java.io.IOException;
import org.jitsi.bouncycastle.asn1.ASN1String;
import org.jitsi.bouncycastle.asn1.cms.Attributes;
import org.jitsi.bouncycastle.asn1.cms.MetaData;
import org.jitsi.bouncycastle.cms.CMSException;
import org.jitsi.bouncycastle.operator.DigestCalculator;

class MetaDataUtil {
    private final MetaData metaData;

    MetaDataUtil(MetaData metaData) {
        this.metaData = metaData;
    }

    private String convertString(ASN1String aSN1String) {
        return aSN1String != null ? aSN1String.toString() : null;
    }

    /* access modifiers changed from: 0000 */
    public String getFileName() {
        return this.metaData != null ? convertString(this.metaData.getFileName()) : null;
    }

    /* access modifiers changed from: 0000 */
    public String getMediaType() {
        return this.metaData != null ? convertString(this.metaData.getMediaType()) : null;
    }

    /* access modifiers changed from: 0000 */
    public Attributes getOtherMetaData() {
        return this.metaData != null ? this.metaData.getOtherMetaData() : null;
    }

    /* access modifiers changed from: 0000 */
    public void initialiseMessageImprintDigestCalculator(DigestCalculator digestCalculator) throws CMSException {
        if (this.metaData != null && this.metaData.isHashProtected()) {
            try {
                digestCalculator.getOutputStream().write(this.metaData.getEncoded("DER"));
            } catch (IOException e) {
                throw new CMSException("unable to initialise calculator from metaData: " + e.getMessage(), e);
            }
        }
    }
}
