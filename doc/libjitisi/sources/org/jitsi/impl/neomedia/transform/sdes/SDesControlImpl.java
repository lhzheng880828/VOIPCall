package org.jitsi.impl.neomedia.transform.sdes;

import ch.imvs.sdes4j.srtp.SrtpCryptoAttribute;
import ch.imvs.sdes4j.srtp.SrtpSDesFactory;
import gnu.java.zrtp.utils.ZrtpFortuna;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.jitsi.impl.neomedia.AbstractRTPConnector;
import org.jitsi.service.neomedia.AbstractSrtpControl;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.SDesControl;
import org.jitsi.service.neomedia.SrtpControlType;
import org.jitsi.service.neomedia.event.SrtpListener;

public class SDesControlImpl extends AbstractSrtpControl<SDesTransformEngine> implements SDesControl {
    private SrtpCryptoAttribute[] attributes;
    private final List<String> enabledCryptoSuites = new ArrayList(3);
    private SrtpSDesFactory sdesFactory;
    private SrtpCryptoAttribute selectedInAttribute;
    private SrtpCryptoAttribute selectedOutAttribute;
    private final List<String> supportedCryptoSuites = new ArrayList(3);

    public SDesControlImpl() {
        super(SrtpControlType.SDES);
        this.enabledCryptoSuites.add("AES_CM_128_HMAC_SHA1_80");
        this.enabledCryptoSuites.add("AES_CM_128_HMAC_SHA1_32");
        this.enabledCryptoSuites.add("F8_128_HMAC_SHA1_80");
        this.supportedCryptoSuites.add("AES_CM_128_HMAC_SHA1_80");
        this.supportedCryptoSuites.add("AES_CM_128_HMAC_SHA1_32");
        this.supportedCryptoSuites.add("F8_128_HMAC_SHA1_80");
        this.sdesFactory = new SrtpSDesFactory();
        this.sdesFactory.setRandomGenerator(new Random() {
            private static final long serialVersionUID = 0;

            public void nextBytes(byte[] bytes) {
                ZrtpFortuna.getInstance().getFortuna().nextBytes(bytes);
            }
        });
    }

    public SrtpCryptoAttribute getInAttribute() {
        return this.selectedInAttribute;
    }

    public SrtpCryptoAttribute[] getInitiatorCryptoAttributes() {
        initAttributes();
        return this.attributes;
    }

    public SrtpCryptoAttribute getOutAttribute() {
        return this.selectedOutAttribute;
    }

    public boolean getSecureCommunicationStatus() {
        return this.transformEngine != null;
    }

    public Iterable<String> getSupportedCryptoSuites() {
        return Collections.unmodifiableList(this.supportedCryptoSuites);
    }

    /* access modifiers changed from: protected */
    public SDesTransformEngine createTransformEngine() {
        return new SDesTransformEngine(this.selectedInAttribute, this.selectedOutAttribute);
    }

    private void initAttributes() {
        if (this.attributes != null) {
            return;
        }
        if (this.selectedOutAttribute != null) {
            this.attributes = new SrtpCryptoAttribute[1];
            this.attributes[0] = this.selectedOutAttribute;
            return;
        }
        this.attributes = new SrtpCryptoAttribute[this.enabledCryptoSuites.size()];
        for (int i = 0; i < this.attributes.length; i++) {
            this.attributes[i] = this.sdesFactory.createCryptoAttribute(i + 1, (String) this.enabledCryptoSuites.get(i));
        }
    }

    public SrtpCryptoAttribute initiatorSelectAttribute(Iterable<SrtpCryptoAttribute> peerAttributes) {
        for (SrtpCryptoAttribute peerCA : peerAttributes) {
            for (SrtpCryptoAttribute localCA : this.attributes) {
                if (localCA.getCryptoSuite().equals(peerCA.getCryptoSuite())) {
                    this.selectedInAttribute = peerCA;
                    this.selectedOutAttribute = localCA;
                    if (this.transformEngine == null) {
                        return peerCA;
                    }
                    ((SDesTransformEngine) this.transformEngine).update(this.selectedInAttribute, this.selectedOutAttribute);
                    return peerCA;
                }
            }
        }
        return null;
    }

    public boolean requiresSecureSignalingTransport() {
        return true;
    }

    public SrtpCryptoAttribute responderSelectAttribute(Iterable<SrtpCryptoAttribute> peerAttributes) {
        for (SrtpCryptoAttribute ea : peerAttributes) {
            for (String suite : this.enabledCryptoSuites) {
                if (suite.equals(ea.getCryptoSuite().encode())) {
                    this.selectedInAttribute = ea;
                    this.selectedOutAttribute = this.sdesFactory.createCryptoAttribute(1, suite);
                    if (this.transformEngine != null) {
                        ((SDesTransformEngine) this.transformEngine).update(this.selectedInAttribute, this.selectedOutAttribute);
                    }
                    return this.selectedOutAttribute;
                }
            }
        }
        return null;
    }

    public void setConnector(AbstractRTPConnector connector) {
    }

    public void setEnabledCiphers(Iterable<String> ciphers) {
        this.enabledCryptoSuites.clear();
        for (String c : ciphers) {
            this.enabledCryptoSuites.add(c);
        }
    }

    public void start(MediaType mediaType) {
        SrtpListener srtpListener = getSrtpListener();
        srtpListener.securityNegotiationStarted(mediaType, this);
        srtpListener.securityTurnedOn(mediaType, this.selectedInAttribute.getCryptoSuite().encode(), this);
    }
}
