package javax.media.rtp;

import java.io.IOException;
import javax.media.rtp.rtcp.SourceDescription;

public interface SendStream extends RTPStream {
    void close();

    TransmissionStats getSourceTransmissionStats();

    int setBitRate(int i);

    void setSourceDescription(SourceDescription[] sourceDescriptionArr);

    void start() throws IOException;

    void stop() throws IOException;
}
