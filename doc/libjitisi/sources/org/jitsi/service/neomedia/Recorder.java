package org.jitsi.service.neomedia;

import java.io.IOException;
import java.util.List;

public interface Recorder {
    public static final String FORMAT = "net.java.sip.communicator.impl.neomedia.Recorder.FORMAT";
    public static final String SAVED_CALLS_PATH = "net.java.sip.communicator.impl.neomedia.SAVED_CALLS_PATH";

    public interface Listener {
        void recorderStopped(Recorder recorder);
    }

    void addListener(Listener listener);

    String getFilename();

    List<String> getSupportedFormats();

    void removeListener(Listener listener);

    void setMute(boolean z);

    void start(String str, String str2) throws IOException, MediaException;

    void stop();
}
