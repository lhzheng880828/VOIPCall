package javax.sound.sampled;

import java.util.Map;

public class AudioFormat {
    protected Encoding encoding;

    public static class Encoding {
        public static final Encoding ALAW = new Encoding("ALAW");
        public static final Encoding PCM_SIGNED = new Encoding("PCM_SIGNED");
        public static final Encoding PCM_UNSIGNED = new Encoding("PCM_UNSIGNED");
        public static final Encoding ULAW = new Encoding("ULAW");
        private String name;

        public Encoding(String name) {
            this.name = name;
        }
    }

    public AudioFormat(float sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian) {
    }

    public AudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian) {
    }

    public AudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian, Map<String, Object> map) {
    }

    public float getSampleRate() {
        return 0.0f;
    }

    public int getSampleSizeInBits() {
        return 0;
    }

    public int getChannels() {
        return 1;
    }

    public boolean isBigEndian() {
        return false;
    }

    public float getFrameRate() {
        return 0.0f;
    }

    public int getFrameSize() {
        return 0;
    }

    public Encoding getEncoding() {
        return this.encoding;
    }


}
