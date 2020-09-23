package net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

public class ProvideServicePermissionType {
    private AllServicesType allServices;
    private List<Element> any;
    private List<ClassType> classes;
    private List<OccurrenceIdType> occurrences;
    private List<ServiceUriType> serviceUriList;
    private List<ServiceUriSchemeType> serviceUriSchemeList;

    public static class AllServicesType {
    }

    public static class ServiceUriSchemeType {
        private String value;

        public ServiceUriSchemeType(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class ServiceUriType {
        private String value;

        public ServiceUriType(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public void setAllServices(AllServicesType allServices) {
        this.allServices = allServices;
    }

    public AllServicesType getAllServices() {
        return this.allServices;
    }

    public List<ServiceUriType> getServiceUriList() {
        if (this.serviceUriList == null) {
            this.serviceUriList = new ArrayList();
        }
        return this.serviceUriList;
    }

    public List<ServiceUriSchemeType> getServiceUriSchemeList() {
        if (this.serviceUriSchemeList == null) {
            this.serviceUriSchemeList = new ArrayList();
        }
        return this.serviceUriSchemeList;
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
