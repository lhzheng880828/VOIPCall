package net.sf.fmj.media;

import javax.media.Buffer;
import javax.media.Owned;
import javax.media.control.PacketSizeControl;
import org.jitsi.android.util.java.awt.Component;

public abstract class AbstractPacketizer extends AbstractCodec {
    private static final boolean TRACE = false;
    private int bytesInPacketBuffer = 0;
    private boolean doNotSpanInputBuffers = false;
    private byte[] packetBuffer;
    /* access modifiers changed from: private */
    public int packetSize;

    private class PSC implements PacketSizeControl, Owned {
        private PSC() {
        }

        public Component getControlComponent() {
            return null;
        }

        public Object getOwner() {
            return AbstractPacketizer.this;
        }

        public int getPacketSize() {
            return AbstractPacketizer.this.packetSize;
        }

        public int setPacketSize(int numBytes) {
            AbstractPacketizer.this.setPacketSizeImpl(numBytes);
            return AbstractPacketizer.this.packetSize;
        }
    }

    public AbstractPacketizer() {
        addControl(new PSC());
    }

    /* access modifiers changed from: protected */
    public int doBuildPacketHeader(Buffer inputBuffer, byte[] packetBuffer) {
        return 0;
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer) {
        if (!checkInputBuffer(inputBuffer)) {
            return 1;
        }
        if (isEOM(inputBuffer)) {
            propagateEOM(outputBuffer);
            return 0;
        }
        int bytesToCopy;
        boolean packetComplete;
        if (this.bytesInPacketBuffer == 0) {
            this.bytesInPacketBuffer += doBuildPacketHeader(inputBuffer, this.packetBuffer);
        }
        int bytesNeededToCompletePacket = this.packetSize - this.bytesInPacketBuffer;
        int bytesAvailable = inputBuffer.getLength();
        if (bytesNeededToCompletePacket < bytesAvailable) {
            bytesToCopy = bytesNeededToCompletePacket;
        } else {
            bytesToCopy = bytesAvailable;
        }
        System.arraycopy(inputBuffer.getData(), inputBuffer.getOffset(), this.packetBuffer, this.bytesInPacketBuffer, bytesToCopy);
        this.bytesInPacketBuffer += bytesToCopy;
        inputBuffer.setOffset(inputBuffer.getOffset() + bytesToCopy);
        inputBuffer.setLength(inputBuffer.getLength() - bytesToCopy);
        if ((this.doNotSpanInputBuffers && inputBuffer.getLength() == 0) || this.bytesInPacketBuffer == this.packetSize) {
            packetComplete = true;
        } else {
            packetComplete = false;
        }
        if (!packetComplete) {
            return 4;
        }
        outputBuffer.setData(this.packetBuffer);
        outputBuffer.setOffset(0);
        outputBuffer.setLength(this.bytesInPacketBuffer);
        this.bytesInPacketBuffer = 0;
        if (inputBuffer.getLength() == 0) {
            return 0;
        }
        return 2;
    }

    /* access modifiers changed from: protected */
    public void setDoNotSpanInputBuffers(boolean doNotSpanInputBuffers) {
        this.doNotSpanInputBuffers = doNotSpanInputBuffers;
    }

    /* access modifiers changed from: protected */
    public void setPacketSize(int packetSize) {
        setPacketSizeImpl(packetSize);
    }

    /* access modifiers changed from: protected */
    public void setPacketSizeImpl(int packetSize) {
        this.packetSize = packetSize;
        this.packetBuffer = new byte[packetSize];
    }
}
