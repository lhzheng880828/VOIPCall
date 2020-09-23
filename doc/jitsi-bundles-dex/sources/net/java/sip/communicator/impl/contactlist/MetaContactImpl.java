package net.java.sip.communicator.impl.contactlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import net.java.sip.communicator.service.contactlist.MetaContact;
import net.java.sip.communicator.service.contactlist.MetaContactGroup;
import net.java.sip.communicator.service.contactlist.event.MetaContactModifiedEvent;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ContactGroup;
import net.java.sip.communicator.service.protocol.OperationSet;
import net.java.sip.communicator.service.protocol.OperationSetContactCapabilities;
import net.java.sip.communicator.service.protocol.PresenceStatus;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.util.AvatarCacheUtils;
import net.java.sip.communicator.util.DataObject;
import net.java.sip.communicator.util.Logger;

public class MetaContactImpl extends DataObject implements MetaContact {
    private static final Logger logger = Logger.getLogger(MetaContactImpl.class);
    private boolean avatarFileCacheAlreadyQueried;
    private byte[] cachedAvatar;
    private final Map<String, List<Contact>> capabilities;
    private int contactsOnline;
    private Contact defaultContact;
    private Map<String, List<String>> details;
    private String displayName;
    private boolean isDisplayNameUserDefined;
    private MetaContactGroupImpl parentGroup;
    private final List<Contact> protoContacts;
    private final String uid;

    MetaContactImpl() {
        this.protoContacts = new Vector();
        this.capabilities = new HashMap();
        this.contactsOnline = 0;
        this.displayName = "";
        this.defaultContact = null;
        this.cachedAvatar = null;
        this.avatarFileCacheAlreadyQueried = false;
        this.parentGroup = null;
        this.isDisplayNameUserDefined = false;
        this.uid = String.valueOf(System.currentTimeMillis()) + String.valueOf(hashCode());
        this.details = null;
    }

    MetaContactImpl(String metaUID, Map<String, List<String>> details) {
        this.protoContacts = new Vector();
        this.capabilities = new HashMap();
        this.contactsOnline = 0;
        this.displayName = "";
        this.defaultContact = null;
        this.cachedAvatar = null;
        this.avatarFileCacheAlreadyQueried = false;
        this.parentGroup = null;
        this.isDisplayNameUserDefined = false;
        this.uid = metaUID;
        this.details = details;
    }

    public int getContactCount() {
        return this.protoContacts.size();
    }

    public Iterator<Contact> getContactsForProvider(ProtocolProviderService provider) {
        LinkedList<Contact> providerContacts = new LinkedList();
        for (Contact contact : this.protoContacts) {
            if (contact.getProtocolProvider() == provider) {
                providerContacts.add(contact);
            }
        }
        return providerContacts.iterator();
    }

    public List<Contact> getContactsForOperationSet(Class<? extends OperationSet> opSetClass) {
        LinkedList<Contact> opSetContacts = new LinkedList();
        for (Contact contact : this.protoContacts) {
            ProtocolProviderService contactProvider = contact.getProtocolProvider();
            if (((OperationSetContactCapabilities) contactProvider.getOperationSet(OperationSetContactCapabilities.class)) != null) {
                List<Contact> capContacts = (List) this.capabilities.get(opSetClass.getName());
                if (capContacts != null && capContacts.contains(contact)) {
                    opSetContacts.add(contact);
                }
            } else if (contactProvider.getOperationSet(opSetClass) != null) {
                opSetContacts.add(contact);
            }
        }
        return opSetContacts;
    }

    public Iterator<Contact> getContactsForContactGroup(ContactGroup parentProtoGroup) {
        List<Contact> providerContacts = new LinkedList();
        for (Contact contact : this.protoContacts) {
            if (contact.getParentContactGroup() == parentProtoGroup) {
                providerContacts.add(contact);
            }
        }
        return providerContacts.iterator();
    }

    public Contact getContact(String contactAddress, ProtocolProviderService ownerProvider) {
        for (Contact contact : this.protoContacts) {
            if (contact.getProtocolProvider() == ownerProvider && (contact.getAddress().equals(contactAddress) || contact.equals(contactAddress))) {
                return contact;
            }
        }
        return null;
    }

    public Contact getContact(String contactAddress, String accountID) {
        for (Contact contact : this.protoContacts) {
            if (contact.getProtocolProvider().getAccountID().getAccountUniqueID().equals(accountID) && contact.getAddress().equals(contactAddress)) {
                return contact;
            }
        }
        return null;
    }

    public boolean containsContact(Contact protocolContact) {
        return this.protoContacts.contains(protocolContact);
    }

    public Iterator<Contact> getContacts() {
        return new LinkedList(this.protoContacts).iterator();
    }

    public Contact getDefaultContact() {
        if (this.defaultContact == null) {
            PresenceStatus currentStatus = null;
            for (Contact protoContact : this.protoContacts) {
                PresenceStatus contactStatus = protoContact.getPresenceStatus();
                if (currentStatus == null) {
                    currentStatus = contactStatus;
                    this.defaultContact = protoContact;
                } else if (currentStatus.getStatus() < contactStatus.getStatus()) {
                    currentStatus = contactStatus;
                    this.defaultContact = protoContact;
                }
            }
        }
        return this.defaultContact;
    }

    public Contact getDefaultContact(Class<? extends OperationSet> operationSet) {
        ProtocolProviderService contactProvider;
        List<Contact> capContacts;
        Contact defaultOpSetContact = null;
        Contact defaultContact = getDefaultContact();
        if (defaultContact != null) {
            contactProvider = defaultContact.getProtocolProvider();
            if (((OperationSetContactCapabilities) contactProvider.getOperationSet(OperationSetContactCapabilities.class)) != null) {
                capContacts = (List) this.capabilities.get(operationSet.getName());
                if (capContacts != null && capContacts.contains(defaultContact)) {
                    defaultOpSetContact = defaultContact;
                }
            } else if (contactProvider.getOperationSet(operationSet) != null) {
                defaultOpSetContact = defaultContact;
            }
        }
        if (defaultOpSetContact == null) {
            PresenceStatus currentStatus = null;
            for (Contact protoContact : this.protoContacts) {
                contactProvider = protoContact.getProtocolProvider();
                if (((OperationSetContactCapabilities) contactProvider.getOperationSet(OperationSetContactCapabilities.class)) != null) {
                    capContacts = (List) this.capabilities.get(operationSet.getName());
                    if (capContacts != null) {
                        if (!capContacts.contains(protoContact)) {
                        }
                    }
                } else if (contactProvider.getOperationSet(operationSet) == null) {
                }
                PresenceStatus contactStatus = protoContact.getPresenceStatus();
                if (currentStatus == null) {
                    currentStatus = contactStatus;
                    defaultOpSetContact = protoContact;
                } else if (currentStatus.getStatus() < contactStatus.getStatus()) {
                    currentStatus = contactStatus;
                    defaultOpSetContact = protoContact;
                }
            }
        }
        return defaultOpSetContact;
    }

    public String getMetaUID() {
        return this.uid;
    }

    public int compareTo(MetaContact o) {
        int isOnline;
        int targetIsOnline;
        MetaContactImpl target = (MetaContactImpl) o;
        if (this.contactsOnline > 0) {
            isOnline = 1;
        } else {
            isOnline = 0;
        }
        if (target.contactsOnline > 0) {
            targetIsOnline = 1;
        } else {
            targetIsOnline = 0;
        }
        return ((((10 - isOnline) - (10 - targetIsOnline)) * 100000000) + (getDisplayName().compareToIgnoreCase(target.getDisplayName()) * MetaContactListServiceImpl.CONTACT_LIST_MODIFICATION_TIMEOUT)) + getMetaUID().compareTo(target.getMetaUID());
    }

    public String toString() {
        return new StringBuffer("MetaContact[ DisplayName=").append(getDisplayName()).append("]").toString();
    }

    public String getDisplayName() {
        return this.displayName;
    }

    /* access modifiers changed from: 0000 */
    public boolean isDisplayNameUserDefined() {
        return this.isDisplayNameUserDefined;
    }

    /* access modifiers changed from: 0000 */
    public void setDisplayNameUserDefined(boolean value) {
        this.isDisplayNameUserDefined = value;
    }

    private byte[] queryProtoContactAvatar(Contact contact) {
        try {
            byte[] contactImage = contact.getImage();
            if (contactImage != null && contactImage.length > 0) {
                return contactImage;
            }
        } catch (Exception ex) {
            logger.error("Failed to get the photo of contact " + contact, ex);
        }
        return null;
    }

    public byte[] getAvatar(boolean isLazy) {
        if (!isLazy) {
            Iterator<Contact> protoContacts = getContacts();
            while (protoContacts.hasNext()) {
                Contact contact = (Contact) protoContacts.next();
                byte[] result = queryProtoContactAvatar(contact);
                if (result != null && result.length > 0) {
                    cacheAvatar(contact, result);
                    return result;
                }
            }
        }
        if (this.cachedAvatar != null && this.cachedAvatar.length > 0) {
            return this.cachedAvatar;
        }
        if (this.avatarFileCacheAlreadyQueried) {
            return null;
        }
        this.avatarFileCacheAlreadyQueried = true;
        Iterator<Contact> iter = getContacts();
        while (iter.hasNext()) {
            this.cachedAvatar = AvatarCacheUtils.getCachedAvatar((Contact) iter.next());
            if (this.cachedAvatar != null && this.cachedAvatar.length > 0) {
                return this.cachedAvatar;
            }
        }
        return null;
    }

    public byte[] getAvatar() {
        return getAvatar(false);
    }

    /* access modifiers changed from: 0000 */
    public void setDisplayName(String displayName) {
        synchronized (getParentGroupModLock()) {
            if (this.parentGroup != null) {
                this.parentGroup.lightRemoveMetaContact(this);
            }
            if (displayName == null) {
                displayName = "";
            }
            this.displayName = displayName;
            if (this.parentGroup != null) {
                this.parentGroup.lightAddMetaContact(this);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void addProtoContact(Contact contact) {
        synchronized (getParentGroupModLock()) {
            if (this.parentGroup != null) {
                this.parentGroup.lightRemoveMetaContact(this);
            }
            this.contactsOnline = (contact.getPresenceStatus().isOnline() ? 1 : 0) + this.contactsOnline;
            this.protoContacts.add(contact);
            this.defaultContact = null;
            if (this.protoContacts.size() == 1 && (this.displayName == null || this.displayName.trim().length() == 0)) {
                this.displayName = contact.getDisplayName();
            }
            if (this.parentGroup != null) {
                this.parentGroup.lightAddMetaContact(this);
            }
            OperationSetContactCapabilities capOpSet = (OperationSetContactCapabilities) contact.getProtocolProvider().getOperationSet(OperationSetContactCapabilities.class);
            if (capOpSet != null) {
                addCapabilities(contact, capOpSet.getSupportedOperationSets(contact));
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public int reevalContact() {
        synchronized (getParentGroupModLock()) {
            if (this.parentGroup != null) {
                this.parentGroup.lightRemoveMetaContact(this);
            }
            this.contactsOnline = 0;
            int maxContactStatus = 0;
            for (Contact contact : this.protoContacts) {
                int contactStatus = contact.getPresenceStatus().getStatus();
                if (maxContactStatus < contactStatus) {
                    maxContactStatus = contactStatus;
                    this.defaultContact = contact;
                }
                if (contact.getPresenceStatus().isOnline()) {
                    this.contactsOnline++;
                }
            }
            if (this.parentGroup != null) {
                int lightAddMetaContact = this.parentGroup.lightAddMetaContact(this);
                return lightAddMetaContact;
            }
            return -1;
        }
    }

    /* access modifiers changed from: 0000 */
    public void removeProtoContact(Contact contact) {
        synchronized (getParentGroupModLock()) {
            if (this.parentGroup != null) {
                this.parentGroup.lightRemoveMetaContact(this);
            }
            this.contactsOnline -= contact.getPresenceStatus().isOnline() ? 1 : 0;
            this.protoContacts.remove(contact);
            if (this.defaultContact == contact) {
                this.defaultContact = null;
            }
            if (this.protoContacts.size() > 0 && this.displayName.equals(contact.getDisplayName())) {
                this.displayName = getDefaultContact().getDisplayName();
            }
            if (this.parentGroup != null) {
                this.parentGroup.lightAddMetaContact(this);
            }
            OperationSetContactCapabilities capOpSet = (OperationSetContactCapabilities) contact.getProtocolProvider().getOperationSet(OperationSetContactCapabilities.class);
            if (capOpSet != null) {
                removeCapabilities(contact, capOpSet.getSupportedOperationSets(contact));
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean removeContactsForProvider(ProtocolProviderService provider) {
        boolean modified = false;
        Iterator<Contact> contactsIter = this.protoContacts.iterator();
        while (contactsIter.hasNext()) {
            if (((Contact) contactsIter.next()).getProtocolProvider() == provider) {
                contactsIter.remove();
                modified = true;
            }
        }
        if (modified && !this.protoContacts.contains(this.defaultContact)) {
            this.defaultContact = null;
        }
        return modified;
    }

    /* access modifiers changed from: 0000 */
    public boolean removeContactsForGroup(ContactGroup protoGroup) {
        boolean modified = false;
        Iterator<Contact> contactsIter = this.protoContacts.iterator();
        while (contactsIter.hasNext()) {
            if (((Contact) contactsIter.next()).getParentContactGroup() == protoGroup) {
                contactsIter.remove();
                modified = true;
            }
        }
        if (modified && !this.protoContacts.contains(this.defaultContact)) {
            this.defaultContact = null;
        }
        return modified;
    }

    /* access modifiers changed from: 0000 */
    public void setParentGroup(MetaContactGroupImpl parentGroup) {
        if (parentGroup == null) {
            throw new NullPointerException("Do not call this method with a null argument even if a group is removing this contact from itself as this could lead to race conditions (imagine another group setting itself as the new parent and you  removing it). Use unsetParentGroup instead.");
        }
        synchronized (getParentGroupModLock()) {
            this.parentGroup = parentGroup;
        }
    }

    /* access modifiers changed from: 0000 */
    public void unsetParentGroup(MetaContactGroupImpl parentGrp) {
        synchronized (getParentGroupModLock()) {
            if (this.parentGroup == parentGrp) {
                this.parentGroup = null;
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public MetaContactGroupImpl getParentGroup() {
        return this.parentGroup;
    }

    public MetaContactGroup getParentMetaContactGroup() {
        return getParentGroup();
    }

    public void addDetail(String name, String value) {
        if (this.details == null) {
            this.details = new Hashtable();
        }
        List<String> values = (List) this.details.get(name);
        if (values == null) {
            values = new ArrayList();
            this.details.put(name, values);
        }
        values.add(value);
        fireMetaContactModified(name, null, value);
    }

    public void removeDetail(String name, String value) {
        if (this.details != null) {
            List<String> values = (List) this.details.get(name);
            if (values != null) {
                values.remove(value);
                fireMetaContactModified(name, value, null);
            }
        }
    }

    public void removeDetails(String name) {
        if (this.details != null) {
            fireMetaContactModified(name, this.details.remove(name), null);
        }
    }

    public void changeDetail(String name, String oldValue, String newValue) {
        if (this.details != null) {
            List<String> values = (List) this.details.get(name);
            if (values != null) {
                int changedIx = values.indexOf(oldValue);
                if (changedIx != -1) {
                    values.set(changedIx, newValue);
                    fireMetaContactModified(name, oldValue, newValue);
                }
            }
        }
    }

    private void fireMetaContactModified(String modificationName, Object oldValue, Object newValue) {
        MetaContactGroupImpl parentGroup = getParentGroup();
        if (parentGroup != null) {
            parentGroup.getMclServiceImpl().fireMetaContactEvent(new MetaContactModifiedEvent(this, modificationName, oldValue, newValue));
        }
    }

    public List<String> getDetails(String name) {
        List<String> values = this.details == null ? null : (List) this.details.get(name);
        if (values == null) {
            return new ArrayList();
        }
        return new ArrayList(values);
    }

    public void cacheAvatar(Contact protoContact, byte[] avatarBytes) {
        this.cachedAvatar = avatarBytes;
        this.avatarFileCacheAlreadyQueried = true;
        AvatarCacheUtils.cacheAvatar(protoContact, avatarBytes);
    }

    public void updateCapabilities(Contact contact, Map<String, ? extends OperationSet> opSets) {
        if (((OperationSetContactCapabilities) contact.getProtocolProvider().getOperationSet(OperationSetContactCapabilities.class)) != null) {
            removeCapabilities(contact, opSets);
            addCapabilities(contact, opSets);
        }
    }

    private void removeCapabilities(Contact contact, Map<String, ? extends OperationSet> opSets) {
        Iterator<Entry<String, List<Contact>>> caps = this.capabilities.entrySet().iterator();
        Set<String> contactNewCaps = opSets.keySet();
        while (caps.hasNext()) {
            Entry<String, List<Contact>> entry = (Entry) caps.next();
            String opSetName = (String) entry.getKey();
            List<Contact> contactsForCap = (List) entry.getValue();
            if (contactsForCap.contains(contact) && !contactNewCaps.contains(opSetName)) {
                contactsForCap.remove(contact);
                if (contactsForCap.size() == 0) {
                    caps.remove();
                }
            }
        }
    }

    private void addCapabilities(Contact contact, Map<String, ? extends OperationSet> opSets) {
        for (String newCap : opSets.keySet()) {
            List<Contact> capContacts;
            if (this.capabilities.containsKey(newCap)) {
                capContacts = (List) this.capabilities.get(newCap);
                if (!capContacts.contains(contact)) {
                    capContacts.add(contact);
                }
            } else {
                capContacts = new LinkedList();
                capContacts.add(contact);
                this.capabilities.put(newCap, capContacts);
            }
        }
    }

    private Object getParentGroupModLock() {
        return this.uid;
    }
}
