package net.java.sip.communicator.impl.protocol.sip.security;

import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import net.java.sip.communicator.impl.protocol.jabber.extensions.ConferenceDescriptionPacketExtension;
import net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl;
import net.java.sip.communicator.impl.protocol.sip.SipActivator;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.SecurityAuthority;
import net.java.sip.communicator.service.protocol.UserCredentials;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.SIPHeaderList;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.SipProvider;
import org.jitsi.javax.sip.TransactionUnavailableException;
import org.jitsi.javax.sip.header.AuthorizationHeader;
import org.jitsi.javax.sip.header.CSeqHeader;
import org.jitsi.javax.sip.header.CallIdHeader;
import org.jitsi.javax.sip.header.HeaderFactory;
import org.jitsi.javax.sip.header.ProxyAuthenticateHeader;
import org.jitsi.javax.sip.header.ViaHeader;
import org.jitsi.javax.sip.header.WWWAuthenticateHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public class SipSecurityManager {
    private static final Logger logger = Logger.getLogger(SipSecurityManager.class);
    private final AccountID accountID;
    private CredentialsCache cachedCredentials = new CredentialsCache();
    private HeaderFactory headerFactory = null;
    private final ProtocolProviderServiceSipImpl protocolProvider;
    private SecurityAuthority securityAuthority = null;

    public SipSecurityManager(AccountID accountID, ProtocolProviderServiceSipImpl protocolProvider) {
        this.accountID = accountID;
        this.protocolProvider = protocolProvider;
    }

    public void setHeaderFactory(HeaderFactory headerFactory) {
        this.headerFactory = headerFactory;
    }

    public synchronized ClientTransaction handleChallenge(Response challenge, ClientTransaction challengedTransaction, SipProvider transactionCreator) throws SipException, InvalidArgumentException, OperationFailedException, NullPointerException {
        return handleChallenge(challenge, challengedTransaction, transactionCreator, -1);
    }

    public synchronized ClientTransaction handleChallenge(Response challenge, ClientTransaction challengedTransaction, SipProvider transactionCreator, long newCSeq) throws SipException, InvalidArgumentException, OperationFailedException, NullPointerException {
        ClientTransaction retryTran;
        String branchID = challengedTransaction.getBranchId();
        Request reoriginatedRequest = cloneReqForAuthentication(challengedTransaction.getRequest(), challenge);
        incrementRequestSeqNo(reoriginatedRequest, newCSeq);
        ListIterator<WWWAuthenticateHeader> authHeaders = extractChallenges(challenge);
        retryTran = transactionCreator.getNewClientTransaction(reoriginatedRequest);
        Dialog tranDialog = retryTran.getDialog();
        if (!(tranDialog == null || tranDialog.getLocalSeqNumber() == getRequestSeqNo(reoriginatedRequest))) {
            tranDialog.incrementLocalSequenceNumber();
        }
        while (authHeaders.hasNext()) {
            WWWAuthenticateHeader authHeader = (WWWAuthenticateHeader) authHeaders.next();
            String realm = authHeader.getRealm();
            CredentialsCacheEntry ccEntry = this.cachedCredentials.remove(realm);
            boolean ccEntryHasSeenTran = false;
            if (ccEntry != null) {
                ccEntryHasSeenTran = ccEntry.popBranchID(branchID);
            }
            long authenticationDuration = System.currentTimeMillis();
            String storedPassword = SipActivator.getProtocolProviderFactory().loadPassword(this.accountID);
            if (ccEntry == null) {
                if (storedPassword != null) {
                    ccEntry = createCcEntryWithStoredPassword(storedPassword);
                    if (logger.isTraceEnabled()) {
                        logger.trace("seem to have a stored pass! Try with it.");
                    }
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("We don't seem to have a good pass! Get one.");
                    }
                    ccEntry = createCcEntryWithNewCredentials(realm, 0);
                    if (ccEntry == null) {
                        throw new OperationFailedException("User has canceled the authentication process.", 15);
                    }
                }
            } else if (ccEntryHasSeenTran && !authHeader.isStale()) {
                SipActivator.getProtocolProviderFactory().storePassword(this.accountID, null);
                this.protocolProvider.getRegistrarConnection().setRegistrationState(RegistrationState.AUTHENTICATION_FAILED, 1, null);
                ccEntry = createCcEntryWithNewCredentials(realm, 1);
                if (ccEntry == null) {
                    throw new OperationFailedException("User has canceled the authentication process.", 15);
                }
            } else if (logger.isTraceEnabled()) {
                logger.trace("We seem to have a pass in the cache. Let's try with it.");
            }
            if (ccEntry.userCredentials == null) {
                throw new OperationFailedException("Unable to authenticate with realm " + realm + ". User did not provide credentials.", Response.UNAUTHORIZED);
            }
            String str;
            boolean authDurTooLong = System.currentTimeMillis() - authenticationDuration > 25000;
            String method = reoriginatedRequest.getMethod();
            String uri = reoriginatedRequest.getRequestURI().toString();
            if (reoriginatedRequest.getContent() == null) {
                str = "";
            } else {
                str = reoriginatedRequest.getContent().toString();
            }
            AuthorizationHeader authorization = createAuthorizationHeader(method, uri, str, authHeader, ccEntry.userCredentials);
            if (!authDurTooLong) {
                ccEntry.pushBranchID(retryTran.getBranchId());
            }
            this.cachedCredentials.cacheEntry(realm, ccEntry);
            if (logger.isDebugEnabled()) {
                logger.debug("Created authorization header: " + authorization.toString());
            }
            CallIdHeader call = (CallIdHeader) reoriginatedRequest.getHeader("Call-ID");
            if (call != null) {
                this.cachedCredentials.cacheAuthorizationHeader(call.getCallId(), authorization);
            }
            reoriginatedRequest.addHeader(authorization);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Returning authorization transaction.");
        }
        return retryTran;
    }

    public void setSecurityAuthority(SecurityAuthority authority) {
        this.securityAuthority = authority;
    }

    public SecurityAuthority getSecurityAuthority() {
        return this.securityAuthority;
    }

    public synchronized ClientTransaction handleForbiddenResponse(Response forbidden, ClientTransaction endedTransaction, SipProvider transactionCreator) throws InvalidArgumentException, TransactionUnavailableException, OperationFailedException {
        ClientTransaction retryTran;
        this.cachedCredentials.clear();
        SipActivator.getProtocolProviderFactory().storePassword(this.accountID, null);
        Request reoriginatedRequest = (Request) endedTransaction.getRequest().clone();
        removeBranchID(reoriginatedRequest);
        List<String> realms = removeAuthHeaders(reoriginatedRequest);
        if (realms.size() == 0) {
            throw new OperationFailedException("No realms present, cannot authenticate", Response.FORBIDDEN);
        }
        incrementRequestSeqNo(reoriginatedRequest, -1);
        retryTran = transactionCreator.getNewClientTransaction(reoriginatedRequest);
        Dialog tranDialog = retryTran.getDialog();
        if (!(tranDialog == null || tranDialog.getLocalSeqNumber() == getRequestSeqNo(reoriginatedRequest))) {
            tranDialog.incrementLocalSequenceNumber();
        }
        for (String cacheEntry : realms) {
            CredentialsCacheEntry ccEntry = createCcEntryWithStoredPassword("");
            ccEntry.pushBranchID(retryTran.getBranchId());
            this.cachedCredentials.cacheEntry(cacheEntry, ccEntry);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Returning authorization transaction.");
        }
        return retryTran;
    }

    private List<String> removeAuthHeaders(Request request) {
        Iterator<SIPHeader> headers = ((SIPRequest) request).getHeaders();
        List<String> realms = new LinkedList();
        removeAuthHeaders(headers, realms);
        request.removeHeader("Authorization");
        request.removeHeader("Proxy-Authorization");
        return realms;
    }

    private void removeAuthHeaders(Iterator<SIPHeader> headers, List<String> realms) {
        while (headers.hasNext()) {
            SIPHeader header = (SIPHeader) headers.next();
            if (header instanceof AuthorizationHeader) {
                realms.add(((AuthorizationHeader) header).getRealm());
            } else if (header instanceof SIPHeaderList) {
                removeAuthHeaders(((SIPHeaderList) header).iterator(), realms);
            }
        }
    }

    private Request cloneReqForAuthentication(Request challengedRequest, Response challenge) {
        Request reoriginatedRequest = (Request) challengedRequest.clone();
        removeBranchID(reoriginatedRequest);
        if (challenge.getStatusCode() == Response.UNAUTHORIZED) {
            reoriginatedRequest.removeHeader("Authorization");
        } else if (challenge.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED) {
            reoriginatedRequest.removeHeader("Proxy-Authorization");
        }
        return reoriginatedRequest;
    }

    private ListIterator<WWWAuthenticateHeader> extractChallenges(Response challenge) {
        if (challenge.getStatusCode() == Response.UNAUTHORIZED) {
            return challenge.getHeaders("WWW-Authenticate");
        }
        if (challenge.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED) {
            return challenge.getHeaders("Proxy-Authenticate");
        }
        return null;
    }

    private AuthorizationHeader createAuthorizationHeader(String method, String uri, String requestBody, WWWAuthenticateHeader authHeader, UserCredentials userCredentials) throws OperationFailedException {
        String qop = authHeader.getQop() != null ? ConferenceDescriptionPacketExtension.PASSWORD_ATTR_NAME : null;
        String nc_value = "00000001";
        String cnonce = "xyz";
        try {
            String response = MessageDigestAlgorithm.calculateResponse(authHeader.getAlgorithm(), userCredentials.getUserName(), authHeader.getRealm(), new String(userCredentials.getPassword()), authHeader.getNonce(), nc_value, cnonce, method, uri, requestBody, qop);
            try {
                AuthorizationHeader authorization;
                if (authHeader instanceof ProxyAuthenticateHeader) {
                    authorization = this.headerFactory.createProxyAuthorizationHeader(authHeader.getScheme());
                } else {
                    authorization = this.headerFactory.createAuthorizationHeader(authHeader.getScheme());
                }
                authorization.setUsername(userCredentials.getUserName());
                authorization.setRealm(authHeader.getRealm());
                authorization.setNonce(authHeader.getNonce());
                authorization.setParameter("uri", uri);
                authorization.setResponse(response);
                if (authHeader.getAlgorithm() != null) {
                    authorization.setAlgorithm(authHeader.getAlgorithm());
                }
                if (authHeader.getOpaque() != null) {
                    authorization.setOpaque(authHeader.getOpaque());
                }
                if (qop != null) {
                    authorization.setQop(qop);
                    authorization.setCNonce(cnonce);
                    authorization.setNonceCount(Integer.parseInt(nc_value));
                }
                authorization.setResponse(response);
                return authorization;
            } catch (ParseException e) {
                throw new SecurityException("Failed to create an authorization header!");
            }
        } catch (NullPointerException exc) {
            throw new OperationFailedException("The authenticate header was malformatted", 1, exc);
        }
    }

    public void cacheCredentials(String realm, UserCredentials credentials) {
        CredentialsCacheEntry ccEntry = new CredentialsCacheEntry();
        ccEntry.userCredentials = credentials;
        this.cachedCredentials.cacheEntry(realm, ccEntry);
    }

    private void removeBranchID(Request request) {
        ViaHeader viaHeader = (ViaHeader) request.getHeader("Via");
        request.removeHeader("Via");
        try {
            request.setHeader(this.headerFactory.createViaHeader(viaHeader.getHost(), viaHeader.getPort(), viaHeader.getTransport(), null));
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("failed to reset a Via header");
            }
        }
    }

    private CredentialsCacheEntry createCcEntryWithNewCredentials(String realm, int reasonCode) {
        CredentialsCacheEntry ccEntry = new CredentialsCacheEntry();
        UserCredentials defaultCredentials = new UserCredentials();
        String authName = this.accountID.getAccountPropertyString("AUTHORIZATION_NAME");
        if (authName == null || authName.length() <= 0) {
            defaultCredentials.setUserName(this.accountID.getUserID());
        } else {
            defaultCredentials.setUserName(authName);
        }
        UserCredentials newCredentials = getSecurityAuthority().obtainCredentials(this.accountID.getDisplayName(), defaultCredentials, reasonCode);
        if (newCredentials == null) {
            return null;
        }
        if (newCredentials.getPassword() == null) {
            return null;
        }
        ccEntry.userCredentials = newCredentials;
        if (ccEntry.userCredentials == null || !ccEntry.userCredentials.isPasswordPersistent()) {
            return ccEntry;
        }
        SipActivator.getProtocolProviderFactory().storePassword(this.accountID, ccEntry.userCredentials.getPasswordAsString());
        return ccEntry;
    }

    private CredentialsCacheEntry createCcEntryWithStoredPassword(String password) {
        CredentialsCacheEntry ccEntry = new CredentialsCacheEntry();
        ccEntry.userCredentials = new UserCredentials();
        String authName = this.accountID.getAccountPropertyString("AUTHORIZATION_NAME");
        if (authName == null || authName.length() <= 0) {
            ccEntry.userCredentials.setUserName(this.accountID.getUserID());
        } else {
            ccEntry.userCredentials.setUserName(authName);
        }
        ccEntry.userCredentials.setPassword(password.toCharArray());
        return ccEntry;
    }

    public AuthorizationHeader getCachedAuthorizationHeader(String callID) {
        return this.cachedCredentials.getCachedAuthorizationHeader(callID);
    }

    private void incrementRequestSeqNo(Request request, long newCSeq) throws InvalidArgumentException {
        CSeqHeader cSeq = (CSeqHeader) request.getHeader("CSeq");
        if (newCSeq == -1) {
            cSeq.setSeqNumber(cSeq.getSeqNumber() + 1);
        } else {
            cSeq.setSeqNumber(newCSeq);
        }
    }

    private long getRequestSeqNo(Request request) {
        return ((CSeqHeader) request.getHeader("CSeq")).getSeqNumber();
    }
}
