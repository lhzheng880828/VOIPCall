package org.jitsi.impl.neomedia.transform.zrtp;

import gnu.java.zrtp.ZrtpCodes.MessageSeverity;
import gnu.java.zrtp.utils.ZrtpUtils;
import java.util.EnumSet;
import org.jitsi.impl.neomedia.AbstractRTPConnector;
import org.jitsi.service.neomedia.AbstractSrtpControl;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.SrtpControl;
import org.jitsi.service.neomedia.SrtpControlType;
import org.jitsi.service.neomedia.ZrtpControl;

public class ZrtpControlImpl extends AbstractSrtpControl<ZRTPTransformEngine> implements ZrtpControl {
    private boolean masterSession = false;
    private AbstractRTPConnector zrtpConnector = null;

    public enum ZRTPCustomInfoCodes {
        ZRTPDisabledByCallEnd,
        ZRTPEnabledByDefault,
        ZRTPEngineInitFailure,
        ZRTPNotEnabledByUser
    }

    public ZrtpControlImpl() {
        super(SrtpControlType.ZRTP);
    }

    public void cleanup() {
        super.cleanup();
        this.zrtpConnector = null;
    }

    public String getCipherString() {
        return ((ZRTPTransformEngine) getTransformEngine()).getUserCallback().getCipherString();
    }

    public int getCurrentProtocolVersion() {
        ZRTPTransformEngine zrtpEngine = this.transformEngine;
        return zrtpEngine != null ? zrtpEngine.getCurrentProtocolVersion() : 0;
    }

    public String getHelloHash(int index) {
        return ((ZRTPTransformEngine) getTransformEngine()).getHelloHash(index);
    }

    public String[] getHelloHashSep(int index) {
        return ((ZRTPTransformEngine) getTransformEngine()).getHelloHashSep(index);
    }

    public int getNumberSupportedVersions() {
        ZRTPTransformEngine zrtpEngine = this.transformEngine;
        return zrtpEngine != null ? zrtpEngine.getNumberSupportedVersions() : 0;
    }

    public String getPeerHelloHash() {
        ZRTPTransformEngine zrtpEngine = this.transformEngine;
        if (zrtpEngine != null) {
            return zrtpEngine.getPeerHelloHash();
        }
        return new String();
    }

    public byte[] getPeerZid() {
        return ((ZRTPTransformEngine) getTransformEngine()).getPeerZid();
    }

    public String getPeerZidString() {
        byte[] zid = getPeerZid();
        return new String(ZrtpUtils.bytesToHexString(zid, zid.length));
    }

    public boolean getSecureCommunicationStatus() {
        ZRTPTransformEngine zrtpEngine = this.transformEngine;
        return zrtpEngine != null && zrtpEngine.getSecureCommunicationStatus();
    }

    public String getSecurityString() {
        return ((ZRTPTransformEngine) getTransformEngine()).getUserCallback().getSecurityString();
    }

    public long getTimeoutValue() {
        return 3750;
    }

    /* access modifiers changed from: protected */
    public ZRTPTransformEngine createTransformEngine() {
        ZRTPTransformEngine transformEngine = new ZRTPTransformEngine();
        transformEngine.initialize("GNUZRTP4J.zid", false, ZrtpConfigureUtils.getZrtpConfiguration());
        transformEngine.setUserCallback(new SecurityEventManager(this));
        return transformEngine;
    }

    public boolean isSecurityVerified() {
        return ((ZRTPTransformEngine) getTransformEngine()).getUserCallback().isSecurityVerified();
    }

    public boolean requiresSecureSignalingTransport() {
        return false;
    }

    public void setConnector(AbstractRTPConnector connector) {
        this.zrtpConnector = connector;
    }

    public void setMasterSession(boolean masterSession) {
        if (masterSession) {
            this.masterSession = masterSession;
        }
    }

    public void setMultistream(SrtpControl master) {
        if (master != null && master != this) {
            if (master instanceof ZrtpControlImpl) {
                ZRTPTransformEngine engine = (ZRTPTransformEngine) getTransformEngine();
                engine.setMultiStrParams(((ZRTPTransformEngine) ((ZrtpControlImpl) master).getTransformEngine()).getMultiStrParams());
                engine.setEnableZrtp(true);
                return;
            }
            throw new IllegalArgumentException("master is no ZRTP control");
        }
    }

    public void setSASVerification(boolean verified) {
        ZRTPTransformEngine engine = (ZRTPTransformEngine) getTransformEngine();
        if (verified) {
            engine.SASVerified();
        } else {
            engine.resetSASVerified();
        }
    }

    public void start(MediaType mediaType) {
        boolean zrtpAutoStart;
        ZRTPTransformEngine engine = (ZRTPTransformEngine) getTransformEngine();
        SecurityEventManager securityEventManager = engine.getUserCallback();
        if (this.masterSession) {
            zrtpAutoStart = true;
            securityEventManager.setDHSession(true);
            securityEventManager.setSessionType(mediaType);
        } else {
            zrtpAutoStart = ((ZRTPTransformEngine) this.transformEngine).isEnableZrtp();
            securityEventManager.setSessionType(mediaType);
        }
        engine.setConnector(this.zrtpConnector);
        securityEventManager.setSrtpListener(getSrtpListener());
        engine.setEnableZrtp(zrtpAutoStart);
        engine.sendInfo(MessageSeverity.Info, EnumSet.of(ZRTPCustomInfoCodes.ZRTPEnabledByDefault));
    }
}
