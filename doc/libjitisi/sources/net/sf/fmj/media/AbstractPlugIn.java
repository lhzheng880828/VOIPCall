package net.sf.fmj.media;

import javax.media.PlugIn;
import javax.media.ResourceUnavailableException;

public abstract class AbstractPlugIn extends AbstractControls implements PlugIn {
    private boolean opened = false;

    public void close() {
        this.opened = false;
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public void open() throws ResourceUnavailableException {
        this.opened = true;
    }

    public void reset() {
    }
}
