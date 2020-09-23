package javax.sdp;

import java.util.Date;

public interface Time extends Field {
    Date getStart() throws SdpParseException;

    Date getStop() throws SdpParseException;

    boolean getTypedTime();

    boolean isZero();

    void setStart(Date date) throws SdpException;

    void setStop(Date date) throws SdpException;

    void setTypedTime(boolean z);

    void setZero();
}
