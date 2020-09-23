package gov.nist.javax.sdp;

import gov.nist.javax.sdp.fields.AttributeField;
import gov.nist.javax.sdp.fields.BandwidthField;
import gov.nist.javax.sdp.fields.ConnectionField;
import gov.nist.javax.sdp.fields.InformationField;
import gov.nist.javax.sdp.fields.KeyField;
import gov.nist.javax.sdp.fields.MediaField;
import gov.nist.javax.sdp.fields.PreconditionFields;
import gov.nist.javax.sdp.fields.SDPField;
import java.util.Vector;
import javax.sdp.Connection;
import javax.sdp.Info;
import javax.sdp.Key;
import javax.sdp.Media;
import javax.sdp.MediaDescription;
import javax.sdp.SdpConstants;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.PayloadTypePacketExtension;
import org.jitsi.gov.nist.core.NameValue;
import org.jitsi.gov.nist.core.Separators;

public class MediaDescriptionImpl implements MediaDescription {
    protected Vector attributeFields = new Vector();
    protected Vector bandwidthFields = new Vector();
    protected ConnectionField connectionField;
    protected InformationField informationField;
    protected KeyField keyField;
    protected MediaField mediaField;
    protected PreconditionFields preconditionFields = new PreconditionFields();

    public String encode() {
        int i;
        StringBuilder retval = new StringBuilder();
        if (this.mediaField != null) {
            retval.append(this.mediaField.encode());
        }
        if (this.informationField != null) {
            retval.append(this.informationField.encode());
        }
        if (this.connectionField != null) {
            retval.append(this.connectionField.encode());
        }
        if (this.bandwidthFields != null) {
            for (i = 0; i < this.bandwidthFields.size(); i++) {
                retval.append(((SDPField) this.bandwidthFields.elementAt(i)).encode());
            }
            if (this.preconditionFields != null) {
                int precondSize = this.preconditionFields.getPreconditionSize();
                for (i = 0; i < precondSize; i++) {
                    retval.append(((SDPField) this.preconditionFields.getPreconditions().elementAt(i)).encode());
                }
            }
        }
        if (this.keyField != null) {
            retval.append(this.keyField.encode());
        }
        if (this.attributeFields != null) {
            for (i = 0; i < this.attributeFields.size(); i++) {
                retval.append(((SDPField) this.attributeFields.elementAt(i)).encode());
            }
        }
        return retval.toString();
    }

    public String toString() {
        return encode();
    }

    public MediaField getMediaField() {
        return this.mediaField;
    }

    public InformationField getInformationField() {
        return this.informationField;
    }

    public ConnectionField getConnectionField() {
        return this.connectionField;
    }

    public KeyField getKeyField() {
        return this.keyField;
    }

    public Vector getAttributeFields() {
        return this.attributeFields;
    }

    public void setMediaField(MediaField m) {
        this.mediaField = m;
    }

    public void setInformationField(InformationField i) {
        this.informationField = i;
    }

    public void setConnectionField(ConnectionField c) {
        this.connectionField = c;
    }

    public void addBandwidthField(BandwidthField b) {
        this.bandwidthFields.add(b);
    }

    public void setKeyField(KeyField k) {
        this.keyField = k;
    }

    public void setAttributeFields(Vector a) {
        this.attributeFields = a;
    }

    public Media getMedia() {
        return this.mediaField;
    }

    public void addAttribute(AttributeField af) {
        this.attributeFields.add(af);
    }

    /* access modifiers changed from: protected */
    public boolean hasAttribute(String name) {
        for (int i = 0; i < this.attributeFields.size(); i++) {
            if (((AttributeField) this.attributeFields.elementAt(i)).getAttribute().getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void setMedia(Media media) throws SdpException {
        if (media == null) {
            throw new SdpException("The media is null");
        } else if (media instanceof MediaField) {
            this.mediaField = (MediaField) media;
        } else {
            throw new SdpException("A mediaField parameter is required");
        }
    }

    public Info getInfo() {
        InformationField informationField = getInformationField();
        if (informationField == null) {
            return null;
        }
        return informationField;
    }

    public void setInfo(Info i) throws SdpException {
        if (i == null) {
            throw new SdpException("The info is null");
        } else if (i instanceof InformationField) {
            this.informationField = (InformationField) i;
        } else {
            throw new SdpException("A informationField parameter is required");
        }
    }

    public Connection getConnection() {
        return this.connectionField;
    }

    public void setConnection(Connection conn) throws SdpException {
        if (conn == null) {
            throw new SdpException("The conn is null");
        } else if (conn instanceof ConnectionField) {
            this.connectionField = (ConnectionField) conn;
        } else {
            throw new SdpException("bad implementation");
        }
    }

    public Vector getBandwidths(boolean create) {
        return this.bandwidthFields;
    }

    public void setBandwidths(Vector bandwidths) throws SdpException {
        if (bandwidths == null) {
            throw new SdpException("The vector bandwidths is null");
        }
        this.bandwidthFields = bandwidths;
    }

    public int getBandwidth(String name) throws SdpParseException {
        if (name == null) {
            throw new NullPointerException("null parameter");
        } else if (this.bandwidthFields == null) {
            return -1;
        } else {
            for (int i = 0; i < this.bandwidthFields.size(); i++) {
                BandwidthField bandwidthField = (BandwidthField) this.bandwidthFields.elementAt(i);
                String type = bandwidthField.getBwtype();
                if (type != null && type.equals(name)) {
                    return bandwidthField.getBandwidth();
                }
            }
            return -1;
        }
    }

    public void setBandwidth(String name, int value) throws SdpException {
        if (name == null) {
            throw new SdpException("The name is null");
        }
        BandwidthField bandwidthField;
        int i = 0;
        while (i < this.bandwidthFields.size()) {
            bandwidthField = (BandwidthField) this.bandwidthFields.elementAt(i);
            String type = bandwidthField.getBwtype();
            if (type != null && type.equals(name)) {
                bandwidthField.setBandwidth(value);
                break;
            }
            i++;
        }
        if (i == this.bandwidthFields.size()) {
            bandwidthField = new BandwidthField();
            bandwidthField.setType(name);
            bandwidthField.setValue(value);
            this.bandwidthFields.add(bandwidthField);
        }
    }

    public void removeBandwidth(String name) {
        if (name == null) {
            throw new NullPointerException("null bandwidth type");
        }
        int i = 0;
        while (i < this.bandwidthFields.size()) {
            String type = ((BandwidthField) this.bandwidthFields.elementAt(i)).getBwtype();
            if (type != null && type.equals(name)) {
                break;
            }
            i++;
        }
        if (i < this.bandwidthFields.size()) {
            this.bandwidthFields.removeElementAt(i);
        }
    }

    public Key getKey() {
        if (this.keyField == null) {
            return null;
        }
        return this.keyField;
    }

    public void setKey(Key key) throws SdpException {
        if (key == null) {
            throw new SdpException("The key is null");
        } else if (key instanceof KeyField) {
            setKeyField((KeyField) key);
        } else {
            throw new SdpException("A keyField parameter is required");
        }
    }

    public Vector getAttributes(boolean create) {
        return this.attributeFields;
    }

    public void setAttributes(Vector attributes) throws SdpException {
        this.attributeFields = attributes;
    }

    public String getAttribute(String name) throws SdpParseException {
        if (name != null) {
            for (int i = 0; i < this.attributeFields.size(); i++) {
                AttributeField af = (AttributeField) this.attributeFields.elementAt(i);
                if (name.equals(af.getAttribute().getName())) {
                    return (String) af.getAttribute().getValueAsObject();
                }
            }
            return null;
        }
        throw new NullPointerException("null arg!");
    }

    public void setAttribute(String name, String value) throws SdpException {
        if (name == null) {
            throw new SdpException("The parameters are null");
        }
        AttributeField af;
        int i = 0;
        while (i < this.attributeFields.size()) {
            af = (AttributeField) this.attributeFields.elementAt(i);
            if (af.getAttribute().getName().equals(name)) {
                af.getAttribute().setValueAsObject(value);
                break;
            }
            i++;
        }
        if (i == this.attributeFields.size()) {
            af = new AttributeField();
            af.setAttribute(new NameValue(name, value));
            this.attributeFields.add(af);
        }
    }

    public String getDuplexity() {
        for (int i = 0; i < this.attributeFields.size(); i++) {
            AttributeField af = (AttributeField) this.attributeFields.elementAt(i);
            if (af.getAttribute().getName().equalsIgnoreCase("sendrecv") || af.getAttribute().getName().equalsIgnoreCase("recvonly") || af.getAttribute().getName().equalsIgnoreCase("sendonly") || af.getAttribute().getName().equalsIgnoreCase("inactive")) {
                return af.getAttribute().getName();
            }
        }
        return null;
    }

    public void setDuplexity(String duplexity) {
        if (duplexity == null) {
            throw new NullPointerException("Null arg");
        }
        AttributeField af;
        int i = 0;
        while (i < this.attributeFields.size()) {
            af = (AttributeField) this.attributeFields.elementAt(i);
            if (af.getAttribute().getName().equalsIgnoreCase("sendrecv") || af.getAttribute().getName().equalsIgnoreCase("recvonly") || af.getAttribute().getName().equalsIgnoreCase("sendonly") || af.getAttribute().getName().equalsIgnoreCase("inactive")) {
                af.setAttribute(new NameValue(duplexity, null));
                return;
            }
            i++;
        }
        if (i == this.attributeFields.size()) {
            af = new AttributeField();
            af.setAttribute(new NameValue(duplexity, null));
            this.attributeFields.add(af);
        }
    }

    public void removeAttribute(String name) {
        if (name == null) {
            throw new NullPointerException("null arg!");
        } else if (name != null) {
            int i = 0;
            while (i < this.attributeFields.size() && !((AttributeField) this.attributeFields.elementAt(i)).getAttribute().getName().equals(name)) {
                i++;
            }
            if (i < this.attributeFields.size()) {
                this.attributeFields.removeElementAt(i);
            }
        }
    }

    public Vector getMimeTypes() throws SdpException {
        MediaField mediaField = (MediaField) getMedia();
        String type = mediaField.getMediaType();
        String protocol = mediaField.getProtocol();
        Vector formats = mediaField.getMediaFormats(false);
        Vector v = new Vector();
        for (int i = 0; i < formats.size(); i++) {
            String result = null;
            if (!protocol.equals(SdpConstants.RTP_AVP)) {
                result = type + Separators.SLASH + protocol;
            } else if (getAttribute(SdpConstants.RTPMAP) != null) {
                result = type + Separators.SLASH + protocol;
            }
            v.addElement(result);
        }
        return v;
    }

    public Vector getMimeParameters() throws SdpException {
        String rate = getAttribute("rate");
        String ptime = getAttribute(PayloadTypePacketExtension.PTIME_ATTR_NAME);
        String maxptime = getAttribute(PayloadTypePacketExtension.MAXPTIME_ATTR_NAME);
        String ftmp = getAttribute("ftmp");
        Vector result = new Vector();
        result.addElement(rate);
        result.addElement(ptime);
        result.addElement(maxptime);
        result.addElement(ftmp);
        return result;
    }

    public void addDynamicPayloads(Vector payloadNames, Vector payloadValues) throws SdpException {
        if (payloadNames == null || payloadValues == null) {
            throw new SdpException(" The vectors are null");
        } else if (payloadNames.isEmpty() || payloadValues.isEmpty()) {
            throw new SdpException(" The vectors are empty");
        } else if (payloadNames.size() != payloadValues.size()) {
            throw new SdpException(" The vector sizes are unequal");
        } else {
            for (int i = 0; i < payloadNames.size(); i++) {
                setAttribute((String) payloadNames.elementAt(i), (String) payloadValues.elementAt(i));
            }
        }
    }

    public void setPreconditionFields(Vector precondition) throws SdpException {
        this.preconditionFields.setPreconditions(precondition);
    }

    public void setPreconditions(PreconditionFields precondition) {
        this.preconditionFields = precondition;
    }

    public Vector getPreconditionFields() {
        return this.preconditionFields.getPreconditions();
    }
}
