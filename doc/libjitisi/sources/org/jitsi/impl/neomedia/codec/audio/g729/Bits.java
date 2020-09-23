package org.jitsi.impl.neomedia.codec.audio.g729;

class Bits {
    Bits() {
    }

    static void prm2bits_ld8k(int[] prm, short[] bits) {
        int[] bitsno = TabLd8k.bitsno;
        bits[0] = (short) 27425;
        int j = 0 + 1;
        bits[j] = (short) 80;
        j++;
        for (int i = 0; i < 11; i++) {
            int2bin(prm[i], bitsno[i], bits, j);
            j += bitsno[i];
        }
    }

    private static void int2bin(int value, int no_of_bits, short[] bitstream, int bitstream_offset) {
        int pt_bitstream = bitstream_offset + no_of_bits;
        for (int i = 0; i < no_of_bits; i++) {
            if ((value & 1) == 0) {
                pt_bitstream--;
                bitstream[pt_bitstream] = (short) 127;
            } else {
                pt_bitstream--;
                bitstream[pt_bitstream] = (short) 129;
            }
            value >>= 1;
        }
    }

    static void bits2prm_ld8k(short[] bits, int[] prm) {
        bits2prm_ld8k(bits, 0, prm, 0);
    }

    static void bits2prm_ld8k(short[] bits, int bits_offset, int[] prm, int prm_offset) {
        int[] bitsno = TabLd8k.bitsno;
        for (int i = 0; i < 11; i++) {
            prm[i + prm_offset] = bin2int(bitsno[i], bits, bits_offset);
            bits_offset += bitsno[i];
        }
    }

    private static int bin2int(int no_of_bits, short[] bitstream, int bitstream_offset) {
        int value = 0;
        int i = 0;
        int bitstream_offset2 = bitstream_offset;
        while (i < no_of_bits) {
            value <<= 1;
            bitstream_offset = bitstream_offset2 + 1;
            if (bitstream[bitstream_offset2] == (short) 129) {
                value++;
            }
            i++;
            bitstream_offset2 = bitstream_offset;
        }
        return value;
    }
}
