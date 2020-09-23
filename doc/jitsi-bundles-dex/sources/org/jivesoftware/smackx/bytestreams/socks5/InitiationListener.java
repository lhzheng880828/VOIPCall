package org.jivesoftware.smackx.bytestreams.socks5;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.bytestreams.BytestreamListener;
import org.jivesoftware.smackx.bytestreams.socks5.packet.Bytestream;

final class InitiationListener implements PacketListener {
    private final PacketFilter initFilter = new AndFilter(new PacketTypeFilter(Bytestream.class), new IQTypeFilter(Type.SET));
    private final ExecutorService initiationListenerExecutor;
    private final Socks5BytestreamManager manager;

    protected InitiationListener(Socks5BytestreamManager manager) {
        this.manager = manager;
        this.initiationListenerExecutor = Executors.newCachedThreadPool();
    }

    public void processPacket(final Packet packet) {
        this.initiationListenerExecutor.execute(new Runnable() {
            public void run() {
                InitiationListener.this.processRequest(packet);
            }
        });
    }

    /* access modifiers changed from: private */
    public void processRequest(Packet packet) {
        Bytestream byteStreamRequest = (Bytestream) packet;
        if (!this.manager.getIgnoredBytestreamRequests().remove(byteStreamRequest.getSessionID())) {
            Socks5BytestreamRequest request = new Socks5BytestreamRequest(this.manager, byteStreamRequest);
            BytestreamListener userListener = this.manager.getUserListener(byteStreamRequest.getFrom());
            if (userListener != null) {
                userListener.incomingBytestreamRequest(request);
            } else if (this.manager.getAllRequestListeners().isEmpty()) {
                this.manager.replyRejectPacket(byteStreamRequest);
            } else {
                for (BytestreamListener listener : this.manager.getAllRequestListeners()) {
                    listener.incomingBytestreamRequest(request);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public PacketFilter getFilter() {
        return this.initFilter;
    }

    /* access modifiers changed from: protected */
    public void shutdown() {
        this.initiationListenerExecutor.shutdownNow();
    }
}
