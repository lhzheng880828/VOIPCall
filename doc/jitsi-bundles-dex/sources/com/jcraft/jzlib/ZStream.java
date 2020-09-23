package com.jcraft.jzlib;

public final class ZStream {
    private static final int DEF_WBITS = 15;
    private static final int MAX_MEM_LEVEL = 9;
    private static final int MAX_WBITS = 15;
    private static final int Z_BUF_ERROR = -5;
    private static final int Z_DATA_ERROR = -3;
    private static final int Z_ERRNO = -1;
    private static final int Z_FINISH = 4;
    private static final int Z_FULL_FLUSH = 3;
    private static final int Z_MEM_ERROR = -4;
    private static final int Z_NEED_DICT = 2;
    private static final int Z_NO_FLUSH = 0;
    private static final int Z_OK = 0;
    private static final int Z_PARTIAL_FLUSH = 1;
    private static final int Z_STREAM_END = 1;
    private static final int Z_STREAM_ERROR = -2;
    private static final int Z_SYNC_FLUSH = 2;
    private static final int Z_VERSION_ERROR = -6;
    Adler32 _adler = new Adler32();
    public long adler;
    public int avail_in;
    public int avail_out;
    int data_type;
    Deflate dstate;
    Inflate istate;
    public String msg;
    public byte[] next_in;
    public int next_in_index;
    public byte[] next_out;
    public int next_out_index;
    public long total_in;
    public long total_out;

    public int inflateInit() {
        return inflateInit(15);
    }

    public int inflateInit(boolean nowrap) {
        return inflateInit(15, nowrap);
    }

    public int inflateInit(int w) {
        return inflateInit(w, false);
    }

    public int inflateInit(int w, boolean nowrap) {
        this.istate = new Inflate();
        Inflate inflate = this.istate;
        if (nowrap) {
            w = -w;
        }
        return inflate.inflateInit(this, w);
    }

    public int inflate(int f) {
        if (this.istate == null) {
            return -2;
        }
        return this.istate.inflate(this, f);
    }

    public int inflateEnd() {
        if (this.istate == null) {
            return -2;
        }
        int ret = this.istate.inflateEnd(this);
        this.istate = null;
        return ret;
    }

    public int inflateSync() {
        if (this.istate == null) {
            return -2;
        }
        return this.istate.inflateSync(this);
    }

    public int inflateSetDictionary(byte[] dictionary, int dictLength) {
        if (this.istate == null) {
            return -2;
        }
        return this.istate.inflateSetDictionary(this, dictionary, dictLength);
    }

    public int deflateInit(int level) {
        return deflateInit(level, 15);
    }

    public int deflateInit(int level, boolean nowrap) {
        return deflateInit(level, 15, nowrap);
    }

    public int deflateInit(int level, int bits) {
        return deflateInit(level, bits, false);
    }

    public int deflateInit(int level, int bits, boolean nowrap) {
        this.dstate = new Deflate();
        Deflate deflate = this.dstate;
        if (nowrap) {
            bits = -bits;
        }
        return deflate.deflateInit(this, level, bits);
    }

    public int deflate(int flush) {
        if (this.dstate == null) {
            return -2;
        }
        return this.dstate.deflate(this, flush);
    }

    public int deflateEnd() {
        if (this.dstate == null) {
            return -2;
        }
        int ret = this.dstate.deflateEnd();
        this.dstate = null;
        return ret;
    }

    public int deflateParams(int level, int strategy) {
        if (this.dstate == null) {
            return -2;
        }
        return this.dstate.deflateParams(this, level, strategy);
    }

    public int deflateSetDictionary(byte[] dictionary, int dictLength) {
        if (this.dstate == null) {
            return -2;
        }
        return this.dstate.deflateSetDictionary(this, dictionary, dictLength);
    }

    /* access modifiers changed from: 0000 */
    public void flush_pending() {
        int len = this.dstate.pending;
        if (len > this.avail_out) {
            len = this.avail_out;
        }
        if (len != 0) {
            if (this.dstate.pending_buf.length <= this.dstate.pending_out || this.next_out.length <= this.next_out_index || this.dstate.pending_buf.length < this.dstate.pending_out + len || this.next_out.length < this.next_out_index + len) {
                System.out.println(new StringBuffer().append(this.dstate.pending_buf.length).append(", ").append(this.dstate.pending_out).append(", ").append(this.next_out.length).append(", ").append(this.next_out_index).append(", ").append(len).toString());
                System.out.println(new StringBuffer().append("avail_out=").append(this.avail_out).toString());
            }
            System.arraycopy(this.dstate.pending_buf, this.dstate.pending_out, this.next_out, this.next_out_index, len);
            this.next_out_index += len;
            Deflate deflate = this.dstate;
            deflate.pending_out += len;
            this.total_out += (long) len;
            this.avail_out -= len;
            deflate = this.dstate;
            deflate.pending -= len;
            if (this.dstate.pending == 0) {
                this.dstate.pending_out = 0;
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public int read_buf(byte[] buf, int start, int size) {
        int len = this.avail_in;
        if (len > size) {
            len = size;
        }
        if (len == 0) {
            return 0;
        }
        this.avail_in -= len;
        if (this.dstate.noheader == 0) {
            this.adler = this._adler.adler32(this.adler, this.next_in, this.next_in_index, len);
        }
        System.arraycopy(this.next_in, this.next_in_index, buf, start, len);
        this.next_in_index += len;
        this.total_in += (long) len;
        return len;
    }

    public void free() {
        this.next_in = null;
        this.next_out = null;
        this.msg = null;
        this._adler = null;
    }
}
