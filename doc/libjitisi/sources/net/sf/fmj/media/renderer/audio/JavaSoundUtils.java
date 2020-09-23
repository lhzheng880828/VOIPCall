package net.sf.fmj.media.renderer.audio;

import java.util.HashMap;
import java.util.Map;
import javax.media.Format;
import org.jitsi.android.util.javax.sound.sampled.AudioFormat;
import org.jitsi.android.util.javax.sound.sampled.AudioFormat.Encoding;

public class JavaSoundUtils {
    public static boolean onlyStandardFormats = false;

    public static AudioFormat convertFormat(javax.media.format.AudioFormat format) {
        Encoding encoding;
        String encodingString = format.getEncoding();
        int channels = format.getChannels();
        double frameRate = format.getFrameRate();
        int frameSize = format.getFrameSizeInBits() / 8;
        double sampleRate = format.getSampleRate();
        int sampleSize = format.getSampleSizeInBits();
        boolean endian = format.getEndian() == 1;
        int signed = format.getSigned();
        if (javax.media.format.AudioFormat.LINEAR.equals(encodingString)) {
            switch (signed) {
                case 0:
                    encoding = Encoding.PCM_UNSIGNED;
                    break;
                case 1:
                    encoding = Encoding.PCM_SIGNED;
                    break;
                default:
                    throw new IllegalArgumentException("Signed/Unsigned must be specified");
            }
        } else if (javax.media.format.AudioFormat.ALAW.equals(encodingString)) {
            encoding = Encoding.ALAW;
        } else if (javax.media.format.AudioFormat.ULAW.equals(encodingString)) {
            encoding = Encoding.ULAW;
        } else if (toMpegEncoding(encodingString) != null) {
            encoding = toMpegEncoding(encodingString);
        } else if (toVorbisEncoding(encodingString) != null) {
            encoding = toVorbisEncoding(encodingString);
        } else {
            encoding = new CustomEncoding(encodingString);
        }
        Class<?> classMpegEncoding = null;
        Class<?> classVorbisEncoding = null;
        if (!onlyStandardFormats) {
            try {
                classMpegEncoding = Class.forName("javazoom.spi.mpeg.sampled.file.MpegEncoding");
                classVorbisEncoding = Class.forName("javazoom.spi.vorbis.sampled.file.VorbisEncoding");
            } catch (Exception e) {
            }
        }
        if (encoding == Encoding.PCM_SIGNED) {
            return new AudioFormat((float) sampleRate, sampleSize, channels, true, endian);
        }
        if (encoding == Encoding.PCM_UNSIGNED) {
            return new AudioFormat((float) sampleRate, sampleSize, channels, false, endian);
        }
        if (classMpegEncoding != null && classMpegEncoding.isInstance(encoding)) {
            try {
                return (AudioFormat) Class.forName("javazoom.spi.mpeg.sampled.file.MpegAudioFormat").getConstructor(new Class[]{Encoding.class, Float.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Float.TYPE, Boolean.TYPE, Map.class}).newInstance(new Object[]{encoding, Float.valueOf((float) sampleRate), Integer.valueOf(sampleSize), Integer.valueOf(channels), Integer.valueOf(frameSize), Float.valueOf((float) frameRate), Boolean.valueOf(endian), new HashMap()});
            } catch (Exception e2) {
                return null;
            }
        } else if (classVorbisEncoding == null || !classVorbisEncoding.isInstance(encoding)) {
            return new AudioFormat(encoding, (float) sampleRate, sampleSize, channels, frameSize, (float) frameRate, endian);
        } else {
            try {
                return (AudioFormat) Class.forName("javazoom.spi.vorbis.sampled.file.VorbisAudioFormat").getConstructor(new Class[]{Encoding.class, Float.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Float.TYPE, Boolean.TYPE, Map.class}).newInstance(new Object[]{encoding, Float.valueOf((float) sampleRate), Integer.valueOf(sampleSize), Integer.valueOf(channels), Integer.valueOf(frameSize), Float.valueOf((float) frameRate), Boolean.valueOf(endian), new HashMap()});
            } catch (Exception e3) {
                return null;
            }
        }
    }

    public static javax.media.format.AudioFormat convertFormat(AudioFormat format) {
        Encoding encoding = format.getEncoding();
        int channels = format.getChannels();
        float frameRate = format.getFrameRate();
        int frameSize = format.getFrameSize() < 0 ? format.getFrameSize() : format.getFrameSize() * 8;
        float sampleRate = format.getSampleRate();
        int sampleSize = format.getSampleSizeInBits();
        int endian = format.isBigEndian() ? 1 : 0;
        int signed = -1;
        String encodingString = javax.media.format.AudioFormat.LINEAR;
        if (encoding == Encoding.PCM_SIGNED) {
            signed = 1;
            encodingString = javax.media.format.AudioFormat.LINEAR;
        } else if (encoding == Encoding.PCM_UNSIGNED) {
            signed = 0;
            encodingString = javax.media.format.AudioFormat.LINEAR;
        } else if (encoding == Encoding.ALAW) {
            encodingString = javax.media.format.AudioFormat.ALAW;
        } else if (encoding == Encoding.ULAW) {
            encodingString = javax.media.format.AudioFormat.ULAW;
        } else {
            encodingString = encoding.toString();
        }
        return new javax.media.format.AudioFormat(encodingString, (double) sampleRate, sampleSize, channels, endian, signed, frameSize, (double) frameRate, Format.byteArray);
    }

    private static Encoding toMpegEncoding(String encodingStr) {
        try {
            if (!onlyStandardFormats) {
                int i;
                Class<?> classMpegEncoding = Class.forName("javazoom.spi.mpeg.sampled.file.MpegEncoding");
                String[] mpegEncodingStrings = new String[]{"MPEG1L1", "MPEG1L2", "MPEG1L3", "MPEG2DOT5L1", "MPEG2DOT5L2", "MPEG2DOT5L3", "MPEG2L1", "MPEG2L2", "MPEG2L3"};
                Encoding[] mpegEncodings = new Encoding[mpegEncodingStrings.length];
                for (i = 0; i < mpegEncodings.length; i++) {
                    mpegEncodings[i] = (Encoding) classMpegEncoding.getDeclaredField(mpegEncodingStrings[i]).get(null);
                }
                for (i = 0; i < mpegEncodings.length; i++) {
                    if (encodingStr.equals(mpegEncodings[i].toString())) {
                        return mpegEncodings[i];
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    private static Encoding toVorbisEncoding(String encodingStr) {
        try {
            if (!onlyStandardFormats) {
                int i;
                Class<?> classVorbisEncoding = Class.forName("javazoom.spi.vorbis.sampled.file.VorbisEncoding");
                String[] vorbisEncodingStrings = new String[]{"VORBISENC"};
                Encoding[] vorbisEncodings = new Encoding[vorbisEncodingStrings.length];
                for (i = 0; i < vorbisEncodings.length; i++) {
                    vorbisEncodings[i] = (Encoding) classVorbisEncoding.getDeclaredField(vorbisEncodingStrings[i]).get(null);
                }
                for (i = 0; i < vorbisEncodings.length; i++) {
                    if (encodingStr.equals(vorbisEncodings[i].toString())) {
                        return vorbisEncodings[i];
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
}
