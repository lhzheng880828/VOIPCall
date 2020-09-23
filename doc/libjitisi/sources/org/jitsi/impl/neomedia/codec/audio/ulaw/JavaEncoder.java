package org.jitsi.impl.neomedia.codec.audio.ulaw;

import com.ibm.media.codec.audio.AudioCodec;
import com.lti.utils.UnsignedUtils;
import com.sun.media.format.WavAudioFormat;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;
import net.sf.fmj.media.codec.audio.ulaw.MuLawEncoderUtil;

public class JavaEncoder extends AudioCodec {
    private boolean downmix;
    private int inputBias;
    private int inputSampleSize;
    private Format lastFormat;
    private int lsbOffset;
    private int msbOffset;
    private int numberOfInputChannels;
    private int numberOfOutputChannels;
    private int signMask;

    public JavaEncoder() {
        this.downmix = false;
        this.lastFormat = null;
        this.numberOfOutputChannels = 1;
        this.supportedInputFormats = new AudioFormat[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, 1, -1, -1), new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, 2, -1, -1), new AudioFormat(AudioFormat.LINEAR, -1.0d, 8, 1, -1, -1), new AudioFormat(AudioFormat.LINEAR, -1.0d, 8, 2, -1, -1)};
        this.defaultOutputFormats = new AudioFormat[]{new AudioFormat(AudioFormat.ULAW, 8000.0d, 8, 1, -1, -1)};
        this.PLUGIN_NAME = "pcm to mu-law converter";
    }

    private int calculateOutputSize(int inputLength) {
        if (this.inputSampleSize == 16) {
            inputLength /= 2;
        }
        if (this.downmix) {
            return inputLength / 2;
        }
        return inputLength;
    }

    private void convert(byte[] input, int inputOffset, int inputLength, byte[] outData, int outputOffset) {
        int i = inputOffset + this.msbOffset;
        while (true) {
            int i2 = i;
            int i3 = outputOffset;
            if (i2 < inputLength + inputOffset) {
                int inputSample;
                int signBit;
                if (8 == this.inputSampleSize) {
                    i = i2 + 1;
                    inputSample = input[i2] << 8;
                    if (this.downmix) {
                        inputSample = ((this.signMask & inputSample) + ((input[i] << 8) & this.signMask)) >> 1;
                        i++;
                    }
                } else {
                    inputSample = (input[i2] << 8) + (input[this.lsbOffset + i2] & UnsignedUtils.MAX_UBYTE);
                    i = i2 + 2;
                    if (this.downmix) {
                        inputSample = ((this.signMask & inputSample) + (((input[i] << 8) + (input[this.lsbOffset + i] & UnsignedUtils.MAX_UBYTE)) & this.signMask)) >> 1;
                        i += 2;
                    }
                }
                int sample = (short) (this.inputBias + inputSample);
                if (sample >= 0) {
                    signBit = 128;
                } else {
                    sample = -sample;
                    signBit = 0;
                }
                sample = (sample + MuLawEncoderUtil.BIAS) >> 3;
                outputOffset = i3 + 1;
                int i4 = sample < 32 ? (signBit | WavAudioFormat.WAVE_FORMAT_VOXWARE_AC8) | (31 - (sample >> 0)) : sample < 64 ? (signBit | 96) | (31 - (sample >> 1)) : sample < 128 ? (signBit | 80) | (31 - (sample >> 2)) : sample < 256 ? (signBit | 64) | (31 - (sample >> 3)) : sample < 512 ? (signBit | 48) | (31 - (sample >> 4)) : sample < 1024 ? (signBit | 32) | (31 - (sample >> 5)) : sample < 2048 ? (signBit | 16) | (31 - (sample >> 6)) : sample < 4096 ? (signBit | 0) | (31 - (sample >> 7)) : (signBit | 0) | 0;
                outData[i3] = (byte) i4;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public Format[] getMatchingOutputFormats(Format in) {
        AudioFormat inFormat = (AudioFormat) in;
        int sampleRate = (int) inFormat.getSampleRate();
        if (inFormat.getChannels() == 2) {
            this.supportedOutputFormats = new AudioFormat[]{new AudioFormat(AudioFormat.ULAW, (double) sampleRate, 8, 2, -1, -1), new AudioFormat(AudioFormat.ULAW, (double) sampleRate, 8, 1, -1, -1)};
        } else {
            this.supportedOutputFormats = new AudioFormat[]{new AudioFormat(AudioFormat.ULAW, (double) sampleRate, 8, 1, -1, -1)};
        }
        return this.supportedOutputFormats;
    }

    private void initConverter(AudioFormat inFormat) {
        boolean z = true;
        this.lastFormat = inFormat;
        this.numberOfInputChannels = inFormat.getChannels();
        if (this.outputFormat != null) {
            this.numberOfOutputChannels = this.outputFormat.getChannels();
        }
        this.inputSampleSize = inFormat.getSampleSizeInBits();
        if (inFormat.getEndian() == 1 || 8 == this.inputSampleSize) {
            this.lsbOffset = 1;
            this.msbOffset = 0;
        } else {
            this.lsbOffset = -1;
            this.msbOffset = 1;
        }
        if (inFormat.getSigned() == 1) {
            this.inputBias = 0;
            this.signMask = -1;
        } else {
            this.inputBias = 32768;
            this.signMask = 65535;
        }
        if (!(this.numberOfInputChannels == 2 && this.numberOfOutputChannels == 1)) {
            z = false;
        }
        this.downmix = z;
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer) {
        if (!checkInputBuffer(inputBuffer)) {
            return 1;
        }
        if (isEOM(inputBuffer)) {
            propagateEOM(outputBuffer);
            return 0;
        }
        Format newFormat = inputBuffer.getFormat();
        if (this.lastFormat != newFormat) {
            initConverter((AudioFormat) newFormat);
        }
        int inpLength = inputBuffer.getLength();
        int outLength = calculateOutputSize(inputBuffer.getLength());
        convert((byte[]) inputBuffer.getData(), inputBuffer.getOffset(), inpLength, validateByteArraySize(outputBuffer, outLength), 0);
        updateOutput(outputBuffer, this.outputFormat, outLength, 0);
        return 0;
    }
}
