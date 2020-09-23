package org.jivesoftware.smackx.provider;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.jitsi.gov.nist.javax.sip.parser.TokenNames;
import org.jitsi.org.xmlpull.v1.XmlPullParser;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.DelayInformation;

public class DelayInformationProvider implements PacketExtensionProvider {
    private static final SimpleDateFormat XEP_0082_UTC_FORMAT_WITHOUT_MILLIS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final SimpleDateFormat XEP_0091_UTC_FALLBACK_FORMAT = new SimpleDateFormat("yyyyMd'T'HH:mm:ss");
    private static Map<String, DateFormat> formats = new HashMap();

    static {
        XEP_0091_UTC_FALLBACK_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        XEP_0082_UTC_FORMAT_WITHOUT_MILLIS.setTimeZone(TimeZone.getTimeZone("UTC"));
        formats.put("^\\d+T\\d+:\\d+:\\d+$", DelayInformation.XEP_0091_UTC_FORMAT);
        formats.put("^\\d+-\\d+-\\d+T\\d+:\\d+:\\d+\\.\\d+Z$", StringUtils.XEP_0082_UTC_FORMAT);
        formats.put("^\\d+-\\d+-\\d+T\\d+:\\d+:\\d+Z$", XEP_0082_UTC_FORMAT_WITHOUT_MILLIS);
    }

    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        String stampString = parser.getAttributeValue("", "stamp");
        Date stamp = null;
        DateFormat format = null;
        for (String regexp : formats.keySet()) {
            if (stampString.matches(regexp)) {
                try {
                    format = (DateFormat) formats.get(regexp);
                    synchronized (format) {
                        stamp = format.parse(stampString);
                        break;
                    }
                } catch (ParseException e) {
                }
            }
        }
        if (format == DelayInformation.XEP_0091_UTC_FORMAT && stampString.split(TokenNames.T)[0].length() < 8) {
            stamp = handleDateWithMissingLeadingZeros(stampString);
        }
        if (stamp == null) {
            stamp = new Date();
        }
        DelayInformation delayInformation = new DelayInformation(stamp);
        delayInformation.setFrom(parser.getAttributeValue("", "from"));
        String reason = parser.nextText();
        if ("".equals(reason)) {
            reason = null;
        }
        delayInformation.setReason(reason);
        return delayInformation;
    }

    private Date handleDateWithMissingLeadingZeros(String stampString) {
        Calendar now = new GregorianCalendar();
        Calendar xep91 = parseXEP91Date(stampString, DelayInformation.XEP_0091_UTC_FORMAT);
        Calendar xep91Fallback = parseXEP91Date(stampString, XEP_0091_UTC_FALLBACK_FORMAT);
        List<Calendar> dates = filterDatesBefore(now, xep91, xep91Fallback);
        if (dates.isEmpty()) {
            return null;
        }
        return determineNearestDate(now, dates).getTime();
    }

    private Calendar parseXEP91Date(String stampString, DateFormat dateFormat) {
        try {
            Calendar calendar;
            synchronized (dateFormat) {
                dateFormat.parse(stampString);
                calendar = dateFormat.getCalendar();
            }
            return calendar;
        } catch (ParseException e) {
            return null;
        }
    }

    private List<Calendar> filterDatesBefore(Calendar now, Calendar... dates) {
        List<Calendar> result = new ArrayList();
        for (Calendar calendar : dates) {
            if (calendar != null && calendar.before(now)) {
                result.add(calendar);
            }
        }
        return result;
    }

    private Calendar determineNearestDate(final Calendar now, List<Calendar> dates) {
        Collections.sort(dates, new Comparator<Calendar>() {
            public int compare(Calendar o1, Calendar o2) {
                return new Long(now.getTimeInMillis() - o1.getTimeInMillis()).compareTo(new Long(now.getTimeInMillis() - o2.getTimeInMillis()));
            }
        });
        return (Calendar) dates.get(0);
    }
}
