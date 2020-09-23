package org.jitsi.impl.neomedia.codec.audio.g729;

class Postfil extends Ld8k {
    private final float[] apond2 = new float[20];
    private float gain_prec;
    private final float[] mem_stp = new float[10];
    private final float[] mem_zero = new float[10];
    private float[] ptr_mem_stp;
    private int ptr_mem_stp_offset;
    private final float[] res2 = new float[192];
    private int res2_ptr;

    Postfil() {
    }

    /* access modifiers changed from: 0000 */
    public void init_post_filter() {
        int i;
        for (i = 0; i < 152; i++) {
            this.res2[i] = 0.0f;
        }
        this.res2_ptr = 152;
        for (i = 0; i < 10; i++) {
            this.mem_stp[i] = 0.0f;
        }
        this.ptr_mem_stp = this.mem_stp;
        this.ptr_mem_stp_offset = 9;
        for (i = 11; i < 20; i++) {
            this.apond2[i] = 0.0f;
        }
        for (i = 0; i < 10; i++) {
            this.mem_zero[i] = 0.0f;
        }
        this.gain_prec = 1.0f;
    }

    /* access modifiers changed from: 0000 */
    public int post(int t0, float[] signal_ptr, int signal_ptr_offset, float[] coeff, int coeff_offset, float[] sig_out, int sig_out_offset) {
        apond1 = new float[11];
        float[] sig_ltp = new float[41];
        Lpcfunc.weight_az(coeff, coeff_offset, 0.7f, 10, apond1);
        Lpcfunc.weight_az(coeff, coeff_offset, 0.55f, 10, this.apond2);
        Filter.residu(this.apond2, 0, signal_ptr, signal_ptr_offset, this.res2, this.res2_ptr, 40);
        int vo = pst_ltp(t0, this.res2, this.res2_ptr, sig_ltp, 1);
        sig_ltp[0] = this.ptr_mem_stp[this.ptr_mem_stp_offset];
        float parcor0 = calc_st_filt(this.apond2, apond1, sig_ltp, 1);
        Filter.syn_filt(apond1, 0, sig_ltp, 1, sig_ltp, 1, 40, this.mem_stp, 0, 1);
        filt_mu(sig_ltp, sig_out, sig_out_offset, parcor0);
        this.gain_prec = scale_st(signal_ptr, signal_ptr_offset, sig_out, sig_out_offset, this.gain_prec);
        Util.copy(this.res2, 40, this.res2, 152);
        return vo;
    }

    private int pst_ltp(int t0, float[] ptr_sig_in, int ptr_sig_in_offset, float[] ptr_sig_pst0, int ptr_sig_pst0_offset) {
        float[] y_up = new float[287];
        IntReference _ltpdel = new IntReference();
        IntReference _phase = new IntReference();
        FloatReference _num_gltp = new FloatReference();
        FloatReference _den_gltp = new FloatReference();
        IntReference _off_yup = new IntReference();
        search_del(t0, ptr_sig_in, ptr_sig_in_offset, _ltpdel, _phase, _num_gltp, _den_gltp, y_up, _off_yup);
        int ltpdel = _ltpdel.value;
        int phase = _phase.value;
        float num_gltp = _num_gltp.value;
        float den_gltp = _den_gltp.value;
        int off_yup = _off_yup.value;
        int vo = ltpdel;
        if (num_gltp == 0.0f) {
            Util.copy(ptr_sig_in, ptr_sig_in_offset, ptr_sig_pst0, ptr_sig_pst0_offset, 40);
        } else {
            float[] ptr_y_up;
            int ptr_y_up_offset;
            float gain_plt;
            if (phase == 0) {
                ptr_y_up = ptr_sig_in;
                ptr_y_up_offset = ptr_sig_in_offset - ltpdel;
            } else {
                FloatReference _num2_gltp = new FloatReference();
                FloatReference _den2_gltp = new FloatReference();
                compute_ltp_l(ptr_sig_in, ptr_sig_in_offset, ltpdel, phase, ptr_sig_pst0, ptr_sig_pst0_offset, _num2_gltp, _den2_gltp);
                float num2_gltp = _num2_gltp.value;
                float den2_gltp = _den2_gltp.value;
                if (select_ltp(num_gltp, den_gltp, num2_gltp, den2_gltp) == 1) {
                    ptr_y_up = y_up;
                    ptr_y_up_offset = ((phase - 1) * 41) + off_yup;
                } else {
                    num_gltp = num2_gltp;
                    den_gltp = den2_gltp;
                    ptr_y_up = ptr_sig_pst0;
                    ptr_y_up_offset = ptr_sig_pst0_offset;
                }
            }
            if (num_gltp > den_gltp) {
                gain_plt = 0.6666667f;
            } else {
                gain_plt = den_gltp / ((0.5f * num_gltp) + den_gltp);
            }
            filt_plt(ptr_sig_in, ptr_sig_in_offset, ptr_y_up, ptr_y_up_offset, ptr_sig_pst0, ptr_sig_pst0_offset, gain_plt);
        }
        return vo;
    }

    private void search_del(int t0, float[] ptr_sig_in, int ptr_sig_in_offset, IntReference ltpdel, IntReference phase, FloatReference num_gltp, FloatReference den_gltp, float[] y_up, IntReference off_yup) {
        int i;
        float[] tab_hup_s = TabLd8k.tab_hup_s;
        float[] tab_den0 = new float[7];
        float[] tab_den1 = new float[7];
        float ener = 0.0f;
        for (i = 0; i < 40; i++) {
            ener += ptr_sig_in[ptr_sig_in_offset + i] * ptr_sig_in[ptr_sig_in_offset + i];
        }
        if (ener < 0.1f) {
            num_gltp.value = 0.0f;
            den_gltp.value = 1.0f;
            ltpdel.value = 0;
            phase.value = 0;
            return;
        }
        float num;
        int n;
        int lambda = t0 - 1;
        int ptr_sig_past = ptr_sig_in_offset - lambda;
        float num_int = -1.0E30f;
        int i_max = 0;
        for (i = 0; i < 3; i++) {
            num = 0.0f;
            for (n = 0; n < 40; n++) {
                num += ptr_sig_in[ptr_sig_in_offset + n] * ptr_sig_in[ptr_sig_past + n];
            }
            if (num > num_int) {
                i_max = i;
                num_int = num;
            }
            ptr_sig_past--;
        }
        if (num_int <= 0.0f) {
            num_gltp.value = 0.0f;
            den_gltp.value = 1.0f;
            ltpdel.value = 0;
            phase.value = 0;
            return;
        }
        lambda += i_max;
        ptr_sig_past = ptr_sig_in_offset - lambda;
        float den_int = 0.0f;
        for (n = 0; n < 40; n++) {
            den_int += ptr_sig_in[ptr_sig_past + n] * ptr_sig_in[ptr_sig_past + n];
        }
        if (den_int < 0.1f) {
            num_gltp.value = 0.0f;
            den_gltp.value = 1.0f;
            ltpdel.value = 0;
            phase.value = 0;
            return;
        }
        float den0;
        float den1;
        int ptr_y_up = 0;
        float den_max = den_int;
        int ptr_den0 = 0;
        int ptr_den1 = 0;
        int ptr_h = 0;
        int ptr_sig_past0 = ((ptr_sig_in_offset + 2) - 1) - lambda;
        int phi = 1;
        while (phi < 8) {
            int ptr_sig_past2;
            float temp0;
            ptr_sig_past = ptr_sig_past0;
            n = 0;
            while (true) {
                ptr_sig_past2 = ptr_sig_past;
                if (n > 40) {
                    break;
                }
                ptr_sig_past = ptr_sig_past2 + 1;
                int ptr1 = ptr_sig_past2;
                temp0 = 0.0f;
                for (i = 0; i < 4; i++) {
                    temp0 += tab_hup_s[ptr_h + i] * ptr_sig_in[ptr1 - i];
                }
                y_up[ptr_y_up + n] = temp0;
                n++;
            }
            temp0 = 0.0f;
            for (n = 1; n < 40; n++) {
                temp0 += y_up[ptr_y_up + n] * y_up[ptr_y_up + n];
            }
            den0 = temp0 + (y_up[ptr_y_up + 0] * y_up[ptr_y_up + 0]);
            tab_den0[ptr_den0] = den0;
            ptr_den0++;
            den1 = temp0 + (y_up[ptr_y_up + 40] * y_up[ptr_y_up + 40]);
            tab_den1[ptr_den1] = den1;
            ptr_den1++;
            if (Math.abs(y_up[ptr_y_up + 0]) > Math.abs(y_up[ptr_y_up + 40])) {
                if (den0 > den_max) {
                    den_max = den0;
                }
            } else if (den1 > den_max) {
                den_max = den1;
            }
            ptr_y_up += 41;
            ptr_h += 4;
            phi++;
            ptr_sig_past = ptr_sig_past2;
        }
        if (den_max < 0.1f) {
            num_gltp.value = 0.0f;
            den_gltp.value = 1.0f;
            ltpdel.value = 0;
            phase.value = 0;
            return;
        }
        float num_max = num_int;
        den_max = den_int;
        float numsq_max = num_max * num_max;
        int phi_max = 0;
        int ioff = 1;
        ptr_den0 = 0;
        ptr_den1 = 0;
        ptr_y_up = 0;
        for (phi = 1; phi < 8; phi++) {
            num = 0.0f;
            for (n = 0; n < 40; n++) {
                num += ptr_sig_in[n] * y_up[ptr_y_up + n];
            }
            if (num < 0.0f) {
                num = 0.0f;
            }
            float numsq = num * num;
            den0 = tab_den0[ptr_den0];
            ptr_den0++;
            if (numsq * den_max > numsq_max * den0) {
                num_max = num;
                numsq_max = numsq;
                den_max = den0;
                ioff = 0;
                phi_max = phi;
            }
            ptr_y_up++;
            num = 0.0f;
            for (n = 0; n < 40; n++) {
                num += ptr_sig_in[n] * y_up[ptr_y_up + n];
            }
            if (num < 0.0f) {
                num = 0.0f;
            }
            numsq = num * num;
            den1 = tab_den1[ptr_den1];
            ptr_den1++;
            if (numsq * den_max > numsq_max * den1) {
                num_max = num;
                numsq_max = numsq;
                den_max = den1;
                ioff = 1;
                phi_max = phi;
            }
            ptr_y_up += 40;
        }
        if (num_max == 0.0f || den_max <= 0.1f) {
            num_gltp.value = 0.0f;
            den_gltp.value = 1.0f;
            ltpdel.value = 0;
            phase.value = 0;
        } else if (numsq_max >= (den_max * ener) * 0.5f) {
            ltpdel.value = (lambda + 1) - ioff;
            off_yup.value = ioff;
            phase.value = phi_max;
            num_gltp.value = num_max;
            den_gltp.value = den_max;
        } else {
            num_gltp.value = 0.0f;
            den_gltp.value = 1.0f;
            ltpdel.value = 0;
            phase.value = 0;
        }
    }

    private void filt_plt(float[] s_in, int s_in_offset, float[] s_ltp, int s_ltp_offset, float[] s_out, int s_out_offset, float gain_plt) {
        float gain_plt_1 = 1.0f - gain_plt;
        for (int n = 0; n < 40; n++) {
            s_out[s_out_offset + n] = (gain_plt * s_in[s_in_offset + n]) + (s_ltp[s_ltp_offset + n] * gain_plt_1);
        }
    }

    private void compute_ltp_l(float[] s_in, int s_in_offset, int ltpdel, int phase, float[] y_up, int y_up_offset, FloatReference num, FloatReference den) {
        int n;
        float[] tab_hup_l = TabLd8k.tab_hup_l;
        int ptr_h = (phase - 1) * 16;
        int ptr2 = (s_in_offset - ltpdel) + 8;
        int toIndex = y_up_offset + 40;
        for (n = y_up_offset; n < toIndex; n++) {
            float temp = 0.0f;
            for (int i = 0; i < 16; i++) {
                temp += tab_hup_l[ptr_h + i] * s_in[ptr2];
                ptr2--;
            }
            y_up[n] = temp;
            ptr2 += 17;
        }
        float _num = 0.0f;
        for (n = 0; n < 40; n++) {
            _num += y_up[y_up_offset + n] * s_in[s_in_offset + n];
        }
        if (_num < 0.0f) {
            _num = 0.0f;
        }
        num.value = _num;
        float _den = 0.0f;
        for (n = y_up_offset; n < y_up_offset + 40; n++) {
            _den += y_up[n] * y_up[n];
        }
        den.value = _den;
    }

    private int select_ltp(float num1, float den1, float num2, float den2) {
        if (den2 != 0.0f && (num2 * num2) * den1 > (num1 * num1) * den2) {
            return 2;
        }
        return 1;
    }

    private float calc_st_filt(float[] apond2, float[] apond1, float[] sig_ltp_ptr, int sig_ltp_ptr_offset) {
        int i;
        float[] h = new float[20];
        Filter.syn_filt(apond1, 0, apond2, 0, h, 0, 20, this.mem_zero, 0, 0);
        float parcor0 = calc_rc0_h(h);
        float g0 = 0.0f;
        for (i = 0; i < 20; i++) {
            g0 += Math.abs(h[i]);
        }
        if (g0 > 1.0f) {
            float temp = 1.0f / g0;
            int toIndex = sig_ltp_ptr_offset + 40;
            for (i = sig_ltp_ptr_offset; i < toIndex; i++) {
                sig_ltp_ptr[i] = sig_ltp_ptr[i] * temp;
            }
        }
        return parcor0;
    }

    private float calc_rc0_h(float[] h) {
        int i;
        float temp = 0.0f;
        for (i = 0; i < 20; i++) {
            temp += h[i] * h[i];
        }
        float acf0 = temp;
        temp = 0.0f;
        int ptrs = 0;
        for (i = 0; i < 19; i++) {
            float temp2 = h[ptrs];
            ptrs++;
            temp += h[ptrs] * temp2;
        }
        float acf1 = temp;
        if (acf0 != 0.0f && acf0 >= Math.abs(acf1)) {
            return (-acf1) / acf0;
        }
        return 0.0f;
    }

    private void filt_mu(float[] sig_in, float[] sig_out, int sig_out_offset, float parcor0) {
        float mu;
        if (parcor0 > 0.0f) {
            mu = parcor0 * 0.2f;
        } else {
            mu = parcor0 * 0.9f;
        }
        float ga = 1.0f / (1.0f - Math.abs(mu));
        int ptrs = 0;
        for (int n = 0; n < 40; n++) {
            ptrs++;
            sig_out[sig_out_offset + n] = ga * ((mu * sig_in[ptrs]) + sig_in[ptrs]);
        }
    }

    private float scale_st(float[] sig_in, int sig_in_offset, float[] sig_out, int sig_out_offset, float gain_prec) {
        int i;
        float g0;
        float gain_prec2;
        float gain_in = 0.0f;
        for (i = sig_in_offset; i < sig_in_offset + 40; i++) {
            gain_in += Math.abs(sig_in[i]);
        }
        if (gain_in == 0.0f) {
            g0 = 0.0f;
        } else {
            float gain_out = 0.0f;
            for (i = sig_out_offset; i < sig_out_offset + 40; i++) {
                gain_out += Math.abs(sig_out[i]);
            }
            if (gain_out == 0.0f) {
                gain_prec2 = 0.0f;
                return 0.0f;
            }
            g0 = (gain_in / gain_out) * 0.012499988f;
        }
        int toIndex = sig_out_offset + 40;
        for (i = sig_out_offset; i < toIndex; i++) {
            gain_prec = (gain_prec * 0.9875f) + g0;
            sig_out[i] = sig_out[i] * gain_prec;
        }
        gain_prec2 = gain_prec;
        return gain_prec;
    }
}
