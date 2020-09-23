package org.jitsi.gov.nist.javax.sip.header;

import org.jitsi.javax.sip.header.ViaHeader;

public interface ViaHeaderExt extends ViaHeader {
    String getSentByField();

    String getSentProtocolField();
}
