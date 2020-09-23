package org.jivesoftware.smackx;

import java.util.ArrayList;
import java.util.List;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.packet.PEPEvent;
import org.jivesoftware.smackx.packet.PEPItem;
import org.jivesoftware.smackx.packet.PEPPubSub;

public class PEPManager {
    private Connection connection;
    private PacketFilter packetFilter = new PacketExtensionFilter("event", "http://jabber.org/protocol/pubsub#event");
    private PacketListener packetListener;
    private List<PEPListener> pepListeners = new ArrayList();

    public PEPManager(Connection connection) {
        this.connection = connection;
        init();
    }

    public void addPEPListener(PEPListener pepListener) {
        synchronized (this.pepListeners) {
            if (!this.pepListeners.contains(pepListener)) {
                this.pepListeners.add(pepListener);
            }
        }
    }

    public void removePEPListener(PEPListener pepListener) {
        synchronized (this.pepListeners) {
            this.pepListeners.remove(pepListener);
        }
    }

    public void publish(PEPItem item) {
        PEPPubSub pubSub = new PEPPubSub(item);
        pubSub.setType(Type.SET);
        this.connection.sendPacket(pubSub);
    }

    /* access modifiers changed from: private */
    public void firePEPListeners(String from, PEPEvent event) {
        PEPListener[] listeners;
        synchronized (this.pepListeners) {
            listeners = new PEPListener[this.pepListeners.size()];
            this.pepListeners.toArray(listeners);
        }
        for (PEPListener eventReceived : listeners) {
            eventReceived.eventReceived(from, event);
        }
    }

    private void init() {
        this.packetListener = new PacketListener() {
            public void processPacket(Packet packet) {
                Message message = (Message) packet;
                PEPManager.this.firePEPListeners(message.getFrom(), (PEPEvent) message.getExtension("event", "http://jabber.org/protocol/pubsub#event"));
            }
        };
        this.connection.addPacketListener(this.packetListener, this.packetFilter);
    }

    public void destroy() {
        if (this.connection != null) {
            this.connection.removePacketListener(this.packetListener);
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        destroy();
        super.finalize();
    }
}
