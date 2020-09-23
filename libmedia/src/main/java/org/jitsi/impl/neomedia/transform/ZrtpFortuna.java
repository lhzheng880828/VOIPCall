package org.jitsi.impl.neomedia.transform;

import org.bouncycastle.crypto.prng.RandomGenerator;
import org.jitsi.bccontrib.prng.FortunaGenerator;

import java.security.SecureRandom;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-9-21
 */
public class ZrtpFortuna implements RandomGenerator {
    private static ZrtpFortuna singleInstance = null;
    private FortunaGenerator fortuna = null;

    protected ZrtpFortuna() {
    }

    public static synchronized ZrtpFortuna getInstance() {
        if (singleInstance == null) {
            singleInstance = new ZrtpFortuna();
            singleInstance.initialize();
        }

        return singleInstance;
    }

    private void initialize() {
        byte[] someData = new byte[256];
        (new SecureRandom()).nextBytes(someData);
        this.fortuna = new FortunaGenerator(someData);
    }

    public FortunaGenerator getFortuna() {
        return this.fortuna;
    }

    public synchronized void setFortuna(FortunaGenerator fortuna) {
        this.fortuna = fortuna;
    }

    public synchronized void addSeedMaterial(byte[] entropy) {
        this.fortuna.addSeedMaterial(entropy);
    }

    public synchronized void addSeedMaterial(long entropy) {
        this.fortuna.addSeedMaterial(entropy);
    }

    public synchronized void addSeedMaterial(byte[] entropy, int offset, int length) {
        this.fortuna.addSeedMaterial(entropy, offset, length);
    }

    public synchronized void addSeedMaterial(int poolNumber, byte[] entropy, int offset, int length) {
        this.fortuna.addSeedMaterial(poolNumber, entropy, offset, length);
    }

    public synchronized void nextBytes(byte[] randomData) {
        this.fortuna.nextBytes(randomData);
    }

    public synchronized void nextBytes(byte[] randomData, int offset, int length) {
        this.fortuna.nextBytes(randomData, offset, length);
    }
}
