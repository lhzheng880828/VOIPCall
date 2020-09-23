package net.sf.fmj.media;

import javax.media.Buffer;
import javax.media.Format;

public class BasicOutputConnector extends BasicConnector implements OutputConnector {
    protected InputConnector inputConnector = null;
    private boolean reset = false;

    public Format canConnectTo(InputConnector inputConnector, Format useThisFormat) {
        if (getProtocol() == inputConnector.getProtocol()) {
            return null;
        }
        throw new RuntimeException("protocols do not match:: ");
    }

    public Format connectTo(InputConnector inputConnector, Format useThisFormat) {
        Format format = canConnectTo(inputConnector, useThisFormat);
        this.inputConnector = inputConnector;
        inputConnector.setOutputConnector(this);
        this.circularBuffer = new CircularBuffer(Math.max(getSize(), inputConnector.getSize()));
        inputConnector.setCircularBuffer(this.circularBuffer);
        return null;
    }

    public Buffer getEmptyBuffer() {
        switch (this.protocol) {
            case 0:
                if (!isEmptyBufferAvailable() && this.reset) {
                    return null;
                }
                this.reset = false;
                return this.circularBuffer.getEmptyBuffer();
            case 1:
                synchronized (this.circularBuffer) {
                    this.reset = false;
                    while (!this.reset && !isEmptyBufferAvailable()) {
                        try {
                            this.circularBuffer.wait();
                        } catch (Exception e) {
                        }
                    }
                    if (this.reset) {
                        return null;
                    }
                    Buffer buffer = this.circularBuffer.getEmptyBuffer();
                    this.circularBuffer.notifyAll();
                    return buffer;
                }
            default:
                throw new RuntimeException();
        }
    }

    public InputConnector getInputConnector() {
        return this.inputConnector;
    }

    public boolean isEmptyBufferAvailable() {
        return this.circularBuffer.canWrite();
    }

    public void reset() {
        synchronized (this.circularBuffer) {
            this.reset = true;
            super.reset();
            if (this.inputConnector != null) {
                this.inputConnector.reset();
            }
            this.circularBuffer.notifyAll();
        }
    }

    public void writeReport() {
        switch (this.protocol) {
            case 0:
                synchronized (this.circularBuffer) {
                    if (this.reset) {
                        return;
                    }
                    this.circularBuffer.writeReport();
                    getInputConnector().getModule().connectorPushed(getInputConnector());
                    return;
                }
            case 1:
                synchronized (this.circularBuffer) {
                    if (this.reset) {
                        return;
                    }
                    this.circularBuffer.writeReport();
                    this.circularBuffer.notifyAll();
                    return;
                }
            default:
                throw new RuntimeException();
        }
    }
}
