package org.jitsi.impl.neomedia.codec.audio.ulaw;

import com.ibm.media.codec.audio.AudioCodec;
import com.lti.utils.UnsignedUtils;
import com.sun.media.controls.SilenceSuppressionAdapter;
import com.sun.media.format.WavAudioFormat;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;
import net.sf.fmj.media.codec.audio.ulaw.MuLawEncoderUtil;

public class JavaDecoder extends AudioCodec {
    private static final byte[] lutTableH = new byte[256];
    private static final byte[] lutTableL = new byte[256];

    public JavaDecoder() {
        this.supportedInputFormats = new AudioFormat[]{new AudioFormat(AudioFormat.ULAW)};
        this.defaultOutputFormats = new AudioFormat[]{new AudioFormat(AudioFormat.LINEAR)};
        this.PLUGIN_NAME = "Mu-Law Decoder";
    }

    public Object[] getControls() {
        if (this.controls == null) {
            this.controls = new Object[]{new SilenceSuppressionAdapter(this, false, false)};
        }
        return this.controls;
    }

    /* access modifiers changed from: protected */
    public Format[] getMatchingOutputFormats(Format in) {
        AudioFormat af = (AudioFormat) in;
        this.supportedOutputFormats = new AudioFormat[]{new AudioFormat(AudioFormat.LINEAR, af.getSampleRate(), 16, af.getChannels(), 0, 1)};
        return this.supportedOutputFormats;
    }

    private void initTables() {
        for (int i = 0; i < 256; i++) {
            int input = i ^ -1;
            int value = ((((input & 15) << 3) + MuLawEncoderUtil.BIAS) << ((input & WavAudioFormat.WAVE_FORMAT_VOXWARE_AC8) >> 4)) - 132;
            if ((input & 128) != 0) {
                value = -value;
            }
            lutTableL[i] = (byte) value;
            lutTableH[i] = (byte) (value >> 8);
        }
    }

    public void open() {
        initTables();
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer) {
        if (!checkInputBuffer(inputBuffer)) {
            return 1;
        }
        if (isEOM(inputBuffer)) {
            propagateEOM(outputBuffer);
            return 0;
        }
        byte[] inData = (byte[]) inputBuffer.getData();
        byte[] outData = validateByteArraySize(outputBuffer, inData.length * 2);
        int inpLength = inputBuffer.getLength();
        int outLength = inpLength * 2;
        int inOffset = inputBuffer.getOffset();
        int i = 0;
        int outOffset = outputBuffer.getOffset();
        int inOffset2 = inOffset;
        while (i < inpLength) {
            inOffset = inOffset2 + 1;
            int temp = inData[inOffset2] & UnsignedUtils.MAX_UBYTE;
            int i2 = outOffset + 1;
            outData[outOffset] = lutTableL[temp];
            outOffset = i2 + 1;
            outData[i2] = lutTableH[temp];
            i++;
            inOffset2 = inOffset;
        }
        updateOutput(outputBuffer, this.outputFormat, outLength, outputBuffer.getOffset());
        return 0;
    }
}
