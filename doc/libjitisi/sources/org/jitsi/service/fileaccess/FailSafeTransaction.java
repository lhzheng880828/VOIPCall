package org.jitsi.service.fileaccess;

import java.io.IOException;

public interface FailSafeTransaction {
    void beginTransaction() throws IllegalStateException, IOException;

    void commit() throws IllegalStateException, IOException;

    void restoreFile() throws IllegalStateException, IOException;

    void rollback() throws IllegalStateException, IOException;
}
