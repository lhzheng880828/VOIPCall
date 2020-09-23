package net.java.sip.communicator.service.netaddr;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import net.java.sip.communicator.service.netaddr.event.NetworkConfigurationChangeListener;
import org.ice4j.ice.Agent;
import org.ice4j.ice.IceMediaStream;
import org.ice4j.ice.harvest.StunCandidateHarvester;

public interface NetworkAddressManagerService {
    public static final int BIND_RETRIES_DEFAULT_VALUE = 50;
    public static final String BIND_RETRIES_PROPERTY_NAME = "net.java.sip.communicator.service.netaddr.BIND_RETRIES";

    void addNetworkConfigurationChangeListener(NetworkConfigurationChangeListener networkConfigurationChangeListener);

    DatagramSocket createDatagramSocket(InetAddress inetAddress, int i, int i2, int i3) throws IllegalArgumentException, IOException, BindException;

    Agent createIceAgent();

    IceMediaStream createIceStream(int i, String str, Agent agent) throws IllegalArgumentException, IOException, BindException;

    StunCandidateHarvester discoverStunServer(String str, byte[] bArr, byte[] bArr2);

    byte[] getHardwareAddress(NetworkInterface networkInterface);

    InetAddress getLocalHost(InetAddress inetAddress);

    InetSocketAddress getPublicAddressFor(InetAddress inetAddress, int i) throws IOException, BindException;

    void removeNetworkConfigurationChangeListener(NetworkConfigurationChangeListener networkConfigurationChangeListener);
}
