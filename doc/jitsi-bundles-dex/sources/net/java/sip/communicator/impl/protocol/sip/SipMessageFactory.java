package net.java.sip.communicator.impl.protocol.sip;

import java.net.URLDecoder;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetCusaxUtils;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.javax.sip.address.GenericURI;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.ServerTransaction;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.address.SipURI;
import org.jitsi.javax.sip.address.URI;
import org.jitsi.javax.sip.header.AuthorizationHeader;
import org.jitsi.javax.sip.header.CSeqHeader;
import org.jitsi.javax.sip.header.CallIdHeader;
import org.jitsi.javax.sip.header.CallInfoHeader;
import org.jitsi.javax.sip.header.ContactHeader;
import org.jitsi.javax.sip.header.ContentTypeHeader;
import org.jitsi.javax.sip.header.ExpiresHeader;
import org.jitsi.javax.sip.header.FromHeader;
import org.jitsi.javax.sip.header.Header;
import org.jitsi.javax.sip.header.HeaderFactory;
import org.jitsi.javax.sip.header.MaxForwardsHeader;
import org.jitsi.javax.sip.header.ToHeader;
import org.jitsi.javax.sip.header.UserAgentHeader;
import org.jitsi.javax.sip.header.ViaHeader;
import org.jitsi.javax.sip.message.Message;
import org.jitsi.javax.sip.message.MessageFactory;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public class SipMessageFactory implements MessageFactory {
    private static Random localTagGenerator = null;
    public static final Logger logger = Logger.getLogger(SipMessageFactory.class);
    private final ProtocolProviderServiceSipImpl protocolProvider;
    private final MessageFactory wrappedFactory;

    public SipMessageFactory(ProtocolProviderServiceSipImpl service, MessageFactory wrappedFactory) {
        if (service == null) {
            throw new NullPointerException("service is null");
        } else if (wrappedFactory == null) {
            throw new NullPointerException("wrappedFactory is null");
        } else {
            this.protocolProvider = service;
            this.wrappedFactory = wrappedFactory;
        }
    }

    public Request createRequest(URI requestURI, String method, CallIdHeader callId, CSeqHeader cSeq, FromHeader from, ToHeader to, List via, MaxForwardsHeader maxForwards, ContentTypeHeader contentType, Object content) throws ParseException {
        return (Request) attachScSpecifics(this.wrappedFactory.createRequest(requestURI, method, callId, cSeq, from, to, via, maxForwards, contentType, content));
    }

    public Request createRequest(URI requestURI, String method, CallIdHeader callId, CSeqHeader cSeq, FromHeader from, ToHeader to, List via, MaxForwardsHeader maxForwards, ContentTypeHeader contentType, byte[] content) throws ParseException {
        return (Request) attachScSpecifics(this.wrappedFactory.createRequest(requestURI, method, callId, cSeq, from, to, via, maxForwards, contentType, content));
    }

    public Request createRequest(URI requestURI, String method, CallIdHeader callId, CSeqHeader cSeq, FromHeader from, ToHeader to, List via, MaxForwardsHeader maxForwards) throws ParseException {
        return (Request) attachScSpecifics(this.wrappedFactory.createRequest(requestURI, method, callId, cSeq, from, to, via, maxForwards));
    }

    public Request createRequest(String requestParam) throws ParseException {
        return (Request) attachScSpecifics(this.wrappedFactory.createRequest(requestParam));
    }

    public Response createResponse(int statusCode, CallIdHeader callId, CSeqHeader cSeq, FromHeader from, ToHeader to, List via, MaxForwardsHeader maxForwards, ContentTypeHeader contentType, Object content) throws ParseException {
        return (Response) attachScSpecifics(this.wrappedFactory.createResponse(statusCode, callId, cSeq, from, to, via, maxForwards, contentType, content));
    }

    public Response createResponse(int statusCode, CallIdHeader callId, CSeqHeader cSeq, FromHeader from, ToHeader to, List via, MaxForwardsHeader maxForwards, ContentTypeHeader contentType, byte[] content) throws ParseException {
        return (Response) attachScSpecifics(this.wrappedFactory.createResponse(statusCode, callId, cSeq, from, to, via, maxForwards, contentType, content));
    }

    public Response createResponse(int statusCode, CallIdHeader callId, CSeqHeader cSeq, FromHeader from, ToHeader to, List via, MaxForwardsHeader maxForwards) throws ParseException {
        return (Response) attachScSpecifics(this.wrappedFactory.createResponse(statusCode, callId, cSeq, from, to, via, maxForwards));
    }

    public Response createResponse(int statusCode, Request request, ContentTypeHeader contentType, Object content) throws ParseException {
        Response response = this.wrappedFactory.createResponse(statusCode, request, contentType, content);
        extractAndApplyDialogToTag((SIPRequest) request, response);
        return (Response) attachScSpecifics(response);
    }

    public Response createResponse(int statusCode, Request request, ContentTypeHeader contentType, byte[] content) throws ParseException {
        Response response = this.wrappedFactory.createResponse(statusCode, request, contentType, content);
        extractAndApplyDialogToTag((SIPRequest) request, response);
        return (Response) attachScSpecifics(response);
    }

    public Response createResponse(int statusCode, Request request) throws ParseException {
        Response response = this.wrappedFactory.createResponse(statusCode, request);
        extractAndApplyDialogToTag((SIPRequest) request, response);
        return (Response) attachScSpecifics(response);
    }

    public Response createResponse(String responseParam) throws ParseException {
        return (Response) attachScSpecifics(this.wrappedFactory.createResponse(responseParam));
    }

    private void extractAndApplyDialogToTag(SIPRequest request, Response response) {
        ServerTransaction tran = (ServerTransaction) request.getTransaction();
        if (tran != null) {
            Dialog dialog = tran.getDialog();
            if (dialog != null) {
                String localDialogTag = dialog.getLocalTag();
                if (localDialogTag != null && localDialogTag.length() != 0) {
                    ToHeader to = (ToHeader) response.getHeader("To");
                    if (to != null) {
                        try {
                            to.setTag(localDialogTag);
                        } catch (ParseException e) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Failed to attach a to tag", e);
                            }
                        }
                    }
                }
            }
        }
    }

    private Message attachScSpecifics(Message message) {
        SipApplicationData.setApplicationData(message, "service", this.protocolProvider);
        if (message instanceof Response) {
            FromHeader from = (FromHeader) message.getHeader("From");
            String fromTag = from == null ? null : from.getTag();
            Response response = (Response) message;
            if (fromTag != null && fromTag.trim().length() > 0 && response.getStatusCode() > 100 && response.getStatusCode() < 300) {
                attachToTag(response, null);
            }
        }
        attachContactHeader(message);
        if ((message instanceof Request) && !"ACK".equals(((Request) message).getMethod())) {
            preAuthenticateRequest((Request) message);
        }
        UserAgentHeader userAgentHeader = this.protocolProvider.getSipCommUserAgentHeader();
        if (userAgentHeader != null) {
            message.setHeader(userAgentHeader);
        }
        return message;
    }

    private Message attachContactHeader(Message message) {
        if (message instanceof Request) {
            Request request = (Request) message;
            request.setHeader(this.protocolProvider.getContactHeader((SipURI) request.getRequestURI()));
            return request;
        }
        SipURI intendedDestinationURI;
        Response response = (Response) message;
        ViaHeader via = (ViaHeader) response.getHeader("Via");
        String transport = via.getTransport();
        String host = via.getHost();
        int port = via.getPort();
        try {
            intendedDestinationURI = this.protocolProvider.getAddressFactory().createSipURI(null, host);
            intendedDestinationURI.setPort(port);
            if (transport != null) {
                intendedDestinationURI.setTransportParam(transport);
            }
        } catch (ParseException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(via + " does not seem to be a valid header.");
            }
            intendedDestinationURI = (SipURI) ((FromHeader) response.getHeader("From")).getAddress().getURI();
        }
        response.setHeader(this.protocolProvider.getContactHeader(intendedDestinationURI));
        return response;
    }

    public static synchronized String generateLocalTag() {
        String toHexString;
        synchronized (SipMessageFactory.class) {
            if (localTagGenerator == null) {
                localTagGenerator = new Random();
            }
            toHexString = Integer.toHexString(localTagGenerator.nextInt());
        }
        return toHexString;
    }

    private void attachToTag(Response response, Dialog containingDialog) {
        ToHeader to = (ToHeader) response.getHeader("To");
        if (to == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Strange ... no to To header in response:" + response);
            }
        } else if (containingDialog == null || containingDialog.getLocalTag() == null) {
            try {
                if (to.getTag() == null || to.getTag().trim().length() == 0) {
                    String toTag = generateLocalTag();
                    if (logger.isDebugEnabled()) {
                        logger.debug("generated to tag: " + toTag);
                    }
                    to.setTag(toTag);
                }
            } catch (ParseException ex) {
                logger.error("Failed to attach a to tag to an outgoing response.", ex);
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("We seem to already have a tag in this dialog. Returning");
        }
    }

    public Request createRequest(Dialog dialog, String method) throws OperationFailedException {
        Request request = null;
        try {
            request = dialog.createRequest(method);
        } catch (SipException ex) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to create " + method + " request.", 4, ex, logger);
        }
        request.setHeader((Header) this.protocolProvider.getLocalViaHeaders(dialog.getRemoteParty()).get(0));
        attachScSpecifics(request);
        return request;
    }

    public Request createInviteRequest(Address toAddress) throws OperationFailedException, IllegalArgumentException {
        CallIdHeader callIdHeader = this.protocolProvider.getDefaultJainSipProvider().getNewCallId();
        HeaderFactory headerFactory = this.protocolProvider.getHeaderFactory();
        CSeqHeader cSeqHeader = null;
        try {
            cSeqHeader = headerFactory.createCSeqHeader(1, "INVITE");
        } catch (InvalidArgumentException ex) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("Error occurred while constructing the CSeqHeadder", 4, ex, logger);
        } catch (ParseException exc) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("Error while constructing a CSeqHeadder", 4, exc, logger);
        }
        Header replacesHeader = stripReplacesHeader(toAddress);
        String localTag = generateLocalTag();
        FromHeader fromHeader = null;
        ToHeader toHeader = null;
        try {
            fromHeader = headerFactory.createFromHeader(this.protocolProvider.getOurSipAddress(toAddress), localTag);
            toHeader = headerFactory.createToHeader(toAddress, null);
        } catch (ParseException ex2) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("An unexpected erro occurred whileconstructing the ToHeader", 4, ex2, logger);
        }
        Request invite = null;
        try {
            invite = this.protocolProvider.getMessageFactory().createRequest(toHeader.getAddress().getURI(), "INVITE", callIdHeader, cSeqHeader, fromHeader, toHeader, this.protocolProvider.getLocalViaHeaders(toAddress), this.protocolProvider.getMaxForwardsHeader());
        } catch (ParseException ex22) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("Failed to create invite Request!", 4, ex22, logger);
        }
        CallInfoHeader callInfoHeader = null;
        ProtocolProviderService cusaxProvider = null;
        try {
            OperationSetCusaxUtils cusaxOpSet = (OperationSetCusaxUtils) this.protocolProvider.getOperationSet(OperationSetCusaxUtils.class);
            if (cusaxOpSet != null) {
                cusaxProvider = cusaxOpSet.getLinkedCusaxProvider();
            }
            String alternativeImppAddress = null;
            if (cusaxProvider != null) {
                alternativeImppAddress = cusaxProvider.getAccountID().getAccountAddress();
            }
            if (alternativeImppAddress != null) {
                callInfoHeader = headerFactory.createCallInfoHeader(new GenericURI("xmpp:" + alternativeImppAddress));
                callInfoHeader.setParameter("purpose", "impp");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (callInfoHeader != null) {
            invite.setHeader(callInfoHeader);
        }
        if (replacesHeader != null) {
            invite.setHeader(replacesHeader);
        }
        return invite;
    }

    public Request createInviteRequest(Address toAddress, Message cause) throws OperationFailedException, IllegalArgumentException {
        Request invite = createInviteRequest(toAddress);
        if (cause != null) {
            reflectCauseOnEffect(cause, invite);
        }
        return invite;
    }

    private void reflectCauseOnEffect(Message cause, Message effect) {
        Header referredBy = cause.getHeader("Referred-By");
        if (referredBy != null) {
            effect.setHeader(referredBy);
        }
    }

    private Header stripReplacesHeader(Address address) throws OperationFailedException {
        URI uri = address.getURI();
        if (!uri.isSipURI()) {
            return null;
        }
        SipURI sipURI = (SipURI) uri;
        String replacesHeaderValue = sipURI.getHeader("Replaces");
        if (replacesHeaderValue == null) {
            return null;
        }
        Iterator<?> headerNameIter = sipURI.getHeaderNames();
        while (headerNameIter.hasNext()) {
            if ("Replaces".equals(headerNameIter.next())) {
                headerNameIter.remove();
                break;
            }
        }
        try {
            return this.protocolProvider.getHeaderFactory().createHeader("Replaces", URLDecoder.decode(replacesHeaderValue, "UTF-8"));
        } catch (Exception ex) {
            throw new OperationFailedException("Failed to create ReplacesHeader from " + replacesHeaderValue, 4, ex);
        }
    }

    public Request createAck(ClientTransaction clientTransaction) throws InvalidArgumentException, SipException {
        Request ack = clientTransaction.getDialog().createAck(((CSeqHeader) clientTransaction.getRequest().getHeader("CSeq")).getSeqNumber());
        attachScSpecifics(ack);
        return ack;
    }

    public void preAuthenticateRequest(Request request) {
        CallIdHeader callIdHeader = (CallIdHeader) request.getHeader("Call-ID");
        if (callIdHeader != null) {
            AuthorizationHeader authorization = this.protocolProvider.getSipSecurityManager().getCachedAuthorizationHeader(callIdHeader.getCallId());
            if (authorization != null) {
                request.setHeader(authorization);
            }
        }
    }

    public Request createRegisterRequest(Address addressOfRecord, int registrationsExpiration, CallIdHeader callIdHeader, long cSeqValue) throws InvalidArgumentException, ParseException, OperationFailedException {
        FromHeader fromHeader = this.protocolProvider.getHeaderFactory().createFromHeader(addressOfRecord, generateLocalTag());
        CSeqHeader cSeqHeader = this.protocolProvider.getHeaderFactory().createCSeqHeader(cSeqValue, "REGISTER");
        ToHeader toHeader = this.protocolProvider.getHeaderFactory().createToHeader(addressOfRecord, null);
        MaxForwardsHeader maxForwardsHeader = this.protocolProvider.getMaxForwardsHeader();
        SipURI requestURI = this.protocolProvider.getRegistrarConnection().getRegistrarURI();
        Request request = createRequest(requestURI, "REGISTER", callIdHeader, cSeqHeader, fromHeader, toHeader, this.protocolProvider.getLocalViaHeaders(requestURI), maxForwardsHeader);
        ExpiresHeader expHeader = null;
        for (int retry = 0; retry < 2; retry++) {
            try {
                expHeader = this.protocolProvider.getHeaderFactory().createExpiresHeader(registrationsExpiration);
            } catch (InvalidArgumentException ex) {
                if (retry == 0) {
                    registrationsExpiration = DesktopSharingProtocolSipImpl.SUBSCRIPTION_DURATION;
                } else {
                    throw new IllegalArgumentException("Invalid registrations expiration parameter - " + registrationsExpiration, ex);
                }
            }
        }
        request.addHeader(expHeader);
        ContactHeader contactHeader = (ContactHeader) request.getHeader("Contact");
        contactHeader.setExpires(registrationsExpiration);
        request.setHeader(contactHeader);
        return request;
    }

    public Request createUnRegisterRequest(Request registerRequest, long cSeqValue) throws InvalidArgumentException {
        Request unregisterRequest = (Request) registerRequest.clone();
        unregisterRequest.getExpires().setExpires(0);
        ((CSeqHeader) unregisterRequest.getHeader("CSeq")).setSeqNumber(cSeqValue);
        ViaHeader via = (ViaHeader) unregisterRequest.getHeader("Via");
        if (via != null) {
            via.removeParameter("branch");
        }
        ((ContactHeader) unregisterRequest.getHeader("Contact")).setExpires(0);
        attachScSpecifics(unregisterRequest);
        return unregisterRequest;
    }
}
