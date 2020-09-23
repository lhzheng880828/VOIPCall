package javax.media;

import java.io.IOException;
import javax.media.datasink.DataSinkListener;

public interface DataSink extends MediaHandler, Controls {
    void addDataSinkListener(DataSinkListener dataSinkListener);

    void close();

    String getContentType();

    MediaLocator getOutputLocator();

    void open() throws IOException, SecurityException;

    void removeDataSinkListener(DataSinkListener dataSinkListener);

    void setOutputLocator(MediaLocator mediaLocator);

    void start() throws IOException;

    void stop() throws IOException;
}
