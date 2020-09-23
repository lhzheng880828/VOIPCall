package org.jitsi.impl.neomedia.quicktime;

public class NSObject {
    private long ptr;

    public static native void release(long j);

    static native void retain(long j);

    static {
        System.loadLibrary("jnquicktime");
    }

    public NSObject(long ptr) {
        setPtr(ptr);
    }

    public long getPtr() {
        return this.ptr;
    }

    public void release() {
        release(this.ptr);
    }

    public void retain() {
        retain(this.ptr);
    }

    /* access modifiers changed from: protected */
    public void setPtr(long ptr) {
        if (ptr == 0) {
            throw new IllegalArgumentException("ptr");
        }
        this.ptr = ptr;
    }
}
