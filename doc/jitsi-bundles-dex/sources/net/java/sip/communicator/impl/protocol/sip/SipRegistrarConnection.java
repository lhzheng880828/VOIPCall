package net.java.sip.communicator.impl.protocol.sip;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import javax.net.ssl.SSLHandshakeException;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetBasicInstantMessaging;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.message.SIPMessage;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.IOExceptionEvent;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.ListeningPoint;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.SipProvider;
import org.jitsi.javax.sip.TimeoutEvent;
import org.jitsi.javax.sip.TransactionState;
import org.jitsi.javax.sip.TransactionUnavailableException;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.address.SipURI;
import org.jitsi.javax.sip.header.AllowHeader;
import org.jitsi.javax.sip.header.CSeqHeader;
import org.jitsi.javax.sip.header.CallIdHeader;
import org.jitsi.javax.sip.header.ContactHeader;
import org.jitsi.javax.sip.header.ExpiresHeader;
import org.jitsi.javax.sip.header.FromHeader;
import org.jitsi.javax.sip.header.MinExpiresHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public class SipRegistrarConnection extends MethodProcessorAdapter {
    private static final int DEFAULT_REGISTRATION_EXPIRATION = 600;
    private static final String FORCE_MESSAGING_PROP = "FORCE_MESSAGING";
    private static final String KEEP_ALIVE_INTERVAL = "KEEP_ALIVE_INTERVAL";
    private static final int KEEP_ALIVE_INTERVAL_DEFAULT_VALUE = 25;
    private static final String KEEP_ALIVE_METHOD = "KEEP_ALIVE_METHOD";
    private static final String REGISTRATION_EXPIRATION = "net.java.sip.communicator.impl.protocol.sip.REGISTRATION_EXPIRATION";
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(SipRegistrarConnection.class);
    private CallIdHeader callIdHeader = null;
    private RegistrationState currentRegistrationState = RegistrationState.UNREGISTERED;
    private InetAddress lastRegisterAddressReceived = null;
    private int lastRegisterPortReceived = -1;
    private long nextCSeqValue = 1;
    private Address ourSipAddressOfRecord = null;
    private Timer reRegisterTimer = new Timer();
    ClientTransaction regTrans = null;
    private Request registerRequest = null;
    private String registrarName = null;
    private int registrarPort = -1;
    private SipURI registrarURI = null;
    private String registrationTransport = null;
    private int registrationsExpiration = 600;
    private ProtocolProviderServiceSipImpl sipProvider = null;
    private boolean useRouteHeader = false;

    private class ReRegisterTask extends TimerTask {
        public void run() {
            try {
                if (SipRegistrarConnection.this.getRegistrationState() == RegistrationState.REGISTERED) {
                    SipRegistrarConnection.this.register();
                }
            } catch (OperationFailedException ex) {
                SipRegistrarConnection.logger.error("Failed to reRegister", ex);
                SipRegistrarConnection.this.setRegistrationState(RegistrationState.CONNECTION_FAILED, 6, "Failed to re register with the SIP server.");
            }
        }
    }

    public SipRegistrarConnection(String registrarName, int registrarPort, String registrationTransport, ProtocolProviderServiceSipImpl sipProviderCallback) {
        this.registrarPort = registrarPort;
        this.registrationTransport = registrationTransport;
        this.registrarName = registrarName;
        this.sipProvider = sipProviderCallback;
        this.registrationsExpiration = SipActivator.getConfigurationService().getInt(REGISTRATION_EXPIRATION, 600);
        getAddressOfRecord();
        sipProviderCallback.registerMethodProcessor("REGISTER", this);
    }

    protected SipRegistrarConnection() {
    }

    /* access modifiers changed from: 0000 */
    public void setTransport(String newRegistrationTransport) {
        if (!newRegistrationTransport.equals(this.registrationTransport)) {
            this.registrationTransport = newRegistrationTransport;
            if (!this.registrationTransport.equals(ListeningPoint.UDP)) {
                this.registrarURI = null;
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void register() throws OperationFailedException {
        if (getRegistrationState() != RegistrationState.REGISTERED) {
            setRegistrationState(RegistrationState.REGISTERING, -1, null);
        }
        try {
            if (this.callIdHeader == null) {
                this.callIdHeader = getJainSipProvider().getNewCallId();
            }
            Request request = this.sipProvider.getMessageFactory().createRegisterRequest(getAddressOfRecord(), this.registrationsExpiration, this.callIdHeader, getNextCSeqValue());
            try {
                this.regTrans = getJainSipProvider().getNewClientTransaction(request);
                try {
                    this.regTrans.sendRequest();
                    this.registerRequest = request;
                } catch (Exception ex) {
                    if ((!(ex.getCause() instanceof SocketException) && !(ex.getCause() instanceof IOException)) || !this.sipProvider.registerUsingNextAddress()) {
                        logger.error("Could not send out the register request!", ex);
                        setRegistrationState(RegistrationState.CONNECTION_FAILED, 6, ex.getMessage());
                        throw new OperationFailedException("Could not send out the register request!", 2, ex);
                    }
                }
            } catch (TransactionUnavailableException ex2) {
                logger.error("Could not create a register transaction!\nCheck that the Registrar address is correct!", ex2);
                setRegistrationState(RegistrationState.CONNECTION_FAILED, 6, ex2.getMessage());
                throw new OperationFailedException("Could not create a register transaction!\nCheck that the Registrar address is correct!", 2, ex2);
            }
        } catch (Exception exc) {
            if ((exc.getCause() instanceof SocketException) || (exc.getCause() instanceof IOException) || (exc.getCause() instanceof SSLHandshakeException)) {
                if ((exc.getCause().getCause() instanceof CertificateException) || exc.getCause().getMessage().startsWith("Received fatal alert")) {
                    setRegistrationState(RegistrationState.UNREGISTERED, 0, exc.getMessage());
                    return;
                } else if (this.sipProvider.registerUsingNextAddress()) {
                    return;
                }
            }
            logger.error("Failed to create a Register request.", exc);
            setRegistrationState(RegistrationState.CONNECTION_FAILED, 6, exc.getMessage());
            if (exc instanceof OperationFailedException) {
                throw ((OperationFailedException) exc);
            }
            throw new OperationFailedException("Failed to generate a from header for our register request.", 4, exc);
        }
    }

    public void processOK(ClientTransaction clientTransatcion, Response response) {
        int requestedExpiration;
        ExpiresHeader expiresHeader;
        int grantedExpiration;
        Request register = clientTransatcion.getRequest();
        ContactHeader contactHeader = (ContactHeader) register.getHeader("Contact");
        if (contactHeader != null) {
            requestedExpiration = contactHeader.getExpires();
        } else {
            requestedExpiration = 0;
        }
        if (requestedExpiration <= 0) {
            expiresHeader = register.getExpires();
            if (expiresHeader != null) {
                requestedExpiration = expiresHeader.getExpires();
            }
        }
        FromHeader fromHeader = (FromHeader) register.getHeader("From");
        if (!(fromHeader == null || fromHeader.getAddress() == null || !this.sipProvider.setOurDisplayName(fromHeader.getAddress().getDisplayName()))) {
            this.ourSipAddressOfRecord = null;
        }
        ContactHeader responseContactHdr = (ContactHeader) response.getHeader("Contact");
        if (responseContactHdr != null) {
            grantedExpiration = responseContactHdr.getExpires();
        }
        expiresHeader = response.getExpires();
        if (expiresHeader != null) {
            grantedExpiration = expiresHeader.getExpires();
        } else {
            grantedExpiration = requestedExpiration;
        }
        if (grantedExpiration <= 0 || requestedExpiration <= 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Account " + this.sipProvider.getAccountID().getDisplayName() + " unregistered!");
            }
            setRegistrationState(RegistrationState.UNREGISTERED, 0, "Registration terminated.");
            return;
        }
        int scheduleTime = grantedExpiration;
        String keepAliveMethod = this.sipProvider.getAccountID().getAccountPropertyString(KEEP_ALIVE_METHOD);
        if (keepAliveMethod != null && keepAliveMethod.equalsIgnoreCase("register")) {
            int registrationInterval = this.sipProvider.getAccountID().getAccountPropertyInt(KEEP_ALIVE_INTERVAL, 25);
            if (registrationInterval < grantedExpiration) {
                scheduleTime = registrationInterval;
            }
        }
        this.lastRegisterAddressReceived = ((SIPMessage) response).getRemoteAddress();
        this.lastRegisterPortReceived = ((SIPMessage) response).getRemotePort();
        scheduleReRegistration(scheduleTime);
        ListIterator<AllowHeader> headerIter = response.getHeaders("Allow");
        if (headerIter != null && headerIter.hasNext()) {
            updateSupportedOperationSets(headerIter);
        }
        if (logger.isDebugEnabled() && getRegistrationState().equals(RegistrationState.REGISTERING)) {
            logger.debug("Account " + this.sipProvider.getAccountID().getDisplayName() + " registered!");
        }
        setRegistrationState(RegistrationState.REGISTERED, -1, null);
    }

    public boolean isRequestFromSameConnection(Request request) {
        SIPMessage msg = (SIPMessage) request;
        if (msg.getRemoteAddress() == null || this.lastRegisterAddressReceived == null || (msg.getRemoteAddress().equals(this.lastRegisterAddressReceived) && msg.getRemotePort() == this.lastRegisterPortReceived)) {
            return true;
        }
        return false;
    }

    public void unregister() throws OperationFailedException {
        unregister(true);
    }

    private void unregister(boolean sendUnregister) throws OperationFailedException {
        if (getRegistrationState() != RegistrationState.UNREGISTERED) {
            cancelPendingRegistrations();
            if (this.registerRequest == null) {
                logger.error("Couldn't find the initial register request");
                setRegistrationState(RegistrationState.CONNECTION_FAILED, 6, "Could not find the initial regiest request.");
                throw new OperationFailedException("Could not find the initial register request.", 4);
            }
            setRegistrationState(RegistrationState.UNREGISTERING, 0, "");
            if (sendUnregister) {
                try {
                    try {
                        ClientTransaction unregisterTransaction = getJainSipProvider().getNewClientTransaction(this.sipProvider.getMessageFactory().createUnRegisterRequest(this.registerRequest, getNextCSeqValue()));
                        try {
                            this.callIdHeader = null;
                            unregisterTransaction.sendRequest();
                            if (!getRegistrationState().equals(RegistrationState.REGISTERED) && !getRegistrationState().equals(RegistrationState.UNREGISTERING)) {
                                if (logger.isInfoEnabled()) {
                                    logger.info("Setting state to UNREGISTERED.");
                                }
                                setRegistrationState(RegistrationState.UNREGISTERED, 0, null);
                                if (this.regTrans != null && this.regTrans.getState().getValue() <= TransactionState.PROCEEDING.getValue()) {
                                    if (logger.isTraceEnabled()) {
                                        logger.trace("Will try to terminate reg tran ...");
                                    }
                                    this.regTrans.terminate();
                                    if (logger.isTraceEnabled()) {
                                        logger.trace("Transaction terminated!");
                                    }
                                }
                            }
                        } catch (SipException ex) {
                            logger.error("Failed to send unregister request", ex);
                            setRegistrationState(RegistrationState.CONNECTION_FAILED, 6, "Unable to create a unregister transaction");
                            throw new OperationFailedException("Failed to send unregister request", 4, ex);
                        }
                    } catch (TransactionUnavailableException ex2) {
                        logger.error("Unable to create a unregister transaction", ex2);
                        setRegistrationState(RegistrationState.CONNECTION_FAILED, 6, "Unable to create a unregister transaction");
                        throw new OperationFailedException("Unable to create a unregister transaction", 4, ex2);
                    }
                } catch (InvalidArgumentException ex3) {
                    logger.error("Unable to create an unREGISTER request.", ex3);
                    setRegistrationState(RegistrationState.CONNECTION_FAILED, 6, "Unable to set Expires Header");
                    throw new OperationFailedException("Unable to set Expires Header", 4, ex3);
                }
            }
        } else if (logger.isTraceEnabled()) {
            logger.trace("Trying to unregister when already unresgistered");
        }
    }

    public RegistrationState getRegistrationState() {
        return this.currentRegistrationState;
    }

    public void setRegistrationState(RegistrationState newState, int reasonCode, String reason) {
        if (!this.currentRegistrationState.equals(newState)) {
            RegistrationState oldState = this.currentRegistrationState;
            this.currentRegistrationState = newState;
            this.sipProvider.fireRegistrationStateChanged(oldState, newState, reasonCode, reason);
        }
    }

    private void cancelPendingRegistrations() {
        this.reRegisterTimer.cancel();
        this.reRegisterTimer = null;
        this.reRegisterTimer = new Timer();
    }

    private void scheduleReRegistration(int expires) {
        ReRegisterTask reRegisterTask = new ReRegisterTask();
        if (expires > 60) {
            expires *= 900;
        } else {
            expires *= 1000;
        }
        this.reRegisterTimer.schedule(reRegisterTask, (long) expires);
    }

    private long getNextCSeqValue() {
        long j = this.nextCSeqValue;
        this.nextCSeqValue = 1 + j;
        return j;
    }

    public void processNotImplemented(ClientTransaction transatcion, Response response) {
        setRegistrationState(RegistrationState.CONNECTION_FAILED, -1, this.registrarName + " does not appear to be a sip registrar. (Returned a " + "NOT_IMPLEMENTED response to a register request)");
    }

    private void updateSupportedOperationSets(ListIterator<AllowHeader> headerIter) {
        HashSet<String> set = new HashSet();
        while (headerIter.hasNext()) {
            set.add(((AllowHeader) headerIter.next()).getMethod());
        }
        if (!set.contains("MESSAGE") && !this.sipProvider.getAccountID().getAccountPropertyBoolean(FORCE_MESSAGING_PROP, false)) {
            this.sipProvider.removeSupportedOperationSet(OperationSetBasicInstantMessaging.class);
        }
    }

    public SipProvider getJainSipProvider() {
        return this.sipProvider.getJainSipProvider(getTransport());
    }

    public String getTransport() {
        return this.registrationTransport;
    }

    public boolean processResponse(ResponseEvent responseEvent) {
        ClientTransaction clientTransaction = responseEvent.getClientTransaction();
        Response response = responseEvent.getResponse();
        SipProvider sourceProvider = (SipProvider) responseEvent.getSource();
        if (response.getStatusCode() == Response.OK) {
            processOK(clientTransaction, response);
            return true;
        } else if (response.getStatusCode() == Response.NOT_IMPLEMENTED) {
            processNotImplemented(clientTransaction, response);
            return true;
        } else if (response.getStatusCode() == 100) {
            return false;
        } else {
            if (response.getStatusCode() == Response.UNAUTHORIZED || response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED || response.getStatusCode() == Response.FORBIDDEN) {
                processAuthenticationChallenge(clientTransaction, response, sourceProvider);
                return true;
            } else if (response.getStatusCode() == Response.INTERVAL_TOO_BRIEF) {
                processIntervalTooBrief(response);
                return true;
            } else if (response.getStatusCode() < Response.BAD_REQUEST) {
                return false;
            } else {
                logger.error("Received an error response (" + response.getStatusCode() + Separators.RPAREN);
                int registrationStateReason = -1;
                if (response.getStatusCode() == Response.NOT_FOUND) {
                    registrationStateReason = 3;
                }
                setRegistrationState(RegistrationState.CONNECTION_FAILED, registrationStateReason, "Received an error while trying to register. Server returned error:" + response.getReasonPhrase());
                return true;
            }
        }
    }

    private void processAuthenticationChallenge(ClientTransaction clientTransaction, Response response, SipProvider jainSipProvider) {
        try {
            ClientTransaction retryTran;
            if (logger.isDebugEnabled()) {
                logger.debug("Authenticating a Register request.");
            }
            if (response.getStatusCode() == Response.UNAUTHORIZED || response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED) {
                retryTran = this.sipProvider.getSipSecurityManager().handleChallenge(response, clientTransaction, jainSipProvider);
            } else if (this.sipProvider.getAccountID().getAccountPropertyString("CLIENT_TLS_CERTIFICATE") != null) {
                setRegistrationState(RegistrationState.AUTHENTICATION_FAILED, 3, "We failed to authenticate with the server.");
                return;
            } else {
                retryTran = this.sipProvider.getSipSecurityManager().handleForbiddenResponse(response, clientTransaction, jainSipProvider);
            }
            if (retryTran == null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("No password supplied or error occured!");
                }
                unregister(false);
                return;
            }
            updateRegisterSequenceNumber(retryTran);
            retryTran.sendRequest();
        } catch (OperationFailedException exc) {
            if (exc.getErrorCode() == 15) {
                setRegistrationState(RegistrationState.UNREGISTERED, 0, "User has canceled the authentication process.");
            } else if (exc.getErrorCode() == Response.FORBIDDEN) {
                setRegistrationState(RegistrationState.CONNECTION_FAILED, 1, exc.getMessage());
            } else {
                setRegistrationState(RegistrationState.AUTHENTICATION_FAILED, 1, "We failed to authenticate with the server.");
            }
        } catch (Exception exc2) {
            logger.error("We failed to authenticate a Register request.", exc2);
            setRegistrationState(RegistrationState.AUTHENTICATION_FAILED, 1, "We failed to authenticate with the server.");
        }
    }

    public boolean processRequest(RequestEvent requestEvent) {
        return false;
    }

    public boolean processTimeout(TimeoutEvent timeoutEvent) {
        if (this.sipProvider.registerUsingNextAddress()) {
            return false;
        }
        if (!getRegistrationState().equals(RegistrationState.UNREGISTERED)) {
            setRegistrationState(RegistrationState.CONNECTION_FAILED, -1, "A timeout occurred while trying to connect to the server.");
        }
        return true;
    }

    public boolean processIOException(IOExceptionEvent exceptionEvent) {
        setRegistrationState(RegistrationState.CONNECTION_FAILED, -1, "An error occurred while trying to connect to the server.[" + exceptionEvent.getHost() + "]:" + exceptionEvent.getPort() + Separators.SLASH + exceptionEvent.getTransport());
        return true;
    }

    private void processIntervalTooBrief(Response response) {
        MinExpiresHeader header = (MinExpiresHeader) response.getHeader("Min-Expires");
        if (header != null) {
            int expires = header.getExpires();
            if (expires > this.registrationsExpiration) {
                this.registrationsExpiration = expires;
                try {
                    register();
                    return;
                } catch (Throwable e) {
                    logger.error("Cannot send register!", e);
                    setRegistrationState(RegistrationState.CONNECTION_FAILED, -1, "A timeout occurred while trying to connect to the server.");
                    return;
                }
            }
        }
        setRegistrationState(RegistrationState.CONNECTION_FAILED, -1, "Received an error while trying to register. Server returned error:" + response.getReasonPhrase());
    }

    public String toString() {
        String className = getClass().getName();
        try {
            className = className.substring(className.lastIndexOf(46) + 1);
        } catch (Exception e) {
        }
        return className + "-[dn=" + this.sipProvider.getOurDisplayName() + " addr=" + getAddressOfRecord() + "]";
    }

    private void updateRegisterSequenceNumber(ClientTransaction lastClientTran) {
        this.nextCSeqValue = 1 + ((CSeqHeader) lastClientTran.getRequest().getHeader("CSeq")).getSeqNumber();
    }

    public boolean isRouteHeaderEnabled() {
        return this.useRouteHeader;
    }

    public boolean isRegistrarless() {
        return false;
    }

    public SipURI getRegistrarURI() throws ParseException {
        if (this.registrarURI == null) {
            this.registrarURI = this.sipProvider.getAddressFactory().createSipURI(null, this.registrarName);
            if (this.registrarPort != 5060) {
                this.registrarURI.setPort(this.registrarPort);
            }
            if (!this.registrationTransport.equals(ListeningPoint.UDP)) {
                this.registrarURI.setTransportParam(this.registrationTransport);
            }
        }
        return this.registrarURI;
    }

    public Address getAddressOfRecord() {
        if (this.ourSipAddressOfRecord != null) {
            return this.ourSipAddressOfRecord;
        }
        if (isRegistrarless()) {
            return null;
        }
        String ourUserID = this.sipProvider.getAccountID().getAccountPropertyString("USER_ID");
        String sipUriHost = null;
        if (ourUserID.indexOf(Separators.AT) != -1 && ourUserID.indexOf(Separators.AT) < ourUserID.length() - 1) {
            sipUriHost = ourUserID.substring(ourUserID.indexOf(Separators.AT) + 1);
            ourUserID = ourUserID.substring(0, ourUserID.indexOf(Separators.AT));
        }
        if (sipUriHost == null) {
            sipUriHost = this.registrarName;
        }
        try {
            this.ourSipAddressOfRecord = this.sipProvider.getAddressFactory().createAddress(this.sipProvider.getOurDisplayName(), this.sipProvider.getAddressFactory().createSipURI(ourUserID, sipUriHost));
            this.ourSipAddressOfRecord.setDisplayName(this.sipProvider.getOurDisplayName());
            return this.ourSipAddressOfRecord;
        } catch (ParseException e) {
            throw new IllegalArgumentException("Could not create a SIP URI for user " + ourUserID + Separators.AT + sipUriHost + " and registrar " + this.registrarName);
        }
    }
}
