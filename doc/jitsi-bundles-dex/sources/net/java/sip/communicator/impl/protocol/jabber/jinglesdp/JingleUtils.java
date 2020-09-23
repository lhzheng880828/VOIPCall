package net.java.sip.communicator.impl.protocol.jabber.jinglesdp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.java.sip.communicator.impl.protocol.jabber.JabberActivator;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CandidatePacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension.CreatorEnum;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension.SendersEnum;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.IceUdpTransportPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ParameterPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.PayloadTypePacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RTPHdrExtPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RemoteCandidatePacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RtpDescriptionPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail.ThumbnailElement;
import net.java.sip.communicator.service.protocol.media.DynamicPayloadTypeRegistry;
import net.java.sip.communicator.service.protocol.media.DynamicRTPExtensionsRegistry;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.NetworkUtils;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaService;
import org.jitsi.service.neomedia.MediaStreamTarget;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.RTPExtension;
import org.jitsi.service.neomedia.format.AudioMediaFormat;
import org.jitsi.service.neomedia.format.MediaFormat;
import org.jitsi.service.neomedia.format.MediaFormatFactory;

public class JingleUtils {
    private static final Logger logger = Logger.getLogger(JingleUtils.class);

    public static RtpDescriptionPacketExtension getRtpDescription(ContentPacketExtension content) {
        return (RtpDescriptionPacketExtension) content.getFirstChildOfType(RtpDescriptionPacketExtension.class);
    }

    public static List<MediaFormat> extractFormats(RtpDescriptionPacketExtension description, DynamicPayloadTypeRegistry ptRegistry) {
        List<MediaFormat> mediaFmts = new ArrayList();
        for (PayloadTypePacketExtension ptExt : description.getPayloadTypes()) {
            MediaFormat format = payloadTypeToMediaFormat(ptExt, ptRegistry);
            if (format != null) {
                mediaFmts.add(format);
            } else if (logger.isTraceEnabled()) {
                logger.trace("Unsupported remote format: " + ptExt.toXML());
            }
        }
        return mediaFmts;
    }

    public static MediaFormat payloadTypeToMediaFormat(PayloadTypePacketExtension payloadType, DynamicPayloadTypeRegistry ptRegistry) {
        return payloadTypeToMediaFormat(payloadType, JabberActivator.getMediaService(), ptRegistry);
    }

    public static MediaFormat payloadTypeToMediaFormat(PayloadTypePacketExtension payloadType, MediaService mediaService, DynamicPayloadTypeRegistry ptRegistry) {
        byte pt = (byte) payloadType.getID();
        boolean unknown = false;
        List<ParameterPacketExtension> params = payloadType.getParameters();
        Map<String, String> paramsMap = new HashMap();
        Map<String, String> advancedMap = new HashMap();
        for (ParameterPacketExtension param : params) {
            if (param.getName().equals("imageattr")) {
                advancedMap.put(param.getName(), param.getValue());
            } else {
                paramsMap.put(param.getName(), param.getValue());
            }
        }
        for (String attr : payloadType.getAttributeNames()) {
            if (attr.equals(ThumbnailElement.WIDTH) || attr.equals(ThumbnailElement.HEIGHT)) {
                paramsMap.put(attr, payloadType.getAttributeAsString(attr));
            }
        }
        MediaFormatFactory formatFactory = mediaService.getFormatFactory();
        MediaFormat format = formatFactory.createMediaFormat(pt, payloadType.getName(), (double) payloadType.getClockrate(), payloadType.getChannels(), -1.0f, paramsMap, advancedMap);
        if (format == null) {
            unknown = true;
            format = formatFactory.createUnknownMediaFormat(MediaType.AUDIO);
        }
        if (ptRegistry != null && pt >= (byte) 96 && pt <= Byte.MAX_VALUE) {
            ptRegistry.addMapping(format, pt);
        }
        return unknown ? null : format;
    }

    public static List<RTPExtension> extractRTPExtensions(RtpDescriptionPacketExtension desc, DynamicRTPExtensionsRegistry extMap) {
        List<RTPExtension> extensionsList = new ArrayList();
        for (RTPHdrExtPacketExtension extmap : desc.getExtmapList()) {
            RTPExtension rtpExtension = new RTPExtension(extmap.getURI(), getDirection(extmap.getSenders(), false), extmap.getAttributes());
            if (rtpExtension != null) {
                extensionsList.add(rtpExtension);
            }
        }
        return extensionsList;
    }

    public static SendersEnum getSenders(MediaDirection direction, boolean initiatorPerspective) {
        if (direction == MediaDirection.SENDRECV) {
            return SendersEnum.both;
        }
        if (direction == MediaDirection.INACTIVE) {
            return SendersEnum.none;
        }
        if (initiatorPerspective) {
            if (direction == MediaDirection.SENDONLY) {
                return SendersEnum.initiator;
            }
            return SendersEnum.responder;
        } else if (direction == MediaDirection.SENDONLY) {
            return SendersEnum.responder;
        } else {
            return SendersEnum.initiator;
        }
    }

    public static MediaDirection getDirection(ContentPacketExtension content, boolean initiatorPerspective) {
        return getDirection(content.getSenders(), initiatorPerspective);
    }

    public static MediaDirection getDirection(SendersEnum senders, boolean initiatorPerspective) {
        if (senders == null) {
            return MediaDirection.SENDRECV;
        }
        if (senders == SendersEnum.initiator) {
            if (initiatorPerspective) {
                return MediaDirection.SENDONLY;
            }
            return MediaDirection.RECVONLY;
        } else if (senders == SendersEnum.responder) {
            if (initiatorPerspective) {
                return MediaDirection.RECVONLY;
            }
            return MediaDirection.SENDONLY;
        } else if (senders == SendersEnum.both) {
            return MediaDirection.SENDRECV;
        } else {
            return MediaDirection.INACTIVE;
        }
    }

    public static MediaStreamTarget extractDefaultTarget(ContentPacketExtension content) {
        IceUdpTransportPacketExtension transport = (IceUdpTransportPacketExtension) content.getFirstChildOfType(IceUdpTransportPacketExtension.class);
        return transport == null ? null : extractDefaultTarget(transport);
    }

    public static MediaStreamTarget extractDefaultTarget(IceUdpTransportPacketExtension transport) {
        CandidatePacketExtension rtpCand = getFirstCandidate(transport, 1);
        if (rtpCand == null) {
            return null;
        }
        try {
            InetSocketAddress rtcpTarget;
            InetAddress rtpAddress = NetworkUtils.getInetAddress(rtpCand.getIP());
            int rtpPort = rtpCand.getPort();
            InetSocketAddress rtpTarget = new InetSocketAddress(rtpAddress, rtpPort);
            CandidatePacketExtension rtcpCand = getFirstCandidate(transport, 2);
            if (rtcpCand == null) {
                rtcpTarget = new InetSocketAddress(rtpAddress, rtpPort + 1);
            } else {
                try {
                    rtcpTarget = new InetSocketAddress(NetworkUtils.getInetAddress(rtcpCand.getIP()), rtcpCand.getPort());
                } catch (UnknownHostException exc) {
                    throw new IllegalArgumentException("Failed to parse address " + rtcpCand.getIP(), exc);
                }
            }
            return new MediaStreamTarget(rtpTarget, rtcpTarget);
        } catch (UnknownHostException exc2) {
            throw new IllegalArgumentException("Failed to parse address " + rtpCand.getIP(), exc2);
        }
    }

    public static CandidatePacketExtension getFirstCandidate(ContentPacketExtension content, int componentID) {
        IceUdpTransportPacketExtension transport = (IceUdpTransportPacketExtension) content.getFirstChildOfType(IceUdpTransportPacketExtension.class);
        return transport == null ? null : getFirstCandidate(transport, componentID);
    }

    public static CandidatePacketExtension getFirstCandidate(IceUdpTransportPacketExtension transport, int componentID) {
        for (CandidatePacketExtension cand : transport.getCandidateList()) {
            if (!(cand instanceof RemoteCandidatePacketExtension) && cand.getComponent() == componentID) {
                return cand;
            }
        }
        return null;
    }

    public static ContentPacketExtension createDescription(CreatorEnum creator, String contentName, SendersEnum senders, List<MediaFormat> formats, List<RTPExtension> rtpExtensions, DynamicPayloadTypeRegistry dynamicPayloadTypes, DynamicRTPExtensionsRegistry rtpExtensionsRegistry) {
        ContentPacketExtension content = new ContentPacketExtension();
        RtpDescriptionPacketExtension description = new RtpDescriptionPacketExtension();
        content.setCreator(creator);
        content.setName(contentName);
        if (!(senders == null || senders == SendersEnum.both)) {
            content.setSenders(senders);
        }
        content.addChildExtension(description);
        description.setMedia(((MediaFormat) formats.get(0)).getMediaType().toString());
        for (MediaFormat fmt : formats) {
            description.addPayloadType(formatToPayloadType(fmt, dynamicPayloadTypes));
        }
        if (rtpExtensions != null && rtpExtensions.size() > 0) {
            for (RTPExtension extension : rtpExtensions) {
                byte extID = rtpExtensionsRegistry.obtainExtensionMapping(extension);
                URI uri = extension.getURI();
                MediaDirection extDirection = extension.getDirection();
                String attributes = extension.getExtensionAttributes();
                SendersEnum sendersEnum = getSenders(extDirection, false);
                RTPHdrExtPacketExtension ext = new RTPHdrExtPacketExtension();
                ext.setURI(uri);
                ext.setSenders(sendersEnum);
                ext.setID(Byte.toString(extID));
                ext.setAttributes(attributes);
                description.addChildExtension(ext);
            }
        }
        return content;
    }

    public static PayloadTypePacketExtension formatToPayloadType(MediaFormat format, DynamicPayloadTypeRegistry ptRegistry) {
        ParameterPacketExtension ext;
        PayloadTypePacketExtension ptExt = new PayloadTypePacketExtension();
        int payloadType = format.getRTPPayloadType();
        if (payloadType == -1) {
            payloadType = ptRegistry.obtainPayloadTypeNumber(format);
        }
        ptExt.setId(payloadType);
        ptExt.setName(format.getEncoding());
        if (format instanceof AudioMediaFormat) {
            ptExt.setChannels(((AudioMediaFormat) format).getChannels());
        }
        ptExt.setClockrate((int) format.getClockRate());
        for (Entry<String, String> entry : format.getFormatParameters().entrySet()) {
            ext = new ParameterPacketExtension();
            ext.setName((String) entry.getKey());
            ext.setValue((String) entry.getValue());
            ptExt.addParameter(ext);
        }
        for (Entry<String, String> entry2 : format.getAdvancedAttributes().entrySet()) {
            ext = new ParameterPacketExtension();
            ext.setName((String) entry2.getKey());
            ext.setValue((String) entry2.getValue());
            ptExt.addParameter(ext);
        }
        return ptExt;
    }

    public static MediaType getMediaType(ContentPacketExtension content) {
        if (content == null) {
            return null;
        }
        RtpDescriptionPacketExtension desc = getRtpDescription(content);
        if (desc == null) {
            return null;
        }
        String mediaTypeStr = desc.getMedia();
        if (mediaTypeStr != null) {
            return MediaType.parseString(mediaTypeStr);
        }
        return null;
    }
}
