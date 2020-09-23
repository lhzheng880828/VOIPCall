package org.jitsi.impl.neomedia.codec.audio.g729;

import com.lti.utils.UnsignedUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class Util {
    private static short seed = (short) 21845;

    Util() {
    }

    static void set_zero(float[] x, int L) {
        set_zero(x, 0, L);
    }

    static void set_zero(float[] x, int offset, int length) {
        int toIndex = offset + length;
        for (int i = offset; i < toIndex; i++) {
            x[i] = 0.0f;
        }
    }

    static void copy(float[] x, float[] y, int L) {
        copy(x, 0, y, L);
    }

    static void copy(float[] x, int x_offset, float[] y, int L) {
        copy(x, x_offset, y, 0, L);
    }

    static void copy(float[] x, int x_offset, float[] y, int y_offset, int L) {
        for (int i = 0; i < L; i++) {
            y[y_offset + i] = x[x_offset + i];
        }
    }

    static short random_g729() {
        seed = (short) ((int) ((((long) seed) * 31821) + 13849));
        return seed;
    }

    static void fwrite(short[] data, int length, OutputStream fp) throws IOException {
        byte[] bytes = new byte[2];
        for (int i = 0; i < length; i++) {
            int value = data[i];
            bytes[0] = (byte) (value & UnsignedUtils.MAX_UBYTE);
            bytes[1] = (byte) (value >> 8);
            fp.write(bytes);
        }
    }

    static int fread(short[] data, int length, InputStream fp) throws IOException {
        byte[] bytes = new byte[2];
        int readLength = 0;
        for (int i = 0; i < length && fp.read(bytes) == 2; i++) {
            data[i] = (short) ((bytes[1] << 8) | (bytes[0] & UnsignedUtils.MAX_UBYTE));
            readLength++;
        }
        return readLength;
    }
}
