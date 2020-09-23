package net.sf.fmj.media.codec.audio.alaw;

import java.util.logging.Logger;
import javax.media.Format;
import javax.media.format.AudioFormat;
import net.sf.fmj.media.AbstractPacketizer;
import net.sf.fmj.utility.LoggerSingleton;

public class Packetizer extends AbstractPacketizer {
    private static final int PACKET_SIZE = 480;
    private static final Logger logger = LoggerSingleton.logger;
    protected Format[] outputFormats;

    public Packetizer() {
        Format[] formatArr = new Format[1];
        formatArr[0] = new AudioFormat("ALAW/rtp", -1.0d, 8, 1, -1, -1, 8, -1.0d, Format.byteArray);
        this.outputFormats = formatArr;
        formatArr = new Format[1];
        formatArr[0] = new AudioFormat(AudioFormat.ALAW, -1.0d, 8, 1, -1, -1, 8, -1.0d, Format.byteArray);
        this.inputFormats = formatArr;
    }

    public void close() {
    }

    public String getName() {
        return "ALAW Packetizer";
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return this.outputFormats;
        }
        if (input instanceof AudioFormat) {
            AudioFormat inputCast = (AudioFormat) input;
            if (inputCast.getEncoding().equals(AudioFormat.ALAW) && ((inputCast.getSampleSizeInBits() == 8 || inputCast.getSampleSizeInBits() == -1) && ((inputCast.getChannels() == 1 || inputCast.getChannels() == -1) && (inputCast.getFrameSizeInBits() == 8 || inputCast.getFrameSizeInBits() == -1)))) {
                AudioFormat result = new AudioFormat("ALAW/rtp", inputCast.getSampleRate(), 8, 1, inputCast.getEndian(), inputCast.getSigned(), 8, inputCast.getFrameRate(), inputCast.getDataType());
                return new Format[]{result};
            }
            logger.warning(getClass().getSimpleName() + ".getSupportedOutputFormats: input format does not match, returning format array of {null} for " + input);
            return new Format[]{null};
        }
        logger.warning(getClass().getSimpleName() + ".getSupportedOutputFormats: input format does not match, returning format array of {null} for " + input);
        return new Format[]{null};
    }

    public void open() {
        setPacketSize(480);
    }

    public Format setInputFormat(Format arg0) {
        return super.setInputFormat(arg0);
    }

    public Format setOutputFormat(Format arg0) {
        return super.setOutputFormat(arg0);
    }
}
