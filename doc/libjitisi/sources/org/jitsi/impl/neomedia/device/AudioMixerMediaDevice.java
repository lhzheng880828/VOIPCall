package org.jitsi.impl.neomedia.device;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.media.Buffer;
import javax.media.Player;
import javax.media.Processor;
import javax.media.Renderer;
import javax.media.control.TrackControl;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.ReceiveStream;
import org.jitsi.impl.neomedia.audiolevel.AudioLevelEventDispatcher;
import org.jitsi.impl.neomedia.audiolevel.AudioLevelMap;
import org.jitsi.impl.neomedia.conference.AudioMixer;
import org.jitsi.impl.neomedia.conference.AudioMixingPushBufferDataSource;
import org.jitsi.impl.neomedia.conference.DataSourceFilter;
import org.jitsi.impl.neomedia.protocol.PushBufferDataSourceDelegate;
import org.jitsi.impl.neomedia.protocol.TranscodingDataSource;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.QualityPreset;
import org.jitsi.service.neomedia.RTPExtension;
import org.jitsi.service.neomedia.VolumeControl;
import org.jitsi.service.neomedia.codec.EncodingConfiguration;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.device.MediaDeviceWrapper;
import org.jitsi.service.neomedia.event.SimpleAudioLevelListener;
import org.jitsi.service.neomedia.format.MediaFormat;
import org.jitsi.util.Logger;

public class AudioMixerMediaDevice extends AbstractMediaDevice implements MediaDeviceWrapper {
    private static final Logger logger = Logger.getLogger(AudioMixerMediaDevice.class);
    /* access modifiers changed from: private|final */
    public final AudioLevelMap audioLevelCache = new AudioLevelMap();
    private AudioMixer audioMixer;
    private final AudioMediaDeviceImpl device;
    private AudioMixerMediaDeviceSession deviceSession;
    /* access modifiers changed from: private */
    public int lastMeasuredLocalUserAudioLevel = 0;
    /* access modifiers changed from: private|final */
    public final SimpleAudioLevelListener localUserAudioLevelDelegate = new SimpleAudioLevelListener() {
        public void audioLevelChanged(int level) {
            AudioMixerMediaDevice.this.lastMeasuredLocalUserAudioLevel = level;
            AudioMixerMediaDevice.this.fireLocalUserAudioLevelChanged(level);
        }
    };
    /* access modifiers changed from: private|final */
    public final AudioLevelEventDispatcher localUserAudioLevelDispatcher = new AudioLevelEventDispatcher("Local User Audio Level Dispatcher (Mixer Edition)");
    /* access modifiers changed from: private */
    public List<SimpleAudioLevelListenerWrapper> localUserAudioLevelListeners = new ArrayList();
    /* access modifiers changed from: private|final */
    public final Object localUserAudioLevelListenersSyncRoot = new Object();
    private List<RTPExtension> rtpExtensions = null;
    /* access modifiers changed from: private|final */
    public final Map<ReceiveStream, AudioLevelEventDispatcher> streamAudioLevelListeners = new HashMap();

    private class AudioMixerMediaDeviceSession extends MediaDeviceSession {
        private final List<MediaStreamMediaDeviceSession> mediaStreamMediaDeviceSessions = new LinkedList();
        private VolumeControl outputVolumeControl;

        public AudioMixerMediaDeviceSession() {
            super(AudioMixerMediaDevice.this);
        }

        /* access modifiers changed from: 0000 */
        public void addLocalUserAudioLevelListener(SimpleAudioLevelListener l) {
            if (l != null) {
                synchronized (AudioMixerMediaDevice.this.localUserAudioLevelListenersSyncRoot) {
                    if (AudioMixerMediaDevice.this.localUserAudioLevelListeners.isEmpty()) {
                        AudioMixerMediaDevice.this.localUserAudioLevelDispatcher.setAudioLevelListener(AudioMixerMediaDevice.this.localUserAudioLevelDelegate);
                    }
                    SimpleAudioLevelListenerWrapper wrapper = new SimpleAudioLevelListenerWrapper(l);
                    int index = AudioMixerMediaDevice.this.localUserAudioLevelListeners.indexOf(wrapper);
                    if (index != -1) {
                        wrapper = (SimpleAudioLevelListenerWrapper) AudioMixerMediaDevice.this.localUserAudioLevelListeners.get(index);
                        wrapper.referenceCount++;
                    } else {
                        AudioMixerMediaDevice.this.localUserAudioLevelListeners = new ArrayList(AudioMixerMediaDevice.this.localUserAudioLevelListeners);
                        AudioMixerMediaDevice.this.localUserAudioLevelListeners.add(wrapper);
                    }
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void addMediaStreamMediaDeviceSession(MediaStreamMediaDeviceSession mediaStreamMediaDeviceSession) {
            if (mediaStreamMediaDeviceSession == null) {
                throw new NullPointerException("mediaStreamMediaDeviceSession");
            }
            synchronized (this.mediaStreamMediaDeviceSessions) {
                if (!this.mediaStreamMediaDeviceSessions.contains(mediaStreamMediaDeviceSession)) {
                    this.mediaStreamMediaDeviceSessions.add(mediaStreamMediaDeviceSession);
                }
            }
        }

        public void addPlaybackDataSource(DataSource playbackDataSource) {
            super.addPlaybackDataSource(getCaptureDevice());
        }

        public void addReceiveStream(ReceiveStream receiveStream) {
            addSSRC(4294967295L & receiveStream.getSSRC());
        }

        /* access modifiers changed from: protected */
        public DataSource createCaptureDevice() {
            return AudioMixerMediaDevice.this.getAudioMixer().getLocalOutDataSource();
        }

        /* access modifiers changed from: protected */
        public Player createPlayer(DataSource dataSource) {
            return super.createPlayer(dataSource);
        }

        /* access modifiers changed from: 0000 */
        public void setOutputVolumeControl(VolumeControl outputVolumeControl) {
            this.outputVolumeControl = outputVolumeControl;
        }

        /* access modifiers changed from: 0000 */
        public void setStreamAudioLevelListener(ReceiveStream stream, SimpleAudioLevelListener listener) {
            synchronized (AudioMixerMediaDevice.this.streamAudioLevelListeners) {
                AudioLevelEventDispatcher dispatcher = (AudioLevelEventDispatcher) AudioMixerMediaDevice.this.streamAudioLevelListeners.get(stream);
                if (listener != null) {
                    if (dispatcher == null) {
                        dispatcher = new AudioLevelEventDispatcher("Stream Audio Level Dispatcher (Mixer Edition)");
                        dispatcher.setAudioLevelCache(AudioMixerMediaDevice.this.audioLevelCache, 4294967295L & stream.getSSRC());
                        AudioMixerMediaDevice.this.streamAudioLevelListeners.put(stream, dispatcher);
                    }
                    dispatcher.setAudioLevelListener(listener);
                } else if (dispatcher != null) {
                    try {
                        dispatcher.setAudioLevelListener(null);
                        dispatcher.setAudioLevelCache(null, -1);
                        AudioMixerMediaDevice.this.streamAudioLevelListeners.remove(stream);
                    } catch (Throwable th) {
                        AudioMixerMediaDevice.this.streamAudioLevelListeners.remove(stream);
                    }
                }
            }
        }

        /* access modifiers changed from: protected */
        public Renderer createRenderer(Player player, TrackControl trackControl) {
            Renderer renderer = super.createRenderer(player, trackControl);
            if (renderer != null) {
                AudioMediaDeviceSession.setVolumeControl(renderer, this.outputVolumeControl);
            }
            return renderer;
        }

        /* access modifiers changed from: 0000 */
        public void removeLocalUserAudioLevelListener(SimpleAudioLevelListener l) {
            synchronized (AudioMixerMediaDevice.this.localUserAudioLevelListenersSyncRoot) {
                int index = AudioMixerMediaDevice.this.localUserAudioLevelListeners.indexOf(new SimpleAudioLevelListenerWrapper(l));
                if (index != -1) {
                    SimpleAudioLevelListenerWrapper wrapper = (SimpleAudioLevelListenerWrapper) AudioMixerMediaDevice.this.localUserAudioLevelListeners.get(index);
                    if (wrapper.referenceCount > 1) {
                        wrapper.referenceCount--;
                    } else {
                        AudioMixerMediaDevice.this.localUserAudioLevelListeners = new ArrayList(AudioMixerMediaDevice.this.localUserAudioLevelListeners);
                        AudioMixerMediaDevice.this.localUserAudioLevelListeners.remove(wrapper);
                    }
                }
                if (AudioMixerMediaDevice.this.localUserAudioLevelListeners.isEmpty()) {
                    AudioMixerMediaDevice.this.localUserAudioLevelDispatcher.setAudioLevelListener(null);
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void removeMediaStreamMediaDeviceSession(MediaStreamMediaDeviceSession mediaStreamMediaDeviceSession) {
            if (mediaStreamMediaDeviceSession != null) {
                synchronized (this.mediaStreamMediaDeviceSessions) {
                    if (this.mediaStreamMediaDeviceSessions.remove(mediaStreamMediaDeviceSession) && this.mediaStreamMediaDeviceSessions.isEmpty()) {
                        close();
                    }
                }
            }
        }

        public void removePlaybackDataSource(final DataSource playbackDataSource) {
            AudioMixerMediaDevice.this.removeInputDataSources(new DataSourceFilter() {
                public boolean accept(DataSource dataSource) {
                    return dataSource.equals(playbackDataSource);
                }
            });
        }

        public void removeReceiveStream(ReceiveStream receiveStream) {
            long ssrc = 4294967295L & receiveStream.getSSRC();
            removeSSRC(ssrc);
            AudioMixerMediaDevice.this.audioLevelCache.removeLevel(ssrc);
        }
    }

    private static class MediaStreamMediaDeviceSession extends AudioMediaDeviceSession implements PropertyChangeListener {
        private final AudioMixerMediaDeviceSession audioMixerMediaDeviceSession;
        private SimpleAudioLevelListener localUserAudioLevelListener = null;
        private SimpleAudioLevelListener streamAudioLevelListener = null;
        private final Object streamAudioLevelListenerLock = new Object();

        public MediaStreamMediaDeviceSession(AudioMixerMediaDeviceSession audioMixerMediaDeviceSession) {
            super(audioMixerMediaDeviceSession.getDevice());
            this.audioMixerMediaDeviceSession = audioMixerMediaDeviceSession;
            this.audioMixerMediaDeviceSession.addMediaStreamMediaDeviceSession(this);
            this.audioMixerMediaDeviceSession.addPropertyChangeListener(this);
        }

        public void close() {
            try {
                super.close();
            } finally {
                this.audioMixerMediaDeviceSession.removeMediaStreamMediaDeviceSession(this);
            }
        }

        /* access modifiers changed from: protected */
        public Player createPlayer(DataSource dataSource) {
            return null;
        }

        public long[] getRemoteSSRCList() {
            return this.audioMixerMediaDeviceSession.getRemoteSSRCList();
        }

        /* access modifiers changed from: protected */
        public void playbackDataSourceAdded(DataSource playbackDataSource) {
            super.playbackDataSourceAdded(playbackDataSource);
            DataSource captureDevice = getCaptureDevice();
            if (captureDevice instanceof PushBufferDataSourceDelegate) {
                captureDevice = ((PushBufferDataSourceDelegate) captureDevice).getDataSource();
            }
            if (captureDevice instanceof AudioMixingPushBufferDataSource) {
                ((AudioMixingPushBufferDataSource) captureDevice).addInDataSource(playbackDataSource);
            }
            this.audioMixerMediaDeviceSession.addPlaybackDataSource(playbackDataSource);
        }

        /* access modifiers changed from: protected */
        public void playbackDataSourceRemoved(DataSource playbackDataSource) {
            super.playbackDataSourceRemoved(playbackDataSource);
            this.audioMixerMediaDeviceSession.removePlaybackDataSource(playbackDataSource);
        }

        /* access modifiers changed from: protected */
        public void playbackDataSourceUpdated(DataSource playbackDataSource) {
            super.playbackDataSourceUpdated(playbackDataSource);
            DataSource captureDevice = getCaptureDevice();
            if (captureDevice instanceof PushBufferDataSourceDelegate) {
                captureDevice = ((PushBufferDataSourceDelegate) captureDevice).getDataSource();
            }
            if (captureDevice instanceof AudioMixingPushBufferDataSource) {
                ((AudioMixingPushBufferDataSource) captureDevice).updateInDataSource(playbackDataSource);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (MediaDeviceSession.SSRC_LIST.equals(evt.getPropertyName())) {
                firePropertyChange(MediaDeviceSession.SSRC_LIST, evt.getOldValue(), evt.getNewValue());
            }
        }

        /* access modifiers changed from: protected */
        public void receiveStreamAdded(ReceiveStream receiveStream) {
            super.receiveStreamAdded(receiveStream);
            synchronized (this.streamAudioLevelListenerLock) {
                if (this.streamAudioLevelListener != null) {
                    this.audioMixerMediaDeviceSession.setStreamAudioLevelListener(receiveStream, this.streamAudioLevelListener);
                }
            }
            this.audioMixerMediaDeviceSession.addReceiveStream(receiveStream);
        }

        /* access modifiers changed from: protected */
        public void receiveStreamRemoved(ReceiveStream receiveStream) {
            super.receiveStreamRemoved(receiveStream);
            this.audioMixerMediaDeviceSession.removeReceiveStream(receiveStream);
        }

        /* access modifiers changed from: protected */
        public void registerLocalUserAudioLevelEffect(Processor processor) {
        }

        public void setLocalUserAudioLevelListener(SimpleAudioLevelListener l) {
            if (this.localUserAudioLevelListener != null) {
                this.audioMixerMediaDeviceSession.removeLocalUserAudioLevelListener(this.localUserAudioLevelListener);
                this.localUserAudioLevelListener = null;
            }
            if (l != null) {
                this.localUserAudioLevelListener = l;
                if (!isMute()) {
                    this.audioMixerMediaDeviceSession.addLocalUserAudioLevelListener(l);
                }
            }
        }

        public void setOutputVolumeControl(VolumeControl outputVolumeControl) {
            this.audioMixerMediaDeviceSession.setOutputVolumeControl(outputVolumeControl);
        }

        public void setStreamAudioLevelListener(SimpleAudioLevelListener listener) {
            synchronized (this.streamAudioLevelListenerLock) {
                this.streamAudioLevelListener = listener;
                for (ReceiveStream receiveStream : getReceiveStreams()) {
                    this.audioMixerMediaDeviceSession.setStreamAudioLevelListener(receiveStream, this.streamAudioLevelListener);
                }
            }
        }

        public int getLastMeasuredAudioLevel(long csrc) {
            return ((AudioMixerMediaDevice) getDevice()).audioLevelCache.getLevel(csrc);
        }

        public int getLastMeasuredLocalUserAudioLevel() {
            return ((AudioMixerMediaDevice) getDevice()).lastMeasuredLocalUserAudioLevel;
        }

        public void setMute(boolean mute) {
            boolean oldValue = isMute();
            super.setMute(mute);
            boolean newValue = isMute();
            if (oldValue == newValue) {
                return;
            }
            if (newValue) {
                this.audioMixerMediaDeviceSession.removeLocalUserAudioLevelListener(this.localUserAudioLevelListener);
            } else {
                this.audioMixerMediaDeviceSession.addLocalUserAudioLevelListener(this.localUserAudioLevelListener);
            }
        }
    }

    private static class SimpleAudioLevelListenerWrapper {
        public final SimpleAudioLevelListener listener;
        int referenceCount = 1;

        public SimpleAudioLevelListenerWrapper(SimpleAudioLevelListener l) {
            this.listener = l;
        }

        public boolean equals(Object obj) {
            return (obj instanceof SimpleAudioLevelListenerWrapper) && ((SimpleAudioLevelListenerWrapper) obj).listener == this.listener;
        }

        public int hashCode() {
            return this.listener.hashCode();
        }
    }

    public AudioMixerMediaDevice(AudioMediaDeviceImpl device) {
        if (device.getDirection().allowsSending()) {
            this.device = device;
            return;
        }
        throw new IllegalArgumentException("device must be able to capture");
    }

    public void connect(DataSource captureDevice) throws IOException {
        DataSource effectiveCaptureDevice = captureDevice;
        if (captureDevice instanceof PushBufferDataSourceDelegate) {
            captureDevice = ((PushBufferDataSourceDelegate) captureDevice).getDataSource();
        }
        if (captureDevice instanceof AudioMixingPushBufferDataSource) {
            effectiveCaptureDevice.connect();
        } else {
            this.device.connect(effectiveCaptureDevice);
        }
    }

    public AudioMixingPushBufferDataSource createOutputDataSource() {
        return getAudioMixer().createOutDataSource();
    }

    /* access modifiers changed from: protected */
    public Processor createPlayer(DataSource dataSource) throws Exception {
        return this.device.createPlayer(dataSource);
    }

    /* access modifiers changed from: protected */
    public Renderer createRenderer() {
        return this.device.createRenderer();
    }

    public synchronized MediaDeviceSession createSession() {
        if (this.deviceSession == null) {
            this.deviceSession = new AudioMixerMediaDeviceSession();
        }
        return new MediaStreamMediaDeviceSession(this.deviceSession);
    }

    /* access modifiers changed from: private */
    public void fireLocalUserAudioLevelChanged(int level) {
        List<SimpleAudioLevelListenerWrapper> localUserAudioLevelListeners;
        synchronized (this.localUserAudioLevelListenersSyncRoot) {
            localUserAudioLevelListeners = this.localUserAudioLevelListeners;
        }
        int localUserAudioLevelListenerCount = localUserAudioLevelListeners.size();
        for (int i = 0; i < localUserAudioLevelListenerCount; i++) {
            ((SimpleAudioLevelListenerWrapper) localUserAudioLevelListeners.get(i)).listener.audioLevelChanged(level);
        }
    }

    /* access modifiers changed from: private|declared_synchronized */
    public synchronized AudioMixer getAudioMixer() {
        if (this.audioMixer == null) {
            this.audioMixer = new AudioMixer(this.device.createCaptureDevice()) {
                /* access modifiers changed from: protected */
                public void connect(DataSource dataSource, DataSource inputDataSource) throws IOException {
                    if (inputDataSource == this.captureDevice) {
                        AudioMixerMediaDevice.this.connect(dataSource);
                    } else {
                        super.connect(dataSource, inputDataSource);
                    }
                }

                /* access modifiers changed from: protected */
                public void read(PushBufferStream stream, Buffer buffer, DataSource dataSource) throws IOException {
                    super.read(stream, buffer, dataSource);
                    if (dataSource == this.captureDevice) {
                        synchronized (AudioMixerMediaDevice.this.localUserAudioLevelListenersSyncRoot) {
                            if (AudioMixerMediaDevice.this.localUserAudioLevelListeners.isEmpty()) {
                                return;
                            }
                            AudioMixerMediaDevice.this.localUserAudioLevelDispatcher.addData(buffer);
                        }
                    } else if (dataSource instanceof ReceiveStreamPushBufferDataSource) {
                        AudioLevelEventDispatcher streamEventDispatcher;
                        ReceiveStream receiveStream = ((ReceiveStreamPushBufferDataSource) dataSource).getReceiveStream();
                        synchronized (AudioMixerMediaDevice.this.streamAudioLevelListeners) {
                            streamEventDispatcher = (AudioLevelEventDispatcher) AudioMixerMediaDevice.this.streamAudioLevelListeners.get(receiveStream);
                        }
                        if (streamEventDispatcher != null && !buffer.isDiscard() && buffer.getLength() > 0 && buffer.getData() != null) {
                            streamEventDispatcher.addData(buffer);
                        }
                    }
                }
            };
        }
        return this.audioMixer;
    }

    public MediaDirection getDirection() {
        return this.device.getDirection();
    }

    public MediaFormat getFormat() {
        return this.device.getFormat();
    }

    public MediaType getMediaType() {
        return this.device.getMediaType();
    }

    public List<RTPExtension> getSupportedExtensions() {
        if (this.rtpExtensions == null) {
            URI csrcAudioLevelURN;
            this.rtpExtensions = new ArrayList(1);
            try {
                csrcAudioLevelURN = new URI(RTPExtension.CSRC_AUDIO_LEVEL_URN);
            } catch (URISyntaxException e) {
                csrcAudioLevelURN = null;
                if (logger.isInfoEnabled()) {
                    logger.info("Aha! Someone messed with the source!", e);
                }
            }
            if (csrcAudioLevelURN != null) {
                this.rtpExtensions.add(new RTPExtension(csrcAudioLevelURN, MediaDirection.SENDRECV));
            }
        }
        return this.rtpExtensions;
    }

    public List<MediaFormat> getSupportedFormats(QualityPreset sendPreset, QualityPreset receivePreset) {
        return this.device.getSupportedFormats();
    }

    public List<MediaFormat> getSupportedFormats(QualityPreset sendPreset, QualityPreset receivePreset, EncodingConfiguration encodingConfiguration) {
        return this.device.getSupportedFormats(encodingConfiguration);
    }

    public MediaDevice getWrappedDevice() {
        return this.device;
    }

    /* access modifiers changed from: 0000 */
    public void removeInputDataSources(DataSourceFilter dataSourceFilter) {
        AudioMixer audioMixer = this.audioMixer;
        if (audioMixer != null) {
            audioMixer.removeInDataSources(dataSourceFilter);
        }
    }

    public TranscodingDataSource getTranscodingDataSource(DataSource inputDataSource) {
        return getAudioMixer().getTranscodingDataSource(inputDataSource);
    }
}
