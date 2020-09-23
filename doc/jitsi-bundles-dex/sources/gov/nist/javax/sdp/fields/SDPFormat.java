package gov.nist.javax.sdp.fields;

public class SDPFormat extends SDPObject {
    protected String format;

    public void setFormat(String fmt) {
        this.format = fmt;
    }

    public String getFormat() {
        return this.format;
    }

    public SDPFormat(String s) {
        this.format = s;
    }

    public String encode() {
        return this.format;
    }
}
