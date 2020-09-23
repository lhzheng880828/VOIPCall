package org.jitsi.impl.neomedia.codec.audio.g729;

class PParity {
    PParity() {
    }

    static int parity_pitch(int pitch_index) {
        int temp = pitch_index >> 1;
        int sum = 1;
        for (int i = 0; i <= 5; i++) {
            temp >>= 1;
            sum += temp & 1;
        }
        return sum & 1;
    }

    static int check_parity_pitch(int pitch_index, int parity) {
        int temp = pitch_index >> 1;
        int sum = 1;
        for (int i = 0; i <= 5; i++) {
            temp >>= 1;
            sum += temp & 1;
        }
        return (sum + parity) & 1;
    }
}
