package net.sf.fmj.media.codec.video.jpeg;

import com.lti.utils.UnsignedUtils;
import net.sf.fmj.utility.ArrayUtility;

public class JpegStripper {
    private static boolean STRIP = false;

    static void dump(int[] s) {
        dump(s, 10);
    }

    static void dump(int[] s, int length) {
        for (int i = 0; i < (s.length / length) + 1; i++) {
            int j = i * length;
            while (j < (i * length) + length && j < s.length) {
                String tmp = Integer.toHexString(s[j]);
                if (tmp.length() < 2) {
                    tmp = 0 + tmp;
                }
                System.out.print(tmp + " ");
                j++;
            }
            System.out.println("");
        }
        System.out.println("Length: " + s.length);
    }

    private static int findNextMarker(int[] output) {
        int i = 0;
        while (i < output.length) {
            if (output[i] != 255 || output[i + 1] == 0 || (output[i + 1] >= 208 && output[i + 1] <= 215)) {
                i++;
            } else {
                STRIP = true;
                return i;
            }
        }
        STRIP = false;
        return output.length;
    }

    public static byte[] removeHeaders(byte[] ba) {
        int i;
        int[] ia = new int[ba.length];
        for (i = 0; i < ba.length; i++) {
            ia[i] = ba[i] & UnsignedUtils.MAX_UBYTE;
        }
        ia = removeHeaders(ia);
        ba = new byte[ia.length];
        for (i = 0; i < ia.length; i++) {
            ba[i] = (byte) ia[i];
        }
        return ba;
    }

    public static int[] removeHeaders(int[] input) {
        return stripTrailingHeaders(stripLeadingHeaders(input));
    }

    private static int[] stripHeader(int[] s) {
        return ArrayUtility.copyOfRange(s, 2, s.length);
    }

    private static int[] stripHeaderContent(int[] s) {
        s = stripHeader(s);
        return ArrayUtility.copyOfRange(s, (s[0] * 256) + s[1], s.length);
    }

    private static int[] stripLeadingHeaders(int[] input) {
        int[] output = input;
        if (input[0] != UnsignedUtils.MAX_UBYTE) {
            return output;
        }
        switch (input[1]) {
            case 192:
            case 196:
            case 218:
            case 219:
            case 221:
            case 224:
                output = stripLeadingHeaders(stripHeaderContent(output));
                break;
        }
        if (input[1] == 216) {
            return stripLeadingHeaders(stripHeader(output));
        }
        return output;
    }

    private static int[] stripOtherMarkers(int[] output) {
        return ArrayUtility.copyOfRange(output, 0, findNextMarker(output));
    }

    private static int[] stripTrailingHeaders(int[] output) {
        output = stripOtherMarkers(output);
        if (STRIP) {
            return stripTrailingHeaders(output);
        }
        return output;
    }
}
