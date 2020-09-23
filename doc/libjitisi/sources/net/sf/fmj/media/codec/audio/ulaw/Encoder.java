package net.sf.fmj.media.codec.audio.ulaw;

import java.util.logging.Logger;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;
import net.sf.fmj.media.AbstractCodec;
import net.sf.fmj.utility.LoggerSingleton;

public class Encoder extends AbstractCodec {
    private static final boolean TRACE = false;
    private static final Logger logger = LoggerSingleton.logger;
    protected Format[] outputFormats;

    public Encoder() {
        Format[] formatArr = new Format[1];
        formatArr[0] = new AudioFormat(AudioFormat.ULAW, -1.0d, 8, 1, -1, 1, 8, -1.0d, Format.byteArray);
        this.outputFormats = formatArr;
        formatArr = new Format[1];
        formatArr[0] = new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, 1, -1, 1, 16, -1.0d, Format.byteArray);
        this.inputFormats = formatArr;
    }

    public void close() {
    }

    public String getName() {
        return "ULAW Encoder";
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return this.outputFormats;
        }
        if (input instanceof AudioFormat) {
            AudioFormat inputCast = (AudioFormat) input;
            if (inputCast.getEncoding().equals(AudioFormat.LINEAR) && ((inputCast.getSampleSizeInBits() == 16 || inputCast.getSampleSizeInBits() == -1) && ((inputCast.getChannels() == 1 || inputCast.getChannels() == -1) && ((inputCast.getSigned() == 1 || inputCast.getSigned() == -1) && ((inputCast.getFrameSizeInBits() == 16 || inputCast.getFrameSizeInBits() == -1) && (inputCast.getDataType() == null || inputCast.getDataType() == Format.byteArray)))))) {
                AudioFormat result = new AudioFormat(AudioFormat.ULAW, inputCast.getSampleRate(), 8, 1, -1, 1, 8, inputCast.getFrameRate(), Format.byteArray);
                return new Format[]{result};
            }
            logger.warning(getClass().getSimpleName() + ".getSupportedOutputFormats: input format does not match, returning format array of {null} for " + input);
            return new Format[]{null};
        }
        logger.warning(getClass().getSimpleName() + ".getSupportedOutputFormats: input format does not match, returning format array of {null} for " + input);
        return new Format[]{null};
    }

    public void open() {
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer) {
        boolean bigEndian = true;
        if (!checkInputBuffer(inputBuffer)) {
            return 1;
        }
        if (isEOM(inputBuffer)) {
            propagateEOM(outputBuffer);
            return 0;
        }
        AudioFormat inputAudioFormat = (AudioFormat) inputBuffer.getFormat();
        byte[] outputBufferData = (byte[]) outputBuffer.getData();
        int requiredOutputBufferLength = inputBuffer.getLength() / 2;
        if (outputBufferData == null || outputBufferData.length < requiredOutputBufferLength) {
            outputBufferData = new byte[requiredOutputBufferLength];
            outputBuffer.setData(outputBufferData);
        }
        if (!inputAudioFormat.equals(this.inputFormat)) {
            throw new RuntimeException("Incorrect input format");
        } else if (inputAudioFormat.getEndian() == -1) {
            throw new RuntimeException("Unspecified endian-ness");
        } else {
            if (inputAudioFormat.getEndian() != 1) {
                bigEndian = false;
            }
            MuLawEncoderUtil.muLawEncode(bigEndian, (byte[]) inputBuffer.getData(), inputBuffer.getOffset(), inputBuffer.getLength(), outputBufferData);
            outputBuffer.setLength(requiredOutputBufferLength);
            outputBuffer.setOffset(0);
            outputBuffer.setFormat(this.outputFormat);
            return 0;
        }
    }

    public Format setInputFormat(Format arg0) {
        return super.setInputFormat(arg0);
    }

    public Format setOutputFormat(Format arg0) {
        return super.setOutputFormat(arg0);
    }
}
