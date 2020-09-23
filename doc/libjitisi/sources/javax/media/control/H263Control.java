package javax.media.control;

import javax.media.Control;

public interface H263Control extends Control {
    boolean getAdvancedPrediction();

    boolean getArithmeticCoding();

    int getBppMaxKb();

    boolean getErrorCompensation();

    int getHRD_B();

    boolean getPBFrames();

    boolean getUnrestrictedVector();

    boolean isAdvancedPredictionSupported();

    boolean isArithmeticCodingSupported();

    boolean isErrorCompensationSupported();

    boolean isPBFramesSupported();

    boolean isUnrestrictedVectorSupported();

    boolean setAdvancedPrediction(boolean z);

    boolean setArithmeticCoding(boolean z);

    boolean setErrorCompensation(boolean z);

    boolean setPBFrames(boolean z);

    boolean setUnrestrictedVector(boolean z);
}
