package org.jitsi.service.neomedia;

import java.beans.PropertyChangeListener;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import org.jitsi.service.neomedia.StreamConnector.Protocol;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.format.MediaFormat;

public interface MediaStream {
    public static final String PNAME_LOCAL_SSRC = "localSSRCAvailable";
    public static final String PNAME_REMOTE_SSRC = "remoteSSRCAvailable";

    void addDynamicRTPPayloadType(byte b, MediaFormat mediaFormat);

    void addDynamicRTPPayloadTypeOverride(byte b, byte b2);

    void addPropertyChangeListener(PropertyChangeListener propertyChangeListener);

    void addRTPExtension(byte b, RTPExtension rTPExtension);

    void close();

    Map<Byte, RTPExtension> getActiveRTPExtensions();

    MediaDevice getDevice();

    MediaDirection getDirection();

    Map<Byte, MediaFormat> getDynamicRTPPayloadTypes();

    MediaFormat getFormat();

    long getLocalSourceID();

    MediaStreamStats getMediaStreamStats();

    String getName();

    InetSocketAddress getRemoteControlAddress();

    InetSocketAddress getRemoteDataAddress();

    long getRemoteSourceID();

    List<Long> getRemoteSourceIDs();

    SrtpControl getSrtpControl();

    MediaStreamTarget getTarget();

    Protocol getTransportProtocol();

    boolean isMute();

    boolean isStarted();

    void removePropertyChangeListener(PropertyChangeListener propertyChangeListener);

    void removeReceiveStreamForSsrc(long j);

    void setConnector(StreamConnector streamConnector);

    void setDevice(MediaDevice mediaDevice);

    void setDirection(MediaDirection mediaDirection);

    void setFormat(MediaFormat mediaFormat);

    void setMute(boolean z);

    void setName(String str);

    void setRTPTranslator(RTPTranslator rTPTranslator);

    void setSSRCFactory(SSRCFactory sSRCFactory);

    void setTarget(MediaStreamTarget mediaStreamTarget);

    void start();

    void stop();
}
