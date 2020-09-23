package org.jitsi.impl.neomedia.quicktime;

public class CVImageBuffer {
    private long ptr;

    static {
        System.loadLibrary("jnquicktime");
    }

    public CVImageBuffer(long ptr) {
        setPtr(ptr);
    }

    /* access modifiers changed from: protected */
    public long getPtr() {
        return this.ptr;
    }

    /* access modifiers changed from: protected */
    public void setPtr(long ptr) {
        if (ptr == 0) {
            throw new IllegalArgumentException("ptr");
        }
        this.ptr = ptr;
    }
}
