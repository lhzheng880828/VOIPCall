package net.java.sip.communicator.impl.protocol.sip;

import java.net.URI;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import java.util.Vector;
import net.java.sip.communicator.impl.protocol.sip.EventPackageNotifier.Subscription;
import net.java.sip.communicator.service.protocol.AbstractOperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.AuthorizationHandler;
import net.java.sip.communicator.service.protocol.AuthorizationRequest;
import net.java.sip.communicator.service.protocol.AuthorizationResponse;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ContactGroup;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetContactTypeInfo;
import net.java.sip.communicator.service.protocol.PresenceStatus;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.service.protocol.event.ServerStoredGroupListener;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.stack.SIPServerTransaction;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.DialogTerminatedEvent;
import org.jitsi.javax.sip.IOExceptionEvent;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.SipProvider;
import org.jitsi.javax.sip.TimeoutEvent;
import org.jitsi.javax.sip.TransactionTerminatedEvent;
import org.jitsi.javax.sip.TransactionUnavailableException;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.header.CSeqHeader;
import org.jitsi.javax.sip.header.CallIdHeader;
import org.jitsi.javax.sip.header.ContentTypeHeader;
import org.jitsi.javax.sip.header.EventHeader;
import org.jitsi.javax.sip.header.ExpiresHeader;
import org.jitsi.javax.sip.header.FromHeader;
import org.jitsi.javax.sip.header.HeaderFactory;
import org.jitsi.javax.sip.header.MaxForwardsHeader;
import org.jitsi.javax.sip.header.MinExpiresHeader;
import org.jitsi.javax.sip.header.SIPETagHeader;
import org.jitsi.javax.sip.header.SIPIfMatchHeader;
import org.jitsi.javax.sip.header.SubscriptionStateHeader;
import org.jitsi.javax.sip.header.ToHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;
import org.jitsi.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OperationSetPresenceSipImpl extends AbstractOperationSetPersistentPresence<ProtocolProviderServiceSipImpl> implements MethodProcessor, RegistrationStateChangeListener {
    private static final String ACTIVITY_ELEMENT = "activities";
    private static final String ANY_NS = "*";
    private static final String AWAY_ELEMENT = "away";
    private static final String BASIC_ELEMENT = "basic";
    private static final String BUSY_ELEMENT = "busy";
    private static final String CONTACT_ELEMENT = "contact";
    private static final String DM_NS_ELEMENT = "xmlns:dm";
    private static final String DM_NS_VALUE = "urn:ietf:params:xml:ns:pidf:data-model";
    private static final String ENTITY_ATTRIBUTE = "entity";
    private static final String ID_ATTRIBUTE = "id";
    private static final String NOTE_ELEMENT = "note";
    private static final String NS_ACTIVITY_ELT = "rpid:activities";
    private static final String NS_AWAY_ELT = "rpid:away";
    private static final String NS_BUSY_ELT = "rpid:busy";
    private static final String NS_ELEMENT = "xmlns";
    private static final String NS_OTP_ELT = "rpid:on-the-phone";
    private static final String NS_PERSON_ELT = "dm:person";
    private static final String NS_STATUS_ICON_ELT = "rpid:status-icon";
    private static final String OFFLINE_STATUS = "closed";
    private static final String ONLINE_STATUS = "open";
    private static final String OTP_ELEMENT = "on-the-phone";
    private static final String PACKAGE_ATTRIBUTE = "package";
    private static final String PERSON_ELEMENT = "person";
    private static final String PERSON_ID = ("p" + ((long) (Math.random() * 10000.0d)));
    private static final String PIDF_NS_VALUE = "urn:ietf:params:xml:ns:pidf";
    private static final String PIDF_XML = "pidf+xml";
    private static final int PRESENCE_DEFAULT_EXPIRE = 3600;
    private static final String PRESENCE_ELEMENT = "presence";
    private static final String PRIORITY_ATTRIBUTE = "priority";
    private static final int REFRESH_MARGIN = 60;
    private static final String RESOURCE_ATTRIBUTE = "resource";
    private static final String RPID_NS_ELEMENT = "xmlns:rpid";
    private static final String RPID_NS_VALUE = "urn:ietf:params:xml:ns:pidf:rpid";
    private static final String STATE_ATTRIBUTE = "state";
    private static final String STATUS_ELEMENT = "status";
    private static final String STATUS_ICON_ELEMENT = "status-icon";
    private static final String TUPLE_ELEMENT = "tuple";
    private static final String TUPLE_ID = ("t" + ((long) (Math.random() * 10000.0d)));
    private static final String VERSION_ATTRIBUTE = "version";
    private static final String WATCHERINFO_ELEMENT = "watcherinfo";
    private static final String WATCHERINFO_NS_VALUE = "urn:ietf:params:xml:ns:watcherinfo";
    private static final String WATCHERINFO_XML = "watcherinfo+xml";
    private static final String WATCHERLIST_ELEMENT = "watcher-list";
    private static final String WATCHER_ELEMENT = "watcher";
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(OperationSetPresenceSipImpl.class);
    private static long publish_cseq = 1;
    /* access modifiers changed from: private */
    public AuthorizationHandler authorizationHandler = null;
    /* access modifiers changed from: private */
    public String distantPAET = null;
    private final EventPackageNotifier notifier;
    private PollOfflineContactsTask pollingTask = null;
    private final int pollingTaskPeriod;
    private final boolean presenceEnabled;
    private PresenceStatus presenceStatus;
    private RePublishTask republishTask = null;
    /* access modifiers changed from: private|final */
    public final SipStatusEnum sipStatusEnum;
    /* access modifiers changed from: private */
    public ServerStoredContactList ssContactList;
    private String statusMessage = "Default Status Message";
    private final EventPackageSubscriber subscriber;
    /* access modifiers changed from: private|final */
    public final int subscriptionDuration;
    private final TimerScheduler timer = new TimerScheduler();
    private boolean useDistantPA;
    private final List<String> waitedCallIds = new Vector();
    private final EventPackageSubscriber watcherInfoSubscriber;

    private class PollOfflineContactsTask extends TimerTask {
        private PollOfflineContactsTask() {
        }

        /* synthetic */ PollOfflineContactsTask(OperationSetPresenceSipImpl x0, AnonymousClass1 x1) {
            this();
        }

        public void run() {
            Iterator<Contact> rootContactsIter = OperationSetPresenceSipImpl.this.getServerStoredContactListRoot().contacts();
            while (rootContactsIter.hasNext()) {
                OperationSetPresenceSipImpl.this.forcePollContact((ContactSipImpl) rootContactsIter.next());
            }
            Iterator<ContactGroup> groupsIter = OperationSetPresenceSipImpl.this.getServerStoredContactListRoot().subgroups();
            while (groupsIter.hasNext()) {
                Iterator<Contact> contactsIter = ((ContactGroup) groupsIter.next()).contacts();
                while (contactsIter.hasNext()) {
                    OperationSetPresenceSipImpl.this.forcePollContact((ContactSipImpl) contactsIter.next());
                }
            }
        }
    }

    private class PresenceNotifierSubscription extends Subscription {
        private final ContactSipImpl contact;

        public PresenceNotifierSubscription(Address fromAddress, String eventId) {
            super(fromAddress, eventId);
            OperationSetPresenceSipImpl.this.setUseDistantPA(false);
            ContactSipImpl contact = OperationSetPresenceSipImpl.this.resolveContactID(fromAddress.getURI().toString());
            if (contact == null) {
                contact = new ContactSipImpl(fromAddress, (ProtocolProviderServiceSipImpl) OperationSetPresenceSipImpl.this.parentProvider);
                contact.setResolved(true);
                contact.setResolvable(false);
            }
            if (OperationSetPresenceSipImpl.logger.isDebugEnabled()) {
                OperationSetPresenceSipImpl.logger.debug(contact + " wants to watch your presence status");
            }
            this.contact = contact;
        }

        /* access modifiers changed from: protected */
        public boolean addressEquals(Address address) {
            String addressString = address.getURI().toString();
            String id1 = addressString;
            String id2 = addressString.substring(4);
            int domainBeginIndex = addressString.indexOf(64);
            String id3 = addressString.substring(0, domainBeginIndex);
            String id4 = addressString.substring(4, domainBeginIndex);
            String contactAddressString = this.contact.getAddress();
            if (contactAddressString.equals(id2) || contactAddressString.equals(id1) || contactAddressString.equals(id4) || contactAddressString.equals(id3)) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: protected */
        public byte[] createNotifyContent(String subscriptionState, String reason) {
            return OperationSetPresenceSipImpl.this.getPidfPresenceStatus(OperationSetPresenceSipImpl.this.getLocalContactForDst(this.contact));
        }
    }

    private class PresenceSubscriberSubscription extends EventPackageSubscriber.Subscription {
        private final ContactSipImpl contact;

        public PresenceSubscriberSubscription(ContactSipImpl contact) throws OperationFailedException {
            super(OperationSetPresenceSipImpl.this.getAddress(contact));
            this.contact = contact;
        }

        /* access modifiers changed from: protected */
        public void processActiveRequest(RequestEvent requestEvent, byte[] rawContent) {
            if (rawContent != null) {
                OperationSetPresenceSipImpl.this.setPidfPresenceStatus(new String(rawContent));
            }
            SubscriptionStateHeader stateHeader = (SubscriptionStateHeader) requestEvent.getRequest().getHeader("Subscription-State");
            if (stateHeader == null) {
                return;
            }
            if (SubscriptionStateHeader.PENDING.equals(stateHeader.getState())) {
                this.contact.setSubscriptionState(SubscriptionStateHeader.PENDING);
            } else if ("active".equals(stateHeader.getState())) {
                if (SubscriptionStateHeader.PENDING.equals(this.contact.getSubscriptionState()) && OperationSetPresenceSipImpl.this.authorizationHandler != null) {
                    OperationSetPresenceSipImpl.this.authorizationHandler.processAuthorizationResponse(new AuthorizationResponse(AuthorizationResponse.ACCEPT, ""), this.contact);
                }
                this.contact.setSubscriptionState("active");
            }
        }

        /* access modifiers changed from: protected */
        public void processFailureResponse(ResponseEvent responseEvent, int statusCode) {
            OperationSetPresenceSipImpl.this.changePresenceStatusForContact(this.contact, OperationSetPresenceSipImpl.this.sipStatusEnum.getStatus(Response.TEMPORARILY_UNAVAILABLE == statusCode ? SipStatusEnum.OFFLINE : SipStatusEnum.UNKNOWN));
            if (Response.UNAUTHORIZED != statusCode && Response.PROXY_AUTHENTICATION_REQUIRED != statusCode) {
                this.contact.setResolvable(false);
            }
        }

        /* access modifiers changed from: protected */
        public void processSuccessResponse(ResponseEvent responseEvent, int statusCode) {
            switch (statusCode) {
                case Response.OK /*200*/:
                case Response.ACCEPTED /*202*/:
                    try {
                        if (!this.contact.isResolved()) {
                            if (OperationSetPresenceSipImpl.this.resolveContactID(this.contact.getAddress()) == null) {
                                ContactGroup parentGroup = this.contact.getParentContactGroup();
                                ((ContactGroupSipImpl) parentGroup).addContact(this.contact);
                                OperationSetPresenceSipImpl.this.fireSubscriptionEvent(this.contact, parentGroup, 1);
                            }
                            OperationSetPresenceSipImpl.this.finalizeSubscription(this.contact);
                            return;
                        }
                        return;
                    } catch (NullPointerException e) {
                        if (OperationSetPresenceSipImpl.logger.isDebugEnabled()) {
                            OperationSetPresenceSipImpl.logger.debug("failed to finalize the subscription of the contact", e);
                            return;
                        }
                        return;
                    }
                default:
                    return;
            }
        }

        /* access modifiers changed from: protected */
        public void processTerminatedRequest(RequestEvent requestEvent, String reasonCode) {
            OperationSetPresenceSipImpl.this.terminateSubscription(this.contact);
            if (SubscriptionStateHeader.DEACTIVATED.equals(reasonCode)) {
                try {
                    OperationSetPresenceSipImpl.this.ssContactList.removeContact(this.contact);
                } catch (OperationFailedException e) {
                    OperationSetPresenceSipImpl.logger.error("Cannot remove contact that unsubscribed.", e);
                }
            }
            SubscriptionStateHeader stateHeader = (SubscriptionStateHeader) requestEvent.getRequest().getHeader("Subscription-State");
            if (stateHeader != null && SubscriptionStateHeader.TERMINATED.equals(stateHeader.getState())) {
                if (SubscriptionStateHeader.REJECTED.equals(stateHeader.getReasonCode())) {
                    if (SubscriptionStateHeader.PENDING.equals(this.contact.getSubscriptionState())) {
                        OperationSetPresenceSipImpl.this.authorizationHandler.processAuthorizationResponse(new AuthorizationResponse(AuthorizationResponse.REJECT, ""), this.contact);
                    }
                    this.contact.setResolvable(false);
                }
                this.contact.setSubscriptionState(SubscriptionStateHeader.TERMINATED);
            }
        }
    }

    private class RePublishTask extends TimerTask {
        private RePublishTask() {
        }

        /* synthetic */ RePublishTask(OperationSetPresenceSipImpl x0, AnonymousClass1 x1) {
            this();
        }

        public void run() {
            try {
                Request req;
                if (OperationSetPresenceSipImpl.this.distantPAET != null) {
                    req = OperationSetPresenceSipImpl.this.createPublish(OperationSetPresenceSipImpl.this.subscriptionDuration, false);
                } else {
                    req = OperationSetPresenceSipImpl.this.createPublish(OperationSetPresenceSipImpl.this.subscriptionDuration, true);
                }
                try {
                    try {
                        ((ProtocolProviderServiceSipImpl) OperationSetPresenceSipImpl.this.parentProvider).getDefaultJainSipProvider().getNewClientTransaction(req).sendRequest();
                    } catch (SipException e) {
                        OperationSetPresenceSipImpl.logger.error("can't send the PUBLISH request", e);
                    }
                } catch (TransactionUnavailableException e2) {
                    OperationSetPresenceSipImpl.logger.error("can't create the client transaction", e2);
                }
            } catch (OperationFailedException e3) {
                OperationSetPresenceSipImpl.logger.error("can't create a new PUBLISH message", e3);
            }
        }
    }

    private class WatcherInfoSubscriberSubscription extends EventPackageSubscriber.Subscription {
        /* access modifiers changed from: private */
        public int version = -1;

        public WatcherInfoSubscriberSubscription(Address toAddress) {
            super(toAddress);
        }

        /* access modifiers changed from: protected */
        public void processActiveRequest(RequestEvent requestEvent, byte[] rawContent) {
            if (rawContent != null) {
                OperationSetPresenceSipImpl.this.setWatcherInfoStatus(this, new String(rawContent));
            }
        }

        /* access modifiers changed from: protected */
        public void processFailureResponse(ResponseEvent responseEvent, int statusCode) {
            if (OperationSetPresenceSipImpl.logger.isDebugEnabled()) {
                OperationSetPresenceSipImpl.logger.debug("Cannot subscripe to presence watcher info!");
            }
        }

        /* access modifiers changed from: protected */
        public void processSuccessResponse(ResponseEvent responseEvent, int statusCode) {
            if (OperationSetPresenceSipImpl.logger.isDebugEnabled()) {
                OperationSetPresenceSipImpl.logger.debug("Subscriped to presence watcher info! status:" + statusCode);
            }
        }

        /* access modifiers changed from: protected */
        public void processTerminatedRequest(RequestEvent requestEvent, String reasonCode) {
            OperationSetPresenceSipImpl.logger.error("Subscription to presence watcher info terminated!");
        }
    }

    private enum WatcherStatus {
        PENDING(SubscriptionStateHeader.PENDING),
        ACTIVE("active"),
        WAITING("waiting"),
        TERMINATED(SubscriptionStateHeader.TERMINATED);
        
        private final String value;

        private WatcherStatus(String v) {
            this.value = v;
        }

        public String getValue() {
            return this.value;
        }
    }

    public OperationSetPresenceSipImpl(ProtocolProviderServiceSipImpl provider, boolean presenceEnabled, boolean forceP2PMode, int pollingPeriod, int subscriptionExpiration) {
        super(provider);
        if (provider.getAccountID().getAccountPropertyBoolean("XIVO_ENABLE", false)) {
            this.ssContactList = new ServerStoredContactListXivoImpl(provider, this);
        } else {
            this.ssContactList = new ServerStoredContactListSipImpl(provider, this);
            provider.addSupportedOperationSet(OperationSetContactTypeInfo.class, new OperationSetContactTypeInfoImpl(this));
        }
        ((ProtocolProviderServiceSipImpl) this.parentProvider).addRegistrationStateChangeListener(this);
        this.presenceEnabled = presenceEnabled;
        this.subscriptionDuration = subscriptionExpiration > 0 ? subscriptionExpiration : 3600;
        if (this.presenceEnabled) {
            this.subscriber = new EventPackageSubscriber((ProtocolProviderServiceSipImpl) this.parentProvider, PRESENCE_ELEMENT, this.subscriptionDuration, PIDF_XML, this.timer, 60);
            this.notifier = new EventPackageNotifier((ProtocolProviderServiceSipImpl) this.parentProvider, PRESENCE_ELEMENT, 3600, PIDF_XML, this.timer) {
                /* access modifiers changed from: protected */
                public Subscription createSubscription(Address fromAddress, String eventId) {
                    return new PresenceNotifierSubscription(fromAddress, eventId);
                }
            };
            this.watcherInfoSubscriber = new EventPackageSubscriber((ProtocolProviderServiceSipImpl) this.parentProvider, "presence.winfo", this.subscriptionDuration, WATCHERINFO_XML, this.timer, 60);
        } else {
            this.subscriber = null;
            this.notifier = null;
            this.watcherInfoSubscriber = null;
        }
        ((ProtocolProviderServiceSipImpl) this.parentProvider).registerMethodProcessor("SUBSCRIBE", this);
        ((ProtocolProviderServiceSipImpl) this.parentProvider).registerMethodProcessor("NOTIFY", this);
        ((ProtocolProviderServiceSipImpl) this.parentProvider).registerMethodProcessor("PUBLISH", this);
        ((ProtocolProviderServiceSipImpl) this.parentProvider).registerEvent(PRESENCE_ELEMENT);
        if (logger.isDebugEnabled()) {
            logger.debug("presence initialized with :" + presenceEnabled + ", " + forceP2PMode + ", " + pollingPeriod + ", " + subscriptionExpiration + " for " + ((ProtocolProviderServiceSipImpl) this.parentProvider).getOurDisplayName());
        }
        this.pollingTaskPeriod = pollingPeriod > 0 ? pollingPeriod * 1000 : 30000;
        this.useDistantPA = !forceP2PMode;
        this.sipStatusEnum = ((ProtocolProviderServiceSipImpl) this.parentProvider).getSipStatusEnum();
        this.presenceStatus = this.sipStatusEnum.getStatus(SipStatusEnum.OFFLINE);
    }

    public void addServerStoredGroupChangeListener(ServerStoredGroupListener listener) {
        this.ssContactList.addGroupListener(listener);
    }

    public void removeServerStoredGroupChangeListener(ServerStoredGroupListener listener) {
        this.ssContactList.removeGroupListener(listener);
    }

    public PresenceStatus getPresenceStatus() {
        return this.presenceStatus;
    }

    /* access modifiers changed from: private */
    public void setUseDistantPA(boolean useDistantPA) {
        this.useDistantPA = useDistantPA;
        if (!this.useDistantPA && this.republishTask != null) {
            this.republishTask.cancel();
            this.republishTask = null;
        }
    }

    public ContactGroup getServerStoredContactListRoot() {
        return this.ssContactList.getRootGroup();
    }

    public void createServerStoredContactGroup(ContactGroup parentGroup, String groupName) throws OperationFailedException {
        if (parentGroup instanceof ContactGroupSipImpl) {
            this.ssContactList.createGroup((ContactGroupSipImpl) parentGroup, groupName, true);
            return;
        }
        throw new IllegalArgumentException(String.format("Group %1s does not seem to belong to this protocol's contact list", new Object[]{parentGroup.getGroupName()}));
    }

    public ContactGroup createUnresolvedContactGroup(String groupUID, String persistentData, ContactGroup parentGroup) {
        if (parentGroup == null) {
            parentGroup = getServerStoredContactListRoot();
        }
        return this.ssContactList.createUnresolvedContactGroup((ContactGroupSipImpl) parentGroup, ContactGroupSipImpl.createNameFromUID(groupUID));
    }

    public void renameServerStoredContactGroup(ContactGroup group, String newName) {
        if (group instanceof ContactGroupSipImpl) {
            this.ssContactList.renameGroup((ContactGroupSipImpl) group, newName);
        } else {
            throw new IllegalArgumentException(String.format("Group %1s does not seem to belong to this protocol's contact list", new Object[]{group.getGroupName()}));
        }
    }

    public void moveContactToGroup(Contact contactToMove, ContactGroup newParent) {
        if (contactToMove instanceof ContactSipImpl) {
            try {
                this.ssContactList.moveContactToGroup((ContactSipImpl) contactToMove, (ContactGroupSipImpl) newParent);
                if (this.presenceEnabled) {
                    this.subscriber.subscribe(new PresenceSubscriberSubscription((ContactSipImpl) contactToMove));
                }
            } catch (OperationFailedException ex) {
                throw new IllegalStateException("Failed to move contact " + contactToMove.getAddress(), ex);
            }
        }
    }

    public void removeServerStoredContactGroup(ContactGroup group) {
        if (group instanceof ContactGroupSipImpl) {
            this.ssContactList.removeGroup((ContactGroupSipImpl) group);
            return;
        }
        throw new IllegalArgumentException(String.format("Group %1s does not seem to belong to this protocol's contact list", new Object[]{group.getGroupName()}));
    }

    public void publishPresenceStatus(PresenceStatus status, String statusMsg) throws IllegalArgumentException, IllegalStateException, OperationFailedException {
        PresenceStatus oldStatus = this.presenceStatus;
        this.presenceStatus = status;
        String oldMessage = this.statusMessage;
        this.statusMessage = statusMsg;
        if (!this.presenceEnabled || (((ProtocolProviderServiceSipImpl) this.parentProvider).getRegistrarConnection() instanceof SipRegistrarlessConnection)) {
            fireProviderStatusChangeEvent(oldStatus);
            fireProviderMsgStatusChangeEvent(oldMessage);
            return;
        }
        if (!status.equals(this.sipStatusEnum.getStatus(SipStatusEnum.OFFLINE))) {
            assertConnected();
        }
        if (this.useDistantPA) {
            Request req;
            if (status.equals(this.sipStatusEnum.getStatus(SipStatusEnum.OFFLINE))) {
                req = createPublish(0, false);
                synchronized (this.waitedCallIds) {
                    this.waitedCallIds.add(((CallIdHeader) req.getHeader("Call-ID")).getCallId());
                }
            } else {
                req = createPublish(this.subscriptionDuration, true);
            }
            try {
                try {
                    ((ProtocolProviderServiceSipImpl) this.parentProvider).getDefaultJainSipProvider().getNewClientTransaction(req).sendRequest();
                } catch (SipException e) {
                    logger.error("can't send the PUBLISH request", e);
                    throw new OperationFailedException("can't send the PUBLISH request", 2);
                }
            } catch (TransactionUnavailableException e2) {
                logger.error("can't create the client transaction", e2);
                throw new OperationFailedException("can't create the client transaction", 2);
            }
        }
        String subscriptionState;
        String reason;
        if (status.equals(this.sipStatusEnum.getStatus(SipStatusEnum.OFFLINE))) {
            subscriptionState = SubscriptionStateHeader.TERMINATED;
            reason = SubscriptionStateHeader.PROBATION;
        } else {
            subscriptionState = "active";
            reason = null;
        }
        this.notifier.notifyAll(subscriptionState, reason);
        if (status.equals(this.sipStatusEnum.getStatus(SipStatusEnum.OFFLINE))) {
            unsubscribeToAllEventSubscribers();
            unsubscribeToAllContact();
        }
        fireProviderStatusChangeEvent(oldStatus);
        fireProviderMsgStatusChangeEvent(oldMessage);
    }

    public void fireProviderMsgStatusChangeEvent(String oldValue) {
        fireProviderStatusMessageChangeEvent(oldValue, this.statusMessage);
    }

    /* access modifiers changed from: private */
    public Request createPublish(int expires, boolean insertPresDoc) throws OperationFailedException {
        CallIdHeader callIdHeader = ((ProtocolProviderServiceSipImpl) this.parentProvider).getDefaultJainSipProvider().getNewCallId();
        String localTag = SipMessageFactory.generateLocalTag();
        try {
            byte[] doc;
            Address ourAOR = ((ProtocolProviderServiceSipImpl) this.parentProvider).getRegistrarConnection().getAddressOfRecord();
            FromHeader fromHeader = ((ProtocolProviderServiceSipImpl) this.parentProvider).getHeaderFactory().createFromHeader(ourAOR, localTag);
            ToHeader toHeader = ((ProtocolProviderServiceSipImpl) this.parentProvider).getHeaderFactory().createToHeader(ourAOR, null);
            List viaHeaders = ((ProtocolProviderServiceSipImpl) this.parentProvider).getLocalViaHeaders(toHeader.getAddress());
            MaxForwardsHeader maxForwards = ((ProtocolProviderServiceSipImpl) this.parentProvider).getMaxForwardsHeader();
            if (insertPresDoc) {
                doc = getPidfPresenceStatus(getLocalContactForDst(toHeader.getAddress()));
            } else {
                doc = new byte[0];
            }
            try {
                ContentTypeHeader contTypeHeader = ((ProtocolProviderServiceSipImpl) this.parentProvider).getHeaderFactory().createContentTypeHeader(SIPServerTransaction.CONTENT_TYPE_APPLICATION, PIDF_XML);
                SIPIfMatchHeader ifmHeader = null;
                try {
                    if (this.distantPAET != null) {
                        ifmHeader = ((ProtocolProviderServiceSipImpl) this.parentProvider).getHeaderFactory().createSIPIfMatchHeader(this.distantPAET);
                    }
                    try {
                        HeaderFactory headerFactory = ((ProtocolProviderServiceSipImpl) this.parentProvider).getHeaderFactory();
                        long j = publish_cseq;
                        publish_cseq = 1 + j;
                        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(j, "PUBLISH");
                        try {
                            ExpiresHeader expHeader = ((ProtocolProviderServiceSipImpl) this.parentProvider).getHeaderFactory().createExpiresHeader(expires);
                            try {
                                EventHeader evtHeader = ((ProtocolProviderServiceSipImpl) this.parentProvider).getHeaderFactory().createEventHeader(PRESENCE_ELEMENT);
                                try {
                                    Request req = ((ProtocolProviderServiceSipImpl) this.parentProvider).getMessageFactory().createRequest(toHeader.getAddress().getURI(), "PUBLISH", callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards, contTypeHeader, doc);
                                    req.setHeader(expHeader);
                                    req.setHeader(evtHeader);
                                    if (ifmHeader != null) {
                                        req.setHeader(ifmHeader);
                                    }
                                    return req;
                                } catch (ParseException ex) {
                                    logger.error("Failed to create message Request!", ex);
                                    throw new OperationFailedException("Failed to create message Request!", 4, ex);
                                }
                            } catch (ParseException e) {
                                logger.error("An unexpected error occurred whileconstructing the Event header", e);
                                throw new OperationFailedException("An unexpected error occurred whileconstructing the Event header", 4, e);
                            }
                        } catch (InvalidArgumentException e2) {
                            logger.error("An unexpected error occurred whileconstructing the Expires header", e2);
                            throw new OperationFailedException("An unexpected error occurred whileconstructing the Expires header", 4, e2);
                        }
                    } catch (InvalidArgumentException ex2) {
                        logger.error("An unexpected error occurred whileconstructing the CSeqHeader", ex2);
                        throw new OperationFailedException("An unexpected error occurred whileconstructing the CSeqHeader", 4, ex2);
                    } catch (ParseException ex3) {
                        logger.error("An unexpected error occurred whileconstructing the CSeqHeader", ex3);
                        throw new OperationFailedException("An unexpected error occurred whileconstructing the CSeqHeader", 4, ex3);
                    }
                } catch (ParseException e3) {
                    logger.error("An unexpected error occurred whileconstructing the SIPIfMatch header", e3);
                    throw new OperationFailedException("An unexpected error occurred whileconstructing the SIPIfMatch header", 4, e3);
                }
            } catch (ParseException ex32) {
                logger.error("An unexpected error occurred whileconstructing the content headers", ex32);
                throw new OperationFailedException("An unexpected error occurred whileconstructing the content headers", 4, ex32);
            }
        } catch (ParseException ex322) {
            logger.error("An unexpected error occurred whileconstructing the FromHeader or ToHeader", ex322);
            throw new OperationFailedException("An unexpected error occurred whileconstructing the FromHeader or ToHeader", 4, ex322);
        }
    }

    public Iterator<PresenceStatus> getSupportedStatusSet() {
        return this.sipStatusEnum.getSupportedStatusSet();
    }

    public PresenceStatus queryContactStatus(String contactIdentifier) throws IllegalArgumentException, IllegalStateException, OperationFailedException {
        Contact contact = resolveContactID(contactIdentifier);
        if (contact != null) {
            return contact.getPresenceStatus();
        }
        throw new IllegalArgumentException("contact " + contactIdentifier + " unknown");
    }

    public void subscribe(String contactIdentifier) throws IllegalArgumentException, IllegalStateException, OperationFailedException {
        subscribe(this.ssContactList.getRootGroup(), contactIdentifier);
    }

    public void subscribe(ContactGroup parentGroup, String contactIdentifier) throws IllegalArgumentException, IllegalStateException, OperationFailedException {
        subscribe(parentGroup, contactIdentifier, null);
    }

    /* access modifiers changed from: 0000 */
    public void subscribe(ContactGroup parentGroup, String contactIdentifier, String contactType) throws IllegalArgumentException, IllegalStateException, OperationFailedException {
        assertConnected();
        if (parentGroup instanceof ContactGroupSipImpl) {
            ContactSipImpl contact = resolveContactID(contactIdentifier);
            if (contact != null) {
                if (contact.isPersistent()) {
                    throw new OperationFailedException("Contact " + contactIdentifier + " already exists.", 5);
                }
                this.ssContactList.removeContact(contact);
            }
            contact = this.ssContactList.createContact((ContactGroupSipImpl) parentGroup, contactIdentifier, true, contactType);
            if (this.presenceEnabled) {
                this.subscriber.subscribe(new PresenceSubscriberSubscription(contact));
                return;
            }
            return;
        }
        throw new IllegalArgumentException(String.format("Group %1s does not seem to belong to this protocol's contact list", new Object[]{parentGroup.getGroupName()}));
    }

    private void assertConnected() throws IllegalStateException {
        if (this.parentProvider == null) {
            throw new IllegalStateException("The provider must be non-null and signed on the service before being able to communicate.");
        } else if (!((ProtocolProviderServiceSipImpl) this.parentProvider).isRegistered()) {
            throw new IllegalStateException("The provider must be signed on the service before being able to communicate.");
        }
    }

    public void unsubscribe(Contact contact) throws IllegalArgumentException, IllegalStateException, OperationFailedException {
        assertConnected();
        if (contact instanceof ContactSipImpl) {
            ContactSipImpl sipContact = (ContactSipImpl) contact;
            unsubscribe(sipContact, false);
            this.ssContactList.removeContact(sipContact);
            return;
        }
        throw new IllegalArgumentException("The contact is not a SIP contact");
    }

    private void unsubscribe(ContactSipImpl sipcontact, boolean assertConnectedAndSubscribed) throws IllegalArgumentException, IllegalStateException, OperationFailedException {
        if (this.presenceEnabled && sipcontact.isResolvable()) {
            if (assertConnectedAndSubscribed) {
                assertConnected();
            }
            this.subscriber.unsubscribe(getAddress(sipcontact), assertConnectedAndSubscribed);
        }
        terminateSubscription(sipcontact);
    }

    public boolean processResponse(ResponseEvent responseEvent) {
        if (!this.presenceEnabled) {
            return false;
        }
        ClientTransaction clientTransaction = responseEvent.getClientTransaction();
        Response response = responseEvent.getResponse();
        CSeqHeader cseq = (CSeqHeader) response.getHeader("CSeq");
        if (cseq == null) {
            logger.error("An incoming response did not contain a CSeq header");
            return false;
        } else if (!cseq.getMethod().equals("PUBLISH")) {
            return false;
        } else {
            if (!(response.getStatusCode() == Response.UNAUTHORIZED || response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED || response.getStatusCode() == Response.INTERVAL_TOO_BRIEF)) {
                synchronized (this.waitedCallIds) {
                    this.waitedCallIds.remove(((CallIdHeader) response.getHeader("Call-ID")).getCallId());
                }
            }
            if (response.getStatusCode() == Response.OK) {
                SIPETagHeader etHeader = (SIPETagHeader) response.getHeader("SIP-ETag");
                if (etHeader == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("can't find the ETag header");
                    }
                    return false;
                }
                this.distantPAET = etHeader.getETag();
                ExpiresHeader expires = (ExpiresHeader) response.getHeader("Expires");
                if (expires == null) {
                    logger.error("no Expires header in the response");
                    return false;
                } else if (expires.getExpires() == 0) {
                    this.distantPAET = null;
                    return true;
                } else {
                    if (this.republishTask != null) {
                        this.republishTask.cancel();
                    }
                    this.republishTask = new RePublishTask(this, null);
                    int republishDelay = expires.getExpires();
                    if (republishDelay >= 120) {
                        republishDelay -= 60;
                    }
                    this.timer.schedule(this.republishTask, (long) (republishDelay * 1000));
                }
            } else if (response.getStatusCode() == Response.UNAUTHORIZED || response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED) {
                try {
                    processAuthenticationChallenge(clientTransaction, response, (SipProvider) responseEvent.getSource());
                } catch (OperationFailedException e) {
                    logger.error("can't handle the challenge", e);
                    return false;
                }
            } else if (response.getStatusCode() == Response.INTERVAL_TOO_BRIEF) {
                MinExpiresHeader min = (MinExpiresHeader) response.getHeader("Min-Expires");
                if (min == null) {
                    logger.error("can't find a min expires header in the 423 error message");
                    return false;
                }
                try {
                    try {
                        try {
                            ((ProtocolProviderServiceSipImpl) this.parentProvider).getDefaultJainSipProvider().getNewClientTransaction(createPublish(min.getExpires(), true)).sendRequest();
                        } catch (SipException e2) {
                            logger.error("can't send the PUBLISH request", e2);
                            return false;
                        }
                    } catch (TransactionUnavailableException e3) {
                        logger.error("can't create the client transaction", e3);
                        return false;
                    }
                } catch (OperationFailedException e4) {
                    logger.error("can't create the new publish request", e4);
                    return false;
                }
            } else if (response.getStatusCode() == Response.CONDITIONAL_REQUEST_FAILED) {
                this.distantPAET = null;
                try {
                    try {
                        try {
                            ((ProtocolProviderServiceSipImpl) this.parentProvider).getDefaultJainSipProvider().getNewClientTransaction(createPublish(this.subscriptionDuration, true)).sendRequest();
                        } catch (SipException e22) {
                            logger.error("can't send the PUBLISH request", e22);
                            return false;
                        }
                    } catch (TransactionUnavailableException e32) {
                        logger.error("can't create the client transaction", e32);
                        return false;
                    }
                } catch (OperationFailedException e42) {
                    logger.error("can't create the new publish request", e42);
                    return false;
                }
            } else if (response.getStatusCode() < 100 || response.getStatusCode() >= Response.OK) {
                if (logger.isDebugEnabled()) {
                    logger.debug("error received from the network" + response);
                }
                this.distantPAET = null;
                if (this.useDistantPA) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("we enter into the peer-to-peer mode as the distant PA mode fails");
                    }
                    setUseDistantPA(false);
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void finalizeSubscription(ContactSipImpl contact) throws NullPointerException {
        if (contact == null) {
            throw new NullPointerException(CONTACT_ELEMENT);
        }
        contact.setResolved(true);
        fireSubscriptionEvent(contact, contact.getParentContactGroup(), 4);
        if (logger.isDebugEnabled()) {
            logger.debug("contact " + contact + " resolved");
        }
    }

    /* access modifiers changed from: private */
    public void terminateSubscription(ContactSipImpl contact) {
        if (contact == null) {
            logger.error("null contact provided, can't terminate subscription");
            return;
        }
        changePresenceStatusForContact(contact, this.sipStatusEnum.getStatus(SipStatusEnum.UNKNOWN));
        contact.setResolved(false);
    }

    public boolean processRequest(RequestEvent requestEvent) {
        if (!this.presenceEnabled) {
            return false;
        }
        Request request = requestEvent.getRequest();
        EventHeader eventHeader = (EventHeader) request.getHeader("Event");
        if (eventHeader == null) {
            return false;
        }
        String eventType = eventHeader.getEventType();
        if (!PRESENCE_ELEMENT.equalsIgnoreCase(eventType) && !"presence.winfo".equalsIgnoreCase(eventType)) {
            return false;
        }
        String requestMethod = request.getMethod();
        if ((PRESENCE_ELEMENT.equalsIgnoreCase(eventType) && "PUBLISH".equals(requestMethod)) || ("presence.winfo".equalsIgnoreCase(eventType) && "SUBSCRIBE".equals(requestMethod))) {
            return EventPackageSupport.sendNotImplementedResponse((ProtocolProviderServiceSipImpl) this.parentProvider, requestEvent);
        }
        return false;
    }

    public boolean processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        return false;
    }

    public boolean processIOException(IOExceptionEvent exceptionEvent) {
        return false;
    }

    public boolean processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        return false;
    }

    public boolean processTimeout(TimeoutEvent timeoutEvent) {
        logger.error("timeout reached, it looks really abnormal: " + timeoutEvent.toString());
        return false;
    }

    private void processAuthenticationChallenge(ClientTransaction clientTransaction, Response response, SipProvider jainSipProvider) throws OperationFailedException {
        EventPackageSupport.processAuthenticationChallenge((ProtocolProviderServiceSipImpl) this.parentProvider, clientTransaction, response, jainSipProvider);
    }

    /* access modifiers changed from: private */
    public void changePresenceStatusForContact(ContactSipImpl contact, PresenceStatus newStatus) {
        PresenceStatus oldStatus = contact.getPresenceStatus();
        contact.setPresenceStatus(newStatus);
        fireContactPresenceStatusChangeEvent(contact, contact.getParentContactGroup(), oldStatus);
    }

    public ContactSipImpl findContactByID(String contactID) {
        return this.ssContactList.getRootGroup().findContactByID(contactID);
    }

    public ContactSipImpl getLocalContactForDst(ContactSipImpl destination) {
        return getLocalContactForDst(destination.getSipAddress());
    }

    public ContactSipImpl getLocalContactForDst(Address destination) {
        ContactSipImpl res = new ContactSipImpl(((ProtocolProviderServiceSipImpl) this.parentProvider).getOurSipAddress(destination), (ProtocolProviderServiceSipImpl) this.parentProvider);
        res.setPresenceStatus(this.presenceStatus);
        return res;
    }

    public void setAuthorizationHandler(AuthorizationHandler handler) {
        this.authorizationHandler = handler;
    }

    public String getCurrentStatusMessage() {
        return this.statusMessage;
    }

    public Contact createUnresolvedContact(String address, String persistentData) {
        return createUnresolvedContact(address, persistentData, getServerStoredContactListRoot());
    }

    public Contact createUnresolvedContact(String contactId, String persistentData, ContactGroup parent) {
        return this.ssContactList.createUnresolvedContact((ContactGroupSipImpl) parent, contactId, persistentData);
    }

    public ContactSipImpl createVolatileContact(String contactAddress, String displayName) {
        try {
            ContactGroupSipImpl volatileGroup = getNonPersistentGroup();
            if (volatileGroup == null) {
                volatileGroup = this.ssContactList.createGroup(this.ssContactList.getRootGroup(), SipActivator.getResources().getI18NString("service.gui.NOT_IN_CONTACT_LIST_GROUP_NAME"), false);
            }
            if (displayName != null) {
                return this.ssContactList.createContact(volatileGroup, contactAddress, displayName, false, null);
            }
            return this.ssContactList.createContact(volatileGroup, contactAddress, false, null);
        } catch (OperationFailedException e) {
            return null;
        }
    }

    public ContactSipImpl createVolatileContact(String contactAddress) {
        return createVolatileContact(contactAddress, null);
    }

    private ContactGroupSipImpl getNonPersistentGroup() {
        for (int i = 0; i < getServerStoredContactListRoot().countSubgroups(); i++) {
            ContactGroupSipImpl gr = (ContactGroupSipImpl) getServerStoredContactListRoot().getGroup(i);
            if (!gr.isPersistent()) {
                return gr;
            }
        }
        return null;
    }

    /* access modifiers changed from: 0000 */
    public ContactSipImpl resolveContactID(String contactID) {
        ContactSipImpl res = findContactByID(contactID);
        if (res != null) {
            return res;
        }
        if (contactID.startsWith("sip:")) {
            res = findContactByID(contactID.substring(4));
        }
        if (res != null) {
            return res;
        }
        int domainBeginIndex = contactID.indexOf(64);
        if (domainBeginIndex > -1) {
            res = findContactByID(contactID.substring(0, domainBeginIndex));
            if (res == null && contactID.startsWith("sip:")) {
                res = findContactByID(contactID.substring(4, domainBeginIndex));
            }
        }
        if (res != null) {
            return res;
        }
        int domainEndIndex = contactID.indexOf(Separators.COLON, 4);
        if (domainEndIndex < 0) {
            domainEndIndex = contactID.indexOf(Separators.SEMICOLON, 4);
        }
        if (domainEndIndex > -1) {
            return findContactByID(contactID.substring(4, domainEndIndex));
        }
        return res;
    }

    /* access modifiers changed from: 0000 */
    public Document createDocument() {
        try {
            return XMLUtils.createDocument();
        } catch (Exception e) {
            logger.error("Can't create xml document", e);
            return null;
        }
    }

    /* access modifiers changed from: 0000 */
    public String convertDocument(Document document) {
        try {
            return XMLUtils.createXml(document);
        } catch (Exception e) {
            logger.error("Can't convert the xml document into a string", e);
            return null;
        }
    }

    /* access modifiers changed from: 0000 */
    public Document convertDocument(String document) {
        try {
            return XMLUtils.createDocument(document);
        } catch (Exception e) {
            logger.error("Can't convert the string into a xml document", e);
            return null;
        }
    }

    public byte[] getPidfPresenceStatus(ContactSipImpl contact) {
        Document doc = createDocument();
        if (doc == null) {
            return null;
        }
        String contactUri = contact.getSipAddress().getURI().toString();
        Element presence = doc.createElement(PRESENCE_ELEMENT);
        presence.setAttribute(NS_ELEMENT, PIDF_NS_VALUE);
        presence.setAttribute(RPID_NS_ELEMENT, RPID_NS_VALUE);
        presence.setAttribute(DM_NS_ELEMENT, DM_NS_VALUE);
        presence.setAttribute("entity", contactUri);
        doc.appendChild(presence);
        Element person = doc.createElement(NS_PERSON_ELT);
        person.setAttribute("id", PERSON_ID);
        presence.appendChild(person);
        Element activities = doc.createElement(NS_ACTIVITY_ELT);
        person.appendChild(activities);
        URI imageUri = this.ssContactList.getImageUri();
        if (imageUri != null) {
            Element statusIcon = doc.createElement(NS_STATUS_ICON_ELT);
            statusIcon.setTextContent(imageUri.toString());
            person.appendChild(statusIcon);
        }
        if (contact.getPresenceStatus().equals(this.sipStatusEnum.getStatus(SipStatusEnum.AWAY))) {
            activities.appendChild(doc.createElement(NS_AWAY_ELT));
        } else if (contact.getPresenceStatus().equals(this.sipStatusEnum.getStatus(SipStatusEnum.BUSY))) {
            activities.appendChild(doc.createElement(NS_BUSY_ELT));
        } else if (contact.getPresenceStatus().equals(this.sipStatusEnum.getStatus(SipStatusEnum.ON_THE_PHONE))) {
            activities.appendChild(doc.createElement(NS_OTP_ELT));
        }
        Element tuple = doc.createElement(TUPLE_ELEMENT);
        tuple.setAttribute("id", TUPLE_ID);
        presence.appendChild(tuple);
        Element status = doc.createElement("status");
        tuple.appendChild(status);
        Element basic = doc.createElement(BASIC_ELEMENT);
        if (contact.getPresenceStatus().equals(this.sipStatusEnum.getStatus(SipStatusEnum.OFFLINE))) {
            basic.appendChild(doc.createTextNode(OFFLINE_STATUS));
        } else {
            basic.appendChild(doc.createTextNode(ONLINE_STATUS));
        }
        status.appendChild(basic);
        Element contactUriEl = doc.createElement(CONTACT_ELEMENT);
        contactUriEl.appendChild(doc.createTextNode(contactUri));
        tuple.appendChild(contactUriEl);
        Element noteNodeEl = doc.createElement(NOTE_ELEMENT);
        noteNodeEl.appendChild(doc.createTextNode(contact.getPresenceStatus().getStatusName()));
        tuple.appendChild(noteNodeEl);
        String res = convertDocument(doc);
        if (res == null) {
            return null;
        }
        return res.getBytes();
    }

    /* JADX WARNING: Removed duplicated region for block: B:199:0x030d A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x03d6  */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x0366  */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x037b  */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x039b  */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x03d6  */
    /* JADX WARNING: Removed duplicated region for block: B:199:0x030d A:{SYNTHETIC} */
    public void setPidfPresenceStatus(java.lang.String r62) {
        /*
        r61 = this;
        r18 = r61.convertDocument(r62);
        if (r18 != 0) goto L_0x0007;
    L_0x0006:
        return;
    L_0x0007:
        r57 = logger;
        r57 = r57.isDebugEnabled();
        if (r57 == 0) goto L_0x002b;
    L_0x000f:
        r57 = logger;
        r58 = new java.lang.StringBuilder;
        r58.<init>();
        r59 = "parsing:\n";
        r58 = r58.append(r59);
        r0 = r58;
        r1 = r62;
        r58 = r0.append(r1);
        r58 = r58.toString();
        r57.debug(r58);
    L_0x002b:
        r57 = "urn:ietf:params:xml:ns:pidf";
        r58 = "presence";
        r0 = r18;
        r1 = r57;
        r2 = r58;
        r35 = r0.getElementsByTagNameNS(r1, r2);
        r57 = r35.getLength();
        if (r57 != 0) goto L_0x005b;
    L_0x003f:
        r57 = "*";
        r58 = "presence";
        r0 = r18;
        r1 = r57;
        r2 = r58;
        r35 = r0.getElementsByTagNameNS(r1, r2);
        r57 = r35.getLength();
        if (r57 != 0) goto L_0x005b;
    L_0x0053:
        r57 = logger;
        r58 = "no presence element in this document";
        r57.error(r58);
        goto L_0x0006;
    L_0x005b:
        r57 = r35.getLength();
        r58 = 1;
        r0 = r57;
        r1 = r58;
        if (r0 <= r1) goto L_0x006e;
    L_0x0067:
        r57 = logger;
        r58 = "more than one presence element in this document";
        r57.warn(r58);
    L_0x006e:
        r57 = 0;
        r0 = r35;
        r1 = r57;
        r36 = r0.item(r1);
        r57 = r36.getNodeType();
        r58 = 1;
        r0 = r57;
        r1 = r58;
        if (r0 == r1) goto L_0x008d;
    L_0x0084:
        r57 = logger;
        r58 = "the presence node is not an element";
        r57.error(r58);
        goto L_0x0006;
    L_0x008d:
        r37 = r36;
        r37 = (org.w3c.dom.Element) r37;
        r33 = 0;
        r34 = 0;
        r57 = "*";
        r58 = "person";
        r0 = r37;
        r1 = r57;
        r2 = r58;
        r31 = r0.getElementsByTagNameNS(r1, r2);
        r57 = r31.getLength();
        if (r57 <= 0) goto L_0x018d;
    L_0x00a9:
        r57 = 0;
        r0 = r31;
        r1 = r57;
        r32 = r0.item(r1);
        r57 = r32.getNodeType();
        r58 = 1;
        r0 = r57;
        r1 = r58;
        if (r0 == r1) goto L_0x00c8;
    L_0x00bf:
        r57 = logger;
        r58 = "the person node is not an element";
        r57.error(r58);
        goto L_0x0006;
    L_0x00c8:
        r30 = r32;
        r30 = (org.w3c.dom.Element) r30;
        r57 = "*";
        r58 = "activities";
        r0 = r30;
        r1 = r57;
        r2 = r58;
        r5 = r0.getElementsByTagNameNS(r1, r2);
        r57 = r5.getLength();
        if (r57 <= 0) goto L_0x0147;
    L_0x00e0:
        r4 = 0;
        r21 = 0;
    L_0x00e3:
        r57 = r5.getLength();
        r0 = r21;
        r1 = r57;
        if (r0 >= r1) goto L_0x0147;
    L_0x00ed:
        r0 = r21;
        r6 = r5.item(r0);
        r57 = r6.getNodeType();
        r58 = 1;
        r0 = r57;
        r1 = r58;
        if (r0 == r1) goto L_0x0102;
    L_0x00ff:
        r21 = r21 + 1;
        goto L_0x00e3;
    L_0x0102:
        r4 = r6;
        r4 = (org.w3c.dom.Element) r4;
        r47 = r4.getChildNodes();
        r24 = 0;
    L_0x010b:
        r57 = r47.getLength();
        r0 = r24;
        r1 = r57;
        if (r0 >= r1) goto L_0x0145;
    L_0x0115:
        r0 = r47;
        r1 = r24;
        r48 = r0.item(r1);
        r57 = r48.getNodeType();
        r58 = 1;
        r0 = r57;
        r1 = r58;
        if (r0 != r1) goto L_0x0230;
    L_0x0129:
        r49 = r48.getLocalName();
        r57 = "away";
        r0 = r49;
        r1 = r57;
        r57 = r0.equals(r1);
        if (r57 == 0) goto L_0x01fc;
    L_0x0139:
        r0 = r61;
        r0 = r0.sipStatusEnum;
        r57 = r0;
        r58 = "Away";
        r33 = r57.getStatus(r58);
    L_0x0145:
        if (r33 == 0) goto L_0x00ff;
    L_0x0147:
        r57 = "*";
        r58 = "status-icon";
        r0 = r30;
        r1 = r57;
        r2 = r58;
        r45 = r0.getElementsByTagNameNS(r1, r2);
        r57 = r45.getLength();
        if (r57 <= 0) goto L_0x018d;
    L_0x015b:
        r57 = 0;
        r0 = r45;
        r1 = r57;
        r46 = r0.item(r1);
        r57 = r46.getNodeType();
        r58 = 1;
        r0 = r57;
        r1 = r58;
        if (r0 != r1) goto L_0x018d;
    L_0x0171:
        r44 = r46;
        r44 = (org.w3c.dom.Element) r44;
        r0 = r61;
        r1 = r44;
        r16 = r0.getTextContent(r1);
        if (r16 == 0) goto L_0x018d;
    L_0x017f:
        r57 = r16.trim();
        r57 = r57.length();
        if (r57 == 0) goto L_0x018d;
    L_0x0189:
        r34 = java.net.URI.create(r16);	 Catch:{ IllegalArgumentException -> 0x0234 }
    L_0x018d:
        if (r34 == 0) goto L_0x01be;
    L_0x018f:
        r57 = "entity";
        r0 = r36;
        r1 = r57;
        r13 = org.jitsi.util.xml.XMLUtils.getAttribute(r0, r1);
        r57 = "pres:";
        r0 = r57;
        r57 = r13.startsWith(r0);
        if (r57 == 0) goto L_0x01af;
    L_0x01a3:
        r57 = "pres:";
        r57 = r57.length();
        r0 = r57;
        r13 = r13.substring(r0);
    L_0x01af:
        r0 = r61;
        r11 = r0.resolveContactID(r13);
        r11 = (net.java.sip.communicator.impl.protocol.sip.ContactSipImpl) r11;
        r0 = r61;
        r1 = r34;
        r0.updateContactIcon(r11, r1);
    L_0x01be:
        r26 = new java.util.Vector;
        r57 = 3;
        r58 = 2;
        r0 = r26;
        r1 = r57;
        r2 = r58;
        r0.<init>(r1, r2);
        r57 = "tuple";
        r0 = r61;
        r1 = r37;
        r2 = r57;
        r55 = r0.getPidfChilds(r1, r2);
        r21 = 0;
    L_0x01db:
        r57 = r55.getLength();
        r0 = r21;
        r1 = r57;
        if (r0 >= r1) goto L_0x057a;
    L_0x01e5:
        r0 = r55;
        r1 = r21;
        r56 = r0.item(r1);
        r57 = r56.getNodeType();
        r58 = 1;
        r0 = r57;
        r1 = r58;
        if (r0 == r1) goto L_0x0259;
    L_0x01f9:
        r21 = r21 + 1;
        goto L_0x01db;
    L_0x01fc:
        r57 = "busy";
        r0 = r49;
        r1 = r57;
        r57 = r0.equals(r1);
        if (r57 == 0) goto L_0x0216;
    L_0x0208:
        r0 = r61;
        r0 = r0.sipStatusEnum;
        r57 = r0;
        r58 = "Busy (DND)";
        r33 = r57.getStatus(r58);
        goto L_0x0145;
    L_0x0216:
        r57 = "on-the-phone";
        r0 = r49;
        r1 = r57;
        r57 = r0.equals(r1);
        if (r57 == 0) goto L_0x0230;
    L_0x0222:
        r0 = r61;
        r0 = r0.sipStatusEnum;
        r57 = r0;
        r58 = "On the phone";
        r33 = r57.getStatus(r58);
        goto L_0x0145;
    L_0x0230:
        r24 = r24 + 1;
        goto L_0x010b;
    L_0x0234:
        r20 = move-exception;
        r57 = logger;
        r58 = new java.lang.StringBuilder;
        r58.<init>();
        r59 = "Person's status icon uri: ";
        r58 = r58.append(r59);
        r0 = r58;
        r1 = r16;
        r58 = r0.append(r1);
        r59 = " is invalid";
        r58 = r58.append(r59);
        r58 = r58.toString();
        r57.error(r58);
        goto L_0x018d;
    L_0x0259:
        r54 = r56;
        r54 = (org.w3c.dom.Element) r54;
        r57 = "contact";
        r0 = r61;
        r1 = r54;
        r2 = r57;
        r14 = r0.getPidfChilds(r1, r2);
        r40 = new java.util.Vector;
        r57 = 1;
        r58 = 3;
        r0 = r40;
        r1 = r57;
        r2 = r58;
        r0.<init>(r1, r2);
        r13 = 0;
        r57 = r14.getLength();
        if (r57 != 0) goto L_0x02ef;
    L_0x027f:
        r57 = "entity";
        r0 = r36;
        r1 = r57;
        r13 = org.jitsi.util.xml.XMLUtils.getAttribute(r0, r1);
        r57 = "pres:";
        r0 = r57;
        r57 = r13.startsWith(r0);
        if (r57 == 0) goto L_0x029f;
    L_0x0293:
        r57 = "pres:";
        r57 = r57.length();
        r0 = r57;
        r13 = r13.substring(r0);
    L_0x029f:
        r0 = r61;
        r53 = r0.resolveContactID(r13);
        if (r53 == 0) goto L_0x02c5;
    L_0x02a7:
        r57 = 2;
        r0 = r57;
        r0 = new java.lang.Object[r0];
        r57 = r0;
        r58 = 0;
        r57[r58] = r53;
        r58 = 1;
        r59 = new java.lang.Float;
        r60 = 0;
        r59.<init>(r60);
        r57[r58] = r59;
        r0 = r40;
        r1 = r57;
        r0.add(r1);
    L_0x02c5:
        r57 = r40.isEmpty();
        if (r57 == 0) goto L_0x040b;
    L_0x02cb:
        r57 = logger;
        r57 = r57.isDebugEnabled();
        if (r57 == 0) goto L_0x01f9;
    L_0x02d3:
        r57 = logger;
        r58 = new java.lang.StringBuilder;
        r58.<init>();
        r59 = "no contact found for id: ";
        r58 = r58.append(r59);
        r0 = r58;
        r58 = r0.append(r13);
        r58 = r58.toString();
        r57.debug(r58);
        goto L_0x01f9;
    L_0x02ef:
        r24 = 0;
    L_0x02f1:
        r57 = r14.getLength();
        r0 = r24;
        r1 = r57;
        if (r0 >= r1) goto L_0x02c5;
    L_0x02fb:
        r0 = r24;
        r15 = r14.item(r0);
        r57 = r15.getNodeType();
        r58 = 1;
        r0 = r57;
        r1 = r58;
        if (r0 == r1) goto L_0x0310;
    L_0x030d:
        r24 = r24 + 1;
        goto L_0x02f1;
    L_0x0310:
        r11 = r15;
        r11 = (org.w3c.dom.Element) r11;
        r0 = r61;
        r13 = r0.getTextContent(r11);
        r57 = "pres:";
        r0 = r57;
        r57 = r13.startsWith(r0);
        if (r57 == 0) goto L_0x032f;
    L_0x0323:
        r57 = "pres:";
        r57 = r57.length();
        r0 = r57;
        r13 = r13.substring(r0);
    L_0x032f:
        r0 = r61;
        r53 = r0.resolveContactID(r13);
        if (r53 == 0) goto L_0x030d;
    L_0x0337:
        r57 = 2;
        r0 = r57;
        r0 = new java.lang.Object[r0];
        r50 = r0;
        r57 = "priority";
        r0 = r57;
        r39 = r11.getAttribute(r0);
        r38 = 0;
        if (r39 == 0) goto L_0x0351;
    L_0x034b:
        r57 = r39.length();	 Catch:{ NumberFormatException -> 0x03e5 }
        if (r57 != 0) goto L_0x03df;
    L_0x0351:
        r38 = new java.lang.Float;	 Catch:{ NumberFormatException -> 0x03e5 }
        r57 = 0;
        r0 = r38;
        r1 = r57;
        r0.<init>(r1);	 Catch:{ NumberFormatException -> 0x03e5 }
    L_0x035c:
        r57 = r38.floatValue();
        r58 = 0;
        r57 = (r57 > r58 ? 1 : (r57 == r58 ? 0 : -1));
        if (r57 >= 0) goto L_0x0371;
    L_0x0366:
        r38 = new java.lang.Float;
        r57 = 0;
        r0 = r38;
        r1 = r57;
        r0.<init>(r1);
    L_0x0371:
        r57 = r38.floatValue();
        r58 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r57 = (r57 > r58 ? 1 : (r57 == r58 ? 0 : -1));
        if (r57 <= 0) goto L_0x0386;
    L_0x037b:
        r38 = new java.lang.Float;
        r57 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r0 = r38;
        r1 = r57;
        r0.<init>(r1);
    L_0x0386:
        r57 = 0;
        r50[r57] = r53;
        r57 = 1;
        r50[r57] = r38;
        r12 = 0;
        r25 = 0;
    L_0x0391:
        r57 = r40.size();
        r0 = r25;
        r1 = r57;
        if (r0 >= r1) goto L_0x03d4;
    L_0x039b:
        r0 = r40;
        r1 = r25;
        r52 = r0.get(r1);
        r52 = (java.lang.Object[]) r52;
        r57 = 0;
        r57 = r52[r57];
        r0 = r57;
        r1 = r53;
        r57 = r0.equals(r1);
        if (r57 == 0) goto L_0x0408;
    L_0x03b3:
        r12 = 1;
        r57 = 1;
        r57 = r52[r57];
        r57 = (java.lang.Float) r57;
        r57 = r57.floatValue();
        r58 = r38.floatValue();
        r57 = (r57 > r58 ? 1 : (r57 == r58 ? 0 : -1));
        if (r57 >= 0) goto L_0x03d4;
    L_0x03c6:
        r0 = r40;
        r1 = r25;
        r0.remove(r1);
        r0 = r40;
        r1 = r50;
        r0.add(r1);
    L_0x03d4:
        if (r12 != 0) goto L_0x030d;
    L_0x03d6:
        r0 = r40;
        r1 = r50;
        r0.add(r1);
        goto L_0x030d;
    L_0x03df:
        r38 = java.lang.Float.valueOf(r39);	 Catch:{ NumberFormatException -> 0x03e5 }
        goto L_0x035c;
    L_0x03e5:
        r19 = move-exception;
        r57 = logger;
        r57 = r57.isDebugEnabled();
        if (r57 == 0) goto L_0x03fb;
    L_0x03ee:
        r57 = logger;
        r58 = "contact priority is not a valid float";
        r0 = r57;
        r1 = r58;
        r2 = r19;
        r0.debug(r1, r2);
    L_0x03fb:
        r38 = new java.lang.Float;
        r57 = 0;
        r0 = r38;
        r1 = r57;
        r0.<init>(r1);
        goto L_0x035c;
    L_0x0408:
        r25 = r25 + 1;
        goto L_0x0391;
    L_0x040b:
        r57 = "status";
        r0 = r61;
        r1 = r54;
        r2 = r57;
        r47 = r0.getPidfChilds(r1, r2);
        r57 = r47.getLength();
        r23 = r57 + -1;
        r48 = 0;
    L_0x041f:
        r0 = r47;
        r1 = r23;
        r51 = r0.item(r1);
        r57 = r51.getNodeType();
        r58 = 1;
        r0 = r57;
        r1 = r58;
        if (r0 != r1) goto L_0x0479;
    L_0x0433:
        r48 = r51;
    L_0x0435:
        r7 = 0;
        if (r48 != 0) goto L_0x047e;
    L_0x0438:
        r57 = logger;
        r57 = r57.isDebugEnabled();
        if (r57 == 0) goto L_0x0447;
    L_0x0440:
        r57 = logger;
        r58 = "no valid status in this tuple";
        r57.debug(r58);
    L_0x0447:
        r57 = "note";
        r0 = r61;
        r1 = r54;
        r2 = r57;
        r28 = r0.getPidfChilds(r1, r2);
        r10 = 0;
        r25 = 0;
    L_0x0456:
        r57 = r28.getLength();
        r0 = r25;
        r1 = r57;
        if (r0 >= r1) goto L_0x0503;
    L_0x0460:
        if (r10 != 0) goto L_0x0503;
    L_0x0462:
        r0 = r28;
        r1 = r25;
        r29 = r0.item(r1);
        r57 = r29.getNodeType();
        r58 = 1;
        r0 = r57;
        r1 = r58;
        if (r0 == r1) goto L_0x04c4;
    L_0x0476:
        r25 = r25 + 1;
        goto L_0x0456;
    L_0x0479:
        r23 = r23 + -1;
        if (r23 >= 0) goto L_0x041f;
    L_0x047d:
        goto L_0x0435;
    L_0x047e:
        r43 = r48;
        r43 = (org.w3c.dom.Element) r43;
        r57 = "basic";
        r0 = r61;
        r1 = r43;
        r2 = r57;
        r8 = r0.getPidfChilds(r1, r2);
        r57 = r8.getLength();
        r23 = r57 + -1;
        r9 = 0;
    L_0x0495:
        r0 = r23;
        r51 = r8.item(r0);
        r57 = r51.getNodeType();
        r58 = 1;
        r0 = r57;
        r1 = r58;
        if (r0 != r1) goto L_0x04bb;
    L_0x04a7:
        r9 = r51;
    L_0x04a9:
        if (r9 != 0) goto L_0x04c0;
    L_0x04ab:
        r57 = logger;
        r57 = r57.isDebugEnabled();
        if (r57 == 0) goto L_0x0447;
    L_0x04b3:
        r57 = logger;
        r58 = "no valid <basic> in this status";
        r57.debug(r58);
        goto L_0x0447;
    L_0x04bb:
        r23 = r23 + -1;
        if (r23 >= 0) goto L_0x0495;
    L_0x04bf:
        goto L_0x04a9;
    L_0x04c0:
        r7 = r9;
        r7 = (org.w3c.dom.Element) r7;
        goto L_0x0447;
    L_0x04c4:
        r27 = r29;
        r27 = (org.w3c.dom.Element) r27;
        r0 = r61;
        r1 = r27;
        r41 = r0.getTextContent(r1);
        r0 = r61;
        r0 = r0.sipStatusEnum;
        r57 = r0;
        r42 = r57.getSupportedStatusSet();
    L_0x04da:
        r57 = r42.hasNext();
        if (r57 == 0) goto L_0x0476;
    L_0x04e0:
        r17 = r42.next();
        r17 = (net.java.sip.communicator.service.protocol.PresenceStatus) r17;
        r57 = r17.getStatusName();
        r0 = r57;
        r1 = r41;
        r57 = r0.equalsIgnoreCase(r1);
        if (r57 == 0) goto L_0x04da;
    L_0x04f4:
        r10 = 1;
        r0 = r61;
        r1 = r17;
        r2 = r40;
        r3 = r26;
        r26 = r0.setStatusForContacts(r1, r2, r3);
        goto L_0x0476;
    L_0x0503:
        if (r10 != 0) goto L_0x0567;
    L_0x0505:
        if (r7 == 0) goto L_0x0567;
    L_0x0507:
        r0 = r61;
        r57 = r0.getTextContent(r7);
        r58 = "open";
        r57 = r57.equalsIgnoreCase(r58);
        if (r57 == 0) goto L_0x053f;
    L_0x0515:
        if (r33 == 0) goto L_0x0525;
    L_0x0517:
        r0 = r61;
        r1 = r33;
        r2 = r40;
        r3 = r26;
        r26 = r0.setStatusForContacts(r1, r2, r3);
        goto L_0x01f9;
    L_0x0525:
        r0 = r61;
        r0 = r0.sipStatusEnum;
        r57 = r0;
        r58 = "Online";
        r57 = r57.getStatus(r58);
        r0 = r61;
        r1 = r57;
        r2 = r40;
        r3 = r26;
        r26 = r0.setStatusForContacts(r1, r2, r3);
        goto L_0x01f9;
    L_0x053f:
        r0 = r61;
        r57 = r0.getTextContent(r7);
        r58 = "closed";
        r57 = r57.equalsIgnoreCase(r58);
        if (r57 == 0) goto L_0x01f9;
    L_0x054d:
        r0 = r61;
        r0 = r0.sipStatusEnum;
        r57 = r0;
        r58 = "Offline";
        r57 = r57.getStatus(r58);
        r0 = r61;
        r1 = r57;
        r2 = r40;
        r3 = r26;
        r26 = r0.setStatusForContacts(r1, r2, r3);
        goto L_0x01f9;
    L_0x0567:
        if (r10 != 0) goto L_0x01f9;
    L_0x0569:
        r57 = logger;
        r57 = r57.isDebugEnabled();
        if (r57 == 0) goto L_0x01f9;
    L_0x0571:
        r57 = logger;
        r58 = "no suitable presence state found in this tuple";
        r57.debug(r58);
        goto L_0x01f9;
    L_0x057a:
        r22 = r26.iterator();
    L_0x057e:
        r57 = r22.hasNext();
        if (r57 == 0) goto L_0x0006;
    L_0x0584:
        r50 = r22.next();
        r50 = (java.lang.Object[]) r50;
        r57 = 0;
        r11 = r50[r57];
        r11 = (net.java.sip.communicator.impl.protocol.sip.ContactSipImpl) r11;
        r57 = 2;
        r43 = r50[r57];
        r43 = (net.java.sip.communicator.service.protocol.PresenceStatus) r43;
        r0 = r61;
        r1 = r43;
        r0.changePresenceStatusForContact(r11, r1);
        goto L_0x057e;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.sip.OperationSetPresenceSipImpl.setPidfPresenceStatus(java.lang.String):void");
    }

    public void setWatcherInfoStatus(WatcherInfoSubscriberSubscription subscriber, String watcherInfoDoc) {
        if (this.authorizationHandler == null) {
            logger.warn("AuthorizationHandler missing!");
            return;
        }
        Document doc = convertDocument(watcherInfoDoc);
        if (doc != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("parsing:\n" + watcherInfoDoc);
            }
            NodeList watchList = doc.getElementsByTagNameNS(WATCHERINFO_NS_VALUE, WATCHERINFO_ELEMENT);
            if (watchList.getLength() == 0) {
                watchList = doc.getElementsByTagNameNS("*", WATCHERINFO_ELEMENT);
                if (watchList.getLength() == 0) {
                    logger.error("no watcherinfo element in this document");
                    return;
                }
            }
            if (watchList.getLength() > 1) {
                logger.warn("more than one watcherinfo element in this document");
            }
            Node watcherInfoNode = watchList.item(0);
            if (watcherInfoNode.getNodeType() != (short) 1) {
                logger.error("the watcherinfo node is not an element");
                return;
            }
            Element watcherInfo = (Element) watcherInfoNode;
            if (logger.isDebugEnabled()) {
                logger.debug("Watcherinfo is with state: " + watcherInfo.getAttribute("state"));
            }
            int currentVersion = -1;
            try {
                currentVersion = Integer.parseInt(watcherInfo.getAttribute("version"));
            } catch (Throwable t) {
                logger.error("Cannot parse version!", t);
            }
            if (currentVersion == -1 || currentVersion > subscriber.version) {
                subscriber.version = currentVersion;
                Element wlist = XMLUtils.locateElement(watcherInfo, WATCHERLIST_ELEMENT, RESOURCE_ATTRIBUTE, ((ProtocolProviderServiceSipImpl) this.parentProvider).getRegistrarConnection().getAddressOfRecord().getURI().toString());
                if (wlist == null || !wlist.getAttribute(PACKAGE_ATTRIBUTE).equals(PRESENCE_ELEMENT)) {
                    logger.error("Watcher list for us is missing in this document!");
                    return;
                }
                NodeList watcherList = wlist.getElementsByTagNameNS("*", WATCHER_ELEMENT);
                for (int i = 0; i < watcherList.getLength(); i++) {
                    Node watcherNode = watcherList.item(i);
                    if (watcherNode.getNodeType() != (short) 1) {
                        logger.error("the watcher node is not an element");
                        return;
                    }
                    Element watcher = (Element) watcherNode;
                    String status = watcher.getAttribute("status");
                    String contactID = getTextContent(watcher);
                    if (status == null || contactID == null) {
                        logger.warn("Status or contactID missing for watcher!");
                    } else if (status.equals("waiting") || status.equals(SubscriptionStateHeader.PENDING)) {
                        if (resolveContactID(contactID) != null) {
                            logger.warn("We are not supposed to have this contact in our list or its just rerequest of authorization!");
                            return;
                        }
                        ContactSipImpl contact = createVolatileContact(contactID);
                        AuthorizationResponse response = this.authorizationHandler.processAuthorisationRequest(new AuthorizationRequest(), contact);
                        if (response.getResponseCode() == AuthorizationResponse.ACCEPT) {
                            this.ssContactList.authorizationAccepted(contact);
                        } else {
                            if (response.getResponseCode() == AuthorizationResponse.REJECT) {
                                this.ssContactList.authorizationRejected(contact);
                            } else {
                                if (response.getResponseCode() == AuthorizationResponse.IGNORE) {
                                    this.ssContactList.authorizationIgnored(contact);
                                }
                            }
                        }
                    }
                }
                return;
            }
            logger.warn("Document version is old, ignore it.");
        }
    }

    public static boolean isEquals(URI uri1, URI uri2) {
        return (uri1 == null && uri2 == null) || (uri1 != null && uri1.equals(uri2));
    }

    private void updateContactIcon(ContactSipImpl contact, URI imageUri) {
        if (!isEquals(contact.getImageUri(), imageUri) && imageUri != null) {
            byte[] oldImage = contact.getImage();
            byte[] newImage = this.ssContactList.getImage(imageUri);
            if (oldImage != null || newImage != null) {
                contact.setImageUri(imageUri);
                contact.setImage(newImage);
                fireContactPropertyChangeEvent("Image", contact, oldImage, newImage);
            }
        }
    }

    private String getTextContent(Element node) {
        String res = XMLUtils.getText(node);
        if (res != null) {
            return res;
        }
        logger.warn("no text for element '" + node.getNodeName() + Separators.QUOTE);
        return "";
    }

    private NodeList getPidfChilds(Element element, String childName) {
        NodeList res = element.getElementsByTagNameNS(PIDF_NS_VALUE, childName);
        if (res.getLength() == 0) {
            return element.getElementsByTagNameNS("*", childName);
        }
        return res;
    }

    private List<Object[]> setStatusForContacts(PresenceStatus presenceState, Iterable<Object[]> contacts, List<Object[]> curStatus) {
        if (presenceState == null || contacts == null || curStatus == null) {
            return null;
        }
        for (Object[] tab : contacts) {
            Contact contact = tab[0];
            float priority = ((Float) tab[1]).floatValue();
            int pos = 0;
            boolean skip = false;
            int i = 0;
            while (i < curStatus.size()) {
                Object[] tab2 = (Object[]) curStatus.get(i);
                Contact curContact = tab2[0];
                float curPriority = ((Float) tab2[1]).floatValue();
                if (pos == 0 && curPriority <= priority) {
                    pos = i;
                }
                if (curContact.equals(contact)) {
                    if (curPriority > priority) {
                        skip = true;
                        break;
                    }
                    if (curPriority < priority) {
                        curStatus.remove(i);
                    } else if (tab2[2].getStatus() >= presenceState.getStatus()) {
                        skip = true;
                        break;
                    } else {
                        curStatus.remove(i);
                    }
                    i--;
                }
                i++;
            }
            if (!skip) {
                curStatus.add(pos, new Object[]{contact, new Float(priority), presenceState});
            }
        }
        return curStatus;
    }

    public void forcePollContact(ContactSipImpl contact) {
        if (this.presenceEnabled && contact.isResolvable() && contact.isPersistent()) {
            try {
                this.subscriber.poll(new PresenceSubscriberSubscription(contact));
            } catch (OperationFailedException ex) {
                logger.error("Failed to create and send the subcription", ex);
            }
        }
    }

    public void unsubscribeToAllContact() {
        if (logger.isDebugEnabled()) {
            logger.debug("Trying to unsubscribe to every contact");
        }
        for (ContactSipImpl contact : this.ssContactList.getUniqueContacts(this.ssContactList.getRootGroup())) {
            try {
                unsubscribe(contact, false);
            } catch (Throwable ex) {
                logger.error("Failed to unsubscribe to contact " + contact, ex);
            }
        }
    }

    private void unsubscribeToAllEventSubscribers() {
        if (this.watcherInfoSubscriber != null) {
            try {
                this.watcherInfoSubscriber.unsubscribe(((ProtocolProviderServiceSipImpl) this.parentProvider).getRegistrarConnection().getAddressOfRecord(), false);
            } catch (Throwable ex) {
                logger.error("Failed to send the unsubscription for watcher info.", ex);
            }
        }
    }

    public void setDisplayName(Contact contact, String newName) throws IllegalArgumentException {
        assertConnected();
        if (contact instanceof ContactSipImpl) {
            this.ssContactList.renameContact((ContactSipImpl) contact, newName);
            return;
        }
        throw new IllegalArgumentException("The contact is not a SIP contact");
    }

    /* access modifiers changed from: protected */
    public ServerStoredContactList getSsContactList() {
        return this.ssContactList;
    }

    private void cancelTimer() {
        if (this.republishTask != null) {
            this.republishTask = null;
        }
        if (this.pollingTask != null) {
            this.pollingTask = null;
        }
        this.timer.cancel();
    }

    /* JADX WARNING: Missing block: B:10:0x0013, code skipped:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:13:?, code skipped:
            wait(500);
     */
    /* JADX WARNING: Missing block: B:15:?, code skipped:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:16:0x001a, code skipped:
            r1 = (byte) (r1 + 1);
     */
    /* JADX WARNING: Missing block: B:21:0x0021, code skipped:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:24:0x0028, code skipped:
            if (logger.isDebugEnabled() != false) goto L_0x002a;
     */
    /* JADX WARNING: Missing block: B:25:0x002a, code skipped:
            logger.debug("abnormal behavior, may cause unnecessary CPU use", r0);
     */
    private void stopEvents() {
        /*
        r4 = this;
        r1 = 0;
    L_0x0001:
        r2 = 10;
        if (r1 >= r2) goto L_0x0011;
    L_0x0005:
        r3 = r4.waitedCallIds;
        monitor-enter(r3);
        r2 = r4.waitedCallIds;	 Catch:{ all -> 0x001e }
        r2 = r2.size();	 Catch:{ all -> 0x001e }
        if (r2 != 0) goto L_0x0012;
    L_0x0010:
        monitor-exit(r3);	 Catch:{ all -> 0x001e }
    L_0x0011:
        return;
    L_0x0012:
        monitor-exit(r3);	 Catch:{ all -> 0x001e }
        monitor-enter(r4);
        r2 = 500; // 0x1f4 float:7.0E-43 double:2.47E-321;
        r4.wait(r2);	 Catch:{ InterruptedException -> 0x0021 }
    L_0x0019:
        monitor-exit(r4);	 Catch:{ all -> 0x0032 }
        r2 = r1 + 1;
        r1 = (byte) r2;
        goto L_0x0001;
    L_0x001e:
        r2 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x001e }
        throw r2;
    L_0x0021:
        r0 = move-exception;
        r2 = logger;	 Catch:{ all -> 0x0032 }
        r2 = r2.isDebugEnabled();	 Catch:{ all -> 0x0032 }
        if (r2 == 0) goto L_0x0019;
    L_0x002a:
        r2 = logger;	 Catch:{ all -> 0x0032 }
        r3 = "abnormal behavior, may cause unnecessary CPU use";
        r2.debug(r3, r0);	 Catch:{ all -> 0x0032 }
        goto L_0x0019;
    L_0x0032:
        r2 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x0032 }
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.sip.OperationSetPresenceSipImpl.stopEvents():void");
    }

    public void registrationStateChanged(RegistrationStateChangeEvent evt) {
        if (evt.getNewState().equals(RegistrationState.UNREGISTERING)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Enter unregistering state");
            }
            cancelTimer();
            this.ssContactList.destroy();
            try {
                publishPresenceStatus(this.sipStatusEnum.getStatus(SipStatusEnum.OFFLINE), "");
            } catch (OperationFailedException e) {
                logger.error("can't set the offline mode", e);
            }
            stopEvents();
        } else if (evt.getNewState().equals(RegistrationState.REGISTERED)) {
            if (logger.isDebugEnabled()) {
                logger.debug("enter registered state");
            }
            this.ssContactList.init();
            if (this.presenceEnabled && this.pollingTask == null) {
                for (ContactSipImpl contact : this.ssContactList.getAllContacts(this.ssContactList.getRootGroup())) {
                    forcePollContact(contact);
                }
                this.pollingTask = new PollOfflineContactsTask(this, null);
                this.timer.schedule(this.pollingTask, (long) this.pollingTaskPeriod, (long) this.pollingTaskPeriod);
                if (this.useDistantPA) {
                    try {
                        this.watcherInfoSubscriber.subscribe(new WatcherInfoSubscriberSubscription(((ProtocolProviderServiceSipImpl) this.parentProvider).getRegistrarConnection().getAddressOfRecord()));
                    } catch (OperationFailedException ex) {
                        logger.error("Failed to create and send the subcription for watcher info.", ex);
                    }
                }
            }
        } else if (evt.getNewState().equals(RegistrationState.CONNECTION_FAILED) || evt.getNewState().equals(RegistrationState.AUTHENTICATION_FAILED)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Enter connction failed state");
            }
            this.ssContactList.destroy();
            for (ContactSipImpl contact2 : this.ssContactList.getAllContacts(this.ssContactList.getRootGroup())) {
                PresenceStatus oldContactStatus = contact2.getPresenceStatus();
                if (this.subscriber != null) {
                    try {
                        this.subscriber.removeSubscription(getAddress(contact2));
                    } catch (OperationFailedException e2) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Failed to remove subscription to contact " + contact2);
                        }
                    }
                }
                if (oldContactStatus.isOnline()) {
                    contact2.setPresenceStatus(this.sipStatusEnum.getStatus(SipStatusEnum.OFFLINE));
                    fireContactPresenceStatusChangeEvent(contact2, contact2.getParentContactGroup(), oldContactStatus);
                }
            }
            if (this.useDistantPA) {
                try {
                    this.watcherInfoSubscriber.removeSubscription(((ProtocolProviderServiceSipImpl) this.parentProvider).getRegistrarConnection().getAddressOfRecord());
                } catch (Throwable ex2) {
                    logger.error("Failed to remove subscription for watcher info.", ex2);
                }
            }
            cancelTimer();
            this.waitedCallIds.clear();
            PresenceStatus oldStatus = this.presenceStatus;
            this.presenceStatus = this.sipStatusEnum.getStatus(SipStatusEnum.OFFLINE);
            fireProviderStatusChangeEvent(oldStatus);
        }
    }

    /* access modifiers changed from: private */
    public Address getAddress(ContactSipImpl contact) throws OperationFailedException {
        try {
            return ((ProtocolProviderServiceSipImpl) this.parentProvider).parseAddressString(contact.getAddress());
        } catch (ParseException ex) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("An unexpected error occurred while constructing the address", 4, ex, logger);
            return null;
        }
    }

    /* access modifiers changed from: 0000 */
    public void shutdown() {
        ((ProtocolProviderServiceSipImpl) this.parentProvider).removeRegistrationStateChangeListener(this);
    }
}
