package org.jitsi.impl.neomedia.codec.audio.silk;

import com.sun.media.format.WavAudioFormat;
import java.lang.reflect.Array;
import java.util.Arrays;
import javax.media.Buffer;
import org.jitsi.impl.neomedia.device.DeviceConfiguration;

public class NSQDelDec {
    static final /* synthetic */ boolean $assertionsDisabled = (!NSQDelDec.class.desiredAssertionStatus());

    static void SKP_Silk_NSQ_del_dec(SKP_Silk_encoder_state psEncC, SKP_Silk_encoder_control psEncCtrlC, SKP_Silk_nsq_state NSQ, short[] x, byte[] q, int LSFInterpFactor_Q2, short[] PredCoef_Q12, short[] LTPCoef_Q14, short[] AR2_Q13, int[] HarmShapeGain_Q14, int[] Tilt_Q14, int[] LF_shp_Q14, int[] Gains_Q16, int Lambda_Q10, int LTP_scale_Q14) {
        int[] sLTP_Q16 = new int[960];
        short[] sLTP = new short[960];
        int[] FiltState = new int[16];
        int[] x_sc_Q10 = new int[WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18];
        NSQDelDecStruct[] psDelDec = new NSQDelDecStruct[4];
        for (int psDelDecIni_i = 0; psDelDecIni_i < 4; psDelDecIni_i++) {
            psDelDec[psDelDecIni_i] = new NSQDelDecStruct();
        }
        int subfr_length = psEncC.frame_length / 4;
        int lag = NSQ.lagPrev;
        if ($assertionsDisabled || NSQ.prev_inv_gain_Q16 != 0) {
            int k;
            NSQDelDecStruct psDD;
            int decisionDelay;
            int LSF_interpolation_flag;
            int RDmin_Q10;
            int Winner_ind;
            int i;
            int last_smple_idx;
            short[] x_tmp = (short[]) x.clone();
            int x_tmp_offset = 0;
            byte[] q_tmp = (byte[]) q.clone();
            int q_tmp_offset = 0;
            for (int inx = 0; inx < psEncC.nStatesDelayedDecision; inx++) {
                psDelDec[inx].FieldsInit();
            }
            for (k = 0; k < psEncC.nStatesDelayedDecision; k++) {
                psDD = psDelDec[k];
                psDD.Seed = (psEncCtrlC.Seed + k) & 3;
                psDD.SeedInit = psDD.Seed;
                psDD.RD_Q10 = 0;
                psDD.LF_AR_Q12 = NSQ.sLF_AR_shp_Q12;
                psDD.Shape_Q10[0] = NSQ.sLTP_shp_Q10[psEncC.frame_length - 1];
                System.arraycopy(NSQ.sLPC_Q14, 0, psDD.sLPC_Q14, 0, Define.NSQ_LPC_BUF_LENGTH());
            }
            int offset_Q10 = TablesOther.SKP_Silk_Quantization_Offsets_Q10[psEncCtrlC.sigtype][psEncCtrlC.QuantOffsetType];
            int smpl_buf_idx = 0;
            if (32 < subfr_length) {
                decisionDelay = 32;
            } else {
                decisionDelay = subfr_length;
            }
            if (psEncCtrlC.sigtype == 0) {
                for (k = 0; k < 4; k++) {
                    if (decisionDelay >= (psEncCtrlC.pitchL[k] - 2) - 1) {
                        decisionDelay = (psEncCtrlC.pitchL[k] - 2) - 1;
                    }
                }
            }
            if (LSFInterpFactor_Q2 == 4) {
                LSF_interpolation_flag = 0;
            } else {
                LSF_interpolation_flag = 1;
            }
            short[] pxq = NSQ.xq;
            int pxq_offset = psEncC.frame_length;
            NSQ.sLTP_shp_buf_idx = psEncC.frame_length;
            NSQ.sLTP_buf_idx = psEncC.frame_length;
            int subfr = 0;
            k = 0;
            while (k < 4) {
                short[] A_Q12 = PredCoef_Q12;
                int A_Q12_offset = ((k >> 1) | (1 - LSF_interpolation_flag)) * 16;
                short[] B_Q14 = LTPCoef_Q14;
                int B_Q14_offset = k * 5;
                short[] AR_shp_Q13 = AR2_Q13;
                int AR_shp_Q13_offset = k * 16;
                NSQ.rewhite_flag = 0;
                if (psEncCtrlC.sigtype == 0) {
                    lag = psEncCtrlC.pitchL[k];
                    if (((3 - (LSF_interpolation_flag << 1)) & k) == 0) {
                        if (k == 2) {
                            RDmin_Q10 = psDelDec[0].RD_Q10;
                            Winner_ind = 0;
                            for (i = 1; i < psEncC.nStatesDelayedDecision; i++) {
                                if (psDelDec[i].RD_Q10 < RDmin_Q10) {
                                    RDmin_Q10 = psDelDec[i].RD_Q10;
                                    Winner_ind = i;
                                }
                            }
                            i = 0;
                            while (i < psEncC.nStatesDelayedDecision) {
                                if (i != Winner_ind) {
                                    NSQDelDecStruct nSQDelDecStruct = psDelDec[i];
                                    nSQDelDecStruct.RD_Q10 += 134217727;
                                    if (!$assertionsDisabled && psDelDec[i].RD_Q10 < 0) {
                                        throw new AssertionError();
                                    }
                                }
                                i++;
                            }
                            psDD = psDelDec[Winner_ind];
                            last_smple_idx = smpl_buf_idx + decisionDelay;
                            for (i = 0; i < decisionDelay; i++) {
                                last_smple_idx = (last_smple_idx - 1) & 31;
                                q_tmp[(q_tmp_offset + i) - decisionDelay] = (byte) (psDD.Q_Q10[last_smple_idx] >> 10);
                                pxq[(pxq_offset + i) - decisionDelay] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(Macros.SKP_SMULWW(psDD.Xq_Q10[last_smple_idx], psDD.Gain_Q16[last_smple_idx]), 10));
                                NSQ.sLTP_shp_Q10[(NSQ.sLTP_shp_buf_idx - decisionDelay) + i] = psDD.Shape_Q10[last_smple_idx];
                            }
                            subfr = 0;
                        }
                        int start_idx = SigProcFIX.SKP_LIMIT_int(((psEncC.frame_length - lag) - psEncC.predictLPCOrder) - 2, 0, psEncC.frame_length - psEncC.predictLPCOrder);
                        Arrays.fill(FiltState, 0, psEncC.predictLPCOrder, 0);
                        MA.SKP_Silk_MA_Prediction(NSQ.xq, start_idx + (psEncC.subfr_length * k), A_Q12, A_Q12_offset, FiltState, sLTP, start_idx, psEncC.frame_length - start_idx, psEncC.predictLPCOrder);
                        NSQ.sLTP_buf_idx = psEncC.frame_length;
                        NSQ.rewhite_flag = 1;
                    }
                }
                if ($assertionsDisabled || HarmShapeGain_Q14[k] >= 0) {
                    int HarmShapeFIRPacked_Q14 = (HarmShapeGain_Q14[k] >> 2) | ((HarmShapeGain_Q14[k] >> 1) << 16);
                    SKP_Silk_nsq_del_dec_scale_states(NSQ, psDelDec, x_tmp, x_tmp_offset, x_sc_Q10, subfr_length, sLTP, sLTP_Q16, k, psEncC.nStatesDelayedDecision, smpl_buf_idx, LTP_scale_Q14, Gains_Q16, psEncCtrlC.pitchL);
                    int[] smpl_buf_idx_ptr = new int[]{smpl_buf_idx};
                    int subfr2 = subfr + 1;
                    SKP_Silk_noise_shape_quantizer_del_dec(NSQ, psDelDec, psEncCtrlC.sigtype, x_sc_Q10, q_tmp, q_tmp_offset, pxq, pxq_offset, sLTP_Q16, A_Q12, A_Q12_offset, B_Q14, B_Q14_offset, AR_shp_Q13, AR_shp_Q13_offset, lag, HarmShapeFIRPacked_Q14, Tilt_Q14[k], LF_shp_Q14[k], Gains_Q16[k], Lambda_Q10, offset_Q10, psEncC.subfr_length, subfr, psEncC.shapingLPCOrder, psEncC.predictLPCOrder, psEncC.nStatesDelayedDecision, smpl_buf_idx_ptr, decisionDelay);
                    smpl_buf_idx = smpl_buf_idx_ptr[0];
                    x_tmp_offset += psEncC.subfr_length;
                    q_tmp_offset += psEncC.subfr_length;
                    pxq_offset += psEncC.subfr_length;
                    k++;
                    subfr = subfr2;
                } else {
                    throw new AssertionError();
                }
            }
            RDmin_Q10 = psDelDec[0].RD_Q10;
            Winner_ind = 0;
            for (k = 1; k < psEncC.nStatesDelayedDecision; k++) {
                if (psDelDec[k].RD_Q10 < RDmin_Q10) {
                    RDmin_Q10 = psDelDec[k].RD_Q10;
                    Winner_ind = k;
                }
            }
            psDD = psDelDec[Winner_ind];
            psEncCtrlC.Seed = psDD.SeedInit;
            last_smple_idx = smpl_buf_idx + decisionDelay;
            for (i = 0; i < decisionDelay; i++) {
                last_smple_idx = (last_smple_idx - 1) & 31;
                q_tmp[(q_tmp_offset + i) - decisionDelay] = (byte) (psDD.Q_Q10[last_smple_idx] >> 10);
                pxq[(pxq_offset + i) - decisionDelay] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(Macros.SKP_SMULWW(psDD.Xq_Q10[last_smple_idx], psDD.Gain_Q16[last_smple_idx]), 10));
                NSQ.sLTP_shp_Q10[(NSQ.sLTP_shp_buf_idx - decisionDelay) + i] = psDD.Shape_Q10[last_smple_idx];
                sLTP_Q16[(NSQ.sLTP_buf_idx - decisionDelay) + i] = psDD.Pred_Q16[last_smple_idx];
            }
            System.arraycopy(psDD.sLPC_Q14, psEncC.subfr_length, NSQ.sLPC_Q14, 0, Define.NSQ_LPC_BUF_LENGTH());
            NSQ.sLF_AR_shp_Q12 = psDD.LF_AR_Q12;
            NSQ.prev_inv_gain_Q16 = NSQ.prev_inv_gain_Q16;
            NSQ.lagPrev = psEncCtrlC.pitchL[3];
            System.arraycopy(NSQ.xq, psEncC.frame_length, NSQ.xq, 0, psEncC.frame_length);
            System.arraycopy(NSQ.sLTP_shp_Q10, psEncC.frame_length, NSQ.sLTP_shp_Q10, 0, psEncC.frame_length);
            System.arraycopy(q_tmp, 0, q, 0, q.length);
            return;
        }
        throw new AssertionError();
    }

    static void SKP_Silk_noise_shape_quantizer_del_dec(SKP_Silk_nsq_state NSQ, NSQDelDecStruct[] psDelDec, int sigtype, int[] x_Q10, byte[] q, int q_offset, short[] xq, int xq_offset, int[] sLTP_Q16, short[] a_Q12, int a_Q12_offset, short[] b_Q14, int b_Q14_offset, short[] AR_shp_Q13, int AR_shp_Q13_offset, int lag, int HarmShapeFIRPacked_Q14, int Tilt_Q14, int LF_shp_Q14, int Gain_Q16, int Lambda_Q10, int offset_Q10, int length, int subfr, int shapingLPCOrder, int predictLPCOrder, int nStatesDelayedDecision, int[] smpl_buf_idx, int decisionDelay) {
        int k;
        NSQDelDecStruct psDD;
        NSQ_sample_struct[][] psSampleState = (NSQ_sample_struct[][]) Array.newInstance(NSQ_sample_struct.class, new int[]{4, 2});
        for (int Ini_i = 0; Ini_i < 4; Ini_i++) {
            for (int Ini_j = 0; Ini_j < 2; Ini_j++) {
                psSampleState[Ini_i][Ini_j] = new NSQ_sample_struct();
            }
        }
        int[] shp_lag_ptr = NSQ.sLTP_shp_Q10;
        int shp_lag_ptr_offset = (NSQ.sLTP_shp_buf_idx - lag) + 1;
        int[] pred_lag_ptr = sLTP_Q16;
        int pred_lag_ptr_offset = (NSQ.sLTP_buf_idx - lag) + 2;
        int i = 0;
        while (i < length) {
            int LTP_pred_Q14;
            int n_LTP_Q14;
            NSQ_sample_struct[] psSS;
            if (sigtype == 0) {
                LTP_pred_Q14 = Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMULWB(pred_lag_ptr[pred_lag_ptr_offset + 0], b_Q14[b_Q14_offset + 0]), pred_lag_ptr[pred_lag_ptr_offset - 1], b_Q14[b_Q14_offset + 1]), pred_lag_ptr[pred_lag_ptr_offset - 2], b_Q14[b_Q14_offset + 2]), pred_lag_ptr[pred_lag_ptr_offset - 3], b_Q14[b_Q14_offset + 3]), pred_lag_ptr[pred_lag_ptr_offset - 4], b_Q14[b_Q14_offset + 4]);
                pred_lag_ptr_offset++;
            } else {
                LTP_pred_Q14 = 0;
            }
            if (lag > 0) {
                n_LTP_Q14 = Macros.SKP_SMLAWT(Macros.SKP_SMULWB(shp_lag_ptr[shp_lag_ptr_offset + 0] + shp_lag_ptr[shp_lag_ptr_offset - 2], HarmShapeFIRPacked_Q14), shp_lag_ptr[shp_lag_ptr_offset - 1], HarmShapeFIRPacked_Q14) << 6;
                shp_lag_ptr_offset++;
            } else {
                n_LTP_Q14 = 0;
            }
            k = 0;
            while (k < nStatesDelayedDecision) {
                psDD = psDelDec[k];
                psSS = psSampleState[k];
                psDD.Seed = SigProcFIX.SKP_RAND(psDD.Seed);
                int dither = psDD.Seed >> 31;
                int[] psLPC_Q14 = psDD.sLPC_Q14;
                int psLPC_Q14_offset = (Define.NSQ_LPC_BUF_LENGTH() - 1) + i;
                if (!$assertionsDisabled && predictLPCOrder < 10) {
                    throw new AssertionError();
                } else if ($assertionsDisabled || (predictLPCOrder & 1) == 0) {
                    int j;
                    int LPC_pred_Q10 = Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMULWB(psLPC_Q14[psLPC_Q14_offset + 0], a_Q12[a_Q12_offset + 0]), psLPC_Q14[psLPC_Q14_offset - 1], a_Q12[a_Q12_offset + 1]), psLPC_Q14[psLPC_Q14_offset - 2], a_Q12[a_Q12_offset + 2]), psLPC_Q14[psLPC_Q14_offset - 3], a_Q12[a_Q12_offset + 3]), psLPC_Q14[psLPC_Q14_offset - 4], a_Q12[a_Q12_offset + 4]), psLPC_Q14[psLPC_Q14_offset - 5], a_Q12[a_Q12_offset + 5]), psLPC_Q14[psLPC_Q14_offset - 6], a_Q12[a_Q12_offset + 6]), psLPC_Q14[psLPC_Q14_offset - 7], a_Q12[a_Q12_offset + 7]), psLPC_Q14[psLPC_Q14_offset - 8], a_Q12[a_Q12_offset + 8]), psLPC_Q14[psLPC_Q14_offset - 9], a_Q12[a_Q12_offset + 9]);
                    for (j = 10; j < predictLPCOrder; j++) {
                        LPC_pred_Q10 = Macros.SKP_SMLAWB(LPC_pred_Q10, psLPC_Q14[psLPC_Q14_offset - j], a_Q12[a_Q12_offset + j]);
                    }
                    if (!$assertionsDisabled && (shapingLPCOrder & 1) != 0) {
                        throw new AssertionError();
                    } else if ($assertionsDisabled || shapingLPCOrder >= 12) {
                        int q1_Q10;
                        int rd1_Q10;
                        int rd2_Q10;
                        int q2_Q10;
                        int n_AR_Q10 = Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMULWB(psLPC_Q14[psLPC_Q14_offset + 0], AR_shp_Q13[AR_shp_Q13_offset + 0]), psLPC_Q14[psLPC_Q14_offset - 1], AR_shp_Q13[AR_shp_Q13_offset + 1]), psLPC_Q14[psLPC_Q14_offset - 2], AR_shp_Q13[AR_shp_Q13_offset + 2]), psLPC_Q14[psLPC_Q14_offset - 3], AR_shp_Q13[AR_shp_Q13_offset + 3]), psLPC_Q14[psLPC_Q14_offset - 4], AR_shp_Q13[AR_shp_Q13_offset + 4]), psLPC_Q14[psLPC_Q14_offset - 5], AR_shp_Q13[AR_shp_Q13_offset + 5]), psLPC_Q14[psLPC_Q14_offset - 6], AR_shp_Q13[AR_shp_Q13_offset + 6]), psLPC_Q14[psLPC_Q14_offset - 7], AR_shp_Q13[AR_shp_Q13_offset + 7]), psLPC_Q14[psLPC_Q14_offset - 8], AR_shp_Q13[AR_shp_Q13_offset + 8]), psLPC_Q14[psLPC_Q14_offset - 9], AR_shp_Q13[AR_shp_Q13_offset + 9]), psLPC_Q14[psLPC_Q14_offset - 10], AR_shp_Q13[AR_shp_Q13_offset + 10]), psLPC_Q14[psLPC_Q14_offset - 11], AR_shp_Q13[AR_shp_Q13_offset + 11]);
                        for (j = 12; j < shapingLPCOrder; j++) {
                            n_AR_Q10 = Macros.SKP_SMLAWB(n_AR_Q10, psLPC_Q14[psLPC_Q14_offset - j], AR_shp_Q13[AR_shp_Q13_offset + j]);
                        }
                        n_AR_Q10 = Macros.SKP_SMLAWB(n_AR_Q10 >> 1, psDD.LF_AR_Q12, Tilt_Q14);
                        int n_LF_Q10 = Macros.SKP_SMLAWT(Macros.SKP_SMULWB(psDD.Shape_Q10[smpl_buf_idx[0]], LF_shp_Q14) << 2, psDD.LF_AR_Q12, LF_shp_Q14);
                        int r_Q10 = SigProcFIX.SKP_LIMIT_32((((x_Q10[i] - (((SigProcFIX.SKP_RSHIFT_ROUND(LTP_pred_Q14 - n_LTP_Q14, 4) + LPC_pred_Q10) - n_AR_Q10) - n_LF_Q10)) ^ dither) - dither) - offset_Q10, -65536, Buffer.FLAG_SKIP_FEC);
                        if (r_Q10 < -1536) {
                            q1_Q10 = SigProcFIX.SKP_RSHIFT_ROUND(r_Q10, 10) << 10;
                            r_Q10 -= q1_Q10;
                            rd1_Q10 = Macros.SKP_SMLABB((-(q1_Q10 + offset_Q10)) * Lambda_Q10, r_Q10, r_Q10) >> 10;
                            rd2_Q10 = (rd1_Q10 + 1024) - SigProcFIX.SKP_ADD_LSHIFT32(Lambda_Q10, r_Q10, 1);
                            q2_Q10 = q1_Q10 + 1024;
                        } else if (r_Q10 > 512) {
                            q1_Q10 = SigProcFIX.SKP_RSHIFT_ROUND(r_Q10, 10) << 10;
                            r_Q10 -= q1_Q10;
                            rd1_Q10 = Macros.SKP_SMLABB((q1_Q10 + offset_Q10) * Lambda_Q10, r_Q10, r_Q10) >> 10;
                            rd2_Q10 = (rd1_Q10 + 1024) - SigProcFIX.SKP_SUB_LSHIFT32(Lambda_Q10, r_Q10, 1);
                            q2_Q10 = q1_Q10 - 1024;
                        } else {
                            int rr_Q20 = Macros.SKP_SMULBB(offset_Q10, Lambda_Q10);
                            rd2_Q10 = Macros.SKP_SMLABB(rr_Q20, r_Q10, r_Q10) >> 10;
                            rd1_Q10 = (rd2_Q10 + 1024) + SigProcFIX.SKP_SUB_RSHIFT32(SigProcFIX.SKP_ADD_LSHIFT32(Lambda_Q10, r_Q10, 1), rr_Q20, 9);
                            q1_Q10 = -1024;
                            q2_Q10 = 0;
                        }
                        if (rd1_Q10 < rd2_Q10) {
                            psSS[0].RD_Q10 = psDD.RD_Q10 + rd1_Q10;
                            psSS[1].RD_Q10 = psDD.RD_Q10 + rd2_Q10;
                            psSS[0].Q_Q10 = q1_Q10;
                            psSS[1].Q_Q10 = q2_Q10;
                        } else {
                            psSS[0].RD_Q10 = psDD.RD_Q10 + rd2_Q10;
                            psSS[1].RD_Q10 = psDD.RD_Q10 + rd1_Q10;
                            psSS[0].Q_Q10 = q2_Q10;
                            psSS[1].Q_Q10 = q1_Q10;
                        }
                        int LPC_exc_Q10 = (((offset_Q10 + psSS[0].Q_Q10) ^ dither) - dither) + SigProcFIX.SKP_RSHIFT_ROUND(LTP_pred_Q14, 4);
                        int xq_Q10 = LPC_exc_Q10 + LPC_pred_Q10;
                        int sLF_AR_shp_Q10 = xq_Q10 - n_AR_Q10;
                        psSS[0].sLTP_shp_Q10 = sLF_AR_shp_Q10 - n_LF_Q10;
                        psSS[0].LF_AR_Q12 = sLF_AR_shp_Q10 << 2;
                        psSS[0].xq_Q14 = xq_Q10 << 4;
                        psSS[0].LPC_exc_Q16 = LPC_exc_Q10 << 6;
                        LPC_exc_Q10 = (((offset_Q10 + psSS[1].Q_Q10) ^ dither) - dither) + SigProcFIX.SKP_RSHIFT_ROUND(LTP_pred_Q14, 4);
                        xq_Q10 = LPC_exc_Q10 + LPC_pred_Q10;
                        sLF_AR_shp_Q10 = xq_Q10 - n_AR_Q10;
                        psSS[1].sLTP_shp_Q10 = sLF_AR_shp_Q10 - n_LF_Q10;
                        psSS[1].LF_AR_Q12 = sLF_AR_shp_Q10 << 2;
                        psSS[1].xq_Q14 = xq_Q10 << 4;
                        psSS[1].LPC_exc_Q16 = LPC_exc_Q10 << 6;
                        k++;
                    } else {
                        throw new AssertionError();
                    }
                } else {
                    throw new AssertionError();
                }
            }
            smpl_buf_idx[0] = (smpl_buf_idx[0] - 1) & 31;
            int last_smple_idx = (smpl_buf_idx[0] + decisionDelay) & 31;
            int RDmin_Q10 = psSampleState[0][0].RD_Q10;
            int Winner_ind = 0;
            for (k = 1; k < nStatesDelayedDecision; k++) {
                if (psSampleState[k][0].RD_Q10 < RDmin_Q10) {
                    RDmin_Q10 = psSampleState[k][0].RD_Q10;
                    Winner_ind = k;
                }
            }
            int Winner_rand_state = psDelDec[Winner_ind].RandState[last_smple_idx];
            k = 0;
            while (k < nStatesDelayedDecision) {
                if (psDelDec[k].RandState[last_smple_idx] != Winner_rand_state) {
                    psSampleState[k][0].RD_Q10 += 134217727;
                    psSampleState[k][1].RD_Q10 += 134217727;
                    if (!$assertionsDisabled && psSampleState[k][0].RD_Q10 < 0) {
                        throw new AssertionError();
                    }
                }
                k++;
            }
            int RDmax_Q10 = psSampleState[0][0].RD_Q10;
            RDmin_Q10 = psSampleState[0][1].RD_Q10;
            int RDmax_ind = 0;
            int RDmin_ind = 0;
            for (k = 1; k < nStatesDelayedDecision; k++) {
                if (psSampleState[k][0].RD_Q10 > RDmax_Q10) {
                    RDmax_Q10 = psSampleState[k][0].RD_Q10;
                    RDmax_ind = k;
                }
                if (psSampleState[k][1].RD_Q10 < RDmin_Q10) {
                    RDmin_Q10 = psSampleState[k][1].RD_Q10;
                    RDmin_ind = k;
                }
            }
            if (RDmin_Q10 < RDmax_Q10) {
                SKP_Silk_copy_del_dec_state(psDelDec[RDmax_ind], psDelDec[RDmin_ind], i);
                psSampleState[RDmax_ind][0] = (NSQ_sample_struct) psSampleState[RDmin_ind][1].clone();
            }
            psDD = psDelDec[Winner_ind];
            if (subfr > 0 || i >= decisionDelay) {
                q[(q_offset + i) - decisionDelay] = (byte) (psDD.Q_Q10[last_smple_idx] >> 10);
                xq[(xq_offset + i) - decisionDelay] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(Macros.SKP_SMULWW(psDD.Xq_Q10[last_smple_idx], psDD.Gain_Q16[last_smple_idx]), 10));
                NSQ.sLTP_shp_Q10[NSQ.sLTP_shp_buf_idx - decisionDelay] = psDD.Shape_Q10[last_smple_idx];
                sLTP_Q16[NSQ.sLTP_buf_idx - decisionDelay] = psDD.Pred_Q16[last_smple_idx];
            }
            NSQ.sLTP_shp_buf_idx++;
            NSQ.sLTP_buf_idx++;
            for (k = 0; k < nStatesDelayedDecision; k++) {
                psDD = psDelDec[k];
                psSS = psSampleState[k];
                psDD.LF_AR_Q12 = psSS[0].LF_AR_Q12;
                psDD.sLPC_Q14[Define.NSQ_LPC_BUF_LENGTH() + i] = psSS[0].xq_Q14;
                psDD.Xq_Q10[smpl_buf_idx[0]] = psSS[0].xq_Q14 >> 4;
                psDD.Q_Q10[smpl_buf_idx[0]] = psSS[0].Q_Q10;
                psDD.Pred_Q16[smpl_buf_idx[0]] = psSS[0].LPC_exc_Q16;
                psDD.Shape_Q10[smpl_buf_idx[0]] = psSS[0].sLTP_shp_Q10;
                psDD.Seed = SigProcFIX.SKP_ADD_RSHIFT32(psDD.Seed, psSS[0].Q_Q10, 10);
                psDD.RandState[smpl_buf_idx[0]] = psDD.Seed;
                psDD.RD_Q10 = psSS[0].RD_Q10;
                psDD.Gain_Q16[smpl_buf_idx[0]] = Gain_Q16;
            }
            i++;
        }
        for (k = 0; k < nStatesDelayedDecision; k++) {
            psDD = psDelDec[k];
            System.arraycopy(psDD.sLPC_Q14, length, psDD.sLPC_Q14, 0, Define.NSQ_LPC_BUF_LENGTH());
        }
    }

    static void SKP_Silk_nsq_del_dec_scale_states(SKP_Silk_nsq_state NSQ, NSQDelDecStruct[] psDelDec, short[] x, int x_offset, int[] x_sc_Q10, int length, short[] sLTP, int[] sLTP_Q16, int subfr, int nStatesDelayedDecision, int smpl_buf_idx, int LTP_scale_Q14, int[] Gains_Q16, int[] pitchL) {
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
            i = (NSQ.sLTP_buf_idx - lag) - 2;
            while (i < NSQ.sLTP_buf_idx) {
                if ($assertionsDisabled || i < DeviceConfiguration.DEFAULT_VIDEO_HEIGHT) {
                    sLTP_Q16[i] = Macros.SKP_SMULWB(inv_gain_Q32, sLTP[i]);
                    i++;
                } else {
                    throw new AssertionError();
                }
            }
        }
        if (inv_gain_Q16 != NSQ.prev_inv_gain_Q16) {
            int gain_adj_Q16 = Inlines.SKP_DIV32_varQ(inv_gain_Q16, NSQ.prev_inv_gain_Q16, 16);
            for (int k = 0; k < nStatesDelayedDecision; k++) {
                NSQDelDecStruct psDD = psDelDec[k];
                psDD.LF_AR_Q12 = Macros.SKP_SMULWW(gain_adj_Q16, psDD.LF_AR_Q12);
                for (i = 0; i < Define.NSQ_LPC_BUF_LENGTH(); i++) {
                    psDD.sLPC_Q14[(Define.NSQ_LPC_BUF_LENGTH() - i) - 1] = Macros.SKP_SMULWW(gain_adj_Q16, psDD.sLPC_Q14[(Define.NSQ_LPC_BUF_LENGTH() - i) - 1]);
                }
                for (i = 0; i < 32; i++) {
                    psDD.Pred_Q16[i] = Macros.SKP_SMULWW(gain_adj_Q16, psDD.Pred_Q16[i]);
                    psDD.Shape_Q10[i] = Macros.SKP_SMULWW(gain_adj_Q16, psDD.Shape_Q10[i]);
                }
            }
            for (i = NSQ.sLTP_shp_buf_idx - Math.max((length * 4) - Macros.SKP_SMULBB(4 - (subfr + 1), length), lag + 5); i < NSQ.sLTP_shp_buf_idx; i++) {
                NSQ.sLTP_shp_Q10[i] = Macros.SKP_SMULWW(gain_adj_Q16, NSQ.sLTP_shp_Q10[i]);
            }
            if (NSQ.rewhite_flag == 0) {
                for (i = (NSQ.sLTP_buf_idx - lag) - 2; i < NSQ.sLTP_buf_idx; i++) {
                    sLTP_Q16[i] = Macros.SKP_SMULWW(gain_adj_Q16, sLTP_Q16[i]);
                }
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

    static void SKP_Silk_copy_del_dec_state(NSQDelDecStruct DD_dst, NSQDelDecStruct DD_src, int LPC_state_idx) {
        System.arraycopy(DD_src.RandState, 0, DD_dst.RandState, 0, 32);
        System.arraycopy(DD_src.Q_Q10, 0, DD_dst.Q_Q10, 0, 32);
        System.arraycopy(DD_src.Pred_Q16, 0, DD_dst.Pred_Q16, 0, 32);
        System.arraycopy(DD_src.Shape_Q10, 0, DD_dst.Shape_Q10, 0, 32);
        System.arraycopy(DD_src.Xq_Q10, 0, DD_dst.Xq_Q10, 0, 32);
        System.arraycopy(DD_src.sLPC_Q14, LPC_state_idx, DD_dst.sLPC_Q14, LPC_state_idx, Define.NSQ_LPC_BUF_LENGTH());
        DD_dst.LF_AR_Q12 = DD_src.LF_AR_Q12;
        DD_dst.Seed = DD_src.Seed;
        DD_dst.SeedInit = DD_src.SeedInit;
        DD_dst.RD_Q10 = DD_src.RD_Q10;
    }
}
