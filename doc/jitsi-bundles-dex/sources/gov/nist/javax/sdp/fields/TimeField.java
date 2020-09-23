package gov.nist.javax.sdp.fields;

import java.util.Date;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.Time;
import org.jitsi.gov.nist.core.Separators;

public class TimeField extends SDPField implements Time {
    protected long startTime;
    protected long stopTime;

    public TimeField() {
        super(SDPFieldNames.TIME_FIELD);
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getStopTime() {
        return this.stopTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public Date getStart() throws SdpParseException {
        return SdpFactory.getDateFromNtp(this.startTime);
    }

    public Date getStop() throws SdpParseException {
        return SdpFactory.getDateFromNtp(this.stopTime);
    }

    public void setStop(Date stop) throws SdpException {
        if (stop == null) {
            throw new SdpException("The date is null");
        }
        this.stopTime = SdpFactory.getNtpTime(stop);
    }

    public void setStart(Date start) throws SdpException {
        if (start == null) {
            throw new SdpException("The date is null");
        }
        this.startTime = SdpFactory.getNtpTime(start);
    }

    public boolean getTypedTime() {
        return false;
    }

    public void setTypedTime(boolean typedTime) {
    }

    public boolean isZero() {
        return getStartTime() == 0 && getStopTime() == 0;
    }

    public void setZero() {
        setStopTime(0);
        setStartTime(0);
    }

    public String encode() {
        return SDPFieldNames.TIME_FIELD + this.startTime + Separators.SP + this.stopTime + Separators.NEWLINE;
    }
}
