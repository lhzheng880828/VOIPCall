package org.dhcp4java.examples;

import java.util.Random;
import org.dhcp4java.DHCPPacket;

public class DHCPClient {
    private static byte[] macAddress = new byte[]{(byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5};

    private DHCPClient() {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) {
        DHCPPacket discover = new DHCPPacket();
        discover.setOp((byte) 1);
        discover.setHtype((byte) 1);
        discover.setHlen((byte) 6);
        discover.setHops((byte) 0);
        discover.setXid(new Random().nextInt());
        discover.setSecs((short) 0);
        discover.setFlags((short) 0);
        discover.setChaddr(macAddress);
    }
}
