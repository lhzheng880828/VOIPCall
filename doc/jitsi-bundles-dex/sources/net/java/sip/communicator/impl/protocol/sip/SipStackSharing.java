package net.java.sip.communicator.impl.protocol.sip;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;
import java.util.Vector;
import net.java.sip.communicator.service.netaddr.event.ChangeEvent;
import net.java.sip.communicator.service.netaddr.event.NetworkConfigurationChangeListener;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.NetworkUtils;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.SipStackImpl;
import org.jitsi.gov.nist.javax.sip.header.HeaderFactoryImpl;
import org.jitsi.gov.nist.javax.sip.stack.SIPTransactionStack;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.DialogTerminatedEvent;
import org.jitsi.javax.sip.IOExceptionEvent;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.ListeningPoint;
import org.jitsi.javax.sip.ObjectInUseException;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.ServerTransaction;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.SipFactory;
import org.jitsi.javax.sip.SipListener;
import org.jitsi.javax.sip.SipProvider;
import org.jitsi.javax.sip.SipStack;
import org.jitsi.javax.sip.TimeoutEvent;
import org.jitsi.javax.sip.Transaction;
import org.jitsi.javax.sip.TransactionAlreadyExistsException;
import org.jitsi.javax.sip.TransactionTerminatedEvent;
import org.jitsi.javax.sip.TransactionUnavailableException;
import org.jitsi.javax.sip.TransportAlreadySupportedException;
import org.jitsi.javax.sip.TransportNotSupportedException;
import org.jitsi.javax.sip.address.SipURI;
import org.jitsi.javax.sip.address.URI;
import org.jitsi.javax.sip.header.EventHeader;
import org.jitsi.javax.sip.header.FromHeader;
import org.jitsi.javax.sip.header.ToHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.util.OSUtils;

public class SipStackSharing implements SipListener, NetworkConfigurationChangeListener {
    public static final String CONTACT_ADDRESS_CUSTOM_PARAM_NAME = "registering_acc";
    private static final String PREFERRED_CLEAR_PORT_PROPERTY_NAME = "net.java.sip.communicator.SIP_PREFERRED_CLEAR_PORT";
    private static final String PREFERRED_SECURE_PORT_PROPERTY_NAME = "net.java.sip.communicator.SIP_PREFERRED_SECURE_PORT";
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(SipStackSharing.class);
    /* access modifiers changed from: private */
    public SipProvider clearJainSipProvider = null;
    private final Set<ProtocolProviderServiceSipImpl> listeners = new HashSet();
    Map<String, TimerTask> resetListeningPointsTimers = new HashMap();
    private SipProvider secureJainSipProvider = null;
    /* access modifiers changed from: private|final */
    public final SipStack stack;

    private class ResetListeningPoint extends TimerTask implements RegistrationStateChangeListener {
        private static final int TIME_FOR_PP_TO_UNREGISTER = 20000;
        private final ProtocolProviderServiceSipImpl protocolProvider;

        ResetListeningPoint(ProtocolProviderServiceSipImpl pp) {
            this.protocolProvider = pp;
        }

        public void registrationStateChanged(RegistrationStateChangeEvent evt) {
            if (evt.getNewState() == RegistrationState.UNREGISTERING) {
                new Timer().schedule(this, 20000);
                return;
            }
            this.protocolProvider.removeRegistrationStateChangeListener(this);
            SipStackSharing.this.resetListeningPointsTimers.remove(this.protocolProvider.getRegistrarConnection().getTransport());
        }

        public void run() {
            if (this.protocolProvider.getRegistrationState() == RegistrationState.UNREGISTERING) {
                String transport = this.protocolProvider.getRegistrarConnection().getTransport();
                try {
                    SipStackSharing.this.stack.deleteListeningPoint(SipStackSharing.this.getLP(transport));
                } catch (Throwable t) {
                    SipStackSharing.logger.warn("Error replacing ListeningPoint for " + transport, t);
                }
                try {
                    SipStackSharing.this.clearJainSipProvider.addListeningPoint(SipStackSharing.this.stack.createListeningPoint(NetworkUtils.IN_ADDR_ANY, transport.equals(ListeningPoint.TCP) ? SipStackSharing.this.getPreferredClearPort() : SipStackSharing.this.getPreferredSecurePort(), transport));
                } catch (Throwable t2) {
                    SipStackSharing.logger.warn("Error replacing ListeningPoint for " + this.protocolProvider.getRegistrarConnection().getTransport(), t2);
                }
            }
            SipStackSharing.this.resetListeningPointsTimers.remove(this.protocolProvider.getRegistrarConnection().getTransport());
        }
    }

    SipStackSharing() throws OperationFailedException {
        try {
            SipFactory sipFactory = SipFactory.getInstance();
            if (OSUtils.IS_ANDROID) {
                sipFactory.setPathName("org.jitsi.gov.nist");
            } else {
                sipFactory.setPathName("gov.nist");
            }
            this.stack = sipFactory.createSipStack(new SipStackProperties());
            if (logger.isTraceEnabled()) {
                logger.trace("Created stack: " + this.stack);
            }
            ((SIPTransactionStack) this.stack).setAddressResolver(new AddressResolverImpl());
            SipActivator.getNetworkAddressManagerService().addNetworkConfigurationChangeListener(this);
        } catch (Exception ex) {
            logger.fatal("Failed to get SIP Factory.", ex);
            throw new OperationFailedException("Failed to get SIP Factory", 4, ex);
        }
    }

    public void addSipListener(ProtocolProviderServiceSipImpl listener) throws OperationFailedException {
        synchronized (this.listeners) {
            if (this.listeners.size() == 0) {
                startListening();
            }
            this.listeners.add(listener);
            if (logger.isTraceEnabled()) {
                logger.trace(this.listeners.size() + " listeners now");
            }
        }
    }

    public void removeSipListener(ProtocolProviderServiceSipImpl listener) {
        synchronized (this.listeners) {
            this.listeners.remove(listener);
            int listenerCount = this.listeners.size();
            if (logger.isTraceEnabled()) {
                logger.trace(listenerCount + " listeners left");
            }
            if (listenerCount == 0) {
                stopListening();
            }
        }
    }

    private Set<ProtocolProviderServiceSipImpl> getSipListeners() {
        HashSet hashSet;
        synchronized (this.listeners) {
            hashSet = new HashSet(this.listeners);
        }
        return hashSet;
    }

    public ListeningPoint getLP(String transport) {
        Iterator<ListeningPoint> it = this.stack.getListeningPoints();
        while (it.hasNext()) {
            ListeningPoint lp = (ListeningPoint) it.next();
            if (lp.getTransport().toLowerCase().equals(transport.toLowerCase())) {
                return lp;
            }
        }
        throw new IllegalArgumentException("Invalid transport: " + transport);
    }

    private void startListening() throws OperationFailedException {
        try {
            int bindRetriesValue = getBindRetriesValue();
            createProvider(getPreferredClearPort(), bindRetriesValue, false);
            createProvider(getPreferredSecurePort(), bindRetriesValue, true);
            this.stack.start();
            if (logger.isTraceEnabled()) {
                logger.trace("started listening");
            }
        } catch (Exception ex) {
            logger.error("An unexpected error happened while creating theSipProviders and ListeningPoints.");
            throw new OperationFailedException("An unexpected error hapennedwhile initializing the SIP stack", 4, ex);
        }
    }

    private void createProvider(int preferredPort, int retries, boolean secure) throws TransportNotSupportedException, InvalidArgumentException, ObjectInUseException, TransportAlreadySupportedException, TooManyListenersException {
        String context = secure ? "TLS: " : "clear UDP/TCP: ";
        if (retries < 0) {
            logger.error(context + "couldn't find free ports to listen on.");
            return;
        }
        if (secure) {
            try {
                ListeningPoint tlsLP = this.stack.createListeningPoint(NetworkUtils.IN_ADDR_ANY, preferredPort, ListeningPoint.TLS);
                if (logger.isTraceEnabled()) {
                    logger.trace("TLS secure ListeningPoint has been created.");
                }
                this.secureJainSipProvider = this.stack.createSipProvider(tlsLP);
                this.secureJainSipProvider.addSipListener(this);
            } catch (InvalidArgumentException ex) {
                if (null != null) {
                    this.stack.deleteListeningPoint(null);
                }
                if (null != null) {
                    this.stack.deleteListeningPoint(null);
                }
                if (null != null) {
                    this.stack.deleteListeningPoint(null);
                }
                if (ex.getCause() instanceof IOException) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Port " + preferredPort + " seems in use for either TCP or UDP.");
                    }
                    int currentlyTriedPort = NetworkUtils.getRandomPortNumber();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Retrying bind on port " + currentlyTriedPort);
                    }
                    createProvider(currentlyTriedPort, retries - 1, secure);
                    return;
                }
                throw ex;
            }
        }
        ListeningPoint udpLP = this.stack.createListeningPoint(NetworkUtils.IN_ADDR_ANY, preferredPort, ListeningPoint.UDP);
        ListeningPoint tcpLP = this.stack.createListeningPoint(NetworkUtils.IN_ADDR_ANY, preferredPort, ListeningPoint.TCP);
        if (logger.isTraceEnabled()) {
            logger.trace("UDP and TCP clear ListeningPoints have been created.");
        }
        this.clearJainSipProvider = this.stack.createSipProvider(udpLP);
        this.clearJainSipProvider.addListeningPoint(tcpLP);
        this.clearJainSipProvider.addSipListener(this);
        if (logger.isTraceEnabled()) {
            logger.trace(context + "SipProvider has been created.");
        }
    }

    private void stopListening() {
        try {
            if (this.secureJainSipProvider != null) {
                this.secureJainSipProvider.removeSipListener(this);
                this.stack.deleteSipProvider(this.secureJainSipProvider);
                this.secureJainSipProvider = null;
            }
            if (this.clearJainSipProvider != null) {
                this.clearJainSipProvider.removeSipListener(this);
                this.stack.deleteSipProvider(this.clearJainSipProvider);
                this.clearJainSipProvider = null;
            }
            Iterator<ListeningPoint> it = this.stack.getListeningPoints();
            Vector<ListeningPoint> lpointsToRemove = new Vector();
            while (it.hasNext()) {
                lpointsToRemove.add(it.next());
            }
            it = lpointsToRemove.iterator();
            while (it.hasNext()) {
                this.stack.deleteListeningPoint((ListeningPoint) it.next());
            }
            this.stack.stop();
            if (logger.isTraceEnabled()) {
                logger.trace("stopped listening");
            }
        } catch (ObjectInUseException ex) {
            logger.fatal("Failed to stop listening", ex);
        }
    }

    public SipProvider getJainSipProvider(String transport) {
        SipProvider sp = null;
        if (transport.equalsIgnoreCase(ListeningPoint.UDP) || transport.equalsIgnoreCase(ListeningPoint.TCP)) {
            sp = this.clearJainSipProvider;
        } else if (transport.equalsIgnoreCase(ListeningPoint.TLS)) {
            sp = this.secureJainSipProvider;
        }
        if (sp != null) {
            return sp;
        }
        throw new IllegalArgumentException("invalid transport");
    }

    /* access modifiers changed from: private */
    public int getPreferredClearPort() {
        int preferredPort = SipActivator.getConfigurationService().getInt(PREFERRED_CLEAR_PORT_PROPERTY_NAME, -1);
        if (preferredPort <= 1) {
            preferredPort = SipActivator.getResources().getSettingsInt(PREFERRED_CLEAR_PORT_PROPERTY_NAME);
        }
        if (preferredPort <= 1) {
            return 5060;
        }
        return preferredPort;
    }

    /* access modifiers changed from: private */
    public int getPreferredSecurePort() {
        int preferredPort = SipActivator.getConfigurationService().getInt(PREFERRED_SECURE_PORT_PROPERTY_NAME, -1);
        if (preferredPort <= 1) {
            preferredPort = SipActivator.getResources().getSettingsInt(PREFERRED_SECURE_PORT_PROPERTY_NAME);
        }
        if (preferredPort <= 1) {
            return 5061;
        }
        return preferredPort;
    }

    private int getBindRetriesValue() {
        return SipActivator.getConfigurationService().getInt("net.java.sip.communicator.service.protocol.BIND_RETRIES", 50);
    }

    public void processDialogTerminated(DialogTerminatedEvent event) {
        try {
            ProtocolProviderServiceSipImpl recipient = (ProtocolProviderServiceSipImpl) SipApplicationData.getApplicationData(event.getDialog(), "service");
            if (recipient == null) {
                logger.error("Dialog wasn't marked, please report this to dev@sip-communicator.dev.java.net");
                return;
            }
            if (logger.isTraceEnabled()) {
                logger.trace("service was found with dialog data");
            }
            recipient.processDialogTerminated(event);
        } catch (Throwable exc) {
            logApplicationException(DialogTerminatedEvent.class, exc);
        }
    }

    public void processIOException(IOExceptionEvent event) {
        try {
            if (logger.isTraceEnabled()) {
                logger.trace(event);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("@todo implement processIOException()");
            }
        } catch (Throwable exc) {
            logApplicationException(DialogTerminatedEvent.class, exc);
        }
    }

    public void processRequest(RequestEvent event) {
        Request request;
        try {
            request = event.getRequest();
            if (logger.isTraceEnabled()) {
                logger.trace("received request: " + request.getMethod());
            }
            if (event.getServerTransaction() == null) {
                if (!applyNonConformanceHacks(event)) {
                    SipProvider source = (SipProvider) event.getSource();
                    ServerTransaction transaction = source.getNewServerTransaction(request);
                    event = new RequestEvent(source, transaction, transaction.getDialog(), request);
                } else {
                    return;
                }
            }
        } catch (SipException ex) {
            logger.error("couldn't create transaction, please report this to dev@sip-communicator.dev.java.net", ex);
        } catch (Throwable exc) {
            logApplicationException(DialogTerminatedEvent.class, exc);
            if (exc instanceof ThreadDeath) {
                ThreadDeath exc2 = (ThreadDeath) exc;
            } else {
                return;
            }
        }
        ProtocolProviderServiceSipImpl service = getServiceData(event.getServerTransaction());
        if (service != null) {
            service.processRequest(event);
            return;
        }
        service = findTargetFor(request);
        if (service == null) {
            logger.error("couldn't find a ProtocolProviderServiceSipImpl to dispatch to");
            if (event.getServerTransaction() != null) {
                event.getServerTransaction().terminate();
                return;
            }
            return;
        }
        Object container = event.getDialog();
        if (container == null) {
            container = request;
        }
        SipApplicationData.setApplicationData(container, "service", service);
        service.processRequest(event);
    }

    public void processResponse(ResponseEvent event) {
        try {
            ClientTransaction transaction = event.getClientTransaction();
            if (logger.isTraceEnabled()) {
                logger.trace("received response: " + event.getResponse().getStatusCode() + Separators.SP + event.getResponse().getReasonPhrase());
            }
            if (transaction == null) {
                logger.warn("Transaction is null, probably already expired!");
                return;
            }
            ProtocolProviderServiceSipImpl service = getServiceData(transaction);
            if (service != null) {
                if (event.getDialog() != null) {
                    SipApplicationData.setApplicationData(event.getDialog(), "service", service);
                }
                service.processResponse(event);
                return;
            }
            logger.error("We received a response which wasn't marked, please report this to dev@sip-communicator.dev.java.net");
        } catch (Throwable exc) {
            logApplicationException(DialogTerminatedEvent.class, exc);
        }
    }

    public void processTimeout(TimeoutEvent event) {
        try {
            Transaction transaction;
            if (event.isServerTransaction()) {
                transaction = event.getServerTransaction();
            } else {
                transaction = event.getClientTransaction();
            }
            ProtocolProviderServiceSipImpl recipient = getServiceData(transaction);
            if (recipient == null) {
                logger.error("We received a timeout which wasn't marked, please report this to dev@sip-communicator.dev.java.net");
            } else {
                recipient.processTimeout(event);
            }
        } catch (Throwable exc) {
            logApplicationException(DialogTerminatedEvent.class, exc);
        }
    }

    public void processTransactionTerminated(TransactionTerminatedEvent event) {
        try {
            Transaction transaction;
            if (event.isServerTransaction()) {
                transaction = event.getServerTransaction();
            } else {
                transaction = event.getClientTransaction();
            }
            ProtocolProviderServiceSipImpl recipient = getServiceData(transaction);
            if (recipient == null) {
                logger.error("We received a transaction terminated which wasn't marked, please report this to dev@sip-communicator.dev.java.net");
            } else {
                recipient.processTransactionTerminated(event);
            }
        } catch (Throwable exc) {
            logApplicationException(DialogTerminatedEvent.class, exc);
        }
    }

    private ProtocolProviderServiceSipImpl findTargetFor(Request request) {
        if (request == null) {
            logger.error("request shouldn't be null.");
            return null;
        }
        List<ProtocolProviderServiceSipImpl> currentListenersCopy = new ArrayList(getSipListeners());
        filterByAddress(currentListenersCopy, request);
        if (currentListenersCopy.size() == 0) {
            logger.error("no listeners");
            return null;
        }
        URI requestURI = request.getRequestURI();
        if (requestURI.isSipURI()) {
            String requestUser = ((SipURI) requestURI).getUser();
            List<ProtocolProviderServiceSipImpl> candidates = new ArrayList();
            for (ProtocolProviderServiceSipImpl listener : currentListenersCopy) {
                if (listener.getAccountID().getUserID().equals(requestUser)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("suitable candidate found: " + listener.getAccountID());
                    }
                    candidates.add(listener);
                }
            }
            ProtocolProviderServiceSipImpl target;
            if (candidates.size() == 1) {
                ProtocolProviderServiceSipImpl perfectMatch = (ProtocolProviderServiceSipImpl) candidates.get(0);
                if (logger.isTraceEnabled()) {
                    logger.trace("Will dispatch to \"" + perfectMatch.getAccountID() + Separators.DOUBLE_QUOTE);
                }
                return perfectMatch;
            } else if (candidates.size() > 1) {
                for (ProtocolProviderServiceSipImpl candidate : candidates) {
                    String hostValue = ((SipURI) requestURI).getParameter(CONTACT_ADDRESS_CUSTOM_PARAM_NAME);
                    if (hostValue != null && hostValue.equals(candidate.getContactAddressCustomParamValue())) {
                        if (!logger.isTraceEnabled()) {
                            return candidate;
                        }
                        logger.trace("Will dispatch to \"" + candidate.getAccountID() + "\" because " + "\" the custom param was set");
                        return candidate;
                    }
                }
                for (ProtocolProviderServiceSipImpl candidate2 : candidates) {
                    URI fromURI = ((FromHeader) request.getHeader("From")).getAddress().getURI();
                    if (fromURI.isSipURI()) {
                        String ourHost = ((SipURI) candidate2.getOurSipAddress((SipURI) fromURI).getURI()).getHost();
                        URI toURI = ((ToHeader) request.getHeader("To")).getAddress().getURI();
                        if (toURI.isSipURI() && ((SipURI) toURI).getHost().equals(ourHost)) {
                            if (!logger.isTraceEnabled()) {
                                return candidate2;
                            }
                            logger.trace("Will dispatch to \"" + candidate2.getAccountID() + "\" because " + "host in the To: is the same as in our AOR");
                            return candidate2;
                        }
                    }
                }
                target = (ProtocolProviderServiceSipImpl) candidates.iterator().next();
                logger.info("Will randomly dispatch to \"" + target.getAccountID() + "\" because there is ambiguity on the username from" + " the Request-URI");
                if (logger.isTraceEnabled()) {
                    logger.trace(Separators.RETURN + request);
                }
                return target;
            } else {
                target = (ProtocolProviderServiceSipImpl) currentListenersCopy.iterator().next();
                if (logger.isDebugEnabled()) {
                    logger.debug("Will randomly dispatch to \"" + target.getAccountID() + "\" because the username in the Request-URI " + "is unknown or empty");
                }
                if (logger.isTraceEnabled()) {
                    logger.trace(Separators.RETURN + request);
                }
                return target;
            }
        }
        logger.error("Request-URI is not a SIP URI, dropping");
        return null;
    }

    private void filterByAddress(List<ProtocolProviderServiceSipImpl> candidates, Request request) {
        Iterator<ProtocolProviderServiceSipImpl> iterPP = candidates.iterator();
        while (iterPP.hasNext()) {
            ProtocolProviderServiceSipImpl candidate = (ProtocolProviderServiceSipImpl) iterPP.next();
            if (!(candidate.getRegistrarConnection() == null || candidate.getRegistrarConnection().isRegistrarless() || candidate.getRegistrarConnection().isRequestFromSameConnection(request))) {
                iterPP.remove();
            }
        }
    }

    private ProtocolProviderServiceSipImpl getServiceData(Transaction transaction) {
        ProtocolProviderServiceSipImpl service = (ProtocolProviderServiceSipImpl) SipApplicationData.getApplicationData(transaction.getRequest(), "service");
        if (service != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("service was found in request data");
            }
            return service;
        }
        service = (ProtocolProviderServiceSipImpl) SipApplicationData.getApplicationData(transaction.getDialog(), "service");
        if (service != null && logger.isTraceEnabled()) {
            logger.trace("service was found in dialog data");
        }
        return service;
    }

    private void logApplicationException(Class<DialogTerminatedEvent> eventClass, Throwable exc) {
        String message = "An error occurred while processing event of type: " + eventClass.getName();
        logger.error(message, exc);
        if (logger.isDebugEnabled()) {
            logger.debug(message, exc);
        }
    }

    public static ServerTransaction getOrCreateServerTransaction(RequestEvent event) throws TransactionAlreadyExistsException, TransactionUnavailableException {
        ServerTransaction serverTransaction = event.getServerTransaction();
        if (serverTransaction == null) {
            return ((SipProvider) event.getSource()).getNewServerTransaction(event.getRequest());
        }
        return serverTransaction;
    }

    public InetSocketAddress getLocalAddressForDestination(InetAddress dst, int dstPort, InetAddress localAddress, String transport) throws IOException {
        if (ListeningPoint.TLS.equalsIgnoreCase(transport)) {
            return (InetSocketAddress) ((SipStackImpl) this.stack).getLocalAddressForTlsDst(dst, dstPort, localAddress);
        }
        return (InetSocketAddress) ((SipStackImpl) this.stack).getLocalAddressForTcpDst(dst, dstPort, localAddress, 0);
    }

    private boolean applyNonConformanceHacks(RequestEvent event) {
        Request request = event.getRequest();
        try {
            if (request.getHeader("Max-Forwards") == null) {
                request.setHeader(SipFactory.getInstance().createHeaderFactory().createMaxForwardsHeader(70));
            }
        } catch (Throwable ex) {
            logger.warn("Cannot apply incoming request modification!", ex);
        }
        try {
            if (request.getMethod().equals("NOTIFY") && request.getHeader("Event") != null && ((EventHeader) request.getHeader("Event")).getEventType().equals("message-summary") && request.getHeader("Subscription-State") == null) {
                request.addHeader(new HeaderFactoryImpl().createSubscriptionStateHeader("active"));
            }
        } catch (Throwable ex2) {
            logger.warn("Cannot apply incoming request modification!", ex2);
        }
        try {
            if (request.getMethod().equals("NOTIFY") && request.getHeader("Subscription-State") == null) {
                return true;
            }
        } catch (Throwable ex22) {
            logger.warn("Cannot apply incoming request modification!", ex22);
        }
        return false;
    }

    public void configurationChanged(ChangeEvent event) {
        if (!event.isInitial() && event.getType() == 2) {
            for (ProtocolProviderServiceSipImpl pp : this.listeners) {
                if (pp.getRegistrarConnection().getTransport() != null && (pp.getRegistrarConnection().getTransport().equals(ListeningPoint.TCP) || pp.getRegistrarConnection().getTransport().equals(ListeningPoint.TLS))) {
                    synchronized (this.resetListeningPointsTimers) {
                        if (this.resetListeningPointsTimers.containsKey(pp.getRegistrarConnection().getTransport())) {
                        } else {
                            ResetListeningPoint reseter = new ResetListeningPoint(pp);
                            this.resetListeningPointsTimers.put(pp.getRegistrarConnection().getTransport(), reseter);
                            pp.addRegistrationStateChangeListener(reseter);
                        }
                    }
                }
            }
        }
    }
}
