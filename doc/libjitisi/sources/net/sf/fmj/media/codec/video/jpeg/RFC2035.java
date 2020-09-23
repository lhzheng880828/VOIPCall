package net.sf.fmj.media.codec.video.jpeg;

import com.lti.utils.UnsignedUtils;
import com.sun.media.format.WavAudioFormat;
import net.sf.fmj.utility.ArrayUtility;

public class RFC2035 {
    public static final short[] chm_ac_codelens = new short[]{(short) 0, (short) 2, (short) 1, (short) 2, (short) 4, (short) 4, (short) 3, (short) 4, (short) 7, (short) 5, (short) 4, (short) 4, (short) 0, (short) 1, (short) 2, (short) 119};
    public static final short[] chm_ac_symbols = new short[]{(short) 0, (short) 1, (short) 2, (short) 3, (short) 17, (short) 4, (short) 5, (short) 33, (short) 49, (short) 6, (short) 18, (short) 65, (short) 81, (short) 7, (short) 97, (short) 113, (short) 19, (short) 34, (short) 50, (short) 129, (short) 8, (short) 20, (short) 66, (short) 145, (short) 161, (short) 177, (short) 193, (short) 9, (short) 35, (short) 51, (short) 82, (short) 240, (short) 21, (short) 98, (short) 114, (short) 209, (short) 10, (short) 22, (short) 36, (short) 52, (short) 225, (short) 37, (short) 241, (short) 23, (short) 24, (short) 25, (short) 26, (short) 38, (short) 39, (short) 40, (short) 41, (short) 42, (short) 53, (short) 54, (short) 55, (short) 56, (short) 57, (short) 58, (short) 67, (short) 68, (short) 69, (short) 70, (short) 71, (short) 72, (short) 73, (short) 74, (short) 83, (short) 84, (short) 85, (short) 86, (short) 87, (short) 88, (short) 89, (short) 90, (short) 99, (short) 100, (short) 101, (short) 102, (short) 103, (short) 104, (short) 105, (short) 106, (short) 115, (short) 116, (short) 117, (short) 118, (short) 119, (short) 120, (short) 121, (short) 122, (short) 130, (short) 131, (short) 132, (short) 133, (short) 134, (short) 135, (short) 136, (short) 137, (short) 138, (short) 146, (short) 147, (short) 148, (short) 149, (short) 150, (short) 151, (short) 152, (short) 153, (short) 154, (short) 162, (short) 163, (short) 164, (short) 165, (short) 166, (short) 167, (short) 168, (short) 169, (short) 170, (short) 178, (short) 179, (short) 180, (short) 181, (short) 182, (short) 183, (short) 184, (short) 185, (short) 186, (short) 194, (short) 195, (short) 196, (short) 197, (short) 198, (short) 199, (short) 200, (short) 201, (short) 202, (short) 210, (short) 211, (short) 212, (short) 213, (short) 214, (short) 215, (short) 216, (short) 217, (short) 218, (short) 226, (short) 227, (short) 228, (short) 229, (short) 230, (short) 231, (short) 232, (short) 233, (short) 234, (short) 242, (short) 243, (short) 244, (short) 245, (short) 246, (short) 247, (short) 248, (short) 249, (short) 250};
    public static final short[] chm_dc_codelens = new short[]{(short) 0, (short) 3, (short) 1, (short) 1, (short) 1, (short) 1, (short) 1, (short) 1, (short) 1, (short) 1, (short) 1, (short) 0, (short) 0, (short) 0, (short) 0, (short) 0};
    public static final short[] chm_dc_symbols = new short[]{(short) 0, (short) 1, (short) 2, (short) 3, (short) 4, (short) 5, (short) 6, (short) 7, (short) 8, (short) 9, (short) 10, (short) 11};
    public static final int[] jpeg_chroma_quantizer_normal = new int[]{17, 18, 24, 47, 99, 99, 99, 99, 18, 21, 26, 66, 99, 99, 99, 99, 24, 26, 56, 99, 99, 99, 99, 99, 47, 66, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99};
    public static final int[] jpeg_chroma_quantizer_zigzag = new int[]{17, 18, 18, 24, 21, 24, 47, 26, 26, 47, 99, 66, 56, 66, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99};
    public static final int[] jpeg_luma_quantizer_normal = new int[]{16, 11, 10, 16, 24, 40, 51, 61, 12, 12, 14, 19, 26, 58, 60, 55, 14, 13, 16, 24, 40, 57, 69, 56, 14, 17, 22, 29, 51, 87, 80, 62, 18, 22, 37, 56, 68, 109, 103, 77, 24, 35, 55, 64, 81, 104, WavAudioFormat.WAVE_FORMAT_VOXWARE_AC10, 92, 49, 64, 78, 87, 103, WavAudioFormat.WAVE_FORMAT_VOXWARE_TQ40, WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18, 101, 72, 92, 95, 98, WavAudioFormat.WAVE_FORMAT_VOXWARE_AC8, 100, 103, 99};
    public static final int[] jpeg_luma_quantizer_zigzag = new int[]{16, 11, 12, 14, 12, 10, 16, 14, 13, 14, 18, 17, 16, 19, 24, 40, 26, 24, 22, 22, 24, 49, 35, 37, 29, 40, 58, 51, 61, 60, 57, 51, 56, 55, 64, 72, 92, 78, 64, 68, 87, 69, 55, 56, 80, 109, 81, 87, 95, 98, 103, 104, 103, 62, 77, WavAudioFormat.WAVE_FORMAT_VOXWARE_AC10, WavAudioFormat.WAVE_FORMAT_VOXWARE_TQ40, WavAudioFormat.WAVE_FORMAT_VOXWARE_AC8, 100, WavAudioFormat.WAVE_FORMAT_VOXWARE_VR18, 92, 101, 103, 99};
    public static final short[] lum_ac_codelens = new short[]{(short) 0, (short) 2, (short) 1, (short) 3, (short) 3, (short) 2, (short) 4, (short) 3, (short) 5, (short) 5, (short) 4, (short) 4, (short) 0, (short) 0, (short) 1, (short) 125};
    public static final short[] lum_ac_symbols = new short[]{(short) 1, (short) 2, (short) 3, (short) 0, (short) 4, (short) 17, (short) 5, (short) 18, (short) 33, (short) 49, (short) 65, (short) 6, (short) 19, (short) 81, (short) 97, (short) 7, (short) 34, (short) 113, (short) 20, (short) 50, (short) 129, (short) 145, (short) 161, (short) 8, (short) 35, (short) 66, (short) 177, (short) 193, (short) 21, (short) 82, (short) 209, (short) 240, (short) 36, (short) 51, (short) 98, (short) 114, (short) 130, (short) 9, (short) 10, (short) 22, (short) 23, (short) 24, (short) 25, (short) 26, (short) 37, (short) 38, (short) 39, (short) 40, (short) 41, (short) 42, (short) 52, (short) 53, (short) 54, (short) 55, (short) 56, (short) 57, (short) 58, (short) 67, (short) 68, (short) 69, (short) 70, (short) 71, (short) 72, (short) 73, (short) 74, (short) 83, (short) 84, (short) 85, (short) 86, (short) 87, (short) 88, (short) 89, (short) 90, (short) 99, (short) 100, (short) 101, (short) 102, (short) 103, (short) 104, (short) 105, (short) 106, (short) 115, (short) 116, (short) 117, (short) 118, (short) 119, (short) 120, (short) 121, (short) 122, (short) 131, (short) 132, (short) 133, (short) 134, (short) 135, (short) 136, (short) 137, (short) 138, (short) 146, (short) 147, (short) 148, (short) 149, (short) 150, (short) 151, (short) 152, (short) 153, (short) 154, (short) 162, (short) 163, (short) 164, (short) 165, (short) 166, (short) 167, (short) 168, (short) 169, (short) 170, (short) 178, (short) 179, (short) 180, (short) 181, (short) 182, (short) 183, (short) 184, (short) 185, (short) 186, (short) 194, (short) 195, (short) 196, (short) 197, (short) 198, (short) 199, (short) 200, (short) 201, (short) 202, (short) 210, (short) 211, (short) 212, (short) 213, (short) 214, (short) 215, (short) 216, (short) 217, (short) 218, (short) 225, (short) 226, (short) 227, (short) 228, (short) 229, (short) 230, (short) 231, (short) 232, (short) 233, (short) 234, (short) 241, (short) 242, (short) 243, (short) 244, (short) 245, (short) 246, (short) 247, (short) 248, (short) 249, (short) 250};
    public static final short[] lum_dc_codelens = new short[]{(short) 0, (short) 1, (short) 5, (short) 1, (short) 1, (short) 1, (short) 1, (short) 1, (short) 1, (short) 0, (short) 0, (short) 0, (short) 0, (short) 0, (short) 0, (short) 0};
    public static final short[] lum_dc_symbols = new short[]{(short) 0, (short) 1, (short) 2, (short) 3, (short) 4, (short) 5, (short) 6, (short) 7, (short) 8, (short) 9, (short) 10, (short) 11};

    public static int[] createZigZag(int[] array) {
        return createZigZag(array, 8, 8);
    }

    public static int[] createZigZag(int[] array, int xmax, int ymax) {
        int[] zz = new int[array.length];
        if (array.length != xmax * ymax) {
            throw new IllegalArgumentException();
        }
        int zzi = 0;
        int x = 0;
        int y = 0;
        zz[0] = array[0];
        while ((y * xmax) + x < (xmax * ymax) - 1) {
            if (x < xmax - 1) {
                x++;
            } else {
                y++;
            }
            zzi++;
            zz[zzi] = array[(y * xmax) + x];
            while (x > 0 && y < ymax - 1) {
                x--;
                y++;
                zzi++;
                zz[zzi] = array[(y * xmax) + x];
            }
            if (y < ymax - 1) {
                y++;
            } else {
                x++;
            }
            zzi++;
            zz[zzi] = array[(y * xmax) + x];
            while (y > 0 && x < xmax - 1) {
                y--;
                x++;
                zzi++;
                zz[zzi] = array[(y * xmax) + x];
            }
        }
        return zz;
    }

    private static int MakeDRIHeader(byte[] p, int i, int dri) {
        int i2 = i + 1;
        p[i] = (byte) -1;
        i = i2 + 1;
        p[i2] = (byte) -35;
        i2 = i + 1;
        p[i] = (byte) 0;
        i = i2 + 1;
        p[i2] = (byte) 4;
        i2 = i + 1;
        p[i] = (byte) (dri >> 8);
        i = i2 + 1;
        p[i2] = (byte) (dri & UnsignedUtils.MAX_UBYTE);
        return i;
    }

    public static int MakeHeaders(boolean includeSOI, byte[] p, int i, int type, int q, int w, int h) {
        return MakeHeaders(includeSOI, p, i, type, q, w, h, null, null, 0);
    }

    public static int MakeHeaders(boolean includeSOI, byte[] p, int i, int type, int q, int w, int h, byte[] luma, byte[] chroma, int dri) {
        byte[] lqt;
        byte[] cqt;
        int i2;
        if (luma == null && chroma == null) {
            lqt = new byte[64];
            cqt = new byte[64];
            MakeTables(q, lqt, cqt);
        } else {
            lqt = luma;
            cqt = chroma;
        }
        w <<= 3;
        h <<= 3;
        if (includeSOI) {
            i2 = i + 1;
            p[i] = (byte) -1;
            i = i2 + 1;
            p[i2] = (byte) -40;
        }
        i = MakeQuantHeader(p, MakeQuantHeader(p, i, lqt, 0), cqt, 1);
        if (dri != 0) {
            i = MakeDRIHeader(p, i, dri);
        }
        i = MakeHuffmanHeader(p, MakeHuffmanHeader(p, MakeHuffmanHeader(p, MakeHuffmanHeader(p, i, lum_dc_codelens, lum_dc_codelens.length, lum_dc_symbols, lum_dc_symbols.length, 0, 0), lum_ac_codelens, lum_ac_codelens.length, lum_ac_symbols, lum_ac_symbols.length, 0, 1), chm_dc_codelens, chm_dc_codelens.length, chm_dc_symbols, chm_dc_symbols.length, 1, 0), chm_ac_codelens, chm_ac_codelens.length, chm_ac_symbols, chm_ac_symbols.length, 1, 1);
        i2 = i + 1;
        p[i] = (byte) -1;
        i = i2 + 1;
        p[i2] = (byte) -64;
        i2 = i + 1;
        p[i] = (byte) 0;
        i = i2 + 1;
        p[i2] = (byte) 17;
        i2 = i + 1;
        p[i] = (byte) 8;
        i = i2 + 1;
        p[i2] = (byte) (h >> 8);
        i2 = i + 1;
        p[i] = (byte) h;
        i = i2 + 1;
        p[i2] = (byte) (w >> 8);
        i2 = i + 1;
        p[i] = (byte) w;
        i = i2 + 1;
        p[i2] = (byte) 3;
        i2 = i + 1;
        p[i] = (byte) 1;
        if (type == 0) {
            i = i2 + 1;
            p[i2] = (byte) 33;
        } else {
            i = i2 + 1;
            p[i2] = (byte) 34;
        }
        i2 = i + 1;
        p[i] = (byte) 0;
        i = i2 + 1;
        p[i2] = (byte) 2;
        i2 = i + 1;
        p[i] = (byte) 17;
        i = i2 + 1;
        p[i2] = (byte) 1;
        i2 = i + 1;
        p[i] = (byte) 3;
        i = i2 + 1;
        p[i2] = (byte) 17;
        i2 = i + 1;
        p[i] = (byte) 1;
        i = i2 + 1;
        p[i2] = (byte) -1;
        i2 = i + 1;
        p[i] = (byte) -38;
        i = i2 + 1;
        p[i2] = (byte) 0;
        i2 = i + 1;
        p[i] = (byte) 12;
        i = i2 + 1;
        p[i2] = (byte) 3;
        i2 = i + 1;
        p[i] = (byte) 1;
        i = i2 + 1;
        p[i2] = (byte) 0;
        i2 = i + 1;
        p[i] = (byte) 2;
        i = i2 + 1;
        p[i2] = (byte) 17;
        i2 = i + 1;
        p[i] = (byte) 3;
        i = i2 + 1;
        p[i2] = (byte) 17;
        i2 = i + 1;
        p[i] = (byte) 0;
        i = i2 + 1;
        p[i2] = (byte) 63;
        i2 = i + 1;
        p[i] = (byte) 0;
        return i2;
    }

    private static int MakeHuffmanHeader(byte[] p, int i, short[] codelens, int ncodes, short[] symbols, int nsymbols, int tableNo, int tableClass) {
        int i2 = i + 1;
        p[i] = (byte) -1;
        i = i2 + 1;
        p[i2] = (byte) -60;
        i2 = i + 1;
        p[i] = (byte) 0;
        i = i2 + 1;
        p[i2] = (byte) ((ncodes + 3) + nsymbols);
        i2 = i + 1;
        p[i] = (byte) ((tableClass << 4) | tableNo);
        System.arraycopy(ArrayUtility.shortArrayToByteArray(codelens), 0, p, i2, ncodes);
        i = i2 + ncodes;
        System.arraycopy(ArrayUtility.shortArrayToByteArray(symbols), 0, p, i, nsymbols);
        return i + nsymbols;
    }

    public static int MakeQuantHeader(byte[] p, int i, byte[] qt, int tableNo) {
        int i2 = i + 1;
        p[i] = (byte) -1;
        i = i2 + 1;
        p[i2] = (byte) -37;
        i2 = i + 1;
        p[i] = (byte) 0;
        i = i2 + 1;
        p[i2] = (byte) 67;
        i2 = i + 1;
        p[i] = (byte) tableNo;
        System.arraycopy(qt, 0, p, i2, 64);
        return i2 + 64;
    }

    private static void MakeTables(int q, byte[] lum_q, byte[] chr_q) {
        MakeTables(q, lum_q, chr_q, jpeg_luma_quantizer_zigzag, jpeg_luma_quantizer_zigzag);
    }

    public static void MakeTables(int q, byte[] lum_q, byte[] chr_q, int[] jpeg_luma, int[] jpeg_chroma) {
        int factor = q;
        if (q < 1) {
            factor = 1;
        }
        if (q > 99) {
            factor = 99;
        }
        if (q < 50) {
            q = 5000 / factor;
        } else {
            q = 200 - (factor * 2);
        }
        for (int i = 0; i < 64; i++) {
            int lq = ((jpeg_luma[i] * q) + 50) / 100;
            int cq = ((jpeg_chroma[i] * q) + 50) / 100;
            if (lq < 1) {
                lq = 1;
            } else if (lq > UnsignedUtils.MAX_UBYTE) {
                lq = UnsignedUtils.MAX_UBYTE;
            }
            lum_q[i] = (byte) lq;
            if (cq < 1) {
                cq = 1;
            } else if (cq > UnsignedUtils.MAX_UBYTE) {
                cq = UnsignedUtils.MAX_UBYTE;
            }
            chr_q[i] = (byte) cq;
        }
    }
}
