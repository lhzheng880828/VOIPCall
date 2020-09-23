package com.jcraft.jzlib;

import java.io.IOException;
import java.io.OutputStream;

public class ZOutputStream extends OutputStream {
    protected byte[] buf;
    protected byte[] buf1;
    protected int bufsize;
    protected boolean compress;
    protected int flush;
    protected OutputStream out;
    protected ZStream z;

    public ZOutputStream(OutputStream out) {
        this.z = new ZStream();
        this.bufsize = 512;
        this.flush = 0;
        this.buf = new byte[this.bufsize];
        this.buf1 = new byte[1];
        this.out = out;
        this.z.inflateInit();
        this.compress = false;
    }

    public ZOutputStream(OutputStream out, int level) {
        this(out, level, false);
    }

    public ZOutputStream(OutputStream out, int level, boolean nowrap) {
        this.z = new ZStream();
        this.bufsize = 512;
        this.flush = 0;
        this.buf = new byte[this.bufsize];
        this.buf1 = new byte[1];
        this.out = out;
        this.z.deflateInit(level, nowrap);
        this.compress = true;
    }

    public void write(int b) throws IOException {
        this.buf1[0] = (byte) b;
        write(this.buf1, 0, 1);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (len != 0) {
            this.z.next_in = b;
            this.z.next_in_index = off;
            this.z.avail_in = len;
            while (true) {
                int err;
                this.z.next_out = this.buf;
                this.z.next_out_index = 0;
                this.z.avail_out = this.bufsize;
                if (this.compress) {
                    err = this.z.deflate(this.flush);
                } else {
                    err = this.z.inflate(this.flush);
                }
                if (err != 0) {
                    throw new ZStreamException(new StringBuffer().append(this.compress ? "de" : "in").append("flating: ").append(this.z.msg).toString());
                }
                this.out.write(this.buf, 0, this.bufsize - this.z.avail_out);
                if (this.z.avail_in <= 0 && this.z.avail_out != 0) {
                    return;
                }
            }
        }
    }

    public int getFlushMode() {
        return this.flush;
    }

    public void setFlushMode(int flush) {
        this.flush = flush;
    }

    public void finish() throws IOException {
        while (true) {
            int err;
            this.z.next_out = this.buf;
            this.z.next_out_index = 0;
            this.z.avail_out = this.bufsize;
            if (this.compress) {
                err = this.z.deflate(4);
            } else {
                err = this.z.inflate(4);
            }
            if (err == 1 || err == 0) {
                if (this.bufsize - this.z.avail_out > 0) {
                    this.out.write(this.buf, 0, this.bufsize - this.z.avail_out);
                }
                if (this.z.avail_in <= 0 && this.z.avail_out != 0) {
                    flush();
                    return;
                }
            } else {
                throw new ZStreamException(new StringBuffer().append(this.compress ? "de" : "in").append("flating: ").append(this.z.msg).toString());
            }
        }
    }

    public void end() {
        if (this.z != null) {
            if (this.compress) {
                this.z.deflateEnd();
            } else {
                this.z.inflateEnd();
            }
            this.z.free();
            this.z = null;
        }
    }

    public void close() throws IOException {
        try {
            finish();
        } catch (IOException e) {
        } catch (Throwable th) {
            end();
            this.out.close();
            this.out = null;
        }
        end();
        this.out.close();
        this.out = null;
    }

    public long getTotalIn() {
        return this.z.total_in;
    }

    public long getTotalOut() {
        return this.z.total_out;
    }

    public void flush() throws IOException {
        this.out.flush();
    }
}
