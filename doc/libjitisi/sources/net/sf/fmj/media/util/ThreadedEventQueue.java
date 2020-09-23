package net.sf.fmj.media.util;

import java.util.List;
import java.util.Vector;
import javax.media.ControllerEvent;

public abstract class ThreadedEventQueue extends MediaThread {
    private List<ControllerEvent> eventQueue = new Vector();
    private boolean killed = false;

    public abstract void processEvent(ControllerEvent controllerEvent);

    public ThreadedEventQueue() {
        useControlPriority();
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    public boolean dispatchEvents() {
        /*
        r7 = this;
        r5 = 1;
        r4 = 0;
        r2 = 0;
        monitor-enter(r7);
    L_0x0004:
        r3 = r7.killed;	 Catch:{ InterruptedException -> 0x0014 }
        if (r3 != 0) goto L_0x0030;
    L_0x0008:
        r3 = r7.eventQueue;	 Catch:{ InterruptedException -> 0x0014 }
        r3 = r3.size();	 Catch:{ InterruptedException -> 0x0014 }
        if (r3 != 0) goto L_0x0030;
    L_0x0010:
        r7.wait();	 Catch:{ InterruptedException -> 0x0014 }
        goto L_0x0004;
    L_0x0014:
        r1 = move-exception;
        r3 = java.lang.System.err;	 Catch:{ all -> 0x0057 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0057 }
        r4.<init>();	 Catch:{ all -> 0x0057 }
        r6 = "MediaNode event thread ";
        r4 = r4.append(r6);	 Catch:{ all -> 0x0057 }
        r4 = r4.append(r1);	 Catch:{ all -> 0x0057 }
        r4 = r4.toString();	 Catch:{ all -> 0x0057 }
        r3.println(r4);	 Catch:{ all -> 0x0057 }
        monitor-exit(r7);	 Catch:{ all -> 0x0057 }
        r3 = r5;
    L_0x002f:
        return r3;
    L_0x0030:
        r3 = r7.eventQueue;	 Catch:{ all -> 0x0057 }
        r3 = r3.size();	 Catch:{ all -> 0x0057 }
        if (r3 <= 0) goto L_0x0043;
    L_0x0038:
        r3 = r7.eventQueue;	 Catch:{ all -> 0x0057 }
        r6 = 0;
        r3 = r3.remove(r6);	 Catch:{ all -> 0x0057 }
        r0 = r3;
        r0 = (javax.media.ControllerEvent) r0;	 Catch:{ all -> 0x0057 }
        r2 = r0;
    L_0x0043:
        monitor-exit(r7);	 Catch:{ all -> 0x0057 }
        if (r2 == 0) goto L_0x0049;
    L_0x0046:
        r7.processEvent(r2);
    L_0x0049:
        r3 = r7.killed;
        if (r3 == 0) goto L_0x0055;
    L_0x004d:
        r3 = r7.eventQueue;
        r3 = r3.size();
        if (r3 == 0) goto L_0x005a;
    L_0x0055:
        r3 = r5;
        goto L_0x002f;
    L_0x0057:
        r3 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x0057 }
        throw r3;
    L_0x005a:
        r3 = r4;
        goto L_0x002f;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.util.ThreadedEventQueue.dispatchEvents():boolean");
    }

    public synchronized void kill() {
        this.killed = true;
        notifyAll();
    }

    public synchronized void postEvent(ControllerEvent evt) {
        this.eventQueue.add(evt);
        notifyAll();
    }

    public void run() {
        do {
        } while (dispatchEvents());
    }
}
