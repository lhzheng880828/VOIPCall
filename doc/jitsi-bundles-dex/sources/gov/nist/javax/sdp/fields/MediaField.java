package gov.nist.javax.sdp.fields;

import java.util.Vector;
import javax.sdp.Media;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import org.jitsi.gov.nist.core.Separators;

public class MediaField extends SDPField implements Media {
    protected Vector formats = new Vector();
    protected String media;
    protected int nports;
    protected int port;
    protected String proto;

    public MediaField() {
        super(SDPFieldNames.MEDIA_FIELD);
    }

    public String getMedia() {
        return this.media;
    }

    public int getPort() {
        return this.port;
    }

    public int getNports() {
        return this.nports;
    }

    public String getProto() {
        return this.proto;
    }

    public Vector getFormats() {
        return this.formats;
    }

    public void setMedia(String m) {
        this.media = m;
    }

    public void setPort(int p) {
        this.port = p;
    }

    public void setNports(int n) {
        this.nports = n;
    }

    public void setProto(String p) {
        this.proto = p;
    }

    public void setFormats(Vector formats) {
        this.formats = formats;
    }

    public String getMediaType() throws SdpParseException {
        return getMedia();
    }

    public void setMediaType(String mediaType) throws SdpException {
        if (mediaType == null) {
            throw new SdpException("The mediaType is null");
        }
        setMedia(mediaType);
    }

    public int getMediaPort() throws SdpParseException {
        return getPort();
    }

    public void setMediaPort(int port) throws SdpException {
        if (port < 0) {
            throw new SdpException("The port is < 0");
        }
        setPort(port);
    }

    public int getPortCount() throws SdpParseException {
        return getNports();
    }

    public void setPortCount(int portCount) throws SdpException {
        if (portCount < 0) {
            throw new SdpException("The port count is < 0");
        }
        setNports(portCount);
    }

    public String getProtocol() throws SdpParseException {
        return getProto();
    }

    public void setProtocol(String protocol) throws SdpException {
        if (protocol == null) {
            throw new SdpException("The protocol is null");
        }
        setProto(protocol);
    }

    public Vector getMediaFormats(boolean create) throws SdpParseException {
        if (create || this.formats.size() != 0) {
            return this.formats;
        }
        return null;
    }

    public void setMediaFormats(Vector mediaFormats) throws SdpException {
        if (mediaFormats == null) {
            throw new SdpException("The mediaFormats is null");
        }
        this.formats = mediaFormats;
    }

    private String encodeFormats() {
        StringBuilder retval = new StringBuilder(this.formats.size() * 3);
        for (int i = 0; i < this.formats.size(); i++) {
            retval.append(this.formats.elementAt(i));
            if (i < this.formats.size() - 1) {
                retval.append(Separators.SP);
            }
        }
        return retval.toString();
    }

    public String encode() {
        String encoded_string = SDPFieldNames.MEDIA_FIELD;
        if (this.media != null) {
            encoded_string = encoded_string + this.media.toLowerCase() + Separators.SP + this.port;
        }
        if (this.nports > 1) {
            encoded_string = encoded_string + Separators.SLASH + this.nports;
        }
        if (this.proto != null) {
            encoded_string = encoded_string + Separators.SP + this.proto;
        }
        if (this.formats != null) {
            encoded_string = encoded_string + Separators.SP + encodeFormats();
        }
        return encoded_string + Separators.NEWLINE;
    }

    public Object clone() {
        MediaField retval = (MediaField) super.clone();
        if (this.formats != null) {
            retval.formats = (Vector) this.formats.clone();
        }
        return retval;
    }
}
