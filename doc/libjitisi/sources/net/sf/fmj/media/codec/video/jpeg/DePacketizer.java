package net.sf.fmj.media.codec.video.jpeg;

import com.lti.utils.StringUtils;
import com.lti.utils.UnsignedUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.JPEGFormat;
import javax.media.format.VideoFormat;
import net.sf.fmj.media.AbstractCodec;
import net.sf.fmj.utility.ArrayUtility;
import net.sf.fmj.utility.LoggingStringUtils;
import org.jitsi.android.util.java.awt.Dimension;

public class DePacketizer extends AbstractCodec implements Codec {
    private static final boolean COMPARE_WITH_BASELINE = false;
    private static final boolean EXIT_AFTER_ONE_FRAME = false;
    private static final int MAX_ACTIVE_FRAME_ASSEMBLERS = 3;
    private static final int MAX_DUMP_SIZE = 200000;
    private static final boolean TRACE = false;
    /* access modifiers changed from: private|static|final */
    public static final BufferFragmentOffsetComparator bufferFragmentOffsetComparator = new BufferFragmentOffsetComparator();
    private Codec baselineCodec;
    private final FrameAssemblerCollection frameAssemblers = new FrameAssemblerCollection();
    private long lastRTPtimestamp = -1;
    private long lastTimestamp;
    private final Format[] supportedInputFormats = new Format[]{new VideoFormat(VideoFormat.JPEG_RTP, null, -1, Format.byteArray, -1.0f)};
    private final Format[] supportedOutputFormats = new Format[]{new JPEGFormat()};

    private static class BufferFragmentOffsetComparator implements Comparator<Buffer> {
        private BufferFragmentOffsetComparator() {
        }

        public int compare(Buffer a, Buffer b) {
            if (a == null && b == null) {
                return 0;
            }
            if (a == null) {
                return -1;
            }
            if (b == null) {
                return 1;
            }
            return JpegRTPHeader.parse((byte[]) a.getData(), a.getOffset()).getFragmentOffset() - JpegRTPHeader.parse((byte[]) b.getData(), b.getOffset()).getFragmentOffset();
        }
    }

    static class FrameAssembler {
        private final List<Buffer> list = new ArrayList();
        private boolean rtpMarker;

        FrameAssembler() {
        }

        public boolean complete() {
            if (this.rtpMarker && this.list.size() > 0 && contiguous()) {
                return true;
            }
            return false;
        }

        private boolean contiguous() {
            int expect = 0;
            for (Buffer b : this.list) {
                JpegRTPHeader jpegRtpHeader = parseJpegRTPHeader(b);
                int otherOffset = 0;
                if (jpegRtpHeader.getType() > 63) {
                    otherOffset = 0 + 4;
                }
                if (jpegRtpHeader.getQ() >= 128) {
                    int j = ((b.getOffset() + 8) + otherOffset) + 2;
                    byte[] data = (byte[]) b.getData();
                    otherOffset += (((data[j] & UnsignedUtils.MAX_UBYTE) << 8) | (data[j + 1] & UnsignedUtils.MAX_UBYTE)) + 4;
                }
                if (jpegRtpHeader.getFragmentOffset() != expect) {
                    return false;
                }
                expect += (b.getLength() - 8) - otherOffset;
            }
            return true;
        }

        public int copyToBuffer(Buffer bDest) {
            if (!this.rtpMarker) {
                throw new IllegalStateException();
            } else if (this.list.size() <= 0) {
                throw new IllegalStateException();
            } else {
                byte[] data;
                Buffer bFirst = (Buffer) this.list.get(0);
                boolean prependHeader = !DePacketizer.hasJPEGHeaders((byte[]) ((byte[]) bFirst.getData()), bFirst.getOffset() + 8, bFirst.getLength() + -8);
                int MAX_HEADER = prependHeader ? 1024 : 0;
                int frameLength = frameLength();
                int inputOffset = bFirst.getOffset();
                int dri = 0;
                byte[] lqt = null;
                byte[] cqt = null;
                byte[] inputData = (byte[]) bFirst.getData();
                if (bDest.getData() == null || ((byte[]) bDest.getData()).length < (frameLength + MAX_HEADER) + 2) {
                    data = new byte[((frameLength + MAX_HEADER) + 2)];
                } else {
                    data = (byte[]) bDest.getData();
                    DePacketizer.zeroData(data);
                }
                int offsetAfterHeaders = 0;
                if (prependHeader) {
                    int inputOffset2;
                    int i = 0 + 1;
                    data[0] = (byte) -1;
                    offsetAfterHeaders = i + 1;
                    data[i] = (byte) -40;
                    offsetAfterHeaders = DePacketizer.buildJFIFHeader(data, offsetAfterHeaders);
                    JpegRTPHeader jpegRtpHeaderFirst = parseJpegRTPHeader(bFirst);
                    inputOffset += 8;
                    if (jpegRtpHeaderFirst.getType() >= 64 && jpegRtpHeaderFirst.getType() <= 127) {
                        inputOffset2 = inputOffset + 1;
                        dri = ((inputData[inputOffset] & UnsignedUtils.MAX_UBYTE) << 8) | (inputData[inputOffset2] & UnsignedUtils.MAX_UBYTE);
                        inputOffset = (inputOffset2 + 1) + 2;
                    }
                    if (jpegRtpHeaderFirst.getQ() > 127) {
                        inputOffset += 2;
                        inputOffset2 = inputOffset + 1;
                        inputOffset = inputOffset2 + 1;
                        int length = ((inputData[inputOffset] & UnsignedUtils.MAX_UBYTE) << 8) | (inputData[inputOffset2] & UnsignedUtils.MAX_UBYTE);
                        lqt = ArrayUtility.copyOfRange(inputData, inputOffset, (length / 2) + inputOffset);
                        inputOffset += length / 2;
                        cqt = ArrayUtility.copyOfRange(inputData, inputOffset, (length / 2) + inputOffset);
                        inputOffset += length / 2;
                    }
                    offsetAfterHeaders = RFC2035.MakeHeaders(false, data, offsetAfterHeaders, jpegRtpHeaderFirst.getType(), jpegRtpHeaderFirst.getQ(), jpegRtpHeaderFirst.getWidthInBlocks(), jpegRtpHeaderFirst.getHeightInBlocks(), lqt, cqt, dri);
                }
                for (Buffer b : this.list) {
                    System.arraycopy(b.getData(), b.getOffset() + 8, data, parseJpegRTPHeader(b).getFragmentOffset() + offsetAfterHeaders, b.getLength() - 8);
                }
                int trailing = 0;
                if (!DePacketizer.hasJPEGTrailer(data, offsetAfterHeaders + frameLength, 2)) {
                    int trailing2 = 0 + 1;
                    data[(offsetAfterHeaders + frameLength) + 0] = (byte) -1;
                    trailing = trailing2 + 1;
                    data[(offsetAfterHeaders + frameLength) + 1] = (byte) -39;
                }
                bDest.setData(data);
                bDest.setLength((offsetAfterHeaders + frameLength) + trailing);
                bDest.setOffset(0);
                bDest.setTimeStamp(bFirst.getTimeStamp());
                return offsetAfterHeaders;
            }
        }

        public int frameLength() {
            if (!this.rtpMarker) {
                throw new IllegalStateException();
            } else if (this.list.size() <= 0) {
                throw new IllegalStateException();
            } else {
                Buffer b = (Buffer) this.list.get(this.list.size() - 1);
                return (parseJpegRTPHeader(b).getFragmentOffset() + b.getLength()) - 8;
            }
        }

        private JpegRTPHeader parseJpegRTPHeader(Buffer b) {
            return JpegRTPHeader.parse((byte[]) b.getData(), b.getOffset());
        }

        public void put(Buffer buffer) {
            if (!this.rtpMarker) {
                this.rtpMarker = (buffer.getFlags() & 2048) != 0;
            }
            if (buffer.getLength() > 8) {
                this.list.add(buffer);
                Collections.sort(this.list, DePacketizer.bufferFragmentOffsetComparator);
            }
        }
    }

    private static class FrameAssemblerCollection {
        private Map<Long, FrameAssembler> frameAssemblers;

        private FrameAssemblerCollection() {
            this.frameAssemblers = new HashMap();
        }

        public void clear() {
            this.frameAssemblers.clear();
        }

        public FrameAssembler findOrAdd(long timestamp) {
            Long timestampObj = Long.valueOf(timestamp);
            FrameAssembler result = (FrameAssembler) this.frameAssemblers.get(timestampObj);
            if (result != null) {
                return result;
            }
            result = new FrameAssembler();
            this.frameAssemblers.put(timestampObj, result);
            return result;
        }

        public long getOldestTimestamp() {
            long oldestSoFar = -1;
            for (Long ts : this.frameAssemblers.keySet()) {
                if (oldestSoFar < 0 || ts.longValue() < oldestSoFar) {
                    oldestSoFar = ts.longValue();
                }
            }
            return oldestSoFar;
        }

        public void remove(long timestamp) {
            this.frameAssemblers.remove(Long.valueOf(timestamp));
        }

        public void removeAllButNewestN(int n) {
            while (this.frameAssemblers.size() > n) {
                long oldestTimestamp = getOldestTimestamp();
                if (oldestTimestamp < 0) {
                    throw new RuntimeException();
                }
                Long key = Long.valueOf(oldestTimestamp);
                String completeIncomplete;
                if (((FrameAssembler) this.frameAssemblers.get(key)).complete()) {
                    completeIncomplete = "complete";
                } else {
                    completeIncomplete = "incomplete";
                }
                this.frameAssemblers.remove(key);
            }
        }

        public void removeOlderThan(long timestamp) {
            Iterator<Entry<Long, FrameAssembler>> i = this.frameAssemblers.entrySet().iterator();
            while (i.hasNext()) {
                if (((Long) ((Entry) i.next()).getKey()).longValue() < timestamp) {
                    i.remove();
                }
            }
        }
    }

    /* access modifiers changed from: private|static */
    public static int buildJFIFHeader(byte[] data, int offset) {
        int i = offset + 1;
        data[offset] = (byte) -1;
        offset = i + 1;
        data[i] = (byte) -32;
        i = offset + 1;
        data[offset] = (byte) 0;
        offset = i + 1;
        data[i] = (byte) 16;
        i = offset + 1;
        data[offset] = (byte) 74;
        offset = i + 1;
        data[i] = (byte) 70;
        i = offset + 1;
        data[offset] = (byte) 73;
        offset = i + 1;
        data[i] = (byte) 70;
        i = offset + 1;
        data[offset] = (byte) 0;
        offset = i + 1;
        data[i] = (byte) 1;
        i = offset + 1;
        data[offset] = (byte) 1;
        offset = i + 1;
        data[i] = (byte) 0;
        i = offset + 1;
        data[offset] = (byte) 0;
        offset = i + 1;
        data[i] = (byte) 1;
        i = offset + 1;
        data[offset] = (byte) 0;
        offset = i + 1;
        data[i] = (byte) 1;
        i = offset + 1;
        data[offset] = (byte) 0;
        offset = i + 1;
        data[i] = (byte) 0;
        return offset;
    }

    private static void dump(Buffer b, String name) {
    }

    private static String dump(byte[] data, int offset, int len) {
        return StringUtils.dump(data, offset, offset + len);
    }

    /* access modifiers changed from: private|static */
    public static boolean hasJPEGHeaders(byte[] data, int offset, int len) {
        if (len < 2) {
            throw new IllegalArgumentException();
        }
        int offset2 = offset + 1;
        if (data[offset] != (byte) -1) {
            offset = offset2;
            return false;
        }
        offset = offset2 + 1;
        if (data[offset2] == (byte) -40) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private|static */
    public static boolean hasJPEGTrailer(byte[] data, int offset, int len) {
        if (len < 2) {
            throw new IllegalArgumentException();
        }
        int offset2 = offset + 1;
        if (data[offset] != (byte) -1) {
            offset = offset2;
            return false;
        }
        offset = offset2 + 1;
        if (data[offset2] == (byte) -39) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private|static */
    public static void zeroData(byte[] data) {
        int len = data.length;
        for (int i = 0; i < len; i++) {
            data[i] = (byte) 0;
        }
    }

    public void close() {
        if (this.baselineCodec != null) {
            this.baselineCodec.close();
        }
        super.close();
        this.frameAssemblers.clear();
    }

    public Object getControl(String controlType) {
        if (this.baselineCodec != null) {
            return this.baselineCodec.getControl(controlType);
        }
        return super.getControl(controlType);
    }

    public Object[] getControls() {
        if (this.baselineCodec != null) {
            return this.baselineCodec.getControls();
        }
        return super.getControls();
    }

    public String getName() {
        return "JPEG DePacketizer";
    }

    public Format[] getSupportedInputFormats() {
        return this.supportedInputFormats;
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return this.supportedOutputFormats;
        }
        Dimension size;
        VideoFormat inputCast = (VideoFormat) input;
        Dimension HARD_CODED_SIZE = new Dimension(320, 240);
        Format[] result = new Format[1];
        if (inputCast.getSize() != null) {
            size = inputCast.getSize();
        } else {
            size = HARD_CODED_SIZE;
        }
        result[0] = new JPEGFormat(size, -1, Format.byteArray, -1.0f, -1, -1);
        if (this.baselineCodec == null) {
            return result;
        }
        Format[] baselineResult = this.baselineCodec.getSupportedOutputFormats(input);
        System.out.println("input:  " + LoggingStringUtils.formatToStr(input));
        for (int i = 0; i < baselineResult.length; i++) {
            System.out.println("output: " + LoggingStringUtils.formatToStr(baselineResult[0]));
        }
        return result;
    }

    public void open() throws ResourceUnavailableException {
        if (this.baselineCodec != null) {
            this.baselineCodec.open();
        }
        super.open();
    }

    public int process(Buffer input, Buffer output) {
        if (input.isDiscard()) {
            output.setDiscard(true);
            return 4;
        }
        if (this.baselineCodec != null) {
            this.baselineCodec.process(input, output);
        }
        if (input.getLength() >= 8) {
            JpegRTPHeader jpegRtpHeader = JpegRTPHeader.parse((byte[]) input.getData(), input.getOffset());
        }
        long timestamp = input.getTimeStamp();
        if ((input.getFlags() & 2048) != 0) {
        }
        FrameAssembler assembler = this.frameAssemblers.findOrAdd(timestamp);
        assembler.put((Buffer) input.clone());
        if (assembler.complete()) {
            Buffer bComplete;
            if (this.baselineCodec == null) {
                bComplete = output;
            } else {
                bComplete = new Buffer();
            }
            int offsetAfterHeaders = assembler.copyToBuffer(bComplete);
            this.frameAssemblers.remove(timestamp);
            this.frameAssemblers.removeOlderThan(timestamp);
            if (this.lastRTPtimestamp == -1) {
                this.lastRTPtimestamp = input.getTimeStamp();
                this.lastTimestamp = System.nanoTime();
            }
            return 0;
        }
        this.frameAssemblers.removeAllButNewestN(3);
        output.setDiscard(true);
        return 4;
    }

    public void reset() {
        if (this.baselineCodec != null) {
            this.baselineCodec.reset();
        }
        super.reset();
        this.frameAssemblers.clear();
    }

    public Format setInputFormat(Format format) {
        if (this.baselineCodec == null) {
            return super.setInputFormat(format);
        }
        super.setInputFormat(format);
        return this.baselineCodec.setInputFormat(format);
    }

    public Format setOutputFormat(Format format) {
        if (this.baselineCodec == null) {
            return super.setOutputFormat(format);
        }
        super.setOutputFormat(format);
        return this.baselineCodec.setOutputFormat(format);
    }
}
