package org.jitsi.service.version.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jitsi.service.version.Version;
import org.jitsi.service.version.VersionService;

public abstract class AbstractVersionService implements VersionService {
    private static final Pattern PARSE_VERSION_STRING_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.([\\d\\.]+)");

    public abstract Version createVersionImpl(int i, int i2, String str);

    public Version parseVersionString(String version) {
        Matcher matcher = PARSE_VERSION_STRING_PATTERN.matcher(version);
        if (matcher.matches() && matcher.groupCount() == 3) {
            return createVersionImpl(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), matcher.group(3));
        }
        return null;
    }
}
