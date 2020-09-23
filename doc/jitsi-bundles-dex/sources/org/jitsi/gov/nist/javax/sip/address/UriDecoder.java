package org.jitsi.gov.nist.javax.sip.address;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import org.jitsi.gov.nist.core.Separators;

public class UriDecoder {
    static Charset utf8CharSet;

    static {
        utf8CharSet = null;
        try {
            utf8CharSet = Charset.forName("UTF8");
        } catch (UnsupportedCharsetException e) {
            throw new RuntimeException("Problem in decodePath: UTF-8 charset not supported.", e);
        }
    }

    public static String decode(String uri) {
        String uriToWorkOn = uri;
        int indexOfNextPercent = uriToWorkOn.indexOf(Separators.PERCENT);
        StringBuilder decodedUri = new StringBuilder();
        while (indexOfNextPercent != -1) {
            decodedUri.append(uriToWorkOn.substring(0, indexOfNextPercent));
            if (indexOfNextPercent + 2 < uriToWorkOn.length()) {
                String hexadecimalString = uriToWorkOn.substring(indexOfNextPercent + 1, indexOfNextPercent + 3);
                try {
                    byte hexadecimalNumber = (byte) Integer.parseInt(hexadecimalString, 16);
                    decodedUri.append(utf8CharSet.decode(ByteBuffer.wrap(new byte[]{hexadecimalNumber})).toString());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Illegal hex characters in pattern %" + hexadecimalString);
                }
            }
            uriToWorkOn = uriToWorkOn.substring(indexOfNextPercent + 3);
            indexOfNextPercent = uriToWorkOn.indexOf(Separators.PERCENT);
        }
        decodedUri.append(uriToWorkOn);
        return decodedUri.toString();
    }
}
