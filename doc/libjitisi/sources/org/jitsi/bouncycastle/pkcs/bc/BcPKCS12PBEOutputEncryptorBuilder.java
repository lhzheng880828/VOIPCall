package org.jitsi.bouncycastle.pkcs.bc;

import java.io.OutputStream;
import java.security.SecureRandom;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.jitsi.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.jitsi.bouncycastle.crypto.BlockCipher;
import org.jitsi.bouncycastle.crypto.BufferedBlockCipher;
import org.jitsi.bouncycastle.crypto.ExtendedDigest;
import org.jitsi.bouncycastle.crypto.digests.SHA1Digest;
import org.jitsi.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import org.jitsi.bouncycastle.crypto.io.CipherOutputStream;
import org.jitsi.bouncycastle.crypto.paddings.PKCS7Padding;
import org.jitsi.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.jitsi.bouncycastle.operator.GenericKey;
import org.jitsi.bouncycastle.operator.OutputEncryptor;

public class BcPKCS12PBEOutputEncryptorBuilder {
    /* access modifiers changed from: private */
    public ASN1ObjectIdentifier algorithm;
    private ExtendedDigest digest;
    /* access modifiers changed from: private */
    public BufferedBlockCipher engine;
    private SecureRandom random;

    public BcPKCS12PBEOutputEncryptorBuilder(ASN1ObjectIdentifier aSN1ObjectIdentifier, BlockCipher blockCipher) {
        this(aSN1ObjectIdentifier, blockCipher, new SHA1Digest());
    }

    public BcPKCS12PBEOutputEncryptorBuilder(ASN1ObjectIdentifier aSN1ObjectIdentifier, BlockCipher blockCipher, ExtendedDigest extendedDigest) {
        this.algorithm = aSN1ObjectIdentifier;
        this.engine = new PaddedBufferedBlockCipher(blockCipher, new PKCS7Padding());
        this.digest = extendedDigest;
    }

    public OutputEncryptor build(final char[] cArr) {
        if (this.random == null) {
            this.random = new SecureRandom();
        }
        byte[] bArr = new byte[20];
        this.random.nextBytes(bArr);
        final PKCS12PBEParams pKCS12PBEParams = new PKCS12PBEParams(bArr, 1024);
        this.engine.init(true, PKCS12PBEUtils.createCipherParameters(this.algorithm, this.digest, this.engine.getBlockSize(), pKCS12PBEParams, cArr));
        return new OutputEncryptor() {
            public AlgorithmIdentifier getAlgorithmIdentifier() {
                return new AlgorithmIdentifier(BcPKCS12PBEOutputEncryptorBuilder.this.algorithm, pKCS12PBEParams);
            }

            public GenericKey getKey() {
                return new GenericKey(new AlgorithmIdentifier(BcPKCS12PBEOutputEncryptorBuilder.this.algorithm, pKCS12PBEParams), PKCS12ParametersGenerator.PKCS12PasswordToBytes(cArr));
            }

            public OutputStream getOutputStream(OutputStream outputStream) {
                return new CipherOutputStream(outputStream, BcPKCS12PBEOutputEncryptorBuilder.this.engine);
            }
        };
    }
}
