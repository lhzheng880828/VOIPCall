package net.sf.fmj.utility;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.format.H261Format;
import javax.media.format.H263Format;
import javax.media.format.IndexedColorFormat;
import javax.media.format.JPEGFormat;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;
import net.sf.fmj.media.BonusAudioFormatEncodings;
import net.sf.fmj.media.BonusVideoFormatEncodings;
import net.sf.fmj.media.format.GIFFormat;
import net.sf.fmj.media.format.PNGFormat;
import org.jitsi.android.util.java.awt.Dimension;

public class FormatArgUtils {
    public static final String BIG_ENDIAN = "B";
    public static final String BYTE_ARRAY = "B";
    public static final String INT_ARRAY = "I";
    public static final String LITTLE_ENDIAN = "L";
    public static final String NOT_SPECIFIED = "?";
    private static final char SEP = ':';
    public static final String SHORT_ARRAY = "S";
    public static final String SIGNED = "S";
    public static final String UNSIGNED = "U";
    private static final Map<String, Class<?>> formatClasses = new HashMap();
    private static final Map<String, String> formatEncodings = new HashMap();

    private static class Tokens {
        private final String[] items;
        private int ix = 0;

        public Tokens(String[] items) {
            this.items = items;
        }

        public Class<?> nextDataType() throws ParseException {
            String s = nextString();
            if (s == null || s.equals(FormatArgUtils.NOT_SPECIFIED)) {
                return null;
            }
            s = s.toUpperCase();
            if (s.equals("B")) {
                return Format.byteArray;
            }
            if (s.equals("S")) {
                return Format.shortArray;
            }
            if (s.equals(FormatArgUtils.INT_ARRAY)) {
                return Format.intArray;
            }
            throw new ParseException("Expected one of [B,S,I]: " + s, -1);
        }

        public Dimension nextDimension() throws ParseException {
            String s = nextString();
            if (s == null || s.equals(FormatArgUtils.NOT_SPECIFIED)) {
                return null;
            }
            s = s.toUpperCase();
            String[] strings = s.split("X");
            if (strings.length != 2) {
                throw new ParseException("Expected WIDTHxHEIGHT: " + s, -1);
            }
            try {
                try {
                    return new Dimension(Integer.parseInt(strings[0]), Integer.parseInt(strings[1]));
                } catch (NumberFormatException e) {
                    throw new ParseException("Expected integer: " + strings[1], -1);
                }
            } catch (NumberFormatException e2) {
                throw new ParseException("Expected integer: " + strings[0], -1);
            }
        }

        public double nextDouble() throws ParseException {
            String s = nextString();
            if (s == null || s.equals(FormatArgUtils.NOT_SPECIFIED)) {
                return -1.0d;
            }
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                throw new ParseException("Expected double: " + s, -1);
            }
        }

        public int nextEndian() throws ParseException {
            String s = nextString();
            if (s == null || s.equals(FormatArgUtils.NOT_SPECIFIED)) {
                return -1;
            }
            s = s.toUpperCase();
            if (s.equals("B")) {
                return 1;
            }
            if (s.equals(FormatArgUtils.LITTLE_ENDIAN)) {
                return 0;
            }
            throw new ParseException("Expected one of [B,L]: " + s, -1);
        }

        public float nextFloat() throws ParseException {
            String s = nextString();
            if (s == null || s.equals(FormatArgUtils.NOT_SPECIFIED)) {
                return -1.0f;
            }
            try {
                return Float.parseFloat(s);
            } catch (NumberFormatException e) {
                throw new ParseException("Expected float: " + s, -1);
            }
        }

        public int nextInt() throws ParseException {
            int i = -1;
            String s = nextString();
            if (s == null || s.equals(FormatArgUtils.NOT_SPECIFIED)) {
                return i;
            }
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw new ParseException("Expected integer: " + s, i);
            }
        }

        public int nextRGBFormatEndian() throws ParseException {
            String s = nextString();
            if (s == null || s.equals(FormatArgUtils.NOT_SPECIFIED)) {
                return -1;
            }
            s = s.toUpperCase();
            if (s.equals("B")) {
                return 0;
            }
            if (s.equals(FormatArgUtils.LITTLE_ENDIAN)) {
                return 1;
            }
            throw new ParseException("Expected one of [B,L]: " + s, -1);
        }

        public int nextSigned() throws ParseException {
            String s = nextString();
            if (s == null || s.equals(FormatArgUtils.NOT_SPECIFIED)) {
                return -1;
            }
            s = s.toUpperCase();
            if (s.equals(FormatArgUtils.UNSIGNED)) {
                return 0;
            }
            if (s.equals("S")) {
                return 1;
            }
            throw new ParseException("Expected one of [U,U]: " + s, -1);
        }

        public String nextString() {
            return nextString(null);
        }

        public String nextString(String defaultResult) {
            if (this.ix >= this.items.length) {
                return defaultResult;
            }
            String result = this.items[this.ix];
            this.ix++;
            return result;
        }
    }

    static {
        buildFormatMap();
    }

    private static final void addAudioFormat(String s) {
        addFormat(s, AudioFormat.class);
    }

    private static final void addFormat(String s, Class<?> clazz) {
        formatClasses.put(s.toLowerCase(), clazz);
        formatEncodings.put(s.toLowerCase(), s);
    }

    private static final void addVideoFormat(String s) {
        addFormat(s, VideoFormat.class);
    }

    private static final void buildFormatMap() {
        addAudioFormat(AudioFormat.LINEAR);
        addAudioFormat(AudioFormat.ULAW);
        addAudioFormat(AudioFormat.ULAW_RTP);
        addAudioFormat(AudioFormat.ALAW);
        addAudioFormat(AudioFormat.IMA4);
        addAudioFormat(AudioFormat.IMA4_MS);
        addAudioFormat(AudioFormat.MSADPCM);
        addAudioFormat(AudioFormat.DVI);
        addAudioFormat(AudioFormat.DVI_RTP);
        addAudioFormat(AudioFormat.G723);
        addAudioFormat(AudioFormat.G723_RTP);
        addAudioFormat(AudioFormat.G728);
        addAudioFormat(AudioFormat.G728_RTP);
        addAudioFormat(AudioFormat.G729);
        addAudioFormat(AudioFormat.G729_RTP);
        addAudioFormat(AudioFormat.G729A);
        addAudioFormat(AudioFormat.G729A_RTP);
        addAudioFormat("gsm");
        addAudioFormat(AudioFormat.GSM_MS);
        addAudioFormat(AudioFormat.GSM_RTP);
        addAudioFormat(AudioFormat.MAC3);
        addAudioFormat(AudioFormat.MAC6);
        addAudioFormat(AudioFormat.TRUESPEECH);
        addAudioFormat(AudioFormat.MSNAUDIO);
        addAudioFormat(AudioFormat.MPEGLAYER3);
        addAudioFormat(AudioFormat.VOXWAREAC8);
        addAudioFormat(AudioFormat.VOXWAREAC10);
        addAudioFormat(AudioFormat.VOXWAREAC16);
        addAudioFormat(AudioFormat.VOXWAREAC20);
        addAudioFormat(AudioFormat.VOXWAREMETAVOICE);
        addAudioFormat(AudioFormat.VOXWAREMETASOUND);
        addAudioFormat(AudioFormat.VOXWARERT29H);
        addAudioFormat(AudioFormat.VOXWAREVR12);
        addAudioFormat(AudioFormat.VOXWAREVR18);
        addAudioFormat(AudioFormat.VOXWARETQ40);
        addAudioFormat(AudioFormat.VOXWARETQ60);
        addAudioFormat(AudioFormat.MSRT24);
        addAudioFormat(AudioFormat.MPEG);
        addAudioFormat(AudioFormat.MPEG_RTP);
        addAudioFormat(AudioFormat.DOLBYAC3);
        for (String e : BonusAudioFormatEncodings.ALL) {
            addAudioFormat(e);
        }
        addVideoFormat(VideoFormat.CINEPAK);
        addFormat(VideoFormat.JPEG, JPEGFormat.class);
        addVideoFormat(VideoFormat.JPEG_RTP);
        addVideoFormat(VideoFormat.MPEG);
        addVideoFormat(VideoFormat.MPEG_RTP);
        addFormat(VideoFormat.H261, H261Format.class);
        addVideoFormat(VideoFormat.H261_RTP);
        addFormat(VideoFormat.H263, H263Format.class);
        addVideoFormat(VideoFormat.H263_RTP);
        addVideoFormat("h263-1998/rtp");
        addFormat(VideoFormat.RGB, RGBFormat.class);
        addFormat(VideoFormat.YUV, YUVFormat.class);
        addFormat(VideoFormat.IRGB, IndexedColorFormat.class);
        addVideoFormat(VideoFormat.SMC);
        addVideoFormat(VideoFormat.RLE);
        addVideoFormat(VideoFormat.RPZA);
        addVideoFormat(VideoFormat.MJPG);
        addVideoFormat(VideoFormat.MJPEGA);
        addVideoFormat(VideoFormat.MJPEGB);
        addVideoFormat(VideoFormat.INDEO32);
        addVideoFormat(VideoFormat.INDEO41);
        addVideoFormat(VideoFormat.INDEO50);
        addFormat(BonusVideoFormatEncodings.GIF, GIFFormat.class);
        addFormat(BonusVideoFormatEncodings.PNG, PNGFormat.class);
    }

    private static final String dataTypeToStr(Class<?> clazz) {
        if (clazz == null) {
            return NOT_SPECIFIED;
        }
        if (clazz == Format.byteArray) {
            return "B";
        }
        if (clazz == Format.shortArray) {
            return "S";
        }
        if (clazz == Format.intArray) {
            return INT_ARRAY;
        }
        throw new IllegalArgumentException("" + clazz);
    }

    private static final String dimensionToStr(Dimension d) {
        if (d == null) {
            return NOT_SPECIFIED;
        }
        return ((int) d.getWidth()) + "x" + ((int) d.getHeight());
    }

    private static final String endianToStr(int endian) {
        if (endian == -1) {
            return NOT_SPECIFIED;
        }
        if (endian == 1) {
            return "B";
        }
        if (endian == 0) {
            return LITTLE_ENDIAN;
        }
        throw new IllegalArgumentException("Unknown endianness: " + endian);
    }

    private static final String floatToStr(float v) {
        if (v == -1.0f) {
            return NOT_SPECIFIED;
        }
        return "" + v;
    }

    private static final String intToStr(int i) {
        if (i == -1) {
            return NOT_SPECIFIED;
        }
        return "" + i;
    }

    public static Format parse(String s) throws ParseException {
        Tokens tokens = new Tokens(s.split(":"));
        String encodingIgnoreCase = tokens.nextString(null);
        if (encodingIgnoreCase == null) {
            throw new ParseException("No encoding specified", 0);
        }
        Class<?> formatClass = (Class) formatClasses.get(encodingIgnoreCase.toLowerCase());
        if (formatClass == null) {
            throw new ParseException("Unknown encoding: " + encodingIgnoreCase, -1);
        }
        String encoding = (String) formatEncodings.get(encodingIgnoreCase.toLowerCase());
        int endian;
        Class<?> dataType;
        Dimension size;
        int maxDataLength;
        if (encoding == null) {
            throw new ParseException("Unknown encoding: " + encodingIgnoreCase, -1);
        } else if (AudioFormat.class.isAssignableFrom(formatClass)) {
            double sampleRate = tokens.nextDouble();
            int sampleSizeInBits = tokens.nextInt();
            int channels = tokens.nextInt();
            endian = tokens.nextEndian();
            int signed = tokens.nextSigned();
            int frameSizeInBits = tokens.nextInt();
            double frameRate = tokens.nextDouble();
            dataType = tokens.nextDataType();
            if (dataType == null) {
                dataType = Format.byteArray;
            }
            return new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels, endian, signed, frameSizeInBits, frameRate, dataType);
        } else if (!VideoFormat.class.isAssignableFrom(formatClass)) {
            throw new RuntimeException("Unknown class: " + formatClass);
        } else if (formatClass == JPEGFormat.class) {
            size = tokens.nextDimension();
            maxDataLength = tokens.nextInt();
            dataType = tokens.nextDataType();
            if (dataType == null) {
                dataType = Format.byteArray;
            }
            return new JPEGFormat(size, maxDataLength, dataType, tokens.nextFloat(), -1, -1);
        } else if (formatClass == GIFFormat.class) {
            size = tokens.nextDimension();
            maxDataLength = tokens.nextInt();
            dataType = tokens.nextDataType();
            if (dataType == null) {
                dataType = Format.byteArray;
            }
            return new GIFFormat(size, maxDataLength, dataType, tokens.nextFloat());
        } else if (formatClass == PNGFormat.class) {
            size = tokens.nextDimension();
            maxDataLength = tokens.nextInt();
            dataType = tokens.nextDataType();
            if (dataType == null) {
                dataType = Format.byteArray;
            }
            return new PNGFormat(size, maxDataLength, dataType, tokens.nextFloat());
        } else if (formatClass == VideoFormat.class) {
            size = tokens.nextDimension();
            maxDataLength = tokens.nextInt();
            dataType = tokens.nextDataType();
            if (dataType == null) {
                dataType = Format.byteArray;
            }
            return new VideoFormat(encoding, size, maxDataLength, dataType, tokens.nextFloat());
        } else if (formatClass == RGBFormat.class) {
            size = tokens.nextDimension();
            maxDataLength = tokens.nextInt();
            dataType = tokens.nextDataType();
            if (dataType == null) {
                dataType = Format.byteArray;
            }
            float frameRate2 = tokens.nextFloat();
            int bitsPerPixel = tokens.nextInt();
            int red = tokens.nextInt();
            int green = tokens.nextInt();
            int blue = tokens.nextInt();
            int pixelStride = tokens.nextInt();
            int lineStride = tokens.nextInt();
            int flipped = tokens.nextInt();
            endian = tokens.nextRGBFormatEndian();
            if (pixelStride == -1 && lineStride == -1 && flipped == -1 && endian == -1) {
                return new RGBFormat(size, maxDataLength, dataType, frameRate2, bitsPerPixel, red, green, blue);
            }
            return new RGBFormat(size, maxDataLength, dataType, frameRate2, bitsPerPixel, red, green, blue, pixelStride, lineStride, flipped, endian);
        } else {
            throw new RuntimeException("TODO: Unknown class: " + formatClass);
        }
    }

    private static final String rgbFormatEndianToStr(int endian) {
        if (endian == -1) {
            return NOT_SPECIFIED;
        }
        if (endian == 0) {
            return "B";
        }
        if (endian == 1) {
            return LITTLE_ENDIAN;
        }
        throw new IllegalArgumentException("Unknown endianness: " + endian);
    }

    private static final String signedToStr(int signed) {
        if (signed == -1) {
            return NOT_SPECIFIED;
        }
        if (signed == 1) {
            return "S";
        }
        if (signed == 0) {
            return UNSIGNED;
        }
        throw new IllegalArgumentException("Unknown signedness: " + signed);
    }

    public static String toString(Format f) {
        List<String> list = new ArrayList();
        list.add(f.getEncoding().toUpperCase());
        if (f instanceof AudioFormat) {
            AudioFormat af = (AudioFormat) f;
            list.add(intToStr((int) af.getSampleRate()));
            list.add(intToStr(af.getSampleSizeInBits()));
            list.add(intToStr(af.getChannels()));
            list.add(endianToStr(af.getEndian()));
            list.add(signedToStr(af.getSigned()));
            list.add(intToStr(af.getFrameSizeInBits()));
            list.add(intToStr((int) af.getFrameRate()));
            if (!(af.getDataType() == null || af.getDataType() == Format.byteArray)) {
                list.add(dataTypeToStr(af.getDataType()));
            }
        } else if (f instanceof VideoFormat) {
            VideoFormat vf = (VideoFormat) f;
            if (f.getClass() == JPEGFormat.class) {
                JPEGFormat jf = (JPEGFormat) vf;
                list.add(dimensionToStr(jf.getSize()));
                list.add(intToStr(jf.getMaxDataLength()));
                if (!(jf.getDataType() == null || jf.getDataType() == Format.byteArray)) {
                    list.add(dataTypeToStr(jf.getDataType()));
                }
                list.add(floatToStr(jf.getFrameRate()));
            } else if (f.getClass() == GIFFormat.class) {
                GIFFormat gf = (GIFFormat) vf;
                list.add(dimensionToStr(gf.getSize()));
                list.add(intToStr(gf.getMaxDataLength()));
                if (!(gf.getDataType() == null || gf.getDataType() == Format.byteArray)) {
                    list.add(dataTypeToStr(gf.getDataType()));
                }
                list.add(floatToStr(gf.getFrameRate()));
            } else if (f.getClass() == PNGFormat.class) {
                PNGFormat pf = (PNGFormat) vf;
                list.add(dimensionToStr(pf.getSize()));
                list.add(intToStr(pf.getMaxDataLength()));
                if (!(pf.getDataType() == null || pf.getDataType() == Format.byteArray)) {
                    list.add(dataTypeToStr(pf.getDataType()));
                }
                list.add(floatToStr(pf.getFrameRate()));
            } else if (f.getClass() == VideoFormat.class) {
                list.add(dimensionToStr(vf.getSize()));
                list.add(intToStr(vf.getMaxDataLength()));
                if (!(vf.getDataType() == null || vf.getDataType() == Format.byteArray)) {
                    list.add(dataTypeToStr(vf.getDataType()));
                }
                list.add(floatToStr(vf.getFrameRate()));
            } else if (f.getClass() == RGBFormat.class) {
                RGBFormat rf = (RGBFormat) vf;
                list.add(dimensionToStr(vf.getSize()));
                list.add(intToStr(vf.getMaxDataLength()));
                if (!(vf.getDataType() == null || vf.getDataType() == Format.byteArray)) {
                    list.add(dataTypeToStr(vf.getDataType()));
                }
                list.add(floatToStr(vf.getFrameRate()));
                list.add(intToStr(rf.getBitsPerPixel()));
                list.add(intToStr(rf.getRedMask()));
                list.add(intToStr(rf.getGreenMask()));
                list.add(intToStr(rf.getBlueMask()));
                list.add(intToStr(rf.getPixelStride()));
                list.add(intToStr(rf.getLineStride()));
                list.add(intToStr(rf.getFlipped()));
                list.add(rgbFormatEndianToStr(rf.getEndian()));
            } else {
                throw new IllegalArgumentException("Unknown or unsupported format: " + f);
            }
        } else {
            throw new IllegalArgumentException("" + f);
        }
        while (true) {
            if (list.get(list.size() - 1) != null && !((String) list.get(list.size() - 1)).equals(NOT_SPECIFIED)) {
                break;
            }
            list.remove(list.size() - 1);
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                b.append(SEP);
            }
            b.append((String) list.get(i));
        }
        return b.toString();
    }
}
