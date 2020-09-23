package net.sf.fmj.utility;

import com.lti.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import javax.media.Buffer;
import javax.media.Format;

public final class LoggingStringUtils {
    public static String bufferFlagsToStr(int flags) {
        List<String> strings = new ArrayList();
        if ((flags & 1) != 0) {
            strings.add("FLAG_EOM");
        }
        if ((flags & 2) != 0) {
            strings.add("FLAG_DISCARD");
        }
        if ((flags & 4) != 0) {
            strings.add("FLAG_SILENCE");
        }
        if ((flags & 8) != 0) {
            strings.add("FLAG_SID");
        }
        if ((flags & 16) != 0) {
            strings.add("FLAG_KEY_FRAME");
        }
        if ((flags & 64) != 0) {
            strings.add("FLAG_NO_WAIT");
        }
        if ((flags & 96) != 0) {
            strings.add("FLAG_NO_SYNC");
        }
        if ((flags & 128) != 0) {
            strings.add("FLAG_SYSTEM_TIME");
        }
        if ((flags & 256) != 0) {
            strings.add("FLAG_RELATIVE_TIME");
        }
        if ((flags & 512) != 0) {
            strings.add("FLAG_FLUSH");
        }
        if ((flags & 1024) != 0) {
            strings.add("FLAG_SYSTEM_MARKER");
        }
        if ((flags & 2048) != 0) {
            strings.add("FLAG_RTP_MARKER");
        }
        if ((flags & 4096) != 0) {
            strings.add("FLAG_RTP_TIME");
        }
        if ((flags & 8192) != 0) {
            strings.add("FLAG_BUF_OVERFLOWN");
        }
        if ((flags & 16384) != 0) {
            strings.add("FLAG_BUF_UNDERFLOWN");
        }
        if ((32768 & flags) != 0) {
            strings.add("FLAG_LIVE_DATA");
        }
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < strings.size(); i++) {
            if (b.length() != 0) {
                b.append(" | ");
            }
            b.append((String) strings.get(i));
        }
        return b.toString();
    }

    public static String bufferToStr(Buffer buffer) {
        if (buffer == null) {
            return "null";
        }
        StringBuffer b = new StringBuffer();
        b.append(buffer);
        b.append(" seq=" + buffer.getSequenceNumber());
        b.append(" off=" + buffer.getOffset());
        b.append(" len=" + buffer.getLength());
        b.append(" flags=[" + bufferFlagsToStr(buffer.getFlags()) + "]");
        b.append(" fmt=[" + buffer.getFormat() + "]");
        if (buffer.getData() != null && (buffer.getData() instanceof byte[])) {
            b.append(" data=[" + buffer.getData() + " " + StringUtils.byteArrayToHexString((byte[]) buffer.getData(), buffer.getLength(), buffer.getOffset()) + "]");
        } else if (buffer.getData() != null) {
            b.append(" data=[" + buffer.getData() + "]");
        } else {
            b.append(" data=[null]");
        }
        return b.toString();
    }

    public static String formatToStr(Format f) {
        return "" + f;
    }

    public static String plugInResultToStr(int result) {
        switch (result) {
            case 0:
                return "BUFFER_PROCESSED_OK";
            case 1:
                return "BUFFER_PROCESSED_FAILED";
            case 2:
                return "INPUT_BUFFER_NOT_CONSUMED";
            case 4:
                return "OUTPUT_BUFFER_NOT_FILLED";
            case 8:
                return "PLUGIN_TERMINATED";
            default:
                return "" + result;
        }
    }

    private LoggingStringUtils() {
    }
}
