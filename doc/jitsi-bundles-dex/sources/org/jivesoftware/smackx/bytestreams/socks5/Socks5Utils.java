package org.jivesoftware.smackx.bytestreams.socks5;

import java.io.DataInputStream;
import java.io.IOException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.StringUtils;

class Socks5Utils {
    Socks5Utils() {
    }

    public static String createDigest(String sessionID, String initiatorJID, String targetJID) {
        StringBuilder b = new StringBuilder();
        b.append(sessionID).append(initiatorJID).append(targetJID);
        return StringUtils.hash(b.toString());
    }

    public static byte[] receiveSocks5Message(DataInputStream in) throws IOException, XMPPException {
        byte[] header = new byte[5];
        in.readFully(header, 0, 5);
        if (header[3] != (byte) 3) {
            throw new XMPPException("Unsupported SOCKS5 address type");
        }
        int addressLength = header[4];
        byte[] response = new byte[(addressLength + 7)];
        System.arraycopy(header, 0, response, 0, header.length);
        in.readFully(response, header.length, addressLength + 2);
        return response;
    }
}
