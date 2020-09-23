package org.xmpp.jnodes.nio;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class LocalIPResolver {
    private static String overrideIp;

    public static String getLocalIP() {
        if (overrideIp != null && overrideIp.length() >= 7) {
            return overrideIp;
        }
        try {
            Enumeration iaddresses;
            InetAddress iaddress;
            Enumeration ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                iaddresses = ((NetworkInterface) ifaces.nextElement()).getInetAddresses();
                while (iaddresses.hasMoreElements()) {
                    iaddress = (InetAddress) iaddresses.nextElement();
                    if (!iaddress.isLoopbackAddress() && !iaddress.isLinkLocalAddress() && !iaddress.isSiteLocalAddress()) {
                        return iaddress.getHostAddress() != null ? iaddress.getHostAddress() : iaddress.getHostName();
                    }
                }
            }
            ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                iaddresses = ((NetworkInterface) ifaces.nextElement()).getInetAddresses();
                while (iaddresses.hasMoreElements()) {
                    iaddress = (InetAddress) iaddresses.nextElement();
                    if (!iaddress.isLoopbackAddress() && !iaddress.isLinkLocalAddress()) {
                        return iaddress.getHostAddress() != null ? iaddress.getHostAddress() : iaddress.getHostName();
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress() != null ? InetAddress.getLocalHost().getHostAddress() : InetAddress.getLocalHost().getHostName();
        } catch (SocketException e) {
            e.printStackTrace();
            return "127.0.0.1";
        } catch (UnknownHostException e2) {
            e2.printStackTrace();
            return "127.0.0.1";
        }
    }

    public static String getOverrideIp() {
        return overrideIp;
    }

    public static void setOverrideIp(String overrideIp) {
        overrideIp = overrideIp;
    }
}
