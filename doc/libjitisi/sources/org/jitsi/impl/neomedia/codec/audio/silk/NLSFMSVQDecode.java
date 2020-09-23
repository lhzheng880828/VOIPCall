package org.jitsi.impl.neomedia.codec.audio.silk;

public class NLSFMSVQDecode {
    static void SKP_Silk_NLSF_MSVQ_decode(int[] pNLSF_Q15, SKP_Silk_NLSF_CB_struct psNLSF_CB, int[] NLSFIndices, int LPC_order) {
        boolean z;
        int i;
        if (NLSFIndices[0] < 0 || NLSFIndices[0] >= psNLSF_CB.CBStages[0].nVectors) {
            z = false;
        } else {
            z = true;
        }
        Typedef.SKP_assert(z);
        short[] pCB_element = psNLSF_CB.CBStages[0].CB_NLSF_Q15;
        int pCB_element_offset = NLSFIndices[0] * LPC_order;
        for (i = 0; i < LPC_order; i++) {
            pNLSF_Q15[i] = pCB_element[pCB_element_offset + i];
        }
        int s = 1;
        while (s < psNLSF_CB.nStages) {
            if (NLSFIndices[s] < 0 || NLSFIndices[s] >= psNLSF_CB.CBStages[s].nVectors) {
                z = false;
            } else {
                z = true;
            }
            Typedef.SKP_assert(z);
            if (LPC_order == 16) {
                pCB_element = psNLSF_CB.CBStages[s].CB_NLSF_Q15;
                pCB_element_offset = NLSFIndices[s] << 4;
                pNLSF_Q15[0] = pNLSF_Q15[0] + pCB_element[pCB_element_offset + 0];
                pNLSF_Q15[1] = pNLSF_Q15[1] + pCB_element[pCB_element_offset + 1];
                pNLSF_Q15[2] = pNLSF_Q15[2] + pCB_element[pCB_element_offset + 2];
                pNLSF_Q15[3] = pNLSF_Q15[3] + pCB_element[pCB_element_offset + 3];
                pNLSF_Q15[4] = pNLSF_Q15[4] + pCB_element[pCB_element_offset + 4];
                pNLSF_Q15[5] = pNLSF_Q15[5] + pCB_element[pCB_element_offset + 5];
                pNLSF_Q15[6] = pNLSF_Q15[6] + pCB_element[pCB_element_offset + 6];
                pNLSF_Q15[7] = pNLSF_Q15[7] + pCB_element[pCB_element_offset + 7];
                pNLSF_Q15[8] = pNLSF_Q15[8] + pCB_element[pCB_element_offset + 8];
                pNLSF_Q15[9] = pNLSF_Q15[9] + pCB_element[pCB_element_offset + 9];
                pNLSF_Q15[10] = pNLSF_Q15[10] + pCB_element[pCB_element_offset + 10];
                pNLSF_Q15[11] = pNLSF_Q15[11] + pCB_element[pCB_element_offset + 11];
                pNLSF_Q15[12] = pNLSF_Q15[12] + pCB_element[pCB_element_offset + 12];
                pNLSF_Q15[13] = pNLSF_Q15[13] + pCB_element[pCB_element_offset + 13];
                pNLSF_Q15[14] = pNLSF_Q15[14] + pCB_element[pCB_element_offset + 14];
                pNLSF_Q15[15] = pNLSF_Q15[15] + pCB_element[pCB_element_offset + 15];
            } else {
                pCB_element = psNLSF_CB.CBStages[s].CB_NLSF_Q15;
                pCB_element_offset = Macros.SKP_SMULBB(NLSFIndices[s], LPC_order);
                for (i = 0; i < LPC_order; i++) {
                    pNLSF_Q15[i] = pNLSF_Q15[i] + pCB_element[pCB_element_offset + i];
                }
            }
            s++;
        }
        NLSFStabilize.SKP_Silk_NLSF_stabilize(pNLSF_Q15, 0, psNLSF_CB.NDeltaMin_Q15, LPC_order);
    }
}
