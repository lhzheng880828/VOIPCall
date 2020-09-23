package org.jitsi.impl.neomedia.codec.audio.silk;

import org.jitsi.impl.neomedia.portaudio.Pa;

public class EnergyFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!EnergyFLP.class.desiredAssertionStatus());

    static double SKP_Silk_energy_FLP(float[] data, int data_offset, int dataSize) {
        double result = Pa.LATENCY_UNSPECIFIED;
        int i = 0;
        while (i < (dataSize & 65532)) {
            result += (double) ((((data[(data_offset + i) + 0] * data[(data_offset + i) + 0]) + (data[(data_offset + i) + 1] * data[(data_offset + i) + 1])) + (data[(data_offset + i) + 2] * data[(data_offset + i) + 2])) + (data[(data_offset + i) + 3] * data[(data_offset + i) + 3]));
            i += 4;
        }
        while (i < dataSize) {
            result += (double) (data[data_offset + i] * data[data_offset + i]);
            i++;
        }
        if ($assertionsDisabled || result >= Pa.LATENCY_UNSPECIFIED) {
            return result;
        }
        throw new AssertionError();
    }
}
