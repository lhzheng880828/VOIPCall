package org.jivesoftware.smack.packet;

import net.java.sip.communicator.impl.protocol.jabber.extensions.geolocation.GeolocationPacketExtension;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;

public abstract class IQ extends Packet {
    private Type type = Type.GET;

    /* renamed from: org.jivesoftware.smack.packet.IQ$2 */
    static class AnonymousClass2 extends IQ {
        final /* synthetic */ IQ val$request;

        AnonymousClass2(IQ iq) {
            this.val$request = iq;
        }

        public String getChildElementXML() {
            return this.val$request.getChildElementXML();
        }
    }

    public static class Type {
        public static final Type ERROR = new Type(GeolocationPacketExtension.ERROR);
        public static final Type GET = new Type("get");
        public static final Type RESULT = new Type(Form.TYPE_RESULT);
        public static final Type SET = new Type("set");
        private String value;

        public static Type fromString(String type) {
            if (type == null) {
                return null;
            }
            type = type.toLowerCase();
            if (GET.toString().equals(type)) {
                return GET;
            }
            if (SET.toString().equals(type)) {
                return SET;
            }
            if (ERROR.toString().equals(type)) {
                return ERROR;
            }
            if (RESULT.toString().equals(type)) {
                return RESULT;
            }
            return null;
        }

        private Type(String value) {
            this.value = value;
        }

        public String toString() {
            return this.value;
        }
    }

    public abstract String getChildElementXML();

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        if (type == null) {
            this.type = Type.GET;
        } else {
            this.type = type;
        }
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<iq ");
        if (getPacketID() != null) {
            buf.append("id=\"" + getPacketID() + "\" ");
        }
        if (getTo() != null) {
            buf.append("to=\"").append(StringUtils.escapeForXML(getTo())).append("\" ");
        }
        if (getFrom() != null) {
            buf.append("from=\"").append(StringUtils.escapeForXML(getFrom())).append("\" ");
        }
        if (this.type == null) {
            buf.append("type=\"get\">");
        } else {
            buf.append("type=\"").append(getType()).append("\">");
        }
        String queryXML = getChildElementXML();
        if (queryXML != null) {
            buf.append(queryXML);
        }
        XMPPError error = getError();
        if (error != null) {
            buf.append(error.toXML());
        }
        buf.append("</iq>");
        return buf.toString();
    }

    public static IQ createResultIQ(IQ request) {
        if (request.getType() == Type.GET || request.getType() == Type.SET) {
            IQ result = new IQ() {
                public String getChildElementXML() {
                    return null;
                }
            };
            result.setType(Type.RESULT);
            result.setPacketID(request.getPacketID());
            result.setFrom(request.getTo());
            result.setTo(request.getFrom());
            return result;
        }
        throw new IllegalArgumentException("IQ must be of type 'set' or 'get'. Original IQ: " + request.toXML());
    }

    public static IQ createErrorResponse(IQ request, XMPPError error) {
        if (request.getType() == Type.GET || request.getType() == Type.SET) {
            IQ result = new AnonymousClass2(request);
            result.setType(Type.ERROR);
            result.setPacketID(request.getPacketID());
            result.setFrom(request.getTo());
            result.setTo(request.getFrom());
            result.setError(error);
            return result;
        }
        throw new IllegalArgumentException("IQ must be of type 'set' or 'get'. Original IQ: " + request.toXML());
    }
}
