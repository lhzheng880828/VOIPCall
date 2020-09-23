package org.jitsi.impl.neomedia.codec.audio.silk;

import java.lang.reflect.Array;

/* compiled from: StructsFLP */
class SKP_Silk_encoder_control_FLP {
    float[] AR1 = new float[64];
    float[] AR2 = new float[64];
    short[] AR2_Q13 = new short[64];
    float[] Gains = new float[4];
    float[] GainsPre = new float[4];
    int[] Gains_Q16 = new int[4];
    float[] HarmBoost = new float[4];
    float[] HarmShapeGain = new float[4];
    int[] HarmShapeGain_Q14 = new int[4];
    float[] LF_AR_shp = new float[4];
    float[] LF_MA_shp = new float[4];
    int[] LF_shp_Q14 = new int[4];
    float[] LTPCoef = new float[20];
    short[] LTPCoef_Q14 = new short[20];
    float LTP_scale;
    int LTP_scale_Q14;
    float LTPredCodGain;
    float Lambda;
    int Lambda_Q10;
    float[][] PredCoef = ((float[][]) Array.newInstance(Float.TYPE, new int[]{2, 16}));
    short[][] PredCoef_Q12 = ((short[][]) Array.newInstance(Short.TYPE, new int[]{2, 16}));
    float[] ResNrg = new float[4];
    float[] Tilt = new float[4];
    int[] Tilt_Q14 = new int[4];
    float coding_quality;
    float current_SNR_dB;
    int dummy_int32AR2_Q13;
    int[] dummy_int32PredCoef_Q12 = new int[2];
    float input_quality;
    float[] input_quality_bands = new float[4];
    float input_tilt;
    float pitch_freq_low_Hz;
    SKP_Silk_encoder_control sCmn = new SKP_Silk_encoder_control();
    float sparseness;

    SKP_Silk_encoder_control_FLP() {
    }
}
