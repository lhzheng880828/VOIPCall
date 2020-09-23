package net.java.sip.communicator.impl.protocol.jabber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import net.java.sip.communicator.service.customavatar.CustomAvatarService;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ContactGroup;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.GenericDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.ImageDetail;
import net.java.sip.communicator.service.protocol.event.ServerStoredGroupEvent;
import net.java.sip.communicator.service.protocol.event.ServerStoredGroupListener;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.message.Response;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket.ItemStatus;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.util.StringUtils;
import org.osgi.framework.ServiceReference;

public class ServerStoredContactListJabberImpl {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(ServerStoredContactListJabberImpl.class);
    private ImageRetriever imageRetriever = null;
    /* access modifiers changed from: private */
    public InfoRetreiver infoRetreiver = null;
    private final ProtocolProviderServiceJabberImpl jabberProvider;
    /* access modifiers changed from: private|final */
    public final OperationSetPersistentPresenceJabberImpl parentOperationSet;
    /* access modifiers changed from: private|final */
    public final RootContactGroupJabberImpl rootGroup;
    /* access modifiers changed from: private */
    public Roster roster = null;
    private ChangeListener rosterChangeListener = null;
    private Vector<ServerStoredGroupListener> serverStoredGroupListeners = new Vector();

    private class ChangeListener implements RosterListener {
        private ChangeListener() {
        }

        public void rosterError(XMPPError error, Packet packet) {
            ServerStoredContactListJabberImpl.logger.error("Error received in roster " + error.getCode() + Separators.SP + error.getMessage());
        }

        public void entriesAdded(Collection<String> addresses) {
            if (ServerStoredContactListJabberImpl.logger.isTraceEnabled()) {
                ServerStoredContactListJabberImpl.logger.trace("entriesAdded " + addresses);
            }
            for (String id : addresses) {
                addEntryToContactList(id);
            }
        }

        private ContactJabberImpl addEntryToContactList(String rosterEntryID) {
            RosterEntry entry = ServerStoredContactListJabberImpl.this.roster.getEntry(rosterEntryID);
            if (!ServerStoredContactListJabberImpl.isEntryDisplayable(entry)) {
                return null;
            }
            ContactJabberImpl contact = ServerStoredContactListJabberImpl.this.findContactById(entry.getUser());
            if (contact == null) {
                contact = findPrivateContactByRealId(entry.getUser());
            }
            if (contact != null) {
                if (contact.isPersistent()) {
                    contact.setResolved(entry);
                    return contact;
                } else if (!(contact instanceof VolatileContactJabberImpl)) {
                    return contact;
                } else {
                    ContactGroup oldParentGroup = contact.getParentContactGroup();
                    if ((oldParentGroup instanceof ContactGroupJabberImpl) && !oldParentGroup.isPersistent()) {
                        ((ContactGroupJabberImpl) oldParentGroup).removeContact(contact);
                        ServerStoredContactListJabberImpl.this.fireContactRemoved(oldParentGroup, contact);
                    }
                }
            }
            contact = new ContactJabberImpl(entry, ServerStoredContactListJabberImpl.this, true, true);
            if (entry.getGroups() == null || entry.getGroups().size() == 0) {
                ServerStoredContactListJabberImpl.this.rootGroup.addContact(contact);
                ServerStoredContactListJabberImpl.this.fireContactAdded(ServerStoredContactListJabberImpl.this.rootGroup, contact);
                return contact;
            }
            Iterator i$ = entry.getGroups().iterator();
            if (!i$.hasNext()) {
                return contact;
            }
            RosterGroup group = (RosterGroup) i$.next();
            ContactGroupJabberImpl parentGroup = ServerStoredContactListJabberImpl.this.findContactGroup(group.getName());
            if (parentGroup != null) {
                parentGroup.addContact(contact);
                ServerStoredContactListJabberImpl.this.fireContactAdded(ServerStoredContactListJabberImpl.this.findContactGroup(contact), contact);
                return contact;
            }
            ContactGroupJabberImpl newGroup = new ContactGroupJabberImpl(group, group.getEntries().iterator(), ServerStoredContactListJabberImpl.this, true);
            ServerStoredContactListJabberImpl.this.rootGroup.addSubGroup(newGroup);
            ServerStoredContactListJabberImpl.this.fireGroupEvent(newGroup, 1);
            return contact;
        }

        private ContactJabberImpl findPrivateContactByRealId(String id) {
            ContactGroupJabberImpl volatileGroup = ServerStoredContactListJabberImpl.this.getNonPersistentGroup();
            if (volatileGroup == null) {
                return null;
            }
            Iterator<Contact> it = volatileGroup.contacts();
            while (it.hasNext()) {
                Contact contact = (Contact) it.next();
                if (contact.getPersistableAddress() != null && contact.getPersistableAddress().equals(StringUtils.parseBareAddress(id))) {
                    return (ContactJabberImpl) contact;
                }
            }
            return null;
        }

        public void entriesUpdated(Collection<String> addresses) {
            if (ServerStoredContactListJabberImpl.logger.isTraceEnabled()) {
                ServerStoredContactListJabberImpl.logger.trace("entriesUpdated  " + addresses);
            }
            for (String contactID : addresses) {
                RosterEntry entry = ServerStoredContactListJabberImpl.this.roster.getEntry(contactID);
                ContactJabberImpl contact = addEntryToContactList(contactID);
                if (entry.getGroups().size() == 0) {
                    checkForRename(entry.getName(), contact);
                }
                for (RosterGroup gr : entry.getGroups()) {
                    ContactGroup cgr = ServerStoredContactListJabberImpl.this.findContactGroup(gr.getName());
                    if (cgr == null) {
                        ContactGroupJabberImpl group = ServerStoredContactListJabberImpl.this.findContactGroupByNameCopy(gr.getName());
                        if (group != null) {
                            group.setSourceGroup(gr);
                            ServerStoredContactListJabberImpl.this.fireGroupEvent(group, 3);
                        } else {
                            ContactGroup currentParentGroup = contact.getParentContactGroup();
                            if (currentParentGroup.countContacts() > 1) {
                                cgr = currentParentGroup;
                            } else {
                                boolean present = false;
                                for (RosterGroup entryGr : entry.getGroups()) {
                                    if (entryGr.getName().equals(currentParentGroup.getGroupName())) {
                                        present = true;
                                        break;
                                    }
                                }
                                if (!present && (currentParentGroup instanceof ContactGroupJabberImpl)) {
                                    ContactGroupJabberImpl currentGroup = (ContactGroupJabberImpl) currentParentGroup;
                                    currentGroup.setSourceGroup(gr);
                                    ServerStoredContactListJabberImpl.this.fireGroupEvent(currentGroup, 3);
                                }
                            }
                        }
                    }
                    if (cgr != null) {
                        ContactGroup contactGroup = contact.getParentContactGroup();
                        if (gr.getName().equals(contactGroup.getGroupName())) {
                            checkForRename(entry.getName(), contact);
                        } else {
                            if (contactGroup instanceof ContactGroupJabberImpl) {
                                ((ContactGroupJabberImpl) contactGroup).removeContact(contact);
                            } else if (contactGroup instanceof RootContactGroupJabberImpl) {
                                ((RootContactGroupJabberImpl) contactGroup).removeContact(contact);
                            }
                            ContactGroupJabberImpl newParentGroup = ServerStoredContactListJabberImpl.this.findContactGroup(gr.getName());
                            if (newParentGroup == null) {
                                ContactGroupJabberImpl contactGroupJabberImpl = new ContactGroupJabberImpl(gr, new ArrayList().iterator(), ServerStoredContactListJabberImpl.this, true);
                                ServerStoredContactListJabberImpl.this.rootGroup.addSubGroup(contactGroupJabberImpl);
                                ServerStoredContactListJabberImpl.this.fireGroupEvent(contactGroupJabberImpl, 1);
                            }
                            newParentGroup.addContact(contact);
                            ServerStoredContactListJabberImpl.this.fireContactMoved(contactGroup, newParentGroup, contact);
                            if ((contactGroup instanceof ContactGroupJabberImpl) && contactGroup.countContacts() == 0) {
                                ServerStoredContactListJabberImpl.this.rootGroup.removeSubGroup((ContactGroupJabberImpl) contactGroup);
                                ServerStoredContactListJabberImpl.this.fireGroupEvent((ContactGroupJabberImpl) contactGroup, 2);
                            }
                        }
                    }
                }
            }
        }

        private void checkForRename(String newValue, ContactJabberImpl contact) {
            if (newValue != null && !newValue.equals(contact.getServerDisplayName())) {
                String oldValue = contact.getServerDisplayName();
                contact.setServerDisplayName(newValue);
                ServerStoredContactListJabberImpl.this.parentOperationSet.fireContactPropertyChangeEvent("DisplayName", contact, oldValue, newValue);
            }
        }

        public void entriesDeleted(Collection<String> addresses) {
            for (String address : addresses) {
                if (ServerStoredContactListJabberImpl.logger.isTraceEnabled()) {
                    ServerStoredContactListJabberImpl.logger.trace("entry deleted " + address);
                }
                ContactJabberImpl contact = ServerStoredContactListJabberImpl.this.findContactById(address);
                if (contact != null) {
                    ServerStoredContactListJabberImpl.this.contactDeleted(contact);
                } else if (ServerStoredContactListJabberImpl.logger.isTraceEnabled()) {
                    ServerStoredContactListJabberImpl.logger.trace("Could not find contact for deleted entry:" + address);
                }
            }
        }

        public void presenceChanged(Presence presence) {
        }
    }

    private class ImageRetriever extends Thread {
        private final List<ContactJabberImpl> contactsForUpdate = new Vector();
        private boolean running = false;

        ImageRetriever() {
            setDaemon(true);
        }

        /* JADX WARNING: Missing block: B:20:?, code skipped:
            r5 = r1.iterator();
     */
        /* JADX WARNING: Missing block: B:22:0x0034, code skipped:
            if (r5.hasNext() == false) goto L_0x006c;
     */
        /* JADX WARNING: Missing block: B:23:0x0036, code skipped:
            r0 = (net.java.sip.communicator.impl.protocol.jabber.ContactJabberImpl) r5.next();
            r4 = getAvatar(r0);
     */
        /* JADX WARNING: Missing block: B:24:0x0040, code skipped:
            if (r4 == null) goto L_0x0065;
     */
        /* JADX WARNING: Missing block: B:25:0x0042, code skipped:
            r6 = r0.getImage(false);
            r0.setImage(r4);
            net.java.sip.communicator.impl.protocol.jabber.ServerStoredContactListJabberImpl.access$600(r9.this$0).fireContactPropertyChangeEvent("Image", r0, r6, r4);
     */
        /* JADX WARNING: Missing block: B:35:0x0065, code skipped:
            r0.setImage(new byte[0]);
     */
        public void run() {
            /*
            r9 = this;
            r1 = 0;
            r7 = 1;
            r9.running = r7;	 Catch:{ InterruptedException -> 0x0056 }
            r2 = r1;
        L_0x0005:
            r7 = r9.running;	 Catch:{ InterruptedException -> 0x006e }
            if (r7 == 0) goto L_0x001e;
        L_0x0009:
            r8 = r9.contactsForUpdate;	 Catch:{ InterruptedException -> 0x006e }
            monitor-enter(r8);	 Catch:{ InterruptedException -> 0x006e }
            r7 = r9.contactsForUpdate;	 Catch:{ all -> 0x0061 }
            r7 = r7.isEmpty();	 Catch:{ all -> 0x0061 }
            if (r7 == 0) goto L_0x0019;
        L_0x0014:
            r7 = r9.contactsForUpdate;	 Catch:{ all -> 0x0061 }
            r7.wait();	 Catch:{ all -> 0x0061 }
        L_0x0019:
            r7 = r9.running;	 Catch:{ all -> 0x0061 }
            if (r7 != 0) goto L_0x001f;
        L_0x001d:
            monitor-exit(r8);	 Catch:{ all -> 0x0061 }
        L_0x001e:
            return;
        L_0x001f:
            r1 = new java.util.Vector;	 Catch:{ all -> 0x0061 }
            r7 = r9.contactsForUpdate;	 Catch:{ all -> 0x0061 }
            r1.<init>(r7);	 Catch:{ all -> 0x0061 }
            r7 = r9.contactsForUpdate;	 Catch:{ all -> 0x0071 }
            r7.clear();	 Catch:{ all -> 0x0071 }
            monitor-exit(r8);	 Catch:{ all -> 0x0071 }
            r5 = r1.iterator();	 Catch:{ InterruptedException -> 0x0056 }
        L_0x0030:
            r7 = r5.hasNext();	 Catch:{ InterruptedException -> 0x0056 }
            if (r7 == 0) goto L_0x006c;
        L_0x0036:
            r0 = r5.next();	 Catch:{ InterruptedException -> 0x0056 }
            r0 = (net.java.sip.communicator.impl.protocol.jabber.ContactJabberImpl) r0;	 Catch:{ InterruptedException -> 0x0056 }
            r4 = r9.getAvatar(r0);	 Catch:{ InterruptedException -> 0x0056 }
            if (r4 == 0) goto L_0x0065;
        L_0x0042:
            r7 = 0;
            r6 = r0.getImage(r7);	 Catch:{ InterruptedException -> 0x0056 }
            r0.setImage(r4);	 Catch:{ InterruptedException -> 0x0056 }
            r7 = net.java.sip.communicator.impl.protocol.jabber.ServerStoredContactListJabberImpl.this;	 Catch:{ InterruptedException -> 0x0056 }
            r7 = r7.parentOperationSet;	 Catch:{ InterruptedException -> 0x0056 }
            r8 = "Image";
            r7.fireContactPropertyChangeEvent(r8, r0, r6, r4);	 Catch:{ InterruptedException -> 0x0056 }
            goto L_0x0030;
        L_0x0056:
            r3 = move-exception;
        L_0x0057:
            r7 = net.java.sip.communicator.impl.protocol.jabber.ServerStoredContactListJabberImpl.logger;
            r8 = "ImageRetriever error waiting will stop now!";
            r7.error(r8, r3);
            goto L_0x001e;
        L_0x0061:
            r7 = move-exception;
            r1 = r2;
        L_0x0063:
            monitor-exit(r8);	 Catch:{ all -> 0x0071 }
            throw r7;	 Catch:{ InterruptedException -> 0x0056 }
        L_0x0065:
            r7 = 0;
            r7 = new byte[r7];	 Catch:{ InterruptedException -> 0x0056 }
            r0.setImage(r7);	 Catch:{ InterruptedException -> 0x0056 }
            goto L_0x0030;
        L_0x006c:
            r2 = r1;
            goto L_0x0005;
        L_0x006e:
            r3 = move-exception;
            r1 = r2;
            goto L_0x0057;
        L_0x0071:
            r7 = move-exception;
            goto L_0x0063;
            */
            throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.jabber.ServerStoredContactListJabberImpl$ImageRetriever.run():void");
        }

        /* access modifiers changed from: declared_synchronized */
        public synchronized void addContact(ContactJabberImpl contact) {
            synchronized (this.contactsForUpdate) {
                if (!this.contactsForUpdate.contains(contact)) {
                    this.contactsForUpdate.add(contact);
                    this.contactsForUpdate.notifyAll();
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void quit() {
            synchronized (this.contactsForUpdate) {
                this.running = false;
                this.contactsForUpdate.notifyAll();
            }
        }

        private byte[] getAvatar(ContactJabberImpl contact) {
            byte[] result = null;
            try {
                Iterator<GenericDetail> iter = ServerStoredContactListJabberImpl.this.infoRetreiver.getDetails(contact.getAddress(), ImageDetail.class);
                if (iter.hasNext()) {
                    result = ((ImageDetail) iter.next()).getBytes();
                }
                if (result == null) {
                    result = ServerStoredContactListJabberImpl.this.searchForCustomAvatar(contact.getAddress());
                }
                return result;
            } catch (Exception ex) {
                if (ServerStoredContactListJabberImpl.logger.isDebugEnabled()) {
                    ServerStoredContactListJabberImpl.logger.debug("Cannot load image for contact " + contact + ": " + ex.getMessage(), ex);
                }
                result = ServerStoredContactListJabberImpl.this.searchForCustomAvatar(contact.getAddress());
                if (result == null) {
                    result = new byte[0];
                }
                return result;
            }
        }
    }

    ServerStoredContactListJabberImpl(OperationSetPersistentPresenceJabberImpl parentOperationSet, ProtocolProviderServiceJabberImpl provider, InfoRetreiver infoRetreiver) {
        this.parentOperationSet = parentOperationSet;
        this.jabberProvider = provider;
        this.rootGroup = new RootContactGroupJabberImpl(this.jabberProvider);
        this.infoRetreiver = infoRetreiver;
    }

    public ContactGroup getRootGroup() {
        return this.rootGroup;
    }

    /* access modifiers changed from: 0000 */
    public RosterEntry getRosterEntry(String user) {
        if (this.roster == null) {
            return null;
        }
        return this.roster.getEntry(user);
    }

    /* access modifiers changed from: 0000 */
    public RosterGroup getRosterGroup(String name) {
        return this.roster.getGroup(name);
    }

    /* access modifiers changed from: 0000 */
    public void addGroupListener(ServerStoredGroupListener listener) {
        synchronized (this.serverStoredGroupListeners) {
            if (!this.serverStoredGroupListeners.contains(listener)) {
                this.serverStoredGroupListeners.add(listener);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void removeGroupListener(ServerStoredGroupListener listener) {
        synchronized (this.serverStoredGroupListeners) {
            this.serverStoredGroupListeners.remove(listener);
        }
    }

    /* access modifiers changed from: 0000 */
    public void fireGroupEvent(ContactGroupJabberImpl group, int eventID) {
        if (this.parentOperationSet != null) {
            ServerStoredGroupEvent evt = new ServerStoredGroupEvent(group, eventID, this.parentOperationSet.getServerStoredContactListRoot(), this.jabberProvider, this.parentOperationSet);
            if (logger.isTraceEnabled()) {
                logger.trace("Will dispatch the following grp event: " + evt);
            }
            synchronized (this.serverStoredGroupListeners) {
                Iterable<ServerStoredGroupListener> listeners = new ArrayList(this.serverStoredGroupListeners);
            }
            if (eventID == 1) {
                Iterator<Contact> iter = group.contacts();
                while (iter.hasNext()) {
                    ContactJabberImpl c = (ContactJabberImpl) iter.next();
                    if (this.roster != null) {
                        this.parentOperationSet.firePresenceStatusChanged(this.roster.getPresence(c.getAddress()));
                    }
                }
            }
            for (ServerStoredGroupListener listener : listeners) {
                if (eventID == 2) {
                    listener.groupRemoved(evt);
                } else if (eventID == 3) {
                    listener.groupNameChanged(evt);
                } else if (eventID == 1) {
                    listener.groupCreated(evt);
                } else if (eventID == 4) {
                    listener.groupResolved(evt);
                }
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("No presence op. set available. Bailing out.");
        }
    }

    /* access modifiers changed from: 0000 */
    public void fireContactRemoved(ContactGroup parentGroup, ContactJabberImpl contact) {
        if (this.parentOperationSet != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Removing " + contact.getAddress() + " from " + parentGroup.getGroupName());
            }
            this.parentOperationSet.fireSubscriptionEvent(contact, parentGroup, 2);
        } else if (logger.isDebugEnabled()) {
            logger.debug("No presence op. set available. Bailing out.");
        }
    }

    /* access modifiers changed from: private */
    public void fireContactMoved(ContactGroup oldParentGroup, ContactGroupJabberImpl newParentGroup, ContactJabberImpl contact) {
        if (this.parentOperationSet != null) {
            this.parentOperationSet.fireSubscriptionMovedEvent(contact, oldParentGroup, newParentGroup);
        } else if (logger.isDebugEnabled()) {
            logger.debug("No presence op. set available. Bailing out.");
        }
    }

    /* access modifiers changed from: 0000 */
    public ProtocolProviderServiceJabberImpl getParentProvider() {
        return this.jabberProvider;
    }

    public ContactGroupJabberImpl findContactGroup(String name) {
        Iterator<ContactGroup> contactGroups = this.rootGroup.subgroups();
        name = name.trim();
        while (contactGroups.hasNext()) {
            ContactGroupJabberImpl contactGroup = (ContactGroupJabberImpl) contactGroups.next();
            if (contactGroup.getGroupName().trim().equals(name)) {
                return contactGroup;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public ContactGroupJabberImpl findContactGroupByNameCopy(String name) {
        Iterator<ContactGroup> contactGroups = this.rootGroup.subgroups();
        name = name.trim();
        while (contactGroups.hasNext()) {
            ContactGroupJabberImpl contactGroup = (ContactGroupJabberImpl) contactGroups.next();
            if (contactGroup.getNameCopy() != null && contactGroup.getNameCopy().trim().equals(name)) {
                return contactGroup;
            }
        }
        return null;
    }

    public ContactJabberImpl findContactById(String id) {
        ContactJabberImpl result;
        Iterator<ContactGroup> contactGroups = this.rootGroup.subgroups();
        String userId = StringUtils.parseBareAddress(id);
        while (contactGroups.hasNext()) {
            result = ((ContactGroupJabberImpl) contactGroups.next()).findContact(userId);
            if (result != null) {
                return result;
            }
        }
        ContactGroupJabberImpl volatileGroup = getNonPersistentGroup();
        if (volatileGroup != null) {
            result = volatileGroup.findContact(id);
            if (result != null) {
                return result;
            }
        }
        return this.rootGroup.findContact(userId);
    }

    public ContactGroup findContactGroup(ContactJabberImpl child) {
        Iterator<ContactGroup> contactGroups = this.rootGroup.subgroups();
        String contactAddress = child.getAddress();
        while (contactGroups.hasNext()) {
            ContactGroupJabberImpl contactGroup = (ContactGroupJabberImpl) contactGroups.next();
            if (contactGroup.findContact(contactAddress) != null) {
                return contactGroup;
            }
        }
        if (this.rootGroup.findContact(contactAddress) != null) {
            return this.rootGroup;
        }
        return null;
    }

    public void addContact(String id) throws OperationFailedException {
        addContact(null, id);
    }

    public void addContact(ContactGroup parent, String id) throws OperationFailedException {
        if (logger.isTraceEnabled()) {
            logger.trace("Adding contact " + id + " to parent=" + parent);
        }
        String completeID = parseAddressString(id);
        ContactJabberImpl existingContact = findContactById(completeID);
        if (existingContact == null || !existingContact.isPersistent()) {
            String[] parentNames = null;
            if (parent != null) {
                try {
                    parentNames = new String[]{parent.getGroupName()};
                } catch (XMPPException ex) {
                    String errTxt = "Error adding new jabber entry";
                    logger.error(errTxt, ex);
                    int errorCode = 4;
                    XMPPError err = ex.getXMPPError();
                    if (err != null) {
                        if (err.getCode() > Response.BAD_REQUEST && err.getCode() < 500) {
                            errorCode = Response.FORBIDDEN;
                        } else if (err.getCode() > 500) {
                            errorCode = 500;
                        }
                        errTxt = err.getCondition();
                    }
                    throw new OperationFailedException(errTxt, errorCode, ex);
                }
            }
            SmackConfiguration.setPacketReplyTimeout(ProtocolProviderServiceJabberImpl.SMACK_PACKET_REPLY_TIMEOUT);
            this.roster.createEntry(completeID, completeID, parentNames);
            SmackConfiguration.setPacketReplyTimeout(5000);
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Contact " + completeID + " already exists in group " + findContactGroup(existingContact));
        }
        throw new OperationFailedException("Contact " + completeID + " already exists.", 5);
    }

    /* access modifiers changed from: 0000 */
    public ContactJabberImpl createVolatileContact(String id, boolean isPrivateMessagingContact) {
        VolatileContactJabberImpl newVolatileContact = new VolatileContactJabberImpl(id, this, isPrivateMessagingContact);
        ContactGroupJabberImpl theVolatileGroup = getNonPersistentGroup();
        if (theVolatileGroup == null) {
            theVolatileGroup = new VolatileContactGroupJabberImpl(JabberActivator.getResources().getI18NString("service.gui.NOT_IN_CONTACT_LIST_GROUP_NAME"), this);
            theVolatileGroup.addContact(newVolatileContact);
            this.rootGroup.addSubGroup(theVolatileGroup);
            fireGroupEvent(theVolatileGroup, 1);
        } else {
            theVolatileGroup.addContact(newVolatileContact);
            fireContactAdded(theVolatileGroup, newVolatileContact);
        }
        return newVolatileContact;
    }

    public boolean isPrivateMessagingContact(String contactAddress) {
        ContactGroupJabberImpl theVolatileGroup = getNonPersistentGroup();
        if (theVolatileGroup == null) {
            return false;
        }
        ContactJabberImpl contact = theVolatileGroup.findContact(contactAddress);
        if (contact == null || !(contact instanceof VolatileContactJabberImpl)) {
            return false;
        }
        return ((VolatileContactJabberImpl) contact).isPrivateMessagingContact();
    }

    /* access modifiers changed from: 0000 */
    public ContactJabberImpl createUnresolvedContact(ContactGroup parentGroup, String id) {
        ContactJabberImpl newUnresolvedContact = new ContactJabberImpl(id, this, false);
        if (parentGroup instanceof ContactGroupJabberImpl) {
            ((ContactGroupJabberImpl) parentGroup).addContact(newUnresolvedContact);
        } else if (parentGroup instanceof RootContactGroupJabberImpl) {
            ((RootContactGroupJabberImpl) parentGroup).addContact(newUnresolvedContact);
        }
        fireContactAdded(parentGroup, newUnresolvedContact);
        return newUnresolvedContact;
    }

    /* access modifiers changed from: 0000 */
    public ContactGroupJabberImpl createUnresolvedContactGroup(String groupName) {
        ContactGroupJabberImpl newUnresolvedGroup = new ContactGroupJabberImpl(groupName, this);
        this.rootGroup.addSubGroup(newUnresolvedGroup);
        fireGroupEvent(newUnresolvedGroup, 1);
        return newUnresolvedGroup;
    }

    public void createGroup(String groupName) throws OperationFailedException {
        if (logger.isTraceEnabled()) {
            logger.trace("Creating group: " + groupName);
        }
        ContactGroupJabberImpl existingGroup = findContactGroup(groupName);
        if (existingGroup == null || !existingGroup.isPersistent()) {
            ContactGroupJabberImpl newGroup = new ContactGroupJabberImpl(this.roster.createGroup(groupName), new ArrayList().iterator(), this, true);
            this.rootGroup.addSubGroup(newGroup);
            fireGroupEvent(newGroup, 1);
            if (logger.isTraceEnabled()) {
                logger.trace("Group " + groupName + " created.");
                return;
            }
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("ContactGroup " + groupName + " already exists.");
        }
        throw new OperationFailedException("ContactGroup " + groupName + " already exists.", 6);
    }

    public void removeGroup(ContactGroupJabberImpl groupToRemove) throws OperationFailedException {
        try {
            Vector<Contact> localCopy = new Vector();
            Iterator<Contact> iter = groupToRemove.contacts();
            while (iter.hasNext()) {
                localCopy.add(iter.next());
            }
            iter = localCopy.iterator();
            while (iter.hasNext()) {
                ContactJabberImpl item = (ContactJabberImpl) iter.next();
                if (item.isPersistent()) {
                    this.roster.removeEntry(item.getSourceEntry());
                }
            }
        } catch (XMPPException ex) {
            logger.error("Error removing group", ex);
            throw new OperationFailedException(ex.getMessage(), 1, ex);
        }
    }

    /* access modifiers changed from: 0000 */
    public void removeContact(ContactJabberImpl contactToRemove) throws OperationFailedException {
        if (contactToRemove instanceof VolatileContactJabberImpl) {
            contactDeleted(contactToRemove);
            return;
        }
        try {
            RosterEntry entry = contactToRemove.getSourceEntry();
            if (entry != null) {
                this.roster.removeEntry(entry);
            }
        } catch (XMPPException ex) {
            String errTxt = "Error removing contact";
            logger.error(errTxt, ex);
            int errorCode = 4;
            XMPPError err = ex.getXMPPError();
            if (err != null) {
                if (err.getCode() > Response.BAD_REQUEST && err.getCode() < 500) {
                    errorCode = Response.FORBIDDEN;
                } else if (err.getCode() > 500) {
                    errorCode = 500;
                }
                errTxt = err.getCondition();
            }
            throw new OperationFailedException(errTxt, errorCode, ex);
        }
    }

    public void renameGroup(ContactGroupJabberImpl groupToRename, String newName) {
        groupToRename.getSourceGroup().setName(newName);
        groupToRename.setNameCopy(newName);
    }

    public void moveContact(ContactJabberImpl contact, ContactGroupJabberImpl newParent) throws OperationFailedException {
        if (contact.isPersistent()) {
            try {
                SmackConfiguration.setPacketReplyTimeout(ProtocolProviderServiceJabberImpl.SMACK_PACKET_REPLY_TIMEOUT);
                this.roster.createEntry(contact.getSourceEntry().getUser(), contact.getDisplayName(), new String[]{newParent.getGroupName()});
                SmackConfiguration.setPacketReplyTimeout(5000);
                newParent.addContact(contact);
                return;
            } catch (XMPPException ex) {
                logger.error("Cannot move contact! ", ex);
                throw new OperationFailedException(ex.getMessage(), 1, ex);
            }
        }
        String contactAddress;
        if ((contact instanceof VolatileContactJabberImpl) && ((VolatileContactJabberImpl) contact).isPrivateMessagingContact()) {
            contactAddress = contact.getPersistableAddress();
        } else {
            contactAddress = contact.getAddress();
        }
        try {
            addContact(newParent, contactAddress);
        } catch (OperationFailedException ex2) {
            logger.error("Cannot move contact! ", ex2);
            throw new OperationFailedException(ex2.getMessage(), 1, ex2);
        }
    }

    /* access modifiers changed from: 0000 */
    public void init(ContactChangesListener presenceChangeListener) {
        this.roster = this.jabberProvider.getConnection().getRoster();
        presenceChangeListener.storeEvents();
        this.roster.addRosterListener(presenceChangeListener);
        this.roster.setSubscriptionMode(SubscriptionMode.manual);
        initRoster();
        presenceChangeListener.processStoredEvents();
        this.rosterChangeListener = new ChangeListener();
        this.roster.addRosterListener(this.rosterChangeListener);
    }

    /* access modifiers changed from: 0000 */
    public void cleanup() {
        if (this.imageRetriever != null) {
            this.imageRetriever.quit();
            this.imageRetriever = null;
        }
        if (this.roster != null) {
            this.roster.removeRosterListener(this.rosterChangeListener);
        }
        this.rosterChangeListener = null;
        this.roster = null;
    }

    private void initRoster() {
        ContactJabberImpl contact;
        ContactGroupJabberImpl group;
        if (this.roster.getUnfiledEntryCount() > 0) {
            for (RosterEntry item : this.roster.getUnfiledEntries()) {
                contact = findContactById(item.getUser());
                if (isEntryDisplayable(item)) {
                    if (contact == null) {
                        contact = new ContactJabberImpl(item, this, true, true);
                        this.rootGroup.addContact(contact);
                        fireContactAdded(this.rootGroup, contact);
                    } else {
                        contact.setResolved(item);
                        fireContactResolved(this.rootGroup, contact);
                    }
                    try {
                        this.parentOperationSet.firePresenceStatusChanged(this.roster.getPresence(item.getUser()));
                    } catch (Throwable t) {
                        logger.error("Error processing presence", t);
                    }
                } else if (contact != null) {
                    ContactGroup parent = contact.getParentContactGroup();
                    if (parent instanceof RootContactGroupJabberImpl) {
                        ((RootContactGroupJabberImpl) parent).removeContact(contact);
                    } else {
                        ((ContactGroupJabberImpl) parent).removeContact(contact);
                    }
                    fireContactRemoved(parent, contact);
                }
            }
        }
        Iterator<Contact> iter = this.rootGroup.contacts();
        List<ContactJabberImpl> contactsToRemove = new ArrayList();
        while (iter.hasNext()) {
            contact = (ContactJabberImpl) iter.next();
            if (!contact.isResolved()) {
                contactsToRemove.add(contact);
            }
        }
        for (ContactJabberImpl contact2 : contactsToRemove) {
            this.rootGroup.removeContact(contact2);
            fireContactRemoved(this.rootGroup, contact2);
        }
        contactsToRemove.clear();
        for (RosterGroup item2 : this.roster.getGroups()) {
            group = findContactGroup(item2.getName());
            if (group == null) {
                ContactGroupJabberImpl newGroup = new ContactGroupJabberImpl(item2, item2.getEntries().iterator(), this, true);
                this.rootGroup.addSubGroup(newGroup);
                fireGroupEvent(newGroup, 1);
                if (this.roster != null) {
                    Iterator<Contact> cIter = newGroup.contacts();
                    while (cIter.hasNext()) {
                        this.parentOperationSet.firePresenceStatusChanged(this.roster.getPresence(((Contact) cIter.next()).getAddress()));
                    }
                }
            } else {
                group.setResolved(item2);
                fireGroupEvent(group, 4);
            }
        }
        Iterator<ContactGroup> iterGroups = this.rootGroup.subgroups();
        List<ContactGroupJabberImpl> groupsToRemove = new ArrayList();
        while (iterGroups.hasNext()) {
            group = (ContactGroupJabberImpl) iterGroups.next();
            if (!group.isResolved()) {
                groupsToRemove.add(group);
            }
            Iterator<Contact> iterContacts = group.contacts();
            while (iterContacts.hasNext()) {
                contact2 = (ContactJabberImpl) iterContacts.next();
                if (!contact2.isResolved()) {
                    contactsToRemove.add(contact2);
                }
            }
            for (ContactJabberImpl contact22 : contactsToRemove) {
                group.removeContact(contact22);
                fireContactRemoved(group, contact22);
            }
            contactsToRemove.clear();
        }
        for (ContactGroupJabberImpl group2 : groupsToRemove) {
            this.rootGroup.removeSubGroup(group2);
            fireGroupEvent(group2, 2);
        }
    }

    /* access modifiers changed from: 0000 */
    public ContactGroupJabberImpl getNonPersistentGroup() {
        for (int i = 0; i < getRootGroup().countSubgroups(); i++) {
            ContactGroupJabberImpl gr = (ContactGroupJabberImpl) getRootGroup().getGroup(i);
            if (!gr.isPersistent()) {
                return gr;
            }
        }
        return null;
    }

    /* access modifiers changed from: 0000 */
    public void fireContactAdded(ContactGroup parentGroup, ContactJabberImpl contact) {
        if (this.parentOperationSet != null) {
            if (this.roster != null) {
                this.parentOperationSet.firePresenceStatusChanged(this.roster.getPresence(contact.getAddress()));
            }
            this.parentOperationSet.fireSubscriptionEvent(contact, parentGroup, 1);
        } else if (logger.isDebugEnabled()) {
            logger.debug("No presence op. set available. Bailing out.");
        }
    }

    /* access modifiers changed from: 0000 */
    public void fireContactResolved(ContactGroup parentGroup, ContactJabberImpl contact) {
        if (this.parentOperationSet != null) {
            if (this.roster != null) {
                this.parentOperationSet.firePresenceStatusChanged(this.roster.getPresence(contact.getAddress()));
            }
            this.parentOperationSet.fireSubscriptionEvent(contact, parentGroup, 4);
        } else if (logger.isDebugEnabled()) {
            logger.debug("No presence op. set available. Bailing out.");
        }
    }

    /* access modifiers changed from: protected */
    public void addContactForImageUpdate(ContactJabberImpl c) {
        if (this.imageRetriever == null) {
            this.imageRetriever = new ImageRetriever();
            this.imageRetriever.start();
        }
        this.imageRetriever.addContact(c);
    }

    static boolean isEntryDisplayable(RosterEntry entry) {
        if (entry.getType() == ItemType.both || entry.getType() == ItemType.to) {
            return true;
        }
        if (entry.getType() == ItemType.none || entry.getType() == ItemType.from) {
            if (ItemStatus.SUBSCRIPTION_PENDING.equals(entry.getStatus())) {
                return true;
            }
            if (entry.getGroups() != null && entry.getGroups().size() > 0) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void contactDeleted(ContactJabberImpl contact) {
        ContactGroup group = findContactGroup(contact);
        if (group == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Could not find ParentGroup for deleted entry:" + contact.getAddress());
            }
        } else if (group instanceof ContactGroupJabberImpl) {
            ContactGroupJabberImpl groupImpl = (ContactGroupJabberImpl) group;
            groupImpl.removeContact(contact);
            if (groupImpl.countContacts() == 0) {
                this.rootGroup.removeSubGroup(groupImpl);
                fireContactRemoved(groupImpl, contact);
                fireGroupEvent(groupImpl, 2);
                return;
            }
            fireContactRemoved(groupImpl, contact);
        } else if (group instanceof RootContactGroupJabberImpl) {
            this.rootGroup.removeContact(contact);
            fireContactRemoved(this.rootGroup, contact);
        }
    }

    /* access modifiers changed from: private */
    public byte[] searchForCustomAvatar(String address) {
        try {
            ServiceReference[] refs = JabberActivator.bundleContext.getServiceReferences(CustomAvatarService.class.getName(), null);
            if (refs == null) {
                return null;
            }
            for (ServiceReference r : refs) {
                byte[] res = ((CustomAvatarService) JabberActivator.bundleContext.getService(r)).getAvatar(address);
                if (res != null) {
                    return res;
                }
            }
            return null;
        } catch (Throwable th) {
        }
    }

    private String parseAddressString(String id) {
        if (id.indexOf(Separators.AT) >= 0) {
            return id;
        }
        String serverPart;
        AccountID accountID = this.jabberProvider.getAccountID();
        String userID = accountID.getUserID();
        int atIndex = userID.indexOf(64);
        if (atIndex > 0) {
            serverPart = userID.substring(atIndex + 1);
        } else {
            serverPart = accountID.getService();
        }
        return id + Separators.AT + serverPart;
    }
}
