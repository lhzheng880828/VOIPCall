package org.jitsi.impl.neomedia.codec.audio.silk;

public class LPCAnalysisFilterFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!LPCAnalysisFilterFLP.class.desiredAssertionStatus());

    static void SKP_Silk_LPC_analysis_filter_FLP(float[] r_LPC, float[] PredCoef, float[] s, int s_offset, int length, int Order) {
        if ($assertionsDisabled || Order <= length) {
            switch (Order) {
                case 8:
                    SKP_Silk_LPC_analysis_filter8_FLP(r_LPC, PredCoef, s, s_offset, length);
                    break;
                case 10:
                    SKP_Silk_LPC_analysis_filter10_FLP(r_LPC, PredCoef, s, s_offset, length);
                    break;
                case 12:
                    SKP_Silk_LPC_analysis_filter12_FLP(r_LPC, PredCoef, s, s_offset, length);
                    break;
                case 16:
                    SKP_Silk_LPC_analysis_filter16_FLP(r_LPC, PredCoef, s, s_offset, length);
                    break;
                default:
                    if (!$assertionsDisabled) {
                        throw new AssertionError();
                    }
                    break;
            }
            for (int i = 0; i < Order; i++) {
                r_LPC[i] = 0.0f;
            }
            return;
        }
        throw new AssertionError();
    }

    static void SKP_Silk_LPC_analysis_filter16_FLP(float[] r_LPC, float[] PredCoef, float[] s, int s_offset, int length) {
        for (int ix = 16; ix < length; ix++) {
            float[] s_ptr = s;
            int s_ptr_offset = (s_offset + ix) - 1;
            r_LPC[ix] = s_ptr[s_ptr_offset + 1] - ((((((((((((((((s_ptr[s_ptr_offset] * PredCoef[0]) + (s_ptr[s_ptr_offset - 1] * PredCoef[1])) + (s_ptr[s_ptr_offset - 2] * PredCoef[2])) + (s_ptr[s_ptr_offset - 3] * PredCoef[3])) + (s_ptr[s_ptr_offset - 4] * PredCoef[4])) + (s_ptr[s_ptr_offset - 5] * PredCoef[5])) + (s_ptr[s_ptr_offset - 6] * PredCoef[6])) + (s_ptr[s_ptr_offset - 7] * PredCoef[7])) + (s_ptr[s_ptr_offset - 8] * PredCoef[8])) + (s_ptr[s_ptr_offset - 9] * PredCoef[9])) + (s_ptr[s_ptr_offset - 10] * PredCoef[10])) + (s_ptr[s_ptr_offset - 11] * PredCoef[11])) + (s_ptr[s_ptr_offset - 12] * PredCoef[12])) + (s_ptr[s_ptr_offset - 13] * PredCoef[13])) + (s_ptr[s_ptr_offset - 14] * PredCoef[14])) + (s_ptr[s_ptr_offset - 15] * PredCoef[15]));
        }
    }

    static void SKP_Silk_LPC_analysis_filter12_FLP(float[] r_LPC, float[] PredCoef, float[] s, int s_offset, int length) {
        for (int ix = 12; ix < length; ix++) {
            float[] s_ptr = s;
            int s_ptr_offset = (s_offset + ix) - 1;
            r_LPC[ix] = s_ptr[s_ptr_offset + 1] - ((((((((((((s_ptr[s_ptr_offset] * PredCoef[0]) + (s_ptr[s_ptr_offset - 1] * PredCoef[1])) + (s_ptr[s_ptr_offset - 2] * PredCoef[2])) + (s_ptr[s_ptr_offset - 3] * PredCoef[3])) + (s_ptr[s_ptr_offset - 4] * PredCoef[4])) + (s_ptr[s_ptr_offset - 5] * PredCoef[5])) + (s_ptr[s_ptr_offset - 6] * PredCoef[6])) + (s_ptr[s_ptr_offset - 7] * PredCoef[7])) + (s_ptr[s_ptr_offset - 8] * PredCoef[8])) + (s_ptr[s_ptr_offset - 9] * PredCoef[9])) + (s_ptr[s_ptr_offset - 10] * PredCoef[10])) + (s_ptr[s_ptr_offset - 11] * PredCoef[11]));
        }
    }

    static void SKP_Silk_LPC_analysis_filter10_FLP(float[] r_LPC, float[] PredCoef, float[] s, int s_offset, int length) {
        for (int ix = 10; ix < length; ix++) {
            float[] s_ptr = s;
            int s_ptr_offset = (s_offset + ix) - 1;
            r_LPC[ix] = s_ptr[s_ptr_offset + 1] - ((((((((((s_ptr[s_ptr_offset] * PredCoef[0]) + (s_ptr[s_ptr_offset - 1] * PredCoef[1])) + (s_ptr[s_ptr_offset - 2] * PredCoef[2])) + (s_ptr[s_ptr_offset - 3] * PredCoef[3])) + (s_ptr[s_ptr_offset - 4] * PredCoef[4])) + (s_ptr[s_ptr_offset - 5] * PredCoef[5])) + (s_ptr[s_ptr_offset - 6] * PredCoef[6])) + (s_ptr[s_ptr_offset - 7] * PredCoef[7])) + (s_ptr[s_ptr_offset - 8] * PredCoef[8])) + (s_ptr[s_ptr_offset - 9] * PredCoef[9]));
        }
    }

    static void SKP_Silk_LPC_analysis_filter8_FLP(float[] r_LPC, float[] PredCoef, float[] s, int s_offset, int length) {
        for (int ix = 8; ix < length; ix++) {
            float[] s_ptr = s;
            int s_ptr_offset = (s_offset + ix) - 1;
            r_LPC[ix] = s_ptr[s_ptr_offset + 1] - ((((((((s_ptr[s_ptr_offset] * PredCoef[0]) + (s_ptr[s_ptr_offset - 1] * PredCoef[1])) + (s_ptr[s_ptr_offset - 2] * PredCoef[2])) + (s_ptr[s_ptr_offset - 3] * PredCoef[3])) + (s_ptr[s_ptr_offset - 4] * PredCoef[4])) + (s_ptr[s_ptr_offset - 5] * PredCoef[5])) + (s_ptr[s_ptr_offset - 6] * PredCoef[6])) + (s_ptr[s_ptr_offset - 7] * PredCoef[7]));
        }
    }
}
