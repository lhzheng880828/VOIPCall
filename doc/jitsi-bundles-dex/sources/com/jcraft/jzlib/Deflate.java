package com.jcraft.jzlib;

import org.jitsi.javax.sip.message.Response;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;

public final class Deflate {
    private static final int BL_CODES = 19;
    private static final int BUSY_STATE = 113;
    private static final int BlockDone = 1;
    private static final int Buf_size = 16;
    private static final int DEF_MEM_LEVEL = 8;
    private static final int DYN_TREES = 2;
    private static final int D_CODES = 30;
    private static final int END_BLOCK = 256;
    private static final int FAST = 1;
    private static final int FINISH_STATE = 666;
    private static final int FinishDone = 3;
    private static final int FinishStarted = 2;
    private static final int HEAP_SIZE = 573;
    private static final int INIT_STATE = 42;
    private static final int LENGTH_CODES = 29;
    private static final int LITERALS = 256;
    private static final int L_CODES = 286;
    private static final int MAX_BITS = 15;
    private static final int MAX_MATCH = 258;
    private static final int MAX_MEM_LEVEL = 9;
    private static final int MAX_WBITS = 15;
    private static final int MIN_LOOKAHEAD = 262;
    private static final int MIN_MATCH = 3;
    private static final int NeedMore = 0;
    private static final int PRESET_DICT = 32;
    private static final int REPZ_11_138 = 18;
    private static final int REPZ_3_10 = 17;
    private static final int REP_3_6 = 16;
    private static final int SLOW = 2;
    private static final int STATIC_TREES = 1;
    private static final int STORED = 0;
    private static final int STORED_BLOCK = 0;
    private static final int Z_ASCII = 1;
    private static final int Z_BINARY = 0;
    private static final int Z_BUF_ERROR = -5;
    private static final int Z_DATA_ERROR = -3;
    private static final int Z_DEFAULT_COMPRESSION = -1;
    private static final int Z_DEFAULT_STRATEGY = 0;
    private static final int Z_DEFLATED = 8;
    private static final int Z_ERRNO = -1;
    private static final int Z_FILTERED = 1;
    private static final int Z_FINISH = 4;
    private static final int Z_FULL_FLUSH = 3;
    private static final int Z_HUFFMAN_ONLY = 2;
    private static final int Z_MEM_ERROR = -4;
    private static final int Z_NEED_DICT = 2;
    private static final int Z_NO_FLUSH = 0;
    private static final int Z_OK = 0;
    private static final int Z_PARTIAL_FLUSH = 1;
    private static final int Z_STREAM_END = 1;
    private static final int Z_STREAM_ERROR = -2;
    private static final int Z_SYNC_FLUSH = 2;
    private static final int Z_UNKNOWN = 2;
    private static final int Z_VERSION_ERROR = -6;
    private static final Config[] config_table = new Config[10];
    private static final String[] z_errmsg = new String[]{"need dictionary", "stream end", "", "file error", "stream error", "data error", "insufficient memory", "buffer error", "incompatible version", ""};
    short bi_buf;
    int bi_valid;
    short[] bl_count = new short[16];
    Tree bl_desc = new Tree();
    short[] bl_tree = new short[78];
    int block_start;
    int d_buf;
    Tree d_desc = new Tree();
    byte data_type;
    byte[] depth = new byte[HEAP_SIZE];
    short[] dyn_dtree = new short[122];
    short[] dyn_ltree = new short[1146];
    int good_match;
    int hash_bits;
    int hash_mask;
    int hash_shift;
    int hash_size;
    short[] head;
    int[] heap = new int[HEAP_SIZE];
    int heap_len;
    int heap_max;
    int ins_h;
    int l_buf;
    Tree l_desc = new Tree();
    int last_eob_len;
    int last_flush;
    int last_lit;
    int level;
    int lit_bufsize;
    int lookahead;
    int match_available;
    int match_length;
    int match_start;
    int matches;
    int max_chain_length;
    int max_lazy_match;
    byte method;
    int nice_match;
    int noheader;
    int opt_len;
    int pending;
    byte[] pending_buf;
    int pending_buf_size;
    int pending_out;
    short[] prev;
    int prev_length;
    int prev_match;
    int static_len;
    int status;
    int strategy;
    ZStream strm;
    int strstart;
    int w_bits;
    int w_mask;
    int w_size;
    byte[] window;
    int window_size;

    static class Config {
        int func;
        int good_length;
        int max_chain;
        int max_lazy;
        int nice_length;

        Config(int good_length, int max_lazy, int nice_length, int max_chain, int func) {
            this.good_length = good_length;
            this.max_lazy = max_lazy;
            this.nice_length = nice_length;
            this.max_chain = max_chain;
            this.func = func;
        }
    }

    static {
        config_table[0] = new Config(0, 0, 0, 0, 0);
        config_table[1] = new Config(4, 4, 8, 4, 1);
        config_table[2] = new Config(4, 5, 16, 8, 1);
        config_table[3] = new Config(4, 6, 32, 32, 1);
        config_table[4] = new Config(4, 4, 16, 16, 2);
        config_table[5] = new Config(8, 16, 32, 32, 2);
        config_table[6] = new Config(8, 16, 128, 128, 2);
        config_table[7] = new Config(8, 32, 128, 256, 2);
        config_table[8] = new Config(32, 128, MAX_MATCH, 1024, 2);
        config_table[9] = new Config(32, MAX_MATCH, MAX_MATCH, 4096, 2);
    }

    Deflate() {
    }

    /* access modifiers changed from: 0000 */
    public void lm_init() {
        this.window_size = this.w_size * 2;
        this.head[this.hash_size - 1] = (short) 0;
        for (int i = 0; i < this.hash_size - 1; i++) {
            this.head[i] = (short) 0;
        }
        this.max_lazy_match = config_table[this.level].max_lazy;
        this.good_match = config_table[this.level].good_length;
        this.nice_match = config_table[this.level].nice_length;
        this.max_chain_length = config_table[this.level].max_chain;
        this.strstart = 0;
        this.block_start = 0;
        this.lookahead = 0;
        this.prev_length = 2;
        this.match_length = 2;
        this.match_available = 0;
        this.ins_h = 0;
    }

    /* access modifiers changed from: 0000 */
    public void tr_init() {
        this.l_desc.dyn_tree = this.dyn_ltree;
        this.l_desc.stat_desc = StaticTree.static_l_desc;
        this.d_desc.dyn_tree = this.dyn_dtree;
        this.d_desc.stat_desc = StaticTree.static_d_desc;
        this.bl_desc.dyn_tree = this.bl_tree;
        this.bl_desc.stat_desc = StaticTree.static_bl_desc;
        this.bi_buf = (short) 0;
        this.bi_valid = 0;
        this.last_eob_len = 8;
        init_block();
    }

    /* access modifiers changed from: 0000 */
    public void init_block() {
        int i;
        for (i = 0; i < L_CODES; i++) {
            this.dyn_ltree[i * 2] = (short) 0;
        }
        for (i = 0; i < D_CODES; i++) {
            this.dyn_dtree[i * 2] = (short) 0;
        }
        for (i = 0; i < 19; i++) {
            this.bl_tree[i * 2] = (short) 0;
        }
        this.dyn_ltree[512] = (short) 1;
        this.static_len = 0;
        this.opt_len = 0;
        this.matches = 0;
        this.last_lit = 0;
    }

    /* access modifiers changed from: 0000 */
    public void pqdownheap(short[] tree, int k) {
        int v = this.heap[k];
        int j = k << 1;
        while (j <= this.heap_len) {
            if (j < this.heap_len && smaller(tree, this.heap[j + 1], this.heap[j], this.depth)) {
                j++;
            }
            if (smaller(tree, v, this.heap[j], this.depth)) {
                break;
            }
            this.heap[k] = this.heap[j];
            k = j;
            j <<= 1;
        }
        this.heap[k] = v;
    }

    static boolean smaller(short[] tree, int n, int m, byte[] depth) {
        short tn2 = tree[n * 2];
        short tm2 = tree[m * 2];
        return tn2 < tm2 || (tn2 == tm2 && depth[n] <= depth[m]);
    }

    /* access modifiers changed from: 0000 */
    public void scan_tree(short[] tree, int max_code) {
        int prevlen = -1;
        int nextlen = tree[1];
        int count = 0;
        int max_count = 7;
        int min_count = 4;
        if (nextlen == 0) {
            max_count = 138;
            min_count = 3;
        }
        tree[((max_code + 1) * 2) + 1] = (short) -1;
        for (int n = 0; n <= max_code; n++) {
            int curlen = nextlen;
            nextlen = tree[((n + 1) * 2) + 1];
            count++;
            if (count >= max_count || curlen != nextlen) {
                short[] sArr;
                int i;
                if (count < min_count) {
                    sArr = this.bl_tree;
                    i = curlen * 2;
                    sArr[i] = (short) (sArr[i] + count);
                } else if (curlen != 0) {
                    if (curlen != prevlen) {
                        sArr = this.bl_tree;
                        i = curlen * 2;
                        sArr[i] = (short) (sArr[i] + 1);
                    }
                    sArr = this.bl_tree;
                    sArr[32] = (short) (sArr[32] + 1);
                } else if (count <= 10) {
                    sArr = this.bl_tree;
                    sArr[34] = (short) (sArr[34] + 1);
                } else {
                    sArr = this.bl_tree;
                    sArr[36] = (short) (sArr[36] + 1);
                }
                count = 0;
                prevlen = curlen;
                if (nextlen == 0) {
                    max_count = 138;
                    min_count = 3;
                } else if (curlen == nextlen) {
                    max_count = 6;
                    min_count = 3;
                } else {
                    max_count = 7;
                    min_count = 4;
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public int build_bl_tree() {
        scan_tree(this.dyn_ltree, this.l_desc.max_code);
        scan_tree(this.dyn_dtree, this.d_desc.max_code);
        this.bl_desc.build_tree(this);
        int max_blindex = 18;
        while (max_blindex >= 3 && this.bl_tree[(Tree.bl_order[max_blindex] * 2) + 1] == (short) 0) {
            max_blindex--;
        }
        this.opt_len += ((((max_blindex + 1) * 3) + 5) + 5) + 4;
        return max_blindex;
    }

    /* access modifiers changed from: 0000 */
    public void send_all_trees(int lcodes, int dcodes, int blcodes) {
        send_bits(lcodes - 257, 5);
        send_bits(dcodes - 1, 5);
        send_bits(blcodes - 4, 4);
        for (int rank = 0; rank < blcodes; rank++) {
            send_bits(this.bl_tree[(Tree.bl_order[rank] * 2) + 1], 3);
        }
        send_tree(this.dyn_ltree, lcodes - 1);
        send_tree(this.dyn_dtree, dcodes - 1);
    }

    /* access modifiers changed from: 0000 */
    public void send_tree(short[] tree, int max_code) {
        int prevlen = -1;
        int nextlen = tree[1];
        int count = 0;
        int max_count = 7;
        int min_count = 4;
        if (nextlen == 0) {
            max_count = 138;
            min_count = 3;
        }
        for (int n = 0; n <= max_code; n++) {
            int curlen = nextlen;
            nextlen = tree[((n + 1) * 2) + 1];
            count++;
            if (count >= max_count || curlen != nextlen) {
                if (count < min_count) {
                    do {
                        send_code(curlen, this.bl_tree);
                        count--;
                    } while (count != 0);
                } else if (curlen != 0) {
                    if (curlen != prevlen) {
                        send_code(curlen, this.bl_tree);
                        count--;
                    }
                    send_code(16, this.bl_tree);
                    send_bits(count - 3, 2);
                } else if (count <= 10) {
                    send_code(17, this.bl_tree);
                    send_bits(count - 3, 3);
                } else {
                    send_code(18, this.bl_tree);
                    send_bits(count - 11, 7);
                }
                count = 0;
                prevlen = curlen;
                if (nextlen == 0) {
                    max_count = 138;
                    min_count = 3;
                } else if (curlen == nextlen) {
                    max_count = 6;
                    min_count = 3;
                } else {
                    max_count = 7;
                    min_count = 4;
                }
            }
        }
    }

    /* access modifiers changed from: final */
    public final void put_byte(byte[] p, int start, int len) {
        System.arraycopy(p, start, this.pending_buf, this.pending, len);
        this.pending += len;
    }

    /* access modifiers changed from: final */
    public final void put_byte(byte c) {
        byte[] bArr = this.pending_buf;
        int i = this.pending;
        this.pending = i + 1;
        bArr[i] = c;
    }

    /* access modifiers changed from: final */
    public final void put_short(int w) {
        put_byte((byte) w);
        put_byte((byte) (w >>> 8));
    }

    /* access modifiers changed from: final */
    public final void putShortMSB(int b) {
        put_byte((byte) (b >> 8));
        put_byte((byte) b);
    }

    /* access modifiers changed from: final */
    public final void send_code(int c, short[] tree) {
        int c2 = c * 2;
        send_bits(tree[c2] & InBandBytestreamManager.MAXIMUM_BLOCK_SIZE, tree[c2 + 1] & InBandBytestreamManager.MAXIMUM_BLOCK_SIZE);
    }

    /* access modifiers changed from: 0000 */
    public void send_bits(int value, int length) {
        int len = length;
        if (this.bi_valid > 16 - len) {
            int val = value;
            this.bi_buf = (short) (this.bi_buf | ((val << this.bi_valid) & InBandBytestreamManager.MAXIMUM_BLOCK_SIZE));
            put_short(this.bi_buf);
            this.bi_buf = (short) (val >>> (16 - this.bi_valid));
            this.bi_valid += len - 16;
            return;
        }
        this.bi_buf = (short) (this.bi_buf | ((value << this.bi_valid) & InBandBytestreamManager.MAXIMUM_BLOCK_SIZE));
        this.bi_valid += len;
    }

    /* access modifiers changed from: 0000 */
    public void _tr_align() {
        send_bits(2, 3);
        send_code(256, StaticTree.static_ltree);
        bi_flush();
        if (((this.last_eob_len + 1) + 10) - this.bi_valid < 9) {
            send_bits(2, 3);
            send_code(256, StaticTree.static_ltree);
            bi_flush();
        }
        this.last_eob_len = 7;
    }

    /* access modifiers changed from: 0000 */
    public boolean _tr_tally(int dist, int lc) {
        this.pending_buf[this.d_buf + (this.last_lit * 2)] = (byte) (dist >>> 8);
        this.pending_buf[(this.d_buf + (this.last_lit * 2)) + 1] = (byte) dist;
        this.pending_buf[this.l_buf + this.last_lit] = (byte) lc;
        this.last_lit++;
        short[] sArr;
        int i;
        if (dist == 0) {
            sArr = this.dyn_ltree;
            i = lc * 2;
            sArr[i] = (short) (sArr[i] + 1);
        } else {
            this.matches++;
            dist--;
            sArr = this.dyn_ltree;
            i = ((Tree._length_code[lc] + 256) + 1) * 2;
            sArr[i] = (short) (sArr[i] + 1);
            sArr = this.dyn_dtree;
            i = Tree.d_code(dist) * 2;
            sArr[i] = (short) (sArr[i] + 1);
        }
        if ((this.last_lit & 8191) == 0 && this.level > 2) {
            int out_length = this.last_lit * 8;
            int in_length = this.strstart - this.block_start;
            for (int dcode = 0; dcode < D_CODES; dcode++) {
                out_length = (int) (((long) out_length) + (((long) this.dyn_dtree[dcode * 2]) * (5 + ((long) Tree.extra_dbits[dcode]))));
            }
            out_length >>>= 3;
            if (this.matches < this.last_lit / 2 && out_length < in_length / 2) {
                return true;
            }
        }
        if (this.last_lit != this.lit_bufsize - 1) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: 0000 */
    public void compress_block(short[] ltree, short[] dtree) {
        int lx = 0;
        if (this.last_lit != 0) {
            do {
                int dist = ((this.pending_buf[this.d_buf + (lx * 2)] << 8) & 65280) | (this.pending_buf[(this.d_buf + (lx * 2)) + 1] & 255);
                int lc = this.pending_buf[this.l_buf + lx] & 255;
                lx++;
                if (dist == 0) {
                    send_code(lc, ltree);
                } else {
                    int code = Tree._length_code[lc];
                    send_code((code + 256) + 1, ltree);
                    int extra = Tree.extra_lbits[code];
                    if (extra != 0) {
                        send_bits(lc - Tree.base_length[code], extra);
                    }
                    dist--;
                    code = Tree.d_code(dist);
                    send_code(code, dtree);
                    extra = Tree.extra_dbits[code];
                    if (extra != 0) {
                        send_bits(dist - Tree.base_dist[code], extra);
                    }
                }
            } while (lx < this.last_lit);
        }
        send_code(256, ltree);
        this.last_eob_len = ltree[Response.MESSAGE_TOO_LARGE];
    }

    /* access modifiers changed from: 0000 */
    public void set_data_type() {
        int n = 0;
        int ascii_freq = 0;
        int bin_freq = 0;
        while (n < 7) {
            bin_freq += this.dyn_ltree[n * 2];
            n++;
        }
        while (n < 128) {
            ascii_freq += this.dyn_ltree[n * 2];
            n++;
        }
        while (n < 256) {
            bin_freq += this.dyn_ltree[n * 2];
            n++;
        }
        this.data_type = (byte) (bin_freq > (ascii_freq >>> 2) ? 0 : 1);
    }

    /* access modifiers changed from: 0000 */
    public void bi_flush() {
        if (this.bi_valid == 16) {
            put_short(this.bi_buf);
            this.bi_buf = (short) 0;
            this.bi_valid = 0;
        } else if (this.bi_valid >= 8) {
            put_byte((byte) this.bi_buf);
            this.bi_buf = (short) (this.bi_buf >>> 8);
            this.bi_valid -= 8;
        }
    }

    /* access modifiers changed from: 0000 */
    public void bi_windup() {
        if (this.bi_valid > 8) {
            put_short(this.bi_buf);
        } else if (this.bi_valid > 0) {
            put_byte((byte) this.bi_buf);
        }
        this.bi_buf = (short) 0;
        this.bi_valid = 0;
    }

    /* access modifiers changed from: 0000 */
    public void copy_block(int buf, int len, boolean header) {
        bi_windup();
        this.last_eob_len = 8;
        if (header) {
            put_short((short) len);
            put_short((short) (len ^ -1));
        }
        put_byte(this.window, buf, len);
    }

    /* access modifiers changed from: 0000 */
    public void flush_block_only(boolean eof) {
        _tr_flush_block(this.block_start >= 0 ? this.block_start : -1, this.strstart - this.block_start, eof);
        this.block_start = this.strstart;
        this.strm.flush_pending();
    }

    /* access modifiers changed from: 0000 */
    public int deflate_stored(int flush) {
        int i = 1;
        int max_block_size = InBandBytestreamManager.MAXIMUM_BLOCK_SIZE;
        if (InBandBytestreamManager.MAXIMUM_BLOCK_SIZE > this.pending_buf_size - 5) {
            max_block_size = this.pending_buf_size - 5;
        }
        while (true) {
            if (this.lookahead <= 1) {
                fill_window();
                if (this.lookahead == 0 && flush == 0) {
                    return 0;
                }
                if (this.lookahead == 0) {
                    boolean z;
                    if (flush == 4) {
                        z = true;
                    } else {
                        z = false;
                    }
                    flush_block_only(z);
                    if (this.strm.avail_out != 0) {
                        if (flush == 4) {
                            i = 3;
                        }
                        return i;
                    } else if (flush == 4) {
                        return 2;
                    } else {
                        return 0;
                    }
                }
            }
            this.strstart += this.lookahead;
            this.lookahead = 0;
            int max_start = this.block_start + max_block_size;
            if (this.strstart == 0 || this.strstart >= max_start) {
                this.lookahead = this.strstart - max_start;
                this.strstart = max_start;
                flush_block_only(false);
                if (this.strm.avail_out == 0) {
                    return 0;
                }
            }
            if (this.strstart - this.block_start >= this.w_size - 262) {
                flush_block_only(false);
                if (this.strm.avail_out == 0) {
                    return 0;
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void _tr_stored_block(int buf, int stored_len, boolean eof) {
        send_bits((eof ? 1 : 0) + 0, 3);
        copy_block(buf, stored_len, true);
    }

    /* access modifiers changed from: 0000 */
    public void _tr_flush_block(int buf, int stored_len, boolean eof) {
        int opt_lenb;
        int static_lenb;
        int i = 1;
        int max_blindex = 0;
        if (this.level > 0) {
            if (this.data_type == (byte) 2) {
                set_data_type();
            }
            this.l_desc.build_tree(this);
            this.d_desc.build_tree(this);
            max_blindex = build_bl_tree();
            opt_lenb = ((this.opt_len + 3) + 7) >>> 3;
            static_lenb = ((this.static_len + 3) + 7) >>> 3;
            if (static_lenb <= opt_lenb) {
                opt_lenb = static_lenb;
            }
        } else {
            static_lenb = stored_len + 5;
            opt_lenb = static_lenb;
        }
        if (stored_len + 4 <= opt_lenb && buf != -1) {
            _tr_stored_block(buf, stored_len, eof);
        } else if (static_lenb == opt_lenb) {
            if (!eof) {
                i = 0;
            }
            send_bits(i + 2, 3);
            compress_block(StaticTree.static_ltree, StaticTree.static_dtree);
        } else {
            if (!eof) {
                i = 0;
            }
            send_bits(i + 4, 3);
            send_all_trees(this.l_desc.max_code + 1, this.d_desc.max_code + 1, max_blindex + 1);
            compress_block(this.dyn_ltree, this.dyn_dtree);
        }
        init_block();
        if (eof) {
            bi_windup();
        }
    }

    /* access modifiers changed from: 0000 */
    public void fill_window() {
        do {
            int more = (this.window_size - this.lookahead) - this.strstart;
            if (more == 0 && this.strstart == 0 && this.lookahead == 0) {
                more = this.w_size;
            } else if (more == -1) {
                more--;
            } else if (this.strstart >= (this.w_size + this.w_size) - 262) {
                int m;
                System.arraycopy(this.window, this.w_size, this.window, 0, this.w_size);
                this.match_start -= this.w_size;
                this.strstart -= this.w_size;
                this.block_start -= this.w_size;
                int n = this.hash_size;
                int p = n;
                do {
                    p--;
                    m = this.head[p] & InBandBytestreamManager.MAXIMUM_BLOCK_SIZE;
                    this.head[p] = m >= this.w_size ? (short) (m - this.w_size) : (short) 0;
                    n--;
                } while (n != 0);
                n = this.w_size;
                p = n;
                do {
                    short s;
                    p--;
                    m = this.prev[p] & InBandBytestreamManager.MAXIMUM_BLOCK_SIZE;
                    short[] sArr = this.prev;
                    if (m >= this.w_size) {
                        s = (short) (m - this.w_size);
                    } else {
                        s = (short) 0;
                    }
                    sArr[p] = s;
                    n--;
                } while (n != 0);
                more += this.w_size;
            }
            if (this.strm.avail_in != 0) {
                this.lookahead += this.strm.read_buf(this.window, this.strstart + this.lookahead, more);
                if (this.lookahead >= 3) {
                    this.ins_h = this.window[this.strstart] & 255;
                    this.ins_h = ((this.ins_h << this.hash_shift) ^ (this.window[this.strstart + 1] & 255)) & this.hash_mask;
                }
                if (this.lookahead >= MIN_LOOKAHEAD) {
                    return;
                }
            } else {
                return;
            }
        } while (this.strm.avail_in != 0);
    }

    /* access modifiers changed from: 0000 */
    public int deflate_fast(int flush) {
        int i = 1;
        int hash_head = 0;
        while (true) {
            boolean bflush;
            if (this.lookahead < MIN_LOOKAHEAD) {
                fill_window();
                if (this.lookahead < MIN_LOOKAHEAD && flush == 0) {
                    return 0;
                }
                if (this.lookahead == 0) {
                    boolean z;
                    if (flush == 4) {
                        z = true;
                    } else {
                        z = false;
                    }
                    flush_block_only(z);
                    if (this.strm.avail_out != 0) {
                        if (flush == 4) {
                            i = 3;
                        }
                        return i;
                    } else if (flush == 4) {
                        return 2;
                    } else {
                        return 0;
                    }
                }
            }
            if (this.lookahead >= 3) {
                this.ins_h = ((this.ins_h << this.hash_shift) ^ (this.window[this.strstart + 2] & 255)) & this.hash_mask;
                hash_head = this.head[this.ins_h] & InBandBytestreamManager.MAXIMUM_BLOCK_SIZE;
                this.prev[this.strstart & this.w_mask] = this.head[this.ins_h];
                this.head[this.ins_h] = (short) this.strstart;
            }
            if (!(((long) hash_head) == 0 || ((this.strstart - hash_head) & InBandBytestreamManager.MAXIMUM_BLOCK_SIZE) > this.w_size - 262 || this.strategy == 2)) {
                this.match_length = longest_match(hash_head);
            }
            if (this.match_length >= 3) {
                bflush = _tr_tally(this.strstart - this.match_start, this.match_length - 3);
                this.lookahead -= this.match_length;
                if (this.match_length > this.max_lazy_match || this.lookahead < 3) {
                    this.strstart += this.match_length;
                    this.match_length = 0;
                    this.ins_h = this.window[this.strstart] & 255;
                    this.ins_h = ((this.ins_h << this.hash_shift) ^ (this.window[this.strstart + 1] & 255)) & this.hash_mask;
                } else {
                    this.match_length--;
                    int i2;
                    do {
                        this.strstart++;
                        this.ins_h = ((this.ins_h << this.hash_shift) ^ (this.window[this.strstart + 2] & 255)) & this.hash_mask;
                        hash_head = this.head[this.ins_h] & InBandBytestreamManager.MAXIMUM_BLOCK_SIZE;
                        this.prev[this.strstart & this.w_mask] = this.head[this.ins_h];
                        this.head[this.ins_h] = (short) this.strstart;
                        i2 = this.match_length - 1;
                        this.match_length = i2;
                    } while (i2 != 0);
                    this.strstart++;
                }
            } else {
                bflush = _tr_tally(0, this.window[this.strstart] & 255);
                this.lookahead--;
                this.strstart++;
            }
            if (bflush) {
                flush_block_only(false);
                if (this.strm.avail_out == 0) {
                    return 0;
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public int deflate_slow(int flush) {
        int i = 1;
        int hash_head = 0;
        while (true) {
            boolean bflush;
            if (this.lookahead < MIN_LOOKAHEAD) {
                fill_window();
                if (this.lookahead < MIN_LOOKAHEAD && flush == 0) {
                    return 0;
                }
                if (this.lookahead == 0) {
                    boolean z;
                    if (this.match_available != 0) {
                        bflush = _tr_tally(0, this.window[this.strstart - 1] & 255);
                        this.match_available = 0;
                    }
                    if (flush == 4) {
                        z = true;
                    } else {
                        z = false;
                    }
                    flush_block_only(z);
                    if (this.strm.avail_out != 0) {
                        if (flush == 4) {
                            i = 3;
                        }
                        return i;
                    } else if (flush == 4) {
                        return 2;
                    } else {
                        return 0;
                    }
                }
            }
            if (this.lookahead >= 3) {
                this.ins_h = ((this.ins_h << this.hash_shift) ^ (this.window[this.strstart + 2] & 255)) & this.hash_mask;
                hash_head = this.head[this.ins_h] & InBandBytestreamManager.MAXIMUM_BLOCK_SIZE;
                this.prev[this.strstart & this.w_mask] = this.head[this.ins_h];
                this.head[this.ins_h] = (short) this.strstart;
            }
            this.prev_length = this.match_length;
            this.prev_match = this.match_start;
            this.match_length = 2;
            if (hash_head != 0 && this.prev_length < this.max_lazy_match && ((this.strstart - hash_head) & InBandBytestreamManager.MAXIMUM_BLOCK_SIZE) <= this.w_size - 262) {
                if (this.strategy != 2) {
                    this.match_length = longest_match(hash_head);
                }
                if (this.match_length <= 5 && (this.strategy == 1 || (this.match_length == 3 && this.strstart - this.match_start > 4096))) {
                    this.match_length = 2;
                }
            }
            if (this.prev_length >= 3 && this.match_length <= this.prev_length) {
                int max_insert = (this.strstart + this.lookahead) - 3;
                bflush = _tr_tally((this.strstart - 1) - this.prev_match, this.prev_length - 3);
                this.lookahead -= this.prev_length - 1;
                this.prev_length -= 2;
                int i2;
                do {
                    i2 = this.strstart + 1;
                    this.strstart = i2;
                    if (i2 <= max_insert) {
                        this.ins_h = ((this.ins_h << this.hash_shift) ^ (this.window[this.strstart + 2] & 255)) & this.hash_mask;
                        hash_head = this.head[this.ins_h] & InBandBytestreamManager.MAXIMUM_BLOCK_SIZE;
                        this.prev[this.strstart & this.w_mask] = this.head[this.ins_h];
                        this.head[this.ins_h] = (short) this.strstart;
                    }
                    i2 = this.prev_length - 1;
                    this.prev_length = i2;
                } while (i2 != 0);
                this.match_available = 0;
                this.match_length = 2;
                this.strstart++;
                if (bflush) {
                    flush_block_only(false);
                    if (this.strm.avail_out == 0) {
                        return 0;
                    }
                } else {
                    continue;
                }
            } else if (this.match_available != 0) {
                if (_tr_tally(0, this.window[this.strstart - 1] & 255)) {
                    flush_block_only(false);
                }
                this.strstart++;
                this.lookahead--;
                if (this.strm.avail_out == 0) {
                    return 0;
                }
            } else {
                this.match_available = 1;
                this.strstart++;
                this.lookahead--;
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public int longest_match(int cur_match) {
        int chain_length = this.max_chain_length;
        int scan = this.strstart;
        int best_len = this.prev_length;
        int limit = this.strstart > this.w_size + -262 ? this.strstart - (this.w_size - 262) : 0;
        int nice_match = this.nice_match;
        int wmask = this.w_mask;
        int strend = this.strstart + MAX_MATCH;
        byte scan_end1 = this.window[(scan + best_len) - 1];
        byte scan_end = this.window[scan + best_len];
        if (this.prev_length >= this.good_match) {
            chain_length >>= 2;
        }
        if (nice_match > this.lookahead) {
            nice_match = this.lookahead;
        }
        do {
            int match = cur_match;
            if (this.window[match + best_len] == scan_end && this.window[(match + best_len) - 1] == scan_end1 && this.window[match] == this.window[scan]) {
                match++;
                if (this.window[match] == this.window[scan + 1]) {
                    scan += 2;
                    match++;
                    do {
                        scan++;
                        match++;
                        if (this.window[scan] != this.window[match]) {
                            break;
                        }
                        scan++;
                        match++;
                        if (this.window[scan] != this.window[match]) {
                            break;
                        }
                        scan++;
                        match++;
                        if (this.window[scan] != this.window[match]) {
                            break;
                        }
                        scan++;
                        match++;
                        if (this.window[scan] != this.window[match]) {
                            break;
                        }
                        scan++;
                        match++;
                        if (this.window[scan] != this.window[match]) {
                            break;
                        }
                        scan++;
                        match++;
                        if (this.window[scan] != this.window[match]) {
                            break;
                        }
                        scan++;
                        match++;
                        if (this.window[scan] != this.window[match]) {
                            break;
                        }
                        scan++;
                        match++;
                        if (this.window[scan] != this.window[match]) {
                            break;
                        }
                    } while (scan < strend);
                    int len = 258 - (strend - scan);
                    scan = strend - 258;
                    if (len > best_len) {
                        this.match_start = cur_match;
                        best_len = len;
                        if (len >= nice_match) {
                            break;
                        }
                        scan_end1 = this.window[(scan + best_len) - 1];
                        scan_end = this.window[scan + best_len];
                    }
                }
            }
            cur_match = this.prev[cur_match & wmask] & InBandBytestreamManager.MAXIMUM_BLOCK_SIZE;
            if (cur_match <= limit) {
                break;
            }
            chain_length--;
        } while (chain_length != 0);
        return best_len <= this.lookahead ? best_len : this.lookahead;
    }

    /* access modifiers changed from: 0000 */
    public int deflateInit(ZStream strm, int level, int bits) {
        return deflateInit2(strm, level, 8, bits, 8, 0);
    }

    /* access modifiers changed from: 0000 */
    public int deflateInit(ZStream strm, int level) {
        return deflateInit(strm, level, 15);
    }

    /* access modifiers changed from: 0000 */
    public int deflateInit2(ZStream strm, int level, int method, int windowBits, int memLevel, int strategy) {
        int noheader = 0;
        strm.msg = null;
        if (level == -1) {
            level = 6;
        }
        if (windowBits < 0) {
            noheader = 1;
            windowBits = -windowBits;
        }
        if (memLevel < 1 || memLevel > 9 || method != 8 || windowBits < 9 || windowBits > 15 || level < 0 || level > 9 || strategy < 0 || strategy > 2) {
            return -2;
        }
        strm.dstate = this;
        this.noheader = noheader;
        this.w_bits = windowBits;
        this.w_size = 1 << this.w_bits;
        this.w_mask = this.w_size - 1;
        this.hash_bits = memLevel + 7;
        this.hash_size = 1 << this.hash_bits;
        this.hash_mask = this.hash_size - 1;
        this.hash_shift = ((this.hash_bits + 3) - 1) / 3;
        this.window = new byte[(this.w_size * 2)];
        this.prev = new short[this.w_size];
        this.head = new short[this.hash_size];
        this.lit_bufsize = 1 << (memLevel + 6);
        this.pending_buf = new byte[(this.lit_bufsize * 4)];
        this.pending_buf_size = this.lit_bufsize * 4;
        this.d_buf = this.lit_bufsize / 2;
        this.l_buf = this.lit_bufsize * 3;
        this.level = level;
        this.strategy = strategy;
        this.method = (byte) method;
        return deflateReset(strm);
    }

    /* access modifiers changed from: 0000 */
    public int deflateReset(ZStream strm) {
        strm.total_out = 0;
        strm.total_in = 0;
        strm.msg = null;
        strm.data_type = 2;
        this.pending = 0;
        this.pending_out = 0;
        if (this.noheader < 0) {
            this.noheader = 0;
        }
        this.status = this.noheader != 0 ? BUSY_STATE : 42;
        strm.adler = strm._adler.adler32(0, null, 0, 0);
        this.last_flush = 0;
        tr_init();
        lm_init();
        return 0;
    }

    /* access modifiers changed from: 0000 */
    public int deflateEnd() {
        if (this.status != 42 && this.status != BUSY_STATE && this.status != FINISH_STATE) {
            return -2;
        }
        this.pending_buf = null;
        this.head = null;
        this.prev = null;
        this.window = null;
        return this.status == BUSY_STATE ? -3 : 0;
    }

    /* access modifiers changed from: 0000 */
    public int deflateParams(ZStream strm, int _level, int _strategy) {
        int err = 0;
        if (_level == -1) {
            _level = 6;
        }
        if (_level < 0 || _level > 9 || _strategy < 0 || _strategy > 2) {
            return -2;
        }
        if (!(config_table[this.level].func == config_table[_level].func || strm.total_in == 0)) {
            err = strm.deflate(1);
        }
        if (this.level != _level) {
            this.level = _level;
            this.max_lazy_match = config_table[this.level].max_lazy;
            this.good_match = config_table[this.level].good_length;
            this.nice_match = config_table[this.level].nice_length;
            this.max_chain_length = config_table[this.level].max_chain;
        }
        this.strategy = _strategy;
        return err;
    }

    /* access modifiers changed from: 0000 */
    public int deflateSetDictionary(ZStream strm, byte[] dictionary, int dictLength) {
        int length = dictLength;
        int index = 0;
        if (dictionary == null || this.status != 42) {
            return -2;
        }
        strm.adler = strm._adler.adler32(strm.adler, dictionary, 0, dictLength);
        if (length < 3) {
            return 0;
        }
        if (length > this.w_size - 262) {
            length = this.w_size - 262;
            index = dictLength - length;
        }
        System.arraycopy(dictionary, index, this.window, 0, length);
        this.strstart = length;
        this.block_start = length;
        this.ins_h = this.window[0] & 255;
        this.ins_h = ((this.ins_h << this.hash_shift) ^ (this.window[1] & 255)) & this.hash_mask;
        for (int n = 0; n <= length - 3; n++) {
            this.ins_h = ((this.ins_h << this.hash_shift) ^ (this.window[n + 2] & 255)) & this.hash_mask;
            this.prev[this.w_mask & n] = this.head[this.ins_h];
            this.head[this.ins_h] = (short) n;
        }
        return 0;
    }

    /* access modifiers changed from: 0000 */
    public int deflate(ZStream strm, int flush) {
        if (flush > 4 || flush < 0) {
            return -2;
        }
        if (strm.next_out == null || ((strm.next_in == null && strm.avail_in != 0) || (this.status == FINISH_STATE && flush != 4))) {
            strm.msg = z_errmsg[4];
            return -2;
        } else if (strm.avail_out == 0) {
            strm.msg = z_errmsg[7];
            return -5;
        } else {
            this.strm = strm;
            int old_flush = this.last_flush;
            this.last_flush = flush;
            if (this.status == 42) {
                int header = (((this.w_bits - 8) << 4) + 8) << 8;
                int level_flags = ((this.level - 1) & 255) >> 1;
                if (level_flags > 3) {
                    level_flags = 3;
                }
                header |= level_flags << 6;
                if (this.strstart != 0) {
                    header |= 32;
                }
                header += 31 - (header % 31);
                this.status = BUSY_STATE;
                putShortMSB(header);
                if (this.strstart != 0) {
                    putShortMSB((int) (strm.adler >>> 16));
                    putShortMSB((int) (strm.adler & 65535));
                }
                strm.adler = strm._adler.adler32(0, null, 0, 0);
            }
            if (this.pending != 0) {
                strm.flush_pending();
                if (strm.avail_out == 0) {
                    this.last_flush = -1;
                    return 0;
                }
            } else if (strm.avail_in == 0 && flush <= old_flush && flush != 4) {
                strm.msg = z_errmsg[7];
                return -5;
            }
            if (this.status != FINISH_STATE || strm.avail_in == 0) {
                if (!(strm.avail_in == 0 && this.lookahead == 0 && (flush == 0 || this.status == FINISH_STATE))) {
                    int bstate = -1;
                    switch (config_table[this.level].func) {
                        case 0:
                            bstate = deflate_stored(flush);
                            break;
                        case 1:
                            bstate = deflate_fast(flush);
                            break;
                        case 2:
                            bstate = deflate_slow(flush);
                            break;
                    }
                    if (bstate == 2 || bstate == 3) {
                        this.status = FINISH_STATE;
                    }
                    if (bstate == 0 || bstate == 2) {
                        if (strm.avail_out == 0) {
                            this.last_flush = -1;
                        }
                        return 0;
                    } else if (bstate == 1) {
                        if (flush == 1) {
                            _tr_align();
                        } else {
                            _tr_stored_block(0, 0, false);
                            if (flush == 3) {
                                for (int i = 0; i < this.hash_size; i++) {
                                    this.head[i] = (short) 0;
                                }
                            }
                        }
                        strm.flush_pending();
                        if (strm.avail_out == 0) {
                            this.last_flush = -1;
                            return 0;
                        }
                    }
                }
                if (flush != 4) {
                    return 0;
                }
                if (this.noheader != 0) {
                    return 1;
                }
                putShortMSB((int) (strm.adler >>> 16));
                putShortMSB((int) (strm.adler & 65535));
                strm.flush_pending();
                this.noheader = -1;
                return this.pending != 0 ? 0 : 1;
            } else {
                strm.msg = z_errmsg[7];
                return -5;
            }
        }
    }
}
