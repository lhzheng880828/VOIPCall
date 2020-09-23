package org.jitsi.bouncycastle.cms;

import java.util.ArrayList;
import java.util.Enumeration;
import org.jitsi.bouncycastle.asn1.ASN1Encodable;
import org.jitsi.bouncycastle.asn1.ASN1Primitive;
import org.jitsi.bouncycastle.asn1.ASN1Sequence;
import org.jitsi.bouncycastle.asn1.ASN1Set;
import org.jitsi.bouncycastle.asn1.cms.OriginatorInfo;
import org.jitsi.bouncycastle.asn1.x509.Certificate;
import org.jitsi.bouncycastle.asn1.x509.CertificateList;
import org.jitsi.bouncycastle.cert.X509CRLHolder;
import org.jitsi.bouncycastle.cert.X509CertificateHolder;
import org.jitsi.bouncycastle.util.CollectionStore;
import org.jitsi.bouncycastle.util.Store;

public class OriginatorInformation {
    private OriginatorInfo originatorInfo;

    OriginatorInformation(OriginatorInfo originatorInfo) {
        this.originatorInfo = originatorInfo;
    }

    public Store getCRLs() {
        ASN1Set cRLs = this.originatorInfo.getCRLs();
        if (cRLs == null) {
            return new CollectionStore(new ArrayList());
        }
        ArrayList arrayList = new ArrayList(cRLs.size());
        Enumeration objects = cRLs.getObjects();
        while (objects.hasMoreElements()) {
            ASN1Primitive toASN1Primitive = ((ASN1Encodable) objects.nextElement()).toASN1Primitive();
            if (toASN1Primitive instanceof ASN1Sequence) {
                arrayList.add(new X509CRLHolder(CertificateList.getInstance(toASN1Primitive)));
            }
        }
        return new CollectionStore(arrayList);
    }

    public Store getCertificates() {
        ASN1Set certificates = this.originatorInfo.getCertificates();
        if (certificates == null) {
            return new CollectionStore(new ArrayList());
        }
        ArrayList arrayList = new ArrayList(certificates.size());
        Enumeration objects = certificates.getObjects();
        while (objects.hasMoreElements()) {
            ASN1Primitive toASN1Primitive = ((ASN1Encodable) objects.nextElement()).toASN1Primitive();
            if (toASN1Primitive instanceof ASN1Sequence) {
                arrayList.add(new X509CertificateHolder(Certificate.getInstance(toASN1Primitive)));
            }
        }
        return new CollectionStore(arrayList);
    }

    public OriginatorInfo toASN1Structure() {
        return this.originatorInfo;
    }
}
