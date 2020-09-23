package net.sf.fmj.media;

import javax.media.format.AudioFormat;

public class AudioFormatCompleter {
    public static AudioFormat complete(AudioFormat f) {
        if (f.getSampleSizeInBits() <= 8 || f.getEndian() != -1) {
            return f;
        }
        return new AudioFormat(f.getEncoding(), f.getSampleRate(), f.getSampleSizeInBits(), f.getChannels(), 1, f.getSigned(), f.getFrameSizeInBits(), f.getFrameRate(), f.getDataType());
    }
}
