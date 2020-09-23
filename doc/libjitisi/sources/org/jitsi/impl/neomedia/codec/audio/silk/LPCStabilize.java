package org.jitsi.impl.neomedia.codec.audio.silk;

import javax.media.Buffer;

public class LPCStabilize {
    static final /* synthetic */ boolean $assertionsDisabled = (!LPCStabilize.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    static final int LPC_STABILIZE_LPC_MAX_ABS_VALUE_Q16 = 524272;

    static void SKP_Silk_LPC_stabilize(short[] a_Q12, int[] a_Q16, int bwe_Q16, int L) {
        int i;
        int idx = 0;
        Bwexpander32.SKP_Silk_bwexpander_32(a_Q16, L, bwe_Q16);
        while (true) {
            int maxabs = Integer.MIN_VALUE;
            for (i = 0; i < L; i++) {
                int absval = Math.abs(a_Q16[i]);
                if (absval > maxabs) {
                    maxabs = absval;
                    idx = i;
                }
            }
            if (maxabs < LPC_STABILIZE_LPC_MAX_ABS_VALUE_Q16) {
                break;
            }
            Bwexpander32.SKP_Silk_bwexpander_32(a_Q16, L, Macros.SKP_SMULWB(Buffer.FLAG_SKIP_FEC - ((Buffer.FLAG_SKIP_FEC - (Integer.MAX_VALUE / (maxabs >> 4))) / (idx + 1)), 32604) << 1);
        }
        for (i = 0; i < L; i++) {
            a_Q12[i] = (short) SigProcFIX.SKP_RSHIFT_ROUND(a_Q16[i], 4);
        }
        int[] invGain_Q30_ptr = new int[]{0};
        while (LPCInvPredGain.SKP_Silk_LPC_inverse_pred_gain(invGain_Q30_ptr, a_Q12, L) == 1) {
            int invGain_Q30 = invGain_Q30_ptr[0];
            Bwexpander.SKP_Silk_bwexpander(a_Q12, L, 65339);
        }
    }

    static void SKP_Silk_LPC_fit(short[] a_QQ, int[] a_Q24, int QQ, int L) {
        int i;
        int idx = 0;
        int rshift = 24 - QQ;
        while (true) {
            int maxabs = Integer.MIN_VALUE;
            for (i = 0; i < L; i++) {
                int absval = Math.abs(a_Q24[i]);
                if (absval > maxabs) {
                    maxabs = absval;
                    idx = i;
                }
            }
            maxabs >>= rshift;
            if (maxabs < 32767) {
                break;
            }
            maxabs = Math.min(maxabs, 98369);
            Bwexpander32.SKP_Silk_bwexpander_32(a_Q24, L, 65470 - (((maxabs - 32767) * 16367) / SigProcFIX.SKP_RSHIFT32((idx + 1) * maxabs, 2)));
        }
        if (!$assertionsDisabled && rshift <= 0) {
            throw new AssertionError();
        } else if ($assertionsDisabled || rshift < 31) {
            for (i = 0; i < L; i++) {
                a_QQ[i] = (short) SigProcFIX.SKP_RSHIFT_ROUND(a_Q24[i], rshift);
            }
        } else {
            throw new AssertionError();
        }
    }
}
