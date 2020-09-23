package org.jitsi.impl.neomedia.codec.audio.silk;

import com.lti.utils.UnsignedUtils;

public class ResamplerPrivateIIRFIR {
    static void SKP_Silk_resampler_private_IIR_FIR(Object SS, short[] out, int out_offset, short[] in, int in_offset, int inLen) {
        int i_djinn;
        int nSamplesIn;
        SKP_Silk_resampler_state_struct S = (SKP_Silk_resampler_state_struct) SS;
        short[] buf = new short[966];
        for (i_djinn = 0; i_djinn < 6; i_djinn++) {
            buf[i_djinn * 2] = (short) (S.sFIR[i_djinn] & 65535);
            buf[(i_djinn * 2) + 1] = (short) (S.sFIR[i_djinn] >>> 16);
        }
        int index_increment_Q16 = S.invRatio_Q16;
        while (true) {
            int out_offset2;
            nSamplesIn = Math.min(inLen, S.batchSize);
            if (S.input2x == 1) {
                S.up2_function(S.sIIR, buf, 6, in, in_offset, nSamplesIn);
            } else {
                ResamplerPrivateARMA4.SKP_Silk_resampler_private_ARMA4(S.sIIR, 0, buf, 6, in, in_offset, S.Coefs, 0, nSamplesIn);
            }
            int max_index_Q16 = nSamplesIn << (S.input2x + 16);
            int index_Q16 = 0;
            while (true) {
                out_offset2 = out_offset;
                if (index_Q16 >= max_index_Q16) {
                    break;
                }
                int table_index = Macros.SKP_SMULWB(65535 & index_Q16, 144);
                int buf_ptr = index_Q16 >> 16;
                out_offset = out_offset2 + 1;
                out[out_offset2] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(Macros.SKP_SMLABB(Macros.SKP_SMLABB(Macros.SKP_SMLABB(Macros.SKP_SMLABB(Macros.SKP_SMLABB(Macros.SKP_SMULBB(buf[buf_ptr], ResamplerRom.SKP_Silk_resampler_frac_FIR_144[table_index][0]), buf[buf_ptr + 1], ResamplerRom.SKP_Silk_resampler_frac_FIR_144[table_index][1]), buf[buf_ptr + 2], ResamplerRom.SKP_Silk_resampler_frac_FIR_144[table_index][2]), buf[buf_ptr + 3], ResamplerRom.SKP_Silk_resampler_frac_FIR_144[143 - table_index][2]), buf[buf_ptr + 4], ResamplerRom.SKP_Silk_resampler_frac_FIR_144[143 - table_index][1]), buf[buf_ptr + 5], ResamplerRom.SKP_Silk_resampler_frac_FIR_144[143 - table_index][0]), 15));
                index_Q16 += index_increment_Q16;
            }
            in_offset += nSamplesIn;
            inLen -= nSamplesIn;
            if (inLen <= 0) {
                break;
            }
            for (i_djinn = 0; i_djinn < 6; i_djinn++) {
                buf[i_djinn] = buf[(nSamplesIn << S.input2x) + i_djinn];
            }
            out_offset = out_offset2;
        }
        for (i_djinn = 0; i_djinn < 6; i_djinn++) {
            S.sFIR[i_djinn] = buf[(nSamplesIn << S.input2x) + (i_djinn * 2)] & UnsignedUtils.MAX_UBYTE;
            int[] iArr = S.sFIR;
            iArr[i_djinn] = iArr[i_djinn] | ((((buf[(nSamplesIn << S.input2x) + (i_djinn * 2)] >> 8) & UnsignedUtils.MAX_UBYTE) << 8) & 65280);
            iArr = S.sFIR;
            iArr[i_djinn] = iArr[i_djinn] | ((((buf[((nSamplesIn << S.input2x) + (i_djinn * 2)) + 1] >> 0) & UnsignedUtils.MAX_UBYTE) << 16) & 16711680);
            iArr = S.sFIR;
            iArr[i_djinn] = iArr[i_djinn] | ((((buf[((nSamplesIn << S.input2x) + (i_djinn * 2)) + 1] >> 8) & UnsignedUtils.MAX_UBYTE) << 24) & -16777216);
        }
    }
}
