package org.jivesoftware.smackx.workgroup.packet;

import net.java.sip.communicator.impl.protocol.jabber.extensions.geolocation.GeolocationPacketExtension;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;

public class RoomTransfer implements PacketExtension {
    public static final String ELEMENT_NAME = "transfer";
    public static final String NAMESPACE = "http://jabber.org/protocol/workgroup";
    /* access modifiers changed from: private */
    public String invitee;
    /* access modifiers changed from: private */
    public String inviter;
    /* access modifiers changed from: private */
    public String reason;
    /* access modifiers changed from: private */
    public String room;
    /* access modifiers changed from: private */
    public String sessionID;
    /* access modifiers changed from: private */
    public Type type;

    public static class Provider implements PacketExtensionProvider {
        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            RoomTransfer invitation = new RoomTransfer();
            invitation.type = Type.valueOf(parser.getAttributeValue("", "type"));
            boolean done = false;
            while (!done) {
                parser.next();
                String elementName = parser.getName();
                if (parser.getEventType() == 2) {
                    if ("session".equals(elementName)) {
                        invitation.sessionID = parser.getAttributeValue("", "id");
                    } else if ("invitee".equals(elementName)) {
                        invitation.invitee = parser.nextText();
                    } else if ("inviter".equals(elementName)) {
                        invitation.inviter = parser.nextText();
                    } else if ("reason".equals(elementName)) {
                        invitation.reason = parser.nextText();
                    } else if (GeolocationPacketExtension.ROOM.equals(elementName)) {
                        invitation.room = parser.nextText();
                    }
                } else if (parser.getEventType() == 3 && "transfer".equals(elementName)) {
                    done = true;
                }
            }
            return invitation;
        }
    }

    public enum Type {
        user,
        queue,
        workgroup
    }

    public RoomTransfer(Type type, String invitee, String sessionID, String reason) {
        this.type = type;
        this.invitee = invitee;
        this.sessionID = sessionID;
        this.reason = reason;
    }

    private RoomTransfer() {
    }

    public String getElementName() {
        return "transfer";
    }

    public String getNamespace() {
        return "http://jabber.org/protocol/workgroup";
    }

    public String getInviter() {
        return this.inviter;
    }

    public String getRoom() {
        return this.room;
    }

    public String getReason() {
        return this.reason;
    }

    public String getSessionID() {
        return this.sessionID;
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append(Separators.LESS_THAN).append("transfer").append(" xmlns=\"").append("http://jabber.org/protocol/workgroup");
        buf.append("\" type=\"").append(this.type).append("\">");
        buf.append("<session xmlns=\"http://jivesoftware.com/protocol/workgroup\" id=\"").append(this.sessionID).append("\"></session>");
        if (this.invitee != null) {
            buf.append("<invitee>").append(this.invitee).append("</invitee>");
        }
        if (this.inviter != null) {
            buf.append("<inviter>").append(this.inviter).append("</inviter>");
        }
        if (this.reason != null) {
            buf.append("<reason>").append(this.reason).append("</reason>");
        }
        buf.append("</").append("transfer").append("> ");
        return buf.toString();
    }
}
