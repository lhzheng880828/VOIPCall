package org.jitsi.service.version;

public interface Version extends Comparable<Version> {
    public static final String PNAME_APPLICATION_NAME = "sip-communicator.application.name";
    public static final String PNAME_APPLICATION_VERSION = "sip-communicator.version";

    int compareTo(Version version);

    boolean equals(Object obj);

    String getApplicationName();

    String getNightlyBuildID();

    String getPreReleaseID();

    int getVersionMajor();

    int getVersionMinor();

    boolean isNightly();

    boolean isPreRelease();

    String toString();
}
