package org.jivesoftware.smackx.provider;

import net.java.sip.communicator.impl.protocol.jabber.extensions.geolocation.GeolocationPacketExtension;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.jivesoftware.smackx.commands.AdHocCommand.Action;
import org.jivesoftware.smackx.commands.AdHocCommand.SpecificErrorCondition;
import org.jivesoftware.smackx.commands.AdHocCommand.Status;
import org.jivesoftware.smackx.commands.AdHocCommandNote;
import org.jivesoftware.smackx.commands.AdHocCommandNote.Type;
import org.jivesoftware.smackx.packet.AdHocCommandData;
import org.jivesoftware.smackx.packet.AdHocCommandData.SpecificError;
import org.jivesoftware.smackx.packet.DataForm;

public class AdHocCommandDataProvider implements IQProvider {

    public static class BadActionError implements PacketExtensionProvider {
        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            return new SpecificError(SpecificErrorCondition.badAction);
        }
    }

    public static class BadLocaleError implements PacketExtensionProvider {
        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            return new SpecificError(SpecificErrorCondition.badLocale);
        }
    }

    public static class BadPayloadError implements PacketExtensionProvider {
        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            return new SpecificError(SpecificErrorCondition.badPayload);
        }
    }

    public static class BadSessionIDError implements PacketExtensionProvider {
        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            return new SpecificError(SpecificErrorCondition.badSessionid);
        }
    }

    public static class MalformedActionError implements PacketExtensionProvider {
        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            return new SpecificError(SpecificErrorCondition.malformedAction);
        }
    }

    public static class SessionExpiredError implements PacketExtensionProvider {
        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            return new SpecificError(SpecificErrorCondition.sessionExpired);
        }
    }

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        boolean done = false;
        AdHocCommandData adHocCommandData = new AdHocCommandData();
        DataFormProvider dataFormProvider = new DataFormProvider();
        adHocCommandData.setSessionID(parser.getAttributeValue("", "sessionid"));
        adHocCommandData.setNode(parser.getAttributeValue("", "node"));
        String status = parser.getAttributeValue("", "status");
        if (Status.executing.toString().equalsIgnoreCase(status)) {
            adHocCommandData.setStatus(Status.executing);
        } else if (Status.completed.toString().equalsIgnoreCase(status)) {
            adHocCommandData.setStatus(Status.completed);
        } else if (Status.canceled.toString().equalsIgnoreCase(status)) {
            adHocCommandData.setStatus(Status.canceled);
        }
        String action = parser.getAttributeValue("", "action");
        if (action != null) {
            Action realAction = Action.valueOf(action);
            if (realAction == null || realAction.equals(Action.unknown)) {
                adHocCommandData.setAction(Action.unknown);
            } else {
                adHocCommandData.setAction(realAction);
            }
        }
        while (!done) {
            int eventType = parser.next();
            String elementName = parser.getName();
            String namespace = parser.getNamespace();
            if (eventType == 2) {
                if (parser.getName().equals("actions")) {
                    String execute = parser.getAttributeValue("", "execute");
                    if (execute != null) {
                        adHocCommandData.setExecuteAction(Action.valueOf(execute));
                    }
                } else if (parser.getName().equals("next")) {
                    adHocCommandData.addAction(Action.next);
                } else if (parser.getName().equals("complete")) {
                    adHocCommandData.addAction(Action.complete);
                } else if (parser.getName().equals("prev")) {
                    adHocCommandData.addAction(Action.prev);
                } else if (elementName.equals("x") && namespace.equals("jabber:x:data")) {
                    adHocCommandData.setForm((DataForm) dataFormProvider.parseExtension(parser));
                } else if (parser.getName().equals("note")) {
                    adHocCommandData.addNote(new AdHocCommandNote(Type.valueOf(parser.getAttributeValue("", "type")), parser.nextText()));
                } else if (parser.getName().equals(GeolocationPacketExtension.ERROR)) {
                    adHocCommandData.setError(PacketParserUtils.parseError(parser));
                }
            } else if (eventType == 3 && parser.getName().equals("command")) {
                done = true;
            }
        }
        return adHocCommandData;
    }
}
