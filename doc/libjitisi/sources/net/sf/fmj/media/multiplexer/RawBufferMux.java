package net.sf.fmj.media.multiplexer;

import java.io.IOException;
import javax.media.Buffer;
import javax.media.Clock;
import javax.media.ClockStoppedException;
import javax.media.Control;
import javax.media.Format;
import javax.media.IncompatibleTimeBaseException;
import javax.media.Multiplexer;
import javax.media.ResourceUnavailableException;
import javax.media.Time;
import javax.media.TimeBase;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferStream;
import net.sf.fmj.ejmf.toolkit.util.TimeSource;
import net.sf.fmj.media.BasicClock;
import net.sf.fmj.media.BasicPlugIn;
import net.sf.fmj.media.CircularBuffer;
import net.sf.fmj.media.MediaTimeBase;
import net.sf.fmj.media.control.MonitorAdapter;
import net.sf.fmj.media.protocol.BasicPushBufferDataSource;
import net.sf.fmj.media.protocol.BasicSourceStream;
import net.sf.fmj.media.util.MediaThread;

public class RawBufferMux extends BasicPlugIn implements Multiplexer, Clock {
    static AudioFormat mpegAudio = new AudioFormat(AudioFormat.MPEG_RTP);
    boolean allowDrop;
    protected BasicClock clock;
    protected ContentDescriptor contentDesc;
    boolean hasRead;
    protected int masterTrackID;
    protected MonitorAdapter[] mc;
    long mediaStartTime;
    protected long[] mediaTime;
    protected int numTracks;
    protected RawBufferDataSource source;
    boolean sourceDisconnected;
    boolean started;
    protected RawBufferSourceStream[] streams;
    protected ContentDescriptor[] supported;
    long systemStartTime;
    protected RawMuxTimeBase timeBase;
    Object timeSetSync;
    protected Format[] trackFormats;

    class RawBufferDataSource extends BasicPushBufferDataSource {
        public RawBufferDataSource() {
            if (RawBufferMux.this.contentDesc != null) {
                this.contentType = RawBufferMux.this.contentDesc.getContentType();
            }
        }

        public void connect() throws IOException {
            super.connect();
            RawBufferMux.this.sourceDisconnected = false;
        }

        public void disconnect() {
            super.disconnect();
            RawBufferMux.this.sourceDisconnected = true;
            for (int i = 0; i < RawBufferMux.this.streams.length; i++) {
                RawBufferMux.this.streams[i].stop();
                RawBufferMux.this.streams[i].close();
            }
        }

        public PushBufferStream[] getStreams() {
            return RawBufferMux.this.streams;
        }

        /* access modifiers changed from: private */
        public void initialize(Format[] trackFormats) {
            RawBufferMux.this.streams = new RawBufferSourceStream[trackFormats.length];
            for (int i = 0; i < trackFormats.length; i++) {
                RawBufferMux.this.streams[i] = new RawBufferSourceStream(trackFormats[i]);
            }
        }

        public void start() throws IOException {
            super.start();
            for (RawBufferSourceStream start : RawBufferMux.this.streams) {
                start.start();
            }
        }

        public void stop() throws IOException {
            super.stop();
            for (RawBufferSourceStream stop : RawBufferMux.this.streams) {
                stop.stop();
            }
        }
    }

    class RawBufferSourceStream extends BasicSourceStream implements PushBufferStream, Runnable {
        CircularBuffer bufferQ;
        boolean closed = false;
        Object drainSync = new Object();
        boolean draining = false;
        Format format = null;
        BufferTransferHandler handler = null;
        Object startReq = new Integer(0);
        boolean started = false;
        Thread streamThread = null;

        public RawBufferSourceStream(Format fmt) {
            this.contentDescriptor = RawBufferMux.this.contentDesc;
            this.format = fmt;
            this.bufferQ = new CircularBuffer(5);
            this.streamThread = new MediaThread(this, "RawBufferStream Thread");
            if (this.streamThread != null) {
                this.streamThread.start();
            }
        }

        /* access modifiers changed from: protected */
        public void close() {
            this.closed = true;
            if (this.streamThread != null) {
                try {
                    reset();
                    synchronized (this.startReq) {
                        this.startReq.notifyAll();
                    }
                } catch (Exception e) {
                }
            }
        }

        public Format getFormat() {
            return this.format;
        }

        /* access modifiers changed from: protected */
        public int process(Buffer filled) {
            synchronized (this.bufferQ) {
                if (RawBufferMux.this.allowDrop && !this.bufferQ.canWrite() && this.bufferQ.canRead() && (this.bufferQ.peek().getFlags() & 32) == 0) {
                    this.bufferQ.read();
                    this.bufferQ.readReport();
                }
                while (!this.bufferQ.canWrite() && !this.closed) {
                    try {
                        this.bufferQ.wait();
                    } catch (Exception e) {
                    }
                }
                if (this.closed) {
                } else {
                    Buffer buffer = this.bufferQ.getEmptyBuffer();
                    Object bdata = buffer.getData();
                    Object bheader = buffer.getHeader();
                    buffer.setData(filled.getData());
                    buffer.setHeader(filled.getHeader());
                    filled.setData(bdata);
                    filled.setHeader(bheader);
                    buffer.setLength(filled.getLength());
                    buffer.setEOM(filled.isEOM());
                    buffer.setFlags(filled.getFlags());
                    buffer.setTimeStamp(filled.getTimeStamp());
                    buffer.setFormat(filled.getFormat());
                    buffer.setOffset(filled.getOffset());
                    buffer.setSequenceNumber(filled.getSequenceNumber());
                    if (filled.isEOM()) {
                        this.draining = true;
                    }
                    synchronized (this.bufferQ) {
                        this.bufferQ.writeReport();
                        this.bufferQ.notifyAll();
                    }
                    if (filled.isEOM()) {
                        synchronized (this.drainSync) {
                            try {
                                if (this.draining) {
                                    this.drainSync.wait(3000);
                                }
                            } catch (Exception e2) {
                            }
                        }
                    }
                }
            }
            return 0;
        }

        public void read(Buffer buffer) throws IOException {
            if (this.closed) {
                throw new IOException("The source stream is closed");
            }
            Buffer current;
            synchronized (this.bufferQ) {
                while (!this.bufferQ.canRead()) {
                    try {
                        this.bufferQ.wait();
                    } catch (Exception e) {
                    }
                }
                current = this.bufferQ.read();
            }
            if (current.isEOM()) {
                synchronized (this.drainSync) {
                    if (this.draining) {
                        this.draining = false;
                        this.drainSync.notifyAll();
                    }
                }
            }
            Object data = buffer.getData();
            Object hdr = buffer.getHeader();
            buffer.copy(current);
            current.setData(data);
            current.setHeader(hdr);
            synchronized (this.bufferQ) {
                RawBufferMux.this.hasRead = true;
                this.bufferQ.readReport();
                this.bufferQ.notifyAll();
            }
        }

        /* access modifiers changed from: protected */
        public void reset() {
            synchronized (this.bufferQ) {
                while (this.bufferQ.canRead()) {
                    Buffer b = this.bufferQ.read();
                    this.bufferQ.readReport();
                }
                this.bufferQ.notifyAll();
            }
            synchronized (this.drainSync) {
                if (this.draining) {
                    this.draining = false;
                    this.drainSync.notifyAll();
                }
            }
        }

        public void run() {
            while (true) {
                try {
                    synchronized (this.startReq) {
                        while (!this.started && !this.closed) {
                            this.startReq.wait();
                        }
                    }
                    synchronized (this.bufferQ) {
                        do {
                            if (!RawBufferMux.this.hasRead) {
                                this.bufferQ.wait(250);
                            }
                            RawBufferMux.this.hasRead = false;
                            if (this.bufferQ.canRead() || this.closed) {
                            }
                        } while (this.started);
                    }
                    if (!this.closed) {
                        if (this.started && this.handler != null) {
                            this.handler.transferData(this);
                        }
                    } else {
                        return;
                    }
                } catch (InterruptedException e) {
                    System.err.println("Thread " + e.getMessage());
                    return;
                }
            }
            while (true) {
            }
        }

        public void setTransferHandler(BufferTransferHandler handler) {
            this.handler = handler;
        }

        /* access modifiers changed from: protected */
        public void start() {
            synchronized (this.startReq) {
                if (this.started) {
                    return;
                }
                this.started = true;
                this.startReq.notifyAll();
                synchronized (this.bufferQ) {
                    RawBufferMux.this.hasRead = true;
                    this.bufferQ.notifyAll();
                }
            }
        }

        /* access modifiers changed from: protected */
        public void stop() {
            synchronized (this.startReq) {
                this.started = false;
            }
            synchronized (this.bufferQ) {
                this.bufferQ.notifyAll();
            }
        }
    }

    class RawMuxTimeBase extends MediaTimeBase {
        long ticks = 0;
        boolean updated = false;

        RawMuxTimeBase() {
        }

        public long getMediaTime() {
            if (RawBufferMux.this.masterTrackID >= 0) {
                return RawBufferMux.this.mediaTime[RawBufferMux.this.masterTrackID];
            }
            if (!this.updated) {
                return this.ticks;
            }
            if (RawBufferMux.this.mediaTime.length == 1) {
                this.ticks = RawBufferMux.this.mediaTime[0];
            } else {
                this.ticks = RawBufferMux.this.mediaTime[0];
                for (int i = 1; i < RawBufferMux.this.mediaTime.length; i++) {
                    if (RawBufferMux.this.mediaTime[i] < this.ticks) {
                        this.ticks = RawBufferMux.this.mediaTime[i];
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

    public RawBufferMux() {
        this.supported = null;
        this.contentDesc = null;
        this.source = null;
        this.streams = null;
        this.clock = null;
        this.timeBase = null;
        this.masterTrackID = -1;
        this.sourceDisconnected = false;
        this.allowDrop = false;
        this.hasRead = false;
        this.numTracks = 0;
        this.mc = null;
        this.timeSetSync = new Object();
        this.started = false;
        this.systemStartTime = -1;
        this.mediaStartTime = -1;
        this.supported = new ContentDescriptor[1];
        this.supported[0] = new ContentDescriptor(ContentDescriptor.RAW);
        this.timeBase = new RawMuxTimeBase();
        this.clock = new BasicClock();
        try {
            this.clock.setTimeBase(this.timeBase);
        } catch (Exception e) {
        }
    }

    public void close() {
        if (this.source != null) {
            try {
                this.source.stop();
                this.source.disconnect();
            } catch (IOException e) {
            }
            this.source = null;
        }
        for (int i = 0; i < this.mc.length; i++) {
            if (this.mc[i] != null) {
                this.mc[i].close();
            }
        }
    }

    public DataSource getDataOutput() {
        return this.source;
    }

    public long getMediaNanoseconds() {
        return this.clock.getMediaNanoseconds();
    }

    public Time getMediaTime() {
        return this.clock.getMediaTime();
    }

    public String getName() {
        return "Raw Buffer Multiplexer";
    }

    public float getRate() {
        return this.clock.getRate();
    }

    public Time getStopTime() {
        return this.clock.getStopTime();
    }

    public Format[] getSupportedInputFormats() {
        return new Format[]{new AudioFormat(null), new VideoFormat(null)};
    }

    public ContentDescriptor[] getSupportedOutputContentDescriptors(Format[] fmt) {
        return this.supported;
    }

    public Time getSyncTime() {
        return this.clock.getSyncTime();
    }

    public TimeBase getTimeBase() {
        return this.clock.getTimeBase();
    }

    public boolean initializeTracks(Format[] trackFormats) {
        if (this.source.getStreams() != null) {
            throw new Error("initializeTracks has been called previously. ");
        }
        this.source.initialize(trackFormats);
        this.streams = (RawBufferSourceStream[]) this.source.getStreams();
        return true;
    }

    public Time mapToTimeBase(Time t) throws ClockStoppedException {
        return this.clock.mapToTimeBase(t);
    }

    public void open() throws ResourceUnavailableException {
        initializeTracks(this.trackFormats);
        if (this.source == null || this.source.getStreams() == null) {
            throw new ResourceUnavailableException("DataSource and SourceStreams were not created succesfully.");
        }
        try {
            this.source.connect();
            int len = 0;
            this.mediaTime = new long[this.trackFormats.length];
            this.mc = new MonitorAdapter[this.trackFormats.length];
            int i = 0;
            while (i < this.trackFormats.length) {
                this.mediaTime[i] = 0;
                if ((this.trackFormats[i] instanceof VideoFormat) || (this.trackFormats[i] instanceof AudioFormat)) {
                    this.mc[i] = new MonitorAdapter(this.trackFormats[i], this);
                    if (this.mc[i] != null) {
                        len++;
                    }
                }
                i++;
            }
            int j = 0;
            this.controls = new Control[len];
            for (i = 0; i < this.mc.length; i++) {
                if (this.mc[i] != null) {
                    int j2 = j + 1;
                    this.controls[j] = this.mc[i];
                    j = j2;
                }
            }
        } catch (IOException e) {
            throw new ResourceUnavailableException(e.getMessage());
        }
    }

    public int process(Buffer buffer, int trackID) {
        if ((buffer.getFlags() & 4096) != 0) {
            buffer.setFlags((buffer.getFlags() & -4097) | 256);
        }
        if (this.mc[trackID] != null && this.mc[trackID].isEnabled()) {
            this.mc[trackID].process(buffer);
        }
        if (this.streams == null || buffer == null || trackID >= this.streams.length) {
            return 1;
        }
        updateTime(buffer, trackID);
        return this.streams[trackID].process(buffer);
    }

    public void reset() {
        for (int i = 0; i < this.streams.length; i++) {
            this.streams[i].reset();
            if (this.mc[i] != null) {
                this.mc[i].reset();
            }
        }
    }

    public ContentDescriptor setContentDescriptor(ContentDescriptor outputContentDescriptor) {
        if (BasicPlugIn.matches(outputContentDescriptor, this.supported) == null) {
            return null;
        }
        this.contentDesc = outputContentDescriptor;
        this.source = new RawBufferDataSource();
        return this.contentDesc;
    }

    public Format setInputFormat(Format input, int trackID) {
        if (trackID < this.numTracks) {
            this.trackFormats[trackID] = input;
        }
        int i = 0;
        while (i < this.numTracks && this.trackFormats[i] != null) {
            i++;
        }
        return input;
    }

    public void setMediaTime(Time now) {
        synchronized (this.timeSetSync) {
            this.clock.setMediaTime(now);
            for (int i = 0; i < this.mediaTime.length; i++) {
                this.mediaTime[i] = now.getNanoseconds();
            }
            this.timeBase.update();
            this.systemStartTime = System.currentTimeMillis();
            this.mediaStartTime = now.getNanoseconds() / TimeSource.MICROS_PER_SEC;
        }
    }

    public int setNumTracks(int nTracks) {
        this.numTracks = nTracks;
        this.trackFormats = new Format[nTracks];
        for (int i = 0; i < nTracks; i++) {
            this.trackFormats[i] = null;
        }
        return nTracks;
    }

    public float setRate(float factor) {
        return factor == this.clock.getRate() ? factor : this.clock.setRate(1.0f);
    }

    public void setStopTime(Time stopTime) {
        this.clock.setStopTime(stopTime);
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
            this.systemStartTime = System.currentTimeMillis();
            this.mediaStartTime = getMediaNanoseconds() / TimeSource.MICROS_PER_SEC;
        }
    }

    /* access modifiers changed from: protected */
    public void updateTime(Buffer buf, int trackID) {
        if (buf.getFormat() instanceof AudioFormat) {
            if (!mpegAudio.matches(buf.getFormat())) {
                long t = ((AudioFormat) buf.getFormat()).computeDuration((long) buf.getLength());
                if (t >= 0) {
                    long[] jArr = this.mediaTime;
                    jArr[trackID] = jArr[trackID] + t;
                } else {
                    this.mediaTime[trackID] = buf.getTimeStamp();
                }
            } else if (buf.getTimeStamp() >= 0) {
                this.mediaTime[trackID] = buf.getTimeStamp();
            } else if (this.systemStartTime >= 0) {
                this.mediaTime[trackID] = ((this.mediaStartTime + System.currentTimeMillis()) - this.systemStartTime) * TimeSource.MICROS_PER_SEC;
            }
        } else if (buf.getTimeStamp() >= 0) {
            this.mediaTime[trackID] = buf.getTimeStamp();
        } else if (this.systemStartTime >= 0) {
            this.mediaTime[trackID] = ((this.mediaStartTime + System.currentTimeMillis()) - this.systemStartTime) * TimeSource.MICROS_PER_SEC;
        }
        this.timeBase.update();
    }
}
