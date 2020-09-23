package org.jitsi.impl.neomedia.codec.audio.silk;

public class PitchEstTables {
    static short[][] SKP_Silk_CB_lags_stage2 = new short[][]{new short[]{(short) 0, (short) 2, (short) -1, (short) -1, (short) -1, (short) 0, (short) 0, (short) 1, (short) 1, (short) 0, (short) 1}, new short[]{(short) 0, (short) 1, (short) 0, (short) 0, (short) 0, (short) 0, (short) 0, (short) 1, (short) 0, (short) 0, (short) 0}, new short[]{(short) 0, (short) 0, (short) 1, (short) 0, (short) 0, (short) 0, (short) 1, (short) 0, (short) 0, (short) 0, (short) 0}, new short[]{(short) 0, (short) -1, (short) 2, (short) 1, (short) 0, (short) 1, (short) 1, (short) 0, (short) 0, (short) -1, (short) -1}};
    static short[][] SKP_Silk_CB_lags_stage3 = new short[][]{new short[]{(short) -9, (short) -7, (short) -6, (short) -5, (short) -5, (short) -4, (short) -4, (short) -3, (short) -3, (short) -2, (short) -2, (short) -2, (short) -1, (short) -1, (short) -1, (short) 0, (short) 0, (short) 0, (short) 1, (short) 1, (short) 0, (short) 1, (short) 2, (short) 2, (short) 2, (short) 3, (short) 3, (short) 4, (short) 4, (short) 5, (short) 6, (short) 5, (short) 6, (short) 8}, new short[]{(short) -3, (short) -2, (short) -2, (short) -2, (short) -1, (short) -1, (short) -1, (short) -1, (short) -1, (short) 0, (short) 0, (short) -1, (short) 0, (short) 0, (short) 0, (short) 0, (short) 0, (short) 0, (short) 1, (short) 0, (short) 0, (short) 0, (short) 1, (short) 1, (short) 0, (short) 1, (short) 1, (short) 2, (short) 1, (short) 2, (short) 2, (short) 2, (short) 2, (short) 3}, new short[]{(short) 3, (short) 3, (short) 2, (short) 2, (short) 2, (short) 2, (short) 1, (short) 2, (short) 1, (short) 1, (short) 0, (short) 1, (short) 1, (short) 0, (short) 0, (short) 0, (short) 1, (short) 0, (short) 0, (short) 0, (short) 0, (short) 0, (short) 0, (short) -1, (short) 0, (short) 0, (short) -1, (short) -1, (short) -1, (short) -1, (short) -1, (short) -2, (short) -2, (short) -2}, new short[]{(short) 9, (short) 8, (short) 6, (short) 5, (short) 6, (short) 5, (short) 4, (short) 4, (short) 3, (short) 3, (short) 2, (short) 2, (short) 2, (short) 1, (short) 0, (short) 1, (short) 1, (short) 0, (short) 0, (short) 0, (short) -1, (short) -1, (short) -1, (short) -2, (short) -2, (short) -2, (short) -3, (short) -3, (short) -4, (short) -4, (short) -5, (short) -5, (short) -6, (short) -7}};
    static short[][][] SKP_Silk_Lag_range_stage3;
    static short[] SKP_Silk_cbk_offsets_stage3 = new short[]{(short) 9, (short) 5, (short) 0};
    static short[] SKP_Silk_cbk_sizes_stage3 = new short[]{(short) 16, (short) 24, (short) 34};

    static {
        r0 = new short[3][][];
        r0[0] = new short[][]{new short[]{(short) -2, (short) 6}, new short[]{(short) -1, (short) 5}, new short[]{(short) -1, (short) 5}, new short[]{(short) -2, (short) 7}};
        r0[1] = new short[][]{new short[]{(short) -4, (short) 8}, new short[]{(short) -1, (short) 6}, new short[]{(short) -1, (short) 6}, new short[]{(short) -4, (short) 9}};
        r0[2] = new short[][]{new short[]{(short) -9, (short) 12}, new short[]{(short) -3, (short) 7}, new short[]{(short) -2, (short) 7}, new short[]{(short) -7, (short) 13}};
        SKP_Silk_Lag_range_stage3 = r0;
    }
}
