package net.sf.fmj.media.codec.audio.ulaw;

import com.lti.utils.UnsignedUtils;
import javax.media.Buffer;

public class MuLawEncoderUtil {
    public static final int BIAS = 132;
    public static final int MAX = 32635;
    private static byte[] pcmToMuLawMap = new byte[Buffer.FLAG_SKIP_FEC];

    static {
        for (int i = -32768; i <= 32767; i++) {
            pcmToMuLawMap[UnsignedUtils.uShortToInt((short) i)] = encode(i);
        }
    }

    private static byte encode(int pcm) {
        int sign = (32768 & pcm) >> 8;
        if (sign != 0) {
            pcm = -pcm;
        }
        if (pcm > MAX) {
            pcm = MAX;
        }
        pcm += BIAS;
        int exponent = 7;
        for (int expMask = 16384; (pcm & expMask) == 0; expMask >>= 1) {
            exponent--;
        }
        return (byte) (((byte) (((exponent << 4) | sign) | ((pcm >> (exponent + 3)) & 15))) ^ -1);
    }

    public static void muLawEncode(boolean bigEndian, byte[] data, int offset, int len, byte[] target) {
        if (bigEndian) {
            muLawEncodeBigEndian(data, offset, len, target);
        } else {
            muLawEncodeLittleEndian(data, offset, len, target);
        }
    }

    public static byte muLawEncode(int pcm) {
        return pcmToMuLawMap[65535 & pcm];
    }

    public static byte muLawEncode(short pcm) {
        return pcmToMuLawMap[UnsignedUtils.uShortToInt(pcm)];
    }

    public static void muLawEncodeBigEndian(byte[] data, int offset, int len, byte[] target) {
        int size = len / 2;
        for (int i = 0; i < size; i++) {
            target[i] = muLawEncode((data[((i * 2) + offset) + 1] & UnsignedUtils.MAX_UBYTE) | ((data[(i * 2) + offset] & UnsignedUtils.MAX_UBYTE) << 8));
        }
    }

    public static void muLawEncodeLittleEndian(byte[] data, int offset, int len, byte[] target) {
        int size = len / 2;
        for (int i = 0; i < size; i++) {
            target[i] = muLawEncode(((data[((i * 2) + offset) + 1] & UnsignedUtils.MAX_UBYTE) << 8) | (data[(i * 2) + offset] & UnsignedUtils.MAX_UBYTE));
        }
    }

    public boolean getZeroTrap() {
        return pcmToMuLawMap[33000] != (byte) 0;
    }

    public void setZeroTrap(boolean value) {
        byte val = (byte) (value ? 2 : 0);
        for (int i = 32768; i <= 33924; i++) {
            pcmToMuLawMap[i] = val;
        }
    }
}
