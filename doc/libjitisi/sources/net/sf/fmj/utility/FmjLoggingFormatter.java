package net.sf.fmj.utility;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class FmjLoggingFormatter extends Formatter {
    private static final String format = "{0,date} {0,time}";
    private final boolean NO_FIRST_LINE = true;
    private Object[] args = new Object[1];
    Date dat = new Date();
    private MessageFormat formatter;
    private String lineSeparator = System.getProperty("line.separator");

    public synchronized String format(LogRecord record) {
        StringBuffer sb;
        sb = new StringBuffer();
        this.dat.setTime(record.getMillis());
        this.args[0] = this.dat;
        StringBuffer text = new StringBuffer();
        String message = formatMessage(record);
        sb.append(record.getLevel().getLocalizedName());
        sb.append(": ");
        sb.append(message);
        sb.append(this.lineSeparator);
        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception e) {
            }
        }
        return sb.toString();
    }
}
