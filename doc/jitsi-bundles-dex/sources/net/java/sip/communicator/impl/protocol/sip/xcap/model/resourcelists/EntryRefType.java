package net.java.sip.communicator.impl.protocol.sip.xcap.model.resourcelists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

public class EntryRefType {
    private List<Element> any;
    private Map<QName, String> anyAttributes = new HashMap();
    private DisplayNameType displayName;
    private String ref;

    EntryRefType() {
    }

    public EntryRefType(String ref) {
        if (ref == null || ref.trim().length() == 0) {
            throw new IllegalArgumentException("The ref attribute cannot be null or empry");
        }
        this.ref = ref;
    }

    public String getRef() {
        return this.ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
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
