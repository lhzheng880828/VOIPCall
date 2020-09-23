package org.jitsi.impl.neomedia.quicktime;

public class QTCaptureDecompressedVideoOutput extends QTCaptureOutput {

    public static abstract class Delegate {
        private MutableQTSampleBuffer sampleBuffer;
        private MutableCVPixelBuffer videoFrame;

        public abstract void outputVideoFrameWithSampleBuffer(CVImageBuffer cVImageBuffer, QTSampleBuffer qTSampleBuffer);

        /* access modifiers changed from: 0000 */
        public void outputVideoFrameWithSampleBuffer(long videoFramePtr, long sampleBufferPtr) {
            if (this.videoFrame == null) {
                this.videoFrame = new MutableCVPixelBuffer(videoFramePtr);
            } else {
                this.videoFrame.setPtr(videoFramePtr);
            }
            if (this.sampleBuffer == null) {
                this.sampleBuffer = new MutableQTSampleBuffer(sampleBufferPtr);
            } else {
                this.sampleBuffer.setPtr(sampleBufferPtr);
            }
            outputVideoFrameWithSampleBuffer(this.videoFrame, this.sampleBuffer);
        }
    }

    private static class MutableCVPixelBuffer extends CVPixelBuffer {
        private MutableCVPixelBuffer(long ptr) {
            super(ptr);
        }

        public void setPtr(long ptr) {
            super.setPtr(ptr);
        }
    }

    private static class MutableQTSampleBuffer extends QTSampleBuffer {
        private MutableQTSampleBuffer(long ptr) {
            super(ptr);
        }

        public void setPtr(long ptr) {
            super.setPtr(ptr);
        }
    }

    private static native long allocAndInit();

    private static native double minimumVideoFrameInterval(long j);

    private static native long pixelBufferAttributes(long j);

    private static native boolean setAutomaticallyDropsLateVideoFrames(long j, boolean z);

    private static native void setDelegate(long j, Delegate delegate);

    private static native void setMinimumVideoFrameInterval(long j, double d);

    private static native void setPixelBufferAttributes(long j, long j2);

    public QTCaptureDecompressedVideoOutput() {
        this(allocAndInit());
    }

    public QTCaptureDecompressedVideoOutput(long ptr) {
        super(ptr);
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        release();
    }

    public double minimumVideoFrameInterval() {
        return minimumVideoFrameInterval(getPtr());
    }

    public NSDictionary pixelBufferAttributes() {
        long pixelBufferAttributesPtr = pixelBufferAttributes(getPtr());
        return pixelBufferAttributesPtr == 0 ? null : new NSDictionary(pixelBufferAttributesPtr);
    }

    public boolean setAutomaticallyDropsLateVideoFrames(boolean automaticallyDropsLateVideoFrames) {
        return setAutomaticallyDropsLateVideoFrames(getPtr(), automaticallyDropsLateVideoFrames);
    }

    public void setDelegate(Delegate delegate) {
        setDelegate(getPtr(), delegate);
    }

    public void setMinimumVideoFrameInterval(double minimumVideoFrameInterval) {
        setMinimumVideoFrameInterval(getPtr(), minimumVideoFrameInterval);
    }

    public void setPixelBufferAttributes(NSDictionary pixelBufferAttributes) {
        setPixelBufferAttributes(getPtr(), pixelBufferAttributes.getPtr());
    }
}
