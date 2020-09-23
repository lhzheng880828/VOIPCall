package org.jitsi.service.audionotifier;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public abstract class AbstractSCAudioClip implements SCAudioClip {
    private static ExecutorService executorService;
    protected final AudioNotifierService audioNotifier;
    /* access modifiers changed from: private */
    public Runnable command;
    private boolean invalid;
    private int loopInterval;
    private boolean looping;
    /* access modifiers changed from: private */
    public boolean started;
    protected final Object sync = new Object();
    protected final String uri;

    public abstract boolean runOnceInPlayThread();

    protected AbstractSCAudioClip(String uri, AudioNotifierService audioNotifier) {
        this.uri = uri;
        this.audioNotifier = audioNotifier;
    }

    /* access modifiers changed from: protected */
    public void enterRunInPlayThread() {
    }

    /* access modifiers changed from: protected */
    public void enterRunOnceInPlayThread() {
    }

    /* access modifiers changed from: protected */
    public void exitRunInPlayThread() {
    }

    /* access modifiers changed from: protected */
    public void exitRunOnceInPlayThread() {
    }

    public int getLoopInterval() {
        return this.loopInterval;
    }

    /* access modifiers changed from: protected */
    public void internalStop() {
        boolean interrupted = false;
        synchronized (this.sync) {
            this.started = false;
            this.sync.notifyAll();
            while (this.command != null) {
                try {
                    this.sync.wait(500);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean isInvalid() {
        return this.invalid;
    }

    public boolean isLooping() {
        return this.looping;
    }

    public boolean isStarted() {
        boolean z;
        synchronized (this.sync) {
            z = this.started;
        }
        return z;
    }

    public void play() {
        play(-1, null);
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* JADX WARNING: Missing block: B:34:?, code skipped:
            return;
     */
    public void play(int r6, final java.util.concurrent.Callable<java.lang.Boolean> r7) {
        /*
        r5 = this;
        r1 = 1;
        r2 = 0;
        if (r6 < 0) goto L_0x0007;
    L_0x0004:
        if (r7 != 0) goto L_0x0007;
    L_0x0006:
        r6 = -1;
    L_0x0007:
        r3 = r5.sync;
        monitor-enter(r3);
        r4 = r5.command;	 Catch:{ all -> 0x0048 }
        if (r4 == 0) goto L_0x0010;
    L_0x000e:
        monitor-exit(r3);	 Catch:{ all -> 0x0048 }
    L_0x000f:
        return;
    L_0x0010:
        r5.setLoopInterval(r6);	 Catch:{ all -> 0x0048 }
        if (r6 < 0) goto L_0x004b;
    L_0x0015:
        r5.setLooping(r1);	 Catch:{ all -> 0x0048 }
        r2 = org.jitsi.service.audionotifier.AbstractSCAudioClip.class;
        monitor-enter(r2);	 Catch:{ all -> 0x0048 }
        r1 = executorService;	 Catch:{ all -> 0x004d }
        if (r1 != 0) goto L_0x0025;
    L_0x001f:
        r1 = java.util.concurrent.Executors.newCachedThreadPool();	 Catch:{ all -> 0x004d }
        executorService = r1;	 Catch:{ all -> 0x004d }
    L_0x0025:
        r0 = executorService;	 Catch:{ all -> 0x004d }
        monitor-exit(r2);	 Catch:{ all -> 0x004d }
        r1 = 0;
        r5.started = r1;	 Catch:{ all -> 0x0050 }
        r1 = new org.jitsi.service.audionotifier.AbstractSCAudioClip$1;	 Catch:{ all -> 0x0050 }
        r1.m2697init(r7);	 Catch:{ all -> 0x0050 }
        r5.command = r1;	 Catch:{ all -> 0x0050 }
        r1 = r5.command;	 Catch:{ all -> 0x0050 }
        r0.execute(r1);	 Catch:{ all -> 0x0050 }
        r1 = 1;
        r5.started = r1;	 Catch:{ all -> 0x0050 }
        r1 = r5.started;	 Catch:{ all -> 0x0048 }
        if (r1 != 0) goto L_0x0041;
    L_0x003e:
        r1 = 0;
        r5.command = r1;	 Catch:{ all -> 0x0048 }
    L_0x0041:
        r1 = r5.sync;	 Catch:{ all -> 0x0048 }
        r1.notifyAll();	 Catch:{ all -> 0x0048 }
        monitor-exit(r3);	 Catch:{ all -> 0x0048 }
        goto L_0x000f;
    L_0x0048:
        r1 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x0048 }
        throw r1;
    L_0x004b:
        r1 = r2;
        goto L_0x0015;
    L_0x004d:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x004d }
        throw r1;	 Catch:{ all -> 0x0048 }
    L_0x0050:
        r1 = move-exception;
        r2 = r5.started;	 Catch:{ all -> 0x0048 }
        if (r2 != 0) goto L_0x0058;
    L_0x0055:
        r2 = 0;
        r5.command = r2;	 Catch:{ all -> 0x0048 }
    L_0x0058:
        r2 = r5.sync;	 Catch:{ all -> 0x0048 }
        r2.notifyAll();	 Catch:{ all -> 0x0048 }
        throw r1;	 Catch:{ all -> 0x0048 }
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.service.audionotifier.AbstractSCAudioClip.play(int, java.util.concurrent.Callable):void");
    }

    /* access modifiers changed from: private */
    public void runInPlayThread(Callable<Boolean> loopCondition) {
        enterRunInPlayThread();
        boolean interrupted = false;
        boolean loop;
        do {
            try {
                if (!isStarted()) {
                    break;
                }
                if (this.audioNotifier.isMute()) {
                    synchronized (this.sync) {
                        try {
                            this.sync.wait(500);
                        } catch (InterruptedException e) {
                            interrupted = true;
                        }
                    }
                } else {
                    enterRunOnceInPlayThread();
                    if (!runOnceInPlayThread()) {
                        exitRunOnceInPlayThread();
                        break;
                    }
                    exitRunOnceInPlayThread();
                }
                if (!isLooping()) {
                    break;
                }
                synchronized (this.sync) {
                    if (!isStarted()) {
                        break;
                    }
                    try {
                        int loopInterval = getLoopInterval();
                        if (loopInterval > 0) {
                            this.sync.wait((long) loopInterval);
                        }
                    } catch (InterruptedException e2) {
                        interrupted = true;
                    }
                    if (!isStarted() || loopCondition == null) {
                        break;
                    }
                    loop = false;
                    loop = ((Boolean) loopCondition.call()).booleanValue();
                    continue;
                }
            } catch (Throwable th) {
                exitRunInPlayThread();
            }
        } while (loop);
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        exitRunInPlayThread();
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public void setLooping(boolean looping) {
        synchronized (this.sync) {
            if (this.looping != looping) {
                this.looping = looping;
                this.sync.notifyAll();
            }
        }
    }

    public void setLoopInterval(int loopInterval) {
        synchronized (this.sync) {
            if (this.loopInterval != loopInterval) {
                this.loopInterval = loopInterval;
                this.sync.notifyAll();
            }
        }
    }

    public void stop() {
        internalStop();
        setLooping(false);
    }
}
