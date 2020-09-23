package org.jitsi.impl.neomedia.codec.audio.gsm;

import javax.media.Format;
import javax.media.format.AudioFormat;
import net.sf.fmj.media.AbstractPacketizer;

public class Packetizer extends AbstractPacketizer {
    private static final int PACKET_SIZE = 33;
    protected Format[] outputFormats;

    public String getName() {
        return "GSM Packetizer";
    }

    public Packetizer() {
        Format[] formatArr = new Format[1];
        formatArr[0] = new AudioFormat(AudioFormat.GSM_RTP, 8000.0d, 8, 1, -1, 1, 264, -1.0d, Format.byteArray);
        this.outputFormats = formatArr;
        formatArr = new Format[1];
        formatArr[0] = new AudioFormat("gsm", 8000.0d, 8, 1, -1, 1, 264, -1.0d, Format.byteArray);
        this.inputFormats = formatArr;
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return this.outputFormats;
        }
        if (input instanceof AudioFormat) {
            AudioFormat inputCast = (AudioFormat) input;
            if (inputCast.getEncoding().equals("gsm") && ((inputCast.getSampleSizeInBits() == 8 || inputCast.getSampleSizeInBits() == -1) && ((inputCast.getChannels() == 1 || inputCast.getChannels() == -1) && (inputCast.getFrameSizeInBits() == 264 || inputCast.getFrameSizeInBits() == -1)))) {
                AudioFormat result = new AudioFormat(AudioFormat.GSM_RTP, inputCast.getSampleRate(), 8, 1, inputCast.getEndian(), inputCast.getSigned(), 264, inputCast.getFrameRate(), inputCast.getDataType());
                return new Format[]{result};
            }
            return new Format[]{null};
        }
        return new Format[]{null};
    }

    public void open() {
        setPacketSize(PACKET_SIZE);
    }

    public void close() {
    }

    public Format setInputFormat(Format f) {
        return super.setInputFormat(f);
    }

    public Format setOutputFormat(Format f) {
        return super.setOutputFormat(f);
    }
}
