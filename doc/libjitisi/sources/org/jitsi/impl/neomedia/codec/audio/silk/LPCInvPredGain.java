package org.jitsi.impl.neomedia.codec.audio.silk;

import java.lang.reflect.Array;
import org.jitsi.impl.neomedia.codec.video.h264.JNIEncoder;

public class LPCInvPredGain {
    static final /* synthetic */ boolean $assertionsDisabled = (!LPCInvPredGain.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    static final int A_LIMIT = 65520;
    static final int QA = 16;

    static int SKP_Silk_LPC_inverse_pred_gain(int[] invGain_Q30, short[] A_Q12, int order) {
        int k;
        int rc_Q31;
        int[][] Atmp_QA = (int[][]) Array.newInstance(Integer.TYPE, new int[]{2, 16});
        int[] Anew_QA = Atmp_QA[order & 1];
        for (k = 0; k < order; k++) {
            Anew_QA[k] = A_Q12[k] << 4;
        }
        invGain_Q30[0] = JNIEncoder.X264_KEYINT_MAX_INFINITE;
        k = order - 1;
        while (k > 0) {
            if (Anew_QA[k] > A_LIMIT || Anew_QA[k] < -65520) {
                return 1;
            }
            rc_Q31 = -(Anew_QA[k] << 15);
            int rc_mult1_Q30 = 1073741823 - SigProcFIX.SKP_SMMUL(rc_Q31, rc_Q31);
            Typedef.SKP_assert(rc_mult1_Q30 > 32768 ? true : $assertionsDisabled);
            Typedef.SKP_assert(rc_mult1_Q30 < JNIEncoder.X264_KEYINT_MAX_INFINITE ? true : $assertionsDisabled);
            int rc_mult2_Q16 = Inlines.SKP_INVERSE32_varQ(rc_mult1_Q30, 46);
            invGain_Q30[0] = SigProcFIX.SKP_SMMUL(invGain_Q30[0], rc_mult1_Q30) << 2;
            Typedef.SKP_assert(invGain_Q30[0] >= 0 ? true : $assertionsDisabled);
            Typedef.SKP_assert(invGain_Q30[0] <= JNIEncoder.X264_KEYINT_MAX_INFINITE ? true : $assertionsDisabled);
            int[] Aold_QA = Anew_QA;
            Anew_QA = Atmp_QA[k & 1];
            int headrm = Macros.SKP_Silk_CLZ32(rc_mult2_Q16) - 1;
            rc_mult2_Q16 <<= headrm;
            for (int n = 0; n < k; n++) {
                Anew_QA[n] = SigProcFIX.SKP_SMMUL(Aold_QA[n] - (SigProcFIX.SKP_SMMUL(Aold_QA[(k - n) - 1], rc_Q31) << 1), rc_mult2_Q16) << (16 - headrm);
            }
            k--;
        }
        if (Anew_QA[0] > A_LIMIT || Anew_QA[0] < -65520) {
            return 1;
        }
        rc_Q31 = -(Anew_QA[0] << 15);
        invGain_Q30[0] = SigProcFIX.SKP_SMMUL(invGain_Q30[0], 1073741823 - SigProcFIX.SKP_SMMUL(rc_Q31, rc_Q31)) << 2;
        Typedef.SKP_assert(invGain_Q30[0] >= 0 ? true : $assertionsDisabled);
        Typedef.SKP_assert(invGain_Q30[0] <= JNIEncoder.X264_KEYINT_MAX_INFINITE ? true : $assertionsDisabled);
        return 0;
    }

    static int SKP_Silk_LPC_inverse_pred_gain_Q13(int[] invGain_Q30, short[] A_Q13, int order) {
        int k;
        int rc_Q31;
        int[][] Atmp_QA = (int[][]) Array.newInstance(Integer.TYPE, new int[]{2, 16});
        int[] Anew_QA = Atmp_QA[order & 1];
        for (k = 0; k < order; k++) {
            Anew_QA[k] = A_Q13[k] << 3;
        }
        invGain_Q30[0] = JNIEncoder.X264_KEYINT_MAX_INFINITE;
        k = order - 1;
        while (k > 0) {
            if (Anew_QA[k] > A_LIMIT || Anew_QA[k] < -65520) {
                return 1;
            }
            rc_Q31 = -(Anew_QA[k] << 15);
            int rc_mult1_Q30 = 1073741823 - SigProcFIX.SKP_SMMUL(rc_Q31, rc_Q31);
            if (!$assertionsDisabled && rc_mult1_Q30 <= 32768) {
                throw new AssertionError();
            } else if ($assertionsDisabled || rc_mult1_Q30 < JNIEncoder.X264_KEYINT_MAX_INFINITE) {
                int rc_mult2_Q16 = Inlines.SKP_INVERSE32_varQ(rc_mult1_Q30, 46);
                invGain_Q30[0] = SigProcFIX.SKP_SMMUL(invGain_Q30[0], rc_mult1_Q30) << 2;
                Typedef.SKP_assert(invGain_Q30[0] >= 0 ? true : $assertionsDisabled);
                Typedef.SKP_assert(invGain_Q30[0] <= JNIEncoder.X264_KEYINT_MAX_INFINITE ? true : $assertionsDisabled);
                int[] Aold_QA = Anew_QA;
                Anew_QA = Atmp_QA[k & 1];
                int headrm = Macros.SKP_Silk_CLZ32(rc_mult2_Q16) - 1;
                rc_mult2_Q16 <<= headrm;
                for (int n = 0; n < k; n++) {
                    Anew_QA[n] = SigProcFIX.SKP_SMMUL(Aold_QA[n] - (SigProcFIX.SKP_SMMUL(Aold_QA[(k - n) - 1], rc_Q31) << 1), rc_mult2_Q16) << (16 - headrm);
                }
                k--;
            } else {
                throw new AssertionError();
            }
        }
        if (Anew_QA[0] > A_LIMIT || Anew_QA[0] < -65520) {
            return 1;
        }
        rc_Q31 = -(Anew_QA[0] << 15);
        invGain_Q30[0] = SigProcFIX.SKP_SMMUL(invGain_Q30[0], 1073741823 - SigProcFIX.SKP_SMMUL(rc_Q31, rc_Q31)) << 2;
        Typedef.SKP_assert(invGain_Q30[0] >= 0 ? true : $assertionsDisabled);
        Typedef.SKP_assert(invGain_Q30[0] <= JNIEncoder.X264_KEYINT_MAX_INFINITE ? true : $assertionsDisabled);
        return 0;
    }
}
