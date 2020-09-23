package javax.media.rtp.event;

import javax.media.rtp.Participant;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.SessionManager;

public class ApplicationEvent extends ReceiveStreamEvent {
    private byte[] appData;
    private String appString;
    private int appSubtype;

    public ApplicationEvent(SessionManager from, Participant participant, ReceiveStream recvStream, int appSubtype, String appString, byte[] appData) {
        super(from, recvStream, participant);
        this.appSubtype = appSubtype;
        this.appString = appString;
        this.appData = appData;
    }

    public byte[] getAppData() {
        return this.appData;
    }

    public String getAppString() {
        return this.appString;
    }

    public int getAppSubType() {
        return this.appSubtype;
    }
}
