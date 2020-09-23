package net.sf.fmj.media;

import java.io.IOException;
import javax.media.BadHeaderException;
import javax.media.Demultiplexer;
import javax.media.IncompatibleSourceException;
import javax.media.Time;
import javax.media.Track;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;

public abstract class AbstractDemultiplexer extends AbstractPlugIn implements Demultiplexer {
    public abstract ContentDescriptor[] getSupportedInputContentDescriptors();

    public abstract Track[] getTracks() throws IOException, BadHeaderException;

    public abstract void setSource(DataSource dataSource) throws IOException, IncompatibleSourceException;

    public Time getDuration() {
        return DURATION_UNKNOWN;
    }

    public Time getMediaTime() {
        return Time.TIME_UNKNOWN;
    }

    public boolean isPositionable() {
        return false;
    }

    public boolean isRandomAccess() {
        return false;
    }

    public Time setPosition(Time where, int rounding) {
        return Time.TIME_UNKNOWN;
    }

    public void start() throws IOException {
    }

    public void stop() {
    }
}
