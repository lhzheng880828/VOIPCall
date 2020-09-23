package com.jcraft.jzlib;

final class Inflate {
    private static final int BAD = 13;
    private static final int BLOCKS = 7;
    private static final int CHECK1 = 11;
    private static final int CHECK2 = 10;
    private static final int CHECK3 = 9;
    private static final int CHECK4 = 8;
    private static final int DICT0 = 6;
    private static final int DICT1 = 5;
    private static final int DICT2 = 4;
    private static final int DICT3 = 3;
    private static final int DICT4 = 2;
    private static final int DONE = 12;
    private static final int FLAG = 1;
    private static final int MAX_WBITS = 15;
    private static final int METHOD = 0;
    private static final int PRESET_DICT = 32;
    private static final int Z_BUF_ERROR = -5;
    private static final int Z_DATA_ERROR = -3;
    private static final int Z_DEFLATED = 8;
    private static final int Z_ERRNO = -1;
    static final int Z_FINISH = 4;
    static final int Z_FULL_FLUSH = 3;
    private static final int Z_MEM_ERROR = -4;
    private static final int Z_NEED_DICT = 2;
    static final int Z_NO_FLUSH = 0;
    private static final int Z_OK = 0;
    static final int Z_PARTIAL_FLUSH = 1;
    private static final int Z_STREAM_END = 1;
    private static final int Z_STREAM_ERROR = -2;
    static final int Z_SYNC_FLUSH = 2;
    private static final int Z_VERSION_ERROR = -6;
    private static byte[] mark = new byte[]{(byte) 0, (byte) 0, (byte) -1, (byte) -1};
    InfBlocks blocks;
    int marker;
    int method;
    int mode;
    long need;
    int nowrap;
    long[] was = new long[1];
    int wbits;

    Inflate() {
    }

    /* access modifiers changed from: 0000 */
    public int inflateReset(ZStream z) {
        if (z == null || z.istate == null) {
            return -2;
        }
        int i;
        z.total_out = 0;
        z.total_in = 0;
        z.msg = null;
        Inflate inflate = z.istate;
        if (z.istate.nowrap != 0) {
            i = 7;
        } else {
            i = 0;
        }
        inflate.mode = i;
        z.istate.blocks.reset(z, null);
        return 0;
    }

    /* access modifiers changed from: 0000 */
    public int inflateEnd(ZStream z) {
        if (this.blocks != null) {
            this.blocks.free(z);
        }
        this.blocks = null;
        return 0;
    }

    /* access modifiers changed from: 0000 */
    public int inflateInit(ZStream z, int w) {
        Object obj = null;
        z.msg = null;
        this.blocks = null;
        this.nowrap = 0;
        if (w < 0) {
            w = -w;
            this.nowrap = 1;
        }
        if (w < 8 || w > 15) {
            inflateEnd(z);
            return -2;
        }
        this.wbits = w;
        Inflate inflate = z.istate;
        if (z.istate.nowrap == 0) {
            Inflate obj2 = this;
        }
        inflate.blocks = new InfBlocks(z, obj2, 1 << w);
        inflateReset(z);
        return 0;
    }

    /* access modifiers changed from: 0000 */
    /* JADX WARNING: Missing block: B:57:0x01d8, code skipped:
            if (r11.avail_in == 0) goto L_0x000b;
     */
    /* JADX WARNING: Missing block: B:58:0x01da, code skipped:
            r1 = r12;
            r11.avail_in--;
            r11.total_in++;
            r2 = r11.istate;
            r3 = r11.next_in;
            r4 = r11.next_in_index;
            r11.next_in_index = r4 + 1;
            r2.need = ((long) ((r3[r4] & 255) << 24)) & 4278190080L;
            r11.istate.mode = 9;
     */
    /* JADX WARNING: Missing block: B:60:0x0209, code skipped:
            if (r11.avail_in == 0) goto L_0x000b;
     */
    /* JADX WARNING: Missing block: B:61:0x020b, code skipped:
            r1 = r12;
            r11.avail_in--;
            r11.total_in++;
            r2 = r11.istate;
            r4 = r2.need;
            r3 = r11.next_in;
            r6 = r11.next_in_index;
            r11.next_in_index = r6 + 1;
            r2.need = r4 + (((long) ((r3[r6] & 255) << 16)) & 16711680);
            r11.istate.mode = 10;
     */
    /* JADX WARNING: Missing block: B:63:0x023b, code skipped:
            if (r11.avail_in == 0) goto L_0x000b;
     */
    /* JADX WARNING: Missing block: B:64:0x023d, code skipped:
            r1 = r12;
            r11.avail_in--;
            r11.total_in++;
            r2 = r11.istate;
            r4 = r2.need;
            r3 = r11.next_in;
            r6 = r11.next_in_index;
            r11.next_in_index = r6 + 1;
            r2.need = r4 + (((long) ((r3[r6] & 255) << 8)) & 65280);
            r11.istate.mode = 11;
     */
    /* JADX WARNING: Missing block: B:66:0x026d, code skipped:
            if (r11.avail_in == 0) goto L_0x000b;
     */
    /* JADX WARNING: Missing block: B:67:0x026f, code skipped:
            r1 = r12;
            r11.avail_in--;
            r11.total_in++;
            r2 = r11.istate;
            r4 = r2.need;
            r3 = r11.next_in;
            r6 = r11.next_in_index;
            r11.next_in_index = r6 + 1;
            r2.need = r4 + (((long) r3[r6]) & 255);
     */
    /* JADX WARNING: Missing block: B:68:0x029f, code skipped:
            if (((int) r11.istate.was[0]) == ((int) r11.istate.need)) goto L_0x02b2;
     */
    /* JADX WARNING: Missing block: B:69:0x02a1, code skipped:
            r11.istate.mode = 13;
            r11.msg = "incorrect data check";
            r11.istate.marker = 5;
     */
    /* JADX WARNING: Missing block: B:70:0x02b2, code skipped:
            r11.istate.mode = 12;
     */
    /* JADX WARNING: Missing block: B:106:?, code skipped:
            return 1;
     */
    /* JADX WARNING: Missing block: B:108:?, code skipped:
            return r1;
     */
    /* JADX WARNING: Missing block: B:109:?, code skipped:
            return r1;
     */
    /* JADX WARNING: Missing block: B:111:?, code skipped:
            return r1;
     */
    /* JADX WARNING: Missing block: B:114:?, code skipped:
            return r1;
     */
    public int inflate(com.jcraft.jzlib.ZStream r11, int r12) {
        /*
        r10 = this;
        if (r11 == 0) goto L_0x000a;
    L_0x0002:
        r2 = r11.istate;
        if (r2 == 0) goto L_0x000a;
    L_0x0006:
        r2 = r11.next_in;
        if (r2 != 0) goto L_0x000c;
    L_0x000a:
        r1 = -2;
    L_0x000b:
        return r1;
    L_0x000c:
        r2 = 4;
        if (r12 != r2) goto L_0x001a;
    L_0x000f:
        r12 = -5;
    L_0x0010:
        r1 = -5;
    L_0x0011:
        r2 = r11.istate;
        r2 = r2.mode;
        switch(r2) {
            case 0: goto L_0x001c;
            case 1: goto L_0x0075;
            case 2: goto L_0x00bf;
            case 3: goto L_0x00ef;
            case 4: goto L_0x0120;
            case 5: goto L_0x0151;
            case 6: goto L_0x0186;
            case 7: goto L_0x0198;
            case 8: goto L_0x01d6;
            case 9: goto L_0x0207;
            case 10: goto L_0x0239;
            case 11: goto L_0x026b;
            case 12: goto L_0x02b8;
            case 13: goto L_0x02bb;
            default: goto L_0x0018;
        };
    L_0x0018:
        r1 = -2;
        goto L_0x000b;
    L_0x001a:
        r12 = 0;
        goto L_0x0010;
    L_0x001c:
        r2 = r11.avail_in;
        if (r2 == 0) goto L_0x000b;
    L_0x0020:
        r1 = r12;
        r2 = r11.avail_in;
        r2 = r2 + -1;
        r11.avail_in = r2;
        r2 = r11.total_in;
        r4 = 1;
        r2 = r2 + r4;
        r11.total_in = r2;
        r2 = r11.istate;
        r3 = r11.next_in;
        r4 = r11.next_in_index;
        r5 = r4 + 1;
        r11.next_in_index = r5;
        r3 = r3[r4];
        r2.method = r3;
        r2 = r3 & 15;
        r3 = 8;
        if (r2 == r3) goto L_0x0052;
    L_0x0042:
        r2 = r11.istate;
        r3 = 13;
        r2.mode = r3;
        r2 = "unknown compression method";
        r11.msg = r2;
        r2 = r11.istate;
        r3 = 5;
        r2.marker = r3;
        goto L_0x0011;
    L_0x0052:
        r2 = r11.istate;
        r2 = r2.method;
        r2 = r2 >> 4;
        r2 = r2 + 8;
        r3 = r11.istate;
        r3 = r3.wbits;
        if (r2 <= r3) goto L_0x0070;
    L_0x0060:
        r2 = r11.istate;
        r3 = 13;
        r2.mode = r3;
        r2 = "invalid window size";
        r11.msg = r2;
        r2 = r11.istate;
        r3 = 5;
        r2.marker = r3;
        goto L_0x0011;
    L_0x0070:
        r2 = r11.istate;
        r3 = 1;
        r2.mode = r3;
    L_0x0075:
        r2 = r11.avail_in;
        if (r2 == 0) goto L_0x000b;
    L_0x0079:
        r1 = r12;
        r2 = r11.avail_in;
        r2 = r2 + -1;
        r11.avail_in = r2;
        r2 = r11.total_in;
        r4 = 1;
        r2 = r2 + r4;
        r11.total_in = r2;
        r2 = r11.next_in;
        r3 = r11.next_in_index;
        r4 = r3 + 1;
        r11.next_in_index = r4;
        r2 = r2[r3];
        r0 = r2 & 255;
        r2 = r11.istate;
        r2 = r2.method;
        r2 = r2 << 8;
        r2 = r2 + r0;
        r2 = r2 % 31;
        if (r2 == 0) goto L_0x00af;
    L_0x009e:
        r2 = r11.istate;
        r3 = 13;
        r2.mode = r3;
        r2 = "incorrect header check";
        r11.msg = r2;
        r2 = r11.istate;
        r3 = 5;
        r2.marker = r3;
        goto L_0x0011;
    L_0x00af:
        r2 = r0 & 32;
        if (r2 != 0) goto L_0x00ba;
    L_0x00b3:
        r2 = r11.istate;
        r3 = 7;
        r2.mode = r3;
        goto L_0x0011;
    L_0x00ba:
        r2 = r11.istate;
        r3 = 2;
        r2.mode = r3;
    L_0x00bf:
        r2 = r11.avail_in;
        if (r2 == 0) goto L_0x000b;
    L_0x00c3:
        r1 = r12;
        r2 = r11.avail_in;
        r2 = r2 + -1;
        r11.avail_in = r2;
        r2 = r11.total_in;
        r4 = 1;
        r2 = r2 + r4;
        r11.total_in = r2;
        r2 = r11.istate;
        r3 = r11.next_in;
        r4 = r11.next_in_index;
        r5 = r4 + 1;
        r11.next_in_index = r5;
        r3 = r3[r4];
        r3 = r3 & 255;
        r3 = r3 << 24;
        r4 = (long) r3;
        r6 = 4278190080; // 0xff000000 float:-1.7014118E38 double:2.113706745E-314;
        r4 = r4 & r6;
        r2.need = r4;
        r2 = r11.istate;
        r3 = 3;
        r2.mode = r3;
    L_0x00ef:
        r2 = r11.avail_in;
        if (r2 == 0) goto L_0x000b;
    L_0x00f3:
        r1 = r12;
        r2 = r11.avail_in;
        r2 = r2 + -1;
        r11.avail_in = r2;
        r2 = r11.total_in;
        r4 = 1;
        r2 = r2 + r4;
        r11.total_in = r2;
        r2 = r11.istate;
        r4 = r2.need;
        r3 = r11.next_in;
        r6 = r11.next_in_index;
        r7 = r6 + 1;
        r11.next_in_index = r7;
        r3 = r3[r6];
        r3 = r3 & 255;
        r3 = r3 << 16;
        r6 = (long) r3;
        r8 = 16711680; // 0xff0000 float:2.3418052E-38 double:8.256667E-317;
        r6 = r6 & r8;
        r4 = r4 + r6;
        r2.need = r4;
        r2 = r11.istate;
        r3 = 4;
        r2.mode = r3;
    L_0x0120:
        r2 = r11.avail_in;
        if (r2 == 0) goto L_0x000b;
    L_0x0124:
        r1 = r12;
        r2 = r11.avail_in;
        r2 = r2 + -1;
        r11.avail_in = r2;
        r2 = r11.total_in;
        r4 = 1;
        r2 = r2 + r4;
        r11.total_in = r2;
        r2 = r11.istate;
        r4 = r2.need;
        r3 = r11.next_in;
        r6 = r11.next_in_index;
        r7 = r6 + 1;
        r11.next_in_index = r7;
        r3 = r3[r6];
        r3 = r3 & 255;
        r3 = r3 << 8;
        r6 = (long) r3;
        r8 = 65280; // 0xff00 float:9.1477E-41 double:3.22526E-319;
        r6 = r6 & r8;
        r4 = r4 + r6;
        r2.need = r4;
        r2 = r11.istate;
        r3 = 5;
        r2.mode = r3;
    L_0x0151:
        r2 = r11.avail_in;
        if (r2 == 0) goto L_0x000b;
    L_0x0155:
        r1 = r12;
        r2 = r11.avail_in;
        r2 = r2 + -1;
        r11.avail_in = r2;
        r2 = r11.total_in;
        r4 = 1;
        r2 = r2 + r4;
        r11.total_in = r2;
        r2 = r11.istate;
        r4 = r2.need;
        r3 = r11.next_in;
        r6 = r11.next_in_index;
        r7 = r6 + 1;
        r11.next_in_index = r7;
        r3 = r3[r6];
        r6 = (long) r3;
        r8 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r6 = r6 & r8;
        r4 = r4 + r6;
        r2.need = r4;
        r2 = r11.istate;
        r2 = r2.need;
        r11.adler = r2;
        r2 = r11.istate;
        r3 = 6;
        r2.mode = r3;
        r1 = 2;
        goto L_0x000b;
    L_0x0186:
        r2 = r11.istate;
        r3 = 13;
        r2.mode = r3;
        r2 = "need dictionary";
        r11.msg = r2;
        r2 = r11.istate;
        r3 = 0;
        r2.marker = r3;
        r1 = -2;
        goto L_0x000b;
    L_0x0198:
        r2 = r11.istate;
        r2 = r2.blocks;
        r1 = r2.proc(r11, r1);
        r2 = -3;
        if (r1 != r2) goto L_0x01b0;
    L_0x01a3:
        r2 = r11.istate;
        r3 = 13;
        r2.mode = r3;
        r2 = r11.istate;
        r3 = 0;
        r2.marker = r3;
        goto L_0x0011;
    L_0x01b0:
        if (r1 != 0) goto L_0x01b3;
    L_0x01b2:
        r1 = r12;
    L_0x01b3:
        r2 = 1;
        if (r1 != r2) goto L_0x000b;
    L_0x01b6:
        r1 = r12;
        r2 = r11.istate;
        r2 = r2.blocks;
        r3 = r11.istate;
        r3 = r3.was;
        r2.reset(r11, r3);
        r2 = r11.istate;
        r2 = r2.nowrap;
        if (r2 == 0) goto L_0x01d0;
    L_0x01c8:
        r2 = r11.istate;
        r3 = 12;
        r2.mode = r3;
        goto L_0x0011;
    L_0x01d0:
        r2 = r11.istate;
        r3 = 8;
        r2.mode = r3;
    L_0x01d6:
        r2 = r11.avail_in;
        if (r2 == 0) goto L_0x000b;
    L_0x01da:
        r1 = r12;
        r2 = r11.avail_in;
        r2 = r2 + -1;
        r11.avail_in = r2;
        r2 = r11.total_in;
        r4 = 1;
        r2 = r2 + r4;
        r11.total_in = r2;
        r2 = r11.istate;
        r3 = r11.next_in;
        r4 = r11.next_in_index;
        r5 = r4 + 1;
        r11.next_in_index = r5;
        r3 = r3[r4];
        r3 = r3 & 255;
        r3 = r3 << 24;
        r4 = (long) r3;
        r6 = 4278190080; // 0xff000000 float:-1.7014118E38 double:2.113706745E-314;
        r4 = r4 & r6;
        r2.need = r4;
        r2 = r11.istate;
        r3 = 9;
        r2.mode = r3;
    L_0x0207:
        r2 = r11.avail_in;
        if (r2 == 0) goto L_0x000b;
    L_0x020b:
        r1 = r12;
        r2 = r11.avail_in;
        r2 = r2 + -1;
        r11.avail_in = r2;
        r2 = r11.total_in;
        r4 = 1;
        r2 = r2 + r4;
        r11.total_in = r2;
        r2 = r11.istate;
        r4 = r2.need;
        r3 = r11.next_in;
        r6 = r11.next_in_index;
        r7 = r6 + 1;
        r11.next_in_index = r7;
        r3 = r3[r6];
        r3 = r3 & 255;
        r3 = r3 << 16;
        r6 = (long) r3;
        r8 = 16711680; // 0xff0000 float:2.3418052E-38 double:8.256667E-317;
        r6 = r6 & r8;
        r4 = r4 + r6;
        r2.need = r4;
        r2 = r11.istate;
        r3 = 10;
        r2.mode = r3;
    L_0x0239:
        r2 = r11.avail_in;
        if (r2 == 0) goto L_0x000b;
    L_0x023d:
        r1 = r12;
        r2 = r11.avail_in;
        r2 = r2 + -1;
        r11.avail_in = r2;
        r2 = r11.total_in;
        r4 = 1;
        r2 = r2 + r4;
        r11.total_in = r2;
        r2 = r11.istate;
        r4 = r2.need;
        r3 = r11.next_in;
        r6 = r11.next_in_index;
        r7 = r6 + 1;
        r11.next_in_index = r7;
        r3 = r3[r6];
        r3 = r3 & 255;
        r3 = r3 << 8;
        r6 = (long) r3;
        r8 = 65280; // 0xff00 float:9.1477E-41 double:3.22526E-319;
        r6 = r6 & r8;
        r4 = r4 + r6;
        r2.need = r4;
        r2 = r11.istate;
        r3 = 11;
        r2.mode = r3;
    L_0x026b:
        r2 = r11.avail_in;
        if (r2 == 0) goto L_0x000b;
    L_0x026f:
        r1 = r12;
        r2 = r11.avail_in;
        r2 = r2 + -1;
        r11.avail_in = r2;
        r2 = r11.total_in;
        r4 = 1;
        r2 = r2 + r4;
        r11.total_in = r2;
        r2 = r11.istate;
        r4 = r2.need;
        r3 = r11.next_in;
        r6 = r11.next_in_index;
        r7 = r6 + 1;
        r11.next_in_index = r7;
        r3 = r3[r6];
        r6 = (long) r3;
        r8 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r6 = r6 & r8;
        r4 = r4 + r6;
        r2.need = r4;
        r2 = r11.istate;
        r2 = r2.was;
        r3 = 0;
        r2 = r2[r3];
        r2 = (int) r2;
        r3 = r11.istate;
        r4 = r3.need;
        r3 = (int) r4;
        if (r2 == r3) goto L_0x02b2;
    L_0x02a1:
        r2 = r11.istate;
        r3 = 13;
        r2.mode = r3;
        r2 = "incorrect data check";
        r11.msg = r2;
        r2 = r11.istate;
        r3 = 5;
        r2.marker = r3;
        goto L_0x0011;
    L_0x02b2:
        r2 = r11.istate;
        r3 = 12;
        r2.mode = r3;
    L_0x02b8:
        r1 = 1;
        goto L_0x000b;
    L_0x02bb:
        r1 = -3;
        goto L_0x000b;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.jcraft.jzlib.Inflate.inflate(com.jcraft.jzlib.ZStream, int):int");
    }

    /* access modifiers changed from: 0000 */
    public int inflateSetDictionary(ZStream z, byte[] dictionary, int dictLength) {
        int index = 0;
        int length = dictLength;
        if (z == null || z.istate == null || z.istate.mode != 6) {
            return -2;
        }
        if (z._adler.adler32(1, dictionary, 0, dictLength) != z.adler) {
            return -3;
        }
        z.adler = z._adler.adler32(0, null, 0, 0);
        if (length >= (1 << z.istate.wbits)) {
            length = (1 << z.istate.wbits) - 1;
            index = dictLength - length;
        }
        z.istate.blocks.set_dictionary(dictionary, index, length);
        z.istate.mode = 7;
        return 0;
    }

    /* access modifiers changed from: 0000 */
    public int inflateSync(ZStream z) {
        if (z == null || z.istate == null) {
            return -2;
        }
        if (z.istate.mode != 13) {
            z.istate.mode = 13;
            z.istate.marker = 0;
        }
        int n = z.avail_in;
        if (n == 0) {
            return -5;
        }
        int p = z.next_in_index;
        int m = z.istate.marker;
        while (n != 0 && m < 4) {
            if (z.next_in[p] == mark[m]) {
                m++;
            } else if (z.next_in[p] != (byte) 0) {
                m = 0;
            } else {
                m = 4 - m;
            }
            p++;
            n--;
        }
        z.total_in += (long) (p - z.next_in_index);
        z.next_in_index = p;
        z.avail_in = n;
        z.istate.marker = m;
        if (m != 4) {
            return -3;
        }
        long r = z.total_in;
        long w = z.total_out;
        inflateReset(z);
        z.total_in = r;
        z.total_out = w;
        z.istate.mode = 7;
        return 0;
    }

    /* access modifiers changed from: 0000 */
    public int inflateSyncPoint(ZStream z) {
        if (z == null || z.istate == null || z.istate.blocks == null) {
            return -2;
        }
        return z.istate.blocks.sync_point();
    }
}
