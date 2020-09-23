package org.jitsi.impl.neomedia.codec.audio.silk;

import com.lti.utils.UnsignedUtils;
import java.util.Arrays;
import org.jitsi.impl.neomedia.device.DeviceConfiguration;
import org.jitsi.impl.neomedia.jmfext.media.protocol.video4linux2.Video4Linux2;

public class CNG {
    static void SKP_Silk_CNG_exc(short[] residual, int residual_offset, int[] exc_buf_Q10, int exc_buf_Q10_offset, int Gain_Q16, int length, int[] rand_seed) {
        int exc_mask = UnsignedUtils.MAX_UBYTE;
        while (exc_mask > length) {
            exc_mask >>= 1;
        }
        int seed = rand_seed[0];
        for (int i = 0; i < length; i++) {
            boolean z;
            seed = SigProcFIX.SKP_RAND(seed);
            int idx = (seed >> 24) & exc_mask;
            if (idx >= 0) {
                z = true;
            } else {
                z = false;
            }
            Typedef.SKP_assert(z);
            if (idx <= UnsignedUtils.MAX_UBYTE) {
                z = true;
            } else {
                z = false;
            }
            Typedef.SKP_assert(z);
            residual[residual_offset + i] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(Macros.SKP_SMULWW(exc_buf_Q10[idx], Gain_Q16), 10));
        }
        rand_seed[0] = seed;
    }

    static void SKP_Silk_CNG_Reset(SKP_Silk_decoder_state psDec) {
        int NLSF_step_Q15 = 32767 / (psDec.LPC_order + 1);
        int NLSF_acc_Q15 = 0;
        for (int i = 0; i < psDec.LPC_order; i++) {
            NLSF_acc_Q15 += NLSF_step_Q15;
            psDec.sCNG.CNG_smth_NLSF_Q15[i] = NLSF_acc_Q15;
        }
        psDec.sCNG.CNG_smth_Gain_Q16 = 0;
        psDec.sCNG.rand_seed = 3176576;
    }

    static void SKP_Silk_CNG(SKP_Silk_decoder_state psDec, SKP_Silk_decoder_control psDecCtrl, short[] signal, int signal_offset, int length) {
        int i;
        short[] LPC_buf = new short[16];
        short[] CNG_sig = new short[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
        SKP_Silk_CNG_struct psCNG = psDec.sCNG;
        if (psDec.fs_kHz != psCNG.fs_kHz) {
            SKP_Silk_CNG_Reset(psDec);
            psCNG.fs_kHz = psDec.fs_kHz;
        }
        if (psDec.lossCnt == 0 && psDec.vadFlag == 0) {
            for (i = 0; i < psDec.LPC_order; i++) {
                int[] iArr = psCNG.CNG_smth_NLSF_Q15;
                iArr[i] = iArr[i] + Macros.SKP_SMULWB(psDec.prevNLSF_Q15[i] - psCNG.CNG_smth_NLSF_Q15[i], 16348);
            }
            int max_Gain_Q16 = 0;
            int subfr = 0;
            for (i = 0; i < 4; i++) {
                if (psDecCtrl.Gains_Q16[i] > max_Gain_Q16) {
                    max_Gain_Q16 = psDecCtrl.Gains_Q16[i];
                    subfr = i;
                }
            }
            System.arraycopy(psCNG.CNG_exc_buf_Q10, 0, psCNG.CNG_exc_buf_Q10, psDec.subfr_length, psDec.subfr_length * 3);
            System.arraycopy(psDec.exc_Q10, psDec.subfr_length * subfr, psCNG.CNG_exc_buf_Q10, 0, psDec.subfr_length);
            for (i = 0; i < 4; i++) {
                psCNG.CNG_smth_Gain_Q16 += Macros.SKP_SMULWB(psDecCtrl.Gains_Q16[i] - psCNG.CNG_smth_Gain_Q16, 4634);
            }
        }
        if (psDec.lossCnt != 0) {
            int[] psCNG_rand_seed_ptr = new int[]{psCNG.rand_seed};
            SKP_Silk_CNG_exc(CNG_sig, 0, psCNG.CNG_exc_buf_Q10, 0, psCNG.CNG_smth_Gain_Q16, length, psCNG_rand_seed_ptr);
            psCNG.rand_seed = psCNG_rand_seed_ptr[0];
            NLSF2AStable.SKP_Silk_NLSF2A_stable(LPC_buf, psCNG.CNG_smth_NLSF_Q15, psDec.LPC_order);
            if (psDec.LPC_order == 16) {
                LPCSynthesisOrder16.SKP_Silk_LPC_synthesis_order16(CNG_sig, LPC_buf, Video4Linux2.V4L2_CAP_STREAMING, psCNG.CNG_synth_state, CNG_sig, length);
            } else {
                LPCSynthesisFilter.SKP_Silk_LPC_synthesis_filter(CNG_sig, LPC_buf, Video4Linux2.V4L2_CAP_STREAMING, psCNG.CNG_synth_state, CNG_sig, length, psDec.LPC_order);
            }
            for (i = 0; i < length; i++) {
                signal[signal_offset + i] = (short) SigProcFIX.SKP_SAT16(signal[signal_offset + i] + CNG_sig[i]);
            }
            return;
        }
        Arrays.fill(psCNG.CNG_synth_state, 0, psDec.LPC_order, 0);
    }
}
