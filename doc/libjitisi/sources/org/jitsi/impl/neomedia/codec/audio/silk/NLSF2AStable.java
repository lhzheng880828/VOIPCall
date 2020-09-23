package org.jitsi.impl.neomedia.codec.audio.silk;

import javax.media.Buffer;

public class NLSF2AStable {
    static void SKP_Silk_NLSF2A_stable(short[] pAR_Q12, int[] pNLSF, int LPC_order) {
        int[] invGain_Q30_ptr = new int[1];
        NLSF2A.SKP_Silk_NLSF2A(pAR_Q12, pNLSF, LPC_order);
        int i = 0;
        while (i < 20) {
            int invGain_Q30;
            if (LPCInvPredGain.SKP_Silk_LPC_inverse_pred_gain(invGain_Q30_ptr, pAR_Q12, LPC_order) != 1) {
                invGain_Q30 = invGain_Q30_ptr[0];
                break;
            }
            invGain_Q30 = invGain_Q30_ptr[0];
            Bwexpander.SKP_Silk_bwexpander(pAR_Q12, LPC_order, Buffer.FLAG_SKIP_FEC - Macros.SKP_SMULBB(66, i));
            i++;
        }
        if (i == 20) {
            for (i = 0; i < LPC_order; i++) {
                pAR_Q12[i] = (short) 0;
            }
        }
    }
}
