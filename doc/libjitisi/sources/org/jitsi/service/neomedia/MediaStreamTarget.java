package org.jitsi.service.neomedia;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class MediaStreamTarget {
    private final InetSocketAddress rtcpTarget;
    private final InetSocketAddress rtpTarget;

    public MediaStreamTarget(InetSocketAddress rtpTarget, InetSocketAddress rtcpTarget) {
        this.rtpTarget = rtpTarget;
        this.rtcpTarget = rtcpTarget;
    }

    public MediaStreamTarget(InetAddress rtpAddr, int rtpPort, InetAddress rtcpAddr, int rtcpPort) {
        this(new InetSocketAddress(rtpAddr, rtpPort), new InetSocketAddress(rtcpAddr, rtcpPort));
    }

    public static boolean addressesAreEqual(InetSocketAddress addr1, InetSocketAddress addr2) {
        if (addr1 == null) {
            return addr2 == null;
        } else {
            return addr1.equals(addr2);
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!getClass().isInstance(obj)) {
            return false;
        }
        MediaStreamTarget mediaStreamTarget = (MediaStreamTarget) obj;
        if (addressesAreEqual(getControlAddress(), mediaStreamTarget.getControlAddress()) && addressesAreEqual(getDataAddress(), mediaStreamTarget.getDataAddress())) {
            return true;
        }
        return false;
    }

    public InetSocketAddress getDataAddress() {
        return this.rtpTarget;
    }

    public InetSocketAddress getControlAddress() {
        return this.rtcpTarget;
    }

    public int hashCode() {
        int hashCode = 0;
        InetSocketAddress controlAddress = getControlAddress();
        if (controlAddress != null) {
            hashCode = 0 | controlAddress.hashCode();
        }
        InetSocketAddress dataAddress = getDataAddress();
        if (dataAddress != null) {
            return hashCode | dataAddress.hashCode();
        }
        return hashCode;
    }

    public String toString() {
        return getClass().getSimpleName() + " with dataAddress " + getDataAddress() + " and controlAddress " + getControlAddress();
    }
}
