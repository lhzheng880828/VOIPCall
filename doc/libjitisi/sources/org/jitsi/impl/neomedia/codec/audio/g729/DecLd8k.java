package org.jitsi.impl.neomedia.codec.audio.g729;

class DecLd8k extends Ld8k {
    private final DecGain decGain = new DecGain();
    private float[] exc;
    private int exc_offset;
    private final FloatReference gain_code = new FloatReference();
    private final FloatReference gain_pitch = new FloatReference();
    private final float[] lsp_old = new float[]{0.9595f, 0.8413f, 0.6549f, 0.4154f, 0.1423f, -0.1423f, -0.4154f, -0.6549f, -0.8413f, -0.9595f};
    private final Lspdec lspdec = new Lspdec();
    private final float[] mem_syn = new float[10];
    private final float[] old_exc = new float[234];
    private int old_t0;
    private float sharp;

    DecLd8k() {
    }

    /* access modifiers changed from: 0000 */
    public void init_decod_ld8k() {
        this.exc = this.old_exc;
        this.exc_offset = 154;
        Util.set_zero(this.old_exc, 154);
        Util.set_zero(this.mem_syn, 10);
        this.sharp = 0.2f;
        this.old_t0 = 60;
        this.gain_code.value = 0.0f;
        this.gain_pitch.value = 0.0f;
        this.lspdec.lsp_decw_reset();
    }

    /* access modifiers changed from: 0000 */
    public int decod_ld8k(int[] parm, int voicing, float[] synth, int synth_offset, float[] A_t) {
        int t0_first = 0;
        float[] lsp_new = new float[10];
        float[] code = new float[40];
        IntReference t0 = new IntReference();
        IntReference t0_frac = new IntReference();
        int bfi = parm[0];
        int parm_offset = 0 + 1;
        this.lspdec.d_lsp(parm, parm_offset, lsp_new, bfi);
        parm_offset += 2;
        Lpcfunc.int_qlpc(this.lsp_old, lsp_new, A_t);
        Util.copy(lsp_new, this.lsp_old, 10);
        float[] Az = A_t;
        int Az_offset = 0;
        for (int i_subfr = 0; i_subfr < 80; i_subfr += 40) {
            int i;
            int index = parm[parm_offset];
            parm_offset++;
            if (i_subfr == 0) {
                i = parm[parm_offset];
                parm_offset++;
                if (bfi + i == 0) {
                    DecLag3.dec_lag3(index, 20, 143, i_subfr, t0, t0_frac);
                    this.old_t0 = t0.value;
                } else {
                    t0.value = this.old_t0;
                    t0_frac.value = 0;
                    this.old_t0++;
                    if (this.old_t0 > 143) {
                        this.old_t0 = 143;
                    }
                }
                t0_first = t0.value;
            } else if (bfi == 0) {
                DecLag3.dec_lag3(index, 20, 143, i_subfr, t0, t0_frac);
                this.old_t0 = t0.value;
            } else {
                t0.value = this.old_t0;
                t0_frac.value = 0;
                this.old_t0++;
                if (this.old_t0 > 143) {
                    this.old_t0 = 143;
                }
            }
            PredLt3.pred_lt_3(this.exc, this.exc_offset + i_subfr, t0.value, t0_frac.value, 40);
            if (bfi != 0) {
                parm[parm_offset + 0] = Util.random_g729() & 8191;
                parm[parm_offset + 1] = Util.random_g729() & 15;
            }
            DeAcelp.decod_ACELP(parm[parm_offset + 1], parm[parm_offset + 0], code);
            parm_offset += 2;
            for (i = t0.value; i < 40; i++) {
                code[i] = code[i] + (this.sharp * code[i - t0.value]);
            }
            index = parm[parm_offset];
            parm_offset++;
            this.decGain.dec_gain(index, code, 40, bfi, this.gain_pitch, this.gain_code);
            this.sharp = this.gain_pitch.value;
            if (this.sharp > 0.7945f) {
                this.sharp = 0.7945f;
            }
            if (this.sharp < 0.2f) {
                this.sharp = 0.2f;
            }
            if (bfi == 0) {
                for (i = 0; i < 40; i++) {
                    this.exc[(this.exc_offset + i) + i_subfr] = (this.gain_pitch.value * this.exc[(this.exc_offset + i) + i_subfr]) + (this.gain_code.value * code[i]);
                }
            } else if (voicing == 0) {
                for (i = 0; i < 40; i++) {
                    this.exc[(this.exc_offset + i) + i_subfr] = this.gain_code.value * code[i];
                }
            } else {
                for (i = 0; i < 40; i++) {
                    this.exc[(this.exc_offset + i) + i_subfr] = this.gain_pitch.value * this.exc[(this.exc_offset + i) + i_subfr];
                }
            }
            Filter.syn_filt(Az, Az_offset, this.exc, this.exc_offset + i_subfr, synth, synth_offset + i_subfr, 40, this.mem_syn, 0, 1);
            Az_offset += 11;
        }
        Util.copy(this.old_exc, 80, this.old_exc, 154);
        return t0_first;
    }
}
