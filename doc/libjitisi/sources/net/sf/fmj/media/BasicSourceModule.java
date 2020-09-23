package net.sf.fmj.media;

import java.io.IOException;
import java.util.Vector;
import javax.media.BadHeaderException;
import javax.media.Demultiplexer;
import javax.media.Duration;
import javax.media.Format;
import javax.media.IncompatibleSourceException;
import javax.media.PlugInManager;
import javax.media.ResourceUnavailableException;
import javax.media.SystemTimeBase;
import javax.media.Time;
import javax.media.Track;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.Positionable;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PushDataSource;
import javax.media.protocol.SourceStream;
import net.sf.fmj.media.rtp.util.RTPTimeBase;

public class BasicSourceModule extends BasicModule implements Duration, Positionable {
    protected long bitsRead = 0;
    String cname = null;
    protected String[] connectorNames;
    long currentRTPTime = 0;
    protected long currentSystemTime = 0;
    PlaybackEngine engine;
    public String errMsg = null;
    protected Time lastPositionSet = new Time(0);
    protected long lastSystemTime = 0;
    int latencyTrack = -1;
    protected SourceThread[] loops;
    long oldOffset = 0;
    protected long originSystemTime = 0;
    protected Demultiplexer parser;
    Object resetSync = new Object();
    RTPTimeBase rtpMapper = null;
    RTPTimeBase rtpMapperUpdatable = null;
    boolean rtpOffsetInvalid = true;
    protected DataSource source;
    protected boolean started = false;
    protected SystemTimeBase systemTimeBase = new SystemTimeBase();
    protected Track[] tracks = new Track[0];

    protected static Demultiplexer createDemultiplexer(DataSource ds) throws IOException, IncompatibleSourceException {
        Vector cnames = PlugInManager.getPlugInList(new ContentDescriptor(ds.getContentType()), null, 1);
        Demultiplexer parser = null;
        IOException ioe = null;
        IncompatibleSourceException ise = null;
        int i = 0;
        while (i < cnames.size()) {
            try {
                Object p = BasicPlugIn.getClassForName((String) cnames.elementAt(i)).newInstance();
                if (p instanceof Demultiplexer) {
                    parser = (Demultiplexer) p;
                    try {
                        parser.setSource(ds);
                        break;
                    } catch (IOException e) {
                        parser = null;
                        ioe = e;
                    } catch (IncompatibleSourceException e2) {
                        parser = null;
                        ise = e2;
                    }
                } else {
                    continue;
                    i++;
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e3) {
            }
        }
        if (parser == null) {
            if (ioe != null) {
                throw ioe;
            } else if (ise != null) {
                throw ise;
            }
        }
        return parser;
    }

    public static BasicSourceModule createModule(DataSource ds) throws IOException, IncompatibleSourceException {
        Demultiplexer parser = createDemultiplexer(ds);
        if (parser == null) {
            return null;
        }
        return new BasicSourceModule(ds, parser);
    }

    protected BasicSourceModule(DataSource ds, Demultiplexer demux) {
        this.source = ds;
        this.parser = demux;
        SourceStream stream;
        if (this.source instanceof PullDataSource) {
            stream = ((PullDataSource) this.source).getStreams()[0];
        } else if (this.source instanceof PushDataSource) {
            stream = ((PushDataSource) this.source).getStreams()[0];
        }
    }

    public void abortPrefetch() {
        doStop();
    }

    public void abortRealize() {
        this.parser.stop();
        this.parser.close();
    }

    /* access modifiers changed from: protected */
    public boolean checkAllPaused() {
        int i = 0;
        while (i < this.loops.length) {
            if (this.tracks[i].isEnabled() && this.loops[i] != null && !this.loops[i].isPaused()) {
                return false;
            }
            i++;
        }
        return true;
    }

    public void checkLatency() {
        if (this.latencyTrack > -1) {
            if (!this.tracks[this.latencyTrack].isEnabled() || this.loops[this.latencyTrack] == null) {
                this.latencyTrack = -1;
            } else {
                this.loops[this.latencyTrack].checkLatency = true;
                return;
            }
        }
        for (int i = 0; i < this.tracks.length; i++) {
            if (this.tracks[i].isEnabled()) {
                this.latencyTrack = i;
                if (this.tracks[i].getFormat() instanceof VideoFormat) {
                    break;
                }
            }
        }
        if (this.latencyTrack > -1 && this.loops[this.latencyTrack] != null) {
            this.loops[this.latencyTrack].checkLatency = true;
        }
    }

    /* access modifiers changed from: 0000 */
    public SourceThread createSourceThread(int idx) {
        MyOutputConnector oc = (MyOutputConnector) getOutputConnector(this.connectorNames[idx]);
        SourceThread thread;
        if (oc == null || oc.getInputConnector() == null) {
            this.tracks[idx].setEnabled(false);
            thread = null;
            return null;
        }
        SourceThread thread2 = new SourceThread(this, oc, idx);
        if (this.tracks[idx].getFormat() instanceof AudioFormat) {
            thread2.useAudioPriority();
        } else {
            thread2.useVideoPriority();
        }
        thread = thread2;
        return thread2;
    }

    public void doClose() {
        this.parser.close();
        if (this.tracks != null) {
            for (int i = 0; i < this.tracks.length; i++) {
                if (this.loops[i] != null) {
                    this.loops[i].kill();
                }
            }
            if (this.rtpMapperUpdatable != null) {
                RTPTimeBase.returnMapperUpdatable(this.rtpMapperUpdatable);
                this.rtpMapperUpdatable = null;
            }
        }
    }

    public void doDealloc() {
    }

    public void doFailedPrefetch() {
    }

    public void doFailedRealize() {
        this.parser.stop();
        this.parser.close();
    }

    public boolean doPrefetch() {
        super.doPrefetch();
        return true;
    }

    public boolean doRealize() {
        try {
            this.parser.open();
            try {
                this.parser.start();
                this.tracks = this.parser.getTracks();
                if (this.tracks == null || this.tracks.length == 0) {
                    this.errMsg = "The media has 0 track";
                    this.parser.close();
                    return false;
                }
                this.loops = new SourceThread[this.tracks.length];
                this.connectorNames = new String[this.tracks.length];
                for (int i = 0; i < this.tracks.length; i++) {
                    MyOutputConnector oc = new MyOutputConnector(this.tracks[i]);
                    oc.setProtocol(0);
                    oc.setSize(1);
                    this.connectorNames[i] = this.tracks[i].toString();
                    registerOutputConnector(this.tracks[i].toString(), oc);
                    this.loops[i] = null;
                }
                this.engine = (PlaybackEngine) getController();
                if (this.engine == null || !this.engine.isRTP()) {
                    this.parser.stop();
                }
                return true;
            } catch (BadHeaderException e) {
                this.errMsg = "Bad header in the media: " + e.getMessage();
                this.parser.close();
                return false;
            } catch (IOException e2) {
                this.errMsg = "IO exception: " + e2.getMessage();
                this.parser.close();
                return false;
            }
        } catch (ResourceUnavailableException e3) {
            this.errMsg = "Resource unavailable: " + e3.getMessage();
            return false;
        }
    }

    public void doStart() {
        this.lastSystemTime = this.systemTimeBase.getNanoseconds();
        this.originSystemTime = this.currentSystemTime;
        this.rtpOffsetInvalid = true;
        super.doStart();
        try {
            this.parser.start();
        } catch (IOException e) {
        }
        for (int i = 0; i < this.loops.length; i++) {
            if (this.tracks[i].isEnabled()) {
                if (this.loops[i] == null) {
                    SourceThread[] sourceThreadArr = this.loops;
                    SourceThread createSourceThread = createSourceThread(i);
                    sourceThreadArr[i] = createSourceThread;
                    if (createSourceThread == null) {
                    }
                }
                this.loops[i].start();
            }
        }
        this.started = true;
    }

    public void doStop() {
        this.started = false;
    }

    public long getBitsRead() {
        return this.bitsRead;
    }

    public Object getControl(String s) {
        return this.parser.getControl(s);
    }

    public Object[] getControls() {
        return this.parser.getControls();
    }

    public Demultiplexer getDemultiplexer() {
        return this.parser;
    }

    public Time getDuration() {
        return this.parser.getDuration();
    }

    public String[] getOutputConnectorNames() {
        return this.connectorNames;
    }

    public boolean isPositionable() {
        return this.parser.isPositionable();
    }

    public boolean isRandomAccess() {
        return this.parser.isRandomAccess();
    }

    public void pause() {
        synchronized (this.resetSync) {
            int i = 0;
            while (i < this.loops.length) {
                if (!(!this.tracks[i].isEnabled() || this.loops[i] == null || this.loops[i].resetted)) {
                    this.loops[i].pause();
                }
                i++;
            }
            this.parser.stop();
        }
    }

    public void process() {
    }

    /* access modifiers changed from: 0000 */
    public boolean readHasBlocked() {
        if (this.loops == null) {
            return false;
        }
        int i = 0;
        while (i < this.loops.length) {
            if (this.loops[i] != null && this.loops[i].readBlocked) {
                return true;
            }
            i++;
        }
        return false;
    }

    public void reset() {
        synchronized (this.resetSync) {
            super.reset();
            for (int i = 0; i < this.loops.length; i++) {
                if (this.tracks[i].isEnabled()) {
                    if (this.loops[i] == null) {
                        SourceThread[] sourceThreadArr = this.loops;
                        SourceThread createSourceThread = createSourceThread(i);
                        sourceThreadArr[i] = createSourceThread;
                        if (createSourceThread == null) {
                        }
                    }
                    this.loops[i].resetted = true;
                    this.loops[i].start();
                }
            }
        }
    }

    public void resetBitsRead() {
        this.bitsRead = 0;
    }

    public void setFormat(Connector connector, Format format) {
    }

    public Time setPosition(Time when, int rounding) {
        Time t = this.parser.setPosition(when, rounding);
        if (this.lastPositionSet.getNanoseconds() == t.getNanoseconds()) {
            this.lastPositionSet = new Time(t.getNanoseconds() + 1);
        } else {
            this.lastPositionSet = t;
        }
        return t;
    }
}
