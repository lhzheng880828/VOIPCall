package net.sf.fmj.media;

import javax.media.Format;

public abstract class BasicConnector implements Connector {
    protected CircularBuffer circularBuffer = null;
    protected Format format = null;
    protected int minSize = 1;
    protected Module module = null;
    protected String name = null;
    protected int protocol = 0;

    public Object getCircularBuffer() {
        return this.circularBuffer;
    }

    public Format getFormat() {
        return this.format;
    }

    public Module getModule() {
        return this.module;
    }

    public String getName() {
        return this.name;
    }

    public int getProtocol() {
        return this.protocol;
    }

    public int getSize() {
        return this.minSize;
    }

    public void print() {
        this.circularBuffer.print();
    }

    public void reset() {
        this.circularBuffer.reset();
    }

    public void setCircularBuffer(Object cicularBuffer) {
        this.circularBuffer = (CircularBuffer) cicularBuffer;
    }

    public void setFormat(Format format) {
        this.module.setFormat(this, format);
        this.format = format;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public void setSize(int numOfBufferObjects) {
        this.minSize = numOfBufferObjects;
    }
}
