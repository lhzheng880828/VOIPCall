package org.jitsi.impl.neomedia.codec.audio.g729;

class DeAcelp {
    DeAcelp() {
    }

    static void decod_ACELP(int sign, int index, float[] cod) {
        int i;
        int[] pos = new int[4];
        pos[0] = (index & 7) * 5;
        index >>= 3;
        pos[1] = ((index & 7) * 5) + 1;
        index >>= 3;
        pos[2] = ((index & 7) * 5) + 2;
        index >>= 3;
        pos[3] = ((((index >> 1) & 7) * 5) + 3) + (index & 1);
        for (i = 0; i < 40; i++) {
            cod[i] = 0.0f;
        }
        for (int j = 0; j < 4; j++) {
            i = sign & 1;
            sign >>= 1;
            if (i != 0) {
                cod[pos[j]] = 1.0f;
            } else {
                cod[pos[j]] = -1.0f;
            }
        }
    }
}
