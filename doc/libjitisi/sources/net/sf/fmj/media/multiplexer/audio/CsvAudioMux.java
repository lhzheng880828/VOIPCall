package net.sf.fmj.media.multiplexer.audio;

import com.lti.utils.UnsignedUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import javax.media.protocol.ContentDescriptor;
import net.sf.fmj.media.multiplexer.AbstractInputStreamMux;
import net.sf.fmj.utility.FormatArgUtils;
import net.sf.fmj.utility.LoggerSingleton;

public class CsvAudioMux extends AbstractInputStreamMux {
    private static final Logger logger = LoggerSingleton.logger;
    private boolean headerWritten = false;
    private boolean trailerWritten = false;

    public static void audioBufferToStream(AudioFormat f, Buffer buffer, OutputStream os) throws IOException {
        byte[] data = (byte[]) buffer.getData();
        int sampleSizeInBytes = f.getSampleSizeInBits() / 8;
        if (sampleSizeInBytes * 8 != f.getSampleSizeInBits()) {
            throw new RuntimeException("Sample size in bytes must be divisible by 8");
        }
        int frameSizeInBytes = sampleSizeInBytes * f.getChannels();
        int framesInBuffer = buffer.getLength() / frameSizeInBytes;
        if (buffer.getLength() != framesInBuffer * frameSizeInBytes) {
            throw new RuntimeException("Length of buffer not an integral number of samples");
        }
        long inputUnsignedMax = (1 << f.getSampleSizeInBits()) - 1;
        long inputSignedMax = (1 << (f.getSampleSizeInBits() - 1)) - 1;
        for (int frame = 0; frame < framesInBuffer; frame++) {
            for (int channel = 0; channel < f.getChannels(); channel++) {
                long inputSampleLongWithSign;
                long inputSampleLongWithoutSign = UnsignedUtils.uIntToLong(getSample(data, (buffer.getOffset() + (frame * frameSizeInBytes)) + (channel * sampleSizeInBytes), sampleSizeInBytes, f.getEndian()));
                if (f.getSigned() == 0) {
                    inputSampleLongWithSign = inputSampleLongWithoutSign;
                } else if (f.getSigned() != 1) {
                    throw new RuntimeException("input format signed not specified");
                } else if (inputSampleLongWithoutSign > inputSignedMax) {
                    inputSampleLongWithSign = (inputSampleLongWithoutSign - inputUnsignedMax) - 1;
                } else {
                    inputSampleLongWithSign = inputSampleLongWithoutSign;
                }
                if (channel > 0) {
                    os.write(",".getBytes());
                }
                os.write(("" + inputSampleLongWithSign).getBytes());
            }
            os.write("\n".getBytes());
        }
    }

    private static int getSample(byte[] inputBufferData, int byteOffsetOfSample, int inputSampleSizeInBytes, int inputEndian) {
        int sample = 0;
        int j = 0;
        while (j < inputSampleSizeInBytes) {
            sample = (sample << 8) | (inputBufferData[byteOffsetOfSample + (inputEndian == 1 ? j : (inputSampleSizeInBytes - 1) - j)] & UnsignedUtils.MAX_UBYTE);
            j++;
        }
        return sample;
    }

    public CsvAudioMux() {
        super(new ContentDescriptor("audio.csv"));
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
            audioBufferToStream((AudioFormat) this.inputFormats[0], buffer, os);
        }
    }

    public Format[] getSupportedInputFormats() {
        Format[] formatArr = new Format[1];
        formatArr[0] = new AudioFormat(AudioFormat.LINEAR, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray);
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
        os.write(FormatArgUtils.toString(this.inputFormats[0]).getBytes());
        os.write("\n".getBytes());
    }

    private void outputTrailer(OutputStream os) throws IOException {
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
