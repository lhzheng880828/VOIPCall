package org.jitsi.impl.neomedia.codec.audio.silk;

import com.lti.utils.UnsignedUtils;

public class RangeCoder {
    static void SKP_Silk_range_encoder(SKP_Silk_range_coder_state psRC, int data, int[] prob, int prob_offset) {
        long base_Q32 = psRC.base_Q32;
        long range_Q16 = psRC.range_Q16;
        int bufferIx = psRC.bufferIx;
        byte[] buffer = psRC.buffer;
        if (psRC.error == 0) {
            long low_Q16 = (long) prob[prob_offset + data];
            long base_tmp = base_Q32;
            base_Q32 = (base_Q32 + ((range_Q16 * low_Q16) & 4294967295L)) & 4294967295L;
            long range_Q32 = ((((long) prob[(prob_offset + data) + 1]) - low_Q16) * range_Q16) & 4294967295L;
            if (base_Q32 < base_tmp) {
                int bufferIx_tmp = bufferIx;
                byte b;
                do {
                    bufferIx_tmp--;
                    b = (byte) (buffer[bufferIx_tmp] + 1);
                    buffer[bufferIx_tmp] = b;
                } while (b == (byte) 0);
            }
            if ((4278190080L & range_Q32) != 0) {
                range_Q16 = range_Q32 >>> 16;
            } else {
                int bufferIx2;
                if ((4294901760L & range_Q32) != 0) {
                    range_Q16 = range_Q32 >>> 8;
                } else {
                    range_Q16 = range_Q32;
                    if (bufferIx >= psRC.bufferLength) {
                        psRC.error = -1;
                        return;
                    }
                    bufferIx2 = bufferIx + 1;
                    buffer[bufferIx] = (byte) ((int) (base_Q32 >>> 24));
                    base_Q32 = (base_Q32 << 8) & 4294967295L;
                    bufferIx = bufferIx2;
                }
                if (bufferIx >= psRC.bufferLength) {
                    psRC.error = -1;
                    return;
                }
                bufferIx2 = bufferIx + 1;
                buffer[bufferIx] = (byte) ((int) (base_Q32 >>> 24));
                base_Q32 = (base_Q32 << 8) & 4294967295L;
                bufferIx = bufferIx2;
            }
            psRC.base_Q32 = base_Q32;
            psRC.range_Q16 = range_Q16;
            psRC.bufferIx = bufferIx;
        }
    }

    static void SKP_Silk_range_encoder_multi(SKP_Silk_range_coder_state psRC, int[] data, int[][] prob, int nSymbols) {
        for (int k = 0; k < nSymbols; k++) {
            SKP_Silk_range_encoder(psRC, data[k], prob[k], 0);
        }
    }

    static void SKP_Silk_range_decoder(int[] data, int data_offset, SKP_Silk_range_coder_state psRC, int[] prob, int prob_offset, int probIx) {
        long base_Q32 = psRC.base_Q32;
        long range_Q16 = psRC.range_Q16;
        int bufferIx = psRC.bufferIx;
        byte[] buffer = psRC.buffer;
        if (psRC.error != 0) {
            data[data_offset + 0] = 0;
            return;
        }
        long low_Q16;
        long high_Q16 = (long) prob[prob_offset + probIx];
        if (((range_Q16 * high_Q16) & 4294967295L) > base_Q32) {
            do {
                probIx--;
                low_Q16 = (long) prob[probIx + prob_offset];
                if (((range_Q16 * low_Q16) & 4294967295L) > base_Q32) {
                    high_Q16 = low_Q16;
                }
            } while (high_Q16 != 0);
            psRC.error = -2;
            data[data_offset + 0] = 0;
            return;
        }
        do {
            low_Q16 = high_Q16;
            probIx++;
            high_Q16 = (long) prob[probIx + prob_offset];
            if (((range_Q16 * high_Q16) & 4294967295L) > base_Q32) {
                probIx--;
            }
        } while (high_Q16 != 65535);
        psRC.error = -2;
        data[data_offset + 0] = 0;
        return;
        data[data_offset + 0] = probIx;
        base_Q32 = (base_Q32 - ((range_Q16 * low_Q16) & 4294967295L)) & 4294967295L;
        long range_Q32 = (((high_Q16 - low_Q16) * range_Q16) & 4294967295L) & 4294967295L;
        if ((4278190080L & range_Q32) != 0) {
            range_Q16 = range_Q32 >>> 16;
        } else {
            if ((4294901760L & range_Q32) != 0) {
                range_Q16 = range_Q32 >>> 8;
                if ((base_Q32 >>> 24) != 0) {
                    psRC.error = -3;
                    data[data_offset + 0] = 0;
                    return;
                }
            }
            range_Q16 = range_Q32;
            if ((base_Q32 >>> 16) != 0) {
                psRC.error = -3;
                data[data_offset + 0] = 0;
                return;
            }
            base_Q32 = (base_Q32 << 8) & 4294967295L;
            if (bufferIx < psRC.bufferLength) {
                base_Q32 |= (long) (buffer[4 + bufferIx] & UnsignedUtils.MAX_UBYTE);
                bufferIx++;
            }
            base_Q32 = (base_Q32 << 8) & 4294967295L;
            if (bufferIx < psRC.bufferLength) {
                base_Q32 |= (long) (buffer[4 + bufferIx] & UnsignedUtils.MAX_UBYTE);
                bufferIx++;
            }
        }
        if (range_Q16 == 0) {
            psRC.error = -4;
            data[data_offset + 0] = 0;
            return;
        }
        psRC.base_Q32 = base_Q32;
        psRC.range_Q16 = range_Q16;
        psRC.bufferIx = bufferIx;
    }

    static void SKP_Silk_range_decoder_multi(int[] data, SKP_Silk_range_coder_state psRC, int[][] prob, int[] probStartIx, int nSymbols) {
        for (int k = 0; k < nSymbols; k++) {
            SKP_Silk_range_decoder(data, k, psRC, prob[k], 0, probStartIx[k]);
        }
    }

    static void SKP_Silk_range_enc_init(SKP_Silk_range_coder_state psRC) {
        psRC.bufferLength = 1024;
        psRC.range_Q16 = 65535;
        psRC.bufferIx = 0;
        psRC.base_Q32 = 0;
        psRC.error = 0;
    }

    static void SKP_Silk_range_dec_init(SKP_Silk_range_coder_state psRC, byte[] buffer, int buffer_offset, int bufferLength) {
        if (bufferLength > 1024) {
            psRC.error = -8;
            return;
        }
        System.arraycopy(buffer, buffer_offset, psRC.buffer, 0, bufferLength);
        psRC.bufferLength = bufferLength;
        psRC.bufferIx = 0;
        psRC.base_Q32 = ((long) (((((buffer[buffer_offset + 0] & UnsignedUtils.MAX_UBYTE) << 24) | ((buffer[buffer_offset + 1] & UnsignedUtils.MAX_UBYTE) << 16)) | ((buffer[buffer_offset + 2] & UnsignedUtils.MAX_UBYTE) << 8)) | (buffer[buffer_offset + 3] & UnsignedUtils.MAX_UBYTE))) & 4294967295L;
        psRC.range_Q16 = 65535;
        psRC.error = 0;
    }

    static int SKP_Silk_range_coder_get_length(SKP_Silk_range_coder_state psRC, int[] nBytes) {
        int nBits = ((psRC.bufferIx << 3) + Macros.SKP_Silk_CLZ32((int) (psRC.range_Q16 - 1))) - 14;
        nBytes[0] = (nBits + 7) >> 3;
        return nBits;
    }

    static void SKP_Silk_range_enc_wrap_up(SKP_Silk_range_coder_state psRC) {
        byte[] bArr;
        int i;
        long base_Q24 = psRC.base_Q32 >>> 8;
        int[] nBytes_ptr = new int[1];
        int bits_in_stream = SKP_Silk_range_coder_get_length(psRC, nBytes_ptr);
        int nBytes = nBytes_ptr[0];
        int bits_to_store = bits_in_stream - (psRC.bufferIx << 3);
        base_Q24 = ((base_Q24 + ((long) (8388608 >>> (bits_to_store - 1)))) & 4294967295L) & ((long) (-1 << (24 - bits_to_store)));
        if ((16777216 & base_Q24) != 0) {
            int bufferIx_tmp = psRC.bufferIx;
            byte b;
            do {
                bArr = psRC.buffer;
                bufferIx_tmp--;
                b = (byte) (bArr[bufferIx_tmp] + 1);
                bArr[bufferIx_tmp] = b;
            } while (b == (byte) 0);
        }
        if (psRC.bufferIx < psRC.bufferLength) {
            bArr = psRC.buffer;
            i = psRC.bufferIx;
            psRC.bufferIx = i + 1;
            bArr[i] = (byte) ((int) (base_Q24 >>> 16));
            if (bits_to_store > 8 && psRC.bufferIx < psRC.bufferLength) {
                bArr = psRC.buffer;
                i = psRC.bufferIx;
                psRC.bufferIx = i + 1;
                bArr[i] = (byte) ((int) (base_Q24 >>> 8));
            }
        }
        if ((bits_in_stream & 7) != 0) {
            int mask = UnsignedUtils.MAX_UBYTE >> (bits_in_stream & 7);
            if (nBytes - 1 < psRC.bufferLength) {
                bArr = psRC.buffer;
                i = nBytes - 1;
                bArr[i] = (byte) (bArr[i] | mask);
            }
        }
    }

    static void SKP_Silk_range_coder_check_after_decoding(SKP_Silk_range_coder_state psRC) {
        int[] nBytes_ptr = new int[1];
        int bits_in_stream = SKP_Silk_range_coder_get_length(psRC, nBytes_ptr);
        int nBytes = nBytes_ptr[0];
        if (nBytes - 1 >= psRC.bufferLength) {
            psRC.error = -5;
        } else if ((bits_in_stream & 7) != 0) {
            int mask = UnsignedUtils.MAX_UBYTE >> (bits_in_stream & 7);
            if ((psRC.buffer[nBytes - 1] & mask) != mask) {
                psRC.error = -5;
            }
        }
    }
}
