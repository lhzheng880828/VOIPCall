package net.java.sip.communicator.impl.protocol.sip.xcap.model.prescontent;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

public class MimeType {
    private Map<QName, String> anyAttributes = new HashMap();
    protected String value;

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Map<QName, String> getAnyAttributes() {
        return this.anyAttributes;
    }
}
