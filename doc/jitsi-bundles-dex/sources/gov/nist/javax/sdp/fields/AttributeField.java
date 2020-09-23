package gov.nist.javax.sdp.fields;

import javax.sdp.Attribute;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import org.jitsi.gov.nist.core.NameValue;
import org.jitsi.gov.nist.core.Separators;

public class AttributeField extends SDPField implements Attribute {
    protected NameValue attribute;

    public NameValue getAttribute() {
        return this.attribute;
    }

    public AttributeField() {
        super(SDPFieldNames.ATTRIBUTE_FIELD);
    }

    public void setAttribute(NameValue a) {
        this.attribute = a;
        this.attribute.setSeparator(Separators.COLON);
    }

    public String encode() {
        String encoded_string = SDPFieldNames.ATTRIBUTE_FIELD;
        if (this.attribute != null) {
            encoded_string = encoded_string + this.attribute.encode();
        }
        return encoded_string + Separators.NEWLINE;
    }

    public String toString() {
        return encode();
    }

    public String getName() throws SdpParseException {
        NameValue nameValue = getAttribute();
        if (nameValue == null) {
            return null;
        }
        String name = nameValue.getName();
        if (name == null) {
            return null;
        }
        return name;
    }

    public void setName(String name) throws SdpException {
        if (name == null) {
            throw new SdpException("The name is null");
        }
        NameValue nameValue = getAttribute();
        if (nameValue == null) {
            nameValue = new NameValue();
        }
        nameValue.setName(name);
        setAttribute(nameValue);
    }

    public boolean hasValue() throws SdpParseException {
        NameValue nameValue = getAttribute();
        if (nameValue == null || nameValue.getValueAsObject() == null) {
            return false;
        }
        return true;
    }

    public String getValue() throws SdpParseException {
        NameValue nameValue = getAttribute();
        if (nameValue == null) {
            return null;
        }
        Object value = nameValue.getValueAsObject();
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return value.toString();
    }

    public void setValue(String value) throws SdpException {
        if (value == null) {
            throw new SdpException("The value is null");
        }
        NameValue nameValue = getAttribute();
        if (nameValue == null) {
            nameValue = new NameValue();
        }
        nameValue.setValueAsObject(value);
        setAttribute(nameValue);
    }

    public void setValueAllowNull(String value) {
        NameValue nameValue = getAttribute();
        if (nameValue == null) {
            nameValue = new NameValue();
        }
        nameValue.setValueAsObject(value);
        setAttribute(nameValue);
    }

    public Object clone() {
        AttributeField retval = (AttributeField) super.clone();
        if (this.attribute != null) {
            retval.attribute = (NameValue) this.attribute.clone();
        }
        return retval;
    }

    public boolean equals(Object that) {
        if (!(that instanceof AttributeField)) {
            return false;
        }
        AttributeField other = (AttributeField) that;
        if (other.getAttribute().getName().equalsIgnoreCase(getAttribute().getName()) && getAttribute().getValueAsObject().equals(other.getAttribute().getValueAsObject())) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        if (getAttribute() != null) {
            return encode().hashCode();
        }
        throw new UnsupportedOperationException("Attribute is null cannot compute hashCode ");
    }
}
