package net.java.sip.communicator.impl.protocol.jabber.extensions.mailnotification;

import net.java.sip.communicator.util.Logger;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;

public class MailboxIQProvider implements IQProvider {
    private static final Logger logger = Logger.getLogger(MailboxIQProvider.class);

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        MailboxIQ mailboxIQ = new MailboxIQ();
        String resultTimeStr = parser.getAttributeValue("", "result-time");
        if (resultTimeStr != null) {
            mailboxIQ.setResultTime(Long.parseLong(resultTimeStr));
        }
        String totalMatchedStr = parser.getAttributeValue("", "total-matched");
        if (totalMatchedStr != null) {
            mailboxIQ.setTotalMatched(Integer.parseInt(totalMatchedStr));
        }
        String totalEstimateStr = parser.getAttributeValue("", "total-estimate");
        if (totalEstimateStr != null) {
            mailboxIQ.setTotalEstimate("1".equals(totalEstimateStr));
        }
        mailboxIQ.setUrl(parser.getAttributeValue("", "url"));
        int eventType = parser.next();
        while (eventType != 3) {
            if (eventType == 2) {
                if (MailThreadInfo.ELEMENT_NAME.equals(parser.getName())) {
                    mailboxIQ.addThread(MailThreadInfo.parse(parser));
                }
            } else if (logger.isTraceEnabled()) {
                logger.trace("xml parser returned eventType=" + eventType);
                if (logger.isTraceEnabled()) {
                    logger.trace("parser=" + parser.getText());
                }
            }
            eventType = parser.next();
        }
        return mailboxIQ;
    }
}
