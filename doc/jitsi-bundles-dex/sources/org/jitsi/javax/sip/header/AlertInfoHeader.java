package org.jitsi.javax.sip.header;

import org.jitsi.javax.sip.address.URI;

public interface AlertInfoHeader extends Parameters, Header {
    public static final String NAME = "Alert-Info";

    URI getAlertInfo();

    void setAlertInfo(URI uri);
}
