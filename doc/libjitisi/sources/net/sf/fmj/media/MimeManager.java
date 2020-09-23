package net.sf.fmj.media;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import net.sf.fmj.registry.Registry;
import net.sf.fmj.utility.LoggerSingleton;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.util.SoundFileUtils;

public class MimeManager {
    private static final MimeTable defaultMimeTable = new MimeTable();
    private static final Logger logger = LoggerSingleton.logger;

    static {
        put("mvr", "application/mvr");
        put("aif", "audio/x_aiff");
        put(SoundFileUtils.aif, "audio/x_aiff");
        put(SoundFileUtils.mid, "audio/midi");
        put("jmx", "application/x_jmx");
        put("mpv", "video/mpeg");
        put("mpg", "video/mpeg");
        put(SoundFileUtils.wav, "audio/x_wav");
        put("mp3", "audio/mpeg");
        put("mpa", "audio/mpeg");
        put(SoundFileUtils.mp2, "audio/mpeg");
        put("spl", "application/futuresplash");
        put("viv", "video/vivo");
        put(SoundFileUtils.au, "audio/basic");
        put(AudioFormat.G729, "audio/g729");
        put("mov", "video/quicktime");
        put("avi", "video/x_msvideo");
        put(AudioFormat.G728, "audio/g728");
        put("cda", "audio/cdaudio");
        put(AudioFormat.G729A, "audio/g729a");
        put("gsm", "audio/x_gsm");
        put("mid", "audio/midi");
        put("swf", "application/x-shockwave-flash");
        put("rmf", "audio/rmf");
        boolean jmfDefaults = false;
        try {
            jmfDefaults = System.getProperty("net.sf.fmj.utility.JmfRegistry.JMFDefaults", "false").equals("true");
        } catch (SecurityException e) {
        }
        if (!jmfDefaults) {
            put(SoundFileUtils.ogg, "audio/ogg");
            put("ogx", "application/ogg");
            put("oga", "audio/ogg");
            put("ogv", "video/ogg");
            put("spx", "audio/ogg");
            put("flac", "application/flac");
            put("anx", "application/annodex");
            put("axa", "audio/annodex");
            put("axv", "video/annodex");
            put("xspf", "application/xspf+xml ");
            put("asf", "video/x-ms-asf");
            put("asx", "video/x-ms-asf");
            put(SoundFileUtils.wma, "audio/x-ms-wma");
            put("wax", "audio/x-ms-wax");
            put("wmv", "video/x-ms-wmv");
            put("wvx", "video/x-ms-wvx");
            put("wm", "video/x-ms-wm");
            put("wmx", "video/x-ms-wmx");
            put("wmz", "application/x-ms-wmz");
            put("wmd", "application/x-ms-wmd");
            put("mpeg4", "video/mpeg");
            put("mp4", "video/mpeg");
            put("3gp", "video/3gpp");
            put("3g2", "video/3gpp");
            put(Constants.H264, "video/mp4");
            put("m4v", "video/mp4v");
            put("m2v", "video/mp2p");
            put("vob", "video/mp2p");
            put("ts", "video/x-mpegts");
            put(VideoFormat.MPEG, "video/mpeg");
            put("m1v", "video/mpeg");
            put(VideoFormat.MJPG, "video/x-mjpeg");
            put("mjpeg", "video/x-mjpeg");
            put("flv", "video/x-flv");
            put("fli", "video/fli");
            put("flc", "video/flc");
            put("flx", "video/flc");
            put("mkv", "video/x-matroska");
            put("mka", "audio/x-matroska");
            put("mpc", "audio/x-musepack");
            put("mp+", "audio/x-musepack");
            put("mpp", "audio/x-musepack");
            put("rm", "application/vnd.rn-realmedia");
            put("ra", "application/vnd.rn-realmedia");
            put("dv", "video/x-dv");
            put("dif", "video/x-dv");
            put("aac", "audio/X-HX-AAC-ADTS");
            put("mj2", "video/mj2");
            put("mjp2", "video/mj2");
            put("mtv", "video/x-amv");
            put("amv", "video/x-amv");
            put("nsv", "application/x-nsv-vp3-mp3");
            put("nuv", "video/x-nuv");
            put("nuv", "application/mxf");
            put("shn", "application/x-shorten");
            put("tta", "audio/x-tta");
            put("voc", "audio/x-voc");
            put("wv", "audio/x-wavpack");
            put("wvp", "audio/x-wavpack");
            put("4xm", "video/x-4xm");
            put("aud", "video/x-wsaud");
            put("apc", "audio/x-apc");
            put("avs", "video/x-avs");
            put("c93", "video/x-c93");
            put("cin", "video/x-dsicin");
            put("cin", "video/x-idcin");
            put("cpk", "video/x-film-cpk");
            put("dts", "audio/x-raw-dts");
            put("dxa", "video/x-dxa");
            put("gxf", "video/x-gxf");
            put("mm", "video/x-mm");
            put("mve", "video/x-wc3-movie");
            put("mve", "video/x-mve");
            put("roq", "video/x-roq");
            put("seq", "video/x-seq");
            put("smk", "video/x-smk");
            put("sol", "audio/x-sol");
            put("str", "audio/x-psxstr");
            put("thp", "video/x-thp");
            put("txd", "video/x-txd");
            put("uv2", "video/x-ea");
            put("vc1", "video/x-raw-vc1");
            put("vid", "video/x-bethsoft-vid");
            put("vmd", "video/x-vmd");
            put("vqa", "video/x-wsvqa");
            put("wve", "video/x-ea");
            put(VideoFormat.YUV, "video/x-raw-yuv");
            put("mmr", "multipart/x-mixed-replace");
            put("xmv", "video/xml");
        }
    }

    public static final boolean addMimeType(String fileExtension, String mimeType) {
        fileExtension = nullSafeToLowerCase(fileExtension);
        mimeType = nullSafeToLowerCase(mimeType);
        if (defaultMimeTable.getMimeType(fileExtension) != null) {
            logger.warning("Cannot override default mime-table entries");
            return false;
        }
        Registry.getInstance().addMimeType(fileExtension, mimeType);
        return true;
    }

    public static void commit() {
        try {
            Registry.getInstance().commit();
        } catch (IOException e) {
            logger.log(Level.WARNING, "" + e, e);
        }
    }

    public static final String getDefaultExtension(String mimeType) {
        mimeType = nullSafeToLowerCase(mimeType);
        String result = Registry.getInstance().getDefaultExtension(mimeType);
        return result != null ? result : defaultMimeTable.getDefaultExtension(mimeType);
    }

    public static final Hashtable getDefaultMimeTable() {
        return defaultMimeTable.getMimeTable();
    }

    public static final List<String> getExtensions(String mimeType) {
        mimeType = nullSafeToLowerCase(mimeType);
        List<String> result = new ArrayList();
        result.addAll(defaultMimeTable.getExtensions(mimeType));
        result.addAll(Registry.getInstance().getExtensions(mimeType));
        return result;
    }

    public static final Hashtable<String, String> getMimeTable() {
        Hashtable<String, String> result = new Hashtable();
        result.putAll(defaultMimeTable.getMimeTable());
        result.putAll(Registry.getInstance().getMimeTable());
        return result;
    }

    public static final String getMimeType(String fileExtension) {
        fileExtension = nullSafeToLowerCase(fileExtension);
        String result = Registry.getInstance().getMimeType(fileExtension);
        if (result != null) {
            return result;
        }
        result = defaultMimeTable.getMimeType(fileExtension);
        String str = result;
        return result;
    }

    private static final String nullSafeToLowerCase(String s) {
        return s == null ? s : s.toLowerCase();
    }

    private static void put(String ext, String type) {
        defaultMimeTable.addMimeType(nullSafeToLowerCase(ext), nullSafeToLowerCase(type));
    }

    public static final boolean removeMimeType(String fileExtension) {
        return Registry.getInstance().removeMimeType(nullSafeToLowerCase(fileExtension));
    }

    protected MimeManager() {
    }
}
