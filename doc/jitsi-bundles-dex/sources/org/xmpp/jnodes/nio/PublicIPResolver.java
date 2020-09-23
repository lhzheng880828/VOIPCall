package org.xmpp.jnodes.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Random;
import org.jitsi.gov.nist.core.Separators;

public class PublicIPResolver {
    static final byte BINDING_REQUEST_ID = (byte) 1;
    static final byte[] CHANGE_REQUEST_NO_CHANGE = new byte[]{(byte) 0, (byte) 3, (byte) 0, (byte) 4, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
    static final int MAPPED_ADDRESS = 1;
    static final Random r = new Random(System.nanoTime());

    public static class Header {
        final InetSocketAddress address;
        final int length;
        final int type;

        public Header(InetSocketAddress address, int type, int length) {
            this.address = address;
            this.type = type;
            this.length = length;
        }

        public int getType() {
            return this.type;
        }

        public InetSocketAddress getAddress() {
            return this.address;
        }

        public int getLength() {
            return this.length;
        }
    }

    private static byte[] getHeader(int contentLenght) {
        byte[] header = new byte[20];
        header[0] = (byte) 0;
        header[1] = (byte) 1;
        header[2] = (byte) 0;
        header[3] = (byte) contentLenght;
        header[4] = (byte) r.nextInt(9);
        header[5] = (byte) r.nextInt(8);
        header[6] = (byte) r.nextInt(7);
        header[7] = (byte) r.nextInt(6);
        return header;
    }

    public static ByteBuffer createSTUNChangeRequest() {
        byte[] header = getHeader(CHANGE_REQUEST_NO_CHANGE.length);
        byte[] data = new byte[(header.length + CHANGE_REQUEST_NO_CHANGE.length)];
        System.arraycopy(header, 0, data, 0, header.length);
        System.arraycopy(CHANGE_REQUEST_NO_CHANGE, 0, data, header.length, CHANGE_REQUEST_NO_CHANGE.length);
        return ByteBuffer.wrap(data);
    }

    public static Header parseResponse(byte[] data) {
        byte[] lengthArray = new byte[2];
        System.arraycopy(data, 2, lengthArray, 0, 2);
        int length = unsignedShortToInt(lengthArray);
        int offset = 20;
        while (length > 0) {
            byte[] cuttedData = new byte[length];
            System.arraycopy(data, offset, cuttedData, 0, length);
            Header h = parseHeader(cuttedData);
            if (h.getType() == 1) {
                return h;
            }
            length -= h.getLength();
            offset += h.getLength();
        }
        return null;
    }

    private static Header parseHeader(byte[] data) {
        byte[] typeArray = new byte[2];
        System.arraycopy(data, 0, typeArray, 0, 2);
        int type = unsignedShortToInt(typeArray);
        byte[] lengthArray = new byte[2];
        System.arraycopy(data, 2, lengthArray, 0, 2);
        int lengthValue = unsignedShortToInt(lengthArray);
        Object valueArray = new byte[lengthValue];
        System.arraycopy(data, 4, valueArray, 0, lengthValue);
        if (data.length < 8 || unsignedByteToInt(valueArray[1]) != 1) {
            return new Header(null, -1, lengthValue + 4);
        }
        byte[] portArray = new byte[2];
        System.arraycopy(valueArray, 2, portArray, 0, 2);
        int port = unsignedShortToInt(portArray);
        int firstOctet = unsignedByteToInt(valueArray[4]);
        int secondOctet = unsignedByteToInt(valueArray[5]);
        int thirdOctet = unsignedByteToInt(valueArray[6]);
        return new Header(new InetSocketAddress(firstOctet + Separators.DOT + secondOctet + Separators.DOT + thirdOctet + Separators.DOT + unsignedByteToInt(valueArray[7]), port), type, lengthValue + 4);
    }

    public static int unsignedShortToInt(byte[] b) {
        return ((b[0] & 255) << 8) + (b[1] & 255);
    }

    public static int unsignedByteToInt(byte b) {
        return b & 255;
    }

    public static InetSocketAddress getPublicAddress(String stunServer, int port) {
        int lport = 10002;
        int t = 0;
        while (t < 3) {
            try {
                String localIP = (System.getProperty("os.name") == null || System.getProperty("os.name").toLowerCase().indexOf("win") <= -1) ? "0.0.0.0" : LocalIPResolver.getLocalIP();
                return getPublicAddress(SelDatagramChannel.open(null, new InetSocketAddress(localIP, lport)), stunServer, port);
            } catch (IOException e) {
                lport += r.nextInt(10) + 1;
                t++;
            }
        }
        return null;
    }

    public static InetSocketAddress getPublicAddress(SelDatagramChannel channel, String stunServer, int port) {
        final Header[] h = new Header[1];
        try {
            channel.setDatagramListener(new DatagramListener() {
                public void datagramReceived(SelDatagramChannel channel, ByteBuffer buffer, SocketAddress address) {
                    byte[] b = new byte[buffer.position()];
                    buffer.rewind();
                    buffer.get(b, 0, b.length);
                    h[0] = PublicIPResolver.parseResponse(b);
                }
            });
            channel.send(createSTUNChangeRequest(), new InetSocketAddress(stunServer, port));
            Thread.sleep(100);
            for (int i = 0; i < 5; i++) {
                Thread.sleep(100);
                if (h[0] != null) {
                    return h[0].getAddress();
                }
                if (i % 2 == 0) {
                    channel.send(createSTUNChangeRequest(), new InetSocketAddress(stunServer, port));
                }
            }
            return null;
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }
}
