package org.jitsi.service.version.util;

import org.jitsi.service.version.Version;

public abstract class AbstractVersion implements Version {
    private String nightlyBuildID;
    private int versionMajor;
    private int versionMinor;

    protected AbstractVersion(int majorVersion, int minorVersion, String nightlyBuildID) {
        this.versionMajor = majorVersion;
        this.versionMinor = minorVersion;
        this.nightlyBuildID = nightlyBuildID;
    }

    public int getVersionMajor() {
        return this.versionMajor;
    }

    public int getVersionMinor() {
        return this.versionMinor;
    }

    public String getNightlyBuildID() {
        if (isNightly()) {
            return this.nightlyBuildID;
        }
        return null;
    }

    public int compareTo(Version version) {
        if (version == null) {
            return -1;
        }
        if (getVersionMajor() != version.getVersionMajor()) {
            return getVersionMajor() - version.getVersionMajor();
        }
        if (getVersionMinor() != version.getVersionMinor()) {
            return getVersionMinor() - version.getVersionMinor();
        }
        try {
            return compareNightlyBuildIDByComponents(getNightlyBuildID(), version.getNightlyBuildID());
        } catch (Throwable th) {
            return getNightlyBuildID().compareTo(version.getNightlyBuildID());
        }
    }

    private static int compareNightlyBuildIDByComponents(String v1, String v2) {
        String[] s1 = v1.split("\\.");
        String[] s2 = v2.split("\\.");
        int len = Math.max(s1.length, s2.length);
        for (int i = 0; i < len; i++) {
            int n1 = 0;
            int n2 = 0;
            if (i < s1.length) {
                n1 = Integer.valueOf(s1[i]).intValue();
            }
            if (i < s2.length) {
                n2 = Integer.valueOf(s2[i]).intValue();
            }
            if (n1 != n2) {
                return n1 - n2;
            }
        }
        return 0;
    }

    public boolean equals(Object version) {
        return toString().equals(version == null ? "null" : version.toString());
    }

    public String toString() {
        StringBuffer versionStringBuff = new StringBuffer();
        versionStringBuff.append(Integer.toString(getVersionMajor()));
        versionStringBuff.append(".");
        versionStringBuff.append(Integer.toString(getVersionMinor()));
        if (isPreRelease()) {
            versionStringBuff.append("-");
            versionStringBuff.append(getPreReleaseID());
        }
        if (isNightly()) {
            versionStringBuff.append(".");
            versionStringBuff.append(getNightlyBuildID());
        }
        return versionStringBuff.toString();
    }
}
