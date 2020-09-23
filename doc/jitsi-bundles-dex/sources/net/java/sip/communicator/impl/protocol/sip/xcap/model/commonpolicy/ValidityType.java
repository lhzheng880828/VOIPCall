package net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy;

import java.util.ArrayList;
import java.util.List;

public class ValidityType {
    private List<String> fromList;
    private List<String> untilList;

    public List<String> getFromList() {
        if (this.fromList == null) {
            this.fromList = new ArrayList();
        }
        return this.fromList;
    }

    public List<String> getUntilList() {
        if (this.untilList == null) {
            this.untilList = new ArrayList();
        }
        return this.untilList;
    }
}
