package org.jitsi.impl.neomedia.quicktime;

public class QTSampleBuffer extends NSObject {
    private static native byte[] bytesForAllSamples(long j);

    private static native long formatDescription(long j);

    public QTSampleBuffer(long ptr) {
        super(ptr);
    }

    public byte[] bytesForAllSamples() {
        return bytesForAllSamples(getPtr());
    }

    public QTFormatDescription formatDescription() {
        long formatDescriptionPtr = formatDescription(getPtr());
        return formatDescriptionPtr == 0 ? null : new QTFormatDescription(formatDescriptionPtr);
    }
}
