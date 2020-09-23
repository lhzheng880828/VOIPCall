package org.jitsi.bouncycastle.cert.crmf.jcajce;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Provider;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.cert.crmf.CRMFException;
import org.jitsi.bouncycastle.cert.crmf.PKMACValuesCalculator;
import org.jitsi.bouncycastle.jcajce.DefaultJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.NamedJcaJceHelper;
import org.jitsi.bouncycastle.jcajce.ProviderJcaJceHelper;

public class JcePKMACValuesCalculator implements PKMACValuesCalculator {
    private MessageDigest digest;
    private CRMFHelper helper = new CRMFHelper(new DefaultJcaJceHelper());
    private Mac mac;

    public byte[] calculateDigest(byte[] bArr) {
        return this.digest.digest(bArr);
    }

    public byte[] calculateMac(byte[] bArr, byte[] bArr2) throws CRMFException {
        try {
            this.mac.init(new SecretKeySpec(bArr, this.mac.getAlgorithm()));
            return this.mac.doFinal(bArr2);
        } catch (GeneralSecurityException e) {
            throw new CRMFException("failure in setup: " + e.getMessage(), e);
        }
    }

    public JcePKMACValuesCalculator setProvider(String str) {
        this.helper = new CRMFHelper(new NamedJcaJceHelper(str));
        return this;
    }

    public JcePKMACValuesCalculator setProvider(Provider provider) {
        this.helper = new CRMFHelper(new ProviderJcaJceHelper(provider));
        return this;
    }

    public void setup(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2) throws CRMFException {
        this.digest = this.helper.createDigest(algorithmIdentifier.getAlgorithm());
        this.mac = this.helper.createMac(algorithmIdentifier2.getAlgorithm());
    }
}
