package org.jitsi.impl.neomedia.codec.audio.silk;

public class DefineFLP {
    static final float FIND_LPC_COND_FAC = 6.0E-5f;
    static final float FIND_LTP_COND_FAC = 1.0E-5f;
    static final float FIND_PITCH_BANDWITH_EXPANSION = 0.99f;
    static final float FIND_PITCH_CORRELATION_THRESHOLD_HC_MODE = 0.7f;
    static final float FIND_PITCH_CORRELATION_THRESHOLD_LC_MODE = 0.8f;
    static final float FIND_PITCH_CORRELATION_THRESHOLD_MC_MODE = 0.75f;
    static final float FIND_PITCH_WHITE_NOISE_FRACTION = 0.001f;
    static float LBRR_SPEECH_ACTIVITY_THRES = 0.5f;
    static final float LTP_DAMPING = 0.001f;
    static final float LTP_SMOOTHING = 0.1f;
    static final float MU_LTP_QUANT_MB = 0.025f;
    static final float MU_LTP_QUANT_NB = 0.03f;
    static final float MU_LTP_QUANT_SWB = 0.016f;
    static final float MU_LTP_QUANT_WB = 0.02f;
    static final float Q14_CONVERSION_FAC = 6.1035E-5f;
    static final float SPEECH_ACTIVITY_DTX_THRES = 0.1f;
    static final float VARIABLE_HP_MAX_DELTA_FREQ = 0.4f;
    static final float VARIABLE_HP_MAX_FREQ = 150.0f;
    static final float VARIABLE_HP_MIN_FREQ = 80.0f;
    static final float VARIABLE_HP_SMTH_COEF1 = 0.1f;
    static final float VARIABLE_HP_SMTH_COEF2 = 0.015f;
    static final float WB_DETECT_ACTIVE_SPEECH_LEVEL_THRES = 0.7f;
}
