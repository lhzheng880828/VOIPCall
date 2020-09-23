package org.jitsi.impl.neomedia.codec.audio.silk;

public class TablesOther {
    static final short[] SKP_Silk_Dec_A_HP_12 = new short[]{(short) -16043, (short) 7859};
    static final short[] SKP_Silk_Dec_A_HP_16 = new short[]{(short) -16127, (short) 7940};
    static final short[] SKP_Silk_Dec_A_HP_24 = new short[]{(short) -16220, (short) 8030};
    static final short[] SKP_Silk_Dec_A_HP_8 = new short[]{(short) -15885, (short) 7710};
    static final short[] SKP_Silk_Dec_B_HP_12 = new short[]{(short) 8000, (short) -16000, (short) 8000};
    static final short[] SKP_Silk_Dec_B_HP_16 = new short[]{(short) 8000, (short) -16000, (short) 8000};
    static final short[] SKP_Silk_Dec_B_HP_24 = new short[]{(short) 8000, (short) -16000, (short) 8000};
    static final short[] SKP_Silk_Dec_B_HP_8 = new short[]{(short) 8000, (short) -16000, (short) 8000};
    static final int[] SKP_Silk_FrameTermination_CDF = new int[]{0, 20000, 45000, 56000, 65535};
    static final int SKP_Silk_FrameTermination_offset = 2;
    static final short[] SKP_Silk_LTPScales_table_Q14 = new short[]{(short) 15565, (short) 11469, (short) 8192};
    static final int[] SKP_Silk_LTPscale_CDF = new int[]{0, 32000, 48000, 65535};
    static final int SKP_Silk_LTPscale_offset = 2;
    static final int[] SKP_Silk_NLSF_interpolation_factor_CDF = new int[]{0, 3706, 8703, 19226, 30926, 65535};
    static final int SKP_Silk_NLSF_interpolation_factor_offset = 4;
    static final short[][] SKP_Silk_Quantization_Offsets_Q10 = new short[][]{new short[]{(short) 32, (short) 100}, new short[]{(short) 100, (short) 256}};
    static final short[][] SKP_Silk_SWB_detect_A_HP_Q13 = new short[][]{new short[]{(short) 14613, (short) 6868}, new short[]{(short) 12883, (short) 7337}, new short[]{(short) 11586, (short) 7911}};
    static final short[][] SKP_Silk_SWB_detect_B_HP_Q13 = new short[][]{new short[]{(short) 575, (short) -948, (short) 575}, new short[]{(short) 575, (short) -221, (short) 575}, new short[]{(short) 575, (short) 104, (short) 575}};
    static final int[] SKP_Silk_SamplingRates_CDF = new int[]{0, 16000, 32000, 48000, 65535};
    static final int SKP_Silk_SamplingRates_offset = 2;
    static final int[] SKP_Silk_SamplingRates_table = new int[]{8, 12, 16, 24};
    static final int[] SKP_Silk_Seed_CDF = new int[]{0, 16384, 32768, 49152, 65535};
    static final int SKP_Silk_Seed_offset = 2;
    static final int[][] SKP_Silk_Transition_LP_A_Q28 = new int[][]{new int[]{506393414, 239854379}, new int[]{411067935, 169683996}, new int[]{306733530, 116694253}, new int[]{185807084, 77959395}, new int[]{35497197, 57401098}};
    static final int[][] SKP_Silk_Transition_LP_B_Q28 = new int[][]{new int[]{250767114, 501534038, 250767114}, new int[]{209867381, 419732057, 209867381}, new int[]{170987846, 341967853, 170987846}, new int[]{131531482, 263046905, 131531482}, new int[]{89306658, 178584282, 89306658}};
    static final int[] SKP_Silk_lsb_CDF = new int[]{0, 40000, 65535};
    static final int[] SKP_Silk_vadflag_CDF = new int[]{0, 22000, 65535};
    static final int SKP_Silk_vadflag_offset = 1;
    static final int[] SNR_table_Q1 = new int[]{19, 31, 35, 39, 43, 47, 54, 59};
    static final int[] SNR_table_one_bit_per_sample_Q7 = new int[]{1984, 2240, 2408, 2708};
    static final int[] TargetRate_table_MB = new int[]{0, 10000, 12000, 14000, 17000, 21000, 28000, 100000};
    static final int[] TargetRate_table_NB = new int[]{0, 8000, 9000, 11000, 13000, 16000, 22000, 100000};
    static final int[] TargetRate_table_SWB = new int[]{0, 13000, 16000, 19000, 25000, 32000, 46000, 100000};
    static final int[] TargetRate_table_WB = new int[]{0, 11000, 14000, 17000, 21000, 26000, 36000, 100000};
}
