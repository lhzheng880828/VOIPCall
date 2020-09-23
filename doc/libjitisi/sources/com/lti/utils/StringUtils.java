package com.lti.utils;

import net.iharder.Base64;

public final class StringUtils {
    private static final int MAX_STANDARD_ASCII = 255;
    private static final int RADIX_16 = 16;

    public static String byteArrayToBase64String(byte[] value) {
        return Base64.encodeBytes(value);
    }

    public static String byteArrayToHexString(byte[] array) {
        return byteArrayToHexString(array, array.length);
    }

    public static String byteArrayToHexString(byte[] array, int len) {
        return byteArrayToHexString(array, len, 0);
    }

    public static String byteArrayToHexString(byte[] array, int len, int offset) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < len; i++) {
            String byteStr = Integer.toHexString(UnsignedUtils.uByteToInt(array[offset + i]));
            if (byteStr.length() == 1) {
                byteStr = "0" + byteStr;
            }
            b.append(byteStr);
        }
        return b.toString();
    }

    public static String byteToHexString_ZeroPad(byte b) {
        String s = Integer.toHexString(UnsignedUtils.uByteToInt(b));
        if (s.length() == 1) {
            return "0" + s;
        }
        return s;
    }

    public static String dump(byte[] bytes, int offset, int byteslen) {
        StringBuffer b = new StringBuffer();
        int len = 32;
        while (offset < byteslen) {
            int i;
            int remainder = 0;
            if (offset + len > byteslen) {
                len = byteslen - offset;
                remainder = 32 - len;
            }
            b.append(byteArrayToHexString(bytes, len, offset));
            for (i = 0; i < remainder; i++) {
                b.append("  ");
            }
            b.append(" | ");
            for (i = 0; i < len; i++) {
                byte c = bytes[offset + i];
                if (c < (byte) 32 || c > (byte) 126) {
                    b.append('.');
                } else {
                    b.append((char) c);
                }
            }
            b.append(10);
            offset += len;
        }
        return b.toString();
    }

    public static byte hexStringToByte(String s) {
        return (byte) Integer.parseInt(s, 16);
    }

    public static byte[] hexStringToByteArray(String s) {
        byte[] array = new byte[(s.length() / 2)];
        for (int i = 0; i < array.length; i++) {
            array[i] = hexStringToByte(s.substring(i * 2, (i * 2) + 2));
        }
        return array;
    }

    public static String replaceSpecialUrlChars(String raw) {
        return replaceSpecialUrlChars(raw, false);
    }

    public static String replaceSpecialUrlChars(String raw, boolean isPath) {
        if (raw == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if ((c >= 'a' && c <= 'z') || ((c >= 'A' && c <= 'Z') || ((c >= '0' && c <= '9') || c == '.' || c == '-' || c == '_' || c == '~' || (isPath && (c == '/' || c == ':' || c == '\\'))))) {
                buf.append(c);
            } else if (i > 255) {
                throw new IllegalArgumentException();
            } else {
                buf.append('%');
                buf.append(byteToHexString_ZeroPad((byte) c));
            }
        }
        return buf.toString();
    }

    public static String replaceSpecialXMLChars(String raw) {
        return replaceSpecialXMLChars_Core(raw, true);
    }

    private static String replaceSpecialXMLChars_Core(String raw, boolean apos) {
        if (raw == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '&') {
                buf.append("&amp;");
            } else if (c == '<') {
                buf.append("&lt;");
            } else if (c == '>') {
                buf.append("&gt;");
            } else if (c == '\"') {
                buf.append("&quot;");
            } else if (apos && c == '\'') {
                buf.append("&apos;");
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    public static String restoreSpecialURLChars(String cooked) {
        if (cooked == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        StringBuffer hexValueBuf = new StringBuffer();
        int state = 0;
        for (int i = 0; i < cooked.length(); i++) {
            char c = cooked.charAt(i);
            if (state != 0) {
                hexValueBuf.append(c);
                if (hexValueBuf.length() == 2) {
                    buf.append((char) Integer.parseInt(hexValueBuf.toString(), 16));
                    state = 0;
                } else if (hexValueBuf.length() == 1 && hexValueBuf.charAt(0) == '%') {
                    buf.append('%');
                    state = 0;
                }
            } else if (c == '%') {
                state = 1;
                hexValueBuf = new StringBuffer();
            } else {
                buf.append(c);
            }
        }
        if (state == 1) {
            buf.append("&" + hexValueBuf.toString());
        }
        return buf.toString();
    }

    public static String restoreSpecialXMLChars(String cooked) {
        return restoreSpecialXMLChars_Core(cooked, true);
    }

    private static String restoreSpecialXMLChars_Core(String cooked, boolean apos) {
        if (cooked == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        StringBuilder entityNameBuf = new StringBuilder();
        int state = 0;
        for (int i = 0; i < cooked.length(); i++) {
            char c = cooked.charAt(i);
            if (state == 0) {
                if (c == '&') {
                    state = 1;
                    entityNameBuf = new StringBuilder();
                } else {
                    buf.append(c);
                }
            } else if (c == ';') {
                String token = entityNameBuf.toString();
                if (token.equals("amp")) {
                    buf.append('&');
                } else if (token.equals("lt")) {
                    buf.append('<');
                } else if (token.equals("gt")) {
                    buf.append('>');
                } else if (token.equals("quot")) {
                    buf.append('\"');
                } else if ((token.equals("apos") & apos) != 0) {
                    buf.append('\'');
                } else {
                    buf.append("&" + token + ';');
                }
                state = 0;
            } else {
                entityNameBuf.append(c);
            }
        }
        if (state == 1) {
            buf.append("&" + entityNameBuf.toString());
        }
        return buf.toString();
    }
}
