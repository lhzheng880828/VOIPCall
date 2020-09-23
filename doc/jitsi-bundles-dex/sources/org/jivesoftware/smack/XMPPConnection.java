package org.jivesoftware.smack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import javax.net.ssl.SSLContext;
import org.apache.harmony.javax.security.auth.callback.CallbackHandler;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.XMPPError.Condition;
import org.jivesoftware.smack.util.StringUtils;

public class XMPPConnection extends Connection {
    private boolean anonymous = false;
    private boolean authenticated = false;
    private Collection<String> compressionMethods;
    private boolean connected = false;
    String connectionID = null;
    private SSLContext customSslContext = null;
    PacketReader packetReader;
    PacketWriter packetWriter;
    Roster roster = null;
    Socket socket;
    private String user = null;
    private boolean usingCompression;
    private boolean usingTLS = false;
    private boolean wasAuthenticated = false;

    public XMPPConnection(String serviceName, CallbackHandler callbackHandler) {
        super(new ConnectionConfiguration(serviceName));
        this.config.setCompressionEnabled(false);
        this.config.setSASLAuthenticationEnabled(true);
        this.config.setDebuggerEnabled(DEBUG_ENABLED);
        this.config.setCallbackHandler(callbackHandler);
    }

    public XMPPConnection(String serviceName) {
        super(new ConnectionConfiguration(serviceName));
        this.config.setCompressionEnabled(false);
        this.config.setSASLAuthenticationEnabled(true);
        this.config.setDebuggerEnabled(DEBUG_ENABLED);
    }

    public XMPPConnection(ConnectionConfiguration config) {
        super(config);
    }

    public XMPPConnection(ConnectionConfiguration config, CallbackHandler callbackHandler) {
        super(config);
        config.setCallbackHandler(callbackHandler);
    }

    public String getConnectionID() {
        if (isConnected()) {
            return this.connectionID;
        }
        return null;
    }

    public String getUser() {
        if (isAuthenticated()) {
            return this.user;
        }
        return null;
    }

    public void setCustomSslContext(SSLContext customSslContext) {
        this.customSslContext = customSslContext;
    }

    public synchronized void login(String username, String password, String resource) throws XMPPException {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to server.");
        } else if (this.authenticated) {
            throw new IllegalStateException("Already logged in to server.");
        } else {
            String response;
            username = username.toLowerCase().trim();
            if (!this.config.isSASLAuthenticationEnabled() || !this.saslAuthentication.hasNonAnonymousAuthentication()) {
                response = new NonSASLAuthentication(this).authenticate(username, password, resource);
            } else if (password != null) {
                response = this.saslAuthentication.authenticate(username, password, resource);
            } else {
                response = this.saslAuthentication.authenticate(username, resource, this.config.getCallbackHandler());
            }
            if (response != null) {
                this.user = response;
                this.config.setServiceName(StringUtils.parseServer(response));
            } else {
                this.user = username + Separators.AT + getServiceName();
                if (resource != null) {
                    this.user += Separators.SLASH + resource;
                }
            }
            if (this.config.isCompressionEnabled()) {
                useCompression();
            }
            this.authenticated = true;
            this.anonymous = false;
            if (this.roster == null) {
                this.roster = new Roster(this);
            }
            if (this.config.isRosterLoadedAtLogin()) {
                this.roster.reload();
            }
            if (this.config.isSendPresence()) {
                this.packetWriter.sendPacket(new Presence(Type.available));
            }
            this.config.setLoginInfo(username, password, resource);
            if (this.config.isDebuggerEnabled() && this.debugger != null) {
                this.debugger.userHasLogged(this.user);
            }
        }
    }

    public synchronized void loginAnonymously() throws XMPPException {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to server.");
        } else if (this.authenticated) {
            throw new IllegalStateException("Already logged in to server.");
        } else {
            String response;
            if (this.config.isSASLAuthenticationEnabled() && this.saslAuthentication.hasAnonymousAuthentication()) {
                response = this.saslAuthentication.authenticateAnonymously();
            } else {
                response = new NonSASLAuthentication(this).authenticateAnonymously();
            }
            this.user = response;
            this.config.setServiceName(StringUtils.parseServer(response));
            if (this.config.isCompressionEnabled()) {
                useCompression();
            }
            this.packetWriter.sendPacket(new Presence(Type.available));
            this.authenticated = true;
            this.anonymous = true;
            if (this.config.isDebuggerEnabled() && this.debugger != null) {
                this.debugger.userHasLogged(this.user);
            }
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0023, code skipped:
            if (r10.config.isRosterLoadedAtLogin() != false) goto L_0x002a;
     */
    /* JADX WARNING: Missing block: B:14:0x0025, code skipped:
            r10.roster.reload();
     */
    /* JADX WARNING: Missing block: B:16:0x002e, code skipped:
            if (r10.roster.rosterInitialized != false) goto L_0x0049;
     */
    /* JADX WARNING: Missing block: B:18:?, code skipped:
            r7 = r10.roster;
     */
    /* JADX WARNING: Missing block: B:19:0x0032, code skipped:
            monitor-enter(r7);
     */
    /* JADX WARNING: Missing block: B:21:?, code skipped:
            r4 = (long) org.jivesoftware.smack.SmackConfiguration.getPacketReplyTimeout();
            r2 = java.lang.System.currentTimeMillis();
     */
    /* JADX WARNING: Missing block: B:23:0x0040, code skipped:
            if (r10.roster.rosterInitialized != false) goto L_0x0048;
     */
    /* JADX WARNING: Missing block: B:25:0x0046, code skipped:
            if (r4 > 0) goto L_0x004f;
     */
    /* JADX WARNING: Missing block: B:26:0x0048, code skipped:
            monitor-exit(r7);
     */
    /* JADX WARNING: Missing block: B:33:?, code skipped:
            r10.roster.wait(r4);
            r0 = java.lang.System.currentTimeMillis();
            r4 = r4 - (r0 - r2);
            r2 = r0;
     */
    public org.jivesoftware.smack.Roster getRoster() {
        /*
        r10 = this;
        monitor-enter(r10);
        r6 = r10.isAuthenticated();	 Catch:{ all -> 0x004c }
        if (r6 == 0) goto L_0x000d;
    L_0x0007:
        r6 = r10.isAnonymous();	 Catch:{ all -> 0x004c }
        if (r6 == 0) goto L_0x001c;
    L_0x000d:
        r6 = r10.roster;	 Catch:{ all -> 0x004c }
        if (r6 != 0) goto L_0x0018;
    L_0x0011:
        r6 = new org.jivesoftware.smack.Roster;	 Catch:{ all -> 0x004c }
        r6.m1730init(r10);	 Catch:{ all -> 0x004c }
        r10.roster = r6;	 Catch:{ all -> 0x004c }
    L_0x0018:
        r6 = r10.roster;	 Catch:{ all -> 0x004c }
        monitor-exit(r10);	 Catch:{ all -> 0x004c }
    L_0x001b:
        return r6;
    L_0x001c:
        monitor-exit(r10);	 Catch:{ all -> 0x004c }
        r6 = r10.config;
        r6 = r6.isRosterLoadedAtLogin();
        if (r6 != 0) goto L_0x002a;
    L_0x0025:
        r6 = r10.roster;
        r6.reload();
    L_0x002a:
        r6 = r10.roster;
        r6 = r6.rosterInitialized;
        if (r6 != 0) goto L_0x0049;
    L_0x0030:
        r7 = r10.roster;	 Catch:{ InterruptedException -> 0x0060 }
        monitor-enter(r7);	 Catch:{ InterruptedException -> 0x0060 }
        r6 = org.jivesoftware.smack.SmackConfiguration.getPacketReplyTimeout();	 Catch:{ all -> 0x005d }
        r4 = (long) r6;	 Catch:{ all -> 0x005d }
        r2 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x005d }
    L_0x003c:
        r6 = r10.roster;	 Catch:{ all -> 0x005d }
        r6 = r6.rosterInitialized;	 Catch:{ all -> 0x005d }
        if (r6 != 0) goto L_0x0048;
    L_0x0042:
        r8 = 0;
        r6 = (r4 > r8 ? 1 : (r4 == r8 ? 0 : -1));
        if (r6 > 0) goto L_0x004f;
    L_0x0048:
        monitor-exit(r7);	 Catch:{ all -> 0x005d }
    L_0x0049:
        r6 = r10.roster;
        goto L_0x001b;
    L_0x004c:
        r6 = move-exception;
        monitor-exit(r10);	 Catch:{ all -> 0x004c }
        throw r6;
    L_0x004f:
        r6 = r10.roster;	 Catch:{ all -> 0x005d }
        r6.wait(r4);	 Catch:{ all -> 0x005d }
        r0 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x005d }
        r8 = r0 - r2;
        r4 = r4 - r8;
        r2 = r0;
        goto L_0x003c;
    L_0x005d:
        r6 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x005d }
        throw r6;	 Catch:{ InterruptedException -> 0x0060 }
    L_0x0060:
        r6 = move-exception;
        goto L_0x0049;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jivesoftware.smack.XMPPConnection.getRoster():org.jivesoftware.smack.Roster");
    }

    public boolean isConnected() {
        return this.connected;
    }

    public boolean isSecureConnection() {
        return isUsingTLS();
    }

    public boolean isAuthenticated() {
        return this.authenticated;
    }

    public boolean isAnonymous() {
        return this.anonymous;
    }

    /* access modifiers changed from: protected */
    public void shutdown(Presence unavailablePresence) {
        this.packetWriter.sendPacket(unavailablePresence);
        setWasAuthenticated(this.authenticated);
        this.authenticated = false;
        this.connected = false;
        this.packetReader.shutdown();
        this.packetWriter.shutdown();
        try {
            Thread.sleep(150);
        } catch (Exception e) {
        }
        if (this.reader != null) {
            try {
                this.reader.close();
            } catch (Throwable th) {
            }
            this.reader = null;
        }
        if (this.writer != null) {
            try {
                this.writer.close();
            } catch (Throwable th2) {
            }
            this.writer = null;
        }
        try {
            this.socket.close();
        } catch (Exception e2) {
        }
        this.saslAuthentication.init();
    }

    public synchronized void disconnect(Presence unavailablePresence) {
        if (!(this.packetReader == null || this.packetWriter == null)) {
            shutdown(unavailablePresence);
            if (this.roster != null) {
                this.roster.cleanup();
                this.roster = null;
            }
            this.wasAuthenticated = false;
            this.packetWriter.cleanup();
            this.packetWriter = null;
            this.packetReader.cleanup();
            this.packetReader = null;
        }
    }

    public void sendPacket(Packet packet) {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to server.");
        } else if (packet == null) {
            throw new NullPointerException("Packet is null.");
        } else {
            this.packetWriter.sendPacket(packet);
        }
    }

    public void addPacketWriterInterceptor(PacketInterceptor packetInterceptor, PacketFilter packetFilter) {
        addPacketInterceptor(packetInterceptor, packetFilter);
    }

    public void removePacketWriterInterceptor(PacketInterceptor packetInterceptor) {
        removePacketInterceptor(packetInterceptor);
    }

    public void addPacketWriterListener(PacketListener packetListener, PacketFilter packetFilter) {
        addPacketSendingListener(packetListener, packetFilter);
    }

    public void removePacketWriterListener(PacketListener packetListener) {
        removePacketSendingListener(packetListener);
    }

    private void connectUsingConfiguration(ConnectionConfiguration config) throws XMPPException {
        String errorMessage;
        String host = config.getHost();
        int port = config.getPort();
        try {
            if (config.getSocketFactory() == null) {
                this.socket = new Socket(host, port);
            } else {
                this.socket = config.getSocketFactory().createSocket(host, port);
            }
            initConnection();
        } catch (UnknownHostException uhe) {
            errorMessage = "Could not connect to " + host + Separators.COLON + port + Separators.DOT;
            throw new XMPPException(errorMessage, new XMPPError(Condition.remote_server_timeout, errorMessage), uhe);
        } catch (IOException ioe) {
            errorMessage = "XMPPError connecting to " + host + Separators.COLON + port + Separators.DOT;
            throw new XMPPException(errorMessage, new XMPPError(Condition.remote_server_error, errorMessage), ioe);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x0098 A:{SYNTHETIC, Splitter:B:39:0x0098} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00a3 A:{SYNTHETIC, Splitter:B:44:0x00a3} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x008d A:{SYNTHETIC, Splitter:B:34:0x008d} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0098 A:{SYNTHETIC, Splitter:B:39:0x0098} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00a3 A:{SYNTHETIC, Splitter:B:44:0x00a3} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0082 A:{SYNTHETIC, Splitter:B:29:0x0082} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x008d A:{SYNTHETIC, Splitter:B:34:0x008d} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0098 A:{SYNTHETIC, Splitter:B:39:0x0098} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00a3 A:{SYNTHETIC, Splitter:B:44:0x00a3} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00a3 A:{SYNTHETIC, Splitter:B:44:0x00a3} */
    private void initConnection() throws org.jivesoftware.smack.XMPPException {
        /*
        r8 = this;
        r2 = 1;
        r4 = 0;
        r7 = 0;
        r5 = r8.packetReader;
        if (r5 == 0) goto L_0x000b;
    L_0x0007:
        r5 = r8.packetWriter;
        if (r5 != 0) goto L_0x00b4;
    L_0x000b:
        if (r2 != 0) goto L_0x000f;
    L_0x000d:
        r8.usingCompression = r4;
    L_0x000f:
        r8.initReaderAndWriter();
        if (r2 == 0) goto L_0x00b7;
    L_0x0014:
        r5 = new org.jivesoftware.smack.PacketWriter;	 Catch:{ XMPPException -> 0x0072 }
        r5.m1707init(r8);	 Catch:{ XMPPException -> 0x0072 }
        r8.packetWriter = r5;	 Catch:{ XMPPException -> 0x0072 }
        r5 = new org.jivesoftware.smack.PacketReader;	 Catch:{ XMPPException -> 0x0072 }
        r5.m1704init(r8);	 Catch:{ XMPPException -> 0x0072 }
        r8.packetReader = r5;	 Catch:{ XMPPException -> 0x0072 }
        r5 = r8.config;	 Catch:{ XMPPException -> 0x0072 }
        r5 = r5.isDebuggerEnabled();	 Catch:{ XMPPException -> 0x0072 }
        if (r5 == 0) goto L_0x0046;
    L_0x002a:
        r5 = r8.debugger;	 Catch:{ XMPPException -> 0x0072 }
        r5 = r5.getReaderListener();	 Catch:{ XMPPException -> 0x0072 }
        r6 = 0;
        r8.addPacketListener(r5, r6);	 Catch:{ XMPPException -> 0x0072 }
        r5 = r8.debugger;	 Catch:{ XMPPException -> 0x0072 }
        r5 = r5.getWriterListener();	 Catch:{ XMPPException -> 0x0072 }
        if (r5 == 0) goto L_0x0046;
    L_0x003c:
        r5 = r8.debugger;	 Catch:{ XMPPException -> 0x0072 }
        r5 = r5.getWriterListener();	 Catch:{ XMPPException -> 0x0072 }
        r6 = 0;
        r8.addPacketSendingListener(r5, r6);	 Catch:{ XMPPException -> 0x0072 }
    L_0x0046:
        r5 = r8.packetWriter;	 Catch:{ XMPPException -> 0x0072 }
        r5.startup();	 Catch:{ XMPPException -> 0x0072 }
        r5 = r8.packetReader;	 Catch:{ XMPPException -> 0x0072 }
        r5.startup();	 Catch:{ XMPPException -> 0x0072 }
        r5 = 1;
        r8.connected = r5;	 Catch:{ XMPPException -> 0x0072 }
        r5 = r8.packetWriter;	 Catch:{ XMPPException -> 0x0072 }
        r5.startKeepAliveProcess();	 Catch:{ XMPPException -> 0x0072 }
        if (r2 == 0) goto L_0x00c2;
    L_0x005a:
        r5 = org.jivesoftware.smack.Connection.getConnectionCreationListeners();	 Catch:{ XMPPException -> 0x0072 }
        r1 = r5.iterator();	 Catch:{ XMPPException -> 0x0072 }
    L_0x0062:
        r5 = r1.hasNext();	 Catch:{ XMPPException -> 0x0072 }
        if (r5 == 0) goto L_0x00cb;
    L_0x0068:
        r3 = r1.next();	 Catch:{ XMPPException -> 0x0072 }
        r3 = (org.jivesoftware.smack.ConnectionCreationListener) r3;	 Catch:{ XMPPException -> 0x0072 }
        r3.connectionCreated(r8);	 Catch:{ XMPPException -> 0x0072 }
        goto L_0x0062;
    L_0x0072:
        r0 = move-exception;
        r5 = r8.packetWriter;
        if (r5 == 0) goto L_0x007e;
    L_0x0077:
        r5 = r8.packetWriter;	 Catch:{ Throwable -> 0x00d4 }
        r5.shutdown();	 Catch:{ Throwable -> 0x00d4 }
    L_0x007c:
        r8.packetWriter = r7;
    L_0x007e:
        r5 = r8.packetReader;
        if (r5 == 0) goto L_0x0089;
    L_0x0082:
        r5 = r8.packetReader;	 Catch:{ Throwable -> 0x00d2 }
        r5.shutdown();	 Catch:{ Throwable -> 0x00d2 }
    L_0x0087:
        r8.packetReader = r7;
    L_0x0089:
        r5 = r8.reader;
        if (r5 == 0) goto L_0x0094;
    L_0x008d:
        r5 = r8.reader;	 Catch:{ Throwable -> 0x00d0 }
        r5.close();	 Catch:{ Throwable -> 0x00d0 }
    L_0x0092:
        r8.reader = r7;
    L_0x0094:
        r5 = r8.writer;
        if (r5 == 0) goto L_0x009f;
    L_0x0098:
        r5 = r8.writer;	 Catch:{ Throwable -> 0x00ce }
        r5.close();	 Catch:{ Throwable -> 0x00ce }
    L_0x009d:
        r8.writer = r7;
    L_0x009f:
        r5 = r8.socket;
        if (r5 == 0) goto L_0x00aa;
    L_0x00a3:
        r5 = r8.socket;	 Catch:{ Exception -> 0x00cc }
        r5.close();	 Catch:{ Exception -> 0x00cc }
    L_0x00a8:
        r8.socket = r7;
    L_0x00aa:
        r5 = r8.authenticated;
        r8.setWasAuthenticated(r5);
        r8.authenticated = r4;
        r8.connected = r4;
        throw r0;
    L_0x00b4:
        r2 = r4;
        goto L_0x000b;
    L_0x00b7:
        r5 = r8.packetWriter;	 Catch:{ XMPPException -> 0x0072 }
        r5.init();	 Catch:{ XMPPException -> 0x0072 }
        r5 = r8.packetReader;	 Catch:{ XMPPException -> 0x0072 }
        r5.init();	 Catch:{ XMPPException -> 0x0072 }
        goto L_0x0046;
    L_0x00c2:
        r5 = r8.wasAuthenticated;	 Catch:{ XMPPException -> 0x0072 }
        if (r5 != 0) goto L_0x00cb;
    L_0x00c6:
        r5 = r8.packetReader;	 Catch:{ XMPPException -> 0x0072 }
        r5.notifyReconnection();	 Catch:{ XMPPException -> 0x0072 }
    L_0x00cb:
        return;
    L_0x00cc:
        r5 = move-exception;
        goto L_0x00a8;
    L_0x00ce:
        r5 = move-exception;
        goto L_0x009d;
    L_0x00d0:
        r5 = move-exception;
        goto L_0x0092;
    L_0x00d2:
        r5 = move-exception;
        goto L_0x0087;
    L_0x00d4:
        r5 = move-exception;
        goto L_0x007c;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jivesoftware.smack.XMPPConnection.initConnection():void");
    }

    private void initReaderAndWriter() throws XMPPException {
        try {
            if (this.usingCompression) {
                try {
                    Class<?> zoClass = Class.forName("com.jcraft.jzlib.ZOutputStream");
                    Object out = zoClass.getConstructor(new Class[]{OutputStream.class, Integer.TYPE}).newInstance(new Object[]{this.socket.getOutputStream(), Integer.valueOf(9)});
                    zoClass.getMethod("setFlushMode", new Class[]{Integer.TYPE}).invoke(out, new Object[]{Integer.valueOf(2)});
                    this.writer = new BufferedWriter(new OutputStreamWriter((OutputStream) out, "UTF-8"));
                    Class<?> ziClass = Class.forName("com.jcraft.jzlib.ZInputStream");
                    Object in = ziClass.getConstructor(new Class[]{InputStream.class}).newInstance(new Object[]{this.socket.getInputStream()});
                    ziClass.getMethod("setFlushMode", new Class[]{Integer.TYPE}).invoke(in, new Object[]{Integer.valueOf(2)});
                    this.reader = new BufferedReader(new InputStreamReader((InputStream) in, "UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                    this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
                    this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(), "UTF-8"));
                }
            } else {
                this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
                this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(), "UTF-8"));
            }
            initDebugger();
        } catch (IOException ioe) {
            throw new XMPPException("XMPPError establishing connection with server.", new XMPPError(Condition.remote_server_error, "XMPPError establishing connection with server."), ioe);
        }
    }

    public boolean isUsingTLS() {
        return this.usingTLS;
    }

    /* access modifiers changed from: 0000 */
    public void startTLSReceived(boolean required) {
        if (required && this.config.getSecurityMode() == SecurityMode.disabled) {
            this.packetReader.notifyConnectionError(new IllegalStateException("TLS required by server but not allowed by connection configuration"));
        } else if (this.config.getSecurityMode() != SecurityMode.disabled) {
            try {
                this.writer.write("<starttls xmlns=\"urn:ietf:params:xml:ns:xmpp-tls\"/>");
                this.writer.flush();
            } catch (IOException e) {
                this.packetReader.notifyConnectionError(e);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0208 A:{SYNTHETIC, Splitter:B:40:0x0208} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x00d2  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x021a  */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0018  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x00d2  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0208 A:{SYNTHETIC, Splitter:B:40:0x0208} */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0018  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x021a  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0208 A:{SYNTHETIC, Splitter:B:40:0x0208} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x00d2  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x021a  */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0018  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x00d2  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0208 A:{SYNTHETIC, Splitter:B:40:0x0208} */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0018  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x021a  */
    public void proceedTLSReceived() throws java.lang.Exception {
        /*
        r23 = this;
        r11 = 0;
        r10 = 0;
        r14 = 0;
        r0 = r23;
        r0 = r0.config;
        r18 = r0;
        r18 = r18.getCallbackHandler();
        if (r18 != 0) goto L_0x00b6;
    L_0x000f:
        r11 = 0;
    L_0x0010:
        r0 = r23;
        r0 = r0.customSslContext;
        r18 = r0;
        if (r18 != 0) goto L_0x021a;
    L_0x0018:
        r18 = "TLS";
        r7 = javax.net.ssl.SSLContext.getInstance(r18);
        r18 = 1;
        r0 = r18;
        r0 = new javax.net.ssl.TrustManager[r0];
        r18 = r0;
        r19 = 0;
        r20 = new org.jivesoftware.smack.ServerTrustManager;
        r21 = r23.getServiceName();
        r0 = r23;
        r0 = r0.config;
        r22 = r0;
        r20.m1736init(r21, r22);
        r18[r19] = r20;
        r19 = new java.security.SecureRandom;
        r19.<init>();
        r0 = r18;
        r1 = r19;
        r7.init(r10, r0, r1);
    L_0x0045:
        r0 = r23;
        r0 = r0.socket;
        r17 = r0;
        r18 = r7.getSocketFactory();
        r19 = r17.getInetAddress();
        r19 = r19.getHostAddress();
        r20 = r17.getPort();
        r21 = 1;
        r0 = r18;
        r1 = r17;
        r2 = r19;
        r3 = r20;
        r4 = r21;
        r18 = r0.createSocket(r1, r2, r3, r4);
        r0 = r18;
        r1 = r23;
        r1.socket = r0;
        r0 = r23;
        r0 = r0.socket;
        r18 = r0;
        r19 = 0;
        r18.setSoTimeout(r19);
        r0 = r23;
        r0 = r0.socket;
        r18 = r0;
        r19 = 1;
        r18.setKeepAlive(r19);
        r23.initReaderAndWriter();
        r0 = r23;
        r0 = r0.socket;
        r18 = r0;
        r18 = (javax.net.ssl.SSLSocket) r18;
        r18.startHandshake();
        r18 = 1;
        r0 = r18;
        r1 = r23;
        r1.usingTLS = r0;
        r0 = r23;
        r0 = r0.packetWriter;
        r18 = r0;
        r0 = r23;
        r0 = r0.writer;
        r19 = r0;
        r18.setWriter(r19);
        r0 = r23;
        r0 = r0.packetWriter;
        r18 = r0;
        r18.openStream();
        return;
    L_0x00b6:
        r0 = r23;
        r0 = r0.config;
        r18 = r0;
        r18 = r18.getKeystoreType();
        r19 = "NONE";
        r18 = r18.equals(r19);
        if (r18 == 0) goto L_0x00df;
    L_0x00c8:
        r11 = 0;
        r14 = 0;
    L_0x00ca:
        r18 = "SunX509";
        r9 = javax.net.ssl.KeyManagerFactory.getInstance(r18);
        if (r14 != 0) goto L_0x0208;
    L_0x00d2:
        r18 = 0;
        r0 = r18;
        r9.init(r11, r0);	 Catch:{ NullPointerException -> 0x0216 }
    L_0x00d9:
        r10 = r9.getKeyManagers();	 Catch:{ NullPointerException -> 0x0216 }
        goto L_0x0010;
    L_0x00df:
        r0 = r23;
        r0 = r0.config;
        r18 = r0;
        r18 = r18.getKeystoreType();
        r19 = "PKCS11";
        r18 = r18.equals(r19);
        if (r18 == 0) goto L_0x018b;
    L_0x00f1:
        r18 = "sun.security.pkcs11.SunPKCS11";
        r18 = java.lang.Class.forName(r18);	 Catch:{ Exception -> 0x0186 }
        r19 = 1;
        r0 = r19;
        r0 = new java.lang.Class[r0];	 Catch:{ Exception -> 0x0186 }
        r19 = r0;
        r20 = 0;
        r21 = java.io.InputStream.class;
        r19[r20] = r21;	 Catch:{ Exception -> 0x0186 }
        r5 = r18.getConstructor(r19);	 Catch:{ Exception -> 0x0186 }
        r18 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0186 }
        r18.<init>();	 Catch:{ Exception -> 0x0186 }
        r19 = "name = SmartCard\nlibrary = ";
        r18 = r18.append(r19);	 Catch:{ Exception -> 0x0186 }
        r0 = r23;
        r0 = r0.config;	 Catch:{ Exception -> 0x0186 }
        r19 = r0;
        r19 = r19.getPKCS11Library();	 Catch:{ Exception -> 0x0186 }
        r18 = r18.append(r19);	 Catch:{ Exception -> 0x0186 }
        r16 = r18.toString();	 Catch:{ Exception -> 0x0186 }
        r6 = new java.io.ByteArrayInputStream;	 Catch:{ Exception -> 0x0186 }
        r18 = r16.getBytes();	 Catch:{ Exception -> 0x0186 }
        r0 = r18;
        r6.<init>(r0);	 Catch:{ Exception -> 0x0186 }
        r18 = 1;
        r0 = r18;
        r0 = new java.lang.Object[r0];	 Catch:{ Exception -> 0x0186 }
        r18 = r0;
        r19 = 0;
        r18[r19] = r6;	 Catch:{ Exception -> 0x0186 }
        r0 = r18;
        r13 = r5.newInstance(r0);	 Catch:{ Exception -> 0x0186 }
        r13 = (java.security.Provider) r13;	 Catch:{ Exception -> 0x0186 }
        java.security.Security.addProvider(r13);	 Catch:{ Exception -> 0x0186 }
        r18 = "PKCS11";
        r0 = r18;
        r11 = java.security.KeyStore.getInstance(r0, r13);	 Catch:{ Exception -> 0x0186 }
        r15 = new org.apache.harmony.javax.security.auth.callback.PasswordCallback;	 Catch:{ Exception -> 0x0186 }
        r18 = "PKCS11 Password: ";
        r19 = 0;
        r0 = r18;
        r1 = r19;
        r15.<init>(r0, r1);	 Catch:{ Exception -> 0x0186 }
        r0 = r23;
        r0 = r0.config;	 Catch:{ Exception -> 0x0223 }
        r18 = r0;
        r18 = r18.getCallbackHandler();	 Catch:{ Exception -> 0x0223 }
        r19 = 1;
        r0 = r19;
        r0 = new org.apache.harmony.javax.security.auth.callback.Callback[r0];	 Catch:{ Exception -> 0x0223 }
        r19 = r0;
        r20 = 0;
        r19[r20] = r15;	 Catch:{ Exception -> 0x0223 }
        r18.handle(r19);	 Catch:{ Exception -> 0x0223 }
        r18 = 0;
        r19 = r15.getPassword();	 Catch:{ Exception -> 0x0223 }
        r0 = r18;
        r1 = r19;
        r11.load(r0, r1);	 Catch:{ Exception -> 0x0223 }
        r14 = r15;
        goto L_0x00ca;
    L_0x0186:
        r8 = move-exception;
    L_0x0187:
        r11 = 0;
        r14 = 0;
        goto L_0x00ca;
    L_0x018b:
        r0 = r23;
        r0 = r0.config;
        r18 = r0;
        r18 = r18.getKeystoreType();
        r19 = "Apple";
        r18 = r18.equals(r19);
        if (r18 == 0) goto L_0x01b2;
    L_0x019d:
        r18 = "KeychainStore";
        r19 = "Apple";
        r11 = java.security.KeyStore.getInstance(r18, r19);
        r18 = 0;
        r19 = 0;
        r0 = r18;
        r1 = r19;
        r11.load(r0, r1);
        goto L_0x00ca;
    L_0x01b2:
        r0 = r23;
        r0 = r0.config;
        r18 = r0;
        r18 = r18.getKeystoreType();
        r11 = java.security.KeyStore.getInstance(r18);
        r15 = new org.apache.harmony.javax.security.auth.callback.PasswordCallback;	 Catch:{ Exception -> 0x0203 }
        r18 = "Keystore Password: ";
        r19 = 0;
        r0 = r18;
        r1 = r19;
        r15.<init>(r0, r1);	 Catch:{ Exception -> 0x0203 }
        r0 = r23;
        r0 = r0.config;	 Catch:{ Exception -> 0x0220 }
        r18 = r0;
        r18 = r18.getCallbackHandler();	 Catch:{ Exception -> 0x0220 }
        r19 = 1;
        r0 = r19;
        r0 = new org.apache.harmony.javax.security.auth.callback.Callback[r0];	 Catch:{ Exception -> 0x0220 }
        r19 = r0;
        r20 = 0;
        r19[r20] = r15;	 Catch:{ Exception -> 0x0220 }
        r18.handle(r19);	 Catch:{ Exception -> 0x0220 }
        r18 = new java.io.FileInputStream;	 Catch:{ Exception -> 0x0220 }
        r0 = r23;
        r0 = r0.config;	 Catch:{ Exception -> 0x0220 }
        r19 = r0;
        r19 = r19.getKeystorePath();	 Catch:{ Exception -> 0x0220 }
        r18.<init>(r19);	 Catch:{ Exception -> 0x0220 }
        r19 = r15.getPassword();	 Catch:{ Exception -> 0x0220 }
        r0 = r18;
        r1 = r19;
        r11.load(r0, r1);	 Catch:{ Exception -> 0x0220 }
        r14 = r15;
        goto L_0x00ca;
    L_0x0203:
        r8 = move-exception;
    L_0x0204:
        r11 = 0;
        r14 = 0;
        goto L_0x00ca;
    L_0x0208:
        r18 = r14.getPassword();	 Catch:{ NullPointerException -> 0x0216 }
        r0 = r18;
        r9.init(r11, r0);	 Catch:{ NullPointerException -> 0x0216 }
        r14.clearPassword();	 Catch:{ NullPointerException -> 0x0216 }
        goto L_0x00d9;
    L_0x0216:
        r12 = move-exception;
        r10 = 0;
        goto L_0x0010;
    L_0x021a:
        r0 = r23;
        r7 = r0.customSslContext;
        goto L_0x0045;
    L_0x0220:
        r8 = move-exception;
        r14 = r15;
        goto L_0x0204;
    L_0x0223:
        r8 = move-exception;
        r14 = r15;
        goto L_0x0187;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jivesoftware.smack.XMPPConnection.proceedTLSReceived():void");
    }

    /* access modifiers changed from: 0000 */
    public void setAvailableCompressionMethods(Collection<String> methods) {
        this.compressionMethods = methods;
    }

    private boolean hasAvailableCompressionMethod(String method) {
        return this.compressionMethods != null && this.compressionMethods.contains(method);
    }

    public boolean isUsingCompression() {
        return this.usingCompression;
    }

    private boolean useCompression() {
        if (this.authenticated) {
            throw new IllegalStateException("Compression should be negotiated before authentication.");
        }
        try {
            Class.forName("com.jcraft.jzlib.ZOutputStream");
            if (!hasAvailableCompressionMethod("zlib")) {
                return false;
            }
            requestStreamCompression();
            synchronized (this) {
                try {
                    wait((long) (SmackConfiguration.getPacketReplyTimeout() * 5));
                } catch (InterruptedException e) {
                }
            }
            return this.usingCompression;
        } catch (ClassNotFoundException e2) {
            throw new IllegalStateException("Cannot use compression. Add smackx.jar to the classpath");
        }
    }

    private void requestStreamCompression() {
        try {
            this.writer.write("<compress xmlns='http://jabber.org/protocol/compress'>");
            this.writer.write("<method>zlib</method></compress>");
            this.writer.flush();
        } catch (IOException e) {
            this.packetReader.notifyConnectionError(e);
        }
    }

    /* access modifiers changed from: 0000 */
    public void startStreamCompression() throws Exception {
        this.usingCompression = true;
        initReaderAndWriter();
        this.packetWriter.setWriter(this.writer);
        this.packetWriter.openStream();
        synchronized (this) {
            notify();
        }
    }

    /* access modifiers changed from: 0000 */
    public void streamCompressionDenied() {
        synchronized (this) {
            notify();
        }
    }

    public void connect() throws XMPPException {
        connectUsingConfiguration(this.config);
        if (this.connected && this.wasAuthenticated) {
            try {
                if (isAnonymous()) {
                    loginAnonymously();
                } else {
                    login(this.config.getUsername(), this.config.getPassword(), this.config.getResource());
                }
                this.packetReader.notifyReconnection();
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    }

    private void setWasAuthenticated(boolean wasAuthenticated) {
        if (!this.wasAuthenticated) {
            this.wasAuthenticated = wasAuthenticated;
        }
    }

    public Socket getSocket() {
        return this.socket;
    }
}
