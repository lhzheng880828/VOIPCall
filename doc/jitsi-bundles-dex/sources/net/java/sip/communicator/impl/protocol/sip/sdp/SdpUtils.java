package net.java.sip.communicator.impl.protocol.sip.sdp;

import java.io.UnsupportedEncodingException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.sdp.Attribute;
import javax.sdp.Connection;
import javax.sdp.MediaDescription;
import javax.sdp.Origin;
import javax.sdp.SdpConstants;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;
import javax.sdp.TimeDescription;
import net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl;
import net.java.sip.communicator.impl.protocol.sip.SipActivator;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.media.DynamicPayloadTypeRegistry;
import net.java.sip.communicator.service.protocol.media.DynamicRTPExtensionsRegistry;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.NetworkUtils;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.header.ContentTypeHeader;
import org.jitsi.javax.sip.message.Message;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaStreamTarget;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.RTPExtension;
import org.jitsi.service.neomedia.StreamConnector;
import org.jitsi.service.neomedia.format.AudioMediaFormat;
import org.jitsi.service.neomedia.format.MediaFormat;

public class SdpUtils {
    private static final String EXTMAP_ATTR = "extmap";
    private static final String RTCP_ATTR = "rtcp";
    public static final String ZRTP_HASH_ATTR = "zrtp-hash";
    private static final Logger logger = Logger.getLogger(SdpUtils.class);
    private static final SdpFactory sdpFactory = SdpFactory.getInstance();

    public static SessionDescription parseSdpString(String sdp) throws IllegalArgumentException {
        try {
            return sdpFactory.createSessionDescription(sdp);
        } catch (SdpParseException ex) {
            throw new IllegalArgumentException("Failed to parse the SDP description of the peer.", ex);
        }
    }

    public static Attribute createAttribute(String name, String value) {
        return sdpFactory.createAttribute(name, value);
    }

    public static SessionDescription createSessionDescription(InetAddress localAddress) throws OperationFailedException {
        return createSessionDescription(localAddress, null, null);
    }

    public static SessionDescription createSessionDescription(InetAddress localAddress, String userName, Vector<MediaDescription> mediaDescriptions) throws OperationFailedException {
        try {
            SessionDescription sessDescr = sdpFactory.createSessionDescription();
            sessDescr.setVersion(sdpFactory.createVersion(0));
            sessDescr.setSessionName(sdpFactory.createSessionName("-"));
            TimeDescription t = sdpFactory.createTimeDescription();
            Vector<TimeDescription> timeDescs = new Vector();
            timeDescs.add(t);
            sessDescr.setTimeDescriptions(timeDescs);
            String addrType = localAddress instanceof Inet6Address ? "IP6" : "IP4";
            if (userName == null) {
                userName = "jitsi.org";
            }
            sessDescr.setOrigin(sdpFactory.createOrigin(userName, 0, 0, "IN", addrType, localAddress.getHostAddress()));
            sessDescr.setConnection(sdpFactory.createConnection("IN", addrType, localAddress.getHostAddress()));
            if (mediaDescriptions != null) {
                sessDescr.setMediaDescriptions(mediaDescriptions);
            }
            return sessDescr;
        } catch (SdpException exc) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("An error occurred while creating session description", 4, exc, logger);
            return null;
        }
    }

    public static SessionDescription createSessionUpdateDescription(SessionDescription descToUpdate, InetAddress newConnectionAddress, Vector<MediaDescription> newMediaDescriptions) throws OperationFailedException {
        SessionDescription update = createSessionDescription(newConnectionAddress, null, newMediaDescriptions);
        try {
            Origin o = (Origin) descToUpdate.getOrigin().clone();
            o.setSessionVersion(1 + o.getSessionVersion());
            update.setOrigin(o);
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("Something very odd just happened.", e);
            }
        }
        Vector<MediaDescription> prevMedias = extractMediaDescriptions(descToUpdate);
        Vector<MediaDescription> completeMediaDescList = new Vector();
        Vector<MediaDescription> newMediaDescriptions2 = new Vector(newMediaDescriptions);
        Iterator i$ = prevMedias.iterator();
        while (i$.hasNext()) {
            MediaDescription medToUpdate = (MediaDescription) i$.next();
            MediaDescription desc = null;
            try {
                desc = removeMediaDesc(newMediaDescriptions2, getMediaType(medToUpdate));
            } catch (IllegalArgumentException e2) {
            }
            if (desc == null) {
                desc = createDisablingAnswer(medToUpdate);
            }
            completeMediaDescList.add(desc);
        }
        i$ = newMediaDescriptions2.iterator();
        while (i$.hasNext()) {
            completeMediaDescList.add((MediaDescription) i$.next());
        }
        try {
            update.setMediaDescriptions(completeMediaDescList);
        } catch (SdpException e3) {
            if (logger.isInfoEnabled()) {
                logger.info("A crazy thing just happened.", e3);
            }
        }
        return update;
    }

    private static MediaDescription removeMediaDesc(Vector<MediaDescription> descs, MediaType type) {
        Iterator<MediaDescription> descsIter = descs.iterator();
        while (descsIter.hasNext()) {
            MediaDescription mDesc = (MediaDescription) descsIter.next();
            if (getMediaType(mDesc) == type) {
                descsIter.remove();
                return mDesc;
            }
        }
        return null;
    }

    public static List<MediaFormat> extractFormats(MediaDescription mediaDesc, DynamicPayloadTypeRegistry ptRegistry) {
        List<MediaFormat> mediaFmts = new ArrayList();
        try {
            Vector<String> formatStrings = mediaDesc.getMedia().getMediaFormats(true);
            float frameRate = -1.0f;
            try {
                String frStr = mediaDesc.getAttribute("framerate");
                if (frStr != null) {
                    frameRate = Float.parseFloat(frStr);
                }
            } catch (SdpParseException e) {
            }
            Iterator i$ = formatStrings.iterator();
            while (i$.hasNext()) {
                String ptStr = (String) i$.next();
                try {
                    byte pt = Byte.parseByte(ptStr);
                    Attribute rtpmap = null;
                    try {
                        rtpmap = findPayloadTypeSpecificAttribute(mediaDesc.getAttributes(false), SdpConstants.RTPMAP, Byte.toString(pt));
                    } catch (SdpException e2) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(rtpmap + " does not seem like a valid rtpmap: attribute", e2);
                        }
                    }
                    Attribute fmtp = null;
                    try {
                        fmtp = findPayloadTypeSpecificAttribute(mediaDesc.getAttributes(false), "fmtp", ptStr);
                    } catch (SdpException exc) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(fmtp + " does not seem like a valid fmtp: attribute", exc);
                        }
                    }
                    List<Attribute> advp = null;
                    try {
                        advp = findAdvancedAttributes(mediaDesc.getAttributes(false), ptStr);
                    } catch (SdpException exc2) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Problem parsing advanced attributes", exc2);
                        }
                    }
                    try {
                        MediaFormat mediaFormat = createFormat(pt, rtpmap, fmtp, frameRate, advp, ptRegistry);
                        if (mediaFormat != null) {
                            mediaFmts.add(mediaFormat);
                        }
                    } catch (SdpException e22) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("A funny thing just happened ...", e22);
                        }
                    }
                } catch (NumberFormatException e3) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(ptStr + " is not a valid payload type", e3);
                    }
                }
            }
        } catch (SdpParseException exc3) {
            if (logger.isDebugEnabled()) {
                logger.debug("A funny thing just happened ...", exc3);
            }
        }
        return mediaFmts;
    }

    public static List<RTPExtension> extractRTPExtensions(MediaDescription mediaDesc, DynamicRTPExtensionsRegistry extMap) {
        List<RTPExtension> extensionsList = new ArrayList();
        Vector<Attribute> mediaAttributes = mediaDesc.getAttributes(false);
        if (mediaAttributes == null || mediaAttributes.size() == 0) {
            return null;
        }
        Iterator i$ = mediaAttributes.iterator();
        while (i$.hasNext()) {
            Attribute attr = (Attribute) i$.next();
            try {
                if (EXTMAP_ATTR.equals(attr.getName())) {
                    String attrValue = attr.getValue();
                    if (attrValue != null) {
                        RTPExtension rtpExtension = parseRTPExtensionAttribute(attrValue.trim(), extMap);
                        if (rtpExtension != null) {
                            extensionsList.add(rtpExtension);
                        }
                    }
                }
            } catch (SdpException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("A funny thing just happened ...", e);
                }
            }
        }
        return extensionsList;
    }

    private static RTPExtension parseRTPExtensionAttribute(String extmapAttr, DynamicRTPExtensionsRegistry extMap) {
        RTPExtension rtpExtension = null;
        StringTokenizer tokenizer = new StringTokenizer(extmapAttr, Separators.SP);
        if (tokenizer.hasMoreElements()) {
            String extIDStr;
            String idAndDirection = tokenizer.nextToken();
            MediaDirection direction = MediaDirection.SENDRECV;
            if (idAndDirection.contains(Separators.SLASH)) {
                StringTokenizer idAndDirTokenizer = new StringTokenizer(idAndDirection, Separators.SLASH);
                if (idAndDirTokenizer.hasMoreElements()) {
                    extIDStr = idAndDirTokenizer.nextToken();
                    if (idAndDirTokenizer.hasMoreTokens()) {
                        direction = MediaDirection.parseString(idAndDirTokenizer.nextToken());
                    }
                }
            } else {
                extIDStr = idAndDirection;
            }
            if (tokenizer.hasMoreElements()) {
                try {
                    URI uri = new URI(tokenizer.nextToken());
                    String extensionAttributes = null;
                    if (tokenizer.hasMoreElements()) {
                        extensionAttributes = tokenizer.nextToken();
                    }
                    rtpExtension = new RTPExtension(uri, direction, extensionAttributes);
                    byte extID = Byte.parseByte(extIDStr);
                    if (extMap.findExtension(extID) == null) {
                        extMap.addMapping(rtpExtension, extID);
                    }
                } catch (URISyntaxException e) {
                }
            }
        }
        return rtpExtension;
    }

    private static MediaFormat createFormat(byte payloadType, Attribute rtpmap, Attribute fmtp, float frameRate, List<Attribute> advp, DynamicPayloadTypeRegistry ptRegistry) throws SdpException {
        String encoding = null;
        double clockRate = -1.0d;
        int numChannels = 1;
        if (rtpmap != null) {
            StringTokenizer stringTokenizer = new StringTokenizer(rtpmap.getValue(), " /", false);
            if (stringTokenizer.hasMoreTokens()) {
                stringTokenizer.nextToken();
            }
            if (stringTokenizer.hasMoreTokens()) {
                encoding = stringTokenizer.nextToken();
            }
            if (stringTokenizer.hasMoreTokens()) {
                clockRate = Double.parseDouble(stringTokenizer.nextToken());
            }
            if (stringTokenizer.hasMoreTokens()) {
                String nChansStr = stringTokenizer.nextToken();
                try {
                    numChannels = Integer.parseInt(nChansStr);
                } catch (NumberFormatException exc) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(nChansStr + " is not a valid number of channels.", exc);
                    }
                }
            }
        } else {
            MediaFormat fmt = ptRegistry.findFormat(payloadType);
            if (fmt != null) {
                return fmt;
            }
        }
        Map<String, String> fmtParamsMap = null;
        Map<String, String> advancedAttrMap = null;
        if (fmtp != null) {
            fmtParamsMap = parseFmtpAttribute(fmtp);
        }
        if (advp != null) {
            advancedAttrMap = parseAdvancedAttributes(advp);
        }
        MediaFormat format = SipActivator.getMediaService().getFormatFactory().createMediaFormat(payloadType, encoding, clockRate, numChannels, frameRate, fmtParamsMap, advancedAttrMap);
        if (payloadType >= (byte) 96 && payloadType <= Byte.MAX_VALUE && format != null) {
            ptRegistry.addMapping(format, payloadType);
        }
        return format;
    }

    private static Map<String, String> parseAdvancedAttributes(List<Attribute> attrs) {
        if (attrs == null) {
            return null;
        }
        Map<String, String> ret = new Hashtable();
        for (Attribute attr : attrs) {
            try {
                String attrName = attr.getName();
                String attrVal = attr.getValue();
                int idx = attrVal.indexOf(Separators.SP);
                if (idx != -1) {
                    attrVal = attrVal.substring(idx + 1, attrVal.length());
                }
                ret.put(attrName, attrVal);
            } catch (SdpParseException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The impossible has just occurred!", e);
                }
                return null;
            }
        }
        if (ret.size() == 0) {
            return null;
        }
        return ret;
    }

    private static Map<String, String> parseFmtpAttribute(Attribute fmtpAttr) throws SdpException {
        Map<String, String> fmtParamsMap = new Hashtable();
        StringTokenizer tokenizer = new StringTokenizer(fmtpAttr.getValue(), " ;", false);
        if (!tokenizer.hasMoreTokens()) {
            return null;
        }
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int indexOfEq = token.indexOf(Separators.EQUALS);
            if (!(indexOfEq == -1 || indexOfEq == token.length() - 1)) {
                fmtParamsMap.put(token.substring(0, indexOfEq), token.substring(indexOfEq + 1, token.length()));
            }
        }
        if (fmtParamsMap.size() == 0) {
            return null;
        }
        return fmtParamsMap;
    }

    private static List<Attribute> findAdvancedAttributes(Vector<Attribute> mediaAttributes, String payloadType) throws SdpException {
        if (mediaAttributes == null || mediaAttributes.size() == 0) {
            return null;
        }
        List<Attribute> ret = new ArrayList();
        Iterator i$ = mediaAttributes.iterator();
        while (i$.hasNext()) {
            Attribute attr = (Attribute) i$.next();
            String attrName = attr.getName();
            String attrValue = attr.getValue();
            if (!(attrName.equals(SdpConstants.RTPMAP) || attrName.equals("fmtp") || attrValue == null)) {
                attrValue = attrValue.trim();
                if (attrValue.startsWith(payloadType + Separators.SP) || attrValue.startsWith("* ")) {
                    ret.add(attr);
                }
            }
        }
        if (ret.isEmpty()) {
            return null;
        }
        return ret;
    }

    private static Attribute findPayloadTypeSpecificAttribute(Vector<Attribute> mediaAttributes, String attributeName, String payloadType) throws SdpException {
        if (mediaAttributes == null || mediaAttributes.size() == 0) {
            return null;
        }
        Iterator i$ = mediaAttributes.iterator();
        while (i$.hasNext()) {
            Attribute attr = (Attribute) i$.next();
            if (attributeName.equals(attr.getName())) {
                String attrValue = attr.getValue();
                if (attrValue == null) {
                    continue;
                } else if (attrValue.trim().startsWith(payloadType + Separators.SP)) {
                    return attr;
                }
            }
        }
        return null;
    }

    public static MediaStreamTarget extractDefaultTarget(MediaDescription mediaDesc, SessionDescription sessDesc) throws IllegalArgumentException {
        Connection conn = mediaDesc.getConnection();
        if (conn == null) {
            conn = sessDesc.getConnection();
            if (conn == null) {
                throw new IllegalArgumentException("No \"c=\" field in the following media description nor in the enclosing session:\n" + mediaDesc.toString());
            }
        }
        try {
            String address = conn.getAddress();
            try {
                InetAddress rtpAddress = NetworkUtils.getInetAddress(address);
                try {
                    int rtpPort = mediaDesc.getMedia().getMediaPort();
                    try {
                        return new MediaStreamTarget(new InetSocketAddress(rtpAddress, rtpPort), determineRtcpAddress(mediaDesc.getAttribute(RTCP_ATTR), rtpAddress, rtpPort + 1));
                    } catch (SdpParseException exc) {
                        throw new IllegalArgumentException("Couldn't extract attribute value.", exc);
                    }
                } catch (SdpParseException exc2) {
                    throw new IllegalArgumentException("Couldn't extract port from a media description.", exc2);
                }
            } catch (UnknownHostException exc3) {
                throw new IllegalArgumentException("Failed to parse address " + address, exc3);
            }
        } catch (SdpParseException exc22) {
            throw new IllegalArgumentException("Couldn't extract connection address.", exc22);
        }
    }

    private static InetSocketAddress determineRtcpAddress(String rtcpAttrValue, InetAddress defaultAddr, int defaultPort) throws IllegalArgumentException {
        if (rtcpAttrValue == null) {
            return new InetSocketAddress(defaultAddr, defaultPort);
        }
        if (rtcpAttrValue == null || rtcpAttrValue.trim().length() == 0) {
            return new InetSocketAddress(defaultAddr, defaultPort);
        }
        StringTokenizer rtcpTokenizer = new StringTokenizer(rtcpAttrValue.trim(), Separators.SP);
        int tokenCount = rtcpTokenizer.countTokens();
        try {
            int rtcpPort = Integer.parseInt(rtcpTokenizer.nextToken());
            if (tokenCount == 1) {
                return new InetSocketAddress(defaultAddr, rtcpPort);
            }
            if (tokenCount == 4) {
                rtcpTokenizer.nextToken();
                rtcpTokenizer.nextToken();
                InetAddress rtcpAddress = null;
                try {
                    return new InetSocketAddress(NetworkUtils.getInetAddress(rtcpTokenizer.nextToken()), rtcpPort);
                } catch (UnknownHostException exc) {
                    throw new IllegalArgumentException("Failed to parse address " + rtcpAddress, exc);
                }
            }
            throw new IllegalArgumentException("Error while parsing rtcp attribute: " + rtcpAttrValue + ". Too many tokens! (" + tokenCount + Separators.RPAREN);
        } catch (NumberFormatException exc2) {
            throw new IllegalArgumentException("Error while parsing rtcp attribute: " + rtcpAttrValue, exc2);
        }
    }

    public static MediaDirection getDirection(MediaDescription mediaDesc) {
        Vector<Attribute> attributes = mediaDesc.getAttributes(false);
        if (attributes == null) {
            return MediaDirection.SENDRECV;
        }
        Iterator it = attributes.iterator();
        while (it.hasNext()) {
            String attrName = null;
            try {
                attrName = ((Attribute) it.next()).getName();
            } catch (SdpParseException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The impossible has just occurred!", e);
                }
            }
            for (MediaDirection value : MediaDirection.values()) {
                if (value.toString().equals(attrName)) {
                    return value;
                }
            }
        }
        return MediaDirection.SENDRECV;
    }

    public static URL getCallInfoURL(SessionDescription sessDesc) {
        URL url = null;
        javax.sdp.URI sdpUriField = sessDesc.getURI();
        if (sdpUriField != null) {
            try {
                return sdpUriField.get();
            } catch (SdpParseException exc) {
                logger.warn("Failed to parse SDP URI.", exc);
                return url;
            }
        } else if (!logger.isTraceEnabled()) {
            return url;
        } else {
            logger.trace("Call URI was null.");
            return url;
        }
    }

    public static MediaDescription createMediaDescription(String transport, List<MediaFormat> formats, StreamConnector connector, MediaDirection direction, List<RTPExtension> rtpExtensions, DynamicPayloadTypeRegistry dynamicPayloadTypes, DynamicRTPExtensionsRegistry rtpExtensionsRegistry) throws OperationFailedException {
        int[] payloadTypesArray = new int[formats.size()];
        Vector<Attribute> vector = new Vector((payloadTypesArray.length * 2) + 1);
        MediaType mediaType = null;
        if (direction != MediaDirection.SENDRECV) {
            vector.add(createDirectionAttribute(direction));
        }
        for (int i = 0; i < payloadTypesArray.length; i++) {
            MediaFormat format = (MediaFormat) formats.get(i);
            MediaType fmtMediaType = format.getMediaType();
            if (mediaType == null) {
                mediaType = fmtMediaType;
            }
            byte payloadType = format.getRTPPayloadType();
            if (payloadType == (byte) -1) {
                try {
                    payloadType = dynamicPayloadTypes.obtainPayloadTypeNumber(format);
                } catch (IllegalStateException exception) {
                    throw new OperationFailedException("Failed to allocate a new dynamic PT number.", 4, exception);
                }
            }
            String numChannelsStr = "";
            if (format instanceof AudioMediaFormat) {
                int channels = ((AudioMediaFormat) format).getChannels();
                if (channels > 1) {
                    numChannelsStr = Separators.SLASH + channels;
                }
            }
            vector.add(sdpFactory.createAttribute(SdpConstants.RTPMAP, payloadType + Separators.SP + format.getEncoding() + Separators.SLASH + format.getClockRateString() + numChannelsStr));
            if (format.getFormatParameters().size() > 0) {
                vector.add(sdpFactory.createAttribute("fmtp", payloadType + Separators.SP + encodeFmtp(format)));
            }
            for (Entry<String, String> ntry : format.getAdvancedAttributes().entrySet()) {
                vector.add(sdpFactory.createAttribute((String) ntry.getKey(), payloadType + Separators.SP + ((String) ntry.getValue())));
            }
            payloadTypesArray[i] = payloadType;
        }
        int rtpPort = connector.getDataSocket().getLocalPort();
        int rtcpPort = connector.getControlSocket().getLocalPort();
        if (rtpPort + 1 != rtcpPort) {
            vector.add(sdpFactory.createAttribute(RTCP_ATTR, Integer.toString(rtcpPort)));
        }
        if (rtpExtensions != null && rtpExtensions.size() > 0) {
            for (RTPExtension extension : rtpExtensions) {
                String str;
                byte extID = rtpExtensionsRegistry.obtainExtensionMapping(extension);
                String uri = extension.getURI().toString();
                MediaDirection extDirection = extension.getDirection();
                String attributes = extension.getExtensionAttributes();
                StringBuilder append = new StringBuilder().append(Byte.toString(extID));
                if (extDirection == MediaDirection.SENDRECV) {
                    str = "";
                } else {
                    str = Separators.SLASH + extDirection.toString();
                }
                append = append.append(str).append(Separators.SP).append(uri);
                if (attributes == null) {
                    str = "";
                } else {
                    str = Separators.SP + attributes;
                }
                vector.add(sdpFactory.createAttribute(EXTMAP_ATTR, append.append(str).toString()));
            }
        }
        MediaDescription mediaDesc = null;
        try {
            mediaDesc = sdpFactory.createMediaDescription(mediaType.toString(), connector.getDataSocket().getLocalPort(), 1, transport, payloadTypesArray);
            mediaDesc.setAttributes(vector);
            return mediaDesc;
        } catch (Exception cause) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to create a media description", 4, cause, logger);
            return mediaDesc;
        }
    }

    private static String encodeFmtp(MediaFormat format) {
        Iterator<Entry<String, String>> formatParamsIter = format.getFormatParameters().entrySet().iterator();
        StringBuffer fmtpBuff = new StringBuffer();
        while (formatParamsIter.hasNext()) {
            Entry<String, String> ntry = (Entry) formatParamsIter.next();
            fmtpBuff.append((String) ntry.getKey()).append(Separators.EQUALS).append((String) ntry.getValue());
            if (formatParamsIter.hasNext()) {
                fmtpBuff.append(Separators.SEMICOLON);
            }
        }
        return fmtpBuff.toString();
    }

    private static Attribute createDirectionAttribute(MediaDirection direction) {
        String dirStr;
        if (MediaDirection.SENDONLY.equals(direction)) {
            dirStr = "sendonly";
        } else if (MediaDirection.RECVONLY.equals(direction)) {
            dirStr = "recvonly";
        } else if (MediaDirection.SENDRECV.equals(direction)) {
            dirStr = "sendrecv";
        } else {
            dirStr = "inactive";
        }
        return sdpFactory.createAttribute(dirStr, null);
    }

    public static MediaType getMediaType(MediaDescription description) throws IllegalArgumentException {
        try {
            return MediaType.parseString(description.getMedia().getMediaType());
        } catch (SdpException e) {
            String message = "Invalid media type in m= line: " + description;
            if (logger.isDebugEnabled()) {
                logger.debug(message, e);
            }
            throw new IllegalArgumentException(message, e);
        }
    }

    public static boolean containsAttribute(MediaDescription description, String attributeName) throws IllegalArgumentException {
        try {
            Iterator i$ = description.getAttributes(false).iterator();
            while (i$.hasNext()) {
                if (((Attribute) i$.next()).getName().equals(attributeName)) {
                    return true;
                }
            }
            return false;
        } catch (SdpException e) {
            String message = "Invalid media type in a= line: " + description;
            if (logger.isDebugEnabled()) {
                logger.debug(message, e);
            }
            throw new IllegalArgumentException(message, e);
        }
    }

    public static MediaDescription createDisablingAnswer(MediaDescription offer) throws IllegalArgumentException {
        try {
            String mediaType = offer.getMedia().getMediaType();
            Vector<String> formatsVec = offer.getMedia().getMediaFormats(true);
            if (formatsVec == null) {
                formatsVec = new Vector();
                formatsVec.add(Integer.toString(0));
            }
            return sdpFactory.createMediaDescription(mediaType, 0, 1, SdpConstants.RTP_AVP, (String[]) formatsVec.toArray(new String[formatsVec.size()]));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not create a disabling answer", e);
        }
    }

    public static Vector<MediaDescription> extractMediaDescriptions(SessionDescription sessionDescription) throws IllegalArgumentException {
        Vector<MediaDescription> remoteDescriptions = null;
        try {
            remoteDescriptions = sessionDescription.getMediaDescriptions(false);
        } catch (SdpException e) {
        }
        if (remoteDescriptions != null && remoteDescriptions.size() != 0) {
            return remoteDescriptions;
        }
        throw new IllegalArgumentException("Could not find any media descriptions.");
    }

    public static String getContentAsString(Message message) {
        byte[] rawContent = message.getRawContent();
        ContentTypeHeader contentTypeHeader = (ContentTypeHeader) message.getHeader("Content-Type");
        String charset = null;
        if (contentTypeHeader != null) {
            charset = contentTypeHeader.getParameter("charset");
        }
        if (charset == null) {
            charset = "UTF-8";
        }
        try {
            return new String(rawContent, charset);
        } catch (UnsupportedEncodingException uee) {
            logger.warn("SIP message with unsupported charset of its content", uee);
            return new String(rawContent);
        }
    }
}
