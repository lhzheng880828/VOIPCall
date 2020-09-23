package net.sf.fmj.media.protocol;

import com.lti.utils.PathUtils;
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
import javax.media.protocol.DataSource;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.SourceCloneable;
import net.sf.fmj.media.MimeManager;
import net.sf.fmj.utility.LoggerSingleton;
import net.sf.fmj.utility.URLUtils;

public class URLDataSource extends PullDataSource implements SourceCloneable {
    private static final Logger logger = LoggerSingleton.logger;
    protected URLConnection conn;
    protected boolean connected = false;
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
            return URLDataSource.this.contentType;
        }

        public long getContentLength() {
            return (long) URLDataSource.this.conn.getContentLength();
        }

        public Object getControl(String controlType) {
            return null;
        }

        public Object[] getControls() {
            return new Object[0];
        }

        public int read(byte[] buffer, int offset, int length) throws IOException {
            int result = URLDataSource.this.conn.getInputStream().read(buffer, offset, length);
            if (result == -1) {
                this.endOfStream = true;
            }
            return result;
        }

        public boolean willReadBlock() {
            try {
                return URLDataSource.this.conn.getInputStream().available() <= 0;
            } catch (IOException e) {
                return true;
            }
        }
    }

    protected URLDataSource() {
    }

    public URLDataSource(URL url) {
        setLocator(new MediaLocator(url));
    }

    public void connect() throws IOException {
        URL url = getLocator().getURL();
        if (url.getProtocol().equals("file")) {
            String newUrlStr = URLUtils.createAbsoluteFileUrl(url.toExternalForm());
            if (!(newUrlStr == null || newUrlStr.toString().equals(url.toExternalForm()))) {
                logger.warning("Changing file URL to absolute for URL.openConnection, from " + url.toExternalForm() + " to " + newUrlStr);
                url = new URL(newUrlStr);
            }
        }
        this.conn = url.openConnection();
        if (url.getProtocol().equals("ftp") || !this.conn.getURL().getProtocol().equals("ftp")) {
            if (this.conn instanceof HttpURLConnection) {
                HttpURLConnection huc = this.conn;
                huc.connect();
                int code = huc.getResponseCode();
                if (code < 200 || code >= Controller.Realized) {
                    huc.disconnect();
                    throw new IOException("HTTP response code: " + code);
                }
                logger.finer("URL: " + url);
                logger.finer("Response code: " + code);
                logger.finer("Full content type: " + this.conn.getContentType());
                boolean contentTypeSet = false;
                if (stripTrailer(this.conn.getContentType()).equals("text/plain")) {
                    String ext = PathUtils.extractExtension(url.getPath());
                    if (ext != null) {
                        String result = MimeManager.getMimeType(ext);
                        if (result != null) {
                            this.contentTypeStr = ContentDescriptor.mimeTypeToPackageName(result);
                            contentTypeSet = true;
                            logger.fine("Received content type " + this.conn.getContentType() + "; overriding based on extension, to: " + result);
                        }
                    }
                }
                if (!contentTypeSet) {
                    this.contentTypeStr = ContentDescriptor.mimeTypeToPackageName(stripTrailer(this.conn.getContentType()));
                }
            } else {
                this.conn.connect();
                this.contentTypeStr = ContentDescriptor.mimeTypeToPackageName(this.conn.getContentType());
            }
            this.contentType = new ContentDescriptor(this.contentTypeStr);
            this.sources = new URLSourceStream[1];
            this.sources[0] = new URLSourceStream();
            this.connected = true;
            return;
        }
        logger.warning("URL.openConnection() morphed " + url + " to " + this.conn.getURL());
        throw new IOException("URL.openConnection() returned an FTP connection for a non-ftp url: " + url);
    }

    public DataSource createClone() {
        try {
            URLDataSource d = new URLDataSource(getLocator().getURL());
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
