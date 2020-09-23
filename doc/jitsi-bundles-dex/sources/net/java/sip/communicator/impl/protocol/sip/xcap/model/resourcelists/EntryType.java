package net.java.sip.communicator.impl.protocol.sip.xcap.model.resourcelists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

public class EntryType {
    private List<Element> any;
    private Map<QName, String> anyAttributes = new HashMap();
    private DisplayNameType displayName;
    private String uri;

    EntryType() {
    }

    public EntryType(String uri) {
        if (uri == null || uri.trim().length() == 0) {
            throw new IllegalArgumentException("The uri attribute cannot be null or empry");
        }
        this.uri = uri;
    }

    public String getUri() {
        return this.uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
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

    public void setAny(List<Element> any) {
        this.any = any;
    }

    public Map<QName, String> getAnyAttributes() {
        return this.anyAttributes;
    }

    public void setAnyAttributes(Map<QName, String> anyAttributes) {
        this.anyAttributes = anyAttributes;
    }
}
