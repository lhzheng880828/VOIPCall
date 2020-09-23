package org.jitsi.impl.neomedia.codec.audio.ilbc;

import com.sun.media.format.WavAudioFormat;
import org.jitsi.impl.neomedia.ArrayIOUtils;
import org.jitsi.impl.neomedia.device.DeviceConfiguration;
import org.jitsi.impl.neomedia.portaudio.Pa;

class ilbc_decoder {
    ilbc_ulp ULP_inst = null;
    int consPLICount;
    float[] enh_buf;
    float[] enh_period;
    float[] hpomem;
    int last_lag;
    float[] lsfdeqold;
    float[] old_syntdenum;
    float per;
    int prevLag;
    float[] prevLpc;
    int prevPLI;
    float[] prevResidual;
    int prev_enh_pl;
    long seed;
    float[] syntMem;
    int use_enhancer;

    /* access modifiers changed from: 0000 */
    public void syntFilter(float[] Out, int Out_idx, float[] a, int a_idx, int len, float[] mem) {
        int i;
        int pi;
        int pa;
        int j;
        int po = Out_idx;
        for (i = 0; i < ilbc_constants.LPC_FILTERORDER; i++) {
            pi = (Out_idx + i) - 1;
            pa = a_idx + 1;
            int pm = ilbc_constants.LPC_FILTERORDER - 1;
            for (j = 1; j <= i; j++) {
                Out[po] = Out[po] - (a[pa] * Out[pi]);
                pa++;
                pi--;
            }
            for (j = i + 1; j < ilbc_constants.LPC_FILTERORDER + 1; j++) {
                Out[po] = Out[po] - (a[pa] * mem[pm]);
                pa++;
                pm--;
            }
            po++;
        }
        for (i = ilbc_constants.LPC_FILTERORDER; i < len; i++) {
            pi = (Out_idx + i) - 1;
            pa = a_idx + 1;
            for (j = 1; j < ilbc_constants.LPC_FILTERORDER + 1; j++) {
                Out[po] = Out[po] - (a[pa] * Out[pi]);
                pa++;
                pi--;
            }
            po++;
        }
        System.arraycopy(Out, (Out_idx + len) - ilbc_constants.LPC_FILTERORDER, mem, 0, ilbc_constants.LPC_FILTERORDER);
    }

    public void LSFinterpolate2a_dec(float[] a, float[] lsf1, float[] lsf2, int lsf2_idx, float coef, int length) {
        float[] lsftmp = new float[ilbc_constants.LPC_FILTERORDER];
        ilbc_common.interpolate(lsftmp, lsf1, lsf2, lsf2_idx, coef, length);
        ilbc_common.lsf2a(a, lsftmp);
    }

    /* access modifiers changed from: 0000 */
    public void SimplelsfDEQ(float[] lsfdeq, int[] index, int lpc_n) {
        int i;
        int j;
        int pos = 0;
        int cb_pos = 0;
        for (i = 0; i < ilbc_constants.LSF_NSPLIT; i++) {
            for (j = 0; j < ilbc_constants.dim_lsfCbTbl[i]; j++) {
                lsfdeq[pos + j] = ilbc_constants.lsfCbTbl[((int) ((((long) index[i]) * ((long) ilbc_constants.dim_lsfCbTbl[i])) + ((long) j))) + cb_pos];
            }
            pos += ilbc_constants.dim_lsfCbTbl[i];
            cb_pos += ilbc_constants.size_lsfCbTbl[i] * ilbc_constants.dim_lsfCbTbl[i];
        }
        if (lpc_n > 1) {
            pos = 0;
            cb_pos = 0;
            for (i = 0; i < ilbc_constants.LSF_NSPLIT; i++) {
                for (j = 0; j < ilbc_constants.dim_lsfCbTbl[i]; j++) {
                    lsfdeq[(ilbc_constants.LPC_FILTERORDER + pos) + j] = ilbc_constants.lsfCbTbl[(((int) (((long) index[ilbc_constants.LSF_NSPLIT + i]) * ((long) ilbc_constants.dim_lsfCbTbl[i]))) + cb_pos) + j];
                }
                pos += ilbc_constants.dim_lsfCbTbl[i];
                cb_pos += ilbc_constants.size_lsfCbTbl[i] * ilbc_constants.dim_lsfCbTbl[i];
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void DecoderInterpolateLSF(float[] syntdenum, float[] weightdenum, float[] lsfdeq, int length) {
        float[] lp = new float[(ilbc_constants.LPC_FILTERORDER + 1)];
        int lsfdeq2 = length;
        int lp_length = length + 1;
        int pos;
        int i;
        if (this.ULP_inst.mode == 30) {
            LSFinterpolate2a_dec(lp, this.lsfdeqold, lsfdeq, 0, ilbc_constants.lsf_weightTbl_30ms[0], length);
            System.arraycopy(lp, 0, syntdenum, 0, lp_length);
            ilbc_common.bwexpand(weightdenum, 0, lp, ilbc_constants.LPC_CHIRP_WEIGHTDENUM, lp_length);
            pos = lp_length;
            for (i = 1; i < 6; i++) {
                LSFinterpolate2a_dec(lp, lsfdeq, lsfdeq, lsfdeq2, ilbc_constants.lsf_weightTbl_30ms[i], length);
                System.arraycopy(lp, 0, syntdenum, pos, lp_length);
                ilbc_common.bwexpand(weightdenum, pos, lp, ilbc_constants.LPC_CHIRP_WEIGHTDENUM, lp_length);
                pos += lp_length;
            }
        } else {
            pos = 0;
            for (i = 0; i < this.ULP_inst.nsub; i++) {
                LSFinterpolate2a_dec(lp, this.lsfdeqold, lsfdeq, 0, ilbc_constants.lsf_weightTbl_20ms[i], length);
                System.arraycopy(lp, 0, syntdenum, pos, lp_length);
                ilbc_common.bwexpand(weightdenum, pos, lp, ilbc_constants.LPC_CHIRP_WEIGHTDENUM, lp_length);
                pos += lp_length;
            }
        }
        if (this.ULP_inst.mode == 30) {
            System.arraycopy(lsfdeq, lsfdeq2, this.lsfdeqold, 0, length);
        } else {
            System.arraycopy(lsfdeq, 0, this.lsfdeqold, 0, length);
        }
    }

    public void index_conv_dec(int[] index) {
        int k = 1;
        while (k < ilbc_constants.CB_NSTAGES) {
            if (index[k] >= 44 && index[k] < 108) {
                index[k] = index[k] + 64;
            } else if (index[k] >= 108 && index[k] < 128) {
                index[k] = index[k] + 128;
            }
            k++;
        }
    }

    public void hpOutput(float[] In, int len, float[] Out, float[] mem) {
        int i;
        int pi = 0;
        int po = 0;
        for (i = 0; i < len; i++) {
            Out[po] = ilbc_constants.hpo_zero_coefsTbl[0] * In[pi];
            Out[po] = Out[po] + (ilbc_constants.hpo_zero_coefsTbl[1] * mem[0]);
            Out[po] = Out[po] + (ilbc_constants.hpo_zero_coefsTbl[2] * mem[1]);
            mem[1] = mem[0];
            mem[0] = In[pi];
            po++;
            pi++;
        }
        po = 0;
        for (i = 0; i < len; i++) {
            Out[po] = Out[po] - (ilbc_constants.hpo_pole_coefsTbl[1] * mem[2]);
            Out[po] = Out[po] - (ilbc_constants.hpo_pole_coefsTbl[2] * mem[3]);
            mem[3] = mem[2];
            mem[2] = Out[po];
            po++;
        }
    }

    /* access modifiers changed from: 0000 */
    public void DownSample(float[] In, int in_idx, float[] Coef, int lengthIn, float[] state, float[] Out) {
        int i;
        int coef_ptr;
        float o;
        int j;
        int out_ptr = 0;
        int in_ptr = in_idx;
        for (i = ilbc_constants.DELAY_DS; i < lengthIn; i += ilbc_constants.FACTOR_DS) {
            int stop;
            coef_ptr = 0;
            in_ptr = in_idx + i;
            int state_ptr = ilbc_constants.FILTERORDER_DS - 2;
            o = 0.0f;
            if (i < ilbc_constants.FILTERORDER_DS) {
                stop = i + 1;
            } else {
                stop = ilbc_constants.FILTERORDER_DS;
            }
            for (j = 0; j < stop; j++) {
                o += Coef[coef_ptr] * In[in_ptr];
                coef_ptr++;
                in_ptr--;
            }
            for (j = i + 1; j < ilbc_constants.FILTERORDER_DS; j++) {
                o += Coef[coef_ptr] * state[state_ptr];
                coef_ptr++;
                state_ptr--;
            }
            Out[out_ptr] = o;
            out_ptr++;
        }
        for (i = lengthIn + ilbc_constants.FACTOR_DS; i < ilbc_constants.DELAY_DS + lengthIn; i += ilbc_constants.FACTOR_DS) {
            o = 0.0f;
            if (i < lengthIn) {
                coef_ptr = 0;
                in_ptr = in_idx + i;
                for (j = 0; j < ilbc_constants.FILTERORDER_DS; j++) {
                    o += Coef[coef_ptr] * Out[out_ptr];
                    coef_ptr++;
                    out_ptr--;
                }
            } else {
                coef_ptr = i - lengthIn;
                in_ptr = (in_idx + lengthIn) - 1;
                for (j = 0; j < ilbc_constants.FILTERORDER_DS - (i - lengthIn); j++) {
                    o += Coef[coef_ptr] * In[in_ptr];
                    coef_ptr++;
                    in_ptr--;
                }
            }
            Out[out_ptr] = o;
            out_ptr++;
        }
    }

    public int NearestNeighbor(float[] array, float value, int arlength) {
        float crit = array[0] - value;
        float bestcrit = crit * crit;
        int index = 0;
        for (int i = 1; i < arlength; i++) {
            crit = array[i] - value;
            crit *= crit;
            if (crit < bestcrit) {
                bestcrit = crit;
                index = i;
            }
        }
        return index;
    }

    public void mycorr1(float[] corr, int corr_idx, float[] seq1, int seq1_idx, int dim1, float[] seq2, int seq2_idx, int dim2) {
        for (int i = 0; i <= dim1 - dim2; i++) {
            if (corr_idx + i < corr.length) {
                corr[corr_idx + i] = 0.0f;
            }
            for (int j = 0; j < dim2; j++) {
                int i2 = corr_idx + i;
                corr[i2] = corr[i2] + (seq1[(seq1_idx + i) + j] * seq2[seq2_idx + j]);
            }
        }
    }

    public void enh_upsample(float[] useq1, float[] seq1, int dim1, int hfl) {
        int j;
        int i;
        int pp;
        int ps;
        int k;
        int[] polyp = new int[ilbc_constants.ENH_UPS0];
        int filterlength = (hfl * 2) + 1;
        if (filterlength > dim1) {
            int hfl2 = dim1 / 2;
            for (j = 0; j < ilbc_constants.ENH_UPS0; j++) {
                polyp[j] = ((j * filterlength) + hfl) - hfl2;
            }
            hfl = hfl2;
            filterlength = (hfl * 2) + 1;
        } else {
            for (j = 0; j < ilbc_constants.ENH_UPS0; j++) {
                polyp[j] = j * filterlength;
            }
        }
        int pu = 0;
        for (i = hfl; i < filterlength; i++) {
            for (j = 0; j < ilbc_constants.ENH_UPS0; j++) {
                useq1[pu] = 0.0f;
                pp = polyp[j];
                ps = i;
                for (k = 0; k <= i; k++) {
                    useq1[pu] = useq1[pu] + (seq1[ps] * ilbc_constants.polyphaserTbl[pp]);
                    ps--;
                    pp++;
                }
                pu++;
            }
        }
        for (i = filterlength; i < dim1; i++) {
            for (j = 0; j < ilbc_constants.ENH_UPS0; j++) {
                useq1[pu] = 0.0f;
                pp = polyp[j];
                ps = i;
                for (k = 0; k < filterlength; k++) {
                    useq1[pu] = useq1[pu] + (seq1[ps] * ilbc_constants.polyphaserTbl[pp]);
                    ps--;
                    pp++;
                }
                pu++;
            }
        }
        for (int q = 1; q <= hfl; q++) {
            for (j = 0; j < ilbc_constants.ENH_UPS0; j++) {
                useq1[pu] = 0.0f;
                pp = polyp[j] + q;
                ps = dim1 - 1;
                for (k = 0; k < filterlength - q; k++) {
                    useq1[pu] = useq1[pu] + (seq1[ps] * ilbc_constants.polyphaserTbl[pp]);
                    ps--;
                    pp++;
                }
                pu++;
            }
        }
    }

    public float refiner(float[] seg, int seg_idx, float[] idata, int idatal, int centerStartPos, float estSegPos, float period) {
        Object vect = new float[ilbc_constants.ENH_VECTL];
        float[] corrVec = new float[ilbc_constants.ENH_CORRDIM];
        float[] corrVecUps = new float[(ilbc_constants.ENH_CORRDIM * ilbc_constants.ENH_UPS0)];
        int estSegPosRounded = (int) (((double) estSegPos) - 0.5d);
        int searchSegStartPos = estSegPosRounded - ilbc_constants.ENH_SLOP;
        if (searchSegStartPos < 0) {
            searchSegStartPos = 0;
        }
        int searchSegEndPos = estSegPosRounded + ilbc_constants.ENH_SLOP;
        if (ilbc_constants.ENH_BLOCKL + searchSegEndPos >= idatal) {
            searchSegEndPos = (idatal - ilbc_constants.ENH_BLOCKL) - 1;
        }
        int corrdim = (searchSegEndPos - searchSegStartPos) + 1;
        mycorr1(corrVec, 0, idata, searchSegStartPos, (ilbc_constants.ENH_BLOCKL + corrdim) - 1, idata, centerStartPos, ilbc_constants.ENH_BLOCKL);
        enh_upsample(corrVecUps, corrVec, corrdim, ilbc_constants.ENH_FL0);
        int tloc = 0;
        float maxv = corrVecUps[0];
        for (int i = 1; i < ilbc_constants.ENH_UPS0 * corrdim; i++) {
            if (corrVecUps[i] > maxv) {
                tloc = i;
                maxv = corrVecUps[i];
            }
        }
        float updStartPos = (((float) searchSegStartPos) + (((float) tloc) / ((float) ilbc_constants.ENH_UPS0))) + 1.0f;
        int tloc2 = tloc / ilbc_constants.ENH_UPS0;
        if (tloc > ilbc_constants.ENH_UPS0 * tloc2) {
            tloc2++;
        }
        int st = (searchSegStartPos + tloc2) - ilbc_constants.ENH_FL0;
        int li;
        if (st < 0) {
            for (li = 0; li < (-st); li++) {
                vect[li] = 0.0f;
            }
            System.arraycopy(idata, 0, vect, -st, ilbc_constants.ENH_VECTL + st);
        } else {
            int en = st + ilbc_constants.ENH_VECTL;
            if (en > idatal) {
                System.arraycopy(idata, st, vect, 0, ilbc_constants.ENH_VECTL - (en - idatal));
                for (li = 0; li < en - idatal; li++) {
                    vect[(ilbc_constants.ENH_VECTL - (en - idatal)) + li] = 0.0f;
                }
            } else {
                System.arraycopy(idata, st, vect, 0, ilbc_constants.ENH_VECTL);
            }
        }
        float[] fArr = seg;
        int i2 = seg_idx;
        Object obj = vect;
        mycorr1(fArr, i2, obj, 0, ilbc_constants.ENH_VECTL, ilbc_constants.polyphaserTbl, ((ilbc_constants.ENH_FL0 * 2) + 1) * ((ilbc_constants.ENH_UPS0 * tloc2) - tloc), (ilbc_constants.ENH_FL0 * 2) + 1);
        return updStartPos;
    }

    public void smath(float[] odata, int odata_idx, float[] sseq, int hl, float alpha0) {
        int i;
        int k;
        int psseq;
        float[] surround = new float[ilbc_constants.BLOCKL_MAX];
        float[] wt = new float[((ilbc_constants.ENH_HL * 2) + 1)];
        for (i = 1; i <= (hl * 2) + 1; i++) {
            wt[i - 1] = 0.5f * (1.0f - ((float) Math.cos((double) (((2.0f * ilbc_constants.PI) * ((float) i)) / ((float) ((hl * 2) + 2))))));
        }
        wt[hl] = 0.0f;
        for (i = 0; i < ilbc_constants.ENH_BLOCKL; i++) {
            surround[i] = sseq[i] * wt[0];
        }
        for (k = 1; k < hl; k++) {
            psseq = k * ilbc_constants.ENH_BLOCKL;
            for (i = 0; i < ilbc_constants.ENH_BLOCKL; i++) {
                surround[i] = surround[i] + (sseq[psseq + i] * wt[k]);
            }
        }
        for (k = hl + 1; k <= hl * 2; k++) {
            psseq = k * ilbc_constants.ENH_BLOCKL;
            for (i = 0; i < ilbc_constants.ENH_BLOCKL; i++) {
                surround[i] = surround[i] + (sseq[psseq + i] * wt[k]);
            }
        }
        float w11 = 0.0f;
        float w10 = 0.0f;
        float w00 = 0.0f;
        psseq = hl * ilbc_constants.ENH_BLOCKL;
        for (i = 0; i < ilbc_constants.ENH_BLOCKL; i++) {
            w00 += sseq[psseq + i] * sseq[psseq + i];
            w11 += surround[i] * surround[i];
            w10 += surround[i] * sseq[psseq + i];
        }
        if (Math.abs(w11) < 1.0f) {
            w11 = 1.0f;
        }
        float C = (float) Math.sqrt((double) (w00 / w11));
        float errs = 0.0f;
        psseq = hl * ilbc_constants.ENH_BLOCKL;
        for (i = 0; i < ilbc_constants.ENH_BLOCKL; i++) {
            odata[odata_idx + i] = surround[i] * C;
            float err = sseq[psseq + i] - odata[odata_idx + i];
            errs += err * err;
        }
        if (errs > alpha0 * w00) {
            float A;
            float B;
            if (w00 < 1.0f) {
                w00 = 1.0f;
            }
            float denom = ((w11 * w00) - (w10 * w10)) / (w00 * w00);
            if (denom > 1.0E-4f) {
                A = (float) Math.sqrt((double) ((alpha0 - ((alpha0 * alpha0) / 4.0f)) / denom));
                B = (((-alpha0) / 2.0f) - ((A * w10) / w00)) + 1.0f;
            } else {
                A = 0.0f;
                B = 1.0f;
            }
            psseq = hl * ilbc_constants.ENH_BLOCKL;
            for (i = 0; i < ilbc_constants.ENH_BLOCKL; i++) {
                odata[odata_idx + i] = (surround[i] * A) + (sseq[psseq + i] * B);
            }
        }
    }

    public void getsseq(float[] sseq, float[] idata, int idatal, int centerStartPos, float[] period, float[] plocs, int periodl, int hl) {
        int q;
        int psseq;
        int li;
        float[] blockStartPos = new float[((ilbc_constants.ENH_HL * 2) + 1)];
        int[] lagBlock = new int[((ilbc_constants.ENH_HL * 2) + 1)];
        float[] plocs2 = new float[ilbc_constants.ENH_PLOCSL];
        lagBlock[hl] = NearestNeighbor(plocs, 0.5f * ((float) (centerStartPos + ((ilbc_constants.ENH_BLOCKL + centerStartPos) - 1))), periodl);
        blockStartPos[hl] = (float) centerStartPos;
        System.arraycopy(idata, centerStartPos, sseq, ilbc_constants.ENH_BLOCKL * hl, ilbc_constants.ENH_BLOCKL);
        for (q = hl - 1; q >= 0; q--) {
            blockStartPos[q] = blockStartPos[q + 1] - period[lagBlock[q + 1]];
            lagBlock[q] = NearestNeighbor(plocs, (blockStartPos[q] + ((float) ilbc_constants.ENH_BLOCKL_HALF)) - period[lagBlock[q + 1]], periodl);
            if (blockStartPos[q] - ((float) ilbc_constants.ENH_OVERHANG) >= 0.0f) {
                blockStartPos[q] = refiner(sseq, q * ilbc_constants.ENH_BLOCKL, idata, idatal, centerStartPos, blockStartPos[q], period[lagBlock[q + 1]]);
            } else {
                psseq = q * ilbc_constants.ENH_BLOCKL;
                for (li = 0; li < ilbc_constants.ENH_BLOCKL; li++) {
                    sseq[psseq + li] = 0.0f;
                }
            }
        }
        for (int i = 0; i < periodl; i++) {
            plocs2[i] = plocs[i] - period[i];
        }
        for (q = hl + 1; q <= hl * 2; q++) {
            lagBlock[q] = NearestNeighbor(plocs2, blockStartPos[q - 1] + ((float) ilbc_constants.ENH_BLOCKL_HALF), periodl);
            blockStartPos[q] = blockStartPos[q - 1] + period[lagBlock[q]];
            if ((blockStartPos[q] + ((float) ilbc_constants.ENH_BLOCKL)) + ((float) ilbc_constants.ENH_OVERHANG) < ((float) idatal)) {
                blockStartPos[q] = refiner(sseq, q * ilbc_constants.ENH_BLOCKL, idata, idatal, centerStartPos, blockStartPos[q], period[lagBlock[q]]);
            } else {
                psseq = q * ilbc_constants.ENH_BLOCKL;
                for (li = 0; li < ilbc_constants.ENH_BLOCKL; li++) {
                    sseq[psseq + li] = 0.0f;
                }
            }
        }
    }

    public void enhancer(float[] odata, int odata_idx, float[] idata, int idatal, int centerStartPos, float alpha0, float[] period, float[] plocs, int periodl) {
        float[] sseq = new float[(((ilbc_constants.ENH_HL * 2) + 1) * ilbc_constants.ENH_BLOCKL)];
        getsseq(sseq, idata, idatal, centerStartPos, period, plocs, periodl, ilbc_constants.ENH_HL);
        smath(odata, odata_idx, sseq, ilbc_constants.ENH_HL, alpha0);
    }

    public float xCorrCoef(float[] target, int t_idx, float[] regressor, int r_idx, int subl) {
        float ftmp1 = 0.0f;
        float ftmp2 = 0.0f;
        for (int i = 0; i < subl; i++) {
            ftmp1 += target[t_idx + i] * regressor[r_idx + i];
            ftmp2 += regressor[r_idx + i] * regressor[r_idx + i];
        }
        if (ftmp1 > 0.0f) {
            return (ftmp1 * ftmp1) / ftmp2;
        }
        return 0.0f;
    }

    /* access modifiers changed from: 0000 */
    public int enhancerInterface(float[] out, float[] in) {
        int plc_blockl;
        float maxcc;
        int ilag;
        float cc;
        int lag = 0;
        float[] plc_pred = new float[ilbc_constants.ENH_BLOCKL];
        float[] lpState = new float[6];
        float[] downsampled = new float[(((ilbc_constants.ENH_NBLOCKS * ilbc_constants.ENH_BLOCKL) + WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18) / 2)];
        int inLen = (ilbc_constants.ENH_NBLOCKS * ilbc_constants.ENH_BLOCKL) + WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18;
        System.arraycopy(this.enh_buf, this.ULP_inst.blockl, this.enh_buf, 0, ilbc_constants.ENH_BUFL - this.ULP_inst.blockl);
        System.arraycopy(in, 0, this.enh_buf, ilbc_constants.ENH_BUFL - this.ULP_inst.blockl, this.ULP_inst.blockl);
        if (this.ULP_inst.mode == 30) {
            plc_blockl = ilbc_constants.ENH_BLOCKL;
        } else {
            plc_blockl = 40;
        }
        int ioffset = 0;
        if (this.ULP_inst.mode == 20) {
            ioffset = 1;
        }
        int i = 3 - ioffset;
        System.arraycopy(this.enh_period, i, this.enh_period, 0, ilbc_constants.ENH_NBLOCKS_TOT - i);
        System.arraycopy(this.enh_buf, ((ilbc_constants.ENH_NBLOCKS_EXTRA + ioffset) * ilbc_constants.ENH_BLOCKL) - 126, lpState, 0, 6);
        DownSample(this.enh_buf, ((ilbc_constants.ENH_NBLOCKS_EXTRA + ioffset) * ilbc_constants.ENH_BLOCKL) - 120, ilbc_constants.lpFilt_coefsTbl, inLen - (ilbc_constants.ENH_BLOCKL * ioffset), lpState, downsampled);
        int iblock = 0;
        while (iblock < ilbc_constants.ENH_NBLOCKS - ioffset) {
            int lag2 = 10;
            maxcc = xCorrCoef(downsampled, (ilbc_constants.ENH_BLOCKL_HALF * iblock) + 60, downsampled, ((ilbc_constants.ENH_BLOCKL_HALF * iblock) + 60) - 10, ilbc_constants.ENH_BLOCKL_HALF);
            ilag = 11;
            while (ilag < 60) {
                cc = xCorrCoef(downsampled, (ilbc_constants.ENH_BLOCKL_HALF * iblock) + 60, downsampled, ((ilbc_constants.ENH_BLOCKL_HALF * iblock) + 60) - ilag, ilbc_constants.ENH_BLOCKL_HALF);
                if (cc > maxcc) {
                    maxcc = cc;
                    lag = ilag;
                } else {
                    lag = lag2;
                }
                ilag++;
                lag2 = lag;
            }
            this.enh_period[(ilbc_constants.ENH_NBLOCKS_EXTRA + iblock) + ioffset] = ((float) lag2) * 2.0f;
            iblock++;
            lag = lag2;
        }
        if (this.prev_enh_pl == 1) {
            int start;
            int isample;
            int inlag = (int) this.enh_period[ilbc_constants.ENH_NBLOCKS_EXTRA + ioffset];
            lag = inlag - 1;
            maxcc = xCorrCoef(in, 0, in, lag, plc_blockl);
            for (ilag = inlag; ilag <= inlag + 1; ilag++) {
                cc = xCorrCoef(in, 0, in, ilag, plc_blockl);
                if (cc > maxcc) {
                    maxcc = cc;
                    lag = ilag;
                }
            }
            this.enh_period[(ilbc_constants.ENH_NBLOCKS_EXTRA + ioffset) - 1] = (float) lag;
            int inPtr = lag - 1;
            int enh_bufPtr1 = plc_blockl - 1;
            if (lag > plc_blockl) {
                start = plc_blockl;
            } else {
                start = lag;
            }
            for (isample = start; isample > 0; isample--) {
                plc_pred[enh_bufPtr1] = in[inPtr];
                enh_bufPtr1--;
                inPtr--;
            }
            int enh_bufPtr2 = (ilbc_constants.ENH_BUFL - 1) - this.ULP_inst.blockl;
            for (isample = (plc_blockl - 1) - lag; isample >= 0; isample--) {
                plc_pred[enh_bufPtr1] = this.enh_buf[enh_bufPtr2];
                enh_bufPtr1--;
                enh_bufPtr2--;
            }
            float ftmp2 = 0.0f;
            float ftmp1 = 0.0f;
            for (i = 0; i < plc_blockl; i++) {
                ftmp2 += this.enh_buf[((ilbc_constants.ENH_BUFL - 1) - this.ULP_inst.blockl) - i] * this.enh_buf[((ilbc_constants.ENH_BUFL - 1) - this.ULP_inst.blockl) - i];
                ftmp1 += plc_pred[i] * plc_pred[i];
            }
            ftmp1 = (float) Math.sqrt((double) (ftmp1 / ((float) plc_blockl)));
            ftmp2 = (float) Math.sqrt((double) (ftmp2 / ((float) plc_blockl)));
            if (ftmp1 > 2.0f * ftmp2 && ((double) ftmp1) > Pa.LATENCY_UNSPECIFIED) {
                for (i = 0; i < plc_blockl - 10; i++) {
                    plc_pred[i] = plc_pred[i] * ((2.0f * ftmp2) / ftmp1);
                }
                for (i = plc_blockl - 10; i < plc_blockl; i++) {
                    plc_pred[i] = plc_pred[i] * (((((float) ((i - plc_blockl) + 10)) * (1.0f - ((2.0f * ftmp2) / ftmp1))) / 10.0f) + ((2.0f * ftmp2) / ftmp1));
                }
            }
            enh_bufPtr1 = (ilbc_constants.ENH_BUFL - 1) - this.ULP_inst.blockl;
            for (i = 0; i < plc_blockl; i++) {
                ftmp1 = ((float) (i + 1)) / ((float) (plc_blockl + 1));
                float[] fArr = this.enh_buf;
                fArr[enh_bufPtr1] = fArr[enh_bufPtr1] * ftmp1;
                fArr = this.enh_buf;
                fArr[enh_bufPtr1] = fArr[enh_bufPtr1] + ((1.0f - ftmp1) * plc_pred[(plc_blockl - 1) - i]);
                enh_bufPtr1--;
            }
        }
        if (this.ULP_inst.mode == 20) {
            for (iblock = 0; iblock < 2; iblock++) {
                enhancer(out, iblock * ilbc_constants.ENH_BLOCKL, this.enh_buf, ilbc_constants.ENH_BUFL, ((iblock + 5) * ilbc_constants.ENH_BLOCKL) + 40, ilbc_constants.ENH_ALPHA0, this.enh_period, ilbc_constants.enh_plocsTbl, ilbc_constants.ENH_NBLOCKS_TOT);
            }
        } else if (this.ULP_inst.mode == 30) {
            for (iblock = 0; iblock < 3; iblock++) {
                enhancer(out, iblock * ilbc_constants.ENH_BLOCKL, this.enh_buf, ilbc_constants.ENH_BUFL, (iblock + 4) * ilbc_constants.ENH_BLOCKL, ilbc_constants.ENH_ALPHA0, this.enh_period, ilbc_constants.enh_plocsTbl, ilbc_constants.ENH_NBLOCKS_TOT);
            }
        }
        return lag * 2;
    }

    public void compCorr(float[] cc, float[] gc, float[] pm, float[] buffer, int lag, int bLen, int sRange) {
        if ((bLen - sRange) - lag < 0) {
            sRange = bLen - lag;
        }
        float ftmp1 = 0.0f;
        float ftmp2 = 0.0f;
        float ftmp3 = 0.0f;
        for (int i = 0; i < sRange; i++) {
            ftmp1 += buffer[(bLen - sRange) + i] * buffer[((bLen - sRange) + i) - lag];
            ftmp2 += buffer[((bLen - sRange) + i) - lag] * buffer[((bLen - sRange) + i) - lag];
            ftmp3 += buffer[(bLen - sRange) + i] * buffer[(bLen - sRange) + i];
        }
        if (ftmp2 > 0.0f) {
            cc[0] = (ftmp1 * ftmp1) / ftmp2;
            gc[0] = Math.abs(ftmp1 / ftmp2);
            pm[0] = Math.abs(ftmp1) / (((float) Math.sqrt((double) ftmp2)) * ((float) Math.sqrt((double) ftmp3)));
            return;
        }
        cc[0] = 0.0f;
        gc[0] = 0.0f;
        pm[0] = 0.0f;
    }

    public void doThePLC(float[] PLCresidual, float[] PLClpc, int PLI, float[] decresidual, float[] lpc, int lpc_idx, int inlag) {
        int lag = 20;
        float gain_comp = 0.0f;
        float maxcc_comp = 0.0f;
        float per = 0.0f;
        float max_per = 0.0f;
        float[] randvec = new float[ilbc_constants.BLOCKL_MAX];
        float[] a_gain = new float[1];
        float[] a_comp = new float[1];
        float[] a_per = new float[1];
        if (PLI == 1) {
            int i;
            float pitchfact;
            this.consPLICount++;
            if (this.prevPLI != 1) {
                lag = inlag - 3;
                a_comp[0] = 0.0f;
                a_gain[0] = 0.0f;
                a_per[0] = 0.0f;
                compCorr(a_comp, a_gain, a_per, this.prevResidual, lag, this.ULP_inst.blockl, 60);
                float maxcc = a_comp[0];
                float gain = a_gain[0];
                max_per = a_per[0];
                for (i = inlag - 2; i <= inlag + 3; i++) {
                    a_comp[0] = maxcc_comp;
                    a_gain[0] = gain_comp;
                    a_per[0] = per;
                    compCorr(a_comp, a_gain, a_per, this.prevResidual, i, this.ULP_inst.blockl, 60);
                    maxcc_comp = a_comp[0];
                    gain_comp = a_gain[0];
                    per = a_per[0];
                    if (maxcc_comp > maxcc) {
                        maxcc = maxcc_comp;
                        gain = gain_comp;
                        lag = i;
                        max_per = per;
                    }
                }
            } else {
                lag = this.prevLag;
                max_per = this.per;
            }
            float use_gain = 1.0f;
            if (this.consPLICount * this.ULP_inst.blockl > 320) {
                use_gain = 0.9f;
            } else if (this.consPLICount * this.ULP_inst.blockl > DeviceConfiguration.DEFAULT_VIDEO_WIDTH) {
                use_gain = 0.7f;
            } else if (this.consPLICount * this.ULP_inst.blockl > 960) {
                use_gain = 0.5f;
            } else if (this.consPLICount * this.ULP_inst.blockl > 1280) {
                use_gain = 0.0f;
            }
            float ftmp = (float) Math.sqrt((double) max_per);
            if (ftmp > 0.7f) {
                pitchfact = 1.0f;
            } else if (ftmp > 0.4f) {
                pitchfact = (ftmp - 0.4f) / 0.29999998f;
            } else {
                pitchfact = 0.0f;
            }
            int use_lag = lag;
            if (lag < 80) {
                use_lag = lag * 2;
            }
            float energy = 0.0f;
            for (i = 0; i < this.ULP_inst.blockl; i++) {
                this.seed = ((this.seed * 69069) + 1) & 2147483647L;
                int pick = i - (((int) (this.seed % 70)) + 50);
                if (pick < 0) {
                    randvec[i] = this.prevResidual[this.ULP_inst.blockl + pick];
                } else {
                    randvec[i] = randvec[pick];
                }
                pick = i - use_lag;
                if (pick < 0) {
                    PLCresidual[i] = this.prevResidual[this.ULP_inst.blockl + pick];
                } else {
                    PLCresidual[i] = PLCresidual[pick];
                }
                if (i < 80) {
                    PLCresidual[i] = ((PLCresidual[i] * pitchfact) + ((1.0f - pitchfact) * randvec[i])) * use_gain;
                } else if (i < 160) {
                    PLCresidual[i] = (0.95f * use_gain) * ((PLCresidual[i] * pitchfact) + ((1.0f - pitchfact) * randvec[i]));
                } else {
                    PLCresidual[i] = (0.9f * use_gain) * ((PLCresidual[i] * pitchfact) + ((1.0f - pitchfact) * randvec[i]));
                }
                energy += PLCresidual[i] * PLCresidual[i];
            }
            if (((float) Math.sqrt((double) (energy / ((float) this.ULP_inst.blockl)))) < 30.0f) {
                for (i = 0; i < this.ULP_inst.blockl; i++) {
                    PLCresidual[i] = randvec[i];
                }
            }
            System.arraycopy(this.prevLpc, 0, PLClpc, 0, ilbc_constants.LPC_FILTERORDER + 1);
        } else {
            System.arraycopy(decresidual, 0, PLCresidual, 0, this.ULP_inst.blockl);
            System.arraycopy(lpc, lpc_idx, PLClpc, 0, ilbc_constants.LPC_FILTERORDER + 1);
            this.consPLICount = 0;
        }
        if (PLI != 0) {
            this.prevLag = lag;
            this.per = max_per;
        }
        this.prevPLI = PLI;
        System.arraycopy(PLClpc, 0, this.prevLpc, 0, ilbc_constants.LPC_FILTERORDER + 1);
        System.arraycopy(PLCresidual, 0, this.prevResidual, 0, this.ULP_inst.blockl);
    }

    public short decode(byte[] decoded, int decodedOffset, byte[] encoded, int encodedOffset, short mode) {
        float[] decblock = new float[ilbc_constants.BLOCKL_MAX];
        bitstream en_data = new bitstream(encoded, encodedOffset, this.ULP_inst.no_of_bytes);
        if (mode < (short) 0 || mode > (short) 1) {
            System.out.println("\nERROR - Wrong mode - 0, 1 allowed\n");
        }
        iLBC_decode(decblock, en_data, mode);
        int k = 0;
        while (k < this.ULP_inst.blockl) {
            float dtmp = decblock[k];
            if (dtmp < ((float) ilbc_constants.MIN_SAMPLE)) {
                dtmp = (float) ilbc_constants.MIN_SAMPLE;
            } else if (dtmp > ((float) ilbc_constants.MAX_SAMPLE)) {
                dtmp = (float) ilbc_constants.MAX_SAMPLE;
            }
            ArrayIOUtils.writeShort((short) ((int) dtmp), decoded, decodedOffset);
            k++;
            decodedOffset += 2;
        }
        return (short) this.ULP_inst.blockl;
    }

    public void Decode(float[] decresidual, int start, int idxForMax, int[] idxVec, float[] syntdenum, int[] cb_index, int[] gain_index, int[] extra_cb_index, int[] extra_gain_index, int state_first) {
        int start_pos;
        int li;
        int k;
        int meml_gotten;
        int subframe;
        Object reverseDecresidual = new float[ilbc_constants.BLOCKL_MAX];
        float[] mem = new float[ilbc_constants.CB_MEML];
        int diff = ilbc_constants.STATE_LEN - this.ULP_inst.state_short_len;
        if (state_first == 1) {
            start_pos = (start - 1) * ilbc_constants.SUBL;
        } else {
            start_pos = ((start - 1) * ilbc_constants.SUBL) + diff;
        }
        ilbc_common.StateConstructW(idxForMax, idxVec, syntdenum, (start - 1) * (ilbc_constants.LPC_FILTERORDER + 1), decresidual, start_pos, this.ULP_inst.state_short_len);
        if (state_first != 0) {
            for (li = 0; li < ilbc_constants.CB_MEML - this.ULP_inst.state_short_len; li++) {
                mem[li] = 0.0f;
            }
            System.arraycopy(decresidual, start_pos, mem, ilbc_constants.CB_MEML - this.ULP_inst.state_short_len, this.ULP_inst.state_short_len);
            ilbc_common.iCBConstruct(decresidual, start_pos + this.ULP_inst.state_short_len, extra_cb_index, 0, extra_gain_index, 0, mem, ilbc_constants.CB_MEML - ilbc_constants.stMemLTbl, ilbc_constants.stMemLTbl, diff, ilbc_constants.CB_NSTAGES);
        } else {
            for (k = 0; k < diff; k++) {
                reverseDecresidual[k] = decresidual[(((start + 1) * ilbc_constants.SUBL) - 1) - (this.ULP_inst.state_short_len + k)];
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
            for (subframe = 0; subframe < Nfor; subframe++) {
                ilbc_common.iCBConstruct(decresidual, ((start + 1) + subframe) * ilbc_constants.SUBL, cb_index, subcount * ilbc_constants.CB_NSTAGES, gain_index, subcount * ilbc_constants.CB_NSTAGES, mem, ilbc_constants.CB_MEML - ilbc_constants.memLfTbl[subcount], ilbc_constants.memLfTbl[subcount], ilbc_constants.SUBL, ilbc_constants.CB_NSTAGES);
                System.arraycopy(mem, ilbc_constants.SUBL, mem, 0, ilbc_constants.CB_MEML - ilbc_constants.SUBL);
                System.arraycopy(decresidual, ((start + 1) + subframe) * ilbc_constants.SUBL, mem, ilbc_constants.CB_MEML - ilbc_constants.SUBL, ilbc_constants.SUBL);
                subcount++;
            }
        }
        int Nback = start - 1;
        if (Nback > 0) {
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
            for (subframe = 0; subframe < Nback; subframe++) {
                ilbc_common.iCBConstruct(reverseDecresidual, subframe * ilbc_constants.SUBL, cb_index, subcount * ilbc_constants.CB_NSTAGES, gain_index, subcount * ilbc_constants.CB_NSTAGES, mem, ilbc_constants.CB_MEML - ilbc_constants.memLfTbl[subcount], ilbc_constants.memLfTbl[subcount], ilbc_constants.SUBL, ilbc_constants.CB_NSTAGES);
                System.arraycopy(mem, ilbc_constants.SUBL, mem, 0, ilbc_constants.CB_MEML - ilbc_constants.SUBL);
                System.arraycopy(reverseDecresidual, ilbc_constants.SUBL * subframe, mem, ilbc_constants.CB_MEML - ilbc_constants.SUBL, ilbc_constants.SUBL);
                subcount++;
            }
            for (int i = 0; i < ilbc_constants.SUBL * Nback; i++) {
                decresidual[((ilbc_constants.SUBL * Nback) - i) - 1] = reverseDecresidual[i];
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void iLBC_decode(float[] decblock, bitstream bytes, int mode) {
        int i;
        Object data = new float[ilbc_constants.BLOCKL_MAX];
        float[] lsfdeq = new float[(ilbc_constants.LPC_FILTERORDER * ilbc_constants.LPC_N_MAX)];
        float[] PLCresidual = new float[ilbc_constants.BLOCKL_MAX];
        Object PLClpc = new float[(ilbc_constants.LPC_FILTERORDER + 1)];
        float[] zeros = new float[ilbc_constants.BLOCKL_MAX];
        float[] one = new float[(ilbc_constants.LPC_FILTERORDER + 1)];
        int[] idxVec = new int[ilbc_constants.STATE_LEN];
        int[] gain_index = new int[(ilbc_constants.NASUB_MAX * ilbc_constants.CB_NSTAGES)];
        int[] extra_gain_index = new int[ilbc_constants.CB_NSTAGES];
        int[] cb_index = new int[(ilbc_constants.CB_NSTAGES * ilbc_constants.NASUB_MAX)];
        int[] extra_cb_index = new int[ilbc_constants.CB_NSTAGES];
        int[] lsf_i = new int[(ilbc_constants.LSF_NSPLIT * ilbc_constants.LPC_N_MAX)];
        float[] weightdenum = new float[((ilbc_constants.LPC_FILTERORDER + 1) * ilbc_constants.NSUB_MAX)];
        float[] syntdenum = new float[(ilbc_constants.NSUB_MAX * (ilbc_constants.LPC_FILTERORDER + 1))];
        float[] decresidual = new float[ilbc_constants.BLOCKL_MAX];
        if (mode > 0) {
            int k;
            for (k = 0; k < ilbc_constants.LSF_NSPLIT * ilbc_constants.LPC_N_MAX; k++) {
                lsf_i[k] = 0;
            }
            int start = 0;
            int state_first = 0;
            int idxForMax = 0;
            for (k = 0; k < this.ULP_inst.state_short_len; k++) {
                idxVec[k] = 0;
            }
            for (k = 0; k < ilbc_constants.CB_NSTAGES; k++) {
                extra_cb_index[k] = 0;
            }
            for (k = 0; k < ilbc_constants.CB_NSTAGES; k++) {
                extra_gain_index[k] = 0;
            }
            for (i = 0; i < this.ULP_inst.nasub; i++) {
                for (k = 0; k < ilbc_constants.CB_NSTAGES; k++) {
                    cb_index[(ilbc_constants.CB_NSTAGES * i) + k] = 0;
                }
            }
            for (i = 0; i < this.ULP_inst.nasub; i++) {
                for (k = 0; k < ilbc_constants.CB_NSTAGES; k++) {
                    gain_index[(ilbc_constants.CB_NSTAGES * i) + k] = 0;
                }
            }
            for (int ulp = 0; ulp < 3; ulp++) {
                int lastpart;
                for (k = 0; k < ilbc_constants.LSF_NSPLIT * this.ULP_inst.lpc_n; k++) {
                    lastpart = bytes.unpack(this.ULP_inst.lsf_bits[k][ulp]);
                    lsf_i[k] = bytes.packcombine(lsf_i[k], lastpart, this.ULP_inst.lsf_bits[k][ulp]);
                }
                lastpart = bytes.unpack(this.ULP_inst.start_bits[ulp]);
                start = bytes.packcombine(start, lastpart, this.ULP_inst.start_bits[ulp]);
                lastpart = bytes.unpack(this.ULP_inst.startfirst_bits[ulp]);
                state_first = bytes.packcombine(state_first, lastpart, this.ULP_inst.startfirst_bits[ulp]);
                lastpart = bytes.unpack(this.ULP_inst.scale_bits[ulp]);
                idxForMax = bytes.packcombine(idxForMax, lastpart, this.ULP_inst.scale_bits[ulp]);
                for (k = 0; k < this.ULP_inst.state_short_len; k++) {
                    lastpart = bytes.unpack(this.ULP_inst.state_bits[ulp]);
                    idxVec[k] = bytes.packcombine(idxVec[k], lastpart, this.ULP_inst.state_bits[ulp]);
                }
                for (k = 0; k < ilbc_constants.CB_NSTAGES; k++) {
                    lastpart = bytes.unpack(this.ULP_inst.extra_cb_index[k][ulp]);
                    extra_cb_index[k] = bytes.packcombine(extra_cb_index[k], lastpart, this.ULP_inst.extra_cb_index[k][ulp]);
                }
                for (k = 0; k < ilbc_constants.CB_NSTAGES; k++) {
                    lastpart = bytes.unpack(this.ULP_inst.extra_cb_gain[k][ulp]);
                    extra_gain_index[k] = bytes.packcombine(extra_gain_index[k], lastpart, this.ULP_inst.extra_cb_gain[k][ulp]);
                }
                for (i = 0; i < this.ULP_inst.nasub; i++) {
                    for (k = 0; k < ilbc_constants.CB_NSTAGES; k++) {
                        lastpart = bytes.unpack(this.ULP_inst.cb_index[i][k][ulp]);
                        cb_index[(ilbc_constants.CB_NSTAGES * i) + k] = bytes.packcombine(cb_index[(ilbc_constants.CB_NSTAGES * i) + k], lastpart, this.ULP_inst.cb_index[i][k][ulp]);
                    }
                }
                for (i = 0; i < this.ULP_inst.nasub; i++) {
                    for (k = 0; k < ilbc_constants.CB_NSTAGES; k++) {
                        lastpart = bytes.unpack(this.ULP_inst.cb_gain[i][k][ulp]);
                        gain_index[(ilbc_constants.CB_NSTAGES * i) + k] = bytes.packcombine(gain_index[(ilbc_constants.CB_NSTAGES * i) + k], lastpart, this.ULP_inst.cb_gain[i][k][ulp]);
                    }
                }
            }
            int last_bit = bytes.unpack(1);
            if (start < 1) {
                mode = 0;
            }
            if (this.ULP_inst.mode == 20 && start > 3) {
                mode = 0;
            }
            if (this.ULP_inst.mode == 30 && start > 5) {
                mode = 0;
            }
            if (last_bit == 1) {
                mode = 0;
            }
            if (mode == 1) {
                index_conv_dec(cb_index);
                SimplelsfDEQ(lsfdeq, lsf_i, this.ULP_inst.lpc_n);
                ilbc_common.LSF_check(lsfdeq, ilbc_constants.LPC_FILTERORDER, this.ULP_inst.lpc_n);
                DecoderInterpolateLSF(syntdenum, weightdenum, lsfdeq, ilbc_constants.LPC_FILTERORDER);
                Decode(decresidual, start, idxForMax, idxVec, syntdenum, cb_index, gain_index, extra_cb_index, extra_gain_index, state_first);
                doThePLC(PLCresidual, PLClpc, 0, decresidual, syntdenum, (ilbc_constants.LPC_FILTERORDER + 1) * (this.ULP_inst.nsub - 1), this.last_lag);
                System.arraycopy(PLCresidual, 0, decresidual, 0, this.ULP_inst.blockl);
            }
        }
        if (mode == 0) {
            int li;
            for (li = 0; li < ilbc_constants.BLOCKL_MAX; li++) {
                zeros[li] = 0.0f;
            }
            one[0] = 1.0f;
            for (li = 0; li < ilbc_constants.LPC_FILTERORDER; li++) {
                one[li + 1] = 0.0f;
            }
            doThePLC(PLCresidual, PLClpc, 1, zeros, one, 0, this.last_lag);
            System.arraycopy(PLCresidual, 0, decresidual, 0, this.ULP_inst.blockl);
            int order_plus_one = ilbc_constants.LPC_FILTERORDER + 1;
            for (i = 0; i < this.ULP_inst.nsub; i++) {
                System.arraycopy(PLClpc, 0, syntdenum, i * order_plus_one, order_plus_one);
            }
        }
        if (this.use_enhancer == 1) {
            this.last_lag = enhancerInterface(data, decresidual);
            if (this.ULP_inst.mode == 20) {
                syntFilter(data, 0 * ilbc_constants.SUBL, this.old_syntdenum, ((this.ULP_inst.nsub + 0) - 1) * (ilbc_constants.LPC_FILTERORDER + 1), ilbc_constants.SUBL, this.syntMem);
                for (i = 1; i < this.ULP_inst.nsub; i++) {
                    syntFilter(data, i * ilbc_constants.SUBL, syntdenum, (i - 1) * (ilbc_constants.LPC_FILTERORDER + 1), ilbc_constants.SUBL, this.syntMem);
                }
            } else if (this.ULP_inst.mode == 30) {
                for (i = 0; i < 2; i++) {
                    syntFilter(data, i * ilbc_constants.SUBL, this.old_syntdenum, ((this.ULP_inst.nsub + i) - 2) * (ilbc_constants.LPC_FILTERORDER + 1), ilbc_constants.SUBL, this.syntMem);
                }
                for (i = 2; i < this.ULP_inst.nsub; i++) {
                    syntFilter(data, i * ilbc_constants.SUBL, syntdenum, (i - 2) * (ilbc_constants.LPC_FILTERORDER + 1), ilbc_constants.SUBL, this.syntMem);
                }
            }
        } else {
            int lag = 20;
            float maxcc = xCorrCoef(decresidual, ilbc_constants.BLOCKL_MAX - ilbc_constants.ENH_BLOCKL, decresidual, (ilbc_constants.BLOCKL_MAX - ilbc_constants.ENH_BLOCKL) - 20, ilbc_constants.ENH_BLOCKL);
            for (int ilag = 21; ilag < 120; ilag++) {
                float cc = xCorrCoef(decresidual, ilbc_constants.BLOCKL_MAX - ilbc_constants.ENH_BLOCKL, decresidual, (ilbc_constants.BLOCKL_MAX - ilbc_constants.ENH_BLOCKL) - ilag, ilbc_constants.ENH_BLOCKL);
                if (cc > maxcc) {
                    maxcc = cc;
                    lag = ilag;
                }
            }
            this.last_lag = lag;
            System.arraycopy(decresidual, 0, data, 0, this.ULP_inst.blockl);
            for (i = 0; i < this.ULP_inst.nsub; i++) {
                syntFilter(data, i * ilbc_constants.SUBL, syntdenum, i * (ilbc_constants.LPC_FILTERORDER + 1), ilbc_constants.SUBL, this.syntMem);
            }
        }
        hpOutput(data, this.ULP_inst.blockl, decblock, this.hpomem);
        System.arraycopy(syntdenum, 0, this.old_syntdenum, 0, this.ULP_inst.nsub * (ilbc_constants.LPC_FILTERORDER + 1));
        this.prev_enh_pl = 0;
        if (mode == 0) {
            this.prev_enh_pl = 1;
        }
    }

    public ilbc_decoder(int init_mode, int init_enhancer) {
        int li;
        this.ULP_inst = new ilbc_ulp(init_mode);
        this.syntMem = new float[ilbc_constants.LPC_FILTERORDER];
        this.prevLpc = new float[(ilbc_constants.LPC_FILTERORDER + 1)];
        this.prevResidual = new float[(ilbc_constants.NSUB_MAX * ilbc_constants.SUBL)];
        this.old_syntdenum = new float[((ilbc_constants.LPC_FILTERORDER + 1) * ilbc_constants.NSUB_MAX)];
        this.hpomem = new float[4];
        this.enh_buf = new float[ilbc_constants.ENH_BUFL];
        this.enh_period = new float[ilbc_constants.ENH_NBLOCKS_TOT];
        this.lsfdeqold = new float[ilbc_constants.LPC_FILTERORDER];
        for (li = 0; li < this.syntMem.length; li++) {
            this.syntMem[li] = 0.0f;
        }
        System.arraycopy(ilbc_constants.lsfmeanTbl, 0, this.lsfdeqold, 0, ilbc_constants.LPC_FILTERORDER);
        for (li = 0; li < this.old_syntdenum.length; li++) {
            this.old_syntdenum[li] = 0.0f;
        }
        for (li = 0; li < ilbc_constants.NSUB_MAX; li++) {
            this.old_syntdenum[(ilbc_constants.LPC_FILTERORDER + 1) * li] = 1.0f;
        }
        this.last_lag = 20;
        this.prevLag = WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18;
        this.per = 0.0f;
        this.consPLICount = 0;
        this.prevPLI = 0;
        this.prevLpc[0] = 1.0f;
        for (li = 1; li < this.prevLpc.length; li++) {
            this.prevLpc[li] = 0.0f;
        }
        for (li = 0; li < this.prevResidual.length; li++) {
            this.prevResidual[li] = 0.0f;
        }
        this.seed = 777;
        for (li = 0; li < this.hpomem.length; li++) {
            this.hpomem[li] = 0.0f;
        }
        this.use_enhancer = init_enhancer;
        for (li = 0; li < this.enh_buf.length; li++) {
            this.enh_buf[li] = 0.0f;
        }
        for (li = 0; li < ilbc_constants.ENH_NBLOCKS_TOT; li++) {
            this.enh_period[li] = 40.0f;
        }
        this.prev_enh_pl = 0;
    }
}
