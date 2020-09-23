package net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail;

import net.java.sip.communicator.util.Base64;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.provider.IQProvider;

public class ThumbnailIQ extends IQ implements IQProvider {
    public static final String CID = "cid";
    public static final String ELEMENT_NAME = "data";
    public static final String NAMESPACE = "urn:xmpp:bob";
    public static final String TYPE = "type";
    private String cid;
    private byte[] data;
    private String mimeType;

    public ThumbnailIQ(String from, String to, String cid, Type type) {
        this.cid = cid;
        setFrom(from);
        setTo(to);
        setType(type);
    }

    public ThumbnailIQ(String from, String to, String cid, String mimeType, byte[] data, Type type) {
        this(from, to, cid, type);
        this.data = data;
        this.mimeType = mimeType;
    }

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        String elementName = parser.getName();
        String namespace = parser.getNamespace();
        if (elementName.equals("data") && namespace.equals(NAMESPACE)) {
            this.cid = parser.getAttributeValue("", "cid");
            this.mimeType = parser.getAttributeValue("", "type");
        }
        if (parser.next() == 4) {
            this.data = Base64.decode(parser.getText());
        }
        return this;
    }

    public String getChildElementXML() {
        StringBuffer buf = new StringBuffer();
        buf.append(Separators.LESS_THAN).append("data").append(" xmlns=\"").append(NAMESPACE).append(Separators.DOUBLE_QUOTE).append(" cid").append("=\"").append(this.cid).append(Separators.DOUBLE_QUOTE);
        if (this.mimeType != null) {
            buf.append(" type").append("=\"").append(this.mimeType).append("\">");
        } else {
            buf.append(Separators.GREATER_THAN);
        }
        if (this.data != null) {
            buf.append(new String(Base64.encode(this.data)));
        }
        buf.append("</data>");
        return buf.toString();
    }

    public String getCid() {
        return this.cid;
    }

    public byte[] getData() {
        return this.data;
    }
}
