package org.jitsi.impl.neomedia.codec.audio.silk;

public class CodeSigns {
    static int SKP_enc_map(int a) {
        return (a >> 15) + 1;
    }

    static int SKP_dec_map(int a) {
        return (a << 1) - 1;
    }

    static void SKP_Silk_encode_signs(SKP_Silk_range_coder_state sRC, byte[] q, int length, int sigtype, int QuantOffsetType, int RateLevelIndex) {
        cdf = new int[3];
        int i = Macros.SKP_SMULBB(9, (sigtype << 1) + QuantOffsetType) + RateLevelIndex;
        cdf[0] = 0;
        cdf[1] = TablesSign.SKP_Silk_sign_CDF[i];
        cdf[2] = 65535;
        for (i = 0; i < length; i++) {
            if (q[i] != (byte) 0) {
                RangeCoder.SKP_Silk_range_encoder(sRC, (q[i] >> 15) + 1, cdf, 0);
            }
        }
    }

    static void SKP_Silk_decode_signs(SKP_Silk_range_coder_state sRC, int[] q, int length, int sigtype, int QuantOffsetType, int RateLevelIndex) {
        int[] data_ptr = new int[1];
        cdf = new int[3];
        int i = Macros.SKP_SMULBB(9, (sigtype << 1) + QuantOffsetType) + RateLevelIndex;
        cdf[0] = 0;
        cdf[1] = TablesSign.SKP_Silk_sign_CDF[i];
        cdf[2] = 65535;
        for (i = 0; i < length; i++) {
            if (q[i] > 0) {
                RangeCoder.SKP_Silk_range_decoder(data_ptr, 0, sRC, cdf, 0, 1);
                q[i] = q[i] * ((data_ptr[0] << 1) - 1);
            }
        }
    }
}
