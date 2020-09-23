package org.jitsi.impl.fileaccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.jitsi.service.fileaccess.FailSafeTransaction;

public class FailSafeTransactionImpl implements FailSafeTransaction {
    private static final String BAK_EXT = ".bak";
    private static final String PART_EXT = ".part";
    private File backup;
    private File file;

    protected FailSafeTransactionImpl(File file) throws NullPointerException {
        if (file == null) {
            throw new NullPointerException("null file provided");
        }
        this.file = file;
        this.backup = null;
    }

    public void restoreFile() throws IllegalStateException, IOException {
        File back = new File(this.file.getAbsolutePath() + BAK_EXT);
        if (back.exists()) {
            failsafeCopy(back.getAbsolutePath(), this.file.getAbsolutePath());
            back.delete();
        }
    }

    public void beginTransaction() throws IllegalStateException, IOException {
        if (this.backup != null) {
            commit();
        }
        restoreFile();
        this.backup = new File(this.file.getAbsolutePath() + BAK_EXT);
        failsafeCopy(this.file.getAbsolutePath(), this.backup.getAbsolutePath());
    }

    public void commit() throws IllegalStateException, IOException {
        if (this.backup != null) {
            this.backup.delete();
            this.backup = null;
        }
    }

    public void rollback() throws IllegalStateException, IOException {
        if (this.backup != null) {
            failsafeCopy(this.backup.getAbsolutePath(), this.file.getAbsolutePath());
            this.backup.delete();
            this.backup = null;
        }
    }

    private void failsafeCopy(String from, String to) throws IllegalStateException, IOException {
        FileNotFoundException e;
        File toF = new File(to);
        if (toF.exists()) {
            toF.delete();
        }
        File ptoF = new File(to + PART_EXT);
        if (ptoF.exists()) {
            ptoF.delete();
        }
        try {
            FileInputStream in = new FileInputStream(from);
            try {
                FileOutputStream out = new FileOutputStream(to + PART_EXT);
                byte[] buf = new byte[1024];
                while (true) {
                    int len = in.read(buf);
                    if (len > 0) {
                        out.write(buf, 0, len);
                    } else {
                        in.close();
                        out.close();
                        ptoF.renameTo(toF);
                        return;
                    }
                }
            } catch (FileNotFoundException e2) {
                e = e2;
                FileInputStream fileInputStream = in;
                throw new IllegalStateException(e.getMessage());
            }
        } catch (FileNotFoundException e3) {
            e = e3;
            throw new IllegalStateException(e.getMessage());
        }
    }
}
