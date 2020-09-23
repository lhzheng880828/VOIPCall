package net.java.sip.communicator.impl.protocol.sip;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.javax.sip.stack.DefaultRouter;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.SipStack;
import org.jitsi.javax.sip.address.Hop;
import org.jitsi.javax.sip.address.Router;
import org.jitsi.javax.sip.header.ToHeader;
import org.jitsi.javax.sip.message.Request;

public class ProxyRouter implements Router {
    private static final Logger logger = Logger.getLogger(ProxyRouter.class);
    private Router defaultRouter = null;
    private final Map<String, Router> routerCache = new HashMap();
    private final SipStack stack;

    public ProxyRouter(SipStack stack, String defaultRoute) {
        if (stack == null) {
            throw new IllegalArgumentException("stack shouldn't be null!");
        }
        this.stack = stack;
    }

    public Hop getNextHop(Request request) throws SipException {
        return getRouterFor(request).getNextHop(request);
    }

    @Deprecated
    public ListIterator getNextHops(Request request) {
        return getRouterFor(request).getNextHops(request);
    }

    public Hop getOutboundProxy() {
        logger.fatal("If you see this then please please describe your SIP setup and send the following stack trace todev@sip-communicator.dev.java.net", new Exception());
        return null;
    }

    private Router getRouterFor(Request request) {
        ProtocolProviderServiceSipImpl service = SipApplicationData.getApplicationData(request, "service");
        if (service instanceof ProtocolProviderServiceSipImpl) {
            ProtocolProviderServiceSipImpl sipProvider = service;
            String proxy = sipProvider.getConnection().getOutboundProxyString();
            boolean forceLooseRouting = sipProvider.getAccountID().getAccountPropertyBoolean("FORCE_PROXY_BYPASS", false);
            if (proxy == null || forceLooseRouting) {
                return getDefaultRouter();
            }
            Router router = (Router) this.routerCache.get(proxy);
            if (router != null) {
                return router;
            }
            router = new DefaultRouter(this.stack, proxy);
            this.routerCache.put(proxy, router);
            return router;
        }
        if (((ToHeader) request.getHeader("To")).getTag() == null) {
            logger.error("unable to identify the service which created this out-of-dialog request");
        }
        return getDefaultRouter();
    }

    private Router getDefaultRouter() {
        if (this.defaultRouter == null) {
            this.defaultRouter = new DefaultRouter(this.stack, null);
        }
        return this.defaultRouter;
    }
}
