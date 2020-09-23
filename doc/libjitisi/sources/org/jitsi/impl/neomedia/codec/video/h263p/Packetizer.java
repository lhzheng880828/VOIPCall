package org.jitsi.impl.neomedia.codec.video.h263p;

import java.util.LinkedList;
import java.util.List;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.VideoFormat;
import net.sf.fmj.media.AbstractPacketizer;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.codec.AbstractCodec2;
import org.jitsi.service.neomedia.codec.Constants;

public class Packetizer extends AbstractPacketizer {
    private static final Format[] DEFAULT_OUTPUT_FORMATS = new Format[]{new VideoFormat("h263-1998/rtp")};
    public static final int MAX_PAYLOAD_SIZE = 1024;
    private static final String PLUGIN_NAME = "H263+ Packetizer";
    private int sequenceNumber;
    private long timeStamp;
    private final List<byte[]> videoPkts;

    private static int findStartcode(byte[] byteStream, int beginIndex, int endIndex) {
        while (beginIndex < endIndex - 3) {
            if (byteStream[beginIndex] == (byte) 0 && byteStream[beginIndex + 1] == (byte) 0 && (byteStream[beginIndex + 2] & -128) == -128) {
                return beginIndex;
            }
            beginIndex++;
        }
        return endIndex;
    }

    public Packetizer() {
        this.sequenceNumber = 0;
        this.timeStamp = 0;
        this.videoPkts = new LinkedList();
        this.inputFormats = new Format[]{new VideoFormat(Constants.H263P)};
        this.inputFormat = null;
        this.outputFormat = null;
    }

    public synchronized void close() {
        if (this.opened) {
            this.videoPkts.clear();
            this.opened = false;
            super.close();
        }
    }

    private Format[] getMatchingOutputFormats(Format inputFormat) {
        VideoFormat inputVideoFormat = (VideoFormat) inputFormat;
        return new Format[]{new VideoFormat("h263-1998/rtp", inputVideoFormat.getSize(), -1, Format.byteArray, inputVideoFormat.getFrameRate())};
    }

    public String getName() {
        return PLUGIN_NAME;
    }

    public Format[] getSupportedOutputFormats(Format inputFormat) {
        if (inputFormat == null) {
            return DEFAULT_OUTPUT_FORMATS;
        }
        if (!(inputFormat instanceof VideoFormat) || AbstractCodec2.matches(inputFormat, this.inputFormats) == null) {
            return new Format[0];
        }
        return getMatchingOutputFormats(inputFormat);
    }

    public synchronized void open() throws ResourceUnavailableException {
        if (!this.opened) {
            this.videoPkts.clear();
            this.sequenceNumber = 0;
            super.open();
            this.opened = true;
        }
    }

    private boolean packetize(byte[] data, int offset, int length) {
        boolean pktAdded = false;
        while (length > 0) {
            int payloadLength;
            int i;
            boolean isPsc = false;
            int pos = 0;
            int maxPayloadLength = 1024;
            if (data.length > 3 && data[offset] == (byte) 0 && data[offset + 1] == (byte) 0) {
                isPsc = true;
                pos = 2;
            } else {
                maxPayloadLength = 1024 - 2;
            }
            if (length > maxPayloadLength) {
                payloadLength = maxPayloadLength;
            } else {
                payloadLength = length;
            }
            if (isPsc) {
                i = 0;
            } else {
                i = 2;
            }
            byte[] pkt = new byte[(i + payloadLength)];
            if (isPsc) {
                i = 4;
            } else {
                i = 0;
            }
            pkt[0] = (byte) i;
            pkt[1] = (byte) 0;
            System.arraycopy(data, offset + pos, pkt, 2, payloadLength - pos);
            if (this.videoPkts.add(pkt) || pktAdded) {
                pktAdded = true;
            } else {
                pktAdded = false;
            }
            offset += payloadLength;
            length -= payloadLength;
        }
        return pktAdded;
    }

    public int process(Buffer inBuffer, Buffer outBuffer) {
        int inLength = inBuffer.getLength();
        byte[] inData = (byte[]) inBuffer.getData();
        int inOffset = inBuffer.getOffset();
        boolean pktAdded = false;
        if (this.videoPkts.size() > 0) {
            byte[] pktData = (byte[]) this.videoPkts.remove(0);
            outBuffer.setData(pktData);
            outBuffer.setLength(pktData.length);
            outBuffer.setOffset(0);
            outBuffer.setTimeStamp(this.timeStamp);
            int i = this.sequenceNumber;
            this.sequenceNumber = i + 1;
            outBuffer.setSequenceNumber((long) i);
            if (this.videoPkts.size() > 0) {
                return 2;
            }
            outBuffer.setFlags(outBuffer.getFlags() | 2048);
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
            int endIndex = inOffset + inLength;
            int beginIndex = findStartcode(inData, inOffset, endIndex);
            if (beginIndex < endIndex) {
                while (beginIndex < endIndex) {
                    int nextBeginIndex = findStartcode(inData, beginIndex + 3, endIndex);
                    int length = nextBeginIndex - beginIndex;
                    if (length > 0) {
                        pktAdded = packetize(inData, beginIndex, length) || pktAdded;
                        beginIndex += length;
                    }
                    beginIndex = nextBeginIndex + 3;
                }
            }
            this.timeStamp = inBuffer.getTimeStamp();
            if (pktAdded) {
                return process(inBuffer, outBuffer);
            }
            return 1;
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
        this.outputFormat = new VideoFormat(videoFormat.getEncoding(), size, -1, Format.byteArray, videoFormat.getFrameRate());
        return this.outputFormat;
    }
}
