package javax.media.rtp;

import java.io.IOException;
import java.util.Vector;
import javax.media.Controls;
import javax.media.Format;
import javax.media.format.UnsupportedFormatException;
import javax.media.protocol.DataSource;
import javax.media.rtp.rtcp.SourceDescription;

@Deprecated
public interface SessionManager extends Controls {
    public static final long SSRC_UNSPEC = 0;

    void addFormat(Format format, int i);

    void addPeer(SessionAddress sessionAddress) throws IOException, InvalidSessionAddressException;

    void addReceiveStreamListener(ReceiveStreamListener receiveStreamListener);

    void addRemoteListener(RemoteListener remoteListener);

    void addSendStreamListener(SendStreamListener sendStreamListener);

    void addSessionListener(SessionListener sessionListener);

    void closeSession(String str);

    SendStream createSendStream(int i, DataSource dataSource, int i2) throws UnsupportedFormatException, SSRCInUseException, IOException;

    SendStream createSendStream(DataSource dataSource, int i) throws UnsupportedFormatException, IOException;

    String generateCNAME();

    long generateSSRC();

    Vector getActiveParticipants();

    Vector getAllParticipants();

    long getDefaultSSRC();

    GlobalReceptionStats getGlobalReceptionStats();

    GlobalTransmissionStats getGlobalTransmissionStats();

    LocalParticipant getLocalParticipant();

    SessionAddress getLocalSessionAddress();

    int getMulticastScope();

    Vector getPassiveParticipants();

    Vector getPeers();

    Vector getReceiveStreams();

    Vector getRemoteParticipants();

    Vector getSendStreams();

    SessionAddress getSessionAddress();

    RTPStream getStream(long j);

    int initSession(SessionAddress sessionAddress, long j, SourceDescription[] sourceDescriptionArr, double d, double d2) throws InvalidSessionAddressException;

    int initSession(SessionAddress sessionAddress, SourceDescription[] sourceDescriptionArr, double d, double d2) throws InvalidSessionAddressException;

    void removeAllPeers();

    void removePeer(SessionAddress sessionAddress);

    void removeReceiveStreamListener(ReceiveStreamListener receiveStreamListener);

    void removeRemoteListener(RemoteListener remoteListener);

    void removeSendStreamListener(SendStreamListener sendStreamListener);

    void removeSessionListener(SessionListener sessionListener);

    void setMulticastScope(int i);

    int startSession(int i, EncryptionInfo encryptionInfo) throws IOException;

    int startSession(SessionAddress sessionAddress, int i, EncryptionInfo encryptionInfo) throws IOException, InvalidSessionAddressException;

    int startSession(SessionAddress sessionAddress, SessionAddress sessionAddress2, SessionAddress sessionAddress3, EncryptionInfo encryptionInfo) throws IOException, InvalidSessionAddressException;
}
