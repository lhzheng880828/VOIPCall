package org.jitsi.bouncycastle.cert;

import java.math.BigInteger;
import java.util.Date;
import org.jitsi.bouncycastle.asn1.ASN1Integer;
import org.jitsi.bouncycastle.asn1.x500.X500Name;
import org.jitsi.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.jitsi.bouncycastle.asn1.x509.Time;
import org.jitsi.bouncycastle.asn1.x509.V1TBSCertificateGenerator;
import org.jitsi.bouncycastle.operator.ContentSigner;

public class X509v1CertificateBuilder {
    private V1TBSCertificateGenerator tbsGen;

    public X509v1CertificateBuilder(X500Name x500Name, BigInteger bigInteger, Date date, Date date2, X500Name x500Name2, SubjectPublicKeyInfo subjectPublicKeyInfo) {
        if (x500Name == null) {
            throw new IllegalArgumentException("issuer must not be null");
        } else if (subjectPublicKeyInfo == null) {
            throw new IllegalArgumentException("publicKeyInfo must not be null");
        } else {
            this.tbsGen = new V1TBSCertificateGenerator();
            this.tbsGen.setSerialNumber(new ASN1Integer(bigInteger));
            this.tbsGen.setIssuer(x500Name);
            this.tbsGen.setStartDate(new Time(date));
            this.tbsGen.setEndDate(new Time(date2));
            this.tbsGen.setSubject(x500Name2);
            this.tbsGen.setSubjectPublicKeyInfo(subjectPublicKeyInfo);
        }
    }

    public X509CertificateHolder build(ContentSigner contentSigner) {
        this.tbsGen.setSignature(contentSigner.getAlgorithmIdentifier());
        return CertUtils.generateFullCert(contentSigner, this.tbsGen.generateTBSCertificate());
    }
}
