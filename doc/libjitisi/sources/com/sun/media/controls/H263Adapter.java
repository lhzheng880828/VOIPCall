package com.sun.media.controls;

import javax.media.Codec;
import javax.media.control.H263Control;
import org.jitsi.android.util.java.awt.Component;

public class H263Adapter implements H263Control {
    private boolean advancedPrediction;
    private boolean arithmeticCoding;
    private int bppMaxKb;
    private boolean errorCompensation;
    private int hrd_B;
    private Codec owner;
    private boolean pbFrames;
    private boolean settable;
    private boolean unrestrictedVector;

    public H263Adapter(Codec owner, boolean prediction, boolean coding, boolean compensation, boolean frames, boolean vector, int hrd_b, int kb, boolean settable) {
        this.owner = owner;
        this.advancedPrediction = prediction;
        this.arithmeticCoding = coding;
        this.errorCompensation = compensation;
        this.pbFrames = frames;
        this.unrestrictedVector = vector;
        this.hrd_B = hrd_b;
        this.bppMaxKb = kb;
        this.settable = settable;
    }

    public boolean getAdvancedPrediction() {
        return this.advancedPrediction;
    }

    public boolean getArithmeticCoding() {
        return this.arithmeticCoding;
    }

    public int getBppMaxKb() {
        return this.bppMaxKb;
    }

    public Component getControlComponent() {
        throw new UnsupportedOperationException();
    }

    public boolean getErrorCompensation() {
        return this.errorCompensation;
    }

    public int getHRD_B() {
        return this.hrd_B;
    }

    public boolean getPBFrames() {
        return this.pbFrames;
    }

    public boolean getUnrestrictedVector() {
        return this.unrestrictedVector;
    }

    public boolean isAdvancedPredictionSupported() {
        throw new UnsupportedOperationException();
    }

    public boolean isArithmeticCodingSupported() {
        throw new UnsupportedOperationException();
    }

    public boolean isErrorCompensationSupported() {
        throw new UnsupportedOperationException();
    }

    public boolean isPBFramesSupported() {
        throw new UnsupportedOperationException();
    }

    public boolean isUnrestrictedVectorSupported() {
        throw new UnsupportedOperationException();
    }

    public boolean setAdvancedPrediction(boolean newAdvancedPredictionMode) {
        throw new UnsupportedOperationException();
    }

    public boolean setArithmeticCoding(boolean newArithmeticCodingMode) {
        throw new UnsupportedOperationException();
    }

    public boolean setErrorCompensation(boolean newtErrorCompensationMode) {
        throw new UnsupportedOperationException();
    }

    public boolean setPBFrames(boolean newPBFramesMode) {
        throw new UnsupportedOperationException();
    }

    public boolean setUnrestrictedVector(boolean newUnrestrictedVectorMode) {
        throw new UnsupportedOperationException();
    }
}
