package org.jitsi.impl.neomedia.transform.srtp;

import com.lti.utils.UnsignedUtils;
import org.jitsi.bouncycastle.crypto.BlockCipher;

public class SRTPCipherCTR {
    private static final int BLKLEN = 16;
    private static final int MAX_BUFFER_LENGTH = 10240;
    private final byte[] cipherInBlock = new byte[16];
    private byte[] streamBuf = new byte[1024];
    private final byte[] tmpCipherBlock = new byte[16];

    public void process(BlockCipher cipher, byte[] data, int off, int len, byte[] iv) {
        if (off + len <= data.length) {
            byte[] cipherStream;
            if (len > this.streamBuf.length) {
                cipherStream = new byte[len];
                if (cipherStream.length <= MAX_BUFFER_LENGTH) {
                    this.streamBuf = cipherStream;
                }
            } else {
                cipherStream = this.streamBuf;
            }
            getCipherStream(cipher, cipherStream, len, iv);
            for (int i = 0; i < len; i++) {
                int i2 = i + off;
                data[i2] = (byte) (data[i2] ^ cipherStream[i]);
            }
        }
    }

    public void getCipherStream(BlockCipher aesCipher, byte[] out, int length, byte[] iv) {
        System.arraycopy(iv, 0, this.cipherInBlock, 0, 14);
        int ctr = 0;
        while (ctr < length / 16) {
            this.cipherInBlock[14] = (byte) ((ctr & 65280) >> 8);
            this.cipherInBlock[15] = (byte) (ctr & UnsignedUtils.MAX_UBYTE);
            aesCipher.processBlock(this.cipherInBlock, 0, out, ctr * 16);
            ctr++;
        }
        this.cipherInBlock[14] = (byte) ((ctr & 65280) >> 8);
        this.cipherInBlock[15] = (byte) (ctr & UnsignedUtils.MAX_UBYTE);
        aesCipher.processBlock(this.cipherInBlock, 0, this.tmpCipherBlock, 0);
        System.arraycopy(this.tmpCipherBlock, 0, out, ctr * 16, length % 16);
    }
}
