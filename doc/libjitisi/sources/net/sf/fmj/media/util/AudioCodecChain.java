package net.sf.fmj.media.util;

import javax.media.format.AudioFormat;
import javax.media.format.UnsupportedFormatException;
import org.jitsi.android.util.java.awt.Component;

public class AudioCodecChain extends CodecChain {
    Component gainComp = null;

    public AudioCodecChain(AudioFormat af) throws UnsupportedFormatException {
        if (buildChain(af)) {
            this.renderer.close();
            this.firstBuffer = false;
            return;
        }
        throw new UnsupportedFormatException(af);
    }

    public Component getControlComponent() {
        if (this.gainComp != null) {
            return this.gainComp;
        }
        return this.gainComp;
    }

    public void reset() {
    }
}
