package org.jitsi.service.neomedia;

import org.jitsi.service.protocol.DTMFTone;

public class DTMFInbandTone {
    public static final DTMFInbandTone DTMF_INBAND_0 = new DTMFInbandTone("0", FREQUENCY_LIST_1[3], FREQUENCY_LIST_2[1]);
    public static final DTMFInbandTone DTMF_INBAND_1 = new DTMFInbandTone("1", FREQUENCY_LIST_1[0], FREQUENCY_LIST_2[0]);
    public static final DTMFInbandTone DTMF_INBAND_2 = new DTMFInbandTone("2", FREQUENCY_LIST_1[0], FREQUENCY_LIST_2[1]);
    public static final DTMFInbandTone DTMF_INBAND_3 = new DTMFInbandTone("3", FREQUENCY_LIST_1[0], FREQUENCY_LIST_2[2]);
    public static final DTMFInbandTone DTMF_INBAND_4 = new DTMFInbandTone("4", FREQUENCY_LIST_1[1], FREQUENCY_LIST_2[0]);
    public static final DTMFInbandTone DTMF_INBAND_5 = new DTMFInbandTone("5", FREQUENCY_LIST_1[1], FREQUENCY_LIST_2[1]);
    public static final DTMFInbandTone DTMF_INBAND_6 = new DTMFInbandTone("6", FREQUENCY_LIST_1[1], FREQUENCY_LIST_2[2]);
    public static final DTMFInbandTone DTMF_INBAND_7 = new DTMFInbandTone("7", FREQUENCY_LIST_1[2], FREQUENCY_LIST_2[0]);
    public static final DTMFInbandTone DTMF_INBAND_8 = new DTMFInbandTone("8", FREQUENCY_LIST_1[2], FREQUENCY_LIST_2[1]);
    public static final DTMFInbandTone DTMF_INBAND_9 = new DTMFInbandTone("9", FREQUENCY_LIST_1[2], FREQUENCY_LIST_2[2]);
    public static final DTMFInbandTone DTMF_INBAND_A = new DTMFInbandTone("A", FREQUENCY_LIST_1[0], FREQUENCY_LIST_2[3]);
    public static final DTMFInbandTone DTMF_INBAND_B = new DTMFInbandTone("B", FREQUENCY_LIST_1[1], FREQUENCY_LIST_2[3]);
    public static final DTMFInbandTone DTMF_INBAND_C = new DTMFInbandTone("C", FREQUENCY_LIST_1[2], FREQUENCY_LIST_2[3]);
    public static final DTMFInbandTone DTMF_INBAND_D = new DTMFInbandTone("D", FREQUENCY_LIST_1[3], FREQUENCY_LIST_2[3]);
    public static final DTMFInbandTone DTMF_INBAND_SHARP = new DTMFInbandTone("#", FREQUENCY_LIST_1[3], FREQUENCY_LIST_2[2]);
    public static final DTMFInbandTone DTMF_INBAND_STAR = new DTMFInbandTone("*", FREQUENCY_LIST_1[3], FREQUENCY_LIST_2[0]);
    private static final double[] FREQUENCY_LIST_1 = new double[]{697.0d, 770.0d, 852.0d, 941.0d};
    private static final double[] FREQUENCY_LIST_2 = new double[]{1209.0d, 1336.0d, 1477.0d, 1633.0d};
    private static final int INTER_DIGIT_INTERVAL = 45;
    private static final int TONE_DURATION = 150;
    private double frequency1;
    private double frequency2;
    private String value;

    public DTMFInbandTone(String value, double frequency1, double frequency2) {
        this.value = value;
        this.frequency1 = frequency1;
        this.frequency2 = frequency2;
    }

    public String getValue() {
        return this.value;
    }

    public double getFrequency1() {
        return this.frequency1;
    }

    public double getFrequency2() {
        return this.frequency2;
    }

    public double getAudioSampleContinuous(double samplingFrequency, int sampleNumber) {
        return (Math.sin(((double) sampleNumber) * ((this.frequency1 * 6.283185307179586d) / samplingFrequency)) * 0.5d) + (Math.sin(((double) sampleNumber) * ((this.frequency2 * 6.283185307179586d) / samplingFrequency)) * 0.5d);
    }

    public int getAudioSampleDiscrete(double samplingFrequency, int sampleNumber, int sampleSizeInBits) {
        return (int) (getAudioSampleContinuous(samplingFrequency, sampleNumber) * ((double) ((1 << (sampleSizeInBits - 1)) - 1)));
    }

    public short[] getAudioSamples(double sampleRate, int sampleSizeInBits) {
        int kHz = (int) (sampleRate / 1000.0d);
        int nbToneSamples = kHz * 150;
        int nbInterDigitSamples = kHz * INTER_DIGIT_INTERVAL;
        short[] samples = new short[((nbInterDigitSamples + nbToneSamples) + nbInterDigitSamples)];
        int endSampleNumber = nbInterDigitSamples + nbToneSamples;
        for (int sampleNumber = nbInterDigitSamples; sampleNumber < endSampleNumber; sampleNumber++) {
            samples[sampleNumber] = (short) getAudioSampleDiscrete(sampleRate, sampleNumber, sampleSizeInBits);
        }
        return samples;
    }

    public static DTMFInbandTone mapTone(DTMFTone tone) {
        if (tone.equals(DTMFTone.DTMF_0)) {
            return DTMF_INBAND_0;
        }
        if (tone.equals(DTMFTone.DTMF_1)) {
            return DTMF_INBAND_1;
        }
        if (tone.equals(DTMFTone.DTMF_2)) {
            return DTMF_INBAND_2;
        }
        if (tone.equals(DTMFTone.DTMF_3)) {
            return DTMF_INBAND_3;
        }
        if (tone.equals(DTMFTone.DTMF_4)) {
            return DTMF_INBAND_4;
        }
        if (tone.equals(DTMFTone.DTMF_5)) {
            return DTMF_INBAND_5;
        }
        if (tone.equals(DTMFTone.DTMF_6)) {
            return DTMF_INBAND_6;
        }
        if (tone.equals(DTMFTone.DTMF_7)) {
            return DTMF_INBAND_7;
        }
        if (tone.equals(DTMFTone.DTMF_8)) {
            return DTMF_INBAND_8;
        }
        if (tone.equals(DTMFTone.DTMF_9)) {
            return DTMF_INBAND_9;
        }
        if (tone.equals(DTMFTone.DTMF_A)) {
            return DTMF_INBAND_A;
        }
        if (tone.equals(DTMFTone.DTMF_B)) {
            return DTMF_INBAND_B;
        }
        if (tone.equals(DTMFTone.DTMF_C)) {
            return DTMF_INBAND_C;
        }
        if (tone.equals(DTMFTone.DTMF_D)) {
            return DTMF_INBAND_D;
        }
        if (tone.equals(DTMFTone.DTMF_SHARP)) {
            return DTMF_INBAND_SHARP;
        }
        if (tone.equals(DTMFTone.DTMF_STAR)) {
            return DTMF_INBAND_STAR;
        }
        return null;
    }
}
