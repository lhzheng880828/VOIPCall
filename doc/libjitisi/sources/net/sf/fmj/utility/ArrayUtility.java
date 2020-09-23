package net.sf.fmj.utility;

import com.lti.utils.UnsignedUtils;

public class ArrayUtility {
    public static int[] byteArrayToIntArray(byte[] b) {
        int[] result = new int[b.length];
        for (int i = 0; i < b.length; i++) {
            result[i] = b[i] & UnsignedUtils.MAX_UBYTE;
        }
        return result;
    }

    public static byte[] copyOfRange(byte[] inputData, int from, int to) {
        return intArrayToByteArray(copyOfRange(byteArrayToIntArray(inputData), from, to));
    }

    public static int[] copyOfRange(int[] inputData, int from, int to) {
        if (inputData.length <= from || from < 0) {
            throw new ArrayIndexOutOfBoundsException(from);
        } else if (to > inputData.length) {
            throw new ArrayIndexOutOfBoundsException(to);
        } else if (to < from) {
            throw new IllegalArgumentException();
        } else {
            int[] output = new int[(to - from)];
            int i = from;
            int j = 0;
            while (i < to) {
                int j2 = j + 1;
                output[j] = inputData[i];
                i++;
                j = j2;
            }
            return output;
        }
    }

    public static byte[] intArrayToByteArray(int[] b) {
        byte[] result = new byte[b.length];
        for (int i = 0; i < b.length; i++) {
            result[i] = (byte) b[i];
        }
        return result;
    }

    public static byte[] shortArrayToByteArray(short[] b) {
        byte[] result = new byte[b.length];
        for (int i = 0; i < b.length; i++) {
            result[i] = (byte) b[i];
        }
        return result;
    }
}
