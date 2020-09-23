package net.java.sip.communicator.impl.protocol.jabber;

import java.math.BigInteger;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.X509TrustManager;
import net.java.sip.communicator.impl.protocol.jabber.debugger.SmackPacketDebugger;
import net.java.sip.communicator.impl.protocol.jabber.extensions.ConferenceDescriptionPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.ConferenceDescriptionPacketExtension.Provider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.caps.EntityCapsManager.Caps;
import net.java.sip.communicator.impl.protocol.jabber.extensions.carbon.CarbonPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.coin.CoinIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.coin.CoinIQProvider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriConferenceIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriIQProvider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.inputevt.InputEvtIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.inputevt.InputEvtIQProvider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQProvider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingleinfo.JingleInfoQueryIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingleinfo.JingleInfoQueryIQProvider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.keepalive.KeepAliveManager;
import net.java.sip.communicator.impl.protocol.jabber.extensions.messagecorrection.MessageCorrectionExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail.ThumbnailElement;
import net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail.ThumbnailIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.version.VersionManager;
import net.java.sip.communicator.service.certificate.CertificateService;
import net.java.sip.communicator.service.dns.DnssecException;
import net.java.sip.communicator.service.protocol.AbstractProtocolProviderService;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.JingleNodeDescriptor;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetAdvancedTelephony;
import net.java.sip.communicator.service.protocol.OperationSetAvatar;
import net.java.sip.communicator.service.protocol.OperationSetBasicAutoAnswer;
import net.java.sip.communicator.service.protocol.OperationSetBasicInstantMessaging;
import net.java.sip.communicator.service.protocol.OperationSetBasicTelephony;
import net.java.sip.communicator.service.protocol.OperationSetChangePassword;
import net.java.sip.communicator.service.protocol.OperationSetContactCapabilities;
import net.java.sip.communicator.service.protocol.OperationSetCusaxUtils;
import net.java.sip.communicator.service.protocol.OperationSetDTMF;
import net.java.sip.communicator.service.protocol.OperationSetDesktopSharingClient;
import net.java.sip.communicator.service.protocol.OperationSetDesktopSharingServer;
import net.java.sip.communicator.service.protocol.OperationSetDesktopStreaming;
import net.java.sip.communicator.service.protocol.OperationSetExtendedAuthorizations;
import net.java.sip.communicator.service.protocol.OperationSetFileTransfer;
import net.java.sip.communicator.service.protocol.OperationSetGenericNotifications;
import net.java.sip.communicator.service.protocol.OperationSetInstantMessageTransform;
import net.java.sip.communicator.service.protocol.OperationSetInstantMessageTransformImpl;
import net.java.sip.communicator.service.protocol.OperationSetMessageCorrection;
import net.java.sip.communicator.service.protocol.OperationSetMultiUserChat;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresencePermissions;
import net.java.sip.communicator.service.protocol.OperationSetPresence;
import net.java.sip.communicator.service.protocol.OperationSetResourceAwareTelephony;
import net.java.sip.communicator.service.protocol.OperationSetSecureSDesTelephony;
import net.java.sip.communicator.service.protocol.OperationSetSecureZrtpTelephony;
import net.java.sip.communicator.service.protocol.OperationSetServerStoredAccountInfo;
import net.java.sip.communicator.service.protocol.OperationSetServerStoredContactInfo;
import net.java.sip.communicator.service.protocol.OperationSetTelephonyConferencing;
import net.java.sip.communicator.service.protocol.OperationSetThumbnailedFileFactory;
import net.java.sip.communicator.service.protocol.OperationSetTypingNotifications;
import net.java.sip.communicator.service.protocol.OperationSetVideoBridge;
import net.java.sip.communicator.service.protocol.OperationSetVideoTelephony;
import net.java.sip.communicator.service.protocol.OperationSetWhiteboarding;
import net.java.sip.communicator.service.protocol.ProtocolIcon;
import net.java.sip.communicator.service.protocol.ProxyInfo.ProxyType;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.SecurityAuthority;
import net.java.sip.communicator.service.protocol.TransportProtocol;
import net.java.sip.communicator.service.protocol.UserCredentials;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.jabberconstants.JabberStatusEnum;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.NetworkUtils;
import net.java.sip.communicator.util.SRVRecord;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.service.neomedia.DtlsControl;
import org.jitsi.util.OSUtils;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.StreamError;
import org.jivesoftware.smack.packet.XMPPError.Condition;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.XHTMLManager;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5BytestreamManager;
import org.jivesoftware.smackx.packet.AdHocCommandData.SpecificError;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.osgi.framework.ServiceReference;
import org.xmpp.jnodes.smack.SmackServiceNode;
import org.xmpp.jnodes.smack.TrackerEntry;
import org.xmpp.jnodes.smack.TrackerEntry.Policy;

public class ProtocolProviderServiceJabberImpl extends AbstractProtocolProviderService {
    public static final String CAPS_GTALK_WEB_CAMERA = "camera-v1";
    public static final String CAPS_GTALK_WEB_VIDEO = "video-v1";
    public static final String CAPS_GTALK_WEB_VOICE = "voice-v1";
    public static final String GOOGLE_VOICE_DOMAIN = "voice.google.com";
    private static final String IS_CALLING_DISABLED = "net.java.sip.communicator.impl.protocol.jabber.CALLING_DISABLED";
    private static final String IS_DESKTOP_STREAMING_DISABLED = "net.java.sip.communicator.impl.protocol.jabber.DESKTOP_STREAMING_DISABLED";
    public static final int SMACK_PACKET_REPLY_TIMEOUT = 45000;
    public static final String URN_GOOGLE_CAMERA = "http://www.google.com/xmpp/protocol/camera/v1";
    public static final String URN_GOOGLE_VIDEO = "http://www.google.com/xmpp/protocol/video/v1";
    public static final String URN_GOOGLE_VOICE = "http://www.google.com/xmpp/protocol/voice/v1";
    public static final String URN_IETF_RFC_3264 = "urn:ietf:rfc:3264";
    public static final String URN_REGISTER = "jabber:iq:register";
    public static final String URN_XMPP_JINGLE = "urn:xmpp:jingle:1";
    public static final String URN_XMPP_JINGLE_COIN = "urn:xmpp:coin";
    public static final String URN_XMPP_JINGLE_DTLS_SRTP = "urn:xmpp:jingle:apps:dtls:0";
    public static final String URN_XMPP_JINGLE_ICE_UDP_1 = "urn:xmpp:jingle:transports:ice-udp:1";
    public static final String URN_XMPP_JINGLE_NODES = "http://jabber.org/protocol/jinglenodes";
    public static final String URN_XMPP_JINGLE_RAW_UDP_0 = "urn:xmpp:jingle:transports:raw-udp:1";
    public static final String URN_XMPP_JINGLE_RTP = "urn:xmpp:jingle:apps:rtp:1";
    public static final String URN_XMPP_JINGLE_RTP_AUDIO = "urn:xmpp:jingle:apps:rtp:audio";
    public static final String URN_XMPP_JINGLE_RTP_HDREXT = "urn:xmpp:jingle:apps:rtp:rtp-hdrext:0";
    public static final String URN_XMPP_JINGLE_RTP_VIDEO = "urn:xmpp:jingle:apps:rtp:video";
    public static final String URN_XMPP_JINGLE_RTP_ZRTP = "urn:xmpp:jingle:apps:rtp:zrtp:1";
    public static final String URN_XMPP_JINGLE_TRANSFER_0 = "urn:xmpp:jingle:transfer:0";
    public static final String VCARD_REPLY_TIMEOUT_PROPERTY = "net.java.sip.communicator.impl.protocol.jabber.VCARD_REPLY_TIMEOUT";
    private static final String XMPP_DSCP_PROPERTY = "net.java.sip.communicator.impl.protocol.XMPP_DSCP";
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(ProtocolProviderServiceJabberImpl.class);
    private static Object providerCreationLock = new Object();
    private static ProviderManager providerManager = null;
    /* access modifiers changed from: private */
    public boolean abortConnecting = false;
    private AccountID accountID = null;
    private SecurityAuthority authority = null;
    /* access modifiers changed from: private|final */
    public final Object connectAndLoginLock = new Object();
    private XMPPConnection connection;
    private JabberConnectionListener connectionListener;
    private SmackPacketDebugger debugger = null;
    private ScServiceDiscoveryManager discoveryManager = null;
    /* access modifiers changed from: private */
    public RegistrationStateChangeEvent eventDuringLogin;
    private CertificateService guiVerification;
    /* access modifiers changed from: private */
    public boolean inConnectAndLogin = false;
    private final Object initializationLock = new Object();
    private boolean isInitialized = false;
    private ProtocolIconJabberImpl jabberIcon;
    private JabberStatusEnum jabberStatusEnum;
    private SmackServiceNode jingleNodesServiceNode = null;
    private final Object jingleNodesSyncRoot = new Object();
    private KeepAliveManager keepAliveManager = null;
    private OperationSetContactCapabilitiesJabberImpl opsetContactCapabilities;
    private ProxyInfo proxy;
    private String resource = null;
    private final List<String> supportedFeatures = new ArrayList();
    private UserCredentials userCredentials = null;
    private VersionManager versionManager = null;

    enum ConnectState {
        ABORT_CONNECTING,
        CONTINUE_TRYING,
        STOP_TRYING
    }

    private class HostTrustManager implements X509TrustManager {
        private final X509TrustManager tm;

        HostTrustManager(X509TrustManager tm) {
            this.tm = tm;
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException, UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            ProtocolProviderServiceJabberImpl.this.abortConnecting = true;
            try {
                this.tm.checkServerTrusted(chain, authType);
                if (ProtocolProviderServiceJabberImpl.this.abortConnecting) {
                    ProtocolProviderServiceJabberImpl.this.abortConnecting = false;
                } else {
                    new Thread(new Runnable() {
                        public void run() {
                            ProtocolProviderServiceJabberImpl.this.reregister(3);
                        }
                    }).start();
                }
            } catch (CertificateException e) {
                new Thread(new Runnable() {
                    public void run() {
                        ProtocolProviderServiceJabberImpl.this.fireRegistrationStateChanged(ProtocolProviderServiceJabberImpl.this.getRegistrationState(), RegistrationState.UNREGISTERED, 0, "Not trusted certificate");
                    }
                }).start();
                throw e;
            }
        }
    }

    private class JabberConnectionListener implements ConnectionListener {
        private JabberConnectionListener() {
        }

        public void connectionClosed() {
            synchronized (ProtocolProviderServiceJabberImpl.this.connectAndLoginLock) {
                if (ProtocolProviderServiceJabberImpl.this.inConnectAndLogin) {
                    ProtocolProviderServiceJabberImpl.this.eventDuringLogin = new RegistrationStateChangeEvent(ProtocolProviderServiceJabberImpl.this, ProtocolProviderServiceJabberImpl.this.getRegistrationState(), RegistrationState.CONNECTION_FAILED, -1, null);
                    return;
                }
                ProtocolProviderServiceJabberImpl.this.fireRegistrationStateChanged(ProtocolProviderServiceJabberImpl.this.getRegistrationState(), RegistrationState.CONNECTION_FAILED, -1, null);
            }
        }

        public void connectionClosedOnError(Exception exception) {
            ProtocolProviderServiceJabberImpl.logger.error("connectionClosedOnError " + exception.getLocalizedMessage());
            if (exception instanceof XMPPException) {
                StreamError err = ((XMPPException) exception).getStreamError();
                if (err != null && err.getCode().equals(Condition.conflict.toString())) {
                    synchronized (ProtocolProviderServiceJabberImpl.this.connectAndLoginLock) {
                        if (ProtocolProviderServiceJabberImpl.this.inConnectAndLogin) {
                            ProtocolProviderServiceJabberImpl.this.eventDuringLogin = new RegistrationStateChangeEvent(ProtocolProviderServiceJabberImpl.this, ProtocolProviderServiceJabberImpl.this.getRegistrationState(), RegistrationState.UNREGISTERED, 2, "Connecting multiple times with the same resource");
                            return;
                        }
                        ProtocolProviderServiceJabberImpl.this.disconnectAndCleanConnection();
                        ProtocolProviderServiceJabberImpl.this.fireRegistrationStateChanged(ProtocolProviderServiceJabberImpl.this.getRegistrationState(), RegistrationState.UNREGISTERED, 2, "Connecting multiple times with the same resource");
                        return;
                    }
                }
            } else if ((exception instanceof SSLHandshakeException) && (exception.getCause() instanceof CertificateException)) {
                return;
            }
            synchronized (ProtocolProviderServiceJabberImpl.this.connectAndLoginLock) {
                if (ProtocolProviderServiceJabberImpl.this.inConnectAndLogin) {
                    ProtocolProviderServiceJabberImpl.this.eventDuringLogin = new RegistrationStateChangeEvent(ProtocolProviderServiceJabberImpl.this, ProtocolProviderServiceJabberImpl.this.getRegistrationState(), RegistrationState.CONNECTION_FAILED, -1, exception.getMessage());
                    return;
                }
                ProtocolProviderServiceJabberImpl.this.disconnectAndCleanConnection();
                ProtocolProviderServiceJabberImpl.this.fireRegistrationStateChanged(ProtocolProviderServiceJabberImpl.this.getRegistrationState(), RegistrationState.CONNECTION_FAILED, -1, exception.getMessage());
            }
        }

        public void reconnectingIn(int i) {
            if (ProtocolProviderServiceJabberImpl.logger.isInfoEnabled()) {
                ProtocolProviderServiceJabberImpl.logger.info("reconnectingIn " + i);
            }
        }

        public void reconnectionSuccessful() {
            if (ProtocolProviderServiceJabberImpl.logger.isInfoEnabled()) {
                ProtocolProviderServiceJabberImpl.logger.info("reconnectionSuccessful");
            }
        }

        public void reconnectionFailed(Exception exception) {
            if (ProtocolProviderServiceJabberImpl.logger.isInfoEnabled()) {
                ProtocolProviderServiceJabberImpl.logger.info("reconnectionFailed ", exception);
            }
        }
    }

    static {
        if (OSUtils.IS_ANDROID) {
            loadJabberServiceClasses();
        }
    }

    public RegistrationState getRegistrationState() {
        if (this.connection == null) {
            return RegistrationState.UNREGISTERED;
        }
        if (this.connection.isConnected() && this.connection.isAuthenticated()) {
            return RegistrationState.REGISTERED;
        }
        return RegistrationState.UNREGISTERED;
    }

    private CertificateService getCertificateVerificationService() {
        if (this.guiVerification == null) {
            ServiceReference guiVerifyReference = JabberActivator.getBundleContext().getServiceReference(CertificateService.class.getName());
            if (guiVerifyReference != null) {
                this.guiVerification = (CertificateService) JabberActivator.getBundleContext().getService(guiVerifyReference);
            }
        }
        return this.guiVerification;
    }

    public void register(SecurityAuthority authority) throws OperationFailedException {
        if (authority == null) {
            throw new IllegalArgumentException("The register method needs a valid non-null authority impl  in order to be able and retrieve passwords.");
        }
        this.authority = authority;
        try {
            this.abortConnecting = false;
            synchronized (this.connectAndLoginLock) {
                this.inConnectAndLogin = true;
            }
            initializeConnectAndLogin(authority, 0);
            synchronized (this.connectAndLoginLock) {
                if (this.eventDuringLogin != null) {
                    if (this.eventDuringLogin.getNewState().equals(RegistrationState.CONNECTION_FAILED) || this.eventDuringLogin.getNewState().equals(RegistrationState.UNREGISTERED)) {
                        disconnectAndCleanConnection();
                    }
                    fireRegistrationStateChanged(this.eventDuringLogin.getOldState(), this.eventDuringLogin.getNewState(), this.eventDuringLogin.getReasonCode(), this.eventDuringLogin.getReason());
                    this.eventDuringLogin = null;
                    this.inConnectAndLogin = false;
                    return;
                }
                this.inConnectAndLogin = false;
            }
        } catch (XMPPException ex) {
            try {
                logger.error("Error registering", ex);
                this.eventDuringLogin = null;
                fireRegistrationStateChanged(ex);
                synchronized (this.connectAndLoginLock) {
                    if (this.eventDuringLogin != null) {
                        if (this.eventDuringLogin.getNewState().equals(RegistrationState.CONNECTION_FAILED) || this.eventDuringLogin.getNewState().equals(RegistrationState.UNREGISTERED)) {
                            disconnectAndCleanConnection();
                        }
                        fireRegistrationStateChanged(this.eventDuringLogin.getOldState(), this.eventDuringLogin.getNewState(), this.eventDuringLogin.getReasonCode(), this.eventDuringLogin.getReason());
                        this.eventDuringLogin = null;
                        this.inConnectAndLogin = false;
                        return;
                    }
                    this.inConnectAndLogin = false;
                }
            } catch (Throwable th) {
                synchronized (this.connectAndLoginLock) {
                    if (this.eventDuringLogin != null) {
                        if (this.eventDuringLogin.getNewState().equals(RegistrationState.CONNECTION_FAILED) || this.eventDuringLogin.getNewState().equals(RegistrationState.UNREGISTERED)) {
                            disconnectAndCleanConnection();
                        }
                        fireRegistrationStateChanged(this.eventDuringLogin.getOldState(), this.eventDuringLogin.getNewState(), this.eventDuringLogin.getReasonCode(), this.eventDuringLogin.getReason());
                        this.eventDuringLogin = null;
                        this.inConnectAndLogin = false;
                        return;
                    }
                    this.inConnectAndLogin = false;
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void reregister(int authReasonCode) {
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("Trying to reregister us!");
            }
            unregister(false);
            this.abortConnecting = false;
            synchronized (this.connectAndLoginLock) {
                this.inConnectAndLogin = true;
            }
            initializeConnectAndLogin(this.authority, authReasonCode);
            synchronized (this.connectAndLoginLock) {
                if (this.eventDuringLogin != null) {
                    if (this.eventDuringLogin.getNewState().equals(RegistrationState.CONNECTION_FAILED) || this.eventDuringLogin.getNewState().equals(RegistrationState.UNREGISTERED)) {
                        disconnectAndCleanConnection();
                    }
                    fireRegistrationStateChanged(this.eventDuringLogin.getOldState(), this.eventDuringLogin.getNewState(), this.eventDuringLogin.getReasonCode(), this.eventDuringLogin.getReason());
                    this.eventDuringLogin = null;
                    this.inConnectAndLogin = false;
                    return;
                }
                this.inConnectAndLogin = false;
            }
        } catch (OperationFailedException ex) {
            try {
                logger.error("Error ReRegistering", ex);
                this.eventDuringLogin = null;
                disconnectAndCleanConnection();
                fireRegistrationStateChanged(getRegistrationState(), RegistrationState.CONNECTION_FAILED, 6, null);
                synchronized (this.connectAndLoginLock) {
                    if (this.eventDuringLogin != null) {
                        if (this.eventDuringLogin.getNewState().equals(RegistrationState.CONNECTION_FAILED) || this.eventDuringLogin.getNewState().equals(RegistrationState.UNREGISTERED)) {
                            disconnectAndCleanConnection();
                        }
                        fireRegistrationStateChanged(this.eventDuringLogin.getOldState(), this.eventDuringLogin.getNewState(), this.eventDuringLogin.getReasonCode(), this.eventDuringLogin.getReason());
                        this.eventDuringLogin = null;
                        this.inConnectAndLogin = false;
                        return;
                    }
                    this.inConnectAndLogin = false;
                }
            } catch (Throwable th) {
                synchronized (this.connectAndLoginLock) {
                    if (this.eventDuringLogin != null) {
                        if (this.eventDuringLogin.getNewState().equals(RegistrationState.CONNECTION_FAILED) || this.eventDuringLogin.getNewState().equals(RegistrationState.UNREGISTERED)) {
                            disconnectAndCleanConnection();
                        }
                        fireRegistrationStateChanged(this.eventDuringLogin.getOldState(), this.eventDuringLogin.getNewState(), this.eventDuringLogin.getReasonCode(), this.eventDuringLogin.getReason());
                        this.eventDuringLogin = null;
                        this.inConnectAndLogin = false;
                        return;
                    }
                    this.inConnectAndLogin = false;
                }
            }
        } catch (XMPPException ex2) {
            logger.error("Error ReRegistering", ex2);
            this.eventDuringLogin = null;
            fireRegistrationStateChanged(ex2);
            synchronized (this.connectAndLoginLock) {
                if (this.eventDuringLogin != null) {
                    if (this.eventDuringLogin.getNewState().equals(RegistrationState.CONNECTION_FAILED) || this.eventDuringLogin.getNewState().equals(RegistrationState.UNREGISTERED)) {
                        disconnectAndCleanConnection();
                    }
                    fireRegistrationStateChanged(this.eventDuringLogin.getOldState(), this.eventDuringLogin.getNewState(), this.eventDuringLogin.getReasonCode(), this.eventDuringLogin.getReason());
                    this.eventDuringLogin = null;
                    this.inConnectAndLogin = false;
                    return;
                }
                this.inConnectAndLogin = false;
            }
        }
    }

    public boolean isSignalingTransportSecure() {
        return this.connection != null && this.connection.isUsingTLS();
    }

    public TransportProtocol getTransportProtocol() {
        if (this.connection == null || !this.connection.isConnected()) {
            return TransportProtocol.UNKNOWN;
        }
        if (this.connection.isUsingTLS()) {
            return TransportProtocol.TLS;
        }
        return TransportProtocol.TCP;
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* JADX WARNING: Missing block: B:84:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:86:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:87:?, code skipped:
            return;
     */
    private void initializeConnectAndLogin(net.java.sip.communicator.service.protocol.SecurityAuthority r26, int r27) throws org.jivesoftware.smack.XMPPException, net.java.sip.communicator.service.protocol.OperationFailedException {
        /*
        r25 = this;
        r0 = r25;
        r0 = r0.initializationLock;
        r21 = r0;
        monitor-enter(r21);
        r20 = r25.isRegistered();	 Catch:{ all -> 0x0029 }
        if (r20 == 0) goto L_0x000f;
    L_0x000d:
        monitor-exit(r21);	 Catch:{ all -> 0x0029 }
    L_0x000e:
        return;
    L_0x000f:
        r15 = r25.createLoginStrategy();	 Catch:{ all -> 0x0029 }
        r0 = r26;
        r1 = r27;
        r20 = r15.prepareLogin(r0, r1);	 Catch:{ all -> 0x0029 }
        r0 = r20;
        r1 = r25;
        r1.userCredentials = r0;	 Catch:{ all -> 0x0029 }
        r20 = r15.loginPreparationSuccessful();	 Catch:{ all -> 0x0029 }
        if (r20 != 0) goto L_0x002c;
    L_0x0027:
        monitor-exit(r21);	 Catch:{ all -> 0x0029 }
        goto L_0x000e;
    L_0x0029:
        r20 = move-exception;
        monitor-exit(r21);	 Catch:{ all -> 0x0029 }
        throw r20;
    L_0x002c:
        r20 = r25.getAccountID();	 Catch:{ all -> 0x0029 }
        r20 = r20.getUserID();	 Catch:{ all -> 0x0029 }
        r18 = org.jivesoftware.smack.util.StringUtils.parseServer(r20);	 Catch:{ all -> 0x0029 }
        r25.loadResource();	 Catch:{ all -> 0x0029 }
        r25.loadProxy();	 Catch:{ all -> 0x0029 }
        r20 = org.jivesoftware.smack.Roster.SubscriptionMode.manual;	 Catch:{ all -> 0x0029 }
        org.jivesoftware.smack.Roster.setDefaultSubscriptionMode(r20);	 Catch:{ all -> 0x0029 }
        r20 = 1;
        r0 = r20;
        r10 = new boolean[r0];	 Catch:{ all -> 0x0029 }
        r20 = 0;
        r22 = 0;
        r10[r20] = r22;	 Catch:{ all -> 0x0029 }
        r20 = r25.getAccountID();	 Catch:{ all -> 0x0029 }
        r22 = "IS_SERVER_OVERRIDDEN";
        r23 = 0;
        r0 = r20;
        r1 = r22;
        r2 = r23;
        r12 = r0.getAccountPropertyBoolean(r1, r2);	 Catch:{ all -> 0x0029 }
        if (r12 != 0) goto L_0x008a;
    L_0x0063:
        r0 = r25;
        r1 = r18;
        r2 = r18;
        r19 = r0.connectUsingSRVRecords(r1, r2, r10, r15);	 Catch:{ all -> 0x0029 }
        r20 = 0;
        r20 = r10[r20];	 Catch:{ all -> 0x0029 }
        if (r20 == 0) goto L_0x0078;
    L_0x0073:
        r25.setDnssecLoginFailure();	 Catch:{ all -> 0x0029 }
        monitor-exit(r21);	 Catch:{ all -> 0x0029 }
        goto L_0x000e;
    L_0x0078:
        r20 = net.java.sip.communicator.impl.protocol.jabber.ProtocolProviderServiceJabberImpl.ConnectState.ABORT_CONNECTING;	 Catch:{ all -> 0x0029 }
        r0 = r19;
        r1 = r20;
        if (r0 == r1) goto L_0x0088;
    L_0x0080:
        r20 = net.java.sip.communicator.impl.protocol.jabber.ProtocolProviderServiceJabberImpl.ConnectState.STOP_TRYING;	 Catch:{ all -> 0x0029 }
        r0 = r19;
        r1 = r20;
        if (r0 != r1) goto L_0x008a;
    L_0x0088:
        monitor-exit(r21);	 Catch:{ all -> 0x0029 }
        goto L_0x000e;
    L_0x008a:
        r20 = r25.getAccountID();	 Catch:{ all -> 0x0029 }
        r22 = "CUSTOM_XMPP_DOMAIN";
        r0 = r20;
        r1 = r22;
        r7 = r0.getAccountPropertyString(r1);	 Catch:{ all -> 0x0029 }
        if (r7 == 0) goto L_0x0105;
    L_0x009a:
        r20 = 0;
        r20 = r10[r20];	 Catch:{ all -> 0x0029 }
        if (r20 != 0) goto L_0x0105;
    L_0x00a0:
        r20 = logger;	 Catch:{ all -> 0x0029 }
        r22 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0029 }
        r22.<init>();	 Catch:{ all -> 0x0029 }
        r23 = "Connect using custom xmpp domain: ";
        r22 = r22.append(r23);	 Catch:{ all -> 0x0029 }
        r0 = r22;
        r22 = r0.append(r7);	 Catch:{ all -> 0x0029 }
        r22 = r22.toString();	 Catch:{ all -> 0x0029 }
        r0 = r20;
        r1 = r22;
        r0.info(r1);	 Catch:{ all -> 0x0029 }
        r0 = r25;
        r1 = r18;
        r19 = r0.connectUsingSRVRecords(r7, r1, r10, r15);	 Catch:{ all -> 0x0029 }
        r20 = logger;	 Catch:{ all -> 0x0029 }
        r22 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0029 }
        r22.<init>();	 Catch:{ all -> 0x0029 }
        r23 = "state for connectUsingSRVRecords: ";
        r22 = r22.append(r23);	 Catch:{ all -> 0x0029 }
        r0 = r22;
        r1 = r19;
        r22 = r0.append(r1);	 Catch:{ all -> 0x0029 }
        r22 = r22.toString();	 Catch:{ all -> 0x0029 }
        r0 = r20;
        r1 = r22;
        r0.info(r1);	 Catch:{ all -> 0x0029 }
        r20 = 0;
        r20 = r10[r20];	 Catch:{ all -> 0x0029 }
        if (r20 == 0) goto L_0x00f2;
    L_0x00ec:
        r25.setDnssecLoginFailure();	 Catch:{ all -> 0x0029 }
        monitor-exit(r21);	 Catch:{ all -> 0x0029 }
        goto L_0x000e;
    L_0x00f2:
        r20 = net.java.sip.communicator.impl.protocol.jabber.ProtocolProviderServiceJabberImpl.ConnectState.ABORT_CONNECTING;	 Catch:{ all -> 0x0029 }
        r0 = r19;
        r1 = r20;
        if (r0 == r1) goto L_0x0102;
    L_0x00fa:
        r20 = net.java.sip.communicator.impl.protocol.jabber.ProtocolProviderServiceJabberImpl.ConnectState.STOP_TRYING;	 Catch:{ all -> 0x0029 }
        r0 = r19;
        r1 = r20;
        if (r0 != r1) goto L_0x0105;
    L_0x0102:
        monitor-exit(r21);	 Catch:{ all -> 0x0029 }
        goto L_0x000e;
    L_0x0105:
        r20 = r25.getAccountID();	 Catch:{ all -> 0x0029 }
        r22 = "SERVER_ADDRESS";
        r0 = r20;
        r1 = r22;
        r16 = r0.getAccountPropertyString(r1);	 Catch:{ all -> 0x0029 }
        r20 = r25.getAccountID();	 Catch:{ all -> 0x0029 }
        r22 = "SERVER_PORT";
        r23 = 5222; // 0x1466 float:7.318E-42 double:2.58E-320;
        r0 = r20;
        r1 = r22;
        r2 = r23;
        r17 = r0.getAccountPropertyInt(r1, r2);	 Catch:{ all -> 0x0029 }
        r5 = 0;
        r5 = net.java.sip.communicator.util.NetworkUtils.getAandAAAARecords(r16, r17);	 Catch:{ ParseException -> 0x015e, DnssecException -> 0x016b }
    L_0x012a:
        if (r5 == 0) goto L_0x0131;
    L_0x012c:
        r0 = r5.length;	 Catch:{ all -> 0x0029 }
        r20 = r0;
        if (r20 != 0) goto L_0x017d;
    L_0x0131:
        r20 = logger;	 Catch:{ all -> 0x0029 }
        r22 = "No server addresses found";
        r0 = r20;
        r1 = r22;
        r0.error(r1);	 Catch:{ all -> 0x0029 }
        r20 = 0;
        r0 = r20;
        r1 = r25;
        r1.eventDuringLogin = r0;	 Catch:{ all -> 0x0029 }
        r20 = r25.getRegistrationState();	 Catch:{ all -> 0x0029 }
        r22 = net.java.sip.communicator.service.protocol.RegistrationState.CONNECTION_FAILED;	 Catch:{ all -> 0x0029 }
        r23 = 8;
        r24 = "No server addresses found";
        r0 = r25;
        r1 = r20;
        r2 = r22;
        r3 = r23;
        r4 = r24;
        r0.fireRegistrationStateChanged(r1, r2, r3, r4);	 Catch:{ all -> 0x0029 }
    L_0x015b:
        monitor-exit(r21);	 Catch:{ all -> 0x0029 }
        goto L_0x000e;
    L_0x015e:
        r8 = move-exception;
        r20 = logger;	 Catch:{ all -> 0x0029 }
        r22 = "Domain not resolved";
        r0 = r20;
        r1 = r22;
        r0.error(r1, r8);	 Catch:{ all -> 0x0029 }
        goto L_0x012a;
    L_0x016b:
        r8 = move-exception;
        r20 = logger;	 Catch:{ all -> 0x0029 }
        r22 = "DNSSEC failure for overridden server";
        r0 = r20;
        r1 = r22;
        r0.error(r1, r8);	 Catch:{ all -> 0x0029 }
        r25.setDnssecLoginFailure();	 Catch:{ all -> 0x0029 }
        monitor-exit(r21);	 Catch:{ all -> 0x0029 }
        goto L_0x000e;
    L_0x017d:
        r6 = r5;
        r14 = r6.length;	 Catch:{ all -> 0x0029 }
        r11 = 0;
    L_0x0180:
        if (r11 >= r14) goto L_0x015b;
    L_0x0182:
        r13 = r6[r11];	 Catch:{ all -> 0x0029 }
        r0 = r25;
        r1 = r18;
        r19 = r0.connectAndLogin(r13, r1, r15);	 Catch:{ XMPPException -> 0x019f }
        r20 = net.java.sip.communicator.impl.protocol.jabber.ProtocolProviderServiceJabberImpl.ConnectState.ABORT_CONNECTING;	 Catch:{ XMPPException -> 0x019f }
        r0 = r19;
        r1 = r20;
        if (r0 == r1) goto L_0x019c;
    L_0x0194:
        r20 = net.java.sip.communicator.impl.protocol.jabber.ProtocolProviderServiceJabberImpl.ConnectState.STOP_TRYING;	 Catch:{ XMPPException -> 0x019f }
        r0 = r19;
        r1 = r20;
        if (r0 != r1) goto L_0x01ac;
    L_0x019c:
        monitor-exit(r21);	 Catch:{ all -> 0x0029 }
        goto L_0x000e;
    L_0x019f:
        r9 = move-exception;
        r25.disconnectAndCleanConnection();	 Catch:{ all -> 0x0029 }
        r0 = r25;
        r20 = r0.isAuthenticationFailed(r9);	 Catch:{ all -> 0x0029 }
        if (r20 == 0) goto L_0x01ac;
    L_0x01ab:
        throw r9;	 Catch:{ all -> 0x0029 }
    L_0x01ac:
        r11 = r11 + 1;
        goto L_0x0180;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.jabber.ProtocolProviderServiceJabberImpl.initializeConnectAndLogin(net.java.sip.communicator.service.protocol.SecurityAuthority, int):void");
    }

    private JabberLoginStrategy createLoginStrategy() {
        if (getAccountID().getAccountPropertyString("CLIENT_TLS_CERTIFICATE") != null) {
            return new LoginByClientCertificateStrategy(getAccountID());
        }
        return new LoginByPasswordStrategy(this, getAccountID());
    }

    private void setDnssecLoginFailure() {
        this.eventDuringLogin = new RegistrationStateChangeEvent(this, getRegistrationState(), RegistrationState.UNREGISTERED, 0, "No usable host found due to DNSSEC failures");
    }

    private ConnectState connectUsingSRVRecords(String domain, String serviceName, boolean[] dnssecState, JabberLoginStrategy loginStrategy) throws XMPPException {
        SRVRecord[] srvRecords = null;
        try {
            srvRecords = NetworkUtils.getSRVRecords("xmpp-client", "tcp", domain);
        } catch (ParseException e) {
            logger.error("SRV record not resolved", e);
        } catch (DnssecException e2) {
            logger.error("DNSSEC failure for SRV lookup", e2);
            dnssecState[0] = true;
        }
        if (srvRecords != null) {
            for (SRVRecord srv : srvRecords) {
                InetSocketAddress[] addrs = null;
                try {
                    addrs = NetworkUtils.getAandAAAARecords(srv.getTarget(), srv.getPort());
                } catch (ParseException e3) {
                    logger.error("Invalid SRV record target", e3);
                } catch (DnssecException e22) {
                    logger.error("DNSSEC failure for A/AAAA lookup of SRV", e22);
                    dnssecState[0] = true;
                }
                if (addrs == null || addrs.length == 0) {
                    logger.error("No A/AAAA addresses found for " + srv.getTarget());
                } else {
                    InetSocketAddress[] arr$ = addrs;
                    int len$ = arr$.length;
                    int i$ = 0;
                    while (i$ < len$) {
                        InetSocketAddress isa = arr$[i$];
                        try {
                            if (JabberActivator.getConfigurationService().getBoolean(FailoverConnectionMonitor.REVERSE_FAILOVER_ENABLED_PROP, false)) {
                                FailoverConnectionMonitor.getInstance(this).setCurrent(domain, srv.getTarget());
                            }
                            return connectAndLogin(isa, serviceName, loginStrategy);
                        } catch (XMPPException ex) {
                            logger.error("Error connecting to " + isa + " for domain:" + domain + " serviceName:" + serviceName, ex);
                            disconnectAndCleanConnection();
                            if (isAuthenticationFailed(ex)) {
                                throw ex;
                            }
                            i$++;
                        }
                    }
                    continue;
                }
            }
        } else {
            logger.error("No SRV addresses found for _xmpp-client._tcp." + domain);
        }
        return ConnectState.CONTINUE_TRYING;
    }

    private ConnectState connectAndLogin(InetSocketAddress currentAddress, String serviceName, JabberLoginStrategy loginStrategy) throws XMPPException {
        String userID;
        boolean qualifiedUserID;
        if (getAccountID().getProtocolDisplayName().equals("Google Talk")) {
            userID = getAccountID().getUserID();
            qualifiedUserID = true;
        } else {
            userID = StringUtils.parseName(getAccountID().getUserID());
            qualifiedUserID = false;
        }
        try {
            return connectAndLogin(currentAddress, serviceName, userID, this.resource, loginStrategy);
        } catch (XMPPException ex) {
            disconnectAndCleanConnection();
            if ((ex.getWrappedThrowable() instanceof ConnectException) || (ex.getWrappedThrowable() instanceof NoRouteToHostException)) {
                this.eventDuringLogin = new RegistrationStateChangeEvent(this, getRegistrationState(), RegistrationState.CONNECTION_FAILED, 8, null);
                throw ex;
            } else if (qualifiedUserID) {
                throw ex;
            } else {
                try {
                    return connectAndLogin(currentAddress, serviceName, userID + Separators.AT + serviceName, this.resource, loginStrategy);
                } catch (XMPPException e) {
                    disconnectAndCleanConnection();
                    throw ex;
                }
            }
        }
    }

    private void loadResource() {
        if (this.resource == null) {
            String defaultResource = "jitsi";
            String autoGenenerateResource = getAccountID().getAccountPropertyString("AUTO_GENERATE_RESOURCE");
            if (autoGenenerateResource == null || Boolean.parseBoolean(autoGenenerateResource)) {
                this.resource = defaultResource + "-" + new BigInteger(32, new SecureRandom()).toString(32);
                return;
            }
            this.resource = getAccountID().getAccountPropertyString("RESOURCE");
            if (this.resource == null || this.resource.length() == 0) {
                this.resource = defaultResource;
            }
        }
    }

    private void loadProxy() throws OperationFailedException {
        String globalProxyType = JabberActivator.getConfigurationService().getString("net.java.sip.communicator.service.connectionProxyType");
        if (globalProxyType == null || globalProxyType.equals(ProxyType.NONE.name())) {
            this.proxy = ProxyInfo.forNoProxy();
            return;
        }
        String globalProxyAddress = JabberActivator.getConfigurationService().getString("net.java.sip.communicator.service.connectionProxyAddress");
        String globalProxyPortStr = JabberActivator.getConfigurationService().getString("net.java.sip.communicator.service.connectionProxyPort");
        try {
            int globalProxyPort = Integer.parseInt(globalProxyPortStr);
            String globalProxyUsername = JabberActivator.getConfigurationService().getString("net.java.sip.communicator.service.connectionProxyUsername");
            String globalProxyPassword = JabberActivator.getConfigurationService().getString("net.java.sip.communicator.service.connectionProxyPassword");
            if (globalProxyAddress == null || globalProxyAddress.length() <= 0) {
                throw new OperationFailedException("Missing Proxy Address", 7);
            }
            try {
                this.proxy = new ProxyInfo((ProxyInfo.ProxyType) Enum.valueOf(ProxyInfo.ProxyType.class, globalProxyType), globalProxyAddress, globalProxyPort, globalProxyUsername, globalProxyPassword);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid value for smack proxy enum", e);
                this.proxy = null;
            }
        } catch (NumberFormatException ex) {
            throw new OperationFailedException("Wrong proxy port, " + globalProxyPortStr + " does not represent an integer", 7, ex);
        }
    }

    private ConnectState connectAndLogin(InetSocketAddress address, String serviceName, String userName, String resource, JabberLoginStrategy loginStrategy) throws XMPPException {
        ConnectionConfiguration confConn = new ConnectionConfiguration(address.getAddress().getHostAddress(), address.getPort(), serviceName, this.proxy);
        confConn.setReconnectionAllowed(false);
        boolean tlsRequired = loginStrategy.isTlsRequired();
        confConn.setSecurityMode(tlsRequired ? SecurityMode.required : SecurityMode.enabled);
        if (this.connection != null) {
            logger.error("Connection is not null and isConnected:" + this.connection.isConnected(), new Exception("Trace possible duplicate connections: " + getAccountID().getAccountAddress()));
            disconnectAndCleanConnection();
        }
        this.connection = new XMPPConnection(confConn);
        try {
            CertificateService cvs = getCertificateVerificationService();
            if (cvs != null) {
                this.connection.setCustomSslContext(loginStrategy.createSslContext(cvs, getTrustManager(cvs, serviceName)));
            } else if (tlsRequired) {
                throw new XMPPException("Certificate verification service is unavailable and TLS is required");
            }
            if (this.debugger == null) {
                this.debugger = new SmackPacketDebugger();
            }
            this.debugger.setConnection(this.connection);
            this.connection.addPacketListener(this.debugger, null);
            this.connection.addPacketInterceptor(this.debugger, null);
            this.connection.connect();
            setTrafficClass();
            if (this.abortConnecting) {
                this.abortConnecting = false;
                disconnectAndCleanConnection();
                return ConnectState.ABORT_CONNECTING;
            }
            registerServiceDiscoveryManager();
            if (this.connectionListener == null) {
                this.connectionListener = new JabberConnectionListener();
            }
            if (!this.connection.isSecureConnection() && tlsRequired) {
                throw new XMPPException("TLS is required by client");
            } else if (this.connection.isConnected()) {
                this.connection.addConnectionListener(this.connectionListener);
                if (this.abortConnecting) {
                    this.abortConnecting = false;
                    disconnectAndCleanConnection();
                    return ConnectState.ABORT_CONNECTING;
                }
                fireRegistrationStateChanged(getRegistrationState(), RegistrationState.REGISTERING, -1, null);
                if (!loginStrategy.login(this.connection, userName, resource)) {
                    disconnectAndCleanConnection();
                    this.eventDuringLogin = null;
                    fireRegistrationStateChanged(getRegistrationState(), RegistrationState.CONNECTION_FAILED, 1, loginStrategy.getClass().getName() + " requests abort");
                    return ConnectState.ABORT_CONNECTING;
                } else if (this.connection.isAuthenticated()) {
                    this.eventDuringLogin = null;
                    fireRegistrationStateChanged(getRegistrationState(), RegistrationState.REGISTERED, -1, null);
                    try {
                        ((OperationSetPersistentPresenceJabberImpl) getOperationSet(OperationSetPersistentPresence.class)).publishPresenceStatus(getJabberStatusEnum().getStatus("Available"), "");
                    } catch (Exception e) {
                        logger.error("Failed to publish presence status");
                    }
                    return ConnectState.STOP_TRYING;
                } else {
                    disconnectAndCleanConnection();
                    this.eventDuringLogin = null;
                    fireRegistrationStateChanged(getRegistrationState(), RegistrationState.UNREGISTERED, -1, null);
                    return ConnectState.CONTINUE_TRYING;
                }
            } else {
                disconnectAndCleanConnection();
                logger.error("Connection not established, server not found!");
                this.eventDuringLogin = null;
                fireRegistrationStateChanged(getRegistrationState(), RegistrationState.CONNECTION_FAILED, 8, null);
                return ConnectState.ABORT_CONNECTING;
            }
        } catch (GeneralSecurityException e2) {
            logger.error("Error creating custom trust manager", e2);
            throw new XMPPException("Error creating custom trust manager", e2);
        }
    }

    private X509TrustManager getTrustManager(CertificateService cvs, String serviceName) throws GeneralSecurityException {
        return new HostTrustManager(cvs.getTrustManager(Arrays.asList(new String[]{serviceName, "_xmpp-client." + serviceName})));
    }

    private void registerServiceDiscoveryManager() {
        ServiceDiscoveryManager.setIdentityName(System.getProperty("sip-communicator.application.name", "SIP Communicator ") + System.getProperty("sip-communicator.version", "SVN"));
        ServiceDiscoveryManager.setIdentityType("pc");
        this.discoveryManager = new ScServiceDiscoveryManager(this, new String[]{SpecificError.namespace}, (String[]) this.supportedFeatures.toArray(new String[this.supportedFeatures.size()]));
        boolean isCallingDisabled = JabberActivator.getConfigurationService().getBoolean(IS_CALLING_DISABLED, false);
        if (this.accountID != null && this.accountID.getAccountPropertyBoolean("CALLING_DISABLED", false)) {
            isCallingDisabled = true;
        }
        if (!(!isGTalkTesting() || isCallingDisabled || false)) {
            this.discoveryManager.addExtFeature(CAPS_GTALK_WEB_VOICE);
            this.discoveryManager.addExtFeature(CAPS_GTALK_WEB_VIDEO);
            this.discoveryManager.addExtFeature(CAPS_GTALK_WEB_CAMERA);
            this.discoveryManager.addFeature(URN_GOOGLE_VOICE);
            this.discoveryManager.addFeature(URN_GOOGLE_VIDEO);
            this.discoveryManager.addFeature(URN_GOOGLE_CAMERA);
        }
        if (this.opsetContactCapabilities != null) {
            this.opsetContactCapabilities.setDiscoveryManager(this.discoveryManager);
        }
    }

    public void disconnectAndCleanConnection() {
        if (this.connection != null) {
            this.connection.removeConnectionListener(this.connectionListener);
            try {
                OperationSetPersistentPresenceJabberImpl opSet = (OperationSetPersistentPresenceJabberImpl) getOperationSet(OperationSetPersistentPresence.class);
                Presence unavailablePresence = new Presence(Type.unavailable);
                if (!(opSet == null || org.jitsi.util.StringUtils.isNullOrEmpty(opSet.getCurrentStatusMessage()))) {
                    unavailablePresence.setStatus(opSet.getCurrentStatusMessage());
                }
                this.connection.disconnect(unavailablePresence);
            } catch (Exception e) {
            }
            this.connectionListener = null;
            this.connection = null;
            try {
                if (this.opsetContactCapabilities != null) {
                    this.opsetContactCapabilities.setDiscoveryManager(null);
                }
                if (this.discoveryManager != null) {
                    this.discoveryManager.stop();
                    this.discoveryManager = null;
                }
            } catch (Throwable th) {
                if (this.discoveryManager != null) {
                    this.discoveryManager.stop();
                    this.discoveryManager = null;
                }
            }
        }
    }

    public void unregister() {
        unregister(true);
    }

    public void unregister(boolean fireEvent) {
        synchronized (this.initializationLock) {
            if (fireEvent) {
                this.eventDuringLogin = null;
                fireRegistrationStateChanged(getRegistrationState(), RegistrationState.UNREGISTERING, -1, null);
            }
            disconnectAndCleanConnection();
            RegistrationState currRegState = getRegistrationState();
            if (fireEvent) {
                this.eventDuringLogin = null;
                fireRegistrationStateChanged(currRegState, RegistrationState.UNREGISTERED, 0, null);
            }
        }
    }

    public String getProtocolName() {
        return "Jabber";
    }

    /* access modifiers changed from: protected */
    public void initialize(String screenname, AccountID accountID) {
        synchronized (this.initializationLock) {
            this.accountID = accountID;
            this.supportedFeatures.clear();
            clearRegistrationStateChangeListener();
            clearSupportedOperationSet();
            synchronized (providerCreationLock) {
                if (providerManager == null) {
                    try {
                        ProviderManager.setInstance(new ProviderManagerExt());
                        providerManager = ProviderManager.getInstance();
                    } catch (Throwable th) {
                        providerManager = ProviderManager.getInstance();
                    }
                }
            }
            String protocolIconPath = accountID.getAccountPropertyString("PROTOCOL_ICON_PATH");
            if (protocolIconPath == null) {
                protocolIconPath = "resources/images/protocol/jabber";
            }
            this.jabberIcon = new ProtocolIconJabberImpl(protocolIconPath);
            this.jabberStatusEnum = JabberStatusEnum.getJabberStatusEnum(protocolIconPath);
            this.supportedFeatures.add("http://jabber.org/protocol/disco#info");
            String keepAliveStrValue = accountID.getAccountPropertyString("KEEP_ALIVE_METHOD");
            InfoRetreiver infoRetreiver = new InfoRetreiver(this, screenname);
            OperationSetPersistentPresenceJabberImpl operationSetPersistentPresenceJabberImpl = new OperationSetPersistentPresenceJabberImpl(this, infoRetreiver);
            addSupportedOperationSet(OperationSetPersistentPresence.class, operationSetPersistentPresenceJabberImpl);
            addSupportedOperationSet(OperationSetPresence.class, operationSetPersistentPresenceJabberImpl);
            if (accountID.getAccountPropertyString("READ_ONLY_GROUPS") != null) {
                addSupportedOperationSet(OperationSetPersistentPresencePermissions.class, new OperationSetPersistentPresencePermissionsJabberImpl(this));
            }
            OperationSetBasicInstantMessagingJabberImpl basicInstantMessaging = new OperationSetBasicInstantMessagingJabberImpl(this);
            if ((keepAliveStrValue == null || keepAliveStrValue.equalsIgnoreCase("XEP-0199")) && this.keepAliveManager == null) {
                this.keepAliveManager = new KeepAliveManager(this);
            }
            addSupportedOperationSet(OperationSetBasicInstantMessaging.class, basicInstantMessaging);
            addSupportedOperationSet(OperationSetExtendedAuthorizations.class, new OperationSetExtendedAuthorizationsJabberImpl(this, operationSetPersistentPresenceJabberImpl));
            addSupportedOperationSet(OperationSetWhiteboarding.class, new OperationSetWhiteboardingJabberImpl(this));
            addSupportedOperationSet(OperationSetTypingNotifications.class, new OperationSetTypingNotificationsJabberImpl(this));
            addSupportedOperationSet(OperationSetMultiUserChat.class, new OperationSetMultiUserChatJabberImpl(this));
            addSupportedOperationSet(OperationSetServerStoredContactInfo.class, new OperationSetServerStoredContactInfoJabberImpl(infoRetreiver));
            OperationSetServerStoredAccountInfo accountInfo = new OperationSetServerStoredAccountInfoJabberImpl(this, infoRetreiver, screenname);
            addSupportedOperationSet(OperationSetServerStoredAccountInfo.class, accountInfo);
            addSupportedOperationSet(OperationSetAvatar.class, new OperationSetAvatarJabberImpl(this, accountInfo));
            addSupportedOperationSet(OperationSetFileTransfer.class, new OperationSetFileTransferJabberImpl(this));
            addSupportedOperationSet(OperationSetInstantMessageTransform.class, new OperationSetInstantMessageTransformImpl());
            this.supportedFeatures.add(ThumbnailElement.NAMESPACE);
            this.supportedFeatures.add(ThumbnailIQ.NAMESPACE);
            addSupportedOperationSet(OperationSetThumbnailedFileFactory.class, new OperationSetThumbnailedFileFactoryImpl());
            this.supportedFeatures.add("http://jabber.org/protocol/muc#rooms");
            this.supportedFeatures.add("http://jabber.org/protocol/muc#traffic");
            this.supportedFeatures.add("urn:xmpp:jingle:apps:rtp:rtp-hdrext:0");
            providerManager.addIQProvider(JingleIQ.ELEMENT_NAME, "urn:xmpp:jingle:1", new JingleIQProvider());
            providerManager.addIQProvider("inputevt", "http://jitsi.org/protocol/inputevt", new InputEvtIQProvider());
            providerManager.addIQProvider("conference-info", CoinIQ.NAMESPACE, new CoinIQProvider());
            this.supportedFeatures.add(URN_XMPP_JINGLE_COIN);
            providerManager.addIQProvider(JingleInfoQueryIQ.ELEMENT_NAME, JingleInfoQueryIQ.NAMESPACE, new JingleInfoQueryIQProvider());
            providerManager.addIQProvider("conference", ColibriConferenceIQ.NAMESPACE, new ColibriIQProvider());
            providerManager.addExtensionProvider("conference", ConferenceDescriptionPacketExtension.NAMESPACE, new Provider());
            providerManager.addExtensionProvider("received", CarbonPacketExtension.NAMESPACE, new CarbonPacketExtension.Provider("received"));
            providerManager.addExtensionProvider(CarbonPacketExtension.SENT_ELEMENT_NAME, CarbonPacketExtension.NAMESPACE, new CarbonPacketExtension.Provider(CarbonPacketExtension.SENT_ELEMENT_NAME));
            boolean isCallingDisabled = JabberActivator.getConfigurationService().getBoolean(IS_CALLING_DISABLED, false);
            boolean isCallingDisabledForAccount = accountID.getAccountPropertyBoolean("CALLING_DISABLED", false);
            if (!(isCallingDisabled || isCallingDisabledForAccount)) {
                OperationSetBasicTelephonyJabberImpl basicTelephony = new OperationSetBasicTelephonyJabberImpl(this);
                addSupportedOperationSet(OperationSetAdvancedTelephony.class, basicTelephony);
                addSupportedOperationSet(OperationSetBasicTelephony.class, basicTelephony);
                addSupportedOperationSet(OperationSetSecureZrtpTelephony.class, basicTelephony);
                addSupportedOperationSet(OperationSetSecureSDesTelephony.class, basicTelephony);
                addSupportedOperationSet(OperationSetVideoTelephony.class, new OperationSetVideoTelephonyJabberImpl(basicTelephony));
                addSupportedOperationSet(OperationSetTelephonyConferencing.class, new OperationSetTelephonyConferencingJabberImpl(this));
                addSupportedOperationSet(OperationSetBasicAutoAnswer.class, new OperationSetAutoAnswerJabberImpl(this));
                addSupportedOperationSet(OperationSetResourceAwareTelephony.class, new OperationSetResAwareTelephonyJabberImpl(basicTelephony));
                if (!JabberActivator.getConfigurationService().getBoolean("net.java.sip.communicator.service.protocol.VIDEO_BRIDGE_DISABLED", false)) {
                    addSupportedOperationSet(OperationSetVideoBridge.class, new OperationSetVideoBridgeImpl(this));
                }
                addSupportedOperationSet(OperationSetDTMF.class, new OperationSetDTMFJabberImpl(this));
                addJingleFeatures();
                boolean isDesktopStreamingDisabled = JabberActivator.getConfigurationService().getBoolean(IS_DESKTOP_STREAMING_DISABLED, false);
                boolean isAccountDesktopStreamingDisabled = accountID.getAccountPropertyBoolean("DESKTOP_STREAMING_DISABLED", false);
                if (!(isDesktopStreamingDisabled || isAccountDesktopStreamingDisabled)) {
                    addSupportedOperationSet(OperationSetDesktopStreaming.class, new OperationSetDesktopStreamingJabberImpl(basicTelephony));
                    addSupportedOperationSet(OperationSetDesktopSharingServer.class, new OperationSetDesktopSharingServerJabberImpl(basicTelephony));
                    this.supportedFeatures.add(InputEvtIQ.NAMESPACE_SERVER);
                    addSupportedOperationSet(OperationSetDesktopSharingClient.class, new OperationSetDesktopSharingClientJabberImpl(this));
                    this.supportedFeatures.add(InputEvtIQ.NAMESPACE_CLIENT);
                }
            }
            this.opsetContactCapabilities = new OperationSetContactCapabilitiesJabberImpl(this);
            if (this.discoveryManager != null) {
                this.opsetContactCapabilities.setDiscoveryManager(this.discoveryManager);
            }
            addSupportedOperationSet(OperationSetContactCapabilities.class, this.opsetContactCapabilities);
            addSupportedOperationSet(OperationSetGenericNotifications.class, new OperationSetGenericNotificationsJabberImpl(this));
            this.supportedFeatures.add("jabber:iq:version");
            if (this.versionManager == null) {
                this.versionManager = new VersionManager(this);
            }
            this.supportedFeatures.add(MessageCorrectionExtension.NAMESPACE);
            addSupportedOperationSet(OperationSetMessageCorrection.class, basicInstantMessaging);
            addSupportedOperationSet(OperationSetChangePassword.class, new OperationSetChangePasswordJabberImpl(this));
            addSupportedOperationSet(OperationSetCusaxUtils.class, new OperationSetCusaxUtilsJabberImpl(this));
            this.isInitialized = true;
        }
    }

    private void addJingleFeatures() {
        this.supportedFeatures.add("urn:xmpp:jingle:1");
        this.supportedFeatures.add("urn:xmpp:jingle:apps:rtp:1");
        this.supportedFeatures.add("urn:xmpp:jingle:transports:raw-udp:1");
        if (this.accountID.getAccountPropertyBoolean("ICE_ENABLED", true)) {
            this.supportedFeatures.add("urn:xmpp:jingle:transports:ice-udp:1");
        }
        this.supportedFeatures.add(URN_XMPP_JINGLE_RTP_AUDIO);
        this.supportedFeatures.add(URN_XMPP_JINGLE_RTP_VIDEO);
        this.supportedFeatures.add("urn:xmpp:jingle:apps:rtp:zrtp:1");
        if (this.accountID.getAccountPropertyBoolean(ProtocolProviderFactoryJabberImpl.IS_USE_JINGLE_NODES, true)) {
            this.supportedFeatures.add("http://jabber.org/protocol/jinglenodes");
        }
        this.supportedFeatures.add("urn:xmpp:jingle:transfer:0");
        if (this.accountID.getAccountPropertyBoolean("DEFAULT_ENCRYPTION", true) && this.accountID.isEncryptionProtocolEnabled(DtlsControl.PROTO_NAME)) {
            this.supportedFeatures.add("urn:xmpp:jingle:apps:dtls:0");
        }
    }

    public void shutdown() {
        synchronized (this.initializationLock) {
            if (logger.isTraceEnabled()) {
                logger.trace("Killing the Jabber Protocol Provider.");
            }
            OperationSetBasicTelephonyJabberImpl telephony = (OperationSetBasicTelephonyJabberImpl) getOperationSet(OperationSetBasicTelephony.class);
            if (telephony != null) {
                telephony.shutdown();
            }
            disconnectAndCleanConnection();
            this.isInitialized = false;
        }
    }

    public boolean isInitialized() {
        return this.isInitialized;
    }

    public AccountID getAccountID() {
        return this.accountID;
    }

    public XMPPConnection getConnection() {
        return this.connection;
    }

    private boolean isAuthenticationFailed(XMPPException ex) {
        String exMsg = ex.getMessage().toLowerCase();
        return ((exMsg.indexOf("sasl authentication") == -1 || exMsg.indexOf("failed") == -1) && exMsg.indexOf("does not support compatible authentication mechanism") == -1 && exMsg.indexOf("unable to determine password") == -1) ? false : true;
    }

    private void fireRegistrationStateChanged(XMPPException ex) {
        int reason = -1;
        RegistrationState regState = RegistrationState.UNREGISTERED;
        Throwable wrappedEx = ex.getWrappedThrowable();
        if (wrappedEx == null || !((wrappedEx instanceof UnknownHostException) || (wrappedEx instanceof ConnectException) || (wrappedEx instanceof SocketException))) {
            String exMsg = ex.getMessage().toLowerCase();
            if (isAuthenticationFailed(ex)) {
                JabberActivator.getProtocolProviderFactory().storePassword(getAccountID(), null);
                fireRegistrationStateChanged(getRegistrationState(), RegistrationState.AUTHENTICATION_FAILED, 1, null);
                reregister(1);
                return;
            } else if (exMsg.indexOf("no response from the server") != -1 || exMsg.indexOf("connection failed") != -1) {
                reason = -1;
                regState = RegistrationState.CONNECTION_FAILED;
            } else if (exMsg.indexOf("tls is required") != -1) {
                regState = RegistrationState.AUTHENTICATION_FAILED;
                reason = 9;
            }
        } else {
            reason = 8;
            regState = RegistrationState.CONNECTION_FAILED;
        }
        if (regState == RegistrationState.UNREGISTERED || regState == RegistrationState.CONNECTION_FAILED) {
            disconnectAndCleanConnection();
        }
        fireRegistrationStateChanged(getRegistrationState(), regState, reason, null);
    }

    public ProtocolIcon getProtocolIcon() {
        return this.jabberIcon;
    }

    /* access modifiers changed from: 0000 */
    public JabberStatusEnum getJabberStatusEnum() {
        return this.jabberStatusEnum;
    }

    public boolean isExtFeatureListSupported(String jid, String... extFeatures) {
        Caps caps = this.discoveryManager.getCapsManager().getCapsByUser(jid);
        boolean domainEquals = StringUtils.parseServer(jid).equals(this.accountID.getAccountPropertyString("TELEPHONY_BYPASS_GTALK_CAPS"));
        if (caps == null || caps.ext == null) {
            return false;
        }
        String[] exts = caps.ext.split(Separators.SP);
        boolean found = false;
        for (String extFeature : extFeatures) {
            if (extFeature.equals(CAPS_GTALK_WEB_VOICE) && domainEquals) {
                return true;
            }
            found = false;
            for (String ext : exts) {
                if (ext.equals(extFeature)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return found;
            }
        }
        return found;
    }

    public boolean isFeatureListSupported(String jid, String... features) {
        boolean isFeatureListSupported = true;
        try {
            if (this.discoveryManager == null) {
                return 1;
            }
            DiscoverInfo featureInfo = this.discoveryManager.discoverInfoNonBlocking(jid);
            if (featureInfo == null) {
                return 1;
            }
            for (String feature : features) {
                if (!featureInfo.containsFeature(feature)) {
                    isFeatureListSupported = false;
                    break;
                }
            }
            return isFeatureListSupported;
        } catch (XMPPException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to retrive discovery info.", e);
            }
        }
    }

    public boolean isFeatureSupported(String jid, String feature) {
        return isFeatureListSupported(jid, feature);
    }

    public String getFullJid(Contact contact) {
        return getFullJid(contact.getAddress());
    }

    public String getFullJid(String bareJid) {
        XMPPConnection connection = getConnection();
        if (connection != null && connection.isConnected()) {
            Roster roster = connection.getRoster();
            if (roster != null) {
                return roster.getPresence(bareJid).getFrom();
            }
        }
        return null;
    }

    public ScServiceDiscoveryManager getDiscoveryManager() {
        return this.discoveryManager;
    }

    public String getOurJID() {
        String jid = null;
        if (this.connection != null) {
            jid = this.connection.getUser();
        }
        if (jid != null) {
            return jid;
        }
        String accountIDUserID = getAccountID().getUserID();
        String userID = StringUtils.parseName(accountIDUserID);
        return userID + Separators.AT + StringUtils.parseServer(accountIDUserID);
    }

    public InetAddress getNextHop() throws IllegalArgumentException {
        String nextHopStr;
        if (this.proxy == null || this.proxy.getProxyType() == ProxyInfo.ProxyType.NONE) {
            nextHopStr = getConnection().getHost();
        } else {
            nextHopStr = this.proxy.getProxyAddress();
        }
        try {
            InetAddress nextHop = NetworkUtils.getInetAddress(nextHopStr);
            if (logger.isDebugEnabled()) {
                logger.debug("Returning address " + nextHop + " as next hop.");
            }
            return nextHop;
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("seems we don't have a valid next hop.", ex);
        }
    }

    public void startJingleNodesDiscovery() {
        JabberAccountIDImpl accID = (JabberAccountIDImpl) getAccountID();
        SmackServiceNode service = new SmackServiceNode(this.connection, 60000);
        this.connection.addConnectionListener(service);
        for (JingleNodeDescriptor desc : accID.getJingleNodes()) {
            service.addTrackerEntry(new TrackerEntry(desc.isRelaySupported() ? TrackerEntry.Type.relay : TrackerEntry.Type.tracker, Policy._public, desc.getJID(), "udp"));
        }
        new Thread(new JingleNodesServiceDiscovery(service, this.connection, accID, this.jingleNodesSyncRoot)).start();
        this.jingleNodesServiceNode = service;
    }

    public SmackServiceNode getJingleNodesServiceNode() {
        SmackServiceNode smackServiceNode;
        synchronized (this.jingleNodesSyncRoot) {
            smackServiceNode = this.jingleNodesServiceNode;
        }
        return smackServiceNode;
    }

    public static void throwOperationFailedException(String message, int errorCode, Throwable cause, Logger logger) throws OperationFailedException {
        logger.error(message, cause);
        if (cause == null) {
            throw new OperationFailedException(message, errorCode);
        }
        throw new OperationFailedException(message, errorCode, cause);
    }

    public SecurityAuthority getAuthority() {
        return this.authority;
    }

    public boolean isGTalkTesting() {
        return Boolean.getBoolean("gtalktesting") || JabberActivator.getConfigurationService().getBoolean("net.java.sip.communicator.impl.protocol.jabber.gtalktesting", false) || this.accountID.getAccountPropertyBoolean("GTALK_ICE_ENABLED", true);
    }

    /* access modifiers changed from: 0000 */
    public UserCredentials getUserCredentials() {
        return this.userCredentials;
    }

    public boolean isGmailOrGoogleAppsAccount() {
        return isGmailOrGoogleAppsAccount(StringUtils.parseServer(getAccountID().getUserID()));
    }

    public static boolean isGmailOrGoogleAppsAccount(String domain) {
        try {
            SRVRecord[] srvRecords = NetworkUtils.getSRVRecords("xmpp-client", "tcp", domain);
            if (srvRecords == null) {
                return false;
            }
            for (SRVRecord srv : srvRecords) {
                if (srv.getTarget().endsWith("google.com") || srv.getTarget().endsWith("google.com.")) {
                    return true;
                }
            }
            return false;
        } catch (ParseException e) {
            logger.info("Failed to get SRV records for XMPP domain");
            return false;
        } catch (DnssecException e2) {
            logger.error("DNSSEC failure while checking for google domains", e2);
            return false;
        }
    }

    private void setTrafficClass() {
        Socket s = this.connection.getSocket();
        if (s != null) {
            String dscp = JabberActivator.getConfigurationService().getString(XMPP_DSCP_PROPERTY);
            if (dscp != null) {
                try {
                    int dscpInt = Integer.parseInt(dscp) << 2;
                    if (dscpInt > 0) {
                        s.setTrafficClass(dscpInt);
                    }
                } catch (Exception e) {
                    logger.info("Failed to set trafficClass", e);
                }
            }
        }
    }

    public String getJitsiVideobridge() {
        XMPPConnection connection = getConnection();
        if (connection != null) {
            ScServiceDiscoveryManager discoveryManager = getDiscoveryManager();
            String serviceName = connection.getServiceName();
            DiscoverItems discoverItems = null;
            try {
                discoverItems = discoveryManager.discoverItems(serviceName);
            } catch (XMPPException xmppe) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to discover the items associated with Jabber entity: " + serviceName, xmppe);
                }
            }
            if (discoverItems != null) {
                Iterator<Item> discoverItemIter = discoverItems.getItems();
                while (discoverItemIter.hasNext()) {
                    String entityID = ((Item) discoverItemIter.next()).getEntityID();
                    DiscoverInfo discoverInfo = null;
                    try {
                        discoverInfo = discoveryManager.discoverInfo(entityID);
                    } catch (XMPPException xmppe2) {
                        logger.warn("Failed to discover information about Jabber entity: " + entityID, xmppe2);
                    }
                    if (discoverInfo != null && discoverInfo.containsFeature(ColibriConferenceIQ.NAMESPACE)) {
                        return entityID;
                    }
                }
            }
        }
        return null;
    }

    private static void loadJabberServiceClasses() {
        if (OSUtils.IS_ANDROID) {
            try {
                SmackConfiguration.getVersion();
                Class.forName(ServiceDiscoveryManager.class.getName());
                Class.forName(DelayInformation.class.getName());
                Class.forName(DelayInformationProvider.class.getName());
                Class.forName(Socks5BytestreamManager.class.getName());
                Class.forName(XHTMLManager.class.getName());
                Class.forName(InBandBytestreamManager.class.getName());
            } catch (ClassNotFoundException e) {
                logger.error("Error loading classes in smack", e);
            }
        }
    }
}
