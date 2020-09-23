package org.jitsi.impl.neomedia.codec.audio.alaw;

import com.sun.media.codec.audio.AudioCodec;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;
import net.sf.fmj.media.BasicPlugIn;

public class DePacketizer extends AudioCodec {
    public DePacketizer() {
        this.inputFormats = new Format[]{new AudioFormat("ALAW/rtp")};
    }

    public String getName() {
        return "ALAW DePacketizer";
    }

    public Format[] getSupportedOutputFormats(Format in) {
        if (in == null) {
            return new Format[]{new AudioFormat(AudioFormat.ALAW)};
        } else if (BasicPlugIn.matches(in, this.inputFormats) == null) {
            return new Format[1];
        } else {
            if (in instanceof AudioFormat) {
                AudioFormat af = (AudioFormat) in;
                return new Format[]{new AudioFormat(AudioFormat.ALAW, af.getSampleRate(), af.getSampleSizeInBits(), af.getChannels())};
            }
            return new Format[]{new AudioFormat(AudioFormat.ALAW)};
        }
    }

    public void open() {
    }

    public void close() {
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer) {
        if (!checkInputBuffer(inputBuffer)) {
            return 1;
        }
        if (isEOM(inputBuffer)) {
            propagateEOM(outputBuffer);
            return 0;
        }
        Object outData = outputBuffer.getData();
        outputBuffer.setData(inputBuffer.getData());
        inputBuffer.setData(outData);
        outputBuffer.setLength(inputBuffer.getLength());
        outputBuffer.setFormat(this.outputFormat);
        outputBuffer.setOffset(inputBuffer.getOffset());
        return 0;
    }
}
