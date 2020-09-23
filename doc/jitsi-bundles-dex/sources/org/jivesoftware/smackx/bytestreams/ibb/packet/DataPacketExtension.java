package org.jivesoftware.smackx.bytestreams.ibb.packet;

import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;

public class DataPacketExtension implements PacketExtension {
    public static final String ELEMENT_NAME = "data";
    private final String data;
    private byte[] decodedData;
    private final long seq;
    private final String sessionID;

    public DataPacketExtension(String sessionID, long seq, String data) {
        if (sessionID == null || "".equals(sessionID)) {
            throw new IllegalArgumentException("Session ID must not be null or empty");
        } else if (seq < 0 || seq > 65535) {
            throw new IllegalArgumentException("Sequence must not be between 0 and 65535");
        } else if (data == null) {
            throw new IllegalArgumentException("Data must not be null");
        } else {
            this.sessionID = sessionID;
            this.seq = seq;
            this.data = data;
        }
    }

    public String getSessionID() {
        return this.sessionID;
    }

    public long getSeq() {
        return this.seq;
    }

    public String getData() {
        return this.data;
    }

    public byte[] getDecodedData() {
        if (this.decodedData != null) {
            return this.decodedData;
        }
        if (this.data.matches(".*={1,2}+.+")) {
            return null;
        }
        this.decodedData = StringUtils.decodeBase64(this.data);
        return this.decodedData;
    }

    public String getElementName() {
        return "data";
    }

    public String getNamespace() {
        return InBandBytestreamManager.NAMESPACE;
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append(Separators.LESS_THAN);
        buf.append(getElementName());
        buf.append(Separators.SP);
        buf.append("xmlns=\"");
        buf.append(InBandBytestreamManager.NAMESPACE);
        buf.append("\" ");
        buf.append("seq=\"");
        buf.append(this.seq);
        buf.append("\" ");
        buf.append("sid=\"");
        buf.append(this.sessionID);
        buf.append("\">");
        buf.append(this.data);
        buf.append("</");
        buf.append(getElementName());
        buf.append(Separators.GREATER_THAN);
        return buf.toString();
    }
}
