package net.java.sip.communicator.impl.protocol.sip;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import net.java.sip.communicator.impl.protocol.sip.net.AndroidNetworkLayer;
import net.java.sip.communicator.impl.protocol.sip.net.SslNetworkLayer;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.util.OSUtils;
import org.jitsi.util.StringUtils;

public class SipStackProperties extends Properties {
    private static final String JSPNAME_STACK_NAME = "org.jitsi.javax.sip.STACK_NAME";
    private static final String NSPNAME_CACHE_CLIENT_CONNECTIONS = "org.jitsi.gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS";
    private static final String NSPNAME_DEBUG_LOG = "org.jitsi.gov.nist.javax.sip.DEBUG_LOG";
    private static final String NSPNAME_DEBUG_LOG_OVERWRITE = "org.jitsi.gov.nist.javax.sip.DEBUG_LOG_OVERWRITE";
    private static final String NSPNAME_DELIVER_UNSOLICITED_NOTIFY = "org.jitsi.gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY";
    private static final String NSPNAME_LOG_MESSAGE_CONTENT = "org.jitsi.gov.nist.javax.sip.LOG_MESSAGE_CONTENT";
    private static final String NSPNAME_REENTRANT_LISTENER = "org.jitsi.gov.nist.javax.sip.REENTRANT_LISTENER";
    private static final String NSPNAME_ROUTER_PATH = "org.jitsi.javax.sip.ROUTER_PATH";
    private static final String NSPNAME_SERVER_LOGGER = "org.jitsi.gov.nist.javax.sip.SERVER_LOGGER";
    private static final String NSPNAME_SERVER_LOG_OVERWRITE = "org.jitsi.gov.nist.javax.sip.SERVER_LOG_OVERWRITE";
    private static final String NSPNAME_STACK_LOGGER = "org.jitsi.gov.nist.javax.sip.STACK_LOGGER";
    private static final String NSPNAME_TLS_CLIENT_PROTOCOLS = "org.jitsi.gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS";
    private static final String NSPNAME_TRACE_LEVEL = "org.jitsi.gov.nist.javax.sip.TRACE_LEVEL";
    private static final String NSPNAME_USE_ROUTER_FOR_ALL_URIS = "org.jitsi.javax.sip.USE_ROUTER_FOR_ALL_URIS";
    private static final String NSPVALUE_CACHE_CLIENT_CONNECTIONS = "true";
    private static String NSPVALUE_DEBUG_LOG = "log/sc-jainsipdebug.log";
    private static final String NSPVALUE_DEBUG_LOG_OVERWRITE = "true";
    private static final String NSPVALUE_DEFAULT_TRACE_LEVEL = "ERROR";
    private static final String NSPVALUE_DELIVER_UNSOLICITED_NOTIFY = "true";
    private static final String NSPVALUE_LOG_MESSAGE_CONTENT = "true";
    private static final String NSPVALUE_REENTRANT_LISTENER = "true";
    private static final String NSPVALUE_ROUTER_PATH = "net.java.sip.communicator.impl.protocol.sip.ProxyRouter";
    private static final String NSPVALUE_SERVER_LOGGER = "net.java.sip.communicator.impl.protocol.sip.SipLogger";
    private static final String NSPVALUE_SERVER_LOG_OVERWRITE = "true";
    private static final String NSPVALUE_STACK_LOGGER = "net.java.sip.communicator.impl.protocol.sip.SipLogger";
    private static final String NSPVALUE_USE_ROUTER_FOR_ALL_URIS = "true";
    private static final Logger logger = Logger.getLogger(SipStackProperties.class);
    private static final long serialVersionUID = 0;

    public SipStackProperties() {
        String jainSipTraceLevel;
        String logDir = SipActivator.getConfigurationService().getScHomeDirLocation() + System.getProperty("file.separator") + SipActivator.getConfigurationService().getScHomeDirName() + System.getProperty("file.separator");
        if (!NSPVALUE_DEBUG_LOG.startsWith(logDir)) {
            NSPVALUE_DEBUG_LOG = logDir + NSPVALUE_DEBUG_LOG;
        }
        setProperty(JSPNAME_STACK_NAME, "Sip Communicator");
        setProperty(NSPNAME_DEBUG_LOG, NSPVALUE_DEBUG_LOG);
        setProperty(NSPNAME_LOG_MESSAGE_CONTENT, "true");
        setProperty(NSPNAME_DEBUG_LOG_OVERWRITE, "true");
        setProperty(NSPNAME_SERVER_LOG_OVERWRITE, "true");
        setProperty(NSPNAME_CACHE_CLIENT_CONNECTIONS, "true");
        setProperty(NSPNAME_REENTRANT_LISTENER, "true");
        setProperty(NSPNAME_DELIVER_UNSOLICITED_NOTIFY, "true");
        setProperty(NSPNAME_USE_ROUTER_FOR_ALL_URIS, "true");
        setProperty(NSPNAME_ROUTER_PATH, NSPVALUE_ROUTER_PATH);
        System.setProperty("org.jitsi.gov.nist.core.STRIP_ADDR_SCOPES", "true");
        String logLevel = LogManager.getLogManager().getProperty("gov.nist.level");
        if (logLevel == null) {
            jainSipTraceLevel = NSPVALUE_DEFAULT_TRACE_LEVEL;
        } else if (logLevel.equals(Level.FINEST.getName())) {
            jainSipTraceLevel = "TRACE";
        } else if (logLevel.equals(Level.FINER.getName())) {
            jainSipTraceLevel = "DEBUG";
        } else if (logLevel.equals(Level.FINE.getName())) {
            jainSipTraceLevel = Request.INFO;
        } else if (logLevel.equals(Level.WARNING.getName()) || logLevel.equals(Level.SEVERE.getName())) {
            jainSipTraceLevel = NSPVALUE_DEFAULT_TRACE_LEVEL;
        } else if (logLevel.equals(Level.OFF.getName())) {
            jainSipTraceLevel = "OFF";
        } else {
            jainSipTraceLevel = logLevel;
        }
        setProperty(NSPNAME_TRACE_LEVEL, jainSipTraceLevel);
        setProperty(NSPNAME_STACK_LOGGER, "net.java.sip.communicator.impl.protocol.sip.SipLogger");
        setProperty(NSPNAME_SERVER_LOGGER, "net.java.sip.communicator.impl.protocol.sip.SipLogger");
        if (OSUtils.IS_ANDROID) {
            setProperty("org.jitsi.gov.nist.javax.sip.NETWORK_LAYER", AndroidNetworkLayer.class.getName());
        } else {
            setProperty("org.jitsi.gov.nist.javax.sip.NETWORK_LAYER", SslNetworkLayer.class.getName());
        }
        try {
            String enabledSslProtocols = SipActivator.getConfigurationService().getString(NSPNAME_TLS_CLIENT_PROTOCOLS);
            if (StringUtils.isNullOrEmpty(enabledSslProtocols, true)) {
                String[] enabledDefaultProtocols = ((SSLSocket) SSLSocketFactory.getDefault().createSocket()).getEnabledProtocols();
                enabledSslProtocols = "";
                for (int i = 0; i < enabledDefaultProtocols.length; i++) {
                    enabledSslProtocols = enabledSslProtocols + enabledDefaultProtocols[i];
                    if (i < enabledDefaultProtocols.length - 1) {
                        enabledSslProtocols = enabledSslProtocols + Separators.COMMA;
                    }
                }
            }
            setProperty(NSPNAME_TLS_CLIENT_PROTOCOLS, enabledSslProtocols);
        } catch (IOException ex) {
            logger.error("Unable to obtain default SSL protocols from Java, using JSIP defaults.", ex);
        }
    }
}
