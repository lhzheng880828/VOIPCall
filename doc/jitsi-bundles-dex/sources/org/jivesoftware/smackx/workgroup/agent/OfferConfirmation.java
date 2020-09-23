package org.jivesoftware.smackx.workgroup.agent;

import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.provider.IQProvider;

public class OfferConfirmation extends IQ {
    private long sessionID;
    private String userJID;

    private class NotifyServicePacket extends IQ {
        String roomName;

        NotifyServicePacket(String workgroup, String roomName) {
            setTo(workgroup);
            setType(Type.RESULT);
            this.roomName = roomName;
        }

        public String getChildElementXML() {
            return "<offer-confirmation  roomname=\"" + this.roomName + "\" xmlns=\"http://jabber.org/protocol/workgroup" + "\"/>";
        }
    }

    public static class Provider implements IQProvider {
        public IQ parseIQ(XmlPullParser parser) throws Exception {
            OfferConfirmation confirmation = new OfferConfirmation();
            boolean done = false;
            while (!done) {
                parser.next();
                String elementName = parser.getName();
                if (parser.getEventType() == 2 && "user-jid".equals(elementName)) {
                    try {
                        confirmation.setUserJID(parser.nextText());
                    } catch (NumberFormatException e) {
                    }
                } else if (parser.getEventType() == 2 && "session-id".equals(elementName)) {
                    try {
                        confirmation.setSessionID(Long.valueOf(parser.nextText()).longValue());
                    } catch (NumberFormatException e2) {
                    }
                } else if (parser.getEventType() == 3 && "offer-confirmation".equals(elementName)) {
                    done = true;
                }
            }
            return confirmation;
        }
    }

    public String getUserJID() {
        return this.userJID;
    }

    public void setUserJID(String userJID) {
        this.userJID = userJID;
    }

    public long getSessionID() {
        return this.sessionID;
    }

    public void setSessionID(long sessionID) {
        this.sessionID = sessionID;
    }

    public void notifyService(Connection con, String workgroup, String createdRoomName) {
        con.sendPacket(new NotifyServicePacket(workgroup, createdRoomName));
    }

    public String getChildElementXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<offer-confirmation xmlns=\"http://jabber.org/protocol/workgroup\">");
        buf.append("</offer-confirmation>");
        return buf.toString();
    }
}
