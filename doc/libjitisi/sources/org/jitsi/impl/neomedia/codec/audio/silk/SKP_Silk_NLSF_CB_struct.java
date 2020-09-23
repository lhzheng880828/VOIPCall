package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: Structs */
class SKP_Silk_NLSF_CB_struct {
    SKP_Silk_NLSF_CBS[] CBStages;
    int[] CDF;
    int[] MiddleIx;
    int[] NDeltaMin_Q15;
    int[][] StartPtr;
    int nStages;

    public SKP_Silk_NLSF_CB_struct(int nStates, SKP_Silk_NLSF_CBS[] CBStages, int[] NDeltaMin_Q15, int[] CDF, int[][] StartPtr, int[] MiddleIx) {
        this.CBStages = CBStages;
        this.CDF = CDF;
        this.MiddleIx = MiddleIx;
        this.NDeltaMin_Q15 = NDeltaMin_Q15;
        this.nStages = nStates;
        this.StartPtr = StartPtr;
    }
}
