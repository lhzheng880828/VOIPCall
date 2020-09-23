package net.java.sip.communicator.impl.protocol.jabber.extensions;

import net.java.sip.communicator.service.protocol.ConferenceDescription;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.util.StringUtils;

public class ConferenceDescriptionPacketExtension extends AbstractPacketExtension {
    public static final String AVAILABLE_ATTR_NAME = "available";
    public static final String CALLID_ATTR_NAME = "callid";
    public static final String CALLID_ELEM_NAME = "callid";
    public static final String CONFERENCE_NAME_ATTR_NAME = "conference_name";
    public static final String ELEMENT_NAME = "conference";
    public static final String NAMESPACE = "http://jitsi.org/protocol/condesc";
    public static final String PASSWORD_ATTR_NAME = "auth";
    public static final String TRANSPORT_ELEM_NAME = "transport";
    public static final String URI_ATTR_NAME = "uri";

    public static class Provider implements PacketExtensionProvider {
        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            ConferenceDescriptionPacketExtension packetExtension = new ConferenceDescriptionPacketExtension();
            int attrCount = parser.getAttributeCount();
            for (int i = 0; i < attrCount; i++) {
                packetExtension.setAttribute(parser.getAttributeName(i), parser.getAttributeValue(i));
            }
            boolean done = false;
            TransportPacketExtension transportExt = null;
            while (!done) {
                switch (parser.next()) {
                    case 2:
                        if (!"transport".equals(parser.getName())) {
                            break;
                        }
                        String transportNs = parser.getNamespace();
                        if (transportNs == null) {
                            break;
                        }
                        transportExt = new TransportPacketExtension(transportNs);
                        break;
                    case 3:
                        String elementName = parser.getName();
                        if (!"conference".equals(elementName)) {
                            if ("transport".equals(elementName) && transportExt != null) {
                                packetExtension.addChildExtension(transportExt);
                                break;
                            }
                        }
                        done = true;
                        break;
                    default:
                        break;
                }
            }
            return packetExtension;
        }
    }

    public static class TransportPacketExtension extends AbstractPacketExtension {
        public TransportPacketExtension(String namespace) {
            super(namespace, "transport");
        }
    }

    public ConferenceDescriptionPacketExtension() {
        this(null, null, null);
    }

    public ConferenceDescriptionPacketExtension(String uri) {
        this(uri, null, null);
    }

    public ConferenceDescriptionPacketExtension(String uri, String callId) {
        this(uri, callId, null);
    }

    public ConferenceDescriptionPacketExtension(String uri, String callId, String password) {
        super(NAMESPACE, "conference");
        if (uri != null) {
            setUri(uri);
        }
        if (callId != null) {
            setCallId(callId);
        }
        if (password != null) {
            setAuth(password);
        }
    }

    public ConferenceDescriptionPacketExtension(ConferenceDescription cd) {
        this(cd.getUri(), cd.getCallId(), cd.getPassword());
        setAvailable(cd.isAvailable());
        if (cd.getDisplayName() != null) {
            setName(cd.getDisplayName());
        }
        for (String transport : cd.getSupportedTransports()) {
            addChildExtension(new TransportPacketExtension(transport));
        }
    }

    public String getUri() {
        return getAttributeAsString("uri");
    }

    public String getCallId() {
        return getAttributeAsString("callid");
    }

    public String getPassword() {
        return getAttributeAsString(PASSWORD_ATTR_NAME);
    }

    public void setUri(String uri) {
        setAttribute("uri", StringUtils.escapeForXML(uri));
    }

    public void setCallId(String callId) {
        setAttribute("callid", callId);
    }

    public void setAuth(String password) {
        setAttribute(PASSWORD_ATTR_NAME, password);
    }

    public void setAvailable(boolean available) {
        setAttribute(AVAILABLE_ATTR_NAME, Boolean.valueOf(available));
    }

    public void setName(String name) {
        setAttribute(CONFERENCE_NAME_ATTR_NAME, StringUtils.escapeForXML(name));
    }

    public boolean isAvailable() {
        return Boolean.parseBoolean(getAttributeAsString(AVAILABLE_ATTR_NAME));
    }

    public void addTransport(String transport) {
        addChildExtension(new TransportPacketExtension(transport));
    }

    public ConferenceDescription toConferenceDescription() {
        ConferenceDescription conferenceDescription = new ConferenceDescription(getUri(), getCallId(), getPassword());
        conferenceDescription.setAvailable(isAvailable());
        conferenceDescription.setDisplayName(getName());
        for (TransportPacketExtension t : getChildExtensionsOfType(TransportPacketExtension.class)) {
            conferenceDescription.addTransport(t.getNamespace());
        }
        return conferenceDescription;
    }

    private String getName() {
        return getAttributeAsString(CONFERENCE_NAME_ATTR_NAME);
    }
}
