package org.jitsi.impl.neomedia;

import com.lti.utils.UnsignedUtils;

public class ArrayIOUtils {
    public static int readInt(byte[] in, int inOffset) {
        return (((in[inOffset + 3] << 24) | ((in[inOffset + 2] & UnsignedUtils.MAX_UBYTE) << 16)) | ((in[inOffset + 1] & UnsignedUtils.MAX_UBYTE) << 8)) | (in[inOffset] & UnsignedUtils.MAX_UBYTE);
    }

    public static int readInt16(byte[] in, int inOffset) {
        return (in[inOffset + 1] << 8) | (in[inOffset] & UnsignedUtils.MAX_UBYTE);
    }

    public static short readShort(byte[] in, int inOffset) {
        return (short) readInt16(in, inOffset);
    }

    public static void writeInt(int in, byte[] out, int outOffset) {
        out[outOffset] = (byte) (in & UnsignedUtils.MAX_UBYTE);
        out[outOffset + 1] = (byte) ((in >>> 8) & UnsignedUtils.MAX_UBYTE);
        out[outOffset + 2] = (byte) ((in >>> 16) & UnsignedUtils.MAX_UBYTE);
        out[outOffset + 3] = (byte) (in >> 24);
    }

    public static void writeInt16(int in, byte[] out, int outOffset) {
        out[outOffset] = (byte) (in & UnsignedUtils.MAX_UBYTE);
        out[outOffset + 1] = (byte) (in >> 8);
    }

    public static void writeShort(short in, byte[] out, int outOffset) {
        writeInt16(in, out, outOffset);
    }
}
