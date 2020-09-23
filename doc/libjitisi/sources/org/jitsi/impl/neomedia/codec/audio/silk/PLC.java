package org.jitsi.impl.neomedia.codec.audio.silk;

import java.util.Arrays;
import org.jitsi.impl.neomedia.device.DeviceConfiguration;

public class PLC {
    static final int BWE_COEF_Q16 = 64880;
    static short[] HARM_ATT_Q15 = new short[]{(short) 32440, (short) 31130};
    static final int LOG2_INV_LPC_GAIN_HIGH_THRES = 3;
    static final int LOG2_INV_LPC_GAIN_LOW_THRES = 8;
    static final int MAX_PITCH_LAG_MS = 18;
    static final int NB_ATT = 2;
    static final int PITCH_DRIFT_FAC_Q16 = 655;
    static short[] PLC_RAND_ATTENUATE_UV_Q15 = new short[]{(short) 32440, (short) 29491};
    static short[] PLC_RAND_ATTENUATE_V_Q15 = new short[]{(short) 31130, (short) 26214};
    static final int RAND_BUF_MASK = 127;
    static final int RAND_BUF_SIZE = 128;
    static final int SA_THRES_Q8 = 50;
    static final boolean USE_SINGLE_TAP = true;
    static final int V_PITCH_GAIN_START_MAX_Q14 = 15565;
    static final int V_PITCH_GAIN_START_MIN_Q14 = 11469;

    static void SKP_Silk_PLC_Reset(SKP_Silk_decoder_state psDec) {
        psDec.sPLC.pitchL_Q8 = psDec.frame_length >> 1;
    }

    static void SKP_Silk_PLC(SKP_Silk_decoder_state psDec, SKP_Silk_decoder_control psDecCtrl, short[] signal, int signal_offset, int length, int lost) {
        if (psDec.fs_kHz != psDec.sPLC.fs_kHz) {
            SKP_Silk_PLC_Reset(psDec);
            psDec.sPLC.fs_kHz = psDec.fs_kHz;
        }
        if (lost != 0) {
            SKP_Silk_PLC_conceal(psDec, psDecCtrl, signal, signal_offset, length);
        } else {
            SKP_Silk_PLC_update(psDec, psDecCtrl, signal, signal_offset, length);
        }
    }

    static void SKP_Silk_PLC_update(SKP_Silk_decoder_state psDec, SKP_Silk_decoder_control psDecCtrl, short[] signal, int signal_offset, int length) {
        SKP_Silk_PLC_struct psPLC = psDec.sPLC;
        psDec.prev_sigtype = psDecCtrl.sigtype;
        int LTP_Gain_Q14 = 0;
        if (psDecCtrl.sigtype == 0) {
            int i;
            for (int j = 0; psDec.subfr_length * j < psDecCtrl.pitchL[3]; j++) {
                int temp_LTP_Gain_Q14 = 0;
                for (i = 0; i < 5; i++) {
                    temp_LTP_Gain_Q14 += psDecCtrl.LTPCoef_Q14[((3 - j) * 5) + i];
                }
                if (temp_LTP_Gain_Q14 > LTP_Gain_Q14) {
                    LTP_Gain_Q14 = temp_LTP_Gain_Q14;
                    System.arraycopy(psDecCtrl.LTPCoef_Q14, Macros.SKP_SMULBB(3 - j, 5), psPLC.LTPCoef_Q14, 0, 5);
                    psPLC.pitchL_Q8 = psDecCtrl.pitchL[3 - j] << 8;
                }
            }
            Arrays.fill(psPLC.LTPCoef_Q14, 0, 5, (short) 0);
            psPLC.LTPCoef_Q14[2] = (short) LTP_Gain_Q14;
            if (LTP_Gain_Q14 < V_PITCH_GAIN_START_MIN_Q14) {
                int scale_Q10 = 11744256 / Math.max(LTP_Gain_Q14, 1);
                for (i = 0; i < 5; i++) {
                    psPLC.LTPCoef_Q14[i] = (short) (Macros.SKP_SMULBB(psPLC.LTPCoef_Q14[i], scale_Q10) >> 10);
                }
            } else if (LTP_Gain_Q14 > V_PITCH_GAIN_START_MAX_Q14) {
                int scale_Q14 = 255016960 / Math.max(LTP_Gain_Q14, 1);
                for (i = 0; i < 5; i++) {
                    psPLC.LTPCoef_Q14[i] = (short) (Macros.SKP_SMULBB(psPLC.LTPCoef_Q14[i], scale_Q14) << 14);
                }
            }
        } else {
            psPLC.pitchL_Q8 = Macros.SKP_SMULBB(psDec.fs_kHz, 18) << 8;
            Arrays.fill(psPLC.LTPCoef_Q14, 0, 5, (short) 0);
        }
        System.arraycopy(psDecCtrl.PredCoef_Q12[1], 0, psPLC.prevLPC_Q12, 0, psDec.LPC_order);
        psPLC.prevLTP_scale_Q14 = (short) psDecCtrl.LTP_scale_Q14;
        System.arraycopy(psDecCtrl.Gains_Q16, 0, psPLC.prevGain_Q16, 0, 4);
    }

    static void SKP_Silk_PLC_conceal(SKP_Silk_decoder_state psDec, SKP_Silk_decoder_control psDecCtrl, short[] signal, int signal_offset, int length) {
        int k;
        int i;
        int[] rand_ptr;
        int rand_ptr_offset;
        int rand_Gain_Q15;
        int j;
        short[] exc_buf = new short[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
        short[] A_Q12_tmp = new short[16];
        int[] shift1_ptr = new int[1];
        int[] shift2_ptr = new int[1];
        int[] energy1_ptr = new int[1];
        int[] energy2_ptr = new int[1];
        int[] sig_Q10 = new int[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
        SKP_Silk_PLC_struct psPLC = psDec.sPLC;
        System.arraycopy(psDec.sLTP_Q16, psDec.frame_length, psDec.sLTP_Q16, 0, psDec.frame_length);
        Bwexpander.SKP_Silk_bwexpander(psPLC.prevLPC_Q12, psDec.LPC_order, BWE_COEF_Q16);
        short[] exc_buf_ptr = exc_buf;
        int exc_buf_ptr_offset = 0;
        for (k = 2; k < 4; k++) {
            for (i = 0; i < psDec.subfr_length; i++) {
                exc_buf_ptr[exc_buf_ptr_offset + i] = (short) (Macros.SKP_SMULWW(psDec.exc_Q10[(psDec.subfr_length * k) + i], psPLC.prevGain_Q16[k]) >> 10);
            }
            exc_buf_ptr_offset += psDec.subfr_length;
        }
        SumSqrShift.SKP_Silk_sum_sqr_shift(energy1_ptr, shift1_ptr, exc_buf, 0, psDec.subfr_length);
        int energy1 = energy1_ptr[0];
        int shift1 = shift1_ptr[0];
        SumSqrShift.SKP_Silk_sum_sqr_shift(energy2_ptr, shift2_ptr, exc_buf, psDec.subfr_length, psDec.subfr_length);
        int energy2 = energy2_ptr[0];
        int shift2 = shift2_ptr[0];
        if ((energy1 >> shift2) < (energy1 >> shift2)) {
            rand_ptr = psDec.exc_Q10;
            rand_ptr_offset = Math.max(0, (psDec.subfr_length * 3) - 128);
        } else {
            rand_ptr = psDec.exc_Q10;
            rand_ptr_offset = Math.max(0, psDec.frame_length - 128);
        }
        short[] B_Q14 = psPLC.LTPCoef_Q14;
        short rand_scale_Q14 = psPLC.randScale_Q14;
        int harm_Gain_Q15 = HARM_ATT_Q15[Math.min(1, psDec.lossCnt)];
        if (psDec.prev_sigtype == 0) {
            rand_Gain_Q15 = PLC_RAND_ATTENUATE_V_Q15[Math.min(1, psDec.lossCnt)];
        } else {
            rand_Gain_Q15 = PLC_RAND_ATTENUATE_UV_Q15[Math.min(1, psDec.lossCnt)];
        }
        if (psDec.lossCnt == 0) {
            rand_scale_Q14 = (short) 16384;
            if (psDec.prev_sigtype == 0) {
                for (i = 0; i < 5; i++) {
                    rand_scale_Q14 = (short) (rand_scale_Q14 - B_Q14[i]);
                }
                rand_scale_Q14 = (short) (Macros.SKP_SMULBB((short) Math.max(3277, rand_scale_Q14), psPLC.prevLTP_scale_Q14) >> 14);
            }
            if (psDec.prev_sigtype == 1) {
                int[] invGain_Q30_ptr = new int[1];
                LPCInvPredGain.SKP_Silk_LPC_inverse_pred_gain(invGain_Q30_ptr, psPLC.prevLPC_Q12, psDec.LPC_order);
                rand_Gain_Q15 = Macros.SKP_SMULWB(Math.max(4194304, Math.min(134217728, invGain_Q30_ptr[0])) << 3, rand_Gain_Q15) >> 14;
            }
        }
        int rand_seed = psPLC.rand_seed;
        int lag = SigProcFIX.SKP_RSHIFT_ROUND(psPLC.pitchL_Q8, 8);
        psDec.sLTP_buf_idx = psDec.frame_length;
        int[] sig_Q10_ptr = sig_Q10;
        int sig_Q10_ptr_offset = 0;
        for (k = 0; k < 4; k++) {
            int[] pred_lag_ptr = psDec.sLTP_Q16;
            int pred_lag_ptr_offset = (psDec.sLTP_buf_idx - lag) + 2;
            for (i = 0; i < psDec.subfr_length; i++) {
                rand_seed = SigProcFIX.SKP_RAND(rand_seed);
                pred_lag_ptr_offset++;
                int LPC_exc_Q10 = (Macros.SKP_SMULWB(rand_ptr[rand_ptr_offset + ((rand_seed >> 25) & 127)], rand_scale_Q14) << 2) + SigProcFIX.SKP_RSHIFT_ROUND(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMULWB(pred_lag_ptr[pred_lag_ptr_offset + 0], B_Q14[0]), pred_lag_ptr[pred_lag_ptr_offset - 1], B_Q14[1]), pred_lag_ptr[pred_lag_ptr_offset - 2], B_Q14[2]), pred_lag_ptr[pred_lag_ptr_offset - 3], B_Q14[3]), pred_lag_ptr[pred_lag_ptr_offset - 4], B_Q14[4]), 4);
                psDec.sLTP_Q16[psDec.sLTP_buf_idx] = LPC_exc_Q10 << 6;
                psDec.sLTP_buf_idx++;
                sig_Q10_ptr[sig_Q10_ptr_offset + i] = LPC_exc_Q10;
            }
            sig_Q10_ptr_offset += psDec.subfr_length;
            for (j = 0; j < 5; j++) {
                B_Q14[j] = (short) (Macros.SKP_SMULBB(harm_Gain_Q15, B_Q14[j]) >> 15);
            }
            rand_scale_Q14 = (short) (Macros.SKP_SMULBB(rand_scale_Q14, rand_Gain_Q15) >> 15);
            psPLC.pitchL_Q8 += Macros.SKP_SMULWB(psPLC.pitchL_Q8, PITCH_DRIFT_FAC_Q16);
            psPLC.pitchL_Q8 = Math.min(psPLC.pitchL_Q8, Macros.SKP_SMULBB(18, psDec.fs_kHz) << 8);
            lag = SigProcFIX.SKP_RSHIFT_ROUND(psPLC.pitchL_Q8, 8);
        }
        sig_Q10_ptr = sig_Q10;
        sig_Q10_ptr_offset = 0;
        System.arraycopy(psPLC.prevLPC_Q12, 0, A_Q12_tmp, 0, psDec.LPC_order);
        Typedef.SKP_assert(psDec.LPC_order >= 10);
        for (k = 0; k < 4; k++) {
            for (i = 0; i < psDec.subfr_length; i++) {
                int LPC_pred_Q10 = Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMULWB(psDec.sLPC_Q14[(i + 16) - 1], A_Q12_tmp[0]), psDec.sLPC_Q14[(i + 16) - 2], A_Q12_tmp[1]), psDec.sLPC_Q14[(i + 16) - 3], A_Q12_tmp[2]), psDec.sLPC_Q14[(i + 16) - 4], A_Q12_tmp[3]), psDec.sLPC_Q14[(i + 16) - 5], A_Q12_tmp[4]), psDec.sLPC_Q14[(i + 16) - 6], A_Q12_tmp[5]), psDec.sLPC_Q14[(i + 16) - 7], A_Q12_tmp[6]), psDec.sLPC_Q14[(i + 16) - 8], A_Q12_tmp[7]), psDec.sLPC_Q14[(i + 16) - 9], A_Q12_tmp[8]), psDec.sLPC_Q14[(i + 16) - 10], A_Q12_tmp[9]);
                for (j = 10; j < psDec.LPC_order; j++) {
                    LPC_pred_Q10 = Macros.SKP_SMLAWB(LPC_pred_Q10, psDec.sLPC_Q14[((i + 16) - j) - 1], A_Q12_tmp[j]);
                }
                sig_Q10_ptr_offset = i;
                sig_Q10_ptr[i] = sig_Q10_ptr[sig_Q10_ptr_offset + i] + LPC_pred_Q10;
                psDec.sLPC_Q14[i + 16] = sig_Q10_ptr[sig_Q10_ptr_offset + i] << 4;
            }
            sig_Q10_ptr_offset += psDec.subfr_length;
            System.arraycopy(psDec.sLPC_Q14, psDec.subfr_length, psDec.sLPC_Q14, 0, 16);
        }
        for (i = 0; i < psDec.frame_length; i++) {
            signal[signal_offset + i] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(Macros.SKP_SMULWW(sig_Q10[i], psPLC.prevGain_Q16[3]), 10));
        }
        psPLC.rand_seed = rand_seed;
        psPLC.randScale_Q14 = rand_scale_Q14;
        for (i = 0; i < 4; i++) {
            psDecCtrl.pitchL[i] = lag;
        }
    }

    static void SKP_Silk_PLC_glue_frames(SKP_Silk_decoder_state psDec, SKP_Silk_decoder_control psDecCtrl, short[] signal, int signal_offset, int length) {
        int[] energy_ptr = new int[1];
        int[] energy_shift_ptr = new int[1];
        SKP_Silk_PLC_struct psPLC = psDec.sPLC;
        if (psDec.lossCnt != 0) {
            int[] conc_energy_ptr = new int[1];
            int[] conc_energy_shift_ptr = new int[1];
            SumSqrShift.SKP_Silk_sum_sqr_shift(conc_energy_ptr, conc_energy_shift_ptr, signal, signal_offset, length);
            psPLC.conc_energy = conc_energy_ptr[0];
            psPLC.conc_energy_shift = conc_energy_shift_ptr[0];
            psPLC.last_frame_lost = 1;
            return;
        }
        if (psDec.sPLC.last_frame_lost != 0) {
            SumSqrShift.SKP_Silk_sum_sqr_shift(energy_ptr, energy_shift_ptr, signal, signal_offset, length);
            int energy = energy_ptr[0];
            int energy_shift = energy_shift_ptr[0];
            if (energy_shift > psPLC.conc_energy_shift) {
                psPLC.conc_energy >>= energy_shift - psPLC.conc_energy_shift;
            } else if (energy_shift < psPLC.conc_energy_shift) {
                energy >>= psPLC.conc_energy_shift - energy_shift;
            }
            if (energy > psPLC.conc_energy) {
                int LZ = Macros.SKP_Silk_CLZ32(psPLC.conc_energy) - 1;
                psPLC.conc_energy <<= LZ;
                int gain_Q12 = Inlines.SKP_Silk_SQRT_APPROX(psPLC.conc_energy / Math.max(energy >> Math.max(24 - LZ, 0), 1));
                int slope_Q12 = 4096 - (gain_Q12 / length);
                for (int i = 0; i < length; i++) {
                    signal[signal_offset + i] = (short) ((signal[signal_offset + i] * gain_Q12) >> 12);
                    gain_Q12 = Math.min(gain_Q12 + slope_Q12, 4096);
                }
            }
        }
        psPLC.last_frame_lost = 0;
    }
}
