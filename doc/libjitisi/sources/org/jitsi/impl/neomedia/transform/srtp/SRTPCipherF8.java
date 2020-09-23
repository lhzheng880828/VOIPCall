package org.jitsi.impl.neomedia.transform.srtp;

import java.util.Arrays;
import org.jitsi.bouncycastle.crypto.BlockCipher;
import org.jitsi.bouncycastle.crypto.params.KeyParameter;

public class SRTPCipherF8 {
    private static final int BLKLEN = 16;

    class F8Context {
        long J;
        public byte[] S;
        public byte[] ivAccent;

        F8Context() {
        }
    }

    public static void deriveForIV(BlockCipher f8Cipher, byte[] key, byte[] salt) {
        int i;
        byte[] saltMask = new byte[key.length];
        byte[] maskedKey = new byte[key.length];
        System.arraycopy(salt, 0, saltMask, 0, salt.length);
        for (i = salt.length; i < saltMask.length; i++) {
            saltMask[i] = (byte) 85;
        }
        for (i = 0; i < key.length; i++) {
            maskedKey[i] = (byte) (key[i] ^ saltMask[i]);
        }
        f8Cipher.init(true, new KeyParameter(maskedKey));
    }

    public static void process(BlockCipher cipher, byte[] data, int off, int len, byte[] iv, BlockCipher f8Cipher) {
        SRTPCipherF8 sRTPCipherF8 = new SRTPCipherF8();
        sRTPCipherF8.getClass();
        F8Context f8ctx = new F8Context();
        f8ctx.ivAccent = new byte[16];
        f8Cipher.processBlock(iv, 0, f8ctx.ivAccent, 0);
        f8ctx.J = 0;
        f8ctx.S = new byte[16];
        Arrays.fill(f8ctx.S, (byte) 0);
        int inLen = len;
        while (inLen >= 16) {
            processBlock(cipher, f8ctx, data, off, data, off, 16);
            inLen -= 16;
            off += 16;
        }
        if (inLen > 0) {
            processBlock(cipher, f8ctx, data, off, data, off, inLen);
        }
    }

    private static void processBlock(BlockCipher cipher, F8Context f8ctx, byte[] in, int inOff, byte[] out, int outOff, int len) {
        int i;
        byte[] bArr;
        for (i = 0; i < 16; i++) {
            bArr = f8ctx.S;
            bArr[i] = (byte) (bArr[i] ^ f8ctx.ivAccent[i]);
        }
        bArr = f8ctx.S;
        bArr[12] = (byte) ((int) (((long) bArr[12]) ^ (f8ctx.J >> 24)));
        bArr = f8ctx.S;
        bArr[13] = (byte) ((int) (((long) bArr[13]) ^ (f8ctx.J >> 16)));
        bArr = f8ctx.S;
        bArr[14] = (byte) ((int) (((long) bArr[14]) ^ (f8ctx.J >> 8)));
        bArr = f8ctx.S;
        bArr[15] = (byte) ((int) (((long) bArr[15]) ^ (f8ctx.J >> null)));
        f8ctx.J++;
        cipher.processBlock(f8ctx.S, 0, f8ctx.S, 0);
        for (i = 0; i < len; i++) {
            out[outOff + i] = (byte) (in[inOff + i] ^ f8ctx.S[i]);
        }
    }
}
