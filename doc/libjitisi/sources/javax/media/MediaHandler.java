package javax.media;

import java.io.IOException;
import javax.media.protocol.DataSource;

public interface MediaHandler {
    void setSource(DataSource dataSource) throws IOException, IncompatibleSourceException;
}
