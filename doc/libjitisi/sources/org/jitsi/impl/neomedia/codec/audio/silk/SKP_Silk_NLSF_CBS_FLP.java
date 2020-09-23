package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: StructsFLP */
class SKP_Silk_NLSF_CBS_FLP {
    float[] CB;
    float[] Rates;
    int nVectors;

    public SKP_Silk_NLSF_CBS_FLP(int nVectors, float[] CB, float[] Rates) {
        this.nVectors = nVectors;
        this.CB = CB;
        this.Rates = Rates;
    }

    public SKP_Silk_NLSF_CBS_FLP(int nVectors, float[] CB, int CB_offset, float[] Rates, int Rates_offset) {
        this.nVectors = nVectors;
        this.CB = new float[(CB.length - CB_offset)];
        System.arraycopy(CB, CB_offset, this.CB, 0, this.CB.length);
        this.Rates = new float[(Rates.length - Rates_offset)];
        System.arraycopy(Rates, Rates_offset, this.Rates, 0, this.Rates.length);
    }
}
