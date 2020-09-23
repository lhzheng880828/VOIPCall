package org.jitsi.impl.neomedia.codec.audio.silk;

import java.util.Arrays;

/* compiled from: Structs */
class SKP_Silk_nsq_state implements Cloneable {
    int lagPrev;
    int prev_inv_gain_Q16;
    int rand_seed;
    int rewhite_flag;
    int[] sAR2_Q14 = new int[16];
    int sLF_AR_shp_Q12;
    int[] sLPC_Q14 = new int[136];
    int sLTP_buf_idx;
    int[] sLTP_shp_Q10 = new int[960];
    int sLTP_shp_buf_idx;
    short[] xq = new short[960];

    SKP_Silk_nsq_state() {
    }

    public Object clone() {
        SKP_Silk_nsq_state clone = null;
        try {
            return (SKP_Silk_nsq_state) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return clone;
        }
    }

    public void memZero() {
        Arrays.fill(this.sAR2_Q14, 0);
        Arrays.fill(this.sLPC_Q14, 0);
        Arrays.fill(this.sLTP_shp_Q10, 0);
        Arrays.fill(this.xq, (short) 0);
        this.lagPrev = 0;
        this.prev_inv_gain_Q16 = 0;
        this.rand_seed = 0;
        this.rewhite_flag = 0;
        this.sLF_AR_shp_Q12 = 0;
        this.sLTP_buf_idx = 0;
        this.sLTP_shp_buf_idx = 0;
    }
}
