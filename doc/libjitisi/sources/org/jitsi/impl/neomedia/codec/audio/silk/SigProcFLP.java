package org.jitsi.impl.neomedia.codec.audio.silk;

import org.jitsi.impl.neomedia.portaudio.Pa;

public class SigProcFLP extends SigProcFLPConstants {
    static float SKP_min_float(float a, float b) {
        return a < b ? a : b;
    }

    static float SKP_max_float(float a, float b) {
        return a > b ? a : b;
    }

    static float SKP_abs_float(float a) {
        return Math.abs(a);
    }

    static float SKP_LIMIT_float(float a, float limit1, float limit2) {
        if (limit1 <= limit2) {
            if (a <= limit2) {
                limit2 = a < limit1 ? limit1 : a;
            }
            return limit2;
        } else if (a > limit1) {
            return limit1;
        } else {
            return a < limit2 ? limit2 : a;
        }
    }

    static float SKP_sigmoid(float x) {
        return (float) (1.0d / (Math.exp((double) (-x)) + 1.0d));
    }

    static void SKP_float2short_array(short[] out, int out_offset, float[] in, int in_offset, int length) {
        for (int k = length - 1; k >= 0; k--) {
            double x = (double) in[in_offset + k];
            out[out_offset + k] = (short) SigProcFIX.SKP_SAT16((int) (x > Pa.LATENCY_UNSPECIFIED ? x + 0.5d : x - 0.5d));
        }
    }

    static int SKP_float2int(double x) {
        return (int) (x > Pa.LATENCY_UNSPECIFIED ? x + 0.5d : x - 0.5d);
    }

    static void SKP_short2float_array(float[] out, int out_offset, short[] in, int in_offset, int length) {
        for (int k = length - 1; k >= 0; k--) {
            out[out_offset + k] = (float) in[in_offset + k];
        }
    }

    static float SKP_round(float x) {
        return (float) (x >= 0.0f ? (long) (((double) x) + 0.5d) : (long) (((double) x) - 0.5d));
    }
}
