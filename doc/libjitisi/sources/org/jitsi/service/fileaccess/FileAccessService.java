package org.jitsi.service.fileaccess;

import java.io.File;
import java.io.IOException;

public interface FileAccessService {
    FailSafeTransaction createFailSafeTransaction(File file);

    File getDefaultDownloadDirectory() throws IOException;

    @Deprecated
    File getPrivatePersistentDirectory(String str) throws Exception;

    File getPrivatePersistentDirectory(String str, FileCategory fileCategory) throws Exception;

    @Deprecated
    File getPrivatePersistentFile(String str) throws Exception;

    File getPrivatePersistentFile(String str, FileCategory fileCategory) throws Exception;

    File getTemporaryDirectory() throws IOException;

    File getTemporaryFile() throws IOException;
}
