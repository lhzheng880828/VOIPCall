package net.sf.fmj.media.codec.video.jpeg;

import com.lti.utils.UnsignedUtils;
import net.sf.fmj.utility.ArrayUtility;

public class JpegRTPHeader {
    private static final int BITS_PER_BYTE = 8;
    public static final int HEADER_SIZE = 8;
    private static final int MAX_BYTE = 255;
    private static final int MAX_BYTE_PLUS1 = 256;
    private static final int MAX_SIGNED_BYTE = 127;
    private final int fragmentOffset;
    private final byte height;
    private final byte q;
    private final byte type;
    private final byte typeSpecific;
    private final byte width;

    public static byte[] createQHeader(int length, int[] lqt, int[] cqt) {
        byte[] data = new byte[(length + 4)];
        int i = 0 + 1;
        data[0] = (byte) 0;
        int i2 = i + 1;
        data[i] = (byte) 0;
        i = i2 + 1;
        data[i2] = (byte) ((length >> 8) & 255);
        i2 = i + 1;
        data[i] = (byte) length;
        if (length != 0) {
            int[] zzLqt = RFC2035.createZigZag(lqt);
            int[] zzCqt = RFC2035.createZigZag(cqt);
            System.arraycopy(ArrayUtility.intArrayToByteArray(zzLqt), 0, data, i2, lqt.length);
            i2 = lqt.length + 4;
            System.arraycopy(ArrayUtility.intArrayToByteArray(zzCqt), 0, data, i2, cqt.length);
            i2 += cqt.length;
        }
        return data;
    }

    public static byte[] createRstHeader(int dri, int f, int l, int count) {
        data = new byte[4];
        int i = 0 + 1;
        data[0] = (byte) ((dri >> 8) & 255);
        int i2 = i + 1;
        data[i] = (byte) dri;
        data[i2] = (byte) ((f & 1) << 7);
        data[i2] = (byte) (data[i2] | ((byte) ((l & 1) << 6)));
        i = i2 + 1;
        data[i2] = (byte) (data[i2] | (((byte) ((count >> 8) & 255)) & 63));
        data[i] = (byte) count;
        return data;
    }

    private static void encode3ByteIntBE(int value, byte[] ba, int offset) {
        for (int i = 0; i < 3; i++) {
            int byteValue = value & 255;
            if (byteValue > 127) {
                byteValue -= 256;
            }
            ba[((3 - i) - 1) + offset] = (byte) byteValue;
            value >>= 8;
        }
    }

    public static JpegRTPHeader parse(byte[] data, int offset) {
        int i = offset;
        int i2 = i + 1;
        byte typeSpecific = data[i];
        int fragmentOffset = 0;
        int j = 0;
        while (j < 3) {
            fragmentOffset = (fragmentOffset << 8) + (data[i2] & 255);
            j++;
            i2++;
        }
        i = i2 + 1;
        byte type = data[i2];
        i2 = i + 1;
        byte q = data[i];
        i = i2 + 1;
        byte width = data[i2];
        i2 = i + 1;
        return new JpegRTPHeader(typeSpecific, fragmentOffset, type, q, width, data[i]);
    }

    public JpegRTPHeader(byte typeSpecific, int fragmentOffset, byte type, byte q, byte width, byte height) {
        this.typeSpecific = typeSpecific;
        this.fragmentOffset = fragmentOffset;
        this.type = type;
        this.q = q;
        this.width = width;
        this.height = height;
    }

    public boolean equals(Object o) {
        if (!(o instanceof JpegRTPHeader)) {
            return false;
        }
        JpegRTPHeader oCast = (JpegRTPHeader) o;
        if (this.typeSpecific == oCast.typeSpecific && this.fragmentOffset == oCast.fragmentOffset && this.type == oCast.type && this.q == oCast.q && this.width == oCast.width && this.height == oCast.height) {
            return true;
        }
        return false;
    }

    public int getFragmentOffset() {
        return this.fragmentOffset;
    }

    public int getHeightInBlocks() {
        return UnsignedUtils.uByteToInt(this.height);
    }

    public int getHeightInPixels() {
        return UnsignedUtils.uByteToInt(this.height) * 8;
    }

    public int getQ() {
        return UnsignedUtils.uByteToInt(this.q);
    }

    public int getType() {
        return UnsignedUtils.uByteToInt(this.type);
    }

    public int getTypeSpecific() {
        return UnsignedUtils.uByteToInt(this.typeSpecific);
    }

    public int getWidthInBlocks() {
        return UnsignedUtils.uByteToInt(this.width);
    }

    public int getWidthInPixels() {
        return UnsignedUtils.uByteToInt(this.width) * 8;
    }

    public int hashCode() {
        return ((((this.typeSpecific + this.fragmentOffset) + this.type) + this.q) + this.width) + this.height;
    }

    public byte[] toBytes() {
        data = new byte[8];
        int i = 0 + 1;
        data[0] = this.typeSpecific;
        encode3ByteIntBE(this.fragmentOffset, data, i);
        int i2 = i + 3;
        i = i2 + 1;
        data[i2] = this.type;
        i2 = i + 1;
        data[i] = this.q;
        i = i2 + 1;
        data[i2] = this.width;
        i2 = i + 1;
        data[i] = this.height;
        return data;
    }

    public String toString() {
        return "typeSpecific=" + getTypeSpecific() + " fragmentOffset=" + getFragmentOffset() + " type=" + getType() + " q=" + getQ() + " w=" + getWidthInPixels() + " h=" + getHeightInPixels();
    }
}
