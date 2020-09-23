package javax.sound.sampled;

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioFormat.Encoding;

public class AudioSystem {
    public static AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        return null;
    }

    public static AudioFormat[] getTargetFormats(Encoding targetEncoding, AudioFormat sourceFormat) {
        return new AudioFormat[0];
    }
}
