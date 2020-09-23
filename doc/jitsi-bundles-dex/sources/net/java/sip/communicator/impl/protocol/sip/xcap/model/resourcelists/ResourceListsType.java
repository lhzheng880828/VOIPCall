package net.java.sip.communicator.impl.protocol.sip.xcap.model.resourcelists;

import java.util.ArrayList;
import java.util.List;

public class ResourceListsType {
    private List<ListType> list;

    public List<ListType> getList() {
        if (this.list == null) {
            this.list = new ArrayList();
        }
        return this.list;
    }
}
