package org.jitsi.impl.neomedia.codec.audio.silk;

public class Define {
    static final int ACCUM_BITS_DIFF_THRESHOLD = 30000000;
    static final int BWE_AFTER_LOSS_Q16 = 63570;
    static final int CNG_BUF_MASK_MAX = 255;
    static final int CNG_GAIN_SMTH_Q16 = 4634;
    static final int CNG_NLSF_SMTH_Q16 = 16348;
    static final int CONCEC_SWB_SMPLS_THRES = 7200;
    static final int DECISION_DELAY = 32;
    static final int DECISION_DELAY_MASK = 31;
    static final int DEC_HP_ORDER = 2;
    static final int DEL_DEC_STATES_MAX = 4;
    static final int FIND_PITCH_LPC_ORDER_MAX = 16;
    static final int FIND_PITCH_LPC_WIN_MAX = 864;
    static final int FIND_PITCH_LPC_WIN_MS = 36;
    static final int FRAME_LENGTH_MS = 20;
    static final int HARM_SHAPE_FIR_TAPS = 3;
    static final boolean HIGH_PASS_INPUT = true;
    static final int HP_8_KHZ_THRES = 10;
    static final int INBAND_FEC_MIN_RATE_BPS = 18000;
    static final int LA_PITCH_MAX = 72;
    static final int LA_PITCH_MS = 3;
    static final int LA_SHAPE_MAX = 120;
    static final int LA_SHAPE_MS = 5;
    static final int LBRR_IDX_MASK = 1;
    static final int LBRR_LOSS_THRES = 2;
    static final boolean LOW_COMPLEXITY_ONLY = false;
    static final int LTP_BUF_LENGTH = 512;
    static final int LTP_MASK = 511;
    static final int LTP_ORDER = 5;
    static final int MAX_API_FS_KHZ = 48;
    static final int MAX_ARITHM_BYTES = 1024;
    static final int MAX_CONSECUTIVE_DTX = 20;
    static final int MAX_DELTA_GAIN_QUANT = 40;
    static final int MAX_DELTA_LAG = 10;
    static final int MAX_FRAMES_PER_PACKET = 5;
    static final int MAX_FRAME_LENGTH = 480;
    static final int MAX_FS_KHZ = 24;
    static final int MAX_LBRR_DELAY = 2;
    static final int MAX_LPC_ORDER = 16;
    static final int MAX_LPC_STABILIZE_ITERATIONS = 20;
    static final int MAX_MATRIX_SIZE = 16;
    static final int MAX_NB_SHELL_BLOCKS = 30;
    static final int MAX_NLSF_MSVQ_SURVIVORS = 16;
    static final int MAX_NLSF_MSVQ_SURVIVORS_LC_MODE = 2;
    static final int MAX_NLSF_MSVQ_SURVIVORS_MC_MODE = 4;
    static final int MAX_PULSES = 18;
    static final int MAX_QGAIN_DB = 86;
    static final int MB2NB_BITRATE_BPS = 10000;
    static final int MB2WB_BITRATE_BPS = 20000;
    static final int MIN_DELTA_GAIN_QUANT = -4;
    static final int MIN_LPC_ORDER = 10;
    static final int MIN_QGAIN_DB = 6;
    static final int MIN_TARGET_RATE_MB_BPS = 7000;
    static final int MIN_TARGET_RATE_NB_BPS = 5000;
    static final int MIN_TARGET_RATE_SWB_BPS = 20000;
    static final int MIN_TARGET_RATE_WB_BPS = 8000;
    static final int NB2MB_BITRATE_BPS = 14000;
    static final int NB_LTP_CBKS = 3;
    static final int NB_SOS = 3;
    static final int NB_SUBFR = 4;
    static final boolean NLSF_MSVQ_FLUCTUATION_REDUCTION = true;
    static final int NLSF_MSVQ_MAX_CB_STAGES = 10;
    static final int NLSF_MSVQ_MAX_VECTORS_IN_STAGE = 128;
    static final int NLSF_MSVQ_MAX_VECTORS_IN_STAGE_TWO_TO_END = 16;
    static final int NLSF_MSVQ_SURV_MAX_REL_RD = 4;
    static final int NO_LBRR_THRES = 10;
    static final int NO_SPEECH_FRAMES_BEFORE_DTX = 5;
    static final int NO_VOICE_ACTIVITY = 0;
    static final int N_LEVELS_QGAIN = 64;
    static final int N_RATE_LEVELS = 10;
    static final int OFFSET_UVH_Q10 = 256;
    static final int OFFSET_UVL_Q10 = 100;
    static final int OFFSET_VH_Q10 = 100;
    static final int OFFSET_VL_Q10 = 32;
    static final int PITCH_EST_COMPLEXITY_HC_MODE = 2;
    static final int PITCH_EST_COMPLEXITY_LC_MODE = 0;
    static final int PITCH_EST_COMPLEXITY_MC_MODE = 1;
    static final int RANGE_CODER_CDF_OUT_OF_RANGE = -2;
    static final int RANGE_CODER_DECODER_CHECK_FAILED = -5;
    static final int RANGE_CODER_DEC_PAYLOAD_TOO_LONG = -8;
    static final int RANGE_CODER_ILLEGAL_SAMPLING_RATE = -7;
    static final int RANGE_CODER_NORMALIZATION_FAILED = -3;
    static final int RANGE_CODER_READ_BEYOND_BUFFER = -6;
    static final int RANGE_CODER_WRITE_BEYOND_BUFFER = -1;
    static final int RANGE_CODER_ZERO_INTERVAL_WIDTH = -4;
    static final int SHAPE_LPC_ORDER_MAX = 16;
    static final int SHAPE_LPC_WIN_16_KHZ = 240;
    static final int SHAPE_LPC_WIN_24_KHZ = 360;
    static final int SHAPE_LPC_WIN_MAX = 360;
    static final int SHAPE_LPC_WIN_MS = 15;
    static final int SHELL_CODEC_FRAME_LENGTH = 16;
    static final int SIG_TYPE_UNVOICED = 1;
    static final int SIG_TYPE_VOICED = 0;
    static final int SKP_SILK_ADD_LBRR_TO_PLUS1 = 1;
    static final int SKP_SILK_ADD_LBRR_TO_PLUS2 = 2;
    static final int SKP_SILK_EXT_LAYER = 4;
    static final int SKP_SILK_LAST_FRAME = 0;
    static final int SKP_SILK_LBRR_VER1 = 2;
    static final int SKP_SILK_LBRR_VER2 = 3;
    static final int SKP_SILK_MORE_FRAMES = 1;
    static final int SKP_SILK_NO_LBRR = 0;
    static final int SWB2WB_BITRATE_BPS = 26000;
    static final int SWITCH_TRANSITION_FILTERING = 1;
    static final int TARGET_RATE_TAB_SZ = 8;
    static final int TRANSITION_FRAMES_DOWN = 128;
    static final int TRANSITION_FRAMES_UP = 256;
    static final int TRANSITION_INT_NUM = 5;
    static final int TRANSITION_INT_STEPS_DOWN = 32;
    static final int TRANSITION_INT_STEPS_UP = 64;
    static final int TRANSITION_NA = 2;
    static final int TRANSITION_NB = 3;
    static final int TRANSITION_TIME_DOWN_MS = 2560;
    static final int TRANSITION_TIME_UP_MS = 5120;
    static final int USE_HARM_SHAPING = 1;
    static final int USE_LBRR = 1;
    static final int VAD_INTERNAL_SUBFRAMES = 4;
    static final int VAD_INTERNAL_SUBFRAMES_LOG2 = 2;
    static final int VAD_NEGATIVE_OFFSET_Q5 = 128;
    static final int VAD_NOISE_LEVELS_BIAS = 50;
    static final int VAD_NOISE_LEVEL_SMOOTH_COEF_Q16 = 1024;
    static final int VAD_N_BANDS = 4;
    static final int VAD_SNR_FACTOR_Q16 = 45000;
    static final int VAD_SNR_SMOOTH_COEF_Q18 = 4096;
    static final int VOICE_ACTIVITY = 1;
    static final int WB2MB_BITRATE_BPS = 15000;
    static final int WB2SWB_BITRATE_BPS = 32000;
    static final int WB_DETECT_ACTIVE_SPEECH_MS_THRES = 15000;

    static int NSQ_LPC_BUF_LENGTH() {
        return 32;
    }

    static int NLSF_MSVQ_TREE_SEARCH_MAX_VECTORS_EVALUATED_LC_MODE() {
        return 128;
    }

    static int NLSF_MSVQ_TREE_SEARCH_MAX_VECTORS_EVALUATED() {
        return 256;
    }
}