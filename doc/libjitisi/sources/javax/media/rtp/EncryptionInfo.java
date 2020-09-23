package javax.media.rtp;

import java.io.Serializable;

public class EncryptionInfo implements Serializable {
    public static final int DES = 3;
    public static final int MD5 = 2;
    public static final int NO_ENCRYPTION = 0;
    public static final int TRIPLE_DES = 4;
    public static final int XOR = 1;
    private byte[] key;
    private int type;

    public EncryptionInfo(int type, byte[] key) {
        this.type = type;
        this.key = key;
    }

    public byte[] getKey() {
        return this.key;
    }

    public int getType() {
        return this.type;
    }
}
