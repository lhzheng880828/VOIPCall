package org.jitsi.impl.neomedia;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.media.Format;
import javax.media.control.BufferControl;
import javax.media.control.FormatControl;
import javax.media.format.VideoFormat;
import javax.media.protocol.DataSource;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Point;
import org.jitsi.android.util.java.awt.Rectangle;
import org.jitsi.impl.neomedia.control.ImgStreamingControl;
import org.jitsi.impl.neomedia.device.DeviceConfiguration;
import org.jitsi.impl.neomedia.device.DeviceSystem;
import org.jitsi.impl.neomedia.device.MediaDeviceImpl;
import org.jitsi.impl.neomedia.device.MediaDeviceSession;
import org.jitsi.impl.neomedia.device.ScreenDeviceImpl;
import org.jitsi.impl.neomedia.device.VideoMediaDeviceSession;
import org.jitsi.impl.neomedia.portaudio.Pa;
import org.jitsi.service.neomedia.QualityControl;
import org.jitsi.service.neomedia.QualityPreset;
import org.jitsi.service.neomedia.SrtpControl;
import org.jitsi.service.neomedia.StreamConnector;
import org.jitsi.service.neomedia.VideoMediaStream;
import org.jitsi.service.neomedia.control.KeyFrameControl;
import org.jitsi.service.neomedia.control.KeyFrameControlAdapter;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.device.ScreenDevice;
import org.jitsi.service.neomedia.format.MediaFormat;
import org.jitsi.util.Logger;
import org.jitsi.util.OSUtils;
import org.jitsi.util.event.VideoEvent;
import org.jitsi.util.event.VideoListener;
import org.jitsi.util.event.VideoNotifierSupport;

public class VideoMediaStreamImpl extends MediaStreamImpl implements VideoMediaStream {
    private static final boolean USE_PLI = true;
    private static final Logger logger = Logger.getLogger(VideoMediaStreamImpl.class);
    private VideoListener deviceSessionVideoListener;
    private KeyFrameControl keyFrameControl;
    private Dimension outputSize;
    private final QualityControlImpl qualityControl = new QualityControlImpl();
    private final VideoNotifierSupport videoNotifierSupport = new VideoNotifierSupport(this, true);

    /* renamed from: org.jitsi.impl.neomedia.VideoMediaStreamImpl$1FormatInfo */
    class AnonymousClass1FormatInfo {
        public final double difference;
        public final Dimension dimension;
        public final VideoFormat format;
        final /* synthetic */ int val$preferredHeight;
        final /* synthetic */ int val$preferredWidth;

        public AnonymousClass1FormatInfo(Dimension size, int i, int i2) {
            this.val$preferredWidth = i;
            this.val$preferredHeight = i2;
            this.format = null;
            this.dimension = size;
            this.difference = getDifference(this.dimension);
        }

        public AnonymousClass1FormatInfo(VideoFormat format, int i, int i2) {
            this.val$preferredWidth = i;
            this.val$preferredHeight = i2;
            this.format = format;
            this.dimension = format.getSize();
            this.difference = getDifference(this.dimension);
        }

        private double getDifference(Dimension size) {
            double xScale;
            double yScale;
            int height = 0;
            int width = size == null ? 0 : size.width;
            if (width == 0) {
                xScale = Double.POSITIVE_INFINITY;
            } else if (width == this.val$preferredWidth) {
                xScale = 1.0d;
            } else {
                xScale = ((double) this.val$preferredWidth) / ((double) width);
            }
            if (size != null) {
                height = size.height;
            }
            if (height == 0) {
                yScale = Double.POSITIVE_INFINITY;
            } else if (height == this.val$preferredHeight) {
                yScale = 1.0d;
            } else {
                yScale = ((double) this.val$preferredHeight) / ((double) height);
            }
            return Math.abs(1.0d - Math.min(xScale, yScale));
        }
    }

    public static Dimension[] parseSendRecvResolution(String imgattr) {
        int[] val;
        String token;
        Matcher m;
        int i;
        Dimension[] res = new Dimension[2];
        Pattern pSendSingle = Pattern.compile("send \\[x=[0-9]+,y=[0-9]+\\]");
        Pattern pRecvSingle = Pattern.compile("recv \\[x=[0-9]+,y=[0-9]+\\]");
        Pattern pSendRange = Pattern.compile("send \\[x=\\[[0-9]+-[0-9]+\\],y=\\[[0-9]+-[0-9]+\\]\\]");
        Pattern pRecvRange = Pattern.compile("recv \\[x=\\[[0-9]+-[0-9]+\\],y=\\[[0-9]+-[0-9]+\\]\\]");
        Pattern pNumeric = Pattern.compile("[0-9]+");
        Matcher mSingle = pSendSingle.matcher(imgattr);
        Matcher mRange = pSendRange.matcher(imgattr);
        if (mSingle.find()) {
            val = new int[2];
            token = imgattr.substring(mSingle.start(), mSingle.end());
            m = pNumeric.matcher(token);
            while (m.find() && 0 < 2) {
                val[0] = Integer.parseInt(token.substring(m.start(), m.end()));
            }
            res[0] = new Dimension(val[0], val[1]);
        } else if (mRange.find()) {
            val = new int[4];
            i = 0;
            token = imgattr.substring(mRange.start(), mRange.end());
            m = pNumeric.matcher(token);
            while (m.find() && i < 4) {
                val[i] = Integer.parseInt(token.substring(m.start(), m.end()));
                i++;
            }
            res[0] = new Dimension(val[1], val[3]);
        }
        mSingle = pRecvSingle.matcher(imgattr);
        mRange = pRecvRange.matcher(imgattr);
        if (mSingle.find()) {
            val = new int[2];
            token = imgattr.substring(mSingle.start(), mSingle.end());
            m = pNumeric.matcher(token);
            while (m.find() && 0 < 2) {
                val[0] = Integer.parseInt(token.substring(m.start(), m.end()));
            }
            res[1] = new Dimension(val[0], val[1]);
        } else if (mRange.find()) {
            val = new int[4];
            i = 0;
            token = imgattr.substring(mRange.start(), mRange.end());
            m = pNumeric.matcher(token);
            while (m.find() && i < 4) {
                val[i] = Integer.parseInt(token.substring(m.start(), m.end()));
                i++;
            }
            res[1] = new Dimension(val[1], val[3]);
        }
        return res;
    }

    public static Dimension selectVideoSize(DataSource videoDS, int preferredWidth, int preferredHeight) {
        if (videoDS == null) {
            return null;
        }
        FormatControl formatControl = (FormatControl) videoDS.getControl(FormatControl.class.getName());
        if (formatControl == null) {
            return null;
        }
        Format[] formats = formatControl.getSupportedFormats();
        int count = formats.length;
        if (count < 1) {
            return null;
        }
        Format selectedFormat = null;
        if (count == 1) {
            selectedFormat = formats[0];
        } else {
            int i;
            AnonymousClass1FormatInfo anonymousClass1FormatInfo;
            AnonymousClass1FormatInfo[] infos = new AnonymousClass1FormatInfo[count];
            for (i = 0; i < count; i++) {
                anonymousClass1FormatInfo = new AnonymousClass1FormatInfo((VideoFormat) formats[i], preferredWidth, preferredHeight);
                infos[i] = anonymousClass1FormatInfo;
                if (anonymousClass1FormatInfo.difference == Pa.LATENCY_UNSPECIFIED) {
                    selectedFormat = anonymousClass1FormatInfo.format;
                    break;
                }
            }
            if (selectedFormat == null) {
                Arrays.sort(infos, new Comparator<AnonymousClass1FormatInfo>() {
                    public int compare(AnonymousClass1FormatInfo info0, AnonymousClass1FormatInfo info1) {
                        return Double.compare(info0.difference, info1.difference);
                    }
                });
                selectedFormat = infos[0].format;
            }
            if (selectedFormat != null && selectedFormat.getSize() == null) {
                VideoFormat currentFormat = (VideoFormat) formatControl.getFormat();
                Dimension currentSize = null;
                int width = preferredWidth;
                int height = preferredHeight;
                if (currentFormat != null) {
                    currentSize = currentFormat.getSize();
                }
                AnonymousClass1FormatInfo[] supportedInfos = new AnonymousClass1FormatInfo[DeviceConfiguration.SUPPORTED_RESOLUTIONS.length];
                for (i = 0; i < supportedInfos.length; i++) {
                    supportedInfos[i] = new AnonymousClass1FormatInfo(DeviceConfiguration.SUPPORTED_RESOLUTIONS[i], preferredWidth, preferredHeight);
                }
                Arrays.sort(infos, new Comparator<AnonymousClass1FormatInfo>() {
                    public int compare(AnonymousClass1FormatInfo info0, AnonymousClass1FormatInfo info1) {
                        return Double.compare(info0.difference, info1.difference);
                    }
                });
                anonymousClass1FormatInfo = new AnonymousClass1FormatInfo(new Dimension(preferredWidth, preferredHeight), preferredWidth, preferredHeight);
                Dimension closestAspect = null;
                for (AnonymousClass1FormatInfo supported : supportedInfos) {
                    if (anonymousClass1FormatInfo.difference <= supported.difference) {
                        if (closestAspect == null) {
                            closestAspect = supported.dimension;
                        }
                        if (supported.dimension.height <= preferredHeight && supported.dimension.width <= preferredWidth) {
                            currentSize = supported.dimension;
                        }
                    }
                }
                if (currentSize == null) {
                    currentSize = closestAspect;
                }
                if (currentSize.width > 0 && currentSize.height > 0) {
                    width = currentSize.width;
                    height = currentSize.height;
                }
                VideoFormat selectedFormat2 = (VideoFormat) new VideoFormat(null, new Dimension(width, height), -1, null, -1.0f).intersects(selectedFormat);
            }
        }
        Format setFormat = formatControl.setFormat(selectedFormat);
        return setFormat instanceof VideoFormat ? ((VideoFormat) setFormat).getSize() : null;
    }

    public VideoMediaStreamImpl(StreamConnector connector, MediaDevice device, SrtpControl srtpControl) {
        super(connector, device, srtpControl);
    }

    /* access modifiers changed from: protected */
    public void addRemoteSourceID(long ssrc) {
        super.addRemoteSourceID(ssrc);
        MediaDeviceSession deviceSession = getDeviceSession();
        if (deviceSession instanceof VideoMediaDeviceSession) {
            ((VideoMediaDeviceSession) deviceSession).setRemoteSSRC(ssrc);
        }
    }

    public void addVideoListener(VideoListener listener) {
        this.videoNotifierSupport.addVideoListener(listener);
    }

    /* access modifiers changed from: protected */
    public void configureDataOutputStream(RTPConnectorOutputStream dataOutputStream) {
        super.configureDataOutputStream(dataOutputStream);
        if (!OSUtils.IS_ANDROID) {
            int maxBandwidth = NeomediaServiceUtils.getMediaServiceImpl().getDeviceConfiguration().getVideoRTPPacingThreshold();
            if (maxBandwidth <= 1000) {
                dataOutputStream.setMaxPacketsPerMillis(1, (long) (1000 / maxBandwidth));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void configureRTPManagerBufferControl(StreamRTPManager rtpManager, BufferControl bufferControl) {
        super.configureRTPManagerBufferControl(rtpManager, bufferControl);
        bufferControl.setBufferLength(-2);
    }

    /* access modifiers changed from: protected */
    public void deviceSessionChanged(MediaDeviceSession oldValue, MediaDeviceSession newValue) {
        super.deviceSessionChanged(oldValue, newValue);
        if (oldValue instanceof VideoMediaDeviceSession) {
            VideoMediaDeviceSession oldVideoMediaDeviceSession = (VideoMediaDeviceSession) oldValue;
            if (this.deviceSessionVideoListener != null) {
                oldVideoMediaDeviceSession.removeVideoListener(this.deviceSessionVideoListener);
            }
            oldVideoMediaDeviceSession.setKeyFrameControl(null);
        }
        if (newValue instanceof VideoMediaDeviceSession) {
            VideoMediaDeviceSession newVideoMediaDeviceSession = (VideoMediaDeviceSession) newValue;
            if (this.deviceSessionVideoListener == null) {
                this.deviceSessionVideoListener = new VideoListener() {
                    public void videoAdded(VideoEvent e) {
                        if (VideoMediaStreamImpl.this.fireVideoEvent(e.getType(), e.getVisualComponent(), e.getOrigin(), true)) {
                            e.consume();
                        }
                    }

                    public void videoRemoved(VideoEvent e) {
                        videoAdded(e);
                    }

                    public void videoUpdate(VideoEvent e) {
                        VideoMediaStreamImpl.this.fireVideoEvent(e, true);
                    }
                };
            }
            newVideoMediaDeviceSession.addVideoListener(this.deviceSessionVideoListener);
            newVideoMediaDeviceSession.setOutputSize(this.outputSize);
            AbstractRTPConnector rtpConnector = getRTPConnector();
            if (rtpConnector != null) {
                newVideoMediaDeviceSession.setConnector(rtpConnector);
            }
            newVideoMediaDeviceSession.setRtcpFeedbackPLI(true);
            newVideoMediaDeviceSession.setKeyFrameControl(getKeyFrameControl());
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
    public void fireVideoEvent(VideoEvent event, boolean wait) {
        this.videoNotifierSupport.fireVideoEvent(event, wait);
    }

    public KeyFrameControl getKeyFrameControl() {
        if (this.keyFrameControl == null) {
            this.keyFrameControl = new KeyFrameControlAdapter();
        }
        return this.keyFrameControl;
    }

    public Component getLocalVisualComponent() {
        MediaDeviceSession deviceSession = getDeviceSession();
        return deviceSession instanceof VideoMediaDeviceSession ? ((VideoMediaDeviceSession) deviceSession).getLocalVisualComponent() : null;
    }

    /* access modifiers changed from: protected */
    public int getPriority() {
        return 5;
    }

    public QualityControl getQualityControl() {
        return this.qualityControl;
    }

    @Deprecated
    public Component getVisualComponent() {
        List<Component> visualComponents = getVisualComponents();
        return visualComponents.isEmpty() ? null : (Component) visualComponents.get(0);
    }

    public Component getVisualComponent(long ssrc) {
        MediaDeviceSession deviceSession = getDeviceSession();
        return deviceSession instanceof VideoMediaDeviceSession ? ((VideoMediaDeviceSession) deviceSession).getVisualComponent(ssrc) : null;
    }

    public List<Component> getVisualComponents() {
        MediaDeviceSession deviceSession = getDeviceSession();
        if (deviceSession instanceof VideoMediaDeviceSession) {
            return ((VideoMediaDeviceSession) deviceSession).getVisualComponents();
        }
        return Collections.emptyList();
    }

    /* access modifiers changed from: protected */
    public void handleAttributes(MediaFormat format, Map<String, String> attrs) {
        if (attrs != null) {
            String width = null;
            String height = null;
            for (Entry<String, String> attr : attrs.entrySet()) {
                String key = (String) attr.getKey();
                String value = (String) attr.getValue();
                if (!key.equals("rtcp-fb")) {
                    Dimension dim;
                    if (key.equals("imageattr")) {
                        if ((!attrs.containsKey("width") && !attrs.containsKey("height")) || this.outputSize == null) {
                            Dimension[] res = parseSendRecvResolution(value);
                            if (res != null) {
                                setOutputSize(res[1]);
                                this.qualityControl.setRemoteSendMaxPreset(new QualityPreset(res[0]));
                                this.qualityControl.setRemoteReceiveResolution(this.outputSize);
                                ((VideoMediaDeviceSession) getDeviceSession()).setOutputSize(this.outputSize);
                            }
                        }
                    } else if (key.equals("CIF")) {
                        dim = new Dimension(352, 288);
                        if (this.outputSize == null || (this.outputSize.width < dim.width && this.outputSize.height < dim.height)) {
                            setOutputSize(dim);
                            ((VideoMediaDeviceSession) getDeviceSession()).setOutputSize(this.outputSize);
                        }
                    } else if (key.equals("QCIF")) {
                        dim = new Dimension(176, 144);
                        if (this.outputSize == null || (this.outputSize.width < dim.width && this.outputSize.height < dim.height)) {
                            setOutputSize(dim);
                            ((VideoMediaDeviceSession) getDeviceSession()).setOutputSize(this.outputSize);
                        }
                    } else if (key.equals("VGA")) {
                        dim = new Dimension(DeviceConfiguration.DEFAULT_VIDEO_WIDTH, DeviceConfiguration.DEFAULT_VIDEO_HEIGHT);
                        if (this.outputSize == null || (this.outputSize.width < dim.width && this.outputSize.height < dim.height)) {
                            setOutputSize(dim);
                            ((VideoMediaDeviceSession) getDeviceSession()).setOutputSize(this.outputSize);
                        }
                    } else if (key.equals("CUSTOM")) {
                        String[] args = value.split(",");
                        if (args.length >= 3) {
                            try {
                                dim = new Dimension(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                                if (this.outputSize == null || (this.outputSize.width < dim.width && this.outputSize.height < dim.height)) {
                                    setOutputSize(dim);
                                    ((VideoMediaDeviceSession) getDeviceSession()).setOutputSize(this.outputSize);
                                }
                            } catch (Exception e) {
                            }
                        }
                    } else if (key.equals("width")) {
                        width = value;
                        if (height != null) {
                            setOutputSize(new Dimension(Integer.parseInt(width), Integer.parseInt(height)));
                            ((VideoMediaDeviceSession) getDeviceSession()).setOutputSize(this.outputSize);
                        }
                    } else if (key.equals("height")) {
                        height = value;
                        if (width != null) {
                            setOutputSize(new Dimension(Integer.parseInt(width), Integer.parseInt(height)));
                            ((VideoMediaDeviceSession) getDeviceSession()).setOutputSize(this.outputSize);
                        }
                    }
                }
            }
        }
    }

    public void movePartialDesktopStreaming(int x, int y) {
        if (DeviceSystem.LOCATOR_PROTOCOL_IMGSTREAMING.equals(((MediaDeviceImpl) getDevice()).getCaptureDeviceInfoLocatorProtocol())) {
            Object imgStreamingControl = getDeviceSession().getCaptureDevice().getControl(ImgStreamingControl.class.getName());
            if (imgStreamingControl != null) {
                int i;
                MediaServiceImpl mediaServiceImpl = NeomediaServiceUtils.getMediaServiceImpl();
                if (x < 0) {
                    i = 0;
                } else {
                    i = x;
                }
                ScreenDevice screen = mediaServiceImpl.getScreenForPoint(new Point(i, y < 0 ? 0 : y));
                if (screen != null) {
                    Rectangle bounds = ((ScreenDeviceImpl) screen).getBounds();
                    ((ImgStreamingControl) imgStreamingControl).setOrigin(0, screen.getIndex(), x - bounds.x, y - bounds.y);
                }
            }
        }
    }

    public void removeVideoListener(VideoListener listener) {
        this.videoNotifierSupport.removeVideoListener(listener);
    }

    /* access modifiers changed from: protected */
    public void rtpConnectorChanged(AbstractRTPConnector oldValue, AbstractRTPConnector newValue) {
        super.rtpConnectorChanged(oldValue, newValue);
        if (newValue != null) {
            MediaDeviceSession deviceSession = getDeviceSession();
            if (deviceSession instanceof VideoMediaDeviceSession) {
                ((VideoMediaDeviceSession) deviceSession).setConnector(newValue);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setLocalSourceID(long localSourceID) {
        super.setLocalSourceID(localSourceID);
        MediaDeviceSession deviceSession = getDeviceSession();
        if (deviceSession instanceof VideoMediaDeviceSession) {
            ((VideoMediaDeviceSession) deviceSession).setLocalSSRC(localSourceID);
        }
    }

    private void setOutputSize(Dimension outputSize) {
        this.outputSize = outputSize;
    }

    public void updateQualityControl(Map<String, String> advancedParams) {
        for (Entry<String, String> entry : advancedParams.entrySet()) {
            if (((String) entry.getKey()).equals("imageattr")) {
                Dimension[] res = parseSendRecvResolution((String) entry.getValue());
                if (res != null) {
                    this.qualityControl.setRemoteSendMaxPreset(new QualityPreset(res[0]));
                    this.qualityControl.setRemoteReceiveResolution(res[1]);
                    setOutputSize(res[1]);
                    ((VideoMediaDeviceSession) getDeviceSession()).setOutputSize(this.outputSize);
                }
            }
        }
    }
}
