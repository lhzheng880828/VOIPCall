package net.sf.fmj.media.protocol.httpauth;

import com.lti.utils.StringUtils;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.Controller;
import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.SourceCloneable;
import net.sf.fmj.utility.LoggerSingleton;

public class DataSource extends PullDataSource implements SourceCloneable {
    private static final Logger logger = LoggerSingleton.logger;
    /* access modifiers changed from: private */
    public URLConnection conn;
    private boolean connected = false;
    /* access modifiers changed from: private */
    public ContentDescriptor contentType;
    private String contentTypeStr;
    protected URLSourceStream[] sources;

    class URLSourceStream implements PullSourceStream {
        private boolean endOfStream = false;

        URLSourceStream() {
        }

        public boolean endOfStream() {
            return this.endOfStream;
        }

        public ContentDescriptor getContentDescriptor() {
            return DataSource.this.contentType;
        }

        public long getContentLength() {
            return (long) DataSource.this.conn.getContentLength();
        }

        public Object getControl(String controlType) {
            return null;
        }

        public Object[] getControls() {
            return new Object[0];
        }

        public int read(byte[] buffer, int offset, int length) throws IOException {
            int result = DataSource.this.conn.getInputStream().read(buffer, offset, length);
            if (result == -1) {
                this.endOfStream = true;
            }
            return result;
        }

        public boolean willReadBlock() {
            try {
                return DataSource.this.conn.getInputStream().available() <= 0;
            } catch (IOException e) {
                return true;
            }
        }
    }

    public DataSource(URL url) {
        setLocator(new MediaLocator(url));
    }

    public void connect() throws IOException {
        String remainder = getLocator().getRemainder();
        int atIndex = remainder.indexOf(64);
        if (atIndex < 0) {
            throw new IOException("Invalid httpauth url: expected: @");
        }
        int colonIndex = remainder.indexOf(58);
        if (colonIndex < 0 || colonIndex > atIndex) {
            throw new IOException("Invalid httpaut url: expected: :");
        }
        String user = remainder.substring(0, colonIndex);
        String pass = remainder.substring(colonIndex + 1, atIndex);
        this.conn = new URL("http:" + getLocator().getRemainder().substring(atIndex + 1)).openConnection();
        if (this.conn instanceof HttpURLConnection) {
            HttpURLConnection huc = this.conn;
            if (!(user == null || user.equals(""))) {
                huc.setRequestProperty("Authorization", "Basic " + StringUtils.byteArrayToBase64String((user + ":" + pass).getBytes()));
            }
            huc.connect();
            int code = huc.getResponseCode();
            if (code < 200 || code >= Controller.Realized) {
                huc.disconnect();
                throw new IOException("HTTP response code: " + code);
            }
            this.contentTypeStr = ContentDescriptor.mimeTypeToPackageName(stripTrailer(this.conn.getContentType()));
        } else {
            this.conn.connect();
            this.contentTypeStr = ContentDescriptor.mimeTypeToPackageName(this.conn.getContentType());
        }
        this.contentType = new ContentDescriptor(this.contentTypeStr);
        this.sources = new URLSourceStream[1];
        this.sources[0] = new URLSourceStream();
        this.connected = true;
    }

    public javax.media.protocol.DataSource createClone() {
        try {
            DataSource d = new DataSource(getLocator().getURL());
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
        } catch (MalformedURLException e2) {
            logger.log(Level.WARNING, "" + e2, e2);
            return null;
        }
    }

    public void disconnect() {
        if (this.connected) {
            if (this.conn != null && (this.conn instanceof HttpURLConnection)) {
                this.conn.disconnect();
            }
            this.connected = false;
        }
    }

    public String getContentType() {
        return this.contentTypeStr;
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
