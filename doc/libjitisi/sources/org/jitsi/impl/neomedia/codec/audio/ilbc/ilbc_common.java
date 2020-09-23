package org.jitsi.impl.neomedia.codec.audio.ilbc;

class ilbc_common {
    ilbc_common() {
    }

    public static int LSF_check(float[] lsf, int dim, int NoAn) {
        int change = 0;
        for (int n = 0; n < 2; n++) {
            for (int m = 0; m < NoAn; m++) {
                for (int k = 0; k < dim - 1; k++) {
                    int pos = (m * dim) + k;
                    if (lsf[pos + 1] - lsf[pos] < 0.039f) {
                        if (lsf[pos + 1] < lsf[pos]) {
                            lsf[pos + 1] = lsf[pos] + 0.0195f;
                            lsf[pos] = lsf[pos + 1] - 0.0195f;
                        } else {
                            lsf[pos] = lsf[pos] - 0.0195f;
                            int i = pos + 1;
                            lsf[i] = lsf[i] + 0.0195f;
                        }
                        change = 1;
                    }
                    if (lsf[pos] < 0.01f) {
                        lsf[pos] = 0.01f;
                        change = 1;
                    }
                    if (lsf[pos] > 3.14f) {
                        lsf[pos] = 3.14f;
                        change = 1;
                    }
                }
            }
        }
        return change;
    }

    public static void StateConstructW(int idxForMax, int[] idxVec, float[] syntDenum, int syntDenum_idx, float[] out, int out_idx, int len) {
        int li;
        int k;
        float[] tmpbuf = new float[(ilbc_constants.LPC_FILTERORDER + (ilbc_constants.STATE_LEN * 2))];
        float[] numerator = new float[(ilbc_constants.LPC_FILTERORDER + 1)];
        float[] foutbuf = new float[(ilbc_constants.LPC_FILTERORDER + (ilbc_constants.STATE_LEN * 2))];
        float maxVal = ((float) Math.pow(10.0d, (double) ilbc_constants.state_frgqTbl[idxForMax])) / 4.5f;
        for (li = 0; li < ilbc_constants.LPC_FILTERORDER; li++) {
            tmpbuf[li] = 0.0f;
            foutbuf[li] = 0.0f;
        }
        for (k = 0; k < ilbc_constants.LPC_FILTERORDER; k++) {
            numerator[k] = syntDenum[(ilbc_constants.LPC_FILTERORDER + syntDenum_idx) - k];
        }
        numerator[ilbc_constants.LPC_FILTERORDER] = syntDenum[syntDenum_idx];
        int tmp = ilbc_constants.LPC_FILTERORDER;
        int fout = ilbc_constants.LPC_FILTERORDER;
        for (k = 0; k < len; k++) {
            tmpbuf[tmp + k] = ilbc_constants.state_sq3Tbl[idxVec[(len - 1) - k]] * maxVal;
        }
        for (li = 0; li < len; li++) {
            tmpbuf[(tmp + len) + li] = 0.0f;
        }
        ZeroPoleFilter(tmpbuf, tmp, numerator, syntDenum, syntDenum_idx, len * 2, ilbc_constants.LPC_FILTERORDER, foutbuf, fout);
        for (k = 0; k < len; k++) {
            out[out_idx + k] = foutbuf[((fout + len) - 1) - k] + foutbuf[(((len * 2) + fout) - 1) - k];
        }
    }

    public static void AllPoleFilter(float[] InOut, int InOut_idx, float[] Coef, int Coef_idx, int lengthInOut, int orderCoef) {
        for (int n = 0; n < lengthInOut; n++) {
            for (int k = 1; k <= orderCoef; k++) {
                int i = n + InOut_idx;
                InOut[i] = InOut[i] - (Coef[Coef_idx + k] * InOut[(n - k) + InOut_idx]);
            }
        }
    }

    public static void AllZeroFilter(float[] In, int In_idx, float[] Coef, int lengthInOut, int orderCoef, float[] Out, int Out_idx) {
        for (int n = 0; n < lengthInOut; n++) {
            Out[Out_idx] = Coef[0] * In[In_idx];
            for (int k = 1; k <= orderCoef; k++) {
                Out[Out_idx] = Out[Out_idx] + (Coef[k] * In[In_idx - k]);
            }
            Out_idx++;
            In_idx++;
        }
    }

    public static void ZeroPoleFilter(float[] In, int In_idx, float[] ZeroCoef, float[] PoleCoef, int PoleCoef_idx, int lengthInOut, int orderCoef, float[] Out, int Out_idx) {
        AllZeroFilter(In, In_idx, ZeroCoef, lengthInOut, orderCoef, Out, Out_idx);
        AllPoleFilter(Out, Out_idx, PoleCoef, PoleCoef_idx, lengthInOut, orderCoef);
    }

    public static void lsf2a(float[] a_coef, float[] freq) {
        int i;
        int li;
        float[] p = new float[ilbc_constants.LPC_HALFORDER];
        float[] q = new float[ilbc_constants.LPC_HALFORDER];
        float[] a = new float[(ilbc_constants.LPC_HALFORDER + 1)];
        float[] a1 = new float[ilbc_constants.LPC_HALFORDER];
        float[] a2 = new float[ilbc_constants.LPC_HALFORDER];
        float[] b = new float[(ilbc_constants.LPC_HALFORDER + 1)];
        float[] b1 = new float[ilbc_constants.LPC_HALFORDER];
        float[] b2 = new float[ilbc_constants.LPC_HALFORDER];
        for (i = 0; i < ilbc_constants.LPC_FILTERORDER; i++) {
            freq[i] = freq[i] * ilbc_constants.PI2;
        }
        if (freq[0] <= 0.0f || ((double) freq[ilbc_constants.LPC_FILTERORDER - 1]) >= 0.5d) {
            if (freq[0] <= 0.0f) {
                freq[0] = 0.022f;
            }
            if (((double) freq[ilbc_constants.LPC_FILTERORDER - 1]) >= 0.5d) {
                freq[ilbc_constants.LPC_FILTERORDER - 1] = 0.499f;
            }
            float hlp = (freq[ilbc_constants.LPC_FILTERORDER - 1] - freq[0]) / ((float) (ilbc_constants.LPC_FILTERORDER - 1));
            for (i = 1; i < ilbc_constants.LPC_FILTERORDER; i++) {
                freq[i] = freq[i - 1] + hlp;
            }
        }
        for (li = 0; li < ilbc_constants.LPC_HALFORDER; li++) {
            a1[li] = 0.0f;
            a2[li] = 0.0f;
            b1[li] = 0.0f;
            b2[li] = 0.0f;
        }
        for (li = 0; li < ilbc_constants.LPC_HALFORDER + 1; li++) {
            a[li] = 0.0f;
            b[li] = 0.0f;
        }
        for (i = 0; i < ilbc_constants.LPC_HALFORDER; i++) {
            p[i] = (float) Math.cos((double) (ilbc_constants.TWO_PI * freq[i * 2]));
            q[i] = (float) Math.cos((double) (ilbc_constants.TWO_PI * freq[(i * 2) + 1]));
        }
        a[0] = 0.25f;
        b[0] = 0.25f;
        for (i = 0; i < ilbc_constants.LPC_HALFORDER; i++) {
            a[i + 1] = (a[i] - ((2.0f * p[i]) * a1[i])) + a2[i];
            b[i + 1] = (b[i] - ((2.0f * q[i]) * b1[i])) + b2[i];
            a2[i] = a1[i];
            a1[i] = a[i];
            b2[i] = b1[i];
            b1[i] = b[i];
        }
        for (int j = 0; j < ilbc_constants.LPC_FILTERORDER; j++) {
            if (j == 0) {
                a[0] = 0.25f;
                b[0] = -0.25f;
            } else {
                b[0] = 0.0f;
                a[0] = 0.0f;
            }
            for (i = 0; i < ilbc_constants.LPC_HALFORDER; i++) {
                a[i + 1] = (a[i] - ((2.0f * p[i]) * a1[i])) + a2[i];
                b[i + 1] = (b[i] - ((2.0f * q[i]) * b1[i])) + b2[i];
                a2[i] = a1[i];
                a1[i] = a[i];
                b2[i] = b1[i];
                b1[i] = b[i];
            }
            a_coef[j + 1] = 2.0f * (a[ilbc_constants.LPC_HALFORDER] + b[ilbc_constants.LPC_HALFORDER]);
        }
        a_coef[0] = 1.0f;
    }

    public static void interpolate(float[] out, float[] in1, float[] in2, int in2_idx, float coef, int length) {
        float invcoef = 1.0f - coef;
        for (int i = 0; i < length; i++) {
            out[i] = (in1[i] * coef) + (in2[i + in2_idx] * invcoef);
        }
    }

    public static void bwexpand(float[] out, int out_idx, float[] in, float coef, int length) {
        float chirp = coef;
        out[out_idx] = in[0];
        for (int i = 1; i < length; i++) {
            out[i + out_idx] = in[i] * chirp;
            chirp *= coef;
        }
    }

    public static void getCBvec(float[] cbvec, float[] mem, int mem_idx, int index, int lMem, int cbveclen) {
        Object tmpbuf = new float[ilbc_constants.CB_MEML];
        int base_size = (lMem - cbveclen) + 1;
        if (cbveclen == ilbc_constants.SUBL) {
            base_size += cbveclen / 2;
        }
        int k;
        int ihigh;
        int ilow;
        float alfa;
        int j;
        Object tempbuff2;
        int li;
        int memInd;
        int pos;
        int pp;
        int pp1;
        if (index < (lMem - cbveclen) + 1) {
            System.arraycopy(mem, (mem_idx + lMem) - (index + cbveclen), cbvec, 0, cbveclen);
        } else if (index < base_size) {
            k = ((index - ((lMem - cbveclen) + 1)) * 2) + cbveclen;
            ihigh = k / 2;
            ilow = ihigh - 5;
            System.arraycopy(mem, (mem_idx + lMem) - (k / 2), cbvec, 0, ilow);
            alfa = 0.0f;
            for (j = ilow; j < ihigh; j++) {
                cbvec[j] = ((1.0f - alfa) * mem[((mem_idx + lMem) - (k / 2)) + j]) + (mem[((mem_idx + lMem) - k) + j] * alfa);
                alfa += 0.2f;
            }
            System.arraycopy(mem, ((mem_idx + lMem) - k) + ihigh, cbvec, ihigh, cbveclen - ihigh);
        } else if (index - base_size < (lMem - cbveclen) + 1) {
            tempbuff2 = new float[((ilbc_constants.CB_MEML + ilbc_constants.CB_FILTERLEN) + 1)];
            for (li = 0; li < ilbc_constants.CB_HALFFILTERLEN; li++) {
                tempbuff2[li] = 0.0f;
            }
            System.arraycopy(mem, mem_idx, tempbuff2, ilbc_constants.CB_HALFFILTERLEN, lMem);
            for (li = 0; li < ilbc_constants.CB_HALFFILTERLEN + 1; li++) {
                tempbuff2[(ilbc_constants.CB_HALFFILTERLEN + lMem) + li] = 0.0f;
            }
            memInd = ((lMem - ((index - base_size) + cbveclen)) + 1) - ilbc_constants.CB_HALFFILTERLEN;
            pos = 0;
            for (li = 0; li < cbveclen; li++) {
                cbvec[li] = 0.0f;
            }
            for (int n = 0; n < cbveclen; n++) {
                pp = (memInd + n) + ilbc_constants.CB_HALFFILTERLEN;
                pp1 = ilbc_constants.CB_FILTERLEN - 1;
                for (j = 0; j < ilbc_constants.CB_FILTERLEN; j++) {
                    cbvec[pos] = cbvec[pos] + (tempbuff2[pp] * ilbc_constants.cbfiltersTbl[pp1]);
                    pp++;
                    pp1--;
                }
                pos++;
            }
        } else {
            tempbuff2 = new float[((ilbc_constants.CB_MEML + ilbc_constants.CB_FILTERLEN) + 1)];
            for (li = 0; li < ilbc_constants.CB_HALFFILTERLEN; li++) {
                tempbuff2[li] = 0.0f;
            }
            System.arraycopy(mem, mem_idx, tempbuff2, ilbc_constants.CB_HALFFILTERLEN, lMem);
            for (li = 0; li < ilbc_constants.CB_HALFFILTERLEN; li++) {
                tempbuff2[(ilbc_constants.CB_HALFFILTERLEN + lMem) + li] = 0.0f;
            }
            k = (((index - base_size) - ((lMem - cbveclen) + 1)) * 2) + cbveclen;
            int sFilt = lMem - k;
            memInd = (sFilt + 1) - ilbc_constants.CB_HALFFILTERLEN;
            pos = sFilt;
            for (li = 0; li < k; li++) {
                tmpbuf[pos + li] = 0.0f;
            }
            for (int i = 0; i < k; i++) {
                pp = (memInd + i) + ilbc_constants.CB_HALFFILTERLEN;
                pp1 = ilbc_constants.CB_FILTERLEN - 1;
                for (j = 0; j < ilbc_constants.CB_FILTERLEN; j++) {
                    tmpbuf[pos] = tmpbuf[pos] + (tempbuff2[pp] * ilbc_constants.cbfiltersTbl[pp1]);
                    pp++;
                    pp1--;
                }
                pos++;
            }
            ihigh = k / 2;
            ilow = ihigh - 5;
            System.arraycopy(tmpbuf, lMem - (k / 2), cbvec, 0, ilow);
            alfa = 0.0f;
            for (j = ilow; j < ihigh; j++) {
                cbvec[j] = ((1.0f - alfa) * tmpbuf[(lMem - (k / 2)) + j]) + (tmpbuf[(lMem - k) + j] * alfa);
                alfa += 0.2f;
            }
            System.arraycopy(tmpbuf, (lMem - k) + ihigh, cbvec, ihigh, cbveclen - ihigh);
        }
    }

    public static float gainquant(float in, float maxIn, int cblen, int[] index, int index_idx) {
        float[] cb;
        float scale = maxIn;
        if (((double) scale) < 0.1d) {
            scale = 0.1f;
        }
        if (cblen == 8) {
            cb = ilbc_constants.gain_sq3Tbl;
        } else if (cblen == 16) {
            cb = ilbc_constants.gain_sq4Tbl;
        } else {
            cb = ilbc_constants.gain_sq5Tbl;
        }
        float minmeasure = 1.0E7f;
        int tindex = 0;
        for (int i = 0; i < cblen; i++) {
            float measure = (in - (cb[i] * scale)) * (in - (cb[i] * scale));
            if (measure < minmeasure) {
                tindex = i;
                minmeasure = measure;
            }
        }
        index[index_idx] = tindex;
        return cb[tindex] * scale;
    }

    public static float gaindequant(int index, float maxIn, int cblen) {
        float scale = Math.abs(maxIn);
        if (((double) scale) < 0.1d) {
            scale = 0.1f;
        }
        if (cblen == 8) {
            return ilbc_constants.gain_sq3Tbl[index] * scale;
        }
        if (cblen == 16) {
            return ilbc_constants.gain_sq4Tbl[index] * scale;
        }
        if (cblen == 32) {
            return ilbc_constants.gain_sq5Tbl[index] * scale;
        }
        return 0.0f;
    }

    public static void iCBConstruct(float[] decvector, int decvector_idx, int[] index, int index_idx, int[] gain_index, int gain_index_idx, float[] mem, int mem_idx, int lMem, int veclen, int nStages) {
        int j;
        float[] gain = new float[ilbc_constants.CB_NSTAGES];
        float[] cbvec = new float[ilbc_constants.SUBL];
        gain[0] = gaindequant(gain_index[gain_index_idx + 0], 1.0f, 32);
        if (nStages > 1) {
            gain[1] = gaindequant(gain_index[gain_index_idx + 1], Math.abs(gain[0]), 16);
        }
        if (nStages > 2) {
            gain[2] = gaindequant(gain_index[gain_index_idx + 2], Math.abs(gain[1]), 8);
        }
        getCBvec(cbvec, mem, mem_idx, index[index_idx + 0], lMem, veclen);
        for (j = 0; j < veclen; j++) {
            decvector[decvector_idx + j] = gain[0] * cbvec[j];
        }
        if (nStages > 1) {
            for (int k = 1; k < nStages; k++) {
                getCBvec(cbvec, mem, mem_idx, index[index_idx + k], lMem, veclen);
                for (j = 0; j < veclen; j++) {
                    int i = decvector_idx + j;
                    decvector[i] = decvector[i] + (gain[k] * cbvec[j]);
                }
            }
        }
    }
}
