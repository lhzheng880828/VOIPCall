package org.jitsi.gov.nist.javax.sip.header;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import javax.sdp.SdpConstants;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.core.Separators;

public class SIPDate implements Cloneable, Serializable {
    public static final String APR = "Apr";
    public static final String AUG = "Aug";
    public static final String DEC = "Dec";
    public static final String FEB = "Feb";
    public static final String FRI = "Fri";
    public static final String GMT = "GMT";
    public static final String JAN = "Jan";
    public static final String JUL = "Jul";
    public static final String JUN = "Jun";
    public static final String MAR = "Mar";
    public static final String MAY = "May";
    public static final String MON = "Mon";
    public static final String NOV = "Nov";
    public static final String OCT = "Oct";
    public static final String SAT = "Sat";
    public static final String SEP = "Sep";
    public static final String SUN = "Sun";
    public static final String THU = "Thu";
    public static final String TUE = "Tue";
    public static final String WED = "Wed";
    private static final long serialVersionUID = 8544101899928346909L;
    protected int day;
    protected int hour;
    private Calendar javaCal;
    protected int minute;
    protected int month;
    protected int second;
    protected String sipMonth;
    protected String sipWkDay;
    protected int wkday;
    protected int year;

    public boolean equals(Object that) {
        if (that.getClass() != getClass()) {
            return false;
        }
        SIPDate other = (SIPDate) that;
        if (this.wkday == other.wkday && this.day == other.day && this.month == other.month && this.year == other.year && this.hour == other.hour && this.minute == other.minute && this.second == other.second) {
            return true;
        }
        return false;
    }

    public SIPDate() {
        this.wkday = -1;
        this.day = -1;
        this.month = -1;
        this.year = -1;
        this.hour = -1;
        this.minute = -1;
        this.second = -1;
        this.javaCal = null;
    }

    public SIPDate(long timeMillis) {
        this.javaCal = new GregorianCalendar(TimeZone.getTimeZone("GMT:0"), Locale.getDefault());
        this.javaCal.setTime(new Date(timeMillis));
        this.wkday = this.javaCal.get(7);
        switch (this.wkday) {
            case 1:
                this.sipWkDay = "Sun";
                break;
            case 2:
                this.sipWkDay = "Mon";
                break;
            case 3:
                this.sipWkDay = "Tue";
                break;
            case 4:
                this.sipWkDay = "Wed";
                break;
            case 5:
                this.sipWkDay = "Thu";
                break;
            case 6:
                this.sipWkDay = "Fri";
                break;
            case 7:
                this.sipWkDay = "Sat";
                break;
            default:
                InternalErrorHandler.handleException("No date map for wkday " + this.wkday);
                break;
        }
        this.day = this.javaCal.get(5);
        this.month = this.javaCal.get(2);
        switch (this.month) {
            case 0:
                this.sipMonth = "Jan";
                break;
            case 1:
                this.sipMonth = "Feb";
                break;
            case 2:
                this.sipMonth = "Mar";
                break;
            case 3:
                this.sipMonth = "Apr";
                break;
            case 4:
                this.sipMonth = "May";
                break;
            case 5:
                this.sipMonth = "Jun";
                break;
            case 6:
                this.sipMonth = "Jul";
                break;
            case 7:
                this.sipMonth = "Aug";
                break;
            case 8:
                this.sipMonth = "Sep";
                break;
            case 9:
                this.sipMonth = "Oct";
                break;
            case 10:
                this.sipMonth = "Nov";
                break;
            case SdpConstants.L16_1CH /*11*/:
                this.sipMonth = "Dec";
                break;
            default:
                InternalErrorHandler.handleException("No date map for month " + this.month);
                break;
        }
        this.year = this.javaCal.get(1);
        this.hour = this.javaCal.get(11);
        this.minute = this.javaCal.get(12);
        this.second = this.javaCal.get(13);
    }

    public StringBuilder encode(StringBuilder encoding) {
        String dayString;
        String hourString;
        String minuteString;
        String secondString;
        if (this.day < 10) {
            dayString = SdpConstants.RESERVED + this.day;
        } else {
            dayString = "" + this.day;
        }
        if (this.hour < 10) {
            hourString = SdpConstants.RESERVED + this.hour;
        } else {
            hourString = "" + this.hour;
        }
        if (this.minute < 10) {
            minuteString = SdpConstants.RESERVED + this.minute;
        } else {
            minuteString = "" + this.minute;
        }
        if (this.second < 10) {
            secondString = SdpConstants.RESERVED + this.second;
        } else {
            secondString = "" + this.second;
        }
        if (this.sipWkDay != null) {
            encoding.append(this.sipWkDay).append(Separators.COMMA).append(Separators.SP);
        }
        encoding.append(dayString).append(Separators.SP);
        if (this.sipMonth != null) {
            encoding.append(this.sipMonth).append(Separators.SP);
        }
        return encoding.append(this.year).append(Separators.SP).append(hourString).append(Separators.COLON).append(minuteString).append(Separators.COLON).append(secondString).append(Separators.SP).append("GMT");
    }

    public Calendar getJavaCal() {
        if (this.javaCal == null) {
            setJavaCal();
        }
        return this.javaCal;
    }

    public String getWkday() {
        return this.sipWkDay;
    }

    public String getMonth() {
        return this.sipMonth;
    }

    public int getHour() {
        return this.hour;
    }

    public int getMinute() {
        return this.minute;
    }

    public int getSecond() {
        return this.second;
    }

    private void setJavaCal() {
        this.javaCal = new GregorianCalendar(TimeZone.getTimeZone("GMT:0"), Locale.getDefault());
        if (this.year != -1) {
            this.javaCal.set(1, this.year);
        }
        if (this.day != -1) {
            this.javaCal.set(5, this.day);
        }
        if (this.month != -1) {
            this.javaCal.set(2, this.month);
        }
        if (this.wkday != -1) {
            this.javaCal.set(7, this.wkday);
        }
        if (this.hour != -1) {
            this.javaCal.set(10, this.hour);
        }
        if (this.minute != -1) {
            this.javaCal.set(12, this.minute);
        }
        if (this.second != -1) {
            this.javaCal.set(13, this.second);
        }
    }

    public void setWkday(String w) throws IllegalArgumentException {
        this.sipWkDay = w;
        if (this.sipWkDay.compareToIgnoreCase("Mon") == 0) {
            this.wkday = 2;
        } else if (this.sipWkDay.compareToIgnoreCase("Tue") == 0) {
            this.wkday = 3;
        } else if (this.sipWkDay.compareToIgnoreCase("Wed") == 0) {
            this.wkday = 4;
        } else if (this.sipWkDay.compareToIgnoreCase("Thu") == 0) {
            this.wkday = 5;
        } else if (this.sipWkDay.compareToIgnoreCase("Fri") == 0) {
            this.wkday = 6;
        } else if (this.sipWkDay.compareToIgnoreCase("Sat") == 0) {
            this.wkday = 7;
        } else if (this.sipWkDay.compareToIgnoreCase("Sun") == 0) {
            this.wkday = 1;
        } else {
            throw new IllegalArgumentException("Illegal Week day :" + w);
        }
    }

    public void setDay(int d) throws IllegalArgumentException {
        if (d < 1 || d > 31) {
            throw new IllegalArgumentException("Illegal Day of the month " + Integer.toString(d));
        }
        this.day = d;
    }

    public void setMonth(String m) throws IllegalArgumentException {
        this.sipMonth = m;
        if (this.sipMonth.compareToIgnoreCase("Jan") == 0) {
            this.month = 0;
        } else if (this.sipMonth.compareToIgnoreCase("Feb") == 0) {
            this.month = 1;
        } else if (this.sipMonth.compareToIgnoreCase("Mar") == 0) {
            this.month = 2;
        } else if (this.sipMonth.compareToIgnoreCase("Apr") == 0) {
            this.month = 3;
        } else if (this.sipMonth.compareToIgnoreCase("May") == 0) {
            this.month = 4;
        } else if (this.sipMonth.compareToIgnoreCase("Jun") == 0) {
            this.month = 5;
        } else if (this.sipMonth.compareToIgnoreCase("Jul") == 0) {
            this.month = 6;
        } else if (this.sipMonth.compareToIgnoreCase("Aug") == 0) {
            this.month = 7;
        } else if (this.sipMonth.compareToIgnoreCase("Sep") == 0) {
            this.month = 8;
        } else if (this.sipMonth.compareToIgnoreCase("Oct") == 0) {
            this.month = 9;
        } else if (this.sipMonth.compareToIgnoreCase("Nov") == 0) {
            this.month = 10;
        } else if (this.sipMonth.compareToIgnoreCase("Dec") == 0) {
            this.month = 11;
        } else {
            throw new IllegalArgumentException("Illegal Month :" + m);
        }
    }

    public void setYear(int y) throws IllegalArgumentException {
        if (y < 0) {
            throw new IllegalArgumentException("Illegal year : " + y);
        }
        this.javaCal = null;
        this.year = y;
    }

    public int getYear() {
        return this.year;
    }

    public void setHour(int h) throws IllegalArgumentException {
        if (h < 0 || h > 24) {
            throw new IllegalArgumentException("Illegal hour : " + h);
        }
        this.javaCal = null;
        this.hour = h;
    }

    public void setMinute(int m) throws IllegalArgumentException {
        if (m < 0 || m >= 60) {
            throw new IllegalArgumentException("Illegal minute : " + Integer.toString(m));
        }
        this.javaCal = null;
        this.minute = m;
    }

    public void setSecond(int s) throws IllegalArgumentException {
        if (s < 0 || s >= 60) {
            throw new IllegalArgumentException("Illegal second : " + Integer.toString(s));
        }
        this.javaCal = null;
        this.second = s;
    }

    public int getDeltaSeconds() {
        return ((int) (getJavaCal().getTime().getTime() - System.currentTimeMillis())) / 1000;
    }

    public Object clone() {
        try {
            SIPDate retval = (SIPDate) super.clone();
            if (this.javaCal != null) {
                retval.javaCal = (Calendar) this.javaCal.clone();
            }
            return retval;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Internal error");
        }
    }
}
