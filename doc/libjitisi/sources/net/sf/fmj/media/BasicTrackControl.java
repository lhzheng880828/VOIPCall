package net.sf.fmj.media;

import java.util.Vector;
import javax.media.Codec;
import javax.media.Control;
import javax.media.Controller;
import javax.media.Format;
import javax.media.NotConfiguredError;
import javax.media.NotRealizedError;
import javax.media.Processor;
import javax.media.Renderer;
import javax.media.Track;
import javax.media.UnsupportedPlugInException;
import javax.media.control.FrameRateControl;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import net.sf.fmj.media.control.ProgressControl;
import net.sf.fmj.media.control.StringControl;
import org.jitsi.android.util.java.awt.Component;

public class BasicTrackControl implements TrackControl {
    static final String connectErr = "Cannot set a PlugIn before reaching the configured state.";
    static final String realizeErr = "Cannot get CodecControl before reaching the realized state.";
    PlaybackEngine engine;
    OutputConnector firstOC;
    float lastFrameRate = 0.0f;
    OutputConnector lastOC;
    long lastStatsTime = 0;
    protected Vector modules = new Vector(7);
    protected BasicMuxModule muxModule = null;
    protected boolean prefetchFailed = false;
    protected boolean rendererFailed = false;
    protected BasicRendererModule rendererModule;
    Track track;

    public BasicTrackControl(PlaybackEngine engine, Track track, OutputConnector oc) {
        this.engine = engine;
        this.track = track;
        this.firstOC = oc;
        this.lastOC = oc;
        setEnabled(track.isEnabled());
    }

    public boolean buildTrack(int trackID, int numTracks) {
        return false;
    }

    /* access modifiers changed from: protected */
    public FrameRateControl frameRateControl() {
        return null;
    }

    public Object getControl(String type) {
        try {
            Class<?> cls = BasicPlugIn.getClassForName(type);
            Object[] cs = getControls();
            for (int i = 0; i < cs.length; i++) {
                if (cls.isInstance(cs[i])) {
                    return cs[i];
                }
            }
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public Component getControlComponent() {
        return null;
    }

    public Object[] getControls() throws NotRealizedError {
        if (this.engine.getState() < Controller.Realized) {
            throw new NotRealizedError(realizeErr);
        }
        int i;
        OutputConnector oc = this.firstOC;
        Vector cv = new Vector();
        while (oc != null) {
            InputConnector ic = oc.getInputConnector();
            if (ic == null) {
                break;
            }
            Module m = ic.getModule();
            Object[] cs = m.getControls();
            if (cs != null) {
                for (Object addElement : cs) {
                    cv.addElement(addElement);
                }
            }
            oc = m.getOutputConnector(null);
        }
        int size = cv.size();
        Control[] controls = new Control[size];
        for (i = 0; i < size; i++) {
            controls[i] = (Control) cv.elementAt(i);
        }
        return controls;
    }

    public Format getFormat() {
        return this.track.getFormat();
    }

    public Format getOriginalFormat() {
        return this.track.getFormat();
    }

    public Format[] getSupportedFormats() {
        return new Format[]{this.track.getFormat()};
    }

    public boolean isCustomized() {
        return false;
    }

    public boolean isEnabled() {
        return this.track.isEnabled();
    }

    public boolean isTimeBase() {
        return false;
    }

    public boolean prefetchTrack() {
        int j = 0;
        while (j < this.modules.size()) {
            BasicModule bm = (BasicModule) this.modules.elementAt(j);
            if (bm.doPrefetch()) {
                j++;
            } else {
                setEnabled(false);
                this.prefetchFailed = true;
                if (!(bm instanceof BasicRendererModule)) {
                    return false;
                }
                this.rendererFailed = true;
                return false;
            }
        }
        if (this.prefetchFailed) {
            setEnabled(true);
            this.prefetchFailed = false;
            this.rendererFailed = false;
        }
        return true;
    }

    public void prError() {
        Log.error("  Unable to handle format: " + getOriginalFormat());
        Log.write("\n");
    }

    /* access modifiers changed from: protected */
    public ProgressControl progressControl() {
        return null;
    }

    public void setCodecChain(Codec[] codec) throws NotConfiguredError, UnsupportedPlugInException {
        if (this.engine.getState() > Processor.Configured) {
            throw new NotConfiguredError(connectErr);
        } else if (codec.length < 1) {
            throw new UnsupportedPlugInException("No codec specified in the array.");
        }
    }

    public void setEnabled(boolean enabled) {
        this.track.setEnabled(enabled);
    }

    public Format setFormat(Format format) {
        if (format == null || !format.matches(getFormat())) {
            return null;
        }
        return getFormat();
    }

    public void setRenderer(Renderer renderer) throws NotConfiguredError {
        if (this.engine.getState() > Processor.Configured) {
            throw new NotConfiguredError(connectErr);
        }
    }

    public void startTrack() {
        for (int j = 0; j < this.modules.size(); j++) {
            ((BasicModule) this.modules.elementAt(j)).doStart();
        }
    }

    public void stopTrack() {
        for (int j = 0; j < this.modules.size(); j++) {
            ((BasicModule) this.modules.elementAt(j)).doStop();
        }
    }

    public void updateFormat() {
        if (this.track.isEnabled()) {
            ProgressControl pc = progressControl();
            if (pc != null) {
                StringControl sc;
                if (this.track.getFormat() instanceof AudioFormat) {
                    String channel = "";
                    AudioFormat afmt = (AudioFormat) this.track.getFormat();
                    pc.getAudioCodec().setValue(afmt.getEncoding());
                    sc = pc.getAudioProperties();
                    if (afmt.getChannels() == 1) {
                        channel = "mono";
                    } else {
                        channel = "stereo";
                    }
                    sc.setValue((afmt.getSampleRate() / 1000.0d) + " KHz, " + afmt.getSampleSizeInBits() + "-bit, " + channel);
                }
                if (this.track.getFormat() instanceof VideoFormat) {
                    VideoFormat vfmt = (VideoFormat) this.track.getFormat();
                    pc.getVideoCodec().setValue(vfmt.getEncoding());
                    sc = pc.getVideoProperties();
                    if (vfmt.getSize() != null) {
                        sc.setValue(vfmt.getSize().width + " X " + vfmt.getSize().height);
                    }
                }
            }
        }
    }

    public void updateRates(long now) {
        FrameRateControl prc = frameRateControl();
        if (prc == null || !this.track.isEnabled() || !(this.track.getFormat() instanceof VideoFormat)) {
            return;
        }
        if (this.rendererModule != null || this.muxModule != null) {
            float rate;
            if (now == this.lastStatsTime) {
                rate = this.lastFrameRate;
            } else {
                int framesPlayed;
                if (this.rendererModule != null) {
                    framesPlayed = this.rendererModule.getFramesPlayed();
                } else {
                    framesPlayed = this.muxModule.getFramesPlayed();
                }
                rate = (((float) framesPlayed) / ((float) (now - this.lastStatsTime))) * 1000.0f;
            }
            prc.setFrameRate(((float) ((int) (((this.lastFrameRate + rate) / 2.0f) * 10.0f))) / 10.0f);
            this.lastFrameRate = rate;
            this.lastStatsTime = now;
            if (this.rendererModule != null) {
                this.rendererModule.resetFramesPlayed();
            } else {
                this.muxModule.resetFramesPlayed();
            }
        }
    }
}
