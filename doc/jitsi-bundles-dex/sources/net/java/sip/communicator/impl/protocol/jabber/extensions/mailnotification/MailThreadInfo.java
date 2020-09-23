package net.java.sip.communicator.impl.protocol.jabber.extensions.mailnotification;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.header.SubscriptionStateHeader;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jitsi.org.xmlpull.v1.XmlPullParserException;

public class MailThreadInfo {
    public static final String ELEMENT_NAME = "mail-thread-info";
    public static final String LABELS_ELEMENT_NAME = "labels";
    public static final int PARTICIPATION_NONE = 0;
    public static final int PARTICIPATION_ONE_OF_MANY = 1;
    public static final int PARTICIPATION_SOLE_RECIPIENT = 2;
    public static final String SENDERS_ELEMENT_NAME = "senders";
    public static final String SNIPPET_ELEMENT_NAME = "snippet";
    public static final String SUBJECT_ELEMENT_NAME = "subject";
    private long date = -1;
    private String formattedDate = null;
    private String labels = null;
    private int messages;
    private int participation = -1;
    private List<Sender> senders = new LinkedList();
    private String snippet = null;
    private String subject = null;
    private String tid = null;
    private String url = null;

    public class Sender {
        public static final String ELEMENT_NAME = "sender";
        public String address = null;
        public String name = null;
        public boolean originator = false;
        public boolean unread = false;

        public String getFirstName() {
            if (this.name == null || this.name.trim().length() == 0) {
                return null;
            }
            String result = this.name.split("\\s")[0];
            if (result.length() > 14) {
                return result.substring(0, 14);
            }
            return result;
        }
    }

    public int getParticipation() {
        return this.participation;
    }

    /* access modifiers changed from: protected */
    public void setParticipation(int participation) {
        this.participation = participation;
    }

    public Iterator<Sender> senders() {
        return this.senders.iterator();
    }

    public int getSenderCount() {
        return this.senders.size();
    }

    public int getUnreadSenderCount() {
        Iterator<Sender> senders = senders();
        int count = 0;
        while (senders.hasNext()) {
            if (((Sender) senders.next()).unread) {
                count++;
            }
        }
        return count;
    }

    public String findOriginator(boolean firstNameOnly) {
        return null;
    }

    /* access modifiers changed from: protected */
    public void addSender(Sender sender) {
        this.senders.add(sender);
    }

    public int getMessageCount() {
        return this.messages;
    }

    /* access modifiers changed from: protected */
    public void setMessageCount(int messageCount) {
        this.messages = messageCount;
    }

    public long getDate() {
        return this.date;
    }

    private String getFormattedDate() {
        if (this.formattedDate != null) {
            return this.formattedDate;
        }
        StringBuffer dateBuff = new StringBuffer();
        Calendar now = Calendar.getInstance();
        Date threadDate = new Date(getDate());
        Calendar threadDateCal = Calendar.getInstance();
        threadDateCal.setTime(new Date(getDate()));
        DateFormat dateFormat = DateFormat.getDateInstance(2);
        DateFormat timeFormat = DateFormat.getTimeInstance(3);
        if (!(now.get(1) == threadDateCal.get(1) && now.get(2) == threadDateCal.get(2) && now.get(5) == threadDateCal.get(5))) {
            dateBuff.append(dateFormat.format(threadDate));
        }
        dateBuff.append(Separators.SP).append(timeFormat.format(threadDate));
        return dateBuff.toString();
    }

    /* access modifiers changed from: protected */
    public void setDate(long date) {
        this.date = date;
    }

    public String getURL() {
        return this.url;
    }

    /* access modifiers changed from: protected */
    public void setURL(String url) {
        this.url = url;
    }

    public String getLabels() {
        return this.labels;
    }

    /* access modifiers changed from: protected */
    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getTid() {
        return this.tid;
    }

    /* access modifiers changed from: protected */
    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getSubject() {
        return this.subject;
    }

    /* access modifiers changed from: protected */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSnippet() {
        return this.snippet;
    }

    /* access modifiers changed from: protected */
    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public static MailThreadInfo parse(XmlPullParser parser) throws XmlPullParserException, NumberFormatException, IOException {
        MailThreadInfo info = new MailThreadInfo();
        info.setTid(parser.getAttributeValue("", "tid"));
        String participationStr = parser.getAttributeValue("", "participation");
        if (participationStr != null) {
            info.setParticipation(Integer.parseInt(participationStr));
        }
        String messagesStr = parser.getAttributeValue("", "messages");
        if (messagesStr != null) {
            info.setMessageCount(Integer.parseInt(messagesStr));
        }
        String dateStr = parser.getAttributeValue("", "date");
        if (dateStr != null) {
            info.setDate(Long.parseLong(dateStr));
        }
        info.setURL(parser.getAttributeValue("", "url"));
        int eventType = parser.next();
        while (eventType != 3) {
            if (eventType == 2) {
                String name = parser.getName();
                if ("senders".equals(name)) {
                    info.parseSenders(parser);
                } else if (LABELS_ELEMENT_NAME.equals(name)) {
                    info.setLabels(parser.nextText());
                } else if ("subject".equals(name)) {
                    info.setSubject(parser.nextText());
                } else if (SNIPPET_ELEMENT_NAME.equals(name)) {
                    info.setSnippet(parser.nextText());
                }
            }
            eventType = parser.next();
        }
        return info;
    }

    private void parseSenders(XmlPullParser parser) throws XmlPullParserException, NumberFormatException, IOException {
        int eventType = parser.next();
        while (eventType != 3) {
            while (eventType != 3) {
                if (Sender.ELEMENT_NAME.equals(parser.getName())) {
                    boolean z;
                    Sender sender = new Sender();
                    sender.address = parser.getAttributeValue("", "address");
                    sender.name = parser.getAttributeValue("", "name");
                    String originatorStr = parser.getAttributeValue("", "originator");
                    if (originatorStr != null) {
                        if (Integer.parseInt(originatorStr) == 1) {
                            z = true;
                        } else {
                            z = false;
                        }
                        sender.originator = z;
                    }
                    String unreadStr = parser.getAttributeValue("", "unread");
                    if (unreadStr != null) {
                        if (Integer.parseInt(unreadStr) == 1) {
                            z = true;
                        } else {
                            z = false;
                        }
                        sender.unread = z;
                    }
                    addSender(sender);
                }
                eventType = parser.next();
            }
            eventType = parser.next();
        }
    }

    private String createParticipantNames() {
        StringBuffer participantNames = new StringBuffer();
        boolean firstNamesOnly = getSenderCount() > 1;
        int remainingSndrsAllowed = 3;
        int maximumUnreadAllowed = Math.min(remainingSndrsAllowed, getUnreadSenderCount());
        int maximumReadAllowed = 3 - maximumUnreadAllowed;
        Iterator<Sender> senders = senders();
        while (senders.hasNext() && remainingSndrsAllowed != 0) {
            Sender sender = (Sender) senders.next();
            String name = firstNamesOnly ? sender.getFirstName() : sender.name;
            if (name == null) {
                if (sender.address != null) {
                    int atIndex = sender.address.indexOf(Separators.AT);
                    if (atIndex != -1) {
                        return sender.address.substring(0, atIndex);
                    }
                    name = sender.address;
                } else {
                    name = SubscriptionStateHeader.UNKNOWN;
                }
            }
            if (sender.unread || maximumReadAllowed != 0) {
                if (remainingSndrsAllowed < 3) {
                    participantNames.append(", ");
                }
                remainingSndrsAllowed--;
                if (sender.unread) {
                    participantNames.append("<b>").append(name).append("</b>");
                    maximumUnreadAllowed--;
                } else {
                    participantNames.append(name);
                    maximumReadAllowed--;
                }
            }
        }
        int messageCount = getMessageCount();
        if (messageCount > 1) {
            participantNames.append(" (").append(messageCount).append(Separators.RPAREN);
        }
        return participantNames.toString();
    }

    public String createHtmlDescription() {
        StringBuffer threadBuff = new StringBuffer();
        threadBuff.append("<tr bgcolor=\"#ffffff\">");
        threadBuff.append("<td>");
        threadBuff.append(createParticipantNames());
        threadBuff.append("</td>");
        threadBuff.append("<td>");
        threadBuff.append(createLabelList()).append("&nbsp;");
        threadBuff.append("<a href=\"");
        threadBuff.append(getURL()).append("\"><b>");
        threadBuff.append(getSubject()).append("</b></a>");
        threadBuff.append("<font color=#7777CC> - ");
        threadBuff.append("<a href=\"");
        threadBuff.append(getURL());
        threadBuff.append("\" style=\"text-decoration:none\">");
        threadBuff.append(getSnippet()).append("</a></font>");
        threadBuff.append("</td>");
        threadBuff.append("<td nowrap>");
        threadBuff.append(getFormattedDate());
        threadBuff.append("</td></tr>");
        return threadBuff.toString();
    }

    private String createLabelList() {
        String[] labelsArray = this.labels.split("\\|");
        StringBuffer labelsList = new StringBuffer();
        for (int i = 0; i < labelsArray.length; i++) {
            String label = labelsArray[i];
            if (!label.startsWith("^")) {
                labelsList.append("<font color=#006633>");
                labelsList.append(label);
                labelsList.append("</font>");
                if (i < labelsArray.length - 1) {
                    labelsList.append(", ");
                }
            }
        }
        return labelsList.toString();
    }
}
