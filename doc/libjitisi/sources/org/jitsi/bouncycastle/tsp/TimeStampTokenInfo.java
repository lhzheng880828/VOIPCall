package org.jitsi.bouncycastle.tsp;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.tsp.Accuracy;
import org.jitsi.bouncycastle.asn1.tsp.TSTInfo;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.asn1.x509.GeneralName;

public class TimeStampTokenInfo {
    Date genTime;
    TSTInfo tstInfo;

    TimeStampTokenInfo(TSTInfo tSTInfo) throws TSPException, IOException {
        this.tstInfo = tSTInfo;
        try {
            this.genTime = tSTInfo.getGenTime().getDate();
        } catch (ParseException e) {
            throw new TSPException("unable to parse genTime field");
        }
    }

    public Accuracy getAccuracy() {
        return this.tstInfo.getAccuracy();
    }

    public byte[] getEncoded() throws IOException {
        return this.tstInfo.getEncoded();
    }

    public Date getGenTime() {
        return this.genTime;
    }

    public GenTimeAccuracy getGenTimeAccuracy() {
        return getAccuracy() != null ? new GenTimeAccuracy(getAccuracy()) : null;
    }

    public AlgorithmIdentifier getHashAlgorithm() {
        return this.tstInfo.getMessageImprint().getHashAlgorithm();
    }

    public ASN1ObjectIdentifier getMessageImprintAlgOID() {
        return this.tstInfo.getMessageImprint().getHashAlgorithm().getAlgorithm();
    }

    public byte[] getMessageImprintDigest() {
        return this.tstInfo.getMessageImprint().getHashedMessage();
    }

    public BigInteger getNonce() {
        return this.tstInfo.getNonce() != null ? this.tstInfo.getNonce().getValue() : null;
    }

    public ASN1ObjectIdentifier getPolicy() {
        return this.tstInfo.getPolicy();
    }

    public BigInteger getSerialNumber() {
        return this.tstInfo.getSerialNumber().getValue();
    }

    public GeneralName getTsa() {
        return this.tstInfo.getTsa();
    }

    public boolean isOrdered() {
        return this.tstInfo.getOrdering().isTrue();
    }

    public TSTInfo toASN1Structure() {
        return this.tstInfo;
    }

    public TSTInfo toTSTInfo() {
        return this.tstInfo;
    }
}
