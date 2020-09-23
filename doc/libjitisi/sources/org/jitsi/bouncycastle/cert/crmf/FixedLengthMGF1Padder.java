package org.jitsi.bouncycastle.cert.crmf;

import com.lti.utils.UnsignedUtils;
import java.security.SecureRandom;
import org.jitsi.bouncycastle.crypto.Digest;
import org.jitsi.bouncycastle.crypto.digests.SHA1Digest;
import org.jitsi.bouncycastle.crypto.generators.MGF1BytesGenerator;
import org.jitsi.bouncycastle.crypto.params.MGFParameters;

public class FixedLengthMGF1Padder implements EncryptedValuePadder {
    private Digest dig;
    private int length;
    private SecureRandom random;

    public FixedLengthMGF1Padder(int i) {
        this(i, null);
    }

    public FixedLengthMGF1Padder(int i, SecureRandom secureRandom) {
        this.dig = new SHA1Digest();
        this.length = i;
        this.random = secureRandom;
    }

    public byte[] getPaddedData(byte[] bArr) {
        byte[] bArr2 = new byte[this.length];
        byte[] bArr3 = new byte[this.dig.getDigestSize()];
        byte[] bArr4 = new byte[(this.length - this.dig.getDigestSize())];
        if (this.random == null) {
            this.random = new SecureRandom();
        }
        this.random.nextBytes(bArr3);
        MGF1BytesGenerator mGF1BytesGenerator = new MGF1BytesGenerator(this.dig);
        mGF1BytesGenerator.init(new MGFParameters(bArr3));
        mGF1BytesGenerator.generateBytes(bArr4, 0, bArr4.length);
        System.arraycopy(bArr3, 0, bArr2, 0, bArr3.length);
        System.arraycopy(bArr, 0, bArr2, bArr3.length, bArr.length);
        int length = bArr3.length + bArr.length;
        while (true) {
            length++;
            if (length == bArr2.length) {
                break;
            }
            bArr2[length] = (byte) (this.random.nextInt(UnsignedUtils.MAX_UBYTE) + 1);
        }
        for (length = 0; length != bArr4.length; length++) {
            int length2 = bArr3.length + length;
            bArr2[length2] = (byte) (bArr2[length2] ^ bArr4[length]);
        }
        return bArr2;
    }

    public byte[] getUnpaddedData(byte[] bArr) {
        int i;
        byte[] bArr2 = new byte[this.dig.getDigestSize()];
        byte[] bArr3 = new byte[(this.length - this.dig.getDigestSize())];
        System.arraycopy(bArr, 0, bArr2, 0, bArr2.length);
        MGF1BytesGenerator mGF1BytesGenerator = new MGF1BytesGenerator(this.dig);
        mGF1BytesGenerator.init(new MGFParameters(bArr2));
        mGF1BytesGenerator.generateBytes(bArr3, 0, bArr3.length);
        for (i = 0; i != bArr3.length; i++) {
            int length = bArr2.length + i;
            bArr[length] = (byte) (bArr[length] ^ bArr3[i]);
        }
        i = bArr.length;
        while (true) {
            i--;
            if (i == bArr2.length) {
                i = 0;
                break;
            } else if (bArr[i] == (byte) 0) {
                break;
            }
        }
        if (i == 0) {
            throw new IllegalStateException("bad padding in encoding");
        }
        byte[] bArr4 = new byte[(i - bArr2.length)];
        System.arraycopy(bArr, bArr2.length, bArr4, 0, bArr4.length);
        return bArr4;
    }
}
