package org.jitsi.impl.neomedia.codec.audio.silk;

public class ShellCoder {
    static void combine_pulses(int[] out, int out_offset, int[] in, int in_offset, int len) {
        for (int k = 0; k < len; k++) {
            out[out_offset + k] = in[(k * 2) + in_offset] + in[((k * 2) + in_offset) + 1];
        }
    }

    static void encode_split(SKP_Silk_range_coder_state sRC, int p_child1, int p, int[] shell_table) {
        if (p > 0) {
            RangeCoder.SKP_Silk_range_encoder(sRC, p_child1, shell_table, TablesPulsesPerBlock.SKP_Silk_shell_code_table_offsets[p]);
        }
    }

    static void decode_split(int[] p_child1, int p_child1_offset, int[] p_child2, int p_child2_offset, SKP_Silk_range_coder_state sRC, int p, int[] shell_table) {
        if (p > 0) {
            int cdf_middle = p >> 1;
            RangeCoder.SKP_Silk_range_decoder(p_child1, p_child1_offset, sRC, shell_table, TablesPulsesPerBlock.SKP_Silk_shell_code_table_offsets[p], cdf_middle);
            p_child2[p_child2_offset + 0] = p - p_child1[p_child1_offset + 0];
            return;
        }
        p_child1[p_child1_offset + 0] = 0;
        p_child2[p_child2_offset + 0] = 0;
    }

    static void SKP_Silk_shell_encoder(SKP_Silk_range_coder_state sRC, int[] pulses0, int pulses0_offset) {
        int[] pulses1 = new int[8];
        int[] pulses2 = new int[4];
        int[] pulses3 = new int[2];
        int[] pulses4 = new int[1];
        combine_pulses(pulses1, 0, pulses0, pulses0_offset, 8);
        combine_pulses(pulses2, 0, pulses1, 0, 4);
        combine_pulses(pulses3, 0, pulses2, 0, 2);
        combine_pulses(pulses4, 0, pulses3, 0, 1);
        encode_split(sRC, pulses3[0], pulses4[0], TablesPulsesPerBlock.SKP_Silk_shell_code_table3);
        encode_split(sRC, pulses2[0], pulses3[0], TablesPulsesPerBlock.SKP_Silk_shell_code_table2);
        encode_split(sRC, pulses1[0], pulses2[0], TablesPulsesPerBlock.SKP_Silk_shell_code_table1);
        encode_split(sRC, pulses0[pulses0_offset + 0], pulses1[0], TablesPulsesPerBlock.SKP_Silk_shell_code_table0);
        encode_split(sRC, pulses0[pulses0_offset + 2], pulses1[1], TablesPulsesPerBlock.SKP_Silk_shell_code_table0);
        encode_split(sRC, pulses1[2], pulses2[1], TablesPulsesPerBlock.SKP_Silk_shell_code_table1);
        encode_split(sRC, pulses0[pulses0_offset + 4], pulses1[2], TablesPulsesPerBlock.SKP_Silk_shell_code_table0);
        encode_split(sRC, pulses0[pulses0_offset + 6], pulses1[3], TablesPulsesPerBlock.SKP_Silk_shell_code_table0);
        encode_split(sRC, pulses2[2], pulses3[1], TablesPulsesPerBlock.SKP_Silk_shell_code_table2);
        encode_split(sRC, pulses1[4], pulses2[2], TablesPulsesPerBlock.SKP_Silk_shell_code_table1);
        encode_split(sRC, pulses0[pulses0_offset + 8], pulses1[4], TablesPulsesPerBlock.SKP_Silk_shell_code_table0);
        encode_split(sRC, pulses0[pulses0_offset + 10], pulses1[5], TablesPulsesPerBlock.SKP_Silk_shell_code_table0);
        encode_split(sRC, pulses1[6], pulses2[3], TablesPulsesPerBlock.SKP_Silk_shell_code_table1);
        encode_split(sRC, pulses0[pulses0_offset + 12], pulses1[6], TablesPulsesPerBlock.SKP_Silk_shell_code_table0);
        encode_split(sRC, pulses0[pulses0_offset + 14], pulses1[7], TablesPulsesPerBlock.SKP_Silk_shell_code_table0);
    }

    static void SKP_Silk_shell_decoder(int[] pulses0, int pulses0_offset, SKP_Silk_range_coder_state sRC, int pulses4) {
        int[] pulses3 = new int[2];
        int[] pulses2 = new int[4];
        int[] pulses1 = new int[8];
        Typedef.SKP_assert(true);
        decode_split(pulses3, 0, pulses3, 1, sRC, pulses4, TablesPulsesPerBlock.SKP_Silk_shell_code_table3);
        decode_split(pulses2, 0, pulses2, 1, sRC, pulses3[0], TablesPulsesPerBlock.SKP_Silk_shell_code_table2);
        decode_split(pulses1, 0, pulses1, 1, sRC, pulses2[0], TablesPulsesPerBlock.SKP_Silk_shell_code_table1);
        decode_split(pulses0, pulses0_offset + 0, pulses0, pulses0_offset + 1, sRC, pulses1[0], TablesPulsesPerBlock.SKP_Silk_shell_code_table0);
        decode_split(pulses0, pulses0_offset + 2, pulses0, pulses0_offset + 3, sRC, pulses1[1], TablesPulsesPerBlock.SKP_Silk_shell_code_table0);
        decode_split(pulses1, 2, pulses1, 3, sRC, pulses2[1], TablesPulsesPerBlock.SKP_Silk_shell_code_table1);
        decode_split(pulses0, pulses0_offset + 4, pulses0, pulses0_offset + 5, sRC, pulses1[2], TablesPulsesPerBlock.SKP_Silk_shell_code_table0);
        decode_split(pulses0, pulses0_offset + 6, pulses0, pulses0_offset + 7, sRC, pulses1[3], TablesPulsesPerBlock.SKP_Silk_shell_code_table0);
        decode_split(pulses2, 2, pulses2, 3, sRC, pulses3[1], TablesPulsesPerBlock.SKP_Silk_shell_code_table2);
        decode_split(pulses1, 4, pulses1, 5, sRC, pulses2[2], TablesPulsesPerBlock.SKP_Silk_shell_code_table1);
        decode_split(pulses0, pulses0_offset + 8, pulses0, pulses0_offset + 9, sRC, pulses1[4], TablesPulsesPerBlock.SKP_Silk_shell_code_table0);
        decode_split(pulses0, pulses0_offset + 10, pulses0, pulses0_offset + 11, sRC, pulses1[5], TablesPulsesPerBlock.SKP_Silk_shell_code_table0);
        decode_split(pulses1, 6, pulses1, 7, sRC, pulses2[3], TablesPulsesPerBlock.SKP_Silk_shell_code_table1);
        decode_split(pulses0, pulses0_offset + 12, pulses0, pulses0_offset + 13, sRC, pulses1[6], TablesPulsesPerBlock.SKP_Silk_shell_code_table0);
        decode_split(pulses0, pulses0_offset + 14, pulses0, pulses0_offset + 15, sRC, pulses1[7], TablesPulsesPerBlock.SKP_Silk_shell_code_table0);
    }
}
