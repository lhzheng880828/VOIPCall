package javax.swing;

public class SwingUtilities {
    public static boolean isEventDispatchThread() {
        return true;
    }

    public static void invokeLater(Runnable doRun) {
        new Thread(doRun).start();
    }
}
