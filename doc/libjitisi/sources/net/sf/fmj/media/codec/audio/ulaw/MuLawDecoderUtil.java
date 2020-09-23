package net.sf.fmj.media.codec.audio.ulaw;

import com.lti.utils.UnsignedUtils;
import com.sun.media.format.WavAudioFormat;

public class MuLawDecoderUtil {
    private static short[] muLawToPcmMap = new short[256];

    static {
        for (short i = (short) 0; i < muLawToPcmMap.length; i = (short) (i + 1)) {
            muLawToPcmMap[i] = decode((byte) i);
        }
    }

    private static short decode(byte mulaw) {
        mulaw = (byte) (mulaw ^ -1);
        int data = (((((mulaw & 15) | 16) << 1) + 1) << (((mulaw & WavAudioFormat.WAVE_FORMAT_VOXWARE_AC8) >> 4) + 2)) - 132;
        if ((mulaw & 128) != 0) {
            data = -data;
        }
        return (short) data;
    }

    public static void muLawDecode(boolean bigEndian, byte[] data, int offset, int len, byte[] decoded) {
        if (bigEndian) {
            muLawDecodeBigEndian(data, offset, len, decoded);
        } else {
            muLawDecodeLittleEndian(data, offset, len, decoded);
        }
    }

    public static short muLawDecode(byte mulaw) {
        return muLawToPcmMap[mulaw & UnsignedUtils.MAX_UBYTE];
    }

    public static void muLawDecodeBigEndian(byte[] data, int offset, int len, byte[] decoded) {
        int size = len;
        for (int i = 0; i < size; i++) {
            decoded[(i * 2) + 1] = (byte) (muLawToPcmMap[data[offset + i] & UnsignedUtils.MAX_UBYTE] & UnsignedUtils.MAX_UBYTE);
            decoded[i * 2] = (byte) ((muLawToPcmMap[data[offset + i] & UnsignedUtils.MAX_UBYTE] >> 8) & UnsignedUtils.MAX_UBYTE);
        }
    }

    public static void muLawDecodeLittleEndian(byte[] data, int offset, int len, byte[] decoded) {
        int size = len;
        for (int i = 0; i < size; i++) {
            decoded[i * 2] = (byte) (muLawToPcmMap[data[offset + i] & UnsignedUtils.MAX_UBYTE] & UnsignedUtils.MAX_UBYTE);
            decoded[(i * 2) + 1] = (byte) ((muLawToPcmMap[data[offset + i] & UnsignedUtils.MAX_UBYTE] >> 8) & UnsignedUtils.MAX_UBYTE);
        }
    }
}
