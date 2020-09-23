package org.jitsi.impl.neomedia.codec.audio.silk;

public class ResamplerPrivateDownFIR {
    static void SKP_Silk_resampler_private_down_FIR(Object SS, short[] out, int out_offset, short[] in, int in_offset, int inLen) {
        int i_djinn;
        int nSamplesIn;
        SKP_Silk_resampler_state_struct S = (SKP_Silk_resampler_state_struct) SS;
        short[] buf1 = new short[240];
        int[] buf2 = new int[492];
        for (i_djinn = 0; i_djinn < 12; i_djinn++) {
            buf2[i_djinn] = S.sFIR[i_djinn];
        }
        short[] FIR_Coefs = S.Coefs;
        int index_increment_Q16 = S.invRatio_Q16;
        while (true) {
            int out_offset2;
            nSamplesIn = Math.min(inLen, S.batchSize);
            if (S.input2x == 1) {
                ResamplerDown2.SKP_Silk_resampler_down2(S.sDown2, 0, buf1, 0, in, in_offset, nSamplesIn);
                nSamplesIn >>= 1;
                ResamplerPrivateAR2.SKP_Silk_resampler_private_AR2(S.sIIR, 0, buf2, 12, buf1, 0, S.Coefs, 0, nSamplesIn);
            } else {
                ResamplerPrivateAR2.SKP_Silk_resampler_private_AR2(S.sIIR, 0, buf2, 12, in, in_offset, S.Coefs, 0, nSamplesIn);
            }
            int max_index_Q16 = nSamplesIn << 16;
            int index_Q16;
            int[] buf_ptr;
            int buf_ptr_offset;
            if (S.FIR_Fracs != 1) {
                index_Q16 = 0;
                while (true) {
                    out_offset2 = out_offset;
                    if (index_Q16 >= max_index_Q16) {
                        break;
                    }
                    buf_ptr = buf2;
                    buf_ptr_offset = index_Q16 >> 16;
                    int interpol_ind = Macros.SKP_SMULWB(65535 & index_Q16, S.FIR_Fracs);
                    short[] interpol_ptr = FIR_Coefs;
                    int interpol_ptr_offset = 2 + (interpol_ind * 6);
                    int res_Q6 = Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMULWB(buf_ptr[buf_ptr_offset], interpol_ptr[interpol_ptr_offset]), buf_ptr[buf_ptr_offset + 1], interpol_ptr[interpol_ptr_offset + 1]), buf_ptr[buf_ptr_offset + 2], interpol_ptr[interpol_ptr_offset + 2]), buf_ptr[buf_ptr_offset + 3], interpol_ptr[interpol_ptr_offset + 3]), buf_ptr[buf_ptr_offset + 4], interpol_ptr[interpol_ptr_offset + 4]), buf_ptr[buf_ptr_offset + 5], interpol_ptr[interpol_ptr_offset + 5]);
                    interpol_ptr = FIR_Coefs;
                    interpol_ptr_offset = 2 + (((S.FIR_Fracs - 1) - interpol_ind) * 6);
                    out_offset = out_offset2 + 1;
                    out[out_offset2] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(res_Q6, buf_ptr[buf_ptr_offset + 11], interpol_ptr[interpol_ptr_offset]), buf_ptr[buf_ptr_offset + 10], interpol_ptr[interpol_ptr_offset + 1]), buf_ptr[buf_ptr_offset + 9], interpol_ptr[interpol_ptr_offset + 2]), buf_ptr[buf_ptr_offset + 8], interpol_ptr[interpol_ptr_offset + 3]), buf_ptr[buf_ptr_offset + 7], interpol_ptr[interpol_ptr_offset + 4]), buf_ptr[buf_ptr_offset + 6], interpol_ptr[interpol_ptr_offset + 5]), 6));
                    index_Q16 += index_increment_Q16;
                }
            } else {
                index_Q16 = 0;
                while (true) {
                    out_offset2 = out_offset;
                    if (index_Q16 >= max_index_Q16) {
                        break;
                    }
                    buf_ptr = buf2;
                    buf_ptr_offset = index_Q16 >> 16;
                    out_offset = out_offset2 + 1;
                    out[out_offset2] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMULWB(buf_ptr[buf_ptr_offset] + buf_ptr[buf_ptr_offset + 11], FIR_Coefs[2]), buf_ptr[buf_ptr_offset + 1] + buf_ptr[buf_ptr_offset + 10], FIR_Coefs[3]), buf_ptr[buf_ptr_offset + 2] + buf_ptr[buf_ptr_offset + 9], FIR_Coefs[4]), buf_ptr[buf_ptr_offset + 3] + buf_ptr[buf_ptr_offset + 8], FIR_Coefs[5]), buf_ptr[buf_ptr_offset + 4] + buf_ptr[buf_ptr_offset + 7], FIR_Coefs[6]), buf_ptr[buf_ptr_offset + 5] + buf_ptr[buf_ptr_offset + 6], FIR_Coefs[7]), 6));
                    index_Q16 += index_increment_Q16;
                }
            }
            out_offset = out_offset2;
            in_offset += nSamplesIn << S.input2x;
            inLen -= nSamplesIn << S.input2x;
            if (inLen <= S.input2x) {
                break;
            }
            for (i_djinn = 0; i_djinn < 12; i_djinn++) {
                buf2[i_djinn] = buf2[nSamplesIn + i_djinn];
            }
        }
        for (i_djinn = 0; i_djinn < 12; i_djinn++) {
            S.sFIR[i_djinn] = buf2[nSamplesIn + i_djinn];
        }
    }
}
