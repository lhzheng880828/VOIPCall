package net.java.sip.communicator.impl.provdisc.dhcp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import net.java.sip.communicator.service.netaddr.NetworkAddressManagerService;
import net.java.sip.communicator.service.provdisc.event.DiscoveryEvent;
import net.java.sip.communicator.service.provdisc.event.DiscoveryListener;
import net.java.sip.communicator.util.Logger;
import org.dhcp4java.DHCPConstants;
import org.dhcp4java.DHCPOption;
import org.dhcp4java.DHCPPacket;

public class DHCPProvisioningDiscover implements Runnable {
    private static final int DHCP_TIMEOUT = 10000;
    private List<DiscoveryListener> listeners = new ArrayList();
    private final Logger logger = Logger.getLogger(DHCPProvisioningDiscover.class);
    private byte option = (byte) -32;
    private int port = 6768;
    private DatagramSocket socket = null;
    private int xid = 0;

    public DHCPProvisioningDiscover(int port, byte option) throws Exception {
        this.port = port;
        this.option = option;
        this.socket = new DatagramSocket(port);
        this.xid = new Random().nextInt();
        this.socket.setSoTimeout(10000);
    }

    public String discoverProvisioningURL() {
        DHCPPacket inform = new DHCPPacket();
        byte[] bArr = new byte[4];
        bArr = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0};
        int i = 4;
        byte[] broadcastIPAddr = new byte[]{(byte) -1, (byte) -1, (byte) -1, (byte) -1};
        DHCPOption[] dhcpOpts = new DHCPOption[1];
        List<DHCPTransaction> transactions = new ArrayList();
        try {
            inform.setOp((byte) 1);
            inform.setHtype((byte) 1);
            inform.setHlen((byte) 6);
            inform.setHops((byte) 0);
            inform.setXid(this.xid);
            inform.setSecs((short) 0);
            inform.setFlags((short) 0);
            inform.setYiaddr(InetAddress.getByAddress(bArr));
            inform.setSiaddr(InetAddress.getByAddress(bArr));
            inform.setGiaddr(InetAddress.getByAddress(bArr));
            inform.setDhcp(true);
            inform.setDHCPMessageType((byte) 8);
            dhcpOpts[0] = new DHCPOption(DHCPConstants.DHO_DHCP_PARAMETER_REQUEST_LIST, new byte[]{this.option});
            inform.setOptions(dhcpOpts);
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface iface = (NetworkInterface) en.nextElement();
                Enumeration<InetAddress> enAddr = iface.getInetAddresses();
                while (enAddr.hasMoreElements()) {
                    InetAddress addr = (InetAddress) enAddr.nextElement();
                    if (addr instanceof Inet4Address) {
                        NetworkAddressManagerService netaddr = ProvisioningDiscoveryDHCPActivator.getNetworkAddressManagerService();
                        if (!addr.isLoopbackAddress()) {
                            byte[] macAddress = netaddr.getHardwareAddress(iface);
                            DHCPPacket p = inform.clone();
                            p.setCiaddr(addr);
                            p.setChaddr(macAddress);
                            byte[] msg = p.serialize();
                            DHCPTransaction dHCPTransaction = new DHCPTransaction(this.socket, new DatagramPacket(msg, msg.length, InetAddress.getByAddress(broadcastIPAddr), this.port - 1));
                            dHCPTransaction.schedule();
                            transactions.add(dHCPTransaction);
                        }
                    }
                }
            }
            try {
                DatagramPacket datagramPacket = new DatagramPacket(new byte[1500], 1500);
                while (!false) {
                    this.socket.receive(datagramPacket);
                    DHCPPacket dhcp = DHCPPacket.getPacket(datagramPacket);
                    if (dhcp.getXid() == this.xid) {
                        DHCPOption optProvisioning = dhcp.getOption(this.option);
                        if (optProvisioning != null) {
                            for (DHCPTransaction t : transactions) {
                                t.cancel();
                            }
                            return new String(optProvisioning.getValue());
                        }
                    }
                }
            } catch (SocketTimeoutException est) {
                this.logger.warn("Timeout, no DHCP answer received", est);
            }
        } catch (Exception e) {
            this.logger.warn("Exception occurred during DHCP discover", e);
        }
        for (DHCPTransaction t2 : transactions) {
            t2.cancel();
        }
        return null;
    }

    public void run() {
        String url = discoverProvisioningURL();
        if (url != null) {
            DiscoveryEvent evt = new DiscoveryEvent(this, url);
            for (DiscoveryListener listener : this.listeners) {
                listener.notifyProvisioningURL(evt);
            }
        }
    }

    public void addDiscoveryListener(DiscoveryListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public void removeDiscoveryListener(DiscoveryListener listener) {
        if (this.listeners.contains(listener)) {
            this.listeners.remove(listener);
        }
    }
}
