package net.sf.fmj.media.protocol.file;

import com.lti.utils.PathUtils;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.Seekable;
import javax.media.protocol.SourceCloneable;
import net.sf.fmj.media.MimeManager;
import net.sf.fmj.utility.LoggerSingleton;
import net.sf.fmj.utility.URLUtils;

public class DataSource extends PullDataSource implements SourceCloneable {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = LoggerSingleton.logger;
    protected boolean connected = false;
    /* access modifiers changed from: private */
    public long contentLength = -1;
    protected ContentDescriptor contentType;
    protected RandomAccessFile raf;
    protected RAFPullSourceStream[] sources;

    class RAFPullSourceStream implements PullSourceStream, Seekable {
        private boolean endOfStream = false;

        RAFPullSourceStream() {
        }

        public boolean endOfStream() {
            return this.endOfStream;
        }

        public ContentDescriptor getContentDescriptor() {
            return DataSource.this.contentType;
        }

        public long getContentLength() {
            return DataSource.this.contentLength;
        }

        public Object getControl(String controlType) {
            return null;
        }

        public Object[] getControls() {
            return new Object[0];
        }

        public boolean isRandomAccess() {
            return true;
        }

        public int read(byte[] buffer, int offset, int length) throws IOException {
            int result = DataSource.this.raf.read(buffer, offset, length);
            if (result == -1) {
                this.endOfStream = true;
            }
            return result;
        }

        public long seek(long where) {
            try {
                DataSource.this.raf.seek(where);
                return DataSource.this.raf.getFilePointer();
            } catch (IOException e) {
                DataSource.logger.log(Level.WARNING, "" + e, e);
                throw new RuntimeException(e);
            }
        }

        public long tell() {
            try {
                return DataSource.this.raf.getFilePointer();
            } catch (IOException e) {
                DataSource.logger.log(Level.WARNING, "" + e, e);
                throw new RuntimeException(e);
            }
        }

        public boolean willReadBlock() {
            return false;
        }
    }

    private static String getContentTypeFor(String path) {
        String result = MimeManager.getMimeType(PathUtils.extractExtension(path));
        if (result != null) {
            return result;
        }
        result = URLConnection.getFileNameMap().getContentTypeFor(path);
        String str = result;
        return result;
    }

    public void connect() throws IOException {
        try {
            String path = URLUtils.extractValidPathFromFileUrl(getLocator().toExternalForm());
            if (path == null) {
                throw new IOException("Cannot determine valid file path from URL: " + getLocator().toExternalForm());
            }
            this.raf = new RandomAccessFile(path, "r");
            this.contentLength = this.raf.length();
            String s = getContentTypeFor(path);
            if (s == null) {
                throw new IOException("Unknown content type for path: " + path);
            }
            this.contentType = new ContentDescriptor(ContentDescriptor.mimeTypeToPackageName(s));
            this.sources = new RAFPullSourceStream[1];
            this.sources[0] = new RAFPullSourceStream();
            this.connected = true;
        } catch (IOException e) {
            logger.log(Level.WARNING, "" + e, e);
            throw e;
        }
    }

    public javax.media.protocol.DataSource createClone() {
        DataSource d = new DataSource();
        d.setLocator(getLocator());
        if (!this.connected) {
            return d;
        }
        try {
            d.connect();
            return d;
        } catch (IOException e) {
            logger.log(Level.WARNING, "" + e, e);
            return null;
        }
    }

    public void disconnect() {
        if (this.connected) {
            this.connected = false;
        }
    }

    public String getContentType() {
        if (this.connected) {
            return ContentDescriptor.mimeTypeToPackageName(getContentTypeFor(getLocator().getRemainder()));
        }
        throw new Error("Source is unconnected.");
    }

    public Object getControl(String controlName) {
        return null;
    }

    public Object[] getControls() {
        return new Object[0];
    }

    public Time getDuration() {
        return Time.TIME_UNKNOWN;
    }

    public PullSourceStream[] getStreams() {
        if (this.connected) {
            return this.sources;
        }
        throw new Error("Unconnected source.");
    }

    public void start() throws IOException {
    }

    public void stop() throws IOException {
    }
}
