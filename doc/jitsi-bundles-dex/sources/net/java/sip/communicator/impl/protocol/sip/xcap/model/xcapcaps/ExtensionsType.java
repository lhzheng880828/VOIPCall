package net.java.sip.communicator.impl.protocol.sip.xcap.model.xcapcaps;

import java.util.ArrayList;
import java.util.List;

public class ExtensionsType {
    private List<String> extension;

    public List<String> getExtension() {
        if (this.extension == null) {
            this.extension = new ArrayList();
        }
        return this.extension;
    }
}
