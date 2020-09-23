package org.jivesoftware.smackx.packet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class DelayInformation implements PacketExtension {
    public static final DateFormat XEP_0091_UTC_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
    private String from;
    private String reason;
    private Date stamp;

    static {
        XEP_0091_UTC_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public DelayInformation(Date stamp) {
        this.stamp = stamp;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Date getStamp() {
        return this.stamp;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getElementName() {
        return "x";
    }

    public String getNamespace() {
        return "jabber:x:delay";
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append(Separators.LESS_THAN).append(getElementName()).append(" xmlns=\"").append(getNamespace()).append(Separators.DOUBLE_QUOTE);
        buf.append(" stamp=\"");
        synchronized (XEP_0091_UTC_FORMAT) {
            buf.append(XEP_0091_UTC_FORMAT.format(this.stamp));
        }
        buf.append(Separators.DOUBLE_QUOTE);
        if (this.from != null && this.from.length() > 0) {
            buf.append(" from=\"").append(this.from).append(Separators.DOUBLE_QUOTE);
        }
        buf.append(Separators.GREATER_THAN);
        if (this.reason != null && this.reason.length() > 0) {
            buf.append(this.reason);
        }
        buf.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        return buf.toString();
    }
}
