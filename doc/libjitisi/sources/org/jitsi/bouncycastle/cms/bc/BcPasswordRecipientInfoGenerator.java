package org.jitsi.bouncycastle.cms.bc;

import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.ASN1OctetString;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.cms.CMSException;
import org.jitsi.bouncycastle.cms.PasswordRecipientInfoGenerator;
import org.jitsi.bouncycastle.crypto.Wrapper;
import org.jitsi.bouncycastle.crypto.params.KeyParameter;
import org.jitsi.bouncycastle.crypto.params.ParametersWithIV;
import org.jitsi.bouncycastle.operator.GenericKey;

public class BcPasswordRecipientInfoGenerator extends PasswordRecipientInfoGenerator {
    public BcPasswordRecipientInfoGenerator(ASN1ObjectIdentifier aSN1ObjectIdentifier, char[] cArr) {
        super(aSN1ObjectIdentifier, cArr);
    }

    public byte[] generateEncryptedBytes(AlgorithmIdentifier algorithmIdentifier, byte[] bArr, GenericKey genericKey) throws CMSException {
        byte[] key = ((KeyParameter) CMSUtils.getBcKey(genericKey)).getKey();
        Wrapper createRFC3211Wrapper = EnvelopedDataHelper.createRFC3211Wrapper(algorithmIdentifier.getAlgorithm());
        createRFC3211Wrapper.init(true, new ParametersWithIV(new KeyParameter(bArr), ASN1OctetString.getInstance(algorithmIdentifier.getParameters()).getOctets()));
        return createRFC3211Wrapper.wrap(key, 0, key.length);
    }
}
