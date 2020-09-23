package org.jitsi.impl.neomedia;

import com.sun.media.util.Registry;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.media.CaptureDeviceInfo;
import javax.media.Codec;
import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NotConfiguredError;
import javax.media.Player;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.UnsupportedPlugInException;
import javax.media.control.TrackControl;
import javax.media.format.RGBFormat;
import javax.media.protocol.DataSource;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Point;
import org.jitsi.android.util.java.awt.Rectangle;
import org.jitsi.android.util.java.awt.Window;
import org.jitsi.android.util.java.awt.event.HierarchyEvent;
import org.jitsi.android.util.java.awt.event.HierarchyListener;
import org.jitsi.android.util.java.awt.event.WindowAdapter;
import org.jitsi.android.util.java.awt.event.WindowEvent;
import org.jitsi.android.util.java.awt.event.WindowListener;
import org.jitsi.android.util.javax.swing.JComponent;
import org.jitsi.android.util.javax.swing.JLabel;
import org.jitsi.android.util.javax.swing.SwingUtilities;
import org.jitsi.impl.neomedia.codec.EncodingConfigurationConfigImpl;
import org.jitsi.impl.neomedia.codec.EncodingConfigurationImpl;
import org.jitsi.impl.neomedia.codec.FMJPlugInConfiguration;
import org.jitsi.impl.neomedia.codec.video.AVFrameFormat;
import org.jitsi.impl.neomedia.codec.video.HFlip;
import org.jitsi.impl.neomedia.codec.video.SwScale;
import org.jitsi.impl.neomedia.device.AudioMediaDeviceImpl;
import org.jitsi.impl.neomedia.device.AudioMixerMediaDevice;
import org.jitsi.impl.neomedia.device.DeviceConfiguration;
import org.jitsi.impl.neomedia.device.DeviceSystem;
import org.jitsi.impl.neomedia.device.MediaDeviceImpl;
import org.jitsi.impl.neomedia.device.ScreenDeviceImpl;
import org.jitsi.impl.neomedia.device.VideoTranslatorMediaDevice;
import org.jitsi.impl.neomedia.format.MediaFormatFactoryImpl;
import org.jitsi.impl.neomedia.format.MediaFormatImpl;
import org.jitsi.impl.neomedia.transform.dtls.DtlsControlImpl;
import org.jitsi.impl.neomedia.transform.sdes.SDesControlImpl;
import org.jitsi.impl.neomedia.transform.zrtp.ZrtpControlImpl;
import org.jitsi.impl.neomedia.transform.zrtp.ZrtpFortunaEntropyGatherer;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.BasicVolumeControl;
import org.jitsi.service.neomedia.MediaService;
import org.jitsi.service.neomedia.MediaStream;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.MediaUseCase;
import org.jitsi.service.neomedia.RTPTranslator;
import org.jitsi.service.neomedia.Recorder;
import org.jitsi.service.neomedia.Recorder.Listener;
import org.jitsi.service.neomedia.SrtpControl;
import org.jitsi.service.neomedia.SrtpControlType;
import org.jitsi.service.neomedia.StreamConnector;
import org.jitsi.service.neomedia.VolumeControl;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.service.neomedia.codec.EncodingConfiguration;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.device.ScreenDevice;
import org.jitsi.service.neomedia.format.MediaFormat;
import org.jitsi.service.neomedia.format.MediaFormatFactory;
import org.jitsi.service.resources.ResourceManagementService;
import org.jitsi.util.Logger;
import org.jitsi.util.OSUtils;
import org.jitsi.util.event.PropertyChangeNotifier;
import org.jitsi.util.swing.VideoContainer;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class MediaServiceImpl extends PropertyChangeNotifier implements MediaService {
    public static final String DISABLE_AUDIO_SUPPORT_PNAME = "net.java.sip.communicator.service.media.DISABLE_AUDIO_SUPPORT";
    public static final String DISABLE_SET_AUDIO_SYSTEM_PNAME = "net.java.sip.communicator.impl.neomedia.audiosystem.DISABLED";
    public static final String DISABLE_VIDEO_SUPPORT_PNAME = "net.java.sip.communicator.service.media.DISABLE_VIDEO_SUPPORT";
    private static final String DYNAMIC_PAYLOAD_TYPE_PREFERENCES_PNAME_PREFIX = "net.java.sip.communicator.impl.neomedia.dynamicPayloadTypePreferences";
    private static final List<MediaDevice> EMPTY_DEVICES = Collections.emptyList();
    private static final String ENCODING_CONFIG_PROP_PREFIX = "net.java.sip.communicator.impl.neomedia.codec.EncodingConfiguration";
    private static final String JMF_REGISTRY_DISABLE_COMMIT = "net.sf.fmj.utility.JmfRegistry.disableCommit";
    private static final String JMF_REGISTRY_DISABLE_LOAD = "net.sf.fmj.utility.JmfRegistry.disableLoad";
    private static Map<MediaFormat, Byte> dynamicPayloadTypePreferences;
    private static VolumeControl inputVolumeControl;
    private static boolean jmfRegistryDisableLoad;
    private static final Logger logger = Logger.getLogger(MediaServiceImpl.class);
    private static VolumeControl outputVolumeControl;
    private static boolean postInitializeOnce;
    private final List<MediaDeviceImpl> audioDevices = new ArrayList();
    private final EncodingConfiguration currentEncodingConfiguration;
    private final DeviceConfiguration deviceConfiguration = new DeviceConfiguration();
    private final PropertyChangeListener deviceConfigurationPropertyChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            MediaServiceImpl.this.deviceConfigurationPropertyChange(event);
        }
    };
    private MediaFormatFactory formatFactory;
    private MediaDevice nonSendAudioDevice;
    private MediaDevice nonSendVideoDevice;
    private final List<Listener> recorderListeners = new ArrayList();
    private final List<MediaDeviceImpl> videoDevices = new ArrayList();

    private static class VideoContainerHierarchyListener implements HierarchyListener {
        private JComponent container;
        private Player player;
        private Component preview = null;
        private Window window;
        private WindowListener windowListener;

        VideoContainerHierarchyListener(JComponent container, Player player) {
            this.container = container;
            this.player = player;
        }

        /* access modifiers changed from: 0000 */
        public void setPreview(Component preview) {
            this.preview = preview;
        }

        public void dispose() {
            if (this.windowListener != null) {
                if (this.window != null) {
                    this.window.removeWindowListener(this.windowListener);
                    this.window = null;
                }
                this.windowListener = null;
            }
            this.container.removeHierarchyListener(this);
            MediaServiceImpl.disposePlayer(this.player);
            if (this.preview != null) {
                this.container.remove(this.preview);
            }
        }

        public void hierarchyChanged(HierarchyEvent event) {
            if ((event.getChangeFlags() & 2) != 0) {
                if (this.container.isDisplayable()) {
                    if (this.preview != null) {
                        this.player.start();
                    }
                    if (this.windowListener == null) {
                        this.window = SwingUtilities.windowForComponent(this.container);
                        if (this.window != null) {
                            this.windowListener = new WindowAdapter() {
                                public void windowClosing(WindowEvent event) {
                                    VideoContainerHierarchyListener.this.dispose();
                                }
                            };
                            this.window.addWindowListener(this.windowListener);
                            return;
                        }
                        return;
                    }
                    return;
                }
                dispose();
            }
        }
    }

    static {
        setupFMJ();
    }

    public MediaServiceImpl() {
        this.deviceConfiguration.addPropertyChangeListener(this.deviceConfigurationPropertyChangeListener);
        this.currentEncodingConfiguration = new EncodingConfigurationConfigImpl(ENCODING_CONFIG_PROP_PREFIX);
        synchronized (MediaServiceImpl.class) {
            if (!postInitializeOnce) {
                postInitializeOnce = true;
                postInitializeOnce(this);
            }
        }
    }

    public MediaStream createMediaStream(MediaDevice device) {
        return createMediaStream(null, device);
    }

    public MediaStream createMediaStream(MediaType mediaType) {
        return createMediaStream(mediaType, null, null, null);
    }

    public MediaStream createMediaStream(StreamConnector connector, MediaDevice device) {
        return createMediaStream(connector, device, null);
    }

    public MediaStream createMediaStream(StreamConnector connector, MediaType mediaType) {
        return createMediaStream(connector, mediaType, null);
    }

    public MediaStream createMediaStream(StreamConnector connector, MediaDevice device, SrtpControl srtpControl) {
        return createMediaStream(null, connector, device, srtpControl);
    }

    public MediaStream createMediaStream(StreamConnector connector, MediaType mediaType, SrtpControl srtpControl) {
        return createMediaStream(mediaType, connector, null, srtpControl);
    }

    private MediaStream createMediaStream(MediaType mediaType, StreamConnector connector, MediaDevice device, SrtpControl srtpControl) {
        if (mediaType == null) {
            if (device == null) {
                throw new NullPointerException("device");
            }
            mediaType = device.getMediaType();
        } else if (!(device == null || mediaType.equals(device.getMediaType()))) {
            throw new IllegalArgumentException("device");
        }
        switch (mediaType) {
            case AUDIO:
                return new AudioMediaStreamImpl(connector, device, srtpControl);
            case VIDEO:
                return new VideoMediaStreamImpl(connector, device, srtpControl);
            default:
                return null;
        }
    }

    public MediaDevice createMixer(MediaDevice device) {
        switch (device.getMediaType()) {
            case AUDIO:
                return new AudioMixerMediaDevice((AudioMediaDeviceImpl) device);
            case VIDEO:
                return new VideoTranslatorMediaDevice((MediaDeviceImpl) device);
            default:
                return null;
        }
    }

    public MediaDevice getDefaultDevice(MediaType mediaType, MediaUseCase useCase) {
        CaptureDeviceInfo captureDeviceInfo;
        switch (mediaType) {
            case AUDIO:
                captureDeviceInfo = getDeviceConfiguration().getAudioCaptureDevice();
                break;
            case VIDEO:
                captureDeviceInfo = getDeviceConfiguration().getVideoCaptureDevice(useCase);
                break;
            default:
                captureDeviceInfo = null;
                break;
        }
        MediaDevice defaultDevice = null;
        if (captureDeviceInfo != null) {
            for (MediaDevice device : getDevices(mediaType, useCase)) {
                if ((device instanceof MediaDeviceImpl) && captureDeviceInfo.equals(((MediaDeviceImpl) device).getCaptureDeviceInfo())) {
                    defaultDevice = device;
                }
            }
        }
        if (defaultDevice != null) {
            return defaultDevice;
        }
        switch (mediaType) {
            case AUDIO:
                return getNonSendAudioDevice();
            case VIDEO:
                return getNonSendVideoDevice();
            default:
                return defaultDevice;
        }
    }

    public DeviceConfiguration getDeviceConfiguration() {
        return this.deviceConfiguration;
    }

    /* JADX WARNING: Removed duplicated region for block: B:54:0x005e A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0089  */
    public java.util.List<org.jitsi.service.neomedia.device.MediaDevice> getDevices(org.jitsi.service.neomedia.MediaType r14, org.jitsi.service.neomedia.MediaUseCase r15) {
        /*
        r13 = this;
        r11 = org.jitsi.service.neomedia.MediaType.VIDEO;
        r11 = r11.equals(r14);
        if (r11 == 0) goto L_0x000d;
    L_0x0008:
        r11 = org.jitsi.service.neomedia.MediaType.VIDEO;
        org.jitsi.impl.neomedia.device.DeviceSystem.initializeDeviceSystems(r11);
    L_0x000d:
        r11 = org.jitsi.impl.neomedia.MediaServiceImpl.AnonymousClass5.$SwitchMap$org$jitsi$service$neomedia$MediaType;
        r12 = r14.ordinal();
        r11 = r11[r12];
        switch(r11) {
            case 1: goto L_0x001b;
            case 2: goto L_0x004f;
            default: goto L_0x0018;
        };
    L_0x0018:
        r10 = EMPTY_DEVICES;
    L_0x001a:
        return r10;
    L_0x001b:
        r11 = r13.getDeviceConfiguration();
        r3 = r11.getAvailableAudioCaptureDevices();
        r9 = r13.audioDevices;
    L_0x0025:
        monitor-enter(r9);
        if (r3 == 0) goto L_0x002e;
    L_0x0028:
        r11 = r3.size();	 Catch:{ all -> 0x008d }
        if (r11 > 0) goto L_0x005a;
    L_0x002e:
        r9.clear();	 Catch:{ all -> 0x008d }
    L_0x0031:
        r10 = new java.util.ArrayList;	 Catch:{ all -> 0x008d }
        r10.<init>(r9);	 Catch:{ all -> 0x008d }
        monitor-exit(r9);	 Catch:{ all -> 0x008d }
        r11 = r10.isEmpty();
        if (r11 == 0) goto L_0x001a;
    L_0x003d:
        r11 = org.jitsi.impl.neomedia.MediaServiceImpl.AnonymousClass5.$SwitchMap$org$jitsi$service$neomedia$MediaType;
        r12 = r14.ordinal();
        r11 = r11[r12];
        switch(r11) {
            case 1: goto L_0x00c0;
            case 2: goto L_0x00c5;
            default: goto L_0x0048;
        };
    L_0x0048:
        r8 = 0;
    L_0x0049:
        if (r8 == 0) goto L_0x001a;
    L_0x004b:
        r10.add(r8);
        goto L_0x001a;
    L_0x004f:
        r11 = r13.getDeviceConfiguration();
        r3 = r11.getAvailableVideoCaptureDevices(r15);
        r9 = r13.videoDevices;
        goto L_0x0025;
    L_0x005a:
        r6 = r9.iterator();	 Catch:{ all -> 0x008d }
    L_0x005e:
        r11 = r6.hasNext();	 Catch:{ all -> 0x008d }
        if (r11 == 0) goto L_0x0090;
    L_0x0064:
        r2 = r3.iterator();	 Catch:{ all -> 0x008d }
        r11 = r6.next();	 Catch:{ all -> 0x008d }
        r11 = (org.jitsi.impl.neomedia.device.MediaDeviceImpl) r11;	 Catch:{ all -> 0x008d }
        r0 = r11.getCaptureDeviceInfo();	 Catch:{ all -> 0x008d }
        r5 = 0;
    L_0x0073:
        r11 = r2.hasNext();	 Catch:{ all -> 0x008d }
        if (r11 == 0) goto L_0x0087;
    L_0x0079:
        r11 = r2.next();	 Catch:{ all -> 0x008d }
        r11 = r0.equals(r11);	 Catch:{ all -> 0x008d }
        if (r11 == 0) goto L_0x0073;
    L_0x0083:
        r5 = 1;
        r2.remove();	 Catch:{ all -> 0x008d }
    L_0x0087:
        if (r5 != 0) goto L_0x005e;
    L_0x0089:
        r6.remove();	 Catch:{ all -> 0x008d }
        goto L_0x005e;
    L_0x008d:
        r11 = move-exception;
        monitor-exit(r9);	 Catch:{ all -> 0x008d }
        throw r11;
    L_0x0090:
        r7 = r3.iterator();	 Catch:{ all -> 0x008d }
    L_0x0094:
        r11 = r7.hasNext();	 Catch:{ all -> 0x008d }
        if (r11 == 0) goto L_0x0031;
    L_0x009a:
        r1 = r7.next();	 Catch:{ all -> 0x008d }
        r1 = (javax.media.CaptureDeviceInfo) r1;	 Catch:{ all -> 0x008d }
        if (r1 == 0) goto L_0x0094;
    L_0x00a2:
        r11 = org.jitsi.impl.neomedia.MediaServiceImpl.AnonymousClass5.$SwitchMap$org$jitsi$service$neomedia$MediaType;	 Catch:{ all -> 0x008d }
        r12 = r14.ordinal();	 Catch:{ all -> 0x008d }
        r11 = r11[r12];	 Catch:{ all -> 0x008d }
        switch(r11) {
            case 1: goto L_0x00b4;
            case 2: goto L_0x00ba;
            default: goto L_0x00ad;
        };	 Catch:{ all -> 0x008d }
    L_0x00ad:
        r4 = 0;
    L_0x00ae:
        if (r4 == 0) goto L_0x0094;
    L_0x00b0:
        r9.add(r4);	 Catch:{ all -> 0x008d }
        goto L_0x0094;
    L_0x00b4:
        r4 = new org.jitsi.impl.neomedia.device.AudioMediaDeviceImpl;	 Catch:{ all -> 0x008d }
        r4.m2303init(r1);	 Catch:{ all -> 0x008d }
        goto L_0x00ae;
    L_0x00ba:
        r4 = new org.jitsi.impl.neomedia.device.MediaDeviceImpl;	 Catch:{ all -> 0x008d }
        r4.m2299init(r1, r14);	 Catch:{ all -> 0x008d }
        goto L_0x00ae;
    L_0x00c0:
        r8 = r13.getNonSendAudioDevice();
        goto L_0x0049;
    L_0x00c5:
        r8 = r13.getNonSendVideoDevice();
        goto L_0x0049;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.MediaServiceImpl.getDevices(org.jitsi.service.neomedia.MediaType, org.jitsi.service.neomedia.MediaUseCase):java.util.List");
    }

    public EncodingConfiguration getCurrentEncodingConfiguration() {
        return this.currentEncodingConfiguration;
    }

    public MediaFormatFactory getFormatFactory() {
        if (this.formatFactory == null) {
            this.formatFactory = new MediaFormatFactoryImpl();
        }
        return this.formatFactory;
    }

    private MediaDevice getNonSendAudioDevice() {
        if (this.nonSendAudioDevice == null) {
            this.nonSendAudioDevice = new AudioMediaDeviceImpl();
        }
        return this.nonSendAudioDevice;
    }

    private MediaDevice getNonSendVideoDevice() {
        if (this.nonSendVideoDevice == null) {
            this.nonSendVideoDevice = new MediaDeviceImpl(MediaType.VIDEO);
        }
        return this.nonSendVideoDevice;
    }

    public SrtpControl createSrtpControl(SrtpControlType srtpControlType) {
        switch (srtpControlType) {
            case DTLS_SRTP:
                return new DtlsControlImpl();
            case SDES:
                return new SDesControlImpl();
            case ZRTP:
                return new ZrtpControlImpl();
            default:
                return null;
        }
    }

    public VolumeControl getOutputVolumeControl() {
        if (outputVolumeControl == null) {
            outputVolumeControl = new BasicVolumeControl(VolumeControl.PLAYBACK_VOLUME_LEVEL_PROPERTY_NAME);
        }
        return outputVolumeControl;
    }

    public VolumeControl getInputVolumeControl() {
        if (inputVolumeControl == null) {
            try {
                inputVolumeControl = new HardwareVolumeControl(this, VolumeControl.CAPTURE_VOLUME_LEVEL_PROPERTY_NAME);
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath t2 = (ThreadDeath) t;
                } else if (t instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
            if (inputVolumeControl == null) {
                inputVolumeControl = new BasicVolumeControl(VolumeControl.CAPTURE_VOLUME_LEVEL_PROPERTY_NAME);
            }
        }
        return inputVolumeControl;
    }

    public List<ScreenDevice> getAvailableScreenDevices() {
        ScreenDevice[] screens = ScreenDeviceImpl.getAvailableScreenDevices();
        if (screens == null || screens.length == 0) {
            return Collections.emptyList();
        }
        return new ArrayList(Arrays.asList(screens));
    }

    public ScreenDevice getDefaultScreenDevice() {
        return ScreenDeviceImpl.getDefaultScreenDevice();
    }

    public Recorder createRecorder(MediaDevice device) {
        if (device instanceof AudioMixerMediaDevice) {
            return new RecorderImpl((AudioMixerMediaDevice) device);
        }
        return null;
    }

    public Map<MediaFormat, Byte> getDynamicPayloadTypePreferences() {
        if (dynamicPayloadTypePreferences == null) {
            dynamicPayloadTypePreferences = new HashMap();
            MediaFormat telephoneEvent = MediaUtils.getMediaFormat(Constants.TELEPHONE_EVENT, 8000.0d);
            if (telephoneEvent != null) {
                dynamicPayloadTypePreferences.put(telephoneEvent, Byte.valueOf((byte) 101));
            }
            MediaFormat h264 = MediaUtils.getMediaFormat("H264", 90000.0d);
            if (h264 != null) {
                dynamicPayloadTypePreferences.put(h264, Byte.valueOf((byte) 99));
            }
            ConfigurationService cfg = LibJitsi.getConfigurationService();
            if (cfg != null) {
                String prefix = DYNAMIC_PAYLOAD_TYPE_PREFERENCES_PNAME_PREFIX;
                for (String propertyName : cfg.getPropertyNamesByPrefix(prefix, true)) {
                    byte dynamicPayloadTypePreference = (byte) 0;
                    Throwable exception = null;
                    try {
                        dynamicPayloadTypePreference = Byte.parseByte(propertyName.substring(prefix.length() + 1));
                    } catch (IndexOutOfBoundsException ioobe) {
                        exception = ioobe;
                    } catch (NumberFormatException nfe) {
                        exception = nfe;
                    }
                    if (exception != null) {
                        logger.warn("Ignoring dynamic payload type preference which could not be parsed: " + propertyName, exception);
                    } else {
                        String source = cfg.getString(propertyName);
                        if (!(source == null || source.length() == 0)) {
                            try {
                                JSONObject json = (JSONObject) JSONValue.parseWithException(source);
                                String encoding = (String) json.get(MediaFormatImpl.ENCODING_PNAME);
                                long clockRate = ((Long) json.get(MediaFormatImpl.CLOCK_RATE_PNAME)).longValue();
                                Map<String, String> fmtps = new HashMap();
                                if (json.containsKey(MediaFormatImpl.FORMAT_PARAMETERS_PNAME)) {
                                    JSONObject jsonFmtps = (JSONObject) json.get(MediaFormatImpl.FORMAT_PARAMETERS_PNAME);
                                    for (Object obj : jsonFmtps.keySet()) {
                                        String key = obj.toString();
                                        fmtps.put(key, (String) jsonFmtps.get(key));
                                    }
                                }
                                MediaFormat mediaFormat = MediaUtils.getMediaFormat(encoding, (double) clockRate, fmtps);
                                if (mediaFormat != null) {
                                    dynamicPayloadTypePreferences.put(mediaFormat, Byte.valueOf(dynamicPayloadTypePreference));
                                }
                            } catch (Throwable jsone) {
                                logger.warn("Ignoring dynamic payload type preference which could not be parsed: " + source, jsone);
                            }
                        }
                    }
                }
            }
        }
        return dynamicPayloadTypePreferences;
    }

    public Object getVideoPreviewComponent(MediaDevice device, int preferredWidth, int preferredHeight) {
        String noPreviewText;
        ResourceManagementService resources = LibJitsi.getResourceManagementService();
        if (resources == null) {
            noPreviewText = "";
        } else {
            noPreviewText = resources.getI18NString("impl.media.configform.NO_PREVIEW");
        }
        JLabel noPreview = new JLabel(noPreviewText);
        noPreview.setHorizontalAlignment(0);
        noPreview.setVerticalAlignment(0);
        final JComponent videoContainer = new VideoContainer(noPreview, false);
        if (preferredWidth > 0 && preferredHeight > 0) {
            videoContainer.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        }
        if (device != null) {
            try {
                CaptureDeviceInfo captureDeviceInfo = ((MediaDeviceImpl) device).getCaptureDeviceInfo();
                if (captureDeviceInfo != null) {
                    DataSource dataSource = Manager.createDataSource(captureDeviceInfo.getLocator());
                    if (preferredWidth < 128 || preferredHeight < 96) {
                        preferredWidth = 128;
                        preferredHeight = 96;
                    }
                    VideoMediaStreamImpl.selectVideoSize(dataSource, preferredWidth, preferredHeight);
                    dataSource.connect();
                    Processor player = Manager.createProcessor(dataSource);
                    final VideoContainerHierarchyListener listener = new VideoContainerHierarchyListener(videoContainer, player);
                    videoContainer.addHierarchyListener(listener);
                    final MediaLocator locator = dataSource.getLocator();
                    player.addControllerListener(new ControllerListener() {
                        public void controllerUpdate(ControllerEvent event) {
                            MediaServiceImpl.controllerUpdateForPreview(event, videoContainer, locator, listener);
                        }
                    });
                    player.configure();
                }
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath t2 = (ThreadDeath) t;
                } else {
                    logger.error("Failed to create video preview", t);
                }
            }
        }
        return videoContainer;
    }

    /* access modifiers changed from: private|static */
    public static void controllerUpdateForPreview(ControllerEvent event, JComponent videoContainer, MediaLocator locator, VideoContainerHierarchyListener listener) {
        if (event instanceof ConfigureCompleteEvent) {
            Processor player = (Processor) event.getSourceController();
            TrackControl[] trackControls = player.getTrackControls();
            if (!(trackControls == null || trackControls.length == 0)) {
                TrackControl[] arr$ = trackControls;
                try {
                    if (0 < arr$.length) {
                        TrackControl trackControl = arr$[0];
                        SwScale scaler = new SwScale();
                        trackControl.setCodecChain(DeviceSystem.LOCATOR_PROTOCOL_IMGSTREAMING.equals(locator.getProtocol()) ? new Codec[]{scaler} : new Codec[]{new HFlip(), scaler});
                    }
                } catch (UnsupportedPlugInException upiex) {
                    logger.warn("Failed to add SwScale/VideoFlipEffect to codec chain", upiex);
                }
            }
            try {
                player.setContentDescriptor(null);
            } catch (NotConfiguredError nce) {
                logger.error("Failed to set ContentDescriptor of Processor", nce);
            }
            player.realize();
        } else if (event instanceof RealizeCompleteEvent) {
            Player player2 = (Player) event.getSourceController();
            Component video = player2.getVisualComponent();
            listener.setPreview(video);
            showPreview(videoContainer, video, player2);
        }
    }

    /* access modifiers changed from: private|static */
    public static void showPreview(final JComponent previewContainer, final Component preview, final Player player) {
        if (SwingUtilities.isEventDispatchThread()) {
            previewContainer.removeAll();
            if (preview != null) {
                previewContainer.add(preview);
                player.start();
                if (previewContainer.isDisplayable()) {
                    previewContainer.revalidate();
                    previewContainer.repaint();
                    return;
                }
                previewContainer.doLayout();
                return;
            }
            disposePlayer(player);
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MediaServiceImpl.showPreview(previewContainer, preview, player);
            }
        });
    }

    /* access modifiers changed from: private|static */
    public static void disposePlayer(final Player player) {
        new Thread(new Runnable() {
            public void run() {
                player.stop();
                player.deallocate();
                player.close();
            }
        }).start();
    }

    public MediaDevice getMediaDeviceForPartialDesktopStreaming(int width, int height, int x, int y) {
        int i;
        String name = "Partial desktop streaming";
        if (x < 0) {
            i = 0;
        } else {
            i = x;
        }
        ScreenDevice dev = getScreenForPoint(new Point(i, y < 0 ? 0 : y));
        if (dev != null) {
            int display = dev.getIndex();
            if (OSUtils.IS_MAC) {
                width = Math.round(((float) width) / 16.0f) * 16;
            } else {
                width = Math.round(((float) width) / 2.0f) * 2;
            }
            Dimension size = new Dimension(width, Math.round(((float) height) / 2.0f) * 2);
            Format[] formats = new Format[]{new AVFrameFormat(size, -1.0f, 27, -1), new RGBFormat(size, -1, Format.byteArray, -1.0f, 32, 2, 3, 4)};
            Rectangle bounds = ((ScreenDeviceImpl) dev).getBounds();
            MediaDevice device = new MediaDeviceImpl(new CaptureDeviceInfo(name + " " + display, new MediaLocator("imgstreaming:" + display + "," + (x - bounds.x) + "," + (y - bounds.y)), formats), MediaType.VIDEO);
            return device;
        }
        MediaDevice mediaDevice = null;
        return null;
    }

    public boolean isPartialStreaming(MediaDevice mediaDevice) {
        if (mediaDevice == null) {
            return false;
        }
        CaptureDeviceInfo cdi = ((MediaDeviceImpl) mediaDevice).getCaptureDeviceInfo();
        if (cdi == null || !cdi.getName().startsWith("Partial desktop streaming")) {
            return false;
        }
        return true;
    }

    public ScreenDevice getScreenForPoint(Point p) {
        for (ScreenDevice dev : getAvailableScreenDevices()) {
            if (dev.containsPoint(p)) {
                return dev;
            }
        }
        return null;
    }

    public Point getOriginForDesktopStreamingDevice(MediaDevice mediaDevice) {
        CaptureDeviceInfo cdi = ((MediaDeviceImpl) mediaDevice).getCaptureDeviceInfo();
        if (cdi == null) {
            return null;
        }
        MediaLocator locator = cdi.getLocator();
        if (!DeviceSystem.LOCATOR_PROTOCOL_IMGSTREAMING.equals(locator.getProtocol())) {
            return null;
        }
        String remainder = locator.getRemainder();
        String[] split = remainder.split(",");
        if (split != null && split.length > 1) {
            remainder = split[0];
        }
        int index = Integer.parseInt(remainder);
        List<ScreenDevice> devs = getAvailableScreenDevices();
        if (devs.size() - 1 < index) {
            return null;
        }
        Rectangle r = ((ScreenDeviceImpl) devs.get(index)).getBounds();
        return new Point(r.x, r.y);
    }

    public void addRecorderListener(Listener listener) {
        synchronized (this.recorderListeners) {
            if (!this.recorderListeners.contains(listener)) {
                this.recorderListeners.add(listener);
            }
        }
    }

    public void removeRecorderListener(Listener listener) {
        synchronized (this.recorderListeners) {
            this.recorderListeners.remove(listener);
        }
    }

    public Iterator<Listener> getRecorderListeners() {
        return this.recorderListeners.iterator();
    }

    /* access modifiers changed from: private */
    public void deviceConfigurationPropertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();
        if ("captureDevice".equals(propertyName) || "notifyDevice".equals(propertyName) || "playbackDevice".equals(propertyName) || DeviceConfiguration.VIDEO_CAPTURE_DEVICE.equals(propertyName)) {
            firePropertyChange(MediaService.DEFAULT_DEVICE, null, null);
        }
    }

    public RTPTranslator createRTPTranslator() {
        return new RTPTranslatorImpl();
    }

    public static boolean isJmfRegistryDisableLoad() {
        return jmfRegistryDisableLoad;
    }

    private static void postInitializeOnce(MediaServiceImpl mediaServiceImpl) {
        new ZrtpFortunaEntropyGatherer(mediaServiceImpl.getDeviceConfiguration()).setEntropy();
    }

    private static void setupFMJ() {
        Registry.set("allowLogging", Boolean.valueOf(true));
        if (System.getProperty(JMF_REGISTRY_DISABLE_LOAD) == null) {
            System.setProperty(JMF_REGISTRY_DISABLE_LOAD, "true");
        }
        jmfRegistryDisableLoad = "true".equalsIgnoreCase(System.getProperty(JMF_REGISTRY_DISABLE_LOAD));
        if (System.getProperty(JMF_REGISTRY_DISABLE_COMMIT) == null) {
            System.setProperty(JMF_REGISTRY_DISABLE_COMMIT, "true");
        }
        String scHomeDirLocation = System.getProperty(ConfigurationService.PNAME_SC_CACHE_DIR_LOCATION);
        if (scHomeDirLocation != null) {
            String scHomeDirName = System.getProperty(ConfigurationService.PNAME_SC_HOME_DIR_NAME);
            if (scHomeDirName != null) {
                File scHomeDir = new File(scHomeDirLocation, scHomeDirName);
                Registry.set("secure.logDir", new File(scHomeDir, "log").getPath());
                String jmfRegistryFilename = "net.sf.fmj.utility.JmfRegistry.filename";
                if (System.getProperty(jmfRegistryFilename) == null) {
                    System.setProperty(jmfRegistryFilename, new File(scHomeDir, ".fmj.registry").getAbsolutePath());
                }
            }
        }
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        if (cfg != null) {
            for (String prop : cfg.getPropertyNamesByPrefix("net.java.sip.communicator.impl.neomedia.adaptive_jitter_buffer", true)) {
                Registry.set("adaptive_jitter_buffer_" + prop.substring(prop.lastIndexOf(".") + 1), cfg.getString(prop));
            }
        }
        FMJPlugInConfiguration.registerCustomPackages();
        FMJPlugInConfiguration.registerCustomCodecs();
    }

    public EncodingConfiguration createEmptyEncodingConfiguration() {
        return new EncodingConfigurationImpl();
    }

    public static boolean isMediaTypeSupportEnabled(MediaType mediaType) {
        String propertyName;
        switch (mediaType) {
            case AUDIO:
                propertyName = DISABLE_AUDIO_SUPPORT_PNAME;
                break;
            case VIDEO:
                propertyName = DISABLE_VIDEO_SUPPORT_PNAME;
                break;
            default:
                return true;
        }
        ConfigurationService cfg = LibJitsi.getConfigurationService();
        if ((cfg == null || !cfg.getBoolean(propertyName, false)) && !Boolean.getBoolean(propertyName)) {
            return true;
        }
        return false;
    }
}
