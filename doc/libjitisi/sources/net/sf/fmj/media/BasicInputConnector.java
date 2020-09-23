package net.sf.fmj.media;

import javax.media.Buffer;

public class BasicInputConnector extends BasicConnector implements InputConnector {
    protected OutputConnector outputConnector = null;
    private boolean reset = false;

    public OutputConnector getOutputConnector() {
        return this.outputConnector;
    }

    public Buffer getValidBuffer() {
        Buffer buffer = null;
        switch (this.protocol) {
            case 0:
                synchronized (this.circularBuffer) {
                    if (isValidBufferAvailable() || !this.reset) {
                        this.reset = false;
                        buffer = this.circularBuffer.read();
                        break;
                    }
                }
                break;
            case 1:
                synchronized (this.circularBuffer) {
                    this.reset = false;
                    while (!this.reset && !isValidBufferAvailable()) {
                        try {
                            this.circularBuffer.wait();
                        } catch (Exception e) {
                        }
                    }
                    if (this.reset) {
                        break;
                    }
                    buffer = this.circularBuffer.read();
                    this.circularBuffer.notifyAll();
                }
                break;
            default:
                throw new RuntimeException();
        }
        return buffer;
    }

    public boolean isValidBufferAvailable() {
        return this.circularBuffer.canRead();
    }

    public void readReport() {
        switch (this.protocol) {
            case 0:
            case 1:
                synchronized (this.circularBuffer) {
                    if (this.reset) {
                        return;
                    }
                    this.circularBuffer.readReport();
                    this.circularBuffer.notifyAll();
                    return;
                }
            default:
                throw new RuntimeException();
        }
    }

    public void reset() {
        synchronized (this.circularBuffer) {
            this.reset = true;
            super.reset();
            this.circularBuffer.notifyAll();
        }
    }

    public void setOutputConnector(OutputConnector outputConnector) {
        this.outputConnector = outputConnector;
    }
}
