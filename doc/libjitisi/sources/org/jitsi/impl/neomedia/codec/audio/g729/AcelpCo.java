package org.jitsi.impl.neomedia.codec.audio.g729;

class AcelpCo {
    private int extra;

    AcelpCo() {
    }

    /* access modifiers changed from: 0000 */
    public int ACELP_codebook(float[] x, float[] h, int t0, float pitch_sharp, int i_subfr, float[] code, float[] y, IntReference sign) {
        int i;
        float[] dn = new float[40];
        float[] rr = new float[616];
        if (t0 < 40) {
            for (i = t0; i < 40; i++) {
                h[i] = h[i] + (h[i - t0] * pitch_sharp);
            }
        }
        cor_h(h, rr);
        CorFunc.cor_h_x(h, x, dn);
        int index = d4i40_17(dn, rr, h, code, y, sign, i_subfr);
        if (t0 < 40) {
            for (i = t0; i < 40; i++) {
                code[i] = code[i] + (code[i - t0] * pitch_sharp);
            }
        }
        return index;
    }

    private void cor_h(float[] h, float[] rr) {
        int i;
        int k;
        int ptr_h2;
        int rri1i1 = 0 + 8;
        int rri2i2 = 8 + 8;
        int rri3i3 = 8 + 16;
        int rri4i4 = 8 + 24;
        int rri0i1 = 8 + 32;
        int rri0i2 = 64 + 40;
        int rri0i3 = 64 + 104;
        int rri0i4 = 64 + 168;
        int rri1i2 = 64 + 232;
        int rri1i3 = 64 + 296;
        int rri1i4 = 64 + 360;
        int rri2i3 = 64 + 424;
        int rri2i4 = 64 + 488;
        int p0 = 8 - 1;
        int p1 = 16 - 1;
        int p2 = 24 - 1;
        int p3 = 32 - 1;
        int p4 = 40 - 1;
        int ptr_h1 = 0;
        float cor = 0.0f;
        for (i = 0; i < 8; i++) {
            cor += h[ptr_h1] * h[ptr_h1];
            ptr_h1++;
            rr[p4] = cor;
            p4--;
            cor += h[ptr_h1] * h[ptr_h1];
            ptr_h1++;
            rr[p3] = cor;
            p3--;
            cor += h[ptr_h1] * h[ptr_h1];
            ptr_h1++;
            rr[p2] = cor;
            p2--;
            cor += h[ptr_h1] * h[ptr_h1];
            ptr_h1++;
            rr[p1] = cor;
            p1--;
            cor += h[ptr_h1] * h[ptr_h1];
            ptr_h1++;
            rr[p0] = cor;
            p0--;
        }
        int l_fin_sup = 64 - 1;
        int l_fin_inf = l_fin_sup - 1;
        int ldec = 8 + 1;
        int ptr_hf = 0 + 1;
        for (k = 0; k < 8; k++) {
            p3 = l_fin_sup + 488;
            p2 = l_fin_sup + 296;
            p1 = l_fin_sup + 40;
            p0 = l_fin_inf + 232;
            cor = 0.0f;
            ptr_h1 = 0;
            ptr_h2 = ptr_hf;
            for (i = k + 1; i < 8; i++) {
                ptr_h1++;
                ptr_h2++;
                cor = (cor + (h[ptr_h1] * h[ptr_h2])) + (h[ptr_h1] * h[ptr_h2]);
                ptr_h1++;
                ptr_h2++;
                rr[p3] = cor;
                cor += h[ptr_h1] * h[ptr_h2];
                ptr_h1++;
                ptr_h2++;
                rr[p2] = cor;
                cor += h[ptr_h1] * h[ptr_h2];
                ptr_h1++;
                ptr_h2++;
                rr[p1] = cor;
                cor += h[ptr_h1] * h[ptr_h2];
                ptr_h1++;
                ptr_h2++;
                rr[p0] = cor;
                p3 -= 9;
                p2 -= 9;
                p1 -= 9;
                p0 -= 9;
            }
            ptr_h1++;
            ptr_h2++;
            cor = (cor + (h[ptr_h1] * h[ptr_h2])) + (h[ptr_h1] * h[ptr_h2]);
            ptr_h1++;
            ptr_h2++;
            rr[p3] = cor;
            cor += h[ptr_h1] * h[ptr_h2];
            ptr_h1++;
            ptr_h2++;
            rr[p2] = cor;
            ptr_h1++;
            ptr_h2++;
            rr[p1] = cor + (h[ptr_h1] * h[ptr_h2]);
            l_fin_sup -= 8;
            l_fin_inf--;
            ptr_hf += 5;
        }
        ptr_hf = 0 + 2;
        l_fin_sup = 64 - 1;
        l_fin_inf = l_fin_sup - 1;
        for (k = 0; k < 8; k++) {
            p4 = l_fin_sup + 552;
            p3 = l_fin_sup + 360;
            p2 = l_fin_sup + 104;
            p1 = l_fin_inf + 424;
            p0 = l_fin_inf + 168;
            cor = 0.0f;
            ptr_h1 = 0;
            ptr_h2 = ptr_hf;
            for (i = k + 1; i < 8; i++) {
                cor += h[ptr_h1] * h[ptr_h2];
                ptr_h1++;
                ptr_h2++;
                rr[p4] = cor;
                cor += h[ptr_h1] * h[ptr_h2];
                ptr_h1++;
                ptr_h2++;
                rr[p3] = cor;
                cor += h[ptr_h1] * h[ptr_h2];
                ptr_h1++;
                ptr_h2++;
                rr[p2] = cor;
                cor += h[ptr_h1] * h[ptr_h2];
                ptr_h1++;
                ptr_h2++;
                rr[p1] = cor;
                cor += h[ptr_h1] * h[ptr_h2];
                ptr_h1++;
                ptr_h2++;
                rr[p0] = cor;
                p4 -= 9;
                p3 -= 9;
                p2 -= 9;
                p1 -= 9;
                p0 -= 9;
            }
            cor += h[ptr_h1] * h[ptr_h2];
            ptr_h1++;
            ptr_h2++;
            rr[p4] = cor;
            cor += h[ptr_h1] * h[ptr_h2];
            ptr_h1++;
            ptr_h2++;
            rr[p3] = cor;
            ptr_h1++;
            ptr_h2++;
            rr[p2] = cor + (h[ptr_h1] * h[ptr_h2]);
            l_fin_sup -= 8;
            l_fin_inf--;
            ptr_hf += 5;
        }
        ptr_hf = 0 + 3;
        l_fin_sup = 64 - 1;
        l_fin_inf = l_fin_sup - 1;
        for (k = 0; k < 8; k++) {
            p4 = l_fin_sup + 424;
            p3 = l_fin_sup + 168;
            p2 = l_fin_inf + 552;
            p1 = l_fin_inf + 360;
            p0 = l_fin_inf + 104;
            ptr_h1 = 0;
            ptr_h2 = ptr_hf;
            cor = 0.0f;
            for (i = k + 1; i < 8; i++) {
                cor += h[ptr_h1] * h[ptr_h2];
                ptr_h1++;
                ptr_h2++;
                rr[p4] = cor;
                cor += h[ptr_h1] * h[ptr_h2];
                ptr_h1++;
                ptr_h2++;
                rr[p3] = cor;
                cor += h[ptr_h1] * h[ptr_h2];
                ptr_h1++;
                ptr_h2++;
                rr[p2] = cor;
                cor += h[ptr_h1] * h[ptr_h2];
                ptr_h1++;
                ptr_h2++;
                rr[p1] = cor;
                cor += h[ptr_h1] * h[ptr_h2];
                ptr_h1++;
                ptr_h2++;
                rr[p0] = cor;
                p4 -= 9;
                p3 -= 9;
                p2 -= 9;
                p1 -= 9;
                p0 -= 9;
            }
            cor += h[ptr_h1] * h[ptr_h2];
            ptr_h1++;
            ptr_h2++;
            rr[p4] = cor;
            ptr_h1++;
            ptr_h2++;
            rr[p3] = cor + (h[ptr_h1] * h[ptr_h2]);
            l_fin_sup -= 8;
            l_fin_inf--;
            ptr_hf += 5;
        }
        ptr_hf = 0 + 4;
        l_fin_sup = 64 - 1;
        l_fin_inf = l_fin_sup - 1;
        for (k = 0; k < 8; k++) {
            p3 = l_fin_sup + 232;
            p2 = l_fin_inf + 488;
            p1 = l_fin_inf + 296;
            p0 = l_fin_inf + 40;
            ptr_h1 = 0;
            ptr_h2 = ptr_hf;
            cor = 0.0f;
            for (i = k + 1; i < 8; i++) {
                cor += h[ptr_h1] * h[ptr_h2];
                ptr_h1++;
                ptr_h2++;
                rr[p3] = cor;
                ptr_h1++;
                ptr_h2++;
                cor = (cor + (h[ptr_h1] * h[ptr_h2])) + (h[ptr_h1] * h[ptr_h2]);
                ptr_h1++;
                ptr_h2++;
                rr[p2] = cor;
                cor += h[ptr_h1] * h[ptr_h2];
                ptr_h1++;
                ptr_h2++;
                rr[p1] = cor;
                cor += h[ptr_h1] * h[ptr_h2];
                ptr_h1++;
                ptr_h2++;
                rr[p0] = cor;
                p3 -= 9;
                p2 -= 9;
                p1 -= 9;
                p0 -= 9;
            }
            ptr_h1++;
            ptr_h2++;
            rr[p3] = cor + (h[ptr_h1] * h[ptr_h2]);
            l_fin_sup -= 8;
            l_fin_inf--;
            ptr_hf += 5;
        }
    }

    private int d4i40_17(float[] dn, float[] rr, float[] h, float[] cod, float[] y, IntReference signs, int i_subfr) {
        int i;
        int i0;
        int i1;
        int i2;
        int i3;
        int j;
        float[] p_sign = new float[40];
        int rri1i1 = 0 + 8;
        int rri2i2 = 8 + 8;
        int rri3i3 = 8 + 16;
        int rri4i4 = 8 + 24;
        int rri0i1 = 8 + 32;
        int rri0i2 = 64 + 40;
        int rri0i3 = 64 + 104;
        int rri0i4 = 64 + 168;
        int rri1i2 = 64 + 232;
        int rri1i3 = 64 + 296;
        int rri1i4 = 64 + 360;
        int rri2i3 = 64 + 424;
        int rri2i4 = 64 + 488;
        if (i_subfr == 0) {
            this.extra = 30;
        }
        for (i = 0; i < 40; i++) {
            if (dn[i] >= 0.0f) {
                p_sign[i] = 1.0f;
            } else {
                p_sign[i] = -1.0f;
                dn[i] = -dn[i];
            }
        }
        float average = (dn[0] + dn[1]) + dn[2];
        float max0 = dn[0];
        float max1 = dn[1];
        float max2 = dn[2];
        for (i = 5; i < 40; i += 5) {
            average += (dn[i] + dn[i + 1]) + dn[i + 2];
            if (dn[i] > max0) {
                max0 = dn[i];
            }
            if (dn[i + 1] > max1) {
                max1 = dn[i + 1];
            }
            if (dn[i + 2] > max2) {
                max2 = dn[i + 2];
            }
        }
        average *= 0.125f;
        float thres = average + (((max0 + (max1 + max2)) - average) * 0.4f);
        int ptr_ri0i1 = rri0i1;
        int ptr_ri0i2 = rri0i2;
        int ptr_ri0i3 = rri0i3;
        int ptr_ri0i4 = rri0i4;
        for (i0 = 0; i0 < 40; i0 += 5) {
            for (i1 = 1; i1 < 40; i1 += 5) {
                rr[ptr_ri0i1] = rr[ptr_ri0i1] * (p_sign[i0] * p_sign[i1]);
                ptr_ri0i1++;
                rr[ptr_ri0i2] = rr[ptr_ri0i2] * (p_sign[i0] * p_sign[i1 + 1]);
                ptr_ri0i2++;
                rr[ptr_ri0i3] = rr[ptr_ri0i3] * (p_sign[i0] * p_sign[i1 + 2]);
                ptr_ri0i3++;
                rr[ptr_ri0i4] = rr[ptr_ri0i4] * (p_sign[i0] * p_sign[i1 + 3]);
                ptr_ri0i4++;
            }
        }
        int ptr_ri1i2 = rri1i2;
        int ptr_ri1i3 = rri1i3;
        int ptr_ri1i4 = rri1i4;
        for (i1 = 1; i1 < 40; i1 += 5) {
            for (i2 = 2; i2 < 40; i2 += 5) {
                rr[ptr_ri1i2] = rr[ptr_ri1i2] * (p_sign[i1] * p_sign[i2]);
                ptr_ri1i2++;
                rr[ptr_ri1i3] = rr[ptr_ri1i3] * (p_sign[i1] * p_sign[i2 + 1]);
                ptr_ri1i3++;
                rr[ptr_ri1i4] = rr[ptr_ri1i4] * (p_sign[i1] * p_sign[i2 + 2]);
                ptr_ri1i4++;
            }
        }
        int ptr_ri2i3 = rri2i3;
        int ptr_ri2i4 = rri2i4;
        for (i2 = 2; i2 < 40; i2 += 5) {
            for (i3 = 3; i3 < 40; i3 += 5) {
                rr[ptr_ri2i3] = rr[ptr_ri2i3] * (p_sign[i2] * p_sign[i3]);
                ptr_ri2i3++;
                rr[ptr_ri2i4] = rr[ptr_ri2i4] * (p_sign[i2] * p_sign[i3 + 1]);
                ptr_ri2i4++;
            }
        }
        int ip0 = 0;
        int ip1 = 1;
        int ip2 = 2;
        int ip3 = 3;
        float psc = 0.0f;
        float alpha = 1000000.0f;
        int time = 75 + this.extra;
        int ptr_ri0i0 = 0;
        ptr_ri0i1 = rri0i1;
        ptr_ri0i2 = rri0i2;
        ptr_ri0i3 = rri0i3;
        ptr_ri0i4 = rri0i4;
        loop8:
        for (i0 = 0; i0 < 40; i0 += 5) {
            float ps0 = dn[i0];
            float alp0 = rr[ptr_ri0i0];
            ptr_ri0i0++;
            int ptr_ri1i1 = rri1i1;
            ptr_ri1i2 = rri1i2;
            ptr_ri1i3 = rri1i3;
            ptr_ri1i4 = rri1i4;
            for (i1 = 1; i1 < 40; i1 += 5) {
                float ps1 = ps0 + dn[i1];
                float alp1 = (rr[ptr_ri1i1] + alp0) + (2.0f * rr[ptr_ri0i1]);
                ptr_ri1i1++;
                ptr_ri0i1++;
                int ptr_ri2i2 = rri2i2;
                ptr_ri2i3 = rri2i3;
                ptr_ri2i4 = rri2i4;
                for (i2 = 2; i2 < 40; i2 += 5) {
                    float ps2 = ps1 + dn[i2];
                    float alp2 = (rr[ptr_ri2i2] + alp1) + (2.0f * (rr[ptr_ri0i2] + rr[ptr_ri1i2]));
                    ptr_ri2i2++;
                    ptr_ri0i2++;
                    ptr_ri1i2++;
                    if (ps2 > thres) {
                        float ps3;
                        float alp3;
                        float ps3c;
                        int ptr_ri3i3 = rri3i3;
                        for (i3 = 3; i3 < 40; i3 += 5) {
                            ps3 = ps2 + dn[i3];
                            alp3 = (rr[ptr_ri3i3] + alp2) + (2.0f * ((rr[ptr_ri1i3] + rr[ptr_ri0i3]) + rr[ptr_ri2i3]));
                            ptr_ri3i3++;
                            ptr_ri1i3++;
                            ptr_ri0i3++;
                            ptr_ri2i3++;
                            ps3c = ps3 * ps3;
                            if (ps3c * alpha > psc * alp3) {
                                psc = ps3c;
                                alpha = alp3;
                                ip0 = i0;
                                ip1 = i1;
                                ip2 = i2;
                                ip3 = i3;
                            }
                        }
                        ptr_ri0i3 -= 8;
                        ptr_ri1i3 -= 8;
                        int ptr_ri4i4 = rri4i4;
                        for (i3 = 4; i3 < 40; i3 += 5) {
                            ps3 = ps2 + dn[i3];
                            alp3 = (rr[ptr_ri4i4] + alp2) + (2.0f * ((rr[ptr_ri1i4] + rr[ptr_ri0i4]) + rr[ptr_ri2i4]));
                            ptr_ri4i4++;
                            ptr_ri1i4++;
                            ptr_ri0i4++;
                            ptr_ri2i4++;
                            ps3c = ps3 * ps3;
                            if (ps3c * alpha > psc * alp3) {
                                psc = ps3c;
                                alpha = alp3;
                                ip0 = i0;
                                ip1 = i1;
                                ip2 = i2;
                                ip3 = i3;
                            }
                        }
                        ptr_ri0i4 -= 8;
                        ptr_ri1i4 -= 8;
                        time--;
                        if (time <= 0) {
                            break loop8;
                        }
                    } else {
                        ptr_ri2i3 += 8;
                        ptr_ri2i4 += 8;
                    }
                }
                ptr_ri0i2 -= 8;
                ptr_ri1i3 += 8;
                ptr_ri1i4 += 8;
            }
            ptr_ri0i2 += 8;
            ptr_ri0i3 += 8;
            ptr_ri0i4 += 8;
        }
        this.extra = time;
        for (i = 0; i < 40; i++) {
            cod[i] = 0.0f;
        }
        cod[ip0] = p_sign[ip0];
        cod[ip1] = p_sign[ip1];
        cod[ip2] = p_sign[ip2];
        cod[ip3] = p_sign[ip3];
        for (i = 0; i < 40; i++) {
            y[i] = 0.0f;
        }
        if (p_sign[ip0] > 0.0f) {
            i = ip0;
            j = 0;
            while (i < 40) {
                y[i] = h[j];
                i++;
                j++;
            }
        } else {
            i = ip0;
            j = 0;
            while (i < 40) {
                y[i] = -h[j];
                i++;
                j++;
            }
        }
        if (p_sign[ip1] > 0.0f) {
            i = ip1;
            j = 0;
            while (i < 40) {
                y[i] = y[i] + h[j];
                i++;
                j++;
            }
        } else {
            i = ip1;
            j = 0;
            while (i < 40) {
                y[i] = y[i] - h[j];
                i++;
                j++;
            }
        }
        if (p_sign[ip2] > 0.0f) {
            i = ip2;
            j = 0;
            while (i < 40) {
                y[i] = y[i] + h[j];
                i++;
                j++;
            }
        } else {
            i = ip2;
            j = 0;
            while (i < 40) {
                y[i] = y[i] - h[j];
                i++;
                j++;
            }
        }
        if (p_sign[ip3] > 0.0f) {
            i = ip3;
            j = 0;
            while (i < 40) {
                y[i] = y[i] + h[j];
                i++;
                j++;
            }
        } else {
            i = ip3;
            j = 0;
            while (i < 40) {
                y[i] = y[i] - h[j];
                i++;
                j++;
            }
        }
        i = 0;
        if (p_sign[ip0] > 0.0f) {
            i = 0 + 1;
        }
        if (p_sign[ip1] > 0.0f) {
            i += 2;
        }
        if (p_sign[ip2] > 0.0f) {
            i += 4;
        }
        if (p_sign[ip3] > 0.0f) {
            i += 8;
        }
        signs.value = i;
        return ((((ip1 / 5) << 3) + (ip0 / 5)) + ((ip2 / 5) << 6)) + ((((ip3 / 5) << 1) + ((ip3 % 5) - 3)) << 9);
    }
}
