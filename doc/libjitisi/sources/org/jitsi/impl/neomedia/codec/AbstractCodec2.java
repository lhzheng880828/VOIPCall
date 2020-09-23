package org.jitsi.impl.neomedia.codec;

import javax.media.Buffer;
import javax.media.Effect;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.YUVFormat;
import net.sf.fmj.media.AbstractCodec;
import org.jitsi.android.util.java.awt.Dimension;

public abstract class AbstractCodec2 extends AbstractCodec {
    public static final int BUFFER_FLAG_FEC = 16777216;
    public static final int BUFFER_FLAG_PLC = 33554432;
    public static final Format[] EMPTY_FORMATS = new Format[0];
    public static final int SEQUENCE_MAX = 65535;
    public static final int SEQUENCE_MIN = 0;
    private final Class<? extends Format> formatClass;
    private final String name;
    private final Format[] supportedOutputFormats;

    public abstract void doClose();

    public abstract void doOpen() throws ResourceUnavailableException;

    public abstract int doProcess(Buffer buffer, Buffer buffer2);

    public static int calculateLostSeqNoCount(long lastSeqNo, long seqNo) {
        if (lastSeqNo == Buffer.SEQUENCE_UNKNOWN) {
            return 0;
        }
        int delta = (int) (seqNo - lastSeqNo);
        if (delta == 0) {
            return 0;
        }
        if (delta > 0) {
            return delta - 1;
        }
        return 65535 + delta;
    }

    public static long incrementSeqNo(long seqNo) {
        seqNo++;
        if (seqNo > 65535) {
            return 0;
        }
        return seqNo;
    }

    public static Format matches(Format in, Format[] outs) {
        for (Format out : outs) {
            if (in.matches(out)) {
                return out;
            }
        }
        return null;
    }

    public static YUVFormat specialize(YUVFormat yuvFormat, Class<?> dataType) {
        Class dataType2;
        int maxDataLength = -1;
        Dimension size = yuvFormat.getSize();
        int strideY = yuvFormat.getStrideY();
        if (strideY == -1 && size != null) {
            strideY = size.width;
        }
        int strideUV = yuvFormat.getStrideUV();
        if (strideUV == -1 && strideY != -1) {
            strideUV = (strideY + 1) / 2;
        }
        int offsetY = yuvFormat.getOffsetY();
        if (offsetY == -1) {
            offsetY = 0;
        }
        int offsetU = yuvFormat.getOffsetU();
        if (!(offsetU != -1 || strideY == -1 || size == null)) {
            offsetU = offsetY + (size.height * strideY);
        }
        int offsetV = yuvFormat.getOffsetV();
        if (!(offsetV != -1 || offsetU == -1 || strideUV == -1 || size == null)) {
            offsetV = offsetU + (((size.height + 1) / 2) * strideUV);
        }
        if (!(strideY == -1 || strideUV == -1 || size == null)) {
            maxDataLength = ((size.height * strideY) + ((strideUV * 2) * ((size.height + 1) / 2))) + 8;
        }
        if (dataType == null) {
            dataType2 = yuvFormat.getDataType();
        } else {
            Class<?> dataType22 = dataType;
        }
        return new YUVFormat(size, maxDataLength, dataType22, yuvFormat.getFrameRate(), 2, strideY, strideUV, offsetY, offsetU, offsetV);
    }

    protected AbstractCodec2(String name, Class<? extends Format> formatClass, Format[] supportedOutputFormats) {
        this.formatClass = formatClass;
        this.name = name;
        this.supportedOutputFormats = supportedOutputFormats;
        if (this instanceof Effect) {
            this.inputFormats = this.supportedOutputFormats;
        }
    }

    public void close() {
        if (this.opened) {
            doClose();
            this.opened = false;
            super.close();
        }
    }

    /* access modifiers changed from: protected */
    public void discardOutputBuffer(Buffer outputBuffer) {
        outputBuffer.setDiscard(true);
    }

    /* access modifiers changed from: protected */
    public Format[] getMatchingOutputFormats(Format inputFormat) {
        if (!(this instanceof Effect)) {
            return this.supportedOutputFormats == null ? EMPTY_FORMATS : (Format[]) this.supportedOutputFormats.clone();
        } else {
            return new Format[]{inputFormat};
        }
    }

    public String getName() {
        return this.name == null ? super.getName() : this.name;
    }

    public Format[] getSupportedOutputFormats(Format inputFormat) {
        if (inputFormat == null) {
            return this.supportedOutputFormats;
        }
        if (!this.formatClass.isInstance(inputFormat) || matches(inputFormat, this.inputFormats) == null) {
            return EMPTY_FORMATS;
        }
        return getMatchingOutputFormats(inputFormat);
    }

    public void open() throws ResourceUnavailableException {
        if (!this.opened) {
            doOpen();
            this.opened = true;
            super.open();
        }
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer) {
        if (!checkInputBuffer(inputBuffer)) {
            return 1;
        }
        if (isEOM(inputBuffer)) {
            propagateEOM(outputBuffer);
            return 0;
        } else if (!inputBuffer.isDiscard()) {
            return doProcess(inputBuffer, outputBuffer);
        } else {
            discardOutputBuffer(outputBuffer);
            return 0;
        }
    }

    public Format setInputFormat(Format format) {
        if (!this.formatClass.isInstance(format) || matches(format, this.inputFormats) == null) {
            return null;
        }
        return super.setInputFormat(format);
    }

    public Format setOutputFormat(Format format) {
        if (!this.formatClass.isInstance(format) || matches(format, getMatchingOutputFormats(this.inputFormat)) == null) {
            return null;
        }
        return super.setOutputFormat(format);
    }

    /* access modifiers changed from: protected */
    public void updateOutput(Buffer outputBuffer, Format format, int length, int offset) {
        outputBuffer.setFormat(format);
        outputBuffer.setLength(length);
        outputBuffer.setOffset(offset);
    }

    public static byte[] validateByteArraySize(Buffer buffer, int newSize, boolean arraycopy) {
        Object data = buffer.getData();
        byte[] newBytes;
        if (data instanceof byte[]) {
            byte[] bytes = (byte[]) data;
            if (bytes.length >= newSize) {
                return bytes;
            }
            newBytes = new byte[newSize];
            buffer.setData(newBytes);
            if (arraycopy) {
                System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
                return newBytes;
            }
            buffer.setLength(0);
            buffer.setOffset(0);
            return newBytes;
        }
        newBytes = new byte[newSize];
        buffer.setData(newBytes);
        buffer.setLength(0);
        buffer.setOffset(0);
        return newBytes;
    }

    /* access modifiers changed from: protected */
    public short[] validateShortArraySize(Buffer buffer, int newSize) {
        short[] newShorts;
        Object data = buffer.getData();
        if (data instanceof short[]) {
            short[] shorts = (short[]) data;
            if (shorts.length >= newSize) {
                return shorts;
            }
            newShorts = new short[newSize];
            System.arraycopy(shorts, 0, newShorts, 0, shorts.length);
        } else {
            newShorts = new short[newSize];
            buffer.setLength(0);
            buffer.setOffset(0);
        }
        buffer.setData(newShorts);
        return newShorts;
    }
}
