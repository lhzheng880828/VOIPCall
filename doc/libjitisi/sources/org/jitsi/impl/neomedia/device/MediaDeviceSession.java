package org.jitsi.impl.neomedia.device;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.media.ConfigureCompleteEvent;
import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Controls;
import javax.media.Format;
import javax.media.Manager;
import javax.media.NoProcessorException;
import javax.media.NotConfiguredError;
import javax.media.NotRealizedError;
import javax.media.Player;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.Renderer;
import javax.media.UnsupportedPlugInException;
import javax.media.control.FormatControl;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.rtp.ReceiveStream;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.impl.neomedia.MediaStreamImpl;
import org.jitsi.impl.neomedia.ProcessorUtility;
import org.jitsi.impl.neomedia.control.AbstractControls;
import org.jitsi.impl.neomedia.format.MediaFormatImpl;
import org.jitsi.impl.neomedia.format.ParameterizedVideoFormat;
import org.jitsi.impl.neomedia.protocol.InbandDTMFDataSource;
import org.jitsi.impl.neomedia.protocol.MuteDataSource;
import org.jitsi.impl.neomedia.protocol.RewritablePullBufferDataSource;
import org.jitsi.impl.neomedia.protocol.RewritablePushBufferDataSource;
import org.jitsi.impl.neomedia.protocol.TranscodingDataSource;
import org.jitsi.service.neomedia.DTMFInbandTone;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.codec.Constants;
import org.jitsi.service.neomedia.control.FormatParametersAwareCodec;
import org.jitsi.service.neomedia.format.MediaFormat;
import org.jitsi.util.Logger;
import org.jitsi.util.OSUtils;
import org.jitsi.util.event.PropertyChangeNotifier;

public class MediaDeviceSession extends PropertyChangeNotifier {
    public static final String OUTPUT_DATA_SOURCE = "OUTPUT_DATA_SOURCE";
    public static final String SSRC_LIST = "SSRC_LIST";
    private static final Logger logger = Logger.getLogger(MediaDeviceSession.class);
    private DataSource captureDevice;
    private boolean captureDeviceIsConnected;
    private ContentDescriptor contentDescriptor;
    private final AbstractMediaDevice device;
    private boolean disposePlayerOnClose = true;
    private MediaFormatImpl<? extends Format> format;
    private boolean mute = false;
    protected boolean outputSizeChanged = false;
    private final List<Playback> playbacks = new LinkedList();
    private ControllerListener playerControllerListener;
    private Processor processor;
    private ControllerListener processorControllerListener;
    private boolean processorIsPrematurelyClosed;
    private long[] ssrcList = null;
    private MediaDirection startedDirection = MediaDirection.INACTIVE;

    private static class Playback {
        public DataSource dataSource;
        public Player player;
        public ReceiveStream receiveStream;

        public Playback(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public Playback(ReceiveStream receiveStream) {
            this.receiveStream = receiveStream;
        }
    }

    protected MediaDeviceSession(AbstractMediaDevice device) {
        checkDevice(device);
        this.device = device;
    }

    public void setDisposePlayerOnClose(boolean disposePlayerOnClose) {
        this.disposePlayerOnClose = disposePlayerOnClose;
    }

    /* access modifiers changed from: protected */
    public void addSSRC(long ssrc) {
        if (this.ssrcList == null) {
            setSsrcList(new long[]{ssrc});
            return;
        }
        int i = 0;
        while (i < this.ssrcList.length) {
            if (ssrc != this.ssrcList[i]) {
                i++;
            } else {
                return;
            }
        }
        long[] newSsrcList = new long[(this.ssrcList.length + 1)];
        System.arraycopy(this.ssrcList, 0, newSsrcList, 0, this.ssrcList.length);
        newSsrcList[newSsrcList.length - 1] = ssrc;
        setSsrcList(newSsrcList);
    }

    private static VideoFormat assertSize(VideoFormat sourceFormat) {
        int width;
        int height;
        if (sourceFormat.matches(new Format(VideoFormat.JPEG_RTP))) {
            Dimension size = sourceFormat.getSize();
            width = size.width % 8 == 0 ? size.width : (size.width / 8) * 8;
            if (size.height % 8 == 0) {
                height = size.height;
            } else {
                height = (size.height / 8) * 8;
            }
        } else if (!sourceFormat.matches(new Format(VideoFormat.H263_RTP))) {
            return sourceFormat;
        } else {
            width = 352;
            height = 288;
        }
        return (VideoFormat) new VideoFormat(null, new Dimension(width, height), -1, null, -1.0f).intersects(sourceFormat);
    }

    /* access modifiers changed from: protected */
    public void checkDevice(AbstractMediaDevice device) {
    }

    public void close() {
        try {
            stop(MediaDirection.SENDRECV);
        } finally {
            disconnectCaptureDevice();
            closeProcessor();
            if (this.disposePlayerOnClose) {
                disposePlayer();
            }
            this.processor = null;
            this.captureDevice = null;
        }
    }

    private void closeProcessor() {
        if (this.processor != null) {
            if (this.processorControllerListener != null) {
                this.processor.removeControllerListener(this.processorControllerListener);
            }
            this.processor.stop();
            if (logger.isTraceEnabled()) {
                logger.trace("Stopped Processor with hashCode " + this.processor.hashCode());
            }
            if (this.processor.getState() == Controller.Realized) {
                DataSource dataOutput;
                try {
                    dataOutput = this.processor.getDataOutput();
                } catch (NotRealizedError e) {
                    dataOutput = null;
                }
                if (dataOutput != null) {
                    dataOutput.disconnect();
                }
            }
            this.processor.deallocate();
            this.processor.close();
            this.processorIsPrematurelyClosed = false;
            disconnectCaptureDevice();
        }
    }

    /* access modifiers changed from: protected */
    public DataSource createCaptureDevice() {
        DataSource captureDevice = getDevice().createOutputDataSource();
        if (captureDevice != null) {
            MuteDataSource muteDataSource = (MuteDataSource) AbstractControls.queryInterface((Controls) captureDevice, MuteDataSource.class);
            if (muteDataSource == null) {
                if (captureDevice instanceof PushBufferDataSource) {
                    captureDevice = new RewritablePushBufferDataSource((PushBufferDataSource) captureDevice);
                } else if (captureDevice instanceof PullBufferDataSource) {
                    captureDevice = new RewritablePullBufferDataSource((PullBufferDataSource) captureDevice);
                }
                muteDataSource = (MuteDataSource) AbstractControls.queryInterface((Controls) captureDevice, MuteDataSource.class);
            }
            if (muteDataSource != null) {
                muteDataSource.setMute(this.mute);
            }
        }
        return captureDevice;
    }

    /* access modifiers changed from: protected */
    public Player createPlayer(DataSource dataSource) {
        Processor player = null;
        Throwable exception = null;
        try {
            player = getDevice().createPlayer(dataSource);
        } catch (Exception ex) {
            exception = ex;
        }
        if (exception != null) {
            logger.error("Failed to create Player for " + MediaStreamImpl.toString(dataSource), exception);
        } else if (player != null) {
            if (this.playerControllerListener == null) {
                this.playerControllerListener = new ControllerListener() {
                    public void controllerUpdate(ControllerEvent event) {
                        MediaDeviceSession.this.playerControllerUpdate(event);
                    }
                };
            }
            player.addControllerListener(this.playerControllerListener);
            player.configure();
            if (logger.isTraceEnabled()) {
                logger.trace("Created Player with hashCode " + player.hashCode() + " for " + MediaStreamImpl.toString(dataSource));
            }
        }
        return player;
    }

    /* access modifiers changed from: protected */
    public Processor createProcessor() {
        DataSource captureDevice = getConnectedCaptureDevice();
        if (captureDevice != null) {
            Processor processor = null;
            Throwable exception = null;
            try {
                processor = Manager.createProcessor(captureDevice);
            } catch (IOException ioe) {
                exception = ioe;
            } catch (NoProcessorException npe) {
                exception = npe;
            }
            if (exception != null) {
                logger.error("Failed to create Processor for " + captureDevice, exception);
            } else {
                if (this.processorControllerListener == null) {
                    this.processorControllerListener = new ControllerListener() {
                        public void controllerUpdate(ControllerEvent event) {
                            MediaDeviceSession.this.processorControllerUpdate(event);
                        }
                    };
                }
                processor.addControllerListener(this.processorControllerListener);
                if (waitForState(processor, Processor.Configured)) {
                    this.processor = processor;
                    this.processorIsPrematurelyClosed = false;
                } else if (this.processorControllerListener != null) {
                    processor.removeControllerListener(this.processorControllerListener);
                }
            }
        }
        return this.processor;
    }

    /* access modifiers changed from: protected */
    public ContentDescriptor createProcessorContentDescriptor(Processor processor) {
        return this.contentDescriptor == null ? new ContentDescriptor(ContentDescriptor.RAW_RTP) : this.contentDescriptor;
    }

    /* access modifiers changed from: protected */
    public Renderer createRenderer(Player player, TrackControl trackControl) {
        return getDevice().createRenderer();
    }

    private void disconnectCaptureDevice() {
        if (this.captureDevice != null) {
            try {
                this.captureDevice.stop();
            } catch (IOException ioe) {
                logger.error("Failed to properly stop captureDevice " + this.captureDevice, ioe);
            }
            this.captureDevice.disconnect();
            this.captureDeviceIsConnected = false;
        }
    }

    private void disposePlayer() {
        synchronized (this.playbacks) {
            for (Playback playback : this.playbacks) {
                if (playback.player != null) {
                    disposePlayer(playback.player);
                    playback.player = null;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void disposePlayer(Player player) {
        synchronized (this.playbacks) {
            if (this.playerControllerListener != null) {
                player.removeControllerListener(this.playerControllerListener);
            }
            player.stop();
            player.deallocate();
            player.close();
        }
    }

    private static Format findFirstMatchingFormat(Format[] formats, Format format) {
        double formatSampleRate;
        ParameterizedVideoFormat parameterizedVideoFormat;
        if (format instanceof AudioFormat) {
            formatSampleRate = ((AudioFormat) format).getSampleRate();
        } else {
            formatSampleRate = -1.0d;
        }
        if (format instanceof ParameterizedVideoFormat) {
            parameterizedVideoFormat = (ParameterizedVideoFormat) format;
        } else {
            parameterizedVideoFormat = null;
        }
        for (Format match : formats) {
            if (match.isSameEncoding(format)) {
                if (match instanceof AudioFormat) {
                    if (formatSampleRate == -1.0d) {
                        return match;
                    }
                    double matchSampleRate = ((AudioFormat) match).getSampleRate();
                    if (matchSampleRate == -1.0d || matchSampleRate == formatSampleRate) {
                        return match;
                    }
                } else if (match instanceof ParameterizedVideoFormat) {
                    if (((ParameterizedVideoFormat) match).formatParametersMatch(format)) {
                        return match;
                    }
                } else if (parameterizedVideoFormat == null || parameterizedVideoFormat.formatParametersMatch(match)) {
                    return match;
                }
            }
        }
        return null;
    }

    public synchronized DataSource getCaptureDevice() {
        if (this.captureDevice == null) {
            this.captureDevice = createCaptureDevice();
        }
        return this.captureDevice;
    }

    /* access modifiers changed from: protected */
    public DataSource getConnectedCaptureDevice() {
        DataSource captureDevice = getCaptureDevice();
        if (captureDevice == null || this.captureDeviceIsConnected) {
            return captureDevice;
        }
        try {
            if (this.format != null) {
                setCaptureDeviceFormat(captureDevice, this.format);
            }
        } catch (Throwable t) {
            logger.warn("Failed to setup an optimized media codec chain by setting the output Format on the input CaptureDevice", t);
        }
        Throwable exception = null;
        try {
            getDevice().connect(captureDevice);
        } catch (IOException ioex) {
            exception = ioex;
        }
        if (exception == null) {
            this.captureDeviceIsConnected = true;
            return captureDevice;
        }
        logger.error("Failed to connect to " + MediaStreamImpl.toString(captureDevice), exception);
        return null;
    }

    public AbstractMediaDevice getDevice() {
        return this.device;
    }

    public Format getProcessorFormat() {
        Processor processor = getProcessor();
        if (!(processor == null || this.processor != processor || this.processorIsPrematurelyClosed)) {
            MediaType mediaType = getMediaType();
            for (TrackControl trackControl : processor.getTrackControls()) {
                if (trackControl.isEnabled()) {
                    Format jmfFormat = trackControl.getFormat();
                    if (mediaType.equals(jmfFormat instanceof VideoFormat ? MediaType.VIDEO : MediaType.AUDIO)) {
                        return jmfFormat;
                    }
                }
            }
        }
        return null;
    }

    public MediaFormatImpl<? extends Format> getFormat() {
        if (logger.isDebugEnabled() && this.processor != null) {
            Format processorFormat = getProcessorFormat();
            Format format = this.format == null ? null : this.format.getFormat();
            boolean processorFormatMatchesFormat = processorFormat == null ? format == null : processorFormat.matches(format);
            if (!processorFormatMatchesFormat) {
                logger.debug("processorFormat != format; processorFormat= `" + processorFormat + "`; format= `" + format + "`");
            }
        }
        return this.format;
    }

    private MediaType getMediaType() {
        return getDevice().getMediaType();
    }

    public DataSource getOutputDataSource() {
        Processor processor = getProcessor();
        if (processor == null || (processor.getState() < Controller.Realized && !waitForState(processor, Controller.Realized))) {
            return null;
        }
        DataSource outputDataSource = processor.getDataOutput();
        if (logger.isTraceEnabled() && outputDataSource != null) {
            logger.trace("Processor with hashCode " + processor.hashCode() + " provided " + MediaStreamImpl.toString(outputDataSource));
        }
        startProcessorInAccordWithDirection(processor);
        return outputDataSource;
    }

    private Playback getPlayback(DataSource dataSource) {
        synchronized (this.playbacks) {
            for (Playback playback : this.playbacks) {
                if (playback.dataSource == dataSource) {
                    return playback;
                }
            }
            return null;
        }
    }

    private Playback getPlayback(ReceiveStream receiveStream) {
        synchronized (this.playbacks) {
            for (Playback playback : this.playbacks) {
                if (playback.receiveStream == receiveStream) {
                    return playback;
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public Player getPlayer(long ssrc) {
        synchronized (this.playbacks) {
            for (Playback playback : this.playbacks) {
                if ((4294967295L & playback.receiveStream.getSSRC()) == ssrc) {
                    Player player = playback.player;
                    return player;
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public List<Player> getPlayers() {
        List<Player> players;
        synchronized (this.playbacks) {
            players = new ArrayList(this.playbacks.size());
            for (Playback playback : this.playbacks) {
                if (playback.player != null) {
                    players.add(playback.player);
                }
            }
        }
        return players;
    }

    private Processor getProcessor() {
        if (this.processor == null) {
            this.processor = createProcessor();
        }
        return this.processor;
    }

    public List<ReceiveStream> getReceiveStreams() {
        List<ReceiveStream> receiveStreams;
        synchronized (this.playbacks) {
            receiveStreams = new ArrayList(this.playbacks.size());
            for (Playback playback : this.playbacks) {
                if (playback.receiveStream != null) {
                    receiveStreams.add(playback.receiveStream);
                }
            }
        }
        return receiveStreams;
    }

    public long[] getRemoteSSRCList() {
        return this.ssrcList;
    }

    public MediaDirection getStartedDirection() {
        return this.startedDirection;
    }

    public List<MediaFormat> getSupportedFormats() {
        Processor processor = getProcessor();
        Set<Format> supportedFormats = new HashSet();
        if (!(processor == null || this.processor != processor || this.processorIsPrematurelyClosed)) {
            MediaType mediaType = getMediaType();
            for (TrackControl trackControl : processor.getTrackControls()) {
                if (trackControl.isEnabled()) {
                    for (Format supportedFormat : trackControl.getSupportedFormats()) {
                        switch (mediaType) {
                            case AUDIO:
                                if (!(supportedFormat instanceof AudioFormat)) {
                                    break;
                                }
                                supportedFormats.add(supportedFormat);
                                break;
                            case VIDEO:
                                if (!(supportedFormat instanceof VideoFormat)) {
                                    break;
                                }
                                supportedFormats.add(supportedFormat);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
        List<MediaFormat> supportedMediaFormats = new ArrayList(supportedFormats.size());
        for (Format format : supportedFormats) {
            supportedMediaFormats.add(MediaFormatImpl.createInstance(format));
        }
        return supportedMediaFormats;
    }

    public boolean isMute() {
        Controls captureDevice = this.captureDevice;
        if (captureDevice == null) {
            return this.mute;
        }
        MuteDataSource muteDataSource = (MuteDataSource) AbstractControls.queryInterface(captureDevice, MuteDataSource.class);
        return muteDataSource == null ? false : muteDataSource.isMute();
    }

    /* access modifiers changed from: protected */
    public void playbackDataSourceAdded(DataSource playbackDataSource) {
    }

    /* access modifiers changed from: protected */
    public void playbackDataSourceRemoved(DataSource playbackDataSource) {
    }

    /* access modifiers changed from: protected */
    public void playbackDataSourceUpdated(DataSource playbackDataSource) {
    }

    public void playbackDataSourceChanged(DataSource playbackDataSource) {
        playbackDataSourceUpdated(playbackDataSource);
    }

    /* access modifiers changed from: protected */
    public void playerConfigureComplete(Processor player) {
        TrackControl[] tcs = player.getTrackControls();
        if (tcs != null && tcs.length != 0) {
            for (int i = 0; i < tcs.length; i++) {
                TrackControl tc = tcs[i];
                Renderer renderer = createRenderer(player, tc);
                if (renderer != null) {
                    try {
                        tc.setRenderer(renderer);
                    } catch (UnsupportedPlugInException upie) {
                        logger.warn("Failed to set " + renderer.getClass().getName() + " renderer on track " + i, upie);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void playerControllerUpdate(ControllerEvent ev) {
        Processor player;
        if (ev instanceof ConfigureCompleteEvent) {
            player = (Processor) ev.getSourceController();
            if (player != null) {
                playerConfigureComplete(player);
                try {
                    player.setContentDescriptor(null);
                    player.realize();
                } catch (NotConfiguredError nce) {
                    logger.error("Failed to set ContentDescriptor to Player.", nce);
                }
            }
        } else if (ev instanceof RealizeCompleteEvent) {
            player = (Processor) ev.getSourceController();
            if (player != null) {
                playerRealizeComplete(player);
                player.start();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void playerRealizeComplete(Processor player) {
    }

    /* access modifiers changed from: protected */
    public void processorControllerUpdate(ControllerEvent ev) {
        Processor processor;
        if (ev instanceof ConfigureCompleteEvent) {
            processor = (Processor) ev.getSourceController();
            if (processor != null) {
                try {
                    processor.setContentDescriptor(createProcessorContentDescriptor(processor));
                } catch (NotConfiguredError nce) {
                    logger.error("Failed to set ContentDescriptor to Processor.", nce);
                }
                if (this.format != null) {
                    setProcessorFormat(processor, this.format);
                }
            }
        } else if (ev instanceof ControllerClosedEvent) {
            processor = (Processor) ev.getSourceController();
            logger.warn(ev);
            if (processor != null && this.processor == processor) {
                this.processorIsPrematurelyClosed = true;
            }
        } else if (ev instanceof RealizeCompleteEvent) {
            for (FormatParametersAwareCodec fpac : getAllTrackControls(FormatParametersAwareCodec.class, (Processor) ev.getSourceController())) {
                Map<String, String> formatParameters = this.format == null ? null : this.format.getFormatParameters();
                if (formatParameters != null) {
                    fpac.setFormatParameters(formatParameters);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void removeSSRC(long ssrc) {
        int index = -1;
        if (this.ssrcList != null && this.ssrcList.length != 0) {
            for (int i = 0; i < this.ssrcList.length; i++) {
                if (this.ssrcList[i] == ssrc) {
                    index = i;
                    break;
                }
            }
            if (index >= 0 && index < this.ssrcList.length) {
                if (this.ssrcList.length == 1) {
                    setSsrcList(null);
                    return;
                }
                long[] newSsrcList = new long[this.ssrcList.length];
                System.arraycopy(this.ssrcList, 0, newSsrcList, 0, index);
                if (index < this.ssrcList.length - 1) {
                    System.arraycopy(this.ssrcList, index + 1, newSsrcList, index, (this.ssrcList.length - index) - 1);
                }
                setSsrcList(newSsrcList);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void receiveStreamAdded(ReceiveStream receiveStream) {
    }

    /* access modifiers changed from: protected */
    public void receiveStreamRemoved(ReceiveStream receiveStream) {
    }

    /* access modifiers changed from: protected */
    public void setCaptureDeviceFormat(DataSource captureDevice, MediaFormatImpl<? extends Format> mediaFormat) {
        Format format = mediaFormat.getFormat();
        if (format instanceof AudioFormat) {
            AudioFormat audioFormat = (AudioFormat) format;
            double sampleRate = OSUtils.IS_ANDROID ? audioFormat.getSampleRate() : -1.0d;
            if (-1 != -1 || sampleRate != -1.0d) {
                FormatControl formatControl = (FormatControl) captureDevice.getControl(FormatControl.class.getName());
                if (formatControl != null) {
                    Format[] supportedFormats = formatControl.getSupportedFormats();
                    if (supportedFormats != null && supportedFormats.length != 0) {
                        if (sampleRate != -1.0d) {
                            String encoding = audioFormat.getEncoding();
                            if ((Constants.G722.equalsIgnoreCase(encoding) || Constants.G722_RTP.equalsIgnoreCase(encoding)) && sampleRate == 8000.0d) {
                                sampleRate = 16000.0d;
                            }
                        }
                        Format supportedAudioFormat = null;
                        for (Format sf : supportedFormats) {
                            if (sf instanceof AudioFormat) {
                                Format saf = (AudioFormat) sf;
                                if ((-1 == -1 || saf.getChannels() == -1) && (-1.0d == sampleRate || saf.getSampleRate() == sampleRate)) {
                                    supportedAudioFormat = saf;
                                    break;
                                }
                            }
                        }
                        if (supportedAudioFormat != null) {
                            formatControl.setFormat(supportedAudioFormat);
                        }
                    }
                }
            }
        }
    }

    public void setContentDescriptor(ContentDescriptor contentDescriptor) {
        if (contentDescriptor == null) {
            throw new NullPointerException("contentDescriptor");
        }
        this.contentDescriptor = contentDescriptor;
    }

    public void setFormat(MediaFormat format) {
        if (getMediaType().equals(format.getMediaType())) {
            this.format = (MediaFormatImpl) format;
            if (logger.isTraceEnabled()) {
                logger.trace("Set format " + this.format + " on " + getClass().getSimpleName() + " " + hashCode());
            }
            if (this.processor != null) {
                int processorState = this.processor.getState();
                if (processorState == Processor.Configured) {
                    setProcessorFormat(this.processor, this.format);
                    return;
                } else if (this.processorIsPrematurelyClosed || ((processorState > Processor.Configured && !this.format.getFormat().equals(getProcessorFormat())) || this.outputSizeChanged)) {
                    this.outputSizeChanged = false;
                    setProcessor(null);
                    return;
                } else {
                    return;
                }
            }
            return;
        }
        throw new IllegalArgumentException("format");
    }

    /* access modifiers changed from: protected */
    public void setProcessorFormat(Processor processor, MediaFormatImpl<? extends Format> mediaFormat) {
        TrackControl[] trackControls = processor.getTrackControls();
        MediaType mediaType = getMediaType();
        Format format = mediaFormat.getFormat();
        for (int trackIndex = 0; trackIndex < trackControls.length; trackIndex++) {
            TrackControl trackControl = trackControls[trackIndex];
            if (trackControl.isEnabled()) {
                Format[] supportedFormats = trackControl.getSupportedFormats();
                if (supportedFormats == null || supportedFormats.length < 1) {
                    trackControl.setEnabled(false);
                } else {
                    Format supportedFormat = null;
                    switch (mediaType) {
                        case AUDIO:
                            if (supportedFormats[0] instanceof AudioFormat) {
                                supportedFormat = findFirstMatchingFormat(supportedFormats, format);
                                if (supportedFormat == null) {
                                    supportedFormat = format;
                                    break;
                                }
                            }
                            break;
                        case VIDEO:
                            if (supportedFormats[0] instanceof VideoFormat) {
                                supportedFormat = findFirstMatchingFormat(supportedFormats, format);
                                if (supportedFormat == null) {
                                    supportedFormat = format;
                                }
                                if (supportedFormat != null) {
                                    supportedFormat = assertSize((VideoFormat) supportedFormat);
                                    break;
                                }
                            }
                            break;
                    }
                    if (supportedFormat == null) {
                        trackControl.setEnabled(false);
                    } else if (!supportedFormat.equals(trackControl.getFormat())) {
                        Format setFormat = setProcessorFormat(trackControl, mediaFormat, supportedFormat);
                        if (setFormat == null) {
                            logger.error("Failed to set format of track " + trackIndex + " to " + supportedFormat + ". Processor is in state " + processor.getState());
                        } else if (setFormat != supportedFormat) {
                            logger.warn("Failed to change format of track " + trackIndex + " from " + setFormat + " to " + supportedFormat + ". Processor is in state " + processor.getState());
                        } else if (logger.isTraceEnabled()) {
                            logger.trace("Set format of track " + trackIndex + " to " + setFormat);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public Format setProcessorFormat(TrackControl trackControl, MediaFormatImpl<? extends Format> mediaFormatImpl, Format format) {
        return trackControl.setFormat(format);
    }

    public void setMute(boolean mute) {
        if (this.mute != mute) {
            this.mute = mute;
            MuteDataSource muteDataSource = (MuteDataSource) AbstractControls.queryInterface(this.captureDevice, MuteDataSource.class);
            if (muteDataSource != null) {
                muteDataSource.setMute(this.mute);
            }
        }
    }

    public void addDTMF(DTMFInbandTone tone) {
        InbandDTMFDataSource inbandDTMFDataSource = (InbandDTMFDataSource) AbstractControls.queryInterface(this.captureDevice, InbandDTMFDataSource.class);
        if (inbandDTMFDataSource != null) {
            inbandDTMFDataSource.addDTMF(tone);
        }
    }

    public void addPlaybackDataSource(DataSource playbackDataSource) {
        synchronized (this.playbacks) {
            Playback playback = getPlayback(playbackDataSource);
            if (playback == null) {
                if (playbackDataSource instanceof ReceiveStreamPushBufferDataSource) {
                    playback = getPlayback(((ReceiveStreamPushBufferDataSource) playbackDataSource).getReceiveStream());
                }
                if (playback == null) {
                    playback = new Playback(playbackDataSource);
                    this.playbacks.add(playback);
                } else {
                    playback.dataSource = playbackDataSource;
                }
                playback.player = createPlayer(playbackDataSource);
                playbackDataSourceAdded(playbackDataSource);
            }
        }
    }

    public void removePlaybackDataSource(DataSource playbackDataSource) {
        synchronized (this.playbacks) {
            Playback playback = getPlayback(playbackDataSource);
            if (playback != null) {
                if (playback.player != null) {
                    disposePlayer(playback.player);
                    playback.player = null;
                }
                playback.dataSource = null;
                if (playback.receiveStream == null) {
                    this.playbacks.remove(playback);
                }
                playbackDataSourceRemoved(playbackDataSource);
            }
        }
    }

    private void setProcessor(Processor processor) {
        if (this.processor != processor) {
            closeProcessor();
            this.processor = processor;
            firePropertyChange(OUTPUT_DATA_SOURCE, null, null);
        }
    }

    public void addReceiveStream(ReceiveStream receiveStream) {
        synchronized (this.playbacks) {
            if (getPlayback(receiveStream) == null) {
                this.playbacks.add(new Playback(receiveStream));
                addSSRC(4294967295L & receiveStream.getSSRC());
                DataSource receiveStreamDataSource = receiveStream.getDataSource();
                if (receiveStreamDataSource != null) {
                    if (receiveStreamDataSource instanceof PushBufferDataSource) {
                        receiveStreamDataSource = new ReceiveStreamPushBufferDataSource(receiveStream, (PushBufferDataSource) receiveStreamDataSource, true);
                    } else {
                        logger.warn("Adding ReceiveStream with DataSource not of type PushBufferDataSource but " + receiveStreamDataSource.getClass().getSimpleName() + " which may prevent the ReceiveStream" + " from properly transferring to another" + " MediaDevice if such a need arises.");
                    }
                    addPlaybackDataSource(receiveStreamDataSource);
                }
                receiveStreamAdded(receiveStream);
            }
        }
    }

    public void removeReceiveStream(ReceiveStream receiveStream) {
        synchronized (this.playbacks) {
            Playback playback = getPlayback(receiveStream);
            if (playback != null) {
                removeSSRC(4294967295L & receiveStream.getSSRC());
                if (playback.dataSource != null) {
                    removePlaybackDataSource(playback.dataSource);
                }
                if (playback.dataSource != null) {
                    logger.warn("Removing ReceiveStream with associated DataSource");
                }
                this.playbacks.remove(playback);
                receiveStreamRemoved(receiveStream);
            }
        }
    }

    private void setSsrcList(long[] newSsrcList) {
        long[] oldSsrcList = getRemoteSSRCList();
        this.ssrcList = newSsrcList;
        firePropertyChange(SSRC_LIST, oldSsrcList, getRemoteSSRCList());
    }

    public void start(MediaDirection direction) {
        if (direction == null) {
            throw new NullPointerException("direction");
        }
        MediaDirection oldValue = this.startedDirection;
        this.startedDirection = this.startedDirection.or(direction);
        if (!oldValue.equals(this.startedDirection)) {
            startedDirectionChanged(oldValue, this.startedDirection);
        }
    }

    /* access modifiers changed from: protected */
    public void startedDirectionChanged(MediaDirection oldValue, MediaDirection newValue) {
        if (newValue.allowsSending()) {
            Processor processor = getProcessor();
            if (processor != null) {
                startProcessorInAccordWithDirection(processor);
            }
        } else if (this.processor != null && this.processor.getState() > Processor.Configured) {
            this.processor.stop();
            if (logger.isTraceEnabled()) {
                logger.trace("Stopped Processor with hashCode " + this.processor.hashCode());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void startProcessorInAccordWithDirection(Processor processor) {
        if (this.startedDirection.allowsSending() && processor.getState() != Controller.Started) {
            processor.start();
            if (logger.isTraceEnabled()) {
                logger.trace("Started Processor with hashCode " + processor.hashCode());
            }
        }
    }

    public void stop(MediaDirection direction) {
        if (direction == null) {
            throw new NullPointerException("direction");
        }
        MediaDirection oldValue = this.startedDirection;
        switch (this.startedDirection) {
            case SENDRECV:
                if (!direction.allowsReceiving()) {
                    if (direction.allowsSending()) {
                        this.startedDirection = MediaDirection.RECVONLY;
                        break;
                    }
                }
                MediaDirection mediaDirection;
                if (direction.allowsSending()) {
                    mediaDirection = MediaDirection.INACTIVE;
                } else {
                    mediaDirection = MediaDirection.SENDONLY;
                }
                this.startedDirection = mediaDirection;
                break;
                break;
            case SENDONLY:
                if (direction.allowsSending()) {
                    this.startedDirection = MediaDirection.INACTIVE;
                    break;
                }
                break;
            case RECVONLY:
                if (direction.allowsReceiving()) {
                    this.startedDirection = MediaDirection.INACTIVE;
                    break;
                }
                break;
            case INACTIVE:
                break;
            default:
                throw new IllegalArgumentException("direction");
        }
        if (!oldValue.equals(this.startedDirection)) {
            startedDirectionChanged(oldValue, this.startedDirection);
        }
    }

    private static boolean waitForState(Processor processor, int state) {
        return new ProcessorUtility().waitForState(processor, state);
    }

    public void copyPlayback(MediaDeviceSession deviceSession) {
        if (deviceSession.disposePlayerOnClose) {
            logger.error("Cannot copy playback if MediaDeviceSession has closed it");
            return;
        }
        this.playbacks.addAll(deviceSession.playbacks);
        setSsrcList(deviceSession.ssrcList);
    }

    public TranscodingDataSource getTranscodingDataSource(ReceiveStream receiveStream) {
        if (this.device instanceof AudioMixerMediaDevice) {
            return ((AudioMixerMediaDevice) this.device).getTranscodingDataSource(receiveStream.getDataSource());
        }
        return null;
    }

    public <T> Set<T> getEncoderControls(Class<T> controlType) {
        return getAllTrackControls(controlType, this.processor);
    }

    public <T> Set<T> getDecoderControls(ReceiveStream receiveStream, Class<T> controlType) {
        TranscodingDataSource transcodingDataSource = getTranscodingDataSource(receiveStream);
        if (transcodingDataSource == null) {
            return Collections.emptySet();
        }
        return getAllTrackControls(controlType, transcodingDataSource.getTranscodingProcessor());
    }

    private <T> Set<T> getAllTrackControls(Class<T> controlType, Processor processor) {
        Set<T> controls = null;
        if (processor != null && processor.getState() >= Controller.Realized) {
            TrackControl[] trackControls = processor.getTrackControls();
            if (!(trackControls == null || trackControls.length == 0)) {
                String className = controlType.getName();
                for (TrackControl trackControl : trackControls) {
                    T o = trackControl.getControl(className);
                    if (controlType.isInstance(o)) {
                        T t = o;
                        if (controls == null) {
                            controls = new HashSet();
                        }
                        controls.add(t);
                    }
                }
            }
        }
        if (controls == null) {
            return Collections.emptySet();
        }
        return controls;
    }
}
