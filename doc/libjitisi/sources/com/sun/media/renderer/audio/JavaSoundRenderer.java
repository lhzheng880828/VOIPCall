package com.sun.media.renderer.audio;

import javax.media.Format;
import javax.media.format.AudioFormat;

public class JavaSoundRenderer extends net.sf.fmj.media.renderer.audio.JavaSoundRenderer {
    private Format[] supportedInputFormats;

    public JavaSoundRenderer() {
        Format[] formatArr = new Format[2];
        formatArr[0] = new AudioFormat(AudioFormat.LINEAR, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray);
        formatArr[1] = new AudioFormat(AudioFormat.ULAW, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray);
        this.supportedInputFormats = formatArr;
    }

    public Format[] getSupportedInputFormats() {
        return this.supportedInputFormats;
    }
}
