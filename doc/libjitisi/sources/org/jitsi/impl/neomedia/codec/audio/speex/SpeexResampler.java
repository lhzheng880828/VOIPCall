package org.jitsi.impl.neomedia.codec.audio.speex;

import com.lti.utils.UnsignedUtils;
import java.util.ArrayList;
import java.util.List;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import net.java.sip.communicator.impl.neomedia.codec.audio.speex.Speex;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;

public class SpeexResampler extends AbstractCodec2 {
    private static final Format[] SUPPORTED_FORMATS;
    private static final double[] SUPPORTED_SAMPLE_RATES = new double[]{8000.0d, 11025.0d, 12000.0d, 16000.0d, 22050.0d, 24000.0d, 32000.0d, 44100.0d, 48000.0d, -1.0d};
    private int channels;
    private int inputSampleRate;
    private int outputSampleRate;
    private long resampler;

    static {
        Speex.assertSpeexIsFunctional();
        int supportedCount = SUPPORTED_SAMPLE_RATES.length;
        SUPPORTED_FORMATS = new Format[(supportedCount * 4)];
        for (int i = 0; i < supportedCount; i++) {
            int j = i * 4;
            SUPPORTED_FORMATS[j] = new AudioFormat(AudioFormat.LINEAR, SUPPORTED_SAMPLE_RATES[i], 16, 1, 0, 1, -1, -1.0d, Format.byteArray);
            SUPPORTED_FORMATS[j + 1] = new AudioFormat(AudioFormat.LINEAR, SUPPORTED_SAMPLE_RATES[i], 16, 1, 0, 1, -1, -1.0d, Format.shortArray);
            SUPPORTED_FORMATS[j + 2] = new AudioFormat(AudioFormat.LINEAR, SUPPORTED_SAMPLE_RATES[i], 16, 2, 0, 1, -1, -1.0d, Format.byteArray);
            SUPPORTED_FORMATS[j + 3] = new AudioFormat(AudioFormat.LINEAR, SUPPORTED_SAMPLE_RATES[i], 16, 2, 0, 1, -1, -1.0d, Format.shortArray);
        }
    }

    public SpeexResampler() {
        super("Speex Resampler", AudioFormat.class, SUPPORTED_FORMATS);
        this.inputFormats = SUPPORTED_FORMATS;
    }

    /* access modifiers changed from: protected */
    public void doClose() {
        if (this.resampler != 0) {
            Speex.speex_resampler_destroy(this.resampler);
            this.resampler = 0;
        }
    }

    /* access modifiers changed from: protected */
    public void doOpen() throws ResourceUnavailableException {
    }

    /* access modifiers changed from: protected */
    public int doProcess(Buffer inBuffer, Buffer outBuffer) {
        Format inFormat = inBuffer.getFormat();
        if (!(inFormat == null || inFormat == this.inputFormat)) {
            if (!inFormat.equals(this.inputFormat) && setInputFormat(inFormat) == null) {
                return 1;
            }
        }
        AudioFormat inAudioFormat = (AudioFormat) this.inputFormat;
        int inSampleRate = (int) inAudioFormat.getSampleRate();
        Format outAudioFormat = (AudioFormat) getOutputFormat();
        int outSampleRate = (int) outAudioFormat.getSampleRate();
        if (inSampleRate == outSampleRate) {
            Class<?> inDataType = inAudioFormat.getDataType();
            Class<?> outDataType = outAudioFormat.getDataType();
            Object input;
            int length;
            Object output;
            int outLength;
            int o;
            int i;
            if (Format.byteArray.equals(inDataType)) {
                input = (byte[]) inBuffer.getData();
                if (Format.byteArray.equals(outDataType)) {
                    if (input == null) {
                        length = 0;
                    } else {
                        length = input.length;
                    }
                    output = AbstractCodec2.validateByteArraySize(outBuffer, length, false);
                    if (!(input == null || output == null)) {
                        System.arraycopy(input, 0, output, 0, length);
                    }
                    outBuffer.setFormat(inBuffer.getFormat());
                    outBuffer.setLength(inBuffer.getLength());
                    outBuffer.setOffset(inBuffer.getOffset());
                } else {
                    outLength = inBuffer.getLength() / 2;
                    short[] output2 = validateShortArraySize(outBuffer, outLength);
                    int i2 = inBuffer.getOffset();
                    for (o = 0; o < outLength; o++) {
                        i = i2 + 1;
                        i2 = i + 1;
                        output2[o] = (short) ((input[i2] & UnsignedUtils.MAX_UBYTE) | ((input[i] & UnsignedUtils.MAX_UBYTE) << 8));
                    }
                    outBuffer.setFormat(outAudioFormat);
                    outBuffer.setLength(outLength);
                    outBuffer.setOffset(0);
                }
            } else {
                input = (short[]) inBuffer.getData();
                if (Format.byteArray.equals(outDataType)) {
                    outLength = inBuffer.getLength() * 2;
                    byte[] output3 = AbstractCodec2.validateByteArraySize(outBuffer, outLength, false);
                    i = inBuffer.getOffset();
                    int i3 = 0;
                    while (i3 < outLength) {
                        short s = input[i];
                        o = i3 + 1;
                        output3[i3] = (byte) (s & UnsignedUtils.MAX_UBYTE);
                        i3 = o + 1;
                        output3[o] = (byte) ((65280 & s) >>> 8);
                        i++;
                    }
                    outBuffer.setFormat(outAudioFormat);
                    outBuffer.setLength(outLength);
                    outBuffer.setOffset(0);
                } else {
                    if (input == null) {
                        length = 0;
                    } else {
                        length = input.length;
                    }
                    output = validateShortArraySize(outBuffer, length);
                    if (!(input == null || output == null)) {
                        System.arraycopy(input, 0, output, 0, length);
                    }
                    outBuffer.setFormat(inBuffer.getFormat());
                    outBuffer.setLength(inBuffer.getLength());
                    outBuffer.setOffset(inBuffer.getOffset());
                }
            }
        } else {
            int channels = inAudioFormat.getChannels();
            if (outAudioFormat.getChannels() != channels) {
                return 1;
            }
            boolean channelsHaveChanged = this.channels != channels;
            if (!(!channelsHaveChanged && this.inputSampleRate == inSampleRate && this.outputSampleRate == outSampleRate)) {
                if (channelsHaveChanged && this.resampler != 0) {
                    Speex.speex_resampler_destroy(this.resampler);
                    this.resampler = 0;
                }
                if (this.resampler == 0) {
                    this.resampler = Speex.speex_resampler_init(channels, inSampleRate, outSampleRate, 3, 0);
                } else {
                    Speex.speex_resampler_set_rate(this.resampler, inSampleRate, outSampleRate);
                }
                if (this.resampler != 0) {
                    this.inputSampleRate = inSampleRate;
                    this.outputSampleRate = outSampleRate;
                    this.channels = channels;
                }
            }
            if (this.resampler == 0) {
                return 1;
            }
            byte[] in = (byte[]) inBuffer.getData();
            int frameSize = channels * (inAudioFormat.getSampleSizeInBits() / 8);
            int inSampleCount = inBuffer.getLength() / frameSize;
            int outSampleCount = (inSampleCount * outSampleRate) / inSampleRate;
            byte[] out = AbstractCodec2.validateByteArraySize(outBuffer, outSampleCount * frameSize, false);
            if (inSampleCount == 0) {
                outSampleCount = 0;
            } else {
                outSampleCount = Speex.speex_resampler_process_interleaved_int(this.resampler, in, inBuffer.getOffset(), inSampleCount, out, 0, outSampleCount);
            }
            outBuffer.setFormat(outAudioFormat);
            outBuffer.setLength(outSampleCount * frameSize);
            outBuffer.setOffset(0);
        }
        outBuffer.setDuration(inBuffer.getDuration());
        outBuffer.setEOM(inBuffer.isEOM());
        outBuffer.setFlags(inBuffer.getFlags());
        outBuffer.setHeader(inBuffer.getHeader());
        outBuffer.setSequenceNumber(inBuffer.getSequenceNumber());
        outBuffer.setTimeStamp(inBuffer.getTimeStamp());
        return 0;
    }

    /* access modifiers changed from: protected */
    public Format[] getMatchingOutputFormats(Format inputFormat) {
        Class<?> inDataType = inputFormat.getDataType();
        List<Format> matchingOutputFormats = new ArrayList();
        if (inputFormat instanceof AudioFormat) {
            AudioFormat inAudioFormat = (AudioFormat) inputFormat;
            int inChannels = inAudioFormat.getChannels();
            double inSampleRate = inAudioFormat.getSampleRate();
            for (Format supportedFormat : SUPPORTED_FORMATS) {
                AudioFormat supportedAudioFormat = (AudioFormat) supportedFormat;
                if (supportedAudioFormat.getChannels() == inChannels && ((Format.byteArray.equals(supportedFormat.getDataType()) && Format.byteArray.equals(inDataType)) || supportedAudioFormat.getSampleRate() == inSampleRate)) {
                    matchingOutputFormats.add(supportedFormat);
                }
            }
        }
        return (Format[]) matchingOutputFormats.toArray(new Format[matchingOutputFormats.size()]);
    }

    public Format setInputFormat(Format format) {
        AudioFormat inFormat = (AudioFormat) super.setInputFormat(format);
        if (inFormat != null) {
            double outSampleRate;
            Class<?> outDataType;
            if (this.outputFormat == null) {
                outSampleRate = inFormat.getSampleRate();
                outDataType = inFormat.getDataType();
            } else {
                AudioFormat outAudioFormat = this.outputFormat;
                outSampleRate = outAudioFormat.getSampleRate();
                outDataType = outAudioFormat.getDataType();
                if (outSampleRate != inFormat.getSampleRate()) {
                    outDataType = inFormat.getDataType();
                }
            }
            setOutputFormat(new AudioFormat(inFormat.getEncoding(), outSampleRate, inFormat.getSampleSizeInBits(), inFormat.getChannels(), inFormat.getEndian(), inFormat.getSigned(), -1, -1.0d, outDataType));
        }
        return inFormat;
    }
}
