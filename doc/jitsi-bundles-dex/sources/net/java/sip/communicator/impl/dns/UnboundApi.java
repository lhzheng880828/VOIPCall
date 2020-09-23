package net.java.sip.communicator.impl.dns;

public class UnboundApi {
    private static boolean isAvailable;
    private static final Object syncRoot = new Object();

    public interface UnboundCallback {
        void UnboundResolveCallback(Object obj, int i, UnboundResult unboundResult);
    }

    public static native void addTrustAnchor(long j, String str);

    public static native void cancelAsync(long j, int i) throws UnboundException;

    public static native long createContext();

    public static native void deleteContext(long j);

    public static native String errorCodeToString(int i);

    public static native void processAsync(long j) throws UnboundException;

    public static native UnboundResult resolve(long j, String str, int i, int i2) throws UnboundException;

    public static native int resolveAsync(long j, String str, int i, int i2, Object obj, UnboundCallback unboundCallback) throws UnboundException;

    public static native void setDebugLevel(long j, int i);

    public static native void setForwarder(long j, String str);

    static {
        tryLoadUnbound();
    }

    public static void tryLoadUnbound() {
        synchronized (syncRoot) {
            try {
                System.loadLibrary("junbound");
                isAvailable = true;
            } catch (UnsatisfiedLinkError e) {
                isAvailable = false;
            }
        }
    }

    public static boolean isAvailable() {
        return isAvailable;
    }
}
