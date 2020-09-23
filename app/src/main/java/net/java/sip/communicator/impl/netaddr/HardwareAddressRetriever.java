package net.java.sip.communicator.impl.netaddr;

public class HardwareAddressRetriever {
    public static native byte[] getHardwareAddress(String str);

    static {
        System.loadLibrary("hwaddressretriever");
    }
}
