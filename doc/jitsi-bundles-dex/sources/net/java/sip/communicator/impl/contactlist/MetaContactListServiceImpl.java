package net.java.sip.communicator.impl.contactlist;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import net.java.sip.communicator.service.contactlist.MetaContact;
import net.java.sip.communicator.service.contactlist.MetaContactGroup;
import net.java.sip.communicator.service.contactlist.MetaContactListException;
import net.java.sip.communicator.service.contactlist.MetaContactListService;
import net.java.sip.communicator.service.contactlist.event.MetaContactAvatarUpdateEvent;
import net.java.sip.communicator.service.contactlist.event.MetaContactEvent;
import net.java.sip.communicator.service.contactlist.event.MetaContactGroupEvent;
import net.java.sip.communicator.service.contactlist.event.MetaContactListListener;
import net.java.sip.communicator.service.contactlist.event.MetaContactModifiedEvent;
import net.java.sip.communicator.service.contactlist.event.MetaContactMovedEvent;
import net.java.sip.communicator.service.contactlist.event.MetaContactPropertyChangeEvent;
import net.java.sip.communicator.service.contactlist.event.MetaContactRenamedEvent;
import net.java.sip.communicator.service.contactlist.event.ProtoContactEvent;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ContactGroup;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetContactCapabilities;
import net.java.sip.communicator.service.protocol.OperationSetMultiUserChat;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.OperationSetPresence;
import net.java.sip.communicator.service.protocol.ProtocolProviderFactory;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.service.protocol.event.ContactCapabilitiesEvent;
import net.java.sip.communicator.service.protocol.event.ContactCapabilitiesListener;
import net.java.sip.communicator.service.protocol.event.ContactPresenceStatusChangeEvent;
import net.java.sip.communicator.service.protocol.event.ContactPresenceStatusListener;
import net.java.sip.communicator.service.protocol.event.ContactPropertyChangeEvent;
import net.java.sip.communicator.service.protocol.event.ServerStoredGroupEvent;
import net.java.sip.communicator.service.protocol.event.ServerStoredGroupListener;
import net.java.sip.communicator.service.protocol.event.SubscriptionEvent;
import net.java.sip.communicator.service.protocol.event.SubscriptionListener;
import net.java.sip.communicator.service.protocol.event.SubscriptionMovedEvent;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.util.xml.XMLException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class MetaContactListServiceImpl implements MetaContactListService, ServiceListener, ContactPresenceStatusListener, ContactCapabilitiesListener {
    public static final int CONTACT_LIST_MODIFICATION_TIMEOUT = 10000;
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(MetaContactListServiceImpl.class);
    private BundleContext bundleContext = null;
    private final ContactListGroupListener clGroupEventHandler = new ContactListGroupListener();
    private final ContactListSubscriptionListener clSubscriptionEventHandler = new ContactListSubscriptionListener();
    private final Hashtable<String, List<ProtocolProviderService>> contactEventIgnoreList = new Hashtable();
    private final Map<String, ProtocolProviderService> currentlyInstalledProviders = new Hashtable();
    private final Hashtable<String, List<ProtocolProviderService>> groupEventIgnoreList = new Hashtable();
    private final List<MetaContactListListener> metaContactListListeners = new Vector();
    final MetaContactGroupImpl rootMetaGroup = new MetaContactGroupImpl(this, ContactlistActivator.getResources().getI18NString("service.gui.CONTACTS"), "RootMetaContactGroup");
    private final MclStorageManager storageManager = new MclStorageManager();

    private static class BlockingGroupEventRetriever implements ServerStoredGroupListener {
        public ServerStoredGroupEvent evt = null;
        private final String groupName;

        BlockingGroupEventRetriever(String groupName) {
            this.groupName = groupName;
        }

        public synchronized void groupCreated(ServerStoredGroupEvent event) {
            if (event.getSourceGroup().getGroupName().equals(this.groupName)) {
                this.evt = event;
                notifyAll();
            }
        }

        public void groupRemoved(ServerStoredGroupEvent event) {
        }

        public void groupNameChanged(ServerStoredGroupEvent event) {
        }

        public void groupResolved(ServerStoredGroupEvent event) {
        }

        public synchronized void waitForEvent(long millis) {
            if (this.evt == null) {
                try {
                    wait(millis);
                } catch (InterruptedException ex) {
                    MetaContactListServiceImpl.logger.error("Interrupted while waiting for group creation", ex);
                }
            }
            return;
        }
    }

    private static class BlockingSubscriptionEventRetriever implements SubscriptionListener, ServerStoredGroupListener {
        public EventObject evt = null;
        public Contact sourceContact = null;
        private final String subscriptionAddress;

        public void groupResolved(ServerStoredGroupEvent event) {
        }

        public void groupRemoved(ServerStoredGroupEvent event) {
        }

        public void groupNameChanged(ServerStoredGroupEvent event) {
        }

        BlockingSubscriptionEventRetriever(String subscriptionAddress) {
            this.subscriptionAddress = subscriptionAddress;
        }

        public synchronized void groupCreated(ServerStoredGroupEvent event) {
            Contact contact = event.getSourceGroup().getContact(this.subscriptionAddress);
            if (contact != null) {
                this.evt = event;
                this.sourceContact = contact;
                notifyAll();
            }
        }

        public synchronized void subscriptionCreated(SubscriptionEvent event) {
            if (event.getSourceContact().getAddress().equals(this.subscriptionAddress) || event.getSourceContact().equals(this.subscriptionAddress)) {
                this.evt = event;
                this.sourceContact = event.getSourceContact();
                notifyAll();
            }
        }

        public void subscriptionRemoved(SubscriptionEvent event) {
        }

        public synchronized void subscriptionFailed(SubscriptionEvent event) {
            if (event.getSourceContact().getAddress().equals(this.subscriptionAddress)) {
                this.evt = event;
                this.sourceContact = event.getSourceContact();
                notifyAll();
            }
        }

        public void subscriptionMoved(SubscriptionMovedEvent event) {
        }

        public void subscriptionResolved(SubscriptionEvent event) {
        }

        public void contactModified(ContactPropertyChangeEvent event) {
        }

        public synchronized void waitForEvent(long millis) {
            if (this.evt == null) {
                try {
                    wait(millis);
                } catch (InterruptedException ex) {
                    MetaContactListServiceImpl.logger.error("Interrupted while waiting for contact creation", ex);
                }
            }
            return;
        }
    }

    private class ContactListGroupListener implements ServerStoredGroupListener {
        private ContactListGroupListener() {
        }

        private MetaContactGroup handleGroupCreatedEvent(MetaContactGroupImpl parent, ContactGroup group) {
            MetaContactGroupImpl newMetaGroup = (MetaContactGroupImpl) parent.getMetaContactSubgroup(group.getGroupName());
            if (newMetaGroup == null) {
                newMetaGroup = new MetaContactGroupImpl(MetaContactListServiceImpl.this, group.getGroupName());
                newMetaGroup.addProtoGroup(group);
                parent.addSubgroup(newMetaGroup);
            } else {
                newMetaGroup.addProtoGroup(group);
            }
            Iterator<ContactGroup> subgroups = group.subgroups();
            while (subgroups.hasNext()) {
                handleGroupCreatedEvent(newMetaGroup, (ContactGroup) subgroups.next());
            }
            Iterator<Contact> contactsIter = group.contacts();
            while (contactsIter.hasNext()) {
                Contact contact = (Contact) contactsIter.next();
                MetaContactImpl newMetaContact = new MetaContactImpl();
                newMetaContact.addProtoContact(contact);
                newMetaContact.setDisplayName(contact.getDisplayName());
                newMetaGroup.addMetaContact(newMetaContact);
            }
            return newMetaGroup;
        }

        public void groupCreated(ServerStoredGroupEvent evt) {
            if (MetaContactListServiceImpl.logger.isTraceEnabled()) {
                MetaContactListServiceImpl.logger.trace("ContactGroup created: " + evt);
            }
            if (!MetaContactListServiceImpl.this.isGroupInEventIgnoreList(evt.getSourceGroup().getGroupName(), evt.getSourceProvider())) {
                MetaContactGroupImpl parentMetaGroup = (MetaContactGroupImpl) MetaContactListServiceImpl.this.findMetaContactGroupByContactGroup(evt.getParentGroup());
                if (parentMetaGroup == null) {
                    MetaContactListServiceImpl.logger.error("Failed to identify a parent where group " + evt.getSourceGroup().getGroupName() + "should be placed.");
                }
                boolean isExisting = parentMetaGroup.getMetaContactSubgroup(evt.getSourceGroup().getGroupName()) != null;
                MetaContactGroup newMetaGroup = handleGroupCreatedEvent(parentMetaGroup, evt.getSourceGroup());
                if (newMetaGroup.countContactGroups() > 1 || isExisting) {
                    MetaContactListServiceImpl.this.fireMetaContactGroupEvent(newMetaGroup, evt.getSourceProvider(), evt.getSourceGroup(), 6);
                } else {
                    MetaContactListServiceImpl.this.fireMetaContactGroupEvent(newMetaGroup, evt.getSourceProvider(), evt.getSourceGroup(), 1);
                }
            }
        }

        public void groupResolved(ServerStoredGroupEvent evt) {
        }

        public void groupRemoved(ServerStoredGroupEvent evt) {
            if (MetaContactListServiceImpl.logger.isTraceEnabled()) {
                MetaContactListServiceImpl.logger.trace("ContactGroup removed: " + evt);
            }
            MetaContactGroupImpl metaContactGroup = (MetaContactGroupImpl) MetaContactListServiceImpl.this.findMetaContactGroupByContactGroup(evt.getSourceGroup());
            if (metaContactGroup == null) {
                MetaContactListServiceImpl.logger.error("Received a RemovedGroup event for an orphan grp: " + evt.getSourceGroup());
                return;
            }
            MetaContactListServiceImpl.this.removeContactGroupFromMetaContactGroup(metaContactGroup, evt.getSourceGroup(), evt.getSourceProvider());
            if (metaContactGroup.countContactGroups() == 0) {
                MetaContactListServiceImpl.this.removeMetaContactGroup(metaContactGroup);
            }
        }

        public void groupNameChanged(ServerStoredGroupEvent evt) {
            if (MetaContactListServiceImpl.logger.isTraceEnabled()) {
                MetaContactListServiceImpl.logger.trace("ContactGroup renamed: " + evt);
            }
            MetaContactGroup metaContactGroup = MetaContactListServiceImpl.this.findMetaContactGroupByContactGroup(evt.getSourceGroup());
            if (metaContactGroup.countContactGroups() == 1) {
                ((MetaContactGroupImpl) metaContactGroup).setGroupName(evt.getSourceGroup().getGroupName());
            }
            MetaContactListServiceImpl.this.fireMetaContactGroupEvent(metaContactGroup, evt.getSourceProvider(), evt.getSourceGroup(), 5);
        }
    }

    private class ContactListSubscriptionListener implements SubscriptionListener {
        private ContactListSubscriptionListener() {
        }

        public void subscriptionCreated(SubscriptionEvent evt) {
            if (MetaContactListServiceImpl.logger.isTraceEnabled()) {
                MetaContactListServiceImpl.logger.trace("Subscription created: " + evt);
            }
            if (!MetaContactListServiceImpl.this.isContactInEventIgnoreList(evt.getSourceContact(), evt.getSourceProvider())) {
                MetaContactGroupImpl parentGroup = (MetaContactGroupImpl) MetaContactListServiceImpl.this.findMetaContactGroupByContactGroup(evt.getParentGroup());
                if (parentGroup == null) {
                    MetaContactListServiceImpl.logger.error("Received a subscription for a group that we hadn't seen before! ");
                    return;
                }
                MetaContactImpl newMetaContact = new MetaContactImpl();
                newMetaContact.addProtoContact(evt.getSourceContact());
                newMetaContact.setDisplayName(evt.getSourceContact().getDisplayName());
                parentGroup.addMetaContact(newMetaContact);
                MetaContactListServiceImpl.this.fireMetaContactEvent(newMetaContact, parentGroup, 1);
                newMetaContact.getAvatar();
            }
        }

        /* JADX WARNING: Missing block: B:28:0x00b5, code skipped:
            if (r3 == null) goto L_?;
     */
        /* JADX WARNING: Missing block: B:29:0x00b7, code skipped:
            net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl.access$400(r12.this$0, r3, r5, 1);
            net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl.access$500(r12.this$0, r7, "ProtoContactMoved", r1, r3);
     */
        /* JADX WARNING: Missing block: B:41:?, code skipped:
            return;
     */
        /* JADX WARNING: Missing block: B:42:?, code skipped:
            return;
     */
        public void subscriptionMoved(net.java.sip.communicator.service.protocol.event.SubscriptionMovedEvent r13) {
            /*
            r12 = this;
            r11 = 1;
            r8 = net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl.logger;
            r8 = r8.isTraceEnabled();
            if (r8 == 0) goto L_0x0025;
        L_0x000b:
            r8 = net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl.logger;
            r9 = new java.lang.StringBuilder;
            r9.<init>();
            r10 = "Subscription moved: ";
            r9 = r9.append(r10);
            r9 = r9.append(r13);
            r9 = r9.toString();
            r8.trace(r9);
        L_0x0025:
            r7 = r13.getSourceContact();
            r8 = net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl.this;
            r9 = r13.getSourceProvider();
            r8 = r8.isContactInEventIgnoreList(r7, r9);
            if (r8 == 0) goto L_0x0036;
        L_0x0035:
            return;
        L_0x0036:
            r8 = net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl.this;
            r9 = r13.getOldParentGroup();
            r6 = r8.findMetaContactGroupByContactGroup(r9);
            r6 = (net.java.sip.communicator.impl.contactlist.MetaContactGroupImpl) r6;
            r8 = net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl.this;
            r9 = r13.getNewParentGroup();
            r5 = r8.findMetaContactGroupByContactGroup(r9);
            r5 = (net.java.sip.communicator.impl.contactlist.MetaContactGroupImpl) r5;
            if (r5 == 0) goto L_0x0052;
        L_0x0050:
            if (r6 != 0) goto L_0x005c;
        L_0x0052:
            r8 = net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl.logger;
            r9 = "Received a subscription for a group that we hadn't seen before! ";
            r8.error(r9);
            goto L_0x0035;
        L_0x005c:
            r8 = net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl.this;
            r1 = r8.findMetaContactByContact(r7);
            r1 = (net.java.sip.communicator.impl.contactlist.MetaContactImpl) r1;
            if (r1 != 0) goto L_0x0077;
        L_0x0066:
            r8 = net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl.logger;
            r9 = "Received a move event for a contact that is not in our contact list.";
            r10 = new java.lang.NullPointerException;
            r11 = "Received a move event for a contact that is not in our contact list.";
            r10.<init>(r11);
            r8.warn(r9, r10);
            goto L_0x0035;
        L_0x0077:
            r2 = r1.getParentMetaContactGroup();
            if (r2 == r5) goto L_0x0035;
        L_0x007d:
            r8 = r1.getContactCount();
            if (r8 != r11) goto L_0x0094;
        L_0x0083:
            r6.removeMetaContact(r1);
            r5.addMetaContact(r1);
            r8 = net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl.this;
            r9 = new net.java.sip.communicator.service.contactlist.event.MetaContactMovedEvent;
            r9.<init>(r1, r6, r5);
            r8.fireMetaContactEvent(r9);
            goto L_0x0035;
        L_0x0094:
            r3 = 0;
            monitor-enter(r7);
            r1.removeProtoContact(r7);	 Catch:{ all -> 0x00c5 }
            r8 = net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl.this;	 Catch:{ all -> 0x00c5 }
            r0 = r8.findMetaContactByContact(r7);	 Catch:{ all -> 0x00c5 }
            if (r0 != 0) goto L_0x00b4;
        L_0x00a1:
            r4 = new net.java.sip.communicator.impl.contactlist.MetaContactImpl;	 Catch:{ all -> 0x00c5 }
            r4.m124init();	 Catch:{ all -> 0x00c5 }
            r8 = r7.getDisplayName();	 Catch:{ all -> 0x00c8 }
            r4.setDisplayName(r8);	 Catch:{ all -> 0x00c8 }
            r5.addMetaContact(r4);	 Catch:{ all -> 0x00c8 }
            r4.addProtoContact(r7);	 Catch:{ all -> 0x00c8 }
            r3 = r4;
        L_0x00b4:
            monitor-exit(r7);	 Catch:{ all -> 0x00c5 }
            if (r3 == 0) goto L_0x0035;
        L_0x00b7:
            r8 = net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl.this;
            r8.fireMetaContactEvent(r3, r5, r11);
            r8 = net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl.this;
            r9 = "ProtoContactMoved";
            r8.fireProtoContactEvent(r7, r9, r1, r3);
            goto L_0x0035;
        L_0x00c5:
            r8 = move-exception;
        L_0x00c6:
            monitor-exit(r7);	 Catch:{ all -> 0x00c5 }
            throw r8;
        L_0x00c8:
            r8 = move-exception;
            r3 = r4;
            goto L_0x00c6;
            */
            throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl$ContactListSubscriptionListener.subscriptionMoved(net.java.sip.communicator.service.protocol.event.SubscriptionMovedEvent):void");
        }

        public void subscriptionFailed(SubscriptionEvent evt) {
            if (MetaContactListServiceImpl.logger.isTraceEnabled()) {
                MetaContactListServiceImpl.logger.trace("Subscription failed: " + evt);
            }
        }

        public void subscriptionResolved(SubscriptionEvent evt) {
            MetaContact mc = (MetaContactImpl) MetaContactListServiceImpl.this.findMetaContactByContact(evt.getSourceContact());
            if (mc != null) {
                mc.getAvatar();
                if (mc.getContactCount() == 1 && !mc.isDisplayNameUserDefined()) {
                    String oldDisplayName = mc.getDisplayName();
                    String newDisplayName = mc.getDefaultContact().getDisplayName();
                    if (newDisplayName != null && !newDisplayName.equals(oldDisplayName)) {
                        mc.setDisplayName(newDisplayName);
                        MetaContactListServiceImpl.this.fireMetaContactEvent(new MetaContactRenamedEvent(mc, oldDisplayName, newDisplayName));
                        MetaContactListServiceImpl.this.fireMetaContactGroupEvent(MetaContactListServiceImpl.this.findParentMetaContactGroup(mc), null, null, 4);
                    }
                }
            }
        }

        public void contactModified(ContactPropertyChangeEvent evt) {
            MetaContactImpl mc = (MetaContactImpl) MetaContactListServiceImpl.this.findMetaContactByContact(evt.getSourceContact());
            if ("DisplayName".equals(evt.getPropertyName())) {
                if (evt.getOldValue() == null || !evt.getOldValue().equals(mc.getDisplayName())) {
                    MetaContactListServiceImpl.this.fireProtoContactEvent(evt.getSourceContact(), "ProtoContactModified", mc, mc);
                } else {
                    MetaContactListServiceImpl.this.renameMetaContact(mc, (String) evt.getNewValue(), false);
                }
            } else if ("Image".equals(evt.getPropertyName()) && evt.getNewValue() != null) {
                MetaContactListServiceImpl.this.changeMetaContactAvatar(mc, evt.getSourceContact(), (byte[]) evt.getNewValue());
            } else if ("PersistentData".equals(evt.getPropertyName()) || "DisplayDetails".equals(evt.getPropertyName())) {
                MetaContactListServiceImpl.this.fireProtoContactEvent(evt.getSourceContact(), "ProtoContactModified", mc, mc);
            }
        }

        public void subscriptionRemoved(SubscriptionEvent evt) {
            if (MetaContactListServiceImpl.logger.isTraceEnabled()) {
                MetaContactListServiceImpl.logger.trace("Subscription removed: " + evt);
            }
            MetaContactImpl metaContact = (MetaContactImpl) MetaContactListServiceImpl.this.findMetaContactByContact(evt.getSourceContact());
            MetaContactGroupImpl metaContactGroup = (MetaContactGroupImpl) MetaContactListServiceImpl.this.findMetaContactGroupByContactGroup(evt.getParentGroup());
            metaContact.removeProtoContact(evt.getSourceContact());
            if (metaContact.getContactCount() == 0) {
                metaContactGroup.removeMetaContact(metaContact);
                MetaContactListServiceImpl.this.fireMetaContactEvent(metaContact, metaContactGroup, 2);
                return;
            }
            MetaContactListServiceImpl.this.fireProtoContactEvent(evt.getSourceContact(), "ProtoContactRemoved", metaContact, null);
        }
    }

    public void start(BundleContext bc) {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting the meta contact list implementation.");
        }
        this.bundleContext = bc;
        try {
            this.storageManager.start(this.bundleContext, this);
        } catch (Exception exc) {
            logger.error("Failed loading the stored contact list.", exc);
        }
        bc.addServiceListener(this);
        try {
            ServiceReference[] protocolProviderRefs = bc.getServiceReferences(ProtocolProviderService.class.getName(), null);
            if (protocolProviderRefs != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found " + protocolProviderRefs.length + " already installed providers.");
                }
                for (ServiceReference providerRef : protocolProviderRefs) {
                    handleProviderAdded((ProtocolProviderService) bc.getService(providerRef));
                }
            }
        } catch (InvalidSyntaxException ex) {
            logger.error("Error while retrieving service refs", ex);
        }
    }

    public void stop(BundleContext bc) {
        this.storageManager.storeContactListAndStopStorageManager();
        bc.removeServiceListener(this);
        for (ProtocolProviderService pp : this.currentlyInstalledProviders.values()) {
            OperationSetPersistentPresence opSetPersPresence = (OperationSetPersistentPresence) pp.getOperationSet(OperationSetPersistentPresence.class);
            if (opSetPersPresence != null) {
                opSetPersPresence.removeContactPresenceStatusListener(this);
                opSetPersPresence.removeSubscriptionListener(this.clSubscriptionEventHandler);
                opSetPersPresence.removeServerStoredGroupChangeListener(this.clGroupEventHandler);
            } else {
                OperationSetPresence opSetPresence = (OperationSetPresence) pp.getOperationSet(OperationSetPresence.class);
                if (opSetPresence != null) {
                    opSetPresence.removeContactPresenceStatusListener(this);
                    opSetPresence.removeSubscriptionListener(this.clSubscriptionEventHandler);
                }
            }
        }
        this.currentlyInstalledProviders.clear();
        this.storageManager.stop();
    }

    public void addMetaContactListListener(MetaContactListListener listener) {
        synchronized (this.metaContactListListeners) {
            if (!this.metaContactListListeners.contains(listener)) {
                this.metaContactListListeners.add(listener);
            }
        }
    }

    public void addNewContactToMetaContact(ProtocolProviderService provider, MetaContact metaContact, String contactID) throws MetaContactListException {
        addNewContactToMetaContact(provider, metaContact, contactID, true);
    }

    public void addNewContactToMetaContact(ProtocolProviderService provider, MetaContact metaContact, String contactID, boolean fireEvent) throws MetaContactListException {
        MetaContactGroup parentMetaGroup = findParentMetaContactGroup(metaContact);
        if (parentMetaGroup == null) {
            throw new MetaContactListException("orphan Contact: " + metaContact, null, 2);
        }
        addNewContactToMetaContact(provider, parentMetaGroup, metaContact, contactID, fireEvent);
    }

    private void addNewContactToMetaContact(ProtocolProviderService provider, MetaContactGroup parentMetaGroup, MetaContact metaContact, String contactID, boolean fireEvent) throws MetaContactListException {
        OperationSetPersistentPresence opSetPersPresence = (OperationSetPersistentPresence) provider.getOperationSet(OperationSetPersistentPresence.class);
        if (opSetPersPresence != null) {
            if (metaContact instanceof MetaContactImpl) {
                ContactGroup parentProtoGroup = resolveProtoPath(provider, (MetaContactGroupImpl) parentMetaGroup);
                if (parentProtoGroup == null) {
                    throw new MetaContactListException("Could not obtain proto group parent for " + metaContact, null, 2);
                }
                BlockingSubscriptionEventRetriever evtRetriever = new BlockingSubscriptionEventRetriever(contactID);
                addContactToEventIgnoreList(contactID, provider);
                opSetPersPresence.addSubscriptionListener(evtRetriever);
                opSetPersPresence.addServerStoredGroupChangeListener(evtRetriever);
                try {
                    if (parentMetaGroup.equals(this.rootMetaGroup)) {
                        opSetPersPresence.subscribe(contactID);
                    } else {
                        opSetPersPresence.subscribe(parentProtoGroup, contactID);
                    }
                    evtRetriever.waitForEvent(10000);
                    removeContactFromEventIgnoreList(contactID, provider);
                    opSetPersPresence.removeSubscriptionListener(evtRetriever);
                    if (evtRetriever.evt == null) {
                        throw new MetaContactListException("Failed to create a contact with address: " + contactID, null, 2);
                    } else if ((evtRetriever.evt instanceof SubscriptionEvent) && ((SubscriptionEvent) evtRetriever.evt).getEventID() == 3) {
                        throw new MetaContactListException("Failed to create a contact with address: " + contactID + Separators.SP + ((SubscriptionEvent) evtRetriever.evt).getErrorReason(), null, 5);
                    } else {
                        ((MetaContactImpl) metaContact).addProtoContact(evtRetriever.sourceContact);
                        if (fireEvent) {
                            fireProtoContactEvent(evtRetriever.sourceContact, "ProtoContactAdded", null, metaContact);
                        }
                        ((MetaContactGroupImpl) parentMetaGroup).addMetaContact((MetaContactImpl) metaContact);
                    }
                } catch (OperationFailedException ex) {
                    if (ex.getErrorCode() == 5) {
                        throw new MetaContactListException("failed to create contact " + contactID, ex, 3);
                    } else if (ex.getErrorCode() == 18) {
                        throw new MetaContactListException("failed to create contact " + contactID, ex, 8);
                    } else {
                        throw new MetaContactListException("failed to create contact " + contactID, ex, 2);
                    }
                } catch (Exception ex2) {
                    throw new MetaContactListException("failed to create contact " + contactID, ex2, 2);
                } catch (Throwable th) {
                    removeContactFromEventIgnoreList(contactID, provider);
                    opSetPersPresence.removeSubscriptionListener(evtRetriever);
                }
            } else {
                throw new IllegalArgumentException(metaContact + " is not an instance of MetaContactImpl");
            }
        }
    }

    private ContactGroup resolveProtoPath(ProtocolProviderService protoProvider, MetaContactGroupImpl metaGroup) {
        Iterator<ContactGroup> contactGroupsForProv = metaGroup.getContactGroupsForProvider(protoProvider);
        if (contactGroupsForProv.hasNext()) {
            return (ContactGroup) contactGroupsForProv.next();
        }
        MetaContactGroupImpl parentMetaGroup = (MetaContactGroupImpl) findParentMetaContactGroup((MetaContactGroup) metaGroup);
        if (parentMetaGroup == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Resolve failed at group" + metaGroup);
            }
            throw new NullPointerException("Internal Error. Orphan group.");
        }
        OperationSetPersistentPresence opSetPersPresence = (OperationSetPersistentPresence) protoProvider.getOperationSet(OperationSetPersistentPresence.class);
        if (opSetPersPresence == null) {
            return null;
        }
        ContactGroup parentProtoGroup;
        if (parentMetaGroup.getParentMetaContactGroup() == null) {
            parentProtoGroup = opSetPersPresence.getServerStoredContactListRoot();
        } else {
            parentProtoGroup = resolveProtoPath(protoProvider, parentMetaGroup);
        }
        BlockingGroupEventRetriever evtRetriever = new BlockingGroupEventRetriever(metaGroup.getGroupName());
        opSetPersPresence.addServerStoredGroupChangeListener(evtRetriever);
        addGroupToEventIgnoreList(metaGroup.getGroupName(), protoProvider);
        try {
            opSetPersPresence.createServerStoredContactGroup(parentProtoGroup, metaGroup.getGroupName());
            evtRetriever.waitForEvent(10000);
            removeGroupFromEventIgnoreList(metaGroup.getGroupName(), protoProvider);
            opSetPersPresence.removeServerStoredGroupChangeListener(evtRetriever);
            if (evtRetriever.evt == null) {
                throw new MetaContactListException("Failed to create a proto group named: " + metaGroup.getGroupName(), null, 2);
            }
            metaGroup.addProtoGroup(evtRetriever.evt.getSourceGroup());
            fireMetaContactGroupEvent(metaGroup, evtRetriever.evt.getSourceProvider(), evtRetriever.evt.getSourceGroup(), 6);
            return evtRetriever.evt.getSourceGroup();
        } catch (Exception ex) {
            throw new MetaContactListException("failed to create contact group " + metaGroup.getGroupName(), ex, 2);
        } catch (Throwable th) {
            removeGroupFromEventIgnoreList(metaGroup.getGroupName(), protoProvider);
            opSetPersPresence.removeServerStoredGroupChangeListener(evtRetriever);
        }
    }

    public MetaContactGroup findParentMetaContactGroup(MetaContactGroup child) {
        return findParentMetaContactGroup(this.rootMetaGroup, child);
    }

    private MetaContactGroup findParentMetaContactGroup(MetaContactGroupImpl root, MetaContactGroup child) {
        return child.getParentMetaContactGroup();
    }

    public MetaContactGroup findParentMetaContactGroup(MetaContact child) {
        if (child instanceof MetaContactImpl) {
            return ((MetaContactImpl) child).getParentGroup();
        }
        throw new IllegalArgumentException(child + " is not a MetaContactImpl instance.");
    }

    public MetaContact createMetaContact(ProtocolProviderService provider, MetaContactGroup metaContactGroup, String contactID) throws MetaContactListException {
        if (metaContactGroup instanceof MetaContactGroupImpl) {
            MetaContact newMetaContact = new MetaContactImpl();
            addNewContactToMetaContact(provider, metaContactGroup, newMetaContact, contactID, false);
            fireMetaContactEvent(newMetaContact, findParentMetaContactGroup(newMetaContact), 1);
            return newMetaContact;
        }
        throw new IllegalArgumentException(metaContactGroup + " is not an instance of MetaContactGroupImpl");
    }

    public MetaContactGroup createMetaContactGroup(MetaContactGroup parent, String groupName) throws MetaContactListException {
        if (parent instanceof MetaContactGroupImpl) {
            Iterator<MetaContactGroup> subgroups = parent.getSubgroups();
            while (subgroups.hasNext()) {
                if (((MetaContactGroup) subgroups.next()).getGroupName().equals(groupName)) {
                    throw new MetaContactListException("Parent " + parent.getGroupName() + " already contains a " + "group called " + groupName, new CloneNotSupportedException("just testing nested exc-s"), 4);
                }
            }
            MetaContactGroupImpl newMetaGroup = new MetaContactGroupImpl(this, groupName);
            ((MetaContactGroupImpl) parent).addSubgroup(newMetaGroup);
            fireMetaContactGroupEvent(newMetaGroup, null, null, 1);
            return newMetaGroup;
        }
        throw new IllegalArgumentException(parent + " is not an instance of MetaContactGroupImpl");
    }

    public void renameMetaContactGroup(MetaContactGroup group, String newGroupName) {
        ((MetaContactGroupImpl) group).setGroupName(newGroupName);
        Iterator<ContactGroup> groups = group.getContactGroups();
        while (groups.hasNext()) {
            ContactGroup protoGroup = (ContactGroup) groups.next();
            OperationSetPersistentPresence opSetPresence = (OperationSetPersistentPresence) protoGroup.getProtocolProvider().getOperationSet(OperationSetPersistentPresence.class);
            if (opSetPresence != null) {
                try {
                    opSetPresence.renameServerStoredContactGroup(protoGroup, newGroupName);
                } catch (Throwable t) {
                    logger.error("Error renaming protocol group: " + protoGroup, t);
                }
            }
        }
        fireMetaContactGroupEvent(group, null, null, 7);
    }

    public MetaContactGroup getRoot() {
        return this.rootMetaGroup;
    }

    public void renameMetaContact(MetaContact metaContact, String newDisplayName) throws IllegalArgumentException {
        renameMetaContact(metaContact, newDisplayName, true);
    }

    /* access modifiers changed from: private */
    public void renameMetaContact(MetaContact metaContact, String newDisplayName, boolean isUserDefined) throws IllegalArgumentException {
        if (metaContact instanceof MetaContactImpl) {
            String oldDisplayName = metaContact.getDisplayName();
            ((MetaContactImpl) metaContact).setDisplayName(newDisplayName);
            if (isUserDefined) {
                ((MetaContactImpl) metaContact).setDisplayNameUserDefined(true);
            }
            Iterator<Contact> contacts = metaContact.getContacts();
            while (contacts.hasNext()) {
                Contact protoContact = (Contact) contacts.next();
                OperationSetPersistentPresence opSetPresence = (OperationSetPersistentPresence) protoContact.getProtocolProvider().getOperationSet(OperationSetPersistentPresence.class);
                if (opSetPresence != null) {
                    try {
                        opSetPresence.setDisplayName(protoContact, newDisplayName);
                    } catch (Throwable t) {
                        logger.error("Error renaming protocol contact: " + protoContact, t);
                    }
                }
            }
            fireMetaContactEvent(new MetaContactRenamedEvent(metaContact, oldDisplayName, newDisplayName));
            fireMetaContactGroupEvent(findParentMetaContactGroup(metaContact), null, null, 4);
            return;
        }
        throw new IllegalArgumentException(metaContact + " is not a MetaContactImpl instance.");
    }

    public void clearUserDefinedDisplayName(MetaContact metaContact) throws IllegalArgumentException {
        if (metaContact instanceof MetaContactImpl) {
            ((MetaContactImpl) metaContact).setDisplayNameUserDefined(false);
            if (metaContact.getContactCount() == 1) {
                renameMetaContact(metaContact, metaContact.getDefaultContact().getDisplayName(), false);
                return;
            } else {
                fireMetaContactEvent(new MetaContactRenamedEvent(metaContact, metaContact.getDisplayName(), metaContact.getDisplayName()));
                return;
            }
        }
        throw new IllegalArgumentException(metaContact + " is not a MetaContactImpl instance.");
    }

    public void changeMetaContactAvatar(MetaContact metaContact, Contact protoContact, byte[] newAvatar) throws IllegalArgumentException {
        if (metaContact instanceof MetaContactImpl) {
            byte[] oldAvatar = metaContact.getAvatar(true);
            ((MetaContactImpl) metaContact).cacheAvatar(protoContact, newAvatar);
            fireMetaContactEvent(new MetaContactAvatarUpdateEvent(metaContact, oldAvatar, newAvatar));
            return;
        }
        throw new IllegalArgumentException(metaContact + " is not a MetaContactImpl instance.");
    }

    /* JADX WARNING: Missing block: B:32:0x0096, code skipped:
            if (r4 == null) goto L_0x00a5;
     */
    /* JADX WARNING: Missing block: B:33:0x0098, code skipped:
            fireMetaContactEvent(r4, r17, 1);
            fireProtoContactEvent(r16, "ProtoContactMoved", r2, r4);
     */
    /* JADX WARNING: Missing block: B:35:0x00a9, code skipped:
            if (r2.getContactCount() != 0) goto L_?;
     */
    /* JADX WARNING: Missing block: B:36:0x00ab, code skipped:
            r9 = r2.getParentGroup();
            r9.removeMetaContact(r2);
            fireMetaContactEvent(r2, r9, 2);
     */
    /* JADX WARNING: Missing block: B:49:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:50:?, code skipped:
            return;
     */
    public void moveContact(net.java.sip.communicator.service.protocol.Contact r16, net.java.sip.communicator.service.contactlist.MetaContactGroup r17) throws net.java.sip.communicator.service.contactlist.MetaContactListException {
        /*
        r15 = this;
        r12 = r16.getPersistableAddress();
        if (r12 != 0) goto L_0x000e;
    L_0x0006:
        r12 = logger;
        r13 = "Contact cannot be moved! This contact doesn't have persistant address.";
        r12.info(r13);
    L_0x000d:
        return;
    L_0x000e:
        r12 = r16.getPersistableAddress();
        if (r12 != 0) goto L_0x001c;
    L_0x0014:
        r12 = logger;
        r13 = "Contact cannot be moved! This contact doesn't have persistant address.";
        r12.info(r13);
        goto L_0x000d;
    L_0x001c:
        r11 = r16.getProtocolProvider();
        r12 = net.java.sip.communicator.service.protocol.OperationSetMultiUserChat.class;
        r7 = r11.getOperationSet(r12);
        r7 = (net.java.sip.communicator.service.protocol.OperationSetMultiUserChat) r7;
        if (r7 == 0) goto L_0x004c;
    L_0x002a:
        r12 = r16.getAddress();
        r12 = r7.isPrivateMessagingContact(r12);
        if (r12 == 0) goto L_0x004c;
    L_0x0034:
        r4 = new net.java.sip.communicator.impl.contactlist.MetaContactImpl;
        r4.m124init();
        r6 = r17;
        r6 = (net.java.sip.communicator.impl.contactlist.MetaContactGroupImpl) r6;
        r6.addMetaContact(r4);
        r12 = 1;
        r15.fireMetaContactEvent(r4, r6, r12);
        r12 = r16.getPersistableAddress();
        r15.addNewContactToMetaContact(r11, r4, r12);
        goto L_0x000d;
    L_0x004c:
        r12 = net.java.sip.communicator.service.protocol.OperationSetPersistentPresence.class;
        r8 = r11.getOperationSet(r12);
        r8 = (net.java.sip.communicator.service.protocol.OperationSetPersistentPresence) r8;
        if (r8 != 0) goto L_0x0056;
    L_0x0056:
        r2 = r15.findMetaContactByContact(r16);
        r2 = (net.java.sip.communicator.impl.contactlist.MetaContactImpl) r2;
        r13 = r16.getProtocolProvider();
        r12 = r17;
        r12 = (net.java.sip.communicator.impl.contactlist.MetaContactGroupImpl) r12;
        r10 = r15.resolveProtoPath(r13, r12);
        r12 = r16.getParentContactGroup();	 Catch:{ OperationFailedException -> 0x00b8 }
        if (r12 == r10) goto L_0x0075;
    L_0x006e:
        if (r8 == 0) goto L_0x0075;
    L_0x0070:
        r0 = r16;
        r8.moveContactToGroup(r0, r10);	 Catch:{ OperationFailedException -> 0x00b8 }
    L_0x0075:
        r0 = r16;
        r2.removeProtoContact(r0);	 Catch:{ OperationFailedException -> 0x00b8 }
        r4 = 0;
        monitor-enter(r16);
        r1 = r15.findMetaContactByContact(r16);	 Catch:{ all -> 0x00c4 }
        if (r1 != 0) goto L_0x0095;
    L_0x0082:
        r5 = new net.java.sip.communicator.impl.contactlist.MetaContactImpl;	 Catch:{ all -> 0x00c4 }
        r5.m124init();	 Catch:{ all -> 0x00c4 }
        r0 = r17;
        r0 = (net.java.sip.communicator.impl.contactlist.MetaContactGroupImpl) r0;	 Catch:{ all -> 0x00c7 }
        r12 = r0;
        r12.addMetaContact(r5);	 Catch:{ all -> 0x00c7 }
        r0 = r16;
        r5.addProtoContact(r0);	 Catch:{ all -> 0x00c7 }
        r4 = r5;
    L_0x0095:
        monitor-exit(r16);	 Catch:{ all -> 0x00c4 }
        if (r4 == 0) goto L_0x00a5;
    L_0x0098:
        r12 = 1;
        r0 = r17;
        r15.fireMetaContactEvent(r4, r0, r12);
        r12 = "ProtoContactMoved";
        r0 = r16;
        r15.fireProtoContactEvent(r0, r12, r2, r4);
    L_0x00a5:
        r12 = r2.getContactCount();
        if (r12 != 0) goto L_0x000d;
    L_0x00ab:
        r9 = r2.getParentGroup();
        r9.removeMetaContact(r2);
        r12 = 2;
        r15.fireMetaContactEvent(r2, r9, r12);
        goto L_0x000d;
    L_0x00b8:
        r3 = move-exception;
        r12 = new net.java.sip.communicator.service.contactlist.MetaContactListException;
        r13 = r3.getMessage();
        r14 = 7;
        r12.<init>(r13, r14);
        throw r12;
    L_0x00c4:
        r12 = move-exception;
    L_0x00c5:
        monitor-exit(r16);	 Catch:{ all -> 0x00c4 }
        throw r12;
    L_0x00c7:
        r12 = move-exception;
        r4 = r5;
        goto L_0x00c5;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl.moveContact(net.java.sip.communicator.service.protocol.Contact, net.java.sip.communicator.service.contactlist.MetaContactGroup):void");
    }

    public void moveContact(Contact contact, MetaContact newParentMetaContact) throws MetaContactListException {
        if (contact.getPersistableAddress() == null) {
            logger.info("Contact cannot be moved! This contact doesn't have persistant address.");
            return;
        }
        ProtocolProviderService provider = contact.getProtocolProvider();
        OperationSetMultiUserChat opSetMUC = (OperationSetMultiUserChat) provider.getOperationSet(OperationSetMultiUserChat.class);
        if (opSetMUC == null || !opSetMUC.isPrivateMessagingContact(contact.getAddress())) {
            OperationSetPersistentPresence opSetPresence = (OperationSetPersistentPresence) provider.getOperationSet(OperationSetPersistentPresence.class);
            if (opSetPresence == null) {
            }
            if (newParentMetaContact instanceof MetaContactImpl) {
                MetaContactImpl currentParentMetaContact = (MetaContactImpl) findMetaContactByContact(contact);
                ContactGroup parentProtoGroup = resolveProtoPath(contact.getProtocolProvider(), (MetaContactGroupImpl) findParentMetaContactGroup(newParentMetaContact));
                try {
                    if (!(contact.getParentContactGroup() == parentProtoGroup || opSetPresence == null)) {
                        opSetPresence.moveContactToGroup(contact, parentProtoGroup);
                    }
                    currentParentMetaContact.removeProtoContact(contact);
                    synchronized (contact) {
                        if (findMetaContactByContact(contact) == null) {
                            ((MetaContactImpl) newParentMetaContact).addProtoContact(contact);
                        }
                    }
                    if (newParentMetaContact.containsContact(contact)) {
                        fireProtoContactEvent(contact, "ProtoContactMoved", currentParentMetaContact, newParentMetaContact);
                    }
                    if (currentParentMetaContact.getContactCount() == 0) {
                        MetaContactGroupImpl parentMetaGroup = currentParentMetaContact.getParentGroup();
                        parentMetaGroup.removeMetaContact(currentParentMetaContact);
                        fireMetaContactEvent(currentParentMetaContact, parentMetaGroup, 2);
                        return;
                    }
                    return;
                } catch (OperationFailedException ex) {
                    throw new MetaContactListException(ex.getMessage(), 7);
                }
            }
            throw new IllegalArgumentException(newParentMetaContact + " is not a MetaContactImpl instance.");
        }
        addNewContactToMetaContact(provider, newParentMetaContact, contact.getPersistableAddress());
    }

    public void moveMetaContact(MetaContact metaContact, MetaContactGroup newMetaGroup) throws MetaContactListException, IllegalArgumentException {
        if (!(newMetaGroup instanceof MetaContactGroupImpl)) {
            throw new IllegalArgumentException(newMetaGroup + " is not a MetaContactGroupImpl instance");
        } else if (metaContact instanceof MetaContactImpl) {
            MetaContactGroupImpl currentParent = (MetaContactGroupImpl) findParentMetaContactGroup(metaContact);
            if (currentParent != null) {
                currentParent.removeMetaContact((MetaContactImpl) metaContact);
            }
            ((MetaContactGroupImpl) newMetaGroup).addMetaContact((MetaContactImpl) metaContact);
            try {
                Iterator<Contact> contacts = metaContact.getContacts();
                while (contacts.hasNext()) {
                    Contact protoContact = (Contact) contacts.next();
                    ContactGroup protoGroup = resolveProtoPath(protoContact.getProtocolProvider(), (MetaContactGroupImpl) newMetaGroup);
                    OperationSetPersistentPresence opSetPresence = (OperationSetPersistentPresence) protoContact.getProtocolProvider().getOperationSet(OperationSetPersistentPresence.class);
                    if (opSetPresence != null) {
                        opSetPresence.moveContactToGroup(protoContact, protoGroup);
                    }
                }
                fireMetaContactEvent(new MetaContactMovedEvent(metaContact, currentParent, newMetaGroup));
            } catch (Exception ex) {
                logger.error("Cannot move contact", ex);
                ((MetaContactGroupImpl) newMetaGroup).removeMetaContact((MetaContactImpl) metaContact);
                currentParent.addMetaContact((MetaContactImpl) metaContact);
                throw new MetaContactListException(ex.getMessage(), 7);
            }
        } else {
            throw new IllegalArgumentException(metaContact + " is not a MetaContactImpl instance");
        }
    }

    public void removeContact(Contact contact) throws MetaContactListException {
        OperationSetPresence opSetPresence = (OperationSetPresence) contact.getProtocolProvider().getOperationSet(OperationSetPresence.class);
        if (opSetPresence == null) {
            opSetPresence = (OperationSetPresence) contact.getProtocolProvider().getOperationSet(OperationSetPersistentPresence.class);
            if (opSetPresence == null) {
                throw new IllegalStateException("Cannot remove a contact from a provider with no presence operation set.");
            }
        }
        try {
            opSetPresence.unsubscribe(contact);
        } catch (Exception ex) {
            String errorTxt = "Failed to remove " + contact + " from its protocol provider. ";
            if (ex instanceof OperationFailedException) {
                errorTxt = errorTxt + ex.getMessage();
            }
            throw new MetaContactListException(errorTxt, ex, 2);
        }
    }

    public void removeMetaContactListListener(MetaContactListListener listener) {
        synchronized (this.metaContactListListeners) {
            this.metaContactListListeners.remove(listener);
        }
    }

    public void removeMetaContact(MetaContact metaContact) throws MetaContactListException {
        Iterator<Contact> protoContactsIter = metaContact.getContacts();
        while (protoContactsIter.hasNext()) {
            removeContact((Contact) protoContactsIter.next());
        }
    }

    public void removeMetaContactGroup(MetaContactGroup groupToRemove) throws MetaContactListException {
        if (groupToRemove instanceof MetaContactGroupImpl) {
            try {
                Iterator<ContactGroup> protoGroups = groupToRemove.getContactGroups();
                while (protoGroups.hasNext()) {
                    ContactGroup protoGroup = (ContactGroup) protoGroups.next();
                    OperationSetPersistentPresence opSetPersPresence = (OperationSetPersistentPresence) protoGroup.getProtocolProvider().getOperationSet(OperationSetPersistentPresence.class);
                    if (opSetPersPresence != null) {
                        opSetPersPresence.removeServerStoredContactGroup(protoGroup);
                    } else {
                        return;
                    }
                }
                ((MetaContactGroupImpl) findParentMetaContactGroup(groupToRemove)).removeSubgroup(groupToRemove);
                fireMetaContactGroupEvent(groupToRemove, null, null, 2);
                return;
            } catch (Exception ex) {
                throw new MetaContactListException(ex.getMessage(), 6);
            }
        }
        throw new IllegalArgumentException(groupToRemove + " is not an instance of MetaContactGroupImpl");
    }

    public void removeContactGroupFromMetaContactGroup(MetaContactGroupImpl metaContainer, ContactGroup groupToRemove, ProtocolProviderService sourceProvider) {
        if (metaContainer == null) {
            logger.warn("No meta container found, when trying to remove group: " + groupToRemove);
            return;
        }
        locallyRemoveAllContactsForProvider(metaContainer, groupToRemove);
        fireMetaContactGroupEvent(metaContainer, sourceProvider, groupToRemove, 3);
    }

    public void purgeLocallyStoredContactListCopy() {
        this.storageManager.storeContactListAndStopStorageManager();
        this.storageManager.removeContactListFile();
        if (logger.isTraceEnabled()) {
            logger.trace("Removed meta contact list storage file.");
        }
    }

    private void locallyRemoveAllContactsForProvider(MetaContactGroupImpl parentMetaGroup, ContactGroup groupToRemove) {
        Iterator<MetaContact> childrenContactsIter = parentMetaGroup.getChildContacts();
        while (childrenContactsIter.hasNext()) {
            MetaContactImpl child = (MetaContactImpl) childrenContactsIter.next();
            Iterator<Contact> contactsToRemove = child.getContactsForContactGroup(groupToRemove);
            child.removeContactsForGroup(groupToRemove);
            if (child.getContactCount() == 0) {
                parentMetaGroup.removeMetaContact(child);
                fireMetaContactEvent(child, parentMetaGroup, 2);
            } else {
                while (contactsToRemove.hasNext()) {
                    fireProtoContactEvent((Contact) contactsToRemove.next(), "ProtoContactRemoved", child, null);
                }
            }
        }
        Iterator<MetaContactGroup> subgroupsIter = parentMetaGroup.getSubgroups();
        while (subgroupsIter.hasNext()) {
            MetaContactGroup subMetaGroup = (MetaContactGroupImpl) subgroupsIter.next();
            Iterator<ContactGroup> contactGroups = subMetaGroup.getContactGroups();
            ContactGroup protoGroup = null;
            while (contactGroups.hasNext()) {
                protoGroup = (ContactGroup) contactGroups.next();
                if (groupToRemove == protoGroup.getParentContactGroup()) {
                    locallyRemoveAllContactsForProvider(subMetaGroup, protoGroup);
                }
            }
            if (subMetaGroup.countSubgroups() == 0 && subMetaGroup.countChildContacts() == 0 && subMetaGroup.countContactGroups() == 0) {
                parentMetaGroup.removeSubgroup(subMetaGroup);
                fireMetaContactGroupEvent(subMetaGroup, groupToRemove.getProtocolProvider(), protoGroup, 2);
            }
        }
        parentMetaGroup.removeProtoGroup(groupToRemove);
    }

    public MetaContactGroup findMetaContactGroupByContactGroup(ContactGroup contactGroup) {
        return this.rootMetaGroup.findMetaContactGroupByContactGroup(contactGroup);
    }

    public MetaContact findMetaContactByContact(Contact contact) {
        return this.rootMetaGroup.findMetaContactByContact(contact);
    }

    public MetaContact findMetaContactByContact(String contactAddress, String accountID) {
        return this.rootMetaGroup.findMetaContactByContact(contactAddress, accountID);
    }

    public MetaContact findMetaContactByMetaUID(String metaContactID) {
        return this.rootMetaGroup.findMetaContactByMetaUID(metaContactID);
    }

    public MetaContactGroup findMetaContactGroupByMetaUID(String metaGroupID) {
        return this.rootMetaGroup.findMetaContactGroupByMetaUID(metaGroupID);
    }

    public Iterator<MetaContact> findAllMetaContactsForProvider(ProtocolProviderService protocolProvider) {
        List<MetaContact> resultList = new ArrayList();
        findAllMetaContactsForProvider(protocolProvider, this.rootMetaGroup, resultList);
        return resultList.iterator();
    }

    public Iterator<MetaContact> findAllMetaContactsForProvider(ProtocolProviderService protocolProvider, MetaContactGroup metaContactGroup) {
        List<MetaContact> resultList = new LinkedList();
        findAllMetaContactsForProvider(protocolProvider, metaContactGroup, resultList);
        return resultList.iterator();
    }

    public Iterator<MetaContact> findAllMetaContactsForAddress(String contactAddress) {
        List<MetaContact> resultList = new LinkedList();
        findAllMetaContactsForAddress(this.rootMetaGroup, contactAddress, resultList);
        return resultList.iterator();
    }

    private void findAllMetaContactsForAddress(MetaContactGroup metaContactGroup, String contactAddress, List<MetaContact> resultList) {
        Iterator<MetaContact> childContacts = metaContactGroup.getChildContacts();
        while (childContacts.hasNext()) {
            MetaContact metaContact = (MetaContact) childContacts.next();
            Iterator<Contact> protocolContacts = metaContact.getContacts();
            while (protocolContacts.hasNext()) {
                Contact protocolContact = (Contact) protocolContacts.next();
                if (protocolContact.getAddress().equals(contactAddress) || protocolContact.getDisplayName().equals(contactAddress)) {
                    resultList.add(metaContact);
                }
            }
        }
        Iterator<MetaContactGroup> subGroups = metaContactGroup.getSubgroups();
        while (subGroups.hasNext()) {
            MetaContactGroup subGroup = (MetaContactGroup) subGroups.next();
            if (subGroup.getContactGroups().hasNext()) {
                findAllMetaContactsForAddress(subGroup, contactAddress, resultList);
            }
        }
    }

    private void findAllMetaContactsForProvider(ProtocolProviderService protocolProvider, MetaContactGroup metaContactGroup, List<MetaContact> resultList) {
        Iterator<MetaContact> childContacts = metaContactGroup.getChildContacts();
        while (childContacts.hasNext()) {
            MetaContact metaContact = (MetaContact) childContacts.next();
            if (metaContact.getContactsForProvider(protocolProvider).hasNext()) {
                resultList.add(metaContact);
            }
        }
        Iterator<MetaContactGroup> subGroups = metaContactGroup.getSubgroups();
        while (subGroups.hasNext()) {
            MetaContactGroup subGroup = (MetaContactGroup) subGroups.next();
            if (subGroup.getContactGroupsForProvider(protocolProvider).hasNext()) {
                findAllMetaContactsForProvider(protocolProvider, subGroup, resultList);
            }
        }
    }

    private void synchronizeOpSetWithLocalContactList(OperationSetPersistentPresence presenceOpSet) {
        ContactGroup rootProtoGroup = presenceOpSet.getServerStoredContactListRoot();
        if (rootProtoGroup != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("subgroups: " + rootProtoGroup.countSubgroups());
            }
            if (logger.isTraceEnabled()) {
                logger.trace("child contacts: " + rootProtoGroup.countContacts());
            }
            addContactGroupToMetaGroup(rootProtoGroup, this.rootMetaGroup, true);
        }
        presenceOpSet.addSubscriptionListener(this.clSubscriptionEventHandler);
        presenceOpSet.addServerStoredGroupChangeListener(this.clGroupEventHandler);
    }

    private void addContactGroupToMetaGroup(ContactGroup protoGroup, MetaContactGroupImpl metaGroup, boolean fireEvents) {
        metaGroup.addProtoGroup(protoGroup);
        Iterator<ContactGroup> subgroupsIter = protoGroup.subgroups();
        while (subgroupsIter.hasNext()) {
            ContactGroup group = (ContactGroup) subgroupsIter.next();
            if (metaGroup.findMetaContactGroupByContactGroup(group) == null) {
                MetaContactGroupImpl newMetaGroup = new MetaContactGroupImpl(this, group.getGroupName());
                metaGroup.addSubgroup(newMetaGroup);
                addContactGroupToMetaGroup(group, newMetaGroup, false);
                if (fireEvents) {
                    fireMetaContactGroupEvent(newMetaGroup, group.getProtocolProvider(), group, 1);
                }
            }
        }
        Iterator<Contact> contactsIter = protoGroup.contacts();
        while (contactsIter.hasNext()) {
            Contact contact = (Contact) contactsIter.next();
            if (metaGroup.findMetaContactByContact(contact) == null) {
                MetaContactImpl newMetaContact = new MetaContactImpl();
                newMetaContact.addProtoContact(contact);
                metaGroup.addMetaContact(newMetaContact);
                if (fireEvents) {
                    fireMetaContactEvent(newMetaContact, metaGroup, 1);
                }
            }
        }
    }

    private synchronized void handleProviderAdded(ProtocolProviderService provider) {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding protocol provider " + provider.getAccountID().getAccountUniqueID());
        }
        OperationSetPersistentPresence opSetPersPresence = (OperationSetPersistentPresence) provider.getOperationSet(OperationSetPersistentPresence.class);
        this.currentlyInstalledProviders.put(provider.getAccountID().getAccountUniqueID(), provider);
        if (opSetPersPresence != null) {
            try {
                this.storageManager.extractContactsForAccount(provider.getAccountID().getAccountUniqueID());
                if (logger.isDebugEnabled()) {
                    logger.debug("All contacts loaded for account " + provider.getAccountID().getAccountUniqueID());
                }
            } catch (XMLException exc) {
                logger.error("Failed to load contacts for account " + provider.getAccountID().getAccountUniqueID(), exc);
            }
            synchronizeOpSetWithLocalContactList(opSetPersPresence);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Service did not have a pers. pres. op. set.");
        }
        if (opSetPersPresence != null) {
            opSetPersPresence.addContactPresenceStatusListener(this);
        }
        OperationSetContactCapabilities capOpSet = (OperationSetContactCapabilities) provider.getOperationSet(OperationSetContactCapabilities.class);
        if (capOpSet != null) {
            capOpSet.addContactCapabilitiesListener(this);
        }
        return;
    }

    private void handleProviderRemoved(ProtocolProviderService provider) {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing protocol provider " + provider);
        }
        this.currentlyInstalledProviders.remove(provider.getAccountID().getAccountUniqueID());
        OperationSetPersistentPresence persPresOpSet = (OperationSetPersistentPresence) provider.getOperationSet(OperationSetPersistentPresence.class);
        if (persPresOpSet != null) {
            persPresOpSet.removeContactPresenceStatusListener(this);
            persPresOpSet.removeSubscriptionListener(this.clSubscriptionEventHandler);
            persPresOpSet.removeServerStoredGroupChangeListener(this.clGroupEventHandler);
            ContactGroup rootGroup = persPresOpSet.getServerStoredContactListRoot();
            Iterator<ContactGroup> subgroups = rootGroup.subgroups();
            while (subgroups.hasNext()) {
                ContactGroup group = (ContactGroup) subgroups.next();
                removeContactGroupFromMetaContactGroup((MetaContactGroupImpl) findMetaContactGroupByContactGroup(group), group, provider);
            }
            removeContactGroupFromMetaContactGroup(this.rootMetaGroup, rootGroup, provider);
        }
        OperationSetContactCapabilities capOpSet = (OperationSetContactCapabilities) provider.getOperationSet(OperationSetContactCapabilities.class);
        if (capOpSet != null) {
            capOpSet.removeContactCapabilitiesListener(this);
        }
    }

    private void addGroupToEventIgnoreList(String group, ProtocolProviderService ownerProvider) {
        if (!isGroupInEventIgnoreList(group, ownerProvider)) {
            List<ProtocolProviderService> existingProvList = (List) this.groupEventIgnoreList.get(group);
            if (existingProvList == null) {
                existingProvList = new LinkedList();
            }
            existingProvList.add(ownerProvider);
            this.groupEventIgnoreList.put(group, existingProvList);
        }
    }

    /* access modifiers changed from: private */
    public boolean isGroupInEventIgnoreList(String group, ProtocolProviderService ownerProvider) {
        List<ProtocolProviderService> existingProvList = (List) this.groupEventIgnoreList.get(group);
        return existingProvList != null && existingProvList.contains(ownerProvider);
    }

    private void removeGroupFromEventIgnoreList(String group, ProtocolProviderService ownerProvider) {
        if (isGroupInEventIgnoreList(group, ownerProvider)) {
            List<ProtocolProviderService> existingProvList = (List) this.groupEventIgnoreList.get(group);
            if (existingProvList.size() < 1) {
                this.groupEventIgnoreList.remove(group);
            } else {
                existingProvList.remove(ownerProvider);
            }
        }
    }

    private void addContactToEventIgnoreList(String contact, ProtocolProviderService ownerProvider) {
        if (!isContactInEventIgnoreList(contact, ownerProvider)) {
            List<ProtocolProviderService> existingProvList = (List) this.contactEventIgnoreList.get(contact);
            if (existingProvList == null) {
                existingProvList = new LinkedList();
            }
            existingProvList.add(ownerProvider);
            this.contactEventIgnoreList.put(contact, existingProvList);
        }
    }

    private boolean isContactInEventIgnoreList(String contact, ProtocolProviderService ownerProvider) {
        List<ProtocolProviderService> existingProvList = (List) this.contactEventIgnoreList.get(contact);
        return existingProvList != null && existingProvList.contains(ownerProvider);
    }

    /* access modifiers changed from: private */
    public boolean isContactInEventIgnoreList(Contact contact, ProtocolProviderService ownerProvider) {
        for (Entry<String, List<ProtocolProviderService>> contactEventIgnoreEntry : this.contactEventIgnoreList.entrySet()) {
            String contactAddress = (String) contactEventIgnoreEntry.getKey();
            if (!contact.getAddress().equals(contactAddress)) {
                if (contact.equals(contactAddress)) {
                }
            }
            List<ProtocolProviderService> existingProvList = (List) contactEventIgnoreEntry.getValue();
            if (existingProvList == null || !existingProvList.contains(ownerProvider)) {
                return false;
            }
            return true;
        }
        return false;
    }

    private void removeContactFromEventIgnoreList(String contact, ProtocolProviderService ownerProvider) {
        if (isContactInEventIgnoreList(contact, ownerProvider)) {
            List<ProtocolProviderService> existingProvList = (List) this.contactEventIgnoreList.get(contact);
            if (existingProvList.size() < 1) {
                this.groupEventIgnoreList.remove(contact);
            } else {
                existingProvList.remove(ownerProvider);
            }
        }
    }

    public void serviceChanged(ServiceEvent event) {
        ProtocolProviderService sService = this.bundleContext.getService(event.getServiceReference());
        if (logger.isTraceEnabled()) {
            logger.trace("Received a service event for: " + sService.getClass().getName());
        }
        if (sService instanceof ProtocolProviderService) {
            if (logger.isDebugEnabled()) {
                logger.debug("Service is a protocol provider.");
            }
            ProtocolProviderService provider = sService;
            ProtocolProviderFactory sourceFactory = null;
            for (ServiceReference bundleServiceRef : event.getServiceReference().getBundle().getRegisteredServices()) {
                ProtocolProviderFactory service = this.bundleContext.getService(bundleServiceRef);
                if (service instanceof ProtocolProviderFactory) {
                    sourceFactory = service;
                    break;
                }
            }
            if (event.getType() == 1) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Handling registration of a new Protocol Provider.");
                }
                String providerMask = System.getProperty("net.java.sip.communicator.service.contactlist.PROVIDER_MASK");
                if (providerMask != null && providerMask.trim().length() > 0) {
                    String servRefMask = (String) event.getServiceReference().getProperty("net.java.sip.communicator.service.contactlist.PROVIDER_MASK");
                    if (servRefMask == null || !servRefMask.equals(providerMask)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Ignoing masked provider: " + provider.getAccountID());
                            return;
                        }
                        return;
                    }
                }
                if (sourceFactory != null && this.currentlyInstalledProviders.containsKey(provider.getAccountID().getAccountUniqueID()) && logger.isDebugEnabled()) {
                    logger.debug("An already installed account: " + provider.getAccountID() + ". Modifying it.");
                }
                handleProviderAdded(sService);
            } else if (event.getType() == 4 && sourceFactory != null) {
                if (ContactlistActivator.getAccountManager().getStoredAccounts().contains(provider.getAccountID())) {
                    synchronized (this) {
                        removeMetaContactListListener(this.storageManager);
                        handleProviderRemoved(sService);
                        addMetaContactListListener(this.storageManager);
                    }
                    return;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Account uninstalled. acc.id=" + provider.getAccountID() + ". Removing from meta " + "contact list.");
                }
                handleProviderRemoved(sService);
            }
        }
    }

    /* access modifiers changed from: private|declared_synchronized */
    public synchronized void fireMetaContactEvent(MetaContact sourceContact, MetaContactGroup parentGroup, int eventID) {
        MetaContactEvent evt = new MetaContactEvent(sourceContact, parentGroup, eventID);
        if (logger.isTraceEnabled()) {
            logger.trace("Will dispatch the following mcl event: " + evt);
        }
        for (MetaContactListListener listener : getMetaContactListListeners()) {
            switch (evt.getEventID()) {
                case 1:
                    listener.metaContactAdded(evt);
                    break;
                case 2:
                    listener.metaContactRemoved(evt);
                    break;
                default:
                    logger.error("Unknown event type " + evt.getEventID());
                    break;
            }
        }
    }

    private MetaContactListListener[] getMetaContactListListeners() {
        MetaContactListListener[] listeners;
        synchronized (this.metaContactListListeners) {
            listeners = (MetaContactListListener[]) this.metaContactListListeners.toArray(new MetaContactListListener[this.metaContactListListeners.size()]);
        }
        return listeners;
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized void fireMetaContactEvent(MetaContactPropertyChangeEvent event) {
        if (logger.isTraceEnabled()) {
            logger.trace("Will dispatch the following mcl property change event: " + event);
        }
        for (MetaContactListListener listener : getMetaContactListListeners()) {
            if (event instanceof MetaContactMovedEvent) {
                listener.metaContactMoved((MetaContactMovedEvent) event);
            } else if (event instanceof MetaContactRenamedEvent) {
                listener.metaContactRenamed((MetaContactRenamedEvent) event);
            } else if (event instanceof MetaContactModifiedEvent) {
                listener.metaContactModified((MetaContactModifiedEvent) event);
            } else if (event instanceof MetaContactAvatarUpdateEvent) {
                listener.metaContactAvatarUpdated((MetaContactAvatarUpdateEvent) event);
            }
        }
    }

    /* access modifiers changed from: private|declared_synchronized */
    public synchronized void fireProtoContactEvent(Contact source, String eventName, MetaContact oldParent, MetaContact newParent) {
        ProtoContactEvent event = new ProtoContactEvent(source, eventName, oldParent, newParent);
        if (logger.isTraceEnabled()) {
            logger.trace("Will dispatch the following mcl property change event: " + event);
        }
        for (MetaContactListListener listener : getMetaContactListListeners()) {
            if (eventName.equals("ProtoContactAdded")) {
                listener.protoContactAdded(event);
            } else if (eventName.equals("ProtoContactMoved")) {
                listener.protoContactMoved(event);
            } else if (eventName.equals("ProtoContactRemoved")) {
                listener.protoContactRemoved(event);
            } else if (eventName.equals("ProtoContactModified")) {
                listener.protoContactModified(event);
            }
        }
    }

    public void contactPresenceStatusChanged(ContactPresenceStatusChangeEvent evt) {
        MetaContact metaContactImpl = (MetaContactImpl) findMetaContactByContact(evt.getSourceContact());
        if (metaContactImpl != null && metaContactImpl.getParentGroup().indexOf(metaContactImpl) != metaContactImpl.reevalContact()) {
            fireMetaContactGroupEvent(findParentMetaContactGroup(metaContactImpl), evt.getSourceProvider(), null, 4);
        }
    }

    /* access modifiers changed from: 0000 */
    public MetaContactGroupImpl loadStoredMetaContactGroup(MetaContactGroupImpl parentGroup, String metaContactGroupUID, String displayName) {
        MetaContactGroupImpl newMetaGroup = (MetaContactGroupImpl) parentGroup.getMetaContactSubgroupByUID(metaContactGroupUID);
        if (newMetaGroup != null) {
            return newMetaGroup;
        }
        newMetaGroup = new MetaContactGroupImpl(this, displayName, metaContactGroupUID);
        parentGroup.addSubgroup(newMetaGroup);
        fireMetaContactGroupEvent(newMetaGroup, null, null, 1);
        return newMetaGroup;
    }

    /* access modifiers changed from: 0000 */
    public ContactGroup loadStoredContactGroup(MetaContactGroupImpl containingMetaGroup, String contactGroupUID, ContactGroup parentProtoGroup, String persistentData, String accountID) {
        OperationSetPersistentPresence presenceOpSet = (OperationSetPersistentPresence) ((ProtocolProviderService) this.currentlyInstalledProviders.get(accountID)).getOperationSet(OperationSetPersistentPresence.class);
        if (parentProtoGroup == null) {
            parentProtoGroup = presenceOpSet.getServerStoredContactListRoot();
        }
        ContactGroup newProtoGroup = presenceOpSet.createUnresolvedContactGroup(contactGroupUID, persistentData, parentProtoGroup);
        containingMetaGroup.addProtoGroup(newProtoGroup);
        return newProtoGroup;
    }

    /* access modifiers changed from: 0000 */
    public MetaContactImpl loadStoredMetaContact(MetaContactGroupImpl parentGroup, String metaUID, String displayName, Map<String, List<String>> details, List<StoredProtoContactDescriptor> protoContacts, String accountID) {
        MetaContactImpl newMetaContact = (MetaContactImpl) findMetaContactByMetaUID(metaUID);
        if (newMetaContact == null) {
            newMetaContact = new MetaContactImpl(metaUID, details);
            newMetaContact.setDisplayName(displayName);
        }
        OperationSetPersistentPresence presenceOpSet = (OperationSetPersistentPresence) ((ProtocolProviderService) this.currentlyInstalledProviders.get(accountID)).getOperationSet(OperationSetPersistentPresence.class);
        for (StoredProtoContactDescriptor contactDescriptor : protoContacts) {
            MetaContact mc = findMetaContactByContact(contactDescriptor.contactAddress, accountID);
            if (mc != null) {
                logger.warn("Ignoring duplicate proto contact " + contactDescriptor + " accountID=" + accountID + ". The contact was also present in the " + "folloing meta contact:" + mc);
            } else {
                newMetaContact.addProtoContact(presenceOpSet.createUnresolvedContact(contactDescriptor.contactAddress, contactDescriptor.persistentData, contactDescriptor.parentProtoGroup == null ? presenceOpSet.getServerStoredContactListRoot() : contactDescriptor.parentProtoGroup));
            }
        }
        if (newMetaContact.getContactCount() == 0) {
            logger.error("Found an empty meta contact. Throwing an exception so that the storage manager would remove it.");
            throw new IllegalArgumentException("MetaContact[" + newMetaContact + "] contains no non-duplicating child contacts.");
        }
        parentGroup.addMetaContact(newMetaContact);
        fireMetaContactEvent(newMetaContact, parentGroup, 1);
        if (logger.isTraceEnabled()) {
            logger.trace("Created meta contact: " + newMetaContact);
        }
        return newMetaContact;
    }

    /* access modifiers changed from: private|declared_synchronized */
    public synchronized void fireMetaContactGroupEvent(MetaContactGroup source, ProtocolProviderService provider, ContactGroup sourceProtoGroup, int eventID) {
        MetaContactGroupEvent evt = new MetaContactGroupEvent(source, provider, sourceProtoGroup, eventID);
        if (logger.isTraceEnabled()) {
            logger.trace("Will dispatch the following mcl event: " + evt);
        }
        for (MetaContactListListener listener : getMetaContactListListeners()) {
            switch (eventID) {
                case 1:
                    listener.metaContactGroupAdded(evt);
                    break;
                case 2:
                    listener.metaContactGroupRemoved(evt);
                    break;
                case 3:
                case 5:
                case 6:
                case 7:
                    listener.metaContactGroupModified(evt);
                    break;
                case 4:
                    listener.childContactsReordered(evt);
                    break;
                default:
                    logger.error("Unknown event type (" + eventID + ") for event: " + evt);
                    break;
            }
        }
    }

    public void supportedOperationSetsChanged(ContactCapabilitiesEvent event) {
        MetaContactImpl metaContactImpl = (MetaContactImpl) findMetaContactByContact(event.getSourceContact());
        if (metaContactImpl != null) {
            Contact contact = event.getSourceContact();
            metaContactImpl.updateCapabilities(contact, event.getOperationSets());
            fireProtoContactEvent(contact, "ProtoContactModified", metaContactImpl, metaContactImpl);
        }
    }

    public int getSourceIndex() {
        return 1;
    }
}
