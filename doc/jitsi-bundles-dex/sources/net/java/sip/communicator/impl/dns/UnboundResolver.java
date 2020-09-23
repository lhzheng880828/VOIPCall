package net.java.sip.communicator.impl.dns;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl;
import net.java.sip.communicator.impl.dns.UnboundApi.UnboundCallback;
import net.java.sip.communicator.service.dns.CustomResolver;
import net.java.sip.communicator.service.dns.DnssecRuntimeException;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.NetworkUtils;
import org.xbill.DNS.Message;
import org.xbill.DNS.ResolverConfig;
import org.xbill.DNS.ResolverListener;
import org.xbill.DNS.TSIG;

public class UnboundResolver implements CustomResolver {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(UnboundResolver.class);
    private String[] forwarders;
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private int timeout = MetaContactListServiceImpl.CONTACT_LIST_MODIFICATION_TIMEOUT;
    private List<String> trustAnchors = new LinkedList();

    private static class CallbackData {
        int asyncId;
        long context;
        ResolverListener listener;
        CountDownLatch sync;

        private CallbackData() {
            this.sync = new CountDownLatch(1);
        }

        /* synthetic */ CallbackData(AnonymousClass1 x0) {
            this();
        }
    }

    public void setForwarders(String[] forwarders) {
        this.forwarders = forwarders;
    }

    public void clearTrustAnchors() {
        this.trustAnchors.clear();
    }

    public void addTrustAnchor(String anchor) {
        this.trustAnchors.add(anchor);
    }

    public SecureMessage send(final Message query) throws IOException {
        try {
            return (SecureMessage) this.threadPool.submit(new Callable<SecureMessage>() {
                public SecureMessage call() throws Exception {
                    Throwable th;
                    if (UnboundResolver.logger.isDebugEnabled()) {
                        UnboundResolver.logger.debug(query);
                    }
                    SecureMessage secureMessage = null;
                    long context = UnboundResolver.this.prepareContext();
                    try {
                        SecureMessage secureMessage2 = new SecureMessage(UnboundApi.resolve(context, query.getQuestion().getName().toString(), query.getQuestion().getType(), query.getQuestion().getDClass()));
                        try {
                            UnboundResolver.this.validateMessage(secureMessage2);
                            UnboundApi.deleteContext(context);
                            if (UnboundResolver.logger.isDebugEnabled() && secureMessage2 != null) {
                                UnboundResolver.logger.debug(secureMessage2);
                            }
                            return secureMessage2;
                        } catch (Throwable th2) {
                            th = th2;
                            secureMessage = secureMessage2;
                            UnboundApi.deleteContext(context);
                            UnboundResolver.logger.debug(secureMessage);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        UnboundApi.deleteContext(context);
                        if (UnboundResolver.logger.isDebugEnabled() && secureMessage != null) {
                            UnboundResolver.logger.debug(secureMessage);
                        }
                        throw th;
                    }
                }
            }).get((long) this.timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error(e);
            throw new IOException(e.getMessage());
        } catch (ExecutionException e2) {
            if (e2.getCause() instanceof DnssecRuntimeException) {
                throw new DnssecRuntimeException(e2.getCause().getMessage());
            }
            logger.error(e2);
            throw new IOException(e2.getMessage());
        } catch (TimeoutException e3) {
            throw new SocketTimeoutException(e3.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public void validateMessage(SecureMessage msg) throws DnssecRuntimeException {
    }

    /* access modifiers changed from: private */
    public long prepareContext() {
        long context = UnboundApi.createContext();
        if (logger.isTraceEnabled()) {
            UnboundApi.setDebugLevel(context, 100);
        }
        for (String fwd : this.forwarders == null ? ResolverConfig.getCurrentConfig().servers() : this.forwarders) {
            String fwd2 = fwd2.trim();
            if (NetworkUtils.isValidIPAddress(fwd2)) {
                if (fwd2.startsWith("[")) {
                    fwd2 = fwd2.substring(1, fwd2.length() - 1);
                }
                UnboundApi.setForwarder(context, fwd2);
            }
        }
        for (String anchor : this.trustAnchors) {
            UnboundApi.addTrustAnchor(context, anchor);
        }
        return context;
    }

    /* access modifiers changed from: private|static|declared_synchronized */
    public static synchronized void deleteContext(CallbackData cbData, boolean cancelAsync) {
        synchronized (UnboundResolver.class) {
            if (cbData.context != 0) {
                if (cancelAsync) {
                    try {
                        UnboundApi.cancelAsync(cbData.context, cbData.asyncId);
                    } catch (UnboundException e) {
                    }
                }
                UnboundApi.deleteContext(cbData.context);
                cbData.context = 0;
            }
        }
    }

    public CallbackData sendAsync(Message query, ResolverListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        final long context = prepareContext();
        final CallbackData cbData = new CallbackData();
        cbData.listener = listener;
        cbData.context = context;
        try {
            cbData.asyncId = UnboundApi.resolveAsync(context, query.getQuestion().getName().toString(), query.getQuestion().getType(), query.getQuestion().getDClass(), cbData, new UnboundCallback() {
                public void UnboundResolveCallback(Object data, int err, UnboundResult result) {
                    CallbackData cbData = (CallbackData) data;
                    UnboundResolver.deleteContext(cbData, false);
                    ResolverListener l = cbData.listener;
                    if (err == 0) {
                        try {
                            l.receiveMessage(data, new SecureMessage(result));
                        } catch (IOException e) {
                            l.handleException(data, e);
                        }
                    } else {
                        l.handleException(data, new Exception(UnboundApi.errorCodeToString(err)));
                    }
                    cbData.sync.countDown();
                }
            });
            this.threadPool.execute(new Runnable() {
                public void run() {
                    try {
                        UnboundApi.processAsync(context);
                    } catch (UnboundException ex) {
                        cbData.listener.handleException(this, ex);
                        UnboundResolver.deleteContext(cbData, false);
                        cbData.sync.countDown();
                    }
                }
            });
            return cbData;
        } catch (UnboundException e) {
            listener.handleException(null, e);
            return null;
        }
    }

    public void setEDNS(int level) {
        throw new UnsupportedOperationException();
    }

    public void setEDNS(int level, int payloadSize, int flags, List options) {
        throw new UnsupportedOperationException();
    }

    public void setIgnoreTruncation(boolean flag) {
        throw new UnsupportedOperationException();
    }

    public void setPort(int port) {
        throw new UnsupportedOperationException();
    }

    public void setTCP(boolean flag) {
        throw new UnsupportedOperationException();
    }

    public void setTSIGKey(TSIG key) {
        throw new UnsupportedOperationException();
    }

    public void setTimeout(int secs) {
        this.timeout = secs * 1000;
    }

    public void setTimeout(int secs, int msecs) {
        this.timeout = (secs * 1000) + msecs;
    }

    public void reset() {
    }
}
