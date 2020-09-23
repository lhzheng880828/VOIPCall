package net.java.sip.communicator.impl.protocol.sip.xcap.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {
    private static int BUFFER_SIZE = 1024;

    private StreamUtils() {
    }

    public static byte[] read(InputStream source) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("Input parameter can't be null");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                int bytesRead = source.read(buffer);
                if (bytesRead <= -1) {
                    break;
                }
                out.write(buffer, 0, bytesRead);
            }
            byte[] toByteArray = out.toByteArray();
            return toByteArray;
        } finally {
            source.close();
            out.close();
        }
    }
}
