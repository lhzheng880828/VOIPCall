package org.jitsi.gov.nist.javax.sip.clientauthutils;

import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Timer;
import net.java.sip.communicator.impl.protocol.jabber.extensions.ConferenceDescriptionPacketExtension;
import org.jitsi.gov.nist.core.CommonLogger;
import org.jitsi.gov.nist.core.StackLogger;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.gov.nist.javax.sip.stack.SIPClientTransaction;
import org.jitsi.gov.nist.javax.sip.stack.SIPTransactionStack;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.DialogState;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.SipProvider;
import org.jitsi.javax.sip.address.Hop;
import org.jitsi.javax.sip.address.SipURI;
import org.jitsi.javax.sip.address.URI;
import org.jitsi.javax.sip.header.AuthorizationHeader;
import org.jitsi.javax.sip.header.CSeqHeader;
import org.jitsi.javax.sip.header.Header;
import org.jitsi.javax.sip.header.HeaderFactory;
import org.jitsi.javax.sip.header.ProxyAuthenticateHeader;
import org.jitsi.javax.sip.header.ViaHeader;
import org.jitsi.javax.sip.header.WWWAuthenticateHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public class AuthenticationHelperImpl implements AuthenticationHelper {
    private static StackLogger logger = CommonLogger.getLogger(AuthenticationHelperImpl.class);
    private Object accountManager = null;
    private CredentialsCache cachedCredentials;
    private HeaderFactory headerFactory;
    private SIPTransactionStack sipStack;
    Timer timer;

    public AuthenticationHelperImpl(SIPTransactionStack sipStack, AccountManager accountManager, HeaderFactory headerFactory) {
        this.accountManager = accountManager;
        this.headerFactory = headerFactory;
        this.sipStack = sipStack;
        this.cachedCredentials = new CredentialsCache(sipStack.getTimer());
    }

    public AuthenticationHelperImpl(SIPTransactionStack sipStack, SecureAccountManager accountManager, HeaderFactory headerFactory) {
        this.accountManager = accountManager;
        this.headerFactory = headerFactory;
        this.sipStack = sipStack;
        this.cachedCredentials = new CredentialsCache(sipStack.getTimer());
    }

    public ClientTransaction handleChallenge(Response challenge, ClientTransaction challengedTransaction, SipProvider transactionCreator, int cacheTime) throws SipException, NullPointerException {
        CSeqHeader cSeq;
        try {
            Request reoriginatedRequest;
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("handleChallenge: " + challenge);
            }
            SIPRequest challengedRequest = (SIPRequest) challengedTransaction.getRequest();
            if (challengedRequest.getToTag() == null && challengedTransaction.getDialog() != null && challengedTransaction.getDialog().getState() == DialogState.CONFIRMED) {
                reoriginatedRequest = challengedTransaction.getDialog().createRequest(challengedRequest.getMethod());
                Iterator<String> headerNames = challengedRequest.getHeaderNames();
                while (headerNames.hasNext()) {
                    String headerName = (String) headerNames.next();
                    if (reoriginatedRequest.getHeader(headerName) != null) {
                        ListIterator<Header> iterator = reoriginatedRequest.getHeaders(headerName);
                        while (iterator.hasNext()) {
                            reoriginatedRequest.addHeader((Header) iterator.next());
                        }
                    }
                }
            } else {
                reoriginatedRequest = (Request) challengedRequest.clone();
            }
            removeBranchID(reoriginatedRequest);
            if (challenge == null || reoriginatedRequest == null) {
                throw new NullPointerException("A null argument was passed to handle challenge.");
            }
            ListIterator authHeaders;
            if (challenge.getStatusCode() == Response.UNAUTHORIZED) {
                authHeaders = challenge.getHeaders("WWW-Authenticate");
            } else if (challenge.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED) {
                authHeaders = challenge.getHeaders("Proxy-Authenticate");
            } else {
                throw new IllegalArgumentException("Unexpected status code ");
            }
            if (authHeaders == null) {
                throw new IllegalArgumentException("Could not find WWWAuthenticate or ProxyAuthenticate headers");
            }
            reoriginatedRequest.removeHeader("Authorization");
            reoriginatedRequest.removeHeader("Proxy-Authorization");
            cSeq = (CSeqHeader) reoriginatedRequest.getHeader("CSeq");
            cSeq.setSeqNumber(cSeq.getSeqNumber() + 1);
            if (challengedRequest.getRouteHeaders() == null) {
                Hop hop = ((SIPClientTransaction) challengedTransaction).getNextHop();
                SipURI sipUri = (SipURI) reoriginatedRequest.getRequestURI();
                sipUri.setMAddrParam(hop.getHost());
                if (hop.getPort() != -1) {
                    sipUri.setPort(hop.getPort());
                }
            }
            ClientTransaction retryTran = transactionCreator.getNewClientTransaction(reoriginatedRequest);
            SipURI requestUri = (SipURI) challengedTransaction.getRequest().getRequestURI();
            while (authHeaders.hasNext()) {
                AuthorizationHeader authorization;
                WWWAuthenticateHeader authHeader = (WWWAuthenticateHeader) authHeaders.next();
                String realm = authHeader.getRealm();
                String sipDomain;
                if (this.accountManager instanceof SecureAccountManager) {
                    UserCredentialHash credHash = ((SecureAccountManager) this.accountManager).getCredentialHash(challengedTransaction, realm);
                    if (credHash == null) {
                        logger.logDebug("Could not find creds");
                        throw new SipException("Cannot find user creds for the given user name and realm");
                    }
                    String str;
                    URI uri = reoriginatedRequest.getRequestURI();
                    sipDomain = credHash.getSipDomain();
                    String method = reoriginatedRequest.getMethod();
                    String obj = uri.toString();
                    if (reoriginatedRequest.getContent() == null) {
                        str = "";
                    } else {
                        str = new String(reoriginatedRequest.getRawContent());
                    }
                    authorization = getAuthorization(method, obj, str, authHeader, credHash);
                } else {
                    UserCredentials userCreds = ((AccountManager) this.accountManager).getCredentials(challengedTransaction, realm);
                    if (userCreds == null) {
                        throw new SipException("Cannot find user creds for the given user name and realm");
                    }
                    sipDomain = userCreds.getSipDomain();
                    authorization = getAuthorization(reoriginatedRequest.getMethod(), reoriginatedRequest.getRequestURI().toString(), reoriginatedRequest.getContent() == null ? "" : new String(reoriginatedRequest.getRawContent()), authHeader, userCreds);
                }
                if (logger.isLoggingEnabled(32)) {
                    logger.logDebug("Created authorization header: " + authorization.toString());
                }
                if (cacheTime != 0) {
                    this.cachedCredentials.cacheAuthorizationHeader(challengedRequest.getCallId().getCallId(), authorization, cacheTime);
                }
                reoriginatedRequest.addHeader(authorization);
            }
            if (logger.isLoggingEnabled(32)) {
                logger.logDebug("Returning authorization transaction." + retryTran);
            }
            return retryTran;
        } catch (InvalidArgumentException e) {
            throw new SipException("Invalid CSeq -- could not increment : " + cSeq.getSeqNumber());
        } catch (SipException ex) {
            throw ex;
        } catch (Exception ex2) {
            logger.logError("Unexpected exception ", ex2);
            throw new SipException("Unexpected exception ", ex2);
        }
    }

    private AuthorizationHeader getAuthorization(String method, String uri, String requestBody, WWWAuthenticateHeader authHeader, UserCredentials userCredentials) {
        String qop = authHeader.getQop() != null ? ConferenceDescriptionPacketExtension.PASSWORD_ATTR_NAME : null;
        String nc_value = "00000001";
        String cnonce = "xyz";
        String response = MessageDigestAlgorithm.calculateResponse(authHeader.getAlgorithm(), userCredentials.getUserName(), authHeader.getRealm(), userCredentials.getPassword(), authHeader.getNonce(), nc_value, cnonce, method, uri, requestBody, qop, logger);
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
            throw new RuntimeException("Failed to create an authorization header!");
        }
    }

    private AuthorizationHeader getAuthorization(String method, String uri, String requestBody, WWWAuthenticateHeader authHeader, UserCredentialHash userCredentials) {
        String qop = authHeader.getQop() != null ? ConferenceDescriptionPacketExtension.PASSWORD_ATTR_NAME : null;
        String nc_value = "00000001";
        String cnonce = "xyz";
        String response = MessageDigestAlgorithm.calculateResponse(authHeader.getAlgorithm(), userCredentials.getHashUserDomainPassword(), authHeader.getNonce(), nc_value, cnonce, method, uri, requestBody, qop, logger);
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
            throw new RuntimeException("Failed to create an authorization header!");
        }
    }

    private void removeBranchID(Request request) {
        ((ViaHeader) request.getHeader("Via")).removeParameter("branch");
    }

    public void setAuthenticationHeaders(Request request) {
        String callId = ((SIPRequest) request).getCallId().getCallId();
        request.removeHeader("Authorization");
        Collection<AuthorizationHeader> authHeaders = this.cachedCredentials.getCachedAuthorizationHeaders(callId);
        if (authHeaders != null) {
            for (AuthorizationHeader authHeader : authHeaders) {
                request.addHeader(authHeader);
            }
        } else if (logger.isLoggingEnabled(32)) {
            logger.logDebug("Could not find authentication headers for " + callId);
        }
    }

    public void removeCachedAuthenticationHeaders(String callId) {
        if (callId == null) {
            throw new NullPointerException("Null callId argument ");
        }
        this.cachedCredentials.removeAuthenticationHeader(callId);
    }
}
