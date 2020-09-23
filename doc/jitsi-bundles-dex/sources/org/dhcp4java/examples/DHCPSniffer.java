package org.dhcp4java.examples;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import org.dhcp4java.DHCPPacket;

public class DHCPSniffer {
    private DHCPSniffer() {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(67);
            while (true) {
                DatagramPacket pac = new DatagramPacket(new byte[1500], 1500);
                socket.receive(pac);
                System.out.println(DHCPPacket.getPacket(pac).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
