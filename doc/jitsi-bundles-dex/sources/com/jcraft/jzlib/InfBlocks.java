package com.jcraft.jzlib;

import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;

final class InfBlocks {
    private static final int BAD = 9;
    private static final int BTREE = 4;
    private static final int CODES = 6;
    private static final int DONE = 8;
    private static final int DRY = 7;
    private static final int DTREE = 5;
    private static final int LENS = 1;
    private static final int MANY = 1440;
    private static final int STORED = 2;
    private static final int TABLE = 3;
    private static final int TYPE = 0;
    private static final int Z_BUF_ERROR = -5;
    private static final int Z_DATA_ERROR = -3;
    private static final int Z_ERRNO = -1;
    private static final int Z_MEM_ERROR = -4;
    private static final int Z_NEED_DICT = 2;
    private static final int Z_OK = 0;
    private static final int Z_STREAM_END = 1;
    private static final int Z_STREAM_ERROR = -2;
    private static final int Z_VERSION_ERROR = -6;
    static final int[] border = new int[]{16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15};
    private static final int[] inflate_mask = new int[]{0, 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095, 8191, 16383, 32767, InBandBytestreamManager.MAXIMUM_BLOCK_SIZE};
    int[] bb = new int[1];
    int bitb;
    int bitk;
    int[] blens;
    long check;
    Object checkfn;
    InfCodes codes = new InfCodes();
    int end;
    int[] hufts = new int[4320];
    int index;
    InfTree inftree = new InfTree();
    int last;
    int left;
    int mode;
    int read;
    int table;
    int[] tb = new int[1];
    byte[] window;
    int write;

    InfBlocks(ZStream z, Object checkfn, int w) {
        this.window = new byte[w];
        this.end = w;
        this.checkfn = checkfn;
        this.mode = 0;
        reset(z, null);
    }

    /* access modifiers changed from: 0000 */
    public void reset(ZStream z, long[] c) {
        if (c != null) {
            c[0] = this.check;
        }
        if (this.mode == 4 || this.mode == 5) {
        }
        if (this.mode == 6) {
            this.codes.free(z);
        }
        this.mode = 0;
        this.bitk = 0;
        this.bitb = 0;
        this.write = 0;
        this.read = 0;
        if (this.checkfn != null) {
            long adler32 = z._adler.adler32(0, null, 0, 0);
            this.check = adler32;
            z.adler = adler32;
        }
    }

    /* access modifiers changed from: 0000 */
    /* JADX WARNING: Missing block: B:8:0x0074, code skipped:
            if (r25 >= 3) goto L_0x00c4;
     */
    /* JADX WARNING: Missing block: B:9:0x0076, code skipped:
            if (r27 == 0) goto L_0x008f;
     */
    /* JADX WARNING: Missing block: B:11:0x008f, code skipped:
            r32.bitb = r20;
            r32.bitk = r25;
            r33.avail_in = r27;
            r33.total_in += (long) (r29 - r33.next_in_index);
            r33.next_in_index = r29;
            r32.write = r30;
            r28 = r29;
     */
    /* JADX WARNING: Missing block: B:12:0x00c4, code skipped:
            r31 = r20 & 7;
            r32.last = r31 & 1;
     */
    /* JADX WARNING: Missing block: B:13:0x00ce, code skipped:
            switch((r31 >>> 1)) {
                case 0: goto L_0x00d5;
                case 1: goto L_0x00e5;
                case 2: goto L_0x0117;
                case 3: goto L_0x0121;
                default: goto L_0x00d1;
            };
     */
    /* JADX WARNING: Missing block: B:14:0x00d1, code skipped:
            r28 = r29;
     */
    /* JADX WARNING: Missing block: B:15:0x00d5, code skipped:
            r25 = r25 - 3;
            r31 = r25 & 7;
            r20 = (r20 >>> 3) >>> r31;
            r25 = r25 - r31;
            r32.mode = 1;
     */
    /* JADX WARNING: Missing block: B:16:0x00e5, code skipped:
            r8 = new int[1];
            r9 = new int[1];
            r10 = new int[1][];
            r11 = new int[1][];
            com.jcraft.jzlib.InfTree.inflate_trees_fixed(r8, r9, r10, r11, r33);
            r32.codes.init(r8[0], r9[0], r10[0], 0, r11[0], null, r33);
            r20 = r20 >>> 3;
            r25 = r25 - 3;
            r32.mode = 6;
     */
    /* JADX WARNING: Missing block: B:17:0x0117, code skipped:
            r20 = r20 >>> 3;
            r25 = r25 - 3;
            r32.mode = 3;
     */
    /* JADX WARNING: Missing block: B:18:0x0121, code skipped:
            r20 = r20 >>> 3;
            r25 = r25 - 3;
            r32.mode = 9;
            r33.msg = "invalid block type";
            r32.bitb = r20;
            r32.bitk = r25;
            r33.avail_in = r27;
            r33.total_in += (long) (r29 - r33.next_in_index);
            r33.next_in_index = r29;
            r32.write = r30;
            r28 = r29;
     */
    /* JADX WARNING: Missing block: B:20:0x016d, code skipped:
            if (r25 >= 32) goto L_0x01be;
     */
    /* JADX WARNING: Missing block: B:21:0x016f, code skipped:
            if (r27 == 0) goto L_0x0188;
     */
    /* JADX WARNING: Missing block: B:23:0x0188, code skipped:
            r32.bitb = r20;
            r32.bitk = r25;
            r33.avail_in = r27;
            r33.total_in += (long) (r29 - r33.next_in_index);
            r33.next_in_index = r29;
            r32.write = r30;
            r28 = r29;
     */
    /* JADX WARNING: Missing block: B:25:0x01cb, code skipped:
            if ((((r20 ^ -1) >>> 16) & org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager.MAXIMUM_BLOCK_SIZE) == (org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager.MAXIMUM_BLOCK_SIZE & r20)) goto L_0x0211;
     */
    /* JADX WARNING: Missing block: B:26:0x01cd, code skipped:
            r32.mode = 9;
            r33.msg = "invalid stored block lengths";
            r32.bitb = r20;
            r32.bitk = r25;
            r33.avail_in = r27;
            r33.total_in += (long) (r29 - r33.next_in_index);
            r33.next_in_index = r29;
            r32.write = r30;
            r28 = r29;
     */
    /* JADX WARNING: Missing block: B:27:0x0211, code skipped:
            r32.left = org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager.MAXIMUM_BLOCK_SIZE & r20;
            r25 = 0;
            r20 = 0;
     */
    /* JADX WARNING: Missing block: B:28:0x0222, code skipped:
            if (r32.left == 0) goto L_0x022d;
     */
    /* JADX WARNING: Missing block: B:29:0x0224, code skipped:
            r4 = 2;
     */
    /* JADX WARNING: Missing block: B:30:0x0225, code skipped:
            r32.mode = r4;
            r28 = r29;
     */
    /* JADX WARNING: Missing block: B:32:0x0231, code skipped:
            if (r32.last == 0) goto L_0x0235;
     */
    /* JADX WARNING: Missing block: B:33:0x0233, code skipped:
            r4 = 7;
     */
    /* JADX WARNING: Missing block: B:34:0x0235, code skipped:
            r4 = 0;
     */
    /* JADX WARNING: Missing block: B:75:0x036c, code skipped:
            if (r25 >= 14) goto L_0x03bd;
     */
    /* JADX WARNING: Missing block: B:76:0x036e, code skipped:
            if (r27 == 0) goto L_0x0387;
     */
    /* JADX WARNING: Missing block: B:78:0x0387, code skipped:
            r32.bitb = r20;
            r32.bitk = r25;
            r33.avail_in = r27;
            r33.total_in += (long) (r29 - r33.next_in_index);
            r33.next_in_index = r29;
            r32.write = r30;
            r28 = r29;
     */
    /* JADX WARNING: Missing block: B:79:0x03bd, code skipped:
            r31 = r20 & 16383;
            r32.table = r31;
     */
    /* JADX WARNING: Missing block: B:80:0x03cd, code skipped:
            if ((r31 & 31) > 29) goto L_0x03d7;
     */
    /* JADX WARNING: Missing block: B:82:0x03d5, code skipped:
            if (((r31 >> 5) & 31) <= 29) goto L_0x041b;
     */
    /* JADX WARNING: Missing block: B:84:0x041b, code skipped:
            r31 = ((r31 & 31) + 258) + ((r31 >> 5) & 31);
     */
    /* JADX WARNING: Missing block: B:85:0x0429, code skipped:
            if (r32.blens == null) goto L_0x0434;
     */
    /* JADX WARNING: Missing block: B:87:0x0432, code skipped:
            if (r32.blens.length >= r31) goto L_0x047a;
     */
    /* JADX WARNING: Missing block: B:88:0x0434, code skipped:
            r32.blens = new int[r31];
     */
    /* JADX WARNING: Missing block: B:89:0x043c, code skipped:
            r20 = r20 >>> 14;
            r25 = r25 - 14;
            r32.index = 0;
            r32.mode = 4;
            r28 = r29;
     */
    /* JADX WARNING: Missing block: B:91:0x0458, code skipped:
            if (r32.index >= ((r32.table >>> 10) + 4)) goto L_0x04e0;
     */
    /* JADX WARNING: Missing block: B:92:0x045c, code skipped:
            r29 = r28;
     */
    /* JADX WARNING: Missing block: B:93:0x045f, code skipped:
            if (r25 >= 3) goto L_0x04c2;
     */
    /* JADX WARNING: Missing block: B:94:0x0461, code skipped:
            if (r27 == 0) goto L_0x048c;
     */
    /* JADX WARNING: Missing block: B:95:0x0463, code skipped:
            r34 = 0;
            r27 = r27 - 1;
            r28 = r29 + 1;
            r20 = r20 | ((r33.next_in[r29] & 255) << r25);
            r25 = r25 + 8;
     */
    /* JADX WARNING: Missing block: B:96:0x047a, code skipped:
            r22 = 0;
     */
    /* JADX WARNING: Missing block: B:98:0x0480, code skipped:
            if (r22 >= r31) goto L_0x043c;
     */
    /* JADX WARNING: Missing block: B:99:0x0482, code skipped:
            r32.blens[r22] = 0;
            r22 = r22 + 1;
     */
    /* JADX WARNING: Missing block: B:100:0x048c, code skipped:
            r32.bitb = r20;
            r32.bitk = r25;
            r33.avail_in = r27;
            r33.total_in += (long) (r29 - r33.next_in_index);
            r33.next_in_index = r29;
            r32.write = r30;
            r28 = r29;
     */
    /* JADX WARNING: Missing block: B:101:0x04c2, code skipped:
            r4 = r32.blens;
            r5 = border;
            r6 = r32.index;
            r32.index = r6 + 1;
            r4[r5[r6]] = r20 & 7;
            r20 = r20 >>> 3;
            r25 = r25 - 3;
            r28 = r29;
     */
    /* JADX WARNING: Missing block: B:103:0x04e6, code skipped:
            if (r32.index >= 19) goto L_0x04fe;
     */
    /* JADX WARNING: Missing block: B:104:0x04e8, code skipped:
            r4 = r32.blens;
            r5 = border;
            r6 = r32.index;
            r32.index = r6 + 1;
            r4[r5[r6]] = 0;
     */
    /* JADX WARNING: Missing block: B:105:0x04fe, code skipped:
            r32.bb[0] = 7;
            r31 = r32.inftree.inflate_trees_bits(r32.blens, r32.bb, r32.tb, r32.hufts, r33);
     */
    /* JADX WARNING: Missing block: B:106:0x0520, code skipped:
            if (r31 == 0) goto L_0x0568;
     */
    /* JADX WARNING: Missing block: B:107:0x0522, code skipped:
            r34 = r31;
     */
    /* JADX WARNING: Missing block: B:108:0x0527, code skipped:
            if (r34 != -3) goto L_0x0534;
     */
    /* JADX WARNING: Missing block: B:109:0x0529, code skipped:
            r32.blens = null;
            r32.mode = 9;
     */
    /* JADX WARNING: Missing block: B:110:0x0534, code skipped:
            r32.bitb = r20;
            r32.bitk = r25;
            r33.avail_in = r27;
            r33.total_in += (long) (r28 - r33.next_in_index);
            r33.next_in_index = r28;
            r32.write = r30;
     */
    /* JADX WARNING: Missing block: B:111:0x0568, code skipped:
            r32.index = 0;
            r32.mode = 5;
     */
    /* JADX WARNING: Missing block: B:112:0x0572, code skipped:
            r31 = r32.table;
     */
    /* JADX WARNING: Missing block: B:113:0x0585, code skipped:
            if (r32.index < (((r31 & 31) + 258) + ((r31 >> 5) & 31))) goto L_0x060e;
     */
    /* JADX WARNING: Missing block: B:114:0x0587, code skipped:
            r32.tb[0] = -1;
            r8 = new int[1];
            r9 = new int[1];
            r10 = new int[1];
            r11 = new int[]{9};
            r9[0] = 6;
            r31 = r32.table;
            r31 = r32.inftree.inflate_trees_dynamic((r31 & 31) + 257, ((r31 >> 5) & 31) + 1, r32.blens, r8, r9, r10, r11, r32.hufts, r33);
     */
    /* JADX WARNING: Missing block: B:115:0x05c6, code skipped:
            if (r31 == 0) goto L_0x07d3;
     */
    /* JADX WARNING: Missing block: B:117:0x05cb, code skipped:
            if (r31 != -3) goto L_0x05d8;
     */
    /* JADX WARNING: Missing block: B:118:0x05cd, code skipped:
            r32.blens = null;
            r32.mode = 9;
     */
    /* JADX WARNING: Missing block: B:119:0x05d8, code skipped:
            r34 = r31;
            r32.bitb = r20;
            r32.bitk = r25;
            r33.avail_in = r27;
            r33.total_in += (long) (r28 - r33.next_in_index);
            r33.next_in_index = r28;
            r32.write = r30;
     */
    /* JADX WARNING: Missing block: B:120:0x060e, code skipped:
            r31 = r32.bb[0];
     */
    /* JADX WARNING: Missing block: B:121:0x0617, code skipped:
            r29 = r28;
     */
    /* JADX WARNING: Missing block: B:122:0x061b, code skipped:
            if (r25 >= r31) goto L_0x066c;
     */
    /* JADX WARNING: Missing block: B:123:0x061d, code skipped:
            if (r27 == 0) goto L_0x0636;
     */
    /* JADX WARNING: Missing block: B:124:0x061f, code skipped:
            r34 = 0;
            r27 = r27 - 1;
            r28 = r29 + 1;
            r20 = r20 | ((r33.next_in[r29] & 255) << r25);
            r25 = r25 + 8;
     */
    /* JADX WARNING: Missing block: B:125:0x0636, code skipped:
            r32.bitb = r20;
            r32.bitk = r25;
            r33.avail_in = r27;
            r33.total_in += (long) (r29 - r33.next_in_index);
            r33.next_in_index = r29;
            r32.write = r30;
            r28 = r29;
     */
    /* JADX WARNING: Missing block: B:127:0x0674, code skipped:
            if (r32.tb[0] != -1) goto L_0x0676;
     */
    /* JADX WARNING: Missing block: B:128:0x0676, code skipped:
            r31 = r32.hufts[((r32.tb[0] + (inflate_mask[r31] & r20)) * 3) + 1];
            r21 = r32.hufts[((r32.tb[0] + (inflate_mask[r31] & r20)) * 3) + 2];
     */
    /* JADX WARNING: Missing block: B:129:0x06aa, code skipped:
            if (r21 >= 16) goto L_0x06c4;
     */
    /* JADX WARNING: Missing block: B:130:0x06ac, code skipped:
            r20 = r20 >>> r31;
            r25 = r25 - r31;
            r4 = r32.blens;
            r5 = r32.index;
            r32.index = r5 + 1;
            r4[r5] = r21;
            r28 = r29;
     */
    /* JADX WARNING: Missing block: B:132:0x06c8, code skipped:
            if (r21 != 18) goto L_0x06f3;
     */
    /* JADX WARNING: Missing block: B:133:0x06ca, code skipped:
            r22 = 7;
     */
    /* JADX WARNING: Missing block: B:135:0x06d0, code skipped:
            if (r21 != 18) goto L_0x06f6;
     */
    /* JADX WARNING: Missing block: B:136:0x06d2, code skipped:
            r24 = 11;
     */
    /* JADX WARNING: Missing block: B:138:0x06d8, code skipped:
            if (r25 >= (r31 + r22)) goto L_0x072f;
     */
    /* JADX WARNING: Missing block: B:139:0x06da, code skipped:
            if (r27 == 0) goto L_0x06f9;
     */
    /* JADX WARNING: Missing block: B:140:0x06dc, code skipped:
            r34 = 0;
            r27 = r27 - 1;
            r20 = r20 | ((r33.next_in[r29] & 255) << r25);
            r25 = r25 + 8;
            r29 = r29 + 1;
     */
    /* JADX WARNING: Missing block: B:141:0x06f3, code skipped:
            r22 = r21 - 14;
     */
    /* JADX WARNING: Missing block: B:142:0x06f6, code skipped:
            r24 = 3;
     */
    /* JADX WARNING: Missing block: B:143:0x06f9, code skipped:
            r32.bitb = r20;
            r32.bitk = r25;
            r33.avail_in = r27;
            r33.total_in += (long) (r29 - r33.next_in_index);
            r33.next_in_index = r29;
            r32.write = r30;
            r28 = r29;
     */
    /* JADX WARNING: Missing block: B:144:0x072f, code skipped:
            r20 = r20 >>> r31;
            r24 = r24 + (inflate_mask[r22] & r20);
            r20 = r20 >>> r22;
            r25 = (r25 - r31) - r22;
            r22 = r32.index;
            r31 = r32.table;
     */
    /* JADX WARNING: Missing block: B:145:0x0756, code skipped:
            if ((r22 + r24) > (((r31 & 31) + 258) + ((r31 >> 5) & 31))) goto L_0x0763;
     */
    /* JADX WARNING: Missing block: B:147:0x075c, code skipped:
            if (r21 != 16) goto L_0x07ac;
     */
    /* JADX WARNING: Missing block: B:149:0x0761, code skipped:
            if (r22 >= 1) goto L_0x07ac;
     */
    /* JADX WARNING: Missing block: B:150:0x0763, code skipped:
            r32.blens = null;
            r32.mode = 9;
            r33.msg = "invalid bit length repeat";
            r32.bitb = r20;
            r32.bitk = r25;
            r33.avail_in = r27;
            r33.total_in += (long) (r29 - r33.next_in_index);
            r33.next_in_index = r29;
            r32.write = r30;
            r28 = r29;
     */
    /* JADX WARNING: Missing block: B:152:0x07b0, code skipped:
            if (r21 != 16) goto L_0x07d0;
     */
    /* JADX WARNING: Missing block: B:153:0x07b2, code skipped:
            r21 = r32.blens[r22 - 1];
     */
    /* JADX WARNING: Missing block: B:154:0x07ba, code skipped:
            r23 = r22 + 1;
            r32.blens[r22] = r21;
            r24 = r24 - 1;
     */
    /* JADX WARNING: Missing block: B:155:0x07c4, code skipped:
            if (r24 != 0) goto L_0x0967;
     */
    /* JADX WARNING: Missing block: B:156:0x07c6, code skipped:
            r32.index = r23;
            r28 = r29;
     */
    /* JADX WARNING: Missing block: B:157:0x07d0, code skipped:
            r21 = 0;
     */
    /* JADX WARNING: Missing block: B:158:0x07d3, code skipped:
            r32.codes.init(r8[0], r9[0], r32.hufts, r10[0], r32.hufts, r11[0], r33);
            r32.mode = 6;
     */
    /* JADX WARNING: Missing block: B:159:0x07f7, code skipped:
            r32.bitb = r20;
            r32.bitk = r25;
            r33.avail_in = r27;
            r33.total_in += (long) (r28 - r33.next_in_index);
            r33.next_in_index = r28;
            r32.write = r30;
            r34 = r32.codes.proc(r32, r33, r34);
     */
    /* JADX WARNING: Missing block: B:160:0x0836, code skipped:
            if (r34 == 1) goto L_0x083e;
     */
    /* JADX WARNING: Missing block: B:162:0x083e, code skipped:
            r34 = 0;
            r32.codes.free(r33);
            r28 = r33.next_in_index;
            r27 = r33.avail_in;
            r20 = r32.bitb;
            r25 = r32.bitk;
            r30 = r32.write;
     */
    /* JADX WARNING: Missing block: B:163:0x086d, code skipped:
            if (r30 >= r32.read) goto L_0x0884;
     */
    /* JADX WARNING: Missing block: B:164:0x086f, code skipped:
            r26 = (r32.read - r30) - 1;
     */
    /* JADX WARNING: Missing block: B:166:0x087b, code skipped:
            if (r32.last != 0) goto L_0x088b;
     */
    /* JADX WARNING: Missing block: B:167:0x087d, code skipped:
            r32.mode = 0;
     */
    /* JADX WARNING: Missing block: B:168:0x0884, code skipped:
            r26 = r32.end - r30;
     */
    /* JADX WARNING: Missing block: B:169:0x088b, code skipped:
            r32.mode = 7;
     */
    /* JADX WARNING: Missing block: B:170:0x0890, code skipped:
            r32.write = r30;
            r34 = inflate_flush(r33, r34);
            r30 = r32.write;
     */
    /* JADX WARNING: Missing block: B:171:0x08a6, code skipped:
            if (r30 >= r32.read) goto L_0x08ee;
     */
    /* JADX WARNING: Missing block: B:172:0x08a8, code skipped:
            r26 = (r32.read - r30) - 1;
     */
    /* JADX WARNING: Missing block: B:174:0x08b8, code skipped:
            if (r32.read == r32.write) goto L_0x08f5;
     */
    /* JADX WARNING: Missing block: B:175:0x08ba, code skipped:
            r32.bitb = r20;
            r32.bitk = r25;
            r33.avail_in = r27;
            r33.total_in += (long) (r28 - r33.next_in_index);
            r33.next_in_index = r28;
            r32.write = r30;
     */
    /* JADX WARNING: Missing block: B:176:0x08ee, code skipped:
            r26 = r32.end - r30;
     */
    /* JADX WARNING: Missing block: B:177:0x08f5, code skipped:
            r32.mode = 8;
     */
    /* JADX WARNING: Missing block: B:178:0x08fb, code skipped:
            r32.bitb = r20;
            r32.bitk = r25;
            r33.avail_in = r27;
            r33.total_in += (long) (r28 - r33.next_in_index);
            r33.next_in_index = r28;
            r32.write = r30;
     */
    /* JADX WARNING: Missing block: B:180:0x0967, code skipped:
            r22 = r23;
     */
    /* JADX WARNING: Missing block: B:224:?, code skipped:
            return inflate_flush(r33, r34);
     */
    /* JADX WARNING: Missing block: B:225:?, code skipped:
            return inflate_flush(r33, -3);
     */
    /* JADX WARNING: Missing block: B:226:?, code skipped:
            return inflate_flush(r33, r34);
     */
    /* JADX WARNING: Missing block: B:227:?, code skipped:
            return inflate_flush(r33, -3);
     */
    /* JADX WARNING: Missing block: B:230:?, code skipped:
            return inflate_flush(r33, r34);
     */
    /* JADX WARNING: Missing block: B:232:?, code skipped:
            return inflate_flush(r33, r34);
     */
    /* JADX WARNING: Missing block: B:233:?, code skipped:
            return inflate_flush(r33, r34);
     */
    /* JADX WARNING: Missing block: B:234:?, code skipped:
            return inflate_flush(r33, r34);
     */
    /* JADX WARNING: Missing block: B:235:?, code skipped:
            return inflate_flush(r33, r34);
     */
    /* JADX WARNING: Missing block: B:236:?, code skipped:
            return inflate_flush(r33, r34);
     */
    /* JADX WARNING: Missing block: B:237:?, code skipped:
            return inflate_flush(r33, -3);
     */
    /* JADX WARNING: Missing block: B:238:?, code skipped:
            return inflate_flush(r33, r34);
     */
    /* JADX WARNING: Missing block: B:239:?, code skipped:
            return inflate_flush(r33, r34);
     */
    /* JADX WARNING: Missing block: B:240:?, code skipped:
            return inflate_flush(r33, 1);
     */
    public int proc(com.jcraft.jzlib.ZStream r33, int r34) {
        /*
        r32 = this;
        r0 = r33;
        r0 = r0.next_in_index;
        r28 = r0;
        r0 = r33;
        r0 = r0.avail_in;
        r27 = r0;
        r0 = r32;
        r0 = r0.bitb;
        r20 = r0;
        r0 = r32;
        r0 = r0.bitk;
        r25 = r0;
        r0 = r32;
        r0 = r0.write;
        r30 = r0;
        r0 = r32;
        r4 = r0.read;
        r0 = r30;
        if (r0 >= r4) goto L_0x006a;
    L_0x0026:
        r0 = r32;
        r4 = r0.read;
        r4 = r4 - r30;
        r26 = r4 + -1;
    L_0x002e:
        r0 = r32;
        r4 = r0.mode;
        switch(r4) {
            case 0: goto L_0x0973;
            case 1: goto L_0x096f;
            case 2: goto L_0x0237;
            case 3: goto L_0x096b;
            case 4: goto L_0x044c;
            case 5: goto L_0x0572;
            case 6: goto L_0x07f7;
            case 7: goto L_0x0890;
            case 8: goto L_0x08fb;
            case 9: goto L_0x0931;
            default: goto L_0x0035;
        };
    L_0x0035:
        r34 = -2;
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r28 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r28;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r4 = r32.inflate_flush(r33, r34);
    L_0x0069:
        return r4;
    L_0x006a:
        r0 = r32;
        r4 = r0.end;
        r26 = r4 - r30;
        goto L_0x002e;
    L_0x0071:
        r4 = 3;
        r0 = r25;
        if (r0 >= r4) goto L_0x00c4;
    L_0x0076:
        if (r27 == 0) goto L_0x008f;
    L_0x0078:
        r34 = 0;
        r27 = r27 + -1;
        r0 = r33;
        r4 = r0.next_in;
        r28 = r29 + 1;
        r4 = r4[r29];
        r4 = r4 & 255;
        r4 = r4 << r25;
        r20 = r20 | r4;
        r25 = r25 + 8;
        r29 = r28;
        goto L_0x0071;
    L_0x008f:
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r29 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r29;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r4 = r32.inflate_flush(r33, r34);
        r28 = r29;
        goto L_0x0069;
    L_0x00c4:
        r31 = r20 & 7;
        r4 = r31 & 1;
        r0 = r32;
        r0.last = r4;
        r4 = r31 >>> 1;
        switch(r4) {
            case 0: goto L_0x00d5;
            case 1: goto L_0x00e5;
            case 2: goto L_0x0117;
            case 3: goto L_0x0121;
            default: goto L_0x00d1;
        };
    L_0x00d1:
        r28 = r29;
        goto L_0x002e;
    L_0x00d5:
        r20 = r20 >>> 3;
        r25 = r25 + -3;
        r31 = r25 & 7;
        r20 = r20 >>> r31;
        r25 = r25 - r31;
        r4 = 1;
        r0 = r32;
        r0.mode = r4;
        goto L_0x00d1;
    L_0x00e5:
        r4 = 1;
        r8 = new int[r4];
        r4 = 1;
        r9 = new int[r4];
        r4 = 1;
        r10 = new int[r4][];
        r4 = 1;
        r11 = new int[r4][];
        r0 = r33;
        com.jcraft.jzlib.InfTree.inflate_trees_fixed(r8, r9, r10, r11, r0);
        r0 = r32;
        r4 = r0.codes;
        r5 = 0;
        r5 = r8[r5];
        r6 = 0;
        r6 = r9[r6];
        r7 = 0;
        r7 = r10[r7];
        r8 = 0;
        r12 = 0;
        r9 = r11[r12];
        r10 = 0;
        r11 = r33;
        r4.init(r5, r6, r7, r8, r9, r10, r11);
        r20 = r20 >>> 3;
        r25 = r25 + -3;
        r4 = 6;
        r0 = r32;
        r0.mode = r4;
        goto L_0x00d1;
    L_0x0117:
        r20 = r20 >>> 3;
        r25 = r25 + -3;
        r4 = 3;
        r0 = r32;
        r0.mode = r4;
        goto L_0x00d1;
    L_0x0121:
        r20 = r20 >>> 3;
        r25 = r25 + -3;
        r4 = 9;
        r0 = r32;
        r0.mode = r4;
        r4 = "invalid block type";
        r0 = r33;
        r0.msg = r4;
        r34 = -3;
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r29 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r29;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r4 = r32.inflate_flush(r33, r34);
        r28 = r29;
        goto L_0x0069;
    L_0x0169:
        r4 = 32;
        r0 = r25;
        if (r0 >= r4) goto L_0x01be;
    L_0x016f:
        if (r27 == 0) goto L_0x0188;
    L_0x0171:
        r34 = 0;
        r27 = r27 + -1;
        r0 = r33;
        r4 = r0.next_in;
        r28 = r29 + 1;
        r4 = r4[r29];
        r4 = r4 & 255;
        r4 = r4 << r25;
        r20 = r20 | r4;
        r25 = r25 + 8;
        r29 = r28;
        goto L_0x0169;
    L_0x0188:
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r29 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r29;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r4 = r32.inflate_flush(r33, r34);
        r28 = r29;
        goto L_0x0069;
    L_0x01be:
        r4 = r20 ^ -1;
        r4 = r4 >>> 16;
        r5 = 65535; // 0xffff float:9.1834E-41 double:3.23786E-319;
        r4 = r4 & r5;
        r5 = 65535; // 0xffff float:9.1834E-41 double:3.23786E-319;
        r5 = r5 & r20;
        if (r4 == r5) goto L_0x0211;
    L_0x01cd:
        r4 = 9;
        r0 = r32;
        r0.mode = r4;
        r4 = "invalid stored block lengths";
        r0 = r33;
        r0.msg = r4;
        r34 = -3;
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r29 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r29;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r4 = r32.inflate_flush(r33, r34);
        r28 = r29;
        goto L_0x0069;
    L_0x0211:
        r4 = 65535; // 0xffff float:9.1834E-41 double:3.23786E-319;
        r4 = r4 & r20;
        r0 = r32;
        r0.left = r4;
        r25 = 0;
        r20 = r25;
        r0 = r32;
        r4 = r0.left;
        if (r4 == 0) goto L_0x022d;
    L_0x0224:
        r4 = 2;
    L_0x0225:
        r0 = r32;
        r0.mode = r4;
        r28 = r29;
        goto L_0x002e;
    L_0x022d:
        r0 = r32;
        r4 = r0.last;
        if (r4 == 0) goto L_0x0235;
    L_0x0233:
        r4 = 7;
        goto L_0x0225;
    L_0x0235:
        r4 = 0;
        goto L_0x0225;
    L_0x0237:
        if (r27 != 0) goto L_0x026d;
    L_0x0239:
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r28 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r28;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r4 = r32.inflate_flush(r33, r34);
        goto L_0x0069;
    L_0x026d:
        if (r26 != 0) goto L_0x031c;
    L_0x026f:
        r0 = r32;
        r4 = r0.end;
        r0 = r30;
        if (r0 != r4) goto L_0x028f;
    L_0x0277:
        r0 = r32;
        r4 = r0.read;
        if (r4 == 0) goto L_0x028f;
    L_0x027d:
        r30 = 0;
        r0 = r32;
        r4 = r0.read;
        r0 = r30;
        if (r0 >= r4) goto L_0x0307;
    L_0x0287:
        r0 = r32;
        r4 = r0.read;
        r4 = r4 - r30;
        r26 = r4 + -1;
    L_0x028f:
        if (r26 != 0) goto L_0x031c;
    L_0x0291:
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r34 = r32.inflate_flush(r33, r34);
        r0 = r32;
        r0 = r0.write;
        r30 = r0;
        r0 = r32;
        r4 = r0.read;
        r0 = r30;
        if (r0 >= r4) goto L_0x030e;
    L_0x02a9:
        r0 = r32;
        r4 = r0.read;
        r4 = r4 - r30;
        r26 = r4 + -1;
    L_0x02b1:
        r0 = r32;
        r4 = r0.end;
        r0 = r30;
        if (r0 != r4) goto L_0x02d1;
    L_0x02b9:
        r0 = r32;
        r4 = r0.read;
        if (r4 == 0) goto L_0x02d1;
    L_0x02bf:
        r30 = 0;
        r0 = r32;
        r4 = r0.read;
        r0 = r30;
        if (r0 >= r4) goto L_0x0315;
    L_0x02c9:
        r0 = r32;
        r4 = r0.read;
        r4 = r4 - r30;
        r26 = r4 + -1;
    L_0x02d1:
        if (r26 != 0) goto L_0x031c;
    L_0x02d3:
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r28 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r28;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r4 = r32.inflate_flush(r33, r34);
        goto L_0x0069;
    L_0x0307:
        r0 = r32;
        r4 = r0.end;
        r26 = r4 - r30;
        goto L_0x028f;
    L_0x030e:
        r0 = r32;
        r4 = r0.end;
        r26 = r4 - r30;
        goto L_0x02b1;
    L_0x0315:
        r0 = r32;
        r4 = r0.end;
        r26 = r4 - r30;
        goto L_0x02d1;
    L_0x031c:
        r34 = 0;
        r0 = r32;
        r0 = r0.left;
        r31 = r0;
        r0 = r31;
        r1 = r27;
        if (r0 <= r1) goto L_0x032c;
    L_0x032a:
        r31 = r27;
    L_0x032c:
        r0 = r31;
        r1 = r26;
        if (r0 <= r1) goto L_0x0334;
    L_0x0332:
        r31 = r26;
    L_0x0334:
        r0 = r33;
        r4 = r0.next_in;
        r0 = r32;
        r5 = r0.window;
        r0 = r28;
        r1 = r30;
        r2 = r31;
        java.lang.System.arraycopy(r4, r0, r5, r1, r2);
        r28 = r28 + r31;
        r27 = r27 - r31;
        r30 = r30 + r31;
        r26 = r26 - r31;
        r0 = r32;
        r4 = r0.left;
        r4 = r4 - r31;
        r0 = r32;
        r0.left = r4;
        if (r4 != 0) goto L_0x002e;
    L_0x0359:
        r0 = r32;
        r4 = r0.last;
        if (r4 == 0) goto L_0x0366;
    L_0x035f:
        r4 = 7;
    L_0x0360:
        r0 = r32;
        r0.mode = r4;
        goto L_0x002e;
    L_0x0366:
        r4 = 0;
        goto L_0x0360;
    L_0x0368:
        r4 = 14;
        r0 = r25;
        if (r0 >= r4) goto L_0x03bd;
    L_0x036e:
        if (r27 == 0) goto L_0x0387;
    L_0x0370:
        r34 = 0;
        r27 = r27 + -1;
        r0 = r33;
        r4 = r0.next_in;
        r28 = r29 + 1;
        r4 = r4[r29];
        r4 = r4 & 255;
        r4 = r4 << r25;
        r20 = r20 | r4;
        r25 = r25 + 8;
        r29 = r28;
        goto L_0x0368;
    L_0x0387:
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r29 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r29;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r4 = r32.inflate_flush(r33, r34);
        r28 = r29;
        goto L_0x0069;
    L_0x03bd:
        r0 = r20;
        r0 = r0 & 16383;
        r31 = r0;
        r0 = r31;
        r1 = r32;
        r1.table = r0;
        r4 = r31 & 31;
        r5 = 29;
        if (r4 > r5) goto L_0x03d7;
    L_0x03cf:
        r4 = r31 >> 5;
        r4 = r4 & 31;
        r5 = 29;
        if (r4 <= r5) goto L_0x041b;
    L_0x03d7:
        r4 = 9;
        r0 = r32;
        r0.mode = r4;
        r4 = "too many length or distance symbols";
        r0 = r33;
        r0.msg = r4;
        r34 = -3;
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r29 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r29;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r4 = r32.inflate_flush(r33, r34);
        r28 = r29;
        goto L_0x0069;
    L_0x041b:
        r4 = r31 & 31;
        r4 = r4 + 258;
        r5 = r31 >> 5;
        r5 = r5 & 31;
        r31 = r4 + r5;
        r0 = r32;
        r4 = r0.blens;
        if (r4 == 0) goto L_0x0434;
    L_0x042b:
        r0 = r32;
        r4 = r0.blens;
        r4 = r4.length;
        r0 = r31;
        if (r4 >= r0) goto L_0x047a;
    L_0x0434:
        r0 = r31;
        r4 = new int[r0];
        r0 = r32;
        r0.blens = r4;
    L_0x043c:
        r20 = r20 >>> 14;
        r25 = r25 + -14;
        r4 = 0;
        r0 = r32;
        r0.index = r4;
        r4 = 4;
        r0 = r32;
        r0.mode = r4;
        r28 = r29;
    L_0x044c:
        r0 = r32;
        r4 = r0.index;
        r0 = r32;
        r5 = r0.table;
        r5 = r5 >>> 10;
        r5 = r5 + 4;
        if (r4 >= r5) goto L_0x04e0;
    L_0x045a:
        r29 = r28;
    L_0x045c:
        r4 = 3;
        r0 = r25;
        if (r0 >= r4) goto L_0x04c2;
    L_0x0461:
        if (r27 == 0) goto L_0x048c;
    L_0x0463:
        r34 = 0;
        r27 = r27 + -1;
        r0 = r33;
        r4 = r0.next_in;
        r28 = r29 + 1;
        r4 = r4[r29];
        r4 = r4 & 255;
        r4 = r4 << r25;
        r20 = r20 | r4;
        r25 = r25 + 8;
        r29 = r28;
        goto L_0x045c;
    L_0x047a:
        r22 = 0;
    L_0x047c:
        r0 = r22;
        r1 = r31;
        if (r0 >= r1) goto L_0x043c;
    L_0x0482:
        r0 = r32;
        r4 = r0.blens;
        r5 = 0;
        r4[r22] = r5;
        r22 = r22 + 1;
        goto L_0x047c;
    L_0x048c:
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r29 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r29;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r4 = r32.inflate_flush(r33, r34);
        r28 = r29;
        goto L_0x0069;
    L_0x04c2:
        r0 = r32;
        r4 = r0.blens;
        r5 = border;
        r0 = r32;
        r6 = r0.index;
        r7 = r6 + 1;
        r0 = r32;
        r0.index = r7;
        r5 = r5[r6];
        r6 = r20 & 7;
        r4[r5] = r6;
        r20 = r20 >>> 3;
        r25 = r25 + -3;
        r28 = r29;
        goto L_0x044c;
    L_0x04e0:
        r0 = r32;
        r4 = r0.index;
        r5 = 19;
        if (r4 >= r5) goto L_0x04fe;
    L_0x04e8:
        r0 = r32;
        r4 = r0.blens;
        r5 = border;
        r0 = r32;
        r6 = r0.index;
        r7 = r6 + 1;
        r0 = r32;
        r0.index = r7;
        r5 = r5[r6];
        r6 = 0;
        r4[r5] = r6;
        goto L_0x04e0;
    L_0x04fe:
        r0 = r32;
        r4 = r0.bb;
        r5 = 0;
        r6 = 7;
        r4[r5] = r6;
        r0 = r32;
        r4 = r0.inftree;
        r0 = r32;
        r5 = r0.blens;
        r0 = r32;
        r6 = r0.bb;
        r0 = r32;
        r7 = r0.tb;
        r0 = r32;
        r8 = r0.hufts;
        r9 = r33;
        r31 = r4.inflate_trees_bits(r5, r6, r7, r8, r9);
        if (r31 == 0) goto L_0x0568;
    L_0x0522:
        r34 = r31;
        r4 = -3;
        r0 = r34;
        if (r0 != r4) goto L_0x0534;
    L_0x0529:
        r4 = 0;
        r0 = r32;
        r0.blens = r4;
        r4 = 9;
        r0 = r32;
        r0.mode = r4;
    L_0x0534:
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r28 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r28;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r4 = r32.inflate_flush(r33, r34);
        goto L_0x0069;
    L_0x0568:
        r4 = 0;
        r0 = r32;
        r0.index = r4;
        r4 = 5;
        r0 = r32;
        r0.mode = r4;
    L_0x0572:
        r0 = r32;
        r0 = r0.table;
        r31 = r0;
        r0 = r32;
        r4 = r0.index;
        r5 = r31 & 31;
        r5 = r5 + 258;
        r6 = r31 >> 5;
        r6 = r6 & 31;
        r5 = r5 + r6;
        if (r4 < r5) goto L_0x060e;
    L_0x0587:
        r0 = r32;
        r4 = r0.tb;
        r5 = 0;
        r6 = -1;
        r4[r5] = r6;
        r4 = 1;
        r8 = new int[r4];
        r4 = 1;
        r9 = new int[r4];
        r4 = 1;
        r10 = new int[r4];
        r4 = 1;
        r11 = new int[r4];
        r4 = 0;
        r5 = 9;
        r8[r4] = r5;
        r4 = 0;
        r5 = 6;
        r9[r4] = r5;
        r0 = r32;
        r0 = r0.table;
        r31 = r0;
        r0 = r32;
        r4 = r0.inftree;
        r5 = r31 & 31;
        r5 = r5 + 257;
        r6 = r31 >> 5;
        r6 = r6 & 31;
        r6 = r6 + 1;
        r0 = r32;
        r7 = r0.blens;
        r0 = r32;
        r12 = r0.hufts;
        r13 = r33;
        r31 = r4.inflate_trees_dynamic(r5, r6, r7, r8, r9, r10, r11, r12, r13);
        if (r31 == 0) goto L_0x07d3;
    L_0x05c8:
        r4 = -3;
        r0 = r31;
        if (r0 != r4) goto L_0x05d8;
    L_0x05cd:
        r4 = 0;
        r0 = r32;
        r0.blens = r4;
        r4 = 9;
        r0 = r32;
        r0.mode = r4;
    L_0x05d8:
        r34 = r31;
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r28 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r28;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r4 = r32.inflate_flush(r33, r34);
        goto L_0x0069;
    L_0x060e:
        r0 = r32;
        r4 = r0.bb;
        r5 = 0;
        r31 = r4[r5];
        r29 = r28;
    L_0x0617:
        r0 = r25;
        r1 = r31;
        if (r0 >= r1) goto L_0x066c;
    L_0x061d:
        if (r27 == 0) goto L_0x0636;
    L_0x061f:
        r34 = 0;
        r27 = r27 + -1;
        r0 = r33;
        r4 = r0.next_in;
        r28 = r29 + 1;
        r4 = r4[r29];
        r4 = r4 & 255;
        r4 = r4 << r25;
        r20 = r20 | r4;
        r25 = r25 + 8;
        r29 = r28;
        goto L_0x0617;
    L_0x0636:
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r29 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r29;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r4 = r32.inflate_flush(r33, r34);
        r28 = r29;
        goto L_0x0069;
    L_0x066c:
        r0 = r32;
        r4 = r0.tb;
        r5 = 0;
        r4 = r4[r5];
        r5 = -1;
        if (r4 != r5) goto L_0x0676;
    L_0x0676:
        r0 = r32;
        r4 = r0.hufts;
        r0 = r32;
        r5 = r0.tb;
        r6 = 0;
        r5 = r5[r6];
        r6 = inflate_mask;
        r6 = r6[r31];
        r6 = r6 & r20;
        r5 = r5 + r6;
        r5 = r5 * 3;
        r5 = r5 + 1;
        r31 = r4[r5];
        r0 = r32;
        r4 = r0.hufts;
        r0 = r32;
        r5 = r0.tb;
        r6 = 0;
        r5 = r5[r6];
        r6 = inflate_mask;
        r6 = r6[r31];
        r6 = r6 & r20;
        r5 = r5 + r6;
        r5 = r5 * 3;
        r5 = r5 + 2;
        r21 = r4[r5];
        r4 = 16;
        r0 = r21;
        if (r0 >= r4) goto L_0x06c4;
    L_0x06ac:
        r20 = r20 >>> r31;
        r25 = r25 - r31;
        r0 = r32;
        r4 = r0.blens;
        r0 = r32;
        r5 = r0.index;
        r6 = r5 + 1;
        r0 = r32;
        r0.index = r6;
        r4[r5] = r21;
        r28 = r29;
        goto L_0x0572;
    L_0x06c4:
        r4 = 18;
        r0 = r21;
        if (r0 != r4) goto L_0x06f3;
    L_0x06ca:
        r22 = 7;
    L_0x06cc:
        r4 = 18;
        r0 = r21;
        if (r0 != r4) goto L_0x06f6;
    L_0x06d2:
        r24 = 11;
    L_0x06d4:
        r4 = r31 + r22;
        r0 = r25;
        if (r0 >= r4) goto L_0x072f;
    L_0x06da:
        if (r27 == 0) goto L_0x06f9;
    L_0x06dc:
        r34 = 0;
        r27 = r27 + -1;
        r0 = r33;
        r4 = r0.next_in;
        r28 = r29 + 1;
        r4 = r4[r29];
        r4 = r4 & 255;
        r4 = r4 << r25;
        r20 = r20 | r4;
        r25 = r25 + 8;
        r29 = r28;
        goto L_0x06d4;
    L_0x06f3:
        r22 = r21 + -14;
        goto L_0x06cc;
    L_0x06f6:
        r24 = 3;
        goto L_0x06d4;
    L_0x06f9:
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r29 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r29;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r4 = r32.inflate_flush(r33, r34);
        r28 = r29;
        goto L_0x0069;
    L_0x072f:
        r20 = r20 >>> r31;
        r25 = r25 - r31;
        r4 = inflate_mask;
        r4 = r4[r22];
        r4 = r4 & r20;
        r24 = r24 + r4;
        r20 = r20 >>> r22;
        r25 = r25 - r22;
        r0 = r32;
        r0 = r0.index;
        r22 = r0;
        r0 = r32;
        r0 = r0.table;
        r31 = r0;
        r4 = r22 + r24;
        r5 = r31 & 31;
        r5 = r5 + 258;
        r6 = r31 >> 5;
        r6 = r6 & 31;
        r5 = r5 + r6;
        if (r4 > r5) goto L_0x0763;
    L_0x0758:
        r4 = 16;
        r0 = r21;
        if (r0 != r4) goto L_0x07ac;
    L_0x075e:
        r4 = 1;
        r0 = r22;
        if (r0 >= r4) goto L_0x07ac;
    L_0x0763:
        r4 = 0;
        r0 = r32;
        r0.blens = r4;
        r4 = 9;
        r0 = r32;
        r0.mode = r4;
        r4 = "invalid bit length repeat";
        r0 = r33;
        r0.msg = r4;
        r34 = -3;
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r29 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r29;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r4 = r32.inflate_flush(r33, r34);
        r28 = r29;
        goto L_0x0069;
    L_0x07ac:
        r4 = 16;
        r0 = r21;
        if (r0 != r4) goto L_0x07d0;
    L_0x07b2:
        r0 = r32;
        r4 = r0.blens;
        r5 = r22 + -1;
        r21 = r4[r5];
    L_0x07ba:
        r0 = r32;
        r4 = r0.blens;
        r23 = r22 + 1;
        r4[r22] = r21;
        r24 = r24 + -1;
        if (r24 != 0) goto L_0x0967;
    L_0x07c6:
        r0 = r23;
        r1 = r32;
        r1.index = r0;
        r28 = r29;
        goto L_0x0572;
    L_0x07d0:
        r21 = 0;
        goto L_0x07ba;
    L_0x07d3:
        r0 = r32;
        r12 = r0.codes;
        r4 = 0;
        r13 = r8[r4];
        r4 = 0;
        r14 = r9[r4];
        r0 = r32;
        r15 = r0.hufts;
        r4 = 0;
        r16 = r10[r4];
        r0 = r32;
        r0 = r0.hufts;
        r17 = r0;
        r4 = 0;
        r18 = r11[r4];
        r19 = r33;
        r12.init(r13, r14, r15, r16, r17, r18, r19);
        r4 = 6;
        r0 = r32;
        r0.mode = r4;
    L_0x07f7:
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r28 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r28;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r0 = r32;
        r4 = r0.codes;
        r0 = r32;
        r1 = r33;
        r2 = r34;
        r34 = r4.proc(r0, r1, r2);
        r4 = 1;
        r0 = r34;
        if (r0 == r4) goto L_0x083e;
    L_0x0838:
        r4 = r32.inflate_flush(r33, r34);
        goto L_0x0069;
    L_0x083e:
        r34 = 0;
        r0 = r32;
        r4 = r0.codes;
        r0 = r33;
        r4.free(r0);
        r0 = r33;
        r0 = r0.next_in_index;
        r28 = r0;
        r0 = r33;
        r0 = r0.avail_in;
        r27 = r0;
        r0 = r32;
        r0 = r0.bitb;
        r20 = r0;
        r0 = r32;
        r0 = r0.bitk;
        r25 = r0;
        r0 = r32;
        r0 = r0.write;
        r30 = r0;
        r0 = r32;
        r4 = r0.read;
        r0 = r30;
        if (r0 >= r4) goto L_0x0884;
    L_0x086f:
        r0 = r32;
        r4 = r0.read;
        r4 = r4 - r30;
        r26 = r4 + -1;
    L_0x0877:
        r0 = r32;
        r4 = r0.last;
        if (r4 != 0) goto L_0x088b;
    L_0x087d:
        r4 = 0;
        r0 = r32;
        r0.mode = r4;
        goto L_0x002e;
    L_0x0884:
        r0 = r32;
        r4 = r0.end;
        r26 = r4 - r30;
        goto L_0x0877;
    L_0x088b:
        r4 = 7;
        r0 = r32;
        r0.mode = r4;
    L_0x0890:
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r34 = r32.inflate_flush(r33, r34);
        r0 = r32;
        r0 = r0.write;
        r30 = r0;
        r0 = r32;
        r4 = r0.read;
        r0 = r30;
        if (r0 >= r4) goto L_0x08ee;
    L_0x08a8:
        r0 = r32;
        r4 = r0.read;
        r4 = r4 - r30;
        r26 = r4 + -1;
    L_0x08b0:
        r0 = r32;
        r4 = r0.read;
        r0 = r32;
        r5 = r0.write;
        if (r4 == r5) goto L_0x08f5;
    L_0x08ba:
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r28 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r28;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r4 = r32.inflate_flush(r33, r34);
        goto L_0x0069;
    L_0x08ee:
        r0 = r32;
        r4 = r0.end;
        r26 = r4 - r30;
        goto L_0x08b0;
    L_0x08f5:
        r4 = 8;
        r0 = r32;
        r0.mode = r4;
    L_0x08fb:
        r34 = 1;
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r28 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r28;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r4 = r32.inflate_flush(r33, r34);
        goto L_0x0069;
    L_0x0931:
        r34 = -3;
        r0 = r20;
        r1 = r32;
        r1.bitb = r0;
        r0 = r25;
        r1 = r32;
        r1.bitk = r0;
        r0 = r27;
        r1 = r33;
        r1.avail_in = r0;
        r0 = r33;
        r4 = r0.total_in;
        r0 = r33;
        r6 = r0.next_in_index;
        r6 = r28 - r6;
        r6 = (long) r6;
        r4 = r4 + r6;
        r0 = r33;
        r0.total_in = r4;
        r0 = r28;
        r1 = r33;
        r1.next_in_index = r0;
        r0 = r30;
        r1 = r32;
        r1.write = r0;
        r4 = r32.inflate_flush(r33, r34);
        goto L_0x0069;
    L_0x0967:
        r22 = r23;
        goto L_0x07ba;
    L_0x096b:
        r29 = r28;
        goto L_0x0368;
    L_0x096f:
        r29 = r28;
        goto L_0x0169;
    L_0x0973:
        r29 = r28;
        goto L_0x0071;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.jcraft.jzlib.InfBlocks.proc(com.jcraft.jzlib.ZStream, int):int");
    }

    /* access modifiers changed from: 0000 */
    public void free(ZStream z) {
        reset(z, null);
        this.window = null;
        this.hufts = null;
    }

    /* access modifiers changed from: 0000 */
    public void set_dictionary(byte[] d, int start, int n) {
        System.arraycopy(d, start, this.window, 0, n);
        this.write = n;
        this.read = n;
    }

    /* access modifiers changed from: 0000 */
    public int sync_point() {
        return this.mode == 1 ? 1 : 0;
    }

    /* access modifiers changed from: 0000 */
    public int inflate_flush(ZStream z, int r) {
        long adler32;
        int p = z.next_out_index;
        int q = this.read;
        int n = (q <= this.write ? this.write : this.end) - q;
        if (n > z.avail_out) {
            n = z.avail_out;
        }
        if (n != 0 && r == -5) {
            r = 0;
        }
        z.avail_out -= n;
        z.total_out += (long) n;
        if (this.checkfn != null) {
            adler32 = z._adler.adler32(this.check, this.window, q, n);
            this.check = adler32;
            z.adler = adler32;
        }
        System.arraycopy(this.window, q, z.next_out, p, n);
        p += n;
        q += n;
        if (q == this.end) {
            if (this.write == this.end) {
                this.write = 0;
            }
            n = this.write - 0;
            if (n > z.avail_out) {
                n = z.avail_out;
            }
            if (n != 0 && r == -5) {
                r = 0;
            }
            z.avail_out -= n;
            z.total_out += (long) n;
            if (this.checkfn != null) {
                adler32 = z._adler.adler32(this.check, this.window, 0, n);
                this.check = adler32;
                z.adler = adler32;
            }
            System.arraycopy(this.window, 0, z.next_out, p, n);
            p += n;
            q = 0 + n;
        }
        z.next_out_index = p;
        this.read = q;
        return r;
    }
}
