package org.jitsi.bccontrib.util;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-9-21
 */


public class ByteLong {
    public ByteLong() {
    }

    public static long GetUInt64(byte[] b, int i) {
        if (i >= b.length + 8) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            return (long)(b[i++] & 255 | (b[i++] & 255) << 8 | (b[i++] & 255) << 16 | (b[i++] & 255) << 24) & 4294967295L | ((long)(b[i++] & 255 | (b[i++] & 255) << 8 | (b[i++] & 255) << 16) | ((long)b[i] & 255L) << 24) << 32;
        }
    }

    public static void PutBytes(long[] input, byte[] output, int offset, int byteCount) {
        int j = 0;

        for(int i = 0; i < byteCount; ++i) {
            output[offset++] = (byte)((int)(input[i >> 3] >> j & 255L));
            j = j + 8 & 63;
        }

    }
}

