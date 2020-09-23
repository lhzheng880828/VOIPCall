package net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

public class ManyType {
    private List<Element> any;
    private String domain;
    private List<ExceptType> excepts;

    public String getDomain() {
        return this.domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<ExceptType> getExcepts() {
        if (this.excepts == null) {
            this.excepts = new ArrayList();
        }
        return this.excepts;
    }

    public List<Element> getAny() {
        if (this.any == null) {
            this.any = new ArrayList();
        }
        return this.any;
    }
}
