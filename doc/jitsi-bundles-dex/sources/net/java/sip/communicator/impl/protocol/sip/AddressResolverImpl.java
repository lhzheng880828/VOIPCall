package net.java.sip.communicator.impl.protocol.sip;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.NetworkUtils;
import net.java.sip.communicator.util.SRVRecord;
import org.jitsi.gov.nist.core.net.AddressResolver;
import org.jitsi.gov.nist.javax.sip.stack.HopImpl;
import org.jitsi.gov.nist.javax.sip.stack.MessageProcessor;
import org.jitsi.javax.sip.ListeningPoint;
import org.jitsi.javax.sip.address.Hop;

public class AddressResolverImpl implements AddressResolver {
    private static final Logger logger = Logger.getLogger(AddressResolverImpl.class);

    public Hop resolveAddress(Hop inputAddress) {
        try {
            String transport = inputAddress.getTransport();
            String hostAddress = inputAddress.getHost();
            if (transport == null) {
                transport = ListeningPoint.UDP;
            }
            String host = null;
            int port = 0;
            if (NetworkUtils.isValidIPAddress(hostAddress)) {
                byte[] addr = NetworkUtils.strToIPv4(hostAddress);
                if (addr == null) {
                    addr = NetworkUtils.strToIPv6(hostAddress);
                }
                return new HopImpl(new InetSocketAddress(InetAddress.getByAddress(hostAddress, addr), inputAddress.getPort()).getHostName(), inputAddress.getPort(), transport);
            }
            Hop returnHop;
            SRVRecord srvRecord;
            if (transport.equalsIgnoreCase(ListeningPoint.TLS)) {
                srvRecord = NetworkUtils.getSRVRecord("sips", ListeningPoint.TCP, hostAddress);
                if (srvRecord != null) {
                    host = srvRecord.getTarget();
                    port = srvRecord.getPort();
                }
            } else {
                srvRecord = NetworkUtils.getSRVRecord("sip", transport, hostAddress);
                if (srvRecord != null) {
                    host = srvRecord.getTarget();
                    port = srvRecord.getPort();
                }
            }
            if (host != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Returning hop as follows host= " + host + " port= " + port + " transport= " + transport);
                }
                return new HopImpl(host, port, transport);
            }
            if (inputAddress.getPort() != -1) {
                returnHop = inputAddress;
            } else {
                transport = inputAddress.getTransport();
                returnHop = new HopImpl(inputAddress.getHost(), MessageProcessor.getDefaultPort(transport), transport);
            }
            if (!logger.isDebugEnabled()) {
                return returnHop;
            }
            logger.debug("Returning hop: " + returnHop);
            return returnHop;
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Domain " + inputAddress + " could not be resolved " + ex.getMessage());
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Printing SRV resolution stack trace", ex);
            }
        }
    }
}
