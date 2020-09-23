package net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

public class ProvidePersonPermissionType {
    private AllPersonsType allPersons;
    private List<Element> any;
    private List<ClassType> classes;
    private List<OccurrenceIdType> occurrences;

    public static class AllPersonsType {
    }

    public AllPersonsType getAllPersons() {
        return this.allPersons;
    }

    public void setAllPersons(AllPersonsType allPersons) {
        this.allPersons = allPersons;
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
