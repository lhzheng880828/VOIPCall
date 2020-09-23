package javax.media;

public interface CachedStream {
    void abortRead();

    boolean getEnabledBuffering();

    void setEnabledBuffering(boolean z);

    boolean willReadBytesBlock(int i);

    boolean willReadBytesBlock(long j, int i);
}
