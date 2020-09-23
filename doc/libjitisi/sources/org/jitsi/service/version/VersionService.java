package org.jitsi.service.version;

public interface VersionService {
    Version getCurrentVersion();

    Version parseVersionString(String str);
}
