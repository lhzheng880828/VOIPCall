package org.jitsi.gov.nist.javax.sip.stack;

import java.util.LinkedList;
import java.util.ListIterator;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.address.AddressImpl;
import org.jitsi.gov.nist.javax.sip.address.ParameterNames;
import org.jitsi.gov.nist.javax.sip.address.SipUri;
import org.jitsi.gov.nist.javax.sip.header.RequestLine;
import org.jitsi.gov.nist.javax.sip.header.Route;
import org.jitsi.gov.nist.javax.sip.header.RouteList;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.SipStack;
import org.jitsi.javax.sip.address.Hop;
import org.jitsi.javax.sip.address.Router;
import org.jitsi.javax.sip.address.SipURI;
import org.jitsi.javax.sip.address.URI;
import org.jitsi.javax.sip.header.ViaHeader;
import org.jitsi.javax.sip.message.Request;

public class DefaultRouter implements Router {
    private static StackLogger logger = CommonLogger.getLogger(DefaultRouter.class);
    private Hop defaultRoute;
    private SIPTransactionStack sipStack;

    private DefaultRouter() {
    }

    public DefaultRouter(SipStack sipStack, String defaultRoute) {
        this.sipStack = (SIPTransactionStack) sipStack;
        if (defaultRoute != null) {
            try {
                this.defaultRoute = this.sipStack.getAddressResolver().resolveAddress(new HopImpl(defaultRoute));
            } catch (IllegalArgumentException ex) {
                logger.logError("Invalid default route specification - need host:port/transport");
                throw ex;
            }
        }
    }

    public Hop getNextHop(Request request) throws SipException {
        SIPRequest sipRequest = (SIPRequest) request;
        RequestLine requestLine = sipRequest.getRequestLine();
        if (requestLine == null) {
            return this.defaultRoute;
        }
        URI requestURI = requestLine.getUri();
        if (requestURI == null) {
            throw new IllegalArgumentException("Bad message: Null requestURI");
        }
        RouteList routes = sipRequest.getRouteHeaders();
        Hop hop;
        if (routes != null) {
            URI uri = ((Route) routes.getFirst()).getAddress().getURI();
            if (uri.isSipURI()) {
                SipURI sipUri = (SipURI) uri;
                if (!sipUri.hasLrParam()) {
                    fixStrictRouting(sipRequest);
                    if (logger.isLoggingEnabled(32)) {
                        logger.logDebug("Route post processing fixed strict routing");
                    }
                }
                hop = createHop(sipUri, request);
                if (!logger.isLoggingEnabled(32)) {
                    return hop;
                }
                logger.logDebug("NextHop based on Route:" + hop);
                return hop;
            }
            throw new SipException("First Route not a SIP URI");
        } else if (requestURI.isSipURI() && ((SipURI) requestURI).getMAddrParam() != null) {
            hop = createHop((SipURI) requestURI, request);
            if (!logger.isLoggingEnabled(32)) {
                return hop;
            }
            logger.logDebug("Using request URI maddr to route the request = " + hop.toString());
            return hop;
        } else if (this.defaultRoute != null) {
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("Using outbound proxy to route the request = " + this.defaultRoute.toString());
            }
            return this.defaultRoute;
        } else if (requestURI.isSipURI()) {
            hop = createHop((SipURI) requestURI, request);
            if (hop != null && logger.isLoggingEnabled(32)) {
                logger.logDebug("Used request-URI for nextHop = " + hop.toString());
                return hop;
            } else if (!logger.isLoggingEnabled(32)) {
                return hop;
            } else {
                logger.logDebug("returning null hop -- loop detected");
                return hop;
            }
        } else {
            InternalErrorHandler.handleException("Unexpected non-sip URI", logger);
            return null;
        }
    }

    public void fixStrictRouting(SIPRequest req) {
        RouteList routes = req.getRouteHeaders();
        SipUri firstUri = (SipUri) ((Route) routes.getFirst()).getAddress().getURI();
        routes.removeFirst();
        AddressImpl addr = new AddressImpl();
        addr.setAddess(req.getRequestURI());
        routes.add((SIPHeader) new Route(addr));
        req.setRequestURI(firstUri);
        if (logger.isLoggingEnabled(32)) {
            logger.logDebug("post: fixStrictRouting" + req);
        }
    }

    private final Hop createHop(SipURI sipUri, Request request) {
        int port;
        String transport = sipUri.isSecure() ? ParameterNames.TLS : sipUri.getTransportParam();
        if (transport == null) {
            transport = ((ViaHeader) request.getHeader("Via")).getTransport();
        }
        if (sipUri.getPort() != -1) {
            port = sipUri.getPort();
        } else if (transport.equalsIgnoreCase(ParameterNames.TLS)) {
            port = 5061;
        } else {
            port = 5060;
        }
        return this.sipStack.getAddressResolver().resolveAddress(new HopImpl(sipUri.getMAddrParam() != null ? sipUri.getMAddrParam() : sipUri.getHost(), port, transport));
    }

    public Hop getOutboundProxy() {
        return this.defaultRoute;
    }

    public ListIterator getNextHops(Request request) {
        try {
            LinkedList llist = new LinkedList();
            llist.add(getNextHop(request));
            return llist.listIterator();
        } catch (SipException e) {
            return null;
        }
    }
}
