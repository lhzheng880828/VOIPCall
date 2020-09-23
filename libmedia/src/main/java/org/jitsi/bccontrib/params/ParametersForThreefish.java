package org.jitsi.bccontrib.params;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-9-21
 */


import org.bouncycastle.crypto.CipherParameters;

public class ParametersForThreefish implements CipherParameters {
    public static final int Threefish256 = 256;
    public static final int Threefish512 = 512;
    public static final int Threefish1024 = 1024;
    private int stateSize;
    private CipherParameters parameters;
    private long[] tweak;

    public ParametersForThreefish(CipherParameters parameters, int stateSize, long[] tweak) {
        this.stateSize = stateSize;
        this.parameters = parameters;
        if (tweak != null) {
            this.tweak = new long[2];
            this.tweak[0] = tweak[0];
            this.tweak[1] = tweak[1];
        }

    }

    public int getStateSize() {
        return this.stateSize;
    }

    public CipherParameters getParameters() {
        return this.parameters;
    }

    public long[] getTweak() {
        return this.tweak;
    }
}

