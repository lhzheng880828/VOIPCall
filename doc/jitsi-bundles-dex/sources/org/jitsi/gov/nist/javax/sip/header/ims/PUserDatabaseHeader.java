package org.jitsi.gov.nist.javax.sip.header.ims;

import org.jitsi.javax.sip.header.Header;
import org.jitsi.javax.sip.header.Parameters;

public interface PUserDatabaseHeader extends Parameters, Header {
    public static final String NAME = "P-User-Database";

    String getDatabaseName();

    void setDatabaseName(String str);
}
