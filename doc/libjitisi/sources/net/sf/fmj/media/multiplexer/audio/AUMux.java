package net.sf.fmj.media.multiplexer.audio;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.Seekable;
import net.sf.fmj.media.codec.JavaSoundCodec;
import net.sf.fmj.media.multiplexer.AbstractInputStreamMux;
import net.sf.fmj.media.multiplexer.InputStreamPushDataSource;
import net.sf.fmj.media.multiplexer.InputStreamPushSourceStream;
import net.sf.fmj.media.renderer.audio.JavaSoundUtils;
import net.sf.fmj.utility.LoggerSingleton;

public class AUMux extends AbstractInputStreamMux {
    private static final Logger logger = LoggerSingleton.logger;
    private long bytesWritten;
    private boolean headerWritten = false;
    private boolean trailerWritten = false;

    public AUMux() {
        super(new FileTypeDescriptor(FileTypeDescriptor.BASIC_AUDIO));
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
            os.write((byte[]) buffer.getData(), buffer.getOffset(), buffer.getLength());
            this.bytesWritten += (long) buffer.getLength();
        }
    }

    public Format[] getSupportedInputFormats() {
        return new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, 8, -1, -1, 1), new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, -1, 1, 1), new AudioFormat(AudioFormat.LINEAR, -1.0d, 24, -1, 1, 1), new AudioFormat(AudioFormat.LINEAR, -1.0d, 32, -1, 1, 1), new AudioFormat(AudioFormat.ULAW), new AudioFormat(AudioFormat.ALAW)};
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
        byte[] header = JavaSoundCodec.createAuHeader(JavaSoundUtils.convertFormat((AudioFormat) this.inputFormats[0]));
        if (header == null) {
            throw new IOException("Unable to create AU header");
        }
        os.write(header);
    }

    private void outputTrailer(OutputStream os) throws IOException {
        DataSource ds = getDataOutput();
        if (ds instanceof InputStreamPushDataSource) {
            PushSourceStream pss = ((InputStreamPushDataSource) ds).getStreams()[0];
            if (pss instanceof InputStreamPushSourceStream) {
                InputStreamPushSourceStream ispss = (InputStreamPushSourceStream) pss;
                if (((Seekable) ispss.getTransferHandler()) instanceof Seekable) {
                    ((Seekable) ispss.getTransferHandler()).seek(8);
                    writeInt(os, this.bytesWritten);
                    if (getDataOutputNoInit() != null) {
                        getDataOutputNoInit().notifyDataAvailable(0);
                    }
                }
            }
        }
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
