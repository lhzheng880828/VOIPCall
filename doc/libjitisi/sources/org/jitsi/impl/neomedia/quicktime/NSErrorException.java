package org.jitsi.impl.neomedia.quicktime;

public class NSErrorException extends Exception {
    private final NSError error;

    public NSErrorException(long errorPtr) {
        this(new NSError(errorPtr));
    }

    public NSErrorException(NSError error) {
        this.error = error;
    }

    public NSError getError() {
        return this.error;
    }
}
