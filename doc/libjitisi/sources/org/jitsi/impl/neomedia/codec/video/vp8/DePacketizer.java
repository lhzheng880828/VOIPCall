package org.jitsi.impl.neomedia.codec.video.vp8;

import javax.media.Buffer;
import javax.media.ResourceUnavailableException;
import javax.media.format.VideoFormat;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.util.Logger;

public class DePacketizer extends AbstractCodec2 {
    private static final int BUFFER_SIZE = 100000;
    private static final boolean TRACE = false;
    private static final Logger logger = Logger.getLogger(DePacketizer.class);
    private byte[] buffer;
    private int bufferPointer;
    private boolean haveSent;
    private boolean waitForNewStart;

    static class VP8PayloadDescriptor {
        private static final byte I_BIT = Byte.MIN_VALUE;
        private static final byte K_BIT = (byte) 16;
        private static final byte L_BIT = (byte) 64;
        public static final int MAX_LENGTH = 6;
        private static final byte M_BIT = Byte.MIN_VALUE;
        private static final byte S_BIT = (byte) 16;
        private static final byte T_BIT = (byte) 32;
        private static final byte X_BIT = Byte.MIN_VALUE;

        VP8PayloadDescriptor() {
        }

        public static byte[] create(boolean startOfPartition) {
            byte b;
            byte[] pd = new byte[1];
            if (startOfPartition) {
                b = (byte) 16;
            } else {
                b = (byte) 0;
            }
            pd[0] = b;
            return pd;
        }

        public static int getSize(byte[] input, int offset) throws Exception {
            if (input.length < offset + 1) {
                throw new Exception("Invalid VP8 Payload Descriptor");
            } else if ((input[offset] & -128) == 0) {
                return 1;
            } else {
                int size = 1;
                if ((input[offset + 1] & -128) != 0) {
                    size = 1 + 1;
                    if ((input[offset + 2] & -128) != 0) {
                        size++;
                    }
                }
                if ((input[offset + 1] & 64) != 0) {
                    size++;
                }
                if ((input[offset + 1] & 48) != 0) {
                    return size + 1;
                }
                return size;
            }
        }

        public static boolean isStartOfPartition(byte[] input, int offset) {
            return (input[offset] & 16) != 0;
        }
    }

    public DePacketizer() {
        super("VP8  RTP DePacketizer", VideoFormat.class, new VideoFormat[]{new VideoFormat(Constants.VP8)});
        this.buffer = new byte[BUFFER_SIZE];
        this.bufferPointer = 0;
        this.haveSent = false;
        this.waitForNewStart = false;
        this.inputFormats = new VideoFormat[]{new VideoFormat(Constants.VP8_RTP)};
    }

    /* access modifiers changed from: protected */
    public void doClose() {
    }

    /* access modifiers changed from: protected */
    public void doOpen() throws ResourceUnavailableException {
        if (logger.isTraceEnabled()) {
            logger.trace("Opened VP8 de-packetizer");
        }
    }

    /* access modifiers changed from: protected */
    public int doProcess(Buffer inBuffer, Buffer outBuffer) {
        byte[] in = (byte[]) inBuffer.getData();
        boolean start = VP8PayloadDescriptor.isStartOfPartition(in, inBuffer.getOffset());
        if (this.waitForNewStart) {
            if (start) {
                this.waitForNewStart = false;
            } else {
                outBuffer.setDiscard(true);
                return 0;
            }
        }
        try {
            int ret;
            int pdSize = VP8PayloadDescriptor.getSize(in, inBuffer.getOffset());
            if (start && this.haveSent) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Sending a frame, size=" + this.bufferPointer);
                }
                System.arraycopy(this.buffer, 0, AbstractCodec2.validateByteArraySize(outBuffer, this.bufferPointer, false), 0, this.bufferPointer);
                outBuffer.setFormat(new VideoFormat(Constants.VP8));
                outBuffer.setLength(this.bufferPointer);
                outBuffer.setOffset(0);
                this.bufferPointer = 0;
                ret = 0;
            } else {
                ret = 4;
            }
            int len = inBuffer.getLength();
            if ((this.bufferPointer + len) - pdSize >= BUFFER_SIZE) {
                this.bufferPointer = 0;
                outBuffer.setDiscard(true);
                this.waitForNewStart = true;
                return 1;
            }
            System.arraycopy(in, inBuffer.getOffset() + pdSize, this.buffer, this.bufferPointer, len - pdSize);
            this.bufferPointer += len - pdSize;
            this.haveSent = true;
            return ret;
        } catch (Exception e) {
            outBuffer.setDiscard(true);
            return 1;
        }
    }
}
