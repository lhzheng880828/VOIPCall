package org.jitsi.impl.neomedia.codec.audio.ilbc;

import java.lang.reflect.Array;

class ilbc_ulp {
    int blockl;
    int[][][] cb_gain;
    int[][][] cb_index;
    int[][] extra_cb_gain;
    int[][] extra_cb_index;
    int lpc_n;
    int[][] lsf_bits;
    int mode;
    int nasub;
    int no_of_bytes;
    int no_of_words;
    int nsub;
    int[] scale_bits;
    int[] start_bits;
    int[] startfirst_bits;
    int[] state_bits;
    int state_short_len;

    public ilbc_ulp(int init_mode) {
        this.mode = init_mode;
        if (this.mode == 20 || this.mode == 30) {
            this.lsf_bits = (int[][]) Array.newInstance(Integer.TYPE, new int[]{6, ilbc_constants.ULP_CLASSES + 2});
            this.start_bits = new int[(ilbc_constants.ULP_CLASSES + 2)];
            this.startfirst_bits = new int[(ilbc_constants.ULP_CLASSES + 2)];
            this.scale_bits = new int[(ilbc_constants.ULP_CLASSES + 2)];
            this.state_bits = new int[(ilbc_constants.ULP_CLASSES + 2)];
            this.extra_cb_index = (int[][]) Array.newInstance(Integer.TYPE, new int[]{ilbc_constants.CB_NSTAGES, ilbc_constants.ULP_CLASSES + 2});
            this.extra_cb_gain = (int[][]) Array.newInstance(Integer.TYPE, new int[]{ilbc_constants.CB_NSTAGES, ilbc_constants.ULP_CLASSES + 2});
            this.cb_index = (int[][][]) Array.newInstance(Integer.TYPE, new int[]{ilbc_constants.NSUB_MAX, ilbc_constants.CB_NSTAGES, ilbc_constants.ULP_CLASSES + 2});
            this.cb_gain = (int[][][]) Array.newInstance(Integer.TYPE, new int[]{ilbc_constants.NSUB_MAX, ilbc_constants.CB_NSTAGES, ilbc_constants.ULP_CLASSES + 2});
            if (this.mode == 20) {
                this.blockl = ilbc_constants.BLOCKL_20MS;
                this.nsub = ilbc_constants.NSUB_20MS;
                this.nasub = ilbc_constants.NASUB_20MS;
                this.lpc_n = ilbc_constants.LPC_N_20MS;
                this.no_of_bytes = 38;
                this.no_of_words = ilbc_constants.NO_OF_WORDS_20MS;
                this.state_short_len = ilbc_constants.STATE_SHORT_LEN_20MS;
                System.arraycopy(ilbc_constants.lsf_bits_20ms, 0, this.lsf_bits, 0, 6);
                System.arraycopy(ilbc_constants.start_bits_20ms, 0, this.start_bits, 0, ilbc_constants.start_bits_20ms.length);
                System.arraycopy(ilbc_constants.startfirst_bits_20ms, 0, this.startfirst_bits, 0, ilbc_constants.startfirst_bits_20ms.length);
                System.arraycopy(ilbc_constants.scale_bits_20ms, 0, this.scale_bits, 0, ilbc_constants.scale_bits_20ms.length);
                System.arraycopy(ilbc_constants.state_bits_20ms, 0, this.state_bits, 0, ilbc_constants.state_bits_20ms.length);
                System.arraycopy(ilbc_constants.extra_cb_index_20ms, 0, this.extra_cb_index, 0, ilbc_constants.CB_NSTAGES);
                System.arraycopy(ilbc_constants.extra_cb_gain_20ms, 0, this.extra_cb_gain, 0, ilbc_constants.CB_NSTAGES);
                System.arraycopy(ilbc_constants.cb_index_20ms, 0, this.cb_index, 0, ilbc_constants.NSUB_20MS);
                System.arraycopy(ilbc_constants.cb_gain_20ms, 0, this.cb_gain, 0, ilbc_constants.NSUB_20MS);
                return;
            } else if (this.mode == 30) {
                this.blockl = ilbc_constants.BLOCKL_30MS;
                this.nsub = ilbc_constants.NSUB_30MS;
                this.nasub = ilbc_constants.NASUB_30MS;
                this.lpc_n = ilbc_constants.LPC_N_30MS;
                this.no_of_bytes = 50;
                this.no_of_words = ilbc_constants.NO_OF_WORDS_30MS;
                this.state_short_len = ilbc_constants.STATE_SHORT_LEN_30MS;
                System.arraycopy(ilbc_constants.lsf_bits_30ms, 0, this.lsf_bits, 0, 6);
                System.arraycopy(ilbc_constants.start_bits_30ms, 0, this.start_bits, 0, ilbc_constants.start_bits_30ms.length);
                System.arraycopy(ilbc_constants.startfirst_bits_30ms, 0, this.startfirst_bits, 0, ilbc_constants.startfirst_bits_30ms.length);
                System.arraycopy(ilbc_constants.scale_bits_30ms, 0, this.scale_bits, 0, ilbc_constants.scale_bits_30ms.length);
                System.arraycopy(ilbc_constants.state_bits_30ms, 0, this.state_bits, 0, ilbc_constants.state_bits_30ms.length);
                System.arraycopy(ilbc_constants.extra_cb_index_30ms, 0, this.extra_cb_index, 0, ilbc_constants.CB_NSTAGES);
                System.arraycopy(ilbc_constants.extra_cb_gain_30ms, 0, this.extra_cb_gain, 0, ilbc_constants.CB_NSTAGES);
                System.arraycopy(ilbc_constants.cb_index_30ms, 0, this.cb_index, 0, ilbc_constants.NSUB_30MS);
                System.arraycopy(ilbc_constants.cb_gain_30ms, 0, this.cb_gain, 0, ilbc_constants.NSUB_30MS);
                return;
            } else {
                return;
            }
        }
        System.out.println("Unknown mode " + init_mode);
    }
}
