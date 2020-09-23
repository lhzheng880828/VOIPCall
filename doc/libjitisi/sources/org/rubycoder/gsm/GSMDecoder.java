package org.rubycoder.gsm;

import com.sun.media.format.WavAudioFormat;
import java.lang.reflect.Array;

public final class GSMDecoder {
    private static final int[] FAC = new int[]{18431, 20479, 22527, 24575, 26623, 28671, 30719, 32767};
    private static final byte GSM_MAGIC = (byte) 13;
    private static final int MAX_WORD = 32767;
    private static final int MIN_WORD = -32768;
    private static final int[] QLB = new int[]{3277, 11469, 21299, 32767};
    private final int[][] LARpp = ((int[][]) Array.newInstance(Integer.TYPE, new int[]{2, 8}));
    private final int[] dp0 = new int[280];
    private int j;
    private int msr;
    private int nrp;
    private int[] u = new int[8];
    private final int[] v = new int[9];

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

    private static void Coefficients_0_12(int[] LARpp_j_1, int[] LARpp_j, int[] LARp) {
        for (int i = 0; i < 8; i++) {
            LARp[i] = add(LARpp_j_1[i] >> 2, LARpp_j[i] >> 2);
            LARp[i] = add(LARp[i], LARpp_j_1[i] >> 1);
        }
    }

    private static void Coefficients_13_26(int[] LARpp_j_1, int[] LARpp_j, int[] LARp) {
        for (int i = 0; i < 8; i++) {
            LARp[i] = add(LARpp_j_1[i] >> 1, LARpp_j[i] >> 1);
        }
    }

    private static void Coefficients_27_39(int[] LARpp_j_1, int[] LARpp_j, int[] LARp) {
        for (int i = 0; i < 8; i++) {
            LARp[i] = add(LARpp_j_1[i] >> 2, LARpp_j[i] >> 2);
            LARp[i] = add(LARp[i], LARpp_j[i] >> 1);
        }
    }

    private static void Coefficients_40_159(int[] LARpp_j, int[] LARp) {
        for (int i = 0; i < 8; i++) {
            LARp[i] = LARpp_j[i];
        }
    }

    private static void decodingOfTheCodedLogAreaRatios(int[] LARc, int[] LARpp) {
        int temp1 = mult_r(13107, add(LARc[0], -32) << 10);
        LARpp[0] = add(temp1, temp1);
        temp1 = mult_r(13107, add(LARc[1], -32) << 10);
        LARpp[1] = add(temp1, temp1);
        temp1 = mult_r(13107, sub(add(LARc[2], -16) << 10, 4096));
        LARpp[2] = add(temp1, temp1);
        temp1 = mult_r(13107, sub(add(LARc[3], -16) << 10, -5120));
        LARpp[3] = add(temp1, temp1);
        temp1 = mult_r(19223, sub(add(LARc[4], -8) << 10, 188));
        LARpp[4] = add(temp1, temp1);
        temp1 = mult_r(17476, sub(add(LARc[5], -8) << 10, -3584));
        LARpp[5] = add(temp1, temp1);
        temp1 = mult_r(31454, sub(add(LARc[6], -4) << 10, -682));
        LARpp[6] = add(temp1, temp1);
        temp1 = mult_r(29708, sub(add(LARc[7], -4) << 10, -2288));
        LARpp[7] = add(temp1, temp1);
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

    /* JADX WARNING: Missing block: B:2:0x0008, code skipped:
            r1 = r0 + 1;
            r9[r0] = 0;
            r0 = r1 + 1;
            r9[r1] = 0;
            r1 = r0 + 1;
            r4 = r3 + 1;
            r9[r0] = r8[r3];
            r2 = r2 - 1;
     */
    /* JADX WARNING: Missing block: B:3:0x001a, code skipped:
            if (r2 > 0) goto L_0x0044;
     */
    /* JADX WARNING: Missing block: B:4:0x001c, code skipped:
            r7 = r7 + 1;
     */
    /* JADX WARNING: Missing block: B:5:0x001f, code skipped:
            if (r7 >= 4) goto L_0x0043;
     */
    /* JADX WARNING: Missing block: B:6:0x0021, code skipped:
            r0 = r1 + 1;
            r9[r1] = 0;
            r1 = r0;
     */
    /* JADX WARNING: Missing block: B:8:0x002c, code skipped:
            r1 = r0 + 1;
            r9[r0] = 0;
            r0 = r1;
     */
    /* JADX WARNING: Missing block: B:9:0x0031, code skipped:
            r1 = r0 + 1;
            r9[r0] = 0;
            r0 = r1;
     */
    /* JADX WARNING: Missing block: B:10:0x0036, code skipped:
            r1 = r0 + 1;
            r4 = 0 + 1;
            r9[r0] = r8[0];
            r2 = 13 - 1;
            r3 = r4;
            r0 = r1;
     */
    /* JADX WARNING: Missing block: B:11:0x0043, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:12:0x0044, code skipped:
            r3 = r4;
            r0 = r1;
     */
    private static void RPE_grid_positioning(int r7, int[] r8, int[] r9) {
        /*
        r6 = 0;
        r2 = 13;
        r0 = 0;
        r3 = 0;
        switch(r7) {
            case 0: goto L_0x0036;
            case 1: goto L_0x0031;
            case 2: goto L_0x002c;
            case 3: goto L_0x0027;
            default: goto L_0x0008;
        };
    L_0x0008:
        r1 = r0 + 1;
        r9[r0] = r6;
        r0 = r1 + 1;
        r9[r1] = r6;
        r1 = r0 + 1;
        r4 = r3 + 1;
        r5 = r8[r3];
        r9[r0] = r5;
        r2 = r2 + -1;
        if (r2 > 0) goto L_0x0044;
    L_0x001c:
        r7 = r7 + 1;
        r5 = 4;
        if (r7 >= r5) goto L_0x0043;
    L_0x0021:
        r0 = r1 + 1;
        r9[r1] = r6;
        r1 = r0;
        goto L_0x001c;
    L_0x0027:
        r1 = r0 + 1;
        r9[r0] = r6;
        r0 = r1;
    L_0x002c:
        r1 = r0 + 1;
        r9[r0] = r6;
        r0 = r1;
    L_0x0031:
        r1 = r0 + 1;
        r9[r0] = r6;
        r0 = r1;
    L_0x0036:
        r1 = r0 + 1;
        r4 = r3 + 1;
        r5 = r8[r3];
        r9[r0] = r5;
        r2 = r2 + -1;
        r3 = r4;
        r0 = r1;
        goto L_0x0008;
    L_0x0043:
        return;
    L_0x0044:
        r3 = r4;
        r0 = r1;
        goto L_0x0008;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.rubycoder.gsm.GSMDecoder.RPE_grid_positioning(int, int[], int[]):void");
    }

    private static int saturate(int x) {
        return x < MIN_WORD ? MIN_WORD : x > 32767 ? 32767 : x;
    }

    private static int sub(int a, int b) {
        return saturate(a - b);
    }

    private void APCMInverseQuantization(int[] xMc, int xMcOffset, int exp, int mant, int[] xMp) {
        int temp1 = FAC[mant];
        int temp2 = sub(6, exp);
        int temp3 = asl(1, sub(temp2, 1));
        int i = 0;
        int i2 = 13;
        while (true) {
            int i3 = i2;
            int i4 = i;
            int xMcOffset2 = xMcOffset;
            i2 = i3 - 1;
            if (i3 > 0) {
                xMcOffset = xMcOffset2 + 1;
                i = i4 + 1;
                xMp[i4] = asr(add(mult_r(temp1, ((xMc[xMcOffset2] << 1) - 7) << 12), temp3), temp2);
            } else {
                return;
            }
        }
    }

    public final int[] decode(byte[] c) throws InvalidGSMFrameException {
        int[] s = new int[160];
        decode(c, s);
        return s;
    }

    public final void decode(byte[] c, int[] s) throws InvalidGSMFrameException {
        if (c.length != 33) {
            throw new InvalidGSMFrameException();
        } else if (((c[0] >> 4) & 15) != 13) {
            throw new InvalidGSMFrameException();
        } else {
            LARc = new int[8];
            int[] Nc = new int[4];
            int[] Mc = new int[4];
            bc = new int[4];
            xmaxc = new int[4];
            xmc = new int[52];
            int i = 0 + 1;
            LARc[0] = (c[0] & 15) << 2;
            LARc[0] = LARc[0] | ((c[i] >> 6) & 3);
            int i2 = i + 1;
            LARc[1] = c[i] & 63;
            LARc[2] = (c[i2] >> 3) & 31;
            i = i2 + 1;
            LARc[3] = (c[i2] & 7) << 2;
            LARc[3] = LARc[3] | ((c[i] >> 6) & 3);
            LARc[4] = (c[i] >> 2) & 15;
            i2 = i + 1;
            LARc[5] = (c[i] & 3) << 2;
            LARc[5] = LARc[5] | ((c[i2] >> 6) & 3);
            LARc[6] = (c[i2] >> 3) & 7;
            i = i2 + 1;
            LARc[7] = c[i2] & 7;
            Nc[0] = (c[i] >> 1) & 127;
            i2 = i + 1;
            bc[0] = (c[i] & 1) << 1;
            bc[0] = bc[0] | ((c[i2] >> 7) & 1);
            Mc[0] = (c[i2] >> 5) & 3;
            i = i2 + 1;
            xmaxc[0] = (c[i2] & 31) << 1;
            xmaxc[0] = xmaxc[0] | ((c[i] >> 7) & 1);
            xmc[0] = (c[i] >> 4) & 7;
            xmc[1] = (c[i] >> 1) & 7;
            i2 = i + 1;
            xmc[2] = (c[i] & 1) << 2;
            xmc[2] = xmc[2] | ((c[i2] >> 6) & 3);
            xmc[3] = (c[i2] >> 3) & 7;
            i = i2 + 1;
            xmc[4] = c[i2] & 7;
            xmc[5] = (c[i] >> 5) & 7;
            xmc[6] = (c[i] >> 2) & 7;
            i2 = i + 1;
            xmc[7] = (c[i] & 3) << 1;
            xmc[7] = xmc[7] | ((c[i2] >> 7) & 1);
            xmc[8] = (c[i2] >> 4) & 7;
            xmc[9] = (c[i2] >> 1) & 7;
            i = i2 + 1;
            xmc[10] = (c[i2] & 1) << 2;
            xmc[10] = xmc[10] | ((c[i] >> 6) & 3);
            xmc[11] = (c[i] >> 3) & 7;
            i2 = i + 1;
            xmc[12] = c[i] & 7;
            Nc[1] = (c[i2] >> 1) & 127;
            i = i2 + 1;
            bc[1] = (c[i2] & 1) << 1;
            bc[1] = bc[1] | ((c[i] >> 7) & 1);
            Mc[1] = (c[i] >> 5) & 3;
            i2 = i + 1;
            xmaxc[1] = (c[i] & 31) << 1;
            xmaxc[1] = xmaxc[1] | ((c[i2] >> 7) & 1);
            xmc[13] = (c[i2] >> 4) & 7;
            xmc[14] = (c[i2] >> 1) & 7;
            i = i2 + 1;
            xmc[15] = (c[i2] & 1) << 2;
            xmc[15] = xmc[15] | ((c[i] >> 6) & 3);
            xmc[16] = (c[i] >> 3) & 7;
            i2 = i + 1;
            xmc[17] = c[i] & 7;
            xmc[18] = (c[i2] >> 5) & 7;
            xmc[19] = (c[i2] >> 2) & 7;
            i = i2 + 1;
            xmc[20] = (c[i2] & 3) << 1;
            xmc[20] = xmc[20] | ((c[i] >> 7) & 1);
            xmc[21] = (c[i] >> 4) & 7;
            xmc[22] = (c[i] >> 1) & 7;
            i2 = i + 1;
            xmc[23] = (c[i] & 1) << 2;
            xmc[23] = xmc[23] | ((c[i2] >> 6) & 3);
            xmc[24] = (c[i2] >> 3) & 7;
            i = i2 + 1;
            xmc[25] = c[i2] & 7;
            Nc[2] = (c[i] >> 1) & 127;
            i2 = i + 1;
            bc[2] = (c[i] & 1) << 1;
            bc[2] = bc[2] | ((c[i2] >> 7) & 1);
            Mc[2] = (c[i2] >> 5) & 3;
            i = i2 + 1;
            xmaxc[2] = (c[i2] & 31) << 1;
            xmaxc[2] = xmaxc[2] | ((c[i] >> 7) & 1);
            xmc[26] = (c[i] >> 4) & 7;
            xmc[27] = (c[i] >> 1) & 7;
            i2 = i + 1;
            xmc[28] = (c[i] & 1) << 2;
            xmc[28] = xmc[28] | ((c[i2] >> 6) & 3);
            xmc[29] = (c[i2] >> 3) & 7;
            i = i2 + 1;
            xmc[30] = c[i2] & 7;
            xmc[31] = (c[i] >> 5) & 7;
            xmc[32] = (c[i] >> 2) & 7;
            i2 = i + 1;
            xmc[33] = (c[i] & 3) << 1;
            xmc[33] = xmc[33] | ((c[i2] >> 7) & 1);
            xmc[34] = (c[i2] >> 4) & 7;
            xmc[35] = (c[i2] >> 1) & 7;
            i = i2 + 1;
            xmc[36] = (c[i2] & 1) << 2;
            xmc[36] = xmc[36] | ((c[i] >> 6) & 3);
            xmc[37] = (c[i] >> 3) & 7;
            i2 = i + 1;
            xmc[38] = c[i] & 7;
            Nc[3] = (c[i2] >> 1) & 127;
            i = i2 + 1;
            bc[3] = (c[i2] & 1) << 1;
            bc[3] = bc[3] | ((c[i] >> 7) & 1);
            Mc[3] = (c[i] >> 5) & 3;
            i2 = i + 1;
            xmaxc[3] = (c[i] & 31) << 1;
            xmaxc[3] = xmaxc[3] | ((c[i2] >> 7) & 1);
            xmc[39] = (c[i2] >> 4) & 7;
            xmc[40] = (c[i2] >> 1) & 7;
            i = i2 + 1;
            xmc[41] = (c[i2] & 1) << 2;
            xmc[41] = xmc[41] | ((c[i] >> 6) & 3);
            xmc[42] = (c[i] >> 3) & 7;
            i2 = i + 1;
            xmc[43] = c[i] & 7;
            xmc[44] = (c[i2] >> 5) & 7;
            xmc[45] = (c[i2] >> 2) & 7;
            i = i2 + 1;
            xmc[46] = (c[i2] & 3) << 1;
            xmc[46] = xmc[46] | ((c[i] >> 7) & 1);
            xmc[47] = (c[i] >> 4) & 7;
            xmc[48] = (c[i] >> 1) & 7;
            i2 = i + 1;
            xmc[49] = (c[i] & 1) << 2;
            xmc[49] = xmc[49] | ((c[i2] >> 6) & 3);
            xmc[50] = (c[i2] >> 3) & 7;
            xmc[51] = c[i2] & 7;
            decoder(LARc, Nc, bc, Mc, xmaxc, xmc, s);
        }
    }

    private void decoder(int[] LARcr, int[] Ncr, int[] bcr, int[] Mcr, int[] xmaxcr, int[] xMcr, int[] s) {
        int[] erp = new int[40];
        int[] wt = new int[160];
        for (int j = 0; j < 4; j++) {
            RPEDecoding(xmaxcr[j], Mcr[j], xMcr, j * 13, erp);
            longTermSynthesisFiltering(Ncr[j], bcr[j], erp, this.dp0);
            for (int k = 0; k < 40; k++) {
                wt[(j * 40) + k] = this.dp0[k + WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18];
            }
        }
        shortTermSynthesisFilter(LARcr, wt, s);
        postprocessing(s);
    }

    public void GSM() {
        this.nrp = 40;
    }

    private void longTermSynthesisFiltering(int Ncr, int bcr, int[] erp, int[] dp0) {
        int Nr;
        int k;
        if (Ncr < 40 || Ncr > WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18) {
            Nr = this.nrp;
        } else {
            Nr = Ncr;
        }
        this.nrp = Nr;
        int brp = QLB[bcr];
        for (k = 0; k <= 39; k++) {
            dp0[k + WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18] = add(erp[k], mult_r(brp, dp0[(k - Nr) + WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18]));
        }
        for (k = 0; k <= WavAudioFormat.WAVE_FORMAT_VOXWARE_VR12; k++) {
            dp0[k] = dp0[k + 40];
        }
    }

    private void postprocessing(int[] s) {
        int soff = 0;
        int k = 160;
        while (true) {
            int k2 = k;
            k = k2 - 1;
            if (k2 > 0) {
                this.msr = add(s[soff], mult_r(this.msr, 28180));
                s[soff] = saturate(add(this.msr, this.msr) & -8);
                soff++;
            } else {
                return;
            }
        }
    }

    private void RPEDecoding(int xmaxcr, int Mcr, int[] xMcr, int xMcrOffset, int[] erp) {
        int[] xMp = new int[13];
        int[] expAndMant = xmaxcToExpAndMant(xmaxcr);
        APCMInverseQuantization(xMcr, xMcrOffset, expAndMant[0], expAndMant[1], xMp);
        RPE_grid_positioning(Mcr, xMp, erp);
    }

    private void shortTermSynthesisFilter(int[] LARcr, int[] wt, int[] s) {
        int[] LARpp_j = this.LARpp[this.j];
        int[][] iArr = this.LARpp;
        int i = this.j ^ 1;
        this.j = i;
        int[] LARpp_j_1 = iArr[i];
        int[] LARp = new int[8];
        decodingOfTheCodedLogAreaRatios(LARcr, LARpp_j);
        Coefficients_0_12(LARpp_j_1, LARpp_j, LARp);
        LARp_to_rp(LARp);
        shortTermSynthesisFiltering(LARp, 13, wt, s, 0);
        Coefficients_13_26(LARpp_j_1, LARpp_j, LARp);
        LARp_to_rp(LARp);
        shortTermSynthesisFiltering(LARp, 14, wt, s, 13);
        Coefficients_27_39(LARpp_j_1, LARpp_j, LARp);
        LARp_to_rp(LARp);
        shortTermSynthesisFiltering(LARp, 13, wt, s, 27);
        Coefficients_40_159(LARpp_j, LARp);
        LARp_to_rp(LARp);
        shortTermSynthesisFiltering(LARp, WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18, wt, s, 40);
    }

    private void shortTermSynthesisFiltering(int[] rrp, int k, int[] wt, int[] sr, int off) {
        int i = off;
        int i2 = off;
        while (true) {
            int i3 = i2;
            int woff = i;
            int k2 = k;
            k = k2 - 1;
            if (k2 > 0) {
                i = woff + 1;
                int sri = wt[woff];
                int i4 = 8;
                while (true) {
                    int i5 = i4;
                    i4 = i5 - 1;
                    if (i5 <= 0) {
                        break;
                    }
                    int tmp1 = rrp[i4];
                    int tmp2 = this.v[i4];
                    tmp2 = (tmp1 == MIN_WORD && tmp2 == MIN_WORD) ? 32767 : saturate(((tmp1 * tmp2) + 16384) >> 15);
                    sri = sub(sri, tmp2);
                    tmp1 = (tmp1 == MIN_WORD && sri == MIN_WORD) ? 32767 : saturate(((tmp1 * sri) + 16384) >> 15);
                    this.v[i4 + 1] = add(this.v[i4], tmp1);
                }
                i2 = i3 + 1;
                this.v[0] = sri;
                sr[i3] = sri;
            } else {
                return;
            }
        }
    }

    private int[] xmaxcToExpAndMant(int xmaxc) {
        int exp = 0;
        if (xmaxc > 15) {
            exp = (xmaxc >> 3) - 1;
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
        return new int[]{exp, mant};
    }
}
