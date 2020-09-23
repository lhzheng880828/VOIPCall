package gov.nist.javax.sdp.fields;

import java.util.ListIterator;

public abstract class SDPFieldList extends SDPField {
    protected SDPObjectList sdpFields;

    public ListIterator listIterator() {
        return this.sdpFields.listIterator();
    }

    public SDPFieldList(String fieldName) {
        super(fieldName);
        this.sdpFields = new SDPObjectList(fieldName);
    }

    public SDPFieldList(String fieldName, String classname) {
        super(fieldName);
        this.sdpFields = new SDPObjectList(fieldName, classname);
    }

    public void add(SDPField h) {
        this.sdpFields.add(h);
    }

    public SDPObject first() {
        return (SDPObject) this.sdpFields.first();
    }

    public SDPObject next() {
        return (SDPObject) this.sdpFields.next();
    }

    public String encode() {
        StringBuilder retval = new StringBuilder();
        ListIterator li = this.sdpFields.listIterator();
        while (li.hasNext()) {
            retval.append(((SDPField) li.next()).encode());
        }
        return retval.toString();
    }

    public String debugDump(int indentation) {
        this.stringRepresentation = "";
        String indent = new Indentation(indentation).getIndentation();
        sprint(indent + getClass().getName());
        sprint(indent + "{");
        sprint(indent + this.sdpFields.debugDump(indentation));
        sprint(indent + "}");
        return this.stringRepresentation;
    }

    public String debugDump() {
        return debugDump(0);
    }

    public String toString() {
        return encode();
    }

    public boolean equals(Object other) {
        if (other == null || !getClass().equals(other.getClass())) {
            return false;
        }
        SDPFieldList that = (SDPFieldList) other;
        if (this.sdpFields != null) {
            return this.sdpFields.equals(that.sdpFields);
        }
        if (that.sdpFields == null) {
            return true;
        }
        return false;
    }

    public boolean match(Object template) {
        if (template == null) {
            return true;
        }
        if (!template.getClass().equals(getClass())) {
            return false;
        }
        SDPFieldList other = (SDPFieldList) template;
        if (this.sdpFields == other.sdpFields) {
            return true;
        }
        if (this.sdpFields == null) {
            return false;
        }
        return this.sdpFields.match(other.sdpFields);
    }

    public Object clone() {
        SDPFieldList retval = (SDPFieldList) super.clone();
        if (this.sdpFields != null) {
            retval.sdpFields = (SDPObjectList) this.sdpFields.clone();
        }
        return retval;
    }
}
