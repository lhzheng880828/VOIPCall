package net.sf.fmj.media.codec.audio.alaw;

import com.lti.utils.UnsignedUtils;
import javax.media.Buffer;

public class ALawEncoderUtil {
    public static final int MAX = 32767;
    private static byte[] pcmToALawMap = new byte[Buffer.FLAG_SKIP_FEC];

    static {
        for (int i = -32768; i <= 32767; i++) {
            pcmToALawMap[UnsignedUtils.uShortToInt((short) i)] = encode(i);
        }
    }

    public static void aLawEncode(boolean bigEndian, byte[] data, int offset, int length, byte[] target) {
        if (bigEndian) {
            aLawEncodeBigEndian(data, offset, length, target);
        } else {
            aLawEncodeLittleEndian(data, offset, length, target);
        }
    }

    public static byte aLawEncode(int pcm) {
        return pcmToALawMap[UnsignedUtils.uShortToInt((short) (65535 & pcm))];
    }

    public static byte aLawEncode(short pcm) {
        return pcmToALawMap[UnsignedUtils.uShortToInt(pcm)];
    }

    public static void aLawEncodeBigEndian(byte[] data, int offset, int length, byte[] target) {
        int size = length / 2;
        for (int i = 0; i < size; i++) {
            target[i] = aLawEncode((data[((i * 2) + offset) + 1] & UnsignedUtils.MAX_UBYTE) | ((data[(i * 2) + offset] & UnsignedUtils.MAX_UBYTE) << 8));
        }
    }

    public static void aLawEncodeLittleEndian(byte[] data, int offset, int length, byte[] target) {
        int size = length / 2;
        for (int i = 0; i < size; i++) {
            target[i] = aLawEncode(((data[((i * 2) + offset) + 1] & UnsignedUtils.MAX_UBYTE) << 8) | (data[(i * 2) + offset] & UnsignedUtils.MAX_UBYTE));
        }
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
