package org.jitsi.bccontrib.digests;

import org.bouncycastle.crypto.ExtendedDigest;
import org.jitsi.bccontrib.engines.ThreefishCipher;
import org.jitsi.bccontrib.util.ByteLong;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-9-21
 */
public class Skein implements ExtendedDigest {
    public static final int NORMAL = 0;
    public static final int ZEROED_STATE = 1;
    public static final int CHAINED_STATE = 2;
    public static final int CHAINED_CONFIG = 3;
    private final byte[] schema = new byte[]{83, 72, 65, 51};
    private ThreefishCipher cipher;
    private int cipherStateBits;
    private int cipherStateBytes;
    private int cipherStateWords;
    private int outputBytes;
    private byte[] inputBuffer;
    private int bytesFilled;
    private long[] cipherInput;
    private long[] state;
    private int hashSize;
    Skein.SkeinConfig configuration;
    public Skein.UbiTweak ubiParameters;

    public int getStateSize() {
        return this.cipherStateBits;
    }

    public Skein(int stateSize, int outputSize) throws IllegalArgumentException {
        this.setup(stateSize, outputSize);
        this.configuration = new Skein.SkeinConfig(this);
        this.configuration.setSchema(this.schema);
        this.configuration.setVersion(1);
        this.configuration.generateConfiguration();
        this.initialize();
    }

    public Skein(int stateSize, int outputSize, long treeInfo, byte[] key) throws IllegalArgumentException {
        this.setup(stateSize, outputSize);
        if (key.length > 0) {
            this.outputBytes = this.cipherStateBytes;
            this.ubiParameters.startNewBlockType(0L);
            this.update(key, 0, key.length);
            byte[] preHash = this.finalPad();

            for(int i = 0; i < this.cipherStateWords; ++i) {
                this.state[i] = ByteLong.GetUInt64(preHash, i * 8);
            }
        }

        this.outputBytes = (outputSize + 7) / 8;
        this.configuration = new Skein.SkeinConfig(this);
        this.configuration.setSchema(this.schema);
        this.configuration.setVersion(1);
        this.initialize(3);
    }

    private void setup(int stateSize, int outputSize) throws IllegalArgumentException {
        if (outputSize <= 0) {
            throw new IllegalArgumentException("Skein: Output bit size must be greater than zero.");
        } else {
            this.cipherStateBits = stateSize;
            this.cipherStateBytes = stateSize / 8;
            this.cipherStateWords = stateSize / 64;
            this.hashSize = outputSize;
            this.outputBytes = (outputSize + 7) / 8;
            this.cipher = ThreefishCipher.createCipher(stateSize);
            if (this.cipher == null) {
                throw new IllegalArgumentException("Skein: Unsupported state size.");
            } else {
                this.inputBuffer = new byte[this.cipherStateBytes];
                this.cipherInput = new long[this.cipherStateWords];
                this.state = new long[this.cipherStateWords];
                this.ubiParameters = new Skein.UbiTweak();
            }
        }
    }

    void ProcessBlock(int bytes) {
        this.cipher.setKey(this.state);
        this.ubiParameters.addBytesProcessed(bytes);
        this.cipher.setTweak(this.ubiParameters.getTweak());
        this.cipher.encrypt(this.cipherInput, this.state);

        for(int i = 0; i < this.cipherInput.length; ++i) {
            long[] var10000 = this.state;
            var10000[i] ^= this.cipherInput[i];
        }

    }

    public void updateBits(byte[] array, int start, int length) throws IllegalStateException {
        if (this.ubiParameters.isBitPad()) {
            throw new IllegalStateException("Skein: partial byte only on last data block");
        } else if ((length & 7) == 0) {
            this.update(array, start, length >>> 3);
        } else {
            this.update(array, start, (length >>> 3) + 1);
            byte mask = (byte)(1 << 7 - (length & 7));
            this.inputBuffer[this.bytesFilled - 1] = (byte)(this.inputBuffer[this.bytesFilled - 1] & 0 - mask | mask);
            this.ubiParameters.setBitPad(true);
        }
    }

    public void update(byte[] array, int start, int length) {
        for(int bytesDone = 0; bytesDone < length; ++bytesDone) {
            if (this.bytesFilled == this.cipherStateBytes) {
                this.InputBufferToCipherInput();
                this.ProcessBlock(this.cipherStateBytes);
                this.ubiParameters.setFirstBlock(false);
                this.bytesFilled = 0;
            }

            this.inputBuffer[this.bytesFilled++] = array[start++];
        }

    }

    public byte[] doFinal() {
        int i;
        for(i = this.bytesFilled; i < this.inputBuffer.length; ++i) {
            this.inputBuffer[i] = 0;
        }

        this.InputBufferToCipherInput();
        this.ubiParameters.setFinalBlock(true);
        this.ProcessBlock(this.bytesFilled);

        for(i = 0; i < this.cipherInput.length; ++i) {
            this.cipherInput[i] = 0L;
        }

        byte[] hash = new byte[this.outputBytes];
        long[] oldState = new long[this.cipherStateWords];

        int j;
        for(j = 0; j < this.state.length; ++j) {
            oldState[j] = this.state[j];
        }

        for(i = 0; i < this.outputBytes; i += this.cipherStateBytes) {
            this.ubiParameters.startNewBlockType(63L);
            this.ubiParameters.setFinalBlock(true);
            this.ProcessBlock(8);
            int outputSize = this.outputBytes - i;
            if (outputSize > this.cipherStateBytes) {
                outputSize = this.cipherStateBytes;
            }

            ByteLong.PutBytes(this.state, hash, i, outputSize);

            for(j = 0; j < this.state.length; ++j) {
                this.state[j] = oldState[j];
            }

            long var10002 = this.cipherInput[0]++;
        }

        this.reset();
        return hash;
    }

    private byte[] finalPad() {
        int i;
        for(i = this.bytesFilled; i < this.inputBuffer.length; ++i) {
            this.inputBuffer[i] = 0;
        }

        this.InputBufferToCipherInput();
        this.ubiParameters.setFinalBlock(true);
        this.ProcessBlock(this.bytesFilled);
        byte[] data = new byte[this.outputBytes];

        for(i = 0; i < this.outputBytes; i += this.cipherStateBytes) {
            int outputSize = this.outputBytes - i;
            if (outputSize > this.cipherStateBytes) {
                outputSize = this.cipherStateBytes;
            }

            ByteLong.PutBytes(this.state, data, i, outputSize);
        }

        return data;
    }

    private void initialize(int initializationType) {
        switch(initializationType) {
            case 0:
                this.initialize();
                return;
            case 1:
                for(int i = 0; i < this.state.length; ++i) {
                    this.state[i] = 0L;
                }
            case 2:
            default:
                this.bytesFilled = 0;
                return;
            case 3:
                this.configuration.generateConfiguration(this.state);
                this.initialize();
        }
    }

    private final void initialize() {
        for(int i = 0; i < this.state.length; ++i) {
            this.state[i] = this.configuration.ConfigValue[i];
        }

        this.ubiParameters.startNewBlockType(48L);
        this.bytesFilled = 0;
    }

    public final void initialize(long[] externalState) {
        for(int i = 0; i < this.state.length; ++i) {
            this.state[i] = externalState[i];
        }

        this.ubiParameters.startNewBlockType(48L);
        this.bytesFilled = 0;
    }

    void InputBufferToCipherInput() {
        for(int i = 0; i < this.cipherStateWords; ++i) {
            this.cipherInput[i] = ByteLong.GetUInt64(this.inputBuffer, i * 8);
        }

    }

    public int getcipherStateBits() {
        return this.cipherStateBits;
    }

    public int getHashSize() {
        return this.hashSize;
    }

    public String getAlgorithmName() {
        return "Skein" + this.cipherStateBits;
    }

    public int getDigestSize() {
        return this.outputBytes;
    }

    public void update(byte in) {
        byte[] tmp = new byte[1];
        this.update(tmp, 0, 1);
    }

    public int doFinal(byte[] out, int outOff) {
        byte[] hash = this.doFinal();
        System.arraycopy(hash, 0, out, outOff, hash.length);
        return hash.length;
    }

    public void reset() {
        this.initialize();
    }

    public int getByteLength() {
        return this.cipherStateBytes;
    }

    public long[] getState() {
        long[] s = new long[this.state.length];

        for(int i = 0; i < this.state.length; ++i) {
            s[i] = this.state[i];
        }

        return s;
    }

    class UbiTweak {
        static final long Key = 0L;
        static final long Config = 4L;
        static final long Personalization = 8L;
        static final long PublicKey = 12L;
        static final long KeyIdentifier = 16L;
        static final long Nonce = 20L;
        static final long Message = 48L;
        static final long Out = 63L;
        private static final long T1FlagFinal = -9223372036854775808L;
        private static final long T1FlagFirst = 4611686018427387904L;
        private static final long T1FlagBitPad = 36028797018963968L;
        private long[] tweak = new long[2];

        UbiTweak() {
        }

        boolean isFirstBlock() {
            return (this.tweak[1] & 4611686018427387904L) != 0L;
        }

        void setFirstBlock(boolean value) {
            long[] var10000;
            if (value) {
                var10000 = this.tweak;
                var10000[1] |= 4611686018427387904L;
            } else {
                var10000 = this.tweak;
                var10000[1] &= -4611686018427387905L;
            }

        }

        boolean isFinalBlock() {
            return (this.tweak[1] & -9223372036854775808L) != 0L;
        }

        void setFinalBlock(boolean value) {
            long[] var10000;
            if (value) {
                var10000 = this.tweak;
                var10000[1] |= -9223372036854775808L;
            } else {
                var10000 = this.tweak;
                var10000[1] &= 9223372036854775807L;
            }

        }

        boolean isBitPad() {
            return (this.tweak[1] & 36028797018963968L) != 0L;
        }

        void setBitPad(boolean value) {
            long[] var10000;
            if (value) {
                var10000 = this.tweak;
                var10000[1] |= 36028797018963968L;
            } else {
                var10000 = this.tweak;
                var10000[1] &= -36028797018963969L;
            }

        }

        byte getTreeLevel() {
            return (byte)((int)(this.tweak[1] >> 48 & 127L));
        }

        void setTreeLevel(int value) throws Exception {
            if (value > 63) {
                throw new Exception("Tree level must be between 0 and 63, inclusive.");
            } else {
                long[] var10000 = this.tweak;
                var10000[1] &= -35747322042253313L;
                var10000 = this.tweak;
                var10000[1] |= (long)value << 48;
            }
        }

        long[] getBitsProcessed() {
            long[] retval = new long[]{this.tweak[0], this.tweak[1] & 4294967295L};
            return retval;
        }

        void setBitsProcessed(long value) {
            this.tweak[0] = value;
            long[] var10000 = this.tweak;
            var10000[1] &= -4294967296L;
        }

        void addBytesProcessed(int value) {
            //int len = true;
            long carry = (long)value;
            long[] words = new long[]{this.tweak[0] & 4294967295L, this.tweak[0] >>> 32 & 4294967295L, this.tweak[1] & 4294967295L};

            for(int i = 0; i < 3; ++i) {
                carry += words[i];
                words[i] = carry;
                carry >>= 32;
            }

            this.tweak[0] = words[0] & 4294967295L;
            long[] var10000 = this.tweak;
            var10000[0] |= (words[1] & 4294967295L) << 32;
            var10000 = this.tweak;
            var10000[1] |= words[2] & 4294967295L;
        }

        long getBlockType() {
            return this.tweak[1] >> 56 & 63L;
        }

        void setBlockType(long value) {
            this.tweak[1] = value << 56;
        }

        void startNewBlockType(long type) {
            this.setBitsProcessed(0L);
            this.setBlockType(type);
            this.setFirstBlock(true);
        }

        long[] getTweak() {
            return this.tweak;
        }

        void setTweak(long[] tweak) {
            this.tweak = tweak;
        }
    }

    class SkeinConfig {
        private final int stateSize;
        long[] ConfigValue;
        long[] ConfigString;

        SkeinConfig(Skein sourceHash) {
            this.stateSize = sourceHash.getcipherStateBits();
            this.ConfigValue = new long[this.stateSize / 64];
            this.ConfigString = new long[this.ConfigValue.length];
            this.ConfigString[1] = (long)sourceHash.getHashSize();
        }

        void generateConfiguration() {
            ThreefishCipher cipher = ThreefishCipher.createCipher(this.stateSize);
            Skein.UbiTweak tweak = Skein.this.new UbiTweak();
            tweak.startNewBlockType(4L);
            tweak.setFinalBlock(true);
            tweak.setBitsProcessed(32L);
            cipher.setTweak(tweak.getTweak());
            cipher.encrypt(this.ConfigString, this.ConfigValue);
            long[] var10000 = this.ConfigValue;
            var10000[0] ^= this.ConfigString[0];
            var10000 = this.ConfigValue;
            var10000[1] ^= this.ConfigString[1];
            var10000 = this.ConfigValue;
            var10000[2] ^= this.ConfigString[2];
        }

        void generateConfiguration(long[] initialState) {
            ThreefishCipher cipher = ThreefishCipher.createCipher(this.stateSize);
            Skein.UbiTweak tweak = Skein.this.new UbiTweak();
            tweak.startNewBlockType(4L);
            tweak.setFinalBlock(true);
            tweak.setBitsProcessed(32L);
            cipher.setKey(initialState);
            cipher.setTweak(tweak.getTweak());
            cipher.encrypt(this.ConfigString, this.ConfigValue);
            long[] var10000 = this.ConfigValue;
            var10000[0] ^= this.ConfigString[0];
            var10000 = this.ConfigValue;
            var10000[1] ^= this.ConfigString[1];
            var10000 = this.ConfigValue;
            var10000[2] ^= this.ConfigString[2];
        }

        void setSchema(byte[] schema) throws IllegalArgumentException {
            if (schema.length != 4) {
                throw new IllegalArgumentException("Skein configuration: Schema must be 4 bytes.");
            } else {
                long n = this.ConfigString[0];
                n &= -4294967296L;
                n |= (long)schema[3] << 24;
                n |= (long)schema[2] << 16;
                n |= (long)schema[1] << 8;
                n |= (long)schema[0];
                this.ConfigString[0] = n;
            }
        }

        void setVersion(int version) throws IllegalArgumentException {
            if (version >= 0 && version <= 3) {
                long[] var10000 = this.ConfigString;
                var10000[0] &= -12884901889L;
                var10000 = this.ConfigString;
                var10000[0] |= (long)version << 32;
            } else {
                throw new IllegalArgumentException("Skein configuration: Version must be between 0 and 3, inclusive.");
            }
        }

        void setTreeLeafSize(byte size) {
            long[] var10000 = this.ConfigString;
            var10000[2] &= -256L;
            var10000 = this.ConfigString;
            var10000[2] |= (long)size;
        }

        void setTreeFanOutSize(byte size) {
            long[] var10000 = this.ConfigString;
            var10000[2] &= -65281L;
            var10000 = this.ConfigString;
            var10000[2] |= (long)size << 8;
        }

        void setMaxTreeHeight(byte height) throws IllegalArgumentException {
            if (height == 1) {
                throw new IllegalArgumentException("Skein configuration: Tree height must be zero or greater than 1.");
            } else {
                long[] var10000 = this.ConfigString;
                var10000[2] &= -16711681L;
                var10000 = this.ConfigString;
                var10000[2] |= (long)height << 16;
            }
        }
    }
}
