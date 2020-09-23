package net.sf.fmj.media.util;

public class MediaThread extends Thread {
    private static int audioPriority;
    private static int controlPriority;
    private static final boolean debug = false;
    private static int defaultMaxPriority;
    private static int networkPriority;
    static boolean securityPrivilege;
    private static ThreadGroup threadGroup;
    private static int videoNetworkPriority;
    private static int videoPriority;
    private String androidThreadPriority;

    static {
        securityPrivilege = true;
        controlPriority = 9;
        audioPriority = 5;
        videoPriority = 3;
        networkPriority = audioPriority + 1;
        videoNetworkPriority = networkPriority - 1;
        defaultMaxPriority = 4;
        try {
            defaultMaxPriority = Thread.currentThread().getPriority();
            defaultMaxPriority = Thread.currentThread().getThreadGroup().getMaxPriority();
        } catch (Throwable th) {
            securityPrivilege = false;
            controlPriority = defaultMaxPriority;
            audioPriority = defaultMaxPriority;
            videoPriority = defaultMaxPriority - 1;
            networkPriority = defaultMaxPriority;
            videoNetworkPriority = defaultMaxPriority;
        }
        if (securityPrivilege) {
            threadGroup = getRootThreadGroup();
        } else {
            threadGroup = null;
        }
    }

    public static int getAudioPriority() {
        return audioPriority;
    }

    public static int getControlPriority() {
        return controlPriority;
    }

    public static int getNetworkPriority() {
        return networkPriority;
    }

    private static ThreadGroup getRootThreadGroup() {
        try {
            ThreadGroup g = Thread.currentThread().getThreadGroup();
            while (g.getParent() != null) {
                g = g.getParent();
            }
            return g;
        } catch (Exception e) {
            return null;
        } catch (Error e2) {
            return null;
        }
    }

    public static int getVideoNetworkPriority() {
        return videoNetworkPriority;
    }

    public static int getVideoPriority() {
        return videoPriority;
    }

    public MediaThread() {
        this("FMJ Thread");
    }

    public MediaThread(Runnable r) {
        this(r, "FMJ Thread");
    }

    public MediaThread(Runnable r, String name) {
        super(threadGroup, r, name);
    }

    public MediaThread(String name) {
        super(threadGroup, name);
    }

    private void checkPriority(String name, int ask, boolean priv, int got) {
        if (ask != got) {
            System.out.println("MediaThread: " + name + " privilege? " + priv + "  ask pri: " + ask + " got pri:  " + got);
        }
    }

    public void run() {
        if (this.androidThreadPriority != null) {
            try {
                String osName = System.getProperty("os.name");
                if (osName != null && osName.startsWith("Linux")) {
                    String javaVmName = System.getProperty("java.vm.name");
                    if (javaVmName != null && javaVmName.equalsIgnoreCase("Dalvik")) {
                        Class<?> androidOsProcess = Class.forName("android.os.Process");
                        int androidThreadPriority = androidOsProcess.getField(this.androidThreadPriority).getInt(null);
                        androidOsProcess.getMethod("setThreadPriority", new Class[]{Integer.class}).invoke(null, new Object[]{Integer.valueOf(androidThreadPriority)});
                        int priority = 10 - Math.round((((float) (androidThreadPriority + 20)) / 40.0f) * 10.0f);
                        if (priority < 1) {
                            priority = 1;
                        } else if (priority > 10) {
                            priority = 10;
                        }
                        setPriority(priority);
                    }
                }
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath t2 = (ThreadDeath) t;
                }
            }
        }
        super.run();
    }

    private void useAndroidThreadPriority(String androidThreadPriority) {
        this.androidThreadPriority = androidThreadPriority;
    }

    public void useAudioPriority() {
        usePriority(audioPriority);
        useAndroidThreadPriority("THREAD_PRIORITY_URGENT_AUDIO");
    }

    public void useControlPriority() {
        usePriority(controlPriority);
    }

    public void useNetworkPriority() {
        usePriority(networkPriority);
    }

    private void usePriority(int priority) {
        try {
            setPriority(priority);
        } catch (Throwable th) {
        }
    }

    public void useVideoNetworkPriority() {
        usePriority(videoNetworkPriority);
    }

    public void useVideoPriority() {
        usePriority(videoPriority);
        useAndroidThreadPriority("THREAD_PRIORITY_URGENT_DISPLAY");
    }
}
