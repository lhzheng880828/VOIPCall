package org.jitsi.impl.neomedia.codec.audio.silk;

public class RegularizeCorrelationsFLP {
    static void SKP_Silk_regularize_correlations_FLP(float[] XX, int XX_offset, float[] xx, int xx_offset, float noise, int D) {
        for (int i = 0; i < D; i++) {
            int i2 = ((i * D) + XX_offset) + i;
            XX[i2] = XX[i2] + noise;
        }
        xx[xx_offset] = xx[xx_offset] + noise;
    }
}
