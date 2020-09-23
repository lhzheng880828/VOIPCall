package net.java.sip.communicator.impl.protocol.sip.net;

import java.net.InetSocketAddress;
import java.text.ParseException;
import net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl;
import net.java.sip.communicator.impl.protocol.sip.SipAccountIDImpl;
import net.java.sip.communicator.service.dns.DnssecException;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.NetworkUtils;
import org.jitsi.gov.nist.core.Separators;

public class ManualProxyConnection extends ProxyConnection {
    private static final Logger logger = Logger.getLogger(ManualProxyConnection.class);
    private String address;
    private int lookupIndex;
    private InetSocketAddress[] lookups;
    private int port;

    public ManualProxyConnection(SipAccountIDImpl account) {
        super(account);
        reset();
    }

    public boolean getNextAddressFromDns() throws DnssecException {
        if (this.lookups == null) {
            try {
                this.lookupIndex = 0;
                this.lookups = NetworkUtils.getAandAAAARecords(this.address, this.port);
                if (this.lookups.length == 0) {
                    this.lookups = null;
                    return false;
                }
            } catch (ParseException e) {
                logger.error("Invalid address <" + this.address + Separators.GREATER_THAN, e);
                return false;
            }
        }
        if (this.lookupIndex >= this.lookups.length) {
            if (logger.isDebugEnabled()) {
                logger.debug("No more addresses for " + this.account);
            }
            this.lookups = null;
            return false;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Returning <" + this.socketAddress + "> as next address for " + this.account);
        }
        this.socketAddress = this.lookups[this.lookupIndex];
        this.lookupIndex++;
        return true;
    }

    public void reset() {
        super.reset();
        this.address = this.account.getAccountPropertyString("PROXY_ADDRESS");
        this.port = this.account.getAccountPropertyInt("PROXY_PORT", 5060);
        this.transport = this.account.getAccountPropertyString("PREFERRED_TRANSPORT");
        if (!ProtocolProviderServiceSipImpl.isValidTransport(this.transport)) {
            throw new IllegalArgumentException(this.transport + " is not a valid SIP transport");
        }
    }
}
