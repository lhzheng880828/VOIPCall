package javax.media;

public interface Clock {
    public static final Time RESET = new Time((long) CachingControl.LENGTH_UNKNOWN);

    long getMediaNanoseconds();

    Time getMediaTime();

    float getRate();

    Time getStopTime();

    Time getSyncTime();

    TimeBase getTimeBase();

    Time mapToTimeBase(Time time) throws ClockStoppedException;

    void setMediaTime(Time time);

    float setRate(float f);

    void setStopTime(Time time);

    void setTimeBase(TimeBase timeBase) throws IncompatibleTimeBaseException;

    void stop();

    void syncStart(Time time);
}
