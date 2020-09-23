package org.jitsi.bccontrib.mac;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-9-21
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import org.jitsi.bccontrib.digests.Skein;
import org.jitsi.bccontrib.params.ParametersForSkein;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.params.KeyParameter;

public class SkeinMac implements Mac {
    private Skein skein;
    private long[] Xsave;

    public SkeinMac() {
    }

    public void init(CipherParameters params) throws IllegalArgumentException {
        ParametersForSkein p = (ParametersForSkein)params;
        KeyParameter kp = (KeyParameter)((KeyParameter)p.getParameters());
        this.skein = new Skein(p.getStateSize(), p.getMacSize(), 0L, kp.getKey());
        this.Xsave = this.skein.getState();
    }

    public String getAlgorithmName() {
        return this.skein.getAlgorithmName() + "/MAC";
    }

    public int getMacSize() {
        return this.skein.getDigestSize();
    }

    public void update(byte in) throws IllegalStateException {
        this.skein.update(in);
    }

    public void updateBits(byte[] in, int inOff, int len) throws DataLengthException, IllegalStateException {
        this.skein.updateBits(in, inOff, len);
    }

    public void update(byte[] in, int inOff, int len) throws DataLengthException, IllegalStateException {
        this.skein.update(in, inOff, len);
    }

    public int doFinal(byte[] out, int outOff) throws DataLengthException, IllegalStateException {
        int len = this.skein.doFinal(out, outOff);
        this.reset();
        return len;
    }

    public void reset() {
        this.skein.initialize(this.Xsave);
    }
}

