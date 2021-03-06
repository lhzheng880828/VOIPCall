package org.jitsi.impl.neomedia.codec.audio.g729;

class Ld8k {
    static final float AGC_FAC = 0.9875f;
    static final float AGC_FAC1 = 0.012499988f;
    static final float ALPHA = -6.0f;
    static final float BETA = 1.0f;
    static final short BIT_0 = (short) 127;
    static final short BIT_1 = (short) 129;
    static final float CONST12 = 1.2f;
    static final int DIM_RR = 616;
    static final int FIR_SIZE_ANA = 13;
    static final int FIR_SIZE_SYN = 31;
    static final float FLT_MAX_G729 = 1.0E38f;
    static final float FLT_MIN_G729 = -1.0E38f;
    static final int F_UP_PST = 8;
    static final float GAIN_PIT_MAX = 1.2f;
    static final float GAMMA1_0 = 0.98f;
    static final float GAMMA1_1 = 0.94f;
    static final float GAMMA1_PST = 0.7f;
    static final float GAMMA2_0_H = 0.7f;
    static final float GAMMA2_0_L = 0.4f;
    static final float GAMMA2_1 = 0.6f;
    static final float GAMMA2_PST = 0.55f;
    static final float GAMMA3_MINUS = 0.9f;
    static final float GAMMA3_PLUS = 0.2f;
    static final float GAMMA_G = 0.5f;
    static final float GAP1 = 0.0012f;
    static final float GAP2 = 6.0E-4f;
    static final float GAP3 = 0.0392f;
    static final float GP0999 = 0.9999f;
    static final float GPCLIP = 0.95f;
    static final float GPCLIP2 = 0.94f;
    static final int GRID_POINTS = 60;
    static final float INV_COEF = -0.032623f;
    static final float INV_L_SUBFR = 0.025f;
    static final int LH2_L = 16;
    static final int LH2_L_P1 = 17;
    static final int LH2_S = 4;
    static final int LH_UP_L = 8;
    static final int LH_UP_S = 2;
    static final int LONG_H_ST = 20;
    static final int L_FRAME = 80;
    static final int L_INTER10 = 10;
    static final int L_INTER4 = 4;
    static final int L_INTERPOL = 11;
    static final float L_LIMIT = 0.005f;
    static final int L_NEXT = 40;
    static final int L_SUBFR = 40;
    static final int L_SUBFRP1 = 41;
    static final int L_TOTAL = 240;
    static final int L_WINDOW = 240;
    static final int M = 10;
    static final int MAX_TIME = 75;
    static final int MA_NP = 4;
    static final float MEAN_ENER = 36.0f;
    static final int MEM_RES2 = 152;
    static final float MIN_GPLT = 0.6666667f;
    static final int MODE = 2;
    static final int MP1 = 11;
    static final int MSIZE = 64;
    static final float M_LIMIT = 3.135f;
    static final int NB_POS = 8;
    static final int NC = 5;
    static final int NC0 = 128;
    static final int NC0_B = 7;
    static final int NC1 = 32;
    static final int NC1_B = 5;
    static final int NCAN1 = 4;
    static final int NCAN2 = 8;
    static final int NCODE1 = 8;
    static final int NCODE1_B = 3;
    static final int NCODE2 = 16;
    static final int NCODE2_B = 4;
    static final float PI = 3.1415927f;
    static final float PI04 = 0.12566371f;
    static final float PI92 = 2.8902655f;
    static final int PIT_MAX = 143;
    static final int PIT_MIN = 20;
    static final int PRM_SIZE = 11;
    static final int SERIAL_SIZE = 82;
    static final float SHARPMAX = 0.7945f;
    static final float SHARPMIN = 0.2f;
    static final short SIZE_WORD = (short) 80;
    static final int SIZ_RES2 = 192;
    static final int SIZ_TAB_HUP_L = 112;
    static final int SIZ_TAB_HUP_S = 28;
    static final int SIZ_Y_UP = 287;
    static final int STEP = 5;
    static final short SYNC_WORD = (short) 27425;
    static final float THRESCRIT = 0.5f;
    static final float THRESHFCB = 0.4f;
    static final float THRESHPIT = 0.85f;
    static final float THRESH_ERR = 60000.0f;
    static final float THRESH_H1 = 0.65f;
    static final float THRESH_H2 = 0.43f;
    static final float THRESH_L1 = -1.74f;
    static final float THRESH_L2 = -1.52f;
    static final int UP_SAMP = 3;

    Ld8k() {
    }
}
