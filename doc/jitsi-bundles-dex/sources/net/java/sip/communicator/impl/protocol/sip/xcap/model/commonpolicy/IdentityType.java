package net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

public class IdentityType {
    private List<Element> any;
    private List<ManyType> manyList;
    private List<OneType> oneList;

    public List<OneType> getOneList() {
        if (this.oneList == null) {
            this.oneList = new ArrayList();
        }
        return this.oneList;
    }

    public List<ManyType> getManyList() {
        if (this.manyList == null) {
            this.manyList = new ArrayList();
        }
        return this.manyList;
    }

    public List<Element> getAny() {
        if (this.any == null) {
            this.any = new ArrayList();
        }
        return this.any;
    }
}
