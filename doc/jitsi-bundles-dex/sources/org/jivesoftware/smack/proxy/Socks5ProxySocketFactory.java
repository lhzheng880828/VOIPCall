package org.jivesoftware.smack.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.SocketFactory;
import org.jivesoftware.smack.proxy.ProxyInfo.ProxyType;

public class Socks5ProxySocketFactory extends SocketFactory {
    private ProxyInfo proxy;

    public Socks5ProxySocketFactory(ProxyInfo proxy) {
        this.proxy = proxy;
    }

    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return socks5ProxifiedSocket(host, port);
    }

    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return socks5ProxifiedSocket(host, port);
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        return socks5ProxifiedSocket(host.getHostAddress(), port);
    }

    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return socks5ProxifiedSocket(address.getHostAddress(), port);
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:10:0x0086=Splitter:B:10:0x0086, B:28:0x0167=Splitter:B:28:0x0167} */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:10:0x0086=Splitter:B:10:0x0086, B:28:0x0167=Splitter:B:28:0x0167} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x018b A:{SYNTHETIC, Splitter:B:34:0x018b} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x01f6  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x01ab  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0090 A:{ExcHandler: RuntimeException (e java.lang.RuntimeException), Splitter:B:3:0x0033} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0090 A:{ExcHandler: RuntimeException (e java.lang.RuntimeException), Splitter:B:3:0x0033} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:13:0x0090, code skipped:
            r5 = e;
     */
    /* JADX WARNING: Missing block: B:14:0x0091, code skipped:
            r16 = r0;
     */
    /* JADX WARNING: Missing block: B:31:0x0186, code skipped:
            r5 = e;
     */
    /* JADX WARNING: Missing block: B:32:0x0187, code skipped:
            r16 = r0;
     */
    private java.net.Socket socks5ProxifiedSocket(java.lang.String r24, int r25) throws java.io.IOException {
        /*
        r23 = this;
        r16 = 0;
        r7 = 0;
        r12 = 0;
        r0 = r23;
        r0 = r0.proxy;
        r19 = r0;
        r14 = r19.getProxyAddress();
        r0 = r23;
        r0 = r0.proxy;
        r19 = r0;
        r15 = r19.getProxyPort();
        r0 = r23;
        r0 = r0.proxy;
        r19 = r0;
        r18 = r19.getProxyUsername();
        r0 = r23;
        r0 = r0.proxy;
        r19 = r0;
        r13 = r19.getProxyPassword();
        r17 = new java.net.Socket;	 Catch:{ RuntimeException -> 0x0208, Exception -> 0x0206 }
        r0 = r17;
        r0.<init>(r14, r15);	 Catch:{ RuntimeException -> 0x0208, Exception -> 0x0206 }
        r7 = r17.getInputStream();	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r12 = r17.getOutputStream();	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = 1;
        r0 = r17;
        r1 = r19;
        r0.setTcpNoDelay(r1);	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
        r0 = r19;
        r3 = new byte[r0];	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r8 = 0;
        r9 = r8 + 1;
        r19 = 5;
        r3[r8] = r19;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r8 = r9 + 1;
        r19 = 2;
        r3[r9] = r19;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r9 = r8 + 1;
        r19 = 0;
        r3[r8] = r19;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r8 = r9 + 1;
        r19 = 2;
        r3[r9] = r19;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = 0;
        r0 = r19;
        r12.write(r3, r0, r8);	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = 2;
        r0 = r23;
        r1 = r19;
        r0.fill(r7, r3, r1);	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r4 = 0;
        r19 = 1;
        r19 = r3[r19];	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r0 = r19;
        r0 = r0 & 255;
        r19 = r0;
        switch(r19) {
            case 0: goto L_0x0094;
            case 1: goto L_0x0081;
            case 2: goto L_0x0096;
            default: goto L_0x0081;
        };
    L_0x0081:
        if (r4 != 0) goto L_0x0106;
    L_0x0083:
        r17.close();	 Catch:{ Exception -> 0x01fe, RuntimeException -> 0x0090 }
    L_0x0086:
        r19 = new org.jivesoftware.smack.proxy.ProxyException;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r20 = org.jivesoftware.smack.proxy.ProxyInfo.ProxyType.SOCKS5;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r21 = "fail in SOCKS5 proxy";
        r19.m1834init(r20, r21);	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        throw r19;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
    L_0x0090:
        r5 = move-exception;
        r16 = r17;
    L_0x0093:
        throw r5;
    L_0x0094:
        r4 = 1;
        goto L_0x0081;
    L_0x0096:
        if (r18 == 0) goto L_0x0081;
    L_0x0098:
        if (r13 == 0) goto L_0x0081;
    L_0x009a:
        r8 = 0;
        r9 = r8 + 1;
        r19 = 1;
        r3[r8] = r19;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r8 = r9 + 1;
        r19 = r18.length();	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r0 = r19;
        r0 = (byte) r0;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = r0;
        r3[r9] = r19;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = r18.getBytes();	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r20 = 0;
        r21 = r18.length();	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r0 = r19;
        r1 = r20;
        r2 = r21;
        java.lang.System.arraycopy(r0, r1, r3, r8, r2);	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = r18.length();	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r8 = r19 + 2;
        r9 = r8 + 1;
        r19 = r13.length();	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r0 = r19;
        r0 = (byte) r0;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = r0;
        r3[r8] = r19;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = r13.getBytes();	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r20 = 0;
        r21 = r13.length();	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r0 = r19;
        r1 = r20;
        r2 = r21;
        java.lang.System.arraycopy(r0, r1, r3, r9, r2);	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = r13.length();	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r8 = r9 + r19;
        r19 = 0;
        r0 = r19;
        r12.write(r3, r0, r8);	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = 2;
        r0 = r23;
        r1 = r19;
        r0.fill(r7, r3, r1);	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = 1;
        r19 = r3[r19];	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        if (r19 != 0) goto L_0x0081;
    L_0x0103:
        r4 = 1;
        goto L_0x0081;
    L_0x0106:
        r8 = 0;
        r9 = r8 + 1;
        r19 = 5;
        r3[r8] = r19;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r8 = r9 + 1;
        r19 = 1;
        r3[r9] = r19;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r9 = r8 + 1;
        r19 = 0;
        r3[r8] = r19;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r6 = r24.getBytes();	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r10 = r6.length;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r8 = r9 + 1;
        r19 = 3;
        r3[r9] = r19;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r9 = r8 + 1;
        r0 = (byte) r10;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = r0;
        r3[r8] = r19;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = 0;
        r0 = r19;
        java.lang.System.arraycopy(r6, r0, r3, r9, r10);	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r8 = r10 + 5;
        r9 = r8 + 1;
        r19 = r25 >>> 8;
        r0 = r19;
        r0 = (byte) r0;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = r0;
        r3[r8] = r19;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r8 = r9 + 1;
        r0 = r25;
        r0 = r0 & 255;
        r19 = r0;
        r0 = r19;
        r0 = (byte) r0;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = r0;
        r3[r9] = r19;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = 0;
        r0 = r19;
        r12.write(r3, r0, r8);	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = 4;
        r0 = r23;
        r1 = r19;
        r0.fill(r7, r3, r1);	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = 1;
        r19 = r3[r19];	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        if (r19 == 0) goto L_0x01b7;
    L_0x0164:
        r17.close();	 Catch:{ Exception -> 0x0201, RuntimeException -> 0x0090 }
    L_0x0167:
        r19 = new org.jivesoftware.smack.proxy.ProxyException;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r20 = org.jivesoftware.smack.proxy.ProxyInfo.ProxyType.SOCKS5;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r21 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r21.<init>();	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r22 = "server returns ";
        r21 = r21.append(r22);	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r22 = 1;
        r22 = r3[r22];	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r21 = r21.append(r22);	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r21 = r21.toString();	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19.m1834init(r20, r21);	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        throw r19;	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
    L_0x0186:
        r5 = move-exception;
        r16 = r17;
    L_0x0189:
        if (r16 == 0) goto L_0x018e;
    L_0x018b:
        r16.close();	 Catch:{ Exception -> 0x0204 }
    L_0x018e:
        r19 = new java.lang.StringBuilder;
        r19.<init>();
        r20 = "ProxySOCKS5: ";
        r19 = r19.append(r20);
        r20 = r5.toString();
        r19 = r19.append(r20);
        r11 = r19.toString();
        r0 = r5 instanceof java.lang.Throwable;
        r19 = r0;
        if (r19 == 0) goto L_0x01f6;
    L_0x01ab:
        r19 = new org.jivesoftware.smack.proxy.ProxyException;
        r20 = org.jivesoftware.smack.proxy.ProxyInfo.ProxyType.SOCKS5;
        r0 = r19;
        r1 = r20;
        r0.m1835init(r1, r11, r5);
        throw r19;
    L_0x01b7:
        r19 = 3;
        r19 = r3[r19];	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r0 = r19;
        r0 = r0 & 255;
        r19 = r0;
        switch(r19) {
            case 1: goto L_0x01c5;
            case 2: goto L_0x01c4;
            case 3: goto L_0x01cf;
            case 4: goto L_0x01ec;
            default: goto L_0x01c4;
        };	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
    L_0x01c4:
        return r17;
    L_0x01c5:
        r19 = 6;
        r0 = r23;
        r1 = r19;
        r0.fill(r7, r3, r1);	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        goto L_0x01c4;
    L_0x01cf:
        r19 = 1;
        r0 = r23;
        r1 = r19;
        r0.fill(r7, r3, r1);	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r19 = 0;
        r19 = r3[r19];	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        r0 = r19;
        r0 = r0 & 255;
        r19 = r0;
        r19 = r19 + 2;
        r0 = r23;
        r1 = r19;
        r0.fill(r7, r3, r1);	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        goto L_0x01c4;
    L_0x01ec:
        r19 = 18;
        r0 = r23;
        r1 = r19;
        r0.fill(r7, r3, r1);	 Catch:{ RuntimeException -> 0x0090, Exception -> 0x0186 }
        goto L_0x01c4;
    L_0x01f6:
        r19 = new java.io.IOException;
        r0 = r19;
        r0.<init>(r11);
        throw r19;
    L_0x01fe:
        r19 = move-exception;
        goto L_0x0086;
    L_0x0201:
        r19 = move-exception;
        goto L_0x0167;
    L_0x0204:
        r19 = move-exception;
        goto L_0x018e;
    L_0x0206:
        r5 = move-exception;
        goto L_0x0189;
    L_0x0208:
        r5 = move-exception;
        goto L_0x0093;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jivesoftware.smack.proxy.Socks5ProxySocketFactory.socks5ProxifiedSocket(java.lang.String, int):java.net.Socket");
    }

    private void fill(InputStream in, byte[] buf, int len) throws IOException {
        int s = 0;
        while (s < len) {
            int i = in.read(buf, s, len - s);
            if (i <= 0) {
                throw new ProxyException(ProxyType.SOCKS5, "stream is closed");
            }
            s += i;
        }
    }
}
