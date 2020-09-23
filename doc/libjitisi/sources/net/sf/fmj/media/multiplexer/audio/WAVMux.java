package net.sf.fmj.media.multiplexer.audio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.protocol.FileTypeDescriptor;
import net.sf.fmj.utility.LoggerSingleton;
import org.jitsi.android.util.javax.sound.sampled.AudioFileFormat.Type;

public class WAVMux extends JavaSoundMux {
    private static final boolean USE_JAVASOUND = true;
    private static final Logger logger = LoggerSingleton.logger;

    public WAVMux() {
        super(new FileTypeDescriptor(FileTypeDescriptor.WAVE), Type.WAVE);
    }

    public Format setInputFormat(Format format, int trackID) {
        AudioFormat af = (AudioFormat) format;
        if (af.getSampleSizeInBits() == 8 && af.getSigned() == 1) {
            return null;
        }
        if (af.getSampleSizeInBits() == 16 && af.getSigned() == 0) {
            return null;
        }
        return super.setInputFormat(format, trackID);
    }

    /* access modifiers changed from: protected */
    public void write(InputStream in, OutputStream out, org.jitsi.android.util.javax.sound.sampled.AudioFormat javaSoundFormat) throws IOException {
        super.write(in, out, javaSoundFormat);
    }
}
