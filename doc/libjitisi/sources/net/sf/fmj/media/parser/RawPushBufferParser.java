package net.sf.fmj.media.parser;

import com.lti.utils.UnsignedUtils;
import java.io.IOException;
import java.util.Vector;
import javax.media.Buffer;
import javax.media.Demultiplexer;
import javax.media.Format;
import javax.media.IncompatibleSourceException;
import javax.media.PlugInManager;
import javax.media.Time;
import javax.media.Track;
import javax.media.TrackListener;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.protocol.SourceStream;
import net.sf.fmj.media.BasicPlugIn;
import net.sf.fmj.media.CircularBuffer;
import net.sf.fmj.media.protocol.DelegateDataSource;
import net.sf.fmj.media.rtp.Depacketizer;
import org.jitsi.android.util.java.awt.Dimension;

public class RawPushBufferParser extends RawStreamParser {
    public static int[][] MPASampleTbl = new int[][]{new int[]{22050, 24000, 16000, 0}, new int[]{44100, 48000, 32000, 0}};
    static final String NAMEBUFFER = "Raw video/audio buffer stream parser";
    static VideoFormat h261Video = new VideoFormat(VideoFormat.H261_RTP);
    static VideoFormat h263Video = new VideoFormat(VideoFormat.H263_RTP);
    static VideoFormat jpegVideo = new VideoFormat(VideoFormat.JPEG_RTP);
    static AudioFormat mpegAudio = new AudioFormat(AudioFormat.MPEG_RTP);
    static VideoFormat mpegVideo = new VideoFormat(VideoFormat.MPEG_RTP);
    final float[] MPEGRateTbl = new float[]{0.0f, 23.976f, 24.0f, 25.0f, 29.97f, 30.0f, 50.0f, 59.94f, 60.0f};
    final int[] h261Heights = new int[]{144, 288};
    final int[] h261Widths = new int[]{176, 352};
    final int[] h263Heights = new int[]{0, 96, 144, 288, 576, 1152, 0, 0};
    final int[] h263Widths = new int[]{0, 128, 176, 352, 704, 1408, 0, 0};
    private boolean started = false;

    class FrameTrack implements Track, BufferTransferHandler {
        CircularBuffer bufferQ;
        boolean checkDepacketizer = false;
        boolean closed = false;
        Depacketizer depacketizer = null;
        boolean enabled = true;
        Format format = null;
        boolean keyFrameFound = false;
        Object keyFrameLock = new Object();
        TrackListener listener;
        Demultiplexer parser;
        PushBufferStream pbs;
        boolean stopped = true;

        public FrameTrack(Demultiplexer parser, PushBufferStream pbs, int numOfBufs) {
            this.pbs = pbs;
            this.format = pbs.getFormat();
            if ((RawPushBufferParser.this.source instanceof DelegateDataSource) || !RawPushBufferParser.this.isRTPFormat(this.format)) {
                this.keyFrameFound = true;
            }
            this.bufferQ = new CircularBuffer(numOfBufs);
            pbs.setTransferHandler(this);
        }

        public void close() {
            setEnabled(false);
            synchronized (this.bufferQ) {
                this.closed = true;
                this.bufferQ.notifyAll();
            }
        }

        private Depacketizer findDepacketizer(String name, Format input) {
            try {
                Object obj = BasicPlugIn.getClassForName(name).newInstance();
                if (!(obj instanceof Depacketizer)) {
                    return null;
                }
                Depacketizer dpktizer = (Depacketizer) obj;
                if (dpktizer.setInputFormat(input) == null) {
                    return null;
                }
                dpktizer.open();
                return dpktizer;
            } catch (Error | Exception e) {
                return null;
            }
        }

        public boolean findH261Key(Buffer b) {
            byte[] data = (byte[]) b.getData();
            if (data == null) {
                return false;
            }
            int offset = b.getOffset();
            if (data[offset + 4] != (byte) 0 || data[(offset + 4) + 1] != (byte) 1 || (data[(offset + 4) + 2] & 252) != 0) {
                return false;
            }
            int s = (data[(offset + 4) + 3] >> 3) & 1;
            this.format = new VideoFormat(VideoFormat.H261_RTP, new Dimension(RawPushBufferParser.this.h261Widths[s], RawPushBufferParser.this.h261Heights[s]), ((VideoFormat) this.format).getMaxDataLength(), ((VideoFormat) this.format).getDataType(), ((VideoFormat) this.format).getFrameRate());
            b.setFormat(this.format);
            return true;
        }

        public boolean findH263_1998Key(Buffer b) {
            byte[] data = (byte[]) b.getData();
            if (data == null) {
                return false;
            }
            int offset = b.getOffset();
            int payloadLen = (((data[offset] & 1) << 5) | ((data[offset + 1] & 248) >> 3)) + 2;
            if ((data[offset] & 2) != 0) {
                payloadLen++;
            }
            int picOffset = -1;
            if (payloadLen > 5) {
                if ((data[offset] & 2) == 2 && (data[offset + 3] & 252) == 128) {
                    picOffset = offset + 3;
                } else if ((data[offset + 2] & 252) == 128) {
                    picOffset = offset + 2;
                }
            } else if ((data[offset] & 4) == 4 && (data[offset + payloadLen] & 252) == 128) {
                picOffset = offset + payloadLen;
            }
            if (picOffset < 0) {
                return false;
            }
            int s = (data[picOffset + 2] >> 2) & 7;
            if (s == 7) {
                if (((data[picOffset + 3] >> 1) & 7) != 1) {
                    return false;
                }
                s = ((data[picOffset + 3] << 2) & 4) | ((data[picOffset + 4] >> 6) & 3);
            }
            if (s < 0) {
                return false;
            }
            this.format = new VideoFormat("h263-1998/rtp", new Dimension(RawPushBufferParser.this.h263Widths[s], RawPushBufferParser.this.h263Heights[s]), ((VideoFormat) this.format).getMaxDataLength(), ((VideoFormat) this.format).getDataType(), ((VideoFormat) this.format).getFrameRate());
            b.setFormat(this.format);
            return true;
        }

        public boolean findH263Key(Buffer b) {
            byte[] data = (byte[]) b.getData();
            if (data == null) {
                return false;
            }
            int payloadLen = getH263PayloadHeaderLength(data, b.getOffset());
            int offset = b.getOffset();
            if (data[offset + payloadLen] != (byte) 0 || data[(offset + payloadLen) + 1] != (byte) 0 || (data[(offset + payloadLen) + 2] & 252) != 128) {
                return false;
            }
            int s = (data[(offset + payloadLen) + 4] >> 2) & 7;
            this.format = new VideoFormat(VideoFormat.H263_RTP, new Dimension(RawPushBufferParser.this.h263Widths[s], RawPushBufferParser.this.h263Heights[s]), ((VideoFormat) this.format).getMaxDataLength(), ((VideoFormat) this.format).getDataType(), ((VideoFormat) this.format).getFrameRate());
            b.setFormat(this.format);
            return true;
        }

        public boolean findJPEGKey(Buffer b) {
            if ((b.getFlags() & 2048) == 0) {
                return false;
            }
            byte[] data = (byte[]) b.getData();
            this.format = new VideoFormat(VideoFormat.JPEG_RTP, new Dimension((data[b.getOffset() + 6] & UnsignedUtils.MAX_UBYTE) * 8, (data[b.getOffset() + 7] & UnsignedUtils.MAX_UBYTE) * 8), ((VideoFormat) this.format).getMaxDataLength(), ((VideoFormat) this.format).getDataType(), ((VideoFormat) this.format).getFrameRate());
            b.setFormat(this.format);
            return true;
        }

        private boolean findKeyFrame(Buffer buf) {
            if (!this.checkDepacketizer) {
                Vector pnames = PlugInManager.getPlugInList(buf.getFormat(), null, 6);
                if (pnames.size() != 0) {
                    this.depacketizer = findDepacketizer((String) pnames.elementAt(0), buf.getFormat());
                }
                this.checkDepacketizer = true;
            }
            Format fmt = buf.getFormat();
            if (fmt == null) {
                return false;
            }
            if (fmt.getEncoding() == null) {
                synchronized (this.keyFrameLock) {
                    this.keyFrameFound = true;
                    this.keyFrameLock.notifyAll();
                }
                return true;
            }
            boolean rtn = true;
            if (RawPushBufferParser.jpegVideo.matches(fmt)) {
                rtn = findJPEGKey(buf);
            } else if (RawPushBufferParser.h261Video.matches(fmt)) {
                rtn = findH261Key(buf);
            } else if (RawPushBufferParser.h263Video.matches(fmt)) {
                rtn = findH263Key(buf);
            } else if (RawPushBufferParser.mpegVideo.matches(fmt)) {
                rtn = findMPEGKey(buf);
            } else if (RawPushBufferParser.mpegAudio.matches(fmt)) {
                rtn = findMPAKey(buf);
            } else if (this.depacketizer != null) {
                fmt = this.depacketizer.parse(buf);
                if (fmt != null) {
                    this.format = fmt;
                    buf.setFormat(this.format);
                    this.depacketizer.close();
                    this.depacketizer = null;
                } else {
                    rtn = false;
                }
            }
            if (rtn) {
                synchronized (this.keyFrameLock) {
                    this.keyFrameFound = true;
                    this.keyFrameLock.notifyAll();
                }
            }
            return this.keyFrameFound;
        }

        public boolean findMPAKey(Buffer b) {
            int channels = 2;
            byte[] data = (byte[]) b.getData();
            if (data == null) {
                return false;
            }
            int off = b.getOffset();
            if (b.getLength() < 8 || data[off + 2] != (byte) 0 || data[off + 3] != (byte) 0) {
                return false;
            }
            off += 4;
            if ((data[off] & UnsignedUtils.MAX_UBYTE) != UnsignedUtils.MAX_UBYTE || (data[off + 1] & 246) <= 240 || (data[off + 2] & 240) == 240 || (data[off + 2] & 12) == 12 || (data[off + 3] & 3) == 2) {
                return false;
            }
            int id = (data[off + 1] >> 3) & 1;
            int six = (data[off + 2] >> 2) & 3;
            if (((data[off + 3] >> 6) & 3) == 3) {
                channels = 1;
            }
            this.format = new AudioFormat(AudioFormat.MPEG_RTP, (double) RawPushBufferParser.MPASampleTbl[id][six], 16, channels, 0, 1);
            b.setFormat(this.format);
            return true;
        }

        public boolean findMPEGKey(Buffer b) {
            byte[] data = (byte[]) b.getData();
            if (data == null) {
                return false;
            }
            int off = b.getOffset();
            if (b.getLength() < 12) {
                return false;
            }
            if ((data[off + 2] & 32) != 32) {
                return false;
            }
            if (data[off + 4] != (byte) 0 || data[off + 5] != (byte) 0 || data[off + 6] != (byte) 1 || (data[off + 7] & UnsignedUtils.MAX_UBYTE) != 179) {
                return false;
            }
            int frix = data[off + 11] & 15;
            if (frix == 0 || frix > 8) {
                return false;
            }
            int width = ((data[off + 8] & UnsignedUtils.MAX_UBYTE) << 4) | ((data[off + 9] & 240) >> 4);
            int height = ((data[off + 9] & 15) << 8) | (data[off + 10] & UnsignedUtils.MAX_UBYTE);
            this.format = new VideoFormat(VideoFormat.MPEG_RTP, new Dimension(width, height), ((VideoFormat) this.format).getMaxDataLength(), ((VideoFormat) this.format).getDataType(), RawPushBufferParser.this.MPEGRateTbl[frix]);
            b.setFormat(this.format);
            return true;
        }

        public Time getDuration() {
            return this.parser.getDuration();
        }

        public Format getFormat() {
            return this.format;
        }

        /* access modifiers changed from: 0000 */
        public int getH263PayloadHeaderLength(byte[] input, int offset) {
            byte b = input[offset];
            if ((b & 128) == 0) {
                return 4;
            }
            if ((b & 64) != 0) {
                return 12;
            }
            return 8;
        }

        public Time getStartTime() {
            return new Time(0);
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public Time mapFrameToTime(int frameNumber) {
            return new Time(0);
        }

        public int mapTimeToFrame(Time t) {
            return -1;
        }

        public void parse() {
            try {
                synchronized (this.keyFrameLock) {
                    while (!this.keyFrameFound) {
                        this.keyFrameLock.wait();
                    }
                }
            } catch (Exception e) {
            }
        }

        public void readFrame(Buffer buffer) {
            if (this.stopped) {
                buffer.setDiscard(true);
                buffer.setFormat(this.format);
                return;
            }
            synchronized (this.bufferQ) {
                while (!this.bufferQ.canRead()) {
                    try {
                        this.bufferQ.wait();
                        if (this.stopped) {
                            buffer.setDiscard(true);
                            buffer.setFormat(this.format);
                            return;
                        }
                    } catch (Exception e) {
                    }
                }
                Buffer filled = this.bufferQ.read();
                Object hdr = buffer.getHeader();
                buffer.copy(filled, true);
                filled.setHeader(hdr);
                this.format = filled.getFormat();
                synchronized (this.bufferQ) {
                    this.bufferQ.readReport();
                    this.bufferQ.notifyAll();
                }
            }
        }

        public void reset() {
        }

        public void setEnabled(boolean t) {
            if (t) {
                this.pbs.setTransferHandler(this);
            } else {
                this.pbs.setTransferHandler(null);
            }
            this.enabled = t;
        }

        public void setTrackListener(TrackListener l) {
            this.listener = l;
        }

        public void start() {
            synchronized (this.bufferQ) {
                this.stopped = false;
                if (RawPushBufferParser.this.source instanceof CaptureDevice) {
                    while (this.bufferQ.canRead()) {
                        this.bufferQ.read();
                        this.bufferQ.readReport();
                    }
                }
                this.bufferQ.notifyAll();
            }
        }

        public void stop() {
            synchronized (this.bufferQ) {
                this.stopped = true;
                this.bufferQ.notifyAll();
            }
        }

        /* JADX WARNING: Missing block: B:23:?, code skipped:
            r5.read(r0);
     */
        /* JADX WARNING: Missing block: B:42:0x0054, code skipped:
            r0.setDiscard(true);
     */
        /* JADX WARNING: Missing block: B:58:?, code skipped:
            return;
     */
        public void transferData(javax.media.protocol.PushBufferStream r5) {
            /*
            r4 = this;
            r3 = r4.bufferQ;
            monitor-enter(r3);
        L_0x0003:
            r2 = r4.bufferQ;	 Catch:{ all -> 0x0050 }
            r2 = r2.canWrite();	 Catch:{ all -> 0x0050 }
            if (r2 != 0) goto L_0x001b;
        L_0x000b:
            r2 = r4.closed;	 Catch:{ all -> 0x0050 }
            if (r2 != 0) goto L_0x001b;
        L_0x000f:
            r2 = r4.stopped;	 Catch:{ all -> 0x0050 }
            if (r2 != 0) goto L_0x001b;
        L_0x0013:
            r2 = r4.bufferQ;	 Catch:{ Exception -> 0x0019 }
            r2.wait();	 Catch:{ Exception -> 0x0019 }
            goto L_0x0003;
        L_0x0019:
            r2 = move-exception;
            goto L_0x0003;
        L_0x001b:
            r2 = r4.closed;	 Catch:{ all -> 0x0050 }
            if (r2 != 0) goto L_0x0023;
        L_0x001f:
            r2 = r4.stopped;	 Catch:{ all -> 0x0050 }
            if (r2 == 0) goto L_0x0025;
        L_0x0023:
            monitor-exit(r3);	 Catch:{ all -> 0x0050 }
        L_0x0024:
            return;
        L_0x0025:
            r2 = r4.bufferQ;	 Catch:{ all -> 0x0050 }
            r0 = r2.getEmptyBuffer();	 Catch:{ all -> 0x0050 }
            monitor-exit(r3);	 Catch:{ all -> 0x0050 }
            r5.read(r0);	 Catch:{ IOException -> 0x0053 }
        L_0x002f:
            r2 = r4.keyFrameFound;
            if (r2 != 0) goto L_0x0059;
        L_0x0033:
            r2 = r4.findKeyFrame(r0);
            if (r2 != 0) goto L_0x0059;
        L_0x0039:
            r3 = r4.bufferQ;
            monitor-enter(r3);
            r2 = r4.bufferQ;	 Catch:{ all -> 0x004d }
            r2.writeReport();	 Catch:{ all -> 0x004d }
            r2 = r4.bufferQ;	 Catch:{ all -> 0x004d }
            r2.read();	 Catch:{ all -> 0x004d }
            r2 = r4.bufferQ;	 Catch:{ all -> 0x004d }
            r2.readReport();	 Catch:{ all -> 0x004d }
            monitor-exit(r3);	 Catch:{ all -> 0x004d }
            goto L_0x0024;
        L_0x004d:
            r2 = move-exception;
            monitor-exit(r3);	 Catch:{ all -> 0x004d }
            throw r2;
        L_0x0050:
            r2 = move-exception;
            monitor-exit(r3);	 Catch:{ all -> 0x0050 }
            throw r2;
        L_0x0053:
            r1 = move-exception;
            r2 = 1;
            r0.setDiscard(r2);
            goto L_0x002f;
        L_0x0059:
            r3 = r4.bufferQ;
            monitor-enter(r3);
            r2 = r4.bufferQ;	 Catch:{ all -> 0x0068 }
            r2.writeReport();	 Catch:{ all -> 0x0068 }
            r2 = r4.bufferQ;	 Catch:{ all -> 0x0068 }
            r2.notifyAll();	 Catch:{ all -> 0x0068 }
            monitor-exit(r3);	 Catch:{ all -> 0x0068 }
            goto L_0x0024;
        L_0x0068:
            r2 = move-exception;
            monitor-exit(r3);	 Catch:{ all -> 0x0068 }
            throw r2;
            */
            throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.parser.RawPushBufferParser$FrameTrack.transferData(javax.media.protocol.PushBufferStream):void");
        }
    }

    public void close() {
        if (this.source != null) {
            try {
                this.source.stop();
                for (int i = 0; i < this.tracks.length; i++) {
                    ((FrameTrack) this.tracks[i]).stop();
                    ((FrameTrack) this.tracks[i]).close();
                }
                this.source.disconnect();
            } catch (Exception e) {
            }
            this.source = null;
        }
        this.started = false;
    }

    public String getName() {
        return NAMEBUFFER;
    }

    public Track[] getTracks() {
        for (Track track : this.tracks) {
            ((FrameTrack) track).parse();
        }
        return this.tracks;
    }

    /* access modifiers changed from: 0000 */
    public boolean isRTPFormat(Format fmt) {
        return (fmt == null || fmt.getEncoding() == null || (!fmt.getEncoding().endsWith("rtp") && !fmt.getEncoding().endsWith("RTP"))) ? false : true;
    }

    public void open() {
        if (this.tracks == null) {
            this.tracks = new Track[this.streams.length];
            for (int i = 0; i < this.streams.length; i++) {
                this.tracks[i] = new FrameTrack(this, (PushBufferStream) this.streams[i], 1);
            }
        }
    }

    public void reset() {
        for (Track track : this.tracks) {
            ((FrameTrack) track).reset();
        }
    }

    public void setSource(DataSource source) throws IOException, IncompatibleSourceException {
        if (source instanceof PushBufferDataSource) {
            this.streams = ((PushBufferDataSource) source).getStreams();
            if (this.streams == null) {
                throw new IOException("Got a null stream from the DataSource");
            } else if (this.streams.length == 0) {
                throw new IOException("Got a empty stream array from the DataSource");
            } else if (supports(this.streams)) {
                this.source = source;
                this.streams = this.streams;
                return;
            } else {
                throw new IncompatibleSourceException("DataSource not supported: " + source);
            }
        }
        throw new IncompatibleSourceException("DataSource not supported: " + source);
    }

    public void start() throws IOException {
        if (!this.started) {
            for (Track track : this.tracks) {
                ((FrameTrack) track).start();
            }
            this.source.start();
            this.started = true;
        }
    }

    public void stop() {
        int i = 0;
        while (i < this.tracks.length) {
            try {
                ((FrameTrack) this.tracks[i]).stop();
                i++;
            } catch (Exception e) {
            }
        }
        this.source.stop();
        this.started = false;
    }

    /* access modifiers changed from: protected */
    public boolean supports(SourceStream[] streams) {
        return streams[0] != null && (streams[0] instanceof PushBufferStream);
    }
}
