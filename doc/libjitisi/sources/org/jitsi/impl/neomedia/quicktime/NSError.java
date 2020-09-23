package org.jitsi.impl.neomedia.quicktime;

public class NSError extends NSObject {
    public NSError(long ptr) {
        super(ptr);
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        release();
    }
}
