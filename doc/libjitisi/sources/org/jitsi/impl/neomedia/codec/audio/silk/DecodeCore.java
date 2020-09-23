package org.jitsi.impl.neomedia.codec.audio.silk;

import com.sun.media.format.WavAudioFormat;
import java.util.Arrays;
import javax.media.Buffer;
import org.jitsi.impl.neomedia.device.DeviceConfiguration;

public class DecodeCore {
    static void SKP_Silk_decode_core(SKP_Silk_decoder_state psDec, SKP_Silk_decoder_control psDecCtrl, short[] xq, int xq_offset, int[] q) {
        int NLSF_interpolation_flag;
        int i;
        int lag = 0;
        short[] A_Q12_tmp = new short[16];
        short[] sLTP = new short[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
        int[] vec_Q10 = new int[WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18];
        int[] FiltState = new int[16];
        Typedef.SKP_assert(psDec.prev_inv_gain_Q16 != 0);
        int offset_Q10 = TablesOther.SKP_Silk_Quantization_Offsets_Q10[psDecCtrl.sigtype][psDecCtrl.QuantOffsetType];
        if (psDecCtrl.NLSFInterpCoef_Q2 < 4) {
            NLSF_interpolation_flag = 1;
        } else {
            NLSF_interpolation_flag = 0;
        }
        int rand_seed = psDecCtrl.Seed;
        for (i = 0; i < psDec.frame_length; i++) {
            rand_seed = SigProcFIX.SKP_RAND(rand_seed);
            int dither = rand_seed >> 31;
            psDec.exc_Q10[i] = (q[i] << 10) + offset_Q10;
            psDec.exc_Q10[i] = (psDec.exc_Q10[i] ^ dither) - dither;
            rand_seed += q[i];
        }
        Object pexc_Q10 = psDec.exc_Q10;
        int pexc_Q10_offset = 0;
        Object pres_Q10 = psDec.res_Q10;
        int pres_Q10_offset = 0;
        short[] pxq = psDec.outBuf;
        int pxq_offset = psDec.frame_length;
        psDec.sLTP_buf_idx = psDec.frame_length;
        int k = 0;
        while (k < 4) {
            short[] A_Q12 = psDecCtrl.PredCoef_Q12[k >> 1];
            System.arraycopy(A_Q12, 0, A_Q12_tmp, 0, psDec.LPC_order);
            short[] B_Q14 = psDecCtrl.LTPCoef_Q14;
            int B_Q14_offset = k * 5;
            int Gain_Q16 = psDecCtrl.Gains_Q16[k];
            int LTP_scale_Q14 = psDecCtrl.LTP_scale_Q14;
            int sigtype = psDecCtrl.sigtype;
            int inv_gain_Q16 = Math.min(Integer.MAX_VALUE / (Gain_Q16 >> 1), 32767);
            int gain_adj_Q16 = Buffer.FLAG_SKIP_FEC;
            if (inv_gain_Q16 != psDec.prev_inv_gain_Q16) {
                gain_adj_Q16 = Inlines.SKP_DIV32_varQ(inv_gain_Q16, psDec.prev_inv_gain_Q16, 16);
            }
            if (psDec.lossCnt != 0 && psDec.prev_sigtype == 0 && psDecCtrl.sigtype == 1 && k < 2) {
                Arrays.fill(B_Q14, B_Q14_offset, B_Q14_offset + 5, (short) 0);
                B_Q14[B_Q14_offset + 2] = (short) 4096;
                sigtype = 0;
                psDecCtrl.pitchL[k] = psDec.lagPrev;
            }
            if (sigtype == 0) {
                lag = psDecCtrl.pitchL[k];
                if (((3 - (NLSF_interpolation_flag << 1)) & k) == 0) {
                    int start_idx = SigProcFIX.SKP_LIMIT_int(((psDec.frame_length - lag) - psDec.LPC_order) - 2, 0, psDec.frame_length - psDec.LPC_order);
                    Arrays.fill(FiltState, 0, psDec.LPC_order, 0);
                    MA.SKP_Silk_MA_Prediction(psDec.outBuf, ((psDec.frame_length >> 2) * k) + start_idx, A_Q12, 0, FiltState, sLTP, start_idx, psDec.frame_length - start_idx, psDec.LPC_order);
                    int inv_gain_Q32 = inv_gain_Q16 << 16;
                    if (k == 0) {
                        inv_gain_Q32 = Macros.SKP_SMULWB(inv_gain_Q32, psDecCtrl.LTP_scale_Q14) << 2;
                    }
                    for (i = 0; i < lag + 2; i++) {
                        psDec.sLTP_Q16[(psDec.sLTP_buf_idx - i) - 1] = Macros.SKP_SMULWB(inv_gain_Q32, sLTP[(psDec.frame_length - i) - 1]);
                    }
                } else if (gain_adj_Q16 != 65536) {
                    for (i = 0; i < lag + 2; i++) {
                        psDec.sLTP_Q16[(psDec.sLTP_buf_idx - i) - 1] = Macros.SKP_SMULWW(gain_adj_Q16, psDec.sLTP_Q16[(psDec.sLTP_buf_idx - i) - 1]);
                    }
                }
            }
            for (i = 0; i < 16; i++) {
                psDec.sLPC_Q14[i] = Macros.SKP_SMULWW(gain_adj_Q16, psDec.sLPC_Q14[i]);
            }
            Typedef.SKP_assert(inv_gain_Q16 != 0);
            psDec.prev_inv_gain_Q16 = inv_gain_Q16;
            if (sigtype == 0) {
                int[] pred_lag_ptr = psDec.sLTP_Q16;
                int pred_lag_ptr_offset = (psDec.sLTP_buf_idx - lag) + 2;
                for (i = 0; i < psDec.subfr_length; i++) {
                    int LTP_pred_Q14 = Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMULWB(pred_lag_ptr[pred_lag_ptr_offset + 0], B_Q14[B_Q14_offset + 0]), pred_lag_ptr[pred_lag_ptr_offset - 1], B_Q14[B_Q14_offset + 1]), pred_lag_ptr[pred_lag_ptr_offset - 2], B_Q14[B_Q14_offset + 2]), pred_lag_ptr[pred_lag_ptr_offset - 3], B_Q14[B_Q14_offset + 3]), pred_lag_ptr[pred_lag_ptr_offset - 4], B_Q14[B_Q14_offset + 4]);
                    pred_lag_ptr_offset++;
                    pres_Q10[pres_Q10_offset + i] = pexc_Q10[pexc_Q10_offset + i] + SigProcFIX.SKP_RSHIFT_ROUND(LTP_pred_Q14, 4);
                    psDec.sLTP_Q16[psDec.sLTP_buf_idx] = pres_Q10[pres_Q10_offset + i] << 6;
                    psDec.sLTP_buf_idx++;
                }
            } else {
                System.arraycopy(pexc_Q10, pexc_Q10_offset + 0, pres_Q10, pres_Q10_offset + 0, psDec.subfr_length);
            }
            for (i = 0; i < psDec.subfr_length; i++) {
                int LPC_pred_Q10 = Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMULWB(psDec.sLPC_Q14[(i + 16) - 1], A_Q12_tmp[0]), psDec.sLPC_Q14[(i + 16) - 2], A_Q12_tmp[1]), psDec.sLPC_Q14[(i + 16) - 3], A_Q12_tmp[2]), psDec.sLPC_Q14[(i + 16) - 4], A_Q12_tmp[3]), psDec.sLPC_Q14[(i + 16) - 5], A_Q12_tmp[4]), psDec.sLPC_Q14[(i + 16) - 6], A_Q12_tmp[5]), psDec.sLPC_Q14[(i + 16) - 7], A_Q12_tmp[6]), psDec.sLPC_Q14[(i + 16) - 8], A_Q12_tmp[7]), psDec.sLPC_Q14[(i + 16) - 9], A_Q12_tmp[8]), psDec.sLPC_Q14[(i + 16) - 10], A_Q12_tmp[9]);
                for (int j = 10; j < psDec.LPC_order; j++) {
                    LPC_pred_Q10 = Macros.SKP_SMLAWB(LPC_pred_Q10, psDec.sLPC_Q14[((i + 16) - j) - 1], A_Q12_tmp[j]);
                }
                vec_Q10[i] = pres_Q10[pres_Q10_offset + i] + LPC_pred_Q10;
                psDec.sLPC_Q14[i + 16] = vec_Q10[i] << 4;
            }
            for (i = 0; i < psDec.subfr_length; i++) {
                pxq[pxq_offset + i] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(Macros.SKP_SMULWW(vec_Q10[i], Gain_Q16), 10));
            }
            System.arraycopy(psDec.sLPC_Q14, psDec.subfr_length, psDec.sLPC_Q14, 0, 16);
            pexc_Q10_offset += psDec.subfr_length;
            pres_Q10_offset += psDec.subfr_length;
            pxq_offset += psDec.subfr_length;
            k++;
        }
        System.arraycopy(psDec.outBuf, psDec.frame_length, xq, xq_offset + 0, psDec.frame_length);
    }
}
