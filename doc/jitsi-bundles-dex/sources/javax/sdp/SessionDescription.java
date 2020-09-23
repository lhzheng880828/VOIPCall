package javax.sdp;

import java.io.Serializable;
import java.util.Vector;

public interface SessionDescription extends Serializable, Cloneable {
    Object clone() throws CloneNotSupportedException;

    String getAttribute(String str) throws SdpParseException;

    Vector getAttributes(boolean z);

    int getBandwidth(String str) throws SdpParseException;

    Vector getBandwidths(boolean z);

    Connection getConnection();

    Vector getEmails(boolean z) throws SdpParseException;

    Info getInfo();

    Key getKey();

    Vector getMediaDescriptions(boolean z) throws SdpException;

    Origin getOrigin();

    Vector getPhones(boolean z) throws SdpException;

    SessionName getSessionName();

    Vector getTimeDescriptions(boolean z) throws SdpException;

    URI getURI();

    Version getVersion();

    Vector getZoneAdjustments(boolean z) throws SdpException;

    void removeAttribute(String str);

    void removeBandwidth(String str);

    void setAttribute(String str, String str2) throws SdpException;

    void setAttributes(Vector vector) throws SdpException;

    void setBandwidth(String str, int i) throws SdpException;

    void setBandwidths(Vector vector) throws SdpException;

    void setConnection(Connection connection) throws SdpException;

    void setEmails(Vector vector) throws SdpException;

    void setInfo(Info info) throws SdpException;

    void setKey(Key key) throws SdpException;

    void setMediaDescriptions(Vector vector) throws SdpException;

    void setOrigin(Origin origin) throws SdpException;

    void setPhones(Vector vector) throws SdpException;

    void setSessionName(SessionName sessionName) throws SdpException;

    void setTimeDescriptions(Vector vector) throws SdpException;

    void setURI(URI uri) throws SdpException;

    void setVersion(Version version) throws SdpException;

    void setZoneAdjustments(Vector vector) throws SdpException;
}
