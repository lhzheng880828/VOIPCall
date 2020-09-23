package org.jitsi.impl.neomedia.codec.audio.ilbc;

import com.lti.utils.UnsignedUtils;

class bitstream {
    int bitcount;
    final byte[] buffer;
    final int buffer_len;
    final int buffer_off;
    private int buffer_pos;
    int pos = 0;

    public bitstream(byte[] buffer, int offset, int length) {
        this.buffer = buffer;
        this.buffer_off = offset;
        this.buffer_len = length;
        this.buffer_pos = this.buffer_off;
        this.bitcount = 0;
    }

    public bitpack packsplit(int index, int bitno_firstpart, int bitno_total) {
        int bitno_rest = bitno_total - bitno_firstpart;
        bitpack rval = new bitpack();
        rval.set_firstpart(index >>> bitno_rest);
        rval.set_rest(index - (rval.get_firstpart() << bitno_rest));
        return rval;
    }

    /* access modifiers changed from: 0000 */
    public int packcombine(int index, int rest, int bitno_rest) {
        return (index << bitno_rest) + rest;
    }

    /* access modifiers changed from: 0000 */
    public void dopack(int index, int bitno) {
        this.bitcount += bitno;
        if (this.pos == 0) {
            this.buffer[this.buffer_pos] = (byte) 0;
        }
        while (bitno > 0) {
            if (this.pos == 8) {
                this.pos = 0;
                this.buffer_pos++;
                this.buffer[this.buffer_pos] = (byte) 0;
            }
            int posLeft = 8 - this.pos;
            byte[] bArr;
            int i;
            if (bitno <= posLeft) {
                bArr = this.buffer;
                i = this.buffer_pos;
                bArr[i] = (byte) (bArr[i] | ((byte) (index << (posLeft - bitno))));
                this.pos += bitno;
                bitno = 0;
            } else {
                bArr = this.buffer;
                i = this.buffer_pos;
                bArr[i] = (byte) (bArr[i] | ((byte) (index >>> (bitno - posLeft))));
                this.pos = 8;
                index -= (index >>> (bitno - posLeft)) << (bitno - posLeft);
                bitno -= posLeft;
            }
        }
    }

    public int unpack(int bitno) {
        int index = 0;
        while (bitno > 0) {
            if (this.pos == 8) {
                this.pos = 0;
                this.buffer_pos++;
            }
            int BitsLeft = 8 - this.pos;
            if (BitsLeft >= bitno) {
                index += ((this.buffer[this.buffer_pos] << this.pos) & UnsignedUtils.MAX_UBYTE) >>> (8 - bitno);
                this.pos += bitno;
                bitno = 0;
            } else {
                if (8 - bitno > 0) {
                    index += ((this.buffer[this.buffer_pos] << this.pos) & UnsignedUtils.MAX_UBYTE) >>> (8 - bitno);
                    this.pos = 8;
                } else {
                    index += ((this.buffer[this.buffer_pos] << this.pos) & UnsignedUtils.MAX_UBYTE) << (bitno - 8);
                    this.pos = 8;
                }
                bitno -= BitsLeft;
            }
        }
        return index;
    }
}
