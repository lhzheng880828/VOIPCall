package net.sf.fmj.media;

import javax.media.Buffer;

public abstract class AbstractDePacketizer extends AbstractCodec {
    private static final boolean TRACE = false;

    public int process(Buffer inputBuffer, Buffer outputBuffer) {
        if (!checkInputBuffer(inputBuffer)) {
            return 1;
        }
        if (isEOM(inputBuffer)) {
            propagateEOM(outputBuffer);
            return 0;
        }
        Object temp = outputBuffer.getData();
        outputBuffer.setData(inputBuffer.getData());
        inputBuffer.setData(temp);
        outputBuffer.setLength(inputBuffer.getLength());
        outputBuffer.setFormat(this.outputFormat);
        outputBuffer.setOffset(inputBuffer.getOffset());
        return 0;
    }
}
