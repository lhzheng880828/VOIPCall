package org.jitsi.impl.neomedia.codec.audio.silk;

public class NLSFMSVQDecodeFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!NLSFMSVQDecodeFLP.class.desiredAssertionStatus());

    static void SKP_Silk_NLSF_MSVQ_decode_FLP(float[] pNLSF, SKP_Silk_NLSF_CB_FLP psNLSF_CB_FLP, int[] NLSFIndices, int NLSFIndices_offset, int LPC_order) {
        if ($assertionsDisabled || (NLSFIndices[NLSFIndices_offset + 0] >= 0 && NLSFIndices[NLSFIndices_offset + 0] < psNLSF_CB_FLP.CBStages[0].nVectors)) {
            System.arraycopy(psNLSF_CB_FLP.CBStages[0].CB, NLSFIndices[NLSFIndices_offset + 0] * LPC_order, pNLSF, 0, LPC_order);
            int s = 1;
            while (s < psNLSF_CB_FLP.nStages) {
                if ($assertionsDisabled || (NLSFIndices[NLSFIndices_offset + s] >= 0 && NLSFIndices[NLSFIndices_offset + s] < psNLSF_CB_FLP.CBStages[s].nVectors)) {
                    float[] pCB_element;
                    int pCB_element_offset;
                    if (LPC_order == 16) {
                        pCB_element = psNLSF_CB_FLP.CBStages[s].CB;
                        pCB_element_offset = NLSFIndices[NLSFIndices_offset + s] << 4;
                        pNLSF[0] = pNLSF[0] + pCB_element[pCB_element_offset + 0];
                        pNLSF[1] = pNLSF[1] + pCB_element[pCB_element_offset + 1];
                        pNLSF[2] = pNLSF[2] + pCB_element[pCB_element_offset + 2];
                        pNLSF[3] = pNLSF[3] + pCB_element[pCB_element_offset + 3];
                        pNLSF[4] = pNLSF[4] + pCB_element[pCB_element_offset + 4];
                        pNLSF[5] = pNLSF[5] + pCB_element[pCB_element_offset + 5];
                        pNLSF[6] = pNLSF[6] + pCB_element[pCB_element_offset + 6];
                        pNLSF[7] = pNLSF[7] + pCB_element[pCB_element_offset + 7];
                        pNLSF[8] = pNLSF[8] + pCB_element[pCB_element_offset + 8];
                        pNLSF[9] = pNLSF[9] + pCB_element[pCB_element_offset + 9];
                        pNLSF[10] = pNLSF[10] + pCB_element[pCB_element_offset + 10];
                        pNLSF[11] = pNLSF[11] + pCB_element[pCB_element_offset + 11];
                        pNLSF[12] = pNLSF[12] + pCB_element[pCB_element_offset + 12];
                        pNLSF[13] = pNLSF[13] + pCB_element[pCB_element_offset + 13];
                        pNLSF[14] = pNLSF[14] + pCB_element[pCB_element_offset + 14];
                        pNLSF[15] = pNLSF[15] + pCB_element[pCB_element_offset + 15];
                    } else {
                        pCB_element = psNLSF_CB_FLP.CBStages[s].CB;
                        pCB_element_offset = NLSFIndices[NLSFIndices_offset + s] * LPC_order;
                        for (int i = 0; i < LPC_order; i++) {
                            pNLSF[i] = pNLSF[i] + pCB_element[pCB_element_offset + i];
                        }
                    }
                    s++;
                } else {
                    throw new AssertionError();
                }
            }
            WrappersFLP.SKP_Silk_NLSF_stabilize_FLP(pNLSF, psNLSF_CB_FLP.NDeltaMin, LPC_order);
            return;
        }
        throw new AssertionError();
    }
}
