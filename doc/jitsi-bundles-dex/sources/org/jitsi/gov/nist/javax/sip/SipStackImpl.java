package org.jitsi.gov.nist.javax.sip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.sdp.SdpConstants;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.LogWriter;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.ServerLogger;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.core.net.AddressResolver;
import org.jitsi.gov.nist.core.net.NetworkLayer;
import org.jitsi.gov.nist.core.net.SslNetworkLayer;
import org.jitsi.gov.nist.javax.sip.clientauthutils.AccountManager;
import org.jitsi.gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import org.jitsi.gov.nist.javax.sip.clientauthutils.AuthenticationHelperImpl;
import org.jitsi.gov.nist.javax.sip.clientauthutils.SecureAccountManager;
import org.jitsi.gov.nist.javax.sip.parser.MessageParserFactory;
import org.jitsi.gov.nist.javax.sip.parser.PipelinedMsgParser;
import org.jitsi.gov.nist.javax.sip.parser.StringMsgParser;
import org.jitsi.gov.nist.javax.sip.parser.StringMsgParserFactory;
import org.jitsi.gov.nist.javax.sip.stack.ClientAuthType;
import org.jitsi.gov.nist.javax.sip.stack.DefaultMessageLogFactory;
import org.jitsi.gov.nist.javax.sip.stack.DefaultRouter;
import org.jitsi.gov.nist.javax.sip.stack.MessageProcessorFactory;
import org.jitsi.gov.nist.javax.sip.stack.OIOMessageProcessorFactory;
import org.jitsi.gov.nist.javax.sip.stack.SIPEventInterceptor;
import org.jitsi.gov.nist.javax.sip.stack.SIPMessageValve;
import org.jitsi.gov.nist.javax.sip.stack.SIPTransactionStack;
import org.jitsi.gov.nist.javax.sip.stack.timers.DefaultSipTimer;
import org.jitsi.gov.nist.javax.sip.stack.timers.SipTimer;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.ListeningPoint;
import org.jitsi.javax.sip.ObjectInUseException;
import org.jitsi.javax.sip.PeerUnavailableException;
import org.jitsi.javax.sip.ProviderDoesNotExistException;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.SipListener;
import org.jitsi.javax.sip.SipProvider;
import org.jitsi.javax.sip.SipStack;
import org.jitsi.javax.sip.TransportNotSupportedException;
import org.jitsi.javax.sip.address.Router;
import org.jitsi.javax.sip.header.HeaderFactory;

public class SipStackImpl extends SIPTransactionStack implements SipStack, SipStackExt {
    public static final Integer MAX_DATAGRAM_SIZE = Integer.valueOf(65536);
    /* access modifiers changed from: private|static */
    public static StackLogger logger = CommonLogger.getLogger(SipStackImpl.class);
    private String[] cipherSuites;
    private Properties configurationProperties;
    private String[] enabledProtocols;
    private EventScanner eventScanner;
    protected Hashtable<String, ListeningPointImpl> listeningPoints;
    private boolean reEntrantListener;
    SipListener sipListener;
    protected List<SipProviderImpl> sipProviders;
    private Semaphore stackSemaphore;
    TlsSecurityPolicy tlsSecurityPolicy;

    protected SipStackImpl() {
        this.stackSemaphore = new Semaphore(1);
        this.cipherSuites = new String[]{"TLS_RSA_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_DH_anon_WITH_AES_128_CBC_SHA", "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA"};
        this.enabledProtocols = new String[]{"SSLv3", "SSLv2Hello", "TLSv1"};
        super.setMessageFactory(new NistSipMessageFactoryImpl(this));
        this.eventScanner = new EventScanner(this);
        this.listeningPoints = new Hashtable();
        this.sipProviders = Collections.synchronizedList(new LinkedList());
    }

    private void reInitialize() {
        super.reInit();
        this.eventScanner = new EventScanner(this);
        this.listeningPoints = new Hashtable();
        this.sipProviders = Collections.synchronizedList(new LinkedList());
        this.sipListener = null;
        if (!getTimer().isStarted()) {
            try {
                setTimer((SipTimer) Class.forName(this.configurationProperties.getProperty("org.jitsi.gov.nist.javax.sip.TIMER_CLASS_NAME", DefaultSipTimer.class.getName())).newInstance());
                getTimer().start(this, this.configurationProperties);
                if (getThreadAuditor().isEnabled()) {
                    getTimer().schedule(new PingTimer(null), 0);
                }
            } catch (Exception e) {
                logger.logError("Bad configuration value for gov.nist.javax.sip.TIMER_CLASS_NAME", e);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean isAutomaticDialogSupportEnabled() {
        return this.isAutomaticDialogSupportEnabled;
    }

    public SipStackImpl(Properties configurationProperties) throws PeerUnavailableException {
        this();
        Properties mergedSystemProperties = new MergedSystemProperties(configurationProperties);
        this.configurationProperties = mergedSystemProperties;
        String address = mergedSystemProperties.getProperty("org.jitsi.javax.sip.IP_ADDRESS");
        if (address != null) {
            try {
                super.setHostAddress(address);
            } catch (UnknownHostException e) {
                throw new PeerUnavailableException("bad address " + address);
            }
        }
        String name = mergedSystemProperties.getProperty("org.jitsi.javax.sip.STACK_NAME");
        if (name == null) {
            throw new PeerUnavailableException("stack name is missing");
        }
        super.setStackName(name);
        String stackLoggerClassName = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.STACK_LOGGER");
        if (stackLoggerClassName == null) {
            stackLoggerClassName = "org.jitsi.gov.nist.core.LogWriter";
        }
        try {
            StackLogger stackLogger = (StackLogger) Class.forName(stackLoggerClassName).getConstructor(new Class[0]).newInstance(new Object[0]);
            CommonLogger.legacyLogger = stackLogger;
            stackLogger.setStackProperties(mergedSystemProperties);
            String serverLoggerClassName = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.SERVER_LOGGER");
            if (serverLoggerClassName == null) {
                serverLoggerClassName = "org.jitsi.gov.nist.javax.sip.stack.ServerLog";
            }
            try {
                this.serverLogger = (ServerLogger) Class.forName(serverLoggerClassName).getConstructor(new Class[0]).newInstance(new Object[0]);
                this.serverLogger.setSipStack(this);
                this.serverLogger.setStackProperties(mergedSystemProperties);
                this.outboundProxy = mergedSystemProperties.getProperty("org.jitsi.javax.sip.OUTBOUND_PROXY");
                this.defaultRouter = new DefaultRouter(this, this.outboundProxy);
                String routerPath = mergedSystemProperties.getProperty("org.jitsi.javax.sip.ROUTER_PATH");
                if (routerPath == null) {
                    routerPath = "org.jitsi.gov.nist.javax.sip.stack.DefaultRouter";
                }
                try {
                    StringTokenizer stringTokenizer;
                    super.setRouter((Router) Class.forName(routerPath).getConstructor(new Class[]{SipStack.class, String.class}).newInstance(new Object[]{this, this.outboundProxy}));
                    String useRouterForAll = mergedSystemProperties.getProperty("org.jitsi.javax.sip.USE_ROUTER_FOR_ALL_URIS");
                    this.useRouterForAll = true;
                    if (useRouterForAll != null) {
                        this.useRouterForAll = "true".equalsIgnoreCase(useRouterForAll);
                    }
                    String extensionMethods = mergedSystemProperties.getProperty("org.jitsi.javax.sip.EXTENSION_METHODS");
                    if (extensionMethods != null) {
                        stringTokenizer = new StringTokenizer(extensionMethods);
                        while (stringTokenizer.hasMoreTokens()) {
                            String em = stringTokenizer.nextToken(Separators.COLON);
                            if (em.equalsIgnoreCase("BYE") || em.equalsIgnoreCase("INVITE") || em.equalsIgnoreCase("SUBSCRIBE") || em.equalsIgnoreCase("NOTIFY") || em.equalsIgnoreCase("ACK") || em.equalsIgnoreCase("OPTIONS")) {
                                throw new PeerUnavailableException("Bad extension method " + em);
                            }
                            addExtensionMethod(em);
                        }
                    }
                    String keyStoreFile = mergedSystemProperties.getProperty("javax.net.ssl.keyStore");
                    String trustStoreFile = mergedSystemProperties.getProperty("javax.net.ssl.trustStore");
                    if (keyStoreFile != null) {
                        if (trustStoreFile == null) {
                            trustStoreFile = keyStoreFile;
                        }
                        String keyStorePassword = mergedSystemProperties.getProperty("javax.net.ssl.keyStorePassword");
                        try {
                            char[] toCharArray;
                            if (keyStorePassword != null) {
                                toCharArray = keyStorePassword.toCharArray();
                            } else {
                                toCharArray = null;
                            }
                            this.networkLayer = new SslNetworkLayer(trustStoreFile, keyStoreFile, toCharArray, mergedSystemProperties.getProperty("javax.net.ssl.keyStoreType"));
                        } catch (Exception e1) {
                            logger.logError("could not instantiate SSL networking", e1);
                        }
                    }
                    this.isAutomaticDialogSupportEnabled = mergedSystemProperties.getProperty("org.jitsi.javax.sip.AUTOMATIC_DIALOG_SUPPORT", "on").equalsIgnoreCase("on");
                    this.isAutomaticDialogErrorHandlingEnabled = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.AUTOMATIC_DIALOG_ERROR_HANDLING", "true").equals(Boolean.TRUE.toString());
                    if (this.isAutomaticDialogSupportEnabled) {
                        this.isAutomaticDialogErrorHandlingEnabled = true;
                    }
                    if (mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.MAX_LISTENER_RESPONSE_TIME") != null) {
                        this.maxListenerResponseTime = Integer.parseInt(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.MAX_LISTENER_RESPONSE_TIME"));
                        if (this.maxListenerResponseTime <= 0) {
                            throw new PeerUnavailableException("Bad configuration parameter gov.nist.javax.sip.MAX_LISTENER_RESPONSE_TIME : should be positive");
                        }
                    }
                    this.maxListenerResponseTime = -1;
                    setDeliverTerminatedEventForAck(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_ACK", "false").equalsIgnoreCase("true"));
                    super.setDeliverUnsolicitedNotify(Boolean.parseBoolean(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "false")));
                    String forkedSubscriptions = mergedSystemProperties.getProperty("org.jitsi.javax.sip.FORKABLE_EVENTS");
                    if (forkedSubscriptions != null) {
                        stringTokenizer = new StringTokenizer(forkedSubscriptions);
                        while (stringTokenizer.hasMoreTokens()) {
                            this.forkedEvents.add(stringTokenizer.nextToken());
                        }
                    }
                    String tlsPolicyPath = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.TLS_SECURITY_POLICY");
                    if (tlsPolicyPath == null) {
                        tlsPolicyPath = "org.jitsi.gov.nist.javax.sip.stack.DefaultTlsSecurityPolicy";
                        logger.logWarning("using default tls security policy");
                    }
                    try {
                        String path;
                        this.tlsSecurityPolicy = (TlsSecurityPolicy) Class.forName(tlsPolicyPath).getConstructor(new Class[0]).newInstance(new Object[0]);
                        String clientAuthType = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE");
                        if (clientAuthType != null) {
                            this.clientAuth = ClientAuthType.valueOf(clientAuthType);
                            logger.logInfo("using " + clientAuthType + " tls auth policy");
                        }
                        String NETWORK_LAYER_KEY = "org.jitsi.gov.nist.javax.sip.NETWORK_LAYER";
                        if (mergedSystemProperties.containsKey("org.jitsi.gov.nist.javax.sip.NETWORK_LAYER")) {
                            path = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.NETWORK_LAYER");
                            try {
                                this.networkLayer = (NetworkLayer) Class.forName(path).getConstructor(new Class[0]).newInstance(new Object[0]);
                            } catch (Exception e2) {
                                throw new PeerUnavailableException("can't find or instantiate NetworkLayer implementation: " + path);
                            }
                        }
                        String ADDRESS_RESOLVER_KEY = "org.jitsi.gov.nist.javax.sip.ADDRESS_RESOLVER";
                        if (mergedSystemProperties.containsKey("org.jitsi.gov.nist.javax.sip.ADDRESS_RESOLVER")) {
                            path = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.ADDRESS_RESOLVER");
                            try {
                                this.addressResolver = (AddressResolver) Class.forName(path).getConstructor(new Class[0]).newInstance(new Object[0]);
                            } catch (Exception e3) {
                                throw new PeerUnavailableException("can't find or instantiate AddressResolver implementation: " + path);
                            }
                        }
                        String maxConnections = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.MAX_CONNECTIONS");
                        if (maxConnections != null) {
                            try {
                                this.maxConnections = new Integer(maxConnections).intValue();
                            } catch (NumberFormatException ex) {
                                if (logger.isLoggingEnabled()) {
                                    logger.logError("max connections - bad value " + ex.getMessage());
                                }
                            }
                        }
                        String threadPoolSize = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.THREAD_POOL_SIZE");
                        if (threadPoolSize != null) {
                            try {
                                this.threadPoolSize = new Integer(threadPoolSize).intValue();
                            } catch (NumberFormatException ex2) {
                                if (logger.isLoggingEnabled()) {
                                    logger.logError("thread pool size - bad value " + ex2.getMessage());
                                }
                            }
                        }
                        int congetstionControlTimeout = Integer.parseInt(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.CONGESTION_CONTROL_TIMEOUT", "8000"));
                        this.stackCongenstionControlTimeout = congetstionControlTimeout;
                        String tcpTreadPoolSize = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.TCP_POST_PARSING_THREAD_POOL_SIZE");
                        if (tcpTreadPoolSize != null) {
                            try {
                                int threads = new Integer(tcpTreadPoolSize).intValue();
                                super.setTcpPostParsingThreadPoolSize(threads);
                                PipelinedMsgParser.setPostParseExcutorSize(threads, congetstionControlTimeout);
                            } catch (NumberFormatException ex22) {
                                if (logger.isLoggingEnabled()) {
                                    logger.logError("TCP post-parse thread pool size - bad value " + tcpTreadPoolSize + " : " + ex22.getMessage());
                                }
                            }
                        }
                        String serverTransactionTableSize = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.MAX_SERVER_TRANSACTIONS");
                        if (serverTransactionTableSize != null) {
                            try {
                                this.serverTransactionTableHighwaterMark = new Integer(serverTransactionTableSize).intValue();
                                this.serverTransactionTableLowaterMark = (this.serverTransactionTableHighwaterMark * 80) / 100;
                            } catch (NumberFormatException ex222) {
                                if (logger.isLoggingEnabled()) {
                                    logger.logError("transaction table size - bad value " + ex222.getMessage());
                                }
                            }
                        } else {
                            this.unlimitedServerTransactionTableSize = true;
                        }
                        String clientTransactionTableSize = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.MAX_CLIENT_TRANSACTIONS");
                        if (clientTransactionTableSize != null) {
                            try {
                                this.clientTransactionTableHiwaterMark = new Integer(clientTransactionTableSize).intValue();
                                this.clientTransactionTableLowaterMark = (this.clientTransactionTableLowaterMark * 80) / 100;
                            } catch (NumberFormatException ex2222) {
                                if (logger.isLoggingEnabled()) {
                                    logger.logError("transaction table size - bad value " + ex2222.getMessage());
                                }
                            }
                        } else {
                            this.unlimitedClientTransactionTableSize = true;
                        }
                        this.cacheServerConnections = true;
                        String flag = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS");
                        if (flag != null && "false".equalsIgnoreCase(flag.trim())) {
                            this.cacheServerConnections = false;
                        }
                        this.cacheClientConnections = true;
                        String cacheflag = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS");
                        if (cacheflag != null && "false".equalsIgnoreCase(cacheflag.trim())) {
                            this.cacheClientConnections = false;
                        }
                        String readTimeout = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.READ_TIMEOUT");
                        if (readTimeout != null) {
                            try {
                                int rt = Integer.parseInt(readTimeout);
                                if (rt >= 100) {
                                    this.readTimeout = rt;
                                } else {
                                    System.err.println("Value too low " + readTimeout);
                                }
                            } catch (NumberFormatException e4) {
                                if (logger.isLoggingEnabled()) {
                                    logger.logError("Bad read timeout " + readTimeout);
                                }
                            }
                        }
                        if (mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.STUN_SERVER") != null) {
                            logger.logWarning("Ignoring obsolete property gov.nist.javax.sip.STUN_SERVER");
                        }
                        String maxMsgSize = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.MAX_MESSAGE_SIZE");
                        if (maxMsgSize != null) {
                            try {
                                this.maxMessageSize = new Integer(maxMsgSize).intValue();
                                if (this.maxMessageSize < 4096) {
                                    this.maxMessageSize = 4096;
                                }
                            } catch (NumberFormatException ex22222) {
                                if (logger.isLoggingEnabled()) {
                                    logger.logError("maxMessageSize - bad value " + ex22222.getMessage());
                                }
                            }
                        } else {
                            this.maxMessageSize = 0;
                        }
                        String rel = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.REENTRANT_LISTENER");
                        boolean z = rel != null && "true".equalsIgnoreCase(rel);
                        this.reEntrantListener = z;
                        String interval = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS");
                        if (interval != null) {
                            try {
                                getThreadAuditor().setPingIntervalInMillisecs(Long.valueOf(interval).longValue() / 2);
                            } catch (NumberFormatException ex222222) {
                                if (logger.isLoggingEnabled()) {
                                    logger.logError("THREAD_AUDIT_INTERVAL_IN_MILLISECS - bad value [" + interval + "] " + ex222222.getMessage());
                                }
                            }
                        }
                        setNon2XXAckPassedToListener(Boolean.valueOf(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "false")).booleanValue());
                        this.generateTimeStampHeader = Boolean.valueOf(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP", "false")).booleanValue();
                        String messageLogFactoryClasspath = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.LOG_FACTORY");
                        if (messageLogFactoryClasspath != null) {
                            try {
                                this.logRecordFactory = (LogRecordFactory) Class.forName(messageLogFactoryClasspath).getConstructor(new Class[0]).newInstance(new Object[0]);
                            } catch (Exception e5) {
                                if (logger.isLoggingEnabled()) {
                                    logger.logError("Bad configuration value for LOG_FACTORY -- using default logger");
                                }
                                this.logRecordFactory = new DefaultMessageLogFactory();
                            }
                        } else {
                            this.logRecordFactory = new DefaultMessageLogFactory();
                        }
                        StringMsgParser.setComputeContentLengthFromMessage(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.COMPUTE_CONTENT_LENGTH_FROM_MESSAGE_BODY", "false").equalsIgnoreCase("true"));
                        String tlsClientProtocols = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
                        if (tlsClientProtocols != null) {
                            stringTokenizer = new StringTokenizer(tlsClientProtocols, " ,");
                            String[] protocols = new String[stringTokenizer.countTokens()];
                            int i = 0;
                            while (stringTokenizer.hasMoreTokens()) {
                                int i2 = i + 1;
                                protocols[i] = stringTokenizer.nextToken();
                                i = i2;
                            }
                            this.enabledProtocols = protocols;
                        }
                        this.rfc2543Supported = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.RFC_2543_SUPPORT_ENABLED", "true").equalsIgnoreCase("true");
                        this.cancelClientTransactionChecked = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "true").equalsIgnoreCase("true");
                        this.logStackTraceOnMessageSend = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "false").equalsIgnoreCase("true");
                        if (logger.isLoggingEnabled(32)) {
                            logger.logDebug("created Sip stack. Properties = " + mergedSystemProperties);
                        }
                        InputStream in = getClass().getResourceAsStream("/TIMESTAMP");
                        if (in != null) {
                            try {
                                String buildTimeStamp = new BufferedReader(new InputStreamReader(in)).readLine();
                                if (in != null) {
                                    in.close();
                                }
                                logger.setBuildTimeStamp(buildTimeStamp);
                            } catch (IOException e6) {
                                logger.logError("Could not open build timestamp.");
                            }
                        }
                        super.setReceiveUdpBufferSize(new Integer(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString())).intValue());
                        super.setSendUdpBufferSize(new Integer(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", MAX_DATAGRAM_SIZE.toString())).intValue());
                        this.isBackToBackUserAgent = Boolean.parseBoolean(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.FALSE.toString()));
                        this.checkBranchId = Boolean.parseBoolean(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.REJECT_STRAY_RESPONSES", Boolean.FALSE.toString()));
                        this.isDialogTerminatedEventDeliveredForNullDialog = Boolean.parseBoolean(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_NULL_DIALOG", Boolean.FALSE.toString()));
                        this.maxForkTime = Integer.parseInt(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", SdpConstants.RESERVED));
                        this.earlyDialogTimeout = Integer.parseInt(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.EARLY_DIALOG_TIMEOUT_SECONDS", "180"));
                        this.minKeepAliveInterval = (long) Integer.parseInt(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.MIN_KEEPALIVE_TIME_SECONDS", "-1"));
                        this.deliverRetransmittedAckToListener = Boolean.parseBoolean(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.DELIVER_RETRANSMITTED_ACK_TO_LISTENER", "false"));
                        this.dialogTimeoutFactor = Integer.parseInt(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.DIALOG_TIMEOUT_FACTOR", "64"));
                        try {
                            this.messageParserFactory = (MessageParserFactory) Class.forName(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.MESSAGE_PARSER_FACTORY", StringMsgParserFactory.class.getName())).newInstance();
                        } catch (Exception e7) {
                            logger.logError("Bad configuration value for gov.nist.javax.sip.MESSAGE_PARSER_FACTORY", e7);
                        }
                        try {
                            this.messageProcessorFactory = (MessageProcessorFactory) Class.forName(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", OIOMessageProcessorFactory.class.getName())).newInstance();
                        } catch (Exception e72) {
                            logger.logError("Bad configuration value for gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", e72);
                        }
                        try {
                            setTimer((SipTimer) Class.forName(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.TIMER_CLASS_NAME", DefaultSipTimer.class.getName())).newInstance());
                            getTimer().start(this, mergedSystemProperties);
                            if (getThreadAuditor().isEnabled()) {
                                getTimer().schedule(new PingTimer(null), 0);
                            }
                        } catch (Exception e722) {
                            logger.logError("Bad configuration value for gov.nist.javax.sip.TIMER_CLASS_NAME", e722);
                        }
                        this.aggressiveCleanup = Boolean.parseBoolean(mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.AGGRESSIVE_CLEANUP", Boolean.FALSE.toString()));
                        String valveClassName = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.SIP_MESSAGE_VALVE", null);
                        if (!(valveClassName == null || valveClassName.equals(""))) {
                            try {
                                this.sipMessageValve = (SIPMessageValve) Class.forName(valveClassName).newInstance();
                                new Thread() {
                                    public void run() {
                                        try {
                                            Thread.sleep(100);
                                            SipStackImpl.this.sipMessageValve.init(this);
                                        } catch (Exception e) {
                                            SipStackImpl.logger.logError("Error intializing SIPMessageValve", e);
                                        }
                                    }
                                }.start();
                            } catch (Exception e7222) {
                                logger.logError("Bad configuration value for gov.nist.javax.sip.SIP_MESSAGE_VALVE", e7222);
                            }
                        }
                        String interceptorClassName = mergedSystemProperties.getProperty("org.jitsi.gov.nist.javax.sip.SIP_EVENT_INTERCEPTOR", null);
                        if (interceptorClassName != null && !interceptorClassName.equals("")) {
                            try {
                                this.sipEventInterceptor = (SIPEventInterceptor) Class.forName(interceptorClassName).newInstance();
                                new Thread() {
                                    public void run() {
                                        try {
                                            Thread.sleep(100);
                                            SipStackImpl.this.sipEventInterceptor.init(this);
                                        } catch (Exception e) {
                                            SipStackImpl.logger.logError("Error intializing SIPEventInterceptor", e);
                                        }
                                    }
                                }.start();
                            } catch (Exception e72222) {
                                logger.logError("Bad configuration value for gov.nist.javax.sip.SIP_EVENT_INTERCEPTOR", e72222);
                            }
                        }
                    } catch (InvocationTargetException ex1) {
                        throw new IllegalArgumentException("Cound not instantiate TLS security policy " + tlsPolicyPath + "- check that it is present on the classpath and that there is a no-args constructor defined", ex1);
                    } catch (Exception ex3) {
                        throw new IllegalArgumentException("Cound not instantiate TLS security policy " + tlsPolicyPath + "- check that it is present on the classpath and that there is a no-args constructor defined", ex3);
                    }
                } catch (InvocationTargetException ex12) {
                    logger.logError("could not instantiate router -- invocation target problem", (Exception) ex12.getCause());
                    throw new PeerUnavailableException("Cound not instantiate router - check constructor", ex12);
                } catch (Exception ex32) {
                    logger.logError("could not instantiate router", (Exception) ex32.getCause());
                    throw new PeerUnavailableException("Could not instantiate router", ex32);
                }
            } catch (InvocationTargetException ex122) {
                throw new IllegalArgumentException("Cound not instantiate server logger " + stackLoggerClassName + "- check that it is present on the classpath and that there is a no-args constructor defined", ex122);
            } catch (Exception ex322) {
                throw new IllegalArgumentException("Cound not instantiate server logger " + stackLoggerClassName + "- check that it is present on the classpath and that there is a no-args constructor defined", ex322);
            }
        } catch (InvocationTargetException ex1222) {
            throw new IllegalArgumentException("Cound not instantiate stack logger " + stackLoggerClassName + "- check that it is present on the classpath and that there is a no-args constructor defined", ex1222);
        } catch (Exception ex3222) {
            throw new IllegalArgumentException("Cound not instantiate stack logger " + stackLoggerClassName + "- check that it is present on the classpath and that there is a no-args constructor defined", ex3222);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x0109  */
    public synchronized org.jitsi.javax.sip.ListeningPoint createListeningPoint(java.lang.String r10, int r11, java.lang.String r12) throws org.jitsi.javax.sip.TransportNotSupportedException, org.jitsi.javax.sip.InvalidArgumentException {
        /*
        r9 = this;
        monitor-enter(r9);
        r6 = logger;	 Catch:{ all -> 0x0041 }
        r7 = 32;
        r6 = r6.isLoggingEnabled(r7);	 Catch:{ all -> 0x0041 }
        if (r6 == 0) goto L_0x0037;
    L_0x000b:
        r6 = logger;	 Catch:{ all -> 0x0041 }
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0041 }
        r7.<init>();	 Catch:{ all -> 0x0041 }
        r8 = "createListeningPoint : address = ";
        r7 = r7.append(r8);	 Catch:{ all -> 0x0041 }
        r7 = r7.append(r10);	 Catch:{ all -> 0x0041 }
        r8 = " port = ";
        r7 = r7.append(r8);	 Catch:{ all -> 0x0041 }
        r7 = r7.append(r11);	 Catch:{ all -> 0x0041 }
        r8 = " transport = ";
        r7 = r7.append(r8);	 Catch:{ all -> 0x0041 }
        r7 = r7.append(r12);	 Catch:{ all -> 0x0041 }
        r7 = r7.toString();	 Catch:{ all -> 0x0041 }
        r6.logDebug(r7);	 Catch:{ all -> 0x0041 }
    L_0x0037:
        if (r10 != 0) goto L_0x0044;
    L_0x0039:
        r6 = new java.lang.NullPointerException;	 Catch:{ all -> 0x0041 }
        r7 = "Address for listening point is null!";
        r6.<init>(r7);	 Catch:{ all -> 0x0041 }
        throw r6;	 Catch:{ all -> 0x0041 }
    L_0x0041:
        r6 = move-exception;
        monitor-exit(r9);
        throw r6;
    L_0x0044:
        if (r12 != 0) goto L_0x004e;
    L_0x0046:
        r6 = new java.lang.NullPointerException;	 Catch:{ all -> 0x0041 }
        r7 = "null transport";
        r6.<init>(r7);	 Catch:{ all -> 0x0041 }
        throw r6;	 Catch:{ all -> 0x0041 }
    L_0x004e:
        if (r11 > 0) goto L_0x0058;
    L_0x0050:
        r6 = new org.jitsi.javax.sip.InvalidArgumentException;	 Catch:{ all -> 0x0041 }
        r7 = "bad port";
        r6.m1636init(r7);	 Catch:{ all -> 0x0041 }
        throw r6;	 Catch:{ all -> 0x0041 }
    L_0x0058:
        r6 = "UDP";
        r6 = r12.equalsIgnoreCase(r6);	 Catch:{ all -> 0x0041 }
        if (r6 != 0) goto L_0x0091;
    L_0x0060:
        r6 = "TLS";
        r6 = r12.equalsIgnoreCase(r6);	 Catch:{ all -> 0x0041 }
        if (r6 != 0) goto L_0x0091;
    L_0x0068:
        r6 = "TCP";
        r6 = r12.equalsIgnoreCase(r6);	 Catch:{ all -> 0x0041 }
        if (r6 != 0) goto L_0x0091;
    L_0x0070:
        r6 = "SCTP";
        r6 = r12.equalsIgnoreCase(r6);	 Catch:{ all -> 0x0041 }
        if (r6 != 0) goto L_0x0091;
    L_0x0078:
        r6 = new org.jitsi.javax.sip.TransportNotSupportedException;	 Catch:{ all -> 0x0041 }
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0041 }
        r7.<init>();	 Catch:{ all -> 0x0041 }
        r8 = "bad transport ";
        r7 = r7.append(r8);	 Catch:{ all -> 0x0041 }
        r7 = r7.append(r12);	 Catch:{ all -> 0x0041 }
        r7 = r7.toString();	 Catch:{ all -> 0x0041 }
        r6.m1670init(r7);	 Catch:{ all -> 0x0041 }
        throw r6;	 Catch:{ all -> 0x0041 }
    L_0x0091:
        r6 = r9.isAlive();	 Catch:{ all -> 0x0041 }
        if (r6 != 0) goto L_0x009d;
    L_0x0097:
        r6 = 0;
        r9.toExit = r6;	 Catch:{ all -> 0x0041 }
        r9.reInitialize();	 Catch:{ all -> 0x0041 }
    L_0x009d:
        r2 = org.jitsi.gov.nist.javax.sip.ListeningPointImpl.makeKey(r10, r11, r12);	 Catch:{ all -> 0x0041 }
        r6 = r9.listeningPoints;	 Catch:{ all -> 0x0041 }
        r3 = r6.get(r2);	 Catch:{ all -> 0x0041 }
        r3 = (org.jitsi.gov.nist.javax.sip.ListeningPointImpl) r3;	 Catch:{ all -> 0x0041 }
        if (r3 == 0) goto L_0x00ae;
    L_0x00ab:
        r4 = r3;
    L_0x00ac:
        monitor-exit(r9);
        return r4;
    L_0x00ae:
        r1 = java.net.InetAddress.getByName(r10);	 Catch:{ IOException -> 0x0100 }
        r5 = r9.createMessageProcessor(r1, r11, r12);	 Catch:{ IOException -> 0x0100 }
        r6 = logger;	 Catch:{ IOException -> 0x0100 }
        r7 = 32;
        r6 = r6.isLoggingEnabled(r7);	 Catch:{ IOException -> 0x0100 }
        if (r6 == 0) goto L_0x00ec;
    L_0x00c0:
        r6 = logger;	 Catch:{ IOException -> 0x0100 }
        r7 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0100 }
        r7.<init>();	 Catch:{ IOException -> 0x0100 }
        r8 = "Created Message Processor: ";
        r7 = r7.append(r8);	 Catch:{ IOException -> 0x0100 }
        r7 = r7.append(r10);	 Catch:{ IOException -> 0x0100 }
        r8 = " port = ";
        r7 = r7.append(r8);	 Catch:{ IOException -> 0x0100 }
        r7 = r7.append(r11);	 Catch:{ IOException -> 0x0100 }
        r8 = " transport = ";
        r7 = r7.append(r8);	 Catch:{ IOException -> 0x0100 }
        r7 = r7.append(r12);	 Catch:{ IOException -> 0x0100 }
        r7 = r7.toString();	 Catch:{ IOException -> 0x0100 }
        r6.logDebug(r7);	 Catch:{ IOException -> 0x0100 }
    L_0x00ec:
        r4 = new org.jitsi.gov.nist.javax.sip.ListeningPointImpl;	 Catch:{ IOException -> 0x0100 }
        r4.m1059init(r9, r11, r12);	 Catch:{ IOException -> 0x0100 }
        r4.messageProcessor = r5;	 Catch:{ IOException -> 0x013f }
        r5.setListeningPoint(r4);	 Catch:{ IOException -> 0x013f }
        r6 = r9.listeningPoints;	 Catch:{ IOException -> 0x013f }
        r6.put(r2, r4);	 Catch:{ IOException -> 0x013f }
        r5.start();	 Catch:{ IOException -> 0x013f }
        r3 = r4;
        goto L_0x00ac;
    L_0x0100:
        r0 = move-exception;
    L_0x0101:
        r6 = logger;	 Catch:{ all -> 0x0041 }
        r6 = r6.isLoggingEnabled();	 Catch:{ all -> 0x0041 }
        if (r6 == 0) goto L_0x0135;
    L_0x0109:
        r6 = logger;	 Catch:{ all -> 0x0041 }
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0041 }
        r7.<init>();	 Catch:{ all -> 0x0041 }
        r8 = "Invalid argument address = ";
        r7 = r7.append(r8);	 Catch:{ all -> 0x0041 }
        r7 = r7.append(r10);	 Catch:{ all -> 0x0041 }
        r8 = " port = ";
        r7 = r7.append(r8);	 Catch:{ all -> 0x0041 }
        r7 = r7.append(r11);	 Catch:{ all -> 0x0041 }
        r8 = " transport = ";
        r7 = r7.append(r8);	 Catch:{ all -> 0x0041 }
        r7 = r7.append(r12);	 Catch:{ all -> 0x0041 }
        r7 = r7.toString();	 Catch:{ all -> 0x0041 }
        r6.logError(r7);	 Catch:{ all -> 0x0041 }
    L_0x0135:
        r6 = new org.jitsi.javax.sip.InvalidArgumentException;	 Catch:{ all -> 0x0041 }
        r7 = r0.getMessage();	 Catch:{ all -> 0x0041 }
        r6.m1637init(r7, r0);	 Catch:{ all -> 0x0041 }
        throw r6;	 Catch:{ all -> 0x0041 }
    L_0x013f:
        r0 = move-exception;
        r3 = r4;
        goto L_0x0101;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.SipStackImpl.createListeningPoint(java.lang.String, int, java.lang.String):org.jitsi.javax.sip.ListeningPoint");
    }

    public SipProvider createSipProvider(ListeningPoint listeningPoint) throws ObjectInUseException {
        if (listeningPoint == null) {
            throw new NullPointerException("null listeningPoint");
        }
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("createSipProvider: " + listeningPoint);
        }
        ListeningPointImpl listeningPointImpl = (ListeningPointImpl) listeningPoint;
        if (listeningPointImpl.sipProvider != null) {
            throw new ObjectInUseException("Provider already attached!");
        }
        SipProviderImpl provider = new SipProviderImpl(this);
        provider.setListeningPoint(listeningPointImpl);
        listeningPointImpl.sipProvider = provider;
        this.sipProviders.add(provider);
        return provider;
    }

    public void deleteListeningPoint(ListeningPoint listeningPoint) throws ObjectInUseException {
        if (listeningPoint == null) {
            throw new NullPointerException("null listeningPoint arg");
        }
        ListeningPointImpl lip = (ListeningPointImpl) listeningPoint;
        super.removeMessageProcessor(lip.messageProcessor);
        this.listeningPoints.remove(lip.getKey());
    }

    public void deleteSipProvider(SipProvider sipProvider) throws ObjectInUseException {
        if (sipProvider == null) {
            throw new NullPointerException("null provider arg");
        }
        SipProviderImpl sipProviderImpl = (SipProviderImpl) sipProvider;
        if (sipProviderImpl.getSipListener() != null) {
            throw new ObjectInUseException("SipProvider still has an associated SipListener!");
        }
        sipProviderImpl.removeListeningPoints();
        sipProviderImpl.stop();
        this.sipProviders.remove(sipProvider);
        if (this.sipProviders.isEmpty()) {
            stopStack();
        }
    }

    public String getIPAddress() {
        return super.getHostAddress();
    }

    public Iterator getListeningPoints() {
        return this.listeningPoints.values().iterator();
    }

    public boolean isRetransmissionFilterActive() {
        return true;
    }

    public Iterator<SipProviderImpl> getSipProviders() {
        return this.sipProviders.iterator();
    }

    public String getStackName() {
        return this.stackName;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        stopStack();
    }

    public ListeningPoint createListeningPoint(int port, String transport) throws TransportNotSupportedException, InvalidArgumentException {
        if (this.stackAddress != null) {
            return createListeningPoint(this.stackAddress, port, transport);
        }
        throw new NullPointerException("Stack does not have a default IP Address!");
    }

    public void stop() {
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("stopStack -- stoppping the stack");
            logger.logStackTrace();
        }
        stopStack();
        if (this.sipMessageValve != null) {
            this.sipMessageValve.destroy();
        }
        if (this.sipEventInterceptor != null) {
            this.sipEventInterceptor.destroy();
        }
        this.sipProviders = Collections.synchronizedList(new LinkedList());
        this.listeningPoints = new Hashtable();
        if (this.eventScanner != null) {
            this.eventScanner.forceStop();
        }
        this.eventScanner = null;
    }

    public void start() throws ProviderDoesNotExistException, SipException {
        if (this.eventScanner == null) {
            this.eventScanner = new EventScanner(this);
        }
    }

    public SipListener getSipListener() {
        return this.sipListener;
    }

    public TlsSecurityPolicy getTlsSecurityPolicy() {
        return this.tlsSecurityPolicy;
    }

    public LogRecordFactory getLogRecordFactory() {
        return this.logRecordFactory;
    }

    @Deprecated
    public void addLogAppender(Appender appender) {
        if (logger instanceof LogWriter) {
            ((LogWriter) logger).addAppender(appender);
        }
    }

    @Deprecated
    public Logger getLogger() {
        if (logger instanceof LogWriter) {
            return ((LogWriter) logger).getLogger();
        }
        return null;
    }

    public EventScanner getEventScanner() {
        return this.eventScanner;
    }

    public AuthenticationHelper getAuthenticationHelper(AccountManager accountManager, HeaderFactory headerFactory) {
        return new AuthenticationHelperImpl((SIPTransactionStack) this, accountManager, headerFactory);
    }

    public AuthenticationHelper getSecureAuthenticationHelper(SecureAccountManager accountManager, HeaderFactory headerFactory) {
        return new AuthenticationHelperImpl((SIPTransactionStack) this, accountManager, headerFactory);
    }

    public void setEnabledCipherSuites(String[] newCipherSuites) {
        this.cipherSuites = newCipherSuites;
    }

    public String[] getEnabledCipherSuites() {
        return this.cipherSuites;
    }

    public void setEnabledProtocols(String[] newProtocols) {
        this.enabledProtocols = newProtocols;
    }

    public String[] getEnabledProtocols() {
        return this.enabledProtocols;
    }

    public void setIsBackToBackUserAgent(boolean flag) {
        this.isBackToBackUserAgent = flag;
    }

    public boolean isBackToBackUserAgent() {
        return this.isBackToBackUserAgent;
    }

    public boolean isAutomaticDialogErrorHandlingEnabled() {
        return this.isAutomaticDialogErrorHandlingEnabled;
    }

    public void setTlsSecurityPolicy(TlsSecurityPolicy tlsSecurityPolicy) {
        this.tlsSecurityPolicy = tlsSecurityPolicy;
    }

    public boolean acquireSem() {
        try {
            return this.stackSemaphore.tryAcquire(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    public void releaseSem() {
        this.stackSemaphore.release();
    }

    public Properties getConfigurationProperties() {
        return this.configurationProperties;
    }

    public boolean isReEntrantListener() {
        return this.reEntrantListener;
    }
}
