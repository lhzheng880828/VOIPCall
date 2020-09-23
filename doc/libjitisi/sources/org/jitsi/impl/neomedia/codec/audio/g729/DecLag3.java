package org.jitsi.impl.neomedia.codec.audio.g729;

class DecLag3 {
    DecLag3() {
    }

    static void dec_lag3(int index, int pit_min, int pit_max, int i_subfr, IntReference T0, IntReference T0_frac) {
        int _T0 = T0.value;
        int _T0_frac = T0_frac.value;
        if (i_subfr != 0) {
            int T0_min = _T0 - 5;
            if (T0_min < pit_min) {
                T0_min = pit_min;
            }
            if (T0_min + 9 > pit_max) {
                T0_min = pit_max - 9;
            }
            int i = ((index + 2) / 3) - 1;
            _T0 = i + T0_min;
            _T0_frac = (index - 2) - (i * 3);
        } else if (index < 197) {
            _T0 = ((index + 2) / 3) + 19;
            _T0_frac = (index - (_T0 * 3)) + 58;
        } else {
            _T0 = index - 112;
            _T0_frac = 0;
        }
        T0.value = _T0;
        T0_frac.value = _T0_frac;
    }
}
