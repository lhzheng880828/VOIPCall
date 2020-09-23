package org.jitsi.impl.neomedia.codec.audio.ilbc;

import org.jitsi.impl.neomedia.ArrayIOUtils;

class ilbc_encoder {
    ilbc_ulp ULP_inst = null;
    float[] anaMem;
    float[] hpimem;
    float[] lpc_buffer;
    float[] lsfdeqold;
    float[] lsfold;
    int mode;

    /* access modifiers changed from: 0000 */
    public void AbsQuantW(float[] in, int in_idx, float[] syntDenum, int syntDenum_idx, float[] weightDenum, int weightDenum_idx, int[] out, int len, int state_first) {
        float[] syntOutBuf = new float[(ilbc_constants.LPC_FILTERORDER + ilbc_constants.STATE_SHORT_LEN_30MS)];
        int[] index = new int[1];
        for (int li = 0; li < ilbc_constants.LPC_FILTERORDER; li++) {
            syntOutBuf[li] = 0.0f;
        }
        int syntOut = ilbc_constants.LPC_FILTERORDER;
        if (state_first != 0) {
            ilbc_common.AllPoleFilter(in, in_idx, weightDenum, weightDenum_idx, ilbc_constants.SUBL, ilbc_constants.LPC_FILTERORDER);
        } else {
            ilbc_common.AllPoleFilter(in, in_idx, weightDenum, weightDenum_idx, this.ULP_inst.state_short_len - ilbc_constants.SUBL, ilbc_constants.LPC_FILTERORDER);
        }
        int n = 0;
        while (n < len) {
            if (state_first != 0 && n == ilbc_constants.SUBL) {
                syntDenum_idx += ilbc_constants.LPC_FILTERORDER + 1;
                weightDenum_idx += ilbc_constants.LPC_FILTERORDER + 1;
                ilbc_common.AllPoleFilter(in, in_idx + n, weightDenum, weightDenum_idx, len - n, ilbc_constants.LPC_FILTERORDER);
            } else if (state_first == 0 && n == this.ULP_inst.state_short_len - ilbc_constants.SUBL) {
                syntDenum_idx += ilbc_constants.LPC_FILTERORDER + 1;
                weightDenum_idx += ilbc_constants.LPC_FILTERORDER + 1;
                ilbc_common.AllPoleFilter(in, in_idx + n, weightDenum, weightDenum_idx, len - n, ilbc_constants.LPC_FILTERORDER);
            }
            syntOutBuf[syntOut + n] = 0.0f;
            ilbc_common.AllPoleFilter(syntOutBuf, syntOut + n, weightDenum, weightDenum_idx, 1, ilbc_constants.LPC_FILTERORDER);
            sort_sq(index, 0, in[in_idx + n] - syntOutBuf[syntOut + n], ilbc_constants.state_sq3Tbl, 8);
            out[n] = index[0];
            syntOutBuf[syntOut + n] = ilbc_constants.state_sq3Tbl[out[n]];
            ilbc_common.AllPoleFilter(syntOutBuf, syntOut + n, weightDenum, weightDenum_idx, 1, ilbc_constants.LPC_FILTERORDER);
            n++;
        }
    }

    /* access modifiers changed from: 0000 */
    public void StateSearchW(float[] residual, int residual_idx, float[] syntDenum, int syntDenum_idx, float[] weightDenum, int weightDenum_idx, int[] idxForMax, int[] idxVec, int len, int state_first) {
        int li;
        int k;
        int i;
        float[] tmpbuf = new float[(ilbc_constants.LPC_FILTERORDER + (ilbc_constants.STATE_SHORT_LEN_30MS * 2))];
        float[] numerator = new float[(ilbc_constants.LPC_FILTERORDER + 1)];
        float[] foutbuf = new float[(ilbc_constants.LPC_FILTERORDER + (ilbc_constants.STATE_SHORT_LEN_30MS * 2))];
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
        System.arraycopy(residual, residual_idx, tmpbuf, tmp, len);
        for (li = 0; li < len; li++) {
            tmpbuf[(tmp + len) + li] = 0.0f;
        }
        ilbc_common.ZeroPoleFilter(tmpbuf, tmp, numerator, syntDenum, syntDenum_idx, len * 2, ilbc_constants.LPC_FILTERORDER, foutbuf, fout);
        for (k = 0; k < len; k++) {
            i = fout + k;
            foutbuf[i] = foutbuf[i] + foutbuf[(fout + k) + len];
        }
        float maxVal = foutbuf[fout + 0];
        for (k = 1; k < len; k++) {
            if (foutbuf[fout + k] * foutbuf[fout + k] > maxVal * maxVal) {
                maxVal = foutbuf[fout + k];
            }
        }
        maxVal = Math.abs(maxVal);
        if (maxVal < 10.0f) {
            maxVal = 10.0f;
        }
        sort_sq(idxForMax, 0, (float) (Math.log((double) maxVal) / Math.log(10.0d)), ilbc_constants.state_frgqTbl, 64);
        float scal = 4.5f / ((float) Math.pow(10.0d, (double) ilbc_constants.state_frgqTbl[idxForMax[0]]));
        for (k = 0; k < len; k++) {
            i = fout + k;
            foutbuf[i] = foutbuf[i] * scal;
        }
        AbsQuantW(foutbuf, fout, syntDenum, syntDenum_idx, weightDenum, weightDenum_idx, idxVec, len, state_first);
    }

    /* access modifiers changed from: 0000 */
    public void a2lsf(float[] freq, int freq_idx, float[] a) {
        int i;
        float[] fArr = new float[4];
        fArr = new float[]{0.00635f, 0.003175f, 0.0015875f, 7.9375E-4f};
        float[] p = new float[ilbc_constants.LPC_HALFORDER];
        float[] q = new float[ilbc_constants.LPC_HALFORDER];
        float[] p_pre = new float[ilbc_constants.LPC_HALFORDER];
        float[] q_pre = new float[ilbc_constants.LPC_HALFORDER];
        float[] olds = new float[2];
        for (i = 0; i < ilbc_constants.LPC_HALFORDER; i++) {
            p[i] = -1.0f * (a[i + 1] + a[ilbc_constants.LPC_FILTERORDER - i]);
            q[i] = a[ilbc_constants.LPC_FILTERORDER - i] - a[i + 1];
        }
        p_pre[0] = -1.0f - p[0];
        p_pre[1] = (-p_pre[0]) - p[1];
        p_pre[2] = (-p_pre[1]) - p[2];
        p_pre[3] = (-p_pre[2]) - p[3];
        p_pre[4] = (-p_pre[3]) - p[4];
        p_pre[4] = p_pre[4] / 2.0f;
        q_pre[0] = 1.0f - q[0];
        q_pre[1] = q_pre[0] - q[1];
        q_pre[2] = q_pre[1] - q[2];
        q_pre[3] = q_pre[2] - q[3];
        q_pre[4] = q_pre[3] - q[4];
        q_pre[4] = q_pre[4] / 2.0f;
        float omega = 0.0f;
        float old_omega = 0.0f;
        olds[0] = ilbc_constants.DOUBLE_MAX;
        olds[1] = ilbc_constants.DOUBLE_MAX;
        for (int lsp_index = 0; lsp_index < ilbc_constants.LPC_FILTERORDER; lsp_index++) {
            float[] pq_coef;
            int old;
            if ((lsp_index & 1) == 0) {
                pq_coef = p_pre;
                old = 0;
            } else {
                pq_coef = q_pre;
                old = 1;
            }
            int step_idx = 0;
            float step = fArr[0];
            while (step_idx < ilbc_constants.LSF_NUMBER_OF_STEPS) {
                float hlp = (float) Math.cos((double) (ilbc_constants.TWO_PI * omega));
                float hlp1 = (2.0f * hlp) + pq_coef[0];
                float hlp2 = (((2.0f * hlp) * hlp1) - 1.0f) + pq_coef[1];
                float hlp3 = (((2.0f * hlp) * hlp2) - hlp1) + pq_coef[2];
                float hlp5 = ((hlp * ((((2.0f * hlp) * hlp3) - hlp2) + pq_coef[3])) - hlp3) + pq_coef[4];
                if (olds[old] * hlp5 > 0.0f && ((double) omega) < 0.5d) {
                    olds[old] = hlp5;
                    omega += step;
                } else if (step_idx == ilbc_constants.LSF_NUMBER_OF_STEPS - 1) {
                    if (Math.abs(hlp5) >= Math.abs(olds[old])) {
                        freq[freq_idx + lsp_index] = omega - step;
                    } else {
                        freq[freq_idx + lsp_index] = omega;
                    }
                    if (olds[old] >= 0.0f) {
                        olds[old] = -1.0f * ilbc_constants.DOUBLE_MAX;
                    } else {
                        olds[old] = ilbc_constants.DOUBLE_MAX;
                    }
                    omega = old_omega;
                    step_idx = ilbc_constants.LSF_NUMBER_OF_STEPS;
                } else {
                    if (step_idx == 0) {
                        old_omega = omega;
                    }
                    step_idx++;
                    omega -= fArr[step_idx];
                    step = fArr[step_idx];
                }
            }
        }
        for (i = 0; i < ilbc_constants.LPC_FILTERORDER; i++) {
            freq[freq_idx + i] = freq[freq_idx + i] * ilbc_constants.TWO_PI;
        }
    }

    /* access modifiers changed from: 0000 */
    public void SimpleAnalysis(float[] lsf, float[] data) {
        int is;
        float[] temp = new float[ilbc_constants.BLOCKL_MAX];
        float[] lp = new float[(ilbc_constants.LPC_FILTERORDER + 1)];
        float[] lp2 = new float[(ilbc_constants.LPC_FILTERORDER + 1)];
        float[] r = new float[(ilbc_constants.LPC_FILTERORDER + 1)];
        System.arraycopy(data, 0, this.lpc_buffer, (ilbc_constants.LPC_LOOKBACK + ilbc_constants.BLOCKL_MAX) - this.ULP_inst.blockl, this.ULP_inst.blockl);
        for (int k = 0; k < this.ULP_inst.lpc_n; k++) {
            is = ilbc_constants.LPC_LOOKBACK;
            if (k < this.ULP_inst.lpc_n - 1) {
                window(temp, ilbc_constants.lpc_winTbl, this.lpc_buffer, 0, ilbc_constants.BLOCKL_MAX);
            } else {
                window(temp, ilbc_constants.lpc_asymwinTbl, this.lpc_buffer, is, ilbc_constants.BLOCKL_MAX);
            }
            autocorr(r, temp, ilbc_constants.BLOCKL_MAX, ilbc_constants.LPC_FILTERORDER);
            window(r, r, ilbc_constants.lpc_lagwinTbl, 0, ilbc_constants.LPC_FILTERORDER + 1);
            levdurb(lp, temp, r, ilbc_constants.LPC_FILTERORDER);
            ilbc_common.bwexpand(lp2, 0, lp, ilbc_constants.LPC_CHIRP_SYNTDENUM, ilbc_constants.LPC_FILTERORDER + 1);
            a2lsf(lsf, ilbc_constants.LPC_FILTERORDER * k, lp2);
        }
        is = (ilbc_constants.LPC_LOOKBACK + ilbc_constants.BLOCKL_MAX) - this.ULP_inst.blockl;
        System.arraycopy(this.lpc_buffer, (ilbc_constants.LPC_LOOKBACK + ilbc_constants.BLOCKL_MAX) - is, this.lpc_buffer, 0, is);
    }

    /* access modifiers changed from: 0000 */
    public void LSFinterpolate2a_enc(float[] a, float[] lsf1, float[] lsf2, int lsf2_idx, float coef, long length) {
        float[] lsftmp = new float[ilbc_constants.LPC_FILTERORDER];
        ilbc_common.interpolate(lsftmp, lsf1, lsf2, lsf2_idx, coef, (int) length);
        ilbc_common.lsf2a(a, lsftmp);
    }

    /* access modifiers changed from: 0000 */
    public void SimpleInterpolateLSF(float[] syntdenum, float[] weightdenum, float[] lsf, float[] lsfdeq, float[] lsfold, float[] lsfdeqold, int length) {
        float[] lp = new float[(ilbc_constants.LPC_FILTERORDER + 1)];
        int lsf2 = length;
        int lsfdeq2 = length;
        int lp_length = length + 1;
        int pos;
        int i;
        if (this.ULP_inst.mode == 30) {
            LSFinterpolate2a_enc(lp, lsfdeqold, lsfdeq, 0, ilbc_constants.lsf_weightTbl_30ms[0], (long) length);
            System.arraycopy(lp, 0, syntdenum, 0, lp_length);
            LSFinterpolate2a_enc(lp, lsfold, lsf, 0, ilbc_constants.lsf_weightTbl_30ms[0], (long) length);
            ilbc_common.bwexpand(weightdenum, 0, lp, ilbc_constants.LPC_CHIRP_WEIGHTDENUM, lp_length);
            pos = lp_length;
            for (i = 1; i < this.ULP_inst.nsub; i++) {
                LSFinterpolate2a_enc(lp, lsfdeq, lsfdeq, lsfdeq2, ilbc_constants.lsf_weightTbl_30ms[i], (long) length);
                System.arraycopy(lp, 0, syntdenum, pos, lp_length);
                LSFinterpolate2a_enc(lp, lsf, lsf, lsf2, ilbc_constants.lsf_weightTbl_30ms[i], (long) length);
                ilbc_common.bwexpand(weightdenum, pos, lp, ilbc_constants.LPC_CHIRP_WEIGHTDENUM, lp_length);
                pos += lp_length;
            }
        } else {
            pos = 0;
            for (i = 0; i < this.ULP_inst.nsub; i++) {
                LSFinterpolate2a_enc(lp, lsfdeqold, lsfdeq, 0, ilbc_constants.lsf_weightTbl_20ms[i], (long) length);
                System.arraycopy(lp, 0, syntdenum, pos, lp_length);
                for (int li = 0; li < lp_length; li++) {
                    LSFinterpolate2a_enc(lp, lsfold, lsf, 0, ilbc_constants.lsf_weightTbl_20ms[i], (long) length);
                }
                ilbc_common.bwexpand(weightdenum, pos, lp, ilbc_constants.LPC_CHIRP_WEIGHTDENUM, lp_length);
                pos += lp_length;
            }
        }
        if (this.ULP_inst.mode == 30) {
            System.arraycopy(lsf, lsf2, lsfold, 0, length);
            System.arraycopy(lsfdeq, lsfdeq2, lsfdeqold, 0, length);
            return;
        }
        System.arraycopy(lsf, 0, lsfold, 0, length);
        System.arraycopy(lsfdeq, 0, lsfdeqold, 0, length);
    }

    /* access modifiers changed from: 0000 */
    public void SimplelsfQ(float[] lsfdeq, int[] index, float[] lsf, int lpc_n) {
        SplitVQ(lsfdeq, 0, index, 0, lsf, 0, ilbc_constants.lsfCbTbl, ilbc_constants.LSF_NSPLIT, ilbc_constants.dim_lsfCbTbl, ilbc_constants.size_lsfCbTbl);
        if (lpc_n == 2) {
            SplitVQ(lsfdeq, ilbc_constants.LPC_FILTERORDER, index, ilbc_constants.LSF_NSPLIT, lsf, ilbc_constants.LPC_FILTERORDER, ilbc_constants.lsfCbTbl, ilbc_constants.LSF_NSPLIT, ilbc_constants.dim_lsfCbTbl, ilbc_constants.size_lsfCbTbl);
        }
    }

    /* access modifiers changed from: 0000 */
    public void LPCencode(float[] syntdenum, float[] weightdenum, int[] lsf_index, float[] data) {
        float[] lsf = new float[(ilbc_constants.LPC_FILTERORDER * ilbc_constants.LPC_N_MAX)];
        float[] lsfdeq = new float[(ilbc_constants.LPC_FILTERORDER * ilbc_constants.LPC_N_MAX)];
        SimpleAnalysis(lsf, data);
        SimplelsfQ(lsfdeq, lsf_index, lsf, this.ULP_inst.lpc_n);
        ilbc_common.LSF_check(lsfdeq, ilbc_constants.LPC_FILTERORDER, this.ULP_inst.lpc_n);
        SimpleInterpolateLSF(syntdenum, weightdenum, lsf, lsfdeq, this.lsfold, this.lsfdeqold, ilbc_constants.LPC_FILTERORDER);
    }

    public void iCBSearch(int[] index, int index_idx, int[] gain_index, int gain_index_idx, float[] intarget, int intarget_idx, float[] mem, int mem_idx, int lMem, int lTarget, int nStages, float[] weightDenum, int weightDenum_idx, float[] weightState, int block) {
        int i;
        int j;
        float[] gains = new float[ilbc_constants.CB_NSTAGES];
        float[] target = new float[ilbc_constants.SUBL];
        float[] buf = new float[((ilbc_constants.CB_MEML + ilbc_constants.SUBL) + (ilbc_constants.LPC_FILTERORDER * 2))];
        float[] invenergy = new float[(ilbc_constants.CB_EXPAND * 128)];
        float[] energy = new float[(ilbc_constants.CB_EXPAND * 128)];
        int ppi = 0;
        int ppo = 0;
        int ppe = 0;
        float[] cbvectors = new float[ilbc_constants.CB_MEML];
        float[] cvec = new float[ilbc_constants.SUBL];
        float[] aug_vec = new float[ilbc_constants.SUBL];
        float[] a = new float[1];
        int[] b = new int[1];
        float[] c = new float[1];
        for (int li = 0; li < ilbc_constants.SUBL; li++) {
            cvec[li] = 0.0f;
        }
        int base_size = (lMem - lTarget) + 1;
        if (lTarget == ilbc_constants.SUBL) {
            base_size = ((lMem - lTarget) + 1) + (lTarget / 2);
        }
        System.arraycopy(weightState, 0, buf, 0, ilbc_constants.LPC_FILTERORDER);
        System.arraycopy(mem, mem_idx, buf, ilbc_constants.LPC_FILTERORDER, lMem);
        System.arraycopy(intarget, intarget_idx, buf, ilbc_constants.LPC_FILTERORDER + lMem, lTarget);
        ilbc_common.AllPoleFilter(buf, ilbc_constants.LPC_FILTERORDER, weightDenum, weightDenum_idx, lMem + lTarget, ilbc_constants.LPC_FILTERORDER);
        System.arraycopy(buf, ilbc_constants.LPC_FILTERORDER + lMem, target, 0, lTarget);
        float tene = 0.0f;
        for (i = 0; i < lTarget; i++) {
            tene += target[i] * target[i];
        }
        filteredCBvecs(cbvectors, buf, ilbc_constants.LPC_FILTERORDER, lMem);
        int eIndAug = 0;
        int sIndAug = 0;
        for (int stage = 0; stage < nStages; stage++) {
            float measure;
            int icount;
            int sInd;
            int eInd;
            float[] ppt;
            int range = ilbc_constants.search_rangeTbl[block][stage];
            float max_measure = -1.0E7f;
            float gain = 0.0f;
            int best_index = 0;
            float crossDot = 0.0f;
            int pp = (ilbc_constants.LPC_FILTERORDER + lMem) - lTarget;
            for (j = 0; j < lTarget; j++) {
                crossDot += target[j] * buf[pp];
                pp++;
            }
            if (stage == 0) {
                ppi = ((ilbc_constants.LPC_FILTERORDER + lMem) - lTarget) - 1;
                ppo = (ilbc_constants.LPC_FILTERORDER + lMem) - 1;
                energy[0] = 0.0f;
                pp = (ilbc_constants.LPC_FILTERORDER + lMem) - lTarget;
                for (j = 0; j < lTarget; j++) {
                    energy[0] = energy[0] + (buf[pp] * buf[pp]);
                    pp++;
                }
                if (energy[0] > 0.0f) {
                    invenergy[0] = 1.0f / (energy[0] + ilbc_constants.EPS);
                } else {
                    invenergy[0] = 0.0f;
                }
                ppe = 0 + 1;
                measure = -1.0E7f;
                if (crossDot > 0.0f) {
                    measure = (crossDot * crossDot) * invenergy[0];
                }
            } else {
                measure = (crossDot * crossDot) * invenergy[0];
            }
            float ftmp = crossDot * invenergy[0];
            if (measure > -1.0E7f && Math.abs(ftmp) < ilbc_constants.CB_MAXGAIN) {
                best_index = 0;
                max_measure = measure;
                gain = ftmp;
            }
            for (icount = 1; icount < range; icount++) {
                crossDot = 0.0f;
                pp = ((ilbc_constants.LPC_FILTERORDER + lMem) - lTarget) - icount;
                for (j = 0; j < lTarget; j++) {
                    crossDot += target[j] * buf[pp];
                    pp++;
                }
                if (stage == 0) {
                    energy[ppe] = (energy[icount - 1] + (buf[ppi] * buf[ppi])) - (buf[ppo] * buf[ppo]);
                    ppe++;
                    ppo--;
                    ppi--;
                    if (energy[icount] > 0.0f) {
                        invenergy[icount] = 1.0f / (energy[icount] + ilbc_constants.EPS);
                    } else {
                        invenergy[icount] = 0.0f;
                    }
                    measure = -1.0E7f;
                    if (crossDot > 0.0f) {
                        measure = (crossDot * crossDot) * invenergy[icount];
                    }
                } else {
                    measure = (crossDot * crossDot) * invenergy[icount];
                }
                ftmp = crossDot * invenergy[icount];
                if (measure > max_measure && Math.abs(ftmp) < ilbc_constants.CB_MAXGAIN) {
                    best_index = icount;
                    max_measure = measure;
                    gain = ftmp;
                }
            }
            if (lTarget == ilbc_constants.SUBL) {
                a[0] = max_measure;
                b[0] = best_index;
                c[0] = gain;
                searchAugmentedCB(20, 39, stage, base_size - (lTarget / 2), target, buf, ilbc_constants.LPC_FILTERORDER + lMem, a, b, c, energy, invenergy);
                max_measure = a[0];
                best_index = b[0];
                gain = c[0];
            }
            int base_index = best_index;
            if (ilbc_constants.CB_RESRANGE == -1) {
                sInd = 0;
                eInd = range - 1;
                sIndAug = 20;
                eIndAug = 39;
            } else {
                sIndAug = 0;
                eIndAug = 0;
                sInd = base_index - (ilbc_constants.CB_RESRANGE / 2);
                eInd = sInd + ilbc_constants.CB_RESRANGE;
                if (lTarget != ilbc_constants.SUBL) {
                    if (sInd < 0) {
                        eInd -= sInd;
                        sInd = 0;
                    }
                    if (eInd > range) {
                        sInd -= eInd - range;
                        eInd = range;
                    }
                } else if (sInd < 0) {
                    sIndAug = sInd + 40;
                    eIndAug = 39;
                    sInd = 0;
                } else if (base_index < base_size - 20) {
                    if (eInd > range) {
                        sInd -= eInd - range;
                        eInd = range;
                    }
                } else if (sInd < base_size - 20) {
                    sIndAug = 20;
                    sInd = 0;
                    eInd = 0;
                    eIndAug = ilbc_constants.CB_RESRANGE + 19;
                    if (eIndAug > 39) {
                        eInd = eIndAug - 39;
                        eIndAug = 39;
                    }
                } else {
                    sIndAug = (sInd + 20) - (base_size - 20);
                    eIndAug = 39;
                    sInd = 0;
                    eInd = ilbc_constants.CB_RESRANGE - ((39 - sIndAug) + 1);
                }
            }
            int counter = sInd;
            sInd += base_size;
            eInd += base_size;
            if (stage == 0) {
                ppe = base_size;
                energy[ppe] = 0.0f;
                pp = lMem - lTarget;
                for (j = 0; j < lTarget; j++) {
                    energy[ppe] = energy[ppe] + (cbvectors[pp] * cbvectors[pp]);
                    pp++;
                }
                ppi = (lMem - 1) - lTarget;
                ppo = lMem - 1;
                for (j = 0; j < range - 1; j++) {
                    energy[ppe + 1] = (energy[ppe] + (cbvectors[ppi] * cbvectors[ppi])) - (cbvectors[ppo] * cbvectors[ppo]);
                    ppo--;
                    ppi--;
                    ppe++;
                }
            }
            icount = sInd;
            while (true) {
                int counter2 = counter;
                if (icount >= eInd) {
                    break;
                }
                crossDot = 0.0f;
                counter = counter2 + 1;
                pp = (lMem - counter2) - lTarget;
                for (j = 0; j < lTarget; j++) {
                    crossDot += target[j] * cbvectors[pp];
                    pp++;
                }
                if (energy[icount] > 0.0f) {
                    invenergy[icount] = 1.0f / (energy[icount] + ilbc_constants.EPS);
                } else {
                    invenergy[icount] = 0.0f;
                }
                if (stage == 0) {
                    measure = -1.0E7f;
                    if (crossDot > 0.0f) {
                        measure = (crossDot * crossDot) * invenergy[icount];
                    }
                } else {
                    measure = (crossDot * crossDot) * invenergy[icount];
                }
                ftmp = crossDot * invenergy[icount];
                if (measure > max_measure && Math.abs(ftmp) < ilbc_constants.CB_MAXGAIN) {
                    best_index = icount;
                    max_measure = measure;
                    gain = ftmp;
                }
                icount++;
            }
            if (lTarget == ilbc_constants.SUBL && sIndAug != 0) {
                a[0] = max_measure;
                b[0] = best_index;
                c[0] = gain;
                searchAugmentedCB(sIndAug, eIndAug, stage, (base_size * 2) - 20, target, cbvectors, lMem, a, b, c, energy, invenergy);
                max_measure = a[0];
                best_index = b[0];
                gain = c[0];
            }
            index[index_idx + stage] = best_index;
            if (stage == 0) {
                if (gain < 0.0f) {
                    gain = 0.0f;
                }
                if (gain > ilbc_constants.CB_MAXGAIN) {
                    gain = ilbc_constants.CB_MAXGAIN;
                }
                gain = ilbc_common.gainquant(gain, 1.0f, 32, gain_index, gain_index_idx + stage);
            } else if (stage == 1) {
                gain = ilbc_common.gainquant(gain, Math.abs(gains[stage - 1]), 16, gain_index, gain_index_idx + stage);
            } else {
                gain = ilbc_common.gainquant(gain, Math.abs(gains[stage - 1]), 8, gain_index, gain_index_idx + stage);
            }
            if (lTarget == ilbc_constants.STATE_LEN - this.ULP_inst.state_short_len) {
                if (index[index_idx + stage] < base_size) {
                    pp = ((ilbc_constants.LPC_FILTERORDER + lMem) - lTarget) - index[index_idx + stage];
                    ppt = buf;
                } else {
                    pp = ((lMem - lTarget) - index[index_idx + stage]) + base_size;
                    ppt = cbvectors;
                }
            } else if (index[index_idx + stage] >= base_size) {
                int filterno = index[index_idx + stage] / base_size;
                if (index[index_idx + stage] - (filterno * base_size) < base_size - 20) {
                    pp = (((filterno * lMem) - lTarget) - index[index_idx + stage]) + (filterno * base_size);
                    ppt = cbvectors;
                } else {
                    createAugmentedVec((index[index_idx + stage] - ((filterno + 1) * base_size)) + 40, cbvectors, filterno * lMem, aug_vec);
                    pp = 0;
                    ppt = aug_vec;
                }
            } else if (index[index_idx + stage] < base_size - 20) {
                pp = ((ilbc_constants.LPC_FILTERORDER + lMem) - lTarget) - index[index_idx + stage];
                ppt = buf;
            } else {
                createAugmentedVec((index[index_idx + stage] - base_size) + 40, buf, ilbc_constants.LPC_FILTERORDER + lMem, aug_vec);
                pp = 0;
                ppt = aug_vec;
            }
            for (j = 0; j < lTarget; j++) {
                cvec[j] = cvec[j] + (ppt[pp] * gain);
                target[j] = target[j] - (ppt[pp] * gain);
                pp++;
            }
            gains[stage] = gain;
        }
        float cene = 0.0f;
        for (i = 0; i < lTarget; i++) {
            cene += cvec[i] * cvec[i];
        }
        j = gain_index[gain_index_idx + 0];
        for (i = gain_index[gain_index_idx + 0]; i < 32; i++) {
            if ((ilbc_constants.gain_sq5Tbl[i] * cene) * ilbc_constants.gain_sq5Tbl[i] < (gains[0] * tene) * gains[0] && ilbc_constants.gain_sq5Tbl[j] < 2.0f * gains[0]) {
                j = i;
            }
        }
        gain_index[gain_index_idx + 0] = j;
    }

    public void index_conv_enc(int[] index) {
        int k = 1;
        while (k < ilbc_constants.CB_NSTAGES) {
            if (index[k] >= 108 && index[k] < 172) {
                index[k] = index[k] - 64;
            } else if (index[k] >= 236) {
                index[k] = index[k] - 128;
            }
            k++;
        }
    }

    public void hpInput(float[] In, int len, float[] Out, float[] mem) {
        int i;
        int pi = 0;
        int po = 0;
        for (i = 0; i < len; i++) {
            Out[po] = ilbc_constants.hpi_zero_coefsTbl[0] * In[pi];
            Out[po] = Out[po] + (ilbc_constants.hpi_zero_coefsTbl[1] * mem[0]);
            Out[po] = Out[po] + (ilbc_constants.hpi_zero_coefsTbl[2] * mem[1]);
            mem[1] = mem[0];
            mem[0] = In[pi];
            po++;
            pi++;
        }
        po = 0;
        for (i = 0; i < len; i++) {
            Out[po] = Out[po] - (ilbc_constants.hpi_pole_coefsTbl[1] * mem[2]);
            Out[po] = Out[po] - (ilbc_constants.hpi_pole_coefsTbl[2] * mem[3]);
            mem[3] = mem[2];
            mem[2] = Out[po];
            po++;
        }
    }

    public void autocorr(float[] r, float[] x, int N, int order) {
        for (int lag = 0; lag <= order; lag++) {
            float sum = 0.0f;
            for (int n = 0; n < N - lag; n++) {
                sum += x[n] * x[n + lag];
            }
            r[lag] = sum;
        }
    }

    public void window(float[] z, float[] x, float[] y, int y_idx, int N) {
        for (int i = 0; i < N; i++) {
            z[i] = x[i] * y[i + y_idx];
        }
    }

    public void levdurb(float[] a, float[] k, float[] r, int order) {
        a[0] = 1.0f;
        int i;
        if (r[0] < ilbc_constants.EPS) {
            for (i = 0; i < order; i++) {
                k[i] = 0.0f;
                a[i + 1] = 0.0f;
            }
            return;
        }
        float f = (-r[1]) / r[0];
        k[0] = f;
        a[1] = f;
        float alpha = r[0] + (r[1] * k[0]);
        for (int m = 1; m < order; m++) {
            float sum = r[m + 1];
            for (i = 0; i < m; i++) {
                sum += a[i + 1] * r[m - i];
            }
            k[m] = (-sum) / alpha;
            alpha += k[m] * sum;
            int m_h = (m + 1) >> 1;
            for (i = 0; i < m_h; i++) {
                sum = a[i + 1] + (k[m] * a[m - i]);
                int i2 = m - i;
                a[i2] = a[i2] + (k[m] * a[i + 1]);
                a[i + 1] = sum;
            }
            a[m + 1] = k[m];
        }
    }

    public void vq(float[] Xq, int Xq_idx, int[] index, int index_idx, float[] CB, int CB_idx, float[] X, int X_idx, int n_cb, int dim) {
        int i;
        int pos = 0;
        float mindist = ilbc_constants.DOUBLE_MAX;
        int minindex = 0;
        for (int j = 0; j < n_cb; j++) {
            float dist = X[X_idx] - CB[pos + CB_idx];
            dist *= dist;
            for (i = 1; i < dim; i++) {
                float tmp = X[i + X_idx] - CB[(pos + i) + CB_idx];
                dist += tmp * tmp;
            }
            if (dist < mindist) {
                mindist = dist;
                minindex = j;
            }
            pos += dim;
        }
        for (i = 0; i < dim; i++) {
            Xq[i + Xq_idx] = CB[((minindex * dim) + i) + CB_idx];
        }
        index[index_idx] = minindex;
    }

    public void SplitVQ(float[] qX, int qX_idx, int[] index, int index_idx, float[] X, int X_idx, float[] CB, int nsplit, int[] dim, int[] cbsize) {
        int cb_pos = 0;
        int X_pos = 0;
        for (int i = 0; i < nsplit; i++) {
            vq(qX, X_pos + qX_idx, index, i + index_idx, CB, cb_pos, X, X_pos + X_idx, cbsize[i], dim[i]);
            X_pos += dim[i];
            cb_pos += dim[i] * cbsize[i];
        }
    }

    public float sort_sq(int[] index, int index_idx, float x, float[] cb, int cb_size) {
        if (x <= cb[0]) {
            index[index_idx] = 0;
            return cb[0];
        }
        int i = 0;
        while (x > cb[i] && i < cb_size - 1) {
            i++;
        }
        if (x > (cb[i] + cb[i - 1]) / 2.0f) {
            index[index_idx] = i;
            return cb[i];
        }
        index[index_idx] = i - 1;
        return cb[i - 1];
    }

    /* access modifiers changed from: 0000 */
    public int FrameClassify(float[] residual) {
        int li;
        int l;
        int n;
        float[] fssqEn = new float[ilbc_constants.NSUB_MAX];
        float[] bssqEn = new float[ilbc_constants.NSUB_MAX];
        float[] ssqEn_win = new float[]{0.8f, 0.9f, 1.0f, 0.9f, 0.8f};
        float[] sampEn_win = new float[]{0.16666667f, 0.33333334f, 0.5f, 0.6666667f, 0.8333333f};
        for (li = 0; li < ilbc_constants.NSUB_MAX; li++) {
            fssqEn[li] = 0.0f;
        }
        for (li = 0; li < ilbc_constants.NSUB_MAX; li++) {
            bssqEn[li] = 0.0f;
        }
        int pp = 0;
        for (l = 0; l < 5; l++) {
            fssqEn[0] = fssqEn[0] + ((sampEn_win[l] * residual[pp]) * residual[pp]);
            pp++;
        }
        for (l = 5; l < ilbc_constants.SUBL; l++) {
            fssqEn[0] = fssqEn[0] + (residual[pp] * residual[pp]);
            pp++;
        }
        for (n = 1; n < this.ULP_inst.nsub - 1; n++) {
            pp = n * ilbc_constants.SUBL;
            for (l = 0; l < 5; l++) {
                fssqEn[n] = fssqEn[n] + ((sampEn_win[l] * residual[pp]) * residual[pp]);
                bssqEn[n] = bssqEn[n] + (residual[pp] * residual[pp]);
                pp++;
            }
            for (l = 5; l < ilbc_constants.SUBL - 5; l++) {
                fssqEn[n] = fssqEn[n] + (residual[pp] * residual[pp]);
                bssqEn[n] = bssqEn[n] + (residual[pp] * residual[pp]);
                pp++;
            }
            for (l = ilbc_constants.SUBL - 5; l < ilbc_constants.SUBL; l++) {
                fssqEn[n] = fssqEn[n] + (residual[pp] * residual[pp]);
                bssqEn[n] = bssqEn[n] + ((sampEn_win[(ilbc_constants.SUBL - l) - 1] * residual[pp]) * residual[pp]);
                pp++;
            }
        }
        n = this.ULP_inst.nsub - 1;
        pp = n * ilbc_constants.SUBL;
        for (l = 0; l < ilbc_constants.SUBL - 5; l++) {
            bssqEn[n] = bssqEn[n] + (residual[pp] * residual[pp]);
            pp++;
        }
        for (l = ilbc_constants.SUBL - 5; l < ilbc_constants.SUBL; l++) {
            bssqEn[n] = bssqEn[n] + ((sampEn_win[(ilbc_constants.SUBL - l) - 1] * residual[pp]) * residual[pp]);
            pp++;
        }
        if (this.ULP_inst.mode == 20) {
            l = 1;
        } else {
            l = 0;
        }
        float max_ssqEn = (fssqEn[0] + bssqEn[1]) * ssqEn_win[l];
        int max_ssqEn_n = 1;
        for (n = 2; n < this.ULP_inst.nsub; n++) {
            l++;
            if ((fssqEn[n - 1] + bssqEn[n]) * ssqEn_win[l] > max_ssqEn) {
                max_ssqEn = (fssqEn[n - 1] + bssqEn[n]) * ssqEn_win[l];
                max_ssqEn_n = n;
            }
        }
        return max_ssqEn_n;
    }

    private void anaFilter(float[] In, int in_idx, float[] a, int a_idx, int len, float[] Out, int out_idx, float[] mem) {
        int i;
        int pi;
        int pa;
        int j;
        int po = out_idx;
        for (i = 0; i < ilbc_constants.LPC_FILTERORDER; i++) {
            pi = in_idx + i;
            int pm = ilbc_constants.LPC_FILTERORDER - 1;
            pa = a_idx;
            Out[po] = 0.0f;
            for (j = 0; j <= i; j++) {
                Out[po] = Out[po] + (a[pa] * In[pi]);
                pa++;
                pi--;
            }
            for (j = i + 1; j < ilbc_constants.LPC_FILTERORDER + 1; j++) {
                Out[po] = Out[po] + (a[pa] * mem[pm]);
                pa++;
                pm--;
            }
            po++;
        }
        for (i = ilbc_constants.LPC_FILTERORDER; i < len; i++) {
            pi = in_idx + i;
            pa = a_idx;
            Out[po] = 0.0f;
            for (j = 0; j < ilbc_constants.LPC_FILTERORDER + 1; j++) {
                Out[po] = Out[po] + (a[pa] * In[pi]);
                pa++;
                pi--;
            }
            po++;
        }
        System.arraycopy(In, (in_idx + len) - ilbc_constants.LPC_FILTERORDER, mem, 0, ilbc_constants.LPC_FILTERORDER);
    }

    private void filteredCBvecs(float[] cbvectors, float[] mem, int mem_idx, int lMem) {
        int i;
        float[] tempbuff2 = new float[(ilbc_constants.CB_MEML + ilbc_constants.CB_FILTERLEN)];
        for (i = 0; i < ilbc_constants.CB_HALFFILTERLEN; i++) {
            tempbuff2[i] = 0.0f;
        }
        System.arraycopy(mem, mem_idx, tempbuff2, ilbc_constants.CB_HALFFILTERLEN - 1, lMem);
        for (i = (ilbc_constants.CB_HALFFILTERLEN + lMem) - 1; i < ilbc_constants.CB_FILTERLEN + lMem; i++) {
            tempbuff2[i] = 0.0f;
        }
        int pos = 0;
        for (i = 0; i < lMem; i++) {
            cbvectors[i] = 0.0f;
        }
        for (int k = 0; k < lMem; k++) {
            int pp = k;
            int pp1 = ilbc_constants.CB_FILTERLEN - 1;
            for (int j = 0; j < ilbc_constants.CB_FILTERLEN; j++) {
                cbvectors[pos] = cbvectors[pos] + (tempbuff2[pp] * ilbc_constants.cbfiltersTbl[pp1]);
                pp++;
                pp1--;
            }
            pos++;
        }
    }

    private void searchAugmentedCB(int low, int high, int stage, int startIndex, float[] target, float[] buffer, int buffer_idx, float[] max_measure, int[] best_index, float[] gain, float[] energy, float[] invenergy) {
        int j;
        float nrjRecursive = 0.0f;
        int pp = (1 - low) + buffer_idx;
        for (j = 0; j < low - 5; j++) {
            nrjRecursive += buffer[pp] * buffer[pp];
            pp++;
        }
        int ppe = buffer_idx - low;
        for (int icount = low; icount <= high; icount++) {
            float measure;
            int tmpIndex = (startIndex + icount) - 20;
            int ilow = icount - 4;
            nrjRecursive += buffer[ppe] * buffer[ppe];
            ppe--;
            energy[tmpIndex] = nrjRecursive;
            float crossDot = 0.0f;
            pp = buffer_idx - icount;
            for (j = 0; j < ilow; j++) {
                crossDot += target[j] * buffer[pp];
                pp++;
            }
            float alfa = 0.2f;
            int ppo = buffer_idx - 4;
            int ppi = (buffer_idx - icount) - 4;
            for (j = ilow; j < icount; j++) {
                float weighted = ((1.0f - alfa) * buffer[ppo]) + (buffer[ppi] * alfa);
                ppo++;
                ppi++;
                energy[tmpIndex] = energy[tmpIndex] + (weighted * weighted);
                crossDot += target[j] * weighted;
                alfa += 0.2f;
            }
            pp = buffer_idx - icount;
            for (j = icount; j < ilbc_constants.SUBL; j++) {
                energy[tmpIndex] = energy[tmpIndex] + (buffer[pp] * buffer[pp]);
                crossDot += target[j] * buffer[pp];
                pp++;
            }
            if (energy[tmpIndex] > 0.0f) {
                invenergy[tmpIndex] = 1.0f / (energy[tmpIndex] + ilbc_constants.EPS);
            } else {
                invenergy[tmpIndex] = 0.0f;
            }
            if (stage == 0) {
                measure = -1.0E7f;
                if (crossDot > 0.0f) {
                    measure = (crossDot * crossDot) * invenergy[tmpIndex];
                }
            } else {
                measure = (crossDot * crossDot) * invenergy[tmpIndex];
            }
            float ftmp = crossDot * invenergy[tmpIndex];
            if (measure > max_measure[0] && Math.abs(ftmp) < ilbc_constants.CB_MAXGAIN) {
                best_index[0] = tmpIndex;
                max_measure[0] = measure;
                gain[0] = ftmp;
            }
        }
    }

    private void createAugmentedVec(int index, float[] buffer, int buffer_idx, float[] cbVec) {
        int ilow = index - 5;
        System.arraycopy(buffer, buffer_idx - index, cbVec, 0, index);
        float alfa = 0.0f;
        int ppo = buffer_idx - 5;
        int ppi = (buffer_idx - index) - 5;
        for (int j = ilow; j < index; j++) {
            ppo++;
            ppi++;
            cbVec[j] = ((1.0f - alfa) * buffer[ppo]) + (buffer[ppi] * alfa);
            alfa += 0.2f;
        }
        System.arraycopy(buffer, buffer_idx - index, cbVec, index, ilbc_constants.SUBL - index);
    }

    public ilbc_encoder(int init_mode) throws Error {
        this.mode = init_mode;
        if (this.mode == 30 || this.mode == 20) {
            int li;
            this.ULP_inst = new ilbc_ulp(this.mode);
            this.anaMem = new float[ilbc_constants.LPC_FILTERORDER];
            this.lsfold = new float[ilbc_constants.LPC_FILTERORDER];
            this.lsfdeqold = new float[ilbc_constants.LPC_FILTERORDER];
            this.lpc_buffer = new float[(ilbc_constants.LPC_LOOKBACK + ilbc_constants.BLOCKL_MAX)];
            this.hpimem = new float[4];
            for (li = 0; li < this.anaMem.length; li++) {
                this.anaMem[li] = 0.0f;
            }
            System.arraycopy(ilbc_constants.lsfmeanTbl, 0, this.lsfdeqold, 0, ilbc_constants.LPC_FILTERORDER);
            System.arraycopy(ilbc_constants.lsfmeanTbl, 0, this.lsfold, 0, ilbc_constants.LPC_FILTERORDER);
            for (li = 0; li < this.lpc_buffer.length; li++) {
                this.lpc_buffer[li] = 0.0f;
            }
            for (li = 0; li < this.hpimem.length; li++) {
                this.hpimem[li] = 0.0f;
            }
            return;
        }
        throw new Error("invalid mode");
    }

    public int encode(byte[] encoded, int encodedOffset, byte[] decoded, int decodedOffset) {
        float[] block = new float[this.ULP_inst.blockl];
        bitstream en_data = new bitstream(encoded, encodedOffset, this.ULP_inst.no_of_bytes);
        int k = 0;
        while (k < this.ULP_inst.blockl) {
            block[k] = (float) ArrayIOUtils.readShort(decoded, decodedOffset);
            k++;
            decodedOffset += 2;
        }
        iLBC_encode(en_data, block);
        return this.ULP_inst.no_of_bytes;
    }

    public void iLBC_encode(bitstream bytes, float[] block) {
        int n;
        int i;
        int state_first;
        int start_pos;
        int li;
        int k;
        int meml_gotten;
        int subframe;
        int[] idxForMax = new int[1];
        float[] data = new float[ilbc_constants.BLOCKL_MAX];
        float[] residual = new float[ilbc_constants.BLOCKL_MAX];
        float[] reverseResidual = new float[ilbc_constants.BLOCKL_MAX];
        int[] idxVec = new int[ilbc_constants.STATE_LEN];
        Object reverseDecresidual = new float[ilbc_constants.BLOCKL_MAX];
        Object mem = new float[ilbc_constants.CB_MEML];
        int[] gain_index = new int[(ilbc_constants.CB_NSTAGES * ilbc_constants.NASUB_MAX)];
        int[] extra_gain_index = new int[ilbc_constants.CB_NSTAGES];
        int[] cb_index = new int[(ilbc_constants.CB_NSTAGES * ilbc_constants.NASUB_MAX)];
        int[] extra_cb_index = new int[ilbc_constants.CB_NSTAGES];
        int[] lsf_i = new int[(ilbc_constants.LSF_NSPLIT * ilbc_constants.LPC_N_MAX)];
        float[] weightState = new float[ilbc_constants.LPC_FILTERORDER];
        float[] syntdenum = new float[(ilbc_constants.NSUB_MAX * (ilbc_constants.LPC_FILTERORDER + 1))];
        float[] weightdenum = new float[(ilbc_constants.NSUB_MAX * (ilbc_constants.LPC_FILTERORDER + 1))];
        Object decresidual = new float[ilbc_constants.BLOCKL_MAX];
        hpInput(block, this.ULP_inst.blockl, data, this.hpimem);
        LPCencode(syntdenum, weightdenum, lsf_i, data);
        for (n = 0; n < this.ULP_inst.nsub; n++) {
            anaFilter(data, n * ilbc_constants.SUBL, syntdenum, n * (ilbc_constants.LPC_FILTERORDER + 1), ilbc_constants.SUBL, residual, n * ilbc_constants.SUBL, this.anaMem);
        }
        int start = FrameClassify(residual);
        int diff = ilbc_constants.STATE_LEN - this.ULP_inst.state_short_len;
        float en1 = 0.0f;
        int index = (start - 1) * ilbc_constants.SUBL;
        for (i = 0; i < this.ULP_inst.state_short_len; i++) {
            en1 += residual[index + i] * residual[index + i];
        }
        float en2 = 0.0f;
        index = ((start - 1) * ilbc_constants.SUBL) + diff;
        for (i = 0; i < this.ULP_inst.state_short_len; i++) {
            en2 += residual[index + i] * residual[index + i];
        }
        if (en1 > en2) {
            state_first = 1;
            start_pos = (start - 1) * ilbc_constants.SUBL;
        } else {
            state_first = 0;
            start_pos = ((start - 1) * ilbc_constants.SUBL) + diff;
        }
        StateSearchW(residual, start_pos, syntdenum, (start - 1) * (ilbc_constants.LPC_FILTERORDER + 1), weightdenum, (start - 1) * (ilbc_constants.LPC_FILTERORDER + 1), idxForMax, idxVec, this.ULP_inst.state_short_len, state_first);
        ilbc_common.StateConstructW(idxForMax[0], idxVec, syntdenum, (start - 1) * (ilbc_constants.LPC_FILTERORDER + 1), decresidual, start_pos, this.ULP_inst.state_short_len);
        if (state_first != 0) {
            for (li = 0; li < ilbc_constants.CB_MEML - this.ULP_inst.state_short_len; li++) {
                mem[li] = 0.0f;
            }
            System.arraycopy(decresidual, start_pos, mem, ilbc_constants.CB_MEML - this.ULP_inst.state_short_len, this.ULP_inst.state_short_len);
            for (li = 0; li < ilbc_constants.LPC_FILTERORDER; li++) {
                weightState[li] = 0.0f;
            }
            iCBSearch(extra_cb_index, 0, extra_gain_index, 0, residual, start_pos + this.ULP_inst.state_short_len, mem, ilbc_constants.CB_MEML - ilbc_constants.stMemLTbl, ilbc_constants.stMemLTbl, diff, ilbc_constants.CB_NSTAGES, weightdenum, start * (ilbc_constants.LPC_FILTERORDER + 1), weightState, 0);
            ilbc_common.iCBConstruct(decresidual, start_pos + this.ULP_inst.state_short_len, extra_cb_index, 0, extra_gain_index, 0, mem, ilbc_constants.CB_MEML - ilbc_constants.stMemLTbl, ilbc_constants.stMemLTbl, diff, ilbc_constants.CB_NSTAGES);
        } else {
            for (k = 0; k < diff; k++) {
                reverseResidual[k] = residual[(((start + 1) * ilbc_constants.SUBL) - 1) - (this.ULP_inst.state_short_len + k)];
            }
            meml_gotten = this.ULP_inst.state_short_len;
            k = 0;
            while (k < meml_gotten) {
                mem[(ilbc_constants.CB_MEML - 1) - k] = decresidual[start_pos + k];
                k++;
            }
            for (li = 0; li < ilbc_constants.CB_MEML - k; li++) {
                mem[li] = 0.0f;
            }
            for (li = 0; li < ilbc_constants.LPC_FILTERORDER; li++) {
                weightState[li] = 0.0f;
            }
            iCBSearch(extra_cb_index, 0, extra_gain_index, 0, reverseResidual, 0, mem, ilbc_constants.CB_MEML - ilbc_constants.stMemLTbl, ilbc_constants.stMemLTbl, diff, ilbc_constants.CB_NSTAGES, weightdenum, (start - 1) * (ilbc_constants.LPC_FILTERORDER + 1), weightState, 0);
            ilbc_common.iCBConstruct(reverseDecresidual, 0, extra_cb_index, 0, extra_gain_index, 0, mem, ilbc_constants.CB_MEML - ilbc_constants.stMemLTbl, ilbc_constants.stMemLTbl, diff, ilbc_constants.CB_NSTAGES);
            for (k = 0; k < diff; k++) {
                decresidual[(start_pos - 1) - k] = reverseDecresidual[k];
            }
        }
        int subcount = 0;
        int Nfor = (this.ULP_inst.nsub - start) - 1;
        if (Nfor > 0) {
            for (li = 0; li < ilbc_constants.CB_MEML - ilbc_constants.STATE_LEN; li++) {
                mem[li] = 0.0f;
            }
            System.arraycopy(decresidual, (start - 1) * ilbc_constants.SUBL, mem, ilbc_constants.CB_MEML - ilbc_constants.STATE_LEN, ilbc_constants.STATE_LEN);
            for (li = 0; li < ilbc_constants.LPC_FILTERORDER; li++) {
                weightState[li] = 0.0f;
            }
            for (subframe = 0; subframe < Nfor; subframe++) {
                iCBSearch(cb_index, subcount * ilbc_constants.CB_NSTAGES, gain_index, subcount * ilbc_constants.CB_NSTAGES, residual, ((start + 1) + subframe) * ilbc_constants.SUBL, mem, ilbc_constants.CB_MEML - ilbc_constants.memLfTbl[subcount], ilbc_constants.memLfTbl[subcount], ilbc_constants.SUBL, ilbc_constants.CB_NSTAGES, weightdenum, ((start + 1) + subframe) * (ilbc_constants.LPC_FILTERORDER + 1), weightState, subcount + 1);
                ilbc_common.iCBConstruct(decresidual, ((start + 1) + subframe) * ilbc_constants.SUBL, cb_index, subcount * ilbc_constants.CB_NSTAGES, gain_index, subcount * ilbc_constants.CB_NSTAGES, mem, ilbc_constants.CB_MEML - ilbc_constants.memLfTbl[subcount], ilbc_constants.memLfTbl[subcount], ilbc_constants.SUBL, ilbc_constants.CB_NSTAGES);
                System.arraycopy(mem, ilbc_constants.SUBL, mem, 0, ilbc_constants.CB_MEML - ilbc_constants.SUBL);
                System.arraycopy(decresidual, ((start + 1) + subframe) * ilbc_constants.SUBL, mem, ilbc_constants.CB_MEML - ilbc_constants.SUBL, ilbc_constants.SUBL);
                for (li = 0; li < ilbc_constants.LPC_FILTERORDER; li++) {
                    weightState[li] = 0.0f;
                }
                subcount++;
            }
        }
        int Nback = start - 1;
        if (Nback > 0) {
            for (n = 0; n < Nback; n++) {
                for (k = 0; k < ilbc_constants.SUBL; k++) {
                    reverseResidual[(ilbc_constants.SUBL * n) + k] = residual[((((start - 1) * ilbc_constants.SUBL) - 1) - (ilbc_constants.SUBL * n)) - k];
                    reverseDecresidual[(ilbc_constants.SUBL * n) + k] = decresidual[((((start - 1) * ilbc_constants.SUBL) - 1) - (ilbc_constants.SUBL * n)) - k];
                }
            }
            meml_gotten = ilbc_constants.SUBL * ((this.ULP_inst.nsub + 1) - start);
            if (meml_gotten > ilbc_constants.CB_MEML) {
                meml_gotten = ilbc_constants.CB_MEML;
            }
            k = 0;
            while (k < meml_gotten) {
                mem[(ilbc_constants.CB_MEML - 1) - k] = decresidual[((start - 1) * ilbc_constants.SUBL) + k];
                k++;
            }
            for (li = 0; li < ilbc_constants.CB_MEML - k; li++) {
                mem[li] = 0.0f;
            }
            for (li = 0; li < ilbc_constants.LPC_FILTERORDER; li++) {
                weightState[li] = 0.0f;
            }
            for (subframe = 0; subframe < Nback; subframe++) {
                iCBSearch(cb_index, subcount * ilbc_constants.CB_NSTAGES, gain_index, subcount * ilbc_constants.CB_NSTAGES, reverseResidual, subframe * ilbc_constants.SUBL, mem, ilbc_constants.CB_MEML - ilbc_constants.memLfTbl[subcount], ilbc_constants.memLfTbl[subcount], ilbc_constants.SUBL, ilbc_constants.CB_NSTAGES, weightdenum, ((start - 2) - subframe) * (ilbc_constants.LPC_FILTERORDER + 1), weightState, subcount + 1);
                ilbc_common.iCBConstruct(reverseDecresidual, subframe * ilbc_constants.SUBL, cb_index, subcount * ilbc_constants.CB_NSTAGES, gain_index, subcount * ilbc_constants.CB_NSTAGES, mem, ilbc_constants.CB_MEML - ilbc_constants.memLfTbl[subcount], ilbc_constants.memLfTbl[subcount], ilbc_constants.SUBL, ilbc_constants.CB_NSTAGES);
                System.arraycopy(mem, ilbc_constants.SUBL, mem, 0, ilbc_constants.CB_MEML - ilbc_constants.SUBL);
                System.arraycopy(reverseDecresidual, ilbc_constants.SUBL * subframe, mem, ilbc_constants.CB_MEML - ilbc_constants.SUBL, ilbc_constants.SUBL);
                for (li = 0; li < ilbc_constants.LPC_FILTERORDER; li++) {
                    weightState[li] = 0.0f;
                }
                subcount++;
            }
            for (i = 0; i < ilbc_constants.SUBL * Nback; i++) {
                decresidual[((ilbc_constants.SUBL * Nback) - i) - 1] = reverseDecresidual[i];
            }
        }
        index_conv_enc(cb_index);
        for (int ulp = 0; ulp < 3; ulp++) {
            bitpack pack;
            int firstpart;
            for (k = 0; k < ilbc_constants.LSF_NSPLIT * this.ULP_inst.lpc_n; k++) {
                pack = bytes.packsplit(lsf_i[k], this.ULP_inst.lsf_bits[k][ulp], (this.ULP_inst.lsf_bits[k][ulp] + this.ULP_inst.lsf_bits[k][ulp + 1]) + this.ULP_inst.lsf_bits[k][ulp + 2]);
                firstpart = pack.get_firstpart();
                lsf_i[k] = pack.get_rest();
                bytes.dopack(firstpart, this.ULP_inst.lsf_bits[k][ulp]);
            }
            pack = bytes.packsplit(start, this.ULP_inst.start_bits[ulp], (this.ULP_inst.start_bits[ulp] + this.ULP_inst.start_bits[ulp + 1]) + this.ULP_inst.start_bits[ulp + 2]);
            firstpart = pack.get_firstpart();
            start = pack.get_rest();
            bytes.dopack(firstpart, this.ULP_inst.start_bits[ulp]);
            pack = bytes.packsplit(state_first, this.ULP_inst.startfirst_bits[ulp], (this.ULP_inst.startfirst_bits[ulp] + this.ULP_inst.startfirst_bits[ulp + 1]) + this.ULP_inst.startfirst_bits[ulp + 2]);
            firstpart = pack.get_firstpart();
            state_first = pack.get_rest();
            bytes.dopack(firstpart, this.ULP_inst.startfirst_bits[ulp]);
            pack = bytes.packsplit(idxForMax[0], this.ULP_inst.scale_bits[ulp], (this.ULP_inst.scale_bits[ulp] + this.ULP_inst.scale_bits[ulp + 1]) + this.ULP_inst.scale_bits[ulp + 2]);
            firstpart = pack.get_firstpart();
            idxForMax[0] = pack.get_rest();
            bytes.dopack(firstpart, this.ULP_inst.scale_bits[ulp]);
            for (k = 0; k < this.ULP_inst.state_short_len; k++) {
                pack = bytes.packsplit(idxVec[k], this.ULP_inst.state_bits[ulp], (this.ULP_inst.state_bits[ulp] + this.ULP_inst.state_bits[ulp + 1]) + this.ULP_inst.state_bits[ulp + 2]);
                firstpart = pack.get_firstpart();
                idxVec[k] = pack.get_rest();
                bytes.dopack(firstpart, this.ULP_inst.state_bits[ulp]);
            }
            for (k = 0; k < ilbc_constants.CB_NSTAGES; k++) {
                pack = bytes.packsplit(extra_cb_index[k], this.ULP_inst.extra_cb_index[k][ulp], (this.ULP_inst.extra_cb_index[k][ulp] + this.ULP_inst.extra_cb_index[k][ulp + 1]) + this.ULP_inst.extra_cb_index[k][ulp + 2]);
                firstpart = pack.get_firstpart();
                extra_cb_index[k] = pack.get_rest();
                bytes.dopack(firstpart, this.ULP_inst.extra_cb_index[k][ulp]);
            }
            for (k = 0; k < ilbc_constants.CB_NSTAGES; k++) {
                pack = bytes.packsplit(extra_gain_index[k], this.ULP_inst.extra_cb_gain[k][ulp], (this.ULP_inst.extra_cb_gain[k][ulp] + this.ULP_inst.extra_cb_gain[k][ulp + 1]) + this.ULP_inst.extra_cb_gain[k][ulp + 2]);
                firstpart = pack.get_firstpart();
                extra_gain_index[k] = pack.get_rest();
                bytes.dopack(firstpart, this.ULP_inst.extra_cb_gain[k][ulp]);
            }
            for (i = 0; i < this.ULP_inst.nasub; i++) {
                for (k = 0; k < ilbc_constants.CB_NSTAGES; k++) {
                    pack = bytes.packsplit(cb_index[(ilbc_constants.CB_NSTAGES * i) + k], this.ULP_inst.cb_index[i][k][ulp], (this.ULP_inst.cb_index[i][k][ulp] + this.ULP_inst.cb_index[i][k][ulp + 1]) + this.ULP_inst.cb_index[i][k][ulp + 2]);
                    firstpart = pack.get_firstpart();
                    cb_index[(ilbc_constants.CB_NSTAGES * i) + k] = pack.get_rest();
                    bytes.dopack(firstpart, this.ULP_inst.cb_index[i][k][ulp]);
                }
            }
            for (i = 0; i < this.ULP_inst.nasub; i++) {
                for (k = 0; k < ilbc_constants.CB_NSTAGES; k++) {
                    pack = bytes.packsplit(gain_index[(ilbc_constants.CB_NSTAGES * i) + k], this.ULP_inst.cb_gain[i][k][ulp], (this.ULP_inst.cb_gain[i][k][ulp] + this.ULP_inst.cb_gain[i][k][ulp + 1]) + this.ULP_inst.cb_gain[i][k][ulp + 2]);
                    firstpart = pack.get_firstpart();
                    gain_index[(ilbc_constants.CB_NSTAGES * i) + k] = pack.get_rest();
                    bytes.dopack(firstpart, this.ULP_inst.cb_gain[i][k][ulp]);
                }
            }
        }
        bytes.dopack(0, 1);
    }
}
