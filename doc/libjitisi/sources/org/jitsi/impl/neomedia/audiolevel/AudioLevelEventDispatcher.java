package org.jitsi.impl.neomedia.audiolevel;

import javax.media.Buffer;
import org.jitsi.service.neomedia.event.SimpleAudioLevelListener;

public class AudioLevelEventDispatcher {
    private static final long IDLE_TIMEOUT = 30000;
    /* access modifiers changed from: private */
    public AudioLevelMap cache = null;
    /* access modifiers changed from: private */
    public byte[] data = null;
    /* access modifiers changed from: private */
    public int dataLength = 0;
    private int lastLevel = 0;
    /* access modifiers changed from: private */
    public SimpleAudioLevelListener listener;
    /* access modifiers changed from: private */
    public long ssrc = -1;
    /* access modifiers changed from: private */
    public Thread thread;
    private final String threadName;

    public AudioLevelEventDispatcher(String threadName) {
        this.threadName = threadName;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* JADX WARNING: Missing block: B:41:0x0077, code skipped:
            r14 = org.jitsi.impl.neomedia.audiolevel.AudioLevelCalculator.calculateSoundPressureLevel(r2, 0, r4, 0, 127, r20.lastLevel);
     */
    /* JADX WARNING: Missing block: B:42:0x0083, code skipped:
            monitor-enter(r20);
     */
    /* JADX WARNING: Missing block: B:46:0x0088, code skipped:
            if (r20.data != null) goto L_0x00a4;
     */
    /* JADX WARNING: Missing block: B:48:0x008e, code skipped:
            if (r20.listener != null) goto L_0x00a4;
     */
    /* JADX WARNING: Missing block: B:50:0x0094, code skipped:
            if (r20.cache == null) goto L_0x00a0;
     */
    /* JADX WARNING: Missing block: B:52:0x009e, code skipped:
            if (r20.ssrc != -1) goto L_0x00a4;
     */
    /* JADX WARNING: Missing block: B:53:0x00a0, code skipped:
            r20.data = r2;
     */
    /* JADX WARNING: Missing block: B:54:0x00a4, code skipped:
            monitor-exit(r20);
     */
    /* JADX WARNING: Missing block: B:55:0x00a5, code skipped:
            if (r8 == null) goto L_0x00b2;
     */
    /* JADX WARNING: Missing block: B:57:0x00ab, code skipped:
            if (r16 == -1) goto L_0x00b2;
     */
    /* JADX WARNING: Missing block: B:60:?, code skipped:
            r8.putLevel(r16, r14);
     */
    /* JADX WARNING: Missing block: B:61:0x00b2, code skipped:
            if (r13 == null) goto L_0x00b7;
     */
    /* JADX WARNING: Missing block: B:62:0x00b4, code skipped:
            r13.audioLevelChanged(r14);
     */
    /* JADX WARNING: Missing block: B:63:0x00b7, code skipped:
            r20.lastLevel = r14;
     */
    /* JADX WARNING: Missing block: B:69:0x00c1, code skipped:
            r20.lastLevel = r14;
     */
    public void run() {
        /*
        r20 = this;
        r10 = -1;
    L_0x0002:
        monitor-enter(r20);
        r3 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x002d }
        r0 = r20;
        r5 = r0.thread;	 Catch:{ all -> 0x002d }
        r3 = r3.equals(r5);	 Catch:{ all -> 0x002d }
        if (r3 != 0) goto L_0x0013;
    L_0x0011:
        monitor-exit(r20);	 Catch:{ all -> 0x002d }
    L_0x0012:
        return;
    L_0x0013:
        r0 = r20;
        r13 = r0.listener;	 Catch:{ all -> 0x002d }
        r0 = r20;
        r8 = r0.cache;	 Catch:{ all -> 0x002d }
        r0 = r20;
        r0 = r0.ssrc;	 Catch:{ all -> 0x002d }
        r16 = r0;
        if (r13 != 0) goto L_0x0030;
    L_0x0023:
        if (r8 == 0) goto L_0x002b;
    L_0x0025:
        r6 = -1;
        r3 = (r16 > r6 ? 1 : (r16 == r6 ? 0 : -1));
        if (r3 != 0) goto L_0x0030;
    L_0x002b:
        monitor-exit(r20);	 Catch:{ all -> 0x002d }
        goto L_0x0012;
    L_0x002d:
        r3 = move-exception;
        monitor-exit(r20);	 Catch:{ all -> 0x002d }
        throw r3;
    L_0x0030:
        r0 = r20;
        r2 = r0.data;	 Catch:{ all -> 0x002d }
        r0 = r20;
        r4 = r0.dataLength;	 Catch:{ all -> 0x002d }
        if (r2 == 0) goto L_0x003d;
    L_0x003a:
        r3 = 1;
        if (r4 >= r3) goto L_0x006a;
    L_0x003d:
        r6 = -1;
        r3 = (r10 > r6 ? 1 : (r10 == r6 ? 0 : -1));
        if (r3 != 0) goto L_0x005a;
    L_0x0043:
        r10 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x002d }
    L_0x0047:
        r12 = 0;
        r6 = 30000; // 0x7530 float:4.2039E-41 double:1.4822E-319;
        r0 = r20;
        r0.wait(r6);	 Catch:{ InterruptedException -> 0x0067 }
    L_0x004f:
        if (r12 == 0) goto L_0x0058;
    L_0x0051:
        r3 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x002d }
        r3.interrupt();	 Catch:{ all -> 0x002d }
    L_0x0058:
        monitor-exit(r20);	 Catch:{ all -> 0x002d }
        goto L_0x0002;
    L_0x005a:
        r6 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x002d }
        r6 = r6 - r10;
        r18 = 30000; // 0x7530 float:4.2039E-41 double:1.4822E-319;
        r3 = (r6 > r18 ? 1 : (r6 == r18 ? 0 : -1));
        if (r3 < 0) goto L_0x0047;
    L_0x0065:
        monitor-exit(r20);	 Catch:{ all -> 0x002d }
        goto L_0x0012;
    L_0x0067:
        r9 = move-exception;
        r12 = 1;
        goto L_0x004f;
    L_0x006a:
        r3 = 0;
        r0 = r20;
        r0.data = r3;	 Catch:{ all -> 0x002d }
        r3 = 0;
        r0 = r20;
        r0.dataLength = r3;	 Catch:{ all -> 0x002d }
        r10 = -1;
        monitor-exit(r20);	 Catch:{ all -> 0x002d }
        r3 = 0;
        r5 = 0;
        r6 = 127; // 0x7f float:1.78E-43 double:6.27E-322;
        r0 = r20;
        r7 = r0.lastLevel;
        r14 = org.jitsi.impl.neomedia.audiolevel.AudioLevelCalculator.calculateSoundPressureLevel(r2, r3, r4, r5, r6, r7);
        monitor-enter(r20);
        r0 = r20;
        r3 = r0.data;	 Catch:{ all -> 0x00bd }
        if (r3 != 0) goto L_0x00a4;
    L_0x008a:
        r0 = r20;
        r3 = r0.listener;	 Catch:{ all -> 0x00bd }
        if (r3 != 0) goto L_0x00a4;
    L_0x0090:
        r0 = r20;
        r3 = r0.cache;	 Catch:{ all -> 0x00bd }
        if (r3 == 0) goto L_0x00a0;
    L_0x0096:
        r0 = r20;
        r6 = r0.ssrc;	 Catch:{ all -> 0x00bd }
        r18 = -1;
        r3 = (r6 > r18 ? 1 : (r6 == r18 ? 0 : -1));
        if (r3 != 0) goto L_0x00a4;
    L_0x00a0:
        r0 = r20;
        r0.data = r2;	 Catch:{ all -> 0x00bd }
    L_0x00a4:
        monitor-exit(r20);	 Catch:{ all -> 0x00bd }
        if (r8 == 0) goto L_0x00b2;
    L_0x00a7:
        r6 = -1;
        r3 = (r16 > r6 ? 1 : (r16 == r6 ? 0 : -1));
        if (r3 == 0) goto L_0x00b2;
    L_0x00ad:
        r0 = r16;
        r8.putLevel(r0, r14);	 Catch:{ all -> 0x00c0 }
    L_0x00b2:
        if (r13 == 0) goto L_0x00b7;
    L_0x00b4:
        r13.audioLevelChanged(r14);	 Catch:{ all -> 0x00c0 }
    L_0x00b7:
        r0 = r20;
        r0.lastLevel = r14;
        goto L_0x0002;
    L_0x00bd:
        r3 = move-exception;
        monitor-exit(r20);	 Catch:{ all -> 0x00bd }
        throw r3;
    L_0x00c0:
        r3 = move-exception;
        r0 = r20;
        r0.lastLevel = r14;
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.audiolevel.AudioLevelEventDispatcher.run():void");
    }

    public synchronized void addData(Buffer buffer) {
        if (!(this.listener == null && (this.cache == null || this.ssrc == -1))) {
            this.dataLength = buffer.getLength();
            if (this.dataLength > 0) {
                if (this.data == null || this.data.length < this.dataLength) {
                    this.data = new byte[this.dataLength];
                }
                Object bufferData = buffer.getData();
                if (bufferData != null) {
                    System.arraycopy(bufferData, buffer.getOffset(), this.data, 0, this.dataLength);
                }
                if (this.thread == null) {
                    startThread();
                } else {
                    notify();
                }
            }
        }
    }

    public synchronized void setAudioLevelListener(SimpleAudioLevelListener listener) {
        if (this.listener != listener) {
            this.listener = listener;
            startOrNotifyThread();
        }
    }

    public synchronized void setAudioLevelCache(AudioLevelMap cache, long ssrc) {
        if (!(this.cache == cache && this.ssrc == ssrc)) {
            this.cache = cache;
            this.ssrc = ssrc;
            startOrNotifyThread();
        }
    }

    private synchronized void startOrNotifyThread() {
        if (this.listener == null && (this.cache == null || this.ssrc == -1)) {
            this.thread = null;
            notify();
        } else if (this.data != null && this.dataLength > 0) {
            if (this.thread == null) {
                startThread();
            } else {
                notify();
            }
        }
    }

    /* access modifiers changed from: private|declared_synchronized */
    public synchronized void startThread() {
        this.thread = new Thread() {
            public void run() {
                try {
                    AudioLevelEventDispatcher.this.run();
                    synchronized (AudioLevelEventDispatcher.this) {
                        if (Thread.currentThread().equals(AudioLevelEventDispatcher.this.thread)) {
                            AudioLevelEventDispatcher.this.thread = null;
                        }
                        if (AudioLevelEventDispatcher.this.thread == null && !((AudioLevelEventDispatcher.this.listener == null && (AudioLevelEventDispatcher.this.cache == null || AudioLevelEventDispatcher.this.ssrc == -1)) || AudioLevelEventDispatcher.this.data == null || AudioLevelEventDispatcher.this.dataLength <= 0)) {
                            AudioLevelEventDispatcher.this.startThread();
                        }
                    }
                } catch (Throwable th) {
                    synchronized (AudioLevelEventDispatcher.this) {
                        if (Thread.currentThread().equals(AudioLevelEventDispatcher.this.thread)) {
                            AudioLevelEventDispatcher.this.thread = null;
                        }
                        if (AudioLevelEventDispatcher.this.thread == null && !((AudioLevelEventDispatcher.this.listener == null && (AudioLevelEventDispatcher.this.cache == null || AudioLevelEventDispatcher.this.ssrc == -1)) || AudioLevelEventDispatcher.this.data == null || AudioLevelEventDispatcher.this.dataLength <= 0)) {
                            AudioLevelEventDispatcher.this.startThread();
                        }
                    }
                }
            }
        };
        this.thread.setDaemon(true);
        if (this.threadName != null) {
            this.thread.setName(this.threadName);
        }
        this.thread.start();
    }
}
