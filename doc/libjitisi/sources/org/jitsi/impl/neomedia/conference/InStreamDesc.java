package org.jitsi.impl.neomedia.conference;

import java.lang.ref.SoftReference;
import javax.media.Buffer;
import javax.media.protocol.SourceStream;
import org.jitsi.util.Logger;

class InStreamDesc {
    private SoftReference<Buffer> buffer;
    public final InDataSourceDesc inDataSourceDesc;
    private SourceStream inStream;
    private long nonContributingReadCount;

    public InStreamDesc(SourceStream inStream, InDataSourceDesc inDataSourceDesc) {
        this.inStream = inStream;
        this.inDataSourceDesc = inDataSourceDesc;
    }

    public Buffer getBuffer(boolean create) {
        Buffer buffer = this.buffer == null ? null : (Buffer) this.buffer.get();
        if (buffer != null || !create) {
            return buffer;
        }
        buffer = new Buffer();
        setBuffer(buffer);
        return buffer;
    }

    public SourceStream getInStream() {
        return this.inStream;
    }

    public AudioMixingPushBufferDataSource getOutDataSource() {
        return this.inDataSourceDesc.outDataSource;
    }

    /* access modifiers changed from: 0000 */
    public void incrementNonContributingReadCount(Logger logger) {
        if (logger.isTraceEnabled()) {
            this.nonContributingReadCount++;
            if (this.nonContributingReadCount >= 0) {
                logger.trace("Failed to read actual inputSamples more than " + this.nonContributingReadCount + " times from inputStream with hash code " + getInStream().hashCode());
                this.nonContributingReadCount = 0;
            }
        }
    }

    public void setBuffer(Buffer buffer) {
        this.buffer = buffer == null ? null : new SoftReference(buffer);
    }

    public void setInStream(SourceStream inStream) {
        if (this.inStream != inStream) {
            this.inStream = inStream;
            setBuffer(null);
        }
    }
}
