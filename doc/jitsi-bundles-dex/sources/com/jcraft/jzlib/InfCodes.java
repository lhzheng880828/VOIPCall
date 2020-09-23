package com.jcraft.jzlib;

import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;

final class InfCodes {
    private static final int BADCODE = 9;
    private static final int COPY = 5;
    private static final int DIST = 3;
    private static final int DISTEXT = 4;
    private static final int END = 8;
    private static final int LEN = 1;
    private static final int LENEXT = 2;
    private static final int LIT = 6;
    private static final int START = 0;
    private static final int WASH = 7;
    private static final int Z_BUF_ERROR = -5;
    private static final int Z_DATA_ERROR = -3;
    private static final int Z_ERRNO = -1;
    private static final int Z_MEM_ERROR = -4;
    private static final int Z_NEED_DICT = 2;
    private static final int Z_OK = 0;
    private static final int Z_STREAM_END = 1;
    private static final int Z_STREAM_ERROR = -2;
    private static final int Z_VERSION_ERROR = -6;
    private static final int[] inflate_mask = new int[]{0, 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095, 8191, 16383, 32767, InBandBytestreamManager.MAXIMUM_BLOCK_SIZE};
    byte dbits;
    int dist;
    int[] dtree;
    int dtree_index;
    int get;
    byte lbits;
    int len;
    int lit;
    int[] ltree;
    int ltree_index;
    int mode;
    int need;
    int[] tree;
    int tree_index = 0;

    InfCodes() {
    }

    /* access modifiers changed from: 0000 */
    public void init(int bl, int bd, int[] tl, int tl_index, int[] td, int td_index, ZStream z) {
        this.mode = 0;
        this.lbits = (byte) bl;
        this.dbits = (byte) bd;
        this.ltree = tl;
        this.ltree_index = tl_index;
        this.dtree = td;
        this.dtree_index = td_index;
        this.tree = null;
    }

    /* access modifiers changed from: 0000 */
    /* JADX WARNING: Missing block: B:48:0x02ce, code skipped:
            r15 = r24.need;
     */
    /* JADX WARNING: Missing block: B:49:0x02d4, code skipped:
            r20 = r19;
     */
    /* JADX WARNING: Missing block: B:50:0x02d6, code skipped:
            if (r16 >= r15) goto L_0x0324;
     */
    /* JADX WARNING: Missing block: B:51:0x02d8, code skipped:
            if (r18 == 0) goto L_0x02f0;
     */
    /* JADX WARNING: Missing block: B:52:0x02da, code skipped:
            r27 = 0;
            r18 = r18 - 1;
            r19 = r20 + 1;
            r11 = r11 | ((r26.next_in[r20] & 255) << r16);
            r16 = r16 + 8;
     */
    /* JADX WARNING: Missing block: B:53:0x02f0, code skipped:
            r25.bitb = r11;
            r25.bitk = r16;
            r26.avail_in = r18;
            r26.total_in += (long) (r20 - r26.next_in_index);
            r26.next_in_index = r20;
            r25.write = r21;
            r19 = r20;
     */
    /* JADX WARNING: Missing block: B:54:0x0324, code skipped:
            r23 = (r24.tree_index + (inflate_mask[r15] & r11)) * 3;
            r11 = r11 >> r24.tree[r23 + 1];
            r16 = r16 - r24.tree[r23 + 1];
            r12 = r24.tree[r23];
     */
    /* JADX WARNING: Missing block: B:55:0x034b, code skipped:
            if ((r12 & 16) == 0) goto L_0x0368;
     */
    /* JADX WARNING: Missing block: B:56:0x034d, code skipped:
            r24.get = r12 & 15;
            r24.dist = r24.tree[r23 + 2];
            r24.mode = 4;
            r19 = r20;
     */
    /* JADX WARNING: Missing block: B:58:0x036a, code skipped:
            if ((r12 & 64) != 0) goto L_0x0383;
     */
    /* JADX WARNING: Missing block: B:59:0x036c, code skipped:
            r24.need = r12;
            r24.tree_index = (r23 / 3) + r24.tree[r23 + 2];
            r19 = r20;
     */
    /* JADX WARNING: Missing block: B:60:0x0383, code skipped:
            r24.mode = 9;
            r26.msg = "invalid distance code";
            r25.bitb = r11;
            r25.bitk = r16;
            r26.avail_in = r18;
            r26.total_in += (long) (r20 - r26.next_in_index);
            r26.next_in_index = r20;
            r25.write = r21;
            r19 = r20;
     */
    /* JADX WARNING: Missing block: B:68:0x0433, code skipped:
            r13 = r21 - r24.dist;
     */
    /* JADX WARNING: Missing block: B:69:0x0439, code skipped:
            if (r13 >= 0) goto L_0x0466;
     */
    /* JADX WARNING: Missing block: B:70:0x043b, code skipped:
            r13 = r13 + r25.end;
     */
    /* JADX WARNING: Missing block: B:71:0x0441, code skipped:
            r22 = r21 + 1;
            r14 = r13 + 1;
            r25.window[r21] = r25.window[r13];
            r17 = r17 - 1;
     */
    /* JADX WARNING: Missing block: B:72:0x0457, code skipped:
            if (r14 != r25.end) goto L_0x06c3;
     */
    /* JADX WARNING: Missing block: B:73:0x0459, code skipped:
            r13 = 0;
     */
    /* JADX WARNING: Missing block: B:74:0x045a, code skipped:
            r24.len--;
            r21 = r22;
     */
    /* JADX WARNING: Missing block: B:76:0x046a, code skipped:
            if (r24.len == 0) goto L_0x0519;
     */
    /* JADX WARNING: Missing block: B:77:0x046c, code skipped:
            if (r17 != 0) goto L_0x0441;
     */
    /* JADX WARNING: Missing block: B:79:0x0474, code skipped:
            if (r21 != r25.end) goto L_0x048e;
     */
    /* JADX WARNING: Missing block: B:81:0x047a, code skipped:
            if (r25.read == 0) goto L_0x048e;
     */
    /* JADX WARNING: Missing block: B:82:0x047c, code skipped:
            r21 = 0;
     */
    /* JADX WARNING: Missing block: B:83:0x0484, code skipped:
            if (0 >= r25.read) goto L_0x0504;
     */
    /* JADX WARNING: Missing block: B:84:0x0486, code skipped:
            r17 = (r25.read - 0) - 1;
     */
    /* JADX WARNING: Missing block: B:85:0x048e, code skipped:
            if (r17 != 0) goto L_0x0441;
     */
    /* JADX WARNING: Missing block: B:86:0x0490, code skipped:
            r25.write = r21;
            r27 = r25.inflate_flush(r26, r27);
            r21 = r25.write;
     */
    /* JADX WARNING: Missing block: B:87:0x04a6, code skipped:
            if (r21 >= r25.read) goto L_0x050b;
     */
    /* JADX WARNING: Missing block: B:88:0x04a8, code skipped:
            r17 = (r25.read - r21) - 1;
     */
    /* JADX WARNING: Missing block: B:90:0x04b6, code skipped:
            if (r21 != r25.end) goto L_0x04d0;
     */
    /* JADX WARNING: Missing block: B:92:0x04bc, code skipped:
            if (r25.read == 0) goto L_0x04d0;
     */
    /* JADX WARNING: Missing block: B:93:0x04be, code skipped:
            r21 = 0;
     */
    /* JADX WARNING: Missing block: B:94:0x04c6, code skipped:
            if (0 >= r25.read) goto L_0x0512;
     */
    /* JADX WARNING: Missing block: B:95:0x04c8, code skipped:
            r17 = (r25.read - 0) - 1;
     */
    /* JADX WARNING: Missing block: B:96:0x04d0, code skipped:
            if (r17 != 0) goto L_0x0441;
     */
    /* JADX WARNING: Missing block: B:97:0x04d2, code skipped:
            r25.bitb = r11;
            r25.bitk = r16;
            r26.avail_in = r18;
            r26.total_in += (long) (r19 - r26.next_in_index);
            r26.next_in_index = r19;
            r25.write = r21;
     */
    /* JADX WARNING: Missing block: B:98:0x0504, code skipped:
            r17 = r25.end - 0;
     */
    /* JADX WARNING: Missing block: B:99:0x050b, code skipped:
            r17 = r25.end - r21;
     */
    /* JADX WARNING: Missing block: B:100:0x0512, code skipped:
            r17 = r25.end - 0;
     */
    /* JADX WARNING: Missing block: B:101:0x0519, code skipped:
            r24.mode = 0;
     */
    /* JADX WARNING: Missing block: B:140:0x06c3, code skipped:
            r13 = r14;
     */
    /* JADX WARNING: Missing block: B:175:?, code skipped:
            return r25.inflate_flush(r26, r27);
     */
    /* JADX WARNING: Missing block: B:176:?, code skipped:
            return r25.inflate_flush(r26, -3);
     */
    /* JADX WARNING: Missing block: B:178:?, code skipped:
            return r25.inflate_flush(r26, r27);
     */
    public int proc(com.jcraft.jzlib.InfBlocks r25, com.jcraft.jzlib.ZStream r26, int r27) {
        /*
        r24 = this;
        r11 = 0;
        r16 = 0;
        r19 = 0;
        r0 = r26;
        r0 = r0.next_in_index;
        r19 = r0;
        r0 = r26;
        r0 = r0.avail_in;
        r18 = r0;
        r0 = r25;
        r11 = r0.bitb;
        r0 = r25;
        r0 = r0.bitk;
        r16 = r0;
        r0 = r25;
        r0 = r0.write;
        r21 = r0;
        r0 = r25;
        r2 = r0.read;
        r0 = r21;
        if (r0 >= r2) goto L_0x006b;
    L_0x0029:
        r0 = r25;
        r2 = r0.read;
        r2 = r2 - r21;
        r17 = r2 + -1;
    L_0x0031:
        r0 = r24;
        r2 = r0.mode;
        switch(r2) {
            case 0: goto L_0x0072;
            case 1: goto L_0x012d;
            case 2: goto L_0x0248;
            case 3: goto L_0x02ce;
            case 4: goto L_0x03c5;
            case 5: goto L_0x0433;
            case 6: goto L_0x0520;
            case 7: goto L_0x05e7;
            case 8: goto L_0x065b;
            case 9: goto L_0x068f;
            default: goto L_0x0038;
        };
    L_0x0038:
        r27 = -2;
        r0 = r25;
        r0.bitb = r11;
        r0 = r16;
        r1 = r25;
        r1.bitk = r0;
        r0 = r18;
        r1 = r26;
        r1.avail_in = r0;
        r0 = r26;
        r2 = r0.total_in;
        r0 = r26;
        r4 = r0.next_in_index;
        r4 = r19 - r4;
        r4 = (long) r4;
        r2 = r2 + r4;
        r0 = r26;
        r0.total_in = r2;
        r0 = r19;
        r1 = r26;
        r1.next_in_index = r0;
        r0 = r21;
        r1 = r25;
        r1.write = r0;
        r2 = r25.inflate_flush(r26, r27);
    L_0x006a:
        return r2;
    L_0x006b:
        r0 = r25;
        r2 = r0.end;
        r17 = r2 - r21;
        goto L_0x0031;
    L_0x0072:
        r2 = 258; // 0x102 float:3.62E-43 double:1.275E-321;
        r0 = r17;
        if (r0 < r2) goto L_0x0110;
    L_0x0078:
        r2 = 10;
        r0 = r18;
        if (r0 < r2) goto L_0x0110;
    L_0x007e:
        r0 = r25;
        r0.bitb = r11;
        r0 = r16;
        r1 = r25;
        r1.bitk = r0;
        r0 = r18;
        r1 = r26;
        r1.avail_in = r0;
        r0 = r26;
        r2 = r0.total_in;
        r0 = r26;
        r4 = r0.next_in_index;
        r4 = r19 - r4;
        r4 = (long) r4;
        r2 = r2 + r4;
        r0 = r26;
        r0.total_in = r2;
        r0 = r19;
        r1 = r26;
        r1.next_in_index = r0;
        r0 = r21;
        r1 = r25;
        r1.write = r0;
        r0 = r24;
        r3 = r0.lbits;
        r0 = r24;
        r4 = r0.dbits;
        r0 = r24;
        r5 = r0.ltree;
        r0 = r24;
        r6 = r0.ltree_index;
        r0 = r24;
        r7 = r0.dtree;
        r0 = r24;
        r8 = r0.dtree_index;
        r2 = r24;
        r9 = r25;
        r10 = r26;
        r27 = r2.inflate_fast(r3, r4, r5, r6, r7, r8, r9, r10);
        r0 = r26;
        r0 = r0.next_in_index;
        r19 = r0;
        r0 = r26;
        r0 = r0.avail_in;
        r18 = r0;
        r0 = r25;
        r11 = r0.bitb;
        r0 = r25;
        r0 = r0.bitk;
        r16 = r0;
        r0 = r25;
        r0 = r0.write;
        r21 = r0;
        r0 = r25;
        r2 = r0.read;
        r0 = r21;
        if (r0 >= r2) goto L_0x0106;
    L_0x00f0:
        r0 = r25;
        r2 = r0.read;
        r2 = r2 - r21;
        r17 = r2 + -1;
    L_0x00f8:
        if (r27 == 0) goto L_0x0110;
    L_0x00fa:
        r2 = 1;
        r0 = r27;
        if (r0 != r2) goto L_0x010d;
    L_0x00ff:
        r2 = 7;
    L_0x0100:
        r0 = r24;
        r0.mode = r2;
        goto L_0x0031;
    L_0x0106:
        r0 = r25;
        r2 = r0.end;
        r17 = r2 - r21;
        goto L_0x00f8;
    L_0x010d:
        r2 = 9;
        goto L_0x0100;
    L_0x0110:
        r0 = r24;
        r2 = r0.lbits;
        r0 = r24;
        r0.need = r2;
        r0 = r24;
        r2 = r0.ltree;
        r0 = r24;
        r0.tree = r2;
        r0 = r24;
        r2 = r0.ltree_index;
        r0 = r24;
        r0.tree_index = r2;
        r2 = 1;
        r0 = r24;
        r0.mode = r2;
    L_0x012d:
        r0 = r24;
        r15 = r0.need;
        r20 = r19;
    L_0x0133:
        r0 = r16;
        if (r0 >= r15) goto L_0x0183;
    L_0x0137:
        if (r18 == 0) goto L_0x014f;
    L_0x0139:
        r27 = 0;
        r18 = r18 + -1;
        r0 = r26;
        r2 = r0.next_in;
        r19 = r20 + 1;
        r2 = r2[r20];
        r2 = r2 & 255;
        r2 = r2 << r16;
        r11 = r11 | r2;
        r16 = r16 + 8;
        r20 = r19;
        goto L_0x0133;
    L_0x014f:
        r0 = r25;
        r0.bitb = r11;
        r0 = r16;
        r1 = r25;
        r1.bitk = r0;
        r0 = r18;
        r1 = r26;
        r1.avail_in = r0;
        r0 = r26;
        r2 = r0.total_in;
        r0 = r26;
        r4 = r0.next_in_index;
        r4 = r20 - r4;
        r4 = (long) r4;
        r2 = r2 + r4;
        r0 = r26;
        r0.total_in = r2;
        r0 = r20;
        r1 = r26;
        r1.next_in_index = r0;
        r0 = r21;
        r1 = r25;
        r1.write = r0;
        r2 = r25.inflate_flush(r26, r27);
        r19 = r20;
        goto L_0x006a;
    L_0x0183:
        r0 = r24;
        r2 = r0.tree_index;
        r3 = inflate_mask;
        r3 = r3[r15];
        r3 = r3 & r11;
        r2 = r2 + r3;
        r23 = r2 * 3;
        r0 = r24;
        r2 = r0.tree;
        r3 = r23 + 1;
        r2 = r2[r3];
        r11 = r11 >>> r2;
        r0 = r24;
        r2 = r0.tree;
        r3 = r23 + 1;
        r2 = r2[r3];
        r16 = r16 - r2;
        r0 = r24;
        r2 = r0.tree;
        r12 = r2[r23];
        if (r12 != 0) goto L_0x01bf;
    L_0x01aa:
        r0 = r24;
        r2 = r0.tree;
        r3 = r23 + 2;
        r2 = r2[r3];
        r0 = r24;
        r0.lit = r2;
        r2 = 6;
        r0 = r24;
        r0.mode = r2;
        r19 = r20;
        goto L_0x0031;
    L_0x01bf:
        r2 = r12 & 16;
        if (r2 == 0) goto L_0x01de;
    L_0x01c3:
        r2 = r12 & 15;
        r0 = r24;
        r0.get = r2;
        r0 = r24;
        r2 = r0.tree;
        r3 = r23 + 2;
        r2 = r2[r3];
        r0 = r24;
        r0.len = r2;
        r2 = 2;
        r0 = r24;
        r0.mode = r2;
        r19 = r20;
        goto L_0x0031;
    L_0x01de:
        r2 = r12 & 64;
        if (r2 != 0) goto L_0x01f9;
    L_0x01e2:
        r0 = r24;
        r0.need = r12;
        r2 = r23 / 3;
        r0 = r24;
        r3 = r0.tree;
        r4 = r23 + 2;
        r3 = r3[r4];
        r2 = r2 + r3;
        r0 = r24;
        r0.tree_index = r2;
        r19 = r20;
        goto L_0x0031;
    L_0x01f9:
        r2 = r12 & 32;
        if (r2 == 0) goto L_0x0206;
    L_0x01fd:
        r2 = 7;
        r0 = r24;
        r0.mode = r2;
        r19 = r20;
        goto L_0x0031;
    L_0x0206:
        r2 = 9;
        r0 = r24;
        r0.mode = r2;
        r2 = "invalid literal/length code";
        r0 = r26;
        r0.msg = r2;
        r27 = -3;
        r0 = r25;
        r0.bitb = r11;
        r0 = r16;
        r1 = r25;
        r1.bitk = r0;
        r0 = r18;
        r1 = r26;
        r1.avail_in = r0;
        r0 = r26;
        r2 = r0.total_in;
        r0 = r26;
        r4 = r0.next_in_index;
        r4 = r20 - r4;
        r4 = (long) r4;
        r2 = r2 + r4;
        r0 = r26;
        r0.total_in = r2;
        r0 = r20;
        r1 = r26;
        r1.next_in_index = r0;
        r0 = r21;
        r1 = r25;
        r1.write = r0;
        r2 = r25.inflate_flush(r26, r27);
        r19 = r20;
        goto L_0x006a;
    L_0x0248:
        r0 = r24;
        r15 = r0.get;
        r20 = r19;
    L_0x024e:
        r0 = r16;
        if (r0 >= r15) goto L_0x029e;
    L_0x0252:
        if (r18 == 0) goto L_0x026a;
    L_0x0254:
        r27 = 0;
        r18 = r18 + -1;
        r0 = r26;
        r2 = r0.next_in;
        r19 = r20 + 1;
        r2 = r2[r20];
        r2 = r2 & 255;
        r2 = r2 << r16;
        r11 = r11 | r2;
        r16 = r16 + 8;
        r20 = r19;
        goto L_0x024e;
    L_0x026a:
        r0 = r25;
        r0.bitb = r11;
        r0 = r16;
        r1 = r25;
        r1.bitk = r0;
        r0 = r18;
        r1 = r26;
        r1.avail_in = r0;
        r0 = r26;
        r2 = r0.total_in;
        r0 = r26;
        r4 = r0.next_in_index;
        r4 = r20 - r4;
        r4 = (long) r4;
        r2 = r2 + r4;
        r0 = r26;
        r0.total_in = r2;
        r0 = r20;
        r1 = r26;
        r1.next_in_index = r0;
        r0 = r21;
        r1 = r25;
        r1.write = r0;
        r2 = r25.inflate_flush(r26, r27);
        r19 = r20;
        goto L_0x006a;
    L_0x029e:
        r0 = r24;
        r2 = r0.len;
        r3 = inflate_mask;
        r3 = r3[r15];
        r3 = r3 & r11;
        r2 = r2 + r3;
        r0 = r24;
        r0.len = r2;
        r11 = r11 >> r15;
        r16 = r16 - r15;
        r0 = r24;
        r2 = r0.dbits;
        r0 = r24;
        r0.need = r2;
        r0 = r24;
        r2 = r0.dtree;
        r0 = r24;
        r0.tree = r2;
        r0 = r24;
        r2 = r0.dtree_index;
        r0 = r24;
        r0.tree_index = r2;
        r2 = 3;
        r0 = r24;
        r0.mode = r2;
        r19 = r20;
    L_0x02ce:
        r0 = r24;
        r15 = r0.need;
        r20 = r19;
    L_0x02d4:
        r0 = r16;
        if (r0 >= r15) goto L_0x0324;
    L_0x02d8:
        if (r18 == 0) goto L_0x02f0;
    L_0x02da:
        r27 = 0;
        r18 = r18 + -1;
        r0 = r26;
        r2 = r0.next_in;
        r19 = r20 + 1;
        r2 = r2[r20];
        r2 = r2 & 255;
        r2 = r2 << r16;
        r11 = r11 | r2;
        r16 = r16 + 8;
        r20 = r19;
        goto L_0x02d4;
    L_0x02f0:
        r0 = r25;
        r0.bitb = r11;
        r0 = r16;
        r1 = r25;
        r1.bitk = r0;
        r0 = r18;
        r1 = r26;
        r1.avail_in = r0;
        r0 = r26;
        r2 = r0.total_in;
        r0 = r26;
        r4 = r0.next_in_index;
        r4 = r20 - r4;
        r4 = (long) r4;
        r2 = r2 + r4;
        r0 = r26;
        r0.total_in = r2;
        r0 = r20;
        r1 = r26;
        r1.next_in_index = r0;
        r0 = r21;
        r1 = r25;
        r1.write = r0;
        r2 = r25.inflate_flush(r26, r27);
        r19 = r20;
        goto L_0x006a;
    L_0x0324:
        r0 = r24;
        r2 = r0.tree_index;
        r3 = inflate_mask;
        r3 = r3[r15];
        r3 = r3 & r11;
        r2 = r2 + r3;
        r23 = r2 * 3;
        r0 = r24;
        r2 = r0.tree;
        r3 = r23 + 1;
        r2 = r2[r3];
        r11 = r11 >> r2;
        r0 = r24;
        r2 = r0.tree;
        r3 = r23 + 1;
        r2 = r2[r3];
        r16 = r16 - r2;
        r0 = r24;
        r2 = r0.tree;
        r12 = r2[r23];
        r2 = r12 & 16;
        if (r2 == 0) goto L_0x0368;
    L_0x034d:
        r2 = r12 & 15;
        r0 = r24;
        r0.get = r2;
        r0 = r24;
        r2 = r0.tree;
        r3 = r23 + 2;
        r2 = r2[r3];
        r0 = r24;
        r0.dist = r2;
        r2 = 4;
        r0 = r24;
        r0.mode = r2;
        r19 = r20;
        goto L_0x0031;
    L_0x0368:
        r2 = r12 & 64;
        if (r2 != 0) goto L_0x0383;
    L_0x036c:
        r0 = r24;
        r0.need = r12;
        r2 = r23 / 3;
        r0 = r24;
        r3 = r0.tree;
        r4 = r23 + 2;
        r3 = r3[r4];
        r2 = r2 + r3;
        r0 = r24;
        r0.tree_index = r2;
        r19 = r20;
        goto L_0x0031;
    L_0x0383:
        r2 = 9;
        r0 = r24;
        r0.mode = r2;
        r2 = "invalid distance code";
        r0 = r26;
        r0.msg = r2;
        r27 = -3;
        r0 = r25;
        r0.bitb = r11;
        r0 = r16;
        r1 = r25;
        r1.bitk = r0;
        r0 = r18;
        r1 = r26;
        r1.avail_in = r0;
        r0 = r26;
        r2 = r0.total_in;
        r0 = r26;
        r4 = r0.next_in_index;
        r4 = r20 - r4;
        r4 = (long) r4;
        r2 = r2 + r4;
        r0 = r26;
        r0.total_in = r2;
        r0 = r20;
        r1 = r26;
        r1.next_in_index = r0;
        r0 = r21;
        r1 = r25;
        r1.write = r0;
        r2 = r25.inflate_flush(r26, r27);
        r19 = r20;
        goto L_0x006a;
    L_0x03c5:
        r0 = r24;
        r15 = r0.get;
        r20 = r19;
    L_0x03cb:
        r0 = r16;
        if (r0 >= r15) goto L_0x041b;
    L_0x03cf:
        if (r18 == 0) goto L_0x03e7;
    L_0x03d1:
        r27 = 0;
        r18 = r18 + -1;
        r0 = r26;
        r2 = r0.next_in;
        r19 = r20 + 1;
        r2 = r2[r20];
        r2 = r2 & 255;
        r2 = r2 << r16;
        r11 = r11 | r2;
        r16 = r16 + 8;
        r20 = r19;
        goto L_0x03cb;
    L_0x03e7:
        r0 = r25;
        r0.bitb = r11;
        r0 = r16;
        r1 = r25;
        r1.bitk = r0;
        r0 = r18;
        r1 = r26;
        r1.avail_in = r0;
        r0 = r26;
        r2 = r0.total_in;
        r0 = r26;
        r4 = r0.next_in_index;
        r4 = r20 - r4;
        r4 = (long) r4;
        r2 = r2 + r4;
        r0 = r26;
        r0.total_in = r2;
        r0 = r20;
        r1 = r26;
        r1.next_in_index = r0;
        r0 = r21;
        r1 = r25;
        r1.write = r0;
        r2 = r25.inflate_flush(r26, r27);
        r19 = r20;
        goto L_0x006a;
    L_0x041b:
        r0 = r24;
        r2 = r0.dist;
        r3 = inflate_mask;
        r3 = r3[r15];
        r3 = r3 & r11;
        r2 = r2 + r3;
        r0 = r24;
        r0.dist = r2;
        r11 = r11 >> r15;
        r16 = r16 - r15;
        r2 = 5;
        r0 = r24;
        r0.mode = r2;
        r19 = r20;
    L_0x0433:
        r0 = r24;
        r2 = r0.dist;
        r13 = r21 - r2;
    L_0x0439:
        if (r13 >= 0) goto L_0x0466;
    L_0x043b:
        r0 = r25;
        r2 = r0.end;
        r13 = r13 + r2;
        goto L_0x0439;
    L_0x0441:
        r0 = r25;
        r2 = r0.window;
        r22 = r21 + 1;
        r0 = r25;
        r3 = r0.window;
        r14 = r13 + 1;
        r3 = r3[r13];
        r2[r21] = r3;
        r17 = r17 + -1;
        r0 = r25;
        r2 = r0.end;
        if (r14 != r2) goto L_0x06c3;
    L_0x0459:
        r13 = 0;
    L_0x045a:
        r0 = r24;
        r2 = r0.len;
        r2 = r2 + -1;
        r0 = r24;
        r0.len = r2;
        r21 = r22;
    L_0x0466:
        r0 = r24;
        r2 = r0.len;
        if (r2 == 0) goto L_0x0519;
    L_0x046c:
        if (r17 != 0) goto L_0x0441;
    L_0x046e:
        r0 = r25;
        r2 = r0.end;
        r0 = r21;
        if (r0 != r2) goto L_0x048e;
    L_0x0476:
        r0 = r25;
        r2 = r0.read;
        if (r2 == 0) goto L_0x048e;
    L_0x047c:
        r21 = 0;
        r0 = r25;
        r2 = r0.read;
        r0 = r21;
        if (r0 >= r2) goto L_0x0504;
    L_0x0486:
        r0 = r25;
        r2 = r0.read;
        r2 = r2 - r21;
        r17 = r2 + -1;
    L_0x048e:
        if (r17 != 0) goto L_0x0441;
    L_0x0490:
        r0 = r21;
        r1 = r25;
        r1.write = r0;
        r27 = r25.inflate_flush(r26, r27);
        r0 = r25;
        r0 = r0.write;
        r21 = r0;
        r0 = r25;
        r2 = r0.read;
        r0 = r21;
        if (r0 >= r2) goto L_0x050b;
    L_0x04a8:
        r0 = r25;
        r2 = r0.read;
        r2 = r2 - r21;
        r17 = r2 + -1;
    L_0x04b0:
        r0 = r25;
        r2 = r0.end;
        r0 = r21;
        if (r0 != r2) goto L_0x04d0;
    L_0x04b8:
        r0 = r25;
        r2 = r0.read;
        if (r2 == 0) goto L_0x04d0;
    L_0x04be:
        r21 = 0;
        r0 = r25;
        r2 = r0.read;
        r0 = r21;
        if (r0 >= r2) goto L_0x0512;
    L_0x04c8:
        r0 = r25;
        r2 = r0.read;
        r2 = r2 - r21;
        r17 = r2 + -1;
    L_0x04d0:
        if (r17 != 0) goto L_0x0441;
    L_0x04d2:
        r0 = r25;
        r0.bitb = r11;
        r0 = r16;
        r1 = r25;
        r1.bitk = r0;
        r0 = r18;
        r1 = r26;
        r1.avail_in = r0;
        r0 = r26;
        r2 = r0.total_in;
        r0 = r26;
        r4 = r0.next_in_index;
        r4 = r19 - r4;
        r4 = (long) r4;
        r2 = r2 + r4;
        r0 = r26;
        r0.total_in = r2;
        r0 = r19;
        r1 = r26;
        r1.next_in_index = r0;
        r0 = r21;
        r1 = r25;
        r1.write = r0;
        r2 = r25.inflate_flush(r26, r27);
        goto L_0x006a;
    L_0x0504:
        r0 = r25;
        r2 = r0.end;
        r17 = r2 - r21;
        goto L_0x048e;
    L_0x050b:
        r0 = r25;
        r2 = r0.end;
        r17 = r2 - r21;
        goto L_0x04b0;
    L_0x0512:
        r0 = r25;
        r2 = r0.end;
        r17 = r2 - r21;
        goto L_0x04d0;
    L_0x0519:
        r2 = 0;
        r0 = r24;
        r0.mode = r2;
        goto L_0x0031;
    L_0x0520:
        if (r17 != 0) goto L_0x05cd;
    L_0x0522:
        r0 = r25;
        r2 = r0.end;
        r0 = r21;
        if (r0 != r2) goto L_0x0542;
    L_0x052a:
        r0 = r25;
        r2 = r0.read;
        if (r2 == 0) goto L_0x0542;
    L_0x0530:
        r21 = 0;
        r0 = r25;
        r2 = r0.read;
        r0 = r21;
        if (r0 >= r2) goto L_0x05b8;
    L_0x053a:
        r0 = r25;
        r2 = r0.read;
        r2 = r2 - r21;
        r17 = r2 + -1;
    L_0x0542:
        if (r17 != 0) goto L_0x05cd;
    L_0x0544:
        r0 = r21;
        r1 = r25;
        r1.write = r0;
        r27 = r25.inflate_flush(r26, r27);
        r0 = r25;
        r0 = r0.write;
        r21 = r0;
        r0 = r25;
        r2 = r0.read;
        r0 = r21;
        if (r0 >= r2) goto L_0x05bf;
    L_0x055c:
        r0 = r25;
        r2 = r0.read;
        r2 = r2 - r21;
        r17 = r2 + -1;
    L_0x0564:
        r0 = r25;
        r2 = r0.end;
        r0 = r21;
        if (r0 != r2) goto L_0x0584;
    L_0x056c:
        r0 = r25;
        r2 = r0.read;
        if (r2 == 0) goto L_0x0584;
    L_0x0572:
        r21 = 0;
        r0 = r25;
        r2 = r0.read;
        r0 = r21;
        if (r0 >= r2) goto L_0x05c6;
    L_0x057c:
        r0 = r25;
        r2 = r0.read;
        r2 = r2 - r21;
        r17 = r2 + -1;
    L_0x0584:
        if (r17 != 0) goto L_0x05cd;
    L_0x0586:
        r0 = r25;
        r0.bitb = r11;
        r0 = r16;
        r1 = r25;
        r1.bitk = r0;
        r0 = r18;
        r1 = r26;
        r1.avail_in = r0;
        r0 = r26;
        r2 = r0.total_in;
        r0 = r26;
        r4 = r0.next_in_index;
        r4 = r19 - r4;
        r4 = (long) r4;
        r2 = r2 + r4;
        r0 = r26;
        r0.total_in = r2;
        r0 = r19;
        r1 = r26;
        r1.next_in_index = r0;
        r0 = r21;
        r1 = r25;
        r1.write = r0;
        r2 = r25.inflate_flush(r26, r27);
        goto L_0x006a;
    L_0x05b8:
        r0 = r25;
        r2 = r0.end;
        r17 = r2 - r21;
        goto L_0x0542;
    L_0x05bf:
        r0 = r25;
        r2 = r0.end;
        r17 = r2 - r21;
        goto L_0x0564;
    L_0x05c6:
        r0 = r25;
        r2 = r0.end;
        r17 = r2 - r21;
        goto L_0x0584;
    L_0x05cd:
        r27 = 0;
        r0 = r25;
        r2 = r0.window;
        r22 = r21 + 1;
        r0 = r24;
        r3 = r0.lit;
        r3 = (byte) r3;
        r2[r21] = r3;
        r17 = r17 + -1;
        r2 = 0;
        r0 = r24;
        r0.mode = r2;
        r21 = r22;
        goto L_0x0031;
    L_0x05e7:
        r2 = 7;
        r0 = r16;
        if (r0 <= r2) goto L_0x05f2;
    L_0x05ec:
        r16 = r16 + -8;
        r18 = r18 + 1;
        r19 = r19 + -1;
    L_0x05f2:
        r0 = r21;
        r1 = r25;
        r1.write = r0;
        r27 = r25.inflate_flush(r26, r27);
        r0 = r25;
        r0 = r0.write;
        r21 = r0;
        r0 = r25;
        r2 = r0.read;
        r0 = r21;
        if (r0 >= r2) goto L_0x064e;
    L_0x060a:
        r0 = r25;
        r2 = r0.read;
        r2 = r2 - r21;
        r17 = r2 + -1;
    L_0x0612:
        r0 = r25;
        r2 = r0.read;
        r0 = r25;
        r3 = r0.write;
        if (r2 == r3) goto L_0x0655;
    L_0x061c:
        r0 = r25;
        r0.bitb = r11;
        r0 = r16;
        r1 = r25;
        r1.bitk = r0;
        r0 = r18;
        r1 = r26;
        r1.avail_in = r0;
        r0 = r26;
        r2 = r0.total_in;
        r0 = r26;
        r4 = r0.next_in_index;
        r4 = r19 - r4;
        r4 = (long) r4;
        r2 = r2 + r4;
        r0 = r26;
        r0.total_in = r2;
        r0 = r19;
        r1 = r26;
        r1.next_in_index = r0;
        r0 = r21;
        r1 = r25;
        r1.write = r0;
        r2 = r25.inflate_flush(r26, r27);
        goto L_0x006a;
    L_0x064e:
        r0 = r25;
        r2 = r0.end;
        r17 = r2 - r21;
        goto L_0x0612;
    L_0x0655:
        r2 = 8;
        r0 = r24;
        r0.mode = r2;
    L_0x065b:
        r27 = 1;
        r0 = r25;
        r0.bitb = r11;
        r0 = r16;
        r1 = r25;
        r1.bitk = r0;
        r0 = r18;
        r1 = r26;
        r1.avail_in = r0;
        r0 = r26;
        r2 = r0.total_in;
        r0 = r26;
        r4 = r0.next_in_index;
        r4 = r19 - r4;
        r4 = (long) r4;
        r2 = r2 + r4;
        r0 = r26;
        r0.total_in = r2;
        r0 = r19;
        r1 = r26;
        r1.next_in_index = r0;
        r0 = r21;
        r1 = r25;
        r1.write = r0;
        r2 = r25.inflate_flush(r26, r27);
        goto L_0x006a;
    L_0x068f:
        r27 = -3;
        r0 = r25;
        r0.bitb = r11;
        r0 = r16;
        r1 = r25;
        r1.bitk = r0;
        r0 = r18;
        r1 = r26;
        r1.avail_in = r0;
        r0 = r26;
        r2 = r0.total_in;
        r0 = r26;
        r4 = r0.next_in_index;
        r4 = r19 - r4;
        r4 = (long) r4;
        r2 = r2 + r4;
        r0 = r26;
        r0.total_in = r2;
        r0 = r19;
        r1 = r26;
        r1.next_in_index = r0;
        r0 = r21;
        r1 = r25;
        r1.write = r0;
        r2 = r25.inflate_flush(r26, r27);
        goto L_0x006a;
    L_0x06c3:
        r13 = r14;
        goto L_0x045a;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.jcraft.jzlib.InfCodes.proc(com.jcraft.jzlib.InfBlocks, com.jcraft.jzlib.ZStream, int):int");
    }

    /* access modifiers changed from: 0000 */
    public void free(ZStream z) {
    }

    /* access modifiers changed from: 0000 */
    public int inflate_fast(int bl, int bd, int[] tl, int tl_index, int[] td, int td_index, InfBlocks s, ZStream z) {
        int m;
        int c;
        int p = z.next_in_index;
        int n = z.avail_in;
        int b = s.bitb;
        int k = s.bitk;
        int q = s.write;
        if (q < s.read) {
            m = (s.read - q) - 1;
        } else {
            m = s.end - q;
        }
        int ml = inflate_mask[bl];
        int md = inflate_mask[bd];
        int q2 = q;
        while (true) {
            int p2 = p;
            if (k < 20) {
                n--;
                p = p2 + 1;
                b |= (z.next_in[p2] & 255) << k;
                k += 8;
            } else {
                int t = b & ml;
                int[] tp = tl;
                int tp_index = tl_index;
                int tp_index_t_3 = (tp_index + t) * 3;
                int e = tp[tp_index_t_3];
                if (e == 0) {
                    b >>= tp[tp_index_t_3 + 1];
                    k -= tp[tp_index_t_3 + 1];
                    q = q2 + 1;
                    s.window[q2] = (byte) tp[tp_index_t_3 + 2];
                    m--;
                    p = p2;
                } else {
                    do {
                        b >>= tp[tp_index_t_3 + 1];
                        k -= tp[tp_index_t_3 + 1];
                        if ((e & 16) != 0) {
                            e &= 15;
                            c = tp[tp_index_t_3 + 2] + (inflate_mask[e] & b);
                            b >>= e;
                            k -= e;
                            while (k < 15) {
                                n--;
                                b |= (z.next_in[p2] & 255) << k;
                                k += 8;
                                p2++;
                            }
                            t = b & md;
                            tp = td;
                            tp_index = td_index;
                            tp_index_t_3 = (tp_index + t) * 3;
                            e = tp[tp_index_t_3];
                            while (true) {
                                b >>= tp[tp_index_t_3 + 1];
                                k -= tp[tp_index_t_3 + 1];
                                if ((e & 16) != 0) {
                                    int r;
                                    int r2;
                                    e &= 15;
                                    while (k < e) {
                                        n--;
                                        b |= (z.next_in[p2] & 255) << k;
                                        k += 8;
                                        p2++;
                                    }
                                    int d = tp[tp_index_t_3 + 2] + (inflate_mask[e] & b);
                                    b >>= e;
                                    k -= e;
                                    m -= c;
                                    if (q2 >= d) {
                                        r = q2 - d;
                                        if (q2 - r <= 0 || 2 <= q2 - r) {
                                            System.arraycopy(s.window, r, s.window, q2, 2);
                                            q = q2 + 2;
                                            r += 2;
                                            c -= 2;
                                        } else {
                                            q = q2 + 1;
                                            r2 = r + 1;
                                            s.window[q2] = s.window[r];
                                            q2 = q + 1;
                                            r = r2 + 1;
                                            s.window[q] = s.window[r2];
                                            c -= 2;
                                            q = q2;
                                        }
                                    } else {
                                        r = q2 - d;
                                        do {
                                            r += s.end;
                                        } while (r < 0);
                                        e = s.end - r;
                                        if (c > e) {
                                            c -= e;
                                            if (q2 - r <= 0 || e <= q2 - r) {
                                                System.arraycopy(s.window, r, s.window, q2, e);
                                                q = q2 + e;
                                                r += e;
                                            } else {
                                                while (true) {
                                                    q = q2;
                                                    q2 = q + 1;
                                                    r2 = r + 1;
                                                    s.window[q] = s.window[r];
                                                    e--;
                                                    if (e == 0) {
                                                        break;
                                                    }
                                                    r = r2;
                                                }
                                                r = r2;
                                                q = q2;
                                            }
                                            r = 0;
                                        } else {
                                            q = q2;
                                        }
                                    }
                                    if (q - r <= 0 || c <= q - r) {
                                        System.arraycopy(s.window, r, s.window, q, c);
                                        q += c;
                                        r += c;
                                        p = p2;
                                    } else {
                                        while (true) {
                                            q2 = q + 1;
                                            r2 = r + 1;
                                            s.window[q] = s.window[r];
                                            c--;
                                            if (c == 0) {
                                                break;
                                            }
                                            r = r2;
                                            q = q2;
                                        }
                                        q = q2;
                                        p = p2;
                                    }
                                } else if ((e & 64) == 0) {
                                    t = (t + tp[tp_index_t_3 + 2]) + (inflate_mask[e] & b);
                                    tp_index_t_3 = (tp_index + t) * 3;
                                    e = tp[tp_index_t_3];
                                } else {
                                    z.msg = "invalid distance code";
                                    c = z.avail_in - n;
                                    if ((k >> 3) < c) {
                                        c = k >> 3;
                                    }
                                    n += c;
                                    p = p2 - c;
                                    k -= c << 3;
                                    s.bitb = b;
                                    s.bitk = k;
                                    z.avail_in = n;
                                    z.total_in += (long) (p - z.next_in_index);
                                    z.next_in_index = p;
                                    s.write = q2;
                                    q = q2;
                                    return -3;
                                }
                            }
                        } else if ((e & 64) == 0) {
                            t = (t + tp[tp_index_t_3 + 2]) + (inflate_mask[e] & b);
                            tp_index_t_3 = (tp_index + t) * 3;
                            e = tp[tp_index_t_3];
                        } else if ((e & 32) != 0) {
                            c = z.avail_in - n;
                            if ((k >> 3) < c) {
                                c = k >> 3;
                            }
                            n += c;
                            p = p2 - c;
                            k -= c << 3;
                            s.bitb = b;
                            s.bitk = k;
                            z.avail_in = n;
                            z.total_in += (long) (p - z.next_in_index);
                            z.next_in_index = p;
                            s.write = q2;
                            q = q2;
                            return 1;
                        } else {
                            z.msg = "invalid literal/length code";
                            c = z.avail_in - n;
                            if ((k >> 3) < c) {
                                c = k >> 3;
                            }
                            n += c;
                            p = p2 - c;
                            k -= c << 3;
                            s.bitb = b;
                            s.bitk = k;
                            z.avail_in = n;
                            z.total_in += (long) (p - z.next_in_index);
                            z.next_in_index = p;
                            s.write = q2;
                            q = q2;
                            return -3;
                        }
                    } while (e != 0);
                    b >>= tp[tp_index_t_3 + 1];
                    k -= tp[tp_index_t_3 + 1];
                    q = q2 + 1;
                    s.window[q2] = (byte) tp[tp_index_t_3 + 2];
                    m--;
                    p = p2;
                }
                if (m < 258 || n < 10) {
                    c = z.avail_in - n;
                } else {
                    q2 = q;
                }
            }
        }
        c = z.avail_in - n;
        if ((k >> 3) < c) {
            c = k >> 3;
        }
        n += c;
        p -= c;
        k -= c << 3;
        s.bitb = b;
        s.bitk = k;
        z.avail_in = n;
        z.total_in += (long) (p - z.next_in_index);
        z.next_in_index = p;
        s.write = q;
        return 0;
    }
}
