package org.jitsi.impl.neomedia.codec.audio.silk;

import java.util.Arrays;

public class DecodePulses {
    static void SKP_Silk_decode_pulses(SKP_Silk_range_coder_state psRC, SKP_Silk_decoder_control psDecCtrl, int[] q, int frame_length) {
        int i;
        int[] sum_pulses = new int[30];
        int[] nLshifts = new int[30];
        int[] RateLevelIndex_ptr = new int[]{psDecCtrl.RateLevelIndex};
        RangeCoder.SKP_Silk_range_decoder(RateLevelIndex_ptr, 0, psRC, TablesPulsesPerBlock.SKP_Silk_rate_levels_CDF[psDecCtrl.sigtype], 0, 4);
        psDecCtrl.RateLevelIndex = RateLevelIndex_ptr[0];
        int iter = frame_length / 16;
        int[] cdf_ptr = TablesPulsesPerBlock.SKP_Silk_pulses_per_block_CDF[psDecCtrl.RateLevelIndex];
        for (i = 0; i < iter; i++) {
            nLshifts[i] = 0;
            RangeCoder.SKP_Silk_range_decoder(sum_pulses, i, psRC, cdf_ptr, 0, 6);
            while (sum_pulses[i] == 19) {
                nLshifts[i] = nLshifts[i] + 1;
                RangeCoder.SKP_Silk_range_decoder(sum_pulses, i, psRC, TablesPulsesPerBlock.SKP_Silk_pulses_per_block_CDF[9], 0, 6);
            }
        }
        for (i = 0; i < iter; i++) {
            if (sum_pulses[i] > 0) {
                ShellCoder.SKP_Silk_shell_decoder(q, Macros.SKP_SMULBB(i, 16), psRC, sum_pulses[i]);
            } else {
                Arrays.fill(q, Macros.SKP_SMULBB(i, 16), Macros.SKP_SMULBB(i, 16) + 16, 0);
            }
        }
        for (i = 0; i < iter; i++) {
            if (nLshifts[i] > 0) {
                int nLS = nLshifts[i];
                int[] pulses_ptr = q;
                int pulses_ptr_offset = Macros.SKP_SMULBB(i, 16);
                for (int k = 0; k < 16; k++) {
                    int abs_q = pulses_ptr[pulses_ptr_offset + k];
                    for (int j = 0; j < nLS; j++) {
                        abs_q <<= 1;
                        int[] bit_ptr = new int[1];
                        RangeCoder.SKP_Silk_range_decoder(bit_ptr, 0, psRC, TablesOther.SKP_Silk_lsb_CDF, 0, 1);
                        abs_q += bit_ptr[0];
                    }
                    pulses_ptr[pulses_ptr_offset + k] = abs_q;
                }
            }
        }
        CodeSigns.SKP_Silk_decode_signs(psRC, q, frame_length, psDecCtrl.sigtype, psDecCtrl.QuantOffsetType, psDecCtrl.RateLevelIndex);
    }
}
