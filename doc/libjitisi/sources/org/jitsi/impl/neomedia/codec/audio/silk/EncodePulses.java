package org.jitsi.impl.neomedia.codec.audio.silk;

import org.jitsi.impl.neomedia.device.DeviceConfiguration;

public class EncodePulses {
    static int combine_and_check(int[] pulses_comb, int[] pulses_in, int pulses_in_offset, int max_pulses, int len) {
        for (int k = 0; k < len; k++) {
            int sum = pulses_in[(k * 2) + pulses_in_offset] + pulses_in[((k * 2) + pulses_in_offset) + 1];
            if (sum > max_pulses) {
                return 1;
            }
            pulses_comb[k] = sum;
        }
        return 0;
    }

    static void SKP_Silk_encode_pulses(SKP_Silk_range_coder_state psRC, int sigtype, int QuantOffsetType, byte[] q, int frame_length) {
        int i;
        int k;
        int RateLevelIndex = 0;
        int[] abs_pulses = new int[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
        int[] sum_pulses = new int[30];
        int[] nRshifts = new int[30];
        int[] pulses_comb = new int[8];
        int iter = frame_length / 16;
        for (i = 0; i < frame_length; i += 4) {
            abs_pulses[i + 0] = q[i + 0] > (byte) 0 ? q[i + 0] : -q[i + 0];
            abs_pulses[i + 1] = q[i + 1] > (byte) 0 ? q[i + 1] : -q[i + 1];
            abs_pulses[i + 2] = q[i + 2] > (byte) 0 ? q[i + 2] : -q[i + 2];
            abs_pulses[i + 3] = q[i + 3] > (byte) 0 ? q[i + 3] : -q[i + 3];
        }
        int[] abs_pulses_ptr = abs_pulses;
        int abs_pulses_ptr_offset = 0;
        for (i = 0; i < iter; i++) {
            nRshifts[i] = 0;
            while (true) {
                int scale_down = (combine_and_check(pulses_comb, abs_pulses_ptr, abs_pulses_ptr_offset, TablesPulsesPerBlock.SKP_Silk_max_pulses_table[0], 8) + combine_and_check(pulses_comb, pulses_comb, 0, TablesPulsesPerBlock.SKP_Silk_max_pulses_table[1], 4)) + combine_and_check(pulses_comb, pulses_comb, 0, TablesPulsesPerBlock.SKP_Silk_max_pulses_table[2], 2);
                sum_pulses[i] = pulses_comb[0] + pulses_comb[1];
                if (sum_pulses[i] > TablesPulsesPerBlock.SKP_Silk_max_pulses_table[3]) {
                    scale_down++;
                }
                if (scale_down == 0) {
                    break;
                }
                nRshifts[i] = nRshifts[i] + 1;
                for (k = 0; k < 16; k++) {
                    abs_pulses_ptr[abs_pulses_ptr_offset + k] = abs_pulses_ptr[abs_pulses_ptr_offset + k] >> 1;
                }
            }
            abs_pulses_ptr_offset += 16;
        }
        int minSumBits_Q6 = Integer.MAX_VALUE;
        for (k = 0; k < 9; k++) {
            short[] nBits_ptr = TablesPulsesPerBlock.SKP_Silk_pulses_per_block_BITS_Q6[k];
            int sumBits_Q6 = TablesPulsesPerBlock.SKP_Silk_rate_levels_BITS_Q6[sigtype][k];
            for (i = 0; i < iter; i++) {
                int i2;
                if (nRshifts[i] > 0) {
                    i2 = nBits_ptr[19];
                } else {
                    i2 = nBits_ptr[sum_pulses[i]];
                }
                sumBits_Q6 += i2;
            }
            if (sumBits_Q6 < minSumBits_Q6) {
                minSumBits_Q6 = sumBits_Q6;
                RateLevelIndex = k;
            }
        }
        RangeCoder.SKP_Silk_range_encoder(psRC, RateLevelIndex, TablesPulsesPerBlock.SKP_Silk_rate_levels_CDF[sigtype], 0);
        int[] cdf_ptr = TablesPulsesPerBlock.SKP_Silk_pulses_per_block_CDF[RateLevelIndex];
        for (i = 0; i < iter; i++) {
            if (nRshifts[i] == 0) {
                RangeCoder.SKP_Silk_range_encoder(psRC, sum_pulses[i], cdf_ptr, 0);
            } else {
                RangeCoder.SKP_Silk_range_encoder(psRC, 19, cdf_ptr, 0);
                for (k = 0; k < nRshifts[i] - 1; k++) {
                    RangeCoder.SKP_Silk_range_encoder(psRC, 19, TablesPulsesPerBlock.SKP_Silk_pulses_per_block_CDF[9], 0);
                }
                RangeCoder.SKP_Silk_range_encoder(psRC, sum_pulses[i], TablesPulsesPerBlock.SKP_Silk_pulses_per_block_CDF[9], 0);
            }
        }
        for (i = 0; i < iter; i++) {
            if (sum_pulses[i] > 0) {
                ShellCoder.SKP_Silk_shell_encoder(psRC, abs_pulses, i * 16);
            }
        }
        for (i = 0; i < iter; i++) {
            if (nRshifts[i] > 0) {
                byte[] pulses_ptr = q;
                int pulses_ptr_offset = i * 16;
                int nLS = nRshifts[i] - 1;
                for (k = 0; k < 16; k++) {
                    int abs_q = pulses_ptr[pulses_ptr_offset + k] > (byte) 0 ? pulses_ptr[pulses_ptr_offset + k] : -pulses_ptr[pulses_ptr_offset + k];
                    for (int j = nLS; j > 0; j--) {
                        RangeCoder.SKP_Silk_range_encoder(psRC, (abs_q >> j) & 1, TablesOther.SKP_Silk_lsb_CDF, 0);
                    }
                    RangeCoder.SKP_Silk_range_encoder(psRC, abs_q & 1, TablesOther.SKP_Silk_lsb_CDF, 0);
                }
            }
        }
        CodeSigns.SKP_Silk_encode_signs(psRC, q, frame_length, sigtype, QuantOffsetType, RateLevelIndex);
    }
}
