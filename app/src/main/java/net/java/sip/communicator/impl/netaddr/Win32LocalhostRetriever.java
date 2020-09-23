package net.java.sip.communicator.impl.netaddr;

public class Win32LocalhostRetriever {
    public static native byte[] getSourceForDestination(byte[] bArr);

    static {
        System.loadLibrary("LocalhostRetriever");
    }
}
