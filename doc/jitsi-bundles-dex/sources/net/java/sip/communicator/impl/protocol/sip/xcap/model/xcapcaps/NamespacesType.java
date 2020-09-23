package net.java.sip.communicator.impl.protocol.sip.xcap.model.xcapcaps;

import java.util.ArrayList;
import java.util.List;

public class NamespacesType {
    protected List<String> namespace;

    public List<String> getNamespace() {
        if (this.namespace == null) {
            this.namespace = new ArrayList();
        }
        return this.namespace;
    }
}
