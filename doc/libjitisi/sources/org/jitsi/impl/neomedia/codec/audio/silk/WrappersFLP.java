package org.jitsi.impl.neomedia.codec.audio.silk;

import java.lang.reflect.Array;
import org.jitsi.impl.neomedia.device.DeviceConfiguration;

public class WrappersFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!WrappersFLP.class.desiredAssertionStatus());
    static long ar2_q13_file_offset = 0;
    static int frame_cnt = 0;
    static long x_16_file_offset = 0;

    static void SKP_Silk_A2NLSF_FLP(float[] pNLSF, float[] pAR, int LPC_order) {
        int i;
        int[] NLSF_fix = new int[16];
        int[] a_fix_Q16 = new int[16];
        for (i = 0; i < LPC_order; i++) {
            a_fix_Q16[i] = SigProcFLP.SKP_float2int((double) (pAR[i] * 65536.0f));
        }
        A2NLSF.SKP_Silk_A2NLSF(NLSF_fix, a_fix_Q16, LPC_order);
        for (i = 0; i < LPC_order; i++) {
            pNLSF[i] = ((float) NLSF_fix[i]) * 3.0517578E-5f;
        }
    }

    static void SKP_Silk_NLSF2A_stable_FLP(float[] pAR, float[] pNLSF, int LPC_order) {
        int i;
        int[] NLSF_fix = new int[16];
        short[] a_fix_Q12 = new short[16];
        for (i = 0; i < LPC_order; i++) {
            NLSF_fix[i] = SigProcFLP.SKP_float2int((double) (pNLSF[i] * 32768.0f));
        }
        NLSF2AStable.SKP_Silk_NLSF2A_stable(a_fix_Q12, NLSF_fix, LPC_order);
        for (i = 0; i < LPC_order; i++) {
            pAR[i] = ((float) a_fix_Q12[i]) / 4096.0f;
        }
    }

    static void SKP_Silk_NLSF_stabilize_FLP(float[] pNLSF, float[] pNDelta_min, int LPC_order) {
        int i;
        int[] NLSF_Q15 = new int[16];
        int[] ndelta_min_Q15 = new int[17];
        for (i = 0; i < LPC_order; i++) {
            NLSF_Q15[i] = SigProcFLP.SKP_float2int((double) (pNLSF[i] * 32768.0f));
            ndelta_min_Q15[i] = SigProcFLP.SKP_float2int((double) (pNDelta_min[i] * 32768.0f));
        }
        ndelta_min_Q15[LPC_order] = SigProcFLP.SKP_float2int((double) (pNDelta_min[LPC_order] * 32768.0f));
        NLSFStabilize.SKP_Silk_NLSF_stabilize(NLSF_Q15, 0, ndelta_min_Q15, LPC_order);
        for (i = 0; i < LPC_order; i++) {
            pNLSF[i] = ((float) NLSF_Q15[i]) * 3.0517578E-5f;
        }
    }

    static void SKP_Silk_interpolate_wrapper_FLP(float[] xi, float[] x0, float[] x1, float ifact, int d) {
        int i;
        int[] x0_int = new int[16];
        int[] x1_int = new int[16];
        int[] xi_int = new int[16];
        int ifact_Q2 = (int) (4.0f * ifact);
        for (i = 0; i < d; i++) {
            x0_int[i] = SigProcFLP.SKP_float2int((double) (x0[i] * 32768.0f));
            x1_int[i] = SigProcFLP.SKP_float2int((double) (x1[i] * 32768.0f));
        }
        Interpolate.SKP_Silk_interpolate(xi_int, x0_int, x1_int, ifact_Q2, d);
        for (i = 0; i < d; i++) {
            xi[i] = ((float) xi_int[i]) * 3.0517578E-5f;
        }
    }

    static int SKP_Silk_VAD_FLP(SKP_Silk_encoder_state_FLP psEnc, SKP_Silk_encoder_control_FLP psEncCtrl, short[] pIn, int pIn_offset) {
        int[] SA_Q8 = new int[1];
        int[] SNR_dB_Q7 = new int[1];
        int[] Tilt_Q15 = new int[1];
        int[] Quality_Bands_Q15 = new int[4];
        int ret = VAD.SKP_Silk_VAD_GetSA_Q8(psEnc.sCmn.sVAD, SA_Q8, SNR_dB_Q7, Quality_Bands_Q15, Tilt_Q15, pIn, pIn_offset, psEnc.sCmn.frame_length);
        psEnc.speech_activity = ((float) SA_Q8[0]) / 256.0f;
        for (int i = 0; i < 4; i++) {
            psEncCtrl.input_quality_bands[i] = ((float) Quality_Bands_Q15[i]) / 32768.0f;
        }
        psEncCtrl.input_tilt = ((float) Tilt_Q15[0]) / 32768.0f;
        return ret;
    }

    static void SKP_Silk_NSQ_wrapper_FLP(SKP_Silk_encoder_state_FLP psEnc, SKP_Silk_encoder_control_FLP psEncCtrl, float[] x, int x_offset, byte[] q, int q_offset, int useLBRR) {
        int i;
        int LTP_scale_Q14;
        short[] x_16 = new short[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
        int[] Gains_Q16 = new int[4];
        short[][] PredCoef_Q12 = (short[][]) Array.newInstance(Short.TYPE, new int[]{2, 16});
        short[] LTPCoef_Q14 = new short[20];
        short[] AR2_Q13 = new short[64];
        int[] LF_shp_Q14 = new int[4];
        int[] Tilt_Q14 = new int[4];
        int[] HarmShapeGain_Q14 = new int[4];
        for (i = 0; i < 64; i++) {
            AR2_Q13[i] = (short) SigProcFIX.SKP_SAT16(SigProcFLP.SKP_float2int((double) (psEncCtrl.AR2[i] * 8192.0f)));
        }
        for (i = 0; i < 4; i++) {
            LF_shp_Q14[i] = (SigProcFLP.SKP_float2int((double) (psEncCtrl.LF_AR_shp[i] * 16384.0f)) << 16) | (65535 & SigProcFLP.SKP_float2int((double) (psEncCtrl.LF_MA_shp[i] * 16384.0f)));
            Tilt_Q14[i] = SigProcFLP.SKP_float2int((double) (psEncCtrl.Tilt[i] * 16384.0f));
            HarmShapeGain_Q14[i] = SigProcFLP.SKP_float2int((double) (psEncCtrl.HarmShapeGain[i] * 16384.0f));
        }
        int Lambda_Q10 = SigProcFLP.SKP_float2int((double) (psEncCtrl.Lambda * 1024.0f));
        for (i = 0; i < 20; i++) {
            LTPCoef_Q14[i] = (short) SigProcFLP.SKP_float2int((double) (psEncCtrl.LTPCoef[i] * 16384.0f));
        }
        for (int j = 0; j < 2; j++) {
            for (i = 0; i < 16; i++) {
                PredCoef_Q12[j][i] = (short) SigProcFLP.SKP_float2int((double) (psEncCtrl.PredCoef[j][i] * 4096.0f));
            }
        }
        i = 0;
        while (i < 4) {
            float tmp_float = SigProcFIX.SKP_LIMIT(psEncCtrl.Gains[i] * 65536.0f, 2.14748301E9f, -2.14748301E9f);
            Gains_Q16[i] = SigProcFLP.SKP_float2int((double) tmp_float);
            if (psEncCtrl.Gains[i] > 0.0f) {
                if (!$assertionsDisabled && tmp_float < 0.0f) {
                    throw new AssertionError();
                } else if (!$assertionsDisabled && Gains_Q16[i] < 0) {
                    throw new AssertionError();
                }
            }
            i++;
        }
        if (psEncCtrl.sCmn.sigtype == 0) {
            LTP_scale_Q14 = TablesOther.SKP_Silk_LTPScales_table_Q14[psEncCtrl.sCmn.LTP_scaleIndex];
        } else {
            LTP_scale_Q14 = 0;
        }
        SigProcFLP.SKP_float2short_array(x_16, 0, x, x_offset, psEnc.sCmn.frame_length);
        short[] PredCoef_Q12_dim1_tmp = new short[(PredCoef_Q12.length * PredCoef_Q12[0].length)];
        int PredCoef_Q12_offset = 0;
        for (int PredCoef_Q12_i = 0; PredCoef_Q12_i < PredCoef_Q12.length; PredCoef_Q12_i++) {
            System.arraycopy(PredCoef_Q12[PredCoef_Q12_i], 0, PredCoef_Q12_dim1_tmp, PredCoef_Q12_offset, PredCoef_Q12[PredCoef_Q12_i].length);
            PredCoef_Q12_offset += PredCoef_Q12[PredCoef_Q12_i].length;
        }
        if (useLBRR != 0) {
            psEnc.NoiseShapingQuantizer(psEnc.sCmn, psEncCtrl.sCmn, psEnc.sNSQ_LBRR, x_16, q, psEncCtrl.sCmn.NLSFInterpCoef_Q2, PredCoef_Q12_dim1_tmp, LTPCoef_Q14, AR2_Q13, HarmShapeGain_Q14, Tilt_Q14, LF_shp_Q14, Gains_Q16, Lambda_Q10, LTP_scale_Q14);
            return;
        }
        psEnc.NoiseShapingQuantizer(psEnc.sCmn, psEncCtrl.sCmn, psEnc.sNSQ, x_16, q, psEncCtrl.sCmn.NLSFInterpCoef_Q2, PredCoef_Q12_dim1_tmp, LTPCoef_Q14, AR2_Q13, HarmShapeGain_Q14, Tilt_Q14, LF_shp_Q14, Gains_Q16, Lambda_Q10, LTP_scale_Q14);
    }
}
