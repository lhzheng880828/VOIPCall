package net.sf.fmj.media;

import javax.media.Controller;
import javax.media.NotConfiguredError;
import javax.media.NotRealizedError;
import javax.media.Processor;
import javax.media.control.TrackControl;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;

public abstract class BasicProcessor extends BasicPlayer implements Processor {
    static String NOT_CONFIGURED_ERROR = "cannot be called before the Processor is configured";

    public ContentDescriptor getContentDescriptor() throws NotConfiguredError {
        if (getState() >= Processor.Configured) {
            return null;
        }
        throw new NotConfiguredError("getContentDescriptor " + NOT_CONFIGURED_ERROR);
    }

    public DataSource getDataOutput() throws NotRealizedError {
        if (getState() >= Controller.Realized) {
            return null;
        }
        throw new NotRealizedError("getDataOutput cannot be called before the Processor is realized");
    }

    public ContentDescriptor[] getSupportedContentDescriptors() throws NotConfiguredError {
        if (getState() >= Processor.Configured) {
            return new ContentDescriptor[0];
        }
        throw new NotConfiguredError("getSupportedContentDescriptors " + NOT_CONFIGURED_ERROR);
    }

    public TrackControl[] getTrackControls() throws NotConfiguredError {
        if (getState() >= Processor.Configured) {
            return new TrackControl[0];
        }
        throw new NotConfiguredError("getTrackControls " + NOT_CONFIGURED_ERROR);
    }

    /* access modifiers changed from: protected */
    public boolean isConfigurable() {
        return true;
    }

    public ContentDescriptor setContentDescriptor(ContentDescriptor ocd) throws NotConfiguredError {
        if (getState() >= Processor.Configured) {
            return ocd;
        }
        throw new NotConfiguredError("setContentDescriptor " + NOT_CONFIGURED_ERROR);
    }
}
