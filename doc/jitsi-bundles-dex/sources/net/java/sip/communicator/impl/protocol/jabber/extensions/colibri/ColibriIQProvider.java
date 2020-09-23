package net.java.sip.communicator.impl.protocol.jabber.extensions.colibri;

import net.java.sip.communicator.impl.protocol.jabber.extensions.DefaultPacketExtensionProvider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriConferenceIQ.Channel;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriConferenceIQ.Content;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.IceUdpTransportPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ParameterPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.PayloadTypePacketExtension;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jitsi.service.neomedia.MediaDirection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;

public class ColibriIQProvider implements IQProvider {
    public ColibriIQProvider() {
        ProviderManager providerManager = ProviderManager.getInstance();
        providerManager.addExtensionProvider(PayloadTypePacketExtension.ELEMENT_NAME, ColibriConferenceIQ.NAMESPACE, new DefaultPacketExtensionProvider(PayloadTypePacketExtension.class));
        providerManager.addExtensionProvider(SourcePacketExtension.ELEMENT_NAME, SourcePacketExtension.NAMESPACE, new DefaultPacketExtensionProvider(SourcePacketExtension.class));
        PacketExtensionProvider parameterProvider = new DefaultPacketExtensionProvider(ParameterPacketExtension.class);
        providerManager.addExtensionProvider(ParameterPacketExtension.ELEMENT_NAME, ColibriConferenceIQ.NAMESPACE, parameterProvider);
        providerManager.addExtensionProvider(ParameterPacketExtension.ELEMENT_NAME, SourcePacketExtension.NAMESPACE, parameterProvider);
    }

    private void addChildExtension(Channel channel, PacketExtension childExtension) {
        if (childExtension instanceof PayloadTypePacketExtension) {
            PayloadTypePacketExtension payloadType = (PayloadTypePacketExtension) childExtension;
            if ("opus".equals(payloadType.getName()) && payloadType.getChannels() != 2) {
                payloadType.setChannels(2);
            }
            channel.addPayloadType(payloadType);
        } else if (childExtension instanceof IceUdpTransportPacketExtension) {
            channel.setTransport((IceUdpTransportPacketExtension) childExtension);
        }
    }

    private PacketExtension parseExtension(XmlPullParser parser, String name, String namespace) throws Exception {
        PacketExtensionProvider extensionProvider = (PacketExtensionProvider) ProviderManager.getInstance().getExtensionProvider(name, namespace);
        if (extensionProvider != null) {
            return extensionProvider.parseExtension(parser);
        }
        throwAway(parser, name);
        return null;
    }

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        String namespace = parser.getNamespace();
        if (!"conference".equals(parser.getName()) || !ColibriConferenceIQ.NAMESPACE.equals(namespace)) {
            return null;
        }
        IQ conference = new ColibriConferenceIQ();
        String conferenceID = parser.getAttributeValue("", "id");
        if (!(conferenceID == null || conferenceID.length() == 0)) {
            conference.setID(conferenceID);
        }
        boolean done = false;
        Channel channel = null;
        Content content = null;
        StringBuilder ssrc = null;
        while (!done) {
            String name;
            switch (parser.next()) {
                case 2:
                    name = parser.getName();
                    if (!"channel".equals(name)) {
                        if (!"ssrc".equals(name)) {
                            if (!"content".equals(name)) {
                                if (channel == null) {
                                    break;
                                }
                                String peName = null;
                                String peNamespace = null;
                                if ("transport".equals(name) && "urn:xmpp:jingle:transports:ice-udp:1".equals(parser.getNamespace())) {
                                    peName = name;
                                    peNamespace = "urn:xmpp:jingle:transports:ice-udp:1";
                                } else if (PayloadTypePacketExtension.ELEMENT_NAME.equals(name)) {
                                    peName = name;
                                    peNamespace = namespace;
                                } else if ("transport".equals(name) && "urn:xmpp:jingle:transports:raw-udp:1".equals(parser.getNamespace())) {
                                    peName = name;
                                    peNamespace = "urn:xmpp:jingle:transports:raw-udp:1";
                                } else if (SourcePacketExtension.ELEMENT_NAME.equals(name) && SourcePacketExtension.NAMESPACE.equals(parser.getNamespace())) {
                                    peName = name;
                                    peNamespace = SourcePacketExtension.NAMESPACE;
                                }
                                if (peName != null) {
                                    PacketExtension extension = parseExtension(parser, peName, peNamespace);
                                    if (extension == null) {
                                        break;
                                    }
                                    addChildExtension(channel, extension);
                                    break;
                                }
                                throwAway(parser, name);
                                break;
                            }
                            content = new Content();
                            String contentName = parser.getAttributeValue("", "name");
                            if (!(contentName == null || contentName.length() == 0)) {
                                content.setName(contentName);
                                break;
                            }
                        }
                        ssrc = new StringBuilder();
                        break;
                    }
                    channel = new Channel();
                    String direction = parser.getAttributeValue("", Channel.DIRECTION_ATTR_NAME);
                    if (!(direction == null || direction.length() == 0)) {
                        channel.setDirection(MediaDirection.parseString(direction));
                    }
                    String endpoint = parser.getAttributeValue("", "endpoint");
                    if (!(endpoint == null || endpoint.length() == 0)) {
                        channel.setEndpoint(endpoint);
                    }
                    String expire = parser.getAttributeValue("", Channel.EXPIRE_ATTR_NAME);
                    if (!(expire == null || expire.length() == 0)) {
                        channel.setExpire(Integer.parseInt(expire));
                    }
                    String host = parser.getAttributeValue("", "host");
                    if (!(host == null || host.length() == 0)) {
                        channel.setHost(host);
                    }
                    String channelID = parser.getAttributeValue("", "id");
                    if (!(channelID == null || channelID.length() == 0)) {
                        channel.setID(channelID);
                    }
                    String initiator = parser.getAttributeValue("", "initiator");
                    if (!(initiator == null || initiator.length() == 0)) {
                        channel.setInitiator(Boolean.valueOf(initiator));
                    }
                    String lastN = parser.getAttributeValue("", Channel.LAST_N_ATTR_NAME);
                    if (!(lastN == null || lastN.length() == 0)) {
                        channel.setLastN(Integer.valueOf(Integer.parseInt(lastN)));
                    }
                    String rtcpPort = parser.getAttributeValue("", Channel.RTCP_PORT_ATTR_NAME);
                    if (!(rtcpPort == null || rtcpPort.length() == 0)) {
                        channel.setRTCPPort(Integer.parseInt(rtcpPort));
                    }
                    String rtpLevelRelayType = parser.getAttributeValue("", Channel.RTP_LEVEL_RELAY_TYPE_ATTR_NAME);
                    if (!(rtpLevelRelayType == null || rtpLevelRelayType.length() == 0)) {
                        channel.setRTPLevelRelayType(rtpLevelRelayType);
                    }
                    String rtpPort = parser.getAttributeValue("", Channel.RTP_PORT_ATTR_NAME);
                    if (!(rtpPort == null || rtpPort.length() == 0)) {
                        channel.setRTPPort(Integer.parseInt(rtpPort));
                        break;
                    }
                    break;
                case 3:
                    name = parser.getName();
                    if (!"conference".equals(name)) {
                        if (!"channel".equals(name)) {
                            if (!"ssrc".equals(name)) {
                                if (!"content".equals(name)) {
                                    break;
                                }
                                conference.addContent(content);
                                content = null;
                                break;
                            }
                            String s = ssrc.toString().trim();
                            if (s.length() != 0) {
                                int i;
                                if (s.startsWith("-")) {
                                    i = Integer.parseInt(s);
                                } else {
                                    i = (int) Long.parseLong(s);
                                }
                                channel.addSSRC(i);
                            }
                            ssrc = null;
                            break;
                        }
                        content.addChannel(channel);
                        channel = null;
                        break;
                    }
                    done = true;
                    break;
                case 4:
                    if (ssrc == null) {
                        break;
                    }
                    ssrc.append(parser.getText());
                    break;
                default:
                    break;
            }
        }
        return conference;
    }

    private void throwAway(XmlPullParser parser, String name) throws Exception {
        while (true) {
            if (3 == parser.next() && name.equals(parser.getName())) {
                return;
            }
        }
    }
}
