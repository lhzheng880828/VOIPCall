package org.jitsi.bouncycastle.pkcs.bc;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.jitsi.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.crypto.BlockCipher;
import org.jitsi.bouncycastle.crypto.CipherParameters;
import org.jitsi.bouncycastle.crypto.ExtendedDigest;
import org.jitsi.bouncycastle.crypto.engines.DESedeEngine;
import org.jitsi.bouncycastle.crypto.engines.RC2Engine;
import org.jitsi.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import org.jitsi.bouncycastle.crypto.io.MacOutputStream;
import org.jitsi.bouncycastle.crypto.macs.HMac;
import org.jitsi.bouncycastle.crypto.modes.CBCBlockCipher;
import org.jitsi.bouncycastle.crypto.paddings.PKCS7Padding;
import org.jitsi.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.jitsi.bouncycastle.crypto.params.DESedeParameters;
import org.jitsi.bouncycastle.crypto.params.KeyParameter;
import org.jitsi.bouncycastle.crypto.params.ParametersWithIV;
import org.jitsi.bouncycastle.operator.GenericKey;
import org.jitsi.bouncycastle.operator.MacCalculator;
import org.jitsi.bouncycastle.util.Integers;

class PKCS12PBEUtils {
    private static Set desAlgs = new HashSet();
    private static Map keySizes = new HashMap();
    private static Set noIvAlgs = new HashSet();

    static {
        keySizes.put(PKCSObjectIdentifiers.pbeWithSHAAnd128BitRC4, Integers.valueOf(128));
        keySizes.put(PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC4, Integers.valueOf(40));
        keySizes.put(PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC, Integers.valueOf(192));
        keySizes.put(PKCSObjectIdentifiers.pbeWithSHAAnd2_KeyTripleDES_CBC, Integers.valueOf(128));
        keySizes.put(PKCSObjectIdentifiers.pbeWithSHAAnd128BitRC2_CBC, Integers.valueOf(128));
        keySizes.put(PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC2_CBC, Integers.valueOf(40));
        noIvAlgs.add(PKCSObjectIdentifiers.pbeWithSHAAnd128BitRC4);
        noIvAlgs.add(PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC4);
        desAlgs.add(PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC);
        desAlgs.add(PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC);
    }

    PKCS12PBEUtils() {
    }

    static CipherParameters createCipherParameters(ASN1ObjectIdentifier aSN1ObjectIdentifier, ExtendedDigest extendedDigest, int i, PKCS12PBEParams pKCS12PBEParams, char[] cArr) {
        PKCS12ParametersGenerator pKCS12ParametersGenerator = new PKCS12ParametersGenerator(extendedDigest);
        pKCS12ParametersGenerator.init(PKCS12ParametersGenerator.PKCS12PasswordToBytes(cArr), pKCS12PBEParams.getIV(), pKCS12PBEParams.getIterations().intValue());
        if (hasNoIv(aSN1ObjectIdentifier)) {
            return pKCS12ParametersGenerator.generateDerivedParameters(getKeySize(aSN1ObjectIdentifier));
        }
        CipherParameters generateDerivedParameters = pKCS12ParametersGenerator.generateDerivedParameters(getKeySize(aSN1ObjectIdentifier), i * 8);
        if (!isDesAlg(aSN1ObjectIdentifier)) {
            return generateDerivedParameters;
        }
        DESedeParameters.setOddParity(((KeyParameter) ((ParametersWithIV) generateDerivedParameters).getParameters()).getKey());
        return generateDerivedParameters;
    }

    static MacCalculator createMacCalculator(final ASN1ObjectIdentifier aSN1ObjectIdentifier, ExtendedDigest extendedDigest, final PKCS12PBEParams pKCS12PBEParams, final char[] cArr) {
        PKCS12ParametersGenerator pKCS12ParametersGenerator = new PKCS12ParametersGenerator(extendedDigest);
        pKCS12ParametersGenerator.init(PKCS12ParametersGenerator.PKCS12PasswordToBytes(cArr), pKCS12PBEParams.getIV(), pKCS12PBEParams.getIterations().intValue());
        KeyParameter keyParameter = (KeyParameter) pKCS12ParametersGenerator.generateDerivedMacParameters(extendedDigest.getDigestSize() * 8);
        final HMac hMac = new HMac(extendedDigest);
        hMac.init(keyParameter);
        return new MacCalculator() {
            public AlgorithmIdentifier getAlgorithmIdentifier() {
                return new AlgorithmIdentifier(aSN1ObjectIdentifier, pKCS12PBEParams);
            }

            public GenericKey getKey() {
                return new GenericKey(getAlgorithmIdentifier(), PKCS12ParametersGenerator.PKCS12PasswordToBytes(cArr));
            }

            public byte[] getMac() {
                byte[] bArr = new byte[hMac.getMacSize()];
                hMac.doFinal(bArr, 0);
                return bArr;
            }

            public OutputStream getOutputStream() {
                return new MacOutputStream(hMac);
            }
        };
    }

    static PaddedBufferedBlockCipher getEngine(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        BlockCipher dESedeEngine;
        if (aSN1ObjectIdentifier.equals(PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC) || aSN1ObjectIdentifier.equals(PKCSObjectIdentifiers.pbeWithSHAAnd2_KeyTripleDES_CBC)) {
            dESedeEngine = new DESedeEngine();
        } else if (aSN1ObjectIdentifier.equals(PKCSObjectIdentifiers.pbeWithSHAAnd128BitRC2_CBC) || aSN1ObjectIdentifier.equals(PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC2_CBC)) {
            dESedeEngine = new RC2Engine();
        } else {
            throw new IllegalStateException("unknown algorithm");
        }
        return new PaddedBufferedBlockCipher(new CBCBlockCipher(dESedeEngine), new PKCS7Padding());
    }

    static int getKeySize(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return ((Integer) keySizes.get(aSN1ObjectIdentifier)).intValue();
    }

    static boolean hasNoIv(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return noIvAlgs.contains(aSN1ObjectIdentifier);
    }

    static boolean isDesAlg(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return desAlgs.contains(aSN1ObjectIdentifier);
    }
}
