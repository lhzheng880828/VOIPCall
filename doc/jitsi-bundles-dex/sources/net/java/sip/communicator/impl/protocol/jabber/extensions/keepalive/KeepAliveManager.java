package net.java.sip.communicator.impl.protocol.jabber.extensions.keepalive;

import java.util.Timer;
import java.util.TimerTask;
import net.java.sip.communicator.impl.protocol.jabber.ProtocolProviderServiceJabberImpl;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;

public class KeepAliveManager implements RegistrationStateChangeListener, PacketListener {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(KeepAliveManager.class);
    /* access modifiers changed from: private */
    public int keepAliveCheckInterval;
    private KeepAliveSendTask keepAliveSendTask = null;
    private Timer keepAliveTimer;
    /* access modifiers changed from: private */
    public long lastReceiveActivity = 0;
    /* access modifiers changed from: private */
    public ProtocolProviderServiceJabberImpl parentProvider = null;
    /* access modifiers changed from: private */
    public String waitingForPacketWithID = null;

    private class KeepAliveSendTask extends TimerTask {
        private final long MIN_WAKE_UP_INTERVAL;
        private long lastWakeUp;

        private KeepAliveSendTask() {
            this.MIN_WAKE_UP_INTERVAL = 5000;
        }

        public void run() {
            long sleepDuration = System.currentTimeMillis() - this.lastWakeUp;
            this.lastWakeUp = System.currentTimeMillis();
            if (sleepDuration < 5000) {
                KeepAliveManager.logger.error(this + " woken up too early !");
            } else if (!KeepAliveManager.this.parentProvider.isRegistered()) {
                if (KeepAliveManager.logger.isTraceEnabled()) {
                    KeepAliveManager.logger.trace("provider not registered. won't send keep alive for " + KeepAliveManager.this.parentProvider.getAccountID().getDisplayName());
                }
                KeepAliveManager.this.parentProvider.unregister(false);
                KeepAliveManager.this.parentProvider.fireRegistrationStateChanged(KeepAliveManager.this.parentProvider.getRegistrationState(), RegistrationState.CONNECTION_FAILED, 8, null);
            } else if (System.currentTimeMillis() - KeepAliveManager.this.lastReceiveActivity <= ((long) KeepAliveManager.this.keepAliveCheckInterval)) {
            } else {
                if (KeepAliveManager.this.waitingForPacketWithID != null) {
                    KeepAliveManager.logger.error("un-registering not received ping packet for: " + KeepAliveManager.this.parentProvider.getAccountID().getDisplayName());
                    KeepAliveManager.this.parentProvider.unregister(false);
                    KeepAliveManager.this.parentProvider.fireRegistrationStateChanged(KeepAliveManager.this.parentProvider.getRegistrationState(), RegistrationState.CONNECTION_FAILED, 8, null);
                    return;
                }
                try {
                    KeepAliveEvent ping = new KeepAliveEvent(KeepAliveManager.this.parentProvider.getOurJID(), StringUtils.parseServer(KeepAliveManager.this.parentProvider.getAccountID().getAccountAddress()));
                    if (KeepAliveManager.logger.isTraceEnabled()) {
                        KeepAliveManager.logger.trace("send keepalive for acc: " + KeepAliveManager.this.parentProvider.getAccountID().getDisplayName());
                    }
                    KeepAliveManager.this.waitingForPacketWithID = ping.getPacketID();
                    KeepAliveManager.this.parentProvider.getConnection().sendPacket(ping);
                } catch (Throwable t) {
                    KeepAliveManager.logger.error("Error sending ping request!", t);
                    KeepAliveManager.this.waitingForPacketWithID = null;
                }
            }
        }
    }

    public KeepAliveManager(ProtocolProviderServiceJabberImpl parentProvider) {
        this.parentProvider = parentProvider;
        this.parentProvider.addRegistrationStateChangeListener(this);
        ProviderManager.getInstance().addIQProvider(KeepAliveEvent.ELEMENT_NAME, KeepAliveEvent.NAMESPACE, new KeepAliveEventProvider());
    }

    public void registrationStateChanged(RegistrationStateChangeEvent evt) {
        if (logger.isDebugEnabled()) {
            logger.debug("The provider changed state from: " + evt.getOldState() + " to: " + evt.getNewState());
        }
        if (evt.getNewState() == RegistrationState.REGISTERED) {
            this.parentProvider.getConnection().removePacketListener(this);
            this.parentProvider.getConnection().addPacketListener(this, null);
            if (this.keepAliveSendTask != null) {
                logger.error("Those task is not supposed to be available for " + this.parentProvider.getAccountID().getDisplayName());
                this.keepAliveSendTask.cancel();
                this.keepAliveSendTask = null;
            }
            if (this.keepAliveTimer != null) {
                logger.error("Those timer is not supposed to be available for " + this.parentProvider.getAccountID().getDisplayName());
                this.keepAliveTimer.cancel();
                this.keepAliveTimer = null;
            }
            this.keepAliveSendTask = new KeepAliveSendTask();
            this.waitingForPacketWithID = null;
            this.keepAliveCheckInterval = SmackConfiguration.getKeepAliveInterval();
            if (this.keepAliveCheckInterval == 0) {
                this.keepAliveCheckInterval = 30000;
            }
            this.keepAliveTimer = new Timer("Jabber keepalive timer for <" + this.parentProvider.getAccountID() + Separators.GREATER_THAN, true);
            this.keepAliveTimer.scheduleAtFixedRate(this.keepAliveSendTask, (long) this.keepAliveCheckInterval, (long) this.keepAliveCheckInterval);
        } else if (evt.getNewState() == RegistrationState.UNREGISTERED || evt.getNewState() == RegistrationState.CONNECTION_FAILED || evt.getNewState() == RegistrationState.AUTHENTICATION_FAILED) {
            this.waitingForPacketWithID = null;
            if (this.parentProvider.getConnection() != null) {
                this.parentProvider.getConnection().removePacketListener(this);
            }
            if (this.keepAliveSendTask != null) {
                this.keepAliveSendTask.cancel();
                this.keepAliveSendTask = null;
            }
            if (this.keepAliveTimer != null) {
                this.keepAliveTimer.cancel();
                this.keepAliveTimer = null;
            }
        }
    }

    public void processPacket(Packet packet) {
        this.lastReceiveActivity = System.currentTimeMillis();
        if (this.waitingForPacketWithID != null && this.waitingForPacketWithID.equals(packet.getPacketID())) {
            this.waitingForPacketWithID = null;
        }
        if (packet instanceof KeepAliveEvent) {
            KeepAliveEvent evt = (KeepAliveEvent) packet;
            if (evt.getFrom() != null && evt.getFrom().equals(this.parentProvider.getAccountID().getService())) {
                this.parentProvider.getConnection().sendPacket(IQ.createResultIQ(evt));
            }
        }
    }
}
