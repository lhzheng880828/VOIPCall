package javax.media.protocol;

public interface SourceStream extends Controls {
    public static final long LENGTH_UNKNOWN = -1;

    boolean endOfStream();

    ContentDescriptor getContentDescriptor();

    long getContentLength();
}
