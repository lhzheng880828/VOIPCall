package org.jitsi.impl.neomedia.codec.audio.silk;

public class CorrMatrixFLP {
    static void SKP_Silk_corrVector_FLP(float[] x, int x_offset, float[] t, int t_offset, int L, int Order, float[] Xt) {
        float[] ptr1 = x;
        int ptr1_offset = (x_offset + Order) - 1;
        for (int lag = 0; lag < Order; lag++) {
            Xt[lag] = (float) InnerProductFLP.SKP_Silk_inner_product_FLP(ptr1, ptr1_offset, t, t_offset, L);
            ptr1_offset--;
        }
    }

    static void SKP_Silk_corrMatrix_FLP(float[] x, int x_offset, int L, int Order, float[] XX, int XX_offset) {
        int j;
        float[] ptr1 = x;
        int ptr1_offset = (x_offset + Order) - 1;
        double energy = EnergyFLP.SKP_Silk_energy_FLP(ptr1, ptr1_offset, L);
        XX[XX_offset + 0] = (float) energy;
        for (j = 1; j < Order; j++) {
            energy += (double) ((ptr1[ptr1_offset - j] * ptr1[ptr1_offset - j]) - (ptr1[(ptr1_offset + L) - j] * ptr1[(ptr1_offset + L) - j]));
            XX[((j * Order) + XX_offset) + j] = (float) energy;
        }
        float[] ptr2 = x;
        int ptr2_offset = (x_offset + Order) - 2;
        for (int lag = 1; lag < Order; lag++) {
            energy = InnerProductFLP.SKP_Silk_inner_product_FLP(ptr1, ptr1_offset, ptr2, ptr2_offset, L);
            for (j = 1; j < Order - lag; j++) {
                energy += (double) ((ptr1[ptr1_offset - j] * ptr2[ptr2_offset - j]) - (ptr1[(ptr1_offset + L) - j] * ptr2[(ptr2_offset + L) - j]));
                XX[(((lag + j) * Order) + XX_offset) + j] = (float) energy;
                XX[((j * Order) + XX_offset) + (lag + j)] = (float) energy;
            }
            ptr2_offset--;
        }
    }
}
