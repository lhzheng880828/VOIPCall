package net.sf.fmj.media.multiplexer;

import com.lti.utils.UnsignedUtils;
import java.io.IOException;
import javax.media.Buffer;
import javax.media.Clock;
import javax.media.ClockStoppedException;
import javax.media.Control;
import javax.media.Duration;
import javax.media.Format;
import javax.media.IncompatibleTimeBaseException;
import javax.media.Multiplexer;
import javax.media.Owned;
import javax.media.Time;
import javax.media.TimeBase;
import javax.media.control.StreamWriterControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushDataSource;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.Seekable;
import javax.media.protocol.SourceTransferHandler;
import net.sf.fmj.ejmf.toolkit.util.TimeSource;
import net.sf.fmj.media.BasicClock;
import net.sf.fmj.media.BasicPlugIn;
import net.sf.fmj.media.MediaTimeBase;
import net.sf.fmj.media.control.MonitorAdapter;
import net.sf.fmj.media.datasink.RandomAccess;
import org.jitsi.android.util.java.awt.Component;

public abstract class BasicMux extends BasicPlugIn implements Multiplexer, Clock {
    protected byte[] buf;
    protected int bufLength;
    protected int bufOffset;
    protected BasicClock clock;
    Object dataLock;
    boolean dataReady;
    protected boolean eos;
    protected int filePointer;
    protected int fileSize;
    protected long fileSizeLimit;
    protected boolean fileSizeLimitReached;
    protected boolean firstBuffer;
    Buffer[] firstBuffers;
    boolean[] firstBuffersDone;
    protected boolean flushing;
    protected Format[] inputs;
    protected boolean isLiveData;
    VideoFormat jpegFmt;
    boolean mClosed;
    int master;
    long masterTime;
    protected int maxBufSize;
    protected MonitorAdapter[] mc;
    long[] mediaTime;
    VideoFormat mjpgFmt;
    int[] nonKeyCount;
    protected int numTracks;
    protected ContentDescriptor outputCD;
    boolean[] ready;
    boolean readyToStart;
    VideoFormat rgbFmt;
    protected BasicMuxDataSource source;
    protected Integer sourceLock;
    boolean startCompensated;
    boolean started;
    Object startup;
    protected SourceTransferHandler sth;
    protected BasicMuxPushStream stream;
    protected boolean streamSizeLimitSupported;
    protected Format[] supportedInputs;
    protected ContentDescriptor[] supportedOutputs;
    protected StreamWriterControl swc;
    long systemStartTime;
    protected BasicMuxTimeBase timeBase;
    Object timeSetSync;
    VideoFormat yuvFmt;

    class BasicMuxDataSource extends PushDataSource {
        private ContentDescriptor cd;
        private boolean connected = false;
        private BasicMux mux;
        private boolean started = false;
        private BasicMuxPushStream stream;
        private BasicMuxPushStream[] streams;

        public BasicMuxDataSource(BasicMux mux, ContentDescriptor cd) {
            this.cd = cd;
            this.mux = mux;
        }

        public void connect() throws IOException {
            if (this.streams == null) {
                getStreams();
            }
            this.connected = true;
            synchronized (BasicMux.this.sourceLock) {
                BasicMux.this.sourceLock.notifyAll();
            }
        }

        public void disconnect() {
            this.connected = false;
        }

        public String getContentType() {
            return this.cd.getContentType();
        }

        public Object getControl(String s) {
            return null;
        }

        public Object[] getControls() {
            return new Control[0];
        }

        public Time getDuration() {
            return Duration.DURATION_UNKNOWN;
        }

        public PushSourceStream[] getStreams() {
            if (this.streams == null) {
                this.streams = new BasicMuxPushStream[1];
                this.stream = new BasicMuxPushStream(this.cd);
                this.streams[0] = this.stream;
                BasicMux.this.setStream(this.stream);
            }
            return this.streams;
        }

        /* access modifiers changed from: 0000 */
        public boolean isConnected() {
            return this.connected;
        }

        /* access modifiers changed from: 0000 */
        public boolean isStarted() {
            return this.started;
        }

        public void start() throws IOException {
            if (this.streams == null || !this.connected) {
                throw new IOException("Source not connected yet!");
            }
            this.started = true;
            synchronized (BasicMux.this.sourceLock) {
                BasicMux.this.sourceLock.notifyAll();
            }
        }

        public void stop() {
            this.started = false;
        }
    }

    class BasicMuxPushStream implements PushSourceStream {
        private ContentDescriptor cd;
        private byte[] data;
        private int dataLen;
        private int dataOff;
        private Integer writeLock = new Integer(0);

        public BasicMuxPushStream(ContentDescriptor cd) {
            this.cd = cd;
        }

        public boolean endOfStream() {
            return BasicMux.this.isEOS();
        }

        public ContentDescriptor getContentDescriptor() {
            return this.cd;
        }

        public long getContentLength() {
            return -1;
        }

        public Object getControl(String s) {
            return null;
        }

        public Object[] getControls() {
            return new Control[0];
        }

        public int getMinimumTransferSize() {
            return this.dataLen;
        }

        public int read(byte[] buffer, int offset, int length) throws IOException {
            int transferred;
            synchronized (this.writeLock) {
                if (this.dataLen == -1) {
                    transferred = -1;
                } else {
                    if (length >= this.dataLen) {
                        transferred = this.dataLen;
                    } else {
                        transferred = length;
                    }
                    System.arraycopy(this.data, this.dataOff, buffer, offset, transferred);
                    this.dataLen -= transferred;
                    this.dataOff += transferred;
                }
                this.writeLock.notifyAll();
            }
            return transferred;
        }

        /* access modifiers changed from: declared_synchronized */
        public synchronized int seek(int location) {
            int tell;
            if (BasicMux.this.sth != null) {
                ((Seekable) BasicMux.this.sth).seek((long) location);
                tell = (int) ((Seekable) BasicMux.this.sth).tell();
            } else {
                tell = -1;
            }
            return tell;
        }

        public void setTransferHandler(SourceTransferHandler sth) {
            synchronized (this.writeLock) {
                BasicMux.this.sth = sth;
                if (sth == null || !BasicMux.this.needsSeekable() || (sth instanceof Seekable)) {
                    if (BasicMux.this.requireTwoPass() && sth != null && (sth instanceof RandomAccess)) {
                        ((RandomAccess) sth).setEnabled(true);
                    }
                    this.writeLock.notifyAll();
                } else {
                    throw new Error("SourceTransferHandler needs to be seekable");
                }
            }
        }

        /* access modifiers changed from: declared_synchronized */
        /* JADX WARNING: Missing block: B:29:0x0047, code skipped:
            r6 = r0;
            r1 = r0;
     */
        public synchronized int write(byte[] r4, int r5, int r6) {
            /*
            r3 = this;
            monitor-enter(r3);
            r1 = net.sf.fmj.media.multiplexer.BasicMux.this;	 Catch:{ all -> 0x0060 }
            r1 = r1.sth;	 Catch:{ all -> 0x0060 }
            if (r1 != 0) goto L_0x000b;
        L_0x0007:
            r0 = 0;
            r1 = r0;
        L_0x0009:
            monitor-exit(r3);
            return r1;
        L_0x000b:
            r1 = net.sf.fmj.media.multiplexer.BasicMux.this;	 Catch:{ all -> 0x0060 }
            r1 = r1.isLiveData;	 Catch:{ all -> 0x0060 }
            if (r1 == 0) goto L_0x0022;
        L_0x0011:
            r1 = net.sf.fmj.media.multiplexer.BasicMux.this;	 Catch:{ all -> 0x0060 }
            r1 = r1.sth;	 Catch:{ all -> 0x0060 }
            r1 = r1 instanceof net.sf.fmj.media.Syncable;	 Catch:{ all -> 0x0060 }
            if (r1 == 0) goto L_0x0022;
        L_0x0019:
            r1 = net.sf.fmj.media.multiplexer.BasicMux.this;	 Catch:{ all -> 0x0060 }
            r1 = r1.sth;	 Catch:{ all -> 0x0060 }
            r1 = (net.sf.fmj.media.Syncable) r1;	 Catch:{ all -> 0x0060 }
            r1.setSyncEnabled();	 Catch:{ all -> 0x0060 }
        L_0x0022:
            r2 = r3.writeLock;	 Catch:{ all -> 0x0060 }
            monitor-enter(r2);	 Catch:{ all -> 0x0060 }
            r3.data = r4;	 Catch:{ all -> 0x005d }
            r3.dataOff = r5;	 Catch:{ all -> 0x005d }
            r3.dataLen = r6;	 Catch:{ all -> 0x005d }
            r1 = net.sf.fmj.media.multiplexer.BasicMux.this;	 Catch:{ all -> 0x005d }
            r1 = r1.sth;	 Catch:{ all -> 0x005d }
            r1.transferData(r3);	 Catch:{ all -> 0x005d }
            r0 = r6;
        L_0x0033:
            r1 = r3.dataLen;	 Catch:{ all -> 0x0063 }
            if (r1 <= 0) goto L_0x0046;
        L_0x0037:
            r1 = r3.dataLen;	 Catch:{ all -> 0x0063 }
            if (r1 != r0) goto L_0x0040;
        L_0x003b:
            r1 = r3.writeLock;	 Catch:{ InterruptedException -> 0x0066 }
            r1.wait();	 Catch:{ InterruptedException -> 0x0066 }
        L_0x0040:
            r1 = net.sf.fmj.media.multiplexer.BasicMux.this;	 Catch:{ all -> 0x0063 }
            r1 = r1.sth;	 Catch:{ all -> 0x0063 }
            if (r1 != 0) goto L_0x004a;
        L_0x0046:
            monitor-exit(r2);	 Catch:{ all -> 0x0063 }
            r6 = r0;
            r1 = r0;
            goto L_0x0009;
        L_0x004a:
            r1 = r3.dataLen;	 Catch:{ all -> 0x0063 }
            if (r1 <= 0) goto L_0x0033;
        L_0x004e:
            r1 = r3.dataLen;	 Catch:{ all -> 0x0063 }
            if (r1 == r0) goto L_0x0033;
        L_0x0052:
            r6 = r3.dataLen;	 Catch:{ all -> 0x0063 }
            r1 = net.sf.fmj.media.multiplexer.BasicMux.this;	 Catch:{ all -> 0x005d }
            r1 = r1.sth;	 Catch:{ all -> 0x005d }
            r1.transferData(r3);	 Catch:{ all -> 0x005d }
            r0 = r6;
            goto L_0x0033;
        L_0x005d:
            r1 = move-exception;
        L_0x005e:
            monitor-exit(r2);	 Catch:{ all -> 0x005d }
            throw r1;	 Catch:{ all -> 0x0060 }
        L_0x0060:
            r1 = move-exception;
            monitor-exit(r3);
            throw r1;
        L_0x0063:
            r1 = move-exception;
            r6 = r0;
            goto L_0x005e;
        L_0x0066:
            r1 = move-exception;
            goto L_0x0040;
            */
            throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.multiplexer.BasicMux$BasicMuxPushStream.write(byte[], int, int):int");
        }
    }

    class BasicMuxTimeBase extends MediaTimeBase {
        long ticks = 0;
        boolean updated = false;

        BasicMuxTimeBase() {
        }

        public long getMediaTime() {
            if (!this.updated) {
                return this.ticks;
            }
            if (BasicMux.this.mediaTime.length == 1) {
                this.ticks = BasicMux.this.mediaTime[0];
            } else {
                this.ticks = BasicMux.this.mediaTime[0];
                for (int i = 1; i < BasicMux.this.mediaTime.length; i++) {
                    if (BasicMux.this.mediaTime[i] < this.ticks) {
                        this.ticks = BasicMux.this.mediaTime[i];
                    }
                }
            }
            this.updated = false;
            return this.ticks;
        }

        public void update() {
            this.updated = true;
        }
    }

    class SWC implements StreamWriterControl, Owned {
        private BasicMux bmx;

        public SWC(BasicMux bmx) {
            this.bmx = bmx;
        }

        public Component getControlComponent() {
            return null;
        }

        public Object getOwner() {
            return this.bmx;
        }

        public long getStreamSize() {
            return this.bmx.getStreamSize();
        }

        public boolean setStreamSizeLimit(long limit) {
            this.bmx.fileSizeLimit = limit;
            return BasicMux.this.streamSizeLimitSupported;
        }
    }

    public BasicMux() {
        this.numTracks = 0;
        this.flushing = false;
        this.sourceLock = new Integer(0);
        this.eos = false;
        this.firstBuffer = true;
        this.fileSize = 0;
        this.filePointer = 0;
        this.fileSizeLimit = -1;
        this.streamSizeLimitSupported = true;
        this.fileSizeLimitReached = false;
        this.sth = null;
        this.isLiveData = false;
        this.swc = null;
        this.mc = null;
        this.timeBase = null;
        this.startup = new Integer(0);
        this.readyToStart = false;
        this.clock = null;
        this.master = 0;
        this.mClosed = false;
        this.dataReady = false;
        this.startCompensated = false;
        this.dataLock = new Object();
        this.masterTime = -1;
        this.jpegFmt = new VideoFormat(VideoFormat.JPEG);
        this.mjpgFmt = new VideoFormat(VideoFormat.MJPG);
        this.rgbFmt = new VideoFormat(VideoFormat.RGB);
        this.yuvFmt = new VideoFormat(VideoFormat.YUV);
        this.maxBufSize = 32768;
        this.buf = new byte[this.maxBufSize];
        this.timeSetSync = new Object();
        this.started = false;
        this.systemStartTime = System.currentTimeMillis() * TimeSource.MICROS_PER_SEC;
        this.timeBase = new BasicMuxTimeBase();
        this.clock = new BasicClock();
        try {
            this.clock.setTimeBase(this.timeBase);
        } catch (Exception e) {
        }
        this.swc = new SWC(this);
        this.controls = new Control[]{this.swc};
    }

    /* access modifiers changed from: protected */
    public void bufClear() {
        this.bufOffset = 0;
        this.bufLength = 0;
    }

    /* access modifiers changed from: protected */
    public void bufFlush() {
        this.filePointer -= this.bufLength;
        write(this.buf, 0, this.bufLength);
    }

    /* access modifiers changed from: protected */
    public void bufSkip(int size) {
        this.bufOffset += size;
        this.bufLength += size;
        this.filePointer += size;
    }

    /* access modifiers changed from: protected */
    public void bufWriteByte(byte value) {
        this.buf[this.bufOffset] = value;
        this.bufOffset++;
        this.bufLength++;
        this.filePointer++;
    }

    /* access modifiers changed from: protected */
    public void bufWriteBytes(byte[] bytes) {
        System.arraycopy(bytes, 0, this.buf, this.bufOffset, bytes.length);
        this.bufOffset += bytes.length;
        this.bufLength += bytes.length;
        this.filePointer += bytes.length;
    }

    /* access modifiers changed from: protected */
    public void bufWriteBytes(String s) {
        bufWriteBytes(s.getBytes());
    }

    /* access modifiers changed from: protected */
    public void bufWriteInt(int value) {
        this.buf[this.bufOffset + 0] = (byte) ((value >> 24) & UnsignedUtils.MAX_UBYTE);
        this.buf[this.bufOffset + 1] = (byte) ((value >> 16) & UnsignedUtils.MAX_UBYTE);
        this.buf[this.bufOffset + 2] = (byte) ((value >> 8) & UnsignedUtils.MAX_UBYTE);
        this.buf[this.bufOffset + 3] = (byte) ((value >> 0) & UnsignedUtils.MAX_UBYTE);
        this.bufOffset += 4;
        this.bufLength += 4;
        this.filePointer += 4;
    }

    /* access modifiers changed from: protected */
    public void bufWriteIntLittleEndian(int value) {
        this.buf[this.bufOffset + 3] = (byte) ((value >>> 24) & UnsignedUtils.MAX_UBYTE);
        this.buf[this.bufOffset + 2] = (byte) ((value >>> 16) & UnsignedUtils.MAX_UBYTE);
        this.buf[this.bufOffset + 1] = (byte) ((value >>> 8) & UnsignedUtils.MAX_UBYTE);
        this.buf[this.bufOffset + 0] = (byte) ((value >>> 0) & UnsignedUtils.MAX_UBYTE);
        this.bufOffset += 4;
        this.bufLength += 4;
        this.filePointer += 4;
    }

    /* access modifiers changed from: protected */
    public void bufWriteShort(short value) {
        this.buf[this.bufOffset + 0] = (byte) ((value >> 8) & UnsignedUtils.MAX_UBYTE);
        this.buf[this.bufOffset + 1] = (byte) ((value >> 0) & UnsignedUtils.MAX_UBYTE);
        this.bufOffset += 2;
        this.bufLength += 2;
        this.filePointer += 2;
    }

    /* access modifiers changed from: protected */
    public void bufWriteShortLittleEndian(short value) {
        this.buf[this.bufOffset + 1] = (byte) ((value >> 8) & UnsignedUtils.MAX_UBYTE);
        this.buf[this.bufOffset + 0] = (byte) ((value >> 0) & UnsignedUtils.MAX_UBYTE);
        this.bufOffset += 2;
        this.bufLength += 2;
        this.filePointer += 2;
    }

    private boolean checkReady() {
        if (this.readyToStart) {
            return true;
        }
        for (boolean z : this.ready) {
            if (!z) {
                return false;
            }
        }
        this.readyToStart = true;
        return true;
    }

    public void close() {
        if (this.sth != null) {
            writeFooter();
            write(null, 0, -1);
        }
        for (int i = 0; i < this.mc.length; i++) {
            if (this.mc[i] != null) {
                this.mc[i].close();
            }
        }
        synchronized (this.dataLock) {
            this.mClosed = true;
            this.dataLock.notifyAll();
        }
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* JADX WARNING: Missing block: B:136:?, code skipped:
            return false;
     */
    /* JADX WARNING: Missing block: B:138:?, code skipped:
            return r4;
     */
    private boolean compensateStart(javax.media.Buffer r13, int r14) {
        /*
        r12 = this;
        r4 = 0;
        r5 = 1;
        r6 = r12.dataLock;
        monitor-enter(r6);
        r7 = r12.dataReady;	 Catch:{ all -> 0x007b }
        if (r7 == 0) goto L_0x0095;
    L_0x0009:
        r7 = r12.firstBuffersDone;	 Catch:{ all -> 0x007b }
        r7 = r7[r14];	 Catch:{ all -> 0x007b }
        if (r7 != 0) goto L_0x0092;
    L_0x000f:
        r8 = r13.getTimeStamp();	 Catch:{ all -> 0x007b }
        r10 = r12.masterTime;	 Catch:{ all -> 0x007b }
        r7 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));
        if (r7 >= 0) goto L_0x001b;
    L_0x0019:
        monitor-exit(r6);	 Catch:{ all -> 0x007b }
    L_0x001a:
        return r4;
    L_0x001b:
        r7 = r13.getFormat();	 Catch:{ all -> 0x007b }
        r7 = r7 instanceof javax.media.format.VideoFormat;	 Catch:{ all -> 0x007b }
        if (r7 == 0) goto L_0x007e;
    L_0x0023:
        r1 = r13.getFormat();	 Catch:{ all -> 0x007b }
        r7 = r12.jpegFmt;	 Catch:{ all -> 0x007b }
        r7 = r7.matches(r1);	 Catch:{ all -> 0x007b }
        if (r7 != 0) goto L_0x0047;
    L_0x002f:
        r7 = r12.mjpgFmt;	 Catch:{ all -> 0x007b }
        r7 = r7.matches(r1);	 Catch:{ all -> 0x007b }
        if (r7 != 0) goto L_0x0047;
    L_0x0037:
        r7 = r12.rgbFmt;	 Catch:{ all -> 0x007b }
        r7 = r7.matches(r1);	 Catch:{ all -> 0x007b }
        if (r7 != 0) goto L_0x0047;
    L_0x003f:
        r7 = r12.yuvFmt;	 Catch:{ all -> 0x007b }
        r7 = r7.matches(r1);	 Catch:{ all -> 0x007b }
        if (r7 == 0) goto L_0x0077;
    L_0x0047:
        r3 = r5;
    L_0x0048:
        if (r3 != 0) goto L_0x005e;
    L_0x004a:
        r7 = r13.getFlags();	 Catch:{ all -> 0x007b }
        r7 = r7 & 16;
        if (r7 != 0) goto L_0x005e;
    L_0x0052:
        r7 = r12.nonKeyCount;	 Catch:{ all -> 0x007b }
        r8 = r7[r14];	 Catch:{ all -> 0x007b }
        r9 = r8 + 1;
        r7[r14] = r9;	 Catch:{ all -> 0x007b }
        r7 = 30;
        if (r8 <= r7) goto L_0x0079;
    L_0x005e:
        r8 = r12.masterTime;	 Catch:{ all -> 0x007b }
        r13.setTimeStamp(r8);	 Catch:{ all -> 0x007b }
        r4 = r12.firstBuffersDone;	 Catch:{ all -> 0x007b }
        r7 = 1;
        r4[r14] = r7;	 Catch:{ all -> 0x007b }
    L_0x0068:
        r2 = 0;
    L_0x0069:
        r4 = r12.firstBuffersDone;	 Catch:{ all -> 0x007b }
        r4 = r4.length;	 Catch:{ all -> 0x007b }
        if (r2 >= r4) goto L_0x008c;
    L_0x006e:
        r4 = r12.firstBuffersDone;	 Catch:{ all -> 0x007b }
        r4 = r4[r2];	 Catch:{ all -> 0x007b }
        if (r4 != 0) goto L_0x0089;
    L_0x0074:
        monitor-exit(r6);	 Catch:{ all -> 0x007b }
        r4 = r5;
        goto L_0x001a;
    L_0x0077:
        r3 = r4;
        goto L_0x0048;
    L_0x0079:
        monitor-exit(r6);	 Catch:{ all -> 0x007b }
        goto L_0x001a;
    L_0x007b:
        r4 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x007b }
        throw r4;
    L_0x007e:
        r8 = r12.masterTime;	 Catch:{ all -> 0x007b }
        r13.setTimeStamp(r8);	 Catch:{ all -> 0x007b }
        r4 = r12.firstBuffersDone;	 Catch:{ all -> 0x007b }
        r7 = 1;
        r4[r14] = r7;	 Catch:{ all -> 0x007b }
        goto L_0x0068;
    L_0x0089:
        r2 = r2 + 1;
        goto L_0x0069;
    L_0x008c:
        r4 = 1;
        r12.startCompensated = r4;	 Catch:{ all -> 0x007b }
        monitor-exit(r6);	 Catch:{ all -> 0x007b }
        r4 = r5;
        goto L_0x001a;
    L_0x0092:
        monitor-exit(r6);	 Catch:{ all -> 0x007b }
        r4 = r5;
        goto L_0x001a;
    L_0x0095:
        r8 = r13.getTimeStamp();	 Catch:{ all -> 0x007b }
        r10 = 0;
        r7 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));
        if (r7 >= 0) goto L_0x00ae;
    L_0x009f:
        r4 = 1;
        r12.startCompensated = r4;	 Catch:{ all -> 0x007b }
        r4 = 1;
        r12.dataReady = r4;	 Catch:{ all -> 0x007b }
        r4 = r12.dataLock;	 Catch:{ all -> 0x007b }
        r4.notifyAll();	 Catch:{ all -> 0x007b }
        monitor-exit(r6);	 Catch:{ all -> 0x007b }
        r4 = r5;
        goto L_0x001a;
    L_0x00ae:
        r7 = r12.firstBuffers;	 Catch:{ all -> 0x007b }
        r7[r14] = r13;	 Catch:{ all -> 0x007b }
        r0 = 1;
        r2 = 0;
    L_0x00b4:
        r7 = r12.firstBuffers;	 Catch:{ all -> 0x007b }
        r7 = r7.length;	 Catch:{ all -> 0x007b }
        if (r2 >= r7) goto L_0x00c3;
    L_0x00b9:
        r7 = r12.firstBuffers;	 Catch:{ all -> 0x007b }
        r7 = r7[r2];	 Catch:{ all -> 0x007b }
        if (r7 != 0) goto L_0x00c0;
    L_0x00bf:
        r0 = 0;
    L_0x00c0:
        r2 = r2 + 1;
        goto L_0x00b4;
    L_0x00c3:
        if (r0 != 0) goto L_0x00e6;
    L_0x00c5:
        r7 = r12.dataReady;	 Catch:{ all -> 0x007b }
        if (r7 != 0) goto L_0x00d5;
    L_0x00c9:
        r7 = r12.mClosed;	 Catch:{ all -> 0x007b }
        if (r7 != 0) goto L_0x00d5;
    L_0x00cd:
        r7 = r12.dataLock;	 Catch:{ Exception -> 0x00d3 }
        r7.wait();	 Catch:{ Exception -> 0x00d3 }
        goto L_0x00c5;
    L_0x00d3:
        r7 = move-exception;
        goto L_0x00c5;
    L_0x00d5:
        r7 = r12.mClosed;	 Catch:{ all -> 0x007b }
        if (r7 != 0) goto L_0x00df;
    L_0x00d9:
        r7 = r12.firstBuffers;	 Catch:{ all -> 0x007b }
        r7 = r7[r14];	 Catch:{ all -> 0x007b }
        if (r7 != 0) goto L_0x00e2;
    L_0x00df:
        monitor-exit(r6);	 Catch:{ all -> 0x007b }
        goto L_0x001a;
    L_0x00e2:
        monitor-exit(r6);	 Catch:{ all -> 0x007b }
        r4 = r5;
        goto L_0x001a;
    L_0x00e6:
        r7 = r12.firstBuffers;	 Catch:{ all -> 0x007b }
        r8 = 0;
        r7 = r7[r8];	 Catch:{ all -> 0x007b }
        r8 = r7.getTimeStamp();	 Catch:{ all -> 0x007b }
        r12.masterTime = r8;	 Catch:{ all -> 0x007b }
        r2 = 0;
    L_0x00f2:
        r7 = r12.firstBuffers;	 Catch:{ all -> 0x007b }
        r7 = r7.length;	 Catch:{ all -> 0x007b }
        if (r2 >= r7) goto L_0x010d;
    L_0x00f7:
        r7 = r12.firstBuffers;	 Catch:{ all -> 0x007b }
        r7 = r7[r2];	 Catch:{ all -> 0x007b }
        r7 = r7.getFormat();	 Catch:{ all -> 0x007b }
        r7 = r7 instanceof javax.media.format.AudioFormat;	 Catch:{ all -> 0x007b }
        if (r7 == 0) goto L_0x0135;
    L_0x0103:
        r7 = r12.firstBuffers;	 Catch:{ all -> 0x007b }
        r7 = r7[r2];	 Catch:{ all -> 0x007b }
        r8 = r7.getTimeStamp();	 Catch:{ all -> 0x007b }
        r12.masterTime = r8;	 Catch:{ all -> 0x007b }
    L_0x010d:
        r7 = 1;
        r12.startCompensated = r7;	 Catch:{ all -> 0x007b }
        r2 = 0;
    L_0x0111:
        r7 = r12.firstBuffers;	 Catch:{ all -> 0x007b }
        r7 = r7.length;	 Catch:{ all -> 0x007b }
        if (r2 >= r7) goto L_0x0159;
    L_0x0116:
        r7 = r12.firstBuffers;	 Catch:{ all -> 0x007b }
        r7 = r7[r2];	 Catch:{ all -> 0x007b }
        r8 = r7.getTimeStamp();	 Catch:{ all -> 0x007b }
        r10 = r12.masterTime;	 Catch:{ all -> 0x007b }
        r7 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));
        if (r7 < 0) goto L_0x0150;
    L_0x0124:
        r7 = r12.firstBuffers;	 Catch:{ all -> 0x007b }
        r7 = r7[r2];	 Catch:{ all -> 0x007b }
        r8 = r12.masterTime;	 Catch:{ all -> 0x007b }
        r7.setTimeStamp(r8);	 Catch:{ all -> 0x007b }
        r7 = r12.firstBuffersDone;	 Catch:{ all -> 0x007b }
        r8 = 1;
        r7[r2] = r8;	 Catch:{ all -> 0x007b }
    L_0x0132:
        r2 = r2 + 1;
        goto L_0x0111;
    L_0x0135:
        r7 = r12.firstBuffers;	 Catch:{ all -> 0x007b }
        r7 = r7[r2];	 Catch:{ all -> 0x007b }
        r8 = r7.getTimeStamp();	 Catch:{ all -> 0x007b }
        r10 = r12.masterTime;	 Catch:{ all -> 0x007b }
        r7 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));
        if (r7 >= 0) goto L_0x014d;
    L_0x0143:
        r7 = r12.firstBuffers;	 Catch:{ all -> 0x007b }
        r7 = r7[r2];	 Catch:{ all -> 0x007b }
        r8 = r7.getTimeStamp();	 Catch:{ all -> 0x007b }
        r12.masterTime = r8;	 Catch:{ all -> 0x007b }
    L_0x014d:
        r2 = r2 + 1;
        goto L_0x00f2;
    L_0x0150:
        r7 = r12.firstBuffers;	 Catch:{ all -> 0x007b }
        r8 = 0;
        r7[r2] = r8;	 Catch:{ all -> 0x007b }
        r7 = 0;
        r12.startCompensated = r7;	 Catch:{ all -> 0x007b }
        goto L_0x0132;
    L_0x0159:
        r7 = r12.dataLock;	 Catch:{ all -> 0x007b }
        monitor-enter(r7);	 Catch:{ all -> 0x007b }
        r8 = 1;
        r12.dataReady = r8;	 Catch:{ all -> 0x016f }
        r8 = r12.dataLock;	 Catch:{ all -> 0x016f }
        r8.notifyAll();	 Catch:{ all -> 0x016f }
        monitor-exit(r7);	 Catch:{ all -> 0x016f }
        r7 = r12.firstBuffers;	 Catch:{ all -> 0x007b }
        r7 = r7[r14];	 Catch:{ all -> 0x007b }
        if (r7 == 0) goto L_0x016c;
    L_0x016b:
        r4 = r5;
    L_0x016c:
        monitor-exit(r6);	 Catch:{ all -> 0x007b }
        goto L_0x001a;
    L_0x016f:
        r4 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x016f }
        throw r4;	 Catch:{ all -> 0x007b }
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.multiplexer.BasicMux.compensateStart(javax.media.Buffer, int):boolean");
    }

    /* access modifiers changed from: protected */
    public int doProcess(Buffer buffer, int trackID) {
        byte[] data = (byte[]) buffer.getData();
        int dataLen = buffer.getLength();
        if (!buffer.isEOM()) {
            write(data, buffer.getOffset(), dataLen);
        }
        return 0;
    }

    public DataSource getDataOutput() {
        if (this.source == null) {
            this.source = new BasicMuxDataSource(this, this.outputCD);
            synchronized (this.sourceLock) {
                this.sourceLock.notifyAll();
            }
        }
        return this.source;
    }

    private long getDuration(Buffer buffer) {
        long duration = ((AudioFormat) buffer.getFormat()).computeDuration((long) buffer.getLength());
        if (duration < 0) {
            return 0;
        }
        return duration;
    }

    public long getMediaNanoseconds() {
        return this.clock.getMediaNanoseconds();
    }

    public Time getMediaTime() {
        return this.clock.getMediaTime();
    }

    public float getRate() {
        return this.clock.getRate();
    }

    public Time getStopTime() {
        return this.clock.getStopTime();
    }

    /* access modifiers changed from: 0000 */
    public long getStreamSize() {
        return (long) this.fileSize;
    }

    public Format[] getSupportedInputFormats() {
        return this.supportedInputs;
    }

    public ContentDescriptor[] getSupportedOutputContentDescriptors(Format[] inputs) {
        return this.supportedOutputs;
    }

    public Time getSyncTime() {
        return this.clock.getSyncTime();
    }

    public TimeBase getTimeBase() {
        return this.clock.getTimeBase();
    }

    /* access modifiers changed from: 0000 */
    public boolean isEOS() {
        return this.eos;
    }

    public Time mapToTimeBase(Time t) throws ClockStoppedException {
        return this.clock.mapToTimeBase(t);
    }

    /* access modifiers changed from: 0000 */
    public boolean needsSeekable() {
        return false;
    }

    public void open() {
        int i;
        this.firstBuffer = true;
        this.firstBuffers = new Buffer[this.inputs.length];
        this.firstBuffersDone = new boolean[this.inputs.length];
        this.nonKeyCount = new int[this.inputs.length];
        this.mediaTime = new long[this.inputs.length];
        for (i = 0; i < this.inputs.length; i++) {
            this.firstBuffers[i] = null;
            this.firstBuffersDone[i] = false;
            this.nonKeyCount[i] = 0;
            this.mediaTime[i] = 0;
        }
        this.ready = new boolean[this.inputs.length];
        resetReady();
        int len = 0;
        this.mc = new MonitorAdapter[this.inputs.length];
        i = 0;
        while (i < this.inputs.length) {
            if ((this.inputs[i] instanceof VideoFormat) || (this.inputs[i] instanceof AudioFormat)) {
                this.mc[i] = new MonitorAdapter(this.inputs[i], this);
                if (this.mc[i] != null) {
                    len++;
                }
            }
            i++;
        }
        int j = 0;
        this.controls = new Control[(len + 1)];
        for (i = 0; i < this.mc.length; i++) {
            if (this.mc[i] != null) {
                int j2 = j + 1;
                this.controls[j] = this.mc[i];
                j = j2;
            }
        }
        this.controls[j] = this.swc;
    }

    public int process(Buffer buffer, int trackID) {
        if (buffer.isDiscard()) {
            return 0;
        }
        if (!this.isLiveData && (buffer.getFlags() & 32768) > 0) {
            this.isLiveData = true;
        }
        while (true) {
            if (this.source != null && this.source.isConnected() && this.source.isStarted()) {
                synchronized (this) {
                    if (this.firstBuffer) {
                        writeHeader();
                        this.firstBuffer = false;
                    }
                }
                if (this.numTracks > 1) {
                    if ((buffer.getFlags() & 4096) != 0 && buffer.getTimeStamp() <= 0) {
                        return 0;
                    }
                    if (!(this.startCompensated || compensateStart(buffer, trackID))) {
                        return 0;
                    }
                }
                updateClock(buffer, trackID);
                if (this.mc[trackID] != null && this.mc[trackID].isEnabled()) {
                    this.mc[trackID].process(buffer);
                }
                int processResult = doProcess(buffer, trackID);
                if (this.fileSizeLimitReached) {
                    return processResult | 8;
                }
                return processResult;
            }
            synchronized (this.sourceLock) {
                try {
                    this.sourceLock.wait(500);
                } catch (InterruptedException e) {
                }
                if (this.flushing) {
                    this.flushing = false;
                    buffer.setLength(0);
                    return 0;
                }
            }
        }
    }

    public boolean requireTwoPass() {
        return false;
    }

    public void reset() {
        for (int i = 0; i < this.mediaTime.length; i++) {
            this.mediaTime[i] = 0;
            if (this.mc[i] != null) {
                this.mc[i].reset();
            }
        }
        this.timeBase.update();
        resetReady();
        synchronized (this.sourceLock) {
            this.flushing = true;
            this.sourceLock.notifyAll();
        }
    }

    private void resetReady() {
        for (int i = 0; i < this.ready.length; i++) {
            this.ready[i] = false;
        }
        this.readyToStart = false;
        synchronized (this.startup) {
            this.startup.notifyAll();
        }
    }

    /* access modifiers changed from: protected */
    public int seek(int location) {
        if (this.source == null || !this.source.isConnected()) {
            return location;
        }
        this.filePointer = this.stream.seek(location);
        return this.filePointer;
    }

    public ContentDescriptor setContentDescriptor(ContentDescriptor outputCD) {
        if (BasicPlugIn.matches(outputCD, this.supportedOutputs) == null) {
            return null;
        }
        this.outputCD = outputCD;
        return outputCD;
    }

    public Format setInputFormat(Format format, int trackID) {
        this.inputs[trackID] = format;
        return format;
    }

    public void setMediaTime(Time now) {
        synchronized (this.timeSetSync) {
            this.clock.setMediaTime(now);
            for (int i = 0; i < this.mediaTime.length; i++) {
                this.mediaTime[i] = now.getNanoseconds();
            }
            this.timeBase.update();
        }
    }

    public int setNumTracks(int numTracks) {
        this.numTracks = numTracks;
        if (this.inputs == null) {
            this.inputs = new Format[numTracks];
        } else {
            Format[] newInputs = new Format[numTracks];
            for (int i = 0; i < this.inputs.length; i++) {
                newInputs[i] = this.inputs[i];
            }
            this.inputs = newInputs;
        }
        return numTracks;
    }

    public float setRate(float factor) {
        return factor == this.clock.getRate() ? factor : this.clock.setRate(1.0f);
    }

    public void setStopTime(Time stopTime) {
        this.clock.setStopTime(stopTime);
    }

    /* access modifiers changed from: 0000 */
    public void setStream(BasicMuxPushStream ps) {
        this.stream = ps;
    }

    public void setTimeBase(TimeBase master) throws IncompatibleTimeBaseException {
        if (master != this.timeBase) {
            throw new IncompatibleTimeBaseException();
        }
    }

    public void stop() {
        synchronized (this.timeSetSync) {
            if (this.started) {
                this.started = false;
                this.clock.stop();
                this.timeBase.mediaStopped();
                return;
            }
        }
    }

    public void syncStart(Time at) {
        synchronized (this.timeSetSync) {
            if (this.started) {
                return;
            }
            this.started = true;
            this.clock.syncStart(at);
            this.timeBase.mediaStarted();
            this.systemStartTime = System.currentTimeMillis() * TimeSource.MICROS_PER_SEC;
        }
    }

    private void updateClock(Buffer buffer, int trackID) {
        if (!this.readyToStart && this.numTracks > 1) {
            synchronized (this.startup) {
                this.ready[trackID] = true;
                if (checkReady()) {
                    this.startup.notifyAll();
                } else {
                    while (!this.readyToStart) {
                        try {
                            this.startup.wait(1000);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
        long timestamp = buffer.getTimeStamp();
        if (timestamp <= 0 && (buffer.getFormat() instanceof AudioFormat)) {
            timestamp = this.mediaTime[trackID];
            long[] jArr = this.mediaTime;
            jArr[trackID] = jArr[trackID] + getDuration(buffer);
        } else if (timestamp <= 0) {
            this.mediaTime[trackID] = (System.currentTimeMillis() * TimeSource.MICROS_PER_SEC) - this.systemStartTime;
        } else {
            this.mediaTime[trackID] = timestamp;
        }
        this.timeBase.update();
    }

    /* access modifiers changed from: protected */
    public int write(byte[] data, int offset, int length) {
        if (this.source == null || !this.source.isConnected()) {
            return length;
        }
        if (length > 0) {
            this.filePointer += length;
            if (this.filePointer > this.fileSize) {
                this.fileSize = this.filePointer;
            }
            if (this.fileSizeLimit > 0 && ((long) this.fileSize) >= this.fileSizeLimit) {
                this.fileSizeLimitReached = true;
            }
        }
        return this.stream.write(data, offset, length);
    }

    /* access modifiers changed from: protected */
    public void writeFooter() {
    }

    /* access modifiers changed from: protected */
    public void writeHeader() {
    }
}
