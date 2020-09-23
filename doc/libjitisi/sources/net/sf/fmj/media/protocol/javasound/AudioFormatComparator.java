package net.sf.fmj.media.protocol.javasound;

import java.util.Comparator;
import javax.media.format.AudioFormat;

public class AudioFormatComparator implements Comparator<AudioFormat> {
    public int compare(AudioFormat a, AudioFormat b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        if (a.getSampleRate() > b.getSampleRate()) {
            return 1;
        }
        if (a.getSampleRate() < b.getSampleRate()) {
            return -1;
        }
        if (a.getChannels() > b.getChannels()) {
            return 1;
        }
        if (a.getChannels() < b.getChannels()) {
            return -1;
        }
        if (a.getSampleSizeInBits() > b.getSampleSizeInBits()) {
            return 1;
        }
        if (a.getSampleSizeInBits() < b.getSampleSizeInBits()) {
            return -1;
        }
        return 0;
    }
}
