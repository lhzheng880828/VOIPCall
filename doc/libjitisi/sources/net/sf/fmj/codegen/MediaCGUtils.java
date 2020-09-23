package net.sf.fmj.codegen;

import com.sun.media.format.WavAudioFormat;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.format.H261Format;
import javax.media.format.H263Format;
import javax.media.format.IndexedColorFormat;
import javax.media.format.JPEGFormat;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;
import org.jitsi.android.util.java.awt.Dimension;

public class MediaCGUtils {
    public static String dataTypeToStr(Class<?> dataType) {
        if (dataType == null) {
            return "null";
        }
        if (dataType == Format.byteArray) {
            return "Format.byteArray";
        }
        if (dataType == Format.shortArray) {
            return "Format.shortArray";
        }
        if (dataType == Format.intArray) {
            return "Format.intArray";
        }
        throw new IllegalArgumentException();
    }

    public static String formatToStr(Format f) {
        if (f == null) {
            return "null";
        }
        Class<?> c = f.getClass();
        if (c == RGBFormat.class) {
            RGBFormat o = (RGBFormat) f;
            return "new RGBFormat(" + toLiteral(o.getSize()) + ", " + o.getMaxDataLength() + ", " + dataTypeToStr(o.getDataType()) + ", " + CGUtils.toLiteral(o.getFrameRate()) + ", " + o.getBitsPerPixel() + ", " + CGUtils.toHexLiteral(o.getRedMask()) + ", " + CGUtils.toHexLiteral(o.getGreenMask()) + ", " + CGUtils.toHexLiteral(o.getBlueMask()) + ", " + o.getPixelStride() + ", " + o.getLineStride() + ", " + o.getFlipped() + ", " + o.getEndian() + ")";
        } else if (c == YUVFormat.class) {
            YUVFormat o2 = (YUVFormat) f;
            return "new YUVFormat(" + toLiteral(o2.getSize()) + ", " + o2.getMaxDataLength() + ", " + dataTypeToStr(o2.getDataType()) + ", " + CGUtils.toLiteral(o2.getFrameRate()) + ", " + o2.getYuvType() + ", " + o2.getStrideY() + ", " + o2.getStrideUV() + ", " + o2.getOffsetY() + ", " + o2.getOffsetU() + ", " + o2.getOffsetV() + ")";
        } else if (c == JPEGFormat.class) {
            JPEGFormat o3 = (JPEGFormat) f;
            return "new JPEGFormat(" + toLiteral(o3.getSize()) + ", " + o3.getMaxDataLength() + ", " + dataTypeToStr(o3.getDataType()) + ", " + CGUtils.toLiteral(o3.getFrameRate()) + ", " + o3.getQFactor() + ", " + o3.getDecimation() + ")";
        } else if (c == IndexedColorFormat.class) {
            IndexedColorFormat o4 = (IndexedColorFormat) f;
            return "new IndexedColorFormat(" + toLiteral(o4.getSize()) + ", " + o4.getMaxDataLength() + ", " + dataTypeToStr(o4.getDataType()) + ", " + CGUtils.toLiteral(o4.getFrameRate()) + ", " + o4.getLineStride() + ", " + o4.getMapSize() + ", " + CGUtils.toLiteral(o4.getRedValues()) + ", " + CGUtils.toLiteral(o4.getGreenValues()) + ", " + CGUtils.toLiteral(o4.getBlueValues()) + ")";
        } else if (c == H263Format.class) {
            H263Format o5 = (H263Format) f;
            return "new H263Format(" + toLiteral(o5.getSize()) + ", " + o5.getMaxDataLength() + ", " + dataTypeToStr(o5.getDataType()) + ", " + CGUtils.toLiteral(o5.getFrameRate()) + ", " + o5.getAdvancedPrediction() + ", " + o5.getArithmeticCoding() + ", " + o5.getErrorCompensation() + ", " + o5.getHrDB() + ", " + o5.getPBFrames() + ", " + o5.getUnrestrictedVector() + ")";
        } else if (c == H261Format.class) {
            H261Format o6 = (H261Format) f;
            return "new H261Format(" + toLiteral(o6.getSize()) + ", " + o6.getMaxDataLength() + ", " + dataTypeToStr(o6.getDataType()) + ", " + CGUtils.toLiteral(o6.getFrameRate()) + ", " + o6.getStillImageTransmission() + ")";
        } else if (c == AudioFormat.class) {
            AudioFormat o7 = (AudioFormat) f;
            return "new AudioFormat(" + CGUtils.toLiteral(o7.getEncoding()) + ", " + CGUtils.toLiteral(o7.getSampleRate()) + ", " + CGUtils.toLiteral(o7.getSampleSizeInBits()) + ", " + CGUtils.toLiteral(o7.getChannels()) + ", " + CGUtils.toLiteral(o7.getEndian()) + ", " + CGUtils.toLiteral(o7.getSigned()) + ", " + CGUtils.toLiteral(o7.getFrameSizeInBits()) + ", " + CGUtils.toLiteral(o7.getFrameRate()) + ", " + dataTypeToStr(o7.getDataType()) + ")";
        } else if (c == VideoFormat.class) {
            VideoFormat o8 = (VideoFormat) f;
            return "new VideoFormat(" + CGUtils.toLiteral(o8.getEncoding()) + ", " + toLiteral(o8.getSize()) + ", " + o8.getMaxDataLength() + ", " + dataTypeToStr(o8.getDataType()) + ", " + CGUtils.toLiteral(o8.getFrameRate()) + ")";
        } else if (c == Format.class) {
            Format o9 = f;
            return "new Format(" + CGUtils.toLiteral(o9.getEncoding()) + ", " + dataTypeToStr(o9.getDataType()) + ")";
        } else if (c == FileTypeDescriptor.class) {
            return "new FileTypeDescriptor(" + CGUtils.toLiteral(((FileTypeDescriptor) f).getEncoding()) + ")";
        } else if (c == ContentDescriptor.class) {
            return "new ContentDescriptor(" + CGUtils.toLiteral(((ContentDescriptor) f).getEncoding()) + ")";
        } else if (c == WavAudioFormat.class) {
            WavAudioFormat o10 = (WavAudioFormat) f;
            return "new com.sun.media.format.WavAudioFormat(" + CGUtils.toLiteral(o10.getEncoding()) + ", " + CGUtils.toLiteral(o10.getSampleRate()) + ", " + CGUtils.toLiteral(o10.getSampleSizeInBits()) + ", " + CGUtils.toLiteral(o10.getChannels()) + ", " + CGUtils.toLiteral(o10.getFrameSizeInBits()) + ", " + CGUtils.toLiteral(o10.getAverageBytesPerSecond()) + ", " + CGUtils.toLiteral(o10.getEndian()) + ", " + CGUtils.toLiteral(o10.getSigned()) + ", " + CGUtils.toLiteral((float) o10.getFrameRate()) + ", " + dataTypeToStr(o10.getDataType()) + ", " + CGUtils.toLiteral(o10.getCodecSpecificHeader()) + ")";
        } else {
            throw new IllegalArgumentException("" + f.getClass());
        }
    }

    public static String toLiteral(Dimension size) {
        if (size == null) {
            return "null";
        }
        return "new java.awt.Dimension(" + size.width + ", " + size.height + ")";
    }
}
