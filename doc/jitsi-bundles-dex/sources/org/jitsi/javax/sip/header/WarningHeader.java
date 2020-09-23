package org.jitsi.javax.sip.header;

import java.text.ParseException;
import org.jitsi.javax.sip.InvalidArgumentException;

public interface WarningHeader extends Header {
    public static final int ATTRIBUTE_NOT_UNDERSTOOD = 306;
    public static final int INCOMPATIBLE_BANDWIDTH_UNITS = 303;
    public static final int INCOMPATIBLE_MEDIA_FORMAT = 305;
    public static final int INCOMPATIBLE_NETWORK_ADDRESS_FORMATS = 301;
    public static final int INCOMPATIBLE_NETWORK_PROTOCOL = 300;
    public static final int INCOMPATIBLE_TRANSPORT_PROTOCOL = 302;
    public static final int INSUFFICIENT_BANDWIDTH = 370;
    public static final int MEDIA_TYPE_NOT_AVAILABLE = 304;
    public static final int MISCELLANEOUS_WARNING = 399;
    public static final int MULTICAST_NOT_AVAILABLE = 330;
    public static final String NAME = "Warning";
    public static final int SESSION_DESCRIPTION_PARAMETER_NOT_UNDERSTOOD = 307;
    public static final int UNICAST_NOT_AVAILABLE = 331;

    String getAgent();

    int getCode();

    String getText();

    void setAgent(String str) throws ParseException;

    void setCode(int i) throws InvalidArgumentException;

    void setText(String str) throws ParseException;
}
