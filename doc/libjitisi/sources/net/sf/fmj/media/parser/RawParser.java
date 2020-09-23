package net.sf.fmj.media.parser;

import javax.media.Demultiplexer;
import javax.media.Duration;
import javax.media.Time;
import javax.media.Track;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.Positionable;
import net.sf.fmj.media.BasicPlugIn;

public abstract class RawParser extends BasicPlugIn implements Demultiplexer {
    static final String NAME = "Raw parser";
    protected DataSource source;
    ContentDescriptor[] supported = new ContentDescriptor[]{new ContentDescriptor(ContentDescriptor.RAW)};

    public Object[] getControls() {
        return this.source.getControls();
    }

    public Time getDuration() {
        return this.source == null ? Duration.DURATION_UNKNOWN : this.source.getDuration();
    }

    public Time getMediaTime() {
        return Time.TIME_UNKNOWN;
    }

    public String getName() {
        return NAME;
    }

    public ContentDescriptor[] getSupportedInputContentDescriptors() {
        return this.supported;
    }

    public Track[] getTracks() {
        return null;
    }

    public boolean isPositionable() {
        return this.source instanceof Positionable;
    }

    public boolean isRandomAccess() {
        return (this.source instanceof Positionable) && ((Positionable) this.source).isRandomAccess();
    }

    public void reset() {
    }

    public Time setPosition(Time when, int round) {
        if (this.source instanceof Positionable) {
            return ((Positionable) this.source).setPosition(when, round);
        }
        return when;
    }
}
