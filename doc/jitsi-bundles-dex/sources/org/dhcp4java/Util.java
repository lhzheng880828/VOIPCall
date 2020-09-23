package org.dhcp4java;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class Util {
    private Util() {
        throw new UnsupportedOperationException();
    }

    public static final InetAddress int2InetAddress(int val) {
        try {
            return InetAddress.getByAddress(new byte[]{(byte) ((-16777216 & val) >>> 24), (byte) ((16711680 & val) >>> 16), (byte) ((65280 & val) >>> 8), (byte) (val & 255)});
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public static final InetAddress long2InetAddress(long val) {
        if (val < 0 || val > 4294967295L) {
        }
        return int2InetAddress((int) val);
    }

    public static final int inetAddress2Int(InetAddress addr) {
        if (addr instanceof Inet4Address) {
            byte[] addrBytes = addr.getAddress();
            return ((((addrBytes[0] & 255) << 24) | ((addrBytes[1] & 255) << 16)) | ((addrBytes[2] & 255) << 8)) | (addrBytes[3] & 255);
        }
        throw new IllegalArgumentException("Only IPv4 supported");
    }

    public static final long inetAddress2Long(InetAddress addr) {
        return ((long) inetAddress2Int(addr)) & 4294967295L;
    }
}
