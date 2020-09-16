//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.jitsi.service.neomedia.rtp;

import net.sf.fmj.media.rtp.RTCPPacket;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class RTCPExtendedReport extends RTCPPacket {
    public static final String SDP_ATTRIBUTE = "rtcp-xr";
    public static final int XR = 207;
    private final List<RTCPExtendedReport.ReportBlock> reportBlocks;
    private int ssrc;
    private long systemTimeStamp;



    public RTCPExtendedReport() {
        this.reportBlocks = new LinkedList();
        this.type = 207;
    }



    public RTCPExtendedReport(byte[] buf, int off, int len) throws IOException {
        this(new DataInputStream(new ByteArrayInputStream(buf, off, len)));
    }

    public RTCPExtendedReport(DataInputStream datainputstream) throws IOException {
        this();
        int b0 = datainputstream.readUnsignedByte();
        int pt = datainputstream.readUnsignedByte();
        int length = datainputstream.readUnsignedShort();
        if (length < 1) {
            throw new IOException("Invalid RTCP length.");
        } else {
            this.parse(b0, pt, length, datainputstream);
        }
    }

    public RTCPExtendedReport(int b0, int pt, int length, DataInputStream datainputstream) throws IOException {
        this();
        this.parse(b0, pt, length, datainputstream);
    }

    private void parse(int b0, int pt, int length, DataInputStream datainputstream) throws IOException {
        if ((b0 & 192) != 128) {
            throw new IOException("Invalid RTCP version (V).");
        } else if (pt != 207) {
            throw new IOException("Invalid RTCP packet type (PT).");
        } else {
            this.setSSRC(datainputstream.readInt());

            int blockLength;
            for(length = length + 1 - 2; length > 0; length = length - 1 - blockLength) {
                int bt = datainputstream.readUnsignedByte();
                datainputstream.readByte();
                blockLength = datainputstream.readUnsignedShort();
                if (bt == 7) {
                    this.addReportBlock(new RTCPExtendedReport.VoIPMetricsReportBlock(blockLength, datainputstream));
                } else {
                    for(int i = 0; i < blockLength; ++i) {
                        datainputstream.readInt();
                    }
                }
            }

        }
    }

    public boolean addReportBlock(RTCPExtendedReport.ReportBlock reportBlock) {
        if (reportBlock == null) {
            throw new NullPointerException("reportBlock");
        } else {
            return this.reportBlocks.add(reportBlock);
        }
    }

    public void assemble(DataOutputStream dataoutputstream) throws IOException {
        dataoutputstream.writeByte(128);
        dataoutputstream.writeByte(207);
        dataoutputstream.writeShort(this.calcLength() / 4 - 1);
        dataoutputstream.writeInt(this.getSSRC());
        Iterator var2 = this.getReportBlocks().iterator();

        while(var2.hasNext()) {
            RTCPExtendedReport.ReportBlock reportBlock = (RTCPExtendedReport.ReportBlock)var2.next();
            reportBlock.assemble(dataoutputstream);
        }

    }


    public int calcLength() {
        int length = 8;

        RTCPExtendedReport.ReportBlock reportBlock;
        for(Iterator var2 = this.getReportBlocks().iterator(); var2.hasNext(); length += reportBlock.calcLength()) {
            reportBlock = (RTCPExtendedReport.ReportBlock)var2.next();
        }

        return length;
    }

    public int getReportBlockCount() {
        return this.reportBlocks.size();
    }

    public List<RTCPExtendedReport.ReportBlock> getReportBlocks() {
        return Collections.unmodifiableList(this.reportBlocks);
    }

    public int getSSRC() {
        return this.ssrc;
    }

    public long getSystemTimeStamp() {
        return this.systemTimeStamp;
    }

    public boolean removeReportBlock(RTCPExtendedReport.ReportBlock reportBlock) {
        return reportBlock == null ? false : this.reportBlocks.remove(reportBlock);
    }

    public void setSSRC(int ssrc) {
        this.ssrc = ssrc;
    }

    public void setSystemTimeStamp(long systemTimeStamp) {
        this.systemTimeStamp = systemTimeStamp;
    }

    public String toString() {
        StringBuilder s = new StringBuilder("RTCP XR");
        s.append(", SSRC ").append((long)this.getSSRC() & 4294967295L);
        List<RTCPExtendedReport.ReportBlock> reportBlocks = this.getReportBlocks();
        boolean b = false;
        s.append(", report blocks [");

        RTCPExtendedReport.ReportBlock reportBlock;
        for(Iterator var4 = reportBlocks.iterator(); var4.hasNext(); s.append(reportBlock)) {
            reportBlock = (RTCPExtendedReport.ReportBlock)var4.next();
            if (b) {
                s.append("; ");
            } else {
                b = true;
            }
        }

        s.append("]");
        return s.toString();
    }

    public static class VoIPMetricsReportBlock extends RTCPExtendedReport.ReportBlock {
        public static final byte ADAPTIVE_JITTER_BUFFER_ADAPTIVE = 3;
        public static final byte DISABLED_PACKET_LOSS_CONCEALMENT = 1;
        public static final byte ENHANCED_PACKET_LOSS_CONCEALMENT = 2;
        public static final byte NON_ADAPTIVE_JITTER_BUFFER_ADAPTIVE = 2;
        public static final byte RESERVED_JITTER_BUFFER_ADAPTIVE = 1;
        public static final String SDP_PARAMETER = "voip-metrics";
        public static final byte STANDARD_PACKET_LOSS_CONCEALMENT = 3;
        public static final byte UNKNOWN_JITTER_BUFFER_ADAPTIVE = 0;
        public static final byte UNSPECIFIED_PACKET_LOSS_CONCEALMENT = 0;
        public static final short VOIP_METRICS_REPORT_BLOCK_TYPE = 7;
        private short burstDensity;
        private int burstDuration;
        private short discardRate;
        private int endSystemDelay;
        private byte extRFactor;
        private short gapDensity;
        private int gapDuration;
        private short gMin;
        private int jitterBufferAbsoluteMaximumDelay;
        private byte jitterBufferAdaptive;
        private int jitterBufferMaximumDelay;
        private int jitterBufferNominalDelay;
        private byte jitterBufferRate;
        private short lossRate;
        private byte mosCq;
        private byte mosLq;
        private byte noiseLevel;
        private byte packetLossConcealment;
        private byte residualEchoReturnLoss;
        private byte rFactor;
        private int roundTripDelay;
        private byte signalLevel;
        private int sourceSSRC;

        public VoIPMetricsReportBlock() {
            super((short)7);
            this.burstDensity = 0;
            this.burstDuration = 0;
            this.discardRate = 0;
            this.endSystemDelay = 0;
            this.extRFactor = 127;
            this.gapDensity = 0;
            this.gapDuration = 0;
            this.gMin = 16;
            this.jitterBufferAdaptive = 0;
            this.jitterBufferRate = 0;
            this.lossRate = 0;
            this.mosCq = 127;
            this.mosLq = 127;
            this.noiseLevel = 127;
            this.packetLossConcealment = 0;
            this.residualEchoReturnLoss = 127;
            this.rFactor = 127;
            this.roundTripDelay = 0;
            this.signalLevel = 127;
        }

        public VoIPMetricsReportBlock(int blockLength, DataInputStream datainputstream) throws IOException {
            this();
            if (blockLength != 8) {
                throw new IOException("Invalid RTCP XR block length.");
            } else {
                this.setSourceSSRC(datainputstream.readInt());
                this.setLossRate((short)datainputstream.readUnsignedByte());
                this.setDiscardRate((short)datainputstream.readUnsignedByte());
                this.setBurstDensity((short)datainputstream.readUnsignedByte());
                this.setGapDensity((short)datainputstream.readUnsignedByte());
                this.setBurstDuration(datainputstream.readUnsignedShort());
                this.setGapDuration(datainputstream.readUnsignedShort());
                this.setRoundTripDelay(datainputstream.readUnsignedShort());
                this.setEndSystemDelay(datainputstream.readUnsignedShort());
                this.setSignalLevel(datainputstream.readByte());
                this.setNoiseLevel(datainputstream.readByte());
                this.setResidualEchoReturnLoss(datainputstream.readByte());
                this.setGMin((short)datainputstream.readUnsignedByte());
                this.setRFactor(datainputstream.readByte());
                this.setExtRFactor(datainputstream.readByte());
                this.setMosLq(datainputstream.readByte());
                this.setMosCq(datainputstream.readByte());
                int rxConfig = datainputstream.readUnsignedByte();
                this.setPacketLossConcealment((byte)((rxConfig & 192) >>> 6));
                this.setJitterBufferAdaptive((byte)((rxConfig & 48) >>> 4));
                this.setJitterBufferRate((byte)(rxConfig & 15));
                datainputstream.readByte();
                this.setJitterBufferNominalDelay(datainputstream.readUnsignedShort());
                this.setJitterBufferMaximumDelay(datainputstream.readUnsignedShort());
                this.setJitterBufferAbsoluteMaximumDelay(datainputstream.readUnsignedShort());
            }
        }

        protected void assemble(DataOutputStream dataoutputstream) throws IOException {
            dataoutputstream.writeByte(7);
            dataoutputstream.writeByte(0);
            dataoutputstream.writeShort(8);
            dataoutputstream.writeInt(getSourceSSRC());
            dataoutputstream.writeByte(this.getLossRate());
            dataoutputstream.writeByte(this.getDiscardRate());
            dataoutputstream.writeByte(this.getBurstDensity());
            dataoutputstream.writeByte(this.getGapDensity());
            dataoutputstream.writeShort(this.getBurstDuration());
            dataoutputstream.writeShort(this.getGapDuration());
            dataoutputstream.writeShort(this.getRoundTripDelay());
            dataoutputstream.writeShort(this.getEndSystemDelay());
            dataoutputstream.writeByte(this.getSignalLevel());
            dataoutputstream.writeByte(this.getNoiseLevel());
            dataoutputstream.writeByte(this.getResidualEchoReturnLoss());
            dataoutputstream.writeByte(this.getGMin());
            dataoutputstream.writeByte(this.getRFactor());
            dataoutputstream.writeByte(this.getExtRFactor());
            dataoutputstream.writeByte(this.getMosLq());
            dataoutputstream.writeByte(this.getMosCq());
            dataoutputstream.writeByte((this.getPacketLossConcealment() & 3) << 6 | (this.getJitterBufferAdaptive() & 3) << 4 | this.getJitterBufferRate() & 15);
            dataoutputstream.writeByte(0);
            dataoutputstream.writeShort(this.getJitterBufferNominalDelay());
            dataoutputstream.writeShort(this.getJitterBufferMaximumDelay());
            dataoutputstream.writeShort(this.getJitterBufferAbsoluteMaximumDelay());
        }

        public int calcLength() {
            return 36;
        }

        public short getBurstDensity() {
            return this.burstDensity;
        }

        public int getBurstDuration() {
            return this.burstDuration;
        }

        public short getDiscardRate() {
            return this.discardRate;
        }

        public int getEndSystemDelay() {
            return this.endSystemDelay;
        }

        public byte getExtRFactor() {
            return this.extRFactor;
        }

        public short getGapDensity() {
            return this.gapDensity;
        }

        public int getGapDuration() {
            return this.gapDuration;
        }

        public short getGMin() {
            return this.gMin;
        }

        public int getJitterBufferAbsoluteMaximumDelay() {
            return this.jitterBufferAbsoluteMaximumDelay;
        }

        public byte getJitterBufferAdaptive() {
            return this.jitterBufferAdaptive;
        }

        public int getJitterBufferMaximumDelay() {
            return this.jitterBufferMaximumDelay;
        }

        public int getJitterBufferNominalDelay() {
            return this.jitterBufferNominalDelay;
        }

        public byte getJitterBufferRate() {
            return this.jitterBufferRate;
        }

        public short getLossRate() {
            return this.lossRate;
        }

        public byte getMosCq() {
            return this.mosCq;
        }

        public byte getMosLq() {
            return this.mosLq;
        }

        public byte getNoiseLevel() {
            return this.noiseLevel;
        }

        public byte getPacketLossConcealment() {
            return this.packetLossConcealment;
        }

        public byte getResidualEchoReturnLoss() {
            return this.residualEchoReturnLoss;
        }

        public byte getRFactor() {
            return this.rFactor;
        }

        public int getRoundTripDelay() {
            return this.roundTripDelay;
        }

        public byte getSignalLevel() {
            return this.signalLevel;
        }

        public int getSourceSSRC() {
            return this.sourceSSRC;
        }

        public void setBurstDensity(short burstDensity) {
            this.burstDensity = burstDensity;
        }

        public void setBurstDuration(int burstDuration) {
            this.burstDuration = burstDuration;
        }

        public void setDiscardRate(short discardRate) {
            this.discardRate = discardRate;
        }

        public void setEndSystemDelay(int endSystemDelay) {
            this.endSystemDelay = endSystemDelay;
        }

        public void setExtRFactor(byte extRFactor) {
            this.extRFactor = extRFactor;
        }

        public void setGapDensity(short gapDensity) {
            this.gapDensity = gapDensity;
        }

        public void setGapDuration(int gapDuration) {
            this.gapDuration = gapDuration;
        }

        public void setGMin(short gMin) {
            this.gMin = gMin;
        }

        public void setJitterBufferAbsoluteMaximumDelay(int jitterBufferAbsoluteMaximumDelay) {
            this.jitterBufferAbsoluteMaximumDelay = jitterBufferAbsoluteMaximumDelay;
        }

        public void setJitterBufferAdaptive(byte jitterBufferAdaptive) {
            switch(jitterBufferAdaptive) {
                case 0:
                case 1:
                case 2:
                case 3:
                    this.jitterBufferAdaptive = jitterBufferAdaptive;
                    return;
                default:
                    throw new IllegalArgumentException("jitterBufferAdaptive");
            }
        }

        public void setJitterBufferMaximumDelay(int jitterBufferMaximumDelay) {
            this.jitterBufferMaximumDelay = jitterBufferMaximumDelay;
        }

        public void setJitterBufferNominalDelay(int jitterBufferNominalDelay) {
            this.jitterBufferNominalDelay = jitterBufferNominalDelay;
        }

        public void setJitterBufferRate(byte jitterBufferRate) {
            this.jitterBufferRate = jitterBufferRate;
        }

        public void setLossRate(short lossRate) {
            this.lossRate = lossRate;
        }

        public void setMosCq(byte mosCq) {
            this.mosCq = mosCq;
        }

        public void setMosLq(byte mosLq) {
            this.mosLq = mosLq;
        }

        public void setNoiseLevel(byte noiseLevel) {
            this.noiseLevel = noiseLevel;
        }

        public void setPacketLossConcealment(byte packetLossConcealment) {
            switch(packetLossConcealment) {
                case 0:
                case 1:
                case 2:
                case 3:
                    this.packetLossConcealment = packetLossConcealment;
                    return;
                default:
                    throw new IllegalArgumentException("packetLossConcealment");
            }
        }

        public void setResidualEchoReturnLoss(byte residualEchoReturnLoss) {
            this.residualEchoReturnLoss = residualEchoReturnLoss;
        }

        public void setRFactor(byte rFactor) {
            this.rFactor = rFactor;
        }

        public void setRoundTripDelay(int roundTripDelay) {
            this.roundTripDelay = roundTripDelay;
        }

        public void setSignalLevel(byte signalLevel) {
            this.signalLevel = signalLevel;
        }

        public void setSourceSSRC(int sourceSSRC) {
            this.sourceSSRC = sourceSSRC;
        }

        public String toString() {
            StringBuilder s = new StringBuilder("VoIP Metrics");
            s.append(", SSRC of source ").append((long)this.getSourceSSRC() & 4294967295L);
            s.append(", loss rate ").append(this.getLossRate());
            s.append(", discard rate ").append(this.getDiscardRate());
            s.append(", burst density ").append(this.getBurstDensity());
            s.append(", gap density ").append(this.getGapDensity());
            s.append(", burst duration ").append(this.getBurstDuration());
            s.append(", gap duration ").append(this.getGapDuration());
            s.append(", round trip delay ").append(this.getRoundTripDelay());
            return s.toString();
        }
    }

    public abstract static class ReportBlock {
        public final short blockType;

        protected ReportBlock(short blockType) {
            this.blockType = blockType;
        }

        protected abstract void assemble(DataOutputStream var1) throws IOException;

        public int calcLength() {
            return 4;
        }
    }



}

