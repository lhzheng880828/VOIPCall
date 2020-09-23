package net.java.sip.communicator.impl.protocol.sip;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;
import net.java.sip.communicator.impl.protocol.sip.net.ProxyConnection;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.javax.sip.ListeningPointExt;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.ListeningPoint;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.TransactionUnavailableException;
import org.jitsi.javax.sip.address.SipURI;
import org.jitsi.javax.sip.header.CSeqHeader;
import org.jitsi.javax.sip.header.CallIdHeader;
import org.jitsi.javax.sip.header.FromHeader;
import org.jitsi.javax.sip.header.MaxForwardsHeader;
import org.jitsi.javax.sip.header.ToHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;
import org.jitsi.util.OSUtils;

public class ClientCapabilities extends MethodProcessorAdapter {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(ClientCapabilities.class);
    /* access modifiers changed from: private */
    public Timer keepAliveTimer = null;
    private long nextCSeqValue = 1;
    /* access modifiers changed from: private|final */
    public final ProtocolProviderServiceSipImpl provider;
    private final RegistrationListener registrationListener;

    private class CRLfKeepAliveTask extends TimerTask {
        private CRLfKeepAliveTask() {
        }

        public void run() {
            ProxyConnection connection = ClientCapabilities.this.provider.getConnection();
            if (connection == null) {
                ClientCapabilities.logger.error("No connection found to send CRLF keep alive with " + ClientCapabilities.this.provider);
                return;
            }
            ListeningPoint lp = ClientCapabilities.this.provider.getListeningPoint(connection.getTransport());
            if (lp instanceof ListeningPointExt) {
                InetSocketAddress address = connection.getAddress();
                try {
                    ((ListeningPointExt) lp).sendHeartbeat(address.getAddress().getHostAddress(), address.getPort());
                    return;
                } catch (IOException e) {
                    ClientCapabilities.logger.error("Error while sending a heartbeat", e);
                    return;
                }
            }
            ClientCapabilities.logger.error("ListeningPoint is not ListeningPointExt(or is null)");
        }
    }

    private class OptionsKeepAliveTask extends TimerTask {
        private OptionsKeepAliveTask() {
        }

        public void run() {
            try {
                ClientCapabilities.logger.logEntry();
                try {
                    FromHeader fromHeader = ClientCapabilities.this.provider.getHeaderFactory().createFromHeader(ClientCapabilities.this.provider.getRegistrarConnection().getAddressOfRecord(), SipMessageFactory.generateLocalTag());
                    CallIdHeader callIdHeader = ClientCapabilities.this.provider.getDefaultJainSipProvider().getNewCallId();
                    try {
                        CSeqHeader cSeqHeader = ClientCapabilities.this.provider.getHeaderFactory().createCSeqHeader(ClientCapabilities.this.getNextCSeqValue(), "OPTIONS");
                        try {
                            ToHeader toHeader = ClientCapabilities.this.provider.getHeaderFactory().createToHeader(fromHeader.getAddress(), null);
                            MaxForwardsHeader maxForwardsHeader = ClientCapabilities.this.provider.getMaxForwardsHeader();
                            try {
                                SipURI requestURI = ClientCapabilities.this.provider.getAddressFactory().createSipURI(null, ((SipURI) toHeader.getAddress().getURI()).getHost());
                                Request request = ClientCapabilities.this.provider.getMessageFactory().createRequest(requestURI, "OPTIONS", callIdHeader, cSeqHeader, fromHeader, toHeader, ClientCapabilities.this.provider.getLocalViaHeaders(requestURI), maxForwardsHeader);
                                if (ClientCapabilities.logger.isDebugEnabled()) {
                                    ClientCapabilities.logger.debug("Created OPTIONS request " + request);
                                }
                                for (String method : ClientCapabilities.this.provider.getSupportedMethods()) {
                                    if (!method.equals("REGISTER")) {
                                        request.addHeader(ClientCapabilities.this.provider.getHeaderFactory().createAllowHeader(method));
                                    }
                                }
                                synchronized (ClientCapabilities.this.provider.getKnownEventsList()) {
                                    for (String event : ClientCapabilities.this.provider.getKnownEventsList()) {
                                        Request request2 = request;
                                        request2.addHeader(ClientCapabilities.this.provider.getHeaderFactory().createAllowEventsHeader(event));
                                    }
                                }
                                try {
                                    try {
                                        ClientCapabilities.this.provider.getDefaultJainSipProvider().getNewClientTransaction(request).sendRequest();
                                        if (ClientCapabilities.logger.isDebugEnabled()) {
                                            ClientCapabilities.logger.debug("sent request= " + request);
                                        }
                                    } catch (SipException ex) {
                                        ClientCapabilities.logger.error("Could not send out the options request!", ex);
                                        if (ex.getCause() instanceof IOException) {
                                            ClientCapabilities.this.disconnect();
                                        }
                                    }
                                } catch (TransactionUnavailableException ex2) {
                                    ClientCapabilities.logger.error("Could not create a register transaction!\nCheck that the Registrar address is correct!", ex2);
                                }
                            } catch (ParseException ex3) {
                                ClientCapabilities.logger.error("Could not create an OPTIONS request!", ex3);
                            }
                        } catch (ParseException ex32) {
                            ClientCapabilities.logger.error("Could not create a To header for address:" + fromHeader.getAddress(), ex32);
                        }
                    } catch (ParseException ex322) {
                        ClientCapabilities.logger.error("Corrupt Sip Stack", ex322);
                    } catch (InvalidArgumentException ex4) {
                        ClientCapabilities.logger.error("The application is corrupt", ex4);
                    }
                } catch (ParseException ex3222) {
                    ClientCapabilities.logger.error("Failed to generate a from header for our register request.", ex3222);
                }
            } catch (Exception ex5) {
                ClientCapabilities.logger.error("Cannot send OPTIONS keep alive", ex5);
            }
        }
    }

    private class RegistrationListener implements RegistrationStateChangeListener {
        private RegistrationListener() {
        }

        public void registrationStateChanged(RegistrationStateChangeEvent evt) {
            if (evt.getNewState() == RegistrationState.UNREGISTERING || evt.getNewState() == RegistrationState.AUTHENTICATION_FAILED || evt.getNewState() == RegistrationState.CONNECTION_FAILED) {
                if (ClientCapabilities.this.keepAliveTimer != null) {
                    ClientCapabilities.this.keepAliveTimer.cancel();
                    ClientCapabilities.this.keepAliveTimer = null;
                }
            } else if (evt.getNewState().equals(RegistrationState.REGISTERED)) {
                String keepAliveMethod = ClientCapabilities.this.provider.getAccountID().getAccountPropertyString("KEEP_ALIVE_METHOD");
                if (ClientCapabilities.logger.isTraceEnabled()) {
                    ClientCapabilities.logger.trace("Keep alive method " + keepAliveMethod);
                }
                if (keepAliveMethod == null || keepAliveMethod.equalsIgnoreCase("options") || keepAliveMethod.equalsIgnoreCase("crlf")) {
                    int keepAliveInterval = ClientCapabilities.this.provider.getAccountID().getAccountPropertyInt("KEEP_ALIVE_INTERVAL", -1);
                    if (ClientCapabilities.logger.isTraceEnabled()) {
                        ClientCapabilities.logger.trace("Keep alive interval is " + keepAliveInterval);
                    }
                    if (keepAliveInterval > 0 && !ClientCapabilities.this.provider.getRegistrarConnection().isRegistrarless()) {
                        TimerTask keepAliveTask;
                        if (ClientCapabilities.this.keepAliveTimer == null) {
                            ClientCapabilities.this.keepAliveTimer = new Timer();
                        }
                        if ((OSUtils.IS_ANDROID && keepAliveMethod == null) || "crlf".equalsIgnoreCase(keepAliveMethod)) {
                            keepAliveTask = new CRLfKeepAliveTask();
                        } else {
                            keepAliveTask = new OptionsKeepAliveTask();
                        }
                        if (ClientCapabilities.logger.isDebugEnabled()) {
                            ClientCapabilities.logger.debug("Scheduling keep alives: " + keepAliveTask);
                        }
                        ClientCapabilities.this.keepAliveTimer.schedule(keepAliveTask, 0, (long) (keepAliveInterval * 1000));
                    }
                }
            }
        }
    }

    public ClientCapabilities(ProtocolProviderServiceSipImpl protocolProvider) {
        this.provider = protocolProvider;
        this.provider.registerMethodProcessor("OPTIONS", this);
        this.registrationListener = new RegistrationListener();
        this.provider.addRegistrationStateChangeListener(this.registrationListener);
    }

    public boolean processRequest(RequestEvent requestEvent) {
        try {
            Response optionsOK = this.provider.getMessageFactory().createResponse(Response.OK, requestEvent.getRequest());
            for (String method : this.provider.getSupportedMethods()) {
                if (!method.equals("REGISTER")) {
                    optionsOK.addHeader(this.provider.getHeaderFactory().createAllowHeader(method));
                }
            }
            Iterable<String> knownEventsList = this.provider.getKnownEventsList();
            synchronized (knownEventsList) {
                for (String event : knownEventsList) {
                    optionsOK.addHeader(this.provider.getHeaderFactory().createAllowEventsHeader(event));
                }
            }
            try {
                SipStackSharing.getOrCreateServerTransaction(requestEvent).sendResponse(optionsOK);
                return true;
            } catch (TransactionUnavailableException ex) {
                if (logger.isInfoEnabled()) {
                    logger.info("Failed to respond to an incoming transactionless OPTIONS request");
                }
                if (!logger.isTraceEnabled()) {
                    return false;
                }
                logger.trace("Exception was:", ex);
                return false;
            } catch (InvalidArgumentException ex2) {
                logger.warn("Failed to send an incoming OPTIONS request", ex2);
                return false;
            } catch (SipException ex3) {
                logger.warn("Failed to send an incoming OPTIONS request", ex3);
                return false;
            }
        } catch (ParseException ex4) {
            logger.warn("Failed to create an incoming OPTIONS request", ex4);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public long getNextCSeqValue() {
        long j = this.nextCSeqValue;
        this.nextCSeqValue = 1 + j;
        return j;
    }

    /* access modifiers changed from: private */
    public void disconnect() {
        if (!this.provider.getRegistrationState().equals(RegistrationState.UNREGISTERED)) {
            this.provider.getRegistrarConnection().setRegistrationState(RegistrationState.CONNECTION_FAILED, -1, "A timeout occurred while trying to connect to the server.");
        }
    }

    /* access modifiers changed from: 0000 */
    public void shutdown() {
        this.provider.removeRegistrationStateChangeListener(this.registrationListener);
    }
}
