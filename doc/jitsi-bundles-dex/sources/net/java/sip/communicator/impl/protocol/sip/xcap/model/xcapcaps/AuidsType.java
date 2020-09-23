package net.java.sip.communicator.impl.protocol.sip.xcap.model.xcapcaps;

import java.util.ArrayList;
import java.util.List;

public class AuidsType {
    private List<String> auid;

    public List<String> getAuid() {
        if (this.auid == null) {
            this.auid = new ArrayList();
        }
        return this.auid;
    }
}
