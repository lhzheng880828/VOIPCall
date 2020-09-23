package net.java.sip.communicator.impl.protocol.sip.net;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl;
import net.java.sip.communicator.impl.protocol.sip.SipAccountIDImpl;
import net.java.sip.communicator.service.dns.DnssecException;

public abstract class ProxyConnection {
    protected final SipAccountIDImpl account;
    private List<String> returnedAddresses = new LinkedList();
    protected InetSocketAddress socketAddress;
    protected String transport;

    public abstract boolean getNextAddressFromDns() throws DnssecException;

    protected ProxyConnection(SipAccountIDImpl account) {
        this.account = account;
    }

    public final InetSocketAddress getAddress() {
        return this.socketAddress;
    }

    public final String getTransport() {
        return this.transport;
    }

    public final String getOutboundProxyString() {
        if (this.socketAddress == null) {
            return null;
        }
        InetAddress proxyAddress = this.socketAddress.getAddress();
        StringBuilder proxyStringBuffer = new StringBuilder(proxyAddress.getHostAddress());
        if (proxyAddress instanceof Inet6Address) {
            proxyStringBuffer.insert(0, '[');
            proxyStringBuffer.append(']');
        }
        proxyStringBuffer.append(':');
        proxyStringBuffer.append(this.socketAddress.getPort());
        proxyStringBuffer.append('/');
        proxyStringBuffer.append(this.transport);
        return proxyStringBuffer.toString();
    }

    public final boolean isSameInetAddress(InetAddress addressToTest) {
        if (this.socketAddress != null && addressToTest == this.socketAddress.getAddress()) {
            return true;
        }
        return false;
    }

    public final boolean getNextAddress() throws DnssecException {
        boolean result;
        String key = null;
        do {
            result = getNextAddressFromDns();
            if (result && this.socketAddress != null) {
                key = getOutboundProxyString();
                if (!this.returnedAddresses.contains(key)) {
                    this.returnedAddresses.add(key);
                    break;
                }
            }
            if (!result) {
                break;
            }
        } while (this.returnedAddresses.contains(key));
        return result;
    }

    public void reset() {
        this.returnedAddresses.clear();
    }

    public static ProxyConnection create(ProtocolProviderServiceSipImpl pps) {
        if (pps.getAccountID().getAccountPropertyBoolean("PROXY_AUTO_CONFIG", true)) {
            return new AutoProxyConnection((SipAccountIDImpl) pps.getAccountID(), pps.getDefaultTransport());
        }
        return new ManualProxyConnection((SipAccountIDImpl) pps.getAccountID());
    }
}
