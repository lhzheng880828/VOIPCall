package net.java.sip.communicator.impl.protocol.jabber;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ContactGroup;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;

public class ContactGroupJabberImpl extends AbstractContactGroupJabberImpl {
    private Map<String, Contact> buddies = new Hashtable();
    private List<ContactGroup> dummyGroupsList = new LinkedList();
    private String id = null;
    private boolean isResolved = false;
    private String nameCopy = null;
    private final ServerStoredContactListJabberImpl ssclCallback;
    private String tempId = null;

    ContactGroupJabberImpl(RosterGroup rosterGroup, Iterator<RosterEntry> groupMembers, ServerStoredContactListJabberImpl ssclCallback, boolean isResolved) {
        if (rosterGroup != null) {
            this.id = rosterGroup.getName();
        }
        this.isResolved = isResolved;
        this.ssclCallback = ssclCallback;
        if (rosterGroup != null) {
            this.nameCopy = rosterGroup.getName();
        }
        while (groupMembers.hasNext()) {
            RosterEntry rEntry = (RosterEntry) groupMembers.next();
            if (ServerStoredContactListJabberImpl.isEntryDisplayable(rEntry) && ssclCallback.findContactById(rEntry.getUser()) == null) {
                addContact(new ContactJabberImpl(rEntry, ssclCallback, true, true));
            }
        }
    }

    ContactGroupJabberImpl(String id, ServerStoredContactListJabberImpl ssclCallback) {
        this.tempId = id;
        this.isResolved = false;
        this.ssclCallback = ssclCallback;
    }

    public int countContacts() {
        return this.buddies.size();
    }

    public ContactGroup getParentContactGroup() {
        return this.ssclCallback.getRootGroup();
    }

    /* access modifiers changed from: 0000 */
    public void addContact(ContactJabberImpl contact) {
        this.buddies.put(contact.getAddress().toLowerCase(), contact);
    }

    /* access modifiers changed from: 0000 */
    public void removeContact(ContactJabberImpl contact) {
        this.buddies.remove(contact.getAddress().toLowerCase());
    }

    public Iterator<Contact> contacts() {
        return this.buddies.values().iterator();
    }

    public Contact getContact(String id) {
        return findContact(id);
    }

    public String getGroupName() {
        if (this.isResolved) {
            return this.id;
        }
        return this.tempId;
    }

    public boolean canContainSubgroups() {
        return false;
    }

    public ContactGroup getGroup(int index) {
        return null;
    }

    public ContactGroup getGroup(String groupName) {
        return null;
    }

    public Iterator<ContactGroup> subgroups() {
        return this.dummyGroupsList.iterator();
    }

    public int countSubgroups() {
        return 0;
    }

    public int hashCode() {
        return getGroupName().hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof ContactGroupJabberImpl)) {
            return false;
        }
        if (!((ContactGroup) obj).getGroupName().equals(getGroupName())) {
            return false;
        }
        if (getProtocolProvider() != ((ContactGroup) obj).getProtocolProvider()) {
            return false;
        }
        return true;
    }

    public ProtocolProviderService getProtocolProvider() {
        return this.ssclCallback.getParentProvider();
    }

    public String toString() {
        StringBuffer buff = new StringBuffer("JabberGroup.");
        buff.append(getGroupName()).append(", childContacts=").append(countContacts()).append(":[");
        Iterator<Contact> contacts = contacts();
        while (contacts.hasNext()) {
            buff.append(((Contact) contacts.next()).toString());
            if (contacts.hasNext()) {
                buff.append(", ");
            }
        }
        return buff.append("]").toString();
    }

    /* access modifiers changed from: 0000 */
    public ContactJabberImpl findContact(String id) {
        if (id == null) {
            return null;
        }
        return (ContactJabberImpl) this.buddies.get(id.toLowerCase());
    }

    /* access modifiers changed from: 0000 */
    public void setNameCopy(String newName) {
        this.nameCopy = newName;
    }

    /* access modifiers changed from: 0000 */
    public String getNameCopy() {
        return this.nameCopy;
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

    /* access modifiers changed from: 0000 */
    public void setResolved(RosterGroup source) {
        if (!this.isResolved) {
            this.isResolved = true;
            this.id = source.getName();
            for (RosterEntry item : source.getEntries()) {
                ContactJabberImpl contact = this.ssclCallback.findContactById(item.getUser());
                if (ServerStoredContactListJabberImpl.isEntryDisplayable(item)) {
                    if (contact != null) {
                        contact.setResolved(item);
                        this.ssclCallback.fireContactResolved(this, contact);
                    } else {
                        ContactJabberImpl newContact = new ContactJabberImpl(item, this.ssclCallback, true, true);
                        addContact(newContact);
                        this.ssclCallback.fireContactAdded(this, newContact);
                    }
                } else if (contact != null) {
                    removeContact(contact);
                    this.ssclCallback.fireContactRemoved(this, contact);
                }
            }
        }
    }

    public String getUID() {
        return getGroupName();
    }

    /* access modifiers changed from: 0000 */
    public RosterGroup getSourceGroup() {
        return this.ssclCallback.getRosterGroup(this.id);
    }

    /* access modifiers changed from: 0000 */
    public void setSourceGroup(RosterGroup newGroup) {
        this.id = newGroup.getName();
    }
}
