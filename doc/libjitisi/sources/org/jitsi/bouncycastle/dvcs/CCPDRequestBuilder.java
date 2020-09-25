package org.jitsi.bouncycastle.dvcs;

import org.jitsi.bouncycastle.asn1.dvcs.DVCSRequestInformationBuilder;
import org.jitsi.bouncycastle.asn1.dvcs.Data;
import org.jitsi.bouncycastle.asn1.dvcs.ServiceType;

public class CCPDRequestBuilder extends DVCSRequestBuilder {
    public CCPDRequestBuilder() {
        super(new DVCSRequestInformationBuilder(ServiceType.CCPD));
    }

    public DVCSRequest build(MessageImprint messageImprint) throws DVCSException {
        return createDVCRequest(new Data(messageImprint.toASN1Structure()));
    }
}