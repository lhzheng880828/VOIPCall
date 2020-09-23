package org.jitsi.bouncycastle.cert.cmp;

import org.jitsi.bouncycastle.asn1.cmp.CertConfirmContent;
import org.jitsi.bouncycastle.asn1.cmp.CertStatus;
import org.jitsi.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.jitsi.bouncycastle.operator.DigestAlgorithmIdentifierFinder;

public class CertificateConfirmationContent {
    private CertConfirmContent content;
    private DigestAlgorithmIdentifierFinder digestAlgFinder;

    public CertificateConfirmationContent(CertConfirmContent certConfirmContent) {
        this(certConfirmContent, new DefaultDigestAlgorithmIdentifierFinder());
    }

    public CertificateConfirmationContent(CertConfirmContent certConfirmContent, DigestAlgorithmIdentifierFinder digestAlgorithmIdentifierFinder) {
        this.digestAlgFinder = digestAlgorithmIdentifierFinder;
        this.content = certConfirmContent;
    }

    public CertificateStatus[] getStatusMessages() {
        CertStatus[] toCertStatusArray = this.content.toCertStatusArray();
        CertificateStatus[] certificateStatusArr = new CertificateStatus[toCertStatusArray.length];
        for (int i = 0; i != certificateStatusArr.length; i++) {
            certificateStatusArr[i] = new CertificateStatus(this.digestAlgFinder, toCertStatusArray[i]);
        }
        return certificateStatusArr;
    }

    public CertConfirmContent toASN1Structure() {
        return this.content;
    }
}
