package org.jitsi.bouncycastle.dvcs;

import java.io.IOException;
import org.jitsi.bouncycastle.cms.CMSException;
import org.jitsi.bouncycastle.cms.CMSProcessableByteArray;
import org.jitsi.bouncycastle.cms.CMSSignedData;
import org.jitsi.bouncycastle.cms.CMSSignedDataGenerator;

public class SignedDVCSMessageGenerator {
    private final CMSSignedDataGenerator signedDataGen;

    public SignedDVCSMessageGenerator(CMSSignedDataGenerator cMSSignedDataGenerator) {
        this.signedDataGen = cMSSignedDataGenerator;
    }

    public CMSSignedData build(DVCSMessage dVCSMessage) throws DVCSException {
        try {
            return this.signedDataGen.generate(new CMSProcessableByteArray(dVCSMessage.getContentType(), dVCSMessage.getContent().toASN1Primitive().getEncoded("DER")), true);
        } catch (CMSException e) {
            throw new DVCSException("Could not sign DVCS request", e);
        } catch (IOException e2) {
            throw new DVCSException("Could not encode DVCS request", e2);
        }
    }
}
