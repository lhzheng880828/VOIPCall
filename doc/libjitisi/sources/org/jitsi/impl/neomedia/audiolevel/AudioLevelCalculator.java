package org.jitsi.impl.neomedia.audiolevel;

import com.lti.utils.UnsignedUtils;
import org.jitsi.impl.neomedia.ArrayIOUtils;
import org.jitsi.impl.neomedia.portaudio.Pa;

public class AudioLevelCalculator {
    private static final double DEC_LEVEL = 0.2d;
    private static final double INC_LEVEL = 0.4d;
    private static final int MAX_AUDIO_LEVEL = 32767;
    private static final double MAX_SOUND_PRESSURE_LEVEL = 127.0d;
    private static final int MIN_AUDIO_LEVEL = -32768;

    private static int animateLevel(int level, int minLevel, int maxLevel, int lastLevel) {
        int diff = lastLevel - level;
        int maxDiff;
        if (diff >= 0) {
            maxDiff = (int) (((double) maxLevel) * DEC_LEVEL);
            if (diff > maxDiff) {
                return lastLevel - maxDiff;
            }
            return level;
        }
        maxDiff = (int) (((double) maxLevel) * INC_LEVEL);
        if (diff > maxDiff) {
            return lastLevel + maxDiff;
        }
        return level;
    }

    public static int calculateSignalPowerLevel(byte[] samples, int offset, int length, int minLevel, int maxLevel, int lastLevel) {
        if (length == 0) {
            return 0;
        }
        int samplesNumber = length / 2;
        int absoluteMeanSoundLevel = 0;
        double levelRatio = (double) ((32767 / (maxLevel - minLevel)) / 16);
        int offset2 = offset;
        for (int i = 0; i < samplesNumber; i++) {
            offset = offset2 + 1;
            int tempL = samples[offset2];
            offset2 = offset + 1;
            int soundLevel = (samples[offset] << 8) | (tempL & UnsignedUtils.MAX_UBYTE);
            if (soundLevel > 32767) {
                soundLevel = 32767;
            } else if (soundLevel < MIN_AUDIO_LEVEL) {
                soundLevel = MIN_AUDIO_LEVEL;
            }
            absoluteMeanSoundLevel += Math.abs(soundLevel);
        }
        offset = offset2;
        return animateLevel(ensureLevelRange((int) (((double) (absoluteMeanSoundLevel / samplesNumber)) / levelRatio), minLevel, maxLevel), minLevel, maxLevel, lastLevel);
    }

    public static int calculateSoundPressureLevel(byte[] samples, int offset, int length, int minLevel, int maxLevel, int lastLevel) {
        double db;
        double rms = Pa.LATENCY_UNSPECIFIED;
        int sampleCount = 0;
        while (offset < length) {
            double sample = ((double) ArrayIOUtils.readShort(samples, offset)) / 32767.0d;
            rms += sample * sample;
            sampleCount++;
            offset += 2;
        }
        rms = sampleCount == 0 ? Pa.LATENCY_UNSPECIFIED : Math.sqrt(rms / ((double) sampleCount));
        if (rms > Pa.LATENCY_UNSPECIFIED) {
            db = 20.0d * Math.log10(rms / 2.0E-5d);
        } else {
            db = -127.0d;
        }
        return ensureLevelRange((int) db, minLevel, maxLevel);
    }

    private static int ensureLevelRange(int level, int minLevel, int maxLevel) {
        if (level < minLevel) {
            return minLevel;
        }
        if (level > maxLevel) {
            return maxLevel;
        }
        return level;
    }
}
