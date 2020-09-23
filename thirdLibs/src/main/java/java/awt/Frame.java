package java.awt;

public class Frame extends Window {
    public static final int ICONIFIED = 1;
    public static final int MAXIMIZED_BOTH = 6;
    public static final int MAXIMIZED_HORIZ = 2;
    public static final int MAXIMIZED_VERT = 4;
    public static final int NORMAL = 0;

    public Frame() {
        this(null);
    }

    public Frame(String title) {
        super(null);
    }

    public int getExtendedState() {
        return -1;
    }

    public void setExtendedState(int state) {
    }
}
