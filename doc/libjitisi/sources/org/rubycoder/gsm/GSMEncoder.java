package org.rubycoder.gsm;

import com.lti.utils.UnsignedUtils;
import com.sun.media.format.WavAudioFormat;
import java.lang.reflect.Array;

public class GSMEncoder {
    static final /* synthetic */ boolean $assertionsDisabled = (!GSMEncoder.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    private static final int[] FAC = new int[]{18431, 20479, 22527, 24575, 26623, 28671, 30719, 32767};
    private static final byte GSM_MAGIC = (byte) 13;
    private static final int MAX_LONGWORD = Integer.MAX_VALUE;
    private static final int MAX_WORD = 32767;
    private static final int MIN_LONGWORD = Integer.MIN_VALUE;
    private static final int MIN_WORD = -32768;
    private static final int[] QLB = new int[]{3277, 11469, 21299, 32767};
    private static final byte[] bitoff = new byte[]{(byte) 8, (byte) 7, (byte) 6, (byte) 6, (byte) 5, (byte) 5, (byte) 5, (byte) 5, (byte) 4, (byte) 4, (byte) 4, (byte) 4, (byte) 4, (byte) 4, (byte) 4, (byte) 4, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 3, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
    private final int[][] LARpp = ((int[][]) Array.newInstance(Integer.TYPE, new int[]{2, 8}));
    private int L_z2;
    private int bcOffset;
    private int dOffset;
    private final int[] dp0 = new int[280];
    private int dpOffset;
    private int dppOffset;
    private final int[] e = new int[50];
    private int eOffset;
    private char fast;
    private char frame_chain;
    private char frame_index;
    private final int[] gsm_DLB = new int[]{6554, 16384, 26214, 32767};
    private final int[] gsm_NRFAC = new int[]{29128, 26215, 23832, 21846, 20165, 18725, 17476, 16384};
    private int j;
    private int ltp_cut;
    private int mcoffset = 0;
    private int mp;
    private int msr;
    private int ncOffset;
    private final int[] u = new int[8];
    private int[] v = new int[9];
    private char verbose;
    private char wav_fmt;
    private int xmaxcOffset;
    private int xmcOffset;
    private int z1;

    private static int add(int a, int b) {
        return saturate(a + b);
    }

    private static int asl(int a, int n) {
        if (n >= 16) {
            return 0;
        }
        if (n <= -16) {
            if (a < 0) {
                return -1;
            }
            return 0;
        } else if (n < 0) {
            return asr(a, -n);
        } else {
            return a << n;
        }
    }

    private static int asr(int a, int n) {
        if (n >= 16) {
            if (a < 0) {
                return -1;
            }
            return 0;
        } else if (n <= -16) {
            return 0;
        } else {
            if (n < 0) {
                return a << (-n);
            }
            return a >> n;
        }
    }

    private static void Coefficients_40_159(int[] LARpp_j, int[] LARp) {
        for (int i = 0; i < 8; i++) {
            LARp[i] = LARpp_j[i];
        }
    }

    private static void LARp_to_rp(int[] LARp) {
        for (int i = 0; i < 8; i++) {
            int temp;
            int add;
            if (LARp[i] < 0) {
                temp = LARp[i] == MIN_WORD ? 32767 : -LARp[i];
                add = temp < 11059 ? temp << 1 : temp < 20070 ? temp + 11059 : add(temp >> 2, 26112);
                LARp[i] = -add;
            } else {
                temp = LARp[i];
                add = temp < 11059 ? temp << 1 : temp < 20070 ? temp + 11059 : add(temp >> 2, 26112);
                LARp[i] = add;
            }
        }
    }

    public static void main(String[] args) {
        new GSMEncoder().encode(new int[160]);
    }

    private static int mult_r(int a, int b) {
        if (b == MIN_WORD && a == MIN_WORD) {
            return 32767;
        }
        return saturate(((a * b) + 16384) >> 15);
    }

    public static void print(String name, int[] data) {
        System.out.print("[" + name + ":");
        for (int i = 0; i < data.length; i++) {
            System.out.print("" + data[i]);
            if (i < data.length - 1) {
                System.out.print(",");
            } else {
                System.out.println("]");
            }
        }
    }

    public static void print(String name, int data) {
        System.out.println("[" + name + ":" + data + "]");
    }

    private static int saturate(int x) {
        return x < MIN_WORD ? MIN_WORD : x > 32767 ? 32767 : x;
    }

    private static int sub(int a, int b) {
        return saturate(a - b);
    }

    private int abs(int a) {
        if (a < 0) {
            return a == MIN_WORD ? 32767 : -a;
        } else {
            return a;
        }
    }

    private void APCM_quantization(int[] xM, int[] xMc, int[] mant_out, int[] exp_out, int[] xmaxc_out) {
        int i;
        int temp;
        int[] exp = new int[1];
        int[] mant = new int[1];
        int xmax = 0;
        for (i = 0; i <= 12; i++) {
            temp = abs(xM[i]);
            if (temp > xmax) {
                xmax = temp;
            }
        }
        exp[0] = 0;
        temp = sasr(xmax, 9);
        boolean itest = $assertionsDisabled;
        i = 0;
        while (i <= 5) {
            itest |= temp <= 0 ? 1 : 0;
            temp = sasr(temp, 1);
            if ($assertionsDisabled || exp[0] <= 5) {
                if (!itest) {
                    exp[0] = exp[0] + 1;
                }
                i++;
            } else {
                throw new AssertionError();
            }
        }
        if ($assertionsDisabled || (exp[0] <= 6 && exp[0] >= 0)) {
            temp = exp[0] + 5;
            if ($assertionsDisabled || (temp <= 11 && temp >= 0)) {
                int xmaxc = add(sasr(xmax, temp), exp[0] << 3);
                APCM_quantization_xmaxc_to_exp_mant(xmaxc, exp, mant);
                if (!$assertionsDisabled && (exp[0] > 4096 || exp[0] < -4096)) {
                    throw new AssertionError();
                } else if ($assertionsDisabled || (mant[0] >= 0 && mant[0] <= 7)) {
                    int temp1 = 6 - exp[0];
                    int temp2 = this.gsm_NRFAC[mant[0]];
                    i = 0;
                    while (i <= 12) {
                        if ($assertionsDisabled || (temp1 >= 0 && temp1 < 16)) {
                            xMc[this.xmcOffset + i] = sasr(gsm_mult(xM[i] << temp1, temp2), 12) + 4;
                            i++;
                        } else {
                            throw new AssertionError();
                        }
                    }
                    mant_out[0] = mant[0];
                    exp_out[0] = exp[0];
                    xmaxc_out[this.xmaxcOffset] = xmaxc;
                    return;
                } else {
                    throw new AssertionError();
                }
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    private void APCM_quantization_xmaxc_to_exp_mant(int xmaxc, int[] exp_out, int[] mant_out) {
        int exp = 0;
        if (xmaxc > 15) {
            exp = sasr(xmaxc, 3) - 1;
        }
        int mant = xmaxc - (exp << 3);
        if (mant == 0) {
            exp = -4;
            mant = 7;
        } else {
            while (mant <= 7) {
                mant = (mant << 1) | 1;
                exp--;
            }
            mant -= 8;
        }
        if (!$assertionsDisabled && (exp < -4 || exp > 6)) {
            throw new AssertionError();
        } else if ($assertionsDisabled || (mant >= 0 && mant <= 7)) {
            exp_out[0] = exp;
            mant_out[0] = mant;
        } else {
            throw new AssertionError();
        }
    }

    /* JADX WARNING: Missing block: B:25:0x0058, code skipped:
            throw new java.lang.AssertionError();
     */
    private void APCMInverseQuantization(int[] r13, int r14, int r15, int[] r16) {
        /*
        r12 = this;
        r8 = 0;
        r10 = $assertionsDisabled;
        if (r10 != 0) goto L_0x0010;
    L_0x0005:
        if (r15 < 0) goto L_0x000a;
    L_0x0007:
        r10 = 7;
        if (r15 <= r10) goto L_0x0010;
    L_0x000a:
        r10 = new java.lang.AssertionError;
        r10.<init>();
        throw r10;
    L_0x0010:
        r6 = r12.xmcOffset;
        r10 = FAC;
        r3 = r10[r15];
        r10 = 6;
        r4 = sub(r10, r14);
        r10 = 1;
        r11 = 1;
        r11 = sub(r4, r11);
        r5 = asl(r10, r11);
        r0 = 13;
        r1 = r0;
        r7 = r6;
        r9 = r8;
    L_0x002a:
        r0 = r1 + -1;
        if (r1 <= 0) goto L_0x006f;
    L_0x002e:
        r10 = $assertionsDisabled;
        if (r10 != 0) goto L_0x0041;
    L_0x0032:
        r10 = r13[r7];
        r11 = 7;
        if (r10 > r11) goto L_0x003b;
    L_0x0037:
        r10 = r13[r7];
        if (r10 >= 0) goto L_0x0041;
    L_0x003b:
        r10 = new java.lang.AssertionError;
        r10.<init>();
        throw r10;
    L_0x0041:
        r6 = r7 + 1;
        r10 = r13[r7];
        r10 = r10 << 1;
        r2 = r10 + -7;
        r10 = $assertionsDisabled;
        if (r10 != 0) goto L_0x0059;
    L_0x004d:
        r10 = 7;
        if (r2 > r10) goto L_0x0053;
    L_0x0050:
        r10 = -7;
        if (r2 >= r10) goto L_0x0059;
    L_0x0053:
        r10 = new java.lang.AssertionError;
        r10.<init>();
        throw r10;
    L_0x0059:
        r2 = r2 << 12;
        r2 = mult_r(r3, r2);
        r2 = add(r2, r5);
        r8 = r9 + 1;
        r10 = asr(r2, r4);
        r16[r9] = r10;
        r1 = r0;
        r7 = r6;
        r9 = r8;
        goto L_0x002a;
    L_0x006f:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.rubycoder.gsm.GSMEncoder.APCMInverseQuantization(int[], int, int, int[]):void");
    }

    private void Autocorrelation(int[] s, int[] l_acf) {
        int si = this.dOffset;
        if ($assertionsDisabled || this.dOffset == 0) {
            int k;
            int scalauto;
            int j;
            int smax = 0;
            for (k = 0; k <= 159; k++) {
                int temp = abs(s[si + k]);
                if (temp > smax) {
                    smax = temp;
                }
            }
            if (smax == 0) {
                scalauto = 0;
            } else if ($assertionsDisabled || smax > 0) {
                scalauto = 4 - gsm_norm(smax << 16);
            } else {
                throw new AssertionError();
            }
            if (scalauto > 0) {
                switch (scalauto) {
                    case 1:
                        for (k = 0; k <= 159; k++) {
                            s[k] = mult_r(s[k], 16384);
                        }
                        break;
                    case 2:
                        for (k = 0; k <= 159; k++) {
                            s[k] = mult_r(s[k], 8192);
                        }
                        break;
                    case 3:
                        for (k = 0; k <= 159; k++) {
                            s[k] = mult_r(s[k], 4096);
                        }
                        break;
                    case 4:
                        for (k = 0; k <= 159; k++) {
                            s[k] = mult_r(s[k], 2048);
                        }
                        break;
                }
            }
            int spi = 0;
            int sl = s[0];
            for (k = 9; k > 0; k--) {
                l_acf[k - 1] = 0;
            }
            for (j = 0; j < 8; j++) {
                for (int x = 0; x <= j; x++) {
                    l_acf[x] = l_acf[x] + (s[spi - x] * sl);
                }
                if (j < 7) {
                    spi++;
                    sl = s[spi];
                }
            }
            for (int i = 8; i <= 159; i++) {
                spi++;
                sl = s[spi];
                for (j = 0; j <= 8; j++) {
                    l_acf[j] = l_acf[j] + (s[spi - j] * sl);
                }
            }
            for (k = 9; k > 0; k--) {
                int i2 = k - 1;
                l_acf[i2] = l_acf[i2] << 1;
            }
            if (scalauto <= 0) {
                return;
            }
            if ($assertionsDisabled || scalauto <= 4) {
                k = 160;
                int si2 = si;
                while (k > 0) {
                    si = si2 + 1;
                    s[si2] = s[si2] << scalauto;
                    k--;
                    si2 = si;
                }
                si = si2;
                return;
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    private void Calculation_of_the_LTP_parameters(int[] d, int[] dp, int[] bc_out, int[] nc_out) {
        int k;
        int temp;
        int scal;
        int[] wt = new int[40];
        int dmax = 0;
        for (k = 0; k < 40; k++) {
            temp = abs(d[this.dOffset + k]);
            if (temp > dmax) {
                dmax = temp;
            }
        }
        temp = 0;
        if (dmax != 0) {
            if ($assertionsDisabled || dmax > 0) {
                temp = gsm_norm(dmax << 16);
            } else {
                throw new AssertionError();
            }
        }
        if (temp > 6) {
            scal = 0;
        } else {
            scal = 6 - temp;
        }
        if ($assertionsDisabled || scal >= 0) {
            for (k = 0; k < 40; k++) {
                wt[k] = sasr(d[this.dOffset + k], scal);
            }
            int L_max = 0;
            int Nc = 40;
            for (int lambda = 40; lambda <= 120; lambda++) {
                int L_result = wt[0] * dp[this.dpOffset - lambda];
                for (int i = 1; i < 40; i++) {
                    L_result += wt[i] * dp[(this.dpOffset + i) - lambda];
                }
                if (L_result > L_max) {
                    Nc = lambda;
                    L_max = L_result;
                }
            }
            nc_out[this.ncOffset] = Nc;
            L_max <<= 1;
            if ($assertionsDisabled || (scal <= 100 && scal >= -100)) {
                L_max >>= 6 - scal;
                if ($assertionsDisabled || (Nc <= 120 && Nc >= 40)) {
                    int L_power = 0;
                    for (k = 0; k <= 39; k++) {
                        int L_temp = sasr(dp[(this.dpOffset + k) - Nc], 3);
                        L_power += L_temp * L_temp;
                    }
                    L_power <<= 1;
                    if (L_max <= 0) {
                        bc_out[this.bcOffset] = 0;
                        return;
                    } else if (L_max >= L_power) {
                        bc_out[this.bcOffset] = 3;
                        return;
                    } else {
                        temp = gsm_norm(L_power);
                        int R = sasr(L_max << temp, 16);
                        int S = sasr(L_power << temp, 16);
                        int bc = 0;
                        while (bc <= 2 && R > gsm_mult(S, this.gsm_DLB[bc])) {
                            bc++;
                        }
                        bc_out[this.bcOffset] = bc;
                        return;
                    }
                }
                throw new AssertionError();
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    private void Coefficients_0_12(int[] LARpp_j_1, int[] LARpp_j, int[] LARp) {
        for (int i = 0; i < 8; i++) {
            LARp[i] = add(sasr(LARpp_j_1[i], 2), sasr(LARpp_j[i], 2));
            LARp[i] = add(LARp[i], sasr(LARpp_j_1[i], 1));
        }
    }

    private void Coefficients_13_26(int[] LARpp_j_1, int[] LARpp_j, int[] LARp) {
        for (int i = 0; i < 8; i++) {
            LARp[i] = add(sasr(LARpp_j_1[i], 1), sasr(LARpp_j[i], 1));
        }
    }

    private void Coefficients_27_39(int[] LARpp_j_1, int[] LARpp_j, int[] LARp) {
        for (int i = 0; i < 8; i++) {
            LARp[i] = add(sasr(LARpp_j_1[i], 2), sasr(LARpp_j[i], 2));
            LARp[i] = add(LARp[i], sasr(LARpp_j[i], 1));
        }
    }

    private void DecodingOfTheCodedLogAreaRatios(int[] larc, int[] larpp) {
        int larci = 0 + 1;
        int temp1 = mult_r(13107, sub(add(larc[0], -32) << 10, 0));
        int i = 0 + 1;
        larpp[0] = add(temp1, temp1);
        int larci2 = larci + 1;
        temp1 = mult_r(13107, sub(add(larc[larci], -32) << 10, 0));
        int i2 = i + 1;
        larpp[i] = add(temp1, temp1);
        larci = larci2 + 1;
        temp1 = mult_r(13107, sub(add(larc[larci2], -16) << 10, 4096));
        i = i2 + 1;
        larpp[i2] = add(temp1, temp1);
        larci2 = larci + 1;
        temp1 = mult_r(13107, sub(add(larc[larci], -16) << 10, -5120));
        i2 = i + 1;
        larpp[i] = add(temp1, temp1);
        larci = larci2 + 1;
        temp1 = mult_r(19223, sub(add(larc[larci2], -8) << 10, 188));
        i = i2 + 1;
        larpp[i2] = add(temp1, temp1);
        larci2 = larci + 1;
        temp1 = mult_r(17476, sub(add(larc[larci], -8) << 10, -3584));
        i2 = i + 1;
        larpp[i] = add(temp1, temp1);
        larci = larci2 + 1;
        temp1 = mult_r(31454, sub(add(larc[larci2], -4) << 10, -682));
        i = i2 + 1;
        larpp[i2] = add(temp1, temp1);
        larci2 = larci + 1;
        temp1 = mult_r(29708, sub(add(larc[larci], -4) << 10, -2288));
        i2 = i + 1;
        larpp[i] = add(temp1, temp1);
    }

    public final void encode(byte[] c, int[] s) {
        LARc = new int[8];
        int[] Nc = new int[4];
        int[] Mc = new int[4];
        int[] bc = new int[4];
        xmaxc = new int[4];
        int[] xmc = new int[52];
        encoder(s, LARc, Nc, bc, Mc, xmaxc, xmc);
        int i = 0 + 1;
        c[0] = (byte) (((LARc[0] >> 2) & 15) | 208);
        int i2 = i + 1;
        c[i] = (byte) (((LARc[0] & 3) << 6) | (LARc[1] & 63));
        i = i2 + 1;
        c[i2] = (byte) (((LARc[2] & 31) << 3) | ((LARc[3] >> 2) & 7));
        i2 = i + 1;
        c[i] = (byte) ((((LARc[3] & 3) << 6) | ((LARc[4] & 15) << 2)) | ((LARc[5] >> 2) & 3));
        i = i2 + 1;
        c[i2] = (byte) ((((LARc[5] & 3) << 6) | ((LARc[6] & 7) << 3)) | (LARc[7] & 7));
        i2 = i + 1;
        c[i] = (byte) (((Nc[0] & 127) << 1) | ((bc[0] >>> 1) & 1));
        i = i2 + 1;
        c[i2] = (byte) ((((bc[0] & 1) << 7) | ((Mc[0] & 3) << 5)) | ((xmaxc[0] >> 1) & 31));
        i2 = i + 1;
        c[i] = (byte) (((((xmaxc[0] & 1) << 7) | ((xmc[0] & 7) << 4)) | ((xmc[1] & 7) << 1)) | ((xmc[2] >> 2) & 1));
        i = i2 + 1;
        c[i2] = (byte) ((((xmc[2] & 3) << 6) | ((xmc[3] & 7) << 3)) | (xmc[4] & 7));
        i2 = i + 1;
        c[i] = (byte) ((((xmc[5] & 7) << 5) | ((xmc[6] & 7) << 2)) | ((xmc[7] >> 1) & 3));
        i = i2 + 1;
        c[i2] = (byte) (((((xmc[7] & 1) << 7) | ((xmc[8] & 7) << 4)) | ((xmc[9] & 7) << 1)) | ((xmc[10] >> 2) & 1));
        i2 = i + 1;
        c[i] = (byte) ((((xmc[10] & 3) << 6) | ((xmc[11] & 7) << 3)) | (xmc[12] & 7));
        i = i2 + 1;
        c[i2] = (byte) (((Nc[1] & 127) << 1) | ((bc[1] >> 1) & 1));
        i2 = i + 1;
        c[i] = (byte) ((((bc[1] & 1) << 7) | ((Mc[1] & 3) << 5)) | ((xmaxc[1] >> 1) & 31));
        i = i2 + 1;
        c[i2] = (byte) (((((xmaxc[1] & 1) << 7) | ((xmc[13] & 7) << 4)) | ((xmc[14] & 7) << 1)) | ((xmc[15] >> 2) & 1));
        i2 = i + 1;
        c[i] = (byte) ((((xmc[15] & 3) << 6) | ((xmc[16] & 7) << 3)) | (xmc[17] & 7));
        i = i2 + 1;
        c[i2] = (byte) ((((xmc[18] & 7) << 5) | ((xmc[19] & 7) << 2)) | ((xmc[20] >> 1) & 3));
        i2 = i + 1;
        c[i] = (byte) (((((xmc[20] & 1) << 7) | ((xmc[21] & 7) << 4)) | ((xmc[22] & 7) << 1)) | ((xmc[23] >> 2) & 1));
        i = i2 + 1;
        c[i2] = (byte) ((((xmc[23] & 3) << 6) | ((xmc[24] & 7) << 3)) | (xmc[25] & 7));
        i2 = i + 1;
        c[i] = (byte) (((Nc[2] & 127) << 1) | ((bc[2] >> 1) & 1));
        i = i2 + 1;
        c[i2] = (byte) ((((bc[2] & 1) << 7) | ((Mc[2] & 3) << 5)) | ((xmaxc[2] >> 1) & 31));
        i2 = i + 1;
        c[i] = (byte) (((((xmaxc[2] & 1) << 7) | ((xmc[26] & 7) << 4)) | ((xmc[27] & 7) << 1)) | ((xmc[28] >> 2) & 1));
        i = i2 + 1;
        c[i2] = (byte) ((((xmc[28] & 3) << 6) | ((xmc[29] & 7) << 3)) | (xmc[30] & 7));
        i2 = i + 1;
        c[i] = (byte) ((((xmc[31] & 7) << 5) | ((xmc[32] & 7) << 2)) | ((xmc[33] >> 1) & 3));
        i = i2 + 1;
        c[i2] = (byte) (((((xmc[33] & 1) << 7) | ((xmc[34] & 7) << 4)) | ((xmc[35] & 7) << 1)) | ((xmc[36] >> 2) & 1));
        i2 = i + 1;
        c[i] = (byte) ((((xmc[36] & 3) << 6) | ((xmc[37] & 7) << 3)) | (xmc[38] & 7));
        i = i2 + 1;
        c[i2] = (byte) (((Nc[3] & 127) << 1) | ((bc[3] >> 1) & 1));
        i2 = i + 1;
        c[i] = (byte) ((((bc[3] & 1) << 7) | ((Mc[3] & 3) << 5)) | ((xmaxc[3] >> 1) & 31));
        i = i2 + 1;
        c[i2] = (byte) (((((xmaxc[3] & 1) << 7) | ((xmc[39] & 7) << 4)) | ((xmc[40] & 7) << 1)) | ((xmc[41] >> 2) & 1));
        i2 = i + 1;
        c[i] = (byte) ((((xmc[41] & 3) << 6) | ((xmc[42] & 7) << 3)) | (xmc[43] & 7));
        i = i2 + 1;
        c[i2] = (byte) ((((xmc[44] & 7) << 5) | ((xmc[45] & 7) << 2)) | ((xmc[46] >> 1) & 3));
        i2 = i + 1;
        c[i] = (byte) (((((xmc[46] & 1) << 7) | ((xmc[47] & 7) << 4)) | ((xmc[48] & 7) << 1)) | ((xmc[49] >> 2) & 1));
        i = i2 + 1;
        c[i2] = (byte) ((((xmc[49] & 3) << 6) | ((xmc[50] & 7) << 3)) | (xmc[51] & 7));
    }

    /* access modifiers changed from: final */
    public final int[] encode(int[] s) {
        encode(new byte[33], s);
        return s;
    }

    private void encoder(int[] s, int[] LARc, int[] nc, int[] bc, int[] mc, int[] xmaxc, int[] xmc) {
        int[] dp = this.dp0;
        int[] dpp = this.dp0;
        int[] so = new int[160];
        this.dpOffset = WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18;
        this.dppOffset = WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18;
        this.dOffset = 0;
        this.ncOffset = 0;
        this.bcOffset = 0;
        this.eOffset = 0;
        this.xmaxcOffset = 0;
        this.xmcOffset = 0;
        this.mcoffset = 0;
        GsmPreprocess(s, so);
        GsmLPCAnalysis(so, LARc);
        Gsm_Short_Term_Analysis_Filter(LARc, so);
        int k = 0;
        while (k <= 3) {
            this.dOffset = k * 40;
            this.eOffset = 5;
            Gsm_Long_Term_Predictor(so, dp, this.e, dpp, nc, bc);
            Gsm_RPE_Encoding(this.e, xmaxc, mc, xmc);
            for (int i = 0; i <= 39; i++) {
                dp[this.dpOffset + i] = add(this.e[i + 5], dpp[this.dppOffset + i]);
            }
            this.dpOffset += 40;
            this.dppOffset += 40;
            this.ncOffset++;
            this.bcOffset++;
            this.xmaxcOffset++;
            this.mcoffset++;
            k++;
            this.xmcOffset += 13;
        }
        System.arraycopy(this.dp0, 0, this.dp0, 160, WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18);
    }

    public void GSM() {
    }

    private int gsm_div(int num, int denum) {
        int L_num = num;
        int div = 0;
        int k = 15;
        if ($assertionsDisabled || (num >= 0 && denum >= num)) {
            if (num == 0) {
                return 0;
            }
            while (true) {
                while (true) {
                    int i = k;
                    int div2 = div;
                    k = i - 1;
                    if (i > 0) {
                        div = div2 << 1;
                        L_num <<= 1;
                        if (L_num >= denum) {
                            L_num -= denum;
                            div++;
                        }
                    } else {
                        div = div2;
                        return div2;
                    }
                }
            }
        }
        throw new AssertionError();
    }

    private void Gsm_Long_Term_Predictor(int[] d, int[] dp, int[] e, int[] dpp, int[] nc, int[] bc) {
        Calculation_of_the_LTP_parameters(d, dp, bc, nc);
        Long_term_analysis_filtering(bc[this.bcOffset], nc[this.ncOffset], dp, d, dpp, e);
    }

    /* access modifiers changed from: 0000 */
    public int gsm_mult(int a, int b) {
        if (a == MIN_WORD && b == MIN_WORD) {
            return 32767;
        }
        return (a * b) >> 15;
    }

    private int gsm_norm(int a) {
        if ($assertionsDisabled || a != 0) {
            if (a < 0) {
                if (a <= -1073741824) {
                    return 0;
                }
                a ^= -1;
            }
            return (-65536 & a) != 0 ? (-16777216 & a) != 0 ? bitoff[(a >> 24) & UnsignedUtils.MAX_UBYTE] - 1 : bitoff[(a >> 16) & UnsignedUtils.MAX_UBYTE] + 7 : (65280 & a) != 0 ? bitoff[(a >> 8) & UnsignedUtils.MAX_UBYTE] + 15 : bitoff[a & UnsignedUtils.MAX_UBYTE] + 23;
        } else {
            throw new AssertionError();
        }
    }

    private void Gsm_RPE_Encoding(int[] e, int[] xmaxc, int[] mc, int[] xmc) {
        int[] x = new int[40];
        int[] xM = new int[13];
        int[] xMp = new int[13];
        int[] mant = new int[1];
        int[] exp = new int[1];
        Weighting_filter(e, x);
        RPE_grid_selection(x, xM, mc);
        APCM_quantization(xM, xmc, mant, exp, xmaxc);
        APCMInverseQuantization(xmc, exp[0], mant[0], xMp);
        RPE_grid_positioning(mc[this.mcoffset], xMp, e);
    }

    private void Gsm_Short_Term_Analysis_Filter(int[] larc, int[] s) {
        int[] LARpp_j = this.LARpp[this.j];
        int[][] iArr = this.LARpp;
        int i = this.j ^ 1;
        this.j = i;
        int[] LARpp_j_1 = iArr[i];
        int[] larp = new int[8];
        DecodingOfTheCodedLogAreaRatios(larc, LARpp_j);
        Coefficients_0_12(LARpp_j_1, LARpp_j, larp);
        LARp_to_rp(larp);
        Short_term_analysis_filtering(larp, 13, s, 0);
        Coefficients_13_26(LARpp_j_1, LARpp_j, larp);
        LARp_to_rp(larp);
        Short_term_analysis_filtering(larp, 14, s, 13);
        Coefficients_27_39(LARpp_j_1, LARpp_j, larp);
        LARp_to_rp(larp);
        Short_term_analysis_filtering(larp, 13, s, 27);
        Coefficients_40_159(LARpp_j, larp);
        LARp_to_rp(larp);
        Short_term_analysis_filtering(larp, WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18, s, 40);
    }

    private void GsmLPCAnalysis(int[] s, int[] LARc) {
        int[] L_ACF = new int[9];
        Autocorrelation(s, L_ACF);
        Reflection_coefficients(L_ACF, LARc);
        Transformation_to_Log_Area_Ratios(LARc);
        Quantization_and_coding(LARc);
    }

    /* access modifiers changed from: final */
    public final void GsmPreprocess(int[] s, int[] so) {
        int k = 160;
        int si = 0;
        int i = 0;
        while (true) {
            int i2 = i;
            int k2 = k;
            k = k2 - 1;
            if (k2 > 0) {
                int SO = sasr(s[si], 3) << 2;
                si++;
                if (!$assertionsDisabled && SO < -16384) {
                    throw new AssertionError();
                } else if ($assertionsDisabled || SO <= 16380) {
                    int s1 = SO - this.z1;
                    this.z1 = SO;
                    if ($assertionsDisabled || s1 != MIN_WORD) {
                        int L_s2 = s1 << 15;
                        int msp = sasr(this.L_z2, 15);
                        this.L_z2 = (int) l_add(msp * 32735, L_s2 + mult_r(this.L_z2 - (msp << 15), 32735));
                        int L_temp = (int) l_add(this.L_z2, 16384);
                        msp = mult_r(this.mp, -28180);
                        this.mp = sasr(L_temp, 15);
                        i = i2 + 1;
                        so[i2] = add(this.mp, msp);
                    } else {
                        throw new AssertionError();
                    }
                } else {
                    throw new AssertionError();
                }
            }
            return;
        }
    }

    private long l_add(int a, int b) {
        long utmp;
        if (a < 0) {
            if (b >= 0) {
                return (long) (a + b);
            }
            utmp = ((long) (-(a + 1))) + ((long) (-(b + 1)));
            return utmp >= 2147483647L ? -2147483648L : (-utmp) - 2;
        } else if (b <= 0) {
            return (long) (a + b);
        } else {
            utmp = ((long) a) + ((long) b);
            return utmp >= 2147483647L ? 2147483647L : utmp;
        }
    }

    /* access modifiers changed from: 0000 */
    public void Long_term_analysis_filtering(int bc, int nc, int[] dp, int[] d, int[] dpp, int[] e) {
        int k;
        switch (bc) {
            case 0:
                for (k = 0; k <= 39; k++) {
                    dpp[this.dppOffset + k] = mult_r(3277, dp[(this.dpOffset + k) - nc]);
                    e[this.eOffset + k] = sub(d[this.dOffset + k], dpp[this.dppOffset + k]);
                }
                return;
            case 1:
                for (k = 0; k <= 39; k++) {
                    dpp[this.dppOffset + k] = mult_r(11469, dp[(this.dpOffset + k) - nc]);
                    e[this.eOffset + k] = sub(d[this.dOffset + k], dpp[this.dppOffset + k]);
                }
                return;
            case 2:
                for (k = 0; k <= 39; k++) {
                    dpp[this.dppOffset + k] = mult_r(21299, dp[(this.dpOffset + k) - nc]);
                    e[this.eOffset + k] = sub(d[this.dOffset + k], dpp[this.dppOffset + k]);
                }
                return;
            case 3:
                for (k = 0; k <= 39; k++) {
                    dpp[this.dppOffset + k] = mult_r(32767, dp[(this.dpOffset + k) - nc]);
                    e[this.eOffset + k] = sub(d[this.dOffset + k], dpp[this.dppOffset + k]);
                }
                return;
            default:
                return;
        }
    }

    private void Quantization_and_coding(int[] lar) {
        int i = 15;
        int i2 = 7;
        int temp = sasr(add(add(gsm_mult(20480, lar[0]), 0), 256), 9);
        int i3 = temp > 31 ? 63 : temp < -32 ? 0 : temp + 32;
        lar[0] = i3;
        int lari = 0 + 1;
        temp = sasr(add(add(gsm_mult(20480, lar[lari]), 0), 256), 9);
        i3 = temp > 31 ? 63 : temp < -32 ? 0 : temp + 32;
        lar[lari] = i3;
        lari++;
        temp = sasr(add(add(gsm_mult(20480, lar[lari]), 2048), 256), 9);
        i3 = temp > 15 ? 31 : temp < -16 ? 0 : temp + 16;
        lar[lari] = i3;
        lari++;
        temp = sasr(add(add(gsm_mult(20480, lar[lari]), -2560), 256), 9);
        i3 = temp > 15 ? 31 : temp < -16 ? 0 : temp + 16;
        lar[lari] = i3;
        lari++;
        temp = sasr(add(add(gsm_mult(13964, lar[lari]), 94), 256), 9);
        i3 = temp > 7 ? 15 : temp < -8 ? 0 : temp + 8;
        lar[lari] = i3;
        lari++;
        temp = sasr(add(add(gsm_mult(15360, lar[lari]), -1792), 256), 9);
        if (temp <= 7) {
            i = temp < -8 ? 0 : temp + 8;
        }
        lar[lari] = i;
        lari++;
        temp = sasr(add(add(gsm_mult(8534, lar[lari]), -341), 256), 9);
        i3 = temp > 3 ? 7 : temp < -4 ? 0 : temp + 4;
        lar[lari] = i3;
        lari++;
        temp = sasr(add(add(gsm_mult(9036, lar[lari]), -1144), 256), 9);
        if (temp <= 3) {
            i2 = temp < -4 ? 0 : temp + 4;
        }
        lar[lari] = i2;
        lari++;
    }

    private void Reflection_coefficients(int[] l_acf, int[] r) {
        int ri = 0;
        int[] ACF = new int[9];
        int[] P = new int[9];
        int[] K = new int[9];
        int i;
        int ri2;
        if (l_acf[0] == 0) {
            i = 8;
            ri2 = 0;
            while (i > 0) {
                ri = ri2 + 1;
                r[ri2] = 0;
                i--;
                ri2 = ri;
            }
            ri = ri2;
        } else if ($assertionsDisabled || l_acf[0] != 0) {
            int temp = gsm_norm(l_acf[0]);
            if ($assertionsDisabled || (temp >= 0 && temp < 32)) {
                for (i = 0; i <= 8; i++) {
                    ACF[i] = sasr(l_acf[i] << temp, 16);
                }
                for (i = 1; i <= 7; i++) {
                    K[i] = ACF[i];
                }
                for (i = 0; i <= 8; i++) {
                    P[i] = ACF[i];
                }
                int n = 1;
                while (n <= 8) {
                    temp = abs(P[1]);
                    if (P[0] < temp) {
                        i = n;
                        while (true) {
                            ri2 = ri;
                            if (i <= 8) {
                                ri = ri2 + 1;
                                r[ri2] = 0;
                                i++;
                            } else {
                                ri = ri2;
                                return;
                            }
                        }
                    }
                    r[ri] = gsm_div(temp, P[0]);
                    if ($assertionsDisabled || r[ri] >= 0) {
                        if (P[1] > 0) {
                            r[ri] = -r[ri];
                        }
                        if (!$assertionsDisabled && r[ri] == MIN_WORD) {
                            throw new AssertionError();
                        } else if (n != 8) {
                            P[0] = add(P[0], mult_r(P[1], r[ri]));
                            for (int m = 1; m <= 8 - n; m++) {
                                P[m] = add(P[m + 1], mult_r(K[m], r[ri]));
                                K[m] = add(K[m], mult_r(P[m + 1], r[ri]));
                            }
                            n++;
                            ri++;
                        } else {
                            return;
                        }
                    }
                    throw new AssertionError();
                }
                return;
            }
            throw new AssertionError();
        } else {
            throw new AssertionError();
        }
    }

    /* JADX WARNING: Missing block: B:2:0x0009, code skipped:
            r1 = r0 + 1;
            r10[r0] = 0;
            r0 = r1 + 1;
            r10[r1] = 0;
            r1 = r0 + 1;
            r4 = r3 + 1;
            r10[r0] = r9[r3];
            r2 = r2 - 1;
     */
    /* JADX WARNING: Missing block: B:3:0x001b, code skipped:
            if (r2 > 0) goto L_0x0045;
     */
    /* JADX WARNING: Missing block: B:4:0x001d, code skipped:
            r8 = r8 + 1;
     */
    /* JADX WARNING: Missing block: B:5:0x0020, code skipped:
            if (r8 >= 4) goto L_0x0044;
     */
    /* JADX WARNING: Missing block: B:6:0x0022, code skipped:
            r0 = r1 + 1;
            r10[r1] = 0;
            r1 = r0;
     */
    /* JADX WARNING: Missing block: B:8:0x002d, code skipped:
            r1 = r0 + 1;
            r10[r0] = 0;
            r0 = r1;
     */
    /* JADX WARNING: Missing block: B:9:0x0032, code skipped:
            r1 = r0 + 1;
            r10[r0] = 0;
            r0 = r1;
     */
    /* JADX WARNING: Missing block: B:10:0x0037, code skipped:
            r1 = r0 + 1;
            r4 = 0 + 1;
            r10[r0] = r9[0];
            r2 = 13 - 1;
            r3 = r4;
            r0 = r1;
     */
    /* JADX WARNING: Missing block: B:11:0x0044, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:12:0x0045, code skipped:
            r3 = r4;
            r0 = r1;
     */
    private void RPE_grid_positioning(int r8, int[] r9, int[] r10) {
        /*
        r7 = this;
        r6 = 0;
        r2 = 13;
        r0 = r7.eOffset;
        r3 = 0;
        switch(r8) {
            case 0: goto L_0x0037;
            case 1: goto L_0x0032;
            case 2: goto L_0x002d;
            case 3: goto L_0x0028;
            default: goto L_0x0009;
        };
    L_0x0009:
        r1 = r0 + 1;
        r10[r0] = r6;
        r0 = r1 + 1;
        r10[r1] = r6;
        r1 = r0 + 1;
        r4 = r3 + 1;
        r5 = r9[r3];
        r10[r0] = r5;
        r2 = r2 + -1;
        if (r2 > 0) goto L_0x0045;
    L_0x001d:
        r8 = r8 + 1;
        r5 = 4;
        if (r8 >= r5) goto L_0x0044;
    L_0x0022:
        r0 = r1 + 1;
        r10[r1] = r6;
        r1 = r0;
        goto L_0x001d;
    L_0x0028:
        r1 = r0 + 1;
        r10[r0] = r6;
        r0 = r1;
    L_0x002d:
        r1 = r0 + 1;
        r10[r0] = r6;
        r0 = r1;
    L_0x0032:
        r1 = r0 + 1;
        r10[r0] = r6;
        r0 = r1;
    L_0x0037:
        r1 = r0 + 1;
        r4 = r3 + 1;
        r5 = r9[r3];
        r10[r0] = r5;
        r2 = r2 + -1;
        r3 = r4;
        r0 = r1;
        goto L_0x0009;
    L_0x0044:
        return;
    L_0x0045:
        r3 = r4;
        r0 = r1;
        goto L_0x0009;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.rubycoder.gsm.GSMEncoder.RPE_grid_positioning(int, int[], int[]):void");
    }

    private void RPE_grid_selection(int[] x, int[] xM, int[] Mc_out) {
        int j;
        int L_temp;
        int Mc = 0;
        int L_result = 0;
        for (j = 1; j <= 12; j++) {
            L_temp = sasr(x[j * 3], 2);
            L_result += L_temp * L_temp;
        }
        int L_common_0_3 = L_result;
        L_temp = sasr(x[0], 2);
        int EM = (L_result + (L_temp * L_temp)) << 1;
        L_result = 0;
        for (j = 0; j <= 12; j++) {
            L_temp = sasr(x[(j * 3) + 1], 2);
            L_result += L_temp * L_temp;
        }
        L_result <<= 1;
        if (L_result > EM) {
            Mc = 1;
            EM = L_result;
        }
        L_result = 0;
        for (j = 0; j <= 12; j++) {
            L_temp = sasr(x[(j * 3) + 2], 2);
            L_result += L_temp * L_temp;
        }
        L_result <<= 1;
        if (L_result > EM) {
            Mc = 2;
            EM = L_result;
        }
        L_result = L_common_0_3;
        L_temp = sasr(x[39], 2);
        L_result = (L_result + (L_temp * L_temp)) << 1;
        if (L_result > EM) {
            Mc = 3;
            EM = L_result;
        }
        for (int i = 0; i <= 12; i++) {
            xM[i] = x[(i * 3) + Mc];
        }
        Mc_out[this.mcoffset] = Mc;
    }

    private int sasr(int x, int by) {
        return x >= 0 ? x >> by : ((-(x + 1)) >> by) ^ -1;
    }

    private void Short_term_analysis_filtering(int[] rp, int k_n, int[] s, int offset) {
        int si = offset;
        while (true) {
            int k_n2 = k_n;
            k_n = k_n2 - 1;
            if (k_n2 > 0) {
                int sav = s[si];
                int di = sav;
                for (int i = 0; i < 8; i++) {
                    int ui = this.u[i];
                    int rpi = rp[i];
                    this.u[i] = sav;
                    sav = add(ui, mult_r(rpi, di));
                    di = add(di, mult_r(rpi, ui));
                }
                s[si] = di;
                si++;
            } else {
                return;
            }
        }
    }

    private void Transformation_to_Log_Area_Ratios(int[] r) {
        int ri = 0;
        int i = 1;
        while (i <= 8) {
            int temp = abs(r[ri]);
            if ($assertionsDisabled || temp >= 0) {
                if (temp < 22118) {
                    temp >>= 1;
                } else if (temp < 31130) {
                    if ($assertionsDisabled || temp >= 11059) {
                        temp -= 11059;
                    } else {
                        throw new AssertionError();
                    }
                } else if ($assertionsDisabled || temp >= 26112) {
                    temp = (temp - 26112) << 2;
                } else {
                    throw new AssertionError();
                }
                if (r[ri] < 0) {
                    temp = -temp;
                }
                r[ri] = temp;
                if ($assertionsDisabled || r[ri] != MIN_WORD) {
                    i++;
                    ri++;
                } else {
                    throw new AssertionError();
                }
            }
            throw new AssertionError();
        }
    }

    private void Weighting_filter(int[] e, int[] x) {
        int ei = this.eOffset - 5;
        for (int k = 0; k <= 39; k++) {
            int L_result = sasr(4096 + (((((((((e[ei + k] * -134) + (e[(ei + k) + 1] * -374)) + (e[(ei + k) + 3] * 2054)) + (e[(ei + k) + 4] * 5741)) + (e[(ei + k) + 5] * 8192)) + (e[(ei + k) + 6] * 5741)) + (e[(ei + k) + 7] * 2054)) + (e[(ei + k) + 9] * -374)) + (e[(ei + k) + 10] * -134)), 13);
            if (L_result < MIN_WORD) {
                L_result = MIN_WORD;
            } else if (L_result > 32767) {
                L_result = 32767;
            }
            x[k] = L_result;
        }
    }
}
