package org.jitsi.util;

import java.io.File;
import java.util.Arrays;

public class SoundFileUtils {
    public static final String DEFAULT_CALL_RECORDING_FORMAT = "mp3";
    public static final String aif = "aiff";
    public static final String au = "au";
    public static final String gsm = "gsm";
    public static final String mid = "midi";
    public static final String mod = "mod";
    public static final String mp2 = "mp2";
    public static final String mp3 = "mp3";
    public static final String ogg = "ogg";
    public static final String ram = "ram";
    public static final String wav = "wav";
    public static final String wma = "wma";

    public static boolean isSoundFile(File f) {
        String ext = getExtension(f);
        if (ext == null) {
            return false;
        }
        if (ext.equals(wma) || ext.equals(wav) || ext.equals(ram) || ext.equals(ogg) || ext.equals("mp3") || ext.equals(mp2) || ext.equals(mod) || ext.equals(mid) || ext.equals("gsm") || ext.equals(au)) {
            return true;
        }
        return false;
    }

    public static boolean isSoundFile(File f, String[] soundFormats) {
        if (soundFormats == null) {
            return isSoundFile(f);
        }
        String ext = getExtension(f);
        if (ext == null || Arrays.binarySearch(soundFormats, ext, String.CASE_INSENSITIVE_ORDER) <= -1) {
            return false;
        }
        return true;
    }

    public static String getExtension(File f) {
        String s = f.getName();
        int i = s.lastIndexOf(46);
        if (i <= 0 || i >= s.length() - 1) {
            return null;
        }
        return s.substring(i + 1).toLowerCase();
    }
}
