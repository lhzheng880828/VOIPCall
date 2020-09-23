package org.jivesoftware.smackx.bytestreams.ibb;

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
import org.jivesoftware.smackx.bytestreams.ibb.packet.Open;

class InitiationListener implements PacketListener {
    private final PacketFilter initFilter = new AndFilter(new PacketTypeFilter(Open.class), new IQTypeFilter(Type.SET));
    private final ExecutorService initiationListenerExecutor;
    private final InBandBytestreamManager manager;

    protected InitiationListener(InBandBytestreamManager manager) {
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
        Open ibbRequest = (Open) packet;
        if (ibbRequest.getBlockSize() > this.manager.getMaximumBlockSize()) {
            this.manager.replyResourceConstraintPacket(ibbRequest);
        } else if (!this.manager.getIgnoredBytestreamRequests().remove(ibbRequest.getSessionID())) {
            InBandBytestreamRequest request = new InBandBytestreamRequest(this.manager, ibbRequest);
            BytestreamListener userListener = this.manager.getUserListener(ibbRequest.getFrom());
            if (userListener != null) {
                userListener.incomingBytestreamRequest(request);
            } else if (this.manager.getAllRequestListeners().isEmpty()) {
                this.manager.replyRejectPacket(ibbRequest);
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
