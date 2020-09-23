package javax.media.protocol;

import java.io.IOException;
import javax.media.Duration;
import javax.media.MediaLocator;
import javax.media.Time;

public abstract class DataSource implements Controls, Duration {
    private MediaLocator locator;

    public abstract void connect() throws IOException;

    public abstract void disconnect();

    public abstract String getContentType();

    public abstract Object getControl(String str);

    public abstract Object[] getControls();

    public abstract Time getDuration();

    public abstract void start() throws IOException;

    public abstract void stop() throws IOException;

    public DataSource(MediaLocator source) {
        this.locator = source;
    }

    public MediaLocator getLocator() {
        return this.locator;
    }

    /* access modifiers changed from: protected */
    public void initCheck() {
        if (this.locator == null) {
            throw new Error("Uninitialized DataSource error.");
        }
    }

    public void setLocator(MediaLocator source) {
        this.locator = source;
    }
}
