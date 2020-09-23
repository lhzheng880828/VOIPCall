package net.sf.fmj.media.datasink.rtp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RTPUrlParser {
    private static final Pattern pattern = Pattern.compile("rtp://([a-zA-Z_/\\.0-9]+)(:([0-9]+))(/(audio|video)(/([0-9]+))?)(\\&([a-zA-Z_/\\.0-9]+)(:([0-9]+))(/(audio|video)(/([0-9]+))?))?");

    private static ParsedRTPUrlElement extract(Matcher m, int offset) throws RTPUrlParserException {
        ParsedRTPUrlElement e = new ParsedRTPUrlElement();
        try {
            e.host = m.group(offset + 1);
            e.port = Integer.parseInt(m.group(offset + 3));
            e.type = m.group(offset + 5);
            if (m.group(offset + 7) != null) {
                e.ttl = Integer.parseInt(m.group(offset + 7));
            }
            return e;
        } catch (NumberFormatException ex) {
            throw new RTPUrlParserException(ex);
        }
    }

    public static ParsedRTPUrl parse(String url) throws RTPUrlParserException {
        Matcher m = pattern.matcher(url);
        if (m.matches()) {
            ParsedRTPUrlElement e = extract(m, 0);
            if (m.group(9) == null) {
                return new ParsedRTPUrl(e);
            }
            ParsedRTPUrlElement e2 = extract(m, 8);
            if (!e2.type.equals(e.type)) {
                return new ParsedRTPUrl(e, e2);
            }
            throw new RTPUrlParserException("Both elements of the RTP URL have type " + e.type);
        }
        throw new RTPUrlParserException("URL does not match regular expression for RTP URLs");
    }
}
