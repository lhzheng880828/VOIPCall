package org.jitsi.impl.neomedia.codec.video.h263p;

import java.util.Arrays;
import javax.media.Buffer;
import javax.media.ResourceUnavailableException;
import javax.media.format.VideoFormat;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.util.Logger;

public class DePacketizer extends AbstractCodec2 {
    private static final boolean OUTPUT_INCOMPLETE_BUFFER = true;
    private static final Logger logger = Logger.getLogger(DePacketizer.class);
    private long lastSequenceNumber;
    private final int outputPaddingSize;

    public DePacketizer() {
        super("H263+ DePacketizer", VideoFormat.class, new VideoFormat[]{new VideoFormat(Constants.H263P)});
        this.outputPaddingSize = 8;
        this.lastSequenceNumber = -1;
        this.inputFormats = new VideoFormat[]{new VideoFormat("h263-1998/rtp")};
    }

    /* access modifiers changed from: protected */
    public void doClose() {
    }

    /* access modifiers changed from: protected */
    public void doOpen() throws ResourceUnavailableException {
    }

    /* access modifiers changed from: protected */
    public int doProcess(Buffer inBuffer, Buffer outBuffer) {
        long sequenceNumber = inBuffer.getSequenceNumber();
        if (!(this.lastSequenceNumber == -1 || sequenceNumber - this.lastSequenceNumber == 1)) {
            if (logger.isTraceEnabled()) {
                logger.trace("Dropped RTP packets upto sequenceNumber " + this.lastSequenceNumber + " and continuing with sequenceNumber " + sequenceNumber);
            }
            int ret = reset(outBuffer);
            if ((ret & 4) == 0) {
                this.lastSequenceNumber = -1;
                return ret;
            }
        }
        this.lastSequenceNumber = sequenceNumber;
        byte[] in = (byte[]) inBuffer.getData();
        int inLength = inBuffer.getLength();
        int inOffset = inBuffer.getOffset();
        int outOffset = outBuffer.getOffset();
        if (inLength < 3) {
            return 1;
        }
        int i;
        boolean pBit = (in[inOffset] & 4) > 0;
        boolean vBit = (in[inOffset] & 2) > 0;
        int plen = ((in[inOffset] & 1) << 5) + ((in[inOffset + 1] & 248) >> 3);
        int i2 = (inLength - plen) - (vBit ? 1 : 0);
        if (pBit) {
            i = 0;
        } else {
            i = 2;
        }
        int dataLength = i2 - i;
        byte[] out = AbstractCodec2.validateByteArraySize(outBuffer, (outOffset + dataLength) + 8, true);
        if (pBit) {
            out[0] = (byte) 0;
            out[1] = (byte) 0;
        }
        if (vBit) {
        }
        if (plen > 0 && logger.isInfoEnabled()) {
            logger.info("Extra picture header present PLEN=" + plen);
        }
        i2 = ((vBit ? 1 : 0) + (inOffset + 2)) + plen;
        if (pBit) {
            i = 2;
        } else {
            i = 0;
        }
        int i3 = outOffset + i;
        if (pBit) {
            i = 2;
        } else {
            i = 0;
        }
        System.arraycopy(in, i2, out, i3, dataLength - i);
        padOutput(out, outOffset + dataLength);
        outBuffer.setLength(outOffset + dataLength);
        outBuffer.setSequenceNumber(sequenceNumber);
        if ((inBuffer.getFlags() & 2048) != 0) {
            outBuffer.setFlags(outBuffer.getFlags() | 2048);
            outBuffer.setOffset(0);
            return 0;
        }
        outBuffer.setOffset(outOffset + dataLength);
        return 4;
    }

    private void padOutput(byte[] out, int outOffset) {
        Arrays.fill(out, outOffset, outOffset + 8, (byte) 0);
    }

    private int reset(Buffer outBuffer) {
        if (outBuffer.getLength() > 0 && (outBuffer.getData() instanceof byte[])) {
            return 2;
        }
        outBuffer.setLength(0);
        return 4;
    }
}
