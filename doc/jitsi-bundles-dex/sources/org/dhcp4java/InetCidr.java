package org.dhcp4java;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InetCidr implements Serializable, Comparable<InetCidr> {
    static final /* synthetic */ boolean $assertionsDisabled = (!InetCidr.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    private static final String[] CIDR_MASKS = new String[]{"128.0.0.0", "192.0.0.0", "224.0.0.0", "240.0.0.0", "248.0.0.0", "252.0.0.0", "254.0.0.0", "255.0.0.0", "255.128.0.0", "255.192.0.0", "255.224.0.0", "255.240.0.0", "255.248.0.0", "255.252.0.0", "255.254.0.0", "255.255.0.0", "255.255.128.0", "255.255.192.0", "255.255.224.0", "255.255.240.0", "255.255.248.0", "255.255.252.0", "255.255.254.0", "255.255.255.0", "255.255.255.128", "255.255.255.192", "255.255.255.224", "255.255.255.240", "255.255.255.248", "255.255.255.252", "255.255.255.254", "255.255.255.255"};
    private static final Map<InetAddress, Integer> gCidr = new HashMap(48);
    private static final long[] gCidrMask = new long[33];
    private static final long serialVersionUID = 1;
    private final int addr;
    private final int mask;

    static {
        try {
            gCidrMask[0] = 0;
            for (int i = 0; i < CIDR_MASKS.length; i++) {
                InetAddress mask = InetAddress.getByName(CIDR_MASKS[i]);
                gCidrMask[i + 1] = Util.inetAddress2Long(mask);
                gCidr.put(mask, Integer.valueOf(i + 1));
            }
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Unable to initialize CIDR");
        }
    }

    public InetCidr(InetAddress addr, int mask) {
        if (addr == null) {
            throw new NullPointerException("addr is null");
        } else if (!(addr instanceof Inet4Address)) {
            throw new IllegalArgumentException("Only IPv4 addresses supported");
        } else if (mask < 1 || mask > 32) {
            throw new IllegalArgumentException("Bad mask:" + mask + " must be between 1 and 32");
        } else {
            this.addr = Util.inetAddress2Int(addr) & ((int) gCidrMask[mask]);
            this.mask = mask;
        }
    }

    public InetCidr(InetAddress addr, InetAddress netMask) {
        if (addr == null || netMask == null) {
            throw new NullPointerException();
        } else if ((addr instanceof Inet4Address) && (netMask instanceof Inet4Address)) {
            Integer intMask = (Integer) gCidr.get(netMask);
            if (intMask == null) {
                throw new IllegalArgumentException("netmask: " + netMask + " is not a valid mask");
            }
            this.addr = Util.inetAddress2Int(addr) & ((int) gCidrMask[intMask.intValue()]);
            this.mask = intMask.intValue();
        } else {
            throw new IllegalArgumentException("Only IPv4 addresses supported");
        }
    }

    public String toString() {
        return Util.int2InetAddress(this.addr).getHostAddress() + '/' + this.mask;
    }

    public InetAddress getAddr() {
        return Util.int2InetAddress(this.addr);
    }

    public long getAddrLong() {
        return ((long) this.addr) & 4294967295L;
    }

    public int getMask() {
        return this.mask;
    }

    public final long toLong() {
        return (((long) this.addr) & 4294967295L) + (((long) this.mask) << 32);
    }

    public static final InetCidr fromLong(long l) {
        if (l < 0) {
            throw new IllegalArgumentException("l must not be negative: " + l);
        }
        return new InetCidr(Util.long2InetAddress(l & 4294967295L), (int) (l >> 32));
    }

    public int hashCode() {
        return this.addr ^ this.mask;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof InetCidr)) {
            return $assertionsDisabled;
        }
        InetCidr cidr = (InetCidr) obj;
        if (this.addr == cidr.addr && this.mask == cidr.mask) {
            return true;
        }
        return $assertionsDisabled;
    }

    public static InetCidr[] addr2Cidr(InetAddress addr) {
        if (addr == null) {
            throw new IllegalArgumentException("addr must not be null");
        } else if (addr instanceof Inet4Address) {
            int addrInt = Util.inetAddress2Int(addr);
            InetCidr[] cidrs = new InetCidr[32];
            for (int i = cidrs.length; i >= 1; i--) {
                cidrs[32 - i] = new InetCidr(Util.int2InetAddress(((int) gCidrMask[i]) & addrInt), i);
            }
            return cidrs;
        } else {
            throw new IllegalArgumentException("Only IPv4 addresses supported");
        }
    }

    public int compareTo(InetCidr o) {
        if (o == null) {
            throw new NullPointerException();
        } else if (equals(o)) {
            return 0;
        } else {
            if (int2UnsignedLong(this.addr) < int2UnsignedLong(o.addr)) {
                return -1;
            }
            if (int2UnsignedLong(this.addr) > int2UnsignedLong(o.addr)) {
                return 1;
            }
            if (this.mask < o.mask) {
                return -1;
            }
            if (this.mask > o.mask) {
                return 1;
            }
            return 0;
        }
    }

    private static final long int2UnsignedLong(int i) {
        return ((long) i) & 4294967295L;
    }

    public static boolean isSorted(List<InetCidr> list) {
        if (list == null) {
            return true;
        }
        InetCidr pivot = null;
        for (InetCidr cidr : list) {
            if (cidr == null) {
                throw new NullPointerException();
            } else if (pivot == null) {
                pivot = cidr;
            } else if (pivot.compareTo(cidr) >= 0) {
                return $assertionsDisabled;
            } else {
                pivot = cidr;
            }
        }
        return true;
    }

    public static void checkNoOverlap(List<InetCidr> list) {
        if (list != null) {
            if ($assertionsDisabled || isSorted(list)) {
                InetCidr prev = null;
                long pivotEnd = -1;
                for (InetCidr cidr : list) {
                    if (cidr == null) {
                        throw new NullPointerException();
                    } else if (prev == null || cidr.getAddrLong() > pivotEnd) {
                        pivotEnd = cidr.getAddrLong() + (gCidrMask[cidr.getMask()] ^ 4294967295L);
                        prev = cidr;
                    } else {
                        throw new IllegalStateException("Overlapping cidr: " + prev + ", " + cidr);
                    }
                }
                return;
            }
            throw new AssertionError();
        }
    }
}
