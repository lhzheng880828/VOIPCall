package org.jitsi.impl.neomedia.transform.sdes;

import ch.imvs.sdes4j.srtp.SrtpCryptoAttribute;
import ch.imvs.sdes4j.srtp.SrtpCryptoSuite;
import ch.imvs.sdes4j.srtp.SrtpSessionParam;
import org.jitsi.impl.neomedia.transform.PacketTransformer;
import org.jitsi.impl.neomedia.transform.srtp.SRTCPTransformer;
import org.jitsi.impl.neomedia.transform.srtp.SRTPContextFactory;
import org.jitsi.impl.neomedia.transform.srtp.SRTPPolicy;
import org.jitsi.impl.neomedia.transform.srtp.SRTPTransformer;
import org.jitsi.service.neomedia.SrtpControl.TransformEngine;

public class SDesTransformEngine implements TransformEngine {
    private SRTCPTransformer srtcpTransformer;
    private SRTPTransformer srtpTransformer;

    public SDesTransformEngine(SrtpCryptoAttribute inAttribute, SrtpCryptoAttribute outAttribute) {
        update(inAttribute, outAttribute);
    }

    public void update(SrtpCryptoAttribute inAttribute, SrtpCryptoAttribute outAttribute) {
        SRTPContextFactory forwardCtx = getTransformEngine(outAttribute, true);
        SRTPContextFactory reverseCtx = getTransformEngine(inAttribute, false);
        this.srtpTransformer = new SRTPTransformer(forwardCtx, reverseCtx);
        this.srtcpTransformer = new SRTCPTransformer(forwardCtx, reverseCtx);
    }

    public void cleanup() {
        if (this.srtpTransformer != null) {
            this.srtpTransformer.close();
        }
        if (this.srtcpTransformer != null) {
            this.srtcpTransformer.close();
        }
        this.srtpTransformer = null;
        this.srtcpTransformer = null;
    }

    private static SRTPContextFactory getTransformEngine(SrtpCryptoAttribute attribute, boolean sender) {
        SrtpSessionParam[] sessionParams = attribute.getSessionParams();
        if (sessionParams == null || sessionParams.length <= 0) {
            SrtpCryptoSuite cryptoSuite = attribute.getCryptoSuite();
            return new SRTPContextFactory(sender, getKey(attribute), getSalt(attribute), new SRTPPolicy(getEncryptionCipher(cryptoSuite), cryptoSuite.getEncKeyLength() / 8, getHashAlgorithm(cryptoSuite), cryptoSuite.getSrtpAuthKeyLength() / 8, cryptoSuite.getSrtpAuthTagLength() / 8, cryptoSuite.getSaltKeyLength() / 8), new SRTPPolicy(getEncryptionCipher(cryptoSuite), cryptoSuite.getEncKeyLength() / 8, getHashAlgorithm(cryptoSuite), cryptoSuite.getSrtcpAuthKeyLength() / 8, cryptoSuite.getSrtcpAuthTagLength() / 8, cryptoSuite.getSaltKeyLength() / 8));
        }
        throw new IllegalArgumentException("session parameters are not supported");
    }

    private static byte[] getKey(SrtpCryptoAttribute attribute) {
        int length = attribute.getCryptoSuite().getEncKeyLength() / 8;
        byte[] key = new byte[length];
        System.arraycopy(attribute.getKeyParams()[0].getKey(), 0, key, 0, length);
        return key;
    }

    private static byte[] getSalt(SrtpCryptoAttribute attribute) {
        int keyLength = attribute.getCryptoSuite().getEncKeyLength() / 8;
        byte[] salt = new byte[keyLength];
        System.arraycopy(attribute.getKeyParams()[0].getKey(), keyLength, salt, 0, attribute.getCryptoSuite().getSaltKeyLength() / 8);
        return salt;
    }

    private static int getEncryptionCipher(SrtpCryptoSuite cs) {
        switch (cs.getEncryptionAlgorithm()) {
            case 1:
                return 1;
            case 2:
                return 2;
            default:
                throw new IllegalArgumentException("Unsupported cipher");
        }
    }

    private static int getHashAlgorithm(SrtpCryptoSuite cs) {
        switch (cs.getHashAlgorithm()) {
            case 1:
                return 1;
            default:
                throw new IllegalArgumentException("Unsupported hash");
        }
    }

    public PacketTransformer getRTPTransformer() {
        return this.srtpTransformer;
    }

    public PacketTransformer getRTCPTransformer() {
        return this.srtcpTransformer;
    }
}
