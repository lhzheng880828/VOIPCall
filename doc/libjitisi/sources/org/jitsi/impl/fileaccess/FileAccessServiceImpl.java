package org.jitsi.impl.fileaccess;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeMapped;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.fileaccess.FailSafeTransaction;
import org.jitsi.service.fileaccess.FileAccessService;
import org.jitsi.service.fileaccess.FileCategory;
import org.jitsi.util.Logger;
import org.jitsi.util.OSUtils;

public class FileAccessServiceImpl implements FileAccessService {
    /* access modifiers changed from: private|static */
    public static Map<String, Object> OPT = new HashMap();
    public static final String TEMP_FILE_PREFIX = "SIPCOMM";
    public static final String TEMP_FILE_SUFFIX = "TEMP";
    private static final Logger logger = Logger.getLogger(FileAccessServiceImpl.class);
    private final String cacheDirLocation;
    private final String logDirLocation;
    private final String profileDirLocation = getSystemProperty(ConfigurationService.PNAME_SC_HOME_DIR_LOCATION);
    private final String scHomeDirName;

    public static class GUID extends Structure {
        public int data1;
        public short data2;
        public short data3;
        public byte[] data4;
    }

    private static class HANDLE extends PointerType implements NativeMapped {
        private HANDLE() {
        }
    }

    private static class HWND extends HANDLE {
        private HWND() {
            super();
        }
    }

    private interface Ole32 extends Library {
        public static final Ole32 INSTANCE = ((Ole32) Native.loadLibrary("Ole32", Ole32.class, FileAccessServiceImpl.OPT));

        void CoTaskMemFree(Pointer pointer);
    }

    private interface Shell32 extends Library {
        public static final int CSIDL_MYDOCUMENTS = 5;
        public static final Shell32 INSTANCE = ((Shell32) Native.loadLibrary("shell32", Shell32.class, FileAccessServiceImpl.OPT));
        public static final int KF_FLAG_CREATE = 32768;
        public static final int KF_FLAG_INIT = 2048;
        public static final int MAX_PATH = 260;
        public static final int SHGFP_TYPE_CURRENT = 0;
        public static final int S_OK = 0;

        int SHGetFolderPath(HWND hwnd, int i, HANDLE handle, int i2, char[] cArr);

        int SHGetKnownFolderPath(GUID guid, int i, HANDLE handle, PointerByReference pointerByReference);
    }

    static {
        if (OSUtils.IS_WINDOWS) {
            OPT.put("type-mapper", W32APITypeMapper.UNICODE);
            OPT.put("function-mapper", W32APIFunctionMapper.UNICODE);
        }
    }

    public FileAccessServiceImpl() {
        if (this.profileDirLocation == null) {
            throw new IllegalStateException(ConfigurationService.PNAME_SC_HOME_DIR_LOCATION);
        }
        String cacheDir = getSystemProperty(ConfigurationService.PNAME_SC_CACHE_DIR_LOCATION);
        if (cacheDir == null) {
            cacheDir = this.profileDirLocation;
        }
        this.cacheDirLocation = cacheDir;
        String logDir = getSystemProperty(ConfigurationService.PNAME_SC_LOG_DIR_LOCATION);
        if (logDir == null) {
            logDir = this.profileDirLocation;
        }
        this.logDirLocation = logDir;
        this.scHomeDirName = getSystemProperty(ConfigurationService.PNAME_SC_HOME_DIR_NAME);
        if (this.scHomeDirName == null) {
            throw new IllegalStateException(ConfigurationService.PNAME_SC_HOME_DIR_NAME);
        }
    }

    public File getTemporaryFile() throws IOException {
        File retVal = null;
        try {
            logger.logEntry();
            retVal = TempFileManager.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
            return retVal;
        } finally {
            logger.logExit();
        }
    }

    public File getTemporaryDirectory() throws IOException {
        File file = getTemporaryFile();
        if (!file.delete()) {
            throw new IOException("Could not create temporary directory, because: could not delete temporary file.");
        } else if (file.mkdirs()) {
            return file;
        } else {
            throw new IOException("Could not create temporary directory");
        }
    }

    @Deprecated
    public File getPrivatePersistentFile(String fileName) throws Exception {
        return getPrivatePersistentFile(fileName, FileCategory.PROFILE);
    }

    public File getPrivatePersistentFile(String fileName, FileCategory category) throws Exception {
        logger.logEntry();
        File file = null;
        try {
            file = accessibleFile(getFullPath(category), fileName);
            if (file != null) {
                return file;
            }
            throw new SecurityException("Insufficient rights to access this file in current user's home directory: " + new File(getFullPath(category), fileName).getPath());
        } finally {
            logger.logExit();
        }
    }

    @Deprecated
    public File getPrivatePersistentDirectory(String dirName) throws Exception {
        return getPrivatePersistentDirectory(dirName, FileCategory.PROFILE);
    }

    public File getPrivatePersistentDirectory(String dirName, FileCategory category) throws Exception {
        File dir = new File(getFullPath(category), dirName);
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new RuntimeException("Could not create directory because: A file exists with this name:" + dir.getAbsolutePath());
            }
        } else if (!dir.mkdirs()) {
            throw new IOException("Could not create directory");
        }
        return dir;
    }

    private File getFullPath(FileCategory category) {
        String directory;
        switch (category) {
            case CACHE:
                directory = this.cacheDirLocation;
                break;
            case LOG:
                directory = this.logDirLocation;
                break;
            default:
                directory = this.profileDirLocation;
                break;
        }
        return new File(directory, this.scHomeDirName);
    }

    private static String getSystemProperty(String propertyName) {
        String retval = System.getProperty(propertyName);
        if (retval != null && retval.trim().length() == 0) {
            return null;
        }
        return retval;
    }

    private static File accessibleFile(File homedir, String fileName) throws IOException {
        Throwable th;
        try {
            logger.logEntry();
            File file = new File(homedir, fileName);
            File file2;
            try {
                if (file.canRead() || file.canWrite()) {
                    logger.logExit();
                    file2 = file;
                    return file;
                }
                String message;
                if (!homedir.exists()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Creating home directory : " + homedir.getAbsolutePath());
                    }
                    if (!homedir.mkdirs()) {
                        message = "Could not create the home directory : " + homedir.getAbsolutePath();
                        if (logger.isDebugEnabled()) {
                            logger.debug(message);
                        }
                        throw new IOException(message);
                    } else if (logger.isDebugEnabled()) {
                        logger.debug("Home directory created : " + homedir.getAbsolutePath());
                    }
                } else if (!homedir.canWrite()) {
                    file = null;
                }
                if (file == null || file.getParentFile().exists() || file.getParentFile().mkdirs()) {
                    logger.logExit();
                    file2 = file;
                    return file;
                }
                message = "Could not create the parent directory : " + homedir.getAbsolutePath();
                logger.debug(message);
                throw new IOException(message);
            } catch (Throwable th2) {
                th = th2;
                file2 = file;
                logger.logExit();
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            logger.logExit();
            throw th;
        }
    }

    public File getDefaultDownloadDirectory() throws IOException {
        if (OSUtils.IS_WINDOWS) {
            if (getMajorOSVersion() < 6) {
                char[] pszPath = new char[Shell32.MAX_PATH];
                if (Shell32.INSTANCE.SHGetFolderPath(null, 5, null, 0, pszPath) == 0) {
                    String path = new String(pszPath);
                    return new File(path.substring(0, path.indexOf(0)));
                }
            }
            GUID g = new GUID();
            g.data1 = 927851152;
            g.data2 = (short) 4671;
            g.data3 = (short) 17765;
            g.data4 = new byte[]{(byte) -111, (byte) 100, (byte) 57, (byte) -60, (byte) -110, (byte) 94, (byte) 70, (byte) 123};
            PointerByReference pszPath2 = new PointerByReference();
            if (Shell32.INSTANCE.SHGetKnownFolderPath(g, 34816, null, pszPath2) == 0) {
                File f = new File(pszPath2.getValue().getString(0, true));
                Ole32.INSTANCE.CoTaskMemFree(pszPath2.getValue());
                return f;
            }
        }
        return new File(getSystemProperty("user.home"), "Downloads");
    }

    private static int getMajorOSVersion() {
        String osVersion = System.getProperty("os.version");
        if (osVersion == null || osVersion.length() <= 0) {
            return 0;
        }
        String majorOSVersionString;
        int majorOSVersionEnd = osVersion.indexOf(46);
        if (majorOSVersionEnd > -1) {
            majorOSVersionString = osVersion.substring(0, majorOSVersionEnd);
        } else {
            majorOSVersionString = osVersion;
        }
        return Integer.parseInt(majorOSVersionString);
    }

    public FailSafeTransaction createFailSafeTransaction(File file) {
        return file == null ? null : new FailSafeTransactionImpl(file);
    }
}
