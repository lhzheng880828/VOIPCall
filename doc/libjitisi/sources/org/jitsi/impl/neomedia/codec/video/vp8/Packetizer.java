package org.jitsi.impl.neomedia.codec.video.vp8;

import javax.media.Buffer;
import javax.media.format.VideoFormat;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.util.Logger;

public class Packetizer extends AbstractCodec2 {
    private static final int MAX_SIZE = 1350;
    private static final Logger logger = Logger.getLogger(Packetizer.class);
    private boolean firstPacket;

    public Packetizer() {
        super("VP8 Packetizer", VideoFormat.class, new VideoFormat[]{new VideoFormat(Constants.VP8_RTP)});
        this.firstPacket = true;
        this.inputFormats = new VideoFormat[]{new VideoFormat(Constants.VP8)};
    }

    /* access modifiers changed from: protected */
    public void doClose() {
    }

    /* access modifiers changed from: protected */
    public void doOpen() {
        if (logger.isTraceEnabled()) {
            logger.trace("Opened VP8 packetizer");
        }
    }

    /* access modifiers changed from: protected */
    public int doProcess(Buffer inputBuffer, Buffer outputBuffer) {
        if (!inputBuffer.isDiscard()) {
            int inLen = inputBuffer.getLength();
            if (inLen != 0) {
                int inOff = inputBuffer.getOffset();
                int len = inLen <= MAX_SIZE ? inLen : MAX_SIZE;
                int offset = 6;
                byte[] output = AbstractCodec2.validateByteArraySize(outputBuffer, len + 6, true);
                System.arraycopy(inputBuffer.getData(), inOff, output, offset, len);
                byte[] pd = VP8PayloadDescriptor.create(this.firstPacket);
                System.arraycopy(pd, 0, output, 6 - pd.length, pd.length);
                offset = 6 - pd.length;
                outputBuffer.setFormat(new VideoFormat(Constants.VP8_RTP));
                outputBuffer.setOffset(offset);
                outputBuffer.setLength(pd.length + len);
                if (inLen <= MAX_SIZE) {
                    this.firstPacket = true;
                    return 0;
                }
                this.firstPacket = false;
                inputBuffer.setLength(inLen - 1350);
                inputBuffer.setOffset(inOff + MAX_SIZE);
                return 2;
            }
        }
        outputBuffer.setDiscard(true);
        return 0;
    }
}
