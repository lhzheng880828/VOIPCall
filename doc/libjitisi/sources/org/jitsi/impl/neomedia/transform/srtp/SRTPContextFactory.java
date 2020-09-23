package org.jitsi.impl.neomedia.transform.srtp;

public class SRTPContextFactory {
    private SRTPCryptoContext defaultContext;
    private SRTCPCryptoContext defaultContextControl;

    public SRTPContextFactory(boolean sender, byte[] masterKey, byte[] masterSalt, SRTPPolicy srtpPolicy, SRTPPolicy srtcpPolicy) {
        this.defaultContext = new SRTPCryptoContext(sender, 0, 0, 0, masterKey, masterSalt, srtpPolicy);
        this.defaultContextControl = new SRTCPCryptoContext(0, masterKey, masterSalt, srtcpPolicy);
    }

    public void close() {
        if (this.defaultContext != null) {
            this.defaultContext.close();
            this.defaultContext = null;
        }
        if (this.defaultContextControl != null) {
            this.defaultContextControl.close();
            this.defaultContextControl = null;
        }
    }

    public SRTPCryptoContext getDefaultContext() {
        return this.defaultContext;
    }

    public SRTCPCryptoContext getDefaultContextControl() {
        return this.defaultContextControl;
    }
}
