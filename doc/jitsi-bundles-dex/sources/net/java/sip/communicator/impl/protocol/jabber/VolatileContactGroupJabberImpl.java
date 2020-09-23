package net.java.sip.communicator.impl.protocol.jabber;

import java.util.Iterator;
import java.util.Vector;
import net.java.sip.communicator.service.protocol.Contact;

public class VolatileContactGroupJabberImpl extends ContactGroupJabberImpl {
    private final String contactGroupName;

    VolatileContactGroupJabberImpl(String groupName, ServerStoredContactListJabberImpl ssclCallback) {
        super(null, new Vector().iterator(), ssclCallback, false);
        this.contactGroupName = groupName;
    }

    public String getGroupName() {
        return this.contactGroupName;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer("VolatileJabberGroup.");
        buff.append(getGroupName());
        buff.append(", childContacts=" + countContacts() + ":[");
        Iterator<Contact> contacts = contacts();
        while (contacts.hasNext()) {
            buff.append(((Contact) contacts.next()).toString());
            if (contacts.hasNext()) {
                buff.append(", ");
            }
        }
        return buff.append("]").toString();
    }

    public boolean isPersistent() {
        return false;
    }
}
