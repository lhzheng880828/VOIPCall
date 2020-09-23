package net.java.sip.communicator.impl.protocol.sip;

import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ContactGroup;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.ImageDetail;
import net.java.sip.communicator.service.protocol.event.ServerStoredGroupEvent;
import net.java.sip.communicator.service.protocol.event.ServerStoredGroupListener;
import net.java.sip.communicator.util.Logger;
import org.jitsi.javax.sip.address.Address;

public abstract class ServerStoredContactList {
    protected static final String ROOT_GROUP_NAME = "RootGroup";
    private static final Logger logger = Logger.getLogger(ServerStoredContactList.class);
    protected final OperationSetPresenceSipImpl parentOperationSet;
    protected final ContactGroupSipImpl rootGroup;
    private final Vector<ServerStoredGroupListener> serverStoredGroupListeners = new Vector();
    protected final ProtocolProviderServiceSipImpl sipProvider;

    public abstract void authorizationAccepted(ContactSipImpl contactSipImpl);

    public abstract void authorizationIgnored(ContactSipImpl contactSipImpl);

    public abstract void authorizationRejected(ContactSipImpl contactSipImpl);

    public abstract ContactSipImpl createContact(ContactGroupSipImpl contactGroupSipImpl, String str, String str2, boolean z, String str3) throws OperationFailedException;

    public abstract ContactGroupSipImpl createGroup(ContactGroupSipImpl contactGroupSipImpl, String str, boolean z) throws OperationFailedException;

    public abstract void deleteAccountImage() throws OperationFailedException;

    public abstract void destroy();

    public abstract ImageDetail getAccountImage() throws OperationFailedException;

    public abstract byte[] getImage(URI uri);

    public abstract URI getImageUri();

    public abstract void init();

    public abstract boolean isAccountImageSupported();

    public abstract void moveContactToGroup(ContactSipImpl contactSipImpl, ContactGroupSipImpl contactGroupSipImpl) throws OperationFailedException;

    public abstract void removeContact(ContactSipImpl contactSipImpl) throws OperationFailedException;

    public abstract void removeGroup(ContactGroupSipImpl contactGroupSipImpl);

    public abstract void renameContact(ContactSipImpl contactSipImpl, String str);

    public abstract void renameGroup(ContactGroupSipImpl contactGroupSipImpl, String str);

    public abstract void setAccountImage(byte[] bArr) throws OperationFailedException;

    ServerStoredContactList(ProtocolProviderServiceSipImpl sipProvider, OperationSetPresenceSipImpl parentOperationSet) {
        this.sipProvider = sipProvider;
        this.parentOperationSet = parentOperationSet;
        this.rootGroup = new ContactGroupSipImpl(ROOT_GROUP_NAME, sipProvider);
    }

    public ContactGroupSipImpl getRootGroup() {
        return this.rootGroup;
    }

    public void addGroupListener(ServerStoredGroupListener listener) {
        synchronized (this.serverStoredGroupListeners) {
            if (!this.serverStoredGroupListeners.contains(listener)) {
                this.serverStoredGroupListeners.add(listener);
            }
        }
    }

    public void removeGroupListener(ServerStoredGroupListener listener) {
        synchronized (this.serverStoredGroupListeners) {
            this.serverStoredGroupListeners.remove(listener);
        }
    }

    /* access modifiers changed from: protected */
    public void fireGroupEvent(ContactGroup group, int eventID) {
        ServerStoredGroupEvent event = new ServerStoredGroupEvent(group, eventID, this.parentOperationSet.getServerStoredContactListRoot(), this.sipProvider, this.parentOperationSet);
        if (logger.isTraceEnabled()) {
            logger.trace("Will dispatch the following group event: " + event);
        }
        synchronized (this.serverStoredGroupListeners) {
            Iterable<ServerStoredGroupListener> listeners = new ArrayList(this.serverStoredGroupListeners);
        }
        for (ServerStoredGroupListener listener : listeners) {
            if (eventID == 2) {
                listener.groupRemoved(event);
            } else if (eventID == 3) {
                listener.groupNameChanged(event);
            } else if (eventID == 1) {
                listener.groupCreated(event);
            } else if (eventID == 4) {
                listener.groupResolved(event);
            }
        }
    }

    public synchronized ContactSipImpl createUnresolvedContact(ContactGroupSipImpl parentGroup, String contactId, String persistentData) {
        ContactSipImpl newUnresolvedContact;
        if (parentGroup == null) {
            throw new IllegalArgumentException("Parent group cannot be null");
        }
        if (contactId != null) {
            if (contactId.length() != 0) {
                try {
                    Address contactAddress = this.sipProvider.parseAddressString(contactId);
                    if (logger.isTraceEnabled()) {
                        logger.trace("createUnresolvedContact " + contactId);
                    }
                    newUnresolvedContact = new ContactSipImpl(contactAddress, this.sipProvider);
                    parentGroup.addContact(newUnresolvedContact);
                    newUnresolvedContact.setPersistentData(persistentData);
                    fireContactAdded(parentGroup, newUnresolvedContact);
                } catch (ParseException ex) {
                    throw new IllegalArgumentException(String.format("%1s is no a valid SIP identifier", new Object[]{contactId}), ex);
                }
            }
        }
        throw new IllegalArgumentException("Creating contact id name cannot be null or empty");
        return newUnresolvedContact;
    }

    public synchronized ContactGroupSipImpl createUnresolvedContactGroup(ContactGroupSipImpl parentGroup, String groupName) {
        ContactGroupSipImpl subGroup;
        if (parentGroup == null) {
            throw new IllegalArgumentException("Parent group cannot be null");
        }
        if (groupName != null) {
            if (groupName.length() != 0) {
                if (logger.isTraceEnabled()) {
                    logger.trace("createUnresolvedContactGroup " + groupName);
                }
                subGroup = new ContactGroupSipImpl(groupName, this.sipProvider);
                subGroup.setResolved(false);
                parentGroup.addSubgroup(subGroup);
                fireGroupEvent(subGroup, 1);
            }
        }
        throw new IllegalArgumentException("Creating group name cannot be null or empry");
        return subGroup;
    }

    /* access modifiers changed from: protected */
    public void fireContactAdded(ContactGroupSipImpl parentGroup, ContactSipImpl contact) {
        this.parentOperationSet.fireSubscriptionEvent(contact, parentGroup, 1);
    }

    /* access modifiers changed from: protected */
    public void fireContactMoved(ContactGroupSipImpl oldParentGroup, ContactGroupSipImpl newParentGroup, ContactSipImpl contact) {
        this.parentOperationSet.fireSubscriptionMovedEvent(contact, oldParentGroup, newParentGroup);
    }

    /* access modifiers changed from: protected */
    public void fireContactRemoved(ContactGroupSipImpl parentGroup, ContactSipImpl contact) {
        this.parentOperationSet.fireSubscriptionEvent(contact, parentGroup, 2);
    }

    /* access modifiers changed from: protected */
    public void fireContactResolved(ContactGroupSipImpl parentGroup, ContactSipImpl contact) {
        this.parentOperationSet.fireSubscriptionEvent(contact, parentGroup, 4);
    }

    public synchronized List<ContactSipImpl> getUniqueContacts(ContactGroupSipImpl group) {
        Map<String, ContactSipImpl> uniqueContacts;
        uniqueContacts = new HashMap();
        for (ContactSipImpl contact : getAllContacts(group)) {
            uniqueContacts.put(contact.getUri(), contact);
        }
        return new ArrayList(uniqueContacts.values());
    }

    public synchronized List<ContactSipImpl> getAllContacts(ContactGroupSipImpl group) {
        List<ContactSipImpl> contacts;
        contacts = new ArrayList();
        Iterator<ContactGroup> groupIterator = group.subgroups();
        while (groupIterator.hasNext()) {
            contacts.addAll(getAllContacts((ContactGroupSipImpl) groupIterator.next()));
        }
        Iterator<Contact> contactIterator = group.contacts();
        while (contactIterator.hasNext()) {
            contacts.add((ContactSipImpl) contactIterator.next());
        }
        return contacts;
    }

    public synchronized List<ContactGroupSipImpl> getAllGroups(ContactGroupSipImpl group) {
        List<ContactGroupSipImpl> groups;
        groups = new ArrayList();
        Iterator<ContactGroup> groupIterator = group.subgroups();
        while (groupIterator.hasNext()) {
            groups.addAll(getAllGroups((ContactGroupSipImpl) groupIterator.next()));
        }
        return groups;
    }

    private boolean isContactExists(String contactUri) {
        for (ContactSipImpl uniqueContact : getUniqueContacts(this.rootGroup)) {
            if (uniqueContact.getUri().equals(contactUri)) {
                return true;
            }
        }
        return false;
    }

    private List<ContactSipImpl> getContacts(String contactUri) {
        List<ContactSipImpl> result = new ArrayList();
        for (ContactSipImpl contact : getAllContacts(this.rootGroup)) {
            if (contact.getUri().equals(contactUri)) {
                result.add(contact);
            }
        }
        return result;
    }

    private boolean isContactPersistent(String contactUri) {
        for (ContactSipImpl contact : getContacts(contactUri)) {
            if (contact.isPersistent()) {
                return true;
            }
        }
        return false;
    }

    public synchronized ContactSipImpl createContact(ContactGroupSipImpl parentGroup, String contactId, boolean persistent, String contactType) throws OperationFailedException {
        return createContact(parentGroup, contactId, null, persistent, contactType);
    }
}
