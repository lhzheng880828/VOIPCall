package org.jitsi.gov.nist.javax.sip;

import java.io.IOException;
import java.text.ParseException;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.Host;
import org.jitsi.gov.nist.core.HostPort;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.address.AddressImpl;
import org.jitsi.gov.nist.javax.sip.address.SipUri;
import org.jitsi.gov.nist.javax.sip.header.Contact;
import org.jitsi.gov.nist.javax.sip.header.Via;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.gov.nist.javax.sip.stack.MessageChannel;
import org.jitsi.gov.nist.javax.sip.stack.MessageProcessor;
import org.jitsi.javax.sip.ListeningPoint;
import org.jitsi.javax.sip.SipStack;
import org.jitsi.javax.sip.address.SipURI;
import org.jitsi.javax.sip.header.ContactHeader;
import org.jitsi.javax.sip.header.ViaHeader;

public class ListeningPointImpl implements ListeningPoint, ListeningPointExt {
    private static StackLogger logger = CommonLogger.getLogger(ListeningPointImpl.class);
    protected MessageProcessor messageProcessor;
    int port;
    protected SipProviderImpl sipProvider;
    protected SipStackImpl sipStack;
    protected String transport;

    public static String makeKey(String host, int port, String transport) {
        return (Separators.COLON + port + Separators.SLASH + transport).toLowerCase();
    }

    /* access modifiers changed from: protected */
    public String getKey() {
        return makeKey(getIPAddress(), this.port, this.transport);
    }

    public void setSipProvider(SipProviderImpl sipProviderImpl) {
        this.sipProvider = sipProviderImpl;
    }

    public void removeSipProvider() {
        this.sipProvider = null;
    }

    protected ListeningPointImpl(SipStack sipStack, int port, String transport) {
        this.sipStack = (SipStackImpl) sipStack;
        this.port = port;
        this.transport = transport;
    }

    public Object clone() {
        ListeningPointImpl lip = new ListeningPointImpl(this.sipStack, this.port, null);
        lip.sipStack = this.sipStack;
        return lip;
    }

    public int getPort() {
        return this.messageProcessor.getPort();
    }

    public String getTransport() {
        return this.messageProcessor.getTransport();
    }

    public SipProviderImpl getProvider() {
        return this.sipProvider;
    }

    public String getIPAddress() {
        return this.messageProcessor.getIpAddress().getHostAddress();
    }

    public void setSentBy(String sentBy) throws ParseException {
        this.messageProcessor.setSentBy(sentBy);
    }

    public String getSentBy() {
        return this.messageProcessor.getSentBy();
    }

    public boolean isSentBySet() {
        return this.messageProcessor.isSentBySet();
    }

    public Via getViaHeader() {
        return this.messageProcessor.getViaHeader();
    }

    public MessageProcessor getMessageProcessor() {
        return this.messageProcessor;
    }

    public ContactHeader createContactHeader() {
        try {
            String ipAddress = getIPAddress();
            int port = getPort();
            SipURI sipURI = new SipUri();
            sipURI.setHost(ipAddress);
            sipURI.setPort(port);
            sipURI.setTransportParam(this.transport);
            Contact contact = new Contact();
            AddressImpl address = new AddressImpl();
            address.setURI(sipURI);
            contact.setAddress(address);
            return contact;
        } catch (Exception e) {
            InternalErrorHandler.handleException("Unexpected exception", logger);
            return null;
        }
    }

    public void sendHeartbeat(String ipAddress, int port) throws IOException {
        HostPort targetHostPort = new HostPort();
        targetHostPort.setHost(new Host(ipAddress));
        targetHostPort.setPort(port);
        MessageChannel messageChannel = this.messageProcessor.createMessageChannel(targetHostPort);
        SIPRequest siprequest = new SIPRequest();
        siprequest.setNullRequest();
        messageChannel.sendMessage(siprequest);
    }

    public ViaHeader createViaHeader() {
        return getViaHeader();
    }
}
