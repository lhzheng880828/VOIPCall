package javax.media;

import javax.media.control.TrackControl;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;

public interface Processor extends Player {
    public static final int Configured = 180;
    public static final int Configuring = 140;

    void configure();

    ContentDescriptor getContentDescriptor() throws NotConfiguredError;

    DataSource getDataOutput() throws NotRealizedError;

    ContentDescriptor[] getSupportedContentDescriptors() throws NotConfiguredError;

    TrackControl[] getTrackControls() throws NotConfiguredError;

    ContentDescriptor setContentDescriptor(ContentDescriptor contentDescriptor) throws NotConfiguredError;
}
