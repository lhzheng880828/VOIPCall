package org.jitsi.impl.neomedia.codec.audio.ilbc;

import com.sun.media.controls.SilenceSuppressionAdapter;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.service.neomedia.codec.Constants;

public class JavaEncoder extends AbstractCodec2 {
    /* access modifiers changed from: private */
    public int duration;
    private ilbc_encoder enc;
    private int inputLength;
    private int outputLength;
    private byte[] prevInput;
    private int prevInputLength;

    public JavaEncoder() {
        super("iLBC Encoder", AudioFormat.class, new Format[]{new AudioFormat("ilbc/rtp", 8000.0d, 16, 1, 0, 1)});
        this.duration = 0;
        this.inputFormats = new Format[]{new AudioFormat(AudioFormat.LINEAR, 8000.0d, 16, 1, 0, 1)};
        addControl(new SilenceSuppressionAdapter(this, false, false));
    }

    /* access modifiers changed from: protected */
    public void doClose() {
        this.enc = null;
        this.outputLength = 0;
        this.inputLength = 0;
        this.prevInput = null;
        this.prevInputLength = 0;
        this.duration = 0;
    }

    /* access modifiers changed from: protected */
    public void doOpen() {
        int mode = Constants.ILBC_MODE;
        this.enc = new ilbc_encoder(mode);
        switch (mode) {
            case 20:
                this.outputLength = 38;
                break;
            case 30:
                this.outputLength = 50;
                break;
            default:
                throw new IllegalStateException("mode");
        }
        this.duration = 1000000 * mode;
        this.inputLength = this.enc.ULP_inst.blockl * 2;
        this.prevInput = new byte[this.inputLength];
        this.prevInputLength = 0;
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
    public int doProcess(Buffer inputBuffer, Buffer outputBuffer) {
        int ret;
        int inputLength = inputBuffer.getLength();
        byte[] input = (byte[]) inputBuffer.getData();
        int inputOffset = inputBuffer.getOffset();
        if (this.prevInputLength != 0 || inputLength < this.inputLength) {
            int bytesToCopy = this.inputLength - this.prevInputLength;
            if (bytesToCopy > inputLength) {
                bytesToCopy = inputLength;
            }
            System.arraycopy(input, inputOffset, this.prevInput, this.prevInputLength, bytesToCopy);
            this.prevInputLength += bytesToCopy;
            inputBuffer.setLength(inputLength - bytesToCopy);
            inputBuffer.setOffset(inputOffset + bytesToCopy);
            inputLength = this.prevInputLength;
            input = this.prevInput;
            inputOffset = 0;
        } else {
            inputBuffer.setLength(inputLength - this.inputLength);
            inputBuffer.setOffset(this.inputLength + inputOffset);
        }
        if (inputLength >= this.inputLength) {
            this.prevInputLength = 0;
            this.enc.encode(AbstractCodec2.validateByteArraySize(outputBuffer, this.outputLength + 0, true), 0, input, inputOffset);
            updateOutput(outputBuffer, getOutputFormat(), this.outputLength, 0);
            outputBuffer.setDuration((long) this.duration);
            ret = 0;
        } else {
            ret = 4;
        }
        if (inputBuffer.getLength() > 0) {
            return ret | 2;
        }
        return ret;
    }
}
