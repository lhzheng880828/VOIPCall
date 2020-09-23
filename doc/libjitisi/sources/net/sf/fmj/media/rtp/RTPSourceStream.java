package net.sf.fmj.media.rtp;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.control.BufferControl;
import javax.media.control.PacketQueueControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushBufferStream;
import net.sf.fmj.media.Log;
import net.sf.fmj.media.protocol.BasicSourceStream;
import net.sf.fmj.media.protocol.BufferListener;
import net.sf.fmj.media.protocol.rtp.DataSource;
import net.sf.fmj.media.rtp.util.RTPMediaThread;

public class RTPSourceStream extends BasicSourceStream implements PushBufferStream, Runnable {
    private BufferControlImpl bc = null;
    private JitterBufferBehaviour behaviour;
    private boolean bufferWhenStopped = true;
    private Format format;
    private boolean hasRead = false;
    private boolean killed = false;
    private long lastSeqRecv = Buffer.SEQUENCE_UNKNOWN;
    private long lastSeqSent = Buffer.SEQUENCE_UNKNOWN;
    final JitterBuffer q;
    private final Object startSyncRoot = new Object();
    private boolean started = false;
    final JitterBufferStats stats;
    private RTPMediaThread thread;
    private BufferTransferHandler transferHandler;

    public RTPSourceStream(DataSource datasource) {
        datasource.setSourceStream(this);
        this.q = new JitterBuffer(4);
        this.stats = new JitterBufferStats(this);
        setBehaviour(null);
        createThread();
    }

    /* JADX WARNING: Missing block: B:64:?, code skipped:
            return;
     */
    public void add(javax.media.Buffer r19, boolean r20, net.sf.fmj.media.rtp.RTPRawReceiver r21) {
        /*
        r18 = this;
        r0 = r18;
        r11 = r0.started;
        if (r11 != 0) goto L_0x000d;
    L_0x0006:
        r0 = r18;
        r11 = r0.bufferWhenStopped;
        if (r11 != 0) goto L_0x000d;
    L_0x000c:
        return;
    L_0x000d:
        r6 = r19.getSequenceNumber();
        r0 = r18;
        r12 = r0.q;
        monitor-enter(r12);
        r0 = r18;
        r14 = r0.lastSeqRecv;	 Catch:{ all -> 0x006c }
        r14 = r14 - r6;
        r16 = 256; // 0x100 float:3.59E-43 double:1.265E-321;
        r11 = (r14 > r16 ? 1 : (r14 == r16 ? 0 : -1));
        if (r11 <= 0) goto L_0x004c;
    L_0x0021:
        r11 = new java.lang.StringBuilder;	 Catch:{ all -> 0x006c }
        r11.<init>();	 Catch:{ all -> 0x006c }
        r13 = "Resetting queue, last seq added: ";
        r11 = r11.append(r13);	 Catch:{ all -> 0x006c }
        r0 = r18;
        r14 = r0.lastSeqRecv;	 Catch:{ all -> 0x006c }
        r11 = r11.append(r14);	 Catch:{ all -> 0x006c }
        r13 = ", current seq: ";
        r11 = r11.append(r13);	 Catch:{ all -> 0x006c }
        r11 = r11.append(r6);	 Catch:{ all -> 0x006c }
        r11 = r11.toString();	 Catch:{ all -> 0x006c }
        net.sf.fmj.media.Log.info(r11);	 Catch:{ all -> 0x006c }
        r18.reset();	 Catch:{ all -> 0x006c }
        r0 = r18;
        r0.lastSeqRecv = r6;	 Catch:{ all -> 0x006c }
    L_0x004c:
        r0 = r18;
        r11 = r0.stats;	 Catch:{ all -> 0x006c }
        r11.updateMaxSizeReached();	 Catch:{ all -> 0x006c }
        r0 = r18;
        r11 = r0.stats;	 Catch:{ all -> 0x006c }
        r0 = r19;
        r11.updateSizePerPacket(r0);	 Catch:{ all -> 0x006c }
        r0 = r18;
        r11 = r0.behaviour;	 Catch:{ all -> 0x006c }
        r0 = r19;
        r1 = r21;
        r11 = r11.preAdd(r0, r1);	 Catch:{ all -> 0x006c }
        if (r11 != 0) goto L_0x006f;
    L_0x006a:
        monitor-exit(r12);	 Catch:{ all -> 0x006c }
        goto L_0x000c;
    L_0x006c:
        r11 = move-exception;
        monitor-exit(r12);	 Catch:{ all -> 0x006c }
        throw r11;
    L_0x006f:
        r0 = r18;
        r11 = r0.stats;	 Catch:{ all -> 0x006c }
        r11.incrementNbAdd();	 Catch:{ all -> 0x006c }
        r0 = r18;
        r0.lastSeqRecv = r6;	 Catch:{ all -> 0x006c }
        r3 = 0;
        r0 = r18;
        r11 = r0.q;	 Catch:{ all -> 0x006c }
        r11 = r11.noMoreFree();	 Catch:{ all -> 0x006c }
        if (r11 == 0) goto L_0x00ab;
    L_0x0085:
        r0 = r18;
        r11 = r0.stats;	 Catch:{ all -> 0x006c }
        r11.incrementDiscardedFull();	 Catch:{ all -> 0x006c }
        r0 = r18;
        r11 = r0.q;	 Catch:{ all -> 0x006c }
        r8 = r11.getFirstSeq();	 Catch:{ all -> 0x006c }
        r14 = 9223372036854775806; // 0x7ffffffffffffffe float:NaN double:NaN;
        r11 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1));
        if (r11 == 0) goto L_0x00a4;
    L_0x009d:
        r11 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
        if (r11 >= 0) goto L_0x00a4;
    L_0x00a1:
        monitor-exit(r12);	 Catch:{ all -> 0x006c }
        goto L_0x000c;
    L_0x00a4:
        r0 = r18;
        r11 = r0.behaviour;	 Catch:{ all -> 0x006c }
        r11.dropPkt();	 Catch:{ all -> 0x006c }
    L_0x00ab:
        r0 = r18;
        r11 = r0.q;	 Catch:{ all -> 0x006c }
        r11 = r11.getFreeCount();	 Catch:{ all -> 0x006c }
        r13 = 1;
        if (r11 > r13) goto L_0x00b7;
    L_0x00b6:
        r3 = 1;
    L_0x00b7:
        r0 = r18;
        r11 = r0.q;	 Catch:{ all -> 0x006c }
        r5 = r11.getFree();	 Catch:{ all -> 0x006c }
        r2 = 0;
        r11 = r19.getData();	 Catch:{ all -> 0x0130 }
        r11 = (byte[]) r11;	 Catch:{ all -> 0x0130 }
        r0 = r11;
        r0 = (byte[]) r0;	 Catch:{ all -> 0x0130 }
        r4 = r0;
        r11 = r5.getData();	 Catch:{ all -> 0x0130 }
        r11 = (byte[]) r11;	 Catch:{ all -> 0x0130 }
        r0 = r11;
        r0 = (byte[]) r0;	 Catch:{ all -> 0x0130 }
        r10 = r0;
        if (r10 == 0) goto L_0x00da;
    L_0x00d6:
        r11 = r10.length;	 Catch:{ all -> 0x0130 }
        r13 = r4.length;	 Catch:{ all -> 0x0130 }
        if (r11 >= r13) goto L_0x00dd;
    L_0x00da:
        r11 = r4.length;	 Catch:{ all -> 0x0130 }
        r10 = new byte[r11];	 Catch:{ all -> 0x0130 }
    L_0x00dd:
        r11 = r19.getOffset();	 Catch:{ all -> 0x0130 }
        r13 = r19.getOffset();	 Catch:{ all -> 0x0130 }
        r14 = r19.getLength();	 Catch:{ all -> 0x0130 }
        java.lang.System.arraycopy(r4, r11, r10, r13, r14);	 Catch:{ all -> 0x0130 }
        r0 = r19;
        r5.copy(r0);	 Catch:{ all -> 0x0130 }
        r5.setData(r10);	 Catch:{ all -> 0x0130 }
        if (r3 == 0) goto L_0x0126;
    L_0x00f6:
        r11 = r5.getFlags();	 Catch:{ all -> 0x0130 }
        r11 = r11 | 8192;
        r11 = r11 | 32;
        r5.setFlags(r11);	 Catch:{ all -> 0x0130 }
    L_0x0101:
        r0 = r18;
        r11 = r0.q;	 Catch:{ all -> 0x0130 }
        r11.addPkt(r5);	 Catch:{ all -> 0x0130 }
        r2 = 1;
        if (r2 != 0) goto L_0x0112;
    L_0x010b:
        r0 = r18;
        r11 = r0.q;	 Catch:{ all -> 0x006c }
        r11.returnFree(r5);	 Catch:{ all -> 0x006c }
    L_0x0112:
        r0 = r18;
        r11 = r0.behaviour;	 Catch:{ all -> 0x006c }
        r11 = r11.willReadBlock();	 Catch:{ all -> 0x006c }
        if (r11 != 0) goto L_0x0123;
    L_0x011c:
        r0 = r18;
        r11 = r0.q;	 Catch:{ all -> 0x006c }
        r11.notifyAll();	 Catch:{ all -> 0x006c }
    L_0x0123:
        monitor-exit(r12);	 Catch:{ all -> 0x006c }
        goto L_0x000c;
    L_0x0126:
        r11 = r5.getFlags();	 Catch:{ all -> 0x0130 }
        r11 = r11 | 32;
        r5.setFlags(r11);	 Catch:{ all -> 0x0130 }
        goto L_0x0101;
    L_0x0130:
        r11 = move-exception;
        if (r2 != 0) goto L_0x013a;
    L_0x0133:
        r0 = r18;
        r13 = r0.q;	 Catch:{ all -> 0x006c }
        r13.returnFree(r5);	 Catch:{ all -> 0x006c }
    L_0x013a:
        throw r11;	 Catch:{ all -> 0x006c }
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.rtp.RTPSourceStream.add(javax.media.Buffer, boolean, net.sf.fmj.media.rtp.RTPRawReceiver):void");
    }

    public void close() {
        if (!this.killed) {
            this.stats.printStats();
            stop();
            this.killed = true;
            synchronized (this.startSyncRoot) {
                this.startSyncRoot.notifyAll();
            }
            synchronized (this.q) {
                this.q.notifyAll();
            }
            this.thread = null;
            if (this.bc != null) {
                this.bc.removeSourceStream(this);
            }
        }
    }

    public void connect() {
        this.killed = false;
        createThread();
    }

    private void createThread() {
        if (this.thread == null) {
            this.thread = new RTPMediaThread(this, "RTPStream");
            this.thread.useControlPriority();
            this.thread.start();
        }
    }

    /* access modifiers changed from: 0000 */
    public JitterBufferBehaviour getBehaviour() {
        return this.behaviour;
    }

    /* access modifiers changed from: 0000 */
    public BufferControlImpl getBufferControl() {
        return this.bc;
    }

    public Object getControl(String controlType) {
        return PacketQueueControl.class.getName().equals(controlType) ? this.stats : super.getControl(controlType);
    }

    public Object[] getControls() {
        Object[] superControls = super.getControls();
        Object[] thisControls = new Object[(superControls.length + 1)];
        System.arraycopy(superControls, 0, thisControls, 0, superControls.length);
        thisControls[superControls.length] = this.stats;
        return thisControls;
    }

    public Format getFormat() {
        return this.format;
    }

    /* access modifiers changed from: 0000 */
    public long getLastReadSequenceNumber() {
        return this.lastSeqSent;
    }

    public void prebuffer() {
    }

    public void read(Buffer buffer) {
        synchronized (this.q) {
            try {
                this.behaviour.read(buffer);
                if (!buffer.isDiscard()) {
                    this.lastSeqSent = buffer.getSequenceNumber();
                }
                if (!buffer.isDiscard()) {
                    this.hasRead = true;
                    this.q.notifyAll();
                }
            } catch (Throwable th) {
                if (!buffer.isDiscard()) {
                    this.hasRead = true;
                    this.q.notifyAll();
                }
            }
        }
    }

    public void reset() {
        synchronized (this.q) {
            this.stats.incrementNbReset();
            resetQ();
            this.behaviour.reset();
            this.lastSeqSent = Buffer.SEQUENCE_UNKNOWN;
        }
    }

    public void resetQ() {
        Log.comment("Resetting the RTP packet queue");
        synchronized (this.q) {
            while (this.q.fillNotEmpty()) {
                this.stats.incrementDiscardedReset();
                this.behaviour.dropPkt();
            }
            this.q.notifyAll();
        }
    }

    public void run() {
        do {
            try {
                synchronized (this.startSyncRoot) {
                    if (this.killed || this.started) {
                        synchronized (this.q) {
                            if (this.killed || this.hasRead || !this.behaviour.willReadBlock()) {
                                this.hasRead = false;
                                BufferTransferHandler transferHandler = this.transferHandler;
                                if (transferHandler != null) {
                                    transferHandler.transferData(this);
                                }
                            } else {
                                this.q.wait();
                            }
                        }
                    } else {
                        this.startSyncRoot.wait();
                    }
                }
            } catch (InterruptedException ie) {
                Log.error("Thread " + ie.getMessage());
            }
        } while (!this.killed);
    }

    private void setBehaviour(JitterBufferBehaviour behaviour) {
        if (behaviour == null) {
            if (!(this.behaviour instanceof BasicJitterBufferBehaviour)) {
                behaviour = new BasicJitterBufferBehaviour(this);
            } else {
                return;
            }
        }
        if (this.behaviour != behaviour) {
            this.behaviour = behaviour;
        }
    }

    public void setBufferControl(BufferControl buffercontrol) {
        this.bc = (BufferControlImpl) buffercontrol;
        updateBuffer(this.bc.getBufferLength());
        updateThreshold(this.bc.getMinimumThreshold());
    }

    public void setBufferListener(BufferListener bufferlistener) {
    }

    public void setBufferWhenStopped(boolean flag) {
        this.bufferWhenStopped = flag;
    }

    /* access modifiers changed from: 0000 */
    public void setContentDescriptor(String s) {
        this.contentDescriptor = new ContentDescriptor(s);
    }

    /* access modifiers changed from: protected */
    public void setFormat(Format format) {
        if (this.format != format) {
            JitterBufferBehaviour behaviour;
            this.format = format;
            if (this.format instanceof AudioFormat) {
                behaviour = new AudioJitterBufferBehaviour(this);
            } else if (this.format instanceof VideoFormat) {
                behaviour = new VideoJitterBufferBehaviour(this);
            } else {
                behaviour = null;
            }
            setBehaviour(behaviour);
        }
    }

    public void setTransferHandler(BufferTransferHandler transferHandler) {
        this.transferHandler = transferHandler;
    }

    public void start() {
        Log.info("Starting RTPSourceStream.");
        synchronized (this.startSyncRoot) {
            this.started = true;
            this.startSyncRoot.notifyAll();
        }
        synchronized (this.q) {
            this.q.notifyAll();
        }
    }

    public void stop() {
        Log.info("Stopping RTPSourceStream.");
        synchronized (this.startSyncRoot) {
            this.started = false;
            if (!this.bufferWhenStopped) {
                reset();
            }
        }
        synchronized (this.q) {
            this.q.notifyAll();
        }
    }

    public long updateBuffer(long l) {
        return l;
    }

    public long updateThreshold(long l) {
        return l;
    }
}
