package org.jitsi.impl.neomedia.jmfext.media.protocol;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jitsi.impl.neomedia.codec.video.ByteBuffer;

public class ByteBufferPool {
    private final List<PooledByteBuffer> buffers = new ArrayList();

    private static class PooledByteBuffer extends ByteBuffer {
        private final WeakReference<ByteBufferPool> pool;

        public PooledByteBuffer(int capacity, ByteBufferPool pool) {
            super(capacity);
            this.pool = new WeakReference(pool);
        }

        /* access modifiers changed from: 0000 */
        public void doFree() {
            super.free();
        }

        public void free() {
            ByteBufferPool pool = (ByteBufferPool) this.pool.get();
            if (pool == null) {
                doFree();
            } else {
                pool.returnBuffer(this);
            }
        }
    }

    public synchronized void drain() {
        Iterator<PooledByteBuffer> i = this.buffers.iterator();
        while (i.hasNext()) {
            PooledByteBuffer buffer = (PooledByteBuffer) i.next();
            i.remove();
            buffer.doFree();
        }
    }

    /* JADX WARNING: Missing block: B:14:0x0029, code skipped:
            return r1;
     */
    public synchronized org.jitsi.impl.neomedia.codec.video.ByteBuffer getBuffer(int r6) {
        /*
        r5 = this;
        monitor-enter(r5);
        r6 = r6 + 8;
        r1 = 0;
        r4 = r5.buffers;	 Catch:{ all -> 0x002a }
        r3 = r4.iterator();	 Catch:{ all -> 0x002a }
    L_0x000a:
        r4 = r3.hasNext();	 Catch:{ all -> 0x002a }
        if (r4 == 0) goto L_0x0032;
    L_0x0010:
        r0 = r3.next();	 Catch:{ all -> 0x002a }
        r0 = (org.jitsi.impl.neomedia.codec.video.ByteBuffer) r0;	 Catch:{ all -> 0x002a }
        r4 = r0.getCapacity();	 Catch:{ all -> 0x002a }
        if (r4 < r6) goto L_0x000a;
    L_0x001c:
        r3.remove();	 Catch:{ all -> 0x002a }
        r1 = r0;
        r2 = r1;
    L_0x0021:
        if (r2 != 0) goto L_0x0030;
    L_0x0023:
        r1 = new org.jitsi.impl.neomedia.jmfext.media.protocol.ByteBufferPool$PooledByteBuffer;	 Catch:{ all -> 0x002d }
        r1.m2433init(r6, r5);	 Catch:{ all -> 0x002d }
    L_0x0028:
        monitor-exit(r5);
        return r1;
    L_0x002a:
        r4 = move-exception;
    L_0x002b:
        monitor-exit(r5);
        throw r4;
    L_0x002d:
        r4 = move-exception;
        r1 = r2;
        goto L_0x002b;
    L_0x0030:
        r1 = r2;
        goto L_0x0028;
    L_0x0032:
        r2 = r1;
        goto L_0x0021;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.jmfext.media.protocol.ByteBufferPool.getBuffer(int):org.jitsi.impl.neomedia.codec.video.ByteBuffer");
    }

    /* access modifiers changed from: private|declared_synchronized */
    public synchronized void returnBuffer(PooledByteBuffer buffer) {
        if (!this.buffers.contains(buffer)) {
            this.buffers.add(buffer);
        }
    }
}
