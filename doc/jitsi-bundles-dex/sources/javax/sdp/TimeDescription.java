package javax.sdp;

import java.io.Serializable;
import java.util.Vector;

public interface TimeDescription extends Serializable, Cloneable {
    public static final long NTP_CONST = 2208988800L;

    Vector getRepeatTimes(boolean z);

    Time getTime() throws SdpParseException;

    void setRepeatTimes(Vector vector) throws SdpException;

    void setTime(Time time) throws SdpException;
}
