package org.jitsi.impl.neomedia.codec.audio.silk;

import java.util.Arrays;

/* compiled from: StructsFLP */
class SKP_Silk_prefilter_state_FLP {
    int lagPrev;
    int rand_seed;
    float[] sAR_shp1 = new float[17];
    float[] sAR_shp2 = new float[16];
    int sAR_shp_buf_idx2;
    float sHarmHP;
    float sLF_AR_shp1;
    float sLF_AR_shp2;
    float sLF_MA_shp1;
    float sLF_MA_shp2;
    float[] sLTP_shp1 = new float[512];
    float[] sLTP_shp2 = new float[512];
    int sLTP_shp_buf_idx1;
    int sLTP_shp_buf_idx2;

    SKP_Silk_prefilter_state_FLP() {
    }

    public void memZero() {
        Arrays.fill(this.sAR_shp1, 0.0f);
        Arrays.fill(this.sAR_shp2, 0.0f);
        Arrays.fill(this.sLTP_shp1, 0.0f);
        Arrays.fill(this.sLTP_shp2, 0.0f);
        this.sLTP_shp_buf_idx1 = 0;
        this.sLTP_shp_buf_idx2 = 0;
        this.sAR_shp_buf_idx2 = 0;
        this.sLF_AR_shp1 = 0.0f;
        this.sLF_AR_shp2 = 0.0f;
        this.sLF_MA_shp1 = 0.0f;
        this.sLF_MA_shp2 = 0.0f;
        this.sHarmHP = 0.0f;
        this.rand_seed = 0;
        this.lagPrev = 0;
    }
}
