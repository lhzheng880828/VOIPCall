package javax.media;

public interface Controller extends Clock, Duration {
    public static final Time LATENCY_UNKNOWN = new Time((long) CachingControl.LENGTH_UNKNOWN);
    public static final int Prefetched = 500;
    public static final int Prefetching = 400;
    public static final int Realized = 300;
    public static final int Realizing = 200;
    public static final int Started = 600;
    public static final int Unrealized = 100;

    void addControllerListener(ControllerListener controllerListener);

    void close();

    void deallocate();

    Control getControl(String str);

    Control[] getControls();

    Time getStartLatency();

    int getState();

    int getTargetState();

    void prefetch();

    void realize();

    void removeControllerListener(ControllerListener controllerListener);
}
