package org.jitsi.impl.neomedia.codec.audio.silk;

import java.util.Arrays;

/* compiled from: ResamplerStructs */
class SKP_Silk_resampler_state_struct {
    short[] Coefs;
    int FIR_Fracs;
    int batchSize;
    int batchSizePrePost;
    DownPreFP downPreCB;
    String down_pre_function;
    int input2x;
    int invRatio_Q16;
    int magic_number;
    int nPostUpsamplers;
    int nPreDownsamplers;
    int ratio_Q16;
    ResamplerFP resamplerCB;
    String resampler_function;
    int[] sDown2 = new int[2];
    int[] sDownPre = new int[2];
    int[] sFIR = new int[16];
    int[] sIIR = new int[6];
    int[] sUpPost = new int[2];
    Up2FP up2CB;
    String up2_function;
    UpPostFP upPostCB;
    String up_post_function;

    SKP_Silk_resampler_state_struct() {
    }

    /* access modifiers changed from: 0000 */
    public void resampler_function(Object state, short[] out, int out_offset, short[] in, int in_offset, int len) {
        this.resamplerCB.resampler_function(state, out, out_offset, in, in_offset, len);
    }

    /* access modifiers changed from: 0000 */
    public void up2_function(int[] state, short[] out, int out_offset, short[] in, int in_offset, int len) {
        this.up2CB.up2_function(state, out, out_offset, in, in_offset, len);
    }

    /* access modifiers changed from: 0000 */
    public void down_pre_function(int[] state, short[] out, int out_offset, short[] in, int in_offset, int len) {
        this.downPreCB.down_pre_function(state, out, out_offset, in, in_offset, len);
    }

    /* access modifiers changed from: 0000 */
    public void up_post_function(int[] state, short[] out, int out_offset, short[] in, int in_offset, int len) {
        this.upPostCB.up_post_function(state, out, out_offset, in, in_offset, len);
    }

    public void memZero() {
        this.Coefs = null;
        Arrays.fill(this.sDown2, 0);
        Arrays.fill(this.sDownPre, 0);
        Arrays.fill(this.sFIR, 0);
        Arrays.fill(this.sIIR, 0);
        Arrays.fill(this.sUpPost, 0);
        this.batchSize = 0;
        this.batchSizePrePost = 0;
        this.down_pre_function = null;
        this.downPreCB = null;
        this.FIR_Fracs = 0;
        this.input2x = 0;
        this.invRatio_Q16 = 0;
        this.magic_number = 0;
        this.nPostUpsamplers = 0;
        this.nPreDownsamplers = 0;
        this.ratio_Q16 = 0;
        this.resampler_function = null;
        this.resamplerCB = null;
        this.up2_function = null;
        this.up2CB = null;
        this.up_post_function = null;
        this.upPostCB = null;
    }
}
