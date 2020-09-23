package org.jitsi.impl.neomedia.codec.audio.speex;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import net.java.sip.communicator.impl.neomedia.codec.audio.speex.Speex;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;

public class JNIDecoder extends AbstractCodec2 {
    private static final Format[] SUPPORTED_INPUT_FORMATS;
    private static final Format[] SUPPORTED_OUTPUT_FORMATS = new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, 1, 0, 1, -1, -1.0d, Format.byteArray)};
    private long bits;
    private long duration;
    private int frameSize;
    private int sampleRate;
    private long state;

    static {
        Speex.assertSpeexIsFunctional();
        double[] SUPPORTED_INPUT_SAMPLE_RATES = JNIEncoder.SUPPORTED_INPUT_SAMPLE_RATES;
        int supportedInputCount = SUPPORTED_INPUT_SAMPLE_RATES.length;
        SUPPORTED_INPUT_FORMATS = new Format[supportedInputCount];
        for (int i = 0; i < supportedInputCount; i++) {
            SUPPORTED_INPUT_FORMATS[i] = new AudioFormat("speex/rtp", SUPPORTED_INPUT_SAMPLE_RATES[i], -1, 1, 0, 1, -1, -1.0d, Format.byteArray);
        }
    }

    public JNIDecoder() {
        super("Speex JNI Decoder", AudioFormat.class, SUPPORTED_OUTPUT_FORMATS);
        this.bits = 0;
        this.duration = 0;
        this.frameSize = 0;
        this.sampleRate = 0;
        this.state = 0;
        this.inputFormats = SUPPORTED_INPUT_FORMATS;
    }

    /* access modifiers changed from: protected */
    public void doClose() {
        if (this.state != 0) {
            Speex.speex_decoder_destroy(this.state);
            this.state = 0;
            this.sampleRate = 0;
            this.frameSize = 0;
            this.duration = 0;
        }
        Speex.speex_bits_destroy(this.bits);
        this.bits = 0;
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
            Speex.speex_decoder_destroy(this.state);
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
            this.state = Speex.speex_decoder_init(mode);
            if (this.state == 0) {
                return 1;
            }
            if (Speex.speex_decoder_ctl(this.state, 0, 1) != 0) {
                return 1;
            }
            if (Speex.speex_decoder_ctl(this.state, 24, inputSampleRate) != 0) {
                return 1;
            }
            int frameSize = Speex.speex_decoder_ctl(this.state, 3);
            if (frameSize < 0) {
                return 1;
            }
            this.sampleRate = inputSampleRate;
            this.frameSize = frameSize * 2;
            this.duration = (long) (((frameSize * 1000) * 1000000) / this.sampleRate);
        }
        int inputLength = inputBuffer.getLength();
        if (inputLength > 0) {
            byte[] input = (byte[]) inputBuffer.getData();
            int inputOffset = inputBuffer.getOffset();
            Speex.speex_bits_read_from(this.bits, input, inputOffset, inputLength);
            inputLength = 0;
            inputBuffer.setLength(0);
            inputBuffer.setOffset(inputOffset + 0);
        }
        int outputLength = this.frameSize;
        boolean inputBufferNotConsumed;
        if (outputLength > 0) {
            if (Speex.speex_decode_int(this.state, this.bits, AbstractCodec2.validateByteArraySize(outputBuffer, outputLength, false), 0) == 0) {
                outputBuffer.setDuration(this.duration);
                outputBuffer.setFormat(getOutputFormat());
                outputBuffer.setLength(outputLength);
                outputBuffer.setOffset(0);
                if (Speex.speex_bits_remaining(this.bits) > 0) {
                    inputBufferNotConsumed = true;
                } else {
                    inputBufferNotConsumed = false;
                }
            } else {
                outputBuffer.setLength(0);
                discardOutputBuffer(outputBuffer);
                inputBufferNotConsumed = false;
            }
        } else {
            outputBuffer.setLength(0);
            discardOutputBuffer(outputBuffer);
            inputBufferNotConsumed = false;
        }
        if (inputLength >= 1 || inputBufferNotConsumed) {
            return 2;
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public Format[] getMatchingOutputFormats(Format inputFormat) {
        Format[] formatArr = new Format[1];
        int i = 1;
        formatArr[0] = new AudioFormat(AudioFormat.LINEAR, ((AudioFormat) inputFormat).getSampleRate(), 16, 1, 0, i, -1, -1.0d, Format.byteArray);
        return formatArr;
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
                setOutputFormat(new AudioFormat(AudioFormat.LINEAR, inputSampleRate, 16, inputChannels, 0, 1, -1, -1.0d, Format.byteArray));
            }
        }
        return inputFormat;
    }
}
