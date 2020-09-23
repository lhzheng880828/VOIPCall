package net.sf.fmj.media;

import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import org.jitsi.android.util.java.awt.Dimension;

public abstract class BasicCodec extends BasicPlugIn implements Codec {
    private static final boolean DEBUG = true;
    protected Format inputFormat;
    protected Format[] inputFormats = new Format[0];
    protected boolean opened = false;
    protected Format outputFormat;
    protected Format[] outputFormats = new Format[0];
    protected boolean pendingEOM = false;

    /* access modifiers changed from: protected */
    public int checkEOM(Buffer inputBuffer, Buffer outputBuffer) {
        processAtEOM(inputBuffer, outputBuffer);
        if (outputBuffer.getLength() > 0) {
            this.pendingEOM = true;
            return 2;
        }
        propagateEOM(outputBuffer);
        return 0;
    }

    /* access modifiers changed from: protected */
    public boolean checkFormat(Format format) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkInputBuffer(Buffer inputBuffer) {
        boolean fError;
        if (isEOM(inputBuffer) || !(inputBuffer == null || inputBuffer.getFormat() == null || !checkFormat(inputBuffer.getFormat()))) {
            fError = false;
        } else {
            fError = true;
        }
        if (fError) {
            System.out.println(getClass().getName() + " : [error] checkInputBuffer");
        }
        return !fError;
    }

    public void close() {
        this.opened = false;
    }

    /* access modifiers changed from: protected */
    public int getArrayElementSize(Class<?> type) {
        if (type == Format.intArray) {
            return 4;
        }
        if (type == Format.shortArray) {
            return 2;
        }
        if (type == Format.byteArray) {
            return 1;
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public Format getInputFormat() {
        return this.inputFormat;
    }

    /* access modifiers changed from: protected */
    public Format getOutputFormat() {
        return this.outputFormat;
    }

    public Format[] getSupportedInputFormats() {
        return this.inputFormats;
    }

    /* access modifiers changed from: protected */
    public boolean isEOM(Buffer inputBuffer) {
        return inputBuffer.isEOM();
    }

    public void open() throws ResourceUnavailableException {
        this.opened = true;
    }

    /* access modifiers changed from: protected */
    public int processAtEOM(Buffer inputBuffer, Buffer outputBuffer) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void propagateEOM(Buffer outputBuffer) {
        updateOutput(outputBuffer, getOutputFormat(), 0, 0);
        outputBuffer.setEOM(true);
    }

    public void reset() {
    }

    public Format setInputFormat(Format input) {
        this.inputFormat = input;
        return input;
    }

    public Format setOutputFormat(Format output) {
        this.outputFormat = output;
        return output;
    }

    /* access modifiers changed from: protected */
    public void updateOutput(Buffer outputBuffer, Format format, int length, int offset) {
        outputBuffer.setFormat(format);
        outputBuffer.setLength(length);
        outputBuffer.setOffset(offset);
    }

    /* access modifiers changed from: protected */
    public RGBFormat updateRGBFormat(VideoFormat newFormat, RGBFormat outputFormat) {
        Dimension size = newFormat.getSize();
        RGBFormat oldFormat = outputFormat;
        int lineStride = size.width * oldFormat.getPixelStride();
        return new RGBFormat(size, size.height * lineStride, oldFormat.getDataType(), newFormat.getFrameRate(), oldFormat.getBitsPerPixel(), oldFormat.getRedMask(), oldFormat.getGreenMask(), oldFormat.getBlueMask(), oldFormat.getPixelStride(), lineStride, oldFormat.getFlipped(), oldFormat.getEndian());
    }
}
