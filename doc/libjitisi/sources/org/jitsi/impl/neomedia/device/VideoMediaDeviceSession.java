package org.jitsi.impl.neomedia.device;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.media.Buffer;
import javax.media.Codec;
import javax.media.ConfigureCompleteEvent;
import javax.media.Controller;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NotConfiguredError;
import javax.media.NotRealizedError;
import javax.media.Player;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.SizeChangeEvent;
import javax.media.UnsupportedPlugInException;
import javax.media.control.FormatControl;
import javax.media.control.FrameRateControl;
import javax.media.control.TrackControl;
import javax.media.format.VideoFormat;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import javax.media.protocol.SourceCloneable;
import org.jitsi.android.util.java.awt.Canvas;
import org.jitsi.android.util.java.awt.Color;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Graphics;
import org.jitsi.android.util.java.awt.Image;
import org.jitsi.android.util.java.awt.event.ComponentAdapter;
import org.jitsi.android.util.java.awt.event.ComponentEvent;
import org.jitsi.android.util.javax.swing.ImageIcon;
import org.jitsi.android.util.javax.swing.SwingUtilities;
import org.jitsi.impl.neomedia.AbstractRTPConnector;
import org.jitsi.impl.neomedia.MediaStreamImpl;
import org.jitsi.impl.neomedia.NeomediaServiceUtils;
import org.jitsi.impl.neomedia.RTCPFeedbackPacket;
import org.jitsi.impl.neomedia.VideoMediaStreamImpl;
import org.jitsi.impl.neomedia.codec.video.HFlip;
import org.jitsi.impl.neomedia.codec.video.SwScale;
import org.jitsi.impl.neomedia.codec.video.h264.DePacketizer;
import org.jitsi.impl.neomedia.codec.video.h264.JNIDecoder;
import org.jitsi.impl.neomedia.codec.video.h264.JNIEncoder;
import org.jitsi.impl.neomedia.control.ImgStreamingControl;
import org.jitsi.impl.neomedia.format.MediaFormatImpl;
import org.jitsi.impl.neomedia.transform.ControlTransformInputStream;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.service.neomedia.control.KeyFrameControl;
import org.jitsi.service.neomedia.control.KeyFrameControl.KeyFrameRequester;
import org.jitsi.service.neomedia.control.KeyFrameControlAdapter;
import org.jitsi.service.neomedia.event.RTCPFeedbackCreateListener;
import org.jitsi.service.neomedia.event.RTCPFeedbackEvent;
import org.jitsi.service.neomedia.event.RTCPFeedbackListener;
import org.jitsi.service.neomedia.format.MediaFormat;
import org.jitsi.service.neomedia.format.VideoMediaFormat;
import org.jitsi.service.resources.ResourceManagementService;
import org.jitsi.util.Logger;
import org.jitsi.util.OSUtils;
import org.jitsi.util.event.SizeChangeVideoEvent;
import org.jitsi.util.event.VideoEvent;
import org.jitsi.util.event.VideoListener;
import org.jitsi.util.event.VideoNotifierSupport;
import org.jitsi.util.swing.VideoLayout;

public class VideoMediaDeviceSession extends MediaDeviceSession implements RTCPFeedbackCreateListener {
    private static final String DESKTOP_STREAMING_ICON = "impl.media.DESKTOP_STREAMING_ICON";
    private static final Logger logger = Logger.getLogger(VideoMediaDeviceSession.class);
    private RTCPFeedbackListener encoder = null;
    private KeyFrameControl keyFrameControl;
    private KeyFrameRequester keyFrameRequester;
    private Player localPlayer;
    private final Object localPlayerSyncRoot = new Object();
    /* access modifiers changed from: private */
    public long localSSRC = -1;
    private Dimension outputSize;
    private SwScale playerScaler;
    /* access modifiers changed from: private */
    public long remoteSSRC = -1;
    private List<RTCPFeedbackCreateListener> rtcpFeedbackCreateListeners = new LinkedList();
    /* access modifiers changed from: private */
    public AbstractRTPConnector rtpConnector;
    /* access modifiers changed from: private */
    public boolean usePLI = false;
    private final VideoNotifierSupport videoNotifierSupport = new VideoNotifierSupport(this, false);

    private class PlayerScaler extends SwScale {
        private Dimension lastSize;
        private final Player player;

        public PlayerScaler(Player player) {
            super(true);
            this.player = player;
        }

        public int process(Buffer input, Buffer output) {
            int result = super.process(input, output);
            if (result == 0) {
                Format inputFormat = getInputFormat();
                if (inputFormat != null) {
                    Dimension size = ((VideoFormat) inputFormat).getSize();
                    if (size != null && size.height >= 4 && size.width >= 4 && !size.equals(this.lastSize)) {
                        this.lastSize = size;
                        VideoMediaDeviceSession.this.playerSizeChange(this.player, 2, this.lastSize.width, this.lastSize.height);
                    }
                }
            }
            return result;
        }

        public Format setInputFormat(Format inputFormat) {
            inputFormat = super.setInputFormat(inputFormat);
            if (inputFormat instanceof VideoFormat) {
                Dimension inputSize = ((VideoFormat) inputFormat).getSize();
                if (inputSize != null && inputSize.width > 0) {
                    Dimension outputSize = getOutputSize();
                    if (outputSize != null) {
                        int outputWidth = outputSize.width;
                        if (outputWidth > 0) {
                            int outputHeight = (int) (((float) (inputSize.height * outputWidth)) / ((float) inputSize.width));
                            int outputHeightDelta = outputHeight - outputSize.height;
                            if (outputHeightDelta < -1 || outputHeightDelta > 1) {
                                super.setOutputSize(new Dimension(outputWidth, outputHeight));
                            }
                        }
                    }
                }
            }
            return inputFormat;
        }
    }

    private static Component getVisualComponent(Player player) {
        Component visualComponent = null;
        if (player.getState() < Controller.Realized) {
            return visualComponent;
        }
        try {
            return player.getVisualComponent();
        } catch (NotRealizedError nre) {
            if (!logger.isDebugEnabled()) {
                return visualComponent;
            }
            logger.debug("Called Player#getVisualComponent() on unrealized player " + player, nre);
            return visualComponent;
        }
    }

    public VideoMediaDeviceSession(AbstractMediaDevice device) {
        super(device);
    }

    public void addRTCPFeedbackCreateListner(RTCPFeedbackCreateListener listener) {
        synchronized (this.rtcpFeedbackCreateListeners) {
            this.rtcpFeedbackCreateListeners.add(listener);
        }
        if (this.encoder != null) {
            listener.onRTCPFeedbackCreate(this.encoder);
        }
    }

    public void addVideoListener(VideoListener listener) {
        this.videoNotifierSupport.addVideoListener(listener);
    }

    /* access modifiers changed from: protected */
    public void checkDevice(AbstractMediaDevice device) {
        if (!MediaType.VIDEO.equals(device.getMediaType())) {
            throw new IllegalArgumentException("device");
        }
    }

    /* access modifiers changed from: private */
    public void controllerUpdateForCreateLocalVisualComponent(ControllerEvent ev, boolean hflip) {
        if (ev instanceof ConfigureCompleteEvent) {
            Processor player = (Processor) ev.getSourceController();
            TrackControl[] trackControls = player.getTrackControls();
            if (!(trackControls == null || trackControls.length == 0)) {
                TrackControl[] arr$ = trackControls;
                try {
                    if (0 < arr$.length) {
                        arr$[0].setCodecChain(hflip ? new Codec[]{new HFlip(), new SwScale()} : new Codec[]{new SwScale()});
                    }
                } catch (UnsupportedPlugInException upiex) {
                    logger.warn("Failed to add HFlip/SwScale Effect", upiex);
                }
            }
            try {
                player.setContentDescriptor(null);
            } catch (NotConfiguredError nce) {
                logger.error("Failed to set ContentDescriptor of Processor", nce);
            }
            player.realize();
        } else if (ev instanceof RealizeCompleteEvent) {
            boolean start;
            Player player2 = (Player) ev.getSourceController();
            Component visualComponent = player2.getVisualComponent();
            if (visualComponent == null) {
                start = false;
            } else {
                fireVideoEvent(1, visualComponent, 1, false);
                start = true;
            }
            if (start) {
                player2.start();
                return;
            }
            synchronized (this.localPlayerSyncRoot) {
                if (this.localPlayer == player2) {
                    this.localPlayer = null;
                }
            }
            player2.stop();
            player2.deallocate();
            player2.close();
        } else if (ev instanceof SizeChangeEvent) {
            SizeChangeEvent scev = (SizeChangeEvent) ev;
            playerSizeChange(scev.getSourceController(), 1, scev.getWidth(), scev.getHeight());
        }
    }

    /* access modifiers changed from: protected */
    public DataSource createCaptureDevice() {
        DataSource captureDevice = super.createCaptureDevice();
        if (captureDevice == null) {
            return captureDevice;
        }
        float frameRate;
        MediaLocator locator = captureDevice.getLocator();
        String protocol = locator == null ? null : locator.getProtocol();
        DeviceConfiguration deviceConfig = NeomediaServiceUtils.getMediaServiceImpl().getDeviceConfiguration();
        if (DeviceSystem.LOCATOR_PROTOCOL_IMGSTREAMING.equals(protocol)) {
            frameRate = 10.0f;
        } else {
            Dimension videoSize = deviceConfig.getVideoSize();
            if (this.outputSize != null && videoSize.height > this.outputSize.height && videoSize.width > this.outputSize.width) {
                videoSize = this.outputSize;
            }
            Dimension dim = VideoMediaStreamImpl.selectVideoSize(captureDevice, videoSize.width, videoSize.height);
            frameRate = (float) deviceConfig.getFrameRate();
            if (logger.isInfoEnabled() && dim != null) {
                logger.info("video send resolution: " + dim.width + "x" + dim.height);
            }
        }
        FrameRateControl frameRateControl = (FrameRateControl) captureDevice.getControl(FrameRateControl.class.getName());
        if (frameRateControl != null) {
            float maxSupportedFrameRate = frameRateControl.getMaxSupportedFrameRate();
            if (maxSupportedFrameRate > 0.0f && frameRate > maxSupportedFrameRate) {
                frameRate = maxSupportedFrameRate;
            }
            if (frameRate > 0.0f) {
                frameRateControl.setFrameRate(frameRate);
            }
            if (logger.isInfoEnabled()) {
                logger.info("video send FPS: " + (frameRate == -1.0f ? "default(no restriction)" : Float.valueOf(frameRate)));
            }
        }
        if (captureDevice instanceof SourceCloneable) {
            return captureDevice;
        }
        DataSource cloneableDataSource = Manager.createCloneableDataSource(captureDevice);
        if (cloneableDataSource != null) {
            return cloneableDataSource;
        }
        return captureDevice;
    }

    private Player createLocalPlayer() {
        return createLocalPlayer(getCaptureDevice());
    }

    /* access modifiers changed from: protected */
    public Player createLocalPlayer(DataSource captureDevice) {
        DataSource dataSource = captureDevice instanceof SourceCloneable ? ((SourceCloneable) captureDevice).createClone() : null;
        Processor localPlayer = null;
        if (dataSource != null) {
            Exception exception = null;
            try {
                localPlayer = Manager.createProcessor(dataSource);
            } catch (Exception ex) {
                exception = ex;
            }
            if (exception != null) {
                logger.error("Failed to connect to " + MediaStreamImpl.toString(dataSource), exception);
            } else if (localPlayer != null) {
                final boolean hflip = captureDevice.getControl(ImgStreamingControl.class.getName()) == null;
                localPlayer.addControllerListener(new ControllerListener() {
                    public void controllerUpdate(ControllerEvent ev) {
                        VideoMediaDeviceSession.this.controllerUpdateForCreateLocalVisualComponent(ev, hflip);
                    }
                });
                localPlayer.configure();
            }
        }
        return localPlayer;
    }

    /* access modifiers changed from: protected */
    public Component createLocalVisualComponent() {
        Component localVisualComponent = null;
        if (OSUtils.IS_ANDROID) {
            return null;
        }
        DataSource captureDevice = getCaptureDevice();
        if (captureDevice != null && captureDevice.getControl(ImgStreamingControl.class.getName()) != null) {
            return createLocalVisualComponentForDesktopStreaming();
        }
        synchronized (this.localPlayerSyncRoot) {
            if (this.localPlayer == null) {
                this.localPlayer = createLocalPlayer();
            }
            if (this.localPlayer != null) {
                localVisualComponent = getVisualComponent(this.localPlayer);
            }
        }
        if (localVisualComponent == null) {
            return localVisualComponent;
        }
        fireVideoEvent(1, localVisualComponent, 1, false);
        return localVisualComponent;
    }

    private Component createLocalVisualComponentForDesktopStreaming() {
        ResourceManagementService resources = LibJitsi.getResourceManagementService();
        ImageIcon icon = resources == null ? null : resources.getImage(DESKTOP_STREAMING_ICON);
        if (icon == null) {
            return null;
        }
        final Image img = icon.getImage();
        Canvas canvas = new Canvas() {
            public static final long serialVersionUID = 0;

            public void paint(Graphics g) {
                int width = getWidth();
                int height = getHeight();
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, width, height);
                int imgWidth = img.getWidth(this);
                int imgHeight = img.getHeight(this);
                if (imgWidth >= 1 && imgHeight >= 1) {
                    int dstWidth;
                    int dstHeight;
                    boolean scale = false;
                    float scaleFactor = 1.0f;
                    if (imgWidth > width) {
                        scale = true;
                        scaleFactor = ((float) width) / ((float) imgWidth);
                    }
                    if (imgHeight > height) {
                        scale = true;
                        scaleFactor = Math.min(scaleFactor, ((float) height) / ((float) imgHeight));
                    }
                    if (scale) {
                        dstWidth = Math.round(((float) imgWidth) * scaleFactor);
                        dstHeight = Math.round(((float) imgHeight) * scaleFactor);
                    } else {
                        dstWidth = imgWidth;
                        dstHeight = imgHeight;
                    }
                    int dstX = (width - dstWidth) / 2;
                    int dstY = (height - dstWidth) / 2;
                    g.drawImage(img, dstX, dstY, dstX + dstWidth, dstY + dstHeight, 0, 0, imgWidth, imgHeight, this);
                }
            }
        };
        Dimension iconSize = new Dimension(icon.getIconWidth(), icon.getIconHeight());
        canvas.setMaximumSize(iconSize);
        canvas.setPreferredSize(iconSize);
        canvas.setName(DESKTOP_STREAMING_ICON);
        fireVideoEvent(1, canvas, 1, false);
        return canvas;
    }

    /* access modifiers changed from: protected */
    public void disposeLocalPlayer(Player player) {
        Component visualComponent = null;
        try {
            visualComponent = getVisualComponent(player);
            player.stop();
            player.deallocate();
            player.close();
            synchronized (this.localPlayerSyncRoot) {
                if (this.localPlayer == player) {
                    this.localPlayer = null;
                }
            }
            if (visualComponent != null) {
                fireVideoEvent(2, visualComponent, 1, false);
            }
        } catch (Throwable th) {
            synchronized (this.localPlayerSyncRoot) {
                if (this.localPlayer == player) {
                    this.localPlayer = null;
                }
                if (visualComponent != null) {
                    fireVideoEvent(2, visualComponent, 1, false);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void disposeLocalVisualComponent(Component component) {
        if (component == null) {
            return;
        }
        if (DESKTOP_STREAMING_ICON.equals(component.getName())) {
            fireVideoEvent(2, component, 1, false);
            return;
        }
        Player localPlayer;
        synchronized (this.localPlayerSyncRoot) {
            localPlayer = this.localPlayer;
        }
        if (localPlayer != null) {
            Component localPlayerVisualComponent = getVisualComponent(localPlayer);
            if (localPlayerVisualComponent == null || localPlayerVisualComponent == component) {
                disposeLocalPlayer(localPlayer);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void disposePlayer(Player player) {
        Component visualComponent = getVisualComponent(player);
        super.disposePlayer(player);
        if (visualComponent != null) {
            fireVideoEvent(2, visualComponent, 2, false);
        }
    }

    /* access modifiers changed from: protected */
    public boolean fireVideoEvent(int type, Component visualComponent, int origin, boolean wait) {
        if (logger.isTraceEnabled()) {
            logger.trace("Firing VideoEvent with type " + VideoEvent.typeToString(type) + " and origin " + VideoEvent.originToString(origin));
        }
        return this.videoNotifierSupport.fireVideoEvent(type, visualComponent, origin, wait);
    }

    /* access modifiers changed from: protected */
    public void fireVideoEvent(VideoEvent videoEvent, boolean wait) {
        this.videoNotifierSupport.fireVideoEvent(videoEvent, wait);
    }

    private Format getCaptureDeviceFormat() {
        DataSource captureDevice = getCaptureDevice();
        if (captureDevice != null) {
            FormatControl[] formatControls = null;
            if (captureDevice instanceof CaptureDevice) {
                formatControls = ((CaptureDevice) captureDevice).getFormatControls();
            }
            if ((formatControls == null || formatControls.length == 0) && ((FormatControl) captureDevice.getControl(FormatControl.class.getName())) != null) {
                formatControls = new FormatControl[]{(FormatControl) captureDevice.getControl(FormatControl.class.getName())};
            }
            if (formatControls != null) {
                for (FormatControl formatControl : formatControls) {
                    Format format = formatControl.getFormat();
                    if (format != null) {
                        return format;
                    }
                }
            }
        }
        return null;
    }

    public Component getLocalVisualComponent() {
        Component visualComponent;
        synchronized (this.localPlayerSyncRoot) {
            visualComponent = this.localPlayer == null ? null : getVisualComponent(this.localPlayer);
        }
        return visualComponent;
    }

    public VideoFormat getReceivedVideoFormat() {
        if (this.playerScaler != null) {
            Format format = this.playerScaler.getInputFormat();
            if (format instanceof VideoFormat) {
                return (VideoFormat) format;
            }
        }
        return null;
    }

    public VideoFormat getSentVideoFormat() {
        DataSource capture = getCaptureDevice();
        if (capture instanceof PullBufferDataSource) {
            for (PullBufferStream stream : ((PullBufferDataSource) capture).getStreams()) {
                VideoFormat format = (VideoFormat) stream.getFormat();
                if (format != null) {
                    return format;
                }
            }
        }
        return null;
    }

    public Component getVisualComponent(long ssrc) {
        Player player = getPlayer(ssrc);
        return player == null ? null : getVisualComponent(player);
    }

    public List<Component> getVisualComponents() {
        List<Component> visualComponents = new LinkedList();
        if (getStartedDirection().allowsReceiving()) {
            for (Player player : getPlayers()) {
                Component visualComponent = getVisualComponent(player);
                if (visualComponent != null) {
                    visualComponents.add(visualComponent);
                }
            }
        }
        return visualComponents;
    }

    public void onRTCPFeedbackCreate(RTCPFeedbackListener rtcpFeedbackListener) {
        if (this.rtpConnector != null) {
            try {
                ((ControlTransformInputStream) this.rtpConnector.getControlInputStream()).addRTCPFeedbackListener(rtcpFeedbackListener);
            } catch (IOException ioe) {
                logger.error("Error cannot get RTCP input stream", ioe);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void playerConfigureComplete(Processor player) {
        UnsupportedPlugInException upiex;
        super.playerConfigureComplete(player);
        TrackControl[] trackControls = player.getTrackControls();
        SwScale playerScaler = null;
        if (!(trackControls == null || trackControls.length == 0 || OSUtils.IS_ANDROID)) {
            String fmjEncoding = getFormat().getJMFEncoding();
            TrackControl[] arr$ = trackControls;
            try {
                if (0 < arr$.length) {
                    TrackControl trackControl = arr$[0];
                    SwScale playerScaler2 = new PlayerScaler(player);
                    try {
                        if (Constants.H264_RTP.equalsIgnoreCase(fmjEncoding)) {
                            final DePacketizer depacketizer = new DePacketizer();
                            JNIDecoder decoder = new JNIDecoder();
                            if (this.keyFrameControl != null) {
                                depacketizer.setKeyFrameControl(this.keyFrameControl);
                                decoder.setKeyFrameControl(new KeyFrameControlAdapter() {
                                    public boolean requestKeyFrame(boolean urgent) {
                                        return depacketizer.requestKeyFrame(urgent);
                                    }
                                });
                            }
                            trackControl.setCodecChain(new Codec[]{depacketizer, decoder, playerScaler2});
                            playerScaler = playerScaler2;
                        } else {
                            trackControl.setCodecChain(new Codec[]{playerScaler2});
                            playerScaler = playerScaler2;
                        }
                    } catch (UnsupportedPlugInException e) {
                        upiex = e;
                        playerScaler = playerScaler2;
                        logger.error("Failed to add SwScale or H.264 DePacketizer to codec chain", upiex);
                        playerScaler = null;
                        this.playerScaler = playerScaler;
                    }
                }
            } catch (UnsupportedPlugInException e2) {
                upiex = e2;
                logger.error("Failed to add SwScale or H.264 DePacketizer to codec chain", upiex);
                playerScaler = null;
                this.playerScaler = playerScaler;
            }
        }
        this.playerScaler = playerScaler;
    }

    /* access modifiers changed from: protected */
    public void playerControllerUpdate(ControllerEvent ev) {
        super.playerControllerUpdate(ev);
        if (!(ev instanceof SizeChangeEvent)) {
            return;
        }
        if (this.playerScaler == null || this.playerScaler.getOutputSize() == null) {
            SizeChangeEvent scev = (SizeChangeEvent) ev;
            playerSizeChange(scev.getSourceController(), 2, scev.getWidth(), scev.getHeight());
        }
    }

    /* access modifiers changed from: protected */
    public void playerRealizeComplete(final Processor player) {
        super.playerRealizeComplete(player);
        Component visualComponent = getVisualComponent((Player) player);
        if (visualComponent != null) {
            visualComponent.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent ev) {
                    VideoMediaDeviceSession.this.playerVisualComponentResized(player, ev);
                }
            });
            fireVideoEvent(1, visualComponent, 2, false);
        }
    }

    /* access modifiers changed from: protected */
    public void playerSizeChange(Controller sourceController, int origin, int width, int height) {
        if (SwingUtilities.isEventDispatchThread()) {
            Component visualComponent = getVisualComponent((Player) sourceController);
            if (visualComponent != null) {
                try {
                    Dimension prefSize = visualComponent.getPreferredSize();
                    if (prefSize == null || prefSize.width < 1 || prefSize.height < 1 || !VideoLayout.areAspectRatiosEqual(prefSize, width, height) || prefSize.width < width || prefSize.height < height) {
                        visualComponent.setPreferredSize(new Dimension(width, height));
                    }
                    fireVideoEvent(new SizeChangeVideoEvent(this, visualComponent, origin, width, height), false);
                    return;
                } catch (Throwable th) {
                    Throwable th2 = th;
                    fireVideoEvent(new SizeChangeVideoEvent(this, visualComponent, origin, width, height), false);
                }
            } else {
                return;
            }
        }
        final Controller controller = sourceController;
        final int i = origin;
        final int i2 = width;
        final int i3 = height;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                VideoMediaDeviceSession.this.playerSizeChange(controller, i, i2, i3);
            }
        });
    }

    /* access modifiers changed from: private */
    public void playerVisualComponentResized(Processor player, ComponentEvent ev) {
        if (this.playerScaler != null) {
            Component visualComponent = ev.getComponent();
            if (!visualComponent.isDisplayable()) {
                Dimension outputSize = visualComponent.getSize();
                float outputWidth = (float) outputSize.width;
                float outputHeight = (float) outputSize.height;
                if (outputWidth >= 4.0f && outputHeight >= 4.0f) {
                    Format inputFormat = this.playerScaler.getInputFormat();
                    if (inputFormat != null) {
                        Dimension inputSize = ((VideoFormat) inputFormat).getSize();
                        if (inputSize != null) {
                            int inputWidth = inputSize.width;
                            int inputHeight = inputSize.height;
                            if (inputWidth >= 1 && inputHeight >= 1) {
                                float widthRatio;
                                float heightRatio;
                                outputHeight = (((float) inputHeight) * outputWidth) / ((float) inputWidth);
                                boolean scale = false;
                                if (Math.abs(outputWidth - ((float) inputWidth)) < 1.0f) {
                                    scale = true;
                                    widthRatio = outputWidth / ((float) inputWidth);
                                } else {
                                    widthRatio = 1.0f;
                                }
                                if (Math.abs(outputHeight - ((float) inputHeight)) < 1.0f) {
                                    scale = true;
                                    heightRatio = outputHeight / ((float) inputHeight);
                                } else {
                                    heightRatio = 1.0f;
                                }
                                if (scale) {
                                    float scaleFactor = Math.min(widthRatio, heightRatio);
                                    outputWidth = ((float) inputWidth) * scaleFactor;
                                    outputHeight = ((float) inputHeight) * scaleFactor;
                                }
                                outputSize.width = (int) outputWidth;
                                outputSize.height = (int) outputHeight;
                                Dimension playerScalerOutputSize = this.playerScaler.getOutputSize();
                                if (playerScalerOutputSize == null) {
                                    this.playerScaler.setOutputSize(outputSize);
                                    return;
                                }
                                int outputWidthDelta = outputSize.width - playerScalerOutputSize.width;
                                int outputHeightDelta = outputSize.height - playerScalerOutputSize.height;
                                if (outputWidthDelta < -1 || outputWidthDelta > 1 || outputHeightDelta < -1 || outputHeightDelta > 1) {
                                    this.playerScaler.setOutputSize(outputSize);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void removeRTCPFeedbackCreateListner(RTCPFeedbackCreateListener listener) {
        synchronized (this.rtcpFeedbackCreateListeners) {
            this.rtcpFeedbackCreateListeners.remove(listener);
        }
    }

    public void removeVideoListener(VideoListener listener) {
        this.videoNotifierSupport.removeVideoListener(listener);
    }

    public void setConnector(AbstractRTPConnector rtpConnector) {
        this.rtpConnector = rtpConnector;
    }

    public void setFormat(MediaFormat format) {
        if ((format instanceof VideoMediaFormat) && ((VideoMediaFormat) format).getFrameRate() != -1.0f) {
            FrameRateControl frameRateControl = (FrameRateControl) getCaptureDevice().getControl(FrameRateControl.class.getName());
            if (frameRateControl != null) {
                float frameRate = ((VideoMediaFormat) format).getFrameRate();
                float maxSupportedFrameRate = frameRateControl.getMaxSupportedFrameRate();
                if (maxSupportedFrameRate > 0.0f && frameRate > maxSupportedFrameRate) {
                    frameRate = maxSupportedFrameRate;
                }
                if (frameRate > 0.0f) {
                    frameRateControl.setFrameRate(frameRate);
                    if (logger.isInfoEnabled()) {
                        logger.info("video send FPS: " + frameRate);
                    }
                }
            }
        }
        super.setFormat(format);
    }

    public void setKeyFrameControl(KeyFrameControl keyFrameControl) {
        if (this.keyFrameControl != keyFrameControl) {
            if (!(this.keyFrameControl == null || this.keyFrameRequester == null)) {
                this.keyFrameControl.removeKeyFrameRequester(this.keyFrameRequester);
            }
            this.keyFrameControl = keyFrameControl;
            if (this.keyFrameControl != null && this.keyFrameRequester != null) {
                this.keyFrameControl.addKeyFrameRequester(-1, this.keyFrameRequester);
            }
        }
    }

    public void setLocalSSRC(long localSSRC) {
        this.localSSRC = localSSRC;
    }

    public void setOutputSize(Dimension size) {
        boolean equal = size == null ? this.outputSize == null : size.equals(this.outputSize);
        if (!equal) {
            this.outputSize = size;
            this.outputSizeChanged = true;
        }
    }

    /* access modifiers changed from: protected */
    public void setProcessorFormat(Processor processor, MediaFormatImpl<? extends Format> mediaFormat) {
        Format format = mediaFormat.getFormat();
        if ("h263-1998/rtp".equalsIgnoreCase(format.getEncoding()) && this.outputSize == null) {
            this.outputSize = new Dimension(176, 144);
        }
        if (this.outputSize == null || this.outputSize.width <= 0 || this.outputSize.height <= 0) {
            this.outputSize = null;
        } else {
            Dimension videoFormatSize;
            Dimension deviceSize = ((VideoFormat) getCaptureDeviceFormat()).getSize();
            if (deviceSize == null || (deviceSize.width <= this.outputSize.width && deviceSize.height <= this.outputSize.height)) {
                videoFormatSize = deviceSize;
                this.outputSize = null;
            } else {
                videoFormatSize = this.outputSize;
            }
            VideoFormat videoFormat = (VideoFormat) format;
            VideoFormat videoFormat2 = new VideoFormat(videoFormat.getEncoding(), videoFormatSize, videoFormat.getMaxDataLength(), videoFormat.getDataType(), videoFormat.getFrameRate());
        }
        super.setProcessorFormat(processor, mediaFormat);
    }

    /* access modifiers changed from: protected */
    public Format setProcessorFormat(TrackControl trackControl, MediaFormatImpl<? extends Format> mediaFormat, Format format) {
        int codecCount;
        JNIEncoder encoder = null;
        SwScale scaler = null;
        int codecCount2 = 0;
        if (!OSUtils.IS_ANDROID && Constants.H264_RTP.equalsIgnoreCase(format.getEncoding())) {
            String packetizationMode;
            encoder = new JNIEncoder();
            Map<String, String> formatParameters = mediaFormat.getFormatParameters();
            if (formatParameters == null) {
                packetizationMode = null;
            } else {
                packetizationMode = (String) formatParameters.get(JNIEncoder.PACKETIZATION_MODE_FMTP);
            }
            encoder.setPacketizationMode(packetizationMode);
            encoder.setAdditionalCodecSettings(mediaFormat.getAdditionalCodecSettings());
            this.encoder = encoder;
            onRTCPFeedbackCreate(encoder);
            synchronized (this.rtcpFeedbackCreateListeners) {
                for (RTCPFeedbackCreateListener l : this.rtcpFeedbackCreateListeners) {
                    l.onRTCPFeedbackCreate(encoder);
                }
            }
            if (this.keyFrameControl != null) {
                encoder.setKeyFrameControl(this.keyFrameControl);
            }
            codecCount2 = 0 + 1;
        }
        if (this.outputSize != null) {
            scaler = new SwScale(false, true);
            scaler.setOutputSize(this.outputSize);
            codecCount2++;
        }
        Codec[] codecs = new Codec[codecCount2];
        if (scaler != null) {
            codecCount = 0 + 1;
            codecs[0] = scaler;
        } else {
            codecCount = 0;
        }
        if (encoder != null) {
            codecCount2 = codecCount + 1;
            codecs[codecCount] = encoder;
        } else {
            codecCount2 = codecCount;
        }
        if (codecCount2 != 0) {
            try {
                trackControl.setCodecChain(codecs);
            } catch (UnsupportedPlugInException upiex) {
                logger.error("Failed to add SwScale/JNIEncoder to codec chain", upiex);
            }
        }
        return super.setProcessorFormat(trackControl, mediaFormat, format);
    }

    public void setRemoteSSRC(long remoteSSRC) {
        this.remoteSSRC = remoteSSRC;
    }

    public void setRtcpFeedbackPLI(boolean usePLI) {
        if (this.usePLI != usePLI) {
            this.usePLI = usePLI;
            if (this.usePLI) {
                if (this.keyFrameRequester == null) {
                    this.keyFrameRequester = new KeyFrameRequester() {
                        public boolean requestKeyFrame() {
                            if (!VideoMediaDeviceSession.this.usePLI) {
                                return false;
                            }
                            try {
                                new RTCPFeedbackPacket(1, RTCPFeedbackEvent.PT_PS, VideoMediaDeviceSession.this.localSSRC, VideoMediaDeviceSession.this.remoteSSRC).writeTo(VideoMediaDeviceSession.this.rtpConnector.getControlOutputStream());
                                return true;
                            } catch (IOException e) {
                                return false;
                            }
                        }
                    };
                }
                if (this.keyFrameControl != null) {
                    this.keyFrameControl.addKeyFrameRequester(-1, this.keyFrameRequester);
                }
            } else if (this.keyFrameRequester != null) {
                if (this.keyFrameControl != null) {
                    this.keyFrameControl.removeKeyFrameRequester(this.keyFrameRequester);
                }
                this.keyFrameRequester = null;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void startedDirectionChanged(MediaDirection oldValue, MediaDirection newValue) {
        super.startedDirectionChanged(oldValue, newValue);
        try {
            Player localPlayer;
            synchronized (this.localPlayerSyncRoot) {
                localPlayer = getLocalPlayer();
            }
            if (newValue.allowsSending()) {
                if (localPlayer == null) {
                    createLocalVisualComponent();
                }
            } else if (localPlayer != null) {
                disposeLocalPlayer(localPlayer);
            }
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            } else {
                logger.error("Failed to start/stop the preview of the local video", t);
            }
        }
        for (Player player : getPlayers()) {
            int state = player.getState();
            if (state >= Controller.Realized) {
                Component visualComponent;
                if (newValue.allowsReceiving()) {
                    if (state != Controller.Started) {
                        player.start();
                        visualComponent = getVisualComponent(player);
                        if (visualComponent != null) {
                            fireVideoEvent(1, visualComponent, 2, false);
                        }
                    }
                } else if (state > Processor.Configured) {
                    visualComponent = getVisualComponent(player);
                    player.stop();
                    if (visualComponent != null) {
                        fireVideoEvent(2, visualComponent, 2, false);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public Player getLocalPlayer() {
        Player player;
        synchronized (this.localPlayerSyncRoot) {
            player = this.localPlayer;
        }
        return player;
    }
}
