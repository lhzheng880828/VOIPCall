package net.sf.fmj.media;

import javax.media.Controls;
import javax.media.Format;

public interface Module extends Controls {
    void connectorPushed(InputConnector inputConnector);

    InputConnector getInputConnector(String str);

    String[] getInputConnectorNames();

    String getName();

    OutputConnector getOutputConnector(String str);

    String[] getOutputConnectorNames();

    boolean isInterrupted();

    void registerInputConnector(String str, InputConnector inputConnector);

    void registerOutputConnector(String str, OutputConnector outputConnector);

    void reset();

    void setFormat(Connector connector, Format format);

    void setModuleListener(ModuleListener moduleListener);

    void setName(String str);
}
