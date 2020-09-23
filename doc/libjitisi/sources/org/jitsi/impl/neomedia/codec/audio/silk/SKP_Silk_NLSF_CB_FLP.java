package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: StructsFLP */
class SKP_Silk_NLSF_CB_FLP {
    SKP_Silk_NLSF_CBS_FLP[] CBStages;
    int[] CDF;
    int[] MiddleIx;
    float[] NDeltaMin;
    int[][] StartPtr;
    int nStages;

    public SKP_Silk_NLSF_CB_FLP(int nStages, SKP_Silk_NLSF_CBS_FLP[] CBStages, float[] NDeltaMin, int[] CDF, int[][] StartPtr, int[] MiddleIx) {
        this.nStages = nStages;
        this.CBStages = CBStages;
        this.NDeltaMin = NDeltaMin;
        this.CDF = CDF;
        this.StartPtr = StartPtr;
        this.MiddleIx = MiddleIx;
    }
}
