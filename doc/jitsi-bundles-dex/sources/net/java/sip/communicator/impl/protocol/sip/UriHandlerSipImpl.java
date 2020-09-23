package net.java.sip.communicator.impl.protocol.sip;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import net.java.sip.communicator.service.argdelegation.UriHandler;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.AccountManager;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.ProtocolProviderFactory;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.AccountManagerEvent;
import net.java.sip.communicator.service.protocol.event.AccountManagerListener;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.util.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceRegistration;

public class UriHandlerSipImpl implements UriHandler, ServiceListener, AccountManagerListener {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(UriHandlerSipImpl.class);
    private AccountManager accountManager;
    private ServiceRegistration ourServiceRegistration = null;
    private final ProtocolProviderFactory protoFactory;
    private final Object registrationLock = new Object();
    private final boolean[] storedAccountsAreLoaded = new boolean[1];
    private List<String> uris;

    private class ProtocolRegistrationThread extends Thread implements RegistrationStateChangeListener {
        private ProtocolProviderService handlerProvider = null;
        /* access modifiers changed from: private */
        public String uri = null;

        public ProtocolRegistrationThread(String uri, ProtocolProviderService handlerProvider) {
            super("UriHandlerProviderRegistrationThread:uri=" + uri);
            this.uri = uri;
            this.handlerProvider = handlerProvider;
        }

        public void run() {
            this.handlerProvider.addRegistrationStateChangeListener(this);
            try {
                this.handlerProvider.register(SipActivator.getUIService().getDefaultSecurityAuthority(this.handlerProvider));
            } catch (OperationFailedException exc) {
                UriHandlerSipImpl.logger.error("Failed to manually register provider.");
                UriHandlerSipImpl.logger.warn(exc.getMessage(), exc);
            }
        }

        public void registrationStateChanged(RegistrationStateChangeEvent evt) {
            if (evt.getNewState() == RegistrationState.REGISTERED) {
                Thread uriRehandleThread = new Thread() {
                    public void run() {
                        UriHandlerSipImpl.this.handleUri(ProtocolRegistrationThread.this.uri);
                    }
                };
                uriRehandleThread.setName("UriRehandleThread:uri=" + this.uri);
                uriRehandleThread.start();
            }
            if (evt.getNewState() != RegistrationState.REGISTERING) {
                this.handlerProvider.removeRegistrationStateChangeListener(this);
            }
        }
    }

    private static class ProviderComboBoxEntry {
        public final ProtocolProviderService provider;

        public ProviderComboBoxEntry(ProtocolProviderService provider) {
            this.provider = provider;
        }

        public String toString() {
            return this.provider.getAccountID().getAccountAddress();
        }
    }

    public UriHandlerSipImpl(ProtocolProviderFactorySipImpl protoFactory) throws NullPointerException {
        if (protoFactory == null) {
            throw new NullPointerException("The ProtocolProviderFactory that a UriHandler is created with  cannot be null.");
        }
        this.protoFactory = protoFactory;
        hookStoredAccounts();
        this.protoFactory.getBundleContext().addServiceListener(this);
        registerHandlerService();
    }

    public void dispose() {
        this.protoFactory.getBundleContext().removeServiceListener(this);
        unregisterHandlerService();
        unhookStoredAccounts();
    }

    private void hookStoredAccounts() {
        if (this.accountManager == null) {
            BundleContext bundleContext = this.protoFactory.getBundleContext();
            this.accountManager = (AccountManager) bundleContext.getService(bundleContext.getServiceReference(AccountManager.class.getName()));
            this.accountManager.addListener(this);
        }
    }

    private void unhookStoredAccounts() {
        if (this.accountManager != null) {
            this.accountManager.removeListener(this);
            this.accountManager = null;
        }
    }

    public void handleAccountManagerEvent(AccountManagerEvent event) {
        if (1 == event.getType() && this.protoFactory == event.getFactory()) {
            List<String> uris = null;
            synchronized (this.storedAccountsAreLoaded) {
                this.storedAccountsAreLoaded[0] = true;
                if (this.uris != null) {
                    uris = this.uris;
                    this.uris = null;
                }
            }
            unhookStoredAccounts();
            if (uris != null) {
                for (String handleUri : uris) {
                    handleUri(handleUri);
                }
            }
        }
    }

    public void registerHandlerService() {
        synchronized (this.registrationLock) {
            if (this.ourServiceRegistration != null) {
                return;
            }
            Hashtable<String, String> registrationProperties = new Hashtable();
            registrationProperties.put(UriHandler.PROTOCOL_PROPERTY, getProtocol());
            this.ourServiceRegistration = SipActivator.bundleContext.registerService(UriHandler.class.getName(), this, registrationProperties);
        }
    }

    public void unregisterHandlerService() {
        synchronized (this.registrationLock) {
            if (this.ourServiceRegistration != null) {
                this.ourServiceRegistration.unregister();
                this.ourServiceRegistration = null;
            }
        }
    }

    public String getProtocol() {
        return "sip";
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* JADX WARNING: Missing block: B:12:?, code skipped:
            r1 = selectHandlingProvider(r7);
     */
    /* JADX WARNING: Missing block: B:13:0x0021, code skipped:
            if (r1 != null) goto L_0x0060;
     */
    /* JADX WARNING: Missing block: B:14:0x0023, code skipped:
            showErrorMessage("You need to configure at least one SIP account \nto be able to call " + r7, null);
     */
    /* JADX WARNING: Missing block: B:21:0x0045, code skipped:
            if (logger.isTraceEnabled() != false) goto L_0x0047;
     */
    /* JADX WARNING: Missing block: B:22:0x0047, code skipped:
            logger.trace("User canceled handling of uri " + r7);
     */
    /* JADX WARNING: Missing block: B:23:0x0060, code skipped:
            if (r7 == null) goto L_0x006a;
     */
    /* JADX WARNING: Missing block: B:24:0x0062, code skipped:
            r7 = r7.replace("sip://", "sip:");
     */
    /* JADX WARNING: Missing block: B:27:?, code skipped:
            ((net.java.sip.communicator.service.protocol.OperationSetBasicTelephony) r1.getOperationSet(net.java.sip.communicator.service.protocol.OperationSetBasicTelephony.class)).createCall(r7);
     */
    /* JADX WARNING: Missing block: B:28:0x0076, code skipped:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:30:0x007c, code skipped:
            if (r0.getErrorCode() == 3) goto L_0x007e;
     */
    /* JADX WARNING: Missing block: B:31:0x007e, code skipped:
            promptForRegistration(r7, r1);
     */
    /* JADX WARNING: Missing block: B:32:0x0081, code skipped:
            showErrorMessage("Failed to create a call to " + r7, r0);
     */
    /* JADX WARNING: Missing block: B:33:0x0098, code skipped:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:34:0x0099, code skipped:
            showErrorMessage(r7 + " does not appear to be a valid SIP address", r0);
     */
    /* JADX WARNING: Missing block: B:39:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:40:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:41:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:42:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:43:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:44:?, code skipped:
            return;
     */
    public void handleUri(java.lang.String r7) {
        /*
        r6 = this;
        r4 = r6.storedAccountsAreLoaded;
        monitor-enter(r4);
        r3 = r6.storedAccountsAreLoaded;	 Catch:{ all -> 0x003b }
        r5 = 0;
        r3 = r3[r5];	 Catch:{ all -> 0x003b }
        if (r3 != 0) goto L_0x001c;
    L_0x000a:
        r3 = r6.uris;	 Catch:{ all -> 0x003b }
        if (r3 != 0) goto L_0x0015;
    L_0x000e:
        r3 = new java.util.LinkedList;	 Catch:{ all -> 0x003b }
        r3.<init>();	 Catch:{ all -> 0x003b }
        r6.uris = r3;	 Catch:{ all -> 0x003b }
    L_0x0015:
        r3 = r6.uris;	 Catch:{ all -> 0x003b }
        r3.add(r7);	 Catch:{ all -> 0x003b }
        monitor-exit(r4);	 Catch:{ all -> 0x003b }
    L_0x001b:
        return;
    L_0x001c:
        monitor-exit(r4);	 Catch:{ all -> 0x003b }
        r1 = r6.selectHandlingProvider(r7);	 Catch:{ OperationFailedException -> 0x003e }
        if (r1 != 0) goto L_0x0060;
    L_0x0023:
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "You need to configure at least one SIP account \nto be able to call ";
        r3 = r3.append(r4);
        r3 = r3.append(r7);
        r3 = r3.toString();
        r4 = 0;
        r6.showErrorMessage(r3, r4);
        goto L_0x001b;
    L_0x003b:
        r3 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x003b }
        throw r3;
    L_0x003e:
        r0 = move-exception;
        r3 = logger;
        r3 = r3.isTraceEnabled();
        if (r3 == 0) goto L_0x001b;
    L_0x0047:
        r3 = logger;
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "User canceled handling of uri ";
        r4 = r4.append(r5);
        r4 = r4.append(r7);
        r4 = r4.toString();
        r3.trace(r4);
        goto L_0x001b;
    L_0x0060:
        if (r7 == 0) goto L_0x006a;
    L_0x0062:
        r3 = "sip://";
        r4 = "sip:";
        r7 = r7.replace(r3, r4);
    L_0x006a:
        r3 = net.java.sip.communicator.service.protocol.OperationSetBasicTelephony.class;
        r2 = r1.getOperationSet(r3);
        r2 = (net.java.sip.communicator.service.protocol.OperationSetBasicTelephony) r2;
        r2.createCall(r7);	 Catch:{ OperationFailedException -> 0x0076, ParseException -> 0x0098 }
        goto L_0x001b;
    L_0x0076:
        r0 = move-exception;
        r3 = r0.getErrorCode();
        r4 = 3;
        if (r3 != r4) goto L_0x0081;
    L_0x007e:
        r6.promptForRegistration(r7, r1);
    L_0x0081:
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "Failed to create a call to ";
        r3 = r3.append(r4);
        r3 = r3.append(r7);
        r3 = r3.toString();
        r6.showErrorMessage(r3, r0);
        goto L_0x001b;
    L_0x0098:
        r0 = move-exception;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r3 = r3.append(r7);
        r4 = " does not appear to be a valid SIP address";
        r3 = r3.append(r4);
        r3 = r3.toString();
        r6.showErrorMessage(r3, r0);
        goto L_0x001b;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.sip.UriHandlerSipImpl.handleUri(java.lang.String):void");
    }

    private void promptForRegistration(String uri, ProtocolProviderService provider) {
        if (SipActivator.getUIService().getPopupDialog().showConfirmPopupDialog("You need to be online in order to make a call and your account is currently offline. Do want to connect now?", "Account is currently offline", 0) == 0) {
            new ProtocolRegistrationThread(uri, provider).start();
        }
    }

    public void serviceChanged(ServiceEvent event) {
        if (SipActivator.bundleContext.getService(event.getServiceReference()) == this.protoFactory) {
            switch (event.getType()) {
                case 1:
                    registerHandlerService();
                    return;
                case 4:
                    unregisterHandlerService();
                    return;
                default:
                    return;
            }
        }
    }

    private void showErrorMessage(String message, Exception exc) {
        SipActivator.getUIService().getPopupDialog().showMessagePopupDialog(message, "Failed to create call!", 0);
        logger.error(message, exc);
    }

    public ProtocolProviderService selectHandlingProvider(String uri) throws OperationFailedException {
        ArrayList<AccountID> registeredAccounts = this.protoFactory.getRegisteredAccounts();
        if (registeredAccounts.size() == 0) {
            return null;
        }
        if (registeredAccounts.size() == 1) {
            return (ProtocolProviderService) SipActivator.getBundleContext().getService(this.protoFactory.getProviderForAccount((AccountID) registeredAccounts.get(0)));
        }
        ArrayList<ProviderComboBoxEntry> providers = new ArrayList();
        Iterator i$ = registeredAccounts.iterator();
        while (i$.hasNext()) {
            providers.add(new ProviderComboBoxEntry((ProtocolProviderService) SipActivator.getBundleContext().getService(this.protoFactory.getProviderForAccount((AccountID) i$.next()))));
        }
        Object result = SipActivator.getUIService().getPopupDialog().showInputPopupDialog("Please select the account that you would like \nto use to call " + uri, "Account Selection", 2, providers.toArray(), providers.get(0));
        if (result != null) {
            return ((ProviderComboBoxEntry) result).provider;
        }
        throw new OperationFailedException("Operation cancelled", 16);
    }
}
