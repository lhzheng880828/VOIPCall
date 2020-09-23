package org.jitsi.impl.neomedia;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import javax.media.CachingControl;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.control.BufferControl;
import javax.media.format.UnsupportedFormatException;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.protocol.PushDataSource;
import javax.media.protocol.PushSourceStream;
import javax.media.rtp.GlobalReceptionStats;
import javax.media.rtp.GlobalTransmissionStats;
import javax.media.rtp.RTPConnector;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.RemoteListener;
import javax.media.rtp.SendStream;
import javax.media.rtp.SendStreamListener;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.SessionListener;
import javax.media.rtp.event.NewSendStreamEvent;
import javax.media.rtp.event.ReceiverReportEvent;
import javax.media.rtp.event.RemoteEvent;
import javax.media.rtp.event.SendStreamEvent;
import javax.media.rtp.event.SenderReportEvent;
import javax.media.rtp.event.SessionEvent;
import javax.media.rtp.rtcp.Feedback;
import javax.media.rtp.rtcp.Report;
import javax.media.rtp.rtcp.SenderReport;
import org.jitsi.impl.neomedia.device.AbstractMediaDevice;
import org.jitsi.impl.neomedia.device.MediaDeviceSession;
import org.jitsi.impl.neomedia.device.VideoMediaDeviceSession;
import org.jitsi.impl.neomedia.format.MediaFormatImpl;
import org.jitsi.impl.neomedia.transform.RTPTransformTCPConnector;
import org.jitsi.impl.neomedia.transform.RTPTransformUDPConnector;
import org.jitsi.impl.neomedia.transform.TransformEngine;
import org.jitsi.impl.neomedia.transform.TransformEngineChain;
import org.jitsi.impl.neomedia.transform.TransformTCPInputStream;
import org.jitsi.impl.neomedia.transform.TransformTCPOutputStream;
import org.jitsi.impl.neomedia.transform.TransformUDPInputStream;
import org.jitsi.impl.neomedia.transform.TransformUDPOutputStream;
import org.jitsi.impl.neomedia.transform.csrc.CsrcTransformEngine;
import org.jitsi.impl.neomedia.transform.csrc.SsrcTransformEngine;
import org.jitsi.impl.neomedia.transform.dtmf.DtmfTransformEngine;
import org.jitsi.impl.neomedia.transform.pt.PayloadTypeTransformEngine;
import org.jitsi.impl.neomedia.transform.rtcp.StatisticsEngine;
import org.jitsi.impl.neomedia.transform.zrtp.ZRTPTransformEngine;
import org.jitsi.service.neomedia.AbstractMediaStream;
import org.jitsi.service.neomedia.AudioMediaStream;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaStream;
import org.jitsi.service.neomedia.MediaStreamStats;
import org.jitsi.service.neomedia.MediaStreamTarget;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.RTPExtension;
import org.jitsi.service.neomedia.RTPTranslator;
import org.jitsi.service.neomedia.SSRCFactory;
import org.jitsi.service.neomedia.SrtpControl;
import org.jitsi.service.neomedia.SrtpControlType;
import org.jitsi.service.neomedia.StreamConnector;
import org.jitsi.service.neomedia.StreamConnector.Protocol;
import org.jitsi.service.neomedia.VideoMediaStream;
import org.jitsi.service.neomedia.control.PacketLossAwareEncoder;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.format.MediaFormat;
import org.jitsi.util.Logger;

public class MediaStreamImpl extends AbstractMediaStream implements ReceiveStreamListener, SendStreamListener, SessionListener, RemoteListener {
    protected static final String PROPERTY_NAME_RECEIVE_BUFFER_LENGTH = "net.java.sip.communicator.impl.neomedia.RECEIVE_BUFFER_LENGTH";
    private static final Logger logger = Logger.getLogger(MediaStreamImpl.class);
    private final Map<Byte, RTPExtension> activeRTPExtensions;
    private CsrcTransformEngine csrcEngine;
    private MediaDeviceSession deviceSession;
    private final PropertyChangeListener deviceSessionPropertyChangeListener;
    private MediaDirection direction;
    private final Map<Byte, MediaFormat> dynamicRTPPayloadTypes;
    private long[] localContributingSourceIDs;
    private long localSourceID;
    private long maxRemoteInterArrivalJitter;
    private MediaStreamStatsImpl mediaStreamStatsImpl;
    private long minRemoteInterArrivalJitter;
    private boolean mute;
    private long numberOfReceivedReceiverReports;
    private long numberOfReceivedSenderReports;
    private PayloadTypeTransformEngine ptTransformEngine;
    private final List<ReceiveStream> receiveStreams;
    private final Vector<Long> remoteSourceIDs;
    private AbstractRTPConnector rtpConnector;
    private MediaStreamTarget rtpConnectorTarget;
    private StreamRTPManager rtpManager;
    private RTPTranslator rtpTranslator;
    protected boolean sendStreamsAreCreated;
    private final SrtpControl srtpControl;
    private SSRCFactory ssrcFactory;
    private boolean started;
    private MediaDirection startedDirection;
    private StatisticsEngine statisticsEngine;

    public MediaStreamImpl(MediaDevice device, SrtpControl srtpControl) {
        this(null, device, srtpControl);
    }

    public MediaStreamImpl(StreamConnector connector, MediaDevice device, SrtpControl srtpControl) {
        this.deviceSessionPropertyChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                String propertyName = ev.getPropertyName();
                if (MediaDeviceSession.OUTPUT_DATA_SOURCE.equals(propertyName)) {
                    MediaStreamImpl.this.deviceSessionOutputDataSourceChanged();
                } else if (MediaDeviceSession.SSRC_LIST.equals(propertyName)) {
                    MediaStreamImpl.this.deviceSessionSsrcListChanged(ev);
                }
            }
        };
        this.dynamicRTPPayloadTypes = new HashMap();
        this.receiveStreams = new LinkedList();
        this.sendStreamsAreCreated = false;
        this.started = false;
        this.remoteSourceIDs = new Vector(1, 1);
        this.localSourceID = -1;
        this.mute = false;
        this.activeRTPExtensions = new Hashtable();
        this.numberOfReceivedSenderReports = 0;
        this.numberOfReceivedReceiverReports = 0;
        this.maxRemoteInterArrivalJitter = 0;
        this.minRemoteInterArrivalJitter = -1;
        this.statisticsEngine = null;
        if (device != null) {
            setDevice(device);
        }
        if (srtpControl == null) {
            srtpControl = NeomediaServiceUtils.getMediaServiceImpl().createSrtpControl(SrtpControlType.ZRTP);
        }
        this.srtpControl = srtpControl;
        if (connector != null) {
            setConnector(connector);
        }
        this.mediaStreamStatsImpl = new MediaStreamStatsImpl(this);
        if (logger.isTraceEnabled()) {
            logger.trace("Created " + getClass().getSimpleName() + " with hashCode " + hashCode());
        }
    }

    /* access modifiers changed from: protected */
    public void configureDataOutputStream(RTPConnectorOutputStream dataOutputStream) {
        dataOutputStream.setPriority(getPriority());
    }

    /* access modifiers changed from: protected */
    public void configureDataInputStream(RTPConnectorInputStream dataInputStream) {
        dataInputStream.setPriority(getPriority());
    }

    /* access modifiers changed from: protected */
    public void configureRTPManagerBufferControl(StreamRTPManager rtpManager, BufferControl bufferControl) {
    }

    private TransformEngineChain createTransformEngineChain() {
        List<TransformEngine> engineChain = new ArrayList(5);
        if (this.csrcEngine == null) {
            this.csrcEngine = new CsrcTransformEngine(this);
        }
        engineChain.add(this.csrcEngine);
        DtmfTransformEngine dtmfEngine = createDtmfTransformEngine();
        if (dtmfEngine != null) {
            engineChain.add(dtmfEngine);
        }
        if (this.statisticsEngine == null) {
            this.statisticsEngine = new StatisticsEngine(this);
        }
        engineChain.add(this.statisticsEngine);
        if (this.ptTransformEngine == null) {
            this.ptTransformEngine = new PayloadTypeTransformEngine();
        }
        engineChain.add(this.ptTransformEngine);
        engineChain.add(this.srtpControl.getTransformEngine());
        SsrcTransformEngine ssrcEngine = createSsrcTransformEngine();
        if (ssrcEngine != null) {
            engineChain.add(ssrcEngine);
        }
        return new TransformEngineChain((TransformEngine[]) engineChain.toArray(new TransformEngine[engineChain.size()]));
    }

    /* access modifiers changed from: protected */
    public DtmfTransformEngine createDtmfTransformEngine() {
        return null;
    }

    /* access modifiers changed from: protected */
    public SsrcTransformEngine createSsrcTransformEngine() {
        return null;
    }

    public void addDynamicRTPPayloadType(byte rtpPayloadType, MediaFormat format) {
        MediaFormatImpl<? extends Format> mediaFormatImpl = (MediaFormatImpl) format;
        synchronized (this.dynamicRTPPayloadTypes) {
            this.dynamicRTPPayloadTypes.put(Byte.valueOf(rtpPayloadType), format);
            if (this.rtpManager != null) {
                this.rtpManager.addFormat(mediaFormatImpl.getFormat(), rtpPayloadType);
            }
        }
    }

    public void addRTPExtension(byte extensionID, RTPExtension rtpExtension) {
        synchronized (this.activeRTPExtensions) {
            if (MediaDirection.INACTIVE.equals(rtpExtension.getDirection())) {
                this.activeRTPExtensions.remove(Byte.valueOf(extensionID));
            } else {
                this.activeRTPExtensions.put(Byte.valueOf(extensionID), rtpExtension);
            }
        }
    }

    private void assertDirection(MediaDirection direction, MediaDirection deviceDirection, String illegalArgumentExceptionMessage) throws IllegalArgumentException {
        if (direction != null && !direction.and(deviceDirection).equals(direction)) {
            throw new IllegalArgumentException(illegalArgumentExceptionMessage);
        }
    }

    public Map<Byte, RTPExtension> getActiveRTPExtensions() {
        HashMap hashMap;
        synchronized (this.activeRTPExtensions) {
            hashMap = new HashMap(this.activeRTPExtensions);
        }
        return hashMap;
    }

    public byte getActiveRTPExtensionID(RTPExtension rtpExtension) {
        synchronized (this.activeRTPExtensions) {
            for (Entry<Byte, RTPExtension> entry : this.activeRTPExtensions.entrySet()) {
                if (((RTPExtension) entry.getValue()).equals(rtpExtension)) {
                    byte byteValue = ((Byte) entry.getKey()).byteValue();
                    return byteValue;
                }
            }
            return (byte) -1;
        }
    }

    /* access modifiers changed from: protected */
    public CsrcTransformEngine getCsrcEngine() {
        return this.csrcEngine;
    }

    public void close() {
        if (logger.isInfoEnabled()) {
            printReceiveStreamStatistics();
        }
        stop();
        closeSendStreams();
        this.srtpControl.cleanup();
        if (this.csrcEngine != null) {
            this.csrcEngine.close();
            this.csrcEngine = null;
        }
        if (this.rtpManager != null) {
            if (logger.isInfoEnabled()) {
                printFlowStatistics(this.rtpManager);
            }
            this.rtpManager.removeReceiveStreamListener(this);
            this.rtpManager.removeSendStreamListener(this);
            this.rtpManager.removeSessionListener(this);
            this.rtpManager.removeRemoteListener(this);
            try {
                this.rtpManager.dispose();
                this.rtpManager = null;
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath t2 = (ThreadDeath) t;
                } else {
                    logger.error("Failed to dispose of RTPManager", t);
                }
            }
        }
        if (this.rtpConnector != null) {
            this.rtpConnector.removeTargets();
        }
        this.rtpConnectorTarget = null;
        if (this.deviceSession != null) {
            this.deviceSession.close();
        }
    }

    private void closeSendStreams() {
        stopSendStreams(true);
    }

    private void createSendStreams() {
        int streamCount;
        StreamRTPManager rtpManager = getRTPManager();
        MediaDeviceSession deviceSession = getDeviceSession();
        DataSource dataSource = deviceSession == null ? null : deviceSession.getOutputDataSource();
        if (dataSource instanceof PushBufferDataSource) {
            PushBufferStream[] streams = ((PushBufferDataSource) dataSource).getStreams();
            streamCount = streams == null ? 0 : streams.length;
        } else if (dataSource instanceof PushDataSource) {
            PushSourceStream[] streams2 = ((PushDataSource) dataSource).getStreams();
            streamCount = streams2 == null ? 0 : streams2.length;
        } else if (dataSource instanceof PullBufferDataSource) {
            PullBufferStream[] streams3 = ((PullBufferDataSource) dataSource).getStreams();
            streamCount = streams3 == null ? 0 : streams3.length;
        } else if (dataSource instanceof PullDataSource) {
            PullSourceStream[] streams4 = ((PullDataSource) dataSource).getStreams();
            streamCount = streams4 == null ? 0 : streams4.length;
        } else {
            streamCount = dataSource == null ? 0 : 1;
        }
        registerCustomCodecFormats(rtpManager);
        for (int streamIndex = 0; streamIndex < streamCount; streamIndex++) {
            try {
                SendStream sendStream = rtpManager.createSendStream(dataSource, streamIndex);
                if (logger.isTraceEnabled()) {
                    logger.trace("Created SendStream with hashCode " + sendStream.hashCode() + " for " + toString(dataSource) + " and streamIndex " + streamIndex + " in RTPManager with hashCode " + rtpManager.hashCode());
                }
                long localSSRC = sendStream.getSSRC() & 4294967295L;
                if (getLocalSourceID() != localSSRC) {
                    setLocalSourceID(localSSRC);
                }
            } catch (IOException ioe) {
                logger.error("Failed to create send stream for data source " + dataSource + " and stream index " + streamIndex, ioe);
            } catch (UnsupportedFormatException ufe) {
                logger.error("Failed to create send stream for data source " + dataSource + " and stream index " + streamIndex + " because of failed format " + ufe.getFailedFormat(), ufe);
            }
        }
        this.sendStreamsAreCreated = true;
        if (logger.isTraceEnabled()) {
            Vector<SendStream> sendStreams = rtpManager.getSendStreams();
            logger.trace("Total number of SendStreams in RTPManager with hashCode " + rtpManager.hashCode() + " is " + (sendStreams == null ? 0 : sendStreams.size()));
        }
    }

    /* access modifiers changed from: protected */
    public void deviceSessionChanged(MediaDeviceSession oldValue, MediaDeviceSession newValue) {
        recreateSendStreams();
    }

    /* access modifiers changed from: private */
    public void deviceSessionOutputDataSourceChanged() {
        recreateSendStreams();
    }

    /* access modifiers changed from: private */
    public void deviceSessionSsrcListChanged(PropertyChangeEvent ev) {
        long[] ssrcArray = (long[]) ev.getNewValue();
        if (ssrcArray == null) {
            this.localContributingSourceIDs = null;
            return;
        }
        int i;
        int elementsToRemove = 0;
        Vector<Long> remoteSourceIDs = this.remoteSourceIDs;
        for (long csrc : ssrcArray) {
            if (remoteSourceIDs.contains(Long.valueOf(csrc))) {
                elementsToRemove++;
            }
        }
        if (elementsToRemove >= ssrcArray.length) {
            this.localContributingSourceIDs = null;
            return;
        }
        long[] csrcArray = new long[Math.min((ssrcArray.length - elementsToRemove) + 1, 15)];
        int j = 0;
        for (i = 0; i < ssrcArray.length && j < csrcArray.length - 1; i++) {
            long ssrc = ssrcArray[i];
            if (!remoteSourceIDs.contains(Long.valueOf(ssrc))) {
                csrcArray[j] = ssrc;
                j++;
            }
        }
        csrcArray[csrcArray.length - 1] = getLocalSourceID();
        this.localContributingSourceIDs = csrcArray;
    }

    private void doSetTarget(MediaStreamTarget target) {
        InetSocketAddress newDataAddr;
        InetSocketAddress newControlAddr;
        boolean targetIsSet;
        if (target == null) {
            newDataAddr = null;
            newControlAddr = null;
        } else {
            newDataAddr = target.getDataAddress();
            newControlAddr = target.getControlAddress();
        }
        if (this.rtpConnectorTarget != null) {
            InetSocketAddress oldDataAddr = this.rtpConnectorTarget.getDataAddress();
            boolean removeTargets = oldDataAddr == null ? newDataAddr != null : !oldDataAddr.equals(newDataAddr);
            if (!removeTargets) {
                InetSocketAddress oldControlAddr = this.rtpConnectorTarget.getControlAddress();
                removeTargets = oldControlAddr == null ? newControlAddr != null : !oldControlAddr.equals(newControlAddr);
            }
            if (removeTargets) {
                this.rtpConnector.removeTargets();
                this.rtpConnectorTarget = null;
            }
        }
        if (target == null) {
            targetIsSet = true;
        } else {
            InetAddress controlInetAddr;
            int controlPort;
            if (newControlAddr == null) {
                controlInetAddr = null;
                controlPort = 0;
            } else {
                controlInetAddr = newControlAddr.getAddress();
                controlPort = newControlAddr.getPort();
            }
            try {
                this.rtpConnector.addTarget(new SessionAddress(newDataAddr.getAddress(), newDataAddr.getPort(), controlInetAddr, controlPort));
                targetIsSet = true;
            } catch (IOException ioe) {
                targetIsSet = false;
                logger.error("Failed to set target " + target, ioe);
            }
        }
        if (targetIsSet) {
            this.rtpConnectorTarget = target;
            if (logger.isTraceEnabled()) {
                logger.trace("Set target of " + getClass().getSimpleName() + " with hashCode " + hashCode() + " to " + target);
            }
        }
    }

    public AbstractMediaDevice getDevice() {
        MediaDeviceSession deviceSession = getDeviceSession();
        return deviceSession == null ? null : deviceSession.getDevice();
    }

    private MediaDirection getDeviceDirection() {
        MediaDeviceSession deviceSession = getDeviceSession();
        return deviceSession == null ? MediaDirection.SENDRECV : deviceSession.getDevice().getDirection();
    }

    public MediaDeviceSession getDeviceSession() {
        return this.deviceSession;
    }

    public MediaDirection getDirection() {
        return this.direction == null ? getDeviceDirection() : this.direction;
    }

    public Map<Byte, MediaFormat> getDynamicRTPPayloadTypes() {
        HashMap hashMap;
        synchronized (this.dynamicRTPPayloadTypes) {
            hashMap = new HashMap(this.dynamicRTPPayloadTypes);
        }
        return hashMap;
    }

    public byte getDynamicRTPPayloadType(String encoding) {
        byte byteValue;
        synchronized (this.dynamicRTPPayloadTypes) {
            for (Entry<Byte, MediaFormat> entry : this.dynamicRTPPayloadTypes.entrySet()) {
                if (((MediaFormat) entry.getValue()).getEncoding().equals(encoding)) {
                    byteValue = ((Byte) entry.getKey()).byteValue();
                    break;
                }
            }
            byteValue = (byte) -1;
        }
        return byteValue;
    }

    public MediaFormat getFormat() {
        MediaDeviceSession deviceSession = getDeviceSession();
        return deviceSession == null ? null : deviceSession.getFormat();
    }

    public long getLocalSourceID() {
        return this.localSourceID;
    }

    public InetSocketAddress getRemoteControlAddress() {
        if (this.rtpConnector != null) {
            StreamConnector connector = this.rtpConnector.getConnector();
            if (connector != null) {
                if (connector.getDataSocket() != null) {
                    return (InetSocketAddress) connector.getControlSocket().getRemoteSocketAddress();
                }
                if (connector.getDataTCPSocket() != null) {
                    return (InetSocketAddress) connector.getControlTCPSocket().getRemoteSocketAddress();
                }
            }
        }
        return null;
    }

    public InetSocketAddress getRemoteDataAddress() {
        StreamConnector connector;
        if (this.rtpConnector != null) {
            connector = this.rtpConnector.getConnector();
        } else {
            connector = null;
        }
        if (connector == null) {
            return null;
        }
        if (connector.getDataSocket() != null) {
            return (InetSocketAddress) connector.getDataSocket().getRemoteSocketAddress();
        }
        if (connector.getDataTCPSocket() != null) {
            return (InetSocketAddress) connector.getDataTCPSocket().getRemoteSocketAddress();
        }
        return null;
    }

    public InetSocketAddress getLocalControlAddress() {
        StreamConnector connector;
        if (this.rtpConnector != null) {
            connector = this.rtpConnector.getConnector();
        } else {
            connector = null;
        }
        if (connector == null) {
            return null;
        }
        if (connector.getDataSocket() != null) {
            return (InetSocketAddress) connector.getControlSocket().getLocalSocketAddress();
        }
        if (connector.getDataTCPSocket() != null) {
            return (InetSocketAddress) connector.getControlTCPSocket().getLocalSocketAddress();
        }
        return null;
    }

    public InetSocketAddress getLocalDataAddress() {
        StreamConnector connector;
        if (this.rtpConnector != null) {
            connector = this.rtpConnector.getConnector();
        } else {
            connector = null;
        }
        if (connector == null) {
            return null;
        }
        if (connector.getDataSocket() != null) {
            return (InetSocketAddress) connector.getDataSocket().getLocalSocketAddress();
        }
        if (connector.getDataTCPSocket() != null) {
            return (InetSocketAddress) connector.getDataTCPSocket().getLocalSocketAddress();
        }
        return null;
    }

    public Protocol getTransportProtocol() {
        StreamConnector connector;
        if (this.rtpConnector != null) {
            connector = this.rtpConnector.getConnector();
        } else {
            connector = null;
        }
        if (connector == null) {
            return null;
        }
        return connector.getProtocol();
    }

    public long getRemoteSourceID() {
        return this.remoteSourceIDs.isEmpty() ? -1 : ((Long) this.remoteSourceIDs.lastElement()).longValue();
    }

    public List<Long> getRemoteSourceIDs() {
        return Collections.unmodifiableList(this.remoteSourceIDs);
    }

    /* access modifiers changed from: protected */
    public AbstractRTPConnector getRTPConnector() {
        return this.rtpConnector;
    }

    public StreamRTPManager getRTPManager() {
        if (this.rtpManager == null) {
            RTPConnector rtpConnector = getRTPConnector();
            if (rtpConnector == null) {
                throw new IllegalStateException("rtpConnector");
            }
            this.rtpManager = new StreamRTPManager(this, this.rtpTranslator);
            registerCustomCodecFormats(this.rtpManager);
            this.rtpManager.addReceiveStreamListener(this);
            this.rtpManager.addSendStreamListener(this);
            this.rtpManager.addSessionListener(this);
            this.rtpManager.addRemoteListener(this);
            BufferControl bc = (BufferControl) this.rtpManager.getControl(BufferControl.class);
            if (bc != null) {
                configureRTPManagerBufferControl(this.rtpManager, bc);
            }
            this.rtpManager.setSSRCFactory(this.ssrcFactory);
            this.rtpManager.initialize(rtpConnector);
            long localSSRC = this.rtpManager.getLocalSSRC();
            setLocalSourceID(localSSRC == CachingControl.LENGTH_UNKNOWN ? -1 : 4294967295L & localSSRC);
        }
        return this.rtpManager;
    }

    public SrtpControl getSrtpControl() {
        return this.srtpControl;
    }

    public boolean isMute() {
        MediaDeviceSession deviceSession = getDeviceSession();
        return deviceSession == null ? this.mute : deviceSession.isMute();
    }

    public boolean isStarted() {
        return this.started;
    }

    public StreamRTPManager queryRTPManager() {
        return this.rtpManager;
    }

    /* access modifiers changed from: protected */
    public void recreateSendStreams() {
        if (this.sendStreamsAreCreated) {
            closeSendStreams();
            if (getDeviceSession() != null && this.rtpManager != null) {
                if (MediaDirection.SENDONLY.equals(this.startedDirection) || MediaDirection.SENDRECV.equals(this.startedDirection)) {
                    startSendStreams();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void registerCustomCodecFormats(StreamRTPManager rtpManager) {
        synchronized (this.dynamicRTPPayloadTypes) {
            for (Entry<Byte, MediaFormat> dynamicRTPPayloadType : this.dynamicRTPPayloadTypes.entrySet()) {
                rtpManager.addFormat(((MediaFormatImpl) dynamicRTPPayloadType.getValue()).getFormat(), ((Byte) dynamicRTPPayloadType.getKey()).byteValue());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void rtpConnectorChanged(AbstractRTPConnector oldValue, AbstractRTPConnector newValue) {
        this.srtpControl.setConnector(newValue);
        if (newValue != null) {
            if (newValue instanceof RTPTransformUDPConnector) {
                ((RTPTransformUDPConnector) newValue).setEngine(createTransformEngineChain());
            } else if (newValue instanceof RTPTransformTCPConnector) {
                ((RTPTransformTCPConnector) newValue).setEngine(createTransformEngineChain());
            }
            if (this.rtpConnectorTarget != null) {
                doSetTarget(this.rtpConnectorTarget);
            }
        }
    }

    /* access modifiers changed from: private */
    public void rtpConnectorInputStreamCreated(RTPConnectorInputStream inputStream, boolean data) {
        try {
            String str;
            StringBuilder append = new StringBuilder().append(MediaStreamImpl.class.getName()).append(".rtpConnector.");
            if (data) {
                str = "data";
            } else {
                str = "control";
            }
            firePropertyChange(append.append(str).append("InputStream").toString(), null, inputStream);
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                ThreadDeath t2 = (ThreadDeath) t;
            } else {
                logger.error(t);
            }
        }
    }

    public void setConnector(StreamConnector connector) {
        if (connector == null) {
            throw new NullPointerException("connector");
        }
        AbstractRTPConnector oldValue = this.rtpConnector;
        if (oldValue == null || oldValue.getConnector() != connector) {
            switch (connector.getProtocol()) {
                case UDP:
                    this.rtpConnector = new RTPTransformUDPConnector(connector) {
                        /* access modifiers changed from: protected */
                        public TransformUDPInputStream createControlInputStream() throws IOException {
                            TransformUDPInputStream s = super.createControlInputStream();
                            MediaStreamImpl.this.rtpConnectorInputStreamCreated(s, false);
                            return s;
                        }

                        /* access modifiers changed from: protected */
                        public TransformUDPInputStream createDataInputStream() throws IOException {
                            TransformUDPInputStream s = super.createDataInputStream();
                            MediaStreamImpl.this.rtpConnectorInputStreamCreated(s, true);
                            if (s != null) {
                                MediaStreamImpl.this.configureDataInputStream(s);
                            }
                            return s;
                        }

                        /* access modifiers changed from: protected */
                        public TransformUDPOutputStream createDataOutputStream() throws IOException {
                            TransformUDPOutputStream s = super.createDataOutputStream();
                            if (s != null) {
                                MediaStreamImpl.this.configureDataOutputStream(s);
                            }
                            return s;
                        }
                    };
                    break;
                case TCP:
                    this.rtpConnector = new RTPTransformTCPConnector(connector) {
                        /* access modifiers changed from: protected */
                        public TransformTCPInputStream createControlInputStream() throws IOException {
                            TransformTCPInputStream s = super.createControlInputStream();
                            MediaStreamImpl.this.rtpConnectorInputStreamCreated(s, false);
                            return s;
                        }

                        /* access modifiers changed from: protected */
                        public TransformTCPInputStream createDataInputStream() throws IOException {
                            TransformTCPInputStream s = super.createDataInputStream();
                            MediaStreamImpl.this.rtpConnectorInputStreamCreated(s, true);
                            if (s != null) {
                                MediaStreamImpl.this.configureDataInputStream(s);
                            }
                            return s;
                        }

                        /* access modifiers changed from: protected */
                        public TransformTCPOutputStream createDataOutputStream() throws IOException {
                            TransformTCPOutputStream s = super.createDataOutputStream();
                            if (s != null) {
                                MediaStreamImpl.this.configureDataOutputStream(s);
                            }
                            return s;
                        }
                    };
                    break;
                default:
                    throw new IllegalArgumentException("connector");
            }
            rtpConnectorChanged(oldValue, this.rtpConnector);
        }
    }

    public void setDevice(MediaDevice device) {
        if (device == null) {
            throw new NullPointerException("device");
        }
        AbstractMediaDevice abstractMediaDevice = (AbstractMediaDevice) device;
        if (this.deviceSession == null || this.deviceSession.getDevice() != device) {
            MediaFormat format;
            MediaDirection startedDirection;
            assertDirection(this.direction, device.getDirection(), "device");
            MediaDeviceSession oldValue = this.deviceSession;
            if (this.deviceSession != null) {
                format = getFormat();
                startedDirection = this.deviceSession.getStartedDirection();
                this.deviceSession.removePropertyChangeListener(this.deviceSessionPropertyChangeListener);
                this.deviceSession.setDisposePlayerOnClose(!(this.deviceSession instanceof VideoMediaDeviceSession));
                this.deviceSession.close();
                this.deviceSession = null;
            } else {
                format = null;
                startedDirection = MediaDirection.INACTIVE;
            }
            this.deviceSession = abstractMediaDevice.createSession();
            if (oldValue != null) {
                this.deviceSession.copyPlayback(oldValue);
            }
            this.deviceSession.addPropertyChangeListener(this.deviceSessionPropertyChangeListener);
            this.direction = null;
            if (this.deviceSession != null) {
                if (format != null) {
                    this.deviceSession.setFormat(format);
                }
                this.deviceSession.setMute(this.mute);
            }
            deviceSessionChanged(oldValue, this.deviceSession);
            if (this.deviceSession != null) {
                this.deviceSession.start(startedDirection);
                synchronized (this.receiveStreams) {
                    for (ReceiveStream receiveStream : this.receiveStreams) {
                        this.deviceSession.addReceiveStream(receiveStream);
                    }
                }
            }
        }
    }

    public void setDirection(MediaDirection direction) {
        if (direction == null) {
            throw new NullPointerException("direction");
        } else if (this.direction != direction) {
            if (logger.isTraceEnabled()) {
                logger.trace("Changing direction of stream " + hashCode() + " from:" + this.direction + " to:" + direction);
            }
            assertDirection(direction, getDeviceDirection(), "direction");
            this.direction = direction;
            switch (this.direction) {
                case INACTIVE:
                    stop(MediaDirection.SENDRECV);
                    return;
                case RECVONLY:
                    stop(MediaDirection.SENDONLY);
                    break;
                case SENDONLY:
                    stop(MediaDirection.RECVONLY);
                    break;
                case SENDRECV:
                    break;
                default:
                    return;
            }
            if (this.started) {
                start(this.direction);
            }
        }
    }

    public void setFormat(MediaFormat format) {
        MediaDeviceSession deviceSession = getDeviceSession();
        MediaFormatImpl<? extends Format> deviceSessionFormat = null;
        if (deviceSession != null) {
            deviceSessionFormat = deviceSession.getFormat();
            if (deviceSessionFormat != null && deviceSessionFormat.equals(format) && deviceSessionFormat.advancedAttributesAreEqual(deviceSessionFormat.getAdvancedAttributes(), format.getAdvancedAttributes())) {
                return;
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Changing format of stream " + hashCode() + " from: " + deviceSessionFormat + " to: " + format);
        }
        handleAttributes(format, format.getAdvancedAttributes());
        handleAttributes(format, format.getFormatParameters());
        if (deviceSession != null) {
            deviceSession.setFormat(format);
        }
    }

    /* access modifiers changed from: protected */
    public void handleAttributes(MediaFormat format, Map<String, String> map) {
    }

    public void setMute(boolean mute) {
        if (this.mute != mute) {
            if (logger.isTraceEnabled()) {
                logger.trace((mute ? "Muting" : "Unmuting") + " stream with hashcode " + hashCode());
            }
            this.mute = mute;
            MediaDeviceSession deviceSession = getDeviceSession();
            if (deviceSession != null) {
                deviceSession.setMute(this.mute);
            }
        }
    }

    public MediaStreamTarget getTarget() {
        return this.rtpConnectorTarget;
    }

    public void setTarget(MediaStreamTarget target) {
        if (target == null) {
            if (this.rtpConnectorTarget == null) {
                return;
            }
        } else if (target.equals(this.rtpConnectorTarget)) {
            return;
        }
        doSetTarget(target);
    }

    public void start() {
        start(getDirection());
        this.started = true;
    }

    private void start(MediaDirection direction) {
        if (direction == null) {
            throw new NullPointerException("direction");
        }
        boolean getRTPManagerForRTPTranslator = true;
        MediaDeviceSession deviceSession = getDeviceSession();
        if (direction.allowsSending() && (this.startedDirection == null || !this.startedDirection.allowsSending())) {
            getRTPManagerForRTPTranslator = false;
            startSendStreams();
            if (deviceSession != null) {
                deviceSession.start(MediaDirection.SENDONLY);
            }
            if (MediaDirection.RECVONLY.equals(this.startedDirection)) {
                this.startedDirection = MediaDirection.SENDRECV;
            } else if (this.startedDirection == null) {
                this.startedDirection = MediaDirection.SENDONLY;
            }
            if (logger.isInfoEnabled()) {
                MediaType mediaType = getMediaType();
                MediaStreamStats stats = getMediaStreamStats();
                logger.info(mediaType + " codec/freq: " + stats.getEncoding() + "/" + stats.getEncodingClockRate() + " Hz");
                logger.info(mediaType + " remote IP/port: " + stats.getRemoteIPAddress() + "/" + String.valueOf(stats.getRemotePort()));
            }
        }
        if (direction.allowsReceiving() && (this.startedDirection == null || !this.startedDirection.allowsReceiving())) {
            getRTPManagerForRTPTranslator = false;
            startReceiveStreams();
            if (deviceSession != null) {
                deviceSession.start(MediaDirection.RECVONLY);
            }
            if (MediaDirection.SENDONLY.equals(this.startedDirection)) {
                this.startedDirection = MediaDirection.SENDRECV;
            } else if (this.startedDirection == null) {
                this.startedDirection = MediaDirection.RECVONLY;
            }
        }
        if (getRTPManagerForRTPTranslator && this.rtpTranslator != null) {
            getRTPManager();
        }
    }

    private void startReceiveStreams() {
        List<ReceiveStream> receiveStreams;
        try {
            receiveStreams = getRTPManager().getReceiveStreams();
        } catch (Exception ex) {
            if (logger.isTraceEnabled()) {
                logger.trace("Failed to retrieve receive streams", ex);
            }
            receiveStreams = null;
        }
        if (receiveStreams != null) {
            if (receiveStreams.isEmpty() && this.receiveStreams != null) {
                receiveStreams = this.receiveStreams;
            }
            for (ReceiveStream receiveStream : receiveStreams) {
                try {
                    DataSource receiveStreamDataSource = receiveStream.getDataSource();
                    if (receiveStreamDataSource != null) {
                        receiveStreamDataSource.start();
                    }
                } catch (IOException ioex) {
                    logger.warn("Failed to start receive stream " + receiveStream, ioex);
                }
            }
        }
    }

    private void startSendStreams() {
        if (!this.sendStreamsAreCreated) {
            createSendStreams();
        }
        Iterable<SendStream> sendStreams = getRTPManager().getSendStreams();
        if (sendStreams != null) {
            for (SendStream sendStream : sendStreams) {
                try {
                    DataSource sendStreamDataSource = sendStream.getDataSource();
                    sendStreamDataSource.connect();
                    sendStream.start();
                    sendStreamDataSource.start();
                    if (logger.isTraceEnabled()) {
                        logger.trace("Started SendStream with hashCode " + sendStream.hashCode());
                    }
                } catch (IOException ioe) {
                    logger.warn("Failed to start stream " + sendStream, ioe);
                }
            }
        }
    }

    public void stop() {
        stop(MediaDirection.SENDRECV);
        this.started = false;
    }

    private void stop(MediaDirection direction) {
        if (direction == null) {
            throw new NullPointerException("direction");
        } else if (this.rtpManager != null) {
            if ((MediaDirection.SENDRECV.equals(direction) || MediaDirection.SENDONLY.equals(direction)) && (MediaDirection.SENDRECV.equals(this.startedDirection) || MediaDirection.SENDONLY.equals(this.startedDirection))) {
                stopSendStreams(this instanceof VideoMediaStream);
                if (this.deviceSession != null) {
                    this.deviceSession.stop(MediaDirection.SENDONLY);
                }
                if (MediaDirection.SENDRECV.equals(this.startedDirection)) {
                    this.startedDirection = MediaDirection.RECVONLY;
                } else if (MediaDirection.SENDONLY.equals(this.startedDirection)) {
                    this.startedDirection = null;
                }
            }
            if (!MediaDirection.SENDRECV.equals(direction) && !MediaDirection.RECVONLY.equals(direction)) {
                return;
            }
            if (MediaDirection.SENDRECV.equals(this.startedDirection) || MediaDirection.RECVONLY.equals(this.startedDirection)) {
                stopReceiveStreams();
                if (this.deviceSession != null) {
                    this.deviceSession.stop(MediaDirection.RECVONLY);
                }
                if (MediaDirection.SENDRECV.equals(this.startedDirection)) {
                    this.startedDirection = MediaDirection.SENDONLY;
                } else if (MediaDirection.RECVONLY.equals(this.startedDirection)) {
                    this.startedDirection = null;
                }
            }
        }
    }

    private void stopReceiveStreams() {
        List<ReceiveStream> receiveStreams;
        try {
            receiveStreams = this.rtpManager.getReceiveStreams();
        } catch (Exception ex) {
            if (logger.isTraceEnabled()) {
                logger.trace("Failed to retrieve receive streams", ex);
            }
            receiveStreams = null;
        }
        if (receiveStreams != null) {
            if (receiveStreams.isEmpty() && this.receiveStreams != null) {
                receiveStreams = this.receiveStreams;
            }
            for (ReceiveStream receiveStream : receiveStreams) {
                try {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Stopping receive stream with hashcode " + receiveStream.hashCode());
                    }
                    DataSource receiveStreamDataSource = receiveStream.getDataSource();
                    if (receiveStreamDataSource != null) {
                        receiveStreamDataSource.stop();
                    }
                } catch (IOException ioex) {
                    logger.warn("Failed to stop receive stream " + receiveStream, ioex);
                }
            }
        }
    }

    private Iterable<SendStream> stopSendStreams(boolean close) {
        if (this.rtpManager == null) {
            return null;
        }
        Iterable<SendStream> stoppedSendStreams = stopSendStreams(this.rtpManager.getSendStreams(), close);
        if (!close) {
            return stoppedSendStreams;
        }
        this.sendStreamsAreCreated = false;
        return stoppedSendStreams;
    }

    private Iterable<SendStream> stopSendStreams(Iterable<SendStream> sendStreams, boolean close) {
        if (sendStreams == null) {
            return null;
        }
        for (SendStream sendStream : sendStreams) {
            try {
                if (logger.isTraceEnabled()) {
                    logger.trace("Stopping send stream with hashcode " + sendStream.hashCode());
                }
                sendStream.getDataSource().stop();
                sendStream.stop();
                if (close) {
                    try {
                        sendStream.close();
                    } catch (NullPointerException npe) {
                        logger.error("Failed to close send stream " + sendStream, npe);
                    }
                }
            } catch (IOException ioe) {
                logger.warn("Failed to stop send stream " + sendStream, ioe);
            }
        }
        return sendStreams;
    }

    public static String toString(DataSource dataSource) {
        StringBuffer str = new StringBuffer();
        str.append(dataSource.getClass().getSimpleName());
        str.append(" with hashCode ");
        str.append(dataSource.hashCode());
        MediaLocator locator = dataSource.getLocator();
        if (locator != null) {
            str.append(" and locator ");
            str.append(locator);
        }
        return str.toString();
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    public void update(javax.media.rtp.event.ReceiveStreamEvent r11) {
        /*
        r10 = this;
        r6 = r11 instanceof javax.media.rtp.event.NewReceiveStreamEvent;
        if (r6 == 0) goto L_0x0056;
    L_0x0004:
        r2 = r11.getReceiveStream();
        if (r2 == 0) goto L_0x0052;
    L_0x000a:
        r6 = 4294967295; // 0xffffffff float:NaN double:2.1219957905E-314;
        r8 = r2.getSSRC();
        r4 = r6 & r8;
        r6 = logger;
        r6 = r6.isTraceEnabled();
        if (r6 == 0) goto L_0x0035;
    L_0x001d:
        r6 = logger;
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "Received new ReceiveStream with ssrc ";
        r7 = r7.append(r8);
        r7 = r7.append(r4);
        r7 = r7.toString();
        r6.trace(r7);
    L_0x0035:
        r10.addRemoteSourceID(r4);
        r7 = r10.receiveStreams;
        monitor-enter(r7);
        r6 = r10.receiveStreams;	 Catch:{ all -> 0x0053 }
        r6 = r6.contains(r2);	 Catch:{ all -> 0x0053 }
        if (r6 != 0) goto L_0x0051;
    L_0x0043:
        r6 = r10.receiveStreams;	 Catch:{ all -> 0x0053 }
        r6.add(r2);	 Catch:{ all -> 0x0053 }
        r0 = r10.getDeviceSession();	 Catch:{ all -> 0x0053 }
        if (r0 == 0) goto L_0x0051;
    L_0x004e:
        r0.addReceiveStream(r2);	 Catch:{ all -> 0x0053 }
    L_0x0051:
        monitor-exit(r7);	 Catch:{ all -> 0x0053 }
    L_0x0052:
        return;
    L_0x0053:
        r6 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x0053 }
        throw r6;
    L_0x0056:
        r6 = r11 instanceof javax.media.rtp.event.TimeoutEvent;
        if (r6 == 0) goto L_0x007e;
    L_0x005a:
        r2 = r11.getReceiveStream();
        if (r2 == 0) goto L_0x0052;
    L_0x0060:
        r7 = r10.receiveStreams;
        monitor-enter(r7);
        r6 = r10.receiveStreams;	 Catch:{ all -> 0x007b }
        r6 = r6.contains(r2);	 Catch:{ all -> 0x007b }
        if (r6 == 0) goto L_0x0079;
    L_0x006b:
        r6 = r10.receiveStreams;	 Catch:{ all -> 0x007b }
        r6.remove(r2);	 Catch:{ all -> 0x007b }
        r0 = r10.getDeviceSession();	 Catch:{ all -> 0x007b }
        if (r0 == 0) goto L_0x0079;
    L_0x0076:
        r0.removeReceiveStream(r2);	 Catch:{ all -> 0x007b }
    L_0x0079:
        monitor-exit(r7);	 Catch:{ all -> 0x007b }
        goto L_0x0052;
    L_0x007b:
        r6 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x007b }
        throw r6;
    L_0x007e:
        r6 = r11 instanceof javax.media.rtp.event.RemotePayloadChangeEvent;
        if (r6 == 0) goto L_0x0052;
    L_0x0082:
        r2 = r11.getReceiveStream();
        if (r2 == 0) goto L_0x0052;
    L_0x0088:
        r0 = r10.getDeviceSession();
        if (r0 == 0) goto L_0x0052;
    L_0x008e:
        r3 = r0.getTranscodingDataSource(r2);
        r3.disconnect();	 Catch:{ IOException -> 0x00a3 }
        r3.connect();	 Catch:{ IOException -> 0x00a3 }
        r3.start();	 Catch:{ IOException -> 0x00a3 }
        r6 = r2.getDataSource();	 Catch:{ IOException -> 0x00a3 }
        r0.playbackDataSourceChanged(r6);	 Catch:{ IOException -> 0x00a3 }
        goto L_0x0052;
    L_0x00a3:
        r1 = move-exception;
        r6 = logger;
        r7 = "Error re-creating processor in transcoding DataSource";
        r6.error(r7, r1);
        goto L_0x0052;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.MediaStreamImpl.update(javax.media.rtp.event.ReceiveStreamEvent):void");
    }

    public void update(SendStreamEvent event) {
        if (event instanceof NewSendStreamEvent) {
            long localSourceID = event.getSendStream().getSSRC() & 4294967295L;
            if (getLocalSourceID() != localSourceID) {
                setLocalSourceID(localSourceID);
            }
        }
    }

    public void update(SessionEvent event) {
    }

    public void update(RemoteEvent remoteEvent) {
        if ((remoteEvent instanceof SenderReportEvent) || (remoteEvent instanceof ReceiverReportEvent)) {
            Report report;
            boolean senderReport = false;
            if (remoteEvent instanceof SenderReportEvent) {
                this.numberOfReceivedSenderReports++;
                report = ((SenderReportEvent) remoteEvent).getReport();
                senderReport = true;
            } else {
                this.numberOfReceivedReceiverReports++;
                report = ((ReceiverReportEvent) remoteEvent).getReport();
            }
            Feedback feedback = null;
            long remoteJitter = -1;
            if (report.getFeedbackReports().size() > 0) {
                feedback = (Feedback) report.getFeedbackReports().get(0);
                remoteJitter = feedback.getJitter();
                if (remoteJitter < this.minRemoteInterArrivalJitter || this.minRemoteInterArrivalJitter == -1) {
                    this.minRemoteInterArrivalJitter = remoteJitter;
                }
                if (this.maxRemoteInterArrivalJitter < remoteJitter) {
                    this.maxRemoteInterArrivalJitter = remoteJitter;
                }
            }
            if (!(feedback == null || getDirection() == MediaDirection.INACTIVE)) {
                Set<PacketLossAwareEncoder> plaes = null;
                MediaDeviceSession deviceSession = getDeviceSession();
                if (deviceSession != null) {
                    plaes = deviceSession.getEncoderControls(PacketLossAwareEncoder.class);
                }
                if (!(plaes == null || plaes.isEmpty())) {
                    int expectedPacketLoss = (feedback.getFractionLost() * 100) / 256;
                    for (PacketLossAwareEncoder plae : plaes) {
                        if (plae != null) {
                            plae.setExpectedPacketLoss(expectedPacketLoss);
                        }
                    }
                }
            }
            if (logger.isInfoEnabled() && (this.numberOfReceivedSenderReports + this.numberOfReceivedReceiverReports) % 4 == 1) {
                StringBuilder buff = new StringBuilder(StatisticsEngine.RTP_STAT_PREFIX);
                MediaType mediaType = getMediaType();
                buff.append("Received a ").append(senderReport ? "sender" : "receiver").append(" report for ").append(mediaType == null ? "" : mediaType.toString()).append(" stream SSRC:").append(getLocalSourceID()).append(" [");
                if (senderReport) {
                    buff.append("packet count:").append(((SenderReport) report).getSenderPacketCount()).append(", bytes:").append(((SenderReport) report).getSenderByteCount());
                }
                if (feedback != null) {
                    buff.append(", interarrival jitter:").append(remoteJitter).append(", lost packets:").append(feedback.getNumLost()).append(", time since previous report:").append((int) (((double) feedback.getDLSR()) / 65.536d)).append("ms");
                }
                buff.append(" ]");
                logger.info(buff);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setLocalSourceID(long localSourceID) {
        if (this.localSourceID != localSourceID) {
            Long oldValue = Long.valueOf(this.localSourceID);
            this.localSourceID = localSourceID;
            TransformEngine transformEngine = this.srtpControl.getTransformEngine();
            if (transformEngine instanceof ZRTPTransformEngine) {
                ((ZRTPTransformEngine) transformEngine).setOwnSSRC(getLocalSourceID());
            }
            firePropertyChange(MediaStream.PNAME_LOCAL_SSRC, oldValue, Long.valueOf(this.localSourceID));
        }
    }

    /* access modifiers changed from: protected */
    public void addRemoteSourceID(long remoteSourceID) {
        Long oldValue = Long.valueOf(getRemoteSourceID());
        if (!this.remoteSourceIDs.contains(Long.valueOf(remoteSourceID))) {
            this.remoteSourceIDs.add(Long.valueOf(remoteSourceID));
        }
        firePropertyChange(MediaStream.PNAME_REMOTE_SSRC, oldValue, Long.valueOf(remoteSourceID));
    }

    public long[] getLocalContributingSourceIDs() {
        return this.localContributingSourceIDs;
    }

    public long[] getRemoteContributingSourceIDs() {
        return getDeviceSession().getRemoteSSRCList();
    }

    /* access modifiers changed from: protected */
    public int getPriority() {
        return Thread.currentThread().getPriority();
    }

    private void printFlowStatistics(StreamRTPManager rtpManager) {
        try {
            if (logger.isInfoEnabled()) {
                GlobalTransmissionStats s = rtpManager.getGlobalTransmissionStats();
                StringBuilder buff = new StringBuilder(StatisticsEngine.RTP_STAT_PREFIX);
                MediaType mediaType = getMediaType();
                buff.append("call stats for outgoing ").append(mediaType == null ? "" : mediaType.toString()).append(" stream SSRC:").append(getLocalSourceID()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("bytes sent: ").append(s.getBytesSent()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("RTP sent: ").append(s.getRTPSent()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("remote reported min interarrival jitter : ").append(this.minRemoteInterArrivalJitter).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("remote reported max interarrival jitter : ").append(this.maxRemoteInterArrivalJitter).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("local collisions: ").append(s.getLocalColls()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("remote collisions: ").append(s.getRemoteColls()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("RTCP sent: ").append(s.getRTCPSent()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("transmit failed: ").append(s.getTransmitFailed());
                logger.info(buff);
                GlobalReceptionStats rs = rtpManager.getGlobalReceptionStats();
                MediaFormat format = getFormat();
                buff = new StringBuilder(StatisticsEngine.RTP_STAT_PREFIX);
                StringBuilder append = buff.append("call stats for incoming ");
                if (format == null) {
                    format = "";
                }
                append.append(format).append(" stream SSRC:").append(getRemoteSourceID()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("packets received: ").append(rs.getPacketsRecd()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("bytes received: ").append(rs.getBytesRecd()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("packets lost: ").append(this.statisticsEngine.getLost()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("min interarrival jitter : ").append(this.statisticsEngine.getMinInterArrivalJitter()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("max interarrival jitter : ").append(this.statisticsEngine.getMaxInterArrivalJitter()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("RTCPs received: ").append(rs.getRTCPRecd()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("bad RTCP packets: ").append(rs.getBadRTCPPkts()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("bad RTP packets: ").append(rs.getBadRTPkts()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("local collisions: ").append(rs.getLocalColls()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("malformed BYEs: ").append(rs.getMalformedBye()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("malformed RRs: ").append(rs.getMalformedRR()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("malformed SDESs: ").append(rs.getMalformedSDES()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("malformed SRs: ").append(rs.getMalformedSR()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("packets looped: ").append(rs.getPacketsLooped()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("remote collisions: ").append(rs.getRemoteColls()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("SRRs received: ").append(rs.getSRRecd()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("transmit failed: ").append(rs.getTransmitFailed()).append("\n").append(StatisticsEngine.RTP_STAT_PREFIX).append("unknown types: ").append(rs.getUnknownTypes());
                logger.info(buff);
            }
        } catch (Throwable t) {
            logger.error("Error writing statistics", t);
        }
    }

    private void printReceiveStreamStatistics() {
        this.mediaStreamStatsImpl.updateStats();
        logger.info(new StringBuilder("\nReceive stream stats: discarded RTP packets: ").append(this.mediaStreamStatsImpl.getNbDiscarded()).append("\n").append("Receive stream stats: decoded with FEC: ").append(this.mediaStreamStatsImpl.getNbFec()));
    }

    public void setRTPTranslator(RTPTranslator rtpTranslator) {
        if (this.rtpTranslator != rtpTranslator) {
            this.rtpTranslator = rtpTranslator;
        }
    }

    public MediaStreamStats getMediaStreamStats() {
        return this.mediaStreamStatsImpl;
    }

    public MediaType getMediaType() {
        MediaFormat format = getFormat();
        MediaType mediaType = null;
        if (format != null) {
            mediaType = format.getMediaType();
        }
        if (mediaType != null) {
            return mediaType;
        }
        MediaDeviceSession deviceSession = getDeviceSession();
        if (deviceSession != null) {
            mediaType = deviceSession.getDevice().getMediaType();
        }
        if (mediaType != null) {
            return mediaType;
        }
        if (this instanceof AudioMediaStream) {
            return MediaType.AUDIO;
        }
        if (this instanceof VideoMediaStream) {
            return MediaType.VIDEO;
        }
        return mediaType;
    }

    public void addDynamicRTPPayloadTypeOverride(byte originalPt, byte overloadPt) {
        if (this.ptTransformEngine != null) {
            this.ptTransformEngine.addPTMappingOverride(originalPt, overloadPt);
        }
    }

    public void removeReceiveStreamForSsrc(long ssrc) {
        ReceiveStream toRemove = null;
        Iterator i$ = this.rtpManager.getReceiveStreams().iterator();
        while (i$.hasNext()) {
            ReceiveStream receiveStream = (ReceiveStream) i$.next();
            if ((receiveStream.getSSRC() & 4294967295L) == ssrc) {
                toRemove = receiveStream;
                break;
            }
        }
        if (toRemove != null) {
            synchronized (this.receiveStreams) {
                if (this.receiveStreams.remove(toRemove)) {
                    MediaDeviceSession deviceSession = getDeviceSession();
                    if (deviceSession != null) {
                        deviceSession.removeReceiveStream(toRemove);
                    }
                }
            }
        }
    }

    public void setSSRCFactory(SSRCFactory ssrcFactory) {
        if (this.ssrcFactory != ssrcFactory) {
            this.ssrcFactory = ssrcFactory;
            StreamRTPManager rtpManager = this.rtpManager;
            if (rtpManager != null) {
                rtpManager.setSSRCFactory(ssrcFactory);
            }
        }
    }
}
