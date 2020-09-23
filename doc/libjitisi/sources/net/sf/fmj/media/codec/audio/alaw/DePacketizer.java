package net.sf.fmj.media.codec.audio.alaw;

import java.util.logging.Logger;
import javax.media.Format;
import javax.media.format.AudioFormat;
import net.sf.fmj.media.AbstractDePacketizer;
import net.sf.fmj.utility.LoggerSingleton;

public class DePacketizer extends AbstractDePacketizer {
    private static final Logger logger = LoggerSingleton.logger;
    protected Format[] outputFormats;

    public DePacketizer() {
        Format[] formatArr = new Format[1];
        formatArr[0] = new AudioFormat(AudioFormat.ALAW, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray);
        this.outputFormats = formatArr;
        formatArr = new Format[1];
        formatArr[0] = new AudioFormat("ALAW/rtp", -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray);
        this.inputFormats = formatArr;
    }

    public void close() {
    }

    public String getName() {
        return "ALAW DePacketizer";
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return this.outputFormats;
        }
        if (input instanceof AudioFormat) {
            AudioFormat inputCast = (AudioFormat) input;
            if (inputCast.getEncoding().equals("ALAW/rtp")) {
                AudioFormat result = new AudioFormat(AudioFormat.ALAW, inputCast.getSampleRate(), inputCast.getSampleSizeInBits(), inputCast.getChannels(), inputCast.getEndian(), inputCast.getSigned(), inputCast.getFrameSizeInBits(), inputCast.getFrameRate(), inputCast.getDataType());
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

    public Format setInputFormat(Format arg0) {
        return super.setInputFormat(arg0);
    }

    public Format setOutputFormat(Format arg0) {
        return super.setOutputFormat(arg0);
    }
}
