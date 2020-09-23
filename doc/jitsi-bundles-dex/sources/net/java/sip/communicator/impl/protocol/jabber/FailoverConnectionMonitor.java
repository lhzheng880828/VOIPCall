package net.java.sip.communicator.impl.protocol.jabber;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.SRVRecord;

public class FailoverConnectionMonitor implements RegistrationStateChangeListener {
    private static int CHECK_FOR_PRIMARY_UP_INTERVAL = 60000;
    public static final String FAILOVER_CHECK_INTERVAL_PROP = "net.java.sip.communicator.impl.protocol.jabber.FAILOVER_CHECK_INTERVAL";
    public static final String REVERSE_FAILOVER_ENABLED_PROP = "net.java.sip.communicator.impl.protocol.jabber.REVERSE_FAILOVER_ENABLED";
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(FailoverConnectionMonitor.class);
    private static Hashtable<ProtocolProviderServiceJabberImpl, FailoverConnectionMonitor> providerFilovers = new Hashtable();
    private Timer checkTimer;
    private String currentAddress;
    /* access modifiers changed from: private */
    public ProtocolProviderServiceJabberImpl parentProvider;
    /* access modifiers changed from: private */
    public String serviceName;
    private CheckPrimaryTask task;

    private class CheckPrimaryTask extends TimerTask {
        private CheckPrimaryTask() {
        }

        /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
        public void run() {
            /*
            r9 = this;
            r5 = "xmpp-client";
            r6 = "tcp";
            r7 = net.java.sip.communicator.impl.protocol.jabber.FailoverConnectionMonitor.this;	 Catch:{ Throwable -> 0x005e }
            r7 = r7.serviceName;	 Catch:{ Throwable -> 0x005e }
            r8 = 0;
            r2 = net.java.sip.communicator.util.NetworkUtils.getSRVRecords(r5, r6, r7, r8);	 Catch:{ Throwable -> 0x005e }
            r5 = net.java.sip.communicator.impl.protocol.jabber.FailoverConnectionMonitor.this;	 Catch:{ Throwable -> 0x005e }
            r5 = r5.isConnectedToPrimary(r2);	 Catch:{ Throwable -> 0x005e }
            if (r5 == 0) goto L_0x0018;
        L_0x0017:
            return;
        L_0x0018:
            net.java.sip.communicator.util.NetworkUtils.clearDefaultDNSCache();	 Catch:{ Throwable -> 0x005e }
            r5 = net.java.sip.communicator.impl.protocol.jabber.FailoverConnectionMonitor.this;	 Catch:{ Throwable -> 0x005e }
            r3 = r5.getPrimaryServerRecord(r2);	 Catch:{ Throwable -> 0x005e }
            r0 = new org.jivesoftware.smack.ConnectionConfiguration;	 Catch:{ Throwable -> 0x005e }
            r5 = r3.getTarget();	 Catch:{ Throwable -> 0x005e }
            r6 = r3.getPort();	 Catch:{ Throwable -> 0x005e }
            r0.m1692init(r5, r6);	 Catch:{ Throwable -> 0x005e }
            r5 = 0;
            r0.setReconnectionAllowed(r5);	 Catch:{ Throwable -> 0x005e }
            r1 = new org.jivesoftware.smack.XMPPConnection;	 Catch:{ Throwable -> 0x005e }
            r1.m1741init(r0);	 Catch:{ Throwable -> 0x005e }
            r1.connect();	 Catch:{ Throwable -> 0x005e }
            r1.disconnect();	 Catch:{ Throwable -> 0x005e }
            r5 = net.java.sip.communicator.impl.protocol.jabber.FailoverConnectionMonitor.this;	 Catch:{ Throwable -> 0x0060 }
            r5 = r5.parentProvider;	 Catch:{ Throwable -> 0x0060 }
            r5.unregister();	 Catch:{ Throwable -> 0x0060 }
        L_0x0046:
            r5 = net.java.sip.communicator.impl.protocol.jabber.FailoverConnectionMonitor.this;	 Catch:{ Throwable -> 0x005e }
            r5 = r5.parentProvider;	 Catch:{ Throwable -> 0x005e }
            r6 = net.java.sip.communicator.impl.protocol.jabber.JabberActivator.getUIService();	 Catch:{ Throwable -> 0x005e }
            r7 = net.java.sip.communicator.impl.protocol.jabber.FailoverConnectionMonitor.this;	 Catch:{ Throwable -> 0x005e }
            r7 = r7.parentProvider;	 Catch:{ Throwable -> 0x005e }
            r6 = r6.getDefaultSecurityAuthority(r7);	 Catch:{ Throwable -> 0x005e }
            r5.register(r6);	 Catch:{ Throwable -> 0x005e }
            goto L_0x0017;
        L_0x005e:
            r5 = move-exception;
            goto L_0x0017;
        L_0x0060:
            r4 = move-exception;
            r5 = net.java.sip.communicator.impl.protocol.jabber.FailoverConnectionMonitor.logger;	 Catch:{ Throwable -> 0x005e }
            r6 = "Error un-registering before connecting to primary";
            r5.error(r6, r4);	 Catch:{ Throwable -> 0x005e }
            goto L_0x0046;
            */
            throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.jabber.FailoverConnectionMonitor$CheckPrimaryTask.run():void");
        }
    }

    private FailoverConnectionMonitor(ProtocolProviderServiceJabberImpl provider) {
        this.parentProvider = provider;
        this.parentProvider.addRegistrationStateChangeListener(this);
        CHECK_FOR_PRIMARY_UP_INTERVAL = JabberActivator.getConfigurationService().getInt(FAILOVER_CHECK_INTERVAL_PROP, CHECK_FOR_PRIMARY_UP_INTERVAL);
    }

    public static FailoverConnectionMonitor getInstance(ProtocolProviderServiceJabberImpl provider) {
        FailoverConnectionMonitor fov;
        synchronized (providerFilovers) {
            fov = (FailoverConnectionMonitor) providerFilovers.get(provider);
            if (fov == null) {
                fov = new FailoverConnectionMonitor(provider);
                providerFilovers.put(provider, fov);
            }
        }
        return fov;
    }

    /* access modifiers changed from: 0000 */
    public void setCurrent(String serviceName, String currentAddress) {
        this.currentAddress = currentAddress;
        this.serviceName = serviceName;
    }

    /* access modifiers changed from: private */
    public boolean isConnectedToPrimary(SRVRecord[] recs) {
        String primaryAddress = getPrimaryServerRecord(recs).getTarget();
        if (primaryAddress == null || !primaryAddress.equals(this.currentAddress)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public SRVRecord getPrimaryServerRecord(SRVRecord[] recs) {
        if (recs.length < 1) {
            return null;
        }
        SRVRecord primary = recs[0];
        for (SRVRecord srv : recs) {
            if (srv.getPriority() < primary.getPriority()) {
                primary = srv;
            }
        }
        return primary;
    }

    public void registrationStateChanged(RegistrationStateChangeEvent evt) {
        if (evt.getNewState() == RegistrationState.REGISTERED) {
            if (this.checkTimer == null) {
                this.checkTimer = new Timer(FailoverConnectionMonitor.class.getName(), true);
            }
            if (this.task == null) {
                this.task = new CheckPrimaryTask();
            }
            this.checkTimer.schedule(this.task, (long) CHECK_FOR_PRIMARY_UP_INTERVAL, (long) CHECK_FOR_PRIMARY_UP_INTERVAL);
        } else if (evt.getNewState() == RegistrationState.UNREGISTERED || evt.getNewState() == RegistrationState.AUTHENTICATION_FAILED || evt.getNewState() == RegistrationState.CONNECTION_FAILED) {
            synchronized (providerFilovers) {
                providerFilovers.remove(this.parentProvider);
                this.parentProvider.removeRegistrationStateChangeListener(this);
            }
            if (this.checkTimer != null) {
                this.checkTimer.cancel();
                this.checkTimer = null;
            }
            if (this.task != null) {
                this.task.cancel();
                this.task = null;
            }
        }
    }
}
