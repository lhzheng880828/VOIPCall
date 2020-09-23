package org.jivesoftware.smackx.bytestreams.ibb.packet;

import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager.StanzaType;

public class Open extends IQ {
    private final int blockSize;
    private final String sessionID;
    private final StanzaType stanza;

    public Open(String sessionID, int blockSize, StanzaType stanza) {
        if (sessionID == null || "".equals(sessionID)) {
            throw new IllegalArgumentException("Session ID must not be null or empty");
        } else if (blockSize <= 0) {
            throw new IllegalArgumentException("Block size must be greater than zero");
        } else {
            this.sessionID = sessionID;
            this.blockSize = blockSize;
            this.stanza = stanza;
            setType(Type.SET);
        }
    }

    public Open(String sessionID, int blockSize) {
        this(sessionID, blockSize, StanzaType.IQ);
    }

    public String getSessionID() {
        return this.sessionID;
    }

    public int getBlockSize() {
        return this.blockSize;
    }

    public StanzaType getStanza() {
        return this.stanza;
    }

    public String getChildElementXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<open ");
        buf.append("xmlns=\"");
        buf.append(InBandBytestreamManager.NAMESPACE);
        buf.append("\" ");
        buf.append("block-size=\"");
        buf.append(this.blockSize);
        buf.append("\" ");
        buf.append("sid=\"");
        buf.append(this.sessionID);
        buf.append("\" ");
        buf.append("stanza=\"");
        buf.append(this.stanza.toString().toLowerCase());
        buf.append(Separators.DOUBLE_QUOTE);
        buf.append("/>");
        return buf.toString();
    }
}
