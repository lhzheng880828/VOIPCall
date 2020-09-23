package gov.nist.javax.sdp.fields;

public abstract class SDPField extends SDPObject {
    protected String fieldName;

    public abstract String encode();

    protected SDPField(String hname) {
        this.fieldName = hname;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public char getTypeChar() {
        if (this.fieldName == null) {
            return 0;
        }
        return this.fieldName.charAt(0);
    }

    public String toString() {
        return encode();
    }
}
