package net.java.sip.communicator.impl.protocol.sip.xcap.model.xcapcaps;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

public class XCapCapsType {
    protected List<Element> any;
    protected AuidsType auids;
    protected ExtensionsType extensions;
    protected NamespacesType namespaces;

    public AuidsType getAuids() {
        return this.auids;
    }

    public void setAuids(AuidsType auids) {
        this.auids = auids;
    }

    public ExtensionsType getExtensions() {
        return this.extensions;
    }

    public void setExtensions(ExtensionsType extensions) {
        this.extensions = extensions;
    }

    public NamespacesType getNamespaces() {
        return this.namespaces;
    }

    public void setNamespaces(NamespacesType namespaces) {
        this.namespaces = namespaces;
    }

    public List<Element> getAny() {
        if (this.any == null) {
            this.any = new ArrayList();
        }
        return this.any;
    }
}
