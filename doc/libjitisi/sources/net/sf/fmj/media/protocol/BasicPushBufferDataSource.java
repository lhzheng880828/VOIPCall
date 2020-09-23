package net.sf.fmj.media.protocol;

import java.io.IOException;
import javax.media.Time;
import javax.media.protocol.PushBufferDataSource;

public abstract class BasicPushBufferDataSource extends PushBufferDataSource {
    protected boolean connected = false;
    protected String contentType = "content/unknown";
    protected Object[] controls = new Object[0];
    protected Time duration = DURATION_UNKNOWN;
    protected boolean started = false;

    public void connect() throws IOException {
        if (!this.connected) {
            this.connected = true;
        }
    }

    public void disconnect() {
        try {
            if (this.started) {
                stop();
            }
        } catch (IOException e) {
        }
        this.connected = false;
    }

    public String getContentType() {
        if (this.connected) {
            return this.contentType;
        }
        System.err.println("Error: DataSource not connected");
        return null;
    }

    public Object getControl(String controlType) {
        try {
            Class<?> cls = Class.forName(controlType);
            Object[] cs = getControls();
            for (int i = 0; i < cs.length; i++) {
                if (cls.isInstance(cs[i])) {
                    return cs[i];
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public Object[] getControls() {
        return this.controls;
    }

    public Time getDuration() {
        return this.duration;
    }

    public void start() throws IOException {
        if (!this.connected) {
            throw new Error("DataSource must be connected before it can be started");
        } else if (!this.started) {
            this.started = true;
        }
    }

    public void stop() throws IOException {
        if (this.connected && this.started) {
            this.started = false;
        }
    }
}
