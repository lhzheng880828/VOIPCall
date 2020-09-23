package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import net.java.sip.communicator.impl.protocol.jabber.extensions.CallIdPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.ConferenceDescriptionPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.DefaultPacketExtensionProvider;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;

public class JingleIQProvider implements IQProvider {
    public JingleIQProvider() {
        ProviderManager providerManager = ProviderManager.getInstance();
        providerManager.addExtensionProvider("description", "urn:xmpp:jingle:apps:rtp:1", new DefaultPacketExtensionProvider(RtpDescriptionPacketExtension.class));
        providerManager.addExtensionProvider(PayloadTypePacketExtension.ELEMENT_NAME, "urn:xmpp:jingle:apps:rtp:1", new DefaultPacketExtensionProvider(PayloadTypePacketExtension.class));
        providerManager.addExtensionProvider(ParameterPacketExtension.ELEMENT_NAME, "urn:xmpp:jingle:apps:rtp:1", new DefaultPacketExtensionProvider(ParameterPacketExtension.class));
        providerManager.addExtensionProvider(RTPHdrExtPacketExtension.ELEMENT_NAME, "urn:xmpp:jingle:apps:rtp:rtp-hdrext:0", new DefaultPacketExtensionProvider(RTPHdrExtPacketExtension.class));
        providerManager.addExtensionProvider(EncryptionPacketExtension.ELEMENT_NAME, "urn:xmpp:jingle:apps:rtp:1", new DefaultPacketExtensionProvider(EncryptionPacketExtension.class));
        providerManager.addExtensionProvider("zrtp-hash", "urn:xmpp:jingle:apps:rtp:zrtp:1", new DefaultPacketExtensionProvider(ZrtpHashPacketExtension.class));
        providerManager.addExtensionProvider(CryptoPacketExtension.ELEMENT_NAME, "urn:xmpp:jingle:apps:rtp:1", new DefaultPacketExtensionProvider(CryptoPacketExtension.class));
        providerManager.addExtensionProvider("transport", "urn:xmpp:jingle:transports:ice-udp:1", new DefaultPacketExtensionProvider(IceUdpTransportPacketExtension.class));
        providerManager.addExtensionProvider("transport", "urn:xmpp:jingle:transports:raw-udp:1", new DefaultPacketExtensionProvider(RawUdpTransportPacketExtension.class));
        providerManager.addExtensionProvider(CandidatePacketExtension.ELEMENT_NAME, "urn:xmpp:jingle:transports:ice-udp:1", new DefaultPacketExtensionProvider(CandidatePacketExtension.class));
        providerManager.addExtensionProvider(CandidatePacketExtension.ELEMENT_NAME, "urn:xmpp:jingle:transports:raw-udp:1", new DefaultPacketExtensionProvider(CandidatePacketExtension.class));
        providerManager.addExtensionProvider(RemoteCandidatePacketExtension.ELEMENT_NAME, "urn:xmpp:jingle:transports:ice-udp:1", new DefaultPacketExtensionProvider(RemoteCandidatePacketExtension.class));
        providerManager.addExtensionProvider("inputevt", "http://jitsi.org/protocol/inputevt", new DefaultPacketExtensionProvider(InputEvtPacketExtension.class));
        providerManager.addExtensionProvider("conference-info", "", new DefaultPacketExtensionProvider(CoinPacketExtension.class));
        providerManager.addExtensionProvider(DtlsFingerprintPacketExtension.ELEMENT_NAME, "urn:xmpp:jingle:apps:dtls:0", new DefaultPacketExtensionProvider(DtlsFingerprintPacketExtension.class));
        providerManager.addExtensionProvider("transfer", "urn:xmpp:jingle:transfer:0", new DefaultPacketExtensionProvider(TransferPacketExtension.class));
        providerManager.addExtensionProvider(TransferredPacketExtension.ELEMENT_NAME, "urn:xmpp:jingle:transfer:0", new DefaultPacketExtensionProvider(TransferredPacketExtension.class));
        providerManager.addExtensionProvider("callid", ConferenceDescriptionPacketExtension.NAMESPACE, new DefaultPacketExtensionProvider(CallIdPacketExtension.class));
    }

    public JingleIQ parseIQ(XmlPullParser parser) throws Exception {
        JingleIQ jingleIQ = new JingleIQ();
        JingleAction action = JingleAction.parseString(parser.getAttributeValue("", "action"));
        String initiator = parser.getAttributeValue("", "initiator");
        String responder = parser.getAttributeValue("", JingleIQ.RESPONDER_ATTR_NAME);
        String sid = parser.getAttributeValue("", "sid");
        jingleIQ.setAction(action);
        jingleIQ.setInitiator(initiator);
        jingleIQ.setResponder(responder);
        jingleIQ.setSID(sid);
        boolean done = false;
        DefaultPacketExtensionProvider<ContentPacketExtension> contentProvider = new DefaultPacketExtensionProvider(ContentPacketExtension.class);
        ReasonProvider reasonProvider = new ReasonProvider();
        DefaultPacketExtensionProvider<TransferPacketExtension> defaultPacketExtensionProvider = new DefaultPacketExtensionProvider(TransferPacketExtension.class);
        DefaultPacketExtensionProvider<CoinPacketExtension> coinProvider = new DefaultPacketExtensionProvider(CoinPacketExtension.class);
        DefaultPacketExtensionProvider<CallIdPacketExtension> callidProvider = new DefaultPacketExtensionProvider(CallIdPacketExtension.class);
        while (!done) {
            int eventType = parser.next();
            String elementName = parser.getName();
            String namespace = parser.getNamespace();
            if (eventType == 2) {
                if (elementName.equals("content")) {
                    jingleIQ.addContent((ContentPacketExtension) contentProvider.parseExtension(parser));
                } else if (elementName.equals("reason")) {
                    jingleIQ.setReason(reasonProvider.parseExtension(parser));
                } else if (elementName.equals("transfer") && namespace.equals("urn:xmpp:jingle:transfer:0")) {
                    jingleIQ.addExtension(defaultPacketExtensionProvider.parseExtension(parser));
                } else if (elementName.equals("conference-info")) {
                    jingleIQ.addExtension(coinProvider.parseExtension(parser));
                } else if (elementName.equals("callid")) {
                    jingleIQ.addExtension(callidProvider.parseExtension(parser));
                }
                if (namespace.equals(SessionInfoPacketExtension.NAMESPACE)) {
                    SessionInfoType type = SessionInfoType.valueOf(elementName);
                    if (type == SessionInfoType.mute || type == SessionInfoType.unmute) {
                        boolean z;
                        String name = parser.getAttributeValue("", "name");
                        if (type == SessionInfoType.mute) {
                            z = true;
                        } else {
                            z = false;
                        }
                        jingleIQ.setSessionInfo(new MuteSessionInfoPacketExtension(z, name));
                    } else {
                        jingleIQ.setSessionInfo(new SessionInfoPacketExtension(type));
                    }
                }
            }
            if (eventType == 3 && parser.getName().equals(JingleIQ.ELEMENT_NAME)) {
                done = true;
            }
        }
        return jingleIQ;
    }
}
