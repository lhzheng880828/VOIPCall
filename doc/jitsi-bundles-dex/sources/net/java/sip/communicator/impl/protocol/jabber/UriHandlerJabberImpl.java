package net.java.sip.communicator.impl.protocol.jabber;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import net.java.sip.communicator.service.argdelegation.UriHandler;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.AccountManager;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetPresence;
import net.java.sip.communicator.service.protocol.ProtocolProviderFactory;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.AccountManagerEvent;
import net.java.sip.communicator.service.protocol.event.AccountManagerListener;
import net.java.sip.communicator.service.protocol.event.ProviderPresenceStatusChangeEvent;
import net.java.sip.communicator.service.protocol.event.ProviderPresenceStatusListener;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.util.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceRegistration;

public class UriHandlerJabberImpl implements UriHandler, ServiceListener, AccountManagerListener {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(UriHandlerJabberImpl.class);
    private AccountManager accountManager;
    private boolean networkFailReceived = false;
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
                this.handlerProvider.register(JabberActivator.getUIService().getDefaultSecurityAuthority(this.handlerProvider));
            } catch (OperationFailedException exc) {
                UriHandlerJabberImpl.logger.error("Failed to manually register provider.");
                UriHandlerJabberImpl.logger.warn(exc.getMessage(), exc);
            }
        }

        public void registrationStateChanged(RegistrationStateChangeEvent evt) {
            if (evt.getNewState() == RegistrationState.REGISTERED) {
                Thread uriRehandleThread = new Thread() {
                    public void run() {
                        UriHandlerJabberImpl.this.handleUri(ProtocolRegistrationThread.this.uri);
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

    private class ProviderStatusListener implements ProviderPresenceStatusListener {
        private final OperationSetPresence parentOpSet;
        private final String uri;

        public ProviderStatusListener(String uri, OperationSetPresence parentOpSet) {
            this.uri = uri;
            this.parentOpSet = parentOpSet;
        }

        public void providerStatusChanged(ProviderPresenceStatusChangeEvent ev) {
            if (ev.getNewStatus().isOnline()) {
                this.parentOpSet.removeProviderPresenceStatusListener(this);
                UriHandlerJabberImpl.this.handleUri(this.uri);
            }
        }

        public void providerStatusMessageChanged(PropertyChangeEvent ev) {
        }
    }

    public UriHandlerJabberImpl(ProtocolProviderFactory protoFactory) throws NullPointerException {
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
            this.ourServiceRegistration = JabberActivator.bundleContext.registerService(UriHandler.class.getName(), this, registrationProperties);
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
        return "xmpp";
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* JADX WARNING: Missing block: B:13:?, code skipped:
            r12 = selectHandlingProvider(r20);
     */
    /* JADX WARNING: Missing block: B:14:0x0030, code skipped:
            if (r12 != null) goto L_0x007a;
     */
    /* JADX WARNING: Missing block: B:15:0x0032, code skipped:
            showErrorMessage("You need to configure at least one XMPP account \nto be able to call " + r20, null);
     */
    /* JADX WARNING: Missing block: B:22:0x005b, code skipped:
            if (logger.isTraceEnabled() != false) goto L_0x005d;
     */
    /* JADX WARNING: Missing block: B:23:0x005d, code skipped:
            logger.trace("User canceled handling of uri " + r20);
     */
    /* JADX WARNING: Missing block: B:25:0x0082, code skipped:
            if (r20.contains(org.jitsi.gov.nist.core.Separators.QUESTION) != false) goto L_0x0148;
     */
    /* JADX WARNING: Missing block: B:26:0x0084, code skipped:
            r11 = (net.java.sip.communicator.service.protocol.OperationSetPersistentPresence) r12.getOperationSet(net.java.sip.communicator.service.protocol.OperationSetPersistentPresence.class);
            r3 = r20.replaceFirst(getProtocol() + org.jitsi.gov.nist.core.Separators.COLON, "");
     */
    /* JADX WARNING: Missing block: B:27:0x00bb, code skipped:
            if (java.util.regex.Pattern.compile(".+@.+\\.[a-z]+").matcher(r3).matches() != false) goto L_0x00dd;
     */
    /* JADX WARNING: Missing block: B:28:0x00bd, code skipped:
            showErrorMessage("Wrong contact id : " + r20, null);
     */
    /* JADX WARNING: Missing block: B:29:0x00dd, code skipped:
            r2 = r11.findContactByID(r3);
     */
    /* JADX WARNING: Missing block: B:30:0x00e1, code skipped:
            if (r2 != null) goto L_0x0139;
     */
    /* JADX WARNING: Missing block: B:32:0x011b, code skipped:
            if (java.lang.Integer.valueOf(net.java.sip.communicator.impl.protocol.jabber.JabberActivator.getUIService().getPopupDialog().showConfirmPopupDialog("Do you want to add the contact : " + r3 + " ?", "Add contact", 0)).equals(java.lang.Integer.valueOf(0)) == false) goto L_?;
     */
    /* JADX WARNING: Missing block: B:33:0x011d, code skipped:
            net.java.sip.communicator.impl.protocol.jabber.JabberActivator.getUIService().getExportedWindow(net.java.sip.communicator.service.gui.ExportedWindow.ADD_CONTACT_WINDOW, new java.lang.String[]{r3}).setVisible(true);
     */
    /* JADX WARNING: Missing block: B:34:0x0139, code skipped:
            net.java.sip.communicator.impl.protocol.jabber.JabberActivator.getUIService().getChat(r2).setChatVisible(true);
     */
    /* JADX WARNING: Missing block: B:35:0x0148, code skipped:
            r4 = r20.replaceFirst(getProtocol() + org.jitsi.gov.nist.core.Separators.COLON, "");
            r7 = r4.indexOf(org.jitsi.gov.nist.core.Separators.QUESTION);
            r10 = r4.substring(r7 + 1, r4.length());
            r4 = r4.substring(0, r7);
     */
    /* JADX WARNING: Missing block: B:36:0x0186, code skipped:
            if (r10.equalsIgnoreCase("join") == false) goto L_0x0202;
     */
    /* JADX WARNING: Missing block: B:39:?, code skipped:
            r14 = ((net.java.sip.communicator.service.protocol.OperationSetMultiUserChat) r12.getOperationSet(net.java.sip.communicator.service.protocol.OperationSetMultiUserChat.class)).findRoom(r4);
     */
    /* JADX WARNING: Missing block: B:40:0x0194, code skipped:
            if (r14 == null) goto L_?;
     */
    /* JADX WARNING: Missing block: B:41:0x0196, code skipped:
            r14.join();
     */
    /* JADX WARNING: Missing block: B:42:0x019b, code skipped:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:44:0x01a4, code skipped:
            if (r6.getErrorCode() != 2) goto L_0x01c7;
     */
    /* JADX WARNING: Missing block: B:47:0x01ac, code skipped:
            r19.networkFailReceived = true;
            r11 = (net.java.sip.communicator.service.protocol.OperationSetPresence) r12.getOperationSet(net.java.sip.communicator.service.protocol.OperationSetPresence.class);
            r11.addProviderPresenceStatusListener(new net.java.sip.communicator.impl.protocol.jabber.UriHandlerJabberImpl.ProviderStatusListener(r19, r20, r11));
     */
    /* JADX WARNING: Missing block: B:48:0x01c7, code skipped:
            showErrorMessage("Error joining to  " + r4, r6);
     */
    /* JADX WARNING: Missing block: B:49:0x01e1, code skipped:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:50:0x01e2, code skipped:
            showErrorMessage("Join to " + r4 + ", not supported!", r6);
     */
    /* JADX WARNING: Missing block: B:51:0x0202, code skipped:
            showErrorMessage("Unknown param : " + r10, null);
     */
    /* JADX WARNING: Missing block: B:56:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:57:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:58:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:59:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:60:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:61:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:62:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:63:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:64:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:65:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:66:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:67:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:68:?, code skipped:
            return;
     */
    public void handleUri(java.lang.String r20) {
        /*
        r19 = this;
        r0 = r19;
        r0 = r0.storedAccountsAreLoaded;
        r16 = r0;
        monitor-enter(r16);
        r0 = r19;
        r15 = r0.storedAccountsAreLoaded;	 Catch:{ all -> 0x0051 }
        r17 = 0;
        r15 = r15[r17];	 Catch:{ all -> 0x0051 }
        if (r15 != 0) goto L_0x002b;
    L_0x0011:
        r0 = r19;
        r15 = r0.uris;	 Catch:{ all -> 0x0051 }
        if (r15 != 0) goto L_0x0020;
    L_0x0017:
        r15 = new java.util.LinkedList;	 Catch:{ all -> 0x0051 }
        r15.<init>();	 Catch:{ all -> 0x0051 }
        r0 = r19;
        r0.uris = r15;	 Catch:{ all -> 0x0051 }
    L_0x0020:
        r0 = r19;
        r15 = r0.uris;	 Catch:{ all -> 0x0051 }
        r0 = r20;
        r15.add(r0);	 Catch:{ all -> 0x0051 }
        monitor-exit(r16);	 Catch:{ all -> 0x0051 }
    L_0x002a:
        return;
    L_0x002b:
        monitor-exit(r16);	 Catch:{ all -> 0x0051 }
        r12 = r19.selectHandlingProvider(r20);	 Catch:{ OperationFailedException -> 0x0054 }
        if (r12 != 0) goto L_0x007a;
    L_0x0032:
        r15 = new java.lang.StringBuilder;
        r15.<init>();
        r16 = "You need to configure at least one XMPP account \nto be able to call ";
        r15 = r15.append(r16);
        r0 = r20;
        r15 = r15.append(r0);
        r15 = r15.toString();
        r16 = 0;
        r0 = r19;
        r1 = r16;
        r0.showErrorMessage(r15, r1);
        goto L_0x002a;
    L_0x0051:
        r15 = move-exception;
        monitor-exit(r16);	 Catch:{ all -> 0x0051 }
        throw r15;
    L_0x0054:
        r6 = move-exception;
        r15 = logger;
        r15 = r15.isTraceEnabled();
        if (r15 == 0) goto L_0x002a;
    L_0x005d:
        r15 = logger;
        r16 = new java.lang.StringBuilder;
        r16.<init>();
        r17 = "User canceled handling of uri ";
        r16 = r16.append(r17);
        r0 = r16;
        r1 = r20;
        r16 = r0.append(r1);
        r16 = r16.toString();
        r15.trace(r16);
        goto L_0x002a;
    L_0x007a:
        r15 = "?";
        r0 = r20;
        r15 = r0.contains(r15);
        if (r15 != 0) goto L_0x0148;
    L_0x0084:
        r15 = net.java.sip.communicator.service.protocol.OperationSetPersistentPresence.class;
        r11 = r12.getOperationSet(r15);
        r11 = (net.java.sip.communicator.service.protocol.OperationSetPersistentPresence) r11;
        r15 = new java.lang.StringBuilder;
        r15.<init>();
        r16 = r19.getProtocol();
        r15 = r15.append(r16);
        r16 = ":";
        r15 = r15.append(r16);
        r15 = r15.toString();
        r16 = "";
        r0 = r20;
        r1 = r16;
        r3 = r0.replaceFirst(r15, r1);
        r15 = ".+@.+\\.[a-z]+";
        r9 = java.util.regex.Pattern.compile(r15);
        r15 = r9.matcher(r3);
        r15 = r15.matches();
        if (r15 != 0) goto L_0x00dd;
    L_0x00bd:
        r15 = new java.lang.StringBuilder;
        r15.<init>();
        r16 = "Wrong contact id : ";
        r15 = r15.append(r16);
        r0 = r20;
        r15 = r15.append(r0);
        r15 = r15.toString();
        r16 = 0;
        r0 = r19;
        r1 = r16;
        r0.showErrorMessage(r15, r1);
        goto L_0x002a;
    L_0x00dd:
        r2 = r11.findContactByID(r3);
        if (r2 != 0) goto L_0x0139;
    L_0x00e3:
        r15 = net.java.sip.communicator.impl.protocol.jabber.JabberActivator.getUIService();
        r15 = r15.getPopupDialog();
        r16 = new java.lang.StringBuilder;
        r16.<init>();
        r17 = "Do you want to add the contact : ";
        r16 = r16.append(r17);
        r0 = r16;
        r16 = r0.append(r3);
        r17 = " ?";
        r16 = r16.append(r17);
        r16 = r16.toString();
        r17 = "Add contact";
        r18 = 0;
        r15 = r15.showConfirmPopupDialog(r16, r17, r18);
        r13 = java.lang.Integer.valueOf(r15);
        r15 = 0;
        r15 = java.lang.Integer.valueOf(r15);
        r15 = r13.equals(r15);
        if (r15 == 0) goto L_0x002a;
    L_0x011d:
        r15 = net.java.sip.communicator.impl.protocol.jabber.JabberActivator.getUIService();
        r16 = net.java.sip.communicator.service.gui.ExportedWindow.ADD_CONTACT_WINDOW;
        r17 = 1;
        r0 = r17;
        r0 = new java.lang.String[r0];
        r17 = r0;
        r18 = 0;
        r17[r18] = r3;
        r5 = r15.getExportedWindow(r16, r17);
        r15 = 1;
        r5.setVisible(r15);
        goto L_0x002a;
    L_0x0139:
        r15 = net.java.sip.communicator.impl.protocol.jabber.JabberActivator.getUIService();
        r15 = r15.getChat(r2);
        r16 = 1;
        r15.setChatVisible(r16);
        goto L_0x002a;
    L_0x0148:
        r15 = new java.lang.StringBuilder;
        r15.<init>();
        r16 = r19.getProtocol();
        r15 = r15.append(r16);
        r16 = ":";
        r15 = r15.append(r16);
        r15 = r15.toString();
        r16 = "";
        r0 = r20;
        r1 = r16;
        r4 = r0.replaceFirst(r15, r1);
        r15 = "?";
        r7 = r4.indexOf(r15);
        r15 = r7 + 1;
        r16 = r4.length();
        r0 = r16;
        r10 = r4.substring(r15, r0);
        r15 = 0;
        r4 = r4.substring(r15, r7);
        r15 = "join";
        r15 = r10.equalsIgnoreCase(r15);
        if (r15 == 0) goto L_0x0202;
    L_0x0188:
        r15 = net.java.sip.communicator.service.protocol.OperationSetMultiUserChat.class;
        r8 = r12.getOperationSet(r15);
        r8 = (net.java.sip.communicator.service.protocol.OperationSetMultiUserChat) r8;
        r14 = r8.findRoom(r4);	 Catch:{ OperationFailedException -> 0x019b, OperationNotSupportedException -> 0x01e1 }
        if (r14 == 0) goto L_0x002a;
    L_0x0196:
        r14.join();	 Catch:{ OperationFailedException -> 0x019b, OperationNotSupportedException -> 0x01e1 }
        goto L_0x002a;
    L_0x019b:
        r6 = move-exception;
        r15 = r6.getErrorCode();
        r16 = 2;
        r0 = r16;
        if (r15 != r0) goto L_0x01c7;
    L_0x01a6:
        r0 = r19;
        r15 = r0.networkFailReceived;
        if (r15 != 0) goto L_0x01c7;
    L_0x01ac:
        r15 = 1;
        r0 = r19;
        r0.networkFailReceived = r15;
        r15 = net.java.sip.communicator.service.protocol.OperationSetPresence.class;
        r11 = r12.getOperationSet(r15);
        r11 = (net.java.sip.communicator.service.protocol.OperationSetPresence) r11;
        r15 = new net.java.sip.communicator.impl.protocol.jabber.UriHandlerJabberImpl$ProviderStatusListener;
        r0 = r19;
        r1 = r20;
        r15.m408init(r1, r11);
        r11.addProviderPresenceStatusListener(r15);
        goto L_0x002a;
    L_0x01c7:
        r15 = new java.lang.StringBuilder;
        r15.<init>();
        r16 = "Error joining to  ";
        r15 = r15.append(r16);
        r15 = r15.append(r4);
        r15 = r15.toString();
        r0 = r19;
        r0.showErrorMessage(r15, r6);
        goto L_0x002a;
    L_0x01e1:
        r6 = move-exception;
        r15 = new java.lang.StringBuilder;
        r15.<init>();
        r16 = "Join to ";
        r15 = r15.append(r16);
        r15 = r15.append(r4);
        r16 = ", not supported!";
        r15 = r15.append(r16);
        r15 = r15.toString();
        r0 = r19;
        r0.showErrorMessage(r15, r6);
        goto L_0x002a;
    L_0x0202:
        r15 = new java.lang.StringBuilder;
        r15.<init>();
        r16 = "Unknown param : ";
        r15 = r15.append(r16);
        r15 = r15.append(r10);
        r15 = r15.toString();
        r16 = 0;
        r0 = r19;
        r1 = r16;
        r0.showErrorMessage(r15, r1);
        goto L_0x002a;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.jabber.UriHandlerJabberImpl.handleUri(java.lang.String):void");
    }

    private void promptForRegistration(String uri, ProtocolProviderService provider) {
        if (JabberActivator.getUIService().getPopupDialog().showConfirmPopupDialog("You need to be online in order to chat and your account is currently offline. Do you want to connect now?", "Account is currently offline", 0) == 0) {
            new ProtocolRegistrationThread(uri, provider).start();
        }
    }

    public void serviceChanged(ServiceEvent event) {
        if (JabberActivator.bundleContext.getService(event.getServiceReference()) == this.protoFactory) {
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
        JabberActivator.getUIService().getPopupDialog().showMessagePopupDialog(message, "Failed to create chat!", 0);
        logger.error(message, exc);
    }

    public ProtocolProviderService selectHandlingProvider(String uri) throws OperationFailedException {
        ArrayList<AccountID> registeredAccounts = this.protoFactory.getRegisteredAccounts();
        if (registeredAccounts.size() == 0) {
            return null;
        }
        if (registeredAccounts.size() == 1) {
            return (ProtocolProviderService) JabberActivator.bundleContext.getService(this.protoFactory.getProviderForAccount((AccountID) registeredAccounts.get(0)));
        }
        ArrayList<ProviderComboBoxEntry> providers = new ArrayList();
        Iterator i$ = registeredAccounts.iterator();
        while (i$.hasNext()) {
            providers.add(new ProviderComboBoxEntry((ProtocolProviderService) JabberActivator.bundleContext.getService(this.protoFactory.getProviderForAccount((AccountID) i$.next()))));
        }
        Object result = JabberActivator.getUIService().getPopupDialog().showInputPopupDialog("Please select the account that you would like \nto use to chat with " + uri, "Account Selection", 2, providers.toArray(), providers.get(0));
        if (result != null) {
            return ((ProviderComboBoxEntry) result).provider;
        }
        throw new OperationFailedException("Operation cancelled", 16);
    }
}
