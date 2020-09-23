package net.java.sip.communicator.impl.protocol.sip;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.xml.namespace.QName;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.resourcelists.ListType;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ContactGroup;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import org.jitsi.gov.nist.core.Separators;
import org.w3c.dom.Element;

public class ContactGroupSipImpl implements ContactGroup {
    private static final String UID_SUFFIX = ".uid";
    private Vector<Contact> contacts = new Vector();
    private boolean isPersistent = true;
    private boolean isResolved = true;
    private final ListType list = new ListType();
    private ContactGroupSipImpl parentGroup = null;
    private ProtocolProviderServiceSipImpl parentProvider = null;
    private Vector<ContactGroup> subGroups = new Vector();
    private String uid = null;

    public ContactGroupSipImpl(String groupName, ProtocolProviderServiceSipImpl parentProvider) {
        this.list.setName(groupName);
        this.uid = this.list.getName() + UID_SUFFIX;
        this.parentProvider = parentProvider;
    }

    /* access modifiers changed from: 0000 */
    public ListType getList() {
        return this.list;
    }

    /* access modifiers changed from: 0000 */
    public void setOtherAttributes(Map<QName, String> otherAttributes) {
        this.list.setAnyAttributes(otherAttributes);
    }

    /* access modifiers changed from: 0000 */
    public void setAny(List<Element> any) {
        this.list.setAny(any);
    }

    /* access modifiers changed from: 0000 */
    public void setName(String newName) {
        this.list.setName(newName);
    }

    public boolean canContainSubgroups() {
        return true;
    }

    public ProtocolProviderService getProtocolProvider() {
        return this.parentProvider;
    }

    public Iterator<Contact> contacts() {
        return this.contacts.iterator();
    }

    public void addContact(ContactSipImpl contactToAdd) {
        this.contacts.add(contactToAdd);
        contactToAdd.setParentGroup(this);
        if (contactToAdd.isPersistent()) {
            this.list.getEntries().add(contactToAdd.getEntry());
        }
    }

    public int countContacts() {
        return this.contacts.size();
    }

    public int countSubgroups() {
        return this.subGroups.size();
    }

    /* access modifiers changed from: 0000 */
    public void setParentGroup(ContactGroupSipImpl parent) {
        this.parentGroup = parent;
    }

    public ContactGroup getParentContactGroup() {
        return this.parentGroup;
    }

    public void addSubgroup(ContactGroupSipImpl subgroup) {
        this.subGroups.add(subgroup);
        subgroup.setParentGroup(this);
        if (subgroup.isPersistent()) {
            this.list.getLists().add(subgroup.getList());
        }
    }

    public void removeSubGroup(ContactGroupSipImpl subgroup) {
        this.subGroups.remove(subgroup);
        subgroup.setParentGroup(null);
        if (subgroup.isPersistent()) {
            this.list.getLists().remove(subgroup.getList());
        }
    }

    public ContactGroupSipImpl findGroupParent(ContactGroupSipImpl sipGroup) {
        if (this.subGroups.contains(sipGroup)) {
            return this;
        }
        Iterator<ContactGroup> subGroupsIter = subgroups();
        while (subGroupsIter.hasNext()) {
            ContactGroupSipImpl parent = ((ContactGroupSipImpl) subGroupsIter.next()).findGroupParent(sipGroup);
            if (parent != null) {
                return parent;
            }
        }
        return null;
    }

    public ContactGroupSipImpl findContactParent(ContactSipImpl sipContact) {
        if (this.contacts.contains(sipContact)) {
            return this;
        }
        Iterator<ContactGroup> subGroupsIter = subgroups();
        while (subGroupsIter.hasNext()) {
            ContactGroupSipImpl parent = ((ContactGroupSipImpl) subGroupsIter.next()).findContactParent(sipContact);
            if (parent != null) {
                return parent;
            }
        }
        return null;
    }

    public Contact getContact(String id) {
        Iterator<Contact> contactsIter = contacts();
        while (contactsIter.hasNext()) {
            ContactSipImpl contact = (ContactSipImpl) contactsIter.next();
            if (contact.getUri().equals(id)) {
                return contact;
            }
            if (contact.getAddress().equals(id)) {
                return contact;
            }
        }
        return null;
    }

    public ContactGroup getGroup(int index) {
        return (ContactGroup) this.subGroups.get(index);
    }

    public ContactGroup getGroup(String groupName) {
        Iterator<ContactGroup> groupsIter = subgroups();
        while (groupsIter.hasNext()) {
            ContactGroupSipImpl contactGroup = (ContactGroupSipImpl) groupsIter.next();
            if (contactGroup.getGroupName().equals(groupName)) {
                return contactGroup;
            }
        }
        return null;
    }

    public String getGroupName() {
        return this.list.getName();
    }

    public Iterator<ContactGroup> subgroups() {
        return this.subGroups.iterator();
    }

    public void removeContact(ContactSipImpl contact) {
        this.contacts.remove(contact);
        if (contact.isPersistent()) {
            this.list.getEntries().remove(contact.getEntry());
        }
    }

    public ContactSipImpl findContactByID(String id) {
        ContactSipImpl mContact;
        Iterator<Contact> contactsIter = contacts();
        while (contactsIter.hasNext()) {
            mContact = (ContactSipImpl) contactsIter.next();
            if (mContact.getAddress().equals(id)) {
                return mContact;
            }
        }
        Iterator<ContactGroup> groupsIter = subgroups();
        while (groupsIter.hasNext()) {
            mContact = ((ContactGroupSipImpl) groupsIter.next()).findContactByID(id);
            if (mContact != null) {
                return mContact;
            }
        }
        return null;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer(getGroupName());
        buff.append(".subGroups=" + countSubgroups() + ":\n");
        Iterator<ContactGroup> subGroups = subgroups();
        while (subGroups.hasNext()) {
            buff.append(((ContactGroupSipImpl) subGroups.next()).toString());
            if (subGroups.hasNext()) {
                buff.append(Separators.RETURN);
            }
        }
        buff.append("\nChildContacts=" + countContacts() + ":[");
        Iterator<Contact> contacts = contacts();
        while (contacts.hasNext()) {
            buff.append(((ContactSipImpl) contacts.next()).toString());
            if (contacts.hasNext()) {
                buff.append(", ");
            }
        }
        return buff.append("]").toString();
    }

    public void setPersistent(boolean isPersistent) {
        this.isPersistent = isPersistent;
    }

    public boolean isPersistent() {
        return this.isPersistent;
    }

    public String getPersistentData() {
        return null;
    }

    public boolean isResolved() {
        return this.isResolved;
    }

    public void setResolved(boolean resolved) {
        this.isResolved = resolved;
    }

    public String getUID() {
        return this.uid;
    }

    static String createNameFromUID(String uid) {
        return uid.substring(0, uid.length() - UID_SUFFIX.length());
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ContactGroupSipImpl)) {
            return false;
        }
        ContactGroupSipImpl sipGroup = (ContactGroupSipImpl) obj;
        if (!sipGroup.getGroupName().equals(getGroupName()) || !sipGroup.getUID().equals(getUID()) || sipGroup.countContacts() != countContacts() || sipGroup.countSubgroups() != countSubgroups() || getProtocolProvider() != ((ContactGroup) obj).getProtocolProvider()) {
            return false;
        }
        Iterator<Contact> theirContacts = sipGroup.contacts();
        while (theirContacts.hasNext()) {
            ContactSipImpl theirContact = (ContactSipImpl) theirContacts.next();
            ContactSipImpl ourContact = (ContactSipImpl) getContact(theirContact.getAddress());
            if (ourContact == null) {
                return false;
            }
            if (!ourContact.equals(theirContact)) {
                return false;
            }
        }
        Iterator<ContactGroup> theirSubgroups = sipGroup.subgroups();
        while (theirSubgroups.hasNext()) {
            ContactGroupSipImpl theirSubgroup = (ContactGroupSipImpl) theirSubgroups.next();
            ContactGroupSipImpl ourSubgroup = (ContactGroupSipImpl) getGroup(theirSubgroup.getGroupName());
            if (ourSubgroup == null) {
                return false;
            }
            if (!ourSubgroup.equals(theirSubgroup)) {
                return false;
            }
        }
        return true;
    }
}
