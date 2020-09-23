package org.jitsi.impl.neomedia.codec.audio.silk;

public class Tables {
    static final int PITCH_EST_MAX_LAG_MS = 18;
    static final int PITCH_EST_MIN_LAG_MS = 2;

    static int[] copyOfRange(int[] original, int from, int to) {
        if (from < 0 || from > original.length) {
            throw new ArrayIndexOutOfBoundsException(from);
        } else if (from > to) {
            throw new IllegalArgumentException("to");
        } else {
            int length = to - from;
            int[] copy = new int[length];
            int c = 0;
            int o = from;
            while (c < length) {
                copy[c] = o < original.length ? original[o] : 0;
                c++;
                o++;
            }
            return copy;
        }
    }
}
