package net.sf.fmj.media.rtp;

import java.util.HashMap;
import java.util.Vector;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPStream;
import javax.media.rtp.rtcp.Report;
import javax.media.rtp.rtcp.SourceDescription;

public class RTPParticipant implements Participant {
    private boolean active = false;
    private String cName = "";
    protected long lastReportTime = System.currentTimeMillis();
    private HashMap rtcpReports = new HashMap();
    private int sdesSize = 0;
    protected HashMap sourceDescriptions = new HashMap();
    private Vector streams = new Vector();

    public RTPParticipant(String cName) {
        this.cName = cName;
        addSourceDescription(new SourceDescription(1, cName, 1, false));
        addSourceDescription(new SourceDescription(2, cName, 1, false));
    }

    public void addReport(Report report) {
        this.lastReportTime = System.currentTimeMillis();
        this.rtcpReports.put(new Long(report.getSSRC()), report);
        Vector sdes = report.getSourceDescription();
        for (int i = 0; i < sdes.size(); i++) {
            addSourceDescription((SourceDescription) sdes.get(i));
        }
        if (this.streams.size() == 0 && (report instanceof RTCPReport)) {
            ((RTCPReport) report).sourceDescriptions = new Vector(this.sourceDescriptions.values());
        }
    }

    /* access modifiers changed from: protected */
    public void addSourceDescription(SourceDescription sdes) {
        SourceDescription oldSdes = (SourceDescription) this.sourceDescriptions.get(new Integer(sdes.getType()));
        if (oldSdes != null) {
            this.sdesSize -= oldSdes.getDescription().length();
            this.sdesSize -= 2;
        }
        this.sourceDescriptions.put(new Integer(sdes.getType()), sdes);
        this.sdesSize += 2;
        this.sdesSize += sdes.getDescription().length();
    }

    /* access modifiers changed from: protected */
    public void addStream(RTPStream stream) {
        this.streams.add(stream);
    }

    public String getCNAME() {
        return this.cName;
    }

    public long getLastReportTime() {
        return this.lastReportTime;
    }

    public Vector getReports() {
        return new Vector(this.rtcpReports.values());
    }

    public int getSdesSize() {
        return this.sdesSize;
    }

    public Vector getSourceDescription() {
        return new Vector(this.sourceDescriptions.values());
    }

    public Vector getStreams() {
        return this.streams;
    }

    public boolean isActive() {
        return this.active;
    }

    /* access modifiers changed from: protected */
    public void removeStream(RTPStream stream) {
        this.streams.remove(stream);
    }

    /* access modifiers changed from: protected */
    public void setActive(boolean active) {
        this.active = active;
    }
}
