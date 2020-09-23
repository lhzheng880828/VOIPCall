package javax.sdp;

import gov.nist.javax.sdp.MediaDescriptionImpl;
import gov.nist.javax.sdp.SessionDescriptionImpl;
import gov.nist.javax.sdp.TimeDescriptionImpl;
import gov.nist.javax.sdp.fields.AttributeField;
import gov.nist.javax.sdp.fields.BandwidthField;
import gov.nist.javax.sdp.fields.ConnectionField;
import gov.nist.javax.sdp.fields.EmailField;
import gov.nist.javax.sdp.fields.InformationField;
import gov.nist.javax.sdp.fields.KeyField;
import gov.nist.javax.sdp.fields.MediaField;
import gov.nist.javax.sdp.fields.OriginField;
import gov.nist.javax.sdp.fields.PhoneField;
import gov.nist.javax.sdp.fields.ProtoVersionField;
import gov.nist.javax.sdp.fields.RepeatField;
import gov.nist.javax.sdp.fields.SessionNameField;
import gov.nist.javax.sdp.fields.TimeField;
import gov.nist.javax.sdp.fields.URIField;
import gov.nist.javax.sdp.fields.ZoneField;
import gov.nist.javax.sdp.parser.SDPAnnounceParser;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import org.jitsi.gov.nist.core.Separators;

public class SdpFactory {
    private static final SdpFactory singletonInstance = new SdpFactory();

    private SdpFactory() {
    }

    public static SdpFactory getInstance() {
        return singletonInstance;
    }

    public SessionDescription createSessionDescription() throws SdpException {
        SessionDescriptionImpl sessionDescriptionImpl = new SessionDescriptionImpl();
        ProtoVersionField ProtoVersionField = new ProtoVersionField();
        ProtoVersionField.setVersion(0);
        sessionDescriptionImpl.setVersion(ProtoVersionField);
        OriginField originImpl = null;
        try {
            originImpl = (OriginField) createOrigin("user", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        sessionDescriptionImpl.setOrigin(originImpl);
        SessionNameField sessionNameImpl = new SessionNameField();
        sessionNameImpl.setValue("-");
        sessionDescriptionImpl.setSessionName(sessionNameImpl);
        TimeDescriptionImpl timeDescriptionImpl = new TimeDescriptionImpl();
        TimeField timeImpl = new TimeField();
        timeImpl.setZero();
        timeDescriptionImpl.setTime(timeImpl);
        Vector times = new Vector();
        times.addElement(timeDescriptionImpl);
        sessionDescriptionImpl.setTimeDescriptions(times);
        return sessionDescriptionImpl;
    }

    public SessionDescription createSessionDescription(SessionDescription otherSessionDescription) throws SdpException {
        return new SessionDescriptionImpl(otherSessionDescription);
    }

    public SessionDescription createSessionDescription(String s) throws SdpParseException {
        try {
            return new SDPAnnounceParser(s).parse();
        } catch (ParseException e) {
            e.printStackTrace();
            throw new SdpParseException(0, 0, "Could not parse message");
        }
    }

    public BandWidth createBandwidth(String modifier, int value) {
        BandwidthField bandWidthImpl = new BandwidthField();
        try {
            bandWidthImpl.setType(modifier);
            bandWidthImpl.setValue(value);
        } catch (SdpException s) {
            s.printStackTrace();
        }
        return bandWidthImpl;
    }

    public Attribute createAttribute(String name, String value) {
        AttributeField attributeImpl = new AttributeField();
        try {
            attributeImpl.setName(name);
            attributeImpl.setValueAllowNull(value);
        } catch (SdpException s) {
            s.printStackTrace();
        }
        return attributeImpl;
    }

    public Info createInfo(String value) {
        InformationField infoImpl = new InformationField();
        try {
            infoImpl.setValue(value);
        } catch (SdpException s) {
            s.printStackTrace();
        }
        return infoImpl;
    }

    public Phone createPhone(String value) {
        PhoneField phoneImpl = new PhoneField();
        try {
            phoneImpl.setValue(value);
        } catch (SdpException s) {
            s.printStackTrace();
        }
        return phoneImpl;
    }

    public EMail createEMail(String value) {
        EmailField emailImpl = new EmailField();
        try {
            emailImpl.setValue(value);
        } catch (SdpException s) {
            s.printStackTrace();
        }
        return emailImpl;
    }

    public URI createURI(URL value) throws SdpException {
        URIField uriImpl = new URIField();
        uriImpl.set(value);
        return uriImpl;
    }

    public SessionName createSessionName(String name) {
        SessionNameField sessionNameImpl = new SessionNameField();
        try {
            sessionNameImpl.setValue(name);
        } catch (SdpException s) {
            s.printStackTrace();
        }
        return sessionNameImpl;
    }

    public Key createKey(String method, String key) {
        KeyField keyImpl = new KeyField();
        try {
            keyImpl.setMethod(method);
            keyImpl.setKey(key);
            return keyImpl;
        } catch (SdpException s) {
            s.printStackTrace();
            return null;
        }
    }

    public Version createVersion(int value) {
        ProtoVersionField protoVersionField = new ProtoVersionField();
        try {
            protoVersionField.setVersion(value);
            return protoVersionField;
        } catch (SdpException s) {
            s.printStackTrace();
            return null;
        }
    }

    public Media createMedia(String media, int port, int numPorts, String transport, Vector staticRtpAvpTypes) throws SdpException {
        MediaField mediaImpl = new MediaField();
        mediaImpl.setMediaType(media);
        mediaImpl.setMediaPort(port);
        mediaImpl.setPortCount(numPorts);
        mediaImpl.setProtocol(transport);
        mediaImpl.setMediaFormats(staticRtpAvpTypes);
        return mediaImpl;
    }

    public Origin createOrigin(String userName, String address) throws SdpException {
        OriginField originImpl = new OriginField();
        originImpl.setUsername(userName);
        originImpl.setAddress(address);
        originImpl.setNetworkType("IN");
        originImpl.setAddressType("IP4");
        return originImpl;
    }

    public Origin createOrigin(String userName, long sessionId, long sessionVersion, String networkType, String addrType, String address) throws SdpException {
        OriginField originImpl = new OriginField();
        originImpl.setUsername(userName);
        originImpl.setAddress(address);
        originImpl.setSessionId(sessionId);
        originImpl.setSessionVersion(sessionVersion);
        originImpl.setAddressType(addrType);
        originImpl.setNetworkType(networkType);
        return originImpl;
    }

    public MediaDescription createMediaDescription(String media, int port, int numPorts, String transport, int[] staticRtpAvpTypes) throws IllegalArgumentException, SdpException {
        MediaDescriptionImpl mediaDescriptionImpl = new MediaDescriptionImpl();
        MediaField mediaImpl = new MediaField();
        mediaImpl.setMediaType(media);
        mediaImpl.setMediaPort(port);
        mediaImpl.setPortCount(numPorts);
        mediaImpl.setProtocol(transport);
        mediaDescriptionImpl.setMedia(mediaImpl);
        Vector payload = new Vector();
        for (int num : staticRtpAvpTypes) {
            payload.add(new Integer(num).toString());
        }
        mediaImpl.setMediaFormats(payload);
        return mediaDescriptionImpl;
    }

    public MediaDescription createMediaDescription(String media, int port, int numPorts, String transport, String[] formats) {
        MediaDescriptionImpl mediaDescriptionImpl = new MediaDescriptionImpl();
        try {
            MediaField mediaImpl = new MediaField();
            mediaImpl.setMediaType(media);
            mediaImpl.setMediaPort(port);
            mediaImpl.setPortCount(numPorts);
            mediaImpl.setProtocol(transport);
            Vector formatsV = new Vector(formats.length);
            for (Object add : formats) {
                formatsV.add(add);
            }
            mediaImpl.setMediaFormats(formatsV);
            mediaDescriptionImpl.setMedia(mediaImpl);
        } catch (SdpException s) {
            s.printStackTrace();
        }
        return mediaDescriptionImpl;
    }

    public TimeDescription createTimeDescription(Time t) throws SdpException {
        TimeDescriptionImpl timeDescriptionImpl = new TimeDescriptionImpl();
        timeDescriptionImpl.setTime(t);
        return timeDescriptionImpl;
    }

    public TimeDescription createTimeDescription() throws SdpException {
        TimeDescriptionImpl timeDescriptionImpl = new TimeDescriptionImpl();
        TimeField timeImpl = new TimeField();
        timeImpl.setZero();
        timeDescriptionImpl.setTime(timeImpl);
        return timeDescriptionImpl;
    }

    public TimeDescription createTimeDescription(Date start, Date stop) throws SdpException {
        TimeDescriptionImpl timeDescriptionImpl = new TimeDescriptionImpl();
        TimeField timeImpl = new TimeField();
        timeImpl.setStart(start);
        timeImpl.setStop(stop);
        timeDescriptionImpl.setTime(timeImpl);
        return timeDescriptionImpl;
    }

    public String formatMulticastAddress(String addr, int ttl, int numAddrs) {
        return addr + Separators.SLASH + ttl + Separators.SLASH + numAddrs;
    }

    public Connection createConnection(String netType, String addrType, String addr, int ttl, int numAddrs) throws SdpException {
        ConnectionField connectionImpl = new ConnectionField();
        connectionImpl.setNetworkType(netType);
        connectionImpl.setAddressType(addrType);
        connectionImpl.setAddress(addr);
        return connectionImpl;
    }

    public Connection createConnection(String netType, String addrType, String addr) throws SdpException {
        ConnectionField connectionImpl = new ConnectionField();
        connectionImpl.setNetworkType(netType);
        connectionImpl.setAddressType(addrType);
        connectionImpl.setAddress(addr);
        return connectionImpl;
    }

    public Connection createConnection(String addr, int ttl, int numAddrs) throws SdpException {
        ConnectionField connectionImpl = new ConnectionField();
        connectionImpl.setAddress(addr);
        return connectionImpl;
    }

    public Connection createConnection(String addr) throws SdpException {
        return createConnection("IN", "IP4", addr);
    }

    public Time createTime(Date start, Date stop) throws SdpException {
        TimeField timeImpl = new TimeField();
        timeImpl.setStart(start);
        timeImpl.setStop(stop);
        return timeImpl;
    }

    public Time createTime() throws SdpException {
        TimeField timeImpl = new TimeField();
        timeImpl.setZero();
        return timeImpl;
    }

    public RepeatTime createRepeatTime(int repeatInterval, int activeDuration, int[] offsets) {
        RepeatField repeatTimeField = new RepeatField();
        try {
            repeatTimeField.setRepeatInterval(repeatInterval);
            repeatTimeField.setActiveDuration(activeDuration);
            repeatTimeField.setOffsetArray(offsets);
        } catch (SdpException s) {
            s.printStackTrace();
        }
        return repeatTimeField;
    }

    public TimeZoneAdjustment createTimeZoneAdjustment(Date d, int offset) {
        ZoneField timeZoneAdjustmentImpl = new ZoneField();
        try {
            Hashtable map = new Hashtable();
            map.put(d, new Integer(offset));
            timeZoneAdjustmentImpl.setZoneAdjustments(map);
        } catch (SdpException s) {
            s.printStackTrace();
        }
        return timeZoneAdjustmentImpl;
    }

    public static Date getDateFromNtp(long ntpTime) {
        return new Date((ntpTime - 2208988800L) * 1000);
    }

    public static long getNtpTime(Date d) throws SdpParseException {
        if (d == null) {
            return -1;
        }
        return (d.getTime() / 1000) + 2208988800L;
    }

    public static void main(String[] args) throws SdpParseException, SdpException {
        SessionDescription sessionDescription = new SdpFactory().createSessionDescription("v=0\r\no=CiscoSystemsSIP-GW-UserAgent 2578 3027 IN IP4 83.211.215.216\r\ns=SIP Call\r\nc=IN IP4 62.94.199.36\r\nt=0 0\r\nm=audio 62278 RTP/AVP 18 8 0 4 3 125 101 19\r\nc=IN IP4 62.94.199.36\r\na=rtpmap:18 G729/8000\r\na=fmtp:18 annexb=yes\r\na=rtpmap:8 PCMA/8000\r\na=rtpmap:0 PCMU/8000\r\na=rtpmap:4 G723/8000\r\na=fmtp:4 bitrate=5.3;annexa=no\r\na=rtpmap:3 GSM/8000\r\na=rtpmap:125 X-CCD/8000\r\na=rtpmap:101 telephone-event/8000\r\na=fmtp:101 0-16\r\na=rtpmap:19 CN/8000\r\na=direction:passive\r\n");
        System.out.println("sessionDescription = " + sessionDescription);
        Vector mediaDescriptions = sessionDescription.getMediaDescriptions(true);
        for (int i = 0; i < mediaDescriptions.size(); i++) {
            MediaDescription m = (MediaDescription) mediaDescriptions.elementAt(i);
            ((MediaDescriptionImpl) m).setDuplexity("sendrecv");
            System.out.println("m = " + m.toString());
            System.out.println("formats = " + m.getMedia().getMediaFormats(false));
        }
    }
}
