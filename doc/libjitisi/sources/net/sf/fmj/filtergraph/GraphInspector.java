package net.sf.fmj.filtergraph;

import javax.media.Codec;
import javax.media.Format;
import javax.media.Multiplexer;
import javax.media.PlugIn;
import javax.media.Renderer;

public interface GraphInspector {
    boolean detailMode();

    boolean verify(Codec codec, Format format, Format format2);

    boolean verify(Multiplexer multiplexer, Format[] formatArr);

    boolean verify(Renderer renderer, Format format);

    void verifyInputFailed(PlugIn plugIn, Format format);

    void verifyOutputFailed(PlugIn plugIn, Format format);
}
