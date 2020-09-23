package javax.sdp;

import gov.nist.javax.sdp.fields.AttributeField;
import gov.nist.javax.sdp.fields.PreconditionFields;
import java.io.Serializable;
import java.util.Vector;

public interface MediaDescription extends Serializable, Cloneable {
    void addAttribute(AttributeField attributeField);

    void addDynamicPayloads(Vector vector, Vector vector2) throws SdpException;

    String getAttribute(String str) throws SdpParseException;

    Vector getAttributes(boolean z);

    int getBandwidth(String str) throws SdpParseException;

    Vector getBandwidths(boolean z);

    Connection getConnection();

    Info getInfo();

    Key getKey();

    Media getMedia();

    Vector getMimeParameters() throws SdpException;

    Vector getMimeTypes() throws SdpException;

    Vector getPreconditionFields();

    void removeAttribute(String str);

    void removeBandwidth(String str);

    void setAttribute(String str, String str2) throws SdpException;

    void setAttributes(Vector vector) throws SdpException;

    void setBandwidth(String str, int i) throws SdpException;

    void setBandwidths(Vector vector) throws SdpException;

    void setConnection(Connection connection) throws SdpException;

    void setInfo(Info info) throws SdpException;

    void setKey(Key key) throws SdpException;

    void setMedia(Media media) throws SdpException;

    void setPreconditionFields(Vector vector) throws SdpException;

    void setPreconditions(PreconditionFields preconditionFields);
}
