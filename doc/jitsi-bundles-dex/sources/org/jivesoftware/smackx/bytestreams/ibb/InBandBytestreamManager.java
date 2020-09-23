package org.jivesoftware.smackx.bytestreams.ibb;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.XMPPError.Condition;
import org.jivesoftware.smack.util.SyncPacketSend;
import org.jivesoftware.smackx.bytestreams.BytestreamListener;
import org.jivesoftware.smackx.bytestreams.BytestreamManager;
import org.jivesoftware.smackx.bytestreams.ibb.packet.Open;

public class InBandBytestreamManager implements BytestreamManager {
    public static final int MAXIMUM_BLOCK_SIZE = 65535;
    public static final String NAMESPACE = "http://jabber.org/protocol/ibb";
    private static final String SESSION_ID_PREFIX = "jibb_";
    private static final Map<Connection, InBandBytestreamManager> managers = new HashMap();
    private static final Random randomGenerator = new Random();
    private final List<BytestreamListener> allRequestListeners = Collections.synchronizedList(new LinkedList());
    private final CloseListener closeListener;
    private final Connection connection;
    private final DataListener dataListener;
    private int defaultBlockSize = 4096;
    private List<String> ignoredBytestreamRequests = Collections.synchronizedList(new LinkedList());
    private final InitiationListener initiationListener;
    private int maximumBlockSize = MAXIMUM_BLOCK_SIZE;
    private final Map<String, InBandBytestreamSession> sessions = new ConcurrentHashMap();
    private StanzaType stanza = StanzaType.IQ;
    private final Map<String, BytestreamListener> userListeners = new ConcurrentHashMap();

    public enum StanzaType {
        IQ,
        MESSAGE
    }

    static {
        Connection.addConnectionCreationListener(new ConnectionCreationListener() {
            public void connectionCreated(Connection connection) {
                final InBandBytestreamManager manager = InBandBytestreamManager.getByteStreamManager(connection);
                connection.addConnectionListener(new AbstractConnectionListener() {
                    public void connectionClosed() {
                        manager.disableService();
                    }
                });
            }
        });
    }

    public static synchronized InBandBytestreamManager getByteStreamManager(Connection connection) {
        InBandBytestreamManager manager;
        synchronized (InBandBytestreamManager.class) {
            if (connection == null) {
                manager = null;
            } else {
                manager = (InBandBytestreamManager) managers.get(connection);
                if (manager == null) {
                    manager = new InBandBytestreamManager(connection);
                    managers.put(connection, manager);
                }
            }
        }
        return manager;
    }

    private InBandBytestreamManager(Connection connection) {
        this.connection = connection;
        this.initiationListener = new InitiationListener(this);
        this.connection.addPacketListener(this.initiationListener, this.initiationListener.getFilter());
        this.dataListener = new DataListener(this);
        this.connection.addPacketListener(this.dataListener, this.dataListener.getFilter());
        this.closeListener = new CloseListener(this);
        this.connection.addPacketListener(this.closeListener, this.closeListener.getFilter());
    }

    public void addIncomingBytestreamListener(BytestreamListener listener) {
        this.allRequestListeners.add(listener);
    }

    public void removeIncomingBytestreamListener(BytestreamListener listener) {
        this.allRequestListeners.remove(listener);
    }

    public void addIncomingBytestreamListener(BytestreamListener listener, String initiatorJID) {
        this.userListeners.put(initiatorJID, listener);
    }

    public void removeIncomingBytestreamListener(String initiatorJID) {
        this.userListeners.remove(initiatorJID);
    }

    public void ignoreBytestreamRequestOnce(String sessionID) {
        this.ignoredBytestreamRequests.add(sessionID);
    }

    public int getDefaultBlockSize() {
        return this.defaultBlockSize;
    }

    public void setDefaultBlockSize(int defaultBlockSize) {
        if (defaultBlockSize <= 0 || defaultBlockSize > MAXIMUM_BLOCK_SIZE) {
            throw new IllegalArgumentException("Default block size must be between 1 and 65535");
        }
        this.defaultBlockSize = defaultBlockSize;
    }

    public int getMaximumBlockSize() {
        return this.maximumBlockSize;
    }

    public void setMaximumBlockSize(int maximumBlockSize) {
        if (maximumBlockSize <= 0 || maximumBlockSize > MAXIMUM_BLOCK_SIZE) {
            throw new IllegalArgumentException("Maximum block size must be between 1 and 65535");
        }
        this.maximumBlockSize = maximumBlockSize;
    }

    public StanzaType getStanza() {
        return this.stanza;
    }

    public void setStanza(StanzaType stanza) {
        this.stanza = stanza;
    }

    public InBandBytestreamSession establishSession(String targetJID) throws XMPPException {
        return establishSession(targetJID, getNextSessionID());
    }

    public InBandBytestreamSession establishSession(String targetJID, String sessionID) throws XMPPException {
        Open byteStreamRequest = new Open(sessionID, this.defaultBlockSize, this.stanza);
        byteStreamRequest.setTo(targetJID);
        SyncPacketSend.getReply(this.connection, byteStreamRequest);
        InBandBytestreamSession inBandBytestreamSession = new InBandBytestreamSession(this.connection, byteStreamRequest, targetJID);
        this.sessions.put(sessionID, inBandBytestreamSession);
        return inBandBytestreamSession;
    }

    /* access modifiers changed from: protected */
    public void replyRejectPacket(IQ request) {
        this.connection.sendPacket(IQ.createErrorResponse(request, new XMPPError(Condition.no_acceptable)));
    }

    /* access modifiers changed from: protected */
    public void replyResourceConstraintPacket(IQ request) {
        this.connection.sendPacket(IQ.createErrorResponse(request, new XMPPError(Condition.resource_constraint)));
    }

    /* access modifiers changed from: protected */
    public void replyItemNotFoundPacket(IQ request) {
        this.connection.sendPacket(IQ.createErrorResponse(request, new XMPPError(Condition.item_not_found)));
    }

    private String getNextSessionID() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(SESSION_ID_PREFIX);
        buffer.append(Math.abs(randomGenerator.nextLong()));
        return buffer.toString();
    }

    /* access modifiers changed from: protected */
    public Connection getConnection() {
        return this.connection;
    }

    /* access modifiers changed from: protected */
    public BytestreamListener getUserListener(String initiator) {
        return (BytestreamListener) this.userListeners.get(initiator);
    }

    /* access modifiers changed from: protected */
    public List<BytestreamListener> getAllRequestListeners() {
        return this.allRequestListeners;
    }

    /* access modifiers changed from: protected */
    public Map<String, InBandBytestreamSession> getSessions() {
        return this.sessions;
    }

    /* access modifiers changed from: protected */
    public List<String> getIgnoredBytestreamRequests() {
        return this.ignoredBytestreamRequests;
    }

    /* access modifiers changed from: private */
    public void disableService() {
        managers.remove(this.connection);
        this.connection.removePacketListener(this.initiationListener);
        this.connection.removePacketListener(this.dataListener);
        this.connection.removePacketListener(this.closeListener);
        this.initiationListener.shutdown();
        this.userListeners.clear();
        this.allRequestListeners.clear();
        this.sessions.clear();
        this.ignoredBytestreamRequests.clear();
    }
}
