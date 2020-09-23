package gov.nist.javax.sdp.fields;

import org.jitsi.gov.nist.core.Separators;

public class Email extends SDPObject {
    protected String hostName;
    protected String userName;

    public String getUserName() {
        return this.userName;
    }

    public String getHostName() {
        return this.hostName;
    }

    public void setUserName(String u) {
        this.userName = u;
    }

    public void setHostName(String h) {
        this.hostName = h.trim();
    }

    public String encode() {
        return this.userName + Separators.AT + this.hostName;
    }
}
