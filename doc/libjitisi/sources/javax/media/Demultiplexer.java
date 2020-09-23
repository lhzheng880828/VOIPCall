package javax.media;

import java.io.IOException;
import javax.media.protocol.ContentDescriptor;

public interface Demultiplexer extends PlugIn, MediaHandler, Duration {
    Time getDuration();

    Time getMediaTime();

    ContentDescriptor[] getSupportedInputContentDescriptors();

    Track[] getTracks() throws IOException, BadHeaderException;

    boolean isPositionable();

    boolean isRandomAccess();

    Time setPosition(Time time, int i);

    void start() throws IOException;

    void stop();
}
