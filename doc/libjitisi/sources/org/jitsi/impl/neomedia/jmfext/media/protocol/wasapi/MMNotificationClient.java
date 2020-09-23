package org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi;

import java.util.ArrayList;
import java.util.Collection;
import org.jitsi.util.Logger;

public class MMNotificationClient {
    private static final Logger logger = Logger.getLogger(MMNotificationClient.class);
    private static Collection<IMMNotificationClient> pNotifySet;

    public static void OnDefaultDeviceChanged(int flow, int role, String pwstrDefaultDevice) {
    }

    public static void OnDeviceAdded(String pwstrDeviceId) {
        Iterable<IMMNotificationClient> pNotifySet;
        synchronized (MMNotificationClient.class) {
            pNotifySet = pNotifySet;
        }
        if (pNotifySet != null) {
            for (IMMNotificationClient pNotify : pNotifySet) {
                try {
                    pNotify.OnDeviceAdded(pwstrDeviceId);
                } catch (Throwable t) {
                    if (t instanceof ThreadDeath) {
                        ThreadDeath t2 = (ThreadDeath) t;
                    } else {
                        logger.error("An IMMNotificationClient failed to normally complete the handling of an OnDeviceAdded notification.", t);
                    }
                }
            }
        }
    }

    public static void OnDeviceRemoved(String pwstrDeviceId) {
        Iterable<IMMNotificationClient> pNotifySet;
        synchronized (MMNotificationClient.class) {
            pNotifySet = pNotifySet;
        }
        if (pNotifySet != null) {
            for (IMMNotificationClient pNotify : pNotifySet) {
                try {
                    pNotify.OnDeviceRemoved(pwstrDeviceId);
                } catch (Throwable t) {
                    if (t instanceof ThreadDeath) {
                        ThreadDeath t2 = (ThreadDeath) t;
                    } else {
                        logger.error("An IMMNotificationClient failed to normally complete the handling of an OnDeviceRemoved notification.", t);
                    }
                }
            }
        }
    }

    public static void OnDeviceStateChanged(String pwstrDeviceId, int dwNewState) {
        Iterable<IMMNotificationClient> pNotifySet;
        synchronized (MMNotificationClient.class) {
            pNotifySet = pNotifySet;
        }
        if (pNotifySet != null) {
            for (IMMNotificationClient pNotify : pNotifySet) {
                try {
                    pNotify.OnDeviceStateChanged(pwstrDeviceId, dwNewState);
                } catch (Throwable t) {
                    if (t instanceof ThreadDeath) {
                        ThreadDeath t2 = (ThreadDeath) t;
                    } else {
                        logger.error("An IMMNotificationClient failed to normally complete the handling of an OnDeviceStateChanged notification.", t);
                    }
                }
            }
        }
    }

    public static void OnPropertyValueChanged(String pwstrDeviceId, long key) {
    }

    /* JADX WARNING: Missing block: B:22:?, code skipped:
            return;
     */
    public static void RegisterEndpointNotificationCallback(org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.IMMNotificationClient r3) {
        /*
        if (r3 != 0) goto L_0x000a;
    L_0x0002:
        r1 = new java.lang.NullPointerException;
        r2 = "pNotify";
        r1.<init>(r2);
        throw r1;
    L_0x000a:
        r2 = org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.MMNotificationClient.class;
        monitor-enter(r2);
        r1 = pNotifySet;	 Catch:{ all -> 0x002a }
        if (r1 != 0) goto L_0x0020;
    L_0x0011:
        r0 = new java.util.ArrayList;	 Catch:{ all -> 0x002a }
        r0.<init>();	 Catch:{ all -> 0x002a }
    L_0x0016:
        r1 = r0.add(r3);	 Catch:{ all -> 0x002a }
        if (r1 == 0) goto L_0x001e;
    L_0x001c:
        pNotifySet = r0;	 Catch:{ all -> 0x002a }
    L_0x001e:
        monitor-exit(r2);	 Catch:{ all -> 0x002a }
    L_0x001f:
        return;
    L_0x0020:
        r1 = pNotifySet;	 Catch:{ all -> 0x002a }
        r1 = r1.contains(r3);	 Catch:{ all -> 0x002a }
        if (r1 == 0) goto L_0x002d;
    L_0x0028:
        monitor-exit(r2);	 Catch:{ all -> 0x002a }
        goto L_0x001f;
    L_0x002a:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x002a }
        throw r1;
    L_0x002d:
        r0 = new java.util.ArrayList;	 Catch:{ all -> 0x002a }
        r1 = pNotifySet;	 Catch:{ all -> 0x002a }
        r1 = r1.size();	 Catch:{ all -> 0x002a }
        r1 = r1 + 1;
        r0.<init>(r1);	 Catch:{ all -> 0x002a }
        r1 = pNotifySet;	 Catch:{ all -> 0x002a }
        r0.addAll(r1);	 Catch:{ all -> 0x002a }
        goto L_0x0016;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.MMNotificationClient.RegisterEndpointNotificationCallback(org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi.IMMNotificationClient):void");
    }

    public static void UnregisterEndpointNotificationCallback(IMMNotificationClient pNotify) {
        if (pNotify == null) {
            throw new NullPointerException("pNotify");
        }
        synchronized (MMNotificationClient.class) {
            if (pNotifySet != null && pNotifySet.contains(pNotify)) {
                if (pNotifySet.size() == 1) {
                    pNotifySet = null;
                } else {
                    Collection<IMMNotificationClient> newPNotifySet = new ArrayList(pNotifySet);
                    if (newPNotifySet.remove(pNotify)) {
                        pNotifySet = newPNotifySet;
                    }
                }
            }
        }
    }

    private MMNotificationClient() {
    }
}
