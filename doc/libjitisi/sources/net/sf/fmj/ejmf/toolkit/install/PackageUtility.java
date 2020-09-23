package net.sf.fmj.ejmf.toolkit.install;

import java.util.Vector;
import javax.media.PackageManager;

public class PackageUtility {
    public static void addContentPrefix(String prefix) {
        addContentPrefix(prefix, false);
    }

    public static void addContentPrefix(String prefix, boolean commit) {
        Vector packagePrefix = PackageManager.getContentPrefixList();
        if (!packagePrefix.contains(prefix)) {
            packagePrefix.addElement(prefix);
            PackageManager.setContentPrefixList(packagePrefix);
            if (commit) {
                PackageManager.commitContentPrefixList();
            }
        }
    }

    public static void addProtocolPrefix(String prefix) {
        addProtocolPrefix(prefix, false);
    }

    public static void addProtocolPrefix(String prefix, boolean commit) {
        Vector packagePrefix = PackageManager.getProtocolPrefixList();
        if (!packagePrefix.contains(prefix)) {
            packagePrefix.addElement(prefix);
            PackageManager.setProtocolPrefixList(packagePrefix);
            if (commit) {
                PackageManager.commitProtocolPrefixList();
            }
        }
    }

    public static void removeContentPrefix(String prefix) {
        removeContentPrefix(prefix, false);
    }

    public static void removeContentPrefix(String prefix, boolean commit) {
        Vector packagePrefix = PackageManager.getContentPrefixList();
        if (packagePrefix.contains(prefix)) {
            packagePrefix.removeElement(prefix);
            PackageManager.setContentPrefixList(packagePrefix);
            if (commit) {
                PackageManager.commitContentPrefixList();
            }
        }
    }

    public static void removeProtocolPrefix(String prefix) {
        removeProtocolPrefix(prefix, false);
    }

    public static void removeProtocolPrefix(String prefix, boolean commit) {
        Vector packagePrefix = PackageManager.getProtocolPrefixList();
        if (packagePrefix.contains(prefix)) {
            packagePrefix.removeElement(prefix);
            PackageManager.setProtocolPrefixList(packagePrefix);
            if (commit) {
                PackageManager.commitProtocolPrefixList();
            }
        }
    }
}
