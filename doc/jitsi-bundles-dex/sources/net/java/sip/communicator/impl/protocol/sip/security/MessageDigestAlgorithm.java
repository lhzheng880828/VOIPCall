package net.java.sip.communicator.impl.protocol.sip.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;

public class MessageDigestAlgorithm {
    private static final Logger logger = Logger.getLogger(MessageDigestAlgorithm.class);
    private static final char[] toHex = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /* JADX WARNING: Missing block: B:38:0x012e, code skipped:
            if (r17.equalsIgnoreCase("auth-int") != false) goto L_0x0130;
     */
    static java.lang.String calculateResponse(java.lang.String r7, java.lang.String r8, java.lang.String r9, java.lang.String r10, java.lang.String r11, java.lang.String r12, java.lang.String r13, java.lang.String r14, java.lang.String r15, java.lang.String r16, java.lang.String r17) {
        /*
        r4 = logger;
        r4 = r4.isDebugEnabled();
        if (r4 == 0) goto L_0x0095;
    L_0x0008:
        r5 = logger;
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r6 = "trying to authenticate using : ";
        r4 = r4.append(r6);
        r4 = r4.append(r7);
        r6 = ", ";
        r4 = r4.append(r6);
        r4 = r4.append(r8);
        r6 = ", ";
        r4 = r4.append(r6);
        r4 = r4.append(r9);
        r6 = ", ";
        r6 = r4.append(r6);
        if (r10 == 0) goto L_0x00a9;
    L_0x0035:
        r4 = r10.trim();
        r4 = r4.length();
        if (r4 <= 0) goto L_0x00a9;
    L_0x003f:
        r4 = 1;
    L_0x0040:
        r4 = r6.append(r4);
        r6 = ", ";
        r4 = r4.append(r6);
        r4 = r4.append(r11);
        r6 = ", ";
        r4 = r4.append(r6);
        r4 = r4.append(r12);
        r6 = ", ";
        r4 = r4.append(r6);
        r4 = r4.append(r13);
        r6 = ", ";
        r4 = r4.append(r6);
        r4 = r4.append(r14);
        r6 = ", ";
        r4 = r4.append(r6);
        r4 = r4.append(r15);
        r6 = ", ";
        r4 = r4.append(r6);
        r0 = r16;
        r4 = r4.append(r0);
        r6 = ", ";
        r4 = r4.append(r6);
        r0 = r17;
        r4 = r4.append(r0);
        r4 = r4.toString();
        r5.debug(r4);
    L_0x0095:
        if (r8 == 0) goto L_0x00a1;
    L_0x0097:
        if (r9 == 0) goto L_0x00a1;
    L_0x0099:
        if (r10 == 0) goto L_0x00a1;
    L_0x009b:
        if (r14 == 0) goto L_0x00a1;
    L_0x009d:
        if (r15 == 0) goto L_0x00a1;
    L_0x009f:
        if (r11 != 0) goto L_0x00ab;
    L_0x00a1:
        r4 = new java.lang.NullPointerException;
        r5 = "Null parameter to MessageDigestAlgorithm.calculateResponse()";
        r4.<init>(r5);
        throw r4;
    L_0x00a9:
        r4 = 0;
        goto L_0x0040;
    L_0x00ab:
        r1 = 0;
        if (r7 == 0) goto L_0x00c4;
    L_0x00ae:
        r4 = r7.trim();
        r4 = r4.length();
        if (r4 == 0) goto L_0x00c4;
    L_0x00b8:
        r4 = r7.trim();
        r5 = "MD5";
        r4 = r4.equalsIgnoreCase(r5);
        if (r4 == 0) goto L_0x0174;
    L_0x00c4:
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
    L_0x00e5:
        r2 = 0;
        if (r17 == 0) goto L_0x00fe;
    L_0x00e8:
        r4 = r17.trim();
        r4 = r4.length();
        if (r4 == 0) goto L_0x00fe;
    L_0x00f2:
        r4 = r17.trim();
        r5 = "auth";
        r4 = r4.equalsIgnoreCase(r5);
        if (r4 == 0) goto L_0x01cc;
    L_0x00fe:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r4 = r4.append(r14);
        r5 = ":";
        r4 = r4.append(r5);
        r4 = r4.append(r15);
        r2 = r4.toString();
    L_0x0115:
        r3 = 0;
        if (r13 == 0) goto L_0x01f7;
    L_0x0118:
        if (r17 == 0) goto L_0x01f7;
    L_0x011a:
        if (r12 == 0) goto L_0x01f7;
    L_0x011c:
        r4 = "auth";
        r0 = r17;
        r4 = r0.equalsIgnoreCase(r4);
        if (r4 != 0) goto L_0x0130;
    L_0x0126:
        r4 = "auth-int";
        r0 = r17;
        r4 = r0.equalsIgnoreCase(r4);
        if (r4 == 0) goto L_0x01f7;
    L_0x0130:
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
    L_0x0173:
        return r3;
    L_0x0174:
        if (r13 == 0) goto L_0x017c;
    L_0x0176:
        r4 = r13.length();
        if (r4 != 0) goto L_0x0184;
    L_0x017c:
        r4 = new java.lang.NullPointerException;
        r5 = "cnonce_value may not be absent for MD5-Sess algorithm.";
        r4.<init>(r5);
        throw r4;
    L_0x0184:
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
        goto L_0x00e5;
    L_0x01cc:
        if (r16 != 0) goto L_0x01d0;
    L_0x01ce:
        r16 = "";
    L_0x01d0:
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
        goto L_0x0115;
    L_0x01f7:
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
        goto L_0x0173;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.sip.security.MessageDigestAlgorithm.calculateResponse(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String):java.lang.String");
    }

    private static String H(String data) {
        try {
            return toHexString(MessageDigest.getInstance(DigestServerAuthenticationHelper.DEFAULT_ALGORITHM).digest(data.getBytes()));
        } catch (NoSuchAlgorithmException ex) {
            logger.error("Failed to instantiate an MD5 algorithm", ex);
            return null;
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
