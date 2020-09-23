package org.jitsi.gov.nist.javax.sip.clientauthutils;

import java.security.MessageDigest;
import java.util.Date;
import java.util.Random;
import org.jitsi.gov.nist.core.InternalErrorHandler;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.address.URI;
import org.jitsi.javax.sip.header.HeaderFactory;
import org.jitsi.javax.sip.header.ProxyAuthenticateHeader;
import org.jitsi.javax.sip.header.ProxyAuthorizationHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public class DigestServerAuthenticationHelper {
    public static final String DEFAULT_ALGORITHM = "MD5";
    public static final String DEFAULT_SCHEME = "Digest";
    private static final char[] toHex = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private MessageDigest messageDigest = MessageDigest.getInstance(DEFAULT_ALGORITHM);

    public static String toHexString(byte[] b) {
        int pos = 0;
        char[] c = new char[(b.length * 2)];
        for (int i = 0; i < b.length; i++) {
            int i2 = pos + 1;
            c[pos] = toHex[(b[i] >> 4) & 15];
            pos = i2 + 1;
            c[i2] = toHex[b[i] & 15];
        }
        return new String(c);
    }

    private String generateNonce() {
        return toHexString(this.messageDigest.digest((new Long(new Date().getTime()).toString() + new Long(new Random().nextLong()).toString()).getBytes()));
    }

    public void generateChallenge(HeaderFactory headerFactory, Response response, String realm) {
        try {
            ProxyAuthenticateHeader proxyAuthenticate = headerFactory.createProxyAuthenticateHeader("Digest");
            proxyAuthenticate.setParameter("realm", realm);
            proxyAuthenticate.setParameter("nonce", generateNonce());
            proxyAuthenticate.setParameter("opaque", "");
            proxyAuthenticate.setParameter("stale", "FALSE");
            proxyAuthenticate.setParameter("algorithm", DEFAULT_ALGORITHM);
            response.setHeader(proxyAuthenticate);
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
        }
    }

    public boolean doAuthenticateHashedPassword(Request request, String hashedPassword) {
        ProxyAuthorizationHeader authHeader = (ProxyAuthorizationHeader) request.getHeader("Proxy-Authorization");
        if (authHeader == null) {
            return false;
        }
        String realm = authHeader.getRealm();
        if (authHeader.getUsername() == null || realm == null) {
            return false;
        }
        String nonce = authHeader.getNonce();
        URI uri = authHeader.getURI();
        if (uri == null) {
            return false;
        }
        String HA1 = hashedPassword;
        String HA2 = toHexString(this.messageDigest.digest((request.getMethod().toUpperCase() + Separators.COLON + uri.toString()).getBytes()));
        String cnonce = authHeader.getCNonce();
        String KD = HA1 + Separators.COLON + nonce;
        if (cnonce != null) {
            KD = KD + Separators.COLON + cnonce;
        }
        return toHexString(this.messageDigest.digest((KD + Separators.COLON + HA2).getBytes())).equals(authHeader.getResponse());
    }

    public boolean doAuthenticatePlainTextPassword(Request request, String pass) {
        ProxyAuthorizationHeader authHeader = (ProxyAuthorizationHeader) request.getHeader("Proxy-Authorization");
        if (authHeader == null) {
            return false;
        }
        String realm = authHeader.getRealm();
        String username = authHeader.getUsername();
        if (username == null || realm == null) {
            return false;
        }
        String nonce = authHeader.getNonce();
        URI uri = authHeader.getURI();
        if (uri == null) {
            return false;
        }
        String A1 = username + Separators.COLON + realm + Separators.COLON + pass;
        String A2 = request.getMethod().toUpperCase() + Separators.COLON + uri.toString();
        String HA1 = toHexString(this.messageDigest.digest(A1.getBytes()));
        String HA2 = toHexString(this.messageDigest.digest(A2.getBytes()));
        String cnonce = authHeader.getCNonce();
        String KD = HA1 + Separators.COLON + nonce;
        if (cnonce != null) {
            KD = KD + Separators.COLON + cnonce;
        }
        return toHexString(this.messageDigest.digest((KD + Separators.COLON + HA2).getBytes())).equals(authHeader.getResponse());
    }
}
