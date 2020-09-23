package org.jitsi.impl.fileaccess;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TempFileManager {
    private static final String TEMP_DIR_PREFIX = "tmp-mgr-";
    private static File sTmpDir = null;

    public static File createTempFile(String prefix, String suffix) throws IOException {
        if (sTmpDir == null) {
            String tmpDirName = System.getProperty("java.io.tmpdir");
            File tmpDir = File.createTempFile(TEMP_DIR_PREFIX, ".tmp", new File(tmpDirName));
            tmpDir.delete();
            File lockFile = new File(tmpDirName, tmpDir.getName() + ".lck");
            lockFile.createNewFile();
            lockFile.deleteOnExit();
            if (tmpDir.mkdirs()) {
                sTmpDir = tmpDir;
            } else {
                throw new IOException("Unable to create temporary directory:" + tmpDir.getAbsolutePath());
            }
        }
        return File.createTempFile(prefix, suffix, sTmpDir);
    }

    private static void recursiveDelete(File rootDir) throws IOException {
        File[] files = rootDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                recursiveDelete(files[i]);
            } else if (!files[i].delete()) {
                throw new IOException("Could not delete: " + files[i].getAbsolutePath());
            }
        }
        if (!rootDir.delete()) {
            throw new IOException("Could not delete: " + rootDir.getAbsolutePath());
        }
    }

    static {
        File[] tmpFiles = new File(System.getProperty("java.io.tmpdir")).listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory() && pathname.getName().startsWith(TempFileManager.TEMP_DIR_PREFIX);
            }
        });
        for (File tmpFile : tmpFiles) {
            if (!new File(tmpFile.getParent(), tmpFile.getName() + ".lck").exists()) {
                Logger.getLogger("default").log(Level.FINE, "TempFileManager::deleting old temp directory " + tmpFile);
                try {
                    recursiveDelete(tmpFile);
                } catch (IOException ex) {
                    Logger.getLogger("default").log(Level.INFO, "TempFileManager::unable to delete " + tmpFile.getAbsolutePath());
                    ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                    ex.printStackTrace(new PrintStream(ostream));
                    Logger.getLogger("default").log(Level.FINE, ostream.toString());
                }
            }
        }
    }
}
