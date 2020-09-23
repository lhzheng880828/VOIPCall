package net.sf.fmj.media;

import java.io.IOException;
import javax.media.Controller;
import javax.media.GainControl;
import javax.media.IncompatibleSourceException;
import javax.media.NotRealizedError;
import javax.media.Time;
import javax.media.TimeBase;
import javax.media.protocol.DataSource;
import net.sf.fmj.media.control.ProgressControl;
import org.jitsi.android.util.java.awt.Component;

public class MediaPlayer extends BasicPlayer {
    protected PlaybackEngine engine = new PlaybackEngine(this);

    /* access modifiers changed from: protected */
    public boolean audioEnabled() {
        return this.engine.audioEnabled();
    }

    public GainControl getGainControl() {
        if (getState() < Controller.Realized) {
            throwError(new NotRealizedError("Cannot get gain control on an unrealized player"));
        }
        return this.engine.getGainControl();
    }

    /* access modifiers changed from: protected */
    public TimeBase getMasterTimeBase() {
        return this.engine.getTimeBase();
    }

    public long getMediaNanoseconds() {
        if (this.controllerList.size() > 1) {
            return super.getMediaNanoseconds();
        }
        return this.engine.getMediaNanoseconds();
    }

    public Time getMediaTime() {
        if (this.controllerList.size() > 1) {
            return super.getMediaTime();
        }
        return this.engine.getMediaTime();
    }

    public Component getVisualComponent() {
        super.getVisualComponent();
        return this.engine.getVisualComponent();
    }

    public void setProgressControl(ProgressControl p) {
        this.engine.setProgressControl(p);
    }

    public void setSource(DataSource source) throws IOException, IncompatibleSourceException {
        this.engine.setSource(source);
        manageController(this.engine);
        super.setSource(source);
    }

    public void updateStats() {
        this.engine.updateRates();
    }

    /* access modifiers changed from: protected */
    public boolean videoEnabled() {
        return this.engine.videoEnabled();
    }
}
