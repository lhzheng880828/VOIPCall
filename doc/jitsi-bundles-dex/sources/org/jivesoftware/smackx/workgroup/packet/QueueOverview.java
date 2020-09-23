package org.jivesoftware.smackx.workgroup.packet;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smackx.workgroup.agent.WorkgroupQueue.Status;

public class QueueOverview implements PacketExtension {
    private static final String DATE_FORMAT = "yyyyMMdd'T'HH:mm:ss";
    public static String ELEMENT_NAME = "notify-queue";
    public static String NAMESPACE = "http://jabber.org/protocol/workgroup";
    private int averageWaitTime = -1;
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    private Date oldestEntry = null;
    private Status status = null;
    private int userCount = -1;

    public static class Provider implements PacketExtensionProvider {
        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            int eventType = parser.getEventType();
            QueueOverview queueOverview = new QueueOverview();
            SimpleDateFormat dateFormat = new SimpleDateFormat(QueueOverview.DATE_FORMAT);
            if (eventType != 2) {
            }
            eventType = parser.next();
            while (true) {
                if (eventType == 3 && QueueOverview.ELEMENT_NAME.equals(parser.getName())) {
                    if (eventType != 3) {
                    }
                    return queueOverview;
                }
                if ("count".equals(parser.getName())) {
                    queueOverview.setUserCount(Integer.parseInt(parser.nextText()));
                } else if ("time".equals(parser.getName())) {
                    queueOverview.setAverageWaitTime(Integer.parseInt(parser.nextText()));
                } else if ("oldest".equals(parser.getName())) {
                    queueOverview.setOldestEntry(dateFormat.parse(parser.nextText()));
                } else if ("status".equals(parser.getName())) {
                    queueOverview.setStatus(Status.fromString(parser.nextText()));
                }
                eventType = parser.next();
                if (eventType != 3) {
                }
            }
        }
    }

    QueueOverview() {
    }

    /* access modifiers changed from: 0000 */
    public void setAverageWaitTime(int averageWaitTime) {
        this.averageWaitTime = averageWaitTime;
    }

    public int getAverageWaitTime() {
        return this.averageWaitTime;
    }

    /* access modifiers changed from: 0000 */
    public void setOldestEntry(Date oldestEntry) {
        this.oldestEntry = oldestEntry;
    }

    public Date getOldestEntry() {
        return this.oldestEntry;
    }

    /* access modifiers changed from: 0000 */
    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public int getUserCount() {
        return this.userCount;
    }

    public Status getStatus() {
        return this.status;
    }

    /* access modifiers changed from: 0000 */
    public void setStatus(Status status) {
        this.status = status;
    }

    public String getElementName() {
        return ELEMENT_NAME;
    }

    public String getNamespace() {
        return NAMESPACE;
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append(Separators.LESS_THAN).append(ELEMENT_NAME).append(" xmlns=\"").append(NAMESPACE).append("\">");
        if (this.userCount != -1) {
            buf.append("<count>").append(this.userCount).append("</count>");
        }
        if (this.oldestEntry != null) {
            buf.append("<oldest>").append(this.dateFormat.format(this.oldestEntry)).append("</oldest>");
        }
        if (this.averageWaitTime != -1) {
            buf.append("<time>").append(this.averageWaitTime).append("</time>");
        }
        if (this.status != null) {
            buf.append("<status>").append(this.status).append("</status>");
        }
        buf.append("</").append(ELEMENT_NAME).append(Separators.GREATER_THAN);
        return buf.toString();
    }
}
