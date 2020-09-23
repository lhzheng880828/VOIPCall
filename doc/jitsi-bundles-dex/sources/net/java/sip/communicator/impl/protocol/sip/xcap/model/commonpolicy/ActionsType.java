package net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy;

import java.util.ArrayList;
import java.util.List;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules.SubHandlingType;
import org.w3c.dom.Element;

public class ActionsType {
    private List<Element> any;
    private SubHandlingType subHandling;

    public SubHandlingType getSubHandling() {
        return this.subHandling;
    }

    public void setSubHandling(SubHandlingType subHandling) {
        this.subHandling = subHandling;
    }

    public List<Element> getAny() {
        if (this.any == null) {
            this.any = new ArrayList();
        }
        return this.any;
    }
}
