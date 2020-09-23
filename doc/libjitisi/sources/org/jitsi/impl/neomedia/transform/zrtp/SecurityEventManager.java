package org.jitsi.impl.neomedia.transform.zrtp;

import gnu.java.zrtp.ZrtpCodes.InfoCodes;
import gnu.java.zrtp.ZrtpCodes.MessageSeverity;
import gnu.java.zrtp.ZrtpCodes.SevereCodes;
import gnu.java.zrtp.ZrtpCodes.WarningCodes;
import gnu.java.zrtp.ZrtpCodes.ZrtpErrorCodes;
import gnu.java.zrtp.ZrtpUserCallback;
import java.util.EnumSet;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.event.SrtpListener;
import org.jitsi.service.resources.ResourceManagementService;
import org.jitsi.util.Logger;

public class SecurityEventManager extends ZrtpUserCallback {
    public static final String WARNING_NO_EXPECTED_RS_MATCH = getI18NString("impl.media.security.WARNING_NO_EXPECTED_RS_MATCH", null);
    public static final String WARNING_NO_RS_MATCH = getI18NString("impl.media.security.WARNING_NO_RS_MATCH", null);
    private static final Logger logger = Logger.getLogger(SecurityEventManager.class);
    private String cipher;
    private boolean isDHSession = false;
    private boolean isSasVerified;
    private String sas;
    private SrtpListener securityListener;
    private MediaType sessionType;
    private final ZrtpControlImpl zrtpControl;

    public SecurityEventManager(ZrtpControlImpl zrtpControl) {
        this.zrtpControl = zrtpControl;
        this.securityListener = zrtpControl.getSrtpListener();
    }

    public void setSessionType(MediaType sessionType) {
        this.sessionType = sessionType;
    }

    public void setDHSession(boolean isDHSession) {
        this.isDHSession = isDHSession;
    }

    public void secureOn(String cipher) {
        if (logger.isDebugEnabled()) {
            logger.debug(sessionTypeToString(this.sessionType) + ": cipher enabled: " + cipher);
        }
        this.cipher = cipher;
    }

    public void showSAS(String sas, boolean isVerified) {
        if (logger.isDebugEnabled()) {
            logger.debug(sessionTypeToString(this.sessionType) + ": SAS is: " + sas);
        }
        this.sas = sas;
        this.isSasVerified = isVerified;
    }

    public void setSASVerified(boolean isVerified) {
        boolean z = this.sas != null && isVerified;
        this.isSasVerified = z;
    }

    public void showMessage(MessageSeverity sev, EnumSet<?> subCode) {
        InfoCodes msgCode = subCode.iterator().next();
        String message = null;
        String i18nMessage = null;
        int severity = 0;
        boolean sendEvent = true;
        if (msgCode instanceof InfoCodes) {
            sendEvent = false;
            if (msgCode == InfoCodes.InfoSecureStateOn) {
                if (this.isDHSession) {
                    this.securityListener.securityTurnedOn(this.sessionType, this.cipher, this.zrtpControl);
                } else {
                    this.securityListener.securityTurnedOn(this.sessionType, this.cipher, this.zrtpControl);
                }
            }
        } else if (msgCode instanceof WarningCodes) {
            WarningCodes warn = (WarningCodes) msgCode;
            severity = 1;
            if (warn == WarningCodes.WarningNoRSMatch) {
                message = "No retained shared secret available.";
                i18nMessage = WARNING_NO_RS_MATCH;
            } else if (warn == WarningCodes.WarningNoExpectedRSMatch) {
                message = "An expected retained shared secret is missing.";
                i18nMessage = WARNING_NO_EXPECTED_RS_MATCH;
            } else if (warn == WarningCodes.WarningCRCmismatch) {
                message = "Internal ZRTP packet checksum mismatch.";
                i18nMessage = getI18NString("impl.media.security.CHECKSUM_MISMATCH", null);
            } else {
                sendEvent = false;
            }
        } else if (msgCode instanceof SevereCodes) {
            SevereCodes severe = (SevereCodes) msgCode;
            severity = 2;
            if (severe == SevereCodes.SevereCannotSend) {
                message = "Failed to send data.Internet data connection or peer is down.";
                i18nMessage = getI18NString("impl.media.security.DATA_SEND_FAILED", msgCode.toString());
            } else if (severe == SevereCodes.SevereTooMuchRetries) {
                message = "Too much retries during ZRTP negotiation.";
                i18nMessage = getI18NString("impl.media.security.RETRY_RATE_EXCEEDED", msgCode.toString());
            } else if (severe == SevereCodes.SevereProtocolError) {
                message = "Internal protocol error occured.";
                i18nMessage = getI18NString("impl.media.security.INTERNAL_PROTOCOL_ERROR", msgCode.toString());
            } else {
                message = "General error has occurred.";
                i18nMessage = getI18NString("impl.media.security.ZRTP_GENERIC_MSG", msgCode.toString());
            }
        } else if (msgCode instanceof ZrtpErrorCodes) {
            severity = 3;
            message = "Indicates compatibility problems like for example:unsupported protocol version, unsupported hash type,cypher type, SAS scheme, etc.";
            i18nMessage = getI18NString("impl.media.security.ZRTP_GENERIC_MSG", msgCode.toString());
        } else {
            sendEvent = false;
        }
        if (sendEvent) {
            this.securityListener.securityMessageReceived(message, i18nMessage, severity);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(sessionTypeToString(this.sessionType) + ": ZRTP message: severity: " + sev + ", sub code: " + msgCode + ", DH session: " + this.isDHSession + ", multi: " + 0);
        }
    }

    public void zrtpNegotiationFailed(MessageSeverity severity, EnumSet<?> subCode) {
        Object msgCode = subCode.iterator().next();
        if (logger.isDebugEnabled()) {
            logger.debug(sessionTypeToString(this.sessionType) + ": ZRTP key negotiation failed, sub code: " + msgCode);
        }
    }

    public void secureOff() {
        if (logger.isDebugEnabled()) {
            logger.debug(sessionTypeToString(this.sessionType) + ": Security off");
        }
        this.securityListener.securityTurnedOff(this.sessionType);
    }

    public void zrtpNotSuppOther() {
        if (logger.isDebugEnabled()) {
            logger.debug(sessionTypeToString(this.sessionType) + ": Other party does not support ZRTP key negotiation" + " protocol, no secure calls possible.");
        }
        this.securityListener.securityTimeout(this.sessionType);
    }

    public void confirmGoClear() {
        if (logger.isDebugEnabled()) {
            logger.debug(sessionTypeToString(this.sessionType) + ": GoClear confirmation requested.");
        }
    }

    private String sessionTypeToString(MediaType sessionType) {
        switch (sessionType) {
            case AUDIO:
                return "AUDIO_SESSION";
            case VIDEO:
                return "VIDEO_SESSION";
            default:
                throw new IllegalArgumentException("sessionType");
        }
    }

    private static String getI18NString(String key, String param) {
        String[] params = null;
        ResourceManagementService resources = LibJitsi.getResourceManagementService();
        if (resources == null) {
            return null;
        }
        if (param != null) {
            params = new String[]{param};
        }
        return resources.getI18NString(key, params);
    }

    public void setSrtpListener(SrtpListener securityListener) {
        this.securityListener = securityListener;
    }

    public String getSecurityString() {
        return this.sas;
    }

    public String getCipherString() {
        return this.cipher;
    }

    public boolean isSecurityVerified() {
        return this.isSasVerified;
    }

    public void securityNegotiationStarted() {
        try {
            this.securityListener.securityNegotiationStarted(this.sessionType, this.zrtpControl);
        } catch (Throwable th) {
            logger.error("Error processing security started.");
        }
    }
}
