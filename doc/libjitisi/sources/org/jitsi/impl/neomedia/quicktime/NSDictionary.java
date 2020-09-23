package org.jitsi.impl.neomedia.quicktime;

public class NSDictionary extends NSObject {
    private static native int intForKey(long j, long j2);

    public NSDictionary(long ptr) {
        super(ptr);
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        release();
    }

    public int intForKey(long key) {
        return intForKey(getPtr(), key);
    }
}
