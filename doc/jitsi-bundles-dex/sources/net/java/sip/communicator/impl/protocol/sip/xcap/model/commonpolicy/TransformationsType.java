package net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy;

import java.util.ArrayList;
import java.util.List;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules.ProvideDevicePermissionType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules.ProvidePersonPermissionType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules.ProvideServicePermissionType;
import org.w3c.dom.Element;

public class TransformationsType {
    private List<Element> any;
    private ProvideDevicePermissionType devicePermission;
    private ProvidePersonPermissionType personPermission;
    private ProvideServicePermissionType servicePermission;

    public ProvideServicePermissionType getServicePermission() {
        return this.servicePermission;
    }

    public void setServicePermission(ProvideServicePermissionType servicePermission) {
        this.servicePermission = servicePermission;
    }

    public ProvidePersonPermissionType getPersonPermission() {
        return this.personPermission;
    }

    public void setPersonPermission(ProvidePersonPermissionType personPermission) {
        this.personPermission = personPermission;
    }

    public ProvideDevicePermissionType getDevicePermission() {
        return this.devicePermission;
    }

    public void setDevicePermission(ProvideDevicePermissionType devicePermission) {
        this.devicePermission = devicePermission;
    }

    public List<Element> getAny() {
        if (this.any == null) {
            this.any = new ArrayList();
        }
        return this.any;
    }
}
