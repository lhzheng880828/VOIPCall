package org.jitsi.impl.neomedia.codec.audio.speex;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import net.java.sip.communicator.impl.neomedia.codec.audio.speex.Speex;
import net.sf.fmj.ejmf.toolkit.util.TimeSource;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;

public class JNIEncoder extends AbstractCodec2 {
    private static final Format[] SUPPORTED_INPUT_FORMATS;
    static final double[] SUPPORTED_INPUT_SAMPLE_RATES = new double[]{8000.0d, 16000.0d, 32000.0d};
    private static final Format[] SUPPORTED_OUTPUT_FORMATS = new Format[]{new AudioFormat("speex/rtp")};
    private long bits;
    /* access modifiers changed from: private */
    public long duration;
    private int frameSize;
    private byte[] previousInput;
    private int previousInputLength;
    private int sampleRate;
    private long state;

    static {
        Speex.assertSpeexIsFunctional();
        int supportedInputCount = SUPPORTED_INPUT_SAMPLE_RATES.length;
        SUPPORTED_INPUT_FORMATS = new Format[supportedInputCount];
        for (int i = 0; i < supportedInputCount; i++) {
            SUPPORTED_INPUT_FORMATS[i] = new AudioFormat(AudioFormat.LINEAR, SUPPORTED_INPUT_SAMPLE_RATES[i], 16, 1, 0, 1, -1, -1.0d, Format.byteArray);
        }
    }

    public JNIEncoder() {
        super("Speex JNI Encoder", AudioFormat.class, SUPPORTED_OUTPUT_FORMATS);
        this.bits = 0;
        this.duration = 0;
        this.frameSize = 0;
        this.previousInputLength = 0;
        this.sampleRate = 0;
        this.state = 0;
        this.inputFormats = SUPPORTED_INPUT_FORMATS;
    }

    /* access modifiers changed from: protected */
    public void doClose() {
        if (this.state != 0) {
            Speex.speex_encoder_destroy(this.state);
            this.state = 0;
            this.sampleRate = 0;
            this.frameSize = 0;
            this.duration = 0;
        }
        Speex.speex_bits_destroy(this.bits);
        this.bits = 0;
        this.previousInput = null;
    }

    /* access modifiers changed from: protected */
    public void doOpen() throws ResourceUnavailableException {
        this.bits = Speex.speex_bits_init();
        if (this.bits == 0) {
            throw new ResourceUnavailableException("speex_bits_init");
        }
    }

    /* access modifiers changed from: protected */
    public int doProcess(Buffer inputBuffer, Buffer outputBuffer) {
        Format inputFormat = inputBuffer.getFormat();
        if (inputFormat != null && inputFormat != this.inputFormat && !inputFormat.equals(this.inputFormat) && setInputFormat(inputFormat) == null) {
            return 1;
        }
        int inputSampleRate = (int) ((AudioFormat) this.inputFormat).getSampleRate();
        if (!(this.state == 0 || this.sampleRate == inputSampleRate)) {
            Speex.speex_encoder_destroy(this.state);
            this.state = 0;
            this.sampleRate = 0;
            this.frameSize = 0;
        }
        if (this.state == 0) {
            int i = inputSampleRate == 16000 ? 1 : inputSampleRate == 32000 ? 2 : 0;
            long mode = Speex.speex_lib_get_mode(i);
            if (mode == 0) {
                return 1;
            }
            this.state = Speex.speex_encoder_init(mode);
            if (this.state == 0) {
                return 1;
            }
            if (Speex.speex_encoder_ctl(this.state, 4, 4) != 0) {
                return 1;
            }
            if (Speex.speex_encoder_ctl(this.state, 24, inputSampleRate) != 0) {
                return 1;
            }
            int frameSize = Speex.speex_encoder_ctl(this.state, 3);
            if (frameSize < 0) {
                return 1;
            }
            this.sampleRate = inputSampleRate;
            this.frameSize = frameSize * 2;
            this.duration = ((((long) frameSize) * 1000) * TimeSource.MICROS_PER_SEC) / ((long) this.sampleRate);
        }
        byte[] input = (byte[]) inputBuffer.getData();
        int inputLength = inputBuffer.getLength();
        int inputOffset = inputBuffer.getOffset();
        if (this.previousInput != null && this.previousInputLength > 0) {
            if (this.previousInputLength < this.frameSize) {
                if (this.previousInput.length < this.frameSize) {
                    Object newPreviousInput = new byte[this.frameSize];
                    System.arraycopy(this.previousInput, 0, newPreviousInput, 0, this.previousInput.length);
                    this.previousInput = newPreviousInput;
                }
                int bytesToCopyFromInputToPreviousInput = Math.min(this.frameSize - this.previousInputLength, inputLength);
                if (bytesToCopyFromInputToPreviousInput > 0) {
                    System.arraycopy(input, inputOffset, this.previousInput, this.previousInputLength, bytesToCopyFromInputToPreviousInput);
                    this.previousInputLength += bytesToCopyFromInputToPreviousInput;
                    inputLength -= bytesToCopyFromInputToPreviousInput;
                    inputBuffer.setLength(inputLength);
                    inputBuffer.setOffset(inputOffset + bytesToCopyFromInputToPreviousInput);
                }
            }
            if (this.previousInputLength == this.frameSize) {
                input = this.previousInput;
                inputOffset = 0;
                this.previousInputLength = 0;
            } else if (this.previousInputLength > this.frameSize) {
                input = new byte[this.frameSize];
                System.arraycopy(this.previousInput, 0, input, 0, input.length);
                inputOffset = 0;
                this.previousInputLength -= input.length;
                System.arraycopy(this.previousInput, input.length, this.previousInput, 0, this.previousInputLength);
            } else {
                outputBuffer.setLength(0);
                discardOutputBuffer(outputBuffer);
                if (inputLength < 1) {
                    return 0;
                }
                return 2;
            }
        } else if (inputLength < 1) {
            outputBuffer.setLength(0);
            discardOutputBuffer(outputBuffer);
            return 0;
        } else if (inputLength < this.frameSize) {
            if (this.previousInput == null || this.previousInput.length < inputLength) {
                this.previousInput = new byte[this.frameSize];
            }
            System.arraycopy(input, inputOffset, this.previousInput, 0, inputLength);
            this.previousInputLength = inputLength;
            outputBuffer.setLength(0);
            discardOutputBuffer(outputBuffer);
            return 0;
        } else {
            inputLength -= this.frameSize;
            inputBuffer.setLength(inputLength);
            inputBuffer.setOffset(this.frameSize + inputOffset);
        }
        Speex.speex_bits_reset(this.bits);
        Speex.speex_encode_int(this.state, input, inputOffset, this.bits);
        int outputLength = Speex.speex_bits_nbytes(this.bits);
        if (outputLength > 0) {
            byte[] output = AbstractCodec2.validateByteArraySize(outputBuffer, outputLength, false);
            outputLength = Speex.speex_bits_write(this.bits, output, 0, output.length);
            if (outputLength > 0) {
                outputBuffer.setDuration(this.duration);
                outputBuffer.setFormat(getOutputFormat());
                outputBuffer.setLength(outputLength);
                outputBuffer.setOffset(0);
            } else {
                outputBuffer.setLength(0);
                discardOutputBuffer(outputBuffer);
            }
        } else {
            outputBuffer.setLength(0);
            discardOutputBuffer(outputBuffer);
        }
        if (inputLength < 1) {
            return 0;
        }
        return 2;
    }

    /* access modifiers changed from: protected */
    public Format[] getMatchingOutputFormats(Format inputFormat) {
        Format[] formatArr = new Format[1];
        formatArr[0] = new AudioFormat("speex/rtp", ((AudioFormat) inputFormat).getSampleRate(), -1, 1, 0, 1, -1, -1.0d, Format.byteArray);
        return formatArr;
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
                return JNIEncoder.this.duration;
            }
        });
    }

    public Format setInputFormat(Format format) {
        Format inputFormat = super.setInputFormat(format);
        if (inputFormat != null) {
            double outputSampleRate;
            int outputChannels;
            if (this.outputFormat == null) {
                outputSampleRate = -1.0d;
                outputChannels = -1;
            } else {
                AudioFormat outputAudioFormat = (AudioFormat) this.outputFormat;
                outputSampleRate = outputAudioFormat.getSampleRate();
                outputChannels = outputAudioFormat.getChannels();
            }
            AudioFormat inputAudioFormat = (AudioFormat) inputFormat;
            double inputSampleRate = inputAudioFormat.getSampleRate();
            int inputChannels = inputAudioFormat.getChannels();
            if (!(outputSampleRate == inputSampleRate && outputChannels == inputChannels)) {
                setOutputFormat(new AudioFormat("speex/rtp", inputSampleRate, -1, inputChannels, 0, 1, -1, -1.0d, Format.byteArray));
            }
        }
        return inputFormat;
    }
}
