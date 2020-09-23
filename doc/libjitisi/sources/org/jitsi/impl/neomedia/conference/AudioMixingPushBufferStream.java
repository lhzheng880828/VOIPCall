package org.jitsi.impl.neomedia.conference;

import java.io.IOException;
import java.util.Arrays;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.format.UnsupportedFormatException;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushBufferStream;
import org.jitsi.impl.neomedia.ArrayIOUtils;
import org.jitsi.impl.neomedia.control.ControlsAdapter;
import org.jitsi.util.Logger;

public class AudioMixingPushBufferStream extends ControlsAdapter implements PushBufferStream {
    private static final Logger logger = Logger.getLogger(AudioMixingPushBufferStream.class);
    private final AudioMixerPushBufferStream audioMixerStream;
    private final AudioMixingPushBufferDataSource dataSource;
    private short[][] inSamples;
    private int maxInSampleCount;
    private short[] outSamples;
    private final Object readSyncRoot = new Object();
    private long timeStamp;
    private BufferTransferHandler transferHandler;

    private static int getMaxOutSample(AudioFormat outFormat) throws UnsupportedFormatException {
        switch (outFormat.getSampleSizeInBits()) {
            case 8:
                return 127;
            case 16:
                return 32767;
            case 32:
                return Integer.MAX_VALUE;
            default:
                throw new UnsupportedFormatException("Format.getSampleSizeInBits()", outFormat);
        }
    }

    AudioMixingPushBufferStream(AudioMixerPushBufferStream audioMixerStream, AudioMixingPushBufferDataSource dataSource) {
        this.audioMixerStream = audioMixerStream;
        this.dataSource = dataSource;
    }

    private short[] allocateOutSamples(int minSize) {
        short[] outSamples = this.outSamples;
        if (outSamples != null && outSamples.length >= minSize) {
            return outSamples;
        }
        outSamples = new short[minSize];
        this.outSamples = outSamples;
        return outSamples;
    }

    public boolean endOfStream() {
        return this.audioMixerStream.endOfStream();
    }

    public ContentDescriptor getContentDescriptor() {
        return this.audioMixerStream.getContentDescriptor();
    }

    public long getContentLength() {
        return this.audioMixerStream.getContentLength();
    }

    public AudioMixingPushBufferDataSource getDataSource() {
        return this.dataSource;
    }

    public AudioFormat getFormat() {
        return this.audioMixerStream.getFormat();
    }

    private short[] mix(short[][] inSamples, AudioFormat outFormat, int outSampleCount) {
        short[] inStreamSamples;
        int inStreamSampleCount;
        short[] outSamples;
        if (inSamples.length == 1 || inSamples[1] == null) {
            inStreamSamples = inSamples[0];
            if (inStreamSamples == null) {
                inStreamSampleCount = 0;
                outSamples = allocateOutSamples(outSampleCount);
            } else if (inStreamSamples.length < outSampleCount) {
                inStreamSampleCount = inStreamSamples.length;
                outSamples = allocateOutSamples(outSampleCount);
                System.arraycopy(inStreamSamples, 0, outSamples, 0, inStreamSampleCount);
            } else {
                inStreamSampleCount = outSampleCount;
                outSamples = inStreamSamples;
            }
            if (inStreamSampleCount != outSampleCount) {
                Arrays.fill(outSamples, inStreamSampleCount, outSampleCount, (short) 0);
            }
            return outSamples;
        }
        outSamples = allocateOutSamples(outSampleCount);
        Arrays.fill(outSamples, 0, outSampleCount, (short) 0);
        try {
            int maxOutSample = getMaxOutSample(outFormat);
            for (short[] inStreamSamples2 : inSamples) {
                if (inStreamSamples2 != null) {
                    inStreamSampleCount = Math.min(inStreamSamples2.length, outSampleCount);
                    if (inStreamSampleCount != 0) {
                        for (int i = 0; i < inStreamSampleCount; i++) {
                            int inStreamSample = inStreamSamples2[i];
                            int outSample = outSamples[i];
                            outSamples[i] = (short) ((inStreamSample + outSample) - Math.round(((float) inStreamSample) * (((float) outSample) / ((float) maxOutSample))));
                        }
                    }
                }
            }
            short[] sArr = outSamples;
            return outSamples;
        } catch (UnsupportedFormatException ufex) {
            throw new UnsupportedOperationException(ufex);
        }
    }

    public void read(Buffer buffer) throws IOException {
        short[][] inSamples;
        int maxInSampleCount;
        long timeStamp;
        synchronized (this.readSyncRoot) {
            inSamples = this.inSamples;
            maxInSampleCount = this.maxInSampleCount;
            timeStamp = this.timeStamp;
            this.inSamples = (short[][]) null;
            this.maxInSampleCount = 0;
            this.timeStamp = -1;
        }
        if (inSamples == null || inSamples.length == 0 || maxInSampleCount <= 0) {
            buffer.setDiscard(true);
            return;
        }
        AudioFormat outFormat = getFormat();
        short[] outSamples = mix(inSamples, outFormat, maxInSampleCount);
        int outSampleCount = Math.min(maxInSampleCount, outSamples.length);
        if (Format.byteArray.equals(outFormat.getDataType())) {
            Object o = buffer.getData();
            byte[] outData = null;
            if (o instanceof byte[]) {
                outData = (byte[]) o;
            }
            switch (outFormat.getSampleSizeInBits()) {
                case 16:
                    int outLength = outSampleCount * 2;
                    if (outData == null || outData.length < outLength) {
                        outData = new byte[outLength];
                    }
                    for (int i = 0; i < outSampleCount; i++) {
                        ArrayIOUtils.writeShort(outSamples[i], outData, i * 2);
                    }
                    buffer.setData(outData);
                    buffer.setFormat(outFormat);
                    buffer.setLength(outLength);
                    buffer.setOffset(0);
                    buffer.setTimeStamp(timeStamp);
                    return;
                default:
                    throw new UnsupportedOperationException("AudioMixingPushBufferStream.read(Buffer)");
            }
        }
        throw new UnsupportedOperationException("AudioMixingPushBufferStream.read(Buffer)");
    }

    /* access modifiers changed from: 0000 */
    public void setInSamples(short[][] inSamples, int maxInSampleCount, long timeStamp) {
        synchronized (this.readSyncRoot) {
            this.inSamples = inSamples;
            this.maxInSampleCount = maxInSampleCount;
        }
        BufferTransferHandler transferHandler = this.transferHandler;
        if (transferHandler != null) {
            transferHandler.transferData(this);
        }
    }

    public void setTransferHandler(BufferTransferHandler transferHandler) {
        this.transferHandler = transferHandler;
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized void start() throws IOException {
        this.audioMixerStream.addOutStream(this);
        if (logger.isTraceEnabled()) {
            logger.trace("Started " + getClass().getSimpleName() + " with hashCode " + hashCode());
        }
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized void stop() throws IOException {
        this.audioMixerStream.removeOutStream(this);
        if (logger.isTraceEnabled()) {
            logger.trace("Stopped " + getClass().getSimpleName() + " with hashCode " + hashCode());
        }
    }
}
