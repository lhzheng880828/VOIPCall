package javax.media.rtp;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.Controls;
import javax.media.Format;
import javax.media.PackageManager;
import javax.media.format.UnsupportedFormatException;
import javax.media.protocol.DataSource;
import javax.media.rtp.rtcp.SourceDescription;
import net.sf.fmj.utility.LoggerSingleton;

public abstract class RTPManager implements Controls {
    private static final Logger logger = LoggerSingleton.logger;

    public abstract void addFormat(Format format, int i);

    public abstract void addReceiveStreamListener(ReceiveStreamListener receiveStreamListener);

    public abstract void addRemoteListener(RemoteListener remoteListener);

    public abstract void addSendStreamListener(SendStreamListener sendStreamListener);

    public abstract void addSessionListener(SessionListener sessionListener);

    public abstract void addTarget(SessionAddress sessionAddress) throws InvalidSessionAddressException, IOException;

    public abstract SendStream createSendStream(DataSource dataSource, int i) throws UnsupportedFormatException, IOException;

    public abstract void dispose();

    public abstract Vector getActiveParticipants();

    public abstract Vector getAllParticipants();

    public abstract GlobalReceptionStats getGlobalReceptionStats();

    public abstract GlobalTransmissionStats getGlobalTransmissionStats();

    public abstract LocalParticipant getLocalParticipant();

    public abstract Vector getPassiveParticipants();

    public abstract Vector getReceiveStreams();

    public abstract Vector getRemoteParticipants();

    public abstract Vector getSendStreams();

    public abstract void initialize(RTPConnector rTPConnector);

    public abstract void initialize(SessionAddress sessionAddress) throws InvalidSessionAddressException, IOException;

    public abstract void initialize(SessionAddress[] sessionAddressArr, SourceDescription[] sourceDescriptionArr, double d, double d2, EncryptionInfo encryptionInfo) throws InvalidSessionAddressException, IOException;

    public abstract void removeReceiveStreamListener(ReceiveStreamListener receiveStreamListener);

    public abstract void removeRemoteListener(RemoteListener remoteListener);

    public abstract void removeSendStreamListener(SendStreamListener sendStreamListener);

    public abstract void removeSessionListener(SessionListener sessionListener);

    public abstract void removeTarget(SessionAddress sessionAddress, String str) throws InvalidSessionAddressException;

    public abstract void removeTargets(String str);

    public static Vector<String> getRTPManagerList() {
        Vector<String> result = new Vector();
        Iterator i$ = PackageManager.getProtocolPrefixList().iterator();
        while (i$.hasNext()) {
            result.add(i$.next() + ".media.rtp.RTPSessionMgr");
        }
        return result;
    }

    public static RTPManager newInstance() {
        Iterator i$ = getRTPManagerList().iterator();
        while (i$.hasNext()) {
            String className = (String) i$.next();
            try {
                logger.finer("Trying RTPManager class: " + className);
                return (RTPManager) Class.forName(className).newInstance();
            } catch (ClassNotFoundException e) {
                logger.finer("RTPManager.newInstance: ClassNotFoundException: " + className);
            } catch (Exception e2) {
                logger.log(Level.WARNING, "" + e2, e2);
            }
        }
        return null;
    }
}
