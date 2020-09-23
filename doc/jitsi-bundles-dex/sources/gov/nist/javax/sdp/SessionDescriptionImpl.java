package gov.nist.javax.sdp;

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
import gov.nist.javax.sdp.fields.SDPField;
import gov.nist.javax.sdp.fields.SessionNameField;
import gov.nist.javax.sdp.fields.TimeField;
import gov.nist.javax.sdp.fields.URIField;
import gov.nist.javax.sdp.fields.ZoneField;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Vector;
import javax.sdp.Connection;
import javax.sdp.Info;
import javax.sdp.Key;
import javax.sdp.Origin;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;
import javax.sdp.SessionName;
import javax.sdp.URI;
import javax.sdp.Version;

public class SessionDescriptionImpl implements SessionDescription {
    protected Vector attributesList;
    protected Vector bandwidthList;
    protected ConnectionField connectionImpl;
    private MediaDescriptionImpl currentMediaDescription;
    private TimeDescriptionImpl currentTimeDescription;
    protected Vector emailList;
    protected InformationField infoImpl;
    protected KeyField keyImpl;
    protected Vector mediaDescriptions;
    protected OriginField originImpl;
    protected Vector phoneList;
    protected SessionNameField sessionNameImpl;
    protected Vector timeDescriptions;
    protected URIField uriImpl;
    protected ProtoVersionField versionImpl;
    protected Vector zoneAdjustments;

    public SessionDescriptionImpl(SessionDescription otherSessionDescription) throws SdpException {
        if (otherSessionDescription != null) {
            Version otherVersion = otherSessionDescription.getVersion();
            if (otherVersion != null) {
                setVersion((Version) otherVersion.clone());
            }
            Origin otherOrigin = otherSessionDescription.getOrigin();
            if (otherOrigin != null) {
                setOrigin((Origin) otherOrigin.clone());
            }
            SessionName otherSessionName = otherSessionDescription.getSessionName();
            if (otherSessionName != null) {
                setSessionName((SessionName) otherSessionName.clone());
            }
            Info otherInfo = otherSessionDescription.getInfo();
            if (otherInfo != null) {
                setInfo((Info) otherInfo.clone());
            }
            URIField otherUriField = (URIField) otherSessionDescription.getURI();
            if (otherUriField != null) {
                URIField newUF = new URIField();
                newUF.setURI(otherUriField.toString());
                setURI(newUF);
            }
            Connection otherConnection = otherSessionDescription.getConnection();
            if (otherConnection != null) {
                setConnection((Connection) otherConnection.clone());
            }
            Key otherKey = otherSessionDescription.getKey();
            if (otherKey != null) {
                setKey((Key) otherKey.clone());
            }
            Vector otherTimeDescriptions = otherSessionDescription.getTimeDescriptions(false);
            if (otherTimeDescriptions != null) {
                Vector newTDs = new Vector();
                Iterator itTimeDescriptions = otherTimeDescriptions.iterator();
                while (itTimeDescriptions.hasNext()) {
                    TimeDescriptionImpl otherTimeDescription = (TimeDescriptionImpl) itTimeDescriptions.next();
                    if (otherTimeDescription != null) {
                        TimeDescriptionImpl timeDescriptionImpl = new TimeDescriptionImpl((TimeField) otherTimeDescription.getTime().clone());
                        Vector otherRepeatTimes = otherTimeDescription.getRepeatTimes(false);
                        if (otherRepeatTimes != null) {
                            Iterator itRepeatTimes = otherRepeatTimes.iterator();
                            while (itRepeatTimes.hasNext()) {
                                RepeatField otherRepeatField = (RepeatField) itRepeatTimes.next();
                                if (otherRepeatField != null) {
                                    timeDescriptionImpl.addRepeatField((RepeatField) otherRepeatField.clone());
                                }
                            }
                        }
                        newTDs.add(timeDescriptionImpl);
                    }
                }
                setTimeDescriptions(newTDs);
            }
            Vector otherEmails = otherSessionDescription.getEmails(false);
            if (otherEmails != null) {
                Vector newEmails = new Vector();
                Iterator itEmails = otherEmails.iterator();
                while (itEmails.hasNext()) {
                    EmailField otherEmailField = (EmailField) itEmails.next();
                    if (otherEmailField != null) {
                        newEmails.add((EmailField) otherEmailField.clone());
                    }
                }
                setEmails(newEmails);
            }
            Vector otherPhones = otherSessionDescription.getPhones(false);
            if (otherPhones != null) {
                Vector newPhones = new Vector();
                Iterator itPhones = otherPhones.iterator();
                while (itPhones.hasNext()) {
                    PhoneField otherPhoneField = (PhoneField) itPhones.next();
                    if (otherPhoneField != null) {
                        newPhones.add((PhoneField) otherPhoneField.clone());
                    }
                }
                setPhones(newPhones);
            }
            Vector otherZAs = otherSessionDescription.getZoneAdjustments(false);
            if (otherZAs != null) {
                Vector newZAs = new Vector();
                Iterator itZAs = otherZAs.iterator();
                while (itZAs.hasNext()) {
                    ZoneField otherZoneField = (ZoneField) itZAs.next();
                    if (otherZoneField != null) {
                        newZAs.add((ZoneField) otherZoneField.clone());
                    }
                }
                setZoneAdjustments(newZAs);
            }
            Vector otherBandwidths = otherSessionDescription.getBandwidths(false);
            if (otherBandwidths != null) {
                Vector newBandwidths = new Vector();
                Iterator itBandwidths = otherBandwidths.iterator();
                while (itBandwidths.hasNext()) {
                    BandwidthField otherBandwidthField = (BandwidthField) itBandwidths.next();
                    if (otherBandwidthField != null) {
                        newBandwidths.add((BandwidthField) otherBandwidthField.clone());
                    }
                }
                setBandwidths(newBandwidths);
            }
            Vector otherAttributes = otherSessionDescription.getAttributes(false);
            if (otherAttributes != null) {
                Vector newAttributes = new Vector();
                Iterator itAttributes = otherAttributes.iterator();
                while (itAttributes.hasNext()) {
                    AttributeField otherAttributeField = (AttributeField) itAttributes.next();
                    if (otherAttributeField != null) {
                        newAttributes.add((AttributeField) otherAttributeField.clone());
                    }
                }
                setAttributes(newAttributes);
            }
            Vector otherMediaDescriptions = otherSessionDescription.getMediaDescriptions(false);
            if (otherMediaDescriptions != null) {
                Vector newMDs = new Vector();
                Iterator itMediaDescriptions = otherMediaDescriptions.iterator();
                while (itMediaDescriptions.hasNext()) {
                    MediaDescriptionImpl otherMediaDescription = (MediaDescriptionImpl) itMediaDescriptions.next();
                    if (otherMediaDescription != null) {
                        MediaDescriptionImpl newMD = new MediaDescriptionImpl();
                        MediaField otherMediaField = otherMediaDescription.getMediaField();
                        if (otherMediaField != null) {
                            MediaField newMF = new MediaField();
                            newMF.setMedia(otherMediaField.getMedia());
                            newMF.setPort(otherMediaField.getPort());
                            newMF.setNports(otherMediaField.getNports());
                            newMF.setProto(otherMediaField.getProto());
                            Vector otherFormats = otherMediaField.getFormats();
                            if (otherFormats != null) {
                                Vector newFormats = new Vector();
                                Iterator itFormats = otherFormats.iterator();
                                while (itFormats.hasNext()) {
                                    Object otherFormat = itFormats.next();
                                    if (otherFormat != null) {
                                        newFormats.add(String.valueOf(otherFormat));
                                    }
                                }
                                newMF.setFormats(newFormats);
                            }
                            newMD.setMedia(newMF);
                        }
                        InformationField otherInfoField = otherMediaDescription.getInformationField();
                        if (otherInfoField != null) {
                            newMD.setInformationField((InformationField) otherInfoField.clone());
                        }
                        ConnectionField otherConnectionField = otherMediaDescription.getConnectionField();
                        if (otherConnectionField != null) {
                            newMD.setConnectionField((ConnectionField) otherConnectionField.clone());
                        }
                        Vector otherBFs = otherMediaDescription.getBandwidths(false);
                        if (otherBFs != null) {
                            Vector newBFs = new Vector();
                            Iterator itBFs = otherBFs.iterator();
                            while (itBFs.hasNext()) {
                                BandwidthField otherBF = (BandwidthField) itBFs.next();
                                if (otherBF != null) {
                                    newBFs.add((BandwidthField) otherBF.clone());
                                }
                            }
                            newMD.setBandwidths(newBFs);
                        }
                        KeyField otherKeyField = otherMediaDescription.getKeyField();
                        if (otherKeyField != null) {
                            newMD.setKeyField((KeyField) otherKeyField.clone());
                        }
                        Vector otherAFs = otherMediaDescription.getAttributeFields();
                        if (otherAFs != null) {
                            Vector newAFs = new Vector();
                            Iterator itAFs = otherAFs.iterator();
                            while (itAFs.hasNext()) {
                                AttributeField otherAF = (AttributeField) itAFs.next();
                                if (otherAF != null) {
                                    newAFs.add((AttributeField) otherAF.clone());
                                }
                            }
                            newMD.setAttributeFields(newAFs);
                        }
                        newMDs.add(newMD);
                    }
                }
                setMediaDescriptions(newMDs);
            }
        }
    }

    public void addField(SDPField sdpField) throws ParseException {
        try {
            if (sdpField instanceof ProtoVersionField) {
                this.versionImpl = (ProtoVersionField) sdpField;
            } else if (sdpField instanceof OriginField) {
                this.originImpl = (OriginField) sdpField;
            } else if (sdpField instanceof SessionNameField) {
                this.sessionNameImpl = (SessionNameField) sdpField;
            } else if (sdpField instanceof InformationField) {
                if (this.currentMediaDescription != null) {
                    this.currentMediaDescription.setInformationField((InformationField) sdpField);
                } else {
                    this.infoImpl = (InformationField) sdpField;
                }
            } else if (sdpField instanceof URIField) {
                this.uriImpl = (URIField) sdpField;
            } else if (sdpField instanceof ConnectionField) {
                if (this.currentMediaDescription != null) {
                    this.currentMediaDescription.setConnectionField((ConnectionField) sdpField);
                } else {
                    this.connectionImpl = (ConnectionField) sdpField;
                }
            } else if (sdpField instanceof KeyField) {
                if (this.currentMediaDescription != null) {
                    this.currentMediaDescription.setKey((KeyField) sdpField);
                } else {
                    this.keyImpl = (KeyField) sdpField;
                }
            } else if (sdpField instanceof EmailField) {
                getEmails(true).add(sdpField);
            } else if (sdpField instanceof PhoneField) {
                getPhones(true).add(sdpField);
            } else if (sdpField instanceof TimeField) {
                this.currentTimeDescription = new TimeDescriptionImpl((TimeField) sdpField);
                getTimeDescriptions(true).add(this.currentTimeDescription);
            } else if (sdpField instanceof RepeatField) {
                if (this.currentTimeDescription == null) {
                    throw new ParseException("no time specified", 0);
                }
                this.currentTimeDescription.addRepeatField((RepeatField) sdpField);
            } else if (sdpField instanceof ZoneField) {
                getZoneAdjustments(true).add(sdpField);
            } else if (sdpField instanceof BandwidthField) {
                if (this.currentMediaDescription != null) {
                    this.currentMediaDescription.addBandwidthField((BandwidthField) sdpField);
                } else {
                    getBandwidths(true).add(sdpField);
                }
            } else if (sdpField instanceof AttributeField) {
                if (this.currentMediaDescription != null) {
                    String s = ((AttributeField) sdpField).getName();
                    this.currentMediaDescription.addAttribute((AttributeField) sdpField);
                    return;
                }
                getAttributes(true).add(sdpField);
            } else if (sdpField instanceof MediaField) {
                this.currentMediaDescription = new MediaDescriptionImpl();
                getMediaDescriptions(true).add(this.currentMediaDescription);
                this.currentMediaDescription.setMediaField((MediaField) sdpField);
            }
        } catch (SdpException e) {
            throw new ParseException(sdpField.encode(), 0);
        }
    }

    public Object clone() throws CloneNotSupportedException {
        try {
            return new SessionDescriptionImpl(this);
        } catch (SdpException e) {
            throw new CloneNotSupportedException();
        }
    }

    public Version getVersion() {
        return this.versionImpl;
    }

    public void setVersion(Version v) throws SdpException {
        if (v == null) {
            throw new SdpException("The parameter is null");
        } else if (v instanceof ProtoVersionField) {
            this.versionImpl = (ProtoVersionField) v;
        } else {
            throw new SdpException("The parameter must be an instance of VersionField");
        }
    }

    public Origin getOrigin() {
        return this.originImpl;
    }

    public void setOrigin(Origin origin) throws SdpException {
        if (origin == null) {
            throw new SdpException("The parameter is null");
        } else if (origin instanceof OriginField) {
            this.originImpl = (OriginField) origin;
        } else {
            throw new SdpException("The parameter must be an instance of OriginField");
        }
    }

    public SessionName getSessionName() {
        return this.sessionNameImpl;
    }

    public void setSessionName(SessionName sessionName) throws SdpException {
        if (sessionName == null) {
            throw new SdpException("The parameter is null");
        } else if (sessionName instanceof SessionNameField) {
            this.sessionNameImpl = (SessionNameField) sessionName;
        } else {
            throw new SdpException("The parameter must be an instance of SessionNameField");
        }
    }

    public Info getInfo() {
        return this.infoImpl;
    }

    public void setInfo(Info i) throws SdpException {
        if (i == null) {
            throw new SdpException("The parameter is null");
        } else if (i instanceof InformationField) {
            this.infoImpl = (InformationField) i;
        } else {
            throw new SdpException("The parameter must be an instance of InformationField");
        }
    }

    public URI getURI() {
        return this.uriImpl;
    }

    public void setURI(URI uri) throws SdpException {
        if (uri == null) {
            throw new SdpException("The parameter is null");
        } else if (uri instanceof URIField) {
            this.uriImpl = (URIField) uri;
        } else {
            throw new SdpException("The parameter must be an instance of URIField");
        }
    }

    public Vector getEmails(boolean create) throws SdpParseException {
        if (this.emailList == null && create) {
            this.emailList = new Vector();
        }
        return this.emailList;
    }

    public void setEmails(Vector emails) throws SdpException {
        if (emails == null) {
            throw new SdpException("The parameter is null");
        }
        this.emailList = emails;
    }

    public Vector getPhones(boolean create) throws SdpException {
        if (this.phoneList == null && create) {
            this.phoneList = new Vector();
        }
        return this.phoneList;
    }

    public void setPhones(Vector phones) throws SdpException {
        if (phones == null) {
            throw new SdpException("The parameter is null");
        }
        this.phoneList = phones;
    }

    public Vector getTimeDescriptions(boolean create) throws SdpException {
        if (this.timeDescriptions == null && create) {
            this.timeDescriptions = new Vector();
        }
        return this.timeDescriptions;
    }

    public void setTimeDescriptions(Vector times) throws SdpException {
        if (times == null) {
            throw new SdpException("The parameter is null");
        }
        this.timeDescriptions = times;
    }

    public Vector getZoneAdjustments(boolean create) throws SdpException {
        if (this.zoneAdjustments == null && create) {
            this.zoneAdjustments = new Vector();
        }
        return this.zoneAdjustments;
    }

    public void setZoneAdjustments(Vector zoneAdjustments) throws SdpException {
        if (zoneAdjustments == null) {
            throw new SdpException("The parameter is null");
        }
        this.zoneAdjustments = zoneAdjustments;
    }

    public Connection getConnection() {
        return this.connectionImpl;
    }

    public void setConnection(Connection conn) throws SdpException {
        if (conn == null) {
            throw new SdpException("The parameter is null");
        } else if (conn instanceof ConnectionField) {
            this.connectionImpl = (ConnectionField) conn;
        } else {
            throw new SdpException("Bad implementation class ConnectionField");
        }
    }

    public Vector getBandwidths(boolean create) {
        if (this.bandwidthList == null && create) {
            this.bandwidthList = new Vector();
        }
        return this.bandwidthList;
    }

    public void setBandwidths(Vector bandwidthList) throws SdpException {
        if (bandwidthList == null) {
            throw new SdpException("The parameter is null");
        }
        this.bandwidthList = bandwidthList;
    }

    public int getBandwidth(String name) throws SdpParseException {
        if (name == null || this.bandwidthList == null) {
            return -1;
        }
        for (int i = 0; i < this.bandwidthList.size(); i++) {
            BandwidthField o = this.bandwidthList.elementAt(i);
            if (o instanceof BandwidthField) {
                BandwidthField b = o;
                String type = b.getType();
                if (type != null && name.equals(type)) {
                    return b.getValue();
                }
            }
        }
        return -1;
    }

    public void setBandwidth(String name, int value) throws SdpException {
        if (name == null) {
            throw new SdpException("The parameter is null");
        } else if (this.bandwidthList != null) {
            for (int i = 0; i < this.bandwidthList.size(); i++) {
                BandwidthField o = this.bandwidthList.elementAt(i);
                if (o instanceof BandwidthField) {
                    BandwidthField b = o;
                    String type = b.getType();
                    if (type != null && name.equals(type)) {
                        b.setValue(value);
                    }
                }
            }
        }
    }

    public void removeBandwidth(String name) {
        if (name != null && this.bandwidthList != null) {
            for (int i = 0; i < this.bandwidthList.size(); i++) {
                BandwidthField o = this.bandwidthList.elementAt(i);
                if (o instanceof BandwidthField) {
                    BandwidthField b = o;
                    try {
                        String type = b.getType();
                        if (type != null && name.equals(type)) {
                            this.bandwidthList.remove(b);
                        }
                    } catch (SdpParseException e) {
                    }
                }
            }
        }
    }

    public Key getKey() {
        return this.keyImpl;
    }

    public void setKey(Key key) throws SdpException {
        if (key == null) {
            throw new SdpException("The parameter is null");
        } else if (key instanceof KeyField) {
            this.keyImpl = (KeyField) key;
        } else {
            throw new SdpException("The parameter must be an instance of KeyField");
        }
    }

    public String getAttribute(String name) throws SdpParseException {
        if (name == null || this.attributesList == null) {
            return null;
        }
        for (int i = 0; i < this.attributesList.size(); i++) {
            AttributeField o = this.attributesList.elementAt(i);
            if (o instanceof AttributeField) {
                AttributeField a = o;
                String n = a.getName();
                if (n != null && name.equals(n)) {
                    return a.getValue();
                }
            }
        }
        return null;
    }

    public Vector getAttributes(boolean create) {
        if (this.attributesList == null && create) {
            this.attributesList = new Vector();
        }
        return this.attributesList;
    }

    public void removeAttribute(String name) {
        if (name != null && this.attributesList != null) {
            for (int i = 0; i < this.attributesList.size(); i++) {
                AttributeField o = this.attributesList.elementAt(i);
                if (o instanceof AttributeField) {
                    AttributeField a = o;
                    try {
                        String n = a.getName();
                        if (n != null && name.equals(n)) {
                            this.attributesList.remove(a);
                        }
                    } catch (SdpParseException e) {
                    }
                }
            }
        }
    }

    public void setAttribute(String name, String value) throws SdpException {
        if (name == null || value == null) {
            throw new SdpException("The parameter is null");
        } else if (this.attributesList != null) {
            for (int i = 0; i < this.attributesList.size(); i++) {
                AttributeField o = this.attributesList.elementAt(i);
                if (o instanceof AttributeField) {
                    AttributeField a = o;
                    String n = a.getName();
                    if (n != null && name.equals(n)) {
                        a.setValue(value);
                    }
                }
            }
        }
    }

    public void setAttributes(Vector attributes) throws SdpException {
        if (attributes == null) {
            throw new SdpException("The parameter is null");
        }
        this.attributesList = attributes;
    }

    public Vector getMediaDescriptions(boolean create) throws SdpException {
        if (this.mediaDescriptions == null && create) {
            this.mediaDescriptions = new Vector();
        }
        return this.mediaDescriptions;
    }

    public void setMediaDescriptions(Vector mediaDescriptions) throws SdpException {
        if (mediaDescriptions == null) {
            throw new SdpException("The parameter is null");
        }
        this.mediaDescriptions = mediaDescriptions;
    }

    private String encodeVector(Vector vector) {
        StringBuilder encBuff = new StringBuilder();
        for (int i = 0; i < vector.size(); i++) {
            encBuff.append(vector.elementAt(i));
        }
        return encBuff.toString();
    }

    public String toString() {
        StringBuilder encBuff = new StringBuilder();
        encBuff.append(getVersion() == null ? "" : getVersion().toString());
        encBuff.append(getOrigin() == null ? "" : getOrigin().toString());
        encBuff.append(getSessionName() == null ? "" : getSessionName().toString());
        encBuff.append(getInfo() == null ? "" : getInfo().toString());
        try {
            encBuff.append(getURI() == null ? "" : getURI().toString());
            encBuff.append(getEmails(false) == null ? "" : encodeVector(getEmails(false)));
            encBuff.append(getPhones(false) == null ? "" : encodeVector(getPhones(false)));
            encBuff.append(getConnection() == null ? "" : getConnection().toString());
            encBuff.append(getBandwidths(false) == null ? "" : encodeVector(getBandwidths(false)));
            encBuff.append(getTimeDescriptions(false) == null ? "" : encodeVector(getTimeDescriptions(false)));
            encBuff.append(getZoneAdjustments(false) == null ? "" : encodeVector(getZoneAdjustments(false)));
            encBuff.append(getKey() == null ? "" : getKey().toString());
            encBuff.append(getAttributes(false) == null ? "" : encodeVector(getAttributes(false)));
            encBuff.append(getMediaDescriptions(false) == null ? "" : encodeVector(getMediaDescriptions(false)));
        } catch (SdpException e) {
        }
        return encBuff.toString();
    }
}
