package org.jitsi.impl.neomedia.codec.audio.g729;

class CodLd8k extends Ld8k {
    private final AcelpCo acelpCo = new AcelpCo();
    private final float[] ai_zero = new float[51];
    private float[] error;
    private int error_offset;
    private float[] exc;
    private int exc_offset;
    private final float[] lsp_old = new float[]{0.9595f, 0.8413f, 0.6549f, 0.4154f, 0.1423f, -0.1423f, -0.4154f, -0.6549f, -0.8413f, -0.9595f};
    private final float[] lsp_old_q = new float[10];
    private final float[] mem_err = new float[50];
    private final float[] mem_syn = new float[10];
    private final float[] mem_w = new float[10];
    private final float[] mem_w0 = new float[10];
    float[] new_speech;
    int new_speech_offset;
    private final float[] old_exc = new float[234];
    private final float[] old_speech = new float[240];
    private final float[] old_wsp = new float[223];
    private float[] p_window;
    private int p_window_offset;
    private final Pwf pwf = new Pwf();
    private final QuaGain quaGain = new QuaGain();
    private final QuaLsp quaLsp = new QuaLsp();
    private float sharp;
    private float[] speech;
    private int speech_offset;
    private final Taming taming = new Taming();
    private float[] wsp;
    private int wsp_offset;
    private float[] zero;
    private int zero_offset;

    CodLd8k() {
    }

    /* access modifiers changed from: 0000 */
    public void init_coder_ld8k() {
        this.new_speech = this.old_speech;
        this.new_speech_offset = 160;
        this.speech = this.new_speech;
        this.speech_offset = this.new_speech_offset - 40;
        this.p_window = this.old_speech;
        this.p_window_offset = 0;
        this.wsp = this.old_wsp;
        this.wsp_offset = 143;
        this.exc = this.old_exc;
        this.exc_offset = 154;
        this.zero = this.ai_zero;
        this.zero_offset = 11;
        this.error = this.mem_err;
        this.error_offset = 10;
        Util.set_zero(this.old_speech, 240);
        Util.set_zero(this.old_exc, 154);
        Util.set_zero(this.old_wsp, 143);
        Util.set_zero(this.mem_syn, 10);
        Util.set_zero(this.mem_w, 10);
        Util.set_zero(this.mem_w0, 10);
        Util.set_zero(this.mem_err, 10);
        Util.set_zero(this.zero, this.zero_offset, 40);
        this.sharp = 0.2f;
        Util.copy(this.lsp_old, this.lsp_old_q, 10);
        this.quaLsp.lsp_encw_reset();
        this.taming.init_exc_err();
    }

    /* access modifiers changed from: 0000 */
    public void coder_ld8k(int[] ana) {
        int i;
        float[] r = new float[11];
        float[] A_t = new float[22];
        float[] Aq_t = new float[22];
        float[] Ap1 = new float[11];
        float[] Ap2 = new float[11];
        float[] lsp_new = new float[10];
        float[] lsp_new_q = new float[10];
        float[] lsf_int = new float[10];
        float[] lsf_new = new float[10];
        float[] rc = new float[10];
        float[] gamma1 = new float[2];
        float[] gamma2 = new float[2];
        float[] synth = new float[80];
        float[] h1 = new float[40];
        float[] xn = new float[40];
        float[] xn2 = new float[40];
        float[] code = new float[40];
        float[] y1 = new float[40];
        float[] y2 = new float[40];
        float[] g_coeff = new float[5];
        IntReference iRef = new IntReference();
        IntReference t0_min = new IntReference();
        IntReference t0_max = new IntReference();
        IntReference t0_frac = new IntReference();
        float gain_code = 0.0f;
        FloatReference _gain_pit = new FloatReference();
        FloatReference _gain_code = new FloatReference();
        Lpc.autocorr(this.p_window, this.p_window_offset, 10, r);
        Lpc.lag_window(10, r);
        Lpc.levinson(r, A_t, 11, rc);
        Lpc.az_lsp(A_t, 11, lsp_new, this.lsp_old);
        this.quaLsp.qua_lsp(lsp_new, lsp_new_q, ana);
        int ana_offset = 0 + 2;
        Lpcfunc.int_lpc(this.lsp_old, lsp_new, lsf_int, lsf_new, A_t);
        Lpcfunc.int_qlpc(this.lsp_old_q, lsp_new_q, Aq_t);
        for (i = 0; i < 10; i++) {
            this.lsp_old[i] = lsp_new[i];
            this.lsp_old_q[i] = lsp_new_q[i];
        }
        this.pwf.perc_var(gamma1, gamma2, lsf_int, lsf_new, rc);
        Lpcfunc.weight_az(A_t, 0, gamma1[0], 10, Ap1);
        Lpcfunc.weight_az(A_t, 0, gamma2[0], 10, Ap2);
        Filter.residu(Ap1, 0, this.speech, this.speech_offset, this.wsp, this.wsp_offset, 40);
        Filter.syn_filt(Ap2, 0, this.wsp, this.wsp_offset, this.wsp, this.wsp_offset, 40, this.mem_w, 0, 1);
        Lpcfunc.weight_az(A_t, 11, gamma1[1], 10, Ap1);
        Lpcfunc.weight_az(A_t, 11, gamma2[1], 10, Ap2);
        Filter.residu(Ap1, 0, this.speech, this.speech_offset + 40, this.wsp, this.wsp_offset + 40, 40);
        Filter.syn_filt(Ap2, 0, this.wsp, this.wsp_offset + 40, this.wsp, this.wsp_offset + 40, 40, this.mem_w, 0, 1);
        t0_min.value = Pitch.pitch_ol(this.wsp, this.wsp_offset, 20, 143, 80) - 3;
        if (t0_min.value < 20) {
            t0_min.value = 20;
        }
        t0_max.value = t0_min.value + 6;
        if (t0_max.value > 143) {
            t0_max.value = 143;
            t0_min.value = t0_max.value - 6;
        }
        float[] A = A_t;
        int A_offset = 0;
        float[] Aq = Aq_t;
        int Aq_offset = 0;
        int i_gamma = 0;
        for (int i_subfr = 0; i_subfr < 80; i_subfr += 40) {
            Lpcfunc.weight_az(A, A_offset, gamma1[i_gamma], 10, Ap1);
            Lpcfunc.weight_az(A, A_offset, gamma2[i_gamma], 10, Ap2);
            i_gamma++;
            for (i = 0; i <= 10; i++) {
                this.ai_zero[i] = Ap1[i];
            }
            Filter.syn_filt(Aq, Aq_offset, this.ai_zero, 0, h1, 0, 40, this.zero, this.zero_offset, 0);
            Filter.syn_filt(Ap2, 0, h1, 0, h1, 0, 40, this.zero, this.zero_offset, 0);
            Filter.residu(Aq, Aq_offset, this.speech, this.speech_offset + i_subfr, this.exc, this.exc_offset + i_subfr, 40);
            Filter.syn_filt(Aq, Aq_offset, this.exc, this.exc_offset + i_subfr, this.error, this.error_offset, 40, this.mem_err, 0, 0);
            Filter.residu(Ap1, 0, this.error, this.error_offset, xn, 0, 40);
            Filter.syn_filt(Ap2, 0, xn, 0, xn, 0, 40, this.mem_w0, 0, 0);
            int t0 = Pitch.pitch_fr3(this.exc, this.exc_offset + i_subfr, xn, h1, 40, t0_min.value, t0_max.value, i_subfr, t0_frac);
            int index = Pitch.enc_lag3(t0, t0_frac.value, t0_min, t0_max, 20, 143, i_subfr);
            ana[ana_offset] = index;
            ana_offset++;
            if (i_subfr == 0) {
                ana[ana_offset] = PParity.parity_pitch(index);
                ana_offset++;
            }
            PredLt3.pred_lt_3(this.exc, this.exc_offset + i_subfr, t0, t0_frac.value, 40);
            Filter.convolve(this.exc, this.exc_offset + i_subfr, h1, y1, 40);
            float gain_pit = Pitch.g_pitch(xn, y1, g_coeff, 40);
            int taming = this.taming.test_err(t0, t0_frac.value);
            if (taming == 1 && gain_pit > 0.95f) {
                gain_pit = 0.95f;
            }
            i = 0;
            while (i < 40) {
                xn2[i] = xn[i] - (y1[i] * gain_pit);
                i++;
            }
            iRef.value = i;
            index = this.acelpCo.ACELP_codebook(xn2, h1, t0, this.sharp, i_subfr, code, y2, iRef);
            i = iRef.value;
            ana[ana_offset] = index;
            ana_offset++;
            ana[ana_offset] = i;
            ana_offset++;
            CorFunc.corr_xy2(xn, y1, y2, g_coeff);
            _gain_pit.value = gain_pit;
            _gain_code.value = gain_code;
            ana[ana_offset] = this.quaGain.qua_gain(code, g_coeff, 40, _gain_pit, _gain_code, taming);
            gain_pit = _gain_pit.value;
            gain_code = _gain_code.value;
            ana_offset++;
            this.sharp = gain_pit;
            if (this.sharp > 0.7945f) {
                this.sharp = 0.7945f;
            }
            if (this.sharp < 0.2f) {
                this.sharp = 0.2f;
            }
            for (i = 0; i < 40; i++) {
                this.exc[(this.exc_offset + i) + i_subfr] = (this.exc[(this.exc_offset + i) + i_subfr] * gain_pit) + (code[i] * gain_code);
            }
            this.taming.update_exc_err(gain_pit, t0);
            Filter.syn_filt(Aq, Aq_offset, this.exc, this.exc_offset + i_subfr, synth, i_subfr, 40, this.mem_syn, 0, 1);
            i = 30;
            int j = 0;
            while (i < 40) {
                this.mem_err[j] = this.speech[(this.speech_offset + i_subfr) + i] - synth[i_subfr + i];
                this.mem_w0[j] = (xn[i] - (y1[i] * gain_pit)) - (y2[i] * gain_code);
                i++;
                j++;
            }
            A_offset += 11;
            Aq_offset += 11;
        }
        Util.copy(this.old_speech, 80, this.old_speech, 160);
        Util.copy(this.old_wsp, 80, this.old_wsp, 143);
        Util.copy(this.old_exc, 80, this.old_exc, 154);
    }
}
