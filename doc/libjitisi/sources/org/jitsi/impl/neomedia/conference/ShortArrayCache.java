package org.jitsi.impl.neomedia.conference;

import java.lang.ref.SoftReference;
import javax.media.Buffer;

class ShortArrayCache {
    private SoftReference<short[][]> elements;
    private int length;

    ShortArrayCache() {
    }

    public synchronized short[] allocateShortArray(int minSize) {
        short[] element;
        short[][] elements = this.elements == null ? (short[][]) null : (short[][]) this.elements.get();
        if (elements != null) {
            for (int i = 0; i < this.length; i++) {
                element = elements[i];
                if (element != null && element.length >= minSize) {
                    elements[i] = null;
                    break;
                }
            }
        }
        element = new short[minSize];
        return element;
    }

    /* JADX WARNING: Missing block: B:8:0x0011, code skipped:
            if (r1 == null) goto L_0x0013;
     */
    public synchronized void deallocateShortArray(short[] r9) {
        /*
        r8 = this;
        monitor-enter(r8);
        if (r9 != 0) goto L_0x0005;
    L_0x0003:
        monitor-exit(r8);
        return;
    L_0x0005:
        r5 = r8.elements;	 Catch:{ all -> 0x006c }
        if (r5 == 0) goto L_0x0013;
    L_0x0009:
        r5 = r8.elements;	 Catch:{ all -> 0x006c }
        r1 = r5.get();	 Catch:{ all -> 0x006c }
        r1 = (short[][]) r1;	 Catch:{ all -> 0x006c }
        if (r1 != 0) goto L_0x0021;
    L_0x0013:
        r5 = 8;
        r1 = new short[r5][];	 Catch:{ all -> 0x006c }
        r5 = new java.lang.ref.SoftReference;	 Catch:{ all -> 0x006c }
        r5.<init>(r1);	 Catch:{ all -> 0x006c }
        r8.elements = r5;	 Catch:{ all -> 0x006c }
        r5 = 0;
        r8.length = r5;	 Catch:{ all -> 0x006c }
    L_0x0021:
        r5 = r8.length;	 Catch:{ all -> 0x006c }
        if (r5 == 0) goto L_0x0031;
    L_0x0025:
        r2 = 0;
    L_0x0026:
        r5 = r8.length;	 Catch:{ all -> 0x006c }
        if (r2 >= r5) goto L_0x0031;
    L_0x002a:
        r5 = r1[r2];	 Catch:{ all -> 0x006c }
        if (r5 == r9) goto L_0x0003;
    L_0x002e:
        r2 = r2 + 1;
        goto L_0x0026;
    L_0x0031:
        r5 = r8.length;	 Catch:{ all -> 0x006c }
        r6 = r1.length;	 Catch:{ all -> 0x006c }
        if (r5 != r6) goto L_0x0063;
    L_0x0036:
        r4 = 0;
        r2 = 0;
    L_0x0038:
        r5 = r8.length;	 Catch:{ all -> 0x006c }
        if (r2 >= r5) goto L_0x004c;
    L_0x003c:
        r0 = r1[r2];	 Catch:{ all -> 0x006c }
        if (r0 == 0) goto L_0x0049;
    L_0x0040:
        if (r2 == r4) goto L_0x0047;
    L_0x0042:
        r1[r4] = r0;	 Catch:{ all -> 0x006c }
        r5 = 0;
        r1[r2] = r5;	 Catch:{ all -> 0x006c }
    L_0x0047:
        r4 = r4 + 1;
    L_0x0049:
        r2 = r2 + 1;
        goto L_0x0038;
    L_0x004c:
        r5 = r8.length;	 Catch:{ all -> 0x006c }
        if (r4 != r5) goto L_0x006f;
    L_0x0050:
        r5 = r1.length;	 Catch:{ all -> 0x006c }
        r5 = r5 + 4;
        r3 = new short[r5][];	 Catch:{ all -> 0x006c }
        r5 = 0;
        r6 = 0;
        r7 = r1.length;	 Catch:{ all -> 0x006c }
        java.lang.System.arraycopy(r1, r5, r3, r6, r7);	 Catch:{ all -> 0x006c }
        r1 = r3;
        r5 = new java.lang.ref.SoftReference;	 Catch:{ all -> 0x006c }
        r5.<init>(r1);	 Catch:{ all -> 0x006c }
        r8.elements = r5;	 Catch:{ all -> 0x006c }
    L_0x0063:
        r5 = r8.length;	 Catch:{ all -> 0x006c }
        r6 = r5 + 1;
        r8.length = r6;	 Catch:{ all -> 0x006c }
        r1[r5] = r9;	 Catch:{ all -> 0x006c }
        goto L_0x0003;
    L_0x006c:
        r5 = move-exception;
        monitor-exit(r8);
        throw r5;
    L_0x006f:
        r8.length = r4;	 Catch:{ all -> 0x006c }
        goto L_0x0063;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.conference.ShortArrayCache.deallocateShortArray(short[]):void");
    }

    public short[] validateShortArraySize(Buffer buffer, int newSize) {
        short[] shortArray;
        Object data = buffer.getData();
        if (data instanceof short[]) {
            shortArray = (short[]) data;
            if (shortArray.length < newSize) {
                deallocateShortArray(shortArray);
                shortArray = null;
            }
        } else {
            shortArray = null;
        }
        if (shortArray != null) {
            return shortArray;
        }
        shortArray = allocateShortArray(newSize);
        buffer.setData(shortArray);
        return shortArray;
    }
}
