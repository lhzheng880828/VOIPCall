package net.java.sip.communicator.impl.protocol.sip.xcap.model.resourcelists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

public class ListType {
    private List<Element> any;
    private Map<QName, String> anyAttributes = new HashMap();
    protected DisplayNameType displayName;
    protected List<EntryType> entries;
    protected List<EntryRefType> entryRefs;
    protected List<ExternalType> externals;
    protected List<ListType> lists;
    protected String name;

    public DisplayNameType getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(DisplayNameType displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<EntryType> getEntries() {
        if (this.entries == null) {
            this.entries = new ArrayList();
        }
        return this.entries;
    }

    public List<ExternalType> getExternals() {
        if (this.externals == null) {
            this.externals = new ArrayList();
        }
        return this.externals;
    }

    public List<ListType> getLists() {
        if (this.lists == null) {
            this.lists = new ArrayList();
        }
        return this.lists;
    }

    public List<EntryRefType> getEntryRefs() {
        if (this.entryRefs == null) {
            this.entryRefs = new ArrayList();
        }
        return this.entryRefs;
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
