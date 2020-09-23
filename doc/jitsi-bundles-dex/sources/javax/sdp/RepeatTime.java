package javax.sdp;

public interface RepeatTime extends Field {
    int getActiveDuration() throws SdpParseException;

    int[] getOffsetArray() throws SdpParseException;

    int getRepeatInterval() throws SdpParseException;

    boolean getTypedTime() throws SdpParseException;

    void setActiveDuration(int i) throws SdpException;

    void setOffsetArray(int[] iArr) throws SdpException;

    void setRepeatInterval(int i) throws SdpException;

    void setTypedTime(boolean z);
}
