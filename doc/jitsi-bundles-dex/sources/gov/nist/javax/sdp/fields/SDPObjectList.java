package gov.nist.javax.sdp.fields;

import java.util.Iterator;
import org.jitsi.gov.nist.core.GenericObject;
import org.jitsi.gov.nist.core.GenericObjectList;

public class SDPObjectList extends GenericObjectList {
    protected static final String SDPFIELDS_PACKAGE = "gov.nist.javax.sdp.fields";

    public void mergeObjects(GenericObjectList mergeList) {
        Iterator<GenericObject> it1 = listIterator();
        Iterator<GenericObject> it2 = mergeList.listIterator();
        while (it1.hasNext()) {
            GenericObject outerObj = (GenericObject) it1.next();
            while (it2.hasNext()) {
                outerObj.merge(it2.next());
            }
        }
    }

    public void add(SDPObject s) {
        super.add(s);
    }

    public SDPObjectList(String lname, String classname) {
        super(lname, classname);
    }

    public SDPObjectList() {
        super(null, SDPObject.class);
    }

    public SDPObjectList(String lname) {
        super(lname, "gov.nist.javax.sdp.fields.SDPObject");
    }

    public GenericObject first() {
        return (SDPObject) super.first();
    }

    public GenericObject next() {
        return (SDPObject) super.next();
    }

    public String encode() {
        StringBuilder retval = new StringBuilder();
        SDPObject sdpObject = (SDPObject) first();
        while (sdpObject != null) {
            retval.append(sdpObject.encode());
            sdpObject = (SDPObject) next();
        }
        return retval.toString();
    }

    public String toString() {
        return encode();
    }
}
