package net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy;

import org.jitsi.util.StringUtils;

public class SphereType {
    private String value;

    public SphereType(String value) {
        if (StringUtils.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("value cannot be null or empty");
        }
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
