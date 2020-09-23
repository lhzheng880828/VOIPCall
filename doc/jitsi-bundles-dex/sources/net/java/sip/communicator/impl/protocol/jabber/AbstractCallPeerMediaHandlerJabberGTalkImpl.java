package net.java.sip.communicator.impl.protocol.jabber;

import ch.imvs.sdes4j.srtp.SrtpCryptoAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.java.sip.communicator.impl.protocol.jabber.AbstractCallPeerJabberGTalkImpl;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CryptoPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.EncryptionPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RtpDescriptionPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ZrtpHashPacketExtension;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.CallPeer;
import net.java.sip.communicator.service.protocol.media.CallPeerMediaHandler;
import net.java.sip.communicator.service.protocol.media.SrtpControls;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.SDesControl;
import org.jitsi.service.neomedia.SrtpControl;
import org.jitsi.service.neomedia.SrtpControlType;
import org.jitsi.service.neomedia.ZrtpControl;

public abstract class AbstractCallPeerMediaHandlerJabberGTalkImpl<T extends AbstractCallPeerJabberGTalkImpl<?, ?, ?>> extends CallPeerMediaHandler<T> {
    private static final Logger logger = Logger.getLogger(AbstractCallPeerMediaHandlerJabberGTalkImpl.class);
    private boolean localInputEvtAware = false;

    public AbstractCallPeerMediaHandlerJabberGTalkImpl(T peer) {
        super(peer, peer);
    }

    public boolean getLocalInputEvtAware() {
        return this.localInputEvtAware;
    }

    public void setLocalInputEvtAware(boolean enable) {
        this.localInputEvtAware = enable;
    }

    /* access modifiers changed from: protected */
    public void addZrtpAdvertisedEncryptions(boolean isInitiator, RtpDescriptionPacketExtension description, MediaType mediaType) {
        CallPeer peer = getPeer();
        Call call = peer.getCall();
        if (!call.getConference().isJitsiVideobridge()) {
            EncryptionPacketExtension encryptionPacketExtension = (EncryptionPacketExtension) description.getFirstChildOfType(EncryptionPacketExtension.class);
            if (encryptionPacketExtension != null) {
                AccountID accountID = peer.getProtocolProvider().getAccountID();
                if (accountID.getAccountPropertyBoolean("DEFAULT_ENCRYPTION", true) && accountID.isEncryptionProtocolEnabled(ZrtpControl.PROTO_NAME) && call.isSipZrtpAttribute()) {
                    ZrtpHashPacketExtension zrtpHashPacketExtension = (ZrtpHashPacketExtension) encryptionPacketExtension.getFirstChildOfType(ZrtpHashPacketExtension.class);
                    if (zrtpHashPacketExtension != null && zrtpHashPacketExtension.getValue() != null) {
                        addAdvertisedEncryptionMethod(SrtpControlType.ZRTP);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void addSDesAdvertisedEncryptions(boolean isInitiator, RtpDescriptionPacketExtension description, MediaType mediaType) {
        CallPeer peer = getPeer();
        if (!peer.getCall().getConference().isJitsiVideobridge()) {
            EncryptionPacketExtension encryptionPacketExtension = (EncryptionPacketExtension) description.getFirstChildOfType(EncryptionPacketExtension.class);
            if (encryptionPacketExtension != null) {
                AccountID accountID = peer.getProtocolProvider().getAccountID();
                if (accountID.getAccountPropertyBoolean("DEFAULT_ENCRYPTION", true) && accountID.isEncryptionProtocolEnabled(SDesControl.PROTO_NAME)) {
                    SrtpControls srtpControls = getSrtpControls();
                    SDesControl sdesControl = (SDesControl) srtpControls.getOrCreate(mediaType, SrtpControlType.SDES);
                    if (selectSdesCryptoSuite(isInitiator, sdesControl, encryptionPacketExtension) != null) {
                        removeAndCleanupOtherSrtpControls(mediaType, SrtpControlType.SDES);
                        addAdvertisedEncryptionMethod(SrtpControlType.SDES);
                        return;
                    }
                    sdesControl.cleanup();
                    srtpControls.remove(mediaType, SrtpControlType.SDES);
                }
            } else if (isInitiator) {
                SrtpControl sdesControl2 = getSrtpControls().remove(mediaType, SrtpControlType.SDES);
                if (sdesControl2 != null) {
                    sdesControl2.cleanup();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public SrtpCryptoAttribute selectSdesCryptoSuite(boolean isInitiator, SDesControl sDesControl, EncryptionPacketExtension encryptionPacketExtension) {
        List<CryptoPacketExtension> cryptoPacketExtensions = encryptionPacketExtension.getCryptoList();
        List<SrtpCryptoAttribute> peerAttributes = new ArrayList(cryptoPacketExtensions.size());
        for (CryptoPacketExtension cpe : cryptoPacketExtensions) {
            peerAttributes.add(cpe.toSrtpCryptoAttribute());
        }
        return isInitiator ? sDesControl.initiatorSelectAttribute(peerAttributes) : sDesControl.responderSelectAttribute(peerAttributes);
    }

    /* access modifiers changed from: protected */
    public boolean isRemoteZrtpCapable(EncryptionPacketExtension encryptionPacketExtension) {
        return encryptionPacketExtension.getFirstChildOfType(ZrtpHashPacketExtension.class) != null;
    }

    /* access modifiers changed from: protected */
    public boolean setZrtpEncryptionOnDescription(MediaType mediaType, RtpDescriptionPacketExtension description, RtpDescriptionPacketExtension remoteDescription) {
        CallPeer peer = getPeer();
        Call call = peer.getCall();
        if (call.getConference().isJitsiVideobridge()) {
            return false;
        }
        boolean isRemoteZrtpCapable;
        if (remoteDescription == null) {
            isRemoteZrtpCapable = true;
        } else {
            EncryptionPacketExtension remoteEncryption = (EncryptionPacketExtension) remoteDescription.getFirstChildOfType(EncryptionPacketExtension.class);
            isRemoteZrtpCapable = remoteEncryption != null && isRemoteZrtpCapable(remoteEncryption);
        }
        boolean zrtpHashSet = false;
        if (!isRemoteZrtpCapable) {
            return false;
        }
        AccountID accountID = peer.getProtocolProvider().getAccountID();
        if (!accountID.getAccountPropertyBoolean("DEFAULT_ENCRYPTION", true) || !accountID.isEncryptionProtocolEnabled(ZrtpControl.PROTO_NAME) || !call.isSipZrtpAttribute()) {
            return false;
        }
        ZrtpControl zrtpControl = (ZrtpControl) getSrtpControls().getOrCreate(mediaType, SrtpControlType.ZRTP);
        int numberSupportedVersions = zrtpControl.getNumberSupportedVersions();
        for (int i = 0; i < numberSupportedVersions; i++) {
            String[] helloHash = zrtpControl.getHelloHashSep(i);
            if (helloHash != null && helloHash[1].length() > 0) {
                ZrtpHashPacketExtension hash = new ZrtpHashPacketExtension();
                hash.setVersion(helloHash[0]);
                hash.setValue(helloHash[1]);
                EncryptionPacketExtension encryption = (EncryptionPacketExtension) description.getFirstChildOfType(EncryptionPacketExtension.class);
                if (encryption == null) {
                    encryption = new EncryptionPacketExtension();
                    description.addChildExtension(encryption);
                }
                encryption.addChildExtension(hash);
                zrtpHashSet = true;
            }
        }
        return zrtpHashSet;
    }

    /* access modifiers changed from: protected */
    public boolean setSDesEncryptionOnDescription(MediaType mediaType, RtpDescriptionPacketExtension localDescription, RtpDescriptionPacketExtension remoteDescription) {
        CallPeer peer = getPeer();
        if (peer.getCall().getConference().isJitsiVideobridge()) {
            return false;
        }
        AccountID accountID = peer.getProtocolProvider().getAccountID();
        if (accountID.getAccountPropertyBoolean("DEFAULT_ENCRYPTION", true) && accountID.isEncryptionProtocolEnabled(SDesControl.PROTO_NAME)) {
            SrtpControls srtpControls = getSrtpControls();
            SDesControl sdesControl = (SDesControl) srtpControls.getOrCreate(mediaType, SrtpControlType.SDES);
            String ciphers = accountID.getAccountPropertyString("SDES_CIPHER_SUITES");
            if (ciphers == null) {
                ciphers = JabberActivator.getResources().getSettingsString("net.java.sip.communicator.service.neomedia.SDES_CIPHER_SUITES");
            }
            sdesControl.setEnabledCiphers(Arrays.asList(ciphers.split(Separators.COMMA)));
            EncryptionPacketExtension localEncryption;
            if (remoteDescription == null) {
                localEncryption = (EncryptionPacketExtension) localDescription.getFirstChildOfType(EncryptionPacketExtension.class);
                if (localEncryption == null) {
                    localEncryption = new EncryptionPacketExtension();
                    localDescription.addChildExtension(localEncryption);
                }
                for (SrtpCryptoAttribute ca : sdesControl.getInitiatorCryptoAttributes()) {
                    localEncryption.addChildExtension(new CryptoPacketExtension(ca));
                }
                return true;
            }
            EncryptionPacketExtension remoteEncryption = (EncryptionPacketExtension) remoteDescription.getFirstChildOfType(EncryptionPacketExtension.class);
            if (remoteEncryption != null) {
                SrtpCryptoAttribute selectedSdes = selectSdesCryptoSuite(false, sdesControl, remoteEncryption);
                if (selectedSdes != null) {
                    localEncryption = (EncryptionPacketExtension) localDescription.getFirstChildOfType(EncryptionPacketExtension.class);
                    if (localEncryption == null) {
                        localEncryption = new EncryptionPacketExtension();
                        localDescription.addChildExtension(localEncryption);
                    }
                    localEncryption.addChildExtension(new CryptoPacketExtension(selectedSdes));
                    return true;
                }
                sdesControl.cleanup();
                srtpControls.remove(mediaType, SrtpControlType.SDES);
                logger.warn("Received unsupported sdes crypto attribute");
            } else {
                sdesControl.cleanup();
                srtpControls.remove(mediaType, SrtpControlType.SDES);
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void setAndAddPreferredEncryptionProtocol(MediaType mediaType, RtpDescriptionPacketExtension localDescription, RtpDescriptionPacketExtension remoteDescription) {
        for (String preferredEncryptionProtocol : ((ProtocolProviderServiceJabberImpl) ((AbstractCallPeerJabberGTalkImpl) getPeer()).getProtocolProvider()).getAccountID().getSortedEnabledEncryptionProtocolList()) {
            if (setAndAddPreferredEncryptionProtocol(preferredEncryptionProtocol.substring("ENCRYPTION_PROTOCOL".length() + 1), mediaType, localDescription, remoteDescription)) {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean setAndAddPreferredEncryptionProtocol(String protoName, MediaType mediaType, RtpDescriptionPacketExtension localDescription, RtpDescriptionPacketExtension remoteDescription) {
        if (((AbstractCallPeerJabberGTalkImpl) getPeer()).isJitsiVideobridge()) {
            return false;
        }
        if (SDesControl.PROTO_NAME.equals(protoName)) {
            addSDesAdvertisedEncryptions(false, remoteDescription, mediaType);
            if (setSDesEncryptionOnDescription(mediaType, localDescription, remoteDescription)) {
                return true;
            }
        } else if (ZrtpControl.PROTO_NAME.equals(protoName) && setZrtpEncryptionOnDescription(mediaType, localDescription, remoteDescription)) {
            addZrtpAdvertisedEncryptions(false, remoteDescription, mediaType);
            return true;
        }
        return false;
    }
}
