package javax.media;

public interface ExtendedCachingControl extends CachingControl {
    void addDownloadProgressListener(DownloadProgressListener downloadProgressListener, int i);

    Time getBufferSize();

    long getEndOffset();

    long getStartOffset();

    void pauseDownload();

    void removeDownloadProgressListener(DownloadProgressListener downloadProgressListener);

    void resumeDownload();

    void setBufferSize(Time time);
}
