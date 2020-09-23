package javax.media.protocol;

public interface Seekable {
    boolean isRandomAccess();

    long seek(long j);

    long tell();
}
