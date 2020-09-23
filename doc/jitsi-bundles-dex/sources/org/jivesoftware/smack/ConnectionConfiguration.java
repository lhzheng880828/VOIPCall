package org.jivesoftware.smack;

import java.io.File;
import javax.net.SocketFactory;
import org.apache.harmony.javax.security.auth.callback.CallbackHandler;
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.jivesoftware.smack.util.DNSUtil;
import org.jivesoftware.smack.util.DNSUtil.HostAddress;

public class ConnectionConfiguration implements Cloneable {
    private CallbackHandler callbackHandler;
    private boolean compressionEnabled = false;
    private boolean debuggerEnabled = Connection.DEBUG_ENABLED;
    private boolean expiredCertificatesCheckEnabled = false;
    private String host;
    private String keystorePath;
    private String keystoreType;
    private boolean notMatchingDomainCheckEnabled = false;
    private String password;
    private String pkcs11Library;
    private int port;
    protected ProxyInfo proxy;
    private boolean reconnectionAllowed = true;
    private String resource;
    private boolean rosterLoadedAtLogin = true;
    private boolean saslAuthenticationEnabled = true;
    private SecurityMode securityMode = SecurityMode.enabled;
    private boolean selfSignedCertificateEnabled = false;
    private boolean sendPresence = true;
    private String serviceName;
    private SocketFactory socketFactory;
    private String truststorePassword;
    private String truststorePath;
    private String truststoreType;
    private String username;
    private boolean verifyChainEnabled = false;
    private boolean verifyRootCAEnabled = false;

    public enum SecurityMode {
        required,
        enabled,
        disabled
    }

    public ConnectionConfiguration(String serviceName) {
        HostAddress address = DNSUtil.resolveXMPPDomain(serviceName);
        init(address.getHost(), address.getPort(), serviceName, ProxyInfo.forDefaultProxy());
    }

    public ConnectionConfiguration(String serviceName, ProxyInfo proxy) {
        HostAddress address = DNSUtil.resolveXMPPDomain(serviceName);
        init(address.getHost(), address.getPort(), serviceName, proxy);
    }

    public ConnectionConfiguration(String host, int port, String serviceName) {
        init(host, port, serviceName, ProxyInfo.forDefaultProxy());
    }

    public ConnectionConfiguration(String host, int port, String serviceName, ProxyInfo proxy) {
        init(host, port, serviceName, proxy);
    }

    public ConnectionConfiguration(String host, int port) {
        init(host, port, host, ProxyInfo.forDefaultProxy());
    }

    public ConnectionConfiguration(String host, int port, ProxyInfo proxy) {
        init(host, port, host, proxy);
    }

    private void init(String host, int port, String serviceName, ProxyInfo proxy) {
        this.host = host;
        this.port = port;
        this.serviceName = serviceName;
        this.proxy = proxy;
        String javaHome = System.getProperty("java.home");
        StringBuilder buffer = new StringBuilder();
        buffer.append(javaHome).append(File.separator).append("lib");
        buffer.append(File.separator).append("security");
        buffer.append(File.separator).append("cacerts");
        this.truststorePath = buffer.toString();
        this.truststoreType = "jks";
        this.truststorePassword = "changeit";
        this.keystorePath = System.getProperty("javax.net.ssl.keyStore");
        this.keystoreType = "jks";
        this.pkcs11Library = "pkcs11.config";
        this.socketFactory = proxy.getSocketFactory();
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public SecurityMode getSecurityMode() {
        return this.securityMode;
    }

    public void setSecurityMode(SecurityMode securityMode) {
        this.securityMode = securityMode;
    }

    public String getTruststorePath() {
        return this.truststorePath;
    }

    public void setTruststorePath(String truststorePath) {
        this.truststorePath = truststorePath;
    }

    public String getTruststoreType() {
        return this.truststoreType;
    }

    public void setTruststoreType(String truststoreType) {
        this.truststoreType = truststoreType;
    }

    public String getTruststorePassword() {
        return this.truststorePassword;
    }

    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }

    public String getKeystorePath() {
        return this.keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public String getKeystoreType() {
        return this.keystoreType;
    }

    public void setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
    }

    public String getPKCS11Library() {
        return this.pkcs11Library;
    }

    public void setPKCS11Library(String pkcs11Library) {
        this.pkcs11Library = pkcs11Library;
    }

    public boolean isVerifyChainEnabled() {
        return this.verifyChainEnabled;
    }

    public void setVerifyChainEnabled(boolean verifyChainEnabled) {
        this.verifyChainEnabled = verifyChainEnabled;
    }

    public boolean isVerifyRootCAEnabled() {
        return this.verifyRootCAEnabled;
    }

    public void setVerifyRootCAEnabled(boolean verifyRootCAEnabled) {
        this.verifyRootCAEnabled = verifyRootCAEnabled;
    }

    public boolean isSelfSignedCertificateEnabled() {
        return this.selfSignedCertificateEnabled;
    }

    public void setSelfSignedCertificateEnabled(boolean selfSignedCertificateEnabled) {
        this.selfSignedCertificateEnabled = selfSignedCertificateEnabled;
    }

    public boolean isExpiredCertificatesCheckEnabled() {
        return this.expiredCertificatesCheckEnabled;
    }

    public void setExpiredCertificatesCheckEnabled(boolean expiredCertificatesCheckEnabled) {
        this.expiredCertificatesCheckEnabled = expiredCertificatesCheckEnabled;
    }

    public boolean isNotMatchingDomainCheckEnabled() {
        return this.notMatchingDomainCheckEnabled;
    }

    public void setNotMatchingDomainCheckEnabled(boolean notMatchingDomainCheckEnabled) {
        this.notMatchingDomainCheckEnabled = notMatchingDomainCheckEnabled;
    }

    public boolean isCompressionEnabled() {
        return this.compressionEnabled;
    }

    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }

    public boolean isSASLAuthenticationEnabled() {
        return this.saslAuthenticationEnabled;
    }

    public void setSASLAuthenticationEnabled(boolean saslAuthenticationEnabled) {
        this.saslAuthenticationEnabled = saslAuthenticationEnabled;
    }

    public boolean isDebuggerEnabled() {
        return this.debuggerEnabled;
    }

    public void setDebuggerEnabled(boolean debuggerEnabled) {
        this.debuggerEnabled = debuggerEnabled;
    }

    public void setReconnectionAllowed(boolean isAllowed) {
        this.reconnectionAllowed = isAllowed;
    }

    public boolean isReconnectionAllowed() {
        return this.reconnectionAllowed;
    }

    public void setSocketFactory(SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    public void setSendPresence(boolean sendPresence) {
        this.sendPresence = sendPresence;
    }

    public boolean isRosterLoadedAtLogin() {
        return this.rosterLoadedAtLogin;
    }

    public void setRosterLoadedAtLogin(boolean rosterLoadedAtLogin) {
        this.rosterLoadedAtLogin = rosterLoadedAtLogin;
    }

    public CallbackHandler getCallbackHandler() {
        return this.callbackHandler;
    }

    public void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    public SocketFactory getSocketFactory() {
        return this.socketFactory;
    }

    /* access modifiers changed from: 0000 */
    public String getUsername() {
        return this.username;
    }

    /* access modifiers changed from: 0000 */
    public String getPassword() {
        return this.password;
    }

    /* access modifiers changed from: 0000 */
    public String getResource() {
        return this.resource;
    }

    /* access modifiers changed from: 0000 */
    public boolean isSendPresence() {
        return this.sendPresence;
    }

    /* access modifiers changed from: 0000 */
    public void setLoginInfo(String username, String password, String resource) {
        this.username = username;
        this.password = password;
        this.resource = resource;
    }
}
