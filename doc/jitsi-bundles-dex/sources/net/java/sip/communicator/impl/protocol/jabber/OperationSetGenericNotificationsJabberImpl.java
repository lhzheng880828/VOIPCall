package net.java.sip.communicator.impl.protocol.jabber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.java.sip.communicator.impl.protocol.jabber.extensions.notification.NotificationEventIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.notification.NotificationEventIQProvider;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationSetGenericNotifications;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.GenericEventListener;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.util.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;

public class OperationSetGenericNotificationsJabberImpl implements OperationSetGenericNotifications, PacketListener {
    private static final Logger logger = Logger.getLogger(OperationSetGenericNotificationsJabberImpl.class);
    private final Map<String, List<GenericEventListener>> genericEventListeners = new HashMap();
    /* access modifiers changed from: private|final */
    public final ProtocolProviderServiceJabberImpl jabberProvider;
    /* access modifiers changed from: private */
    public OperationSetPersistentPresenceJabberImpl opSetPersPresence = null;

    private class RegistrationStateListener implements RegistrationStateChangeListener {
        private RegistrationStateListener() {
        }

        public void registrationStateChanged(RegistrationStateChangeEvent evt) {
            if (evt.getNewState() == RegistrationState.REGISTERED) {
                OperationSetGenericNotificationsJabberImpl.this.opSetPersPresence = (OperationSetPersistentPresenceJabberImpl) OperationSetGenericNotificationsJabberImpl.this.jabberProvider.getOperationSet(OperationSetPersistentPresence.class);
                if (OperationSetGenericNotificationsJabberImpl.this.jabberProvider.getConnection() != null) {
                    OperationSetGenericNotificationsJabberImpl.this.jabberProvider.getConnection().addPacketListener(OperationSetGenericNotificationsJabberImpl.this, new PacketTypeFilter(NotificationEventIQ.class));
                }
            } else if (evt.getNewState() == RegistrationState.UNREGISTERED || evt.getNewState() == RegistrationState.CONNECTION_FAILED || evt.getNewState() == RegistrationState.AUTHENTICATION_FAILED) {
                if (OperationSetGenericNotificationsJabberImpl.this.jabberProvider.getConnection() != null) {
                    OperationSetGenericNotificationsJabberImpl.this.jabberProvider.getConnection().removePacketListener(OperationSetGenericNotificationsJabberImpl.this);
                }
                OperationSetGenericNotificationsJabberImpl.this.opSetPersPresence = null;
            }
        }
    }

    OperationSetGenericNotificationsJabberImpl(ProtocolProviderServiceJabberImpl provider) {
        this.jabberProvider = provider;
        provider.addRegistrationStateChangeListener(new RegistrationStateListener());
        ProviderManager.getInstance().addIQProvider(NotificationEventIQ.ELEMENT_NAME, NotificationEventIQ.NAMESPACE, new NotificationEventIQProvider());
    }

    public void notifyForEvent(Contact contact, String eventName, String eventValue) {
        if (this.jabberProvider.isRegistered()) {
            NotificationEventIQ newEvent = new NotificationEventIQ();
            newEvent.setEventName(eventName);
            newEvent.setEventValue(eventValue);
            newEvent.setTo(contact.getAddress());
            newEvent.setEventSource(this.jabberProvider.getOurJID());
            this.jabberProvider.getConnection().sendPacket(newEvent);
        } else if (logger.isTraceEnabled()) {
            logger.trace("provider not registered. won't send keep alive. acc.id=" + this.jabberProvider.getAccountID().getAccountUniqueID());
        }
    }

    public void notifyForEvent(String jid, String eventName, String eventValue) {
        notifyForEvent(jid, eventName, eventValue, null);
    }

    public void notifyForEvent(String jid, String eventName, String eventValue, String source) {
        if (this.jabberProvider.isRegistered()) {
            String fullJid = this.jabberProvider.getFullJid(jid);
            if (fullJid != null) {
                jid = fullJid;
            }
            NotificationEventIQ newEvent = new NotificationEventIQ();
            newEvent.setEventName(eventName);
            newEvent.setEventValue(eventValue);
            newEvent.setTo(jid);
            if (source != null) {
                newEvent.setEventSource(source);
            } else {
                newEvent.setEventSource(this.jabberProvider.getOurJID());
            }
            this.jabberProvider.getConnection().sendPacket(newEvent);
        } else if (logger.isTraceEnabled()) {
            logger.trace("provider not registered. won't send keep alive. acc.id=" + this.jabberProvider.getAccountID().getAccountUniqueID());
        }
    }

    public void addGenericEventListener(String eventName, GenericEventListener listener) {
        synchronized (this.genericEventListeners) {
            List<GenericEventListener> l = (List) this.genericEventListeners.get(eventName);
            if (l == null) {
                l = new ArrayList();
                this.genericEventListeners.put(eventName, l);
            }
            if (!l.contains(listener)) {
                l.add(listener);
            }
        }
    }

    public void removeGenericEventListener(String eventName, GenericEventListener listener) {
        synchronized (this.genericEventListeners) {
            List<GenericEventListener> listenerList = (List) this.genericEventListeners.get(eventName);
            if (listenerList != null) {
                listenerList.remove(listener);
            }
        }
    }

    public void processPacket(Packet packet) {
        if (packet == null || (packet instanceof NotificationEventIQ)) {
            NotificationEventIQ notifyEvent = (NotificationEventIQ) packet;
            if (logger.isDebugEnabled() && logger.isDebugEnabled()) {
                logger.debug("Received notificationEvent from " + notifyEvent.getFrom() + " msg : " + notifyEvent.toXML());
            }
            Contact sender = this.opSetPersPresence.findContactByID(StringUtils.parseBareAddress(notifyEvent.getFrom()));
            if (sender == null) {
                sender = this.opSetPersPresence.createVolatileContact(notifyEvent.getFrom());
            }
            if (notifyEvent.getType() == Type.GET) {
                fireNewEventNotification(sender, notifyEvent.getEventName(), notifyEvent.getEventValue(), notifyEvent.getEventSource(), true);
            } else if (notifyEvent.getType() == Type.ERROR) {
                fireNewEventNotification(sender, notifyEvent.getEventName(), notifyEvent.getEventValue(), notifyEvent.getEventSource(), false);
            }
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0031, code skipped:
            r6 = r8.iterator();
     */
    /* JADX WARNING: Missing block: B:13:0x0039, code skipped:
            if (r6.hasNext() == false) goto L_0x002a;
     */
    /* JADX WARNING: Missing block: B:14:0x003b, code skipped:
            r7 = (net.java.sip.communicator.service.protocol.event.GenericEventListener) r6.next();
     */
    /* JADX WARNING: Missing block: B:15:0x0041, code skipped:
            if (r16 == false) goto L_0x004a;
     */
    /* JADX WARNING: Missing block: B:16:0x0043, code skipped:
            r7.notificationReceived(r0);
     */
    /* JADX WARNING: Missing block: B:21:0x004a, code skipped:
            r7.notificationDeliveryFailed(r0);
     */
    /* JADX WARNING: Missing block: B:30:?, code skipped:
            return;
     */
    private void fireNewEventNotification(net.java.sip.communicator.service.protocol.Contact r12, java.lang.String r13, java.lang.String r14, java.lang.String r15, boolean r16) {
        /*
        r11 = this;
        r10 = org.jivesoftware.smack.util.StringUtils.parseBareAddress(r15);
        r1 = r11.opSetPersPresence;
        r5 = r1.findContactByID(r10);
        if (r5 != 0) goto L_0x0012;
    L_0x000c:
        r1 = r11.opSetPersPresence;
        r5 = r1.createVolatileContact(r15);
    L_0x0012:
        r0 = new net.java.sip.communicator.service.protocol.event.GenericEvent;
        r1 = r11.jabberProvider;
        r2 = r12;
        r3 = r13;
        r4 = r14;
        r0.<init>(r1, r2, r3, r4, r5);
        r2 = r11.genericEventListeners;
        monitor-enter(r2);
        r1 = r11.genericEventListeners;	 Catch:{ all -> 0x0047 }
        r9 = r1.get(r13);	 Catch:{ all -> 0x0047 }
        r9 = (java.util.List) r9;	 Catch:{ all -> 0x0047 }
        if (r9 != 0) goto L_0x002b;
    L_0x0029:
        monitor-exit(r2);	 Catch:{ all -> 0x0047 }
    L_0x002a:
        return;
    L_0x002b:
        r8 = new java.util.ArrayList;	 Catch:{ all -> 0x0047 }
        r8.<init>(r9);	 Catch:{ all -> 0x0047 }
        monitor-exit(r2);	 Catch:{ all -> 0x0047 }
        r6 = r8.iterator();
    L_0x0035:
        r1 = r6.hasNext();
        if (r1 == 0) goto L_0x002a;
    L_0x003b:
        r7 = r6.next();
        r7 = (net.java.sip.communicator.service.protocol.event.GenericEventListener) r7;
        if (r16 == 0) goto L_0x004a;
    L_0x0043:
        r7.notificationReceived(r0);
        goto L_0x0035;
    L_0x0047:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x0047 }
        throw r1;
    L_0x004a:
        r7.notificationDeliveryFailed(r0);
        goto L_0x0035;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.jabber.OperationSetGenericNotificationsJabberImpl.fireNewEventNotification(net.java.sip.communicator.service.protocol.Contact, java.lang.String, java.lang.String, java.lang.String, boolean):void");
    }
}
