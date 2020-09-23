package org.jivesoftware.smack.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.SocketFactory;

public class Socks4ProxySocketFactory extends SocketFactory {
    private ProxyInfo proxy;

    public Socks4ProxySocketFactory(ProxyInfo proxy) {
        this.proxy = proxy;
    }

    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return socks4ProxifiedSocket(host, port);
    }

    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return socks4ProxifiedSocket(host, port);
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        return socks4ProxifiedSocket(host.getHostAddress(), port);
    }

    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return socks4ProxifiedSocket(address.getHostAddress(), port);
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:17:0x0092=Splitter:B:17:0x0092, B:52:0x013b=Splitter:B:52:0x013b} */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:17:0x0092=Splitter:B:17:0x0092, B:52:0x013b=Splitter:B:52:0x013b} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00f5 A:{SYNTHETIC, Splitter:B:36:0x00f5} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x00a6 A:{ExcHandler: RuntimeException (e java.lang.RuntimeException), Splitter:B:3:0x0037} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:20:0x00a6, code skipped:
            r7 = e;
     */
    /* JADX WARNING: Missing block: B:21:0x00a7, code skipped:
            r19 = r0;
     */
    /* JADX WARNING: Missing block: B:33:0x00f0, code skipped:
            r7 = e;
     */
    /* JADX WARNING: Missing block: B:34:0x00f1, code skipped:
            r19 = r0;
     */
    private java.net.Socket socks4ProxifiedSocket(java.lang.String r29, int r30) throws java.io.IOException {
        /*
        r28 = this;
        r19 = 0;
        r9 = 0;
        r14 = 0;
        r0 = r28;
        r0 = r0.proxy;
        r24 = r0;
        r16 = r24.getProxyAddress();
        r0 = r28;
        r0 = r0.proxy;
        r24 = r0;
        r17 = r24.getProxyPort();
        r0 = r28;
        r0 = r0.proxy;
        r24 = r0;
        r23 = r24.getProxyUsername();
        r0 = r28;
        r0 = r0.proxy;
        r24 = r0;
        r15 = r24.getProxyPassword();
        r20 = new java.net.Socket;	 Catch:{ RuntimeException -> 0x017b, Exception -> 0x0178 }
        r0 = r20;
        r1 = r16;
        r2 = r17;
        r0.<init>(r1, r2);	 Catch:{ RuntimeException -> 0x017b, Exception -> 0x0178 }
        r9 = r20.getInputStream();	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r14 = r20.getOutputStream();	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r24 = 1;
        r0 = r20;
        r1 = r24;
        r0.setTcpNoDelay(r1);	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r24 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
        r0 = r24;
        r5 = new byte[r0];	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r10 = 0;
        r10 = 0;
        r11 = r10 + 1;
        r24 = 4;
        r5[r10] = r24;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r10 = r11 + 1;
        r24 = 1;
        r5[r11] = r24;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r11 = r10 + 1;
        r24 = r30 >>> 8;
        r0 = r24;
        r0 = (byte) r0;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r24 = r0;
        r5[r10] = r24;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r10 = r11 + 1;
        r0 = r30;
        r0 = r0 & 255;
        r24 = r0;
        r0 = r24;
        r0 = (byte) r0;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r24 = r0;
        r5[r11] = r24;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r4 = java.net.InetAddress.getByName(r29);	 Catch:{ UnknownHostException -> 0x0091 }
        r6 = r4.getAddress();	 Catch:{ UnknownHostException -> 0x0091 }
        r8 = 0;
        r11 = r10;
    L_0x0080:
        r0 = r6.length;	 Catch:{ UnknownHostException -> 0x017e }
        r24 = r0;
        r0 = r24;
        if (r8 >= r0) goto L_0x00aa;
    L_0x0087:
        r10 = r11 + 1;
        r24 = r6[r8];	 Catch:{ UnknownHostException -> 0x0091 }
        r5[r11] = r24;	 Catch:{ UnknownHostException -> 0x0091 }
        r8 = r8 + 1;
        r11 = r10;
        goto L_0x0080;
    L_0x0091:
        r22 = move-exception;
    L_0x0092:
        r24 = new org.jivesoftware.smack.proxy.ProxyException;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r25 = org.jivesoftware.smack.proxy.ProxyInfo.ProxyType.SOCKS4;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r26 = r22.toString();	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r0 = r24;
        r1 = r25;
        r2 = r26;
        r3 = r22;
        r0.m1835init(r1, r2, r3);	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        throw r24;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
    L_0x00a6:
        r7 = move-exception;
        r19 = r20;
    L_0x00a9:
        throw r7;
    L_0x00aa:
        if (r23 == 0) goto L_0x00c6;
    L_0x00ac:
        r24 = r23.getBytes();	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r25 = 0;
        r26 = r23.length();	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r0 = r24;
        r1 = r25;
        r2 = r26;
        java.lang.System.arraycopy(r0, r1, r5, r11, r2);	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r24 = r23.length();	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r10 = r11 + r24;
        r11 = r10;
    L_0x00c6:
        r10 = r11 + 1;
        r24 = 0;
        r5[r11] = r24;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r24 = 0;
        r0 = r24;
        r14.write(r5, r0, r10);	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r12 = 6;
        r18 = 0;
    L_0x00d6:
        r0 = r18;
        if (r0 >= r12) goto L_0x0107;
    L_0x00da:
        r24 = r12 - r18;
        r0 = r18;
        r1 = r24;
        r8 = r9.read(r5, r0, r1);	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        if (r8 > 0) goto L_0x0104;
    L_0x00e6:
        r24 = new org.jivesoftware.smack.proxy.ProxyException;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r25 = org.jivesoftware.smack.proxy.ProxyInfo.ProxyType.SOCKS4;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r26 = "stream is closed";
        r24.m1834init(r25, r26);	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        throw r24;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
    L_0x00f0:
        r7 = move-exception;
        r19 = r20;
    L_0x00f3:
        if (r19 == 0) goto L_0x00f8;
    L_0x00f5:
        r19.close();	 Catch:{ Exception -> 0x0176 }
    L_0x00f8:
        r24 = new org.jivesoftware.smack.proxy.ProxyException;
        r25 = org.jivesoftware.smack.proxy.ProxyInfo.ProxyType.SOCKS4;
        r26 = r7.toString();
        r24.m1834init(r25, r26);
        throw r24;
    L_0x0104:
        r18 = r18 + r8;
        goto L_0x00d6;
    L_0x0107:
        r24 = 0;
        r24 = r5[r24];	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        if (r24 == 0) goto L_0x012c;
    L_0x010d:
        r24 = new org.jivesoftware.smack.proxy.ProxyException;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r25 = org.jivesoftware.smack.proxy.ProxyInfo.ProxyType.SOCKS4;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r26 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r26.<init>();	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r27 = "server returns VN ";
        r26 = r26.append(r27);	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r27 = 0;
        r27 = r5[r27];	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r26 = r26.append(r27);	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r26 = r26.toString();	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r24.m1834init(r25, r26);	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        throw r24;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
    L_0x012c:
        r24 = 1;
        r24 = r5[r24];	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r25 = 90;
        r0 = r24;
        r1 = r25;
        if (r0 == r1) goto L_0x015e;
    L_0x0138:
        r20.close();	 Catch:{ Exception -> 0x0174, RuntimeException -> 0x00a6 }
    L_0x013b:
        r24 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r24.<init>();	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r25 = "ProxySOCKS4: server returns CD ";
        r24 = r24.append(r25);	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r25 = 1;
        r25 = r5[r25];	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r24 = r24.append(r25);	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r13 = r24.toString();	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r24 = new org.jivesoftware.smack.proxy.ProxyException;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r25 = org.jivesoftware.smack.proxy.ProxyInfo.ProxyType.SOCKS4;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r0 = r24;
        r1 = r25;
        r0.m1834init(r1, r13);	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        throw r24;	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
    L_0x015e:
        r24 = 2;
        r0 = r24;
        r0 = new byte[r0];	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        r21 = r0;
        r24 = 0;
        r25 = 2;
        r0 = r21;
        r1 = r24;
        r2 = r25;
        r9.read(r0, r1, r2);	 Catch:{ RuntimeException -> 0x00a6, Exception -> 0x00f0 }
        return r20;
    L_0x0174:
        r24 = move-exception;
        goto L_0x013b;
    L_0x0176:
        r24 = move-exception;
        goto L_0x00f8;
    L_0x0178:
        r7 = move-exception;
        goto L_0x00f3;
    L_0x017b:
        r7 = move-exception;
        goto L_0x00a9;
    L_0x017e:
        r22 = move-exception;
        r10 = r11;
        goto L_0x0092;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jivesoftware.smack.proxy.Socks4ProxySocketFactory.socks4ProxifiedSocket(java.lang.String, int):java.net.Socket");
    }
}
