package javax.swing.filechooser;

import android.os.Environment;
import java.io.File;

public class FileSystemView {
    public static FileSystemView getFileSystemView() {
        return new FileSystemView();
    }

    public File getHomeDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }
}
