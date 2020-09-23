package org.jitsi.gov.nist.javax.sip;

import java.io.IOException;
import org.jitsi.javax.sip.ListeningPoint;
import org.jitsi.javax.sip.header.ContactHeader;
import org.jitsi.javax.sip.header.ViaHeader;

public interface ListeningPointExt extends ListeningPoint {
    ContactHeader createContactHeader();

    ViaHeader createViaHeader();

    void sendHeartbeat(String str, int i) throws IOException;
}
