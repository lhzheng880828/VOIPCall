package net.sf.fmj.media.multiplexer;

import com.lti.utils.StringUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import net.sf.fmj.utility.FormatArgUtils;
import net.sf.fmj.utility.LoggerSingleton;

public class XmlMovieMux extends AbstractInputStreamMux {
    private static final Logger logger = LoggerSingleton.logger;
    private boolean headerWritten = false;
    private boolean trailerWritten = false;

    public XmlMovieMux() {
        super(new ContentDescriptor("video.xml"));
    }

    public void close() {
        if (!this.trailerWritten) {
            try {
                outputTrailer(getOutputStream());
                this.trailerWritten = true;
            } catch (IOException e) {
                logger.log(Level.WARNING, "" + e, e);
                throw new RuntimeException(e);
            }
        }
        super.close();
    }

    /* access modifiers changed from: protected */
    public void doProcess(Buffer buffer, int trackID, OutputStream os) throws IOException {
        if (!this.headerWritten) {
            outputHeader(os);
            this.headerWritten = true;
        }
        if (buffer.isEOM()) {
            if (!this.trailerWritten) {
                outputTrailer(os);
                this.trailerWritten = true;
            }
            os.close();
        } else if (!buffer.isDiscard()) {
            StringBuilder b = new StringBuilder();
            b.append("<Buffer");
            b.append(" track=\"" + trackID + "\"");
            if (buffer.getSequenceNumber() != Buffer.SEQUENCE_UNKNOWN) {
                b.append(" sequenceNumber=\"" + buffer.getSequenceNumber() + "\"");
            }
            b.append(" timeStamp=\"" + buffer.getTimeStamp() + "\"");
            if (buffer.getDuration() >= 0) {
                b.append(" duration=\"" + buffer.getDuration() + "\"");
            }
            if (buffer.getFlags() != 0) {
                b.append(" flags=\"" + Integer.toHexString(buffer.getFlags()) + "\"");
            }
            if (!(buffer.getFormat() == null || buffer.getFormat().equals(this.inputFormats[trackID]))) {
                b.append(" format=\"" + StringUtils.replaceSpecialXMLChars(FormatArgUtils.toString(buffer.getFormat())) + "\"");
            }
            b.append(">");
            b.append("<Data>");
            b.append(StringUtils.byteArrayToHexString((byte[]) buffer.getData(), buffer.getLength(), buffer.getOffset()));
            b.append("</Data>");
            b.append("</Buffer>\n");
            os.write(b.toString().getBytes());
        }
    }

    public Format[] getSupportedInputFormats() {
        Format[] formatArr = new Format[2];
        formatArr[0] = new AudioFormat(null, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray);
        formatArr[1] = new VideoFormat(null, null, -1, Format.byteArray, -1.0f);
        return formatArr;
    }

    public void open() throws ResourceUnavailableException {
        super.open();
        if (!this.headerWritten) {
            try {
                outputHeader(getOutputStream());
                this.headerWritten = true;
            } catch (IOException e) {
                logger.log(Level.WARNING, "" + e, e);
                throw new ResourceUnavailableException("" + e);
            }
        }
    }

    private void outputHeader(OutputStream os) throws IOException {
        os.write("<?xml version='1.0' encoding='utf-8'?>\n".getBytes());
        os.write("<XmlMovie version=\"1.0\">\n".getBytes());
        os.write("<Tracks>\n".getBytes());
        for (int i = 0; i < this.numTracks; i++) {
            os.write(("\t<Track index=\"" + i + "\" format=\"" + StringUtils.replaceSpecialXMLChars(FormatArgUtils.toString(this.inputFormats[i])) + "\"/>\n").getBytes());
        }
        os.write("</Tracks>\n".getBytes());
    }

    private void outputTrailer(OutputStream os) throws IOException {
        os.write("</XmlMovie>\n".getBytes());
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
}
