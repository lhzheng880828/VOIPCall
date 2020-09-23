package net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

public class ProvideDevicePermissionType {
    private AllDevicesType allDevices;
    private List<Element> any;
    private List<ClassType> classes;
    private List<DeviceIdType> devices;
    private List<OccurrenceIdType> occurrences;

    public static class AllDevicesType {
    }

    public static class DeviceIdType {
        private String value;

        public DeviceIdType(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public void setAllDevices(AllDevicesType allDevices) {
        this.allDevices = allDevices;
    }

    public AllDevicesType getAllDevices() {
        return this.allDevices;
    }

    public List<DeviceIdType> getDevices() {
        if (this.devices == null) {
            this.devices = new ArrayList();
        }
        return this.devices;
    }

    public List<OccurrenceIdType> getOccurrences() {
        if (this.occurrences == null) {
            this.occurrences = new ArrayList();
        }
        return this.occurrences;
    }

    public List<ClassType> getClasses() {
        if (this.classes == null) {
            this.classes = new ArrayList();
        }
        return this.classes;
    }

    public List<Element> getAny() {
        if (this.any == null) {
            this.any = new ArrayList();
        }
        return this.any;
    }
}
