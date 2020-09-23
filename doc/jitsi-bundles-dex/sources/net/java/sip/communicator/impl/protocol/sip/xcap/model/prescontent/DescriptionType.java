package net.java.sip.communicator.impl.protocol.sip.xcap.model.prescontent;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

public class DescriptionType {
    private Map<QName, String> anyAttributes;
    private String lang;
    private String value;

    public DescriptionType() {
        this.anyAttributes = new HashMap();
    }

    public DescriptionType(String value) {
        this(value, null);
    }

    public DescriptionType(String value, String lang) {
        this.anyAttributes = new HashMap();
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

    public Map<QName, String> getAnyAttributes() {
        return this.anyAttributes;
    }
}
