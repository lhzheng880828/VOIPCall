package org.jitsi.gov.nist.javax.sip.clientauthutils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import net.java.sip.communicator.impl.protocol.jabber.extensions.ConferenceDescriptionPacketExtension;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.StackLogger;

public class MessageDigestAlgorithm {
    private static final char[] toHex = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    static String calculateResponse(String algorithm, String hashUserNameRealmPasswd, String nonce_value, String nc_value, String cnonce_value, String method, String digest_uri_value, String entity_body, String qop_value, StackLogger stackLogger) {
        if (stackLogger.isLoggingEnabled(32)) {
            stackLogger.logDebug("trying to authenticate using : " + algorithm + ", " + hashUserNameRealmPasswd + ", " + nonce_value + ", " + nc_value + ", " + cnonce_value + ", " + method + ", " + digest_uri_value + ", " + entity_body + ", " + qop_value);
        }
        if (hashUserNameRealmPasswd == null || method == null || digest_uri_value == null || nonce_value == null) {
            throw new NullPointerException("Null parameter to MessageDigestAlgorithm.calculateResponse()");
        } else if (cnonce_value == null || cnonce_value.length() == 0) {
            throw new NullPointerException("cnonce_value may not be absent for MD5-Sess algorithm.");
        } else {
            String A2;
            if (qop_value == null || qop_value.trim().length() == 0 || qop_value.trim().equalsIgnoreCase(ConferenceDescriptionPacketExtension.PASSWORD_ATTR_NAME)) {
                A2 = method + Separators.COLON + digest_uri_value;
            } else {
                if (entity_body == null) {
                    entity_body = "";
                }
                A2 = method + Separators.COLON + digest_uri_value + Separators.COLON + H(entity_body);
            }
            if (cnonce_value == null || qop_value == null || nc_value == null || (!qop_value.equalsIgnoreCase(ConferenceDescriptionPacketExtension.PASSWORD_ATTR_NAME) && !qop_value.equalsIgnoreCase("auth-int"))) {
                return KD(hashUserNameRealmPasswd, nonce_value + Separators.COLON + H(A2));
            }
            return KD(hashUserNameRealmPasswd, nonce_value + Separators.COLON + nc_value + Separators.COLON + cnonce_value + Separators.COLON + qop_value + Separators.COLON + H(A2));
        }
    }

    /* JADX WARNING: Missing block: B:38:0x0130, code skipped:
            if (r17.equalsIgnoreCase("auth-int") != false) goto L_0x0132;
     */
    static java.lang.String calculateResponse(java.lang.String r7, java.lang.String r8, java.lang.String r9, java.lang.String r10, java.lang.String r11, java.lang.String r12, java.lang.String r13, java.lang.String r14, java.lang.String r15, java.lang.String r16, java.lang.String r17, org.jitsi.gov.nist.core.StackLogger r18) {
        /*
        r4 = 32;
        r0 = r18;
        r4 = r0.isLoggingEnabled(r4);
        if (r4 == 0) goto L_0x0097;
    L_0x000a:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "trying to authenticate using : ";
        r4 = r4.append(r5);
        r4 = r4.append(r7);
        r5 = ", ";
        r4 = r4.append(r5);
        r4 = r4.append(r8);
        r5 = ", ";
        r4 = r4.append(r5);
        r4 = r4.append(r9);
        r5 = ", ";
        r5 = r4.append(r5);
        if (r10 == 0) goto L_0x00ab;
    L_0x0035:
        r4 = r10.trim();
        r4 = r4.length();
        if (r4 <= 0) goto L_0x00ab;
    L_0x003f:
        r4 = 1;
    L_0x0040:
        r4 = r5.append(r4);
        r5 = ", ";
        r4 = r4.append(r5);
        r4 = r4.append(r11);
        r5 = ", ";
        r4 = r4.append(r5);
        r4 = r4.append(r12);
        r5 = ", ";
        r4 = r4.append(r5);
        r4 = r4.append(r13);
        r5 = ", ";
        r4 = r4.append(r5);
        r4 = r4.append(r14);
        r5 = ", ";
        r4 = r4.append(r5);
        r4 = r4.append(r15);
        r5 = ", ";
        r4 = r4.append(r5);
        r0 = r16;
        r4 = r4.append(r0);
        r5 = ", ";
        r4 = r4.append(r5);
        r0 = r17;
        r4 = r4.append(r0);
        r4 = r4.toString();
        r0 = r18;
        r0.logDebug(r4);
    L_0x0097:
        if (r8 == 0) goto L_0x00a3;
    L_0x0099:
        if (r9 == 0) goto L_0x00a3;
    L_0x009b:
        if (r10 == 0) goto L_0x00a3;
    L_0x009d:
        if (r14 == 0) goto L_0x00a3;
    L_0x009f:
        if (r15 == 0) goto L_0x00a3;
    L_0x00a1:
        if (r11 != 0) goto L_0x00ad;
    L_0x00a3:
        r4 = new java.lang.NullPointerException;
        r5 = "Null parameter to MessageDigestAlgorithm.calculateResponse()";
        r4.<init>(r5);
        throw r4;
    L_0x00ab:
        r4 = 0;
        goto L_0x0040;
    L_0x00ad:
        r1 = 0;
        if (r7 == 0) goto L_0x00c6;
    L_0x00b0:
        r4 = r7.trim();
        r4 = r4.length();
        if (r4 == 0) goto L_0x00c6;
    L_0x00ba:
        r4 = r7.trim();
        r5 = "MD5";
        r4 = r4.equalsIgnoreCase(r5);
        if (r4 == 0) goto L_0x0176;
    L_0x00c6:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r4 = r4.append(r8);
        r5 = ":";
        r4 = r4.append(r5);
        r4 = r4.append(r9);
        r5 = ":";
        r4 = r4.append(r5);
        r4 = r4.append(r10);
        r1 = r4.toString();
    L_0x00e7:
        r2 = 0;
        if (r17 == 0) goto L_0x0100;
    L_0x00ea:
        r4 = r17.trim();
        r4 = r4.length();
        if (r4 == 0) goto L_0x0100;
    L_0x00f4:
        r4 = r17.trim();
        r5 = "auth";
        r4 = r4.equalsIgnoreCase(r5);
        if (r4 == 0) goto L_0x01ce;
    L_0x0100:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r4 = r4.append(r14);
        r5 = ":";
        r4 = r4.append(r5);
        r4 = r4.append(r15);
        r2 = r4.toString();
    L_0x0117:
        r3 = 0;
        if (r13 == 0) goto L_0x01f9;
    L_0x011a:
        if (r17 == 0) goto L_0x01f9;
    L_0x011c:
        if (r12 == 0) goto L_0x01f9;
    L_0x011e:
        r4 = "auth";
        r0 = r17;
        r4 = r0.equalsIgnoreCase(r4);
        if (r4 != 0) goto L_0x0132;
    L_0x0128:
        r4 = "auth-int";
        r0 = r17;
        r4 = r0.equalsIgnoreCase(r4);
        if (r4 == 0) goto L_0x01f9;
    L_0x0132:
        r4 = H(r1);
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r5 = r5.append(r11);
        r6 = ":";
        r5 = r5.append(r6);
        r5 = r5.append(r12);
        r6 = ":";
        r5 = r5.append(r6);
        r5 = r5.append(r13);
        r6 = ":";
        r5 = r5.append(r6);
        r0 = r17;
        r5 = r5.append(r0);
        r6 = ":";
        r5 = r5.append(r6);
        r6 = H(r2);
        r5 = r5.append(r6);
        r5 = r5.toString();
        r3 = KD(r4, r5);
    L_0x0175:
        return r3;
    L_0x0176:
        if (r13 == 0) goto L_0x017e;
    L_0x0178:
        r4 = r13.length();
        if (r4 != 0) goto L_0x0186;
    L_0x017e:
        r4 = new java.lang.NullPointerException;
        r5 = "cnonce_value may not be absent for MD5-Sess algorithm.";
        r4.<init>(r5);
        throw r4;
    L_0x0186:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r5 = r5.append(r8);
        r6 = ":";
        r5 = r5.append(r6);
        r5 = r5.append(r9);
        r6 = ":";
        r5 = r5.append(r6);
        r5 = r5.append(r10);
        r5 = r5.toString();
        r5 = H(r5);
        r4 = r4.append(r5);
        r5 = ":";
        r4 = r4.append(r5);
        r4 = r4.append(r11);
        r5 = ":";
        r4 = r4.append(r5);
        r4 = r4.append(r13);
        r1 = r4.toString();
        goto L_0x00e7;
    L_0x01ce:
        if (r16 != 0) goto L_0x01d2;
    L_0x01d0:
        r16 = "";
    L_0x01d2:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r4 = r4.append(r14);
        r5 = ":";
        r4 = r4.append(r5);
        r4 = r4.append(r15);
        r5 = ":";
        r4 = r4.append(r5);
        r5 = H(r16);
        r4 = r4.append(r5);
        r2 = r4.toString();
        goto L_0x0117;
    L_0x01f9:
        r4 = H(r1);
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r5 = r5.append(r11);
        r6 = ":";
        r5 = r5.append(r6);
        r6 = H(r2);
        r5 = r5.append(r6);
        r5 = r5.toString();
        r3 = KD(r4, r5);
        goto L_0x0175;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.clientauthutils.MessageDigestAlgorithm.calculateResponse(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.jitsi.gov.nist.core.StackLogger):java.lang.String");
    }

    private static String H(String data) {
        try {
            return toHexString(MessageDigest.getInstance(DigestServerAuthenticationHelper.DEFAULT_ALGORITHM).digest(data.getBytes()));
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Failed to instantiate an MD5 algorithm", ex);
        }
    }

    private static String KD(String secret, String data) {
        return H(secret + Separators.COLON + data);
    }

    private static String toHexString(byte[] b) {
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
}
