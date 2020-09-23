package org.jitsi.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public final class StringUtils {
    private StringUtils() {
    }

    public static String convertCamelCaseToDisplayString(String camelCase) {
        if (camelCase == null) {
            return null;
        }
        int camelCaseLength = camelCase.length();
        if (camelCaseLength == 0) {
            return camelCase;
        }
        int wordEndIndex = 0;
        int wordBeginIndex = 0;
        StringBuilder display = new StringBuilder();
        while (wordEndIndex < camelCaseLength) {
            if (Character.isUpperCase(camelCase.charAt(wordEndIndex)) && wordBeginIndex != wordEndIndex) {
                display.append(camelCase.substring(wordBeginIndex, wordEndIndex));
                display.append(' ');
                wordBeginIndex = wordEndIndex;
            }
            wordEndIndex++;
        }
        if (wordEndIndex >= camelCaseLength) {
            display.append(camelCase.substring(wordBeginIndex));
        }
        return display.toString();
    }

    public static boolean isNullOrEmpty(String s) {
        return isNullOrEmpty(s, true);
    }

    public static boolean isNullOrEmpty(String s, boolean trim) {
        if (s == null) {
            return true;
        }
        if (trim) {
            s = s.trim();
        }
        if (s.length() != 0) {
            return false;
        }
        return true;
    }

    public static boolean isEquals(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        } else {
            return s1.equals(s2);
        }
    }

    public static InputStream fromString(String string) throws UnsupportedEncodingException {
        return fromString(string, "UTF-8");
    }

    public static InputStream fromString(String string, String encoding) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(string.getBytes(encoding));
    }

    public static byte[] getUTF8Bytes(String string) {
        try {
            return string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return string.getBytes();
        }
    }

    public static String getUTF8String(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new String(bytes);
        }
    }

    public static boolean isNumber(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (!Character.isDigit(string.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsLetters(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (Character.isLetter(string.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static String concatenateWords(String string) {
        char[] chars = string.toCharArray();
        StringBuilder buff = new StringBuilder(chars.length);
        for (char ch : chars) {
            if (ch != ' ') {
                buff.append(ch);
            }
        }
        return buff.toString();
    }

    public static String newString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        Charset defaultCharset = Charset.defaultCharset();
        try {
            return new String(bytes, defaultCharset == null ? "UTF-8" : defaultCharset.name());
        } catch (UnsupportedEncodingException e) {
            return new String(bytes);
        }
    }
}
