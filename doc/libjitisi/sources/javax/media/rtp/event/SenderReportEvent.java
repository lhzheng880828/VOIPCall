package javax.media.rtp.event;

import javax.media.rtp.SessionManager;
import javax.media.rtp.rtcp.SenderReport;

public class SenderReportEvent extends RemoteEvent {
    private SenderReport report;

    public SenderReportEvent(SessionManager from, SenderReport report) {
        super(from);
        this.report = report;
    }

    public SenderReport getReport() {
        return this.report;
    }
}
