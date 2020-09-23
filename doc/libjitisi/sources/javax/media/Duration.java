package javax.media;

public interface Duration {
    public static final Time DURATION_UNBOUNDED = new Time((long) CachingControl.LENGTH_UNKNOWN);
    public static final Time DURATION_UNKNOWN = new Time((long) Buffer.SEQUENCE_UNKNOWN);

    Time getDuration();
}
