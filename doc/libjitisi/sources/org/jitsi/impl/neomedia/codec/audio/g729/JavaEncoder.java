package org.jitsi.impl.neomedia.codec.audio.g729;

import com.lti.utils.UnsignedUtils;
import java.util.Arrays;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import org.jitsi.impl.neomedia.ArrayIOUtils;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;

public class JavaEncoder extends AbstractCodec2 {
    private static final short BIT_1 = (short) 129;
    private static final int INPUT_FRAME_SIZE_IN_BYTES = 160;
    private static final int L_FRAME = 80;
    private static final int OUTPUT_FRAME_SIZE_IN_BYTES = 10;
    private static final int SERIAL_SIZE = 82;
    private Coder coder;
    /* access modifiers changed from: private */
    public int duration;
    private int outputFrameCount;
    private byte[] prevInput;
    private int prevInputLength;
    private short[] serial;
    private short[] sp16;

    public JavaEncoder() {
        super("G.729 Encoder", AudioFormat.class, new AudioFormat[]{new AudioFormat(AudioFormat.G729_RTP, 8000.0d, -1, 1)});
        this.duration = 20000000;
        this.inputFormats = new AudioFormat[]{new AudioFormat(AudioFormat.LINEAR, 8000.0d, 16, 1, 0, 1)};
    }

    public Format getOutputFormat() {
        Format outputFormat = super.getOutputFormat();
        if (outputFormat == null || outputFormat.getClass() != AudioFormat.class) {
            return outputFormat;
        }
        AudioFormat outputAudioFormat = (AudioFormat) outputFormat;
        return setOutputFormat(new AudioFormat(outputAudioFormat.getEncoding(), outputAudioFormat.getSampleRate(), outputAudioFormat.getSampleSizeInBits(), outputAudioFormat.getChannels(), outputAudioFormat.getEndian(), outputAudioFormat.getSigned(), outputAudioFormat.getFrameSizeInBits(), outputAudioFormat.getFrameRate(), outputAudioFormat.getDataType()) {
            private static final long serialVersionUID = 0;

            public long computeDuration(long length) {
                return (long) JavaEncoder.this.duration;
            }
        });
    }

    /* access modifiers changed from: protected */
    public void discardOutputBuffer(Buffer outputBuffer) {
        super.discardOutputBuffer(outputBuffer);
        this.outputFrameCount = 0;
    }

    /* access modifiers changed from: protected */
    public void doClose() {
        this.prevInput = null;
        this.prevInputLength = 0;
        this.sp16 = null;
        this.serial = null;
        this.coder = null;
    }

    /* access modifiers changed from: protected */
    public void doOpen() throws ResourceUnavailableException {
        this.prevInput = new byte[INPUT_FRAME_SIZE_IN_BYTES];
        this.prevInputLength = 0;
        this.sp16 = new short[L_FRAME];
        this.serial = new short[SERIAL_SIZE];
        this.coder = new Coder();
        this.outputFrameCount = 0;
    }

    /* access modifiers changed from: protected */
    public int doProcess(Buffer inputBuffer, Buffer outputBuffer) {
        byte[] input = (byte[]) inputBuffer.getData();
        int inputLength = inputBuffer.getLength();
        int inputOffset = inputBuffer.getOffset();
        if (this.prevInputLength + inputLength < INPUT_FRAME_SIZE_IN_BYTES) {
            System.arraycopy(input, inputOffset, this.prevInput, this.prevInputLength, inputLength);
            this.prevInputLength += inputLength;
            return 4;
        }
        int readShorts = 0;
        if (this.prevInputLength > 0) {
            readShorts = 0 + readShorts(this.prevInput, 0, this.sp16, 0, this.prevInputLength / 2);
            this.prevInputLength = 0;
        }
        int readBytes = readShorts(input, inputOffset, this.sp16, readShorts, this.sp16.length - readShorts) * 2;
        inputLength -= readBytes;
        inputBuffer.setLength(inputLength);
        inputBuffer.setOffset(inputOffset + readBytes);
        this.coder.process(this.sp16, this.serial);
        packetize(this.serial, AbstractCodec2.validateByteArraySize(outputBuffer, outputBuffer.getOffset() + 20, true), outputBuffer.getOffset() + (this.outputFrameCount * 10));
        outputBuffer.setLength(outputBuffer.getLength() + 10);
        outputBuffer.setFormat(this.outputFormat);
        int processResult = 0;
        if (this.outputFrameCount == 1) {
            this.outputFrameCount = 0;
        } else {
            this.outputFrameCount = 1;
            processResult = 0 | 4;
        }
        if (inputLength > 0) {
            processResult |= 2;
        }
        if (processResult != 0) {
            return processResult;
        }
        updateOutput(outputBuffer, getOutputFormat(), outputBuffer.getLength(), outputBuffer.getOffset());
        outputBuffer.setDuration((long) this.duration);
        return processResult;
    }

    private void packetize(short[] serial, byte[] outputFrame, int outputFrameOffset) {
        Arrays.fill(outputFrame, outputFrameOffset, outputFrameOffset + 10, (byte) 0);
        for (int s = 0; s < L_FRAME; s++) {
            if (BIT_1 == serial[s + 2]) {
                int o = outputFrameOffset + (s / 8);
                outputFrame[o] = (byte) ((outputFrame[o] | (1 << (7 - (s % 8)))) & UnsignedUtils.MAX_UBYTE);
            }
        }
    }

    private static int readShorts(byte[] input, int inputOffset, short[] output, int outputOffset, int outputLength) {
        int o = outputOffset;
        int i = inputOffset;
        while (o < outputLength) {
            output[o] = ArrayIOUtils.readShort(input, i);
            o++;
            i += 2;
        }
        return outputLength;
    }
}
