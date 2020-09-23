package org.jitsi.impl.neomedia.quicktime;

public class NSMutableDictionary extends NSDictionary {
    private static native long allocAndInit();

    private static native void setIntForKey(long j, int i, long j2);

    public NSMutableDictionary() {
        this(allocAndInit());
    }

    public NSMutableDictionary(long ptr) {
        super(ptr);
    }

    public void setIntForKey(int value, long key) {
        setIntForKey(getPtr(), value, key);
    }
}
