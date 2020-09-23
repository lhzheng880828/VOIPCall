package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: Structs */
class SKP_Silk_NLSF_CBS {
    short[] CB_NLSF_Q15;
    short[] Rates_Q5;
    int nVectors;

    public SKP_Silk_NLSF_CBS(int nVectors, short[] CB_NLSF_Q15, short[] Rates_Q5) {
        this.CB_NLSF_Q15 = CB_NLSF_Q15;
        this.nVectors = nVectors;
        this.Rates_Q5 = Rates_Q5;
    }

    public SKP_Silk_NLSF_CBS(int nVectors, short[] SKP_Silk_NLSF_MSVQ_CB0_10_Q15, int Q15_offset, short[] SKP_Silk_NLSF_MSVQ_CB0_10_rates_Q5, int Q5_offset) {
        this.nVectors = nVectors;
        this.CB_NLSF_Q15 = new short[(SKP_Silk_NLSF_MSVQ_CB0_10_Q15.length - Q15_offset)];
        System.arraycopy(SKP_Silk_NLSF_MSVQ_CB0_10_Q15, Q15_offset, this.CB_NLSF_Q15, 0, this.CB_NLSF_Q15.length);
        this.Rates_Q5 = new short[(SKP_Silk_NLSF_MSVQ_CB0_10_rates_Q5.length - Q5_offset)];
        System.arraycopy(SKP_Silk_NLSF_MSVQ_CB0_10_rates_Q5, Q5_offset, this.Rates_Q5, 0, this.Rates_Q5.length);
    }
}
