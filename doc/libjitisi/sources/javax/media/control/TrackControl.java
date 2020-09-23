package javax.media.control;

import javax.media.Codec;
import javax.media.Controls;
import javax.media.NotConfiguredError;
import javax.media.Renderer;
import javax.media.UnsupportedPlugInException;

public interface TrackControl extends FormatControl, Controls {
    void setCodecChain(Codec[] codecArr) throws UnsupportedPlugInException, NotConfiguredError;

    void setRenderer(Renderer renderer) throws UnsupportedPlugInException, NotConfiguredError;
}
