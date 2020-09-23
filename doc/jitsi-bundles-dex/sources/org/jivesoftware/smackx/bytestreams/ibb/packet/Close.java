package org.jivesoftware.smackx.bytestreams.ibb.packet;

import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;

public class Close extends IQ {
    private final String sessionID;

    public Close(String sessionID) {
        if (sessionID == null || "".equals(sessionID)) {
            throw new IllegalArgumentException("Session ID must not be null or empty");
        }
        this.sessionID = sessionID;
        setType(Type.SET);
    }

    public String getSessionID() {
        return this.sessionID;
    }

    public String getChildElementXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<close ");
        buf.append("xmlns=\"");
        buf.append(InBandBytestreamManager.NAMESPACE);
        buf.append("\" ");
        buf.append("sid=\"");
        buf.append(this.sessionID);
        buf.append(Separators.DOUBLE_QUOTE);
        buf.append("/>");
        return buf.toString();
    }
}
