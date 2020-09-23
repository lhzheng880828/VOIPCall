package net.sf.fmj.utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

public class IOUtils {
    private static final int BUFFER_SIZE = 2048;

    public static void copyFile(File fileIn, File fileOut) throws IOException {
        InputStream is = new FileInputStream(fileIn);
        OutputStream os = new FileOutputStream(fileOut);
        copyStream(is, os);
        is.close();
        os.close();
    }

    public static void copyFile(String fileIn, String fileOut) throws IOException {
        copyFile(new File(fileIn), new File(fileOut));
    }

    public static void copyStream(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[2048];
        while (true) {
            int len = is.read(buf);
            if (len != -1) {
                os.write(buf, 0, len);
            } else {
                return;
            }
        }
    }

    public static byte[] readAll(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copyStream(is, baos);
        is.close();
        return baos.toByteArray();
    }

    public static String readAll(Reader reader) throws IOException {
        StringBuilder b = new StringBuilder();
        while (true) {
            int c = reader.read();
            if (c == -1) {
                return b.toString();
            }
            b.append((char) c);
        }
    }

    public static String readAllToString(InputStream is) throws IOException {
        return new String(readAll(is));
    }

    public static String readAllToString(InputStream is, String encoding) throws IOException {
        return new String(readAll(is), encoding);
    }

    public static void readAllToStringBuffer(InputStream is, String encoding, StringBuffer b) throws IOException {
        b.append(readAllToString(is, encoding));
    }

    public static void readAllToStringBuffer(InputStream is, StringBuffer b) throws IOException {
        b.append(readAllToString(is));
    }

    public static void readAllToStringBuilder(InputStream is, String encoding, StringBuilder b) throws IOException {
        b.append(readAllToString(is, encoding));
    }

    public static void readAllToStringBuilder(InputStream is, StringBuilder b) throws IOException {
        b.append(readAllToString(is));
    }

    public static void writeStringToFile(String value, String path) throws IOException {
        FileOutputStream fos = new FileOutputStream(path);
        fos.write(value.getBytes());
        fos.close();
    }

    private IOUtils() {
    }
}
