package net.sf.fmj.media.protocol.res;

import com.lti.utils.PathUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.SourceCloneable;
import net.sf.fmj.media.MimeManager;
import net.sf.fmj.utility.LoggerSingleton;

public class DataSource extends PullDataSource implements SourceCloneable {
    private static final Logger logger = LoggerSingleton.logger;
    private boolean connected = false;
    /* access modifiers changed from: private */
    public ContentDescriptor contentType;
    /* access modifiers changed from: private */
    public InputStream inputStream;
    private ResSourceStream[] sources;

    class ResSourceStream implements PullSourceStream {
        private boolean endOfStream = false;

        ResSourceStream() {
        }

        public boolean endOfStream() {
            return this.endOfStream;
        }

        public ContentDescriptor getContentDescriptor() {
            return DataSource.this.contentType;
        }

        public long getContentLength() {
            return -1;
        }

        public Object getControl(String controlType) {
            return null;
        }

        public Object[] getControls() {
            return new Object[0];
        }

        public int read(byte[] buffer, int offset, int length) throws IOException {
            int result = DataSource.this.inputStream.read(buffer, offset, length);
            if (result == -1) {
                this.endOfStream = true;
            }
            return result;
        }

        public boolean willReadBlock() {
            try {
                return DataSource.this.inputStream.available() <= 0;
            } catch (IOException e) {
                return true;
            }
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
        String path = getLocator().getRemainder();
        this.inputStream = DataSource.class.getResourceAsStream(path);
        String s = getContentTypeFor(path);
        if (s == null) {
            throw new IOException("Unknown content type for path: " + path);
        }
        this.contentType = new ContentDescriptor(ContentDescriptor.mimeTypeToPackageName(s));
        this.sources = new ResSourceStream[1];
        this.sources[0] = new ResSourceStream();
        this.connected = true;
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
            if (this.inputStream != null) {
                try {
                    this.inputStream.close();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "" + e, e);
                }
            }
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

    private String stripTrailer(String contentType) {
        int index = contentType.indexOf(";");
        if (index < 0) {
            return contentType;
        }
        return contentType.substring(0, index);
    }
}
