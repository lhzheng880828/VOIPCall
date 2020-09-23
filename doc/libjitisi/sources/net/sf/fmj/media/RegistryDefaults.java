package net.sf.fmj.media;

import com.lti.utils.OSUtils;
import com.lti.utils.UnsignedUtils;
import com.sun.media.format.WavAudioFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.media.Format;
import javax.media.PackageManager;
import javax.media.format.AudioFormat;
import javax.media.format.JPEGFormat;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;
import javax.media.pim.PlugInManager;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;
import net.sf.fmj.registry.Registry;
import net.sf.fmj.utility.PlugInInfo;
import net.sf.fmj.utility.PlugInUtility;

public class RegistryDefaults {
    public static final int ALL = 15;
    public static boolean DISABLE_OGG = ENABLE_GSTREAMER;
    private static final boolean ENABLE_GSTREAMER = false;
    public static final int FMJ = 2;
    public static final int FMJ_NATIVE = 8;
    public static final int JMF = 1;
    public static final int NONE = 0;
    public static final int THIRD_PARTY = 4;
    private static int defaultFlags = -1;

    public static List<String> contentPrefixList(int flags) {
        List<String> contentPrefixList = new ArrayList();
        if ((flags & 1) != 0) {
            contentPrefixList.add("javax");
            contentPrefixList.add("com.sun");
            contentPrefixList.add("com.ibm");
        }
        if ((flags & 8) != 0) {
            if (OSUtils.isMacOSX() || OSUtils.isWindows()) {
                contentPrefixList.add("net.sf.fmj.qt");
            }
            if (OSUtils.isWindows()) {
                contentPrefixList.add("net.sf.fmj.ds");
            }
        }
        if ((flags & 2) != 0) {
            contentPrefixList.add("net.sf.fmj");
        }
        if ((flags & 4) != 0) {
        }
        return contentPrefixList;
    }

    public static final int getDefaultFlags() {
        if (defaultFlags == -1) {
            boolean jmfDefaults = ENABLE_GSTREAMER;
            try {
                jmfDefaults = System.getProperty("net.sf.fmj.utility.JmfRegistry.JMFDefaults", "false").equals("true");
            } catch (SecurityException e) {
            }
            defaultFlags = jmfDefaults ? 1 : 15;
        }
        return defaultFlags;
    }

    public static List<Object> plugInList(int flags) {
        List<Object> result = new ArrayList();
        if (flags != 0) {
            List<Object> list;
            if ((flags & 1) != 0) {
                list = result;
                list.add(new PlugInInfo("com.ibm.media.parser.video.MpegParser", new Format[]{new ContentDescriptor(FileTypeDescriptor.MPEG_AUDIO), new ContentDescriptor(FileTypeDescriptor.MPEG), new ContentDescriptor(FileTypeDescriptor.MPEG_AUDIO)}, new Format[0], 1));
                list = result;
                list.add(new PlugInInfo("com.sun.media.parser.audio.WavParser", new Format[]{new ContentDescriptor(FileTypeDescriptor.WAVE)}, new Format[0], 1));
                list = result;
                list.add(new PlugInInfo("com.sun.media.parser.audio.AuParser", new Format[]{new ContentDescriptor(FileTypeDescriptor.BASIC_AUDIO)}, new Format[0], 1));
                list = result;
                list.add(new PlugInInfo("com.sun.media.parser.audio.AiffParser", new Format[]{new ContentDescriptor(FileTypeDescriptor.AIFF)}, new Format[0], 1));
                list = result;
                list.add(new PlugInInfo("com.sun.media.parser.audio.GsmParser", new Format[]{new ContentDescriptor(FileTypeDescriptor.GSM)}, new Format[0], 1));
            }
            if ((flags & 2) != 0) {
                list = result;
                list.add(new PlugInInfo("net.sf.fmj.media.parser.RawPushBufferParser", new Format[]{new ContentDescriptor(ContentDescriptor.RAW)}, new Format[0], 1));
            }
            if ((flags & 1) != 0) {
                list = result;
                list.add(new PlugInInfo("com.sun.media.parser.RawStreamParser", new Format[]{new ContentDescriptor(ContentDescriptor.RAW)}, new Format[0], 1));
                list = result;
                list.add(new PlugInInfo("com.sun.media.parser.RawBufferParser", new Format[]{new ContentDescriptor(ContentDescriptor.RAW)}, new Format[0], 1));
                list = result;
                list.add(new PlugInInfo("com.sun.media.parser.RawPullStreamParser", new Format[]{new ContentDescriptor(ContentDescriptor.RAW)}, new Format[0], 1));
                list = result;
                list.add(new PlugInInfo("com.sun.media.parser.RawPullBufferParser", new Format[]{new ContentDescriptor(ContentDescriptor.RAW)}, new Format[0], 1));
                list = result;
                list.add(new PlugInInfo("com.sun.media.parser.video.QuicktimeParser", new Format[]{new ContentDescriptor(FileTypeDescriptor.QUICKTIME)}, new Format[0], 1));
                list = result;
                list.add(new PlugInInfo("com.sun.media.parser.video.AviParser", new Format[]{new ContentDescriptor(FileTypeDescriptor.MSVIDEO)}, new Format[0], 1));
                result.add(new PlugInInfo("com.sun.media.codec.audio.mpa.JavaDecoder", new Format[]{new AudioFormat(AudioFormat.MPEG, 16000.0d, -1, -1, -1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 22050.0d, -1, -1, -1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 24000.0d, -1, -1, -1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 32000.0d, -1, -1, -1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 44100.0d, -1, -1, -1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 48000.0d, -1, -1, -1, 1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.sun.media.codec.video.cinepak.JavaDecoder", new Format[]{new VideoFormat(VideoFormat.CINEPAK, null, -1, Format.byteArray, -1.0f)}, new Format[]{new RGBFormat(null, -1, Format.intArray, -1.0f, 32, UnsignedUtils.MAX_UBYTE, 65280, 16711680, 1, -1, 0, -1)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.video.h263.JavaDecoder", new Format[]{new VideoFormat(VideoFormat.H263, null, -1, Format.byteArray, -1.0f), new VideoFormat(VideoFormat.H263_RTP, null, -1, Format.byteArray, -1.0f)}, new Format[]{new RGBFormat(null, -1, null, -1.0f, -1, -1, -1, -1, -1, -1, -1, -1)}, 2));
                result.add(new PlugInInfo("com.sun.media.codec.video.colorspace.JavaRGBConverter", new Format[]{new RGBFormat(null, -1, null, -1.0f, -1, -1, -1, -1, -1, -1, -1, -1)}, new Format[]{new RGBFormat(null, -1, null, -1.0f, -1, -1, -1, -1, -1, -1, -1, -1)}, 2));
                result.add(new PlugInInfo("com.sun.media.codec.video.colorspace.JavaRGBToYUV", new Format[]{new RGBFormat(null, -1, Format.byteArray, -1.0f, 24, -1, -1, -1, -1, -1, -1, -1), new RGBFormat(null, -1, Format.intArray, -1.0f, 32, 16711680, 65280, UnsignedUtils.MAX_UBYTE, 1, -1, -1, -1), new RGBFormat(null, -1, Format.intArray, -1.0f, 32, UnsignedUtils.MAX_UBYTE, 65280, 16711680, 1, -1, -1, -1)}, new Format[]{new YUVFormat(null, -1, Format.byteArray, -1.0f, 2, -1, -1, -1, -1, -1)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.audio.PCMToPCM", new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, 1, -1, -1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, 2, -1, -1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.LINEAR, -1.0d, 8, 1, -1, -1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.LINEAR, -1.0d, 8, 2, -1, -1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.audio.rc.RCModule", new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, 2, -1, -1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, 1, -1, -1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.LINEAR, -1.0d, 8, 2, -1, -1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.LINEAR, -1.0d, 8, 1, -1, -1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.LINEAR, 8000.0d, 16, 2, 0, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.LINEAR, 8000.0d, 16, 1, 0, 1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.sun.media.codec.audio.rc.RateCvrt", new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.sun.media.codec.audio.msadpcm.JavaDecoder", new Format[]{new AudioFormat(AudioFormat.MSADPCM, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.audio.ulaw.JavaDecoder", new Format[]{new AudioFormat(AudioFormat.ULAW, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.audio.alaw.JavaDecoder", new Format[]{new AudioFormat(AudioFormat.ALAW, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.audio.dvi.JavaDecoder", new Format[]{new AudioFormat(AudioFormat.DVI_RTP, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.audio.g723.JavaDecoder", new Format[]{new AudioFormat(AudioFormat.G723, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.G723_RTP, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.audio.gsm.JavaDecoder", new Format[]{new AudioFormat("gsm", -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.GSM_RTP, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.audio.gsm.JavaDecoder_ms", new Format[]{new AudioFormat(AudioFormat.GSM_MS, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.audio.ima4.JavaDecoder", new Format[]{new AudioFormat(AudioFormat.IMA4, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.audio.ima4.JavaDecoder_ms", new Format[]{new AudioFormat(AudioFormat.IMA4_MS, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.audio.ulaw.JavaEncoder", new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, 1, -1, -1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, 2, -1, -1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.LINEAR, -1.0d, 8, 1, -1, -1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.LINEAR, -1.0d, 8, 2, -1, -1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.ULAW, 8000.0d, 8, 1, -1, -1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.audio.dvi.JavaEncoder", new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, 1, 0, 1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.DVI_RTP, -1.0d, 4, 1, -1, -1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.audio.gsm.JavaEncoder", new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, 1, 0, 1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat("gsm", -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.audio.gsm.JavaEncoder_ms", new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, 1, 0, 1, -1, -1.0d, Format.byteArray)}, new Format[]{new WavAudioFormat(AudioFormat.GSM_MS, -1.0d, -1, -1, -1, -1, -1, -1, -1.0f, Format.byteArray, null)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.audio.ima4.JavaEncoder", new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, -1, 0, 1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.IMA4, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.audio.ima4.JavaEncoder_ms", new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, -1, 0, 1, -1, -1.0d, Format.byteArray)}, new Format[]{new WavAudioFormat(AudioFormat.IMA4_MS, -1.0d, -1, -1, -1, -1, -1, -1, -1.0f, Format.byteArray, null)}, 2));
                result.add(new PlugInInfo("com.sun.media.codec.audio.ulaw.Packetizer", new Format[]{new AudioFormat(AudioFormat.ULAW, -1.0d, 8, 1, -1, -1, 8, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.ULAW_RTP, -1.0d, 8, 1, -1, -1, 8, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.sun.media.codec.audio.ulaw.DePacketizer", new Format[]{new AudioFormat(AudioFormat.ULAW_RTP, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.ULAW, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.sun.media.codec.audio.mpa.Packetizer", new Format[]{new AudioFormat(AudioFormat.MPEGLAYER3, 16000.0d, -1, -1, -1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEGLAYER3, 22050.0d, -1, -1, -1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEGLAYER3, 24000.0d, -1, -1, -1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEGLAYER3, 32000.0d, -1, -1, -1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEGLAYER3, 44100.0d, -1, -1, -1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEGLAYER3, 48000.0d, -1, -1, -1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 16000.0d, -1, -1, -1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 22050.0d, -1, -1, -1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 24000.0d, -1, -1, -1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 32000.0d, -1, -1, -1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 44100.0d, -1, -1, -1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 48000.0d, -1, -1, -1, 1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.MPEG_RTP, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.sun.media.codec.audio.mpa.DePacketizer", new Format[]{new AudioFormat(AudioFormat.MPEG_RTP, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.MPEG, 44100.0d, 16, -1, 1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 48000.0d, 16, -1, 1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 32000.0d, 16, -1, 1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 22050.0d, 16, -1, 1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 24000.0d, 16, -1, 1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 16000.0d, 16, -1, 1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 11025.0d, 16, -1, 1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 12000.0d, 16, -1, 1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEG, 8000.0d, 16, -1, 1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEGLAYER3, 44100.0d, 16, -1, 1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEGLAYER3, 48000.0d, 16, -1, 1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEGLAYER3, 32000.0d, 16, -1, 1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEGLAYER3, 22050.0d, 16, -1, 1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEGLAYER3, 24000.0d, 16, -1, 1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEGLAYER3, 16000.0d, 16, -1, 1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEGLAYER3, 11025.0d, 16, -1, 1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEGLAYER3, 12000.0d, 16, -1, 1, 1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.MPEGLAYER3, 8000.0d, 16, -1, 1, 1, -1, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.audio.gsm.Packetizer", new Format[]{new AudioFormat("gsm", 8000.0d, -1, 1, -1, -1, 264, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.GSM_RTP, 8000.0d, -1, 1, -1, -1, 264, -1.0d, Format.byteArray)}, 2));
                result.add(new PlugInInfo("com.ibm.media.codec.audio.g723.Packetizer", new Format[]{new AudioFormat(AudioFormat.G723, 8000.0d, -1, 1, -1, -1, 192, -1.0d, Format.byteArray)}, new Format[]{new AudioFormat(AudioFormat.G723_RTP, 8000.0d, -1, 1, -1, -1, 192, -1.0d, Format.byteArray)}, 2));
            }
            if ((flags & 1) != 0) {
                list = result;
                list.add(new PlugInInfo("com.sun.media.codec.video.jpeg.Packetizer", new Format[]{new JPEGFormat()}, new Format[]{new VideoFormat(VideoFormat.JPEG_RTP, null, -1, Format.byteArray, -1.0f)}, 2));
            }
            if ((flags & 2) != 0) {
                list = result;
                list.add(new PlugInInfo("net.sf.fmj.media.codec.video.jpeg.DePacketizer", new Format[]{new VideoFormat(VideoFormat.JPEG_RTP, null, -1, Format.byteArray, -1.0f)}, new Format[]{new JPEGFormat()}, 2));
            }
            if ((flags & 1) != 0) {
                list = result;
                list.add(new PlugInInfo("com.sun.media.codec.video.jpeg.DePacketizer", new Format[]{new VideoFormat(VideoFormat.JPEG_RTP, null, -1, Format.byteArray, -1.0f)}, new Format[]{new JPEGFormat()}, 2));
                list = result;
                list.add(new PlugInInfo("com.sun.media.codec.video.mpeg.Packetizer", new Format[]{new VideoFormat(VideoFormat.MPEG, null, -1, Format.byteArray, -1.0f)}, new Format[]{new VideoFormat(VideoFormat.MPEG_RTP, null, -1, Format.byteArray, -1.0f)}, 2));
                list = result;
                list.add(new PlugInInfo("com.sun.media.codec.video.mpeg.DePacketizer", new Format[]{new VideoFormat(VideoFormat.MPEG_RTP, null, -1, Format.byteArray, -1.0f)}, new Format[]{new VideoFormat(VideoFormat.MPEG, null, -1, Format.byteArray, -1.0f)}, 2));
            }
            if ((flags & 1) != 0) {
                result.add(new PlugInInfo("com.sun.media.renderer.audio.JavaSoundRenderer", new Format[]{new AudioFormat(AudioFormat.LINEAR, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray), new AudioFormat(AudioFormat.ULAW, -1.0d, -1, -1, -1, -1, -1, -1.0d, Format.byteArray)}, new Format[0], 4));
                result.add(new PlugInInfo("com.sun.media.renderer.audio.SunAudioRenderer", new Format[]{new AudioFormat(AudioFormat.ULAW, 8000.0d, 8, 1, -1, -1, -1, -1.0d, Format.byteArray)}, new Format[0], 4));
                result.add(new PlugInInfo("com.sun.media.renderer.video.AWTRenderer", new Format[]{new RGBFormat(null, -1, Format.intArray, -1.0f, 32, 16711680, 65280, UnsignedUtils.MAX_UBYTE, 1, -1, 0, -1), new RGBFormat(null, -1, Format.intArray, -1.0f, 32, UnsignedUtils.MAX_UBYTE, 65280, 16711680, 1, -1, 0, -1)}, new Format[0], 4));
                result.add(new PlugInInfo("com.sun.media.renderer.video.LightWeightRenderer", new Format[]{new RGBFormat(null, -1, Format.intArray, -1.0f, 32, 16711680, 65280, UnsignedUtils.MAX_UBYTE, 1, -1, 0, -1), new RGBFormat(null, -1, Format.intArray, -1.0f, 32, UnsignedUtils.MAX_UBYTE, 65280, 16711680, 1, -1, 0, -1)}, new Format[0], 4));
                list = result;
                list.add(new PlugInInfo("com.sun.media.renderer.video.JPEGRenderer", new Format[]{new JPEGFormat()}, new Format[0], 4));
                list = result;
                list.add(new PlugInInfo("com.sun.media.multiplexer.RawBufferMux", new Format[0], new Format[]{new ContentDescriptor(ContentDescriptor.RAW)}, 5));
                list = result;
                list.add(new PlugInInfo("com.sun.media.multiplexer.RawSyncBufferMux", new Format[0], new Format[]{new ContentDescriptor(ContentDescriptor.RAW)}, 5));
                list = result;
                list.add(new PlugInInfo("com.sun.media.multiplexer.RTPSyncBufferMux", new Format[0], new Format[]{new ContentDescriptor(ContentDescriptor.RAW_RTP)}, 5));
                list = result;
                list.add(new PlugInInfo("com.sun.media.multiplexer.audio.GSMMux", new Format[0], new Format[]{new FileTypeDescriptor(FileTypeDescriptor.GSM)}, 5));
                list = result;
                list.add(new PlugInInfo("com.sun.media.multiplexer.audio.MPEGMux", new Format[0], new Format[]{new FileTypeDescriptor(FileTypeDescriptor.MPEG_AUDIO)}, 5));
                list = result;
                list.add(new PlugInInfo("com.sun.media.multiplexer.audio.WAVMux", new Format[0], new Format[]{new FileTypeDescriptor(FileTypeDescriptor.WAVE)}, 5));
                list = result;
                list.add(new PlugInInfo("com.sun.media.multiplexer.audio.AIFFMux", new Format[0], new Format[]{new FileTypeDescriptor(FileTypeDescriptor.AIFF)}, 5));
                list = result;
                list.add(new PlugInInfo("com.sun.media.multiplexer.audio.AUMux", new Format[0], new Format[]{new FileTypeDescriptor(FileTypeDescriptor.BASIC_AUDIO)}, 5));
                list = result;
                list.add(new PlugInInfo("com.sun.media.multiplexer.video.AVIMux", new Format[0], new Format[]{new FileTypeDescriptor(FileTypeDescriptor.MSVIDEO)}, 5));
                list = result;
                list.add(new PlugInInfo("com.sun.media.multiplexer.video.QuicktimeMux", new Format[0], new Format[]{new FileTypeDescriptor(FileTypeDescriptor.QUICKTIME)}, 5));
            }
            if ((flags & 2) != 0) {
                result.add("net.sf.fmj.media.codec.video.jpeg.Packetizer");
                result.add("net.sf.fmj.media.renderer.video.SimpleSwingRenderer");
                result.add("net.sf.fmj.media.renderer.video.SimpleAWTRenderer");
                result.add("net.sf.fmj.media.renderer.video.Java2dRenderer");
                result.add("net.sf.fmj.media.renderer.video.JPEGRTPRenderer");
                result.add("net.sf.fmj.media.renderer.video.JPEGRenderer");
                result.add("net.sf.fmj.media.parser.JavaSoundParser");
                result.add("net.sf.fmj.media.codec.JavaSoundCodec");
                result.add("net.sf.fmj.media.renderer.audio.JavaSoundRenderer");
                result.add("net.sf.fmj.media.codec.audio.gsm.Decoder");
                result.add("net.sf.fmj.media.codec.audio.gsm.Encoder");
                result.add("net.sf.fmj.media.codec.audio.gsm.DePacketizer");
                result.add("net.sf.fmj.media.codec.audio.gsm.Packetizer");
                result.add("net.sf.fmj.media.multiplexer.audio.GsmMux");
                result.add("net.sf.fmj.media.parser.GsmParser");
                result.add("net.sf.fmj.media.codec.audio.ulaw.Decoder");
                result.add("net.sf.fmj.media.codec.audio.ulaw.Encoder");
                result.add("net.sf.fmj.media.codec.audio.ulaw.DePacketizer");
                result.add("net.sf.fmj.media.codec.audio.ulaw.Packetizer");
                result.add("net.sf.fmj.media.codec.audio.RateConverter");
                result.add("net.sf.fmj.media.codec.audio.alaw.Decoder");
                result.add("net.sf.fmj.media.codec.audio.alaw.Encoder");
                result.add("net.sf.fmj.media.codec.audio.alaw.DePacketizer");
                result.add("net.sf.fmj.media.codec.audio.alaw.Packetizer");
                result.add("net.sf.fmj.media.codec.video.jpeg.JpegEncoder");
                result.add("net.sf.fmj.media.codec.video.lossless.GIFEncoder");
                result.add("net.sf.fmj.media.codec.video.lossless.GIFDecoder");
                result.add("net.sf.fmj.media.codec.video.lossless.PNGEncoder");
                result.add("net.sf.fmj.media.codec.video.lossless.PNGDecoder");
                result.add("net.sf.fmj.media.parser.RawStreamParser");
                result.add("net.sf.fmj.media.parser.RawPullStreamParser");
                result.add("net.sf.fmj.media.parser.RawPullBufferParser");
                result.add("net.sf.fmj.media.multiplexer.RTPSyncBufferMux");
                result.add("net.sf.fmj.media.multiplexer.RawBufferMux");
                result.add("net.sf.fmj.media.multiplexer.audio.AIFFMux");
                result.add("net.sf.fmj.media.multiplexer.audio.AUMux");
                result.add("net.sf.fmj.media.multiplexer.audio.WAVMux");
                if (!DISABLE_OGG) {
                    result.add("net.sf.fmj.theora_java.JavaOggParser");
                }
                result.add("net.sf.fmj.media.parser.MultipartMixedReplaceParser");
                result.add("net.sf.fmj.media.multiplexer.MultipartMixedReplaceMux");
                result.add("net.sf.fmj.media.parser.XmlMovieParser");
                result.add("net.sf.fmj.media.multiplexer.XmlMovieMux");
                result.add("net.sf.fmj.media.multiplexer.audio.CsvAudioMux");
                result.add("net.java.sip.communicator.impl.media.codec.audio.speex.JavaEncoder");
                result.add("net.java.sip.communicator.impl.media.codec.audio.speex.JavaDecoder");
                result.add("net.java.sip.communicator.impl.media.codec.audio.ilbc.JavaEncoder");
                result.add("net.java.sip.communicator.impl.media.codec.audio.ilbc.JavaDecoder");
                result.add("com.t4l.jmf.JPEGDecoder");
            }
            if ((flags & 8) != 0) {
                result.add("net.sf.fmj.ffmpeg_java.FFMPEGParser");
                result.add("net.sf.fmj.theora_java.NativeOggParser");
            }
            if ((flags & 4) != 0) {
                result.add("com.omnividea.media.parser.video.Parser");
                result.add("com.omnividea.media.codec.video.NativeDecoder");
                result.add("com.omnividea.media.codec.audio.NativeDecoder");
                result.add("com.omnividea.media.codec.video.JavaDecoder");
            }
            if (!((flags & 2) == 0 || (flags & 1) == 0)) {
                list = result;
                list.add(new PlugInInfo("com.ibm.media.parser.video.MpegParser", new Format[]{new ContentDescriptor(FileTypeDescriptor.MPEG)}, new Format[0], 1));
            }
        }
        return result;
    }

    public static List<String> protocolPrefixList(int flags) {
        List<String> protocolPrefixList = new ArrayList();
        if ((flags & 1) != 0) {
            protocolPrefixList.add("javax");
            protocolPrefixList.add("com.sun");
            protocolPrefixList.add("com.ibm");
        }
        if ((flags & 8) != 0) {
            if (OSUtils.isMacOSX() || OSUtils.isWindows()) {
                protocolPrefixList.add("net.sf.fmj.qt");
            }
            if (OSUtils.isWindows()) {
                protocolPrefixList.add("net.sf.fmj.ds");
            }
        }
        if ((flags & 2) != 0) {
            protocolPrefixList.add("net.sf.fmj");
        }
        if ((flags & 4) != 0) {
            protocolPrefixList.add("com.omnividea");
        }
        return protocolPrefixList;
    }

    public static void registerAll(int flags) {
        registerProtocolPrefixList(flags);
        registerContentPrefixList(flags);
        registerPlugins(flags);
    }

    public static void registerContentPrefixList(int flags) {
        if (flags != 0) {
            Vector v = PackageManager.getContentPrefixList();
            for (String s : contentPrefixList(flags)) {
                if (!v.contains(s)) {
                    v.add(s);
                }
            }
            PackageManager.setContentPrefixList(v);
        }
    }

    public static void registerPlugins(int flags) {
        if (flags != 0) {
            for (PlugInInfo o : plugInList(flags)) {
                if (o instanceof PlugInInfo) {
                    PlugInInfo i = o;
                    PlugInManager.addPlugIn(i.className, i.in, i.out, i.type);
                } else {
                    PlugInUtility.registerPlugIn((String) o);
                }
            }
        }
    }

    public static void registerProtocolPrefixList(int flags) {
        if (flags != 0) {
            Vector v = PackageManager.getProtocolPrefixList();
            for (String s : protocolPrefixList(flags)) {
                if (!v.contains(s)) {
                    v.add(s);
                }
            }
            PackageManager.setProtocolPrefixList(v);
        }
    }

    private static List<String> removePluginsFromList(int flags, List v) {
        List<String> result = new ArrayList();
        for (String className : v) {
            boolean remove = ENABLE_GSTREAMER;
            if ((flags & 1) != 0 && (className.startsWith("com.ibm.") || className.startsWith("com.sun.") || className.startsWith("javax.media."))) {
                remove = true;
            }
            if (!((flags & 2) == 0 || ((!className.startsWith("net.sf.fmj") && !className.startsWith("net.java.sip.communicator.impl.media.") && !className.startsWith("com.t4l.jmf")) || className.equals("net.sf.fmj.ffmpeg_java.FFMPEGParser") || className.equals("net.sf.fmj.theora_java.NativeOggParser")))) {
                remove = true;
            }
            if ((flags & 8) != 0 && (className.equals("net.sf.fmj.ffmpeg_java.FFMPEGParser") || className.equals("net.sf.fmj.theora_java.NativeOggParser"))) {
                remove = true;
            }
            if ((flags & 4) != 0 && className.startsWith("com.omnividea.media.")) {
                remove = true;
            }
            if (remove) {
                result.add(className);
            }
        }
        return result;
    }

    public static final void setDefaultFlags(int flags) {
        defaultFlags = flags;
    }

    public static void unRegisterAll(int flags) {
        unRegisterProtocolPrefixList(flags);
        unRegisterContentPrefixList(flags);
        unRegisterPlugins(flags);
    }

    public static void unRegisterContentPrefixList(int flags) {
        if (flags != 0) {
            Vector v = PackageManager.getContentPrefixList();
            for (String s : contentPrefixList(flags)) {
                if (v.contains(s)) {
                    v.remove(s);
                }
            }
            PackageManager.setContentPrefixList(v);
        }
    }

    public static void unRegisterPlugins(int flags) {
        if (flags != 0) {
            int[] types = new int[]{1, 2, 3, 4, 5};
            Registry registry = Registry.getInstance();
            for (int type : types) {
                List<String> v = registry.getPluginList(type);
                v.removeAll(removePluginsFromList(flags, v));
                registry.setPluginList(type, v);
            }
            for (int type2 : types) {
                for (String className : removePluginsFromList(flags, PlugInManager.getPlugInList(null, null, type2))) {
                    PlugInManager.removePlugIn(className, type2);
                }
            }
        }
    }

    public static void unRegisterProtocolPrefixList(int flags) {
        if (flags != 0) {
            Vector v = PackageManager.getProtocolPrefixList();
            for (String s : protocolPrefixList(flags)) {
                if (v.contains(s)) {
                    v.remove(s);
                }
            }
            PackageManager.setProtocolPrefixList(v);
        }
    }
}
