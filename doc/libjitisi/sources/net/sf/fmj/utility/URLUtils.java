package net.sf.fmj.utility;

import com.lti.utils.OSUtils;
import com.lti.utils.StringUtils;
import java.io.File;

public final class URLUtils {
    public static String createAbsoluteFileUrl(String urlStr) {
        String path = extractValidPathFromFileUrl(urlStr);
        if (path == null) {
            return null;
        }
        return createUrlStr(new File(path));
    }

    public static String createUrlStr(File file) {
        String prefix;
        String path = file.getAbsolutePath();
        if (path.startsWith("/")) {
            prefix = "file://";
        } else {
            prefix = "file:///";
        }
        if (OSUtils.isWindows()) {
            path = path.replaceAll("\\\\", "/");
        }
        return prefix + StringUtils.replaceSpecialUrlChars(path, true);
    }

    private static boolean exists(String path, boolean checkParentDirOnly) {
        File f = new File(path);
        if (!checkParentDirOnly) {
            return f.exists();
        }
        if (f.getParentFile() == null || f.getParentFile().exists()) {
            return true;
        }
        return false;
    }

    public static String extractValidNewFilePathFromFileUrl(String url) {
        return extractValidPathFromFileUrl(url, true);
    }

    public static String extractValidPathFromFileUrl(String url) {
        return extractValidPathFromFileUrl(url, false);
    }

    private static String extractValidPathFromFileUrl(String url, boolean checkParentDirOnly) {
        if (!url.startsWith("file:")) {
            return null;
        }
        String remainder = StringUtils.restoreSpecialURLChars(url.substring("file:".length()));
        if (!remainder.startsWith("/")) {
            return remainder;
        }
        String result;
        if (remainder.startsWith("//")) {
            result = remainder.substring(2);
            if (exists(result, checkParentDirOnly)) {
                return windowsSafe(result);
            }
        }
        result = remainder;
        while (result.startsWith("//")) {
            result = result.substring(1);
        }
        if (exists(result, checkParentDirOnly)) {
            return windowsSafe(result);
        }
        while (result.startsWith("/")) {
            result = result.substring(1);
            if (exists(result, checkParentDirOnly)) {
                return windowsSafe(result);
            }
        }
        return null;
    }

    private static String windowsSafe(String result) {
        if (OSUtils.isWindows() && result.startsWith("/")) {
            return result.substring(1);
        }
        return result;
    }

    private URLUtils() {
    }
}
