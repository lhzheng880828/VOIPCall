package org.jitsi.impl.neomedia.codec.audio.silk;

import org.jitsi.impl.neomedia.device.DeviceConfiguration;

public class ResamplerDown3 {
    static final int ORDER_FIR = 4;

    static void SKP_Silk_resampler_down3(int[] S, int S_offset, short[] out, int out_offset, short[] in, int in_offset, int inLen) {
        int i_djinn;
        int nSamplesIn;
        int[] buf = new int[484];
        for (i_djinn = 0; i_djinn < 4; i_djinn++) {
            buf[i_djinn] = S[S_offset + i_djinn];
        }
        while (true) {
            int i;
            nSamplesIn = Math.min(inLen, DeviceConfiguration.DEFAULT_VIDEO_HEIGHT);
            ResamplerPrivateAR2.SKP_Silk_resampler_private_AR2(S, 4, buf, 4, in, in_offset, ResamplerRom.SKP_Silk_Resampler_1_3_COEFS_LQ, 0, nSamplesIn);
            int buf_ptr = 0;
            int counter = nSamplesIn;
            while (true) {
                i = out_offset;
                if (counter <= 2) {
                    break;
                }
                out_offset = i + 1;
                out[i] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(Macros.SKP_SMULWB(buf[buf_ptr] + buf[buf_ptr + 5], ResamplerRom.SKP_Silk_Resampler_1_3_COEFS_LQ[2]), buf[buf_ptr + 1] + buf[buf_ptr + 4], ResamplerRom.SKP_Silk_Resampler_1_3_COEFS_LQ[3]), buf[buf_ptr + 2] + buf[buf_ptr + 3], ResamplerRom.SKP_Silk_Resampler_1_3_COEFS_LQ[4]), 6));
                buf_ptr += 3;
                counter -= 3;
            }
            in_offset += nSamplesIn;
            inLen -= nSamplesIn;
            if (inLen <= 0) {
                break;
            }
            for (i_djinn = 0; i_djinn < 4; i_djinn++) {
                buf[i_djinn] = buf[nSamplesIn + i_djinn];
            }
            out_offset = i;
        }
        for (i_djinn = 0; i_djinn < 4; i_djinn++) {
            S[S_offset + i_djinn] = buf[nSamplesIn + i_djinn];
        }
    }
}
