package org.jitsi.impl.neomedia.codec.audio.g729;

import com.sun.media.format.WavAudioFormat;

class Pitch {
    Pitch() {
    }

    static int pitch_ol(float[] signal, int signal_offset, int pit_min, int pit_max, int l_frame) {
        FloatReference maxRef = new FloatReference();
        int p_max1 = lag_max(signal, signal_offset, l_frame, pit_max, 80, maxRef);
        float max1 = maxRef.value;
        int p_max2 = lag_max(signal, signal_offset, l_frame, 79, 40, maxRef);
        float max2 = maxRef.value;
        int p_max3 = lag_max(signal, signal_offset, l_frame, 39, pit_min, maxRef);
        float max3 = maxRef.value;
        if (max1 * 0.85f < max2) {
            max1 = max2;
            p_max1 = p_max2;
        }
        if (max1 * 0.85f < max3) {
            return p_max3;
        }
        return p_max1;
    }

    private static int lag_max(float[] signal, int signal_offset, int l_frame, int lagmax, int lagmin, FloatReference cor_max) {
        int i;
        int p;
        float t0;
        int p_max = 0;
        float max = -1.0E38f;
        for (i = lagmax; i >= lagmin; i--) {
            p = signal_offset;
            int p1 = signal_offset - i;
            t0 = 0.0f;
            int j = 0;
            while (j < l_frame) {
                t0 += signal[p] * signal[p1];
                j++;
                p++;
                p1++;
            }
            if (t0 >= max) {
                max = t0;
                p_max = i;
            }
        }
        t0 = 0.01f;
        p = signal_offset - p_max;
        i = 0;
        while (i < l_frame) {
            t0 += signal[p] * signal[p];
            i++;
            p++;
        }
        cor_max.value = max * inv_sqrt(t0);
        return p_max;
    }

    static int pitch_fr3(float[] exc, int exc_offset, float[] xn, float[] h, int l_subfr, int t0_min, int t0_max, int i_subfr, IntReference pit_frac) {
        int i;
        int t_min = t0_min - 4;
        float[] corr = new float[18];
        int corr_offset = -t_min;
        norm_corr(exc, exc_offset, xn, h, l_subfr, t_min, t0_max + 4, corr, corr_offset);
        float max = corr[corr_offset + t0_min];
        int lag = t0_min;
        for (i = t0_min + 1; i <= t0_max; i++) {
            if (corr[corr_offset + i] >= max) {
                max = corr[corr_offset + i];
                lag = i;
            }
        }
        if (i_subfr != 0 || lag <= 84) {
            corr_offset += lag;
            max = interpol_3(corr, corr_offset, -2);
            int frac = -2;
            for (i = -1; i <= 2; i++) {
                float corr_int = interpol_3(corr, corr_offset, i);
                if (corr_int > max) {
                    max = corr_int;
                    frac = i;
                }
            }
            if (frac == -2) {
                frac = 1;
                lag--;
            }
            if (frac == 2) {
                frac = -1;
                lag++;
            }
            pit_frac.value = frac;
            int i2 = lag;
            return lag;
        }
        pit_frac.value = 0;
        return lag;
    }

    private static void norm_corr(float[] exc, int exc_offset, float[] xn, float[] h, int l_subfr, int t_min, int t_max, float[] corr_norm, int corr_norm_offset) {
        float[] excf = new float[40];
        int k = exc_offset - t_min;
        Filter.convolve(exc, k, h, excf, l_subfr);
        for (int i = t_min; i <= t_max; i++) {
            int j;
            float alp = 0.01f;
            for (j = 0; j < l_subfr; j++) {
                alp += excf[j] * excf[j];
            }
            float norm = inv_sqrt(alp);
            float s = 0.0f;
            for (j = 0; j < l_subfr; j++) {
                s += xn[j] * excf[j];
            }
            corr_norm[corr_norm_offset + i] = s * norm;
            if (i != t_max) {
                k--;
                for (j = l_subfr - 1; j > 0; j--) {
                    excf[j] = excf[j - 1] + (exc[k] * h[j]);
                }
                excf[0] = exc[k];
            }
        }
    }

    static float g_pitch(float[] xn, float[] y1, float[] g_coeff, int l_subfr) {
        int i;
        float xy = 0.0f;
        for (i = 0; i < l_subfr; i++) {
            xy += xn[i] * y1[i];
        }
        float yy = 0.01f;
        for (i = 0; i < l_subfr; i++) {
            yy += y1[i] * y1[i];
        }
        g_coeff[0] = yy;
        g_coeff[1] = (-2.0f * xy) + 0.01f;
        float gain = xy / yy;
        if (gain < 0.0f) {
            gain = 0.0f;
        }
        if (gain > 1.2f) {
            return 1.2f;
        }
        return gain;
    }

    static int enc_lag3(int T0, int T0_frac, IntReference T0_min, IntReference T0_max, int pit_min, int pit_max, int pit_flag) {
        int index;
        int _T0_min = T0_min.value;
        int _T0_max = T0_max.value;
        if (pit_flag == 0) {
            if (T0 <= 85) {
                index = ((T0 * 3) - 58) + T0_frac;
            } else {
                index = T0 + WavAudioFormat.WAVE_FORMAT_VOXWARE_AC8;
            }
            _T0_min = T0 - 5;
            if (_T0_min < pit_min) {
                _T0_min = pit_min;
            }
            _T0_max = _T0_min + 9;
            if (_T0_max > pit_max) {
                _T0_max = pit_max;
                _T0_min = _T0_max - 9;
            }
        } else {
            index = (((T0 - _T0_min) * 3) + 2) + T0_frac;
        }
        T0_min.value = _T0_min;
        T0_max.value = _T0_max;
        return index;
    }

    private static float interpol_3(float[] x, int x_offset, int frac) {
        float[] inter_3 = TabLd8k.inter_3;
        if (frac < 0) {
            frac += 3;
            x_offset--;
        }
        int x1 = x_offset;
        int x2 = x_offset + 1;
        int c1 = frac;
        int c2 = 3 - frac;
        float s = 0.0f;
        int i = 0;
        while (i < 4) {
            s += (x[x1] * inter_3[c1]) + (x[x2] * inter_3[c2]);
            x1--;
            x2++;
            i++;
            c1 += 3;
            c2 += 3;
        }
        return s;
    }

    private static float inv_sqrt(float x) {
        return 1.0f / ((float) Math.sqrt((double) x));
    }
}
