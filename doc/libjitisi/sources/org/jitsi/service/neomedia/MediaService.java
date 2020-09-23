package org.jitsi.service.neomedia;

import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jitsi.android.util.java.awt.Point;
import org.jitsi.service.neomedia.Recorder.Listener;
import org.jitsi.service.neomedia.codec.EncodingConfiguration;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.device.ScreenDevice;
import org.jitsi.service.neomedia.format.MediaFormat;
import org.jitsi.service.neomedia.format.MediaFormatFactory;

public interface MediaService {
    public static final String DEFAULT_DEVICE = "defaultDevice";

    void addPropertyChangeListener(PropertyChangeListener propertyChangeListener);

    void addRecorderListener(Listener listener);

    EncodingConfiguration createEmptyEncodingConfiguration();

    MediaStream createMediaStream(MediaType mediaType);

    MediaStream createMediaStream(StreamConnector streamConnector, MediaType mediaType);

    MediaStream createMediaStream(StreamConnector streamConnector, MediaType mediaType, SrtpControl srtpControl);

    MediaStream createMediaStream(StreamConnector streamConnector, MediaDevice mediaDevice);

    MediaStream createMediaStream(StreamConnector streamConnector, MediaDevice mediaDevice, SrtpControl srtpControl);

    MediaStream createMediaStream(MediaDevice mediaDevice);

    MediaDevice createMixer(MediaDevice mediaDevice);

    RTPTranslator createRTPTranslator();

    Recorder createRecorder(MediaDevice mediaDevice);

    SrtpControl createSrtpControl(SrtpControlType srtpControlType);

    List<ScreenDevice> getAvailableScreenDevices();

    EncodingConfiguration getCurrentEncodingConfiguration();

    MediaDevice getDefaultDevice(MediaType mediaType, MediaUseCase mediaUseCase);

    ScreenDevice getDefaultScreenDevice();

    List<MediaDevice> getDevices(MediaType mediaType, MediaUseCase mediaUseCase);

    Map<MediaFormat, Byte> getDynamicPayloadTypePreferences();

    MediaFormatFactory getFormatFactory();

    VolumeControl getInputVolumeControl();

    MediaDevice getMediaDeviceForPartialDesktopStreaming(int i, int i2, int i3, int i4);

    Point getOriginForDesktopStreamingDevice(MediaDevice mediaDevice);

    VolumeControl getOutputVolumeControl();

    Iterator<Listener> getRecorderListeners();

    Object getVideoPreviewComponent(MediaDevice mediaDevice, int i, int i2);

    boolean isPartialStreaming(MediaDevice mediaDevice);

    void removePropertyChangeListener(PropertyChangeListener propertyChangeListener);

    void removeRecorderListener(Listener listener);
}
