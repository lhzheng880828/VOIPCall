package net.java.sip.communicator.impl.protocol.sip;

import java.util.ArrayList;
import java.util.List;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.SipException;

public final class EventPackageUtils {

    private static class DialogApplicationData {
        private boolean byeIsProcessed;
        private final List<Object> subscriptions;

        private DialogApplicationData() {
            this.subscriptions = new ArrayList();
        }

        public boolean addSubscription(Object subscription) {
            if (this.subscriptions.contains(subscription)) {
                return false;
            }
            return this.subscriptions.add(subscription);
        }

        public boolean isByeProcessed() {
            return this.byeIsProcessed;
        }

        public int getSubscriptionCount() {
            return this.subscriptions.size();
        }

        public boolean removeSubscription(Object subscription) {
            return this.subscriptions.remove(subscription);
        }

        public void setByeProcessed(boolean byeIsProcessed) {
            this.byeIsProcessed = byeIsProcessed;
        }
    }

    public static boolean addSubscription(Dialog dialog, Object subscription) throws SipException {
        boolean z = false;
        synchronized (dialog) {
            DialogApplicationData appData = (DialogApplicationData) SipApplicationData.getApplicationData(dialog, SipApplicationData.KEY_SUBSCRIPTIONS);
            if (appData == null) {
                appData = new DialogApplicationData();
                SipApplicationData.setApplicationData(dialog, SipApplicationData.KEY_SUBSCRIPTIONS, appData);
            }
            if (appData.addSubscription(subscription)) {
                try {
                    dialog.terminateOnBye(false);
                    z = true;
                } catch (SipException ex) {
                    appData.removeSubscription(subscription);
                    throw ex;
                }
            }
        }
        return z;
    }

    public static boolean isByeProcessed(Dialog dialog) {
        boolean isByeProcessed;
        synchronized (dialog) {
            DialogApplicationData applicationData = (DialogApplicationData) SipApplicationData.getApplicationData(dialog, SipApplicationData.KEY_SUBSCRIPTIONS);
            isByeProcessed = applicationData == null ? false : applicationData.isByeProcessed();
        }
        return isByeProcessed;
    }

    /* JADX WARNING: Missing block: B:16:?, code skipped:
            return false;
     */
    public static boolean processByeThenIsDialogAlive(org.jitsi.javax.sip.Dialog r4) throws org.jitsi.javax.sip.SipException {
        /*
        r1 = 1;
        r2 = 0;
        monitor-enter(r4);
        r3 = "subscriptions";
        r0 = net.java.sip.communicator.impl.protocol.sip.SipApplicationData.getApplicationData(r4, r3);	 Catch:{ all -> 0x0020 }
        r0 = (net.java.sip.communicator.impl.protocol.sip.EventPackageUtils.DialogApplicationData) r0;	 Catch:{ all -> 0x0020 }
        if (r0 == 0) goto L_0x001d;
    L_0x000d:
        r3 = 1;
        r0.setByeProcessed(r3);	 Catch:{ all -> 0x0020 }
        r3 = r0.getSubscriptionCount();	 Catch:{ all -> 0x0020 }
        if (r3 <= 0) goto L_0x001d;
    L_0x0017:
        r2 = 0;
        r4.terminateOnBye(r2);	 Catch:{ all -> 0x0020 }
        monitor-exit(r4);	 Catch:{ all -> 0x0020 }
    L_0x001c:
        return r1;
    L_0x001d:
        monitor-exit(r4);	 Catch:{ all -> 0x0020 }
        r1 = r2;
        goto L_0x001c;
    L_0x0020:
        r1 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x0020 }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.sip.EventPackageUtils.processByeThenIsDialogAlive(org.jitsi.javax.sip.Dialog):boolean");
    }

    public static boolean removeSubscriptionThenIsDialogAlive(Dialog dialog, Object subscription) {
        boolean z;
        synchronized (dialog) {
            DialogApplicationData applicationData = (DialogApplicationData) SipApplicationData.getApplicationData(dialog, SipApplicationData.KEY_SUBSCRIPTIONS);
            if (applicationData == null || !applicationData.removeSubscription(subscription) || applicationData.getSubscriptionCount() > 0 || !applicationData.isByeProcessed()) {
                z = true;
            } else {
                dialog.delete();
                z = false;
            }
        }
        return z;
    }

    private EventPackageUtils() {
    }
}
