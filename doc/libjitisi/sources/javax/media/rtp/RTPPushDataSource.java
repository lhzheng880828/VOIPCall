package javax.media.rtp;

import java.io.IOException;
import javax.media.Time;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushDataSource;
import javax.media.protocol.PushSourceStream;

@Deprecated
public class RTPPushDataSource extends PushDataSource {
    public RTPPushDataSource() {
        throw new UnsupportedOperationException();
    }

    public void connect() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void disconnect() {
        throw new UnsupportedOperationException();
    }

    public String getContentType() {
        throw new UnsupportedOperationException();
    }

    public Object getControl(String controlName) {
        throw new UnsupportedOperationException();
    }

    public Object[] getControls() {
        throw new UnsupportedOperationException();
    }

    public Time getDuration() {
        throw new UnsupportedOperationException();
    }

    public OutputDataStream getInputStream() {
        throw new UnsupportedOperationException();
    }

    public PushSourceStream getOutputStream() {
        throw new UnsupportedOperationException();
    }

    public PushSourceStream[] getStreams() {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public void initCheck() {
        throw new UnsupportedOperationException();
    }

    public boolean isStarted() {
        throw new UnsupportedOperationException();
    }

    public void setChild(DataSource source) {
        throw new UnsupportedOperationException();
    }

    public void setContentType(String contentType) {
        throw new UnsupportedOperationException();
    }

    public void setInputStream(OutputDataStream inputstream) {
        throw new UnsupportedOperationException();
    }

    public void setOutputStream(PushSourceStream outputstream) {
        throw new UnsupportedOperationException();
    }

    public void start() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void stop() throws IOException {
        throw new UnsupportedOperationException();
    }
}
