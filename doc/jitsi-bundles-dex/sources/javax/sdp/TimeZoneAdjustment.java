package javax.sdp;

import java.util.Hashtable;

public interface TimeZoneAdjustment extends Field {
    boolean getTypedTime();

    Hashtable getZoneAdjustments(boolean z) throws SdpParseException;

    void setTypedTime(boolean z);

    void setZoneAdjustments(Hashtable hashtable) throws SdpException;
}
