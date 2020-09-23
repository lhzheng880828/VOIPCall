package gov.nist.javax.sdp;

import gov.nist.javax.sdp.fields.RepeatField;
import gov.nist.javax.sdp.fields.TimeField;
import java.util.Vector;
import javax.sdp.SdpException;
import javax.sdp.Time;
import javax.sdp.TimeDescription;

public class TimeDescriptionImpl implements TimeDescription {
    private Vector repeatList;
    private TimeField timeImpl;

    public TimeDescriptionImpl() {
        this.timeImpl = new TimeField();
        this.repeatList = new Vector();
    }

    public TimeDescriptionImpl(TimeField timeField) {
        this.timeImpl = timeField;
        this.repeatList = new Vector();
    }

    public Time getTime() {
        return this.timeImpl;
    }

    public void setTime(Time timeField) throws SdpException {
        if (timeField == null) {
            throw new SdpException("The parameter is null");
        } else if (timeField instanceof TimeField) {
            this.timeImpl = (TimeField) timeField;
        } else {
            throw new SdpException("The parameter is not an instance of TimeField");
        }
    }

    public Vector getRepeatTimes(boolean create) {
        return this.repeatList;
    }

    public void setRepeatTimes(Vector repeatTimes) throws SdpException {
        this.repeatList = repeatTimes;
    }

    public void addRepeatField(RepeatField repeatField) {
        if (repeatField == null) {
            throw new NullPointerException("null repeatField");
        }
        this.repeatList.add(repeatField);
    }

    public String toString() {
        String retval = this.timeImpl.encode();
        for (int i = 0; i < this.repeatList.size(); i++) {
            retval = retval + ((RepeatField) this.repeatList.elementAt(i)).encode();
        }
        return retval;
    }
}
