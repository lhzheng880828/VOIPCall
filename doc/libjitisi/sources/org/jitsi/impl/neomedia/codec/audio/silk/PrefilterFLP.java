package org.jitsi.impl.neomedia.codec.audio.silk;

import java.util.Arrays;

public class PrefilterFLP {
    static void SKP_Silk_prefilter_FLP(SKP_Silk_encoder_state_FLP psEnc, SKP_Silk_encoder_control_FLP psEncCtrl, float[] xw, float[] x, int x_offset) {
        SKP_Silk_prefilter_state_FLP P = psEnc.sPrefilt;
        float[] B = new float[2];
        float[] AR1_shp = new float[64];
        float[] HarmShapeFIR = new float[3];
        float[] st_res = new float[136];
        float[] px = x;
        int px_offset = x_offset;
        float[] pxw = xw;
        int pxw_offset = 0;
        int lag = P.lagPrev;
        for (int k = 0; k < 4; k++) {
            if (psEncCtrl.sCmn.sigtype == 0) {
                lag = psEncCtrl.sCmn.pitchL[k];
            }
            float HarmShapeGain = psEncCtrl.HarmShapeGain[k] * (1.0f - psEncCtrl.HarmBoost[k]);
            HarmShapeFIR[0] = TablesOtherFLP.SKP_Silk_HarmShapeFIR_FLP[0] * HarmShapeGain;
            HarmShapeFIR[1] = TablesOtherFLP.SKP_Silk_HarmShapeFIR_FLP[1] * HarmShapeGain;
            HarmShapeFIR[2] = TablesOtherFLP.SKP_Silk_HarmShapeFIR_FLP[2] * HarmShapeGain;
            float Tilt = psEncCtrl.Tilt[k];
            float LF_MA_shp = psEncCtrl.LF_MA_shp[k];
            float LF_AR_shp = psEncCtrl.LF_AR_shp[k];
            Arrays.fill(AR1_shp, 0.0f);
            System.arraycopy(psEncCtrl.AR1, k * 16, AR1_shp, 0, psEncCtrl.AR1.length - (k * 16));
            LPCAnalysisFilterFLP.SKP_Silk_LPC_analysis_filter_FLP(st_res, AR1_shp, px, px_offset - psEnc.sCmn.shapingLPCOrder, psEnc.sCmn.subfr_length + psEnc.sCmn.shapingLPCOrder, psEnc.sCmn.shapingLPCOrder);
            float[] pst_res = st_res;
            int pst_res_offset = psEnc.sCmn.shapingLPCOrder;
            B[0] = psEncCtrl.GainsPre[k];
            B[1] = (-psEncCtrl.GainsPre[k]) * (((psEncCtrl.HarmBoost[k] * HarmShapeGain) + 0.04f) + (psEncCtrl.coding_quality * 0.06f));
            pxw[pxw_offset + 0] = (B[0] * pst_res[pst_res_offset + 0]) + (B[1] * P.sHarmHP);
            for (int j = 1; j < psEnc.sCmn.subfr_length; j++) {
                pxw[pxw_offset + j] = (B[0] * pst_res[pst_res_offset + j]) + (B[1] * pst_res[(pst_res_offset + j) - 1]);
            }
            P.sHarmHP = pst_res[(psEnc.sCmn.subfr_length + pst_res_offset) - 1];
            SKP_Silk_prefilt_FLP(P, pxw, pxw_offset, pxw, pxw_offset, HarmShapeFIR, Tilt, LF_MA_shp, LF_AR_shp, lag, psEnc.sCmn.subfr_length);
            px_offset += psEnc.sCmn.subfr_length;
            pxw_offset += psEnc.sCmn.subfr_length;
        }
        P.lagPrev = psEncCtrl.sCmn.pitchL[3];
    }

    static void SKP_Silk_prefilt_FLP(SKP_Silk_prefilter_state_FLP P, float[] st_res, int st_res_offset, float[] xw, int xw_offset, float[] HarmShapeFIR, float Tilt, float LF_MA_shp, float LF_AR_shp, int lag, int length) {
        float[] LTP_shp_buf = P.sLTP_shp1;
        int LTP_shp_buf_idx = P.sLTP_shp_buf_idx1;
        float sLF_AR_shp = P.sLF_AR_shp1;
        float sLF_MA_shp = P.sLF_MA_shp1;
        for (int i = 0; i < length; i++) {
            float n_LTP;
            if (lag > 0) {
                int idx = lag + LTP_shp_buf_idx;
                n_LTP = ((LTP_shp_buf[((idx - 1) - 1) & 511] * HarmShapeFIR[0]) + (LTP_shp_buf[(idx - 1) & 511] * HarmShapeFIR[1])) + (LTP_shp_buf[((idx - 1) + 1) & 511] * HarmShapeFIR[2]);
            } else {
                n_LTP = 0.0f;
            }
            float n_LF = (sLF_AR_shp * LF_AR_shp) + (sLF_MA_shp * LF_MA_shp);
            sLF_AR_shp = st_res[st_res_offset + i] - (sLF_AR_shp * Tilt);
            sLF_MA_shp = sLF_AR_shp - n_LF;
            LTP_shp_buf_idx = (LTP_shp_buf_idx - 1) & 511;
            LTP_shp_buf[LTP_shp_buf_idx] = sLF_MA_shp;
            xw[xw_offset + i] = sLF_MA_shp - n_LTP;
        }
        P.sLF_AR_shp1 = sLF_AR_shp;
        P.sLF_MA_shp1 = sLF_MA_shp;
        P.sLTP_shp_buf_idx1 = LTP_shp_buf_idx;
    }
}
