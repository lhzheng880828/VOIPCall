package org.jivesoftware.smack.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.java.sip.communicator.impl.protocol.jabber.ProtocolProviderServiceJabberImpl;
import net.java.sip.communicator.impl.protocol.jabber.extensions.geolocation.GeolocationPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CandidatePacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ParameterPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingleinfo.JingleInfoQueryIQ;
import org.jitsi.android.util.java.beans.PropertyDescriptor;
import org.jitsi.gov.nist.javax.sip.header.ParameterNames;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jitsi.org.xmlpull.v1.XmlPullParserException;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.Authentication;
import org.jivesoftware.smack.packet.Bind;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.PrivacyItem.PrivacyRule;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.packet.RosterPacket.Item;
import org.jivesoftware.smack.packet.RosterPacket.ItemStatus;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;
import org.jivesoftware.smack.packet.StreamError;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.XMPPError.Condition;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.sasl.SASLMechanism.Failure;
import org.jivesoftware.smackx.FormField;

public class PacketParserUtils {
    private static final String PROPERTIES_NAMESPACE = "http://www.jivesoftware.com/xmlns/xmpp/properties";

    public static Packet parseMessage(XmlPullParser parser) throws Exception {
        String defaultLanguage;
        Message message = new Message();
        String id = parser.getAttributeValue("", "id");
        if (id == null) {
            id = Packet.ID_NOT_AVAILABLE;
        }
        message.setPacketID(id);
        message.setTo(parser.getAttributeValue("", "to"));
        message.setFrom(parser.getAttributeValue("", "from"));
        message.setType(Type.fromString(parser.getAttributeValue("", "type")));
        String language = getLanguageAttribute(parser);
        if (language == null || "".equals(language.trim())) {
            defaultLanguage = Packet.getDefaultLanguage();
        } else {
            message.setLanguage(language);
            defaultLanguage = language;
        }
        boolean done = false;
        String thread = null;
        Map<String, Object> properties = null;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2) {
                String elementName = parser.getName();
                String namespace = parser.getNamespace();
                String xmlLang;
                if (elementName.equals("subject")) {
                    xmlLang = getLanguageAttribute(parser);
                    if (xmlLang == null) {
                        xmlLang = defaultLanguage;
                    }
                    String subject = parseContent(parser);
                    if (message.getSubject(xmlLang) == null) {
                        message.addSubject(xmlLang, subject);
                    }
                } else if (elementName.equals("body")) {
                    xmlLang = getLanguageAttribute(parser);
                    if (xmlLang == null) {
                        xmlLang = defaultLanguage;
                    }
                    String body = parseContent(parser);
                    if (message.getBody(xmlLang) == null) {
                        message.addBody(xmlLang, body);
                    }
                } else if (elementName.equals("thread")) {
                    if (thread == null) {
                        thread = parser.nextText();
                    }
                } else if (elementName.equals(GeolocationPacketExtension.ERROR)) {
                    message.setError(parseError(parser));
                } else if (elementName.equals("properties") && namespace.equals(PROPERTIES_NAMESPACE)) {
                    properties = parseProperties(parser);
                } else {
                    message.addExtension(parsePacketExtension(elementName, namespace, parser));
                }
            } else if (eventType == 3 && parser.getName().equals("message")) {
                done = true;
            }
        }
        message.setThread(thread);
        if (properties != null) {
            for (String name : properties.keySet()) {
                message.setProperty(name, properties.get(name));
            }
        }
        return message;
    }

    private static String parseContent(XmlPullParser parser) throws XmlPullParserException, IOException {
        String content = "";
        int parserDepth = parser.getDepth();
        while (true) {
            if (parser.next() == 3 && parser.getDepth() == parserDepth) {
                return content;
            }
            content = content + parser.getText();
        }
    }

    public static Presence parsePresence(XmlPullParser parser) throws Exception {
        String str;
        Presence.Type type = Presence.Type.available;
        String typeString = parser.getAttributeValue("", "type");
        if (!(typeString == null || typeString.equals(""))) {
            try {
                type = Presence.Type.valueOf(typeString);
            } catch (IllegalArgumentException e) {
                System.err.println("Found invalid presence type " + typeString);
            }
        }
        Presence presence = new Presence(type);
        presence.setTo(parser.getAttributeValue("", "to"));
        presence.setFrom(parser.getAttributeValue("", "from"));
        String id = parser.getAttributeValue("", "id");
        if (id == null) {
            str = Packet.ID_NOT_AVAILABLE;
        } else {
            str = id;
        }
        presence.setPacketID(str);
        String language = getLanguageAttribute(parser);
        if (!(language == null || "".equals(language.trim()))) {
            presence.setLanguage(language);
        }
        if (id == null) {
            id = Packet.ID_NOT_AVAILABLE;
        }
        presence.setPacketID(id);
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2) {
                String elementName = parser.getName();
                String namespace = parser.getNamespace();
                if (elementName.equals("status")) {
                    presence.setStatus(parser.nextText());
                } else if (elementName.equals(CandidatePacketExtension.PRIORITY_ATTR_NAME)) {
                    try {
                        presence.setPriority(Integer.parseInt(parser.nextText()));
                    } catch (NumberFormatException e2) {
                    } catch (IllegalArgumentException e3) {
                        presence.setPriority(0);
                    }
                } else if (elementName.equals("show")) {
                    String modeText = parser.nextText();
                    try {
                        presence.setMode(Mode.valueOf(modeText));
                    } catch (IllegalArgumentException e4) {
                        System.err.println("Found invalid presence mode " + modeText);
                    }
                } else if (elementName.equals(GeolocationPacketExtension.ERROR)) {
                    presence.setError(parseError(parser));
                } else if (elementName.equals("properties") && namespace.equals(PROPERTIES_NAMESPACE)) {
                    Map<String, Object> properties = parseProperties(parser);
                    for (String name : properties.keySet()) {
                        presence.setProperty(name, properties.get(name));
                    }
                } else {
                    presence.addExtension(parsePacketExtension(elementName, namespace, parser));
                }
            } else if (eventType == 3 && parser.getName().equals("presence")) {
                done = true;
            }
        }
        return presence;
    }

    public static IQ parseIQ(XmlPullParser parser, Connection connection) throws Exception {
        IQ iqPacket = null;
        String id = parser.getAttributeValue("", "id");
        String to = parser.getAttributeValue("", "to");
        String from = parser.getAttributeValue("", "from");
        IQ.Type type = IQ.Type.fromString(parser.getAttributeValue("", "type"));
        XMPPError error = null;
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2) {
                String elementName = parser.getName();
                String namespace = parser.getNamespace();
                if (elementName.equals(GeolocationPacketExtension.ERROR)) {
                    error = parseError(parser);
                } else if (elementName.equals(JingleInfoQueryIQ.ELEMENT_NAME) && namespace.equals("jabber:iq:auth")) {
                    iqPacket = parseAuthentication(parser);
                } else if (elementName.equals(JingleInfoQueryIQ.ELEMENT_NAME) && namespace.equals("jabber:iq:roster")) {
                    iqPacket = parseRoster(parser);
                } else if (elementName.equals(JingleInfoQueryIQ.ELEMENT_NAME) && namespace.equals(ProtocolProviderServiceJabberImpl.URN_REGISTER)) {
                    iqPacket = parseRegistration(parser);
                } else if (elementName.equals("bind") && namespace.equals("urn:ietf:params:xml:ns:xmpp-bind")) {
                    iqPacket = parseResourceBinding(parser);
                } else {
                    Object provider = ProviderManager.getInstance().getIQProvider(elementName, namespace);
                    if (provider != null) {
                        if (provider instanceof IQProvider) {
                            iqPacket = ((IQProvider) provider).parseIQ(parser);
                        } else if (provider instanceof Class) {
                            iqPacket = (IQ) parseWithIntrospection(elementName, (Class) provider, parser);
                        }
                    }
                }
            } else if (eventType == 3 && parser.getName().equals("iq")) {
                done = true;
            }
        }
        if (iqPacket == null) {
            if (IQ.Type.GET == type || IQ.Type.SET == type) {
                iqPacket = new IQ() {
                    public String getChildElementXML() {
                        return null;
                    }
                };
                iqPacket.setPacketID(id);
                iqPacket.setTo(from);
                iqPacket.setFrom(to);
                iqPacket.setType(IQ.Type.ERROR);
                iqPacket.setError(new XMPPError(Condition.service_unavailable));
                connection.sendPacket(iqPacket);
                return null;
            }
            iqPacket = new IQ() {
                public String getChildElementXML() {
                    return null;
                }
            };
        }
        iqPacket.setPacketID(id);
        iqPacket.setTo(to);
        iqPacket.setFrom(from);
        iqPacket.setType(type);
        iqPacket.setError(error);
        return iqPacket;
    }

    private static Authentication parseAuthentication(XmlPullParser parser) throws Exception {
        Authentication authentication = new Authentication();
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2) {
                if (parser.getName().equals("username")) {
                    authentication.setUsername(parser.nextText());
                } else if (parser.getName().equals(ParameterNames.PASSWORD)) {
                    authentication.setPassword(parser.nextText());
                } else if (parser.getName().equals("digest")) {
                    authentication.setDigest(parser.nextText());
                } else if (parser.getName().equals("resource")) {
                    authentication.setResource(parser.nextText());
                }
            } else if (eventType == 3 && parser.getName().equals(JingleInfoQueryIQ.ELEMENT_NAME)) {
                done = true;
            }
        }
        return authentication;
    }

    private static RosterPacket parseRoster(XmlPullParser parser) throws Exception {
        RosterPacket roster = new RosterPacket();
        boolean done = false;
        Item item = null;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2) {
                if (parser.getName().equals("item")) {
                    item = new Item(parser.getAttributeValue("", "jid"), parser.getAttributeValue("", "name"));
                    item.setItemStatus(ItemStatus.fromString(parser.getAttributeValue("", "ask")));
                    String subscription = parser.getAttributeValue("", "subscription");
                    if (subscription == null) {
                        subscription = PrivacyRule.SUBSCRIPTION_NONE;
                    }
                    item.setItemType(ItemType.valueOf(subscription));
                }
                if (parser.getName().equals("group") && item != null) {
                    String groupName = parser.nextText();
                    if (groupName != null && groupName.trim().length() > 0) {
                        item.addGroupName(groupName);
                    }
                }
            } else if (eventType == 3) {
                if (parser.getName().equals("item")) {
                    roster.addRosterItem(item);
                }
                if (parser.getName().equals(JingleInfoQueryIQ.ELEMENT_NAME)) {
                    done = true;
                }
            }
        }
        return roster;
    }

    private static Registration parseRegistration(XmlPullParser parser) throws Exception {
        Registration registration = new Registration();
        Map<String, String> fields = null;
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2) {
                if (parser.getNamespace().equals(ProtocolProviderServiceJabberImpl.URN_REGISTER)) {
                    String name = parser.getName();
                    String value = "";
                    if (fields == null) {
                        fields = new HashMap();
                    }
                    if (parser.next() == 4) {
                        value = parser.getText();
                    }
                    if (name.equals("instructions")) {
                        registration.setInstructions(value);
                    } else {
                        fields.put(name, value);
                    }
                } else {
                    registration.addExtension(parsePacketExtension(parser.getName(), parser.getNamespace(), parser));
                }
            } else if (eventType == 3 && parser.getName().equals(JingleInfoQueryIQ.ELEMENT_NAME)) {
                done = true;
            }
        }
        registration.setAttributes(fields);
        return registration;
    }

    private static Bind parseResourceBinding(XmlPullParser parser) throws IOException, XmlPullParserException {
        Bind bind = new Bind();
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2) {
                if (parser.getName().equals("resource")) {
                    bind.setResource(parser.nextText());
                } else if (parser.getName().equals("jid")) {
                    bind.setJid(parser.nextText());
                }
            } else if (eventType == 3 && parser.getName().equals("bind")) {
                done = true;
            }
        }
        return bind;
    }

    public static Collection<String> parseMechanisms(XmlPullParser parser) throws Exception {
        List<String> mechanisms = new ArrayList();
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2) {
                if (parser.getName().equals("mechanism")) {
                    mechanisms.add(parser.nextText());
                }
            } else if (eventType == 3 && parser.getName().equals("mechanisms")) {
                done = true;
            }
        }
        return mechanisms;
    }

    public static Collection<String> parseCompressionMethods(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<String> methods = new ArrayList();
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2) {
                if (parser.getName().equals("method")) {
                    methods.add(parser.nextText());
                }
            } else if (eventType == 3 && parser.getName().equals("compression")) {
                done = true;
            }
        }
        return methods;
    }

    public static Map<String, Object> parseProperties(XmlPullParser parser) throws Exception {
        Map<String, Object> properties = new HashMap();
        while (true) {
            int eventType = parser.next();
            if (eventType == 2 && parser.getName().equals("property")) {
                boolean done = false;
                String name = null;
                String type = null;
                String valueText = null;
                Object value = null;
                while (!done) {
                    eventType = parser.next();
                    if (eventType == 2) {
                        String elementName = parser.getName();
                        if (elementName.equals("name")) {
                            name = parser.nextText();
                        } else if (elementName.equals(ParameterPacketExtension.VALUE_ATTR_NAME)) {
                            type = parser.getAttributeValue("", "type");
                            valueText = parser.nextText();
                        }
                    } else if (eventType == 3 && parser.getName().equals("property")) {
                        if ("integer".equals(type)) {
                            value = Integer.valueOf(valueText);
                        } else if ("long".equals(type)) {
                            value = Long.valueOf(valueText);
                        } else if ("float".equals(type)) {
                            value = Float.valueOf(valueText);
                        } else if ("double".equals(type)) {
                            value = Double.valueOf(valueText);
                        } else if (FormField.TYPE_BOOLEAN.equals(type)) {
                            value = Boolean.valueOf(valueText);
                        } else if ("string".equals(type)) {
                            String value2 = valueText;
                        } else if ("java-object".equals(type)) {
                            try {
                                value = new ObjectInputStream(new ByteArrayInputStream(StringUtils.decodeBase64(valueText))).readObject();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (!(name == null || value == null)) {
                            properties.put(name, value);
                        }
                        done = true;
                    }
                }
            } else if (eventType == 3 && parser.getName().equals("properties")) {
                return properties;
            }
        }
    }

    public static Failure parseSASLFailure(XmlPullParser parser) throws Exception {
        String condition = null;
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2) {
                if (!parser.getName().equals("failure")) {
                    condition = parser.getName();
                }
            } else if (eventType == 3 && parser.getName().equals("failure")) {
                done = true;
            }
        }
        return new Failure(condition);
    }

    public static StreamError parseStreamError(XmlPullParser parser) throws IOException, XmlPullParserException {
        StreamError streamError = null;
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2) {
                streamError = new StreamError(parser.getName());
            } else if (eventType == 3 && parser.getName().equals(GeolocationPacketExtension.ERROR)) {
                done = true;
            }
        }
        return streamError;
    }

    public static XMPPError parseError(XmlPullParser parser) throws Exception {
        String errorNamespace = "urn:ietf:params:xml:ns:xmpp-stanzas";
        String errorCode = "-1";
        String type = null;
        String message = null;
        String condition = null;
        List<PacketExtension> extensions = new ArrayList();
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (parser.getAttributeName(i).equals("code")) {
                errorCode = parser.getAttributeValue("", "code");
            }
            if (parser.getAttributeName(i).equals("type")) {
                type = parser.getAttributeValue("", "type");
            }
        }
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2) {
                if (parser.getName().equals("text")) {
                    message = parser.nextText();
                } else {
                    String elementName = parser.getName();
                    String namespace = parser.getNamespace();
                    if ("urn:ietf:params:xml:ns:xmpp-stanzas".equals(namespace)) {
                        condition = elementName;
                    } else {
                        extensions.add(parsePacketExtension(elementName, namespace, parser));
                    }
                }
            } else if (eventType == 3 && parser.getName().equals(GeolocationPacketExtension.ERROR)) {
                done = true;
            }
        }
        XMPPError.Type errorType = XMPPError.Type.CANCEL;
        if (type != null) {
            try {
                errorType = XMPPError.Type.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException iae) {
                iae.printStackTrace();
            }
        }
        return new XMPPError(Integer.parseInt(errorCode), errorType, condition, message, extensions);
    }

    public static PacketExtension parsePacketExtension(String elementName, String namespace, XmlPullParser parser) throws Exception {
        Object provider = ProviderManager.getInstance().getExtensionProvider(elementName, namespace);
        if (provider != null) {
            if (provider instanceof PacketExtensionProvider) {
                return ((PacketExtensionProvider) provider).parseExtension(parser);
            }
            if (provider instanceof Class) {
                return (PacketExtension) parseWithIntrospection(elementName, (Class) provider, parser);
            }
        }
        DefaultPacketExtension extension = new DefaultPacketExtension(elementName, namespace);
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2) {
                String name = parser.getName();
                if (parser.isEmptyElementTag()) {
                    extension.setValue(name, "");
                } else if (parser.next() == 4) {
                    extension.setValue(name, parser.getText());
                }
            } else if (eventType == 3 && parser.getName().equals(elementName)) {
                done = true;
            }
        }
        return extension;
    }

    private static String getLanguageAttribute(XmlPullParser parser) {
        int i = 0;
        while (i < parser.getAttributeCount()) {
            String attributeName = parser.getAttributeName(i);
            if ("xml:lang".equals(attributeName) || ("lang".equals(attributeName) && "xml".equals(parser.getAttributePrefix(i)))) {
                return parser.getAttributeValue(i);
            }
            i++;
        }
        return null;
    }

    public static Object parseWithIntrospection(String elementName, Class objectClass, XmlPullParser parser) throws Exception {
        boolean done = false;
        Object object = objectClass.newInstance();
        while (!done) {
            int eventType = parser.next();
            if (eventType == 2) {
                String name = parser.getName();
                String stringValue = parser.nextText();
                PropertyDescriptor descriptor = new PropertyDescriptor(name, objectClass);
                Object value = decode(descriptor.getPropertyType(), stringValue);
                descriptor.getWriteMethod().invoke(object, new Object[]{value});
            } else if (eventType == 3 && parser.getName().equals(elementName)) {
                done = true;
            }
        }
        return object;
    }

    private static Object decode(Class type, String value) throws Exception {
        if (type.getName().equals("java.lang.String")) {
            return value;
        }
        if (type.getName().equals(FormField.TYPE_BOOLEAN)) {
            return Boolean.valueOf(value);
        }
        if (type.getName().equals("int")) {
            return Integer.valueOf(value);
        }
        if (type.getName().equals("long")) {
            return Long.valueOf(value);
        }
        if (type.getName().equals("float")) {
            return Float.valueOf(value);
        }
        if (type.getName().equals("double")) {
            return Double.valueOf(value);
        }
        if (type.getName().equals("java.lang.Class")) {
            return Class.forName(value);
        }
        return null;
    }
}
