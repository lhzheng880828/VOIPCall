package net.sf.fmj.media.multiplexer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.JPEGFormat;
import javax.media.protocol.ContentDescriptor;
import net.sf.fmj.media.format.GIFFormat;
import net.sf.fmj.media.format.PNGFormat;
import net.sf.fmj.utility.LoggerSingleton;

public class MultipartMixedReplaceMux extends AbstractInputStreamMux {
    public static final String BOUNDARY = "--ssBoundaryFMJ";
    private static final int MAX_TRACKS = 1;
    public static final String TIMESTAMP_KEY = "X-FMJ-Timestamp";
    private static final Logger logger = LoggerSingleton.logger;

    public MultipartMixedReplaceMux() {
        super(new ContentDescriptor("multipart.x_mixed_replace"));
    }

    /* access modifiers changed from: protected */
    public void doProcess(Buffer buffer, int trackID, OutputStream os) throws IOException {
        if (buffer.isEOM()) {
            os.close();
        } else if (!buffer.isDiscard()) {
            os.write("--ssBoundaryFMJ\n".getBytes());
            os.write(("Content-Type: image/" + buffer.getFormat().getEncoding() + "\n").getBytes());
            os.write(("Content-Length: " + buffer.getLength() + "\n").getBytes());
            os.write(("X-FMJ-Timestamp: " + buffer.getTimeStamp() + "\n").getBytes());
            os.write("\n".getBytes());
            os.write((byte[]) buffer.getData(), buffer.getOffset(), buffer.getLength());
            os.write("\n\n".getBytes());
        }
    }

    public Format[] getSupportedInputFormats() {
        return new Format[]{new JPEGFormat(), new GIFFormat(), new PNGFormat()};
    }

    public Format setInputFormat(Format format, int trackID) {
        logger.finer("setInputFormat " + format + " " + trackID);
        boolean match = false;
        for (Format supported : getSupportedInputFormats()) {
            if (format.matches(supported)) {
                match = true;
                break;
            }
        }
        if (!match) {
            logger.warning("Input format does not match any supported input format: " + format);
            return null;
        } else if (this.inputFormats == null) {
            return format;
        } else {
            this.inputFormats[trackID] = format;
            return format;
        }
    }

    public int setNumTracks(int numTracks) {
        if (numTracks > 1) {
            numTracks = 1;
        }
        return super.setNumTracks(numTracks);
    }
}
