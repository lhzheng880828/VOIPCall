package org.jitsi.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.LogManager;
import org.jitsi.service.configuration.ConfigurationService;

public class FileHandler extends java.util.logging.FileHandler {
    private static int count = -1;
    private static int limit = -1;
    public static String pattern = null;

    public FileHandler(String pattern, int limit, int count) throws IOException, SecurityException {
        super(pattern, limit, count);
    }

    public FileHandler() throws IOException, SecurityException {
        super(getPattern(), getLimit(), getCount());
    }

    private static int getLimit() {
        if (limit == -1) {
            String limitStr = LogManager.getLogManager().getProperty(FileHandler.class.getName() + ".limit");
            limit = 0;
            try {
                limit = Integer.parseInt(limitStr);
            } catch (Exception e) {
            }
        }
        return limit;
    }

    private static String getPattern() {
        if (pattern == null) {
            pattern = LogManager.getLogManager().getProperty(FileHandler.class.getName() + ".pattern");
            String homeLocation = System.getProperty(ConfigurationService.PNAME_SC_HOME_DIR_LOCATION);
            String dirName = System.getProperty(ConfigurationService.PNAME_SC_HOME_DIR_NAME);
            if (!(homeLocation == null || dirName == null)) {
                if (pattern == null) {
                    pattern = homeLocation + "/" + dirName + "/log/jitsi%u.log";
                } else {
                    pattern = pattern.replaceAll("\\%s", homeLocation + "/" + dirName);
                }
            }
            if (pattern == null) {
                pattern = "./log/jitsi%u.log";
            }
            checkDestinationDirectory(pattern);
        }
        return pattern;
    }

    private static int getCount() {
        if (count == -1) {
            String countStr = LogManager.getLogManager().getProperty(FileHandler.class.getName() + ".count");
            count = 1;
            try {
                count = Integer.parseInt(countStr);
            } catch (Exception e) {
            }
        }
        return count;
    }

    private static void checkDestinationDirectory(String pattern) {
        try {
            int ix = pattern.lastIndexOf(47);
            if (ix != -1) {
                new File(pattern.substring(0, ix).replaceAll("%h", System.getProperty("user.home")).replaceAll("%t", System.getProperty("java.io.tmpdir"))).mkdirs();
            }
        } catch (Exception e) {
        }
    }
}
