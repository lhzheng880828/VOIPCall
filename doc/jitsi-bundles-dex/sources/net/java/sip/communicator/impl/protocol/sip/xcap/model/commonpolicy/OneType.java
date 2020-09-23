package net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy;

import org.jitsi.util.StringUtils;
import org.w3c.dom.Element;

public class OneType {
    private Element any;
    private String id;

    public OneType(String id) {
        if (StringUtils.isNullOrEmpty(id)) {
            throw new IllegalArgumentException("id cannot be null or empty");
        }
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Element getAny() {
        return this.any;
    }

    public void setAny(Element any) {
        this.any = any;
    }
}
