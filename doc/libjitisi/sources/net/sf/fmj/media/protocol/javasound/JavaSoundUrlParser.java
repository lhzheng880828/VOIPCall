package net.sf.fmj.media.protocol.javasound;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.media.format.AudioFormat;

public class JavaSoundUrlParser {
    private static final Pattern pattern = Pattern.compile("javasound://(([0-9]+)(/([0-9]+)(/([0-9]+)(/(big|little)(/(signed|unsigned))?)?)?)?)?");

    public static AudioFormat parse(String url) throws JavaSoundUrlParserException {
        if (url == null) {
            throw new JavaSoundUrlParserException(new NullPointerException());
        } else if (url.startsWith("javasound://")) {
            Matcher m = pattern.matcher(url);
            if (m.matches()) {
                int groupCount = m.groupCount();
                double rate = -1.0d;
                int bits = -1;
                int channels = -1;
                int endian = -1;
                int signed = -1;
                try {
                    if (!(m.group(2) == null || m.group(2).equals(""))) {
                        rate = Double.parseDouble(m.group(2));
                    }
                    if (!(m.group(4) == null || m.group(4).equals(""))) {
                        bits = Integer.parseInt(m.group(4));
                    }
                    if (!(m.group(6) == null || m.group(6).equals(""))) {
                        channels = Integer.parseInt(m.group(6));
                    }
                    if (!(m.group(8) == null || m.group(8).equals(""))) {
                        endian = m.group(8).equals("big") ? 1 : 0;
                    }
                    if (!(m.group(10) == null || m.group(10).equals(""))) {
                        signed = m.group(10).equals("signed") ? 1 : 0;
                    }
                    return new AudioFormat(AudioFormat.LINEAR, rate, bits, channels, endian, signed);
                } catch (NumberFormatException e) {
                    throw new JavaSoundUrlParserException("Invalid number", e);
                }
            }
            throw new JavaSoundUrlParserException("URL does not match regular expression for javasound URLs");
        } else {
            throw new JavaSoundUrlParserException("Expected URL to start with: javasound://");
        }
    }
}
