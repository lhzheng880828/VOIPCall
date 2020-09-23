package org.jitsi.bouncycastle.cert.cmp;

import java.math.BigInteger;
import org.jitsi.bouncycastle.asn1.cmp.RevDetails;
import org.jitsi.bouncycastle.asn1.x500.X500Name;

public class RevocationDetails {
    private RevDetails revDetails;

    public RevocationDetails(RevDetails revDetails) {
        this.revDetails = revDetails;
    }

    public X500Name getIssuer() {
        return this.revDetails.getCertDetails().getIssuer();
    }

    public BigInteger getSerialNumber() {
        return this.revDetails.getCertDetails().getSerialNumber().getValue();
    }

    public X500Name getSubject() {
        return this.revDetails.getCertDetails().getSubject();
    }

    public RevDetails toASN1Structure() {
        return this.revDetails;
    }
}
