package org.jitsi.impl.neomedia.conference;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.control.BufferControl;
import javax.media.format.AudioFormat;
import javax.media.format.UnsupportedFormatException;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferStream;
import javax.media.protocol.PushBufferStream;
import javax.media.protocol.SourceStream;
import org.jitsi.impl.neomedia.ArrayIOUtils;
import org.jitsi.impl.neomedia.control.ControlsAdapter;
import org.jitsi.impl.neomedia.protocol.CachingPushBufferStream;
import org.jitsi.impl.neomedia.protocol.StreamSubstituteBufferTransferHandler;
import org.jitsi.util.Logger;

class AudioMixerPushBufferStream extends ControlsAdapter implements PushBufferStream {
    static final long TRACE_NON_CONTRIBUTING_READ_COUNT = 0;
    private static final Logger logger = Logger.getLogger(AudioMixerPushBufferStream.class);
    private final AudioMixer audioMixer;
    private InStreamDesc[] inStreams;
    private final Object inStreamsSyncRoot = new Object();
    private AudioFormat lastReadInFormat;
    private final AudioFormat outFormat;
    private final List<AudioMixingPushBufferStream> outStreams = new ArrayList();
    private long outStreamsGeneration;
    private final ShortArrayCache shortArrayCache = new ShortArrayCache();
    private final BufferTransferHandler transferHandler = new BufferTransferHandler() {
        private final Buffer buffer = new Buffer();

        public void transferData(PushBufferStream stream) {
            this.buffer.setLength(0);
            AudioMixerPushBufferStream.this.transferData(this.buffer);
            this.buffer.setLength(0);
        }
    };
    private AudioMixingPushBufferStream[] unmodifiableOutStreams;

    private static class InSampleDesc {
        private SoftReference<Buffer> buffer;
        /* access modifiers changed from: private|final */
        public final AudioFormat format;
        public final short[][] inSamples;
        public final InStreamDesc[] inStreams;
        private long timeStamp = -1;

        public InSampleDesc(short[][] inSamples, InStreamDesc[] inStreams, AudioFormat format) {
            this.inSamples = inSamples;
            this.inStreams = inStreams;
            this.format = format;
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

        public long getTimeStamp() {
            return this.timeStamp;
        }

        public void setBuffer(Buffer buffer) {
            this.buffer = buffer == null ? null : new SoftReference(buffer);
        }

        public void setTimeStamp(long timeStamp) {
            if (this.timeStamp == -1) {
                this.timeStamp = timeStamp;
                return;
            }
            throw new IllegalStateException("timeStamp");
        }
    }

    public AudioMixerPushBufferStream(AudioMixer audioMixer, AudioFormat outFormat) {
        this.audioMixer = audioMixer;
        this.outFormat = outFormat;
    }

    /* access modifiers changed from: 0000 */
    public void addOutStream(AudioMixingPushBufferStream outStream) throws IOException {
        if (outStream == null) {
            throw new IllegalArgumentException("outStream");
        }
        boolean start = false;
        long generation = 0;
        synchronized (this.outStreams) {
            if (!this.outStreams.contains(outStream) && this.outStreams.add(outStream)) {
                this.unmodifiableOutStreams = null;
                if (this.outStreams.size() == 1) {
                    start = true;
                    long generation2 = this.outStreamsGeneration + 1;
                    this.outStreamsGeneration = generation2;
                    generation = generation2;
                }
            }
        }
        if (start) {
            this.audioMixer.start(this, generation);
        }
    }

    /* JADX WARNING: Missing block: B:20:?, code skipped:
            return true;
     */
    public boolean endOfStream() {
        /*
        r6 = this;
        r5 = r6.inStreamsSyncRoot;
        monitor-enter(r5);
        r4 = r6.inStreams;	 Catch:{ all -> 0x0022 }
        if (r4 == 0) goto L_0x001f;
    L_0x0007:
        r0 = r6.inStreams;	 Catch:{ all -> 0x0022 }
        r3 = r0.length;	 Catch:{ all -> 0x0022 }
        r1 = 0;
    L_0x000b:
        if (r1 >= r3) goto L_0x001f;
    L_0x000d:
        r2 = r0[r1];	 Catch:{ all -> 0x0022 }
        r4 = r2.getInStream();	 Catch:{ all -> 0x0022 }
        r4 = r4.endOfStream();	 Catch:{ all -> 0x0022 }
        if (r4 != 0) goto L_0x001c;
    L_0x0019:
        r4 = 0;
        monitor-exit(r5);	 Catch:{ all -> 0x0022 }
    L_0x001b:
        return r4;
    L_0x001c:
        r1 = r1 + 1;
        goto L_0x000b;
    L_0x001f:
        monitor-exit(r5);	 Catch:{ all -> 0x0022 }
        r4 = 1;
        goto L_0x001b;
    L_0x0022:
        r4 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x0022 }
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.conference.AudioMixerPushBufferStream.endOfStream():boolean");
    }

    /* access modifiers changed from: 0000 */
    /* JADX WARNING: Missing block: B:27:?, code skipped:
            return;
     */
    public void equalizeInStreamBufferLength() {
        /*
        r8 = this;
        r6 = r8.inStreamsSyncRoot;
        monitor-enter(r6);
        r5 = r8.inStreams;	 Catch:{ all -> 0x0039 }
        if (r5 == 0) goto L_0x000d;
    L_0x0007:
        r5 = r8.inStreams;	 Catch:{ all -> 0x0039 }
        r5 = r5.length;	 Catch:{ all -> 0x0039 }
        r7 = 1;
        if (r5 >= r7) goto L_0x000f;
    L_0x000d:
        monitor-exit(r6);	 Catch:{ all -> 0x0039 }
    L_0x000e:
        return;
    L_0x000f:
        r5 = r8.inStreams;	 Catch:{ all -> 0x0039 }
        r7 = 0;
        r5 = r5[r7];	 Catch:{ all -> 0x0039 }
        r0 = r8.getBufferControl(r5);	 Catch:{ all -> 0x0039 }
        if (r0 != 0) goto L_0x0032;
    L_0x001a:
        r2 = 20;
    L_0x001c:
        r1 = 1;
    L_0x001d:
        r5 = r8.inStreams;	 Catch:{ all -> 0x0039 }
        r5 = r5.length;	 Catch:{ all -> 0x0039 }
        if (r1 >= r5) goto L_0x0037;
    L_0x0022:
        r5 = r8.inStreams;	 Catch:{ all -> 0x0039 }
        r5 = r5[r1];	 Catch:{ all -> 0x0039 }
        r4 = r8.getBufferControl(r5);	 Catch:{ all -> 0x0039 }
        if (r4 == 0) goto L_0x002f;
    L_0x002c:
        r4.setBufferLength(r2);	 Catch:{ all -> 0x0039 }
    L_0x002f:
        r1 = r1 + 1;
        goto L_0x001d;
    L_0x0032:
        r2 = r0.getBufferLength();	 Catch:{ all -> 0x0039 }
        goto L_0x001c;
    L_0x0037:
        monitor-exit(r6);	 Catch:{ all -> 0x0039 }
        goto L_0x000e;
    L_0x0039:
        r5 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x0039 }
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.conference.AudioMixerPushBufferStream.equalizeInStreamBufferLength():void");
    }

    private BufferControl getBufferControl(InStreamDesc inStreamDesc) {
        BufferControl bufferControl;
        InDataSourceDesc inDataSourceDesc = inStreamDesc.inDataSourceDesc;
        DataSource effectiveInDataSource = inDataSourceDesc.getEffectiveInDataSource();
        String bufferControlType = BufferControl.class.getName();
        if (effectiveInDataSource != null) {
            bufferControl = (BufferControl) effectiveInDataSource.getControl(bufferControlType);
            if (bufferControl != null) {
                return bufferControl;
            }
        }
        DataSource inDataSource = inDataSourceDesc.inDataSource;
        if (!(inDataSource == null || inDataSource == effectiveInDataSource)) {
            bufferControl = (BufferControl) inDataSource.getControl(bufferControlType);
            if (bufferControl != null) {
                return bufferControl;
            }
        }
        return (BufferControl) inStreamDesc.getInStream().getControl(bufferControlType);
    }

    public ContentDescriptor getContentDescriptor() {
        return new ContentDescriptor(this.audioMixer.getContentType());
    }

    public long getContentLength() {
        Throwable th;
        long contentLength = 0;
        synchronized (this.inStreamsSyncRoot) {
            try {
                if (this.inStreams != null) {
                    for (InStreamDesc inStreamDesc : this.inStreams) {
                        long inContentLength = inStreamDesc.getInStream().getContentLength();
                        if (-1 == inContentLength) {
                            return -1;
                        }
                        if (contentLength < inContentLength) {
                            contentLength = inContentLength;
                        }
                    }
                }
                long contentLength2 = contentLength;
                try {
                    contentLength = contentLength2;
                    return contentLength2;
                } catch (Throwable th2) {
                    th = th2;
                    contentLength = contentLength2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public AudioFormat getFormat() {
        return this.outFormat;
    }

    /* access modifiers changed from: 0000 */
    public InStreamDesc[] getInStreams() {
        InStreamDesc[] inStreamDescArr;
        synchronized (this.inStreamsSyncRoot) {
            inStreamDescArr = this.inStreams == null ? null : (InStreamDesc[]) this.inStreams.clone();
        }
        return inStreamDescArr;
    }

    /* JADX WARNING: Missing block: B:26:?, code skipped:
            r7 = readInPushBufferStreams(r0, r2);
     */
    /* JADX WARNING: Missing block: B:27:0x0044, code skipped:
            r7 = java.lang.Math.max(r7, readInPullBufferStreams(r0, r7, r2));
            r15.setData(r2);
            r15.setLength(r7);
            r8 = r2.getTimeStamp();
     */
    /* JADX WARNING: Missing block: B:28:0x005b, code skipped:
            if (r8 == -1) goto L_?;
     */
    /* JADX WARNING: Missing block: B:29:0x005d, code skipped:
            r15.setTimeStamp(r8);
     */
    /* JADX WARNING: Missing block: B:36:0x0069, code skipped:
            r10 = move-exception;
     */
    /* JADX WARNING: Missing block: B:37:0x006a, code skipped:
            r6 = new java.io.IOException();
            r6.initCause(r10);
     */
    /* JADX WARNING: Missing block: B:38:0x0072, code skipped:
            throw r6;
     */
    /* JADX WARNING: Missing block: B:44:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:45:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:46:?, code skipped:
            return;
     */
    public void read(javax.media.Buffer r15) throws java.io.IOException {
        /*
        r14 = this;
        r0 = r14.getFormat();
        r12 = r14.inStreamsSyncRoot;
        monitor-enter(r12);
        r5 = r14.inStreams;	 Catch:{ all -> 0x0066 }
        if (r5 == 0) goto L_0x000e;
    L_0x000b:
        r11 = r5.length;	 Catch:{ all -> 0x0066 }
        if (r11 != 0) goto L_0x0010;
    L_0x000e:
        monitor-exit(r12);	 Catch:{ all -> 0x0066 }
    L_0x000f:
        return;
    L_0x0010:
        r2 = r15.getData();	 Catch:{ all -> 0x0066 }
        r2 = (org.jitsi.impl.neomedia.conference.AudioMixerPushBufferStream.InSampleDesc) r2;	 Catch:{ all -> 0x0066 }
        if (r2 == 0) goto L_0x001f;
    L_0x0018:
        r11 = r2.format;	 Catch:{ all -> 0x0066 }
        if (r11 == r0) goto L_0x001f;
    L_0x001e:
        r2 = 0;
    L_0x001f:
        r4 = r5.length;	 Catch:{ all -> 0x0066 }
        if (r2 == 0) goto L_0x0031;
    L_0x0022:
        r3 = r2.inStreams;	 Catch:{ all -> 0x0066 }
        r11 = r3.length;	 Catch:{ all -> 0x0066 }
        if (r11 != r4) goto L_0x0064;
    L_0x0027:
        r1 = 0;
    L_0x0028:
        if (r1 >= r4) goto L_0x0031;
    L_0x002a:
        r11 = r3[r1];	 Catch:{ all -> 0x0066 }
        r13 = r5[r1];	 Catch:{ all -> 0x0066 }
        if (r11 == r13) goto L_0x0061;
    L_0x0030:
        r2 = 0;
    L_0x0031:
        if (r2 != 0) goto L_0x0040;
    L_0x0033:
        r2 = new org.jitsi.impl.neomedia.conference.AudioMixerPushBufferStream$InSampleDesc;	 Catch:{ all -> 0x0066 }
        r13 = new short[r4][];	 Catch:{ all -> 0x0066 }
        r11 = r5.clone();	 Catch:{ all -> 0x0066 }
        r11 = (org.jitsi.impl.neomedia.conference.InStreamDesc[]) r11;	 Catch:{ all -> 0x0066 }
        r2.m2278init(r13, r11, r0);	 Catch:{ all -> 0x0066 }
    L_0x0040:
        monitor-exit(r12);	 Catch:{ all -> 0x0066 }
        r7 = r14.readInPushBufferStreams(r0, r2);	 Catch:{ UnsupportedFormatException -> 0x0069 }
        r11 = r14.readInPullBufferStreams(r0, r7, r2);
        r7 = java.lang.Math.max(r7, r11);
        r15.setData(r2);
        r15.setLength(r7);
        r8 = r2.getTimeStamp();
        r12 = -1;
        r11 = (r8 > r12 ? 1 : (r8 == r12 ? 0 : -1));
        if (r11 == 0) goto L_0x000f;
    L_0x005d:
        r15.setTimeStamp(r8);
        goto L_0x000f;
    L_0x0061:
        r1 = r1 + 1;
        goto L_0x0028;
    L_0x0064:
        r2 = 0;
        goto L_0x0031;
    L_0x0066:
        r11 = move-exception;
        monitor-exit(r12);	 Catch:{ all -> 0x0066 }
        throw r11;
    L_0x0069:
        r10 = move-exception;
        r6 = new java.io.IOException;
        r6.<init>();
        r6.initCause(r10);
        throw r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.conference.AudioMixerPushBufferStream.read(javax.media.Buffer):void");
    }

    private int readInPullBufferStreams(AudioFormat outFormat, int outSampleCount, InSampleDesc inSampleDesc) throws IOException {
        for (InStreamDesc inStream : inSampleDesc.inStreams) {
            if (inStream.getInStream() instanceof PullBufferStream) {
                throw new UnsupportedOperationException(AudioMixerPushBufferStream.class.getSimpleName() + ".readInPullBufferStreams" + "(AudioFormat,int,InSampleDesc)");
            }
        }
        return 0;
    }

    private void readInPushBufferStream(InStreamDesc inStreamDesc, AudioFormat outFormat, int sampleCount, Buffer outBuffer) throws IOException, UnsupportedFormatException {
        PushBufferStream inStream = (PushBufferStream) inStreamDesc.getInStream();
        Format inStreamFormat = (AudioFormat) inStream.getFormat();
        Buffer inBuffer = inStreamDesc.getBuffer(true);
        if (sampleCount != 0) {
            if (Format.byteArray.equals(inStreamFormat.getDataType())) {
                Object data = inBuffer.getData();
                int length = sampleCount * (inStreamFormat.getSampleSizeInBits() / 8);
                if (!((data instanceof byte[]) && ((byte[]) data).length == length)) {
                    inBuffer.setData(new byte[length]);
                }
            } else {
                throw new UnsupportedFormatException("!Format.getDataType().equals(byte[].class)", inStreamFormat);
            }
        }
        inBuffer.setFlags(0);
        inBuffer.setLength(0);
        inBuffer.setOffset(0);
        this.audioMixer.read(inStream, inBuffer, inStreamDesc.inDataSourceDesc.inDataSource);
        if (inBuffer.isDiscard()) {
            outBuffer.setDiscard(true);
            return;
        }
        int inLength = inBuffer.getLength();
        if (inLength <= 0) {
            outBuffer.setDiscard(true);
            return;
        }
        AudioFormat inFormat = (AudioFormat) inBuffer.getFormat();
        if (inFormat == null) {
            inFormat = inStreamFormat;
        }
        if (logger.isTraceEnabled()) {
            if (this.lastReadInFormat == null) {
                this.lastReadInFormat = inFormat;
            } else if (!this.lastReadInFormat.matches(inFormat)) {
                this.lastReadInFormat = inFormat;
                logger.trace("Read inSamples in different format " + this.lastReadInFormat);
            }
        }
        int inFormatSigned = inFormat.getSigned();
        if (inFormatSigned == 1 || inFormatSigned == -1) {
            int inChannels = inFormat.getChannels();
            int outChannels = outFormat.getChannels();
            if (inChannels == outChannels || inChannels == -1 || outChannels == -1) {
                double inSampleRate = inFormat.getSampleRate();
                double outSampleRate = outFormat.getSampleRate();
                if (inSampleRate != outSampleRate) {
                    logger.warn("Read inFormat with sampleRate " + inSampleRate + " while expected outFormat sampleRate is " + outSampleRate);
                }
                Object inData = inBuffer.getData();
                if (inData == null) {
                    outBuffer.setDiscard(true);
                    return;
                } else if (inData instanceof byte[]) {
                    int inSampleSizeInBits = inFormat.getSampleSizeInBits();
                    int outSampleSizeInBits = outFormat.getSampleSizeInBits();
                    if (logger.isTraceEnabled() && inSampleSizeInBits != outSampleSizeInBits) {
                        logger.trace("Read inFormat with sampleSizeInBits " + inSampleSizeInBits + ". Will convert to sampleSizeInBits " + outSampleSizeInBits);
                    }
                    byte[] inSamples = (byte[]) inData;
                    switch (inSampleSizeInBits) {
                        case 16:
                            int outLength = inLength / 2;
                            short[] outSamples = this.shortArrayCache.validateShortArraySize(outBuffer, outLength);
                            switch (outSampleSizeInBits) {
                                case 16:
                                    for (int i = 0; i < outLength; i++) {
                                        outSamples[i] = ArrayIOUtils.readShort(inSamples, i * 2);
                                    }
                                    outBuffer.setFlags(inBuffer.getFlags());
                                    outBuffer.setFormat(outFormat);
                                    outBuffer.setLength(outLength);
                                    outBuffer.setOffset(0);
                                    outBuffer.setTimeStamp(inBuffer.getTimeStamp());
                                    return;
                                default:
                                    throw new UnsupportedFormatException("AudioFormat.getSampleSizeInBits()", outFormat);
                            }
                        default:
                            throw new UnsupportedFormatException("AudioFormat.getSampleSizeInBits()", inFormat);
                    }
                } else {
                    throw new UnsupportedFormatException("Format.getDataType().equals(" + inData.getClass() + ")", inFormat);
                }
            }
            logger.error("Read inFormat with channels " + inChannels + " while expected outFormat channels is " + outChannels);
            throw new UnsupportedFormatException("AudioFormat.getChannels()", inFormat);
        }
        throw new UnsupportedFormatException("AudioFormat.getSigned()", inFormat);
    }

    private int readInPushBufferStreams(AudioFormat outFormat, InSampleDesc inSampleDesc) throws IOException, UnsupportedFormatException {
        InStreamDesc[] inStreams = inSampleDesc.inStreams;
        Buffer buffer = inSampleDesc.getBuffer(true);
        int maxInSampleCount = 0;
        short[][] inSamples = inSampleDesc.inSamples;
        for (int i = 0; i < inStreams.length; i++) {
            InStreamDesc inStreamDesc = inStreams[i];
            if (inStreamDesc.getInStream() instanceof PushBufferStream) {
                int sampleCount;
                short[] samples;
                buffer.setDiscard(false);
                buffer.setFlags(0);
                buffer.setLength(0);
                buffer.setOffset(0);
                readInPushBufferStream(inStreamDesc, outFormat, maxInSampleCount, buffer);
                if (buffer.isDiscard()) {
                    sampleCount = 0;
                    samples = null;
                } else {
                    sampleCount = buffer.getLength();
                    if (sampleCount <= 0) {
                        sampleCount = 0;
                        samples = null;
                    } else {
                        samples = (short[]) buffer.getData();
                    }
                }
                if (sampleCount != 0) {
                    buffer.setData(null);
                    if (samples.length > sampleCount) {
                        Arrays.fill(samples, sampleCount, samples.length, (short) 0);
                    }
                    inSamples[i] = (buffer.getFlags() & 4) == 0 ? samples : null;
                    if (maxInSampleCount < samples.length) {
                        maxInSampleCount = samples.length;
                    }
                    if (inSampleDesc.getTimeStamp() == -1) {
                        inSampleDesc.setTimeStamp(buffer.getTimeStamp());
                    }
                }
            }
            inSamples[i] = null;
        }
        return maxInSampleCount;
    }

    /* access modifiers changed from: 0000 */
    public void removeOutStream(AudioMixingPushBufferStream outStream) throws IOException {
        boolean stop = false;
        long generation = 0;
        synchronized (this.outStreams) {
            if (outStream != null) {
                if (this.outStreams.remove(outStream)) {
                    this.unmodifiableOutStreams = null;
                    if (this.outStreams.isEmpty()) {
                        stop = true;
                        long generation2 = this.outStreamsGeneration + 1;
                        this.outStreamsGeneration = generation2;
                        generation = generation2;
                    }
                }
            }
        }
        if (stop) {
            this.audioMixer.stop(this, generation);
        }
    }

    private void setInSamples(AudioMixingPushBufferStream outStream, InSampleDesc inSampleDesc, int maxInSampleCount) {
        short[][] inSamples = inSampleDesc.inSamples;
        InStreamDesc[] inStreams = inSampleDesc.inStreams;
        inSamples = (short[][]) inSamples.clone();
        DataSource captureDevice = this.audioMixer.captureDevice;
        AudioMixingPushBufferDataSource outDataSource = outStream.getDataSource();
        boolean outDataSourceIsSendingDTMF = captureDevice instanceof AudioMixingPushBufferDataSource ? outDataSource.isSendingDTMF() : false;
        boolean outDataSourceIsMute = outDataSource.isMute();
        int o = 0;
        for (int i = 0; i < inSamples.length; i++) {
            InStreamDesc inStreamDesc = inStreams[i];
            DataSource inDataSource = inStreamDesc.inDataSourceDesc.inDataSource;
            if (outDataSourceIsSendingDTMF && inDataSource == captureDevice) {
                AudioFormat inStreamFormat = (AudioFormat) ((PushBufferStream) inStreamDesc.getInStream()).getFormat();
                short[] nextToneSignal = outDataSource.getNextToneSignal(inStreamFormat.getSampleRate(), inStreamFormat.getSampleSizeInBits());
                inSamples[i] = nextToneSignal;
                if (maxInSampleCount < nextToneSignal.length) {
                    maxInSampleCount = nextToneSignal.length;
                }
            } else if (outDataSource.equals(inStreamDesc.getOutDataSource()) || (outDataSourceIsMute && inDataSource == captureDevice)) {
                inSamples[i] = null;
            }
            short[] inStreamSamples = inSamples[i];
            if (inStreamSamples != null) {
                if (i != o) {
                    inSamples[o] = inStreamSamples;
                    inSamples[i] = null;
                }
                o++;
            }
        }
        outStream.setInSamples(inSamples, maxInSampleCount, inSampleDesc.getTimeStamp());
    }

    /* access modifiers changed from: 0000 */
    public void setInStreams(Collection<InStreamDesc> inStreams) {
        InStreamDesc[] oldValue;
        InStreamDesc[] newValue = inStreams == null ? null : (InStreamDesc[]) inStreams.toArray(new InStreamDesc[inStreams.size()]);
        synchronized (this.inStreamsSyncRoot) {
            oldValue = this.inStreams;
            this.inStreams = newValue;
        }
        if (!Arrays.equals(oldValue, newValue)) {
            if (oldValue != null) {
                setTransferHandler(oldValue, null);
            }
            if (newValue != null) {
                boolean skippedForTransferHandler = false;
                for (InStreamDesc inStreamDesc : newValue) {
                    SourceStream inStream = inStreamDesc.getInStream();
                    if (inStream instanceof PushBufferStream) {
                        if (!skippedForTransferHandler) {
                            skippedForTransferHandler = true;
                        } else if (!(inStream instanceof CachingPushBufferStream)) {
                            PushBufferStream cachingInStream = new CachingPushBufferStream((PushBufferStream) inStream);
                            inStreamDesc.setInStream(cachingInStream);
                            if (logger.isTraceEnabled()) {
                                logger.trace("Created CachingPushBufferStream with hashCode " + cachingInStream.hashCode() + " for inStream with hashCode " + inStream.hashCode());
                            }
                        }
                    }
                }
                setTransferHandler(newValue, this.transferHandler);
                equalizeInStreamBufferLength();
                if (logger.isTraceEnabled()) {
                    int oldValueLength = oldValue == null ? 0 : oldValue.length;
                    int newValueLength = newValue == null ? 0 : newValue.length;
                    int difference = newValueLength - oldValueLength;
                    if (difference > 0) {
                        logger.trace("Added " + difference + " inStream(s) and the total is " + newValueLength);
                    } else if (difference < 0) {
                        logger.trace("Removed " + difference + " inStream(s) and the total is " + newValueLength);
                    }
                }
            }
        }
    }

    public void setTransferHandler(BufferTransferHandler transferHandler) {
        throw new UnsupportedOperationException(AudioMixerPushBufferStream.class.getSimpleName() + ".setTransferHandler(BufferTransferHandler)");
    }

    private void setTransferHandler(InStreamDesc[] inStreams, BufferTransferHandler transferHandler) {
        if (inStreams != null && inStreams.length > 0) {
            boolean transferHandlerIsSet = false;
            for (InStreamDesc inStreamDesc : inStreams) {
                SourceStream inStream = inStreamDesc.getInStream();
                if (inStream instanceof PushBufferStream) {
                    BufferTransferHandler inStreamTransferHandler;
                    PushBufferStream inPushBufferStream = (PushBufferStream) inStream;
                    if (transferHandler == null) {
                        inStreamTransferHandler = null;
                    } else if (transferHandlerIsSet) {
                        inStreamTransferHandler = new BufferTransferHandler() {
                            public void transferData(PushBufferStream stream) {
                            }
                        };
                    } else {
                        inStreamTransferHandler = new StreamSubstituteBufferTransferHandler(transferHandler, inPushBufferStream, this);
                    }
                    inPushBufferStream.setTransferHandler(inStreamTransferHandler);
                    transferHandlerIsSet = true;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void transferData(Buffer buffer) {
        try {
            read(buffer);
            InSampleDesc inSampleDesc = (InSampleDesc) buffer.getData();
            short[][] inSamples = inSampleDesc.inSamples;
            int maxInSampleCount = buffer.getLength();
            if (inSamples != null && inSamples.length != 0 && maxInSampleCount > 0) {
                synchronized (this.outStreams) {
                    AudioMixingPushBufferStream[] outStreams = this.unmodifiableOutStreams;
                    if (outStreams == null) {
                        outStreams = (AudioMixingPushBufferStream[]) this.outStreams.toArray(new AudioMixingPushBufferStream[this.outStreams.size()]);
                        this.unmodifiableOutStreams = outStreams;
                    }
                }
                for (AudioMixingPushBufferStream outStream : outStreams) {
                    setInSamples(outStream, inSampleDesc, maxInSampleCount);
                }
                for (int i = 0; i < inSamples.length; i++) {
                    this.shortArrayCache.deallocateShortArray(inSamples[i]);
                    inSamples[i] = null;
                }
            }
        } catch (IOException ex) {
            throw new UndeclaredThrowableException(ex);
        }
    }
}
