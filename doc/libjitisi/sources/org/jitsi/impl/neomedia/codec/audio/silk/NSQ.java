package org.jitsi.impl.neomedia.codec.audio.silk;

import com.sun.media.format.WavAudioFormat;
import java.util.Arrays;
import javax.media.Buffer;

public class NSQ {
    static final /* synthetic */ boolean $assertionsDisabled = (!NSQ.class.desiredAssertionStatus());

    static void SKP_Silk_NSQ(SKP_Silk_encoder_state psEncC, SKP_Silk_encoder_control psEncCtrlC, SKP_Silk_nsq_state NSQ, short[] x, byte[] q, int LSFInterpFactor_Q2, short[] PredCoef_Q12, short[] LTPCoef_Q14, short[] AR2_Q13, int[] HarmShapeGain_Q14, int[] Tilt_Q14, int[] LF_shp_Q14, int[] Gains_Q16, int Lambda_Q10, int LTP_scale_Q14) {
        int[] sLTP_Q16 = new int[960];
        short[] sLTP = new short[960];
        int[] FiltState = new int[16];
        int[] x_sc_Q10 = new int[WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18];
        int subfr_length = psEncC.frame_length / 4;
        NSQ.rand_seed = psEncCtrlC.Seed;
        int lag = NSQ.lagPrev;
        if ($assertionsDisabled || NSQ.prev_inv_gain_Q16 != 0) {
            int LSF_interpolation_flag;
            int offset_Q10 = TablesOther.SKP_Silk_Quantization_Offsets_Q10[psEncCtrlC.sigtype][psEncCtrlC.QuantOffsetType];
            if (LSFInterpFactor_Q2 == 4) {
                LSF_interpolation_flag = 0;
            } else {
                LSF_interpolation_flag = 1;
            }
            NSQ.sLTP_shp_buf_idx = psEncC.frame_length;
            NSQ.sLTP_buf_idx = psEncC.frame_length;
            short[] pxq = NSQ.xq;
            int pxq_offset = psEncC.frame_length;
            short[] x_tmp = (short[]) x.clone();
            int x_tmp_offset = 0;
            Object q_tmp = (byte[]) q.clone();
            int q_tmp_offset = 0;
            int k = 0;
            while (k < 4) {
                short[] A_Q12 = PredCoef_Q12;
                int A_Q12_offset = ((k >> 1) | (1 - LSF_interpolation_flag)) * 16;
                short[] B_Q14 = LTPCoef_Q14;
                int B_Q14_offset = k * 5;
                short[] AR_shp_Q13 = AR2_Q13;
                int AR_shp_Q13_offset = k * 16;
                if ($assertionsDisabled || HarmShapeGain_Q14[k] >= 0) {
                    int HarmShapeFIRPacked_Q14 = (HarmShapeGain_Q14[k] >> 2) | ((HarmShapeGain_Q14[k] >> 1) << 16);
                    if (psEncCtrlC.sigtype == 0) {
                        lag = psEncCtrlC.pitchL[k];
                        NSQ.rewhite_flag = 0;
                        if (((3 - (LSF_interpolation_flag << 1)) & k) == 0) {
                            int start_idx = SigProcFIX.SKP_LIMIT_int(((psEncC.frame_length - lag) - psEncC.predictLPCOrder) - 2, 0, psEncC.frame_length - psEncC.predictLPCOrder);
                            Arrays.fill(FiltState, 0, psEncC.predictLPCOrder, 0);
                            MA.SKP_Silk_MA_Prediction(NSQ.xq, ((psEncC.frame_length >> 2) * k) + start_idx, A_Q12, A_Q12_offset, FiltState, sLTP, start_idx, psEncC.frame_length - start_idx, psEncC.predictLPCOrder);
                            NSQ.rewhite_flag = 1;
                            NSQ.sLTP_buf_idx = psEncC.frame_length;
                        }
                    }
                    SKP_Silk_nsq_scale_states(NSQ, x_tmp, x_tmp_offset, x_sc_Q10, psEncC.subfr_length, sLTP, sLTP_Q16, k, LTP_scale_Q14, Gains_Q16, psEncCtrlC.pitchL);
                    SKP_Silk_noise_shape_quantizer(NSQ, psEncCtrlC.sigtype, x_sc_Q10, q_tmp, q_tmp_offset, pxq, pxq_offset, sLTP_Q16, A_Q12, A_Q12_offset, B_Q14, B_Q14_offset, AR_shp_Q13, AR_shp_Q13_offset, lag, HarmShapeFIRPacked_Q14, Tilt_Q14[k], LF_shp_Q14[k], Gains_Q16[k], Lambda_Q10, offset_Q10, psEncC.subfr_length, psEncC.shapingLPCOrder, psEncC.predictLPCOrder);
                    x_tmp_offset += psEncC.subfr_length;
                    q_tmp_offset += psEncC.subfr_length;
                    pxq_offset += psEncC.subfr_length;
                    k++;
                } else {
                    throw new AssertionError();
                }
            }
            NSQ.sLF_AR_shp_Q12 = NSQ.sLF_AR_shp_Q12;
            NSQ.prev_inv_gain_Q16 = NSQ.prev_inv_gain_Q16;
            NSQ.lagPrev = psEncCtrlC.pitchL[3];
            System.arraycopy(NSQ.xq, psEncC.frame_length, NSQ.xq, 0, psEncC.frame_length);
            System.arraycopy(NSQ.sLTP_shp_Q10, psEncC.frame_length, NSQ.sLTP_shp_Q10, 0, psEncC.frame_length);
            System.arraycopy(q_tmp, 0, q, 0, q.length);
            return;
        }
        throw new AssertionError();
    }

    static void SKP_Silk_noise_shape_quantizer(SKP_Silk_nsq_state NSQ, int sigtype, int[] x_sc_Q10, byte[] q, int q_offset, short[] xq, int xq_offset, int[] sLTP_Q16, short[] a_Q12, int a_Q12_offset, short[] b_Q14, int b_Q14_offset, short[] AR_shp_Q13, int AR_shp_Q13_offset, int lag, int HarmShapeFIRPacked_Q14, int Tilt_Q14, int LF_shp_Q14, int Gain_Q16, int Lambda_Q10, int offset_Q10, int length, int shapingLPCOrder, int predictLPCOrder) {
        int[] shp_lag_ptr = NSQ.sLTP_shp_Q10;
        int shp_lag_ptr_offset = (NSQ.sLTP_shp_buf_idx - lag) + 1;
        int[] pred_lag_ptr = sLTP_Q16;
        int pred_lag_ptr_offset = (NSQ.sLTP_buf_idx - lag) + 2;
        int[] psLPC_Q14 = NSQ.sLPC_Q14;
        int psLPC_Q14_offset = 15;
        int thr1_Q10 = -1536 - (Lambda_Q10 >> 1);
        int thr2_Q10 = (-512 - (Lambda_Q10 >> 1)) + (Macros.SKP_SMULBB(offset_Q10, Lambda_Q10) >> 10);
        int thr3_Q10 = (Lambda_Q10 >> 1) + 512;
        int i = 0;
        while (i < length) {
            NSQ.rand_seed = SigProcFIX.SKP_RAND(NSQ.rand_seed);
            int dither = NSQ.rand_seed >> 31;
            if (!$assertionsDisabled && (predictLPCOrder & 1) != 0) {
                throw new AssertionError();
            } else if ($assertionsDisabled || predictLPCOrder >= 10) {
                int j;
                int LTP_pred_Q14;
                int LPC_pred_Q10 = Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMULWB(psLPC_Q14[psLPC_Q14_offset + 0], a_Q12[a_Q12_offset + 0]), psLPC_Q14[psLPC_Q14_offset - 1], a_Q12[a_Q12_offset + 1]), psLPC_Q14[psLPC_Q14_offset - 2], a_Q12[a_Q12_offset + 2]), psLPC_Q14[psLPC_Q14_offset - 3], a_Q12[a_Q12_offset + 3]), psLPC_Q14[psLPC_Q14_offset - 4], a_Q12[a_Q12_offset + 4]), psLPC_Q14[psLPC_Q14_offset - 5], a_Q12[a_Q12_offset + 5]), psLPC_Q14[psLPC_Q14_offset - 6], a_Q12[a_Q12_offset + 6]), psLPC_Q14[psLPC_Q14_offset - 7], a_Q12[a_Q12_offset + 7]), psLPC_Q14[psLPC_Q14_offset - 8], a_Q12[a_Q12_offset + 8]), psLPC_Q14[psLPC_Q14_offset - 9], a_Q12[a_Q12_offset + 9]);
                for (j = 10; j < predictLPCOrder; j++) {
                    LPC_pred_Q10 = Macros.SKP_SMLAWB(LPC_pred_Q10, psLPC_Q14[psLPC_Q14_offset - j], a_Q12[a_Q12_offset + j]);
                }
                if (sigtype == 0) {
                    LTP_pred_Q14 = Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMULWB(pred_lag_ptr[pred_lag_ptr_offset + 0], b_Q14[b_Q14_offset + 0]), pred_lag_ptr[pred_lag_ptr_offset - 1], b_Q14[b_Q14_offset + 1]), pred_lag_ptr[pred_lag_ptr_offset - 2], b_Q14[b_Q14_offset + 2]), pred_lag_ptr[pred_lag_ptr_offset - 3], b_Q14[b_Q14_offset + 3]), pred_lag_ptr[pred_lag_ptr_offset - 4], b_Q14[b_Q14_offset + 4]);
                    pred_lag_ptr_offset++;
                } else {
                    LTP_pred_Q14 = 0;
                }
                if (!$assertionsDisabled && (shapingLPCOrder & 1) != 0) {
                    throw new AssertionError();
                } else if ($assertionsDisabled || shapingLPCOrder >= 12) {
                    int n_AR_Q10 = Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMULWB(psLPC_Q14[psLPC_Q14_offset + 0], AR_shp_Q13[AR_shp_Q13_offset + 0]), psLPC_Q14[psLPC_Q14_offset - 1], AR_shp_Q13[AR_shp_Q13_offset + 1]), psLPC_Q14[psLPC_Q14_offset - 2], AR_shp_Q13[AR_shp_Q13_offset + 2]), psLPC_Q14[psLPC_Q14_offset - 3], AR_shp_Q13[AR_shp_Q13_offset + 3]), psLPC_Q14[psLPC_Q14_offset - 4], AR_shp_Q13[AR_shp_Q13_offset + 4]), psLPC_Q14[psLPC_Q14_offset - 5], AR_shp_Q13[AR_shp_Q13_offset + 5]), psLPC_Q14[psLPC_Q14_offset - 6], AR_shp_Q13[AR_shp_Q13_offset + 6]), psLPC_Q14[psLPC_Q14_offset - 7], AR_shp_Q13[AR_shp_Q13_offset + 7]), psLPC_Q14[psLPC_Q14_offset - 8], AR_shp_Q13[AR_shp_Q13_offset + 8]), psLPC_Q14[psLPC_Q14_offset - 9], AR_shp_Q13[AR_shp_Q13_offset + 9]), psLPC_Q14[psLPC_Q14_offset - 10], AR_shp_Q13[AR_shp_Q13_offset + 10]), psLPC_Q14[psLPC_Q14_offset - 11], AR_shp_Q13[AR_shp_Q13_offset + 11]);
                    for (j = 12; j < shapingLPCOrder; j++) {
                        n_AR_Q10 = Macros.SKP_SMLAWB(n_AR_Q10, psLPC_Q14[psLPC_Q14_offset - j], AR_shp_Q13[AR_shp_Q13_offset + j]);
                    }
                    n_AR_Q10 = Macros.SKP_SMLAWB(n_AR_Q10 >> 1, NSQ.sLF_AR_shp_Q12, Tilt_Q14);
                    int n_LF_Q10 = Macros.SKP_SMLAWT(Macros.SKP_SMULWB(NSQ.sLTP_shp_Q10[NSQ.sLTP_shp_buf_idx - 1], LF_shp_Q14) << 2, NSQ.sLF_AR_shp_Q12, LF_shp_Q14);
                    if ($assertionsDisabled || lag > 0 || sigtype == 1) {
                        int n_LTP_Q14;
                        int q_Q0;
                        int q_Q10;
                        if (lag > 0) {
                            shp_lag_ptr_offset++;
                            n_LTP_Q14 = Macros.SKP_SMLAWT(Macros.SKP_SMULWB(shp_lag_ptr[shp_lag_ptr_offset + 0] + shp_lag_ptr[shp_lag_ptr_offset - 2], HarmShapeFIRPacked_Q14), shp_lag_ptr[shp_lag_ptr_offset - 1], HarmShapeFIRPacked_Q14) << 6;
                        } else {
                            n_LTP_Q14 = 0;
                        }
                        int r_Q10 = SigProcFIX.SKP_LIMIT_32((((x_sc_Q10[i] - (((SigProcFIX.SKP_RSHIFT_ROUND(LTP_pred_Q14 - n_LTP_Q14, 4) + LPC_pred_Q10) - n_AR_Q10) - n_LF_Q10)) ^ dither) - dither) - offset_Q10, -65536, Buffer.FLAG_SKIP_FEC);
                        if (r_Q10 < thr1_Q10) {
                            q_Q0 = SigProcFIX.SKP_RSHIFT_ROUND((Lambda_Q10 >> 1) + r_Q10, 10);
                            q_Q10 = q_Q0 << 10;
                        } else if (r_Q10 < thr2_Q10) {
                            q_Q0 = -1;
                            q_Q10 = -1024;
                        } else if (r_Q10 > thr3_Q10) {
                            q_Q0 = SigProcFIX.SKP_RSHIFT_ROUND(r_Q10 - (Lambda_Q10 >> 1), 10);
                            q_Q10 = q_Q0 << 10;
                        } else {
                            q_Q0 = 0;
                            q_Q10 = 0;
                        }
                        q[q_offset + i] = (byte) q_Q0;
                        int LPC_exc_Q10 = (((q_Q10 + offset_Q10) ^ dither) - dither) + SigProcFIX.SKP_RSHIFT_ROUND(LTP_pred_Q14, 4);
                        int xq_Q10 = LPC_exc_Q10 + LPC_pred_Q10;
                        xq[xq_offset + i] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(Macros.SKP_SMULWW(xq_Q10, Gain_Q16), 10));
                        psLPC_Q14_offset++;
                        psLPC_Q14[psLPC_Q14_offset] = xq_Q10 << 4;
                        int sLF_AR_shp_Q10 = xq_Q10 - n_AR_Q10;
                        NSQ.sLF_AR_shp_Q12 = sLF_AR_shp_Q10 << 2;
                        NSQ.sLTP_shp_Q10[NSQ.sLTP_shp_buf_idx] = sLF_AR_shp_Q10 - n_LF_Q10;
                        sLTP_Q16[NSQ.sLTP_buf_idx] = LPC_exc_Q10 << 6;
                        NSQ.sLTP_shp_buf_idx++;
                        NSQ.sLTP_buf_idx++;
                        NSQ.rand_seed += q[q_offset + i];
                        i++;
                    } else {
                        throw new AssertionError();
                    }
                } else {
                    throw new AssertionError();
                }
            } else {
                throw new AssertionError();
            }
        }
        System.arraycopy(NSQ.sLPC_Q14, length, NSQ.sLPC_Q14, 0, 16);
    }

    static void SKP_Silk_nsq_scale_states(SKP_Silk_nsq_state NSQ, short[] x, int x_offset, int[] x_sc_Q10, int length, short[] sLTP, int[] sLTP_Q16, int subfr, int LTP_scale_Q14, int[] Gains_Q16, int[] pitchL) {
        int i;
        int inv_gain_Q16 = Integer.MAX_VALUE / (Gains_Q16[subfr] >> 1);
        if (inv_gain_Q16 >= 32767) {
            inv_gain_Q16 = 32767;
        }
        int lag = pitchL[subfr];
        if (NSQ.rewhite_flag != 0) {
            int inv_gain_Q32 = inv_gain_Q16 << 16;
            if (subfr == 0) {
                inv_gain_Q32 = Macros.SKP_SMULWB(inv_gain_Q32, LTP_scale_Q14) << 2;
            }
            for (i = (NSQ.sLTP_buf_idx - lag) - 2; i < NSQ.sLTP_buf_idx; i++) {
                sLTP_Q16[i] = Macros.SKP_SMULWB(inv_gain_Q32, sLTP[i]);
            }
        }
        int scale_length = SigProcFIX.SKP_max_int((length * 4) - Macros.SKP_SMULBB(4 - (subfr + 1), length), lag + 5);
        if (inv_gain_Q16 != NSQ.prev_inv_gain_Q16) {
            int gain_adj_Q16 = Inlines.SKP_DIV32_varQ(inv_gain_Q16, NSQ.prev_inv_gain_Q16, 16);
            for (i = NSQ.sLTP_shp_buf_idx - scale_length; i < NSQ.sLTP_shp_buf_idx; i++) {
                NSQ.sLTP_shp_Q10[i] = Macros.SKP_SMULWW(gain_adj_Q16, NSQ.sLTP_shp_Q10[i]);
            }
            if (NSQ.rewhite_flag == 0) {
                for (i = (NSQ.sLTP_buf_idx - lag) - 2; i < NSQ.sLTP_buf_idx; i++) {
                    sLTP_Q16[i] = Macros.SKP_SMULWW(gain_adj_Q16, sLTP_Q16[i]);
                }
            }
            NSQ.sLF_AR_shp_Q12 = Macros.SKP_SMULWW(gain_adj_Q16, NSQ.sLF_AR_shp_Q12);
            for (i = 0; i < 16; i++) {
                NSQ.sLPC_Q14[i] = Macros.SKP_SMULWW(gain_adj_Q16, NSQ.sLPC_Q14[i]);
            }
        }
        for (i = 0; i < length; i++) {
            x_sc_Q10[i] = Macros.SKP_SMULBB(x[x_offset + i], (short) inv_gain_Q16) >> 6;
        }
        if ($assertionsDisabled || inv_gain_Q16 != 0) {
            NSQ.prev_inv_gain_Q16 = inv_gain_Q16;
            return;
        }
        throw new AssertionError();
    }
}
