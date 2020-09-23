package org.jitsi.impl.neomedia.codec.audio.silk;

import com.sun.media.format.WavAudioFormat;
import java.util.Arrays;

/* compiled from: NSQDelDec */
class NSQDelDecStruct {
    int[] Gain_Q16 = new int[32];
    int LF_AR_Q12;
    int[] Pred_Q16 = new int[32];
    int[] Q_Q10 = new int[32];
    int RD_Q10;
    int[] RandState = new int[32];
    int Seed;
    int SeedInit;
    int[] Shape_Q10 = new int[32];
    int[] Xq_Q10 = new int[32];
    int[] sLPC_Q14 = new int[(Define.NSQ_LPC_BUF_LENGTH() + WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18)];

    NSQDelDecStruct() {
    }

    public void FieldsInit() {
        Arrays.fill(this.RandState, 0);
        Arrays.fill(this.Q_Q10, 0);
        Arrays.fill(this.Xq_Q10, 0);
        Arrays.fill(this.Pred_Q16, 0);
        Arrays.fill(this.Shape_Q10, 0);
        Arrays.fill(this.Gain_Q16, 0);
        Arrays.fill(this.sLPC_Q14, 0);
        this.LF_AR_Q12 = 0;
        this.Seed = 0;
        this.SeedInit = 0;
        this.RD_Q10 = 0;
    }
}
