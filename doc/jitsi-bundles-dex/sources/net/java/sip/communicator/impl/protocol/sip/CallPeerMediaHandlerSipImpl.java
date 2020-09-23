package net.java.sip.communicator.impl.protocol.sip;

import ch.imvs.sdes4j.srtp.SrtpCryptoAttribute;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.sdp.Attribute;
import javax.sdp.Media;
import javax.sdp.MediaDescription;
import javax.sdp.SdpConstants;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CryptoPacketExtension;
import net.java.sip.communicator.impl.protocol.sip.sdp.SdpUtils;
import net.java.sip.communicator.service.netaddr.NetworkAddressManagerService;
import net.java.sip.communicator.service.protocol.AccountID;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.media.CallPeerMediaHandler;
import net.java.sip.communicator.service.protocol.media.MediaAwareCallPeer;
import net.java.sip.communicator.service.protocol.media.SrtpControls;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.neomedia.DtlsControl;
import org.jitsi.service.neomedia.DtlsControl.Setup;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaService;
import org.jitsi.service.neomedia.MediaStream;
import org.jitsi.service.neomedia.MediaStreamTarget;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.QualityControl;
import org.jitsi.service.neomedia.QualityPreset;
import org.jitsi.service.neomedia.RTPExtension;
import org.jitsi.service.neomedia.SDesControl;
import org.jitsi.service.neomedia.SrtpControl;
import org.jitsi.service.neomedia.SrtpControlType;
import org.jitsi.service.neomedia.StreamConnector;
import org.jitsi.service.neomedia.VideoMediaStream;
import org.jitsi.service.neomedia.ZrtpControl;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.format.MediaFormat;

public class CallPeerMediaHandlerSipImpl extends CallPeerMediaHandler<CallPeerSipImpl> {
    private static final String DTLS_SRTP_FINGERPRINT_ATTR = "fingerprint";
    private static final String DTLS_SRTP_SETUP_ATTR = "setup";
    private static final Logger logger = Logger.getLogger(CallPeerMediaHandlerSipImpl.class);
    private URL callInfoURL = null;
    private SessionDescription localSess = null;
    private Object offerAnswerLock = new Object();
    private QualityControlWrapper qualityControls;
    boolean supportQualityControls;
    private final TransportManagerSipImpl transportManager;

    public CallPeerMediaHandlerSipImpl(CallPeerSipImpl peer) {
        super(peer, peer);
        this.transportManager = new TransportManagerSipImpl(peer);
        this.qualityControls = new QualityControlWrapper(peer);
    }

    public String createOffer() throws OperationFailedException {
        return (this.localSess == null ? createFirstOffer() : createUpdateOffer(this.localSess)).toString();
    }

    private SessionDescription createFirstOffer() throws OperationFailedException {
        Vector<MediaDescription> mediaDescs = createMediaDescriptions();
        this.localSess = SdpUtils.createSessionDescription(getTransportManager().getLastUsedLocalHost(), ((ProtocolProviderServiceSipImpl) ((CallPeerSipImpl) getPeer()).getProtocolProvider()).getAccountID().getUserID(), mediaDescs);
        return this.localSess;
    }

    private Vector<MediaDescription> createMediaDescriptions() throws OperationFailedException {
        Vector<MediaDescription> mediaDescs = new Vector();
        QualityPreset sendQualityPreset = null;
        QualityPreset receiveQualityPreset = null;
        if (this.qualityControls != null) {
            sendQualityPreset = this.qualityControls.getRemoteReceivePreset();
            receiveQualityPreset = this.qualityControls.getRemoteSendMaxPreset();
        }
        for (MediaType mediaType : MediaType.values()) {
            MediaDevice dev = getDefaultDevice(mediaType);
            if (isDeviceActive(dev, sendQualityPreset, receiveQualityPreset)) {
                MediaDirection direction = dev.getDirection().and(getDirectionUserPreference(mediaType));
                if (isLocallyOnHold()) {
                    direction = direction.and(MediaDirection.SENDONLY);
                }
                if (direction != MediaDirection.INACTIVE) {
                    for (String proto : getRtpTransports()) {
                        MediaDescription md = createMediaDescription(proto, getLocallySupportedFormats(dev, direction.allowsSending() ? sendQualityPreset : null, receiveQualityPreset), getTransportManager().getStreamConnector(mediaType), direction, dev.getSupportedExtensions());
                        try {
                            if (mediaType.equals(MediaType.VIDEO) && receiveQualityPreset != null) {
                                int frameRate = (int) receiveQualityPreset.getFameRate();
                                if (frameRate > 0) {
                                    md.setAttribute("framerate", String.valueOf(frameRate));
                                }
                            }
                        } catch (SdpException e) {
                        }
                        if ("UDP/TLS/RTP/SAVP".equals(proto) || "UDP/TLS/RTP/SAVPF".equals(proto)) {
                            updateMediaDescriptionForDtls(mediaType, md, null);
                        } else {
                            updateMediaDescriptionForZrtp(mediaType, md, null);
                            if ("RTP/SAVP".equals(proto) || "RTP/SAVPF".equals(proto)) {
                                updateMediaDescriptionForSDes(mediaType, md, null);
                            }
                            if ("RTP/SAVPF".equals(proto)) {
                                updateMediaDescriptionForDtls(mediaType, md, null);
                            }
                        }
                        mediaDescs.add(md);
                    }
                }
            }
        }
        if (mediaDescs.isEmpty()) {
            ProtocolProviderServiceSipImpl.throwOperationFailedException("We couldn't find any active Audio/Video devices and couldn't create a call", 1, null, logger);
        }
        return mediaDescs;
    }

    private SessionDescription createUpdateOffer(SessionDescription sdescToUpdate) throws OperationFailedException {
        SessionDescription newOffer = SdpUtils.createSessionUpdateDescription(sdescToUpdate, getTransportManager().getLastUsedLocalHost(), createMediaDescriptions());
        this.localSess = newOffer;
        return newOffer;
    }

    public String processOffer(String offerString) throws OperationFailedException, IllegalArgumentException {
        String obj;
        SessionDescription offer = SdpUtils.parseSdpString(offerString);
        synchronized (this.offerAnswerLock) {
            if (this.localSess == null) {
                obj = processFirstOffer(offer).toString();
            } else {
                obj = processUpdateOffer(offer, this.localSess).toString();
            }
        }
        return obj;
    }

    private SessionDescription processFirstOffer(SessionDescription offer) throws OperationFailedException, IllegalArgumentException {
        this.localSess = SdpUtils.createSessionDescription(getTransportManager().getLastUsedLocalHost(), getUserName(), createMediaDescriptionsForAnswer(offer));
        return this.localSess;
    }

    private SessionDescription processUpdateOffer(SessionDescription newOffer, SessionDescription previousAnswer) throws OperationFailedException, IllegalArgumentException {
        this.localSess = SdpUtils.createSessionUpdateDescription(previousAnswer, getTransportManager().getLastUsedLocalHost(), createMediaDescriptionsForAnswer(newOffer));
        return this.localSess;
    }

    private Vector<MediaDescription> createMediaDescriptionsForAnswer(SessionDescription offer) throws OperationFailedException, IllegalArgumentException {
        List<MediaDescription> remoteDescriptions = SdpUtils.extractMediaDescriptions(offer);
        Vector<MediaDescription> vector = new Vector(remoteDescriptions.size());
        setCallInfoURL(SdpUtils.getCallInfoURL(offer));
        boolean atLeastOneValidDescription = false;
        boolean rejectedAvpOfferDueToSavpMandatory = false;
        AccountID accountID = ((ProtocolProviderServiceSipImpl) ((CallPeerSipImpl) getPeer()).getProtocolProvider()).getAccountID();
        int savpOption = accountID.getAccountPropertyBoolean("DEFAULT_ENCRYPTION", true) ? accountID.getAccountPropertyInt("SAVP_OPTION", 0) : 0;
        boolean masterStreamSet = false;
        List<MediaType> seenMediaTypes = new ArrayList();
        for (MediaDescription mediaDescription : remoteDescriptions) {
            try {
                String proto = mediaDescription.getMedia().getProtocol();
                if (savpOption != 1 || proto.endsWith("RTP/SAVP") || proto.endsWith("RTP/SAVPF")) {
                    try {
                        MediaType mediaType = SdpUtils.getMediaType(mediaDescription);
                        if (!seenMediaTypes.contains(mediaType)) {
                            List<MediaFormat> mutuallySupportedFormats;
                            seenMediaTypes.add(mediaType);
                            List<MediaFormat> remoteFormats = SdpUtils.extractFormats(mediaDescription, getDynamicPayloadTypes());
                            MediaDevice dev = getDefaultDevice(mediaType);
                            MediaDirection devDirection = (dev == null ? MediaDirection.INACTIVE : dev.getDirection()).and(getDirectionUserPreference(mediaType));
                            MediaDirection direction = devDirection.getDirectionForAnswer(SdpUtils.getDirection(mediaDescription));
                            if (dev == null) {
                                mutuallySupportedFormats = null;
                            } else {
                                if (!mediaType.equals(MediaType.VIDEO) || this.qualityControls == null) {
                                    mutuallySupportedFormats = intersectFormats(remoteFormats, getLocallySupportedFormats(dev));
                                } else {
                                    mutuallySupportedFormats = intersectFormats(remoteFormats, getLocallySupportedFormats(dev, direction.allowsSending() ? this.qualityControls.getRemoteReceivePreset() : null, this.qualityControls.getRemoteSendMaxPreset()));
                                }
                            }
                            MediaStreamTarget target = SdpUtils.extractDefaultTarget(mediaDescription, offer);
                            int targetDataPort = target.getDataAddress().getPort();
                            if (devDirection == MediaDirection.INACTIVE || mutuallySupportedFormats == null || mutuallySupportedFormats.isEmpty() || targetDataPort == 0) {
                                vector.add(SdpUtils.createDisablingAnswer(mediaDescription));
                                closeStream(mediaType);
                            } else {
                                List<RTPExtension> rtpExtensions = intersectRTPExtensions(SdpUtils.extractRTPExtensions(mediaDescription, getRtpExtensionsRegistry()), getExtensionsForType(mediaType));
                                StreamConnector connector = getTransportManager().getStreamConnector(mediaType);
                                if (mediaType.equals(MediaType.VIDEO)) {
                                    MediaStream stream = getStream(MediaType.VIDEO);
                                    if (!(stream == null || dev == null)) {
                                        List<MediaFormat> fmts = intersectFormats(getLocallySupportedFormats(dev), remoteFormats);
                                        if (fmts.size() > 0) {
                                            ((VideoMediaStream) stream).updateQualityControl(((MediaFormat) fmts.get(0)).getAdvancedAttributes());
                                        }
                                    }
                                    this.supportQualityControls = SdpUtils.containsAttribute(mediaDescription, "imageattr");
                                    float frameRate = -1.0f;
                                    try {
                                        String frStr = mediaDescription.getAttribute("framerate");
                                        if (frStr != null) {
                                            frameRate = (float) Integer.parseInt(frStr);
                                        }
                                    } catch (SdpParseException e) {
                                    }
                                    if (frameRate > 0.0f) {
                                        this.qualityControls.setMaxFrameRate(frameRate);
                                    }
                                }
                                MediaDescription md = createMediaDescription(proto, mutuallySupportedFormats, connector, direction, rtpExtensions);
                                setAndAddPreferredEncryptionProtocol(mediaType, md, mediaDescription);
                                MediaFormat fmt = findMediaFormat(remoteFormats, (MediaFormat) mutuallySupportedFormats.get(0));
                                boolean masterStream = false;
                                if (!masterStreamSet) {
                                    if (remoteDescriptions.size() > 1) {
                                        if (mediaType.equals(MediaType.AUDIO)) {
                                            masterStream = true;
                                            masterStreamSet = true;
                                        }
                                    } else {
                                        masterStream = true;
                                        masterStreamSet = true;
                                    }
                                }
                                initStream(connector, dev, fmt, target, direction, rtpExtensions, masterStream);
                                vector.add(md);
                                atLeastOneValidDescription = true;
                            }
                        }
                    } catch (IllegalArgumentException e2) {
                        vector.add(SdpUtils.createDisablingAnswer(mediaDescription));
                    }
                } else {
                    rejectedAvpOfferDueToSavpMandatory = true;
                }
            } catch (SdpParseException e3) {
                throw new OperationFailedException("Unable to create the media description", 11, e3);
            }
        }
        if (atLeastOneValidDescription) {
            return vector;
        }
        if (rejectedAvpOfferDueToSavpMandatory) {
            throw new OperationFailedException("Offer contained no valid media descriptions. Insecure media was rejected (only RTP/AVP instead of RTP/SAVP).", 11);
        }
        throw new OperationFailedException("Offer contained no valid media descriptions.", 11);
    }

    private boolean updateMediaDescriptionForDtls(MediaType mediaType, MediaDescription localMd, MediaDescription remoteMd) {
        AccountID accountID = ((ProtocolProviderServiceSipImpl) ((CallPeerSipImpl) getPeer()).getProtocolProvider()).getAccountID();
        if (!accountID.getAccountPropertyBoolean("DEFAULT_ENCRYPTION", true) || !accountID.isEncryptionProtocolEnabled(DtlsControl.PROTO_NAME)) {
            return false;
        }
        Media localMedia = localMd.getMedia();
        if (localMedia == null) {
            return false;
        }
        String proto;
        try {
            proto = localMedia.getProtocol();
        } catch (SdpParseException e) {
            proto = null;
        }
        boolean dtls = "UDP/TLS/RTP/SAVP".equals(proto) || "UDP/TLS/RTP/SAVPF".equals(proto) || "RTP/SAVPF".equals(proto);
        if (dtls && remoteMd != null) {
            dtls = isDtlsMediaDescription(remoteMd);
        }
        SrtpControls srtpControls = getSrtpControls();
        if (dtls) {
            DtlsControl dtlsControl = (DtlsControl) srtpControls.getOrCreate(mediaType, SrtpControlType.DTLS_SRTP);
            Vector<Attribute> attrs = localMd.getAttributes(true);
            Setup setup = remoteMd == null ? Setup.ACTPASS : Setup.ACTIVE;
            attrs.add(SdpUtils.createAttribute(DTLS_SRTP_SETUP_ATTR, setup.toString()));
            attrs.add(SdpUtils.createAttribute("fingerprint", dtlsControl.getLocalFingerprintHashFunction() + Separators.SP + dtlsControl.getLocalFingerprint()));
            dtlsControl.setSetup(setup);
            if (remoteMd != null) {
                updateSrtpControlsForDtls(mediaType, localMd, remoteMd);
            }
            return true;
        } else if (remoteMd == null) {
            return false;
        } else {
            SrtpControl dtlsControl2 = srtpControls.remove(mediaType, SrtpControlType.DTLS_SRTP);
            if (dtlsControl2 == null) {
                return false;
            }
            dtlsControl2.cleanup();
            return false;
        }
    }

    private void updateSrtpControlsForDtls(MediaType mediaType, MediaDescription localMd, MediaDescription remoteMd) {
        SrtpControls srtpControls = getSrtpControls();
        DtlsControl dtlsControl = (DtlsControl) srtpControls.get(mediaType, SrtpControlType.DTLS_SRTP);
        if (dtlsControl != null) {
            if (isDtlsMediaDescription(remoteMd)) {
                if (localMd == null) {
                    String setup;
                    try {
                        setup = remoteMd.getAttribute(DTLS_SRTP_SETUP_ATTR);
                    } catch (SdpParseException e) {
                        setup = null;
                    }
                    if (Setup.PASSIVE.toString().equals(setup)) {
                        dtlsControl.setSetup(Setup.ACTIVE);
                    }
                }
                Vector<Attribute> attrs = remoteMd.getAttributes(false);
                Map<String, String> remoteFingerprints = new LinkedHashMap();
                if (attrs != null) {
                    Iterator i$ = attrs.iterator();
                    while (i$.hasNext()) {
                        Attribute attr = (Attribute) i$.next();
                        try {
                            if ("fingerprint".equals(attr.getName())) {
                                String fingerprint = attr.getValue();
                                if (fingerprint != null) {
                                    fingerprint = fingerprint.trim();
                                    int spIndex = fingerprint.indexOf(32);
                                    if (spIndex > 0 && spIndex < fingerprint.length() - 1) {
                                        remoteFingerprints.put(fingerprint.substring(0, spIndex), fingerprint.substring(spIndex + 1));
                                    }
                                }
                            }
                        } catch (SdpParseException e2) {
                        }
                    }
                }
                dtlsControl.setRemoteFingerprints(remoteFingerprints);
                removeAndCleanupOtherSrtpControls(mediaType, SrtpControlType.DTLS_SRTP);
                return;
            }
            srtpControls.remove(mediaType, SrtpControlType.DTLS_SRTP);
            dtlsControl.cleanup();
        }
    }

    private boolean updateMediaDescriptionForSDes(MediaType mediaType, MediaDescription localMd, MediaDescription remoteMd) {
        AccountID accountID = ((ProtocolProviderServiceSipImpl) ((CallPeerSipImpl) getPeer()).getProtocolProvider()).getAccountID();
        if (!accountID.getAccountPropertyBoolean("DEFAULT_ENCRYPTION", true) || !accountID.isEncryptionProtocolEnabled(SDesControl.PROTO_NAME)) {
            return false;
        }
        SrtpControls srtpControls = getSrtpControls();
        SDesControl sdesControl = (SDesControl) srtpControls.getOrCreate(mediaType, SrtpControlType.SDES);
        String ciphers = accountID.getAccountPropertyString("SDES_CIPHER_SUITES");
        if (ciphers == null) {
            ciphers = SipActivator.getResources().getSettingsString("net.java.sip.communicator.service.neomedia.SDES_CIPHER_SUITES");
        }
        sdesControl.setEnabledCiphers(Arrays.asList(ciphers.split(Separators.COMMA)));
        if (remoteMd == null) {
            Vector<Attribute> atts = localMd.getAttributes(true);
            for (SrtpCryptoAttribute ca : sdesControl.getInitiatorCryptoAttributes()) {
                atts.add(SdpUtils.createAttribute(CryptoPacketExtension.ELEMENT_NAME, ca.encode()));
            }
            return true;
        }
        SrtpCryptoAttribute localAttr = selectSdesCryptoSuite(false, sdesControl, remoteMd);
        if (localAttr != null) {
            try {
                localMd.setAttribute(CryptoPacketExtension.ELEMENT_NAME, localAttr.encode());
                return true;
            } catch (SdpException e) {
                logger.error("unable to add crypto to answer", e);
            }
        } else {
            sdesControl.cleanup();
            srtpControls.remove(mediaType, SrtpControlType.SDES);
            logger.warn("Received unsupported sdes crypto attribute.");
            return false;
        }
    }

    private boolean updateMediaDescriptionForZrtp(MediaType mediaType, MediaDescription localMd, MediaDescription remoteMd) {
        MediaAwareCallPeer<?, ?, ?> peer = getPeer();
        AccountID accountID = peer.getProtocolProvider().getAccountID();
        boolean b = false;
        if (accountID.getAccountPropertyBoolean("DEFAULT_ENCRYPTION", true) && accountID.isEncryptionProtocolEnabled(ZrtpControl.PROTO_NAME) && peer.getCall().isSipZrtpAttribute()) {
            ZrtpControl zrtpControl = (ZrtpControl) getSrtpControls().getOrCreate(mediaType, SrtpControlType.ZRTP);
            int numberSupportedVersions = zrtpControl.getNumberSupportedVersions();
            int i = 0;
            while (i < numberSupportedVersions) {
                try {
                    String helloHash = zrtpControl.getHelloHash(i);
                    if (helloHash != null && helloHash.length() > 0) {
                        localMd.setAttribute("zrtp-hash", helloHash);
                        b = true;
                    }
                    i++;
                } catch (SdpException ex) {
                    logger.error("Cannot add zrtp-hash to sdp", ex);
                }
            }
        }
        return b;
    }

    private List<String> getRtpTransports() throws OperationFailedException {
        int savpOption;
        AccountID accountID = ((ProtocolProviderServiceSipImpl) ((CallPeerSipImpl) getPeer()).getProtocolProvider()).getAccountID();
        if (accountID.getAccountPropertyBoolean("DEFAULT_ENCRYPTION", true)) {
            savpOption = accountID.getAccountPropertyInt("SAVP_OPTION", 0);
        } else {
            savpOption = 0;
        }
        List<String> result = new ArrayList(3);
        if (savpOption == 0) {
            result.add(SdpConstants.RTP_AVP);
        } else {
            List<String> encryptionProtocols = accountID.getSortedEnabledEncryptionProtocolList();
            for (int epi = encryptionProtocols.size() - 1; epi >= 0; epi--) {
                String[] protos = DtlsControl.PROTO_NAME.equals(((String) encryptionProtocols.get(epi)).substring("ENCRYPTION_PROTOCOL".length() + 1)) ? new String[]{"UDP/TLS/RTP/SAVP", "RTP/SAVPF"} : new String[]{"RTP/SAVP"};
                for (int pi = protos.length - 1; pi >= 0; pi--) {
                    String proto = protos[pi];
                    int ri = result.indexOf(proto);
                    if (ri > 0) {
                        result.remove(ri);
                    }
                    result.add(0, proto);
                }
            }
            if (savpOption == 2) {
                result.add(SdpConstants.RTP_AVP);
            }
        }
        return result;
    }

    public void processAnswer(String answer) throws OperationFailedException, IllegalArgumentException {
        processAnswer(SdpUtils.parseSdpString(answer));
    }

    private void processAnswer(SessionDescription answer) throws OperationFailedException, IllegalArgumentException {
        synchronized (this.offerAnswerLock) {
            doNonSynchronisedProcessAnswer(answer);
        }
    }

    private void doNonSynchronisedProcessAnswer(SessionDescription answer) throws OperationFailedException, IllegalArgumentException {
        List<MediaDescription> remoteDescriptions = SdpUtils.extractMediaDescriptions(answer);
        setCallInfoURL(SdpUtils.getCallInfoURL(answer));
        boolean masterStreamSet = false;
        List<MediaType> seenMediaTypes = new ArrayList();
        for (MediaDescription mediaDescription : remoteDescriptions) {
            try {
                MediaType mediaType = SdpUtils.getMediaType(mediaDescription);
                if (!seenMediaTypes.contains(mediaType)) {
                    seenMediaTypes.add(mediaType);
                    MediaStreamTarget target = SdpUtils.extractDefaultTarget(mediaDescription, answer);
                    if (target.getDataAddress().getPort() == 0) {
                        closeStream(mediaType);
                    } else {
                        List<MediaFormat> supportedFormats = SdpUtils.extractFormats(mediaDescription, getDynamicPayloadTypes());
                        MediaDevice dev = getDefaultDevice(mediaType);
                        if (isDeviceActive(dev)) {
                            MediaDirection devDirection = (dev == null ? MediaDirection.INACTIVE : dev.getDirection()).and(getDirectionUserPreference(mediaType));
                            if (supportedFormats.isEmpty()) {
                                ProtocolProviderServiceSipImpl.throwOperationFailedException("Remote party sent an invalid SDP answer. The codecs in the answer are either not present or not supported", 11, null, logger);
                            }
                            StreamConnector connector = getTransportManager().getStreamConnector(mediaType);
                            MediaDirection direction = devDirection.getDirectionForAnswer(SdpUtils.getDirection(mediaDescription));
                            if (isLocallyOnHold()) {
                                direction = direction.and(MediaDirection.SENDONLY);
                            }
                            List<RTPExtension> rtpExtensions = intersectRTPExtensions(SdpUtils.extractRTPExtensions(mediaDescription, getRtpExtensionsRegistry()), getExtensionsForType(mediaType));
                            if (mediaType.equals(MediaType.VIDEO)) {
                                this.supportQualityControls = SdpUtils.containsAttribute(mediaDescription, "imageattr");
                            }
                            updateSrtpControlsForDtls(mediaType, null, mediaDescription);
                            SrtpControls srtpControls = getSrtpControls();
                            SDesControl sdesControl = (SDesControl) srtpControls.get(mediaType, SrtpControlType.SDES);
                            if (sdesControl != null) {
                                if (selectSdesCryptoSuite(true, sdesControl, mediaDescription) == null) {
                                    sdesControl.cleanup();
                                    srtpControls.remove(mediaType, SrtpControlType.SDES);
                                    logger.warn("Received unsupported sdes crypto attribute.");
                                } else {
                                    removeAndCleanupOtherSrtpControls(mediaType, SrtpControlType.SDES);
                                    addAdvertisedEncryptionMethod(SrtpControlType.SDES);
                                }
                            }
                            boolean masterStream = false;
                            if (!masterStreamSet) {
                                if (remoteDescriptions.size() > 1) {
                                    if (mediaType.equals(MediaType.AUDIO)) {
                                        masterStream = true;
                                        masterStreamSet = true;
                                    }
                                } else {
                                    masterStream = true;
                                    masterStreamSet = true;
                                }
                            }
                            try {
                                if (mediaDescription.getAttribute("zrtp-hash") != null) {
                                    addAdvertisedEncryptionMethod(SrtpControlType.ZRTP);
                                }
                            } catch (SdpParseException e) {
                                logger.error("received an unparsable sdp attribute", e);
                            }
                            initStream(connector, dev, (MediaFormat) supportedFormats.get(0), target, direction, rtpExtensions, masterStream);
                        } else {
                            closeStream(mediaType);
                        }
                    }
                }
            } catch (IllegalArgumentException e2) {
                logger.info("Remote party added to answer a media type that we don't understand. Ignoring stream.");
            }
        }
    }

    private String getUserName() {
        return ((ProtocolProviderServiceSipImpl) ((CallPeerSipImpl) getPeer()).getProtocolProvider()).getAccountID().getUserID();
    }

    private MediaDescription createMediaDescription(String transport, List<MediaFormat> formats, StreamConnector connector, MediaDirection direction, List<RTPExtension> extensions) throws OperationFailedException {
        return SdpUtils.createMediaDescription(transport, formats, connector, direction, extensions, getDynamicPayloadTypes(), getRtpExtensionsRegistry());
    }

    public URL getCallInfoURL() {
        return this.callInfoURL;
    }

    private void setCallInfoURL(URL callInfolURL) {
        this.callInfoURL = callInfolURL;
    }

    /* access modifiers changed from: protected */
    public NetworkAddressManagerService getNetworkAddressManagerService() {
        return SipActivator.getNetworkAddressManagerService();
    }

    /* access modifiers changed from: protected */
    public ConfigurationService getConfigurationService() {
        return SipActivator.getConfigurationService();
    }

    /* access modifiers changed from: protected */
    public MediaService getMediaService() {
        return SipActivator.getMediaService();
    }

    /* access modifiers changed from: protected */
    public void throwOperationFailedException(String message, int errorCode, Throwable cause) throws OperationFailedException {
        ProtocolProviderServiceSipImpl.throwOperationFailedException(message, errorCode, cause, logger);
    }

    /* access modifiers changed from: protected */
    public TransportManagerSipImpl getTransportManager() {
        return this.transportManager;
    }

    /* access modifiers changed from: protected */
    public TransportManagerSipImpl queryTransportManager() {
        return this.transportManager;
    }

    public QualityControl getQualityControl() {
        if (this.supportQualityControls) {
            return this.qualityControls;
        }
        return null;
    }

    public void setSupportQualityControls(boolean value) {
        this.supportQualityControls = value;
    }

    /* access modifiers changed from: protected */
    public SrtpCryptoAttribute selectSdesCryptoSuite(boolean isInitiator, SDesControl sDesControl, MediaDescription mediaDescription) {
        Vector<Attribute> attrs = mediaDescription.getAttributes(true);
        Vector<SrtpCryptoAttribute> peerAttributes = new Vector(attrs.size());
        for (int i = 0; i < attrs.size(); i++) {
            try {
                Attribute a = (Attribute) attrs.get(i);
                if (a.getName().equals(CryptoPacketExtension.ELEMENT_NAME)) {
                    peerAttributes.add(SrtpCryptoAttribute.create(a.getValue()));
                }
            } catch (SdpParseException e) {
                logger.error("received an unparsable sdp attribute", e);
            }
        }
        if (isInitiator) {
            return sDesControl.initiatorSelectAttribute(peerAttributes);
        }
        return sDesControl.responderSelectAttribute(peerAttributes);
    }

    /* access modifiers changed from: protected */
    public void setAndAddPreferredEncryptionProtocol(MediaType mediaType, MediaDescription localMd, MediaDescription remoteMd) {
        for (String preferredEncryptionProtocol : ((ProtocolProviderServiceSipImpl) ((CallPeerSipImpl) getPeer()).getProtocolProvider()).getAccountID().getSortedEnabledEncryptionProtocolList()) {
            String protoName = preferredEncryptionProtocol.substring("ENCRYPTION_PROTOCOL".length() + 1);
            if (DtlsControl.PROTO_NAME.equals(protoName)) {
                if (updateMediaDescriptionForDtls(mediaType, localMd, remoteMd)) {
                    return;
                }
            } else if (SDesControl.PROTO_NAME.equals(protoName)) {
                if (updateMediaDescriptionForSDes(mediaType, localMd, remoteMd)) {
                    return;
                }
            } else if (ZrtpControl.PROTO_NAME.equals(protoName) && updateMediaDescriptionForZrtp(mediaType, localMd, remoteMd)) {
                return;
            }
        }
    }

    public void start() throws IllegalStateException {
        synchronized (this.offerAnswerLock) {
            CallPeerMediaHandlerSipImpl.super.start();
        }
    }

    private boolean isDtlsMediaDescription(MediaDescription mediaDescription) {
        if (mediaDescription == null) {
            return false;
        }
        Media media = mediaDescription.getMedia();
        if (media == null) {
            return false;
        }
        try {
            String proto = media.getProtocol();
            if (!"UDP/TLS/RTP/SAVP".equals(proto) && !"UDP/TLS/RTP/SAVPF".equals(proto) && !"RTP/SAVPF".equals(proto)) {
                return false;
            }
            String fingerprint = mediaDescription.getAttribute("fingerprint");
            if (fingerprint == null || fingerprint.length() == 0) {
                return false;
            }
            String setup = mediaDescription.getAttribute(DTLS_SRTP_SETUP_ATTR);
            if (setup == null || setup.length() == 0) {
                return false;
            }
            return true;
        } catch (SdpParseException e) {
            return false;
        }
    }
}
