package net.java.sip.communicator.impl.protocol.jabber.extensions.mailnotification;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.IQ;

public class MailboxIQ extends IQ {
    public static final String ELEMENT_NAME = "mailbox";
    public static final String NAMESPACE = "google:mail:notify";
    private static final Logger logger = Logger.getLogger(MailboxIQ.class);
    private long date;
    private long resultTime;
    private List<MailThreadInfo> threads = null;
    private boolean totalEstimate;
    private int totalMatched;
    private String url;

    public void setDate(long date) {
        this.date = date;
    }

    public long getDate() {
        return this.date;
    }

    public long getResultTime() {
        return this.resultTime;
    }

    public void setResultTime(long l) {
        this.resultTime = l;
    }

    public int getTotalMatched() {
        return this.totalMatched;
    }

    public void setTotalMatched(int totalMatched) {
        this.totalMatched = totalMatched;
    }

    public boolean isTotalEstimate() {
        return this.totalEstimate;
    }

    public void setTotalEstimate(boolean totalEstimate) {
        this.totalEstimate = totalEstimate;
    }

    public String getChildElementXML() {
        if (logger.isDebugEnabled()) {
            logger.debug("Mailbox.getChildElementXML usage");
        }
        return "<mailbox result-time='" + this.resultTime + "' total-matched='" + this.totalMatched + Separators.QUOTE + (this.totalEstimate ? " total-estimate='1' " : "") + "/>";
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    public void addThread(MailThreadInfo threadInfo) {
        if (this.threads == null) {
            this.threads = new LinkedList();
        }
        this.threads.add(threadInfo);
    }

    public int getThreadCount() {
        return this.threads.size();
    }

    public Iterator<MailThreadInfo> threads() {
        return this.threads.iterator();
    }
}
