package net.java.sip.communicator.impl.provdisc.dhcp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import net.java.sip.communicator.util.Logger;

public class DHCPTransaction {
    /* access modifiers changed from: private */
    public int interval = 2;
    /* access modifiers changed from: private|final */
    public final Logger logger = Logger.getLogger(DHCPTransaction.class);
    /* access modifiers changed from: private */
    public int maxRetransmit = 2;
    /* access modifiers changed from: private|final */
    public final DatagramPacket message;
    /* access modifiers changed from: private */
    public int nbRetransmit = 0;
    /* access modifiers changed from: private|final */
    public final DatagramSocket sock;
    /* access modifiers changed from: private */
    public Timer timer = null;

    private class RetransmissionHandler extends TimerTask {
        private RetransmissionHandler() {
        }

        public void run() {
            int rand = new Random().nextInt(2) - 1;
            try {
                DHCPTransaction.this.sock.send(DHCPTransaction.this.message);
            } catch (Exception e) {
                DHCPTransaction.this.logger.warn("Failed to send DHCP packet", e);
            }
            DHCPTransaction.this.nbRetransmit = DHCPTransaction.this.nbRetransmit + 1;
            if (DHCPTransaction.this.nbRetransmit < DHCPTransaction.this.maxRetransmit) {
                DHCPTransaction.this.timer.schedule(new RetransmissionHandler(), (long) ((DHCPTransaction.this.interval + rand) * 1000));
            }
        }
    }

    public DHCPTransaction(DatagramSocket sock, DatagramPacket message) {
        this.sock = sock;
        this.message = message;
        this.timer = new Timer();
    }

    public void schedule() throws Exception {
        this.sock.send(this.message);
        this.timer.schedule(new RetransmissionHandler(), (long) ((this.interval + (new Random().nextInt(2) - 1)) * 1000));
    }

    public void cancel() {
        this.timer.cancel();
    }

    public void setMaxRetransmit(int maxRetransmit) {
        this.maxRetransmit = maxRetransmit;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
}
