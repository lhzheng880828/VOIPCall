package org.jitsi.impl.neomedia.codec.video.h264;

import com.lti.utils.UnsignedUtils;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.VideoFormat;
import net.sf.fmj.media.AbstractPacketizer;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.impl.neomedia.format.ParameterizedVideoFormat;
import org.jitsi.service.neomedia.codec.Constants;

public class Packetizer extends AbstractPacketizer {
    public static final int MAX_PAYLOAD_SIZE = 1024;
    private static final String PLUGIN_NAME = "H264 Packetizer";
    static final Format[] SUPPORTED_OUTPUT_FORMATS;
    private final List<byte[]> nals;
    private long nalsTimeStamp;
    private int sequenceNumber;

    static {
        Format[] formatArr = new Format[2];
        formatArr[0] = new ParameterizedVideoFormat(Constants.H264_RTP, JNIEncoder.PACKETIZATION_MODE_FMTP, "0");
        formatArr[1] = new ParameterizedVideoFormat(Constants.H264_RTP, JNIEncoder.PACKETIZATION_MODE_FMTP, "1");
        SUPPORTED_OUTPUT_FORMATS = formatArr;
    }

    private static int ff_avc_find_startcode(byte[] byteStream, int beginIndex, int endIndex) {
        while (beginIndex < endIndex - 3) {
            if (byteStream[beginIndex] == (byte) 0 && byteStream[beginIndex + 1] == (byte) 0 && byteStream[beginIndex + 2] == (byte) 1) {
                return beginIndex;
            }
            beginIndex++;
        }
        return endIndex;
    }

    public Packetizer() {
        this.nals = new LinkedList();
        this.inputFormats = JNIEncoder.SUPPORTED_OUTPUT_FORMATS;
        this.inputFormat = null;
        this.outputFormat = null;
    }

    public synchronized void close() {
        if (this.opened) {
            this.opened = false;
            super.close();
        }
    }

    private Format[] getMatchingOutputFormats(Format input) {
        VideoFormat videoInput = (VideoFormat) input;
        Dimension size = videoInput.getSize();
        float frameRate = videoInput.getFrameRate();
        String packetizationMode = getPacketizationMode(input);
        Format[] formatArr = new Format[1];
        formatArr[0] = new ParameterizedVideoFormat(Constants.H264_RTP, size, -1, Format.byteArray, frameRate, ParameterizedVideoFormat.toMap(JNIEncoder.PACKETIZATION_MODE_FMTP, packetizationMode));
        return formatArr;
    }

    public String getName() {
        return PLUGIN_NAME;
    }

    private String getPacketizationMode(Format format) {
        String packetizationMode = null;
        if (format instanceof ParameterizedVideoFormat) {
            packetizationMode = ((ParameterizedVideoFormat) format).getFormatParameter(JNIEncoder.PACKETIZATION_MODE_FMTP);
        }
        if (packetizationMode == null) {
            return "0";
        }
        return packetizationMode;
    }

    public Format[] getSupportedOutputFormats(Format in) {
        if (in == null) {
            return SUPPORTED_OUTPUT_FORMATS;
        }
        if (!(in instanceof VideoFormat) || AbstractCodec2.matches(in, this.inputFormats) == null) {
            return new Format[0];
        }
        return getMatchingOutputFormats(in);
    }

    public synchronized void open() throws ResourceUnavailableException {
        if (!this.opened) {
            this.nals.clear();
            this.sequenceNumber = 0;
            super.open();
            this.opened = true;
        }
    }

    private boolean packetizeNAL(byte[] nal, int nalOffset, int nalLength) {
        if (nalLength <= 1024) {
            byte[] singleNALUnitPacket = new byte[nalLength];
            System.arraycopy(nal, nalOffset, singleNALUnitPacket, 0, nalLength);
            return this.nals.add(singleNALUnitPacket);
        }
        byte octet = nal[nalOffset];
        byte fuIndicator = (byte) ((((octet & 128) | (octet & 96)) | 28) & UnsignedUtils.MAX_UBYTE);
        byte fuHeader = (byte) (((octet & 31) | 128) & UnsignedUtils.MAX_UBYTE);
        nalOffset++;
        nalLength--;
        boolean nalsAdded = false;
        while (nalLength > 0) {
            int fuPayloadLength;
            if (nalLength > 1022) {
                fuPayloadLength = 1022;
            } else {
                fuPayloadLength = nalLength;
                fuHeader = (byte) (fuHeader | 64);
            }
            byte[] fua = new byte[1024];
            fua[0] = fuIndicator;
            fua[1] = fuHeader;
            System.arraycopy(nal, nalOffset, fua, 2, fuPayloadLength);
            nalOffset += fuPayloadLength;
            nalLength -= fuPayloadLength;
            nalsAdded = this.nals.add(fua) || nalsAdded;
            fuHeader = (byte) (fuHeader & -129);
        }
        return nalsAdded;
    }

    public int process(Buffer inBuffer, Buffer outBuffer) {
        if (this.nals.size() > 0) {
            byte[] nal = (byte[]) this.nals.remove(0);
            outBuffer.setData(nal);
            outBuffer.setLength(nal.length);
            outBuffer.setOffset(0);
            outBuffer.setTimeStamp(this.nalsTimeStamp);
            int i = this.sequenceNumber;
            this.sequenceNumber = i + 1;
            outBuffer.setSequenceNumber((long) i);
            if (this.nals.size() > 0) {
                return 2;
            }
            int flags = outBuffer.getFlags() | 2048;
            if (nal.length > 0) {
                int nal_unit_type = nal[0] & 31;
                if (nal_unit_type == 28 && nal.length > 1) {
                    byte fuHeader = nal[1];
                    if ((fuHeader & 64) == 0) {
                        flags &= -2049;
                    } else {
                        nal_unit_type = fuHeader & 31;
                    }
                }
                switch (nal_unit_type) {
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                        flags &= -2049;
                        break;
                }
            }
            outBuffer.setFlags(flags);
            return 0;
        } else if (isEOM(inBuffer)) {
            propagateEOM(outBuffer);
            reset();
            return 0;
        } else if (inBuffer.isDiscard()) {
            outBuffer.setDiscard(true);
            reset();
            return 0;
        } else {
            Format inFormat = inBuffer.getFormat();
            if (!(inFormat == this.inputFormat || inFormat.matches(this.inputFormat))) {
                setInputFormat(inFormat);
            }
            int inLength = inBuffer.getLength();
            if (inLength < 4) {
                outBuffer.setDiscard(true);
                reset();
                return 0;
            }
            byte[] inData = (byte[]) inBuffer.getData();
            int inOffset = inBuffer.getOffset();
            boolean nalsAdded = false;
            int endIndex = inOffset + inLength;
            int beginIndex = ff_avc_find_startcode(inData, inOffset, endIndex);
            if (beginIndex < endIndex) {
                beginIndex += 3;
                while (beginIndex < endIndex) {
                    int nextBeginIndex = ff_avc_find_startcode(inData, beginIndex, endIndex);
                    if (nextBeginIndex > endIndex) {
                        break;
                    }
                    int nalLength = nextBeginIndex - beginIndex;
                    while (nalLength > 0 && inData[(beginIndex + nalLength) - 1] == (byte) 0) {
                        nalLength--;
                    }
                    if (nalLength > 0) {
                        if (packetizeNAL(inData, beginIndex, nalLength) || nalsAdded) {
                            nalsAdded = true;
                        } else {
                            nalsAdded = false;
                        }
                    }
                    beginIndex = nextBeginIndex + 3;
                }
            }
            this.nalsTimeStamp = inBuffer.getTimeStamp();
            return nalsAdded ? process(inBuffer, outBuffer) : 4;
        }
    }

    public Format setInputFormat(Format in) {
        if (!(in instanceof VideoFormat) || AbstractCodec2.matches(in, this.inputFormats) == null) {
            return null;
        }
        this.inputFormat = in;
        return in;
    }

    public Format setOutputFormat(Format format) {
        if (!(format instanceof VideoFormat) || AbstractCodec2.matches(format, getMatchingOutputFormats(this.inputFormat)) == null) {
            return null;
        }
        VideoFormat videoFormat = (VideoFormat) format;
        Dimension size = null;
        if (this.inputFormat != null) {
            size = ((VideoFormat) this.inputFormat).getSize();
        }
        if (size == null && format.matches(this.outputFormat)) {
            size = ((VideoFormat) this.outputFormat).getSize();
        }
        Map<String, String> fmtps = null;
        if (format instanceof ParameterizedVideoFormat) {
            fmtps = ((ParameterizedVideoFormat) format).getFormatParameters();
        }
        if (fmtps == null) {
            fmtps = new HashMap();
        }
        if (fmtps.get(JNIEncoder.PACKETIZATION_MODE_FMTP) == null) {
            fmtps.put(JNIEncoder.PACKETIZATION_MODE_FMTP, getPacketizationMode(this.inputFormat));
        }
        this.outputFormat = new ParameterizedVideoFormat(videoFormat.getEncoding(), size, -1, Format.byteArray, videoFormat.getFrameRate(), fmtps);
        return this.outputFormat;
    }
}
