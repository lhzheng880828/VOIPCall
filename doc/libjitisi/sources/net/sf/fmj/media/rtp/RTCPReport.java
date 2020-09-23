package net.sf.fmj.media.rtp;

import com.lti.utils.UnsignedUtils;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;
import javax.media.rtp.Participant;
import javax.media.rtp.rtcp.Report;
import javax.media.rtp.rtcp.SourceDescription;

public abstract class RTCPReport implements Report {
    private String byeReason = "";
    private String cName = null;
    protected Vector feedbackReports = new Vector();
    protected RTCPHeader header;
    private boolean isBye = false;
    protected Participant participant;
    protected int sdesBytes = 0;
    protected Vector sourceDescriptions = new Vector();
    private long ssrc = 0;

    public RTCPReport(byte[] data, int offset, int length) throws IOException {
        this.header = new RTCPHeader(data, offset, length);
        if (this.header.getPadding() == (short) 1) {
            throw new IOException("First packet has padding");
        } else if ((this.header.getLength() + 1) * 4 > length) {
            throw new IOException("Invalid Length");
        } else {
            this.ssrc = this.header.getSsrc();
        }
    }

    public String getByeReason() {
        return this.byeReason;
    }

    public String getCName() {
        return this.cName;
    }

    public Vector getFeedbackReports() {
        return this.feedbackReports;
    }

    public Participant getParticipant() {
        return this.participant;
    }

    public Vector getSourceDescription() {
        return this.sourceDescriptions;
    }

    public long getSSRC() {
        return this.ssrc;
    }

    public boolean isByePacket() {
        return this.isBye;
    }

    /* access modifiers changed from: protected */
    public void readBye(byte[] data, int offset, int length) throws IOException {
        if (length > 0 && new RTCPHeader(data, offset, length).getPacketType() == (short) 203) {
            this.isBye = true;
            if ((length + 1) * 4 > 8) {
                int len = data[offset + 8] & UnsignedUtils.MAX_UBYTE;
                if (len < length - 8 && len > 0) {
                    this.byeReason = new String(data, (offset + 8) + 1, len);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void readFeedbackReports(byte[] data, int offset, int length) throws IOException {
        for (short i = (short) 0; i < this.header.getReceptionCount(); i++) {
            this.feedbackReports.add(new RTCPFeedback(data, offset, length));
            offset += 24;
        }
    }

    /* access modifiers changed from: protected */
    public void readSourceDescription(byte[] data, int offset, int length) throws IOException {
        if (length > 0) {
            RTCPHeader sdesHeader = new RTCPHeader(data, offset, length);
            if (sdesHeader.getPacketType() == (short) 202) {
                this.ssrc = sdesHeader.getSsrc();
                this.sdesBytes = (sdesHeader.getLength() + 1) * 4;
                DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data, offset + 8, length));
                int type = 1;
                while (type != 0) {
                    type = stream.readUnsignedByte();
                    if (type != 0) {
                        byte[] desc = new byte[stream.readUnsignedByte()];
                        stream.readFully(desc);
                        String descStr = new String(desc, "UTF-8");
                        this.sourceDescriptions.add(new SourceDescription(type, descStr, 0, false));
                        if (type == 1) {
                            this.cName = descStr;
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setParticipant(RTPParticipant participant) {
        this.participant = participant;
        if (participant.getStreams().size() == 0) {
            Vector sdes = participant.getSourceDescription();
            for (int i = 0; i < sdes.size(); i++) {
                participant.addSourceDescription((SourceDescription) sdes.get(i));
            }
        }
    }
}
