package net.sf.fmj.media.multiplexer.audio;

import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;
import net.sf.fmj.media.multiplexer.BasicMux;

public class MPEGMux extends BasicMux {
    public MPEGMux() {
        this.supportedInputs = new Format[2];
        this.supportedInputs[0] = new AudioFormat(AudioFormat.MPEGLAYER3);
        this.supportedInputs[1] = new AudioFormat(AudioFormat.MPEG);
        this.supportedOutputs = new ContentDescriptor[1];
        this.supportedOutputs[0] = new FileTypeDescriptor(FileTypeDescriptor.MPEG_AUDIO);
    }

    public String getName() {
        return "MPEG Audio Multiplexer";
    }

    public Format setInputFormat(Format input, int trackID) {
        if (!(input instanceof AudioFormat)) {
            return null;
        }
        AudioFormat format = (AudioFormat) input;
        double sampleRate = format.getSampleRate();
        String reason = null;
        if (!(format.getEncoding().equalsIgnoreCase(AudioFormat.MPEGLAYER3) || format.getEncoding().equalsIgnoreCase(AudioFormat.MPEG))) {
            reason = "Encoding has to be MPEG audio";
        }
        if (reason != null) {
            return null;
        }
        this.inputs[0] = format;
        return format;
    }
}
