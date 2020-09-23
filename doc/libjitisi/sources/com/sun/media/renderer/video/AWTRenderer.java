package com.sun.media.renderer.video;

import com.lti.utils.UnsignedUtils;
import javax.media.Format;
import javax.media.format.RGBFormat;
import net.sf.fmj.media.renderer.video.SimpleAWTRenderer;

public class AWTRenderer extends SimpleAWTRenderer {
    private final Format[] supportedInputFormats = new Format[]{new RGBFormat(null, -1, Format.intArray, -1.0f, 32, 16711680, 65280, UnsignedUtils.MAX_UBYTE, 1, -1, 0, -1), new RGBFormat(null, -1, Format.intArray, -1.0f, 32, UnsignedUtils.MAX_UBYTE, 65280, 16711680, 1, -1, 0, -1)};

    public String getName() {
        return "AWT Renderer";
    }

    public Format[] getSupportedInputFormats() {
        return this.supportedInputFormats;
    }
}
