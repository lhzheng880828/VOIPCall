package net.sf.fmj.media;

import javax.media.Format;

public interface Connector {
    public static final int ProtocolPush = 0;
    public static final int ProtocolSafe = 1;

    Object getCircularBuffer();

    Format getFormat();

    Module getModule();

    String getName();

    int getProtocol();

    int getSize();

    void reset();

    void setCircularBuffer(Object obj);

    void setFormat(Format format);

    void setModule(Module module);

    void setName(String str);

    void setProtocol(int i);

    void setSize(int i);
}
