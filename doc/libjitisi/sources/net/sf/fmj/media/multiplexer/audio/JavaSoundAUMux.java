package net.sf.fmj.media.multiplexer.audio;

import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.protocol.FileTypeDescriptor;
import org.jitsi.android.util.javax.sound.sampled.AudioFileFormat.Type;

public class JavaSoundAUMux extends JavaSoundMux {
    public JavaSoundAUMux() {
        super(new FileTypeDescriptor(FileTypeDescriptor.BASIC_AUDIO), Type.AU);
    }

    public Format[] getSupportedInputFormats() {
        return new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, 8, -1, -1, 1), new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, -1, 1, 1), new AudioFormat(AudioFormat.LINEAR, -1.0d, 24, -1, 1, 1), new AudioFormat(AudioFormat.LINEAR, -1.0d, 32, -1, 1, 1), new AudioFormat(AudioFormat.ULAW), new AudioFormat(AudioFormat.ALAW)};
    }
}
