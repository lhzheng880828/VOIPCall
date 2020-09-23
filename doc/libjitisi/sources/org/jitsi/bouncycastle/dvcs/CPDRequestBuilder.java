package org.jitsi.bouncycastle.dvcs;

import java.io.IOException;
import org.jitsi.bouncycastle.asn1.dvcs.DVCSRequestInformationBuilder;
import org.jitsi.bouncycastle.asn1.dvcs.Data;
import org.jitsi.bouncycastle.asn1.dvcs.ServiceType;

public class CPDRequestBuilder extends DVCSRequestBuilder {
    public CPDRequestBuilder() {
        super(new DVCSRequestInformationBuilder(ServiceType.CPD));
    }

    public DVCSRequest build(byte[] bArr) throws DVCSException, IOException {
        return createDVCRequest(new Data(bArr));
    }
}
