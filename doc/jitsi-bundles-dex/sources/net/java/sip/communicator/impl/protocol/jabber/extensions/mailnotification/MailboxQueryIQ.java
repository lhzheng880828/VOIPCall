package net.java.sip.communicator.impl.protocol.jabber.extensions.mailnotification;

import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.IQ;

public class MailboxQueryIQ extends IQ {
    public static final String NAMESPACE = "google:mail:notify";
    private static final Logger logger = Logger.getLogger(MailboxQueryIQ.class);
    private long newerThanTid = -1;
    private long newerThanTime = -1;

    public String getChildElementXML() {
        if (logger.isDebugEnabled()) {
            logger.debug("QueryNotify.getChildElementXML usage");
        }
        StringBuffer xml = new StringBuffer("<query xmlns='google:mail:notify'");
        if (getNewerThanTime() != -1) {
            xml.append("newer-than-time='").append(getNewerThanTime()).append(Separators.QUOTE);
        }
        if (getNewerThanTid() != -1) {
            xml.append("newer-than-tid='").append(getNewerThanTid()).append(Separators.QUOTE);
        }
        xml.append("/>");
        return xml.toString();
    }

    public void setNewerThanTime(long newerThanTime) {
        this.newerThanTime = newerThanTime;
    }

    public long getNewerThanTime() {
        return this.newerThanTime;
    }

    public void setNewerThanTid(long newerThanTid) {
        this.newerThanTid = newerThanTid;
    }

    public long getNewerThanTid() {
        return this.newerThanTid;
    }
}
