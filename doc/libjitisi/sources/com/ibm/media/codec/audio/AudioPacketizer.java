package com.ibm.media.codec.audio;

import javax.media.Buffer;

public abstract class AudioPacketizer extends AudioCodec {
    protected byte[] history;
    protected int historyLength;
    protected int packetSize;
    protected int sample_count;

    public synchronized int process(Buffer inputBuffer, Buffer outputBuffer) {
        int i;
        int inpLength = inputBuffer.getLength();
        int outLength = this.packetSize;
        byte[] inpData = (byte[]) inputBuffer.getData();
        byte[] outData = validateByteArraySize(outputBuffer, outLength);
        if (this.historyLength + inpLength >= this.packetSize) {
            int copyFromHistory = Math.min(this.historyLength, this.packetSize);
            System.arraycopy(this.history, 0, outData, 0, copyFromHistory);
            int remainingBytes = this.packetSize - copyFromHistory;
            System.arraycopy(inpData, inputBuffer.getOffset(), outData, this.historyLength, remainingBytes);
            this.historyLength -= copyFromHistory;
            inputBuffer.setOffset(inputBuffer.getOffset() + remainingBytes);
            inputBuffer.setLength(inpLength - remainingBytes);
            updateOutput(outputBuffer, this.outputFormat, outLength, 0);
            i = 2;
        } else if (inputBuffer.isEOM()) {
            System.arraycopy(this.history, 0, outData, 0, this.historyLength);
            System.arraycopy(inpData, inputBuffer.getOffset(), outData, this.historyLength, inpLength);
            updateOutput(outputBuffer, this.outputFormat, this.historyLength + inpLength, 0);
            this.historyLength = 0;
            i = 0;
        } else {
            System.arraycopy(inpData, inputBuffer.getOffset(), this.history, this.historyLength, inpLength);
            this.historyLength += inpLength;
            i = 4;
        }
        return i;
    }

    public void reset() {
        this.historyLength = 0;
    }
}
