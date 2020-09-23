package org.jitsi.util.event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.jitsi.android.util.java.awt.Component;

public class VideoNotifierSupport {
    private static final long THREAD_TIMEOUT = 5000;
    /* access modifiers changed from: private|final */
    public final List<VideoEvent> events;
    private final List<VideoListener> listeners;
    private final Object source;
    private final boolean synchronous;
    /* access modifiers changed from: private */
    public Thread thread;

    public VideoNotifierSupport(Object source) {
        this(source, true);
    }

    public VideoNotifierSupport(Object source, boolean synchronous) {
        this.listeners = new ArrayList();
        this.source = source;
        this.synchronous = synchronous;
        this.events = this.synchronous ? null : new LinkedList();
    }

    public void addVideoListener(VideoListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        synchronized (this.listeners) {
            if (!this.listeners.contains(listener)) {
                this.listeners.add(listener);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void doFireVideoEvent(VideoEvent event) {
        synchronized (this.listeners) {
        }
        for (VideoListener listener : (VideoListener[]) this.listeners.toArray(new VideoListener[this.listeners.size()])) {
            switch (event.getType()) {
                case 1:
                    listener.videoAdded(event);
                    break;
                case 2:
                    listener.videoRemoved(event);
                    break;
                default:
                    listener.videoUpdate(event);
                    break;
            }
        }
    }

    public boolean fireVideoEvent(int type, Component visualComponent, int origin, boolean wait) {
        VideoEvent event = new VideoEvent(this.source, type, visualComponent, origin);
        fireVideoEvent(event, wait);
        return event.isConsumed();
    }

    public void fireVideoEvent(VideoEvent event, boolean wait) {
        if (this.synchronous) {
            doFireVideoEvent(event);
            return;
        }
        synchronized (this.events) {
            this.events.add(event);
            if (this.thread == null) {
                startThread();
            } else {
                this.events.notify();
            }
            if (wait) {
                boolean interrupted = false;
                while (this.events.contains(event) && this.thread != null) {
                    try {
                        this.events.wait();
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void removeVideoListener(VideoListener listener) {
        synchronized (this.listeners) {
            this.listeners.remove(listener);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Missing block: B:27:0x0056, code skipped:
            if (r4 == null) goto L_0x0002;
     */
    /* JADX WARNING: Missing block: B:30:?, code skipped:
            doFireVideoEvent(r4);
     */
    /* JADX WARNING: Missing block: B:31:0x005d, code skipped:
            r11 = r16.events;
     */
    /* JADX WARNING: Missing block: B:32:0x0061, code skipped:
            monitor-enter(r11);
     */
    /* JADX WARNING: Missing block: B:35:?, code skipped:
            r16.events.notify();
     */
    /* JADX WARNING: Missing block: B:36:0x0069, code skipped:
            monitor-exit(r11);
     */
    /* JADX WARNING: Missing block: B:45:0x0071, code skipped:
            r7 = move-exception;
     */
    /* JADX WARNING: Missing block: B:47:0x0074, code skipped:
            if ((r7 instanceof java.lang.ThreadDeath) != false) goto L_0x0076;
     */
    /* JADX WARNING: Missing block: B:48:0x0076, code skipped:
            r7 = (java.lang.ThreadDeath) r7;
     */
    public void runInThread() {
        /*
        r16 = this;
        r14 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;
    L_0x0002:
        r4 = 0;
        r0 = r16;
        r11 = r0.events;
        monitor-enter(r11);
        r2 = -1;
        r6 = 0;
    L_0x000b:
        r0 = r16;
        r10 = r0.events;	 Catch:{ all -> 0x006e }
        r10 = r10.isEmpty();	 Catch:{ all -> 0x006e }
        if (r10 == 0) goto L_0x003f;
    L_0x0015:
        r12 = -1;
        r10 = (r2 > r12 ? 1 : (r2 == r12 ? 0 : -1));
        if (r10 != 0) goto L_0x002c;
    L_0x001b:
        r2 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x006e }
    L_0x001f:
        r0 = r16;
        r10 = r0.events;	 Catch:{ InterruptedException -> 0x0029 }
        r12 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;
        r10.wait(r12);	 Catch:{ InterruptedException -> 0x0029 }
        goto L_0x000b;
    L_0x0029:
        r5 = move-exception;
        r6 = 1;
        goto L_0x000b;
    L_0x002c:
        r8 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x006e }
        r12 = r8 - r2;
        r10 = (r12 > r14 ? 1 : (r12 == r14 ? 0 : -1));
        if (r10 < 0) goto L_0x001f;
    L_0x0036:
        r0 = r16;
        r10 = r0.events;	 Catch:{ all -> 0x006e }
        r10.notify();	 Catch:{ all -> 0x006e }
        monitor-exit(r11);	 Catch:{ all -> 0x006e }
        return;
    L_0x003f:
        if (r6 == 0) goto L_0x0048;
    L_0x0041:
        r10 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x006e }
        r10.interrupt();	 Catch:{ all -> 0x006e }
    L_0x0048:
        r0 = r16;
        r10 = r0.events;	 Catch:{ all -> 0x006e }
        r12 = 0;
        r10 = r10.remove(r12);	 Catch:{ all -> 0x006e }
        r0 = r10;
        r0 = (org.jitsi.util.event.VideoEvent) r0;	 Catch:{ all -> 0x006e }
        r4 = r0;
        monitor-exit(r11);	 Catch:{ all -> 0x006e }
        if (r4 == 0) goto L_0x0002;
    L_0x0058:
        r0 = r16;
        r0.doFireVideoEvent(r4);	 Catch:{ Throwable -> 0x0071 }
    L_0x005d:
        r0 = r16;
        r11 = r0.events;
        monitor-enter(r11);
        r0 = r16;
        r10 = r0.events;	 Catch:{ all -> 0x006b }
        r10.notify();	 Catch:{ all -> 0x006b }
        monitor-exit(r11);	 Catch:{ all -> 0x006b }
        goto L_0x0002;
    L_0x006b:
        r10 = move-exception;
        monitor-exit(r11);	 Catch:{ all -> 0x006b }
        throw r10;
    L_0x006e:
        r10 = move-exception;
        monitor-exit(r11);	 Catch:{ all -> 0x006e }
        throw r10;
    L_0x0071:
        r7 = move-exception;
        r10 = r7 instanceof java.lang.ThreadDeath;
        if (r10 == 0) goto L_0x005d;
    L_0x0076:
        r7 = (java.lang.ThreadDeath) r7;
        throw r7;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.util.event.VideoNotifierSupport.runInThread():void");
    }

    /* access modifiers changed from: private */
    public void startThread() {
        this.thread = new Thread("VideoNotifierSupportThread") {
            public void run() {
                try {
                    VideoNotifierSupport.this.runInThread();
                    synchronized (VideoNotifierSupport.this.events) {
                        if (Thread.currentThread().equals(VideoNotifierSupport.this.thread)) {
                            VideoNotifierSupport.this.thread = null;
                            if (VideoNotifierSupport.this.events.isEmpty()) {
                                VideoNotifierSupport.this.events.notify();
                            } else {
                                VideoNotifierSupport.this.startThread();
                            }
                        }
                    }
                } catch (Throwable th) {
                    synchronized (VideoNotifierSupport.this.events) {
                        if (Thread.currentThread().equals(VideoNotifierSupport.this.thread)) {
                            VideoNotifierSupport.this.thread = null;
                            if (VideoNotifierSupport.this.events.isEmpty()) {
                                VideoNotifierSupport.this.events.notify();
                            } else {
                                VideoNotifierSupport.this.startThread();
                            }
                        }
                    }
                }
            }
        };
        this.thread.setDaemon(true);
        this.thread.start();
    }
}
