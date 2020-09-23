package org.jitsi.impl.neomedia.codec.audio.alaw;

import com.ibm.media.codec.audio.AudioCodec;
import com.lti.utils.UnsignedUtils;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;

public class JavaEncoder extends AudioCodec {
    public static final int MAX = 32767;
    public static final int MAX_USHORT = 65535;
    private static byte[] pcmToALawMap = new byte[Buffer.FLAG_SKIP_FEC];
    private boolean bigEndian;
    private int inputSampleSize;
    private Format lastFormat;

    public JavaEncoder() {
        this.lastFormat = null;
        this.bigEndian = false;
        this.supportedInputFormats = new AudioFormat[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, 1, -1, -1), new AudioFormat(AudioFormat.LINEAR, -1.0d, 8, 1, -1, -1)};
        this.defaultOutputFormats = new AudioFormat[]{new AudioFormat(AudioFormat.ALAW, 8000.0d, 8, 1, -1, -1)};
        this.PLUGIN_NAME = "pcm to alaw converter";
    }

    /* access modifiers changed from: protected */
    public Format[] getMatchingOutputFormats(Format in) {
        int sampleRate = (int) ((AudioFormat) in).getSampleRate();
        this.supportedOutputFormats = new AudioFormat[]{new AudioFormat(AudioFormat.ALAW, (double) sampleRate, 8, 1, -1, -1)};
        return this.supportedOutputFormats;
    }

    public void open() throws ResourceUnavailableException {
    }

    public void close() {
    }

    private int calculateOutputSize(int inputLength) {
        if (this.inputSampleSize == 16) {
            return inputLength / 2;
        }
        return inputLength;
    }

    private void initConverter(AudioFormat inFormat) {
        boolean z = true;
        this.lastFormat = inFormat;
        this.inputSampleSize = inFormat.getSampleSizeInBits();
        if (inFormat.getEndian() != 1) {
            z = false;
        }
        this.bigEndian = z;
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
        if (inputBuffer.getLength() == 0) {
            return 4;
        }
        int outLength = calculateOutputSize(inputBuffer.getLength());
        aLawEncode(this.bigEndian, (byte[]) inputBuffer.getData(), inputBuffer.getOffset(), inputBuffer.getLength(), validateByteArraySize(outputBuffer, outLength));
        updateOutput(outputBuffer, this.outputFormat, outLength, 0);
        return 0;
    }

    static {
        for (int i = -32768; i <= 32767; i++) {
            pcmToALawMap[uShortToInt((short) i)] = encode(i);
        }
    }

    public static int uShortToInt(short value) {
        return value >= (short) 0 ? value : value + (short) 0;
    }

    public static void aLawEncode(boolean bigEndian, byte[] data, int offset, int length, byte[] target) {
        if (bigEndian) {
            aLawEncodeBigEndian(data, offset, length, target);
        } else {
            aLawEncodeLittleEndian(data, offset, length, target);
        }
    }

    public static void aLawEncodeLittleEndian(byte[] data, int offset, int length, byte[] target) {
        int size = length / 2;
        for (int i = 0; i < size; i++) {
            target[i] = aLawEncode(((data[((i * 2) + offset) + 1] & UnsignedUtils.MAX_UBYTE) << 8) | (data[(i * 2) + offset] & UnsignedUtils.MAX_UBYTE));
        }
    }

    public static void aLawEncodeBigEndian(byte[] data, int offset, int length, byte[] target) {
        int size = length / 2;
        for (int i = 0; i < size; i++) {
            target[i] = aLawEncode((data[((i * 2) + offset) + 1] & UnsignedUtils.MAX_UBYTE) | ((data[(i * 2) + offset] & UnsignedUtils.MAX_UBYTE) << 8));
        }
    }

    public static byte aLawEncode(int pcm) {
        return pcmToALawMap[uShortToInt((short) (65535 & pcm))];
    }

    private static byte encode(int pcm) {
        int sign = (32768 & pcm) >> 8;
        if (sign != 0) {
            pcm = -pcm;
        }
        if (pcm > 32767) {
            pcm = 32767;
        }
        int exponent = 7;
        for (int expMask = 16384; (pcm & expMask) == 0 && exponent > 0; expMask >>= 1) {
            exponent--;
        }
        return (byte) (((byte) (((exponent << 4) | sign) | ((pcm >> (exponent == 0 ? 4 : exponent + 3)) & 15))) ^ 213);
    }
}
