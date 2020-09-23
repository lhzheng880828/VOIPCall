package org.jitsi.impl.neomedia.protocol;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.control.BufferControl;
import javax.media.format.AudioFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushBufferStream;
import net.sf.fmj.ejmf.toolkit.util.TimeSource;
import org.jitsi.android.util.java.awt.Component;

public class CachingPushBufferStream implements PushBufferStream {
    public static final long DEFAULT_BUFFER_LENGTH = 20;
    private static final int MAX_CACHE_SIZE = 1024;
    private BufferControl bufferControl;
    private final Object bufferControlSyncRoot = new Object();
    private final List<Buffer> cache = new LinkedList();
    private long cacheLengthInMillis = 0;
    private IOException readException;
    /* access modifiers changed from: private|final */
    public final PushBufferStream stream;
    private BufferTransferHandler transferHandler;

    private static class BufferControlImpl implements BufferControl {
        private long bufferLength;
        private boolean enabledThreshold;
        private long minimumThreshold;

        private BufferControlImpl() {
            this.bufferLength = -1;
            this.minimumThreshold = -1;
        }

        /* synthetic */ BufferControlImpl(AnonymousClass1 x0) {
            this();
        }

        public long getBufferLength() {
            return this.bufferLength;
        }

        public Component getControlComponent() {
            return null;
        }

        public boolean getEnabledThreshold() {
            return this.enabledThreshold;
        }

        public long getMinimumThreshold() {
            return this.minimumThreshold;
        }

        public long setBufferLength(long bufferLength) {
            if (bufferLength == -1 || bufferLength > 0) {
                this.bufferLength = bufferLength;
            }
            return getBufferLength();
        }

        public void setEnabledThreshold(boolean enabledThreshold) {
            this.enabledThreshold = enabledThreshold;
        }

        public long setMinimumThreshold(long minimumThreshold) {
            return getMinimumThreshold();
        }
    }

    public CachingPushBufferStream(PushBufferStream stream) {
        this.stream = stream;
    }

    /* JADX WARNING: Missing block: B:30:?, code skipped:
            return r3;
     */
    private boolean canWriteInCache() {
        /*
        r10 = this;
        r8 = 1;
        r3 = 1;
        r4 = 0;
        r5 = r10.cache;
        monitor-enter(r5);
        r6 = r10.cache;	 Catch:{ all -> 0x0034 }
        r2 = r6.size();	 Catch:{ all -> 0x0034 }
        if (r2 >= r3) goto L_0x0011;
    L_0x000f:
        monitor-exit(r5);	 Catch:{ all -> 0x0034 }
    L_0x0010:
        return r3;
    L_0x0011:
        r6 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
        if (r2 < r6) goto L_0x0018;
    L_0x0015:
        monitor-exit(r5);	 Catch:{ all -> 0x0034 }
        r3 = r4;
        goto L_0x0010;
    L_0x0018:
        r0 = r10.getBufferLength();	 Catch:{ all -> 0x0034 }
        r6 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));
        if (r6 >= 0) goto L_0x0023;
    L_0x0020:
        monitor-exit(r5);	 Catch:{ all -> 0x0034 }
        r3 = r4;
        goto L_0x0010;
    L_0x0023:
        r6 = r10.cacheLengthInMillis;	 Catch:{ all -> 0x0034 }
        r6 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
        if (r6 >= 0) goto L_0x002c;
    L_0x0029:
        monitor-exit(r5);	 Catch:{ all -> 0x0034 }
        r3 = r4;
        goto L_0x0010;
    L_0x002c:
        r6 = r10.cacheLengthInMillis;	 Catch:{ all -> 0x0034 }
        r6 = (r6 > r0 ? 1 : (r6 == r0 ? 0 : -1));
        if (r6 >= 0) goto L_0x0037;
    L_0x0032:
        monitor-exit(r5);	 Catch:{ all -> 0x0034 }
        goto L_0x0010;
    L_0x0034:
        r3 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x0034 }
        throw r3;
    L_0x0037:
        r3 = r4;
        goto L_0x0032;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.protocol.CachingPushBufferStream.canWriteInCache():boolean");
    }

    public boolean endOfStream() {
        return this.stream.endOfStream();
    }

    private BufferControl getBufferControl() {
        BufferControl bufferControl;
        synchronized (this.bufferControlSyncRoot) {
            if (this.bufferControl == null) {
                this.bufferControl = new BufferControlImpl();
            }
            bufferControl = this.bufferControl;
        }
        return bufferControl;
    }

    private long getBufferLength() {
        long bufferLength;
        synchronized (this.bufferControlSyncRoot) {
            bufferLength = this.bufferControl == null ? -1 : this.bufferControl.getBufferLength();
        }
        return bufferLength;
    }

    public ContentDescriptor getContentDescriptor() {
        return this.stream.getContentDescriptor();
    }

    public long getContentLength() {
        return this.stream.getContentLength();
    }

    public Object getControl(String controlType) {
        Object control = this.stream.getControl(controlType);
        if (control == null && BufferControl.class.getName().equals(controlType)) {
            return getBufferControl();
        }
        return control;
    }

    public Object[] getControls() {
        Object[] controls = this.stream.getControls();
        if (controls == null) {
            if (getBufferControl() == null) {
                return controls;
            }
            return new Object[]{getBufferControl()};
        }
        boolean bufferControlExists = false;
        for (Object control : controls) {
            if (control instanceof BufferControl) {
                bufferControlExists = true;
                break;
            }
        }
        if (bufferControlExists) {
            return controls;
        }
        BufferControl bufferControl = getBufferControl();
        if (bufferControl == null) {
            return controls;
        }
        Object[] newControls = new Object[(controls.length + 1)];
        newControls[0] = bufferControl;
        System.arraycopy(controls, 0, newControls, 1, controls.length);
        return controls;
    }

    public Format getFormat() {
        return this.stream.getFormat();
    }

    private long getLengthInMillis(Buffer buffer) {
        int length = buffer.getLength();
        if (length < 1) {
            return 0;
        }
        Format format = buffer.getFormat();
        if (format == null) {
            format = getFormat();
            if (format == null) {
                return 0;
            }
        }
        if (!(format instanceof AudioFormat)) {
            return 0;
        }
        long duration = ((AudioFormat) format).computeDuration((long) length);
        if (duration >= 1) {
            return duration / TimeSource.MICROS_PER_SEC;
        }
        return 0;
    }

    public PushBufferStream getStream() {
        return this.stream;
    }

    public void read(Buffer buffer) throws IOException {
        synchronized (this.cache) {
            if (this.readException != null) {
                IOException ioe = new IOException();
                ioe.initCause(this.readException);
                this.readException = null;
                throw ioe;
            }
            buffer.setLength(0);
            if (!this.cache.isEmpty()) {
                int bufferOffset = buffer.getOffset();
                while (!this.cache.isEmpty()) {
                    Buffer cacheBuffer = (Buffer) this.cache.get(0);
                    int nextBufferOffset = read(cacheBuffer, buffer, bufferOffset);
                    if (cacheBuffer.getLength() <= 0 || cacheBuffer.getData() == null) {
                        this.cache.remove(0);
                    }
                    if (nextBufferOffset < 0) {
                        break;
                    }
                    bufferOffset = nextBufferOffset;
                }
                this.cacheLengthInMillis -= getLengthInMillis(buffer);
                if (this.cacheLengthInMillis < 0) {
                    this.cacheLengthInMillis = 0;
                }
                if (canWriteInCache()) {
                    this.cache.notify();
                }
            }
        }
    }

    private int read(Buffer input, Buffer output, int outputOffset) throws IOException {
        int outputLength;
        Object outputData = output.getData();
        if (outputData != null) {
            Object inputData = input.getData();
            if (inputData == null) {
                output.setFormat(input.getFormat());
                return outputOffset;
            }
            Class<?> dataType = outputData.getClass();
            if (inputData.getClass().equals(dataType) && dataType.equals(byte[].class)) {
                int inputOffset = input.getOffset();
                int inputLength = input.getLength();
                byte[] outputBytes = (byte[]) outputData;
                outputLength = outputBytes.length - outputOffset;
                if (outputLength < 1) {
                    return -1;
                }
                if (inputLength < outputLength) {
                    outputLength = inputLength;
                }
                System.arraycopy(inputData, inputOffset, outputBytes, outputOffset, outputLength);
                output.setData(outputBytes);
                output.setLength(output.getLength() + outputLength);
                if (output.getOffset() == outputOffset) {
                    output.setFormat(input.getFormat());
                    output.setDiscard(input.isDiscard());
                    output.setEOM(input.isEOM());
                    output.setFlags(input.getFlags());
                    output.setHeader(input.getHeader());
                    output.setSequenceNumber(input.getSequenceNumber());
                    output.setTimeStamp(input.getTimeStamp());
                    output.setDuration(-1);
                }
                input.setLength(inputLength - outputLength);
                input.setOffset(inputOffset + outputLength);
                return outputOffset + outputLength;
            }
        }
        if (output.getOffset() == outputOffset) {
            output.copy(input);
            outputLength = output.getLength();
            input.setLength(input.getLength() - outputLength);
            input.setOffset(input.getOffset() + outputLength);
        }
        return -1;
    }

    public void setTransferHandler(BufferTransferHandler transferHandler) {
        BufferTransferHandler substituteTransferHandler;
        if (transferHandler == null) {
            substituteTransferHandler = null;
        } else {
            substituteTransferHandler = new StreamSubstituteBufferTransferHandler(transferHandler, this.stream, this) {
                public void transferData(PushBufferStream stream) {
                    if (CachingPushBufferStream.this.stream == stream) {
                        CachingPushBufferStream.this.transferData(this);
                    }
                    super.transferData(stream);
                }
            };
        }
        synchronized (this.cache) {
            this.stream.setTransferHandler(substituteTransferHandler);
            this.transferHandler = substituteTransferHandler;
            this.cache.notifyAll();
        }
    }

    /* access modifiers changed from: protected */
    public void transferData(BufferTransferHandler transferHandler) {
        boolean canWriteInCache;
        boolean interrupted = false;
        synchronized (this.cache) {
            while (this.transferHandler == transferHandler) {
                if (canWriteInCache()) {
                    canWriteInCache = true;
                    break;
                } else {
                    try {
                        this.cache.wait(10);
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
            }
            canWriteInCache = false;
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        } else if (canWriteInCache) {
            IOException readException;
            Buffer buffer = new Buffer();
            try {
                this.stream.read(buffer);
                readException = null;
            } catch (IOException ioe) {
                readException = ioe;
            }
            if (readException != null) {
                synchronized (this.cache) {
                    this.readException = readException;
                }
            } else if (!buffer.isDiscard() && buffer.getLength() != 0 && buffer.getData() != null) {
                synchronized (this.cache) {
                    this.cache.add(buffer);
                    this.cacheLengthInMillis += getLengthInMillis(buffer);
                }
            }
        }
    }
}
