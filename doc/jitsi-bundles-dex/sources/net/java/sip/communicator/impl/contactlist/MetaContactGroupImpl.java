package net.java.sip.communicator.impl.contactlist;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import net.java.sip.communicator.service.contactlist.MetaContact;
import net.java.sip.communicator.service.contactlist.MetaContactGroup;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ContactGroup;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;

public class MetaContactGroupImpl implements MetaContactGroup {
    private static final Logger logger = Logger.getLogger(MetaContactGroupImpl.class);
    private final Set<MetaContactImpl> childContacts;
    private List<MetaContact> childContactsOrderedCopy;
    private Object[] data;
    private String groupName;
    private String groupUID;
    private final MetaContactListServiceImpl mclServiceImpl;
    private MetaContactGroupImpl parentMetaContactGroup;
    private Vector<ContactGroup> protoGroups;
    private Set<MetaContactGroupImpl> subgroups;
    private List<MetaContactGroup> subgroupsOrderedCopy;

    MetaContactGroupImpl(MetaContactListServiceImpl mclServiceImpl, String groupName) {
        this(mclServiceImpl, groupName, null);
    }

    MetaContactGroupImpl(MetaContactListServiceImpl mclServiceImpl, String groupName, String metaUID) {
        this.subgroups = new TreeSet();
        this.childContacts = new TreeSet();
        this.protoGroups = new Vector();
        this.groupUID = null;
        this.groupName = null;
        this.childContactsOrderedCopy = new LinkedList();
        this.subgroupsOrderedCopy = new LinkedList();
        this.parentMetaContactGroup = null;
        this.mclServiceImpl = mclServiceImpl;
        this.groupName = groupName;
        if (metaUID == null) {
            metaUID = String.valueOf(System.currentTimeMillis()) + String.valueOf(hashCode());
        }
        this.groupUID = metaUID;
    }

    public String getMetaUID() {
        return this.groupUID;
    }

    public MetaContactGroup getParentMetaContactGroup() {
        return this.parentMetaContactGroup;
    }

    public boolean canContainSubgroups() {
        return false;
    }

    public int countChildContacts() {
        return this.childContacts.size();
    }

    public int countOnlineChildContacts() {
        int onlineContactsNumber = 0;
        try {
            Iterator<MetaContact> itr = getChildContacts();
            while (itr.hasNext()) {
                Contact contact = ((MetaContact) itr.next()).getDefaultContact();
                if (contact != null && contact.getPresenceStatus().isOnline()) {
                    onlineContactsNumber++;
                }
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to count online contacts.", e);
            }
        }
        return onlineContactsNumber;
    }

    public int countContactGroups() {
        return this.protoGroups.size();
    }

    public int countSubgroups() {
        return this.subgroups.size();
    }

    public Iterator<MetaContact> getChildContacts() {
        return this.childContactsOrderedCopy.iterator();
    }

    public MetaContact getMetaContact(String metaContactID) {
        Iterator<MetaContact> contactsIter = getChildContacts();
        while (contactsIter.hasNext()) {
            MetaContact contact = (MetaContact) contactsIter.next();
            if (contact.getMetaUID().equals(metaContactID)) {
                return contact;
            }
        }
        return null;
    }

    public int indexOf(MetaContact metaContact) {
        int i = 0;
        Iterator<MetaContact> childrenIter = getChildContacts();
        while (childrenIter.hasNext()) {
            if (((MetaContact) childrenIter.next()) == metaContact) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public int indexOf(MetaContactGroup metaContactGroup) {
        int i = 0;
        Iterator<MetaContactGroup> childrenIter = getSubgroups();
        while (childrenIter.hasNext()) {
            if (((MetaContactGroup) childrenIter.next()) == metaContactGroup) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public MetaContact getMetaContact(ProtocolProviderService provider, String contactID) {
        Iterator<MetaContact> contactsIter = getChildContacts();
        while (contactsIter.hasNext()) {
            MetaContact contact = (MetaContact) contactsIter.next();
            if (contact.getContact(contactID, provider) != null) {
                return contact;
            }
        }
        return null;
    }

    public MetaContact findMetaContactByMetaUID(String metaUID) {
        MetaContact mContact;
        Iterator<MetaContact> contactsIter = getChildContacts();
        while (contactsIter.hasNext()) {
            mContact = (MetaContact) contactsIter.next();
            if (mContact.getMetaUID().equals(metaUID)) {
                return mContact;
            }
        }
        Iterator<MetaContactGroup> groupsIter = getSubgroups();
        while (groupsIter.hasNext()) {
            mContact = ((MetaContactGroupImpl) groupsIter.next()).findMetaContactByMetaUID(metaUID);
            if (mContact != null) {
                return mContact;
            }
        }
        return null;
    }

    public MetaContactGroup findMetaContactGroupByMetaUID(String metaUID) {
        if (metaUID.equals(this.groupUID)) {
            return this;
        }
        Iterator<MetaContactGroup> groupsIter = getSubgroups();
        while (groupsIter.hasNext()) {
            MetaContactGroupImpl mGroup = (MetaContactGroupImpl) groupsIter.next();
            if (metaUID.equals(mGroup.getMetaUID())) {
                return mGroup;
            }
            mGroup.findMetaContactByMetaUID(metaUID);
        }
        return null;
    }

    public Iterator<ContactGroup> getContactGroups() {
        return new LinkedList(this.protoGroups).iterator();
    }

    public ContactGroup getContactGroup(String grpName, ProtocolProviderService ownerProvider) {
        Iterator<ContactGroup> encapsulatedGroups = getContactGroups();
        while (encapsulatedGroups.hasNext()) {
            ContactGroup group = (ContactGroup) encapsulatedGroups.next();
            if (group.getGroupName().equals(grpName) && group.getProtocolProvider() == ownerProvider) {
                return group;
            }
        }
        return null;
    }

    public Iterator<ContactGroup> getContactGroupsForProvider(ProtocolProviderService provider) {
        Iterator<ContactGroup> encapsulatedGroups = getContactGroups();
        LinkedList<ContactGroup> protGroups = new LinkedList();
        while (encapsulatedGroups.hasNext()) {
            ContactGroup group = (ContactGroup) encapsulatedGroups.next();
            if (group.getProtocolProvider() == provider) {
                protGroups.add(group);
            }
        }
        return protGroups.iterator();
    }

    public Iterator<ContactGroup> getContactGroupsForAccountID(String accountID) {
        Iterator<ContactGroup> encapsulatedGroups = getContactGroups();
        LinkedList<ContactGroup> protGroups = new LinkedList();
        while (encapsulatedGroups.hasNext()) {
            ContactGroup group = (ContactGroup) encapsulatedGroups.next();
            if (group.getProtocolProvider().getAccountID().getAccountUniqueID().equals(accountID)) {
                protGroups.add(group);
            }
        }
        return protGroups.iterator();
    }

    public MetaContact findMetaContactByContact(Contact protoContact) {
        MetaContact mContact;
        Iterator<MetaContact> contactsIter = getChildContacts();
        while (contactsIter.hasNext()) {
            mContact = (MetaContact) contactsIter.next();
            if (mContact.getContact(protoContact.getAddress(), protoContact.getProtocolProvider()) != null) {
                return mContact;
            }
        }
        Iterator<MetaContactGroup> groupsIter = getSubgroups();
        while (groupsIter.hasNext()) {
            mContact = ((MetaContactGroupImpl) groupsIter.next()).findMetaContactByContact(protoContact);
            if (mContact != null) {
                return mContact;
            }
        }
        return null;
    }

    public MetaContact findMetaContactByContact(String contactAddress, String accountID) {
        Iterator<MetaContact> contactsIter = getChildContacts();
        while (contactsIter.hasNext()) {
            MetaContactImpl mContact = (MetaContactImpl) contactsIter.next();
            if (mContact.getContact(contactAddress, accountID) != null) {
                return mContact;
            }
        }
        Iterator<MetaContactGroup> groupsIter = getSubgroups();
        while (groupsIter.hasNext()) {
            MetaContact mContact2 = ((MetaContactGroupImpl) groupsIter.next()).findMetaContactByContact(contactAddress, accountID);
            if (mContact2 != null) {
                return mContact2;
            }
        }
        return null;
    }

    public MetaContactGroupImpl findMetaContactGroupByContactGroup(ContactGroup protoContactGroup) {
        if (this.protoGroups.contains(protoContactGroup)) {
            return this;
        }
        Iterator<MetaContactGroup> groupsIter = getSubgroups();
        while (groupsIter.hasNext()) {
            MetaContactGroupImpl foundMetaContactGroup = ((MetaContactGroupImpl) groupsIter.next()).findMetaContactGroupByContactGroup(protoContactGroup);
            if (foundMetaContactGroup != null) {
                return foundMetaContactGroup;
            }
        }
        return null;
    }

    public MetaContact getMetaContact(int index) throws IndexOutOfBoundsException {
        return (MetaContact) this.childContactsOrderedCopy.get(index);
    }

    /* access modifiers changed from: 0000 */
    public void addMetaContact(MetaContactImpl metaContact) {
        metaContact.setParentGroup(this);
        lightAddMetaContact(metaContact);
    }

    /* access modifiers changed from: 0000 */
    public int lightAddMetaContact(MetaContactImpl metaContact) {
        int indexOf;
        synchronized (this.childContacts) {
            this.childContacts.add(metaContact);
            this.childContactsOrderedCopy = new LinkedList(this.childContacts);
            indexOf = this.childContactsOrderedCopy.indexOf(metaContact);
        }
        return indexOf;
    }

    /* access modifiers changed from: 0000 */
    public void lightRemoveMetaContact(MetaContactImpl metaContact) {
        synchronized (this.childContacts) {
            this.childContacts.remove(metaContact);
            this.childContactsOrderedCopy = new LinkedList(this.childContacts);
        }
    }

    /* access modifiers changed from: 0000 */
    public void removeMetaContact(MetaContactImpl metaContact) {
        metaContact.unsetParentGroup(this);
        lightRemoveMetaContact(metaContact);
    }

    public MetaContactGroup getMetaContactSubgroup(int index) throws IndexOutOfBoundsException {
        return (MetaContactGroup) this.subgroupsOrderedCopy.get(index);
    }

    public MetaContactGroup getMetaContactSubgroup(String grpName) {
        Iterator<MetaContactGroup> groupsIter = getSubgroups();
        while (groupsIter.hasNext()) {
            MetaContactGroup mcGroup = (MetaContactGroup) groupsIter.next();
            if (mcGroup.getGroupName().equals(grpName)) {
                return mcGroup;
            }
        }
        return null;
    }

    public MetaContactGroup getMetaContactSubgroupByUID(String grpUID) {
        Iterator<MetaContactGroup> groupsIter = getSubgroups();
        while (groupsIter.hasNext()) {
            MetaContactGroup mcGroup = (MetaContactGroup) groupsIter.next();
            if (mcGroup.getMetaUID().equals(grpUID)) {
                return mcGroup;
            }
        }
        return null;
    }

    public boolean contains(MetaContact contact) {
        boolean contains;
        synchronized (this.childContacts) {
            contains = this.childContacts.contains(contact);
        }
        return contains;
    }

    public boolean contains(MetaContactGroup group) {
        return this.subgroups.contains(group);
    }

    public Iterator<MetaContactGroup> getSubgroups() {
        return this.subgroupsOrderedCopy.iterator();
    }

    public String getGroupName() {
        return this.groupName;
    }

    /* access modifiers changed from: 0000 */
    public void setGroupName(String newGroupName) {
        this.groupName = newGroupName;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer(getGroupName());
        buff.append(".subGroups=" + countSubgroups() + ":\n");
        Iterator<MetaContactGroup> subGroups = getSubgroups();
        while (subGroups.hasNext()) {
            buff.append(((MetaContactGroup) subGroups.next()).getGroupName());
            if (subGroups.hasNext()) {
                buff.append(Separators.RETURN);
            }
        }
        buff.append("\nProtoGroups=" + countContactGroups() + ":[");
        Iterator<ContactGroup> contactGroups = getContactGroups();
        while (contactGroups.hasNext()) {
            ContactGroup contactGroup = (ContactGroup) contactGroups.next();
            buff.append(contactGroup.getProtocolProvider());
            buff.append(Separators.DOT);
            buff.append(contactGroup.getGroupName());
            if (contactGroups.hasNext()) {
                buff.append(", ");
            }
        }
        buff.append("]");
        buff.append("\nRootChildContacts=" + countChildContacts() + ":[");
        Iterator<MetaContact> contacts = getChildContacts();
        while (contacts.hasNext()) {
            buff.append(((MetaContact) contacts.next()).toString());
            if (contacts.hasNext()) {
                buff.append(", ");
            }
        }
        return buff.append("]").toString();
    }

    /* access modifiers changed from: 0000 */
    public void addProtoGroup(ContactGroup protoGroup) {
        this.protoGroups.add(protoGroup);
    }

    /* access modifiers changed from: 0000 */
    public void removeProtoGroup(ContactGroup protoGroup) {
        this.protoGroups.remove(protoGroup);
    }

    /* access modifiers changed from: 0000 */
    public void addSubgroup(MetaContactGroup subgroup) {
        if (logger.isTraceEnabled()) {
            logger.trace("Adding subgroup " + subgroup.getGroupName() + " to" + getGroupName());
        }
        this.subgroups.add((MetaContactGroupImpl) subgroup);
        ((MetaContactGroupImpl) subgroup).parentMetaContactGroup = this;
        this.subgroupsOrderedCopy = new LinkedList(this.subgroups);
    }

    /* access modifiers changed from: 0000 */
    public MetaContactGroupImpl removeSubgroup(int index) {
        MetaContactGroupImpl subgroup = (MetaContactGroupImpl) this.subgroupsOrderedCopy.get(index);
        if (this.subgroups.remove(subgroup)) {
            subgroup.parentMetaContactGroup = null;
        }
        this.subgroupsOrderedCopy = new LinkedList(this.subgroups);
        return subgroup;
    }

    /* access modifiers changed from: 0000 */
    public boolean removeSubgroup(MetaContactGroup group) {
        if (!this.subgroups.contains(group)) {
            return false;
        }
        removeSubgroup(this.subgroupsOrderedCopy.indexOf(group));
        return true;
    }

    /* access modifiers changed from: final */
    public final MetaContactListServiceImpl getMclServiceImpl() {
        return this.mclServiceImpl;
    }

    public Object getData(Object key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        int index = dataIndexOf(key);
        return index == -1 ? null : this.data[index + 1];
    }

    public void setData(Object key, Object value) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        int index = dataIndexOf(key);
        int length;
        Object[] newData;
        if (index != -1) {
            this.data[index + 1] = value;
        } else if (this.data == null) {
            if (value != null) {
                this.data = new Object[]{key, value};
            }
        } else if (value == null) {
            length = this.data.length - 2;
            if (length > 0) {
                newData = new Object[length];
                System.arraycopy(this.data, 0, newData, 0, index);
                System.arraycopy(this.data, index + 2, newData, index, length - index);
                this.data = newData;
                return;
            }
            this.data = null;
        } else {
            length = this.data.length;
            newData = new Object[(length + 2)];
            System.arraycopy(this.data, 0, newData, 0, length);
            this.data = newData;
            int length2 = length + 1;
            this.data[length] = key;
            length = length2 + 1;
            this.data[length2] = value;
        }
    }

    public boolean isPersistent() {
        Iterator<ContactGroup> contactGroupsIter = getContactGroups();
        while (contactGroupsIter.hasNext()) {
            if (((ContactGroup) contactGroupsIter.next()).isPersistent()) {
                return true;
            }
        }
        if (countContactGroups() != 0) {
            return false;
        }
        return true;
    }

    private int dataIndexOf(Object key) {
        if (this.data != null) {
            for (int index = 0; index < this.data.length; index += 2) {
                if (key.equals(this.data[index])) {
                    return index;
                }
            }
        }
        return -1;
    }

    public int compareTo(MetaContactGroup target) {
        return (getGroupName().compareToIgnoreCase(target.getGroupName()) * MetaContactListServiceImpl.CONTACT_LIST_MODIFICATION_TIMEOUT) + getMetaUID().compareTo(target.getMetaUID());
    }
}
