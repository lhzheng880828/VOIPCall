package net.sf.fmj.media.parser;

import java.io.IOException;
import javax.media.Buffer;
import javax.media.Demultiplexer;
import javax.media.Format;
import javax.media.IncompatibleSourceException;
import javax.media.Time;
import javax.media.Track;
import javax.media.TrackListener;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushDataSource;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.SourceStream;
import javax.media.protocol.SourceTransferHandler;
import net.sf.fmj.media.CircularBuffer;

public class RawStreamParser extends RawParser {
    static final String NAME = "Raw stream parser";
    protected SourceStream[] streams;
    protected Track[] tracks = null;

    class FrameTrack implements Track, SourceTransferHandler {
        CircularBuffer bufferQ;
        boolean enabled = true;
        Format format = null;
        TrackListener listener;
        Demultiplexer parser;
        PushSourceStream pss;
        Integer stateReq = new Integer(0);
        boolean stopped = true;

        public FrameTrack(Demultiplexer parser, PushSourceStream pss, int numOfBufs) {
            this.pss = pss;
            pss.setTransferHandler(this);
            this.bufferQ = new CircularBuffer(numOfBufs);
        }

        public Time getDuration() {
            return this.parser.getDuration();
        }

        public Format getFormat() {
            return this.format;
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

        /* JADX WARNING: Missing block: B:8:0x0013, code skipped:
            r3 = r5.bufferQ;
     */
        /* JADX WARNING: Missing block: B:9:0x0015, code skipped:
            monitor-enter(r3);
     */
        /* JADX WARNING: Missing block: B:12:0x001c, code skipped:
            if (r5.bufferQ.canRead() != false) goto L_0x0043;
     */
        /* JADX WARNING: Missing block: B:14:?, code skipped:
            r5.bufferQ.wait();
            r4 = r5.stateReq;
     */
        /* JADX WARNING: Missing block: B:15:0x0025, code skipped:
            monitor-enter(r4);
     */
        /* JADX WARNING: Missing block: B:18:0x0028, code skipped:
            if (r5.stopped == false) goto L_0x003c;
     */
        /* JADX WARNING: Missing block: B:19:0x002a, code skipped:
            r6.setDiscard(true);
            r6.setFormat(r5.format);
     */
        /* JADX WARNING: Missing block: B:20:0x0033, code skipped:
            monitor-exit(r4);
     */
        /* JADX WARNING: Missing block: B:22:?, code skipped:
            monitor-exit(r3);
     */
        /* JADX WARNING: Missing block: B:32:?, code skipped:
            monitor-exit(r4);
     */
        /* JADX WARNING: Missing block: B:41:?, code skipped:
            r1 = r5.bufferQ.read();
            r5.bufferQ.notifyAll();
     */
        /* JADX WARNING: Missing block: B:42:0x004e, code skipped:
            monitor-exit(r3);
     */
        /* JADX WARNING: Missing block: B:43:0x004f, code skipped:
            r0 = (byte[]) r1.getData();
            r1.setData(r6.getData());
            r6.setData(r0);
            r6.setLength(r1.getLength());
            r6.setFormat(r5.format);
            r6.setTimeStamp(-1);
            r3 = r5.bufferQ;
     */
        /* JADX WARNING: Missing block: B:44:0x0075, code skipped:
            monitor-enter(r3);
     */
        /* JADX WARNING: Missing block: B:46:?, code skipped:
            r5.bufferQ.readReport();
            r5.bufferQ.notifyAll();
     */
        /* JADX WARNING: Missing block: B:47:0x0080, code skipped:
            monitor-exit(r3);
     */
        /* JADX WARNING: Missing block: B:60:?, code skipped:
            return;
     */
        /* JADX WARNING: Missing block: B:61:?, code skipped:
            return;
     */
        public void readFrame(javax.media.Buffer r6) {
            /*
            r5 = this;
            r3 = r5.stateReq;
            monitor-enter(r3);
            r2 = r5.stopped;	 Catch:{ all -> 0x0039 }
            if (r2 == 0) goto L_0x0012;
        L_0x0007:
            r2 = 1;
            r6.setDiscard(r2);	 Catch:{ all -> 0x0039 }
            r2 = r5.format;	 Catch:{ all -> 0x0039 }
            r6.setFormat(r2);	 Catch:{ all -> 0x0039 }
            monitor-exit(r3);	 Catch:{ all -> 0x0039 }
        L_0x0011:
            return;
        L_0x0012:
            monitor-exit(r3);	 Catch:{ all -> 0x0039 }
            r3 = r5.bufferQ;
            monitor-enter(r3);
        L_0x0016:
            r2 = r5.bufferQ;	 Catch:{ all -> 0x0036 }
            r2 = r2.canRead();	 Catch:{ all -> 0x0036 }
            if (r2 != 0) goto L_0x0043;
        L_0x001e:
            r2 = r5.bufferQ;	 Catch:{ Exception -> 0x0041 }
            r2.wait();	 Catch:{ Exception -> 0x0041 }
            r4 = r5.stateReq;	 Catch:{ Exception -> 0x0041 }
            monitor-enter(r4);	 Catch:{ Exception -> 0x0041 }
            r2 = r5.stopped;	 Catch:{ all -> 0x003e }
            if (r2 == 0) goto L_0x003c;
        L_0x002a:
            r2 = 1;
            r6.setDiscard(r2);	 Catch:{ all -> 0x003e }
            r2 = r5.format;	 Catch:{ all -> 0x003e }
            r6.setFormat(r2);	 Catch:{ all -> 0x003e }
            monitor-exit(r4);	 Catch:{ all -> 0x003e }
            monitor-exit(r3);	 Catch:{ all -> 0x0036 }
            goto L_0x0011;
        L_0x0036:
            r2 = move-exception;
            monitor-exit(r3);	 Catch:{ all -> 0x0036 }
            throw r2;
        L_0x0039:
            r2 = move-exception;
            monitor-exit(r3);	 Catch:{ all -> 0x0039 }
            throw r2;
        L_0x003c:
            monitor-exit(r4);	 Catch:{ all -> 0x003e }
            goto L_0x0016;
        L_0x003e:
            r2 = move-exception;
            monitor-exit(r4);	 Catch:{ all -> 0x003e }
            throw r2;	 Catch:{ Exception -> 0x0041 }
        L_0x0041:
            r2 = move-exception;
            goto L_0x0016;
        L_0x0043:
            r2 = r5.bufferQ;	 Catch:{ all -> 0x0036 }
            r1 = r2.read();	 Catch:{ all -> 0x0036 }
            r2 = r5.bufferQ;	 Catch:{ all -> 0x0036 }
            r2.notifyAll();	 Catch:{ all -> 0x0036 }
            monitor-exit(r3);	 Catch:{ all -> 0x0036 }
            r2 = r1.getData();
            r2 = (byte[]) r2;
            r0 = r2;
            r0 = (byte[]) r0;
            r2 = r6.getData();
            r1.setData(r2);
            r6.setData(r0);
            r2 = r1.getLength();
            r6.setLength(r2);
            r2 = r5.format;
            r6.setFormat(r2);
            r2 = -1;
            r6.setTimeStamp(r2);
            r3 = r5.bufferQ;
            monitor-enter(r3);
            r2 = r5.bufferQ;	 Catch:{ all -> 0x0082 }
            r2.readReport();	 Catch:{ all -> 0x0082 }
            r2 = r5.bufferQ;	 Catch:{ all -> 0x0082 }
            r2.notifyAll();	 Catch:{ all -> 0x0082 }
            monitor-exit(r3);	 Catch:{ all -> 0x0082 }
            goto L_0x0011;
        L_0x0082:
            r2 = move-exception;
            monitor-exit(r3);	 Catch:{ all -> 0x0082 }
            throw r2;
            */
            throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.parser.RawStreamParser$FrameTrack.readFrame(javax.media.Buffer):void");
        }

        public void setEnabled(boolean t) {
            if (t) {
                this.pss.setTransferHandler(this);
            } else {
                this.pss.setTransferHandler(null);
            }
            this.enabled = t;
        }

        public void setTrackListener(TrackListener l) {
            this.listener = l;
        }

        public void start() {
            synchronized (this.stateReq) {
                this.stopped = false;
            }
            synchronized (this.bufferQ) {
                this.bufferQ.notifyAll();
            }
        }

        public void stop() {
            synchronized (this.stateReq) {
                this.stopped = true;
            }
            synchronized (this.bufferQ) {
                this.bufferQ.notifyAll();
            }
        }

        public void transferData(PushSourceStream pss) {
            Buffer buffer;
            synchronized (this.bufferQ) {
                while (!this.bufferQ.canWrite()) {
                    try {
                        this.bufferQ.wait();
                    } catch (Exception e) {
                    }
                }
                buffer = this.bufferQ.getEmptyBuffer();
                this.bufferQ.notifyAll();
            }
            int size = pss.getMinimumTransferSize();
            byte[] data = (byte[]) buffer.getData();
            if (data == null || data.length < size) {
                data = new byte[size];
                buffer.setData(data);
            }
            try {
                buffer.setLength(pss.read(data, 0, size));
            } catch (IOException e2) {
                buffer.setDiscard(true);
            }
            synchronized (this.bufferQ) {
                this.bufferQ.writeReport();
                this.bufferQ.notifyAll();
            }
        }
    }

    public void close() {
        if (this.source != null) {
            try {
                this.source.stop();
                for (Track track : this.tracks) {
                    ((FrameTrack) track).stop();
                }
                this.source.disconnect();
            } catch (IOException e) {
            }
            this.source = null;
        }
    }

    public String getName() {
        return NAME;
    }

    public Track[] getTracks() {
        return this.tracks;
    }

    public void open() {
        if (this.tracks == null) {
            this.tracks = new Track[this.streams.length];
            for (int i = 0; i < this.streams.length; i++) {
                this.tracks[i] = new FrameTrack(this, (PushSourceStream) this.streams[i], 5);
            }
        }
    }

    public void setSource(DataSource source) throws IOException, IncompatibleSourceException {
        if (source instanceof PushDataSource) {
            this.streams = ((PushDataSource) source).getStreams();
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
        this.source.start();
        for (Track track : this.tracks) {
            ((FrameTrack) track).start();
        }
    }

    public void stop() {
        try {
            this.source.stop();
            for (Track track : this.tracks) {
                ((FrameTrack) track).stop();
            }
        } catch (IOException e) {
        }
    }

    /* access modifiers changed from: protected */
    public boolean supports(SourceStream[] streams) {
        return streams[0] != null && (streams[0] instanceof PushSourceStream);
    }
}
