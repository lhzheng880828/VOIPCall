package net.java.sip.communicator.impl.protocol.sip;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.java.sip.communicator.impl.protocol.sip.net.AutoProxyConnection;
import net.java.sip.communicator.impl.protocol.sip.net.ProxyConnection;
import net.java.sip.communicator.impl.protocol.sip.security.SipSecurityManager;
import net.java.sip.communicator.service.dns.DnssecException;
import net.java.sip.communicator.service.protocol.AbstractProtocolProviderService;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSet;
import net.java.sip.communicator.service.protocol.OperationSetAdvancedAutoAnswer;
import net.java.sip.communicator.service.protocol.OperationSetAdvancedTelephony;
import net.java.sip.communicator.service.protocol.OperationSetAvatar;
import net.java.sip.communicator.service.protocol.OperationSetBasicAutoAnswer;
import net.java.sip.communicator.service.protocol.OperationSetBasicInstantMessaging;
import net.java.sip.communicator.service.protocol.OperationSetBasicTelephony;
import net.java.sip.communicator.service.protocol.OperationSetCusaxUtils;
import net.java.sip.communicator.service.protocol.OperationSetDTMF;
import net.java.sip.communicator.service.protocol.OperationSetDesktopSharingClient;
import net.java.sip.communicator.service.protocol.OperationSetDesktopSharingServer;
import net.java.sip.communicator.service.protocol.OperationSetDesktopStreaming;
import net.java.sip.communicator.service.protocol.OperationSetIncomingDTMF;
import net.java.sip.communicator.service.protocol.OperationSetInstantMessageTransform;
import net.java.sip.communicator.service.protocol.OperationSetInstantMessageTransformImpl;
import net.java.sip.communicator.service.protocol.OperationSetMessageWaiting;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.OperationSetPresence;
import net.java.sip.communicator.service.protocol.OperationSetSecureSDesTelephony;
import net.java.sip.communicator.service.protocol.OperationSetSecureZrtpTelephony;
import net.java.sip.communicator.service.protocol.OperationSetServerStoredAccountInfo;
import net.java.sip.communicator.service.protocol.OperationSetTelephonyConferencing;
import net.java.sip.communicator.service.protocol.OperationSetTypingNotifications;
import net.java.sip.communicator.service.protocol.OperationSetVideoTelephony;
import net.java.sip.communicator.service.protocol.ProtocolIcon;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.SecurityAuthority;
import net.java.sip.communicator.service.protocol.TransportProtocol;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.address.AddressFactoryEx;
import org.jitsi.gov.nist.javax.sip.address.AddressFactoryImpl;
import org.jitsi.gov.nist.javax.sip.header.HeaderFactoryImpl;
import org.jitsi.gov.nist.javax.sip.message.MessageFactoryImpl;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.DialogTerminatedEvent;
import org.jitsi.javax.sip.IOExceptionEvent;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.ListeningPoint;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.ServerTransaction;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.SipListener;
import org.jitsi.javax.sip.SipProvider;
import org.jitsi.javax.sip.TimeoutEvent;
import org.jitsi.javax.sip.Transaction;
import org.jitsi.javax.sip.TransactionAlreadyExistsException;
import org.jitsi.javax.sip.TransactionState;
import org.jitsi.javax.sip.TransactionTerminatedEvent;
import org.jitsi.javax.sip.TransactionUnavailableException;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.address.SipURI;
import org.jitsi.javax.sip.address.URI;
import org.jitsi.javax.sip.header.CSeqHeader;
import org.jitsi.javax.sip.header.ContactHeader;
import org.jitsi.javax.sip.header.EventHeader;
import org.jitsi.javax.sip.header.Header;
import org.jitsi.javax.sip.header.HeaderFactory;
import org.jitsi.javax.sip.header.MaxForwardsHeader;
import org.jitsi.javax.sip.header.UserAgentHeader;
import org.jitsi.javax.sip.header.ViaHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;
import org.jitsi.service.version.Version;
import org.jitsi.util.StringUtils;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class ProtocolProviderServiceSipImpl extends AbstractProtocolProviderService implements SipListener, RegistrationStateChangeListener {
    private static final String DEFAULT_TRANSPORT = "net.java.sip.communicator.impl.protocol.sip.DEFAULT_TRANSPORT";
    private static final String IS_CALLING_DISABLED = "net.java.sip.communicator.impl.protocol.sip.CALLING_DISABLED";
    private static final String IS_DESKTOP_STREAMING_DISABLED = "net.java.sip.communicator.impl.protocol.sip.DESKTOP_STREAMING_DISABLED";
    private static final String IS_MESSAGING_DISABLED = "net.java.sip.communicator.impl.protocol.sip.MESSAGING_DISABLED";
    private static final int MAX_FORWARDS = 70;
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(ProtocolProviderServiceSipImpl.class);
    private static SipStackSharing sipStackSharing = null;
    private AccountID accountID = null;
    /* access modifiers changed from: private */
    public AddressFactoryEx addressFactory;
    /* access modifiers changed from: private */
    public ClientCapabilities capabilities;
    /* access modifiers changed from: private */
    public ProxyConnection connection;
    private final List<SipMessageProcessor> earlyProcessors = new ArrayList();
    private boolean forceLooseRouting = false;
    /* access modifiers changed from: private */
    public HeaderFactory headerFactory;
    private final Object initializationLock = new Object();
    /* access modifiers changed from: private */
    public boolean isInitialized = false;
    private MaxForwardsHeader maxForwardsHeader = null;
    /* access modifiers changed from: private */
    public SipMessageFactory messageFactory;
    /* access modifiers changed from: private|final */
    public final Hashtable<String, List<MethodProcessor>> methodProcessors = new Hashtable();
    /* access modifiers changed from: private */
    public OperationSetBasicInstantMessagingSipImpl opSetBasicIM;
    /* access modifiers changed from: private */
    public OperationSetMessageWaitingSipImpl opSetMWI;
    /* access modifiers changed from: private */
    public OperationSetPresenceSipImpl opSetPersPresence;
    /* access modifiers changed from: private */
    public OperationSetServerStoredAccountInfoSipImpl opSetSSAccountInfo;
    /* access modifiers changed from: private */
    public OperationSetTypingNotificationsSipImpl opSetTypingNotif;
    private String ourDisplayName = null;
    private ProtocolIconSipImpl protocolIcon;
    private final List<String> registeredEvents = new ArrayList();
    private SipRegistrarConnection sipRegistrarConnection = null;
    /* access modifiers changed from: private */
    public SipSecurityManager sipSecurityManager = null;
    private SipStatusEnum sipStatusEnum;
    private UserAgentHeader userAgentHeader = null;

    protected class ShutdownThread implements Runnable {
        protected ShutdownThread() {
        }

        public void run() {
            if (ProtocolProviderServiceSipImpl.logger.isTraceEnabled()) {
                ProtocolProviderServiceSipImpl.logger.trace("Killing the SIP Protocol Provider.");
            }
            ((OperationSetBasicTelephonySipImpl) ProtocolProviderServiceSipImpl.this.getOperationSet(OperationSetBasicTelephony.class)).shutdown();
            if (ProtocolProviderServiceSipImpl.this.isRegistered()) {
                try {
                    ShutdownUnregistrationBlockListener listener = new ShutdownUnregistrationBlockListener();
                    ProtocolProviderServiceSipImpl.this.addRegistrationStateChangeListener(listener);
                    ProtocolProviderServiceSipImpl.this.unregister();
                    listener.waitForEvent(3000);
                } catch (OperationFailedException ex) {
                    ProtocolProviderServiceSipImpl.logger.error("Failed to properly unregister before shutting down. " + ProtocolProviderServiceSipImpl.this.getAccountID(), ex);
                }
            }
            if (ProtocolProviderServiceSipImpl.this.capabilities != null) {
                ProtocolProviderServiceSipImpl.this.capabilities.shutdown();
                ProtocolProviderServiceSipImpl.this.capabilities = null;
            }
            if (ProtocolProviderServiceSipImpl.this.opSetPersPresence != null) {
                ProtocolProviderServiceSipImpl.this.opSetPersPresence.shutdown();
                ProtocolProviderServiceSipImpl.this.opSetPersPresence = null;
            }
            if (ProtocolProviderServiceSipImpl.this.opSetBasicIM != null) {
                ProtocolProviderServiceSipImpl.this.opSetBasicIM.shutdown();
                ProtocolProviderServiceSipImpl.this.opSetBasicIM = null;
            }
            if (ProtocolProviderServiceSipImpl.this.opSetMWI != null) {
                ProtocolProviderServiceSipImpl.this.opSetMWI.shutdown();
                ProtocolProviderServiceSipImpl.this.opSetMWI = null;
            }
            if (ProtocolProviderServiceSipImpl.this.opSetSSAccountInfo != null) {
                ProtocolProviderServiceSipImpl.this.opSetSSAccountInfo.shutdown();
                ProtocolProviderServiceSipImpl.this.opSetSSAccountInfo = null;
            }
            if (ProtocolProviderServiceSipImpl.this.opSetTypingNotif != null) {
                ProtocolProviderServiceSipImpl.this.opSetTypingNotif.shutdown();
                ProtocolProviderServiceSipImpl.this.opSetTypingNotif = null;
            }
            ProtocolProviderServiceSipImpl.this.headerFactory = null;
            ProtocolProviderServiceSipImpl.this.messageFactory = null;
            ProtocolProviderServiceSipImpl.this.addressFactory = null;
            ProtocolProviderServiceSipImpl.this.sipSecurityManager = null;
            ProtocolProviderServiceSipImpl.this.connection = null;
            ProtocolProviderServiceSipImpl.this.methodProcessors.clear();
            ProtocolProviderServiceSipImpl.this.isInitialized = false;
        }
    }

    private static class ShutdownUnregistrationBlockListener implements RegistrationStateChangeListener {
        public List<RegistrationState> collectedNewStates;

        private ShutdownUnregistrationBlockListener() {
            this.collectedNewStates = new LinkedList();
        }

        public void registrationStateChanged(RegistrationStateChangeEvent evt) {
            if (ProtocolProviderServiceSipImpl.logger.isDebugEnabled()) {
                ProtocolProviderServiceSipImpl.logger.debug("Received a RegistrationStateChangeEvent: " + evt);
            }
            this.collectedNewStates.add(evt.getNewState());
            if (evt.getNewState().equals(RegistrationState.UNREGISTERED)) {
                if (ProtocolProviderServiceSipImpl.logger.isDebugEnabled()) {
                    ProtocolProviderServiceSipImpl.logger.debug("We're unregistered and will notify those who wait");
                }
                synchronized (this) {
                    notifyAll();
                }
            }
        }

        /* JADX WARNING: Missing block: B:32:?, code skipped:
            return;
     */
        public void waitForEvent(long r6) {
            /*
            r5 = this;
            r1 = net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl.logger;
            r1 = r1.isTraceEnabled();
            if (r1 == 0) goto L_0x0013;
        L_0x000a:
            r1 = net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl.logger;
            r2 = "Waiting for a RegistrationStateChangeEvent.UNREGISTERED";
            r1.trace(r2);
        L_0x0013:
            monitor-enter(r5);
            r1 = r5.collectedNewStates;	 Catch:{ all -> 0x0066 }
            r2 = net.java.sip.communicator.service.protocol.RegistrationState.UNREGISTERED;	 Catch:{ all -> 0x0066 }
            r1 = r1.contains(r2);	 Catch:{ all -> 0x0066 }
            if (r1 == 0) goto L_0x0046;
        L_0x001e:
            r1 = net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl.logger;	 Catch:{ all -> 0x0066 }
            r1 = r1.isTraceEnabled();	 Catch:{ all -> 0x0066 }
            if (r1 == 0) goto L_0x0044;
        L_0x0028:
            r1 = net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl.logger;	 Catch:{ all -> 0x0066 }
            r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0066 }
            r2.<init>();	 Catch:{ all -> 0x0066 }
            r3 = "Event already received. ";
            r2 = r2.append(r3);	 Catch:{ all -> 0x0066 }
            r3 = r5.collectedNewStates;	 Catch:{ all -> 0x0066 }
            r2 = r2.append(r3);	 Catch:{ all -> 0x0066 }
            r2 = r2.toString();	 Catch:{ all -> 0x0066 }
            r1.trace(r2);	 Catch:{ all -> 0x0066 }
        L_0x0044:
            monitor-exit(r5);	 Catch:{ all -> 0x0066 }
        L_0x0045:
            return;
        L_0x0046:
            r5.wait(r6);	 Catch:{ InterruptedException -> 0x0094 }
            r1 = r5.collectedNewStates;	 Catch:{ InterruptedException -> 0x0094 }
            r1 = r1.size();	 Catch:{ InterruptedException -> 0x0094 }
            if (r1 <= 0) goto L_0x0064;
        L_0x0051:
            r1 = net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl.logger;	 Catch:{ InterruptedException -> 0x0094 }
            r1 = r1.isTraceEnabled();	 Catch:{ InterruptedException -> 0x0094 }
            if (r1 == 0) goto L_0x0069;
        L_0x005b:
            r1 = net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl.logger;	 Catch:{ InterruptedException -> 0x0094 }
            r2 = "Received a RegistrationStateChangeEvent.";
            r1.trace(r2);	 Catch:{ InterruptedException -> 0x0094 }
        L_0x0064:
            monitor-exit(r5);	 Catch:{ all -> 0x0066 }
            goto L_0x0045;
        L_0x0066:
            r1 = move-exception;
            monitor-exit(r5);	 Catch:{ all -> 0x0066 }
            throw r1;
        L_0x0069:
            r1 = net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl.logger;	 Catch:{ InterruptedException -> 0x0094 }
            r1 = r1.isTraceEnabled();	 Catch:{ InterruptedException -> 0x0094 }
            if (r1 == 0) goto L_0x0064;
        L_0x0073:
            r1 = net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl.logger;	 Catch:{ InterruptedException -> 0x0094 }
            r2 = new java.lang.StringBuilder;	 Catch:{ InterruptedException -> 0x0094 }
            r2.<init>();	 Catch:{ InterruptedException -> 0x0094 }
            r3 = "No RegistrationStateChangeEvent received for ";
            r2 = r2.append(r3);	 Catch:{ InterruptedException -> 0x0094 }
            r2 = r2.append(r6);	 Catch:{ InterruptedException -> 0x0094 }
            r3 = "ms.";
            r2 = r2.append(r3);	 Catch:{ InterruptedException -> 0x0094 }
            r2 = r2.toString();	 Catch:{ InterruptedException -> 0x0094 }
            r1.trace(r2);	 Catch:{ InterruptedException -> 0x0094 }
            goto L_0x0064;
        L_0x0094:
            r0 = move-exception;
            r1 = net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl.logger;	 Catch:{ all -> 0x0066 }
            r1 = r1.isDebugEnabled();	 Catch:{ all -> 0x0066 }
            if (r1 == 0) goto L_0x0064;
        L_0x009f:
            r1 = net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl.logger;	 Catch:{ all -> 0x0066 }
            r2 = "Interrupted while waiting for a RegistrationStateChangeEvent";
            r1.debug(r2, r0);	 Catch:{ all -> 0x0066 }
            goto L_0x0064;
            */
            throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl$ShutdownUnregistrationBlockListener.waitForEvent(long):void");
        }
    }

    public AccountID getAccountID() {
        return this.accountID;
    }

    public RegistrationState getRegistrationState() {
        if (this.sipRegistrarConnection == null) {
            return RegistrationState.UNREGISTERED;
        }
        return this.sipRegistrarConnection.getRegistrationState();
    }

    public String getProtocolName() {
        return "SIP";
    }

    public void registerEvent(String event) {
        synchronized (this.registeredEvents) {
            if (!this.registeredEvents.contains(event)) {
                this.registeredEvents.add(event);
            }
        }
    }

    public List<String> getKnownEventsList() {
        return this.registeredEvents;
    }

    public void register(SecurityAuthority authority) throws OperationFailedException {
        if (!this.isInitialized) {
            throw new OperationFailedException("Provided must be initialized before being able to register.", 1);
        } else if (!isRegistered()) {
            this.forceLooseRouting = getAccountID().getAccountPropertyBoolean("FORCE_PROXY_BYPASS", false);
            sipStackSharing.addSipListener(this);
            addRegistrationStateChangeListener(this);
            authority.setUserNameEditable(true);
            this.sipSecurityManager.setSecurityAuthority(authority);
            initRegistrarConnection();
            this.connection = ProxyConnection.create(this);
            if (!registerUsingNextAddress()) {
                logger.error("No address found for " + this);
                fireRegistrationStateChanged(RegistrationState.REGISTERING, RegistrationState.CONNECTION_FAILED, 8, "Invalid or inaccessible server address.");
            }
        }
    }

    public void unregister() throws OperationFailedException {
        if (!getRegistrationState().equals(RegistrationState.UNREGISTERED) && !getRegistrationState().equals(RegistrationState.UNREGISTERING) && !getRegistrationState().equals(RegistrationState.CONNECTION_FAILED)) {
            this.sipRegistrarConnection.unregister();
            this.sipSecurityManager.setSecurityAuthority(null);
        }
    }

    /* access modifiers changed from: protected */
    public void initialize(String sipAddress, SipAccountIDImpl accountID) throws OperationFailedException, IllegalArgumentException {
        synchronized (this.initializationLock) {
            this.accountID = accountID;
            String protocolIconPath = accountID.getAccountPropertyString("PROTOCOL_ICON_PATH");
            if (protocolIconPath == null) {
                protocolIconPath = "resources/images/protocol/sip";
            }
            this.protocolIcon = new ProtocolIconSipImpl(protocolIconPath);
            this.sipStatusEnum = new SipStatusEnum(protocolIconPath);
            if (sipStackSharing == null) {
                sipStackSharing = new SipStackSharing();
            }
            boolean enablePresence = accountID.getAccountPropertyBoolean("IS_PRESENCE_ENABLED", true);
            boolean forceP2P = accountID.getAccountPropertyBoolean("FORCE_P2P_MODE", true);
            int pollingValue = accountID.getAccountPropertyInt("POLLING_PERIOD", 30);
            int subscriptionExpiration = accountID.getAccountPropertyInt("SUBSCRIPTION_EXPIRATION", DesktopSharingProtocolSipImpl.SUBSCRIPTION_DURATION);
            this.headerFactory = new HeaderFactoryImpl();
            this.addressFactory = new AddressFactoryImpl();
            this.ourDisplayName = accountID.getAccountPropertyString("DISPLAY_NAME");
            if (this.ourDisplayName == null || this.ourDisplayName.trim().length() == 0) {
                this.ourDisplayName = accountID.getUserID();
            }
            OperationSetBasicTelephonySipImpl opSetBasicTelephonySipImpl = new OperationSetBasicTelephonySipImpl(this);
            boolean isCallingDisabled = SipActivator.getConfigurationService().getBoolean(IS_CALLING_DISABLED, false);
            boolean isCallingDisabledForAccount = accountID.getAccountPropertyBoolean("CALLING_DISABLED", false);
            if (!(isCallingDisabled || isCallingDisabledForAccount)) {
                addSupportedOperationSet(OperationSetBasicTelephony.class, opSetBasicTelephonySipImpl);
                addSupportedOperationSet(OperationSetAdvancedTelephony.class, opSetBasicTelephonySipImpl);
                OperationSetAutoAnswerSipImpl autoAnswerOpSet = new OperationSetAutoAnswerSipImpl(this);
                addSupportedOperationSet(OperationSetBasicAutoAnswer.class, autoAnswerOpSet);
                addSupportedOperationSet(OperationSetAdvancedAutoAnswer.class, autoAnswerOpSet);
                addSupportedOperationSet(OperationSetSecureZrtpTelephony.class, opSetBasicTelephonySipImpl);
                addSupportedOperationSet(OperationSetSecureSDesTelephony.class, opSetBasicTelephonySipImpl);
                addSupportedOperationSet(OperationSetVideoTelephony.class, new OperationSetVideoTelephonySipImpl(opSetBasicTelephonySipImpl));
                addSupportedOperationSet(OperationSetTelephonyConferencing.class, new OperationSetTelephonyConferencingSipImpl(this));
                OperationSetDTMFSipImpl operationSetDTMFSip = new OperationSetDTMFSipImpl(this);
                addSupportedOperationSet(OperationSetDTMF.class, operationSetDTMFSip);
                addSupportedOperationSet(OperationSetIncomingDTMF.class, new OperationSetIncomingDTMFSipImpl(this, operationSetDTMFSip));
                boolean isDesktopStreamingDisabled = SipActivator.getConfigurationService().getBoolean(IS_DESKTOP_STREAMING_DISABLED, false);
                boolean isAccountDesktopStreamingDisabled = accountID.getAccountPropertyBoolean("DESKTOP_STREAMING_DISABLED", false);
                if (!(isDesktopStreamingDisabled || isAccountDesktopStreamingDisabled)) {
                    addSupportedOperationSet(OperationSetDesktopStreaming.class, new OperationSetDesktopStreamingSipImpl(opSetBasicTelephonySipImpl));
                    addSupportedOperationSet(OperationSetDesktopSharingServer.class, new OperationSetDesktopSharingServerSipImpl(opSetBasicTelephonySipImpl));
                    addSupportedOperationSet(OperationSetDesktopSharingClient.class, new OperationSetDesktopSharingClientSipImpl(this));
                }
            }
            if (enablePresence) {
                this.opSetPersPresence = new OperationSetPresenceSipImpl(this, enablePresence, forceP2P, pollingValue, subscriptionExpiration);
                addSupportedOperationSet(OperationSetPersistentPresence.class, this.opSetPersPresence);
                addSupportedOperationSet(OperationSetPresence.class, this.opSetPersPresence);
                if (!SipActivator.getConfigurationService().getBoolean(IS_MESSAGING_DISABLED, false)) {
                    this.opSetBasicIM = new OperationSetBasicInstantMessagingSipImpl(this);
                    addSupportedOperationSet(OperationSetBasicInstantMessaging.class, this.opSetBasicIM);
                    this.opSetTypingNotif = new OperationSetTypingNotificationsSipImpl(this, this.opSetBasicIM);
                    addSupportedOperationSet(OperationSetTypingNotifications.class, this.opSetTypingNotif);
                    addSupportedOperationSet(OperationSetInstantMessageTransform.class, new OperationSetInstantMessageTransformImpl());
                }
                this.opSetSSAccountInfo = new OperationSetServerStoredAccountInfoSipImpl(this);
                this.opSetSSAccountInfo.setOurDisplayName(this.ourDisplayName);
                addSupportedOperationSet(OperationSetServerStoredAccountInfo.class, this.opSetSSAccountInfo);
                addSupportedOperationSet(OperationSetAvatar.class, new OperationSetAvatarSipImpl(this, this.opSetSSAccountInfo));
            }
            if (accountID.getAccountPropertyBoolean("VOICEMAIL_ENABLED", true)) {
                this.opSetMWI = new OperationSetMessageWaitingSipImpl(this);
                addSupportedOperationSet(OperationSetMessageWaiting.class, this.opSetMWI);
            }
            if (getAccountID().getAccountPropertyString("cusax.XMPP_ACCOUNT_ID") != null) {
                addSupportedOperationSet(OperationSetCusaxUtils.class, new OperationSetCusaxUtilsSipImpl(this));
            }
            this.capabilities = new ClientCapabilities(this);
            this.sipSecurityManager = new SipSecurityManager(accountID, this);
            this.sipSecurityManager.setHeaderFactory(this.headerFactory);
            ProtocolProviderExtensions.registerCustomOperationSets(this);
            this.isInitialized = true;
        }
    }

    /* access modifiers changed from: protected */
    public <T extends OperationSet> void addSupportedOperationSet(Class<T> opsetClass, T opset) {
        ProtocolProviderServiceSipImpl.super.addSupportedOperationSet(opsetClass, opset);
    }

    /* access modifiers changed from: protected */
    public <T extends OperationSet> void removeSupportedOperationSet(Class<T> opsetClass) {
        ProtocolProviderServiceSipImpl.super.removeSupportedOperationSet(opsetClass);
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
    }

    public void processResponse(ResponseEvent responseEvent) {
        if (responseEvent.getClientTransaction() != null) {
            Response response = responseEvent.getResponse();
            earlyProcessMessage(responseEvent);
            String method = ((CSeqHeader) response.getHeader("CSeq")).getMethod();
            List<MethodProcessor> processors = (List) this.methodProcessors.get(method);
            if (processors != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found " + processors.size() + " processor(s) for method " + method);
                }
                for (MethodProcessor processor : processors) {
                    if (processor.processResponse(responseEvent)) {
                        return;
                    }
                }
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("ignoring a transactionless response");
        }
    }

    public void processTimeout(TimeoutEvent timeoutEvent) {
        Transaction transaction;
        if (timeoutEvent.isServerTransaction()) {
            transaction = timeoutEvent.getServerTransaction();
        } else {
            transaction = timeoutEvent.getClientTransaction();
        }
        if (transaction != null) {
            earlyProcessMessage(timeoutEvent);
            Request request = transaction.getRequest();
            if (logger.isDebugEnabled()) {
                logger.debug("received timeout for req=" + request);
            }
            String method = request.getMethod();
            List<MethodProcessor> processors = (List) this.methodProcessors.get(method);
            if (processors != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found " + processors.size() + " processor(s) for method " + method);
                }
                for (MethodProcessor processor : processors) {
                    if (processor.processTimeout(timeoutEvent)) {
                        return;
                    }
                }
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("ignoring a transactionless timeout event");
        }
    }

    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        Transaction transaction;
        if (transactionTerminatedEvent.isServerTransaction()) {
            transaction = transactionTerminatedEvent.getServerTransaction();
        } else {
            transaction = transactionTerminatedEvent.getClientTransaction();
        }
        if (transaction != null) {
            String method = transaction.getRequest().getMethod();
            List<MethodProcessor> processors = (List) this.methodProcessors.get(method);
            if (processors != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found " + processors.size() + " processor(s) for method " + method);
                }
                for (MethodProcessor processor : processors) {
                    if (processor.processTransactionTerminated(transactionTerminatedEvent)) {
                        return;
                    }
                }
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("ignoring a transactionless transaction terminated event");
        }
    }

    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        if (logger.isDebugEnabled()) {
            logger.debug("Dialog terminated for req=" + dialogTerminatedEvent.getDialog());
        }
    }

    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        if (this.sipRegistrarConnection == null || this.sipRegistrarConnection.isRegistrarless() || this.sipRegistrarConnection.isRequestFromSameConnection(request) || this.forceLooseRouting) {
            ServerTransaction serverTransaction;
            earlyProcessMessage(requestEvent);
            EventHeader eventHeader = (EventHeader) request.getHeader("Event");
            if (eventHeader != null) {
                boolean eventKnown;
                synchronized (this.registeredEvents) {
                    eventKnown = this.registeredEvents.contains(eventHeader.getEventType());
                }
                if (!eventKnown) {
                    serverTransaction = requestEvent.getServerTransaction();
                    if (serverTransaction == null) {
                        try {
                            serverTransaction = SipStackSharing.getOrCreateServerTransaction(requestEvent);
                        } catch (TransactionAlreadyExistsException ex) {
                            logger.error("Failed to create a new servertransaction for an incoming request\n(Next message contains the request)", ex);
                            return;
                        } catch (TransactionUnavailableException ex2) {
                            logger.error("Failed to create a new servertransaction for an incoming request\n(Next message contains the request)", ex2);
                            return;
                        }
                    }
                    try {
                        try {
                            serverTransaction.sendResponse(getMessageFactory().createResponse(Response.BAD_EVENT, request));
                            return;
                        } catch (SipException e) {
                            logger.error("failed to send the response", e);
                        } catch (InvalidArgumentException e2) {
                            logger.error("invalid argument provided while trying to send the response", e2);
                        }
                    } catch (ParseException e3) {
                        logger.error("failed to create the 489 response", e3);
                        return;
                    }
                }
            }
            String method = request.getMethod();
            List<MethodProcessor> processors = (List) this.methodProcessors.get(method);
            boolean processedAtLeastOnce = false;
            if (processors != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found " + processors.size() + " processor(s) for method " + method);
                }
                for (MethodProcessor processor : processors) {
                    if (processor.processRequest(requestEvent)) {
                        processedAtLeastOnce = true;
                        break;
                    }
                }
            }
            if (!processedAtLeastOnce) {
                try {
                    serverTransaction = SipStackSharing.getOrCreateServerTransaction(requestEvent);
                    if (serverTransaction == null) {
                        logger.warn("Could not create a transaction for a non-supported method " + request.getMethod());
                        return;
                    }
                    if (TransactionState.TRYING.equals(serverTransaction.getState())) {
                        serverTransaction.sendResponse(getMessageFactory().createResponse(Response.NOT_IMPLEMENTED, request));
                        return;
                    }
                    return;
                } catch (Throwable exc) {
                    logger.warn("Could not respond to a non-supported method " + request.getMethod(), exc);
                    return;
                }
            }
            return;
        }
        logger.warn("Received request not from our proxy, ignoring it! Request:" + request);
        if (requestEvent.getServerTransaction() != null) {
            try {
                requestEvent.getServerTransaction().terminate();
            } catch (Throwable th) {
                logger.warn("Failed to properly terminate transaction for a rogue request. Well ... so be it Request:" + request);
            }
        }
    }

    public void shutdown() {
        if (this.isInitialized) {
            new ShutdownThread().run();
        }
    }

    public ArrayList<ViaHeader> getLocalViaHeaders(Address intendedDestination) throws OperationFailedException {
        return getLocalViaHeaders((SipURI) intendedDestination.getURI());
    }

    public ArrayList<ViaHeader> getLocalViaHeaders(SipURI intendedDestination) throws OperationFailedException {
        ArrayList<ViaHeader> viaHeaders = new ArrayList();
        ListeningPoint srcListeningPoint = getListeningPoint(intendedDestination.getTransportParam());
        try {
            InetSocketAddress targetAddress = getIntendedDestination(intendedDestination);
            InetAddress localAddress = SipActivator.getNetworkAddressManagerService().getLocalHost(targetAddress.getAddress());
            int localPort = srcListeningPoint.getPort();
            String transport = srcListeningPoint.getTransport();
            if (ListeningPoint.TCP.equalsIgnoreCase(transport) || ListeningPoint.TLS.equalsIgnoreCase(transport)) {
                localPort = sipStackSharing.getLocalAddressForDestination(targetAddress.getAddress(), targetAddress.getPort(), localAddress, transport).getPort();
            }
            ViaHeader viaHeader = this.headerFactory.createViaHeader(localAddress.getHostAddress(), localPort, transport, null);
            viaHeaders.add(viaHeader);
            if (logger.isDebugEnabled()) {
                logger.debug("generated via headers:" + viaHeader);
            }
            return viaHeaders;
        } catch (ParseException ex) {
            logger.error("A ParseException occurred while creating Via Headers!", ex);
            throw new OperationFailedException("A ParseException occurred while creating Via Headers!", 4, ex);
        } catch (InvalidArgumentException ex2) {
            logger.error("Unable to create a via header for port " + sipStackSharing.getLP(ListeningPoint.UDP).getPort(), ex2);
            throw new OperationFailedException("Unable to create a via header for port " + sipStackSharing.getLP(ListeningPoint.UDP).getPort(), 4, ex2);
        } catch (IOException ex3) {
            logger.error("Unable to create a via header for port " + sipStackSharing.getLP(ListeningPoint.UDP).getPort(), ex3);
            throw new OperationFailedException("Unable to create a via header for port " + sipStackSharing.getLP(ListeningPoint.UDP).getPort(), 2, ex3);
        }
    }

    public MaxForwardsHeader getMaxForwardsHeader() throws OperationFailedException {
        if (this.maxForwardsHeader == null) {
            try {
                this.maxForwardsHeader = this.headerFactory.createMaxForwardsHeader(MAX_FORWARDS);
                if (logger.isDebugEnabled()) {
                    logger.debug("generated max forwards: " + this.maxForwardsHeader.toString());
                }
            } catch (InvalidArgumentException ex) {
                throw new OperationFailedException("A problem occurred while creating MaxForwardsHeader", 4, ex);
            }
        }
        return this.maxForwardsHeader;
    }

    public ContactHeader getContactHeader(Address intendedDestination) {
        return getContactHeader((SipURI) intendedDestination.getURI());
    }

    public ContactHeader getContactHeader(SipURI intendedDestination) {
        ListeningPoint srcListeningPoint = getListeningPoint(intendedDestination);
        InetSocketAddress targetAddress = getIntendedDestination(intendedDestination);
        try {
            InetAddress localAddress = SipActivator.getNetworkAddressManagerService().getLocalHost(targetAddress.getAddress());
            SipURI contactURI = this.addressFactory.createSipURI(getAccountID().getUserID(), localAddress.getHostAddress());
            String transport = srcListeningPoint.getTransport();
            contactURI.setTransportParam(transport);
            int localPort = srcListeningPoint.getPort();
            if (ListeningPoint.TCP.equalsIgnoreCase(transport) || ListeningPoint.TLS.equalsIgnoreCase(transport)) {
                localPort = sipStackSharing.getLocalAddressForDestination(targetAddress.getAddress(), targetAddress.getPort(), localAddress, transport).getPort();
            }
            contactURI.setPort(localPort);
            String paramValue = getContactAddressCustomParamValue();
            if (paramValue != null) {
                contactURI.setParameter(SipStackSharing.CONTACT_ADDRESS_CUSTOM_PARAM_NAME, paramValue);
            }
            Address contactAddress = this.addressFactory.createAddress((URI) contactURI);
            String ourDisplayName = getOurDisplayName();
            if (ourDisplayName != null) {
                contactAddress.setDisplayName(ourDisplayName);
            }
            ContactHeader registrationContactHeader = this.headerFactory.createContactHeader(contactAddress);
            if (logger.isDebugEnabled()) {
                logger.debug("generated contactHeader:" + registrationContactHeader);
            }
            return registrationContactHeader;
        } catch (ParseException ex) {
            logger.error("A ParseException occurred while creating From Header!", ex);
            throw new IllegalArgumentException("A ParseException occurred while creating From Header!", ex);
        } catch (IOException ex2) {
            logger.error("A ParseException occurred while creating From Header!", ex2);
            throw new IllegalArgumentException("A ParseException occurred while creating From Header!", ex2);
        }
    }

    public String getContactAddressCustomParamValue() {
        SipRegistrarConnection src = this.sipRegistrarConnection;
        if (src == null || src.isRegistrarless()) {
            return null;
        }
        return ((SipURI) src.getAddressOfRecord().getURI()).getHost().replace('.', '_');
    }

    public AddressFactoryEx getAddressFactory() {
        return this.addressFactory;
    }

    public HeaderFactory getHeaderFactory() {
        return this.headerFactory;
    }

    public SipMessageFactory getMessageFactory() {
        if (this.messageFactory == null) {
            this.messageFactory = new SipMessageFactory(this, new MessageFactoryImpl());
        }
        return this.messageFactory;
    }

    public static Set<ProtocolProviderServiceSipImpl> getAllInstances() {
        try {
            Set<ProtocolProviderServiceSipImpl> instances = new HashSet();
            BundleContext context = SipActivator.getBundleContext();
            for (ServiceReference reference : context.getServiceReferences(ProtocolProviderService.class.getName(), null)) {
                Object service = context.getService(reference);
                if (service instanceof ProtocolProviderServiceSipImpl) {
                    instances.add((ProtocolProviderServiceSipImpl) service);
                }
            }
            return instances;
        } catch (InvalidSyntaxException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Problem parcing an osgi expression", ex);
            }
            throw new RuntimeException("getServiceReferences() wasn't supposed to fail!");
        }
    }

    public ListeningPoint getListeningPoint(String transport) {
        if (logger.isTraceEnabled()) {
            logger.trace("Query for a " + transport + " listening point");
        }
        if (this.connection.getAddress() != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Will use proxy address");
            }
            transport = this.connection.getTransport();
        }
        if (!isValidTransport(transport)) {
            transport = getDefaultTransport();
        }
        ListeningPoint lp = null;
        if (transport.equalsIgnoreCase(ListeningPoint.UDP)) {
            lp = sipStackSharing.getLP(ListeningPoint.UDP);
        } else if (transport.equalsIgnoreCase(ListeningPoint.TCP)) {
            lp = sipStackSharing.getLP(ListeningPoint.TCP);
        } else if (transport.equalsIgnoreCase(ListeningPoint.TLS)) {
            lp = sipStackSharing.getLP(ListeningPoint.TLS);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Returning LP " + lp + " for transport [" + transport + "] and ");
        }
        return lp;
    }

    public ListeningPoint getListeningPoint(SipURI intendedDestination) {
        return getListeningPoint(intendedDestination.getTransportParam());
    }

    public SipProvider getJainSipProvider(String transport) {
        return sipStackSharing.getJainSipProvider(transport);
    }

    public SipSecurityManager getSipSecurityManager() {
        return this.sipSecurityManager;
    }

    private void initRegistrarConnection() throws IllegalArgumentException {
        String registrarAddressStr = this.accountID.getAccountPropertyString("SERVER_ADDRESS");
        if (registrarAddressStr == null) {
            String userID = this.accountID.getAccountPropertyString("USER_ID");
            int index = userID.indexOf(64);
            if (index > -1) {
                registrarAddressStr = userID.substring(index + 1);
            }
        }
        if (registrarAddressStr == null || registrarAddressStr.trim().length() == 0) {
            initRegistrarlessConnection();
            return;
        }
        int registrarPort = this.accountID.getAccountPropertyInt("SERVER_PORT", 5060);
        if (registrarPort > InBandBytestreamManager.MAXIMUM_BLOCK_SIZE) {
            throw new IllegalArgumentException(registrarPort + " is larger than " + InBandBytestreamManager.MAXIMUM_BLOCK_SIZE + " and does not therefore represent a valid port number.");
        }
        this.sipRegistrarConnection = new SipRegistrarConnection(registrarAddressStr, registrarPort, getRegistrarTransport(), this);
    }

    private void initRegistrarlessConnection() throws IllegalArgumentException {
        this.sipRegistrarConnection = new SipRegistrarlessConnection(this, getRegistrarTransport());
    }

    private String getRegistrarTransport() {
        String registrarTransport = this.accountID.getAccountPropertyString("SERVER_TRANSPORT");
        if (StringUtils.isNullOrEmpty(registrarTransport)) {
            return getDefaultTransport();
        }
        if (registrarTransport.equals(ListeningPoint.UDP) || registrarTransport.equals(ListeningPoint.TCP) || registrarTransport.equals(ListeningPoint.TLS)) {
            return registrarTransport;
        }
        throw new IllegalArgumentException(registrarTransport + " is not a valid transport protocol. SERVER_TRANSPORT " + "must be left blank or set to TCP, UDP or TLS.");
    }

    public Address getOurSipAddress(Address intendedDestination) {
        return getOurSipAddress((SipURI) intendedDestination.getURI());
    }

    public Address getOurSipAddress(SipURI intendedDestination) {
        SipRegistrarConnection src = this.sipRegistrarConnection;
        if (src != null && !src.isRegistrarless()) {
            return src.getAddressOfRecord();
        }
        InetAddress localHost = SipActivator.getNetworkAddressManagerService().getLocalHost(getIntendedDestination(intendedDestination).getAddress());
        try {
            SipURI ourSipURI = getAddressFactory().createSipURI(getAccountID().getUserID(), localHost.getHostAddress());
            ListeningPoint lp = getListeningPoint(intendedDestination);
            ourSipURI.setTransportParam(lp.getTransport());
            ourSipURI.setPort(lp.getPort());
            Address ourSipAddress = getAddressFactory().createAddress(getOurDisplayName(), ourSipURI);
            ourSipAddress.setDisplayName(getOurDisplayName());
            return ourSipAddress;
        } catch (ParseException exc) {
            if (logger.isTraceEnabled()) {
                logger.trace("Failed to create our SIP AOR address", exc);
            }
            throw new IllegalArgumentException("Failed to create our SIP AOR address", exc);
        }
    }

    public ProxyConnection getConnection() {
        return this.connection;
    }

    public boolean isSignalingTransportSecure() {
        return ListeningPoint.TLS.equalsIgnoreCase(this.connection.getTransport());
    }

    public TransportProtocol getTransportProtocol() {
        if (this.sipRegistrarConnection == null || (this.sipRegistrarConnection instanceof SipRegistrarlessConnection)) {
            return TransportProtocol.UNKNOWN;
        }
        return TransportProtocol.parse(this.sipRegistrarConnection.getTransport());
    }

    public void registerMethodProcessor(String method, MethodProcessor methodProcessor) {
        List<MethodProcessor> processors = (List) this.methodProcessors.get(method);
        if (processors == null) {
            processors = new LinkedList();
            this.methodProcessors.put(method, processors);
        } else {
            Iterator<MethodProcessor> processorIter = processors.iterator();
            Class<? extends MethodProcessor> methodProcessorClass = methodProcessor.getClass();
            String eventPackage = methodProcessor instanceof EventPackageSupport ? ((EventPackageSupport) methodProcessor).getEventPackage() : null;
            while (processorIter.hasNext()) {
                MethodProcessor processor = (MethodProcessor) processorIter.next();
                if (processor.getClass().equals(methodProcessorClass) && (eventPackage == null || !(processor instanceof EventPackageSupport) || eventPackage.equals(((EventPackageSupport) processor).getEventPackage()))) {
                    processorIter.remove();
                }
            }
        }
        processors.add(methodProcessor);
    }

    public void unregisterMethodProcessor(String method, MethodProcessor methodProcessor) {
        List<MethodProcessor> processors = (List) this.methodProcessors.get(method);
        if (processors != null && processors.remove(methodProcessor) && processors.size() <= 0) {
            this.methodProcessors.remove(method);
        }
    }

    public String getDefaultTransport() {
        if (this.sipRegistrarConnection != null && !this.sipRegistrarConnection.isRegistrarless() && this.connection != null && this.connection.getAddress() != null && this.connection.getTransport() != null) {
            return this.connection.getTransport();
        }
        String userSpecifiedDefaultTransport = SipActivator.getConfigurationService().getString(DEFAULT_TRANSPORT);
        if (userSpecifiedDefaultTransport != null) {
            return userSpecifiedDefaultTransport;
        }
        String defTransportDefaultValue = SipActivator.getResources().getSettingsString(DEFAULT_TRANSPORT);
        if (StringUtils.isNullOrEmpty(defTransportDefaultValue)) {
            return ListeningPoint.UDP;
        }
        return defTransportDefaultValue;
    }

    public SipProvider getDefaultJainSipProvider() {
        return getJainSipProvider(getDefaultTransport());
    }

    public String getOurDisplayName() {
        return this.ourDisplayName;
    }

    /* access modifiers changed from: 0000 */
    public boolean setOurDisplayName(String newDisplayName) {
        if (newDisplayName == null || this.ourDisplayName.equals(newDisplayName)) {
            return false;
        }
        getAccountID().putAccountProperty("DISPLAY_NAME", newDisplayName);
        this.ourDisplayName = newDisplayName;
        OperationSetServerStoredAccountInfoSipImpl accountInfoOpSet = (OperationSetServerStoredAccountInfoSipImpl) getOperationSet(OperationSetServerStoredAccountInfo.class);
        if (accountInfoOpSet != null) {
            accountInfoOpSet.setOurDisplayName(newDisplayName);
        }
        return true;
    }

    public UserAgentHeader getSipCommUserAgentHeader() {
        if (this.userAgentHeader == null) {
            try {
                List<String> userAgentTokens = new LinkedList();
                Version ver = SipActivator.getVersionService().getCurrentVersion();
                userAgentTokens.add(ver.getApplicationName());
                userAgentTokens.add(ver.toString());
                userAgentTokens.add(System.getProperty("os.name"));
                this.userAgentHeader = this.headerFactory.createUserAgentHeader(userAgentTokens);
            } catch (ParseException e) {
                return null;
            }
        }
        return this.userAgentHeader;
    }

    public void sayErrorSilently(ServerTransaction serverTransaction, int errorCode) {
        try {
            sayError(serverTransaction, errorCode);
        } catch (OperationFailedException exc) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to send an error " + errorCode + " response", exc);
            }
        }
    }

    public void sendAck(ClientTransaction clientTransaction) throws SipException, InvalidArgumentException {
        clientTransaction.getDialog().sendAck(this.messageFactory.createAck(clientTransaction));
    }

    public void sayError(ServerTransaction serverTransaction, int errorCode) throws OperationFailedException {
        sayError(serverTransaction, errorCode, null);
    }

    public void sayError(ServerTransaction serverTransaction, int errorCode, Header header) throws OperationFailedException {
        Response errorResponse = null;
        try {
            errorResponse = getMessageFactory().createResponse(errorCode, serverTransaction.getRequest());
            if (header != null) {
                errorResponse.setHeader(header);
            }
        } catch (ParseException ex) {
            throwOperationFailedException("Failed to construct an OK response to an INVITE request", 4, ex, logger);
        }
        try {
            serverTransaction.sendResponse(errorResponse);
            if (logger.isDebugEnabled()) {
                logger.debug("sent response: " + errorResponse);
            }
        } catch (Exception ex2) {
            throwOperationFailedException("Failed to send an OK response to an INVITE request", 4, ex2, logger);
        }
    }

    public void sendInDialogRequest(SipProvider sipProvider, Request request, Dialog dialog) throws OperationFailedException {
        ClientTransaction clientTransaction = null;
        try {
            clientTransaction = sipProvider.getNewClientTransaction(request);
        } catch (TransactionUnavailableException ex) {
            throwOperationFailedException("Failed to create a client transaction for request:\n" + request, 4, ex, logger);
        }
        try {
            dialog.sendRequest(clientTransaction);
        } catch (SipException ex2) {
            throwOperationFailedException("Failed to send request:\n" + request, 2, ex2, logger);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Sent request:\n" + request);
        }
    }

    public List<String> getSupportedMethods() {
        return new ArrayList(this.methodProcessors.keySet());
    }

    public ProtocolIcon getProtocolIcon() {
        return this.protocolIcon;
    }

    /* access modifiers changed from: 0000 */
    public SipStatusEnum getSipStatusEnum() {
        return this.sipStatusEnum;
    }

    public SipRegistrarConnection getRegistrarConnection() {
        return this.sipRegistrarConnection;
    }

    public Address parseAddressString(String uriStr) throws ParseException {
        uriStr = uriStr.trim();
        if (uriStr.toLowerCase().startsWith("tel:")) {
            uriStr = "sip:" + uriStr.substring("tel:".length());
        }
        if (uriStr.indexOf(64) == -1) {
            SipRegistrarConnection src = this.sipRegistrarConnection;
            if (!(src == null || src.isRegistrarless())) {
                uriStr = uriStr + Separators.AT + ((SipURI) src.getAddressOfRecord().getURI()).getHost();
            }
        }
        if (!uriStr.toLowerCase().startsWith("sip:")) {
            uriStr = "sip:" + uriStr;
        }
        return getAddressFactory().createAddress(uriStr);
    }

    public InetSocketAddress getIntendedDestination(Address destination) throws IllegalArgumentException {
        return getIntendedDestination((SipURI) destination.getURI());
    }

    public InetSocketAddress getIntendedDestination(SipURI destination) throws IllegalArgumentException {
        return getIntendedDestination(destination.getHost());
    }

    public InetSocketAddress getIntendedDestination(String host) throws IllegalArgumentException {
        InetSocketAddress destinationInetAddress = null;
        InetSocketAddress outboundProxy = this.connection.getAddress();
        if (outboundProxy != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Will use proxy address");
            }
            destinationInetAddress = outboundProxy;
        } else {
            ProxyConnection tempConn = new AutoProxyConnection((SipAccountIDImpl) getAccountID(), host, getDefaultTransport());
            try {
                if (tempConn.getNextAddress()) {
                    destinationInetAddress = tempConn.getAddress();
                } else {
                    throw new IllegalArgumentException(host + " could not be resolved to an internet address.");
                }
            } catch (DnssecException e) {
                logger.error("unable to obtain next hop address", e);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Returning address " + destinationInetAddress + " for destination " + host);
        }
        return destinationInetAddress;
    }

    public void registrationStateChanged(RegistrationStateChangeEvent event) {
        if (event.getNewState() == RegistrationState.UNREGISTERED || event.getNewState() == RegistrationState.CONNECTION_FAILED) {
            ProtocolProviderServiceSipImpl listener = (ProtocolProviderServiceSipImpl) event.getProvider();
            sipStackSharing.removeSipListener(listener);
            listener.removeRegistrationStateChangeListener(this);
        }
    }

    public static void throwOperationFailedException(String message, int errorCode, Throwable cause, Logger logger) throws OperationFailedException {
        logger.error(message, cause);
        if (cause == null) {
            throw new OperationFailedException(message, errorCode);
        }
        throw new OperationFailedException(message, errorCode, cause);
    }

    /* access modifiers changed from: 0000 */
    public void addEarlyMessageProcessor(SipMessageProcessor processor) {
        synchronized (this.earlyProcessors) {
            if (!this.earlyProcessors.contains(processor)) {
                this.earlyProcessors.add(processor);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void removeEarlyMessageProcessor(SipMessageProcessor processor) {
        synchronized (this.earlyProcessors) {
            this.earlyProcessors.remove(processor);
        }
    }

    /* access modifiers changed from: 0000 */
    public void earlyProcessMessage(EventObject message) {
        synchronized (this.earlyProcessors) {
            for (SipMessageProcessor listener : this.earlyProcessors) {
                try {
                    if (message instanceof RequestEvent) {
                        listener.processMessage((RequestEvent) message);
                    } else if (message instanceof ResponseEvent) {
                        listener.processResponse((ResponseEvent) message, null);
                    } else if (message instanceof TimeoutEvent) {
                        listener.processTimeout((TimeoutEvent) message, null);
                    }
                } catch (Throwable t) {
                    logger.error("Error pre-processing message", t);
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean registerUsingNextAddress() {
        if (this.connection == null) {
            return false;
        }
        try {
            if (this.sipRegistrarConnection.isRegistrarless()) {
                this.sipRegistrarConnection.setTransport(getDefaultTransport());
                this.sipRegistrarConnection.register();
                return true;
            }
            if (this.connection.getNextAddress()) {
                this.sipRegistrarConnection.setTransport(this.connection.getTransport());
                this.sipRegistrarConnection.register();
                return true;
            }
            this.connection.reset();
            return false;
        } catch (DnssecException e) {
            logger.error("DNSSEC failure while getting address for " + this, e);
            fireRegistrationStateChanged(RegistrationState.REGISTERING, RegistrationState.UNREGISTERED, 0, "Invalid or inaccessible server address.");
            return true;
        } catch (Throwable e2) {
            logger.error("Cannot send register!", e2);
            this.sipRegistrarConnection.setRegistrationState(RegistrationState.CONNECTION_FAILED, -1, "A timeout occurred while trying to connect to the server.");
        }
    }

    /* access modifiers changed from: protected */
    public void notifyConnectionFailed() {
        if (getRegistrationState().equals(RegistrationState.REGISTERED) && this.sipRegistrarConnection != null) {
            this.sipRegistrarConnection.setRegistrationState(RegistrationState.CONNECTION_FAILED, -1, "A timeout occurred while trying to connect to the server.");
        }
        if (!registerUsingNextAddress() && !getRegistrationState().equals(RegistrationState.UNREGISTERED)) {
            this.sipRegistrarConnection.setRegistrationState(RegistrationState.CONNECTION_FAILED, -1, "A timeout occurred while trying to connect to the server.");
        }
    }

    public static boolean isValidTransport(String transport) {
        return ListeningPoint.UDP.equalsIgnoreCase(transport) || ListeningPoint.TLS.equalsIgnoreCase(transport) || ListeningPoint.TCP.equalsIgnoreCase(transport);
    }
}
