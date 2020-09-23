package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;

public class JingleIQ extends IQ {
    public static final String ACTION_ATTR_NAME = "action";
    public static final String ELEMENT_NAME = "jingle";
    public static final String INITIATOR_ATTR_NAME = "initiator";
    public static final String NAMESPACE = "urn:xmpp:jingle:1";
    public static final String RESPONDER_ATTR_NAME = "responder";
    public static final String SID_ATTR_NAME = "sid";
    private JingleAction action;
    private final List<ContentPacketExtension> contentList = new ArrayList();
    private String initiator;
    private ReasonPacketExtension reason;
    private String responder;
    private SessionInfoPacketExtension sessionInfo;
    private String sid;

    public String getChildElementXML() {
        StringBuilder bldr = new StringBuilder("<jingle");
        bldr.append(" xmlns='urn:xmpp:jingle:1'");
        bldr.append(" action='" + getAction() + Separators.QUOTE);
        if (this.initiator != null) {
            bldr.append(" initiator='" + getInitiator() + Separators.QUOTE);
        }
        if (this.responder != null) {
            bldr.append(" responder='" + getResponder() + Separators.QUOTE);
        }
        bldr.append(" sid='" + getSID() + Separators.QUOTE);
        String extensionsXML = getExtensionsXML();
        if (this.contentList.size() == 0 && this.reason == null && this.sessionInfo == null && (extensionsXML == null || extensionsXML.length() == 0)) {
            bldr.append("/>");
        } else {
            bldr.append(Separators.GREATER_THAN);
            for (ContentPacketExtension cpe : this.contentList) {
                bldr.append(cpe.toXML());
            }
            if (this.reason != null) {
                bldr.append(this.reason.toXML());
            }
            if (this.sessionInfo != null) {
                bldr.append(this.sessionInfo.toXML());
            }
            if (!(extensionsXML == null || extensionsXML.length() == 0)) {
                bldr.append(extensionsXML);
            }
            bldr.append("</jingle>");
        }
        return bldr.toString();
    }

    public void setSID(String sid) {
        this.sid = sid;
    }

    public String getSID() {
        return this.sid;
    }

    public static String generateSID() {
        return new BigInteger(64, new SecureRandom()).toString(32);
    }

    public void setResponder(String responder) {
        this.responder = responder;
    }

    public String getResponder() {
        return this.responder;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public String getInitiator() {
        return this.initiator;
    }

    public void setAction(JingleAction action) {
        this.action = action;
    }

    public JingleAction getAction() {
        return this.action;
    }

    public void setReason(ReasonPacketExtension reason) {
        this.reason = reason;
    }

    public ReasonPacketExtension getReason() {
        return this.reason;
    }

    public List<ContentPacketExtension> getContentList() {
        ArrayList arrayList;
        synchronized (this.contentList) {
            arrayList = new ArrayList(this.contentList);
        }
        return arrayList;
    }

    public void addContent(ContentPacketExtension contentPacket) {
        synchronized (this.contentList) {
            this.contentList.add(contentPacket);
        }
    }

    public boolean containsContentChildOfType(Class<? extends PacketExtension> contentType) {
        if (getContentForType(contentType) != null) {
            return true;
        }
        return false;
    }

    public ContentPacketExtension getContentForType(Class<? extends PacketExtension> contentType) {
        synchronized (this.contentList) {
            for (ContentPacketExtension content : this.contentList) {
                if (content.getFirstChildOfType(contentType) != null) {
                    return content;
                }
            }
            return null;
        }
    }

    public void setSessionInfo(SessionInfoPacketExtension si) {
        this.sessionInfo = si;
    }

    public SessionInfoPacketExtension getSessionInfo() {
        return this.sessionInfo;
    }
}
