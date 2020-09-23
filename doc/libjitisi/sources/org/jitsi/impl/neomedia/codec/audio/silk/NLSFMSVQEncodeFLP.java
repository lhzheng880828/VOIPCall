package org.jitsi.impl.neomedia.codec.audio.silk;

import java.util.Arrays;

public class NLSFMSVQEncodeFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!NLSFMSVQEncodeFLP.class.desiredAssertionStatus());

    static void SKP_Silk_NLSF_MSVQ_encode_FLP(int[] NLSFIndices, float[] pNLSF, SKP_Silk_NLSF_CB_FLP psNLSF_CB_FLP, float[] pNLSF_q_prev, float[] pW, float NLSF_mu, float NLSF_mu_fluc_red, int NLSF_MSVQ_Survivors, int LPC_order, int deactivate_fluc_red) {
        Object pNLSF_in = new float[16];
        float[] pRateDist = new float[Define.NLSF_MSVQ_TREE_SEARCH_MAX_VECTORS_EVALUATED()];
        float[] pRate = new float[16];
        Object pRate_new = new float[16];
        int[] pTempIndices = new int[16];
        Object pPath = new int[160];
        Object pPath_new = new int[160];
        float[] pRes = new float[256];
        Object pRes_new = new float[256];
        if ($assertionsDisabled || NLSF_MSVQ_Survivors <= 16) {
            int s;
            int i;
            int cur_survivors = NLSF_MSVQ_Survivors;
            System.arraycopy(pNLSF, 0, pNLSF_in, 0, LPC_order);
            Arrays.fill(pRate, 0, NLSF_MSVQ_Survivors, 0.0f);
            System.arraycopy(pNLSF, 0, pRes, 0, LPC_order);
            int prev_survivors = 1;
            for (s = 0; s < psNLSF_CB_FLP.nStages; s++) {
                SKP_Silk_NLSF_CBS_FLP pCurrentCBStage = psNLSF_CB_FLP.CBStages[s];
                cur_survivors = Math.min(NLSF_MSVQ_Survivors, pCurrentCBStage.nVectors * prev_survivors);
                NLSFVQRateDistortionFLP.SKP_Silk_NLSF_VQ_rate_distortion_FLP(pRateDist, pCurrentCBStage, pRes, pW, pRate, NLSF_mu, prev_survivors, LPC_order);
                SortFLP.SKP_Silk_insertion_sort_increasing_FLP(pRateDist, 0, pTempIndices, pCurrentCBStage.nVectors * prev_survivors, cur_survivors);
                float rateDistThreshold = 4.0f * pRateDist[0];
                while (pRateDist[cur_survivors - 1] > rateDistThreshold && cur_survivors > 1) {
                    cur_survivors--;
                }
                for (int k = 0; k < cur_survivors; k++) {
                    int input_index;
                    int cb_index;
                    if (s <= 0) {
                        input_index = 0;
                        cb_index = pTempIndices[k];
                    } else if (pCurrentCBStage.nVectors == 8) {
                        input_index = pTempIndices[k] >> 3;
                        cb_index = pTempIndices[k] & 7;
                    } else {
                        input_index = pTempIndices[k] / pCurrentCBStage.nVectors;
                        cb_index = pTempIndices[k] - (pCurrentCBStage.nVectors * input_index);
                    }
                    float[] pConstFloat = pRes;
                    int pConstFloat_offset = input_index * LPC_order;
                    float[] pCB_element = pCurrentCBStage.CB;
                    int pCB_element_offset = cb_index * LPC_order;
                    Object pFloat = pRes_new;
                    int pFloat_offset = k * LPC_order;
                    for (i = 0; i < LPC_order; i++) {
                        pFloat[pFloat_offset + i] = pConstFloat[pConstFloat_offset + i] - pCB_element[pCB_element_offset + i];
                    }
                    pRate_new[k] = pRate[input_index] + pCurrentCBStage.Rates[cb_index];
                    Object pConstInt = pPath;
                    int pConstInt_offset = input_index * psNLSF_CB_FLP.nStages;
                    Object pInt = pPath_new;
                    int pInt_offset = k * psNLSF_CB_FLP.nStages;
                    for (i = 0; i < s; i++) {
                        pInt[pInt_offset + i] = pConstInt[pConstInt_offset + i];
                    }
                    pInt[pInt_offset + s] = cb_index;
                }
                if (s < psNLSF_CB_FLP.nStages - 1) {
                    System.arraycopy(pRes_new, 0, pRes, 0, cur_survivors * LPC_order);
                    System.arraycopy(pRate_new, 0, pRate, 0, cur_survivors);
                    System.arraycopy(pPath_new, 0, pPath, 0, psNLSF_CB_FLP.nStages * cur_survivors);
                }
                prev_survivors = cur_survivors;
            }
            int bestIndex = 0;
            if (deactivate_fluc_red != 1) {
                float bestRateDist = Float.MAX_VALUE;
                for (s = 0; s < cur_survivors; s++) {
                    NLSFMSVQDecodeFLP.SKP_Silk_NLSF_MSVQ_decode_FLP(pNLSF, psNLSF_CB_FLP, pPath_new, psNLSF_CB_FLP.nStages * s, LPC_order);
                    float wsse = 0.0f;
                    for (i = 0; i < LPC_order; i += 2) {
                        float se = pNLSF[i] - pNLSF_q_prev[i];
                        wsse += (pW[i] * se) * se;
                        se = pNLSF[i + 1] - pNLSF_q_prev[i + 1];
                        wsse += (pW[i + 1] * se) * se;
                    }
                    wsse = pRateDist[s] + (wsse * NLSF_mu_fluc_red);
                    if (wsse < bestRateDist) {
                        bestRateDist = wsse;
                        bestIndex = s;
                    }
                }
            }
            System.arraycopy(pPath_new, psNLSF_CB_FLP.nStages * bestIndex, NLSFIndices, 0, psNLSF_CB_FLP.nStages);
            NLSFMSVQDecodeFLP.SKP_Silk_NLSF_MSVQ_decode_FLP(pNLSF, psNLSF_CB_FLP, NLSFIndices, 0, LPC_order);
            return;
        }
        throw new AssertionError();
    }
}
