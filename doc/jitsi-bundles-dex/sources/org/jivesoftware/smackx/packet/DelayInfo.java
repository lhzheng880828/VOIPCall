package org.jivesoftware.smackx.packet;

import java.util.Date;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.util.StringUtils;

public class DelayInfo extends DelayInformation {
    DelayInformation wrappedInfo;

    public DelayInfo(DelayInformation delay) {
        super(delay.getStamp());
        this.wrappedInfo = delay;
    }

    public String getFrom() {
        return this.wrappedInfo.getFrom();
    }

    public String getReason() {
        return this.wrappedInfo.getReason();
    }

    public Date getStamp() {
        return this.wrappedInfo.getStamp();
    }

    public void setFrom(String from) {
        this.wrappedInfo.setFrom(from);
    }

    public void setReason(String reason) {
        this.wrappedInfo.setReason(reason);
    }

    public String getElementName() {
        return "delay";
    }

    public String getNamespace() {
        return "urn:xmpp:delay";
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append(Separators.LESS_THAN).append(getElementName()).append(" xmlns=\"").append(getNamespace()).append(Separators.DOUBLE_QUOTE);
        buf.append(" stamp=\"");
        buf.append(StringUtils.formatXEP0082Date(getStamp()));
        buf.append(Separators.DOUBLE_QUOTE);
        if (getFrom() != null && getFrom().length() > 0) {
            buf.append(" from=\"").append(getFrom()).append(Separators.DOUBLE_QUOTE);
        }
        buf.append(Separators.GREATER_THAN);
        if (getReason() != null && getReason().length() > 0) {
            buf.append(getReason());
        }
        buf.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        return buf.toString();
    }
}
