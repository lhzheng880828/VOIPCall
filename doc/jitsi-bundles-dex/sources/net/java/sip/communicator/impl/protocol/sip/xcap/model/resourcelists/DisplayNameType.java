package net.java.sip.communicator.impl.protocol.sip.xcap.model.resourcelists;

public class DisplayNameType {
    private String lang;
    private String value;

    public DisplayNameType(String value, String lang) {
        this.value = value;
        this.lang = lang;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLang() {
        return this.lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
