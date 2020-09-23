package org.jitsi.bccontrib.params;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-9-21
 */


import org.bouncycastle.crypto.CipherParameters;

public class ParametersForSkein implements CipherParameters {
    public static final int Skein256 = 256;
    public static final int Skein512 = 512;
    public static final int Skein1024 = 1024;
    private int macSize;
    private int stateSize;
    private CipherParameters parameters;

    public ParametersForSkein(CipherParameters parameters, int stateSize, int macSize) {
        this.macSize = macSize;
        this.stateSize = stateSize;
        this.parameters = parameters;
    }

    public int getMacSize() {
        return this.macSize;
    }

    public int getStateSize() {
        return this.stateSize;
    }

    public CipherParameters getParameters() {
        return this.parameters;
    }
}

