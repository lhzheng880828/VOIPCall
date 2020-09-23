package org.jivesoftware.smack.packet;

import org.jitsi.gov.nist.core.Separators;

public class StreamError {
    private String code;

    public StreamError(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public String toString() {
        StringBuilder txt = new StringBuilder();
        txt.append("stream:error (").append(this.code).append(Separators.RPAREN);
        return txt.toString();
    }
}
