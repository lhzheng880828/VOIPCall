package net.sf.fmj.media.multiplexer.audio;

import javax.media.protocol.FileTypeDescriptor;
import org.jitsi.android.util.javax.sound.sampled.AudioFileFormat.Type;

public class AIFFMux extends JavaSoundMux {
    public AIFFMux() {
        super(new FileTypeDescriptor(FileTypeDescriptor.AIFF), Type.AIFF);
    }
}
