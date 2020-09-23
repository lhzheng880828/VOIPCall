package net.sf.fmj.codegen;

import net.sf.fmj.utility.FormatArgUtils;

public class CGUtils {
    private static final int MAX_BYTE_PLUS1 = 256;
    private static final int RADIX_16 = 16;

    public static String byteArrayToHexString(byte[] array) {
        return byteArrayToHexString(array, array.length);
    }

    public static String byteArrayToHexString(byte[] array, int len) {
        return byteArrayToHexString(array, len, 0);
    }

    public static String byteArrayToHexString(byte[] array, int len, int offset) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < len; i++) {
            String byteStr = Integer.toHexString(uByteToInt(array[offset + i]));
            if (byteStr.length() == 1) {
                byteStr = "0" + byteStr;
            }
            b.append(byteStr);
        }
        return b.toString();
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

    public static String replaceSpecialJavaStringChars(String raw) {
        if (raw == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '\"') {
                buf.append("\\\"");
            } else if (c == '\'') {
                buf.append("\\'");
            } else if (c == '\\') {
                buf.append("\\\\");
            } else if (c == 13) {
                buf.append("\\r");
            } else if (c == 10) {
                buf.append("\\n");
            } else if (c == 9) {
                buf.append("\\t");
            } else if (c == 12) {
                buf.append("\\f");
            } else if (c == 8) {
                buf.append("\\b");
            } else if (c == 0) {
                buf.append("\\000");
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    public static String toHexLiteral(int v) {
        return "0x" + Integer.toHexString(v);
    }

    public static String toLiteral(byte[] ba) {
        if (ba == null) {
            return "null";
        }
        StringBuffer buf = new StringBuffer();
        buf.append("new byte[] {");
        for (int i = 0; i < ba.length; i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append("(byte)" + ba[i]);
        }
        buf.append("}");
        return buf.toString();
    }

    public static String toLiteral(double v) {
        return "" + v;
    }

    public static String toLiteral(float v) {
        return "" + v + "f";
    }

    public static String toLiteral(int v) {
        return "" + v;
    }

    public static String toLiteral(int[] ba) {
        if (ba == null) {
            return "null";
        }
        StringBuffer buf = new StringBuffer();
        buf.append("new int[] {");
        boolean first = true;
        for (int b : ba) {
            if (first) {
                first = false;
            } else {
                buf.append(",");
            }
            buf.append("" + b);
        }
        buf.append("}");
        return buf.toString();
    }

    public static String toLiteral(long v) {
        return "" + v + FormatArgUtils.LITTLE_ENDIAN;
    }

    public static String toLiteral(String s) {
        if (s == null) {
            return "null";
        }
        return "\"" + replaceSpecialJavaStringChars(s) + "\"";
    }

    static String toName(Class<?> c) {
        if (c == Integer.TYPE) {
            return "int";
        }
        if (c == Boolean.TYPE) {
            return "boolean";
        }
        if (c == Short.TYPE) {
            return "short";
        }
        if (c == Byte.TYPE) {
            return "byte";
        }
        if (c == Character.TYPE) {
            return "char";
        }
        if (c == Float.TYPE) {
            return "float";
        }
        if (c == Double.TYPE) {
            return "double";
        }
        if (c == Long.TYPE) {
            return "long";
        }
        if (c == byte[].class) {
            return "byte[]";
        }
        if (c == int[].class) {
            return "int[]";
        }
        if (c == short[].class) {
            return "short[]";
        }
        if (c == double[].class) {
            return "double[]";
        }
        if (c == float[].class) {
            return "float[]";
        }
        if (c == long[].class) {
            return "long[]";
        }
        if (c == boolean[].class) {
            return "boolean[]";
        }
        if (c == char[].class) {
            return "char[]";
        }
        if (c.isArray()) {
            return "" + toName(c.getComponentType()) + "[]";
        }
        return "" + c.getName() + "";
    }

    public static String toNameDotClass(Class<?> c) {
        if (c == null) {
            return null;
        }
        return toName(c) + ".class";
    }

    public static int uByteToInt(byte b) {
        return b >= (byte) 0 ? b : b + 256;
    }
}
