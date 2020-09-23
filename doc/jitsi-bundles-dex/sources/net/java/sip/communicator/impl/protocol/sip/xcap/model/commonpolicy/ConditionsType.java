package net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

public class ConditionsType {
    private List<Element> any;
    private List<IdentityType> identities;
    private List<SphereType> spheres;
    private List<ValidityType> validities;

    public List<IdentityType> getIdentities() {
        if (this.identities == null) {
            this.identities = new ArrayList();
        }
        return this.identities;
    }

    public List<SphereType> getSpheres() {
        if (this.spheres == null) {
            this.spheres = new ArrayList();
        }
        return this.spheres;
    }

    public List<ValidityType> getValidities() {
        if (this.validities == null) {
            this.validities = new ArrayList();
        }
        return this.validities;
    }

    public List<Element> getAny() {
        if (this.any == null) {
            this.any = new ArrayList();
        }
        return this.any;
    }
}
