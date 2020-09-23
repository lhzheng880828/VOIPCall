package javax.media;

public interface PlugIn extends Controls {
    public static final int BUFFER_PROCESSED_FAILED = 1;
    public static final int BUFFER_PROCESSED_OK = 0;
    public static final int INPUT_BUFFER_NOT_CONSUMED = 2;
    public static final int OUTPUT_BUFFER_NOT_FILLED = 4;
    public static final int PLUGIN_TERMINATED = 8;

    void close();

    String getName();

    void open() throws ResourceUnavailableException;

    void reset();
}
