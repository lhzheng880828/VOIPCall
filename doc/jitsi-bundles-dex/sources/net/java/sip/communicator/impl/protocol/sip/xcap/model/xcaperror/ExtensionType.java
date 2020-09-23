package net.java.sip.communicator.impl.protocol.sip.xcap.model.xcaperror;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

public class ExtensionType extends BaseXCapError {
    private List<Element> any;

    public ExtensionType() {
        super(null);
    }

    public List<Element> getAny() {
        if (this.any == null) {
            this.any = new ArrayList();
        }
        return this.any;
    }
}
