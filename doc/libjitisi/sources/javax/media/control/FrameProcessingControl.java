package javax.media.control;

import javax.media.Control;

public interface FrameProcessingControl extends Control {
    int getFramesDropped();

    void setFramesBehind(float f);

    boolean setMinimalProcessing(boolean z);
}
