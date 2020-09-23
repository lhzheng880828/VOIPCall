package org.jitsi.bccontrib.prng;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-9-21
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.prng.RandomGenerator;

import java.util.Arrays;

public class FortunaGenerator implements RandomGenerator {
    private static final int SEED_FILE_SIZE = 64;
    private static final int NUM_POOLS = 32;
    private static final int MIN_POOL_SIZE = 64;
    private final FortunaGenerator.Generator generator;
    private final Digest[] pools;
    private long lastReseed;
    private int pool;
    private int pool0Count;
    private int reseedCount;
    private boolean initialized;
    private byte[] buffer;
    protected int ndx;

    public FortunaGenerator() {
        this((byte[])null);
    }

    public FortunaGenerator(byte[] seed) {
        this.lastReseed = 0L;
        this.pool = 0;
        this.pool0Count = 0;
        this.reseedCount = 0;
        this.initialized = false;
        this.ndx = 0;
        this.generator = new FortunaGenerator.Generator(new AESFastEngine(), new SHA256Digest());
        this.pools = new Digest[32];

        for(int i = 0; i < 32; ++i) {
            this.pools[i] = new SHA256Digest();
        }

        this.buffer = new byte[256];
        if (seed != null) {
            this.generator.init(seed);
            this.fillBlock();
            this.initialized = true;
        }

    }

    private void fillBlock() {
        if (this.pool0Count >= 64 && System.currentTimeMillis() - this.lastReseed > 100L) {
            long powerOfTwo = 1L;
            ++this.reseedCount;
            byte[] randomBytes = new byte[this.pools[0].getDigestSize()];

            for(int i = 0; i < 32 && (i == 0 || (long)this.reseedCount % powerOfTwo == 0L); ++i) {
                this.pools[i].doFinal(randomBytes, 0);
                this.generator.addRandomBytes(randomBytes, 0, randomBytes.length);
                powerOfTwo <<= 1;
            }

            this.lastReseed = System.currentTimeMillis();
            this.pool0Count = 0;
        }

        this.generator.nextBytes(this.buffer, 0, this.buffer.length);
    }

    public void nextBytes(byte[] out) {
        this.nextBytes(out, 0, out.length);
    }

    public void nextBytes(byte[] out, int offset, int length) {
        if (!this.initialized) {
            throw new IllegalStateException(" Fortuna generator not initialized/seeded");
        } else if (length != 0) {
            if (offset >= 0 && length >= 0 && offset + length <= out.length) {
                if (this.ndx >= this.buffer.length) {
                    this.fillBlock();
                    this.ndx = 0;
                }

                int count = 0;

                while(count < length) {
                    int amount = Math.min(this.buffer.length - this.ndx, length - count);
                    System.arraycopy(this.buffer, this.ndx, out, offset + count, amount);
                    count += amount;
                    this.ndx += amount;
                    if (this.ndx >= this.buffer.length) {
                        this.fillBlock();
                        this.ndx = 0;
                    }
                }

            } else {
                throw new ArrayIndexOutOfBoundsException("offset=" + offset + " length=" + length + " limit=" + out.length);
            }
        }
    }

    public void addSeedMaterial(long b) {
        this.pools[this.pool].update((byte)((int)(b & 255L)));
        this.pools[this.pool].update((byte)((int)(b >> 8 & 255L)));
        this.pools[this.pool].update((byte)((int)(b >> 16 & 255L)));
        this.pools[this.pool].update((byte)((int)(b >> 24 & 255L)));
        this.pools[this.pool].update((byte)((int)(b >> 32 & 255L)));
        this.pools[this.pool].update((byte)((int)(b >> 40 & 255L)));
        this.pools[this.pool].update((byte)((int)(b >> 48 & 255L)));
        this.pools[this.pool].update((byte)((int)(b >> 56 & 255L)));
        if (this.pool == 0) {
            this.pool0Count += 8;
        }

        this.pool = (this.pool + 1) % 32;
    }

    public void addSeedMaterial(byte[] buf) {
        this.addSeedMaterial(buf, 0, buf.length);
    }

    public void addSeedMaterial(byte[] buf, int offset, int length) {
        this.pools[this.pool].update(buf, offset, length);
        if (this.pool == 0) {
            this.pool0Count += buf.length;
        }

        this.pool = (this.pool + 1) % 32;
    }

    public void addSeedMaterial(int poolNumber, byte[] data, int offset, int length) {
        if (poolNumber >= 0 && poolNumber < this.pools.length) {
            this.pools[poolNumber].update((byte)length);
            this.pools[poolNumber].update(data, offset, length);
            if (poolNumber == 0) {
                this.pool0Count += length;
            }

        } else {
            throw new IllegalArgumentException("pool number out of range: " + poolNumber);
        }
    }

    public byte[] getSeedStatus() {
        byte[] seed = new byte[64];
        this.generator.nextBytes(seed, 0, seed.length);
        return seed;
    }

    public void setSeedStatus(byte[] seedStatus) {
        this.generator.init(seedStatus);
        this.fillBlock();
        this.initialized = true;
    }

    private static class Generator {
        private static final int LIMIT = 1048576;
        private final BlockCipher cipher;
        private final Digest hash;
        private final byte[] counter;
        private final byte[] key;
        private byte[] buffer;
        protected int ndx;

        private Generator(BlockCipher cipher, Digest hash) {
            this.ndx = 0;
            this.cipher = cipher;
            this.hash = hash;
            this.counter = new byte[cipher.getBlockSize()];
            this.buffer = new byte[cipher.getBlockSize()];
            this.key = new byte[32];
        }

        private void nextBytes(byte[] out, int offset, int length) {
            int count = 0;

            do {
                int amount = Math.min(1048576, length - count);
                this.nextBytesInternal(out, offset + count, amount);
                count += amount;

                for(int i = 0; i < this.key.length; i += this.counter.length) {
                    this.fillBlock();
                    int l = Math.min(this.key.length - i, this.cipher.getBlockSize());
                    System.arraycopy(this.buffer, 0, this.key, i, l);
                }

                this.resetKey();
            } while(count < length);

            this.fillBlock();
            this.ndx = 0;
        }

        private void addRandomBytes(byte[] seed, int offset, int length) {
            this.hash.update(this.key, 0, this.key.length);
            this.hash.update(seed, offset, length);
            byte[] newkey = new byte[this.hash.getDigestSize()];
            this.hash.doFinal(newkey, 0);
            System.arraycopy(newkey, 0, this.key, 0, Math.min(this.key.length, newkey.length));
            this.resetKey();
            this.incrementCounter();
        }

        private void fillBlock() {
            this.cipher.processBlock(this.counter, 0, this.buffer, 0);
            this.incrementCounter();
        }

        private void init(byte[] seed) {
            Arrays.fill(this.key, (byte)0);
            Arrays.fill(this.counter, (byte)0);
            if (seed != null) {
                this.addRandomBytes(seed, 0, seed.length);
            }

            this.fillBlock();
        }

        private void nextBytesInternal(byte[] out, int offset, int length) {
            if (length != 0) {
                if (offset >= 0 && length >= 0 && offset + length <= out.length) {
                    if (this.ndx >= this.buffer.length) {
                        this.fillBlock();
                        this.ndx = 0;
                    }

                    int count = 0;

                    while(count < length) {
                        int amount = Math.min(this.buffer.length - this.ndx, length - count);
                        System.arraycopy(this.buffer, this.ndx, out, offset + count, amount);
                        count += amount;
                        this.ndx += amount;
                        if (this.ndx >= this.buffer.length) {
                            this.fillBlock();
                            this.ndx = 0;
                        }
                    }

                } else {
                    throw new ArrayIndexOutOfBoundsException("offset=" + offset + " length=" + length + " limit=" + out.length);
                }
            }
        }

        private void resetKey() {
            this.cipher.reset();
            this.cipher.init(true, new KeyParameter(this.key));
        }

        private void incrementCounter() {
            for(int i = 0; i < this.counter.length; ++i) {
                ++this.counter[i];
                if (this.counter[i] != 0) {
                    break;
                }
            }

        }
    }
}

