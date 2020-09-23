package org.jivesoftware.smackx.bytestreams.socks5;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.TimeoutException;
import net.java.sip.communicator.impl.contactlist.MetaContactListServiceImpl;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.XMPPError.Condition;
import org.jivesoftware.smack.util.Cache;
import org.jivesoftware.smackx.bytestreams.BytestreamRequest;
import org.jivesoftware.smackx.bytestreams.socks5.packet.Bytestream;
import org.jivesoftware.smackx.bytestreams.socks5.packet.Bytestream.StreamHost;

public class Socks5BytestreamRequest implements BytestreamRequest {
    private static final Cache<String, Integer> ADDRESS_BLACKLIST = new Cache(100, BLACKLIST_LIFETIME);
    private static final long BLACKLIST_LIFETIME = 7200000;
    private static final int BLACKLIST_MAX_SIZE = 100;
    private static int CONNECTION_FAILURE_THRESHOLD = 2;
    private Bytestream bytestreamRequest;
    private Socks5BytestreamManager manager;
    private int minimumConnectTimeout = 2000;
    private int totalConnectTimeout = MetaContactListServiceImpl.CONTACT_LIST_MODIFICATION_TIMEOUT;

    public static int getConnectFailureThreshold() {
        return CONNECTION_FAILURE_THRESHOLD;
    }

    public static void setConnectFailureThreshold(int connectFailureThreshold) {
        CONNECTION_FAILURE_THRESHOLD = connectFailureThreshold;
    }

    protected Socks5BytestreamRequest(Socks5BytestreamManager manager, Bytestream bytestreamRequest) {
        this.manager = manager;
        this.bytestreamRequest = bytestreamRequest;
    }

    public int getTotalConnectTimeout() {
        if (this.totalConnectTimeout <= 0) {
            return MetaContactListServiceImpl.CONTACT_LIST_MODIFICATION_TIMEOUT;
        }
        return this.totalConnectTimeout;
    }

    public void setTotalConnectTimeout(int totalConnectTimeout) {
        this.totalConnectTimeout = totalConnectTimeout;
    }

    public int getMinimumConnectTimeout() {
        if (this.minimumConnectTimeout <= 0) {
            return 2000;
        }
        return this.minimumConnectTimeout;
    }

    public void setMinimumConnectTimeout(int minimumConnectTimeout) {
        this.minimumConnectTimeout = minimumConnectTimeout;
    }

    public String getFrom() {
        return this.bytestreamRequest.getFrom();
    }

    public String getSessionID() {
        return this.bytestreamRequest.getSessionID();
    }

    public Socks5BytestreamSession accept() throws XMPPException, InterruptedException {
        Collection<StreamHost> streamHosts = this.bytestreamRequest.getStreamHosts();
        if (streamHosts.size() == 0) {
            cancelRequest();
        }
        StreamHost selectedHost = null;
        Socket socket = null;
        String digest = Socks5Utils.createDigest(this.bytestreamRequest.getSessionID(), this.bytestreamRequest.getFrom(), this.manager.getConnection().getUser());
        int timeout = Math.max(getTotalConnectTimeout() / streamHosts.size(), getMinimumConnectTimeout());
        for (StreamHost streamHost : streamHosts) {
            String address = streamHost.getAddress() + Separators.COLON + streamHost.getPort();
            int failures = getConnectionFailures(address);
            if (CONNECTION_FAILURE_THRESHOLD <= 0 || failures < CONNECTION_FAILURE_THRESHOLD) {
                try {
                    socket = new Socks5Client(streamHost, digest).getSocket(timeout);
                    selectedHost = streamHost;
                    break;
                } catch (TimeoutException e) {
                    incrementConnectionFailures(address);
                } catch (IOException e2) {
                    incrementConnectionFailures(address);
                } catch (XMPPException e3) {
                    incrementConnectionFailures(address);
                }
            }
        }
        if (selectedHost == null || socket == null) {
            cancelRequest();
        }
        this.manager.getConnection().sendPacket(createUsedHostResponse(selectedHost));
        return new Socks5BytestreamSession(socket, selectedHost.getJID().equals(this.bytestreamRequest.getFrom()));
    }

    public void reject() {
        this.manager.replyRejectPacket(this.bytestreamRequest);
    }

    private void cancelRequest() throws XMPPException {
        String errorMessage = "Could not establish socket with any provided host";
        XMPPError error = new XMPPError(Condition.item_not_found, errorMessage);
        this.manager.getConnection().sendPacket(IQ.createErrorResponse(this.bytestreamRequest, error));
        throw new XMPPException(errorMessage, error);
    }

    private Bytestream createUsedHostResponse(StreamHost selectedHost) {
        Bytestream response = new Bytestream(this.bytestreamRequest.getSessionID());
        response.setTo(this.bytestreamRequest.getFrom());
        response.setType(Type.RESULT);
        response.setPacketID(this.bytestreamRequest.getPacketID());
        response.setUsedHost(selectedHost.getJID());
        return response;
    }

    private void incrementConnectionFailures(String address) {
        Integer count = (Integer) ADDRESS_BLACKLIST.get(address);
        ADDRESS_BLACKLIST.put(address, Integer.valueOf(count == null ? 1 : count.intValue() + 1));
    }

    private int getConnectionFailures(String address) {
        Integer count = (Integer) ADDRESS_BLACKLIST.get(address);
        return count != null ? count.intValue() : 0;
    }
}
