package net.sf.fmj.media;

import java.io.IOException;
import javax.media.GainControl;
import javax.media.IncompatibleSourceException;
import javax.media.NotConfiguredError;
import javax.media.NotRealizedError;
import javax.media.Time;
import javax.media.TimeBase;
import javax.media.control.TrackControl;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import org.jitsi.android.util.java.awt.Component;

public class MediaProcessor extends BasicProcessor {
    protected ProcessEngine engine = new ProcessEngine(this);

    /* access modifiers changed from: protected */
    public boolean audioEnabled() {
        return this.engine.audioEnabled();
    }

    public ContentDescriptor getContentDescriptor() throws NotConfiguredError {
        return this.engine.getContentDescriptor();
    }

    public DataSource getDataOutput() throws NotRealizedError {
        return this.engine.getDataOutput();
    }

    public GainControl getGainControl() {
        super.getGainControl();
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

    public ContentDescriptor[] getSupportedContentDescriptors() throws NotConfiguredError {
        return this.engine.getSupportedContentDescriptors();
    }

    public TrackControl[] getTrackControls() throws NotConfiguredError {
        return this.engine.getTrackControls();
    }

    public Component getVisualComponent() {
        super.getVisualComponent();
        return this.engine.getVisualComponent();
    }

    public ContentDescriptor setContentDescriptor(ContentDescriptor ocd) throws NotConfiguredError {
        return this.engine.setContentDescriptor(ocd);
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
