package net.sf.fmj.media;

import javax.media.ConfigureCompleteEvent;
import javax.media.NotConfiguredError;
import javax.media.Processor;
import javax.media.control.TrackControl;
import javax.media.protocol.ContentDescriptor;
import net.sf.fmj.ejmf.toolkit.media.AbstractPlayer;

public abstract class AbstractProcessor extends AbstractPlayer implements Processor {
    protected ContentDescriptor outputContentDescriptor;

    public abstract boolean doConfigure();

    public void configure() {
        if (getState() >= Processor.Configured) {
            postConfigureCompleteEvent();
            return;
        }
        if (getTargetState() < Processor.Configured) {
            setTargetState(Processor.Configured);
        }
        getThreadQueue().addThread(new Thread("Processor Configure Thread") {
            public void run() {
                if (AbstractProcessor.this.getState() < Processor.Configured) {
                    AbstractProcessor.this.synchronousConfigure();
                }
            }
        });
    }

    public ContentDescriptor getContentDescriptor() throws NotConfiguredError {
        if (getState() >= Processor.Configured) {
            return this.outputContentDescriptor;
        }
        throw new NotConfiguredError("Cannot call getContentDescriptor on an unconfigured Processor.");
    }

    public ContentDescriptor[] getSupportedContentDescriptors() throws NotConfiguredError {
        if (getState() >= Processor.Configured) {
            return null;
        }
        throw new NotConfiguredError("Cannot call getSupportedContentDescriptors on an unconfigured Processor.");
    }

    public TrackControl[] getTrackControls() throws NotConfiguredError {
        if (getState() >= Processor.Configured) {
            return null;
        }
        throw new NotConfiguredError("Cannot call getTrackControls on an unconfigured Processor.");
    }

    /* access modifiers changed from: protected */
    public void postConfigureCompleteEvent() {
        postEvent(new ConfigureCompleteEvent(this, getPreviousState(), getState(), getTargetState()));
    }

    public ContentDescriptor setContentDescriptor(ContentDescriptor outputContentDescriptor) throws NotConfiguredError {
        this.outputContentDescriptor = outputContentDescriptor;
        return outputContentDescriptor;
    }

    /* access modifiers changed from: protected */
    public void synchronousConfigure() {
        setState(Processor.Configuring);
        postTransitionEvent();
        if (doConfigure()) {
            setState(Processor.Configured);
            postConfigureCompleteEvent();
            return;
        }
        setState(100);
        setTargetState(100);
    }
}
