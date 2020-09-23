package net.sf.fmj.media.codec.audio.gsm;

import javax.media.Format;
import javax.media.format.AudioFormat;
import net.sf.fmj.media.AbstractDePacketizer;

public class DePacketizer extends AbstractDePacketizer {
    protected Format[] outputFormats;

    public DePacketizer() {
        Format[] formatArr = new Format[1];
        formatArr[0] = new AudioFormat("gsm", 8000.0d, 8, 1, -1, 1, 264, -1.0d, Format.byteArray);
        this.outputFormats = formatArr;
        formatArr = new Format[1];
        formatArr[0] = new AudioFormat(AudioFormat.GSM_RTP, 8000.0d, 8, 1, -1, 1, 264, -1.0d, Format.byteArray);
        this.inputFormats = formatArr;
    }

    public void close() {
    }

    public String getName() {
        return "GSM DePacketizer";
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return this.outputFormats;
        }
        if (input instanceof AudioFormat) {
            AudioFormat inputCast = (AudioFormat) input;
            if (inputCast.getEncoding().equals(AudioFormat.GSM_RTP)) {
                AudioFormat result = new AudioFormat("gsm", inputCast.getSampleRate(), inputCast.getSampleSizeInBits(), inputCast.getChannels(), inputCast.getEndian(), inputCast.getSigned(), inputCast.getFrameSizeInBits(), inputCast.getFrameRate(), inputCast.getDataType());
                return new Format[]{result};
            }
            return new Format[]{null};
        }
        return new Format[]{null};
    }

    public void open() {
    }

    public Format setInputFormat(Format f) {
        return super.setInputFormat(f);
    }

    public Format setOutputFormat(Format f) {
        return super.setOutputFormat(f);
    }
}
