package net.java.sip.communicator.impl.protocol.jabber;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ContactGroup;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import org.jitsi.gov.nist.core.Separators;

public class RootContactGroupJabberImpl extends AbstractContactGroupJabberImpl {
    private String ROOT_CONTACT_GROUP_NAME = "ContactListRoot";
    private Map<String, Contact> contacts = new Hashtable();
    private boolean isResolved = false;
    private final ProtocolProviderServiceJabberImpl protocolProvider;
    private List<ContactGroup> subGroups = new LinkedList();

    RootContactGroupJabberImpl(ProtocolProviderServiceJabberImpl protocolProvider) {
        this.protocolProvider = protocolProvider;
    }

    public boolean canContainSubgroups() {
        return true;
    }

    public String getGroupName() {
        return this.ROOT_CONTACT_GROUP_NAME;
    }

    /* access modifiers changed from: 0000 */
    public void removeContact(ContactJabberImpl contact) {
        this.contacts.remove(contact.getAddress().toLowerCase());
    }

    /* access modifiers changed from: 0000 */
    public void addContact(ContactJabberImpl contact) {
        this.contacts.put(contact.getAddress(), contact);
    }

    /* access modifiers changed from: 0000 */
    public void addSubGroup(ContactGroupJabberImpl group) {
        this.subGroups.add(group);
    }

    /* access modifiers changed from: 0000 */
    public void removeSubGroup(ContactGroupJabberImpl group) {
        removeSubGroup(this.subGroups.indexOf(group));
    }

    /* access modifiers changed from: 0000 */
    public void removeSubGroup(int index) {
        this.subGroups.remove(index);
    }

    public int countSubgroups() {
        return this.subGroups.size();
    }

    public ContactGroup getParentContactGroup() {
        return null;
    }

    public ContactGroup getGroup(int index) {
        return (ContactGroup) this.subGroups.get(index);
    }

    public ContactGroup getGroup(String groupName) {
        Iterator<ContactGroup> subgroups = subgroups();
        while (subgroups.hasNext()) {
            ContactGroup grp = (ContactGroup) subgroups.next();
            if (grp.getGroupName().equals(groupName)) {
                return grp;
            }
        }
        return null;
    }

    public Contact getContact(String id) {
        return findContact(id);
    }

    /* access modifiers changed from: 0000 */
    public ContactJabberImpl findContact(String id) {
        if (id == null) {
            return null;
        }
        return (ContactJabberImpl) this.contacts.get(id.toLowerCase());
    }

    public Iterator<ContactGroup> subgroups() {
        return this.subGroups.iterator();
    }

    public int countContacts() {
        return this.contacts.size();
    }

    public Iterator<Contact> contacts() {
        return this.contacts.values().iterator();
    }

    public String toString() {
        StringBuffer buff = new StringBuffer(getGroupName());
        buff.append(".subGroups=" + countSubgroups() + ":\n");
        Iterator<ContactGroup> subGroups = subgroups();
        while (subGroups.hasNext()) {
            buff.append(((ContactGroup) subGroups.next()).toString());
            if (subGroups.hasNext()) {
                buff.append(Separators.RETURN);
            }
        }
        buff.append(".rootContacts=" + countContacts() + ":\n");
        Iterator<Contact> contactsIter = contacts();
        while (contactsIter.hasNext()) {
            buff.append(contactsIter.next());
            if (contactsIter.hasNext()) {
                buff.append(Separators.RETURN);
            }
        }
        return buff.toString();
    }

    public ProtocolProviderService getProtocolProvider() {
        return this.protocolProvider;
    }

    public boolean isPersistent() {
        return true;
    }

    public String getPersistentData() {
        return null;
    }

    public boolean isResolved() {
        return this.isResolved;
    }

    public String getUID() {
        return getGroupName();
    }
}
