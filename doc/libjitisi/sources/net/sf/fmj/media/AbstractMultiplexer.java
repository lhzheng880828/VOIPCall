package net.sf.fmj.media;

import java.util.logging.Logger;
import javax.media.Format;
import javax.media.Multiplexer;
import javax.media.protocol.ContentDescriptor;
import net.sf.fmj.utility.LoggerSingleton;

public abstract class AbstractMultiplexer extends AbstractPlugIn implements Multiplexer {
    private static final Logger logger = LoggerSingleton.logger;
    protected Format[] inputFormats;
    protected int numTracks;
    protected ContentDescriptor outputContentDescriptor;

    public ContentDescriptor setContentDescriptor(ContentDescriptor outputContentDescriptor) {
        this.outputContentDescriptor = outputContentDescriptor;
        return outputContentDescriptor;
    }

    public Format setInputFormat(Format format, int trackID) {
        if (trackID >= this.numTracks) {
            logger.warning("Rejecting input format for track number out of range: " + trackID + ": " + format);
            return null;
        }
        boolean match = false;
        for (Format supported : getSupportedInputFormats()) {
            if (supported.matches(format)) {
                match = true;
            }
        }
        if (match) {
            logger.finer("setInputFormat " + format + " " + trackID);
            if (this.inputFormats == null) {
                return format;
            }
            this.inputFormats[trackID] = format;
            return format;
        }
        logger.fine("Rejecting unsupported input format for track " + trackID + ": " + format);
        return null;
    }

    public int setNumTracks(int numTracks) {
        logger.finer("setNumTracks " + numTracks);
        this.inputFormats = new Format[numTracks];
        this.numTracks = numTracks;
        return numTracks;
    }
}
