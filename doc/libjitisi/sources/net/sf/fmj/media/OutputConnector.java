package net.sf.fmj.media;

import javax.media.Buffer;
import javax.media.Format;

public interface OutputConnector extends Connector {
    Format canConnectTo(InputConnector inputConnector, Format format);

    Format connectTo(InputConnector inputConnector, Format format);

    Buffer getEmptyBuffer();

    InputConnector getInputConnector();

    boolean isEmptyBufferAvailable();

    void writeReport();
}
