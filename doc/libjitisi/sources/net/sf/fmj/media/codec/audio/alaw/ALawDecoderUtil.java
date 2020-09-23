package net.sf.fmj.media.codec.audio.alaw;

import com.lti.utils.UnsignedUtils;
import com.sun.media.format.WavAudioFormat;

public class ALawDecoderUtil {
    private static short[] aLawToPcmMap = new short[256];

    static {
        for (int i = 0; i < aLawToPcmMap.length; i++) {
            aLawToPcmMap[i] = decode((byte) i);
        }
    }

    public static void aLawDecode(boolean bigEndian, byte[] data, int offset, int length, byte[] decoded) {
        if (bigEndian) {
            aLawDecodeBigEndian(data, offset, length, decoded);
        } else {
            aLawDecodeLittleEndian(data, offset, length, decoded);
        }
    }

    public static short aLawDecode(byte alaw) {
        return aLawToPcmMap[alaw & UnsignedUtils.MAX_UBYTE];
    }

    public static void aLawDecodeBigEndian(byte[] data, int offset, int length, byte[] decoded) {
        int size = length;
        for (int i = 0; i < size; i++) {
            decoded[(i * 2) + 1] = (byte) (aLawToPcmMap[data[offset + i] & UnsignedUtils.MAX_UBYTE] & UnsignedUtils.MAX_UBYTE);
            decoded[i * 2] = (byte) (aLawToPcmMap[data[offset + i] & UnsignedUtils.MAX_UBYTE] >> 8);
        }
    }

    public static void aLawDecodeLittleEndian(byte[] data, int offset, int length, byte[] decoded) {
        int size = length;
        for (int i = 0; i < size; i++) {
            decoded[i * 2] = (byte) (aLawToPcmMap[data[offset + i] & UnsignedUtils.MAX_UBYTE] & UnsignedUtils.MAX_UBYTE);
            decoded[(i * 2) + 1] = (byte) (aLawToPcmMap[data[offset + i] & UnsignedUtils.MAX_UBYTE] >> 8);
        }
    }

    private static short decode(byte alaw) {
        alaw = (byte) (alaw ^ 213);
        int sign = alaw & 128;
        int exponent = (alaw & WavAudioFormat.WAVE_FORMAT_VOXWARE_AC8) >> 4;
        int data = ((alaw & 15) << 4) + 8;
        if (exponent != 0) {
            data += 256;
        }
        if (exponent > 1) {
            data <<= exponent - 1;
        }
        if (sign != 0) {
            data = -data;
        }
        return (short) data;
    }
}
