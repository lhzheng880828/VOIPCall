package org.jitsi.bouncycastle.cert.cmp;

import java.math.BigInteger;
import org.jitsi.bouncycastle.asn1.cmp.CertStatus;
import org.jitsi.bouncycastle.asn1.cmp.PKIStatusInfo;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.cert.X509CertificateHolder;
import org.jitsi.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import org.jitsi.bouncycastle.operator.DigestCalculator;
import org.jitsi.bouncycastle.operator.DigestCalculatorProvider;
import org.jitsi.bouncycastle.operator.OperatorCreationException;
import org.jitsi.bouncycastle.util.Arrays;

public class CertificateStatus {
    private CertStatus certStatus;
    private DigestAlgorithmIdentifierFinder digestAlgFinder;

    CertificateStatus(DigestAlgorithmIdentifierFinder digestAlgorithmIdentifierFinder, CertStatus certStatus) {
        this.digestAlgFinder = digestAlgorithmIdentifierFinder;
        this.certStatus = certStatus;
    }

    public BigInteger getCertRequestID() {
        return this.certStatus.getCertReqId().getValue();
    }

    public PKIStatusInfo getStatusInfo() {
        return this.certStatus.getStatusInfo();
    }

    public boolean isVerified(X509CertificateHolder x509CertificateHolder, DigestCalculatorProvider digestCalculatorProvider) throws CMPException {
        AlgorithmIdentifier find = this.digestAlgFinder.find(x509CertificateHolder.toASN1Structure().getSignatureAlgorithm());
        if (find == null) {
            throw new CMPException("cannot find algorithm for digest from signature");
        }
        try {
            DigestCalculator digestCalculator = digestCalculatorProvider.get(find);
            CMPUtil.derEncodeToStream(x509CertificateHolder.toASN1Structure(), digestCalculator.getOutputStream());
            return Arrays.areEqual(this.certStatus.getCertHash().getOctets(), digestCalculator.getDigest());
        } catch (OperatorCreationException e) {
            throw new CMPException("unable to create digester: " + e.getMessage(), e);
        }
    }
}
