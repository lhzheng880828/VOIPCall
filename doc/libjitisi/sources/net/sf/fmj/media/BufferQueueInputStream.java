package net.sf.fmj.media;

import com.lti.utils.UnsignedUtils;
import com.lti.utils.synchronization.ProducerConsumerQueue;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.Buffer;
import net.sf.fmj.utility.LoggerSingleton;

public class BufferQueueInputStream extends InputStream {
    private static final int DEFAULT_QUEUE_SIZE = 20;
    private static final Logger logger = LoggerSingleton.logger;
    private int available;
    private Buffer buffer;
    private final ProducerConsumerQueue q;
    private boolean trace;

    public BufferQueueInputStream() {
        this(20);
    }

    public BufferQueueInputStream(int qSize) {
        this.trace = false;
        this.available = 0;
        this.q = new ProducerConsumerQueue(qSize);
    }

    public BufferQueueInputStream(ProducerConsumerQueue q) {
        this.trace = false;
        this.available = 0;
        this.q = q;
    }

    public int available() {
        int i;
        synchronized (this.q) {
            if (this.trace) {
                logger.fine(this + " available: available=" + this.available);
            }
            i = this.available;
        }
        return i;
    }

    public void blockingPut(Buffer b) {
        blockingPut(b, true);
    }

    public void blockingPut(Buffer b, boolean clone) {
        put(b, true, clone);
    }

    private void fillBuffer() throws IOException {
        try {
            synchronized (this.q) {
                while (true) {
                    if (this.buffer != null) {
                        if (this.buffer.isEOM()) {
                            return;
                        } else if (this.buffer.getLength() > 0) {
                            return;
                        }
                    }
                    this.buffer = (Buffer) this.q.get();
                    if (this.trace) {
                        logger.fine(this + " Getting buffer: " + this.buffer.getLength());
                    }
                    if (this.buffer.getLength() == 0 && !this.buffer.isDiscard() && this.trace) {
                        logger.fine("Skipping zero-length buffer in queue");
                    }
                    if (!this.buffer.isDiscard() && this.buffer.getLength() != 0) {
                        return;
                    }
                }
            }
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "" + e, e);
            throw new InterruptedIOException();
        }
    }

    public boolean put(Buffer b) {
        return put(b, true);
    }

    public boolean put(Buffer b, boolean clone) {
        return put(b, false, clone);
    }

    /* JADX WARNING: Missing block: B:55:?, code skipped:
            return true;
     */
    private boolean put(javax.media.Buffer r7, boolean r8, boolean r9) {
        /*
        r6 = this;
        r2 = 1;
        r1 = r7.getLength();
        r3 = -1;
        if (r1 != r3) goto L_0x000e;
    L_0x0008:
        r1 = new java.lang.IllegalArgumentException;
        r1.<init>();
        throw r1;
    L_0x000e:
        r1 = r7.getData();
        r1 = r1 instanceof byte[];
        if (r1 != 0) goto L_0x0033;
    L_0x0016:
        r1 = new java.lang.IllegalArgumentException;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "Expected byte array: ";
        r2 = r2.append(r3);
        r3 = r7.getData();
        r2 = r2.append(r3);
        r2 = r2.toString();
        r1.<init>(r2);
        throw r1;
    L_0x0033:
        r1 = r7.isEOM();
        if (r1 == 0) goto L_0x0082;
    L_0x0039:
        r1 = logger;
        r3 = "putting EOM buffer";
        r1.fine(r3);
    L_0x0040:
        r1 = r6.trace;
        if (r1 == 0) goto L_0x0072;
    L_0x0044:
        r1 = logger;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r3 = r3.append(r6);
        r4 = " BufferQueueInputStream.put: Putting buffer, length=";
        r3 = r3.append(r4);
        r4 = r7.getLength();
        r3 = r3.append(r4);
        r4 = " eom=";
        r3 = r3.append(r4);
        r4 = r7.isEOM();
        r3 = r3.append(r4);
        r3 = r3.toString();
        r1.fine(r3);
    L_0x0072:
        r3 = r6.q;	 Catch:{ InterruptedException -> 0x00e8 }
        monitor-enter(r3);	 Catch:{ InterruptedException -> 0x00e8 }
        if (r8 != 0) goto L_0x00a8;
    L_0x0077:
        r1 = r6.q;	 Catch:{ all -> 0x00e5 }
        r1 = r1.isFull();	 Catch:{ all -> 0x00e5 }
        if (r1 == 0) goto L_0x00a8;
    L_0x007f:
        r1 = 0;
        monitor-exit(r3);	 Catch:{ all -> 0x00e5 }
    L_0x0081:
        return r1;
    L_0x0082:
        r1 = r7.getLength();
        if (r1 != 0) goto L_0x0095;
    L_0x0088:
        r1 = r6.trace;
        if (r1 == 0) goto L_0x0093;
    L_0x008c:
        r1 = logger;
        r3 = "Skipping zero length buffer, not adding to queue";
        r1.fine(r3);
    L_0x0093:
        r1 = r2;
        goto L_0x0081;
    L_0x0095:
        r1 = r7.isDiscard();
        if (r1 == 0) goto L_0x0040;
    L_0x009b:
        r1 = r6.trace;
        if (r1 == 0) goto L_0x00a6;
    L_0x009f:
        r1 = logger;
        r3 = "Skipping discard buffer, not adding to queue";
        r1.fine(r3);
    L_0x00a6:
        r1 = r2;
        goto L_0x0081;
    L_0x00a8:
        r1 = r6.available;	 Catch:{ all -> 0x00e5 }
        r4 = r7.getLength();	 Catch:{ all -> 0x00e5 }
        r1 = r1 + r4;
        r6.available = r1;	 Catch:{ all -> 0x00e5 }
        r4 = r6.q;	 Catch:{ all -> 0x00e5 }
        if (r9 == 0) goto L_0x00e3;
    L_0x00b5:
        r1 = r7.clone();	 Catch:{ all -> 0x00e5 }
        r1 = (javax.media.Buffer) r1;	 Catch:{ all -> 0x00e5 }
    L_0x00bb:
        r4.put(r1);	 Catch:{ all -> 0x00e5 }
        r1 = r6.trace;	 Catch:{ all -> 0x00e5 }
        if (r1 == 0) goto L_0x00e0;
    L_0x00c2:
        r1 = logger;	 Catch:{ all -> 0x00e5 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00e5 }
        r4.<init>();	 Catch:{ all -> 0x00e5 }
        r4 = r4.append(r6);	 Catch:{ all -> 0x00e5 }
        r5 = " put: available=";
        r4 = r4.append(r5);	 Catch:{ all -> 0x00e5 }
        r5 = r6.available;	 Catch:{ all -> 0x00e5 }
        r4 = r4.append(r5);	 Catch:{ all -> 0x00e5 }
        r4 = r4.toString();	 Catch:{ all -> 0x00e5 }
        r1.fine(r4);	 Catch:{ all -> 0x00e5 }
    L_0x00e0:
        monitor-exit(r3);	 Catch:{ all -> 0x00e5 }
        r1 = r2;
        goto L_0x0081;
    L_0x00e3:
        r1 = r7;
        goto L_0x00bb;
    L_0x00e5:
        r1 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x00e5 }
        throw r1;	 Catch:{ InterruptedException -> 0x00e8 }
    L_0x00e8:
        r0 = move-exception;
        r1 = logger;
        r2 = java.util.logging.Level.WARNING;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "";
        r3 = r3.append(r4);
        r3 = r3.append(r0);
        r3 = r3.toString();
        r1.log(r2, r3, r0);
        r1 = new java.lang.RuntimeException;
        r1.<init>(r0);
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.BufferQueueInputStream.put(javax.media.Buffer, boolean, boolean):boolean");
    }

    public int read() throws IOException {
        fillBuffer();
        if (this.buffer.getLength() > 0 || !this.buffer.isEOM()) {
            int result = ((byte[]) this.buffer.getData())[this.buffer.getOffset()] & UnsignedUtils.MAX_UBYTE;
            this.buffer.setOffset(this.buffer.getOffset() + 1);
            this.buffer.setLength(this.buffer.getLength() - 1);
            synchronized (this.q) {
                this.available--;
                if (this.trace) {
                    logger.fine(this + " read: available=" + this.available);
                }
            }
            if (!this.trace) {
                return result;
            }
            logger.fine(this + " BufferQueueInputStream.read: returning " + result);
            return result;
        }
        if (this.trace) {
            logger.fine(this + " BufferQueueInputStream.read: returning -1");
        }
        return -1;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        fillBuffer();
        if (this.buffer.getLength() > 0 || !this.buffer.isEOM()) {
            byte[] data = (byte[]) this.buffer.getData();
            if (data == null) {
                throw new NullPointerException("Buffer has null data.  length=" + this.buffer.getLength() + " offset=" + this.buffer.getOffset());
            }
            int lengthToCopy;
            if (this.buffer.getLength() < len) {
                lengthToCopy = this.buffer.getLength();
            } else {
                lengthToCopy = len;
            }
            if (this.trace) {
                logger.fine(this + " BufferQueueInputStream.read: lengthToCopy=" + lengthToCopy + " buffer.getLength()=" + this.buffer.getLength() + " buffer.getOffset()=" + this.buffer.getOffset() + " b.length=" + b.length + " len=" + len + " off=" + off);
            }
            System.arraycopy(data, this.buffer.getOffset(), b, off, lengthToCopy);
            this.buffer.setOffset(this.buffer.getOffset() + lengthToCopy);
            this.buffer.setLength(this.buffer.getLength() - lengthToCopy);
            synchronized (this.q) {
                this.available -= lengthToCopy;
                if (this.trace) {
                    logger.fine(this + " read: available=" + this.available);
                }
            }
            if (!this.trace) {
                return lengthToCopy;
            }
            logger.fine(this + " BufferQueueInputStream.read[]: returning " + lengthToCopy);
            return lengthToCopy;
        }
        if (this.trace) {
            logger.fine(this + " BufferQueueInputStream.read: returning -1");
        }
        return -1;
    }

    public void setTrace(boolean value) {
        this.trace = value;
    }
}
