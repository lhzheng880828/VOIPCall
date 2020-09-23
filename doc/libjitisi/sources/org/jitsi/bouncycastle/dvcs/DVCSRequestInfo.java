package org.jitsi.bouncycastle.dvcs;

import java.math.BigInteger;
import java.util.Date;
import org.jitsi.bouncycastle.asn1.dvcs.DVCSRequestInformation;
import org.jitsi.bouncycastle.asn1.dvcs.DVCSTime;
import org.jitsi.bouncycastle.asn1.x509.GeneralNames;
import org.jitsi.bouncycastle.asn1.x509.PolicyInformation;
import org.jitsi.bouncycastle.tsp.TimeStampToken;
import org.jitsi.bouncycastle.util.Arrays;

public class DVCSRequestInfo {
    private DVCSRequestInformation data;

    public DVCSRequestInfo(DVCSRequestInformation dVCSRequestInformation) {
        this.data = dVCSRequestInformation;
    }

    public DVCSRequestInfo(byte[] bArr) {
        this(DVCSRequestInformation.getInstance(bArr));
    }

    private static boolean clientEqualsServer(Object obj, Object obj2) {
        return (obj == null && obj2 == null) || (obj != null && obj.equals(obj2));
    }

    public static boolean validate(DVCSRequestInfo dVCSRequestInfo, DVCSRequestInfo dVCSRequestInfo2) {
        DVCSRequestInformation dVCSRequestInformation = dVCSRequestInfo.data;
        DVCSRequestInformation dVCSRequestInformation2 = dVCSRequestInfo2.data;
        if (dVCSRequestInformation.getVersion() != dVCSRequestInformation2.getVersion() || !clientEqualsServer(dVCSRequestInformation.getService(), dVCSRequestInformation2.getService()) || !clientEqualsServer(dVCSRequestInformation.getRequestTime(), dVCSRequestInformation2.getRequestTime()) || !clientEqualsServer(dVCSRequestInformation.getRequestPolicy(), dVCSRequestInformation2.getRequestPolicy()) || !clientEqualsServer(dVCSRequestInformation.getExtensions(), dVCSRequestInformation2.getExtensions())) {
            return false;
        }
        if (dVCSRequestInformation.getNonce() != null) {
            if (dVCSRequestInformation2.getNonce() == null) {
                return false;
            }
            byte[] toByteArray = dVCSRequestInformation.getNonce().toByteArray();
            byte[] toByteArray2 = dVCSRequestInformation2.getNonce().toByteArray();
            if (toByteArray2.length < toByteArray.length || !Arrays.areEqual(toByteArray, Arrays.copyOfRange(toByteArray2, 0, toByteArray.length))) {
                return false;
            }
        }
        return true;
    }

    public GeneralNames getDVCSNames() {
        return this.data.getDVCS();
    }

    public GeneralNames getDataLocations() {
        return this.data.getDataLocations();
    }

    public BigInteger getNonce() {
        return this.data.getNonce();
    }

    public PolicyInformation getRequestPolicy() {
        return this.data.getRequestPolicy() != null ? this.data.getRequestPolicy() : null;
    }

    public Date getRequestTime() throws DVCSParsingException {
        DVCSTime requestTime = this.data.getRequestTime();
        if (requestTime == null) {
            return null;
        }
        try {
            return requestTime.getGenTime() != null ? requestTime.getGenTime().getDate() : new TimeStampToken(requestTime.getTimeStampToken()).getTimeStampInfo().getGenTime();
        } catch (Exception e) {
            throw new DVCSParsingException("unable to extract time: " + e.getMessage(), e);
        }
    }

    public GeneralNames getRequester() {
        return this.data.getRequester();
    }

    public int getServiceType() {
        return this.data.getService().getValue().intValue();
    }

    public int getVersion() {
        return this.data.getVersion();
    }

    public DVCSRequestInformation toASN1Structure() {
        return this.data;
    }
}
