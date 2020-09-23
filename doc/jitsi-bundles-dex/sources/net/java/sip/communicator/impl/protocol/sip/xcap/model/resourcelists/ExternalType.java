package net.java.sip.communicator.impl.protocol.sip.xcap.model.resourcelists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

public class ExternalType {
    protected String anchor;
    private List<Element> any;
    private Map<QName, String> anyAttributes = new HashMap();
    private DisplayNameType displayName;

    public String getAnchor() {
        return this.anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    public DisplayNameType getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(DisplayNameType displayName) {
        this.displayName = displayName;
    }

    public List<Element> getAny() {
        if (this.any == null) {
            this.any = new ArrayList();
        }
        return this.any;
    }

    public Map<QName, String> getAnyAttributes() {
        return this.anyAttributes;
    }
}
