package org.jitsi.bouncycastle.cms;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import org.jitsi.bouncycastle.crypto.PBEParametersGenerator;
import org.jitsi.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.jitsi.bouncycastle.crypto.params.KeyParameter;

public class PKCS5Scheme2PBEKey extends CMSPBEKey {
    public PKCS5Scheme2PBEKey(char[] cArr, AlgorithmParameters algorithmParameters) throws InvalidAlgorithmParameterException {
        super(cArr, CMSPBEKey.getParamSpec(algorithmParameters));
    }

    public PKCS5Scheme2PBEKey(char[] cArr, byte[] bArr, int i) {
        super(cArr, bArr, i);
    }

    /* access modifiers changed from: 0000 */
    public byte[] getEncoded(String str) {
        PKCS5S2ParametersGenerator pKCS5S2ParametersGenerator = new PKCS5S2ParametersGenerator();
        pKCS5S2ParametersGenerator.init(PBEParametersGenerator.PKCS5PasswordToBytes(getPassword()), getSalt(), getIterationCount());
        return ((KeyParameter) pKCS5S2ParametersGenerator.generateDerivedParameters(CMSEnvelopedHelper.INSTANCE.getKeySize(str))).getKey();
    }
}
