package org.jitsi.impl.neomedia.codec.audio.silk;

public class AutocorrelationFLP {
    static void SKP_Silk_autocorrelation_FLP(float[] results, int results_offset, float[] inputData, int inputData_offset, int inputDataSize, int correlationCount) {
        if (correlationCount > inputDataSize) {
            correlationCount = inputDataSize;
        }
        for (int i = 0; i < correlationCount; i++) {
            results[results_offset + i] = (float) InnerProductFLP.SKP_Silk_inner_product_FLP(inputData, inputData_offset, inputData, inputData_offset + i, inputDataSize - i);
        }
    }
}
