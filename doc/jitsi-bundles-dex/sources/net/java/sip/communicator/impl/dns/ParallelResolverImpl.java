package net.java.sip.communicator.impl.dns;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.java.sip.communicator.service.dns.CustomResolver;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.NetworkUtils;
import org.jitsi.gov.nist.core.Separators;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Message;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ResolverListener;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.Type;

public class ParallelResolverImpl implements CustomResolver, PropertyChangeListener {
    private static long currentDnsPatience = 1500;
    public static int currentDnsRedemption = 3;
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(ParallelResolverImpl.class);
    private static final Object redemptionLock = new Object();
    private static int redemptionStatus = 0;
    private static volatile boolean redundantMode = false;
    /* access modifiers changed from: private */
    public ExecutorService backupQueriesPool = Executors.newCachedThreadPool();
    /* access modifiers changed from: private */
    public ExtendedResolver backupResolver;
    private final Set<String> configNames = new HashSet<String>(5) {
    };
    /* access modifiers changed from: private */
    public Resolver defaultResolver;

    private class ParallelResolution implements Runnable {
        /* access modifiers changed from: private|volatile */
        public volatile boolean done = false;
        /* access modifiers changed from: private */
        public Throwable exception;
        /* access modifiers changed from: private|volatile */
        public volatile boolean primaryResolverRespondedFirst = true;
        /* access modifiers changed from: private|final */
        public final Message query;
        /* access modifiers changed from: private|volatile */
        public volatile Message response;

        public ParallelResolution(Message query) {
            this.query = query;
        }

        public void sendFirstQuery() {
            ParallelResolverImpl.this.backupQueriesPool.execute(this);
        }

        public void run() {
            Message localResponse = null;
            try {
                localResponse = ParallelResolverImpl.this.defaultResolver.send(this.query);
            } catch (SocketTimeoutException exc) {
                ParallelResolverImpl.logger.info("Default DNS resolver timed out.");
                this.exception = exc;
            } catch (Throwable exc2) {
                ParallelResolverImpl.logger.info("Default DNS resolver failed", exc2);
                this.exception = exc2;
            }
            if (!this.done) {
                synchronized (this) {
                    if (localResponse != null) {
                        if (ParallelResolverImpl.this.isResponseSatisfactory(localResponse)) {
                            this.response = localResponse;
                            this.done = true;
                        }
                    }
                    notify();
                }
            }
        }

        public void sendBackupQueries() {
            ParallelResolverImpl.this.backupQueriesPool.execute(new Runnable() {
                public void run() {
                    if (!ParallelResolution.this.done) {
                        Message localResponse = null;
                        try {
                            ParallelResolverImpl.logger.info("Sending query for " + ParallelResolution.this.query.getQuestion().getName() + Separators.SLASH + Type.string(ParallelResolution.this.query.getQuestion().getType()) + " to backup resolvers");
                            localResponse = ParallelResolverImpl.this.backupResolver.send(ParallelResolution.this.query);
                        } catch (Throwable exc) {
                            ParallelResolverImpl.logger.info("Exception occurred during backup DNS resolving" + exc);
                            ParallelResolution.this.exception = exc;
                        }
                        if (!ParallelResolution.this.done) {
                            synchronized (ParallelResolution.this) {
                                if (ParallelResolution.this.response == null) {
                                    ParallelResolution.this.response = localResponse;
                                    ParallelResolution.this.primaryResolverRespondedFirst = false;
                                }
                                ParallelResolution.this.done = true;
                                ParallelResolution.this.notify();
                            }
                        }
                    }
                }
            });
        }

        public boolean waitForResponse(long waitFor) {
            boolean z;
            synchronized (this) {
                if (this.done) {
                    z = this.done;
                } else {
                    try {
                        wait(waitFor);
                    } catch (InterruptedException e) {
                    }
                    z = this.done;
                }
            }
            return z;
        }

        public Message returnResponseOrThrowUp() throws IOException, RuntimeException, IllegalArgumentException {
            if (!this.done) {
                waitForResponse(0);
            }
            if (this.response != null) {
                return this.response;
            }
            if (this.exception instanceof SocketTimeoutException) {
                ParallelResolverImpl.logger.warn("DNS resolver timed out");
                throw ((IOException) this.exception);
            } else if (this.exception instanceof IOException) {
                ParallelResolverImpl.logger.warn("IO exception while using DNS resolver", this.exception);
                throw ((IOException) this.exception);
            } else if (this.exception instanceof RuntimeException) {
                ParallelResolverImpl.logger.warn("RunTimeException while using DNS resolver", this.exception);
                throw ((RuntimeException) this.exception);
            } else if (this.exception instanceof Error) {
                ParallelResolverImpl.logger.warn("Error while using DNS resolver", this.exception);
                throw ((Error) this.exception);
            } else {
                ParallelResolverImpl.logger.warn("Received a bad response from primary DNS resolver", this.exception);
                throw new IllegalStateException("ExtendedResolver failure");
            }
        }
    }

    ParallelResolverImpl() {
        DnsUtilActivator.getConfigurationService().addPropertyChangeListener(this);
        initProperties();
        reset();
    }

    private void initProperties() {
        String rslvrAddrStr = DnsUtilActivator.getConfigurationService().getString(DnsUtilActivator.PNAME_BACKUP_RESOLVER, DnsUtilActivator.DEFAULT_BACKUP_RESOLVER);
        String customResolverIP = DnsUtilActivator.getConfigurationService().getString(DnsUtilActivator.PNAME_BACKUP_RESOLVER_FALLBACK_IP, DnsUtilActivator.getResources().getSettingsString(DnsUtilActivator.PNAME_BACKUP_RESOLVER_FALLBACK_IP));
        InetAddress resolverAddress = null;
        try {
            resolverAddress = NetworkUtils.getInetAddress(rslvrAddrStr);
        } catch (UnknownHostException e) {
            logger.warn("Seems like the primary DNS is down, trying fallback to " + customResolverIP);
        }
        if (resolverAddress == null) {
            try {
                resolverAddress = NetworkUtils.getInetAddress(customResolverIP);
            } catch (UnknownHostException e2) {
                logger.error(e2);
            }
        }
        setBackupServers(new InetSocketAddress[]{new InetSocketAddress(resolverAddress, DnsUtilActivator.getConfigurationService().getInt(DnsUtilActivator.PNAME_BACKUP_RESOLVER_PORT, 53))});
        currentDnsPatience = DnsUtilActivator.getConfigurationService().getLong("net.java.sip.communicator.util.dns.DNS_PATIENCE", 1500);
        currentDnsRedemption = DnsUtilActivator.getConfigurationService().getInt("net.java.sip.communicator.util.dns.DNS_REDEMPTION", 3);
    }

    private void setBackupServers(InetSocketAddress[] backupServers) {
        try {
            this.backupResolver = new ExtendedResolver(new SimpleResolver[0]);
            for (InetSocketAddress backupServer : backupServers) {
                SimpleResolver sr = new SimpleResolver();
                sr.setAddress(backupServer);
                this.backupResolver.addResolver(sr);
            }
        } catch (UnknownHostException e) {
            throw new IllegalStateException("The impossible just happened: we could not initialize our backup DNS resolver");
        }
    }

    public Message send(Message query) throws IOException {
        ParallelResolution resolution = new ParallelResolution(query);
        resolution.sendFirstQuery();
        if (!redundantMode) {
            if (resolution.waitForResponse(currentDnsPatience)) {
                return resolution.returnResponseOrThrowUp();
            }
            synchronized (redemptionLock) {
                redundantMode = true;
                redemptionStatus = currentDnsRedemption;
                logger.info("Primary DNS seems laggy: no response for " + query.getQuestion().getName() + Separators.SLASH + Type.string(query.getQuestion().getType()) + " after " + currentDnsPatience + "ms. " + "Enabling redundant mode.");
            }
        }
        resolution.sendBackupQueries();
        resolution.waitForResponse(0);
        synchronized (redemptionLock) {
            if (resolution.primaryResolverRespondedFirst) {
                redemptionStatus--;
                if (redemptionStatus <= 0) {
                    redundantMode = false;
                    logger.info("Primary DNS seems back in biz. Disabling redundant mode.");
                }
            } else {
                redemptionStatus = currentDnsRedemption;
            }
        }
        return resolution.returnResponseOrThrowUp();
    }

    public Object sendAsync(Message query, ResolverListener listener) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setPort(int port) {
        this.defaultResolver.setPort(port);
    }

    public void setTCP(boolean flag) {
        this.defaultResolver.setTCP(flag);
    }

    public void setIgnoreTruncation(boolean flag) {
        this.defaultResolver.setIgnoreTruncation(flag);
    }

    public void setEDNS(int level) {
        this.defaultResolver.setEDNS(level);
    }

    public void setEDNS(int level, int payloadSize, int flags, List options) {
        this.defaultResolver.setEDNS(level, payloadSize, flags, options);
    }

    public void setTSIGKey(TSIG key) {
        this.defaultResolver.setTSIGKey(key);
    }

    public void setTimeout(int secs, int msecs) {
        this.defaultResolver.setTimeout(secs, msecs);
    }

    public void setTimeout(int secs) {
        this.defaultResolver.setTimeout(secs);
    }

    public final void reset() {
        Lookup.refreshDefault();
        try {
            Lookup.setDefaultResolver(this);
            ExtendedResolver temp = new ExtendedResolver();
            temp.setTimeout(10);
            this.defaultResolver = temp;
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to initialize resolver");
        }
    }

    /* access modifiers changed from: private */
    public boolean isResponseSatisfactory(Message response) {
        if (response == null) {
            return false;
        }
        Record[] answerRR = response.getSectionArray(1);
        Record[] authorityRR = response.getSectionArray(2);
        Record[] additionalRR = response.getSectionArray(3);
        if ((answerRR != null && answerRR.length > 0) || ((authorityRR != null && authorityRR.length > 0) || (additionalRR != null && additionalRR.length > 0))) {
            return true;
        }
        if (response.getRcode() == 3) {
            return true;
        }
        if (response.getRcode() != 0) {
            return false;
        }
        if (response.getQuestion().getType() == 28 || response.getQuestion().getType() == 35) {
            return true;
        }
        return false;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (this.configNames.contains(evt.getPropertyName())) {
            initProperties();
        }
    }
}
