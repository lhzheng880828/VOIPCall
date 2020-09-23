package org.jitsi.bouncycastle.cms;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.HashMap;
import javax.crypto.KeyGenerator;
import org.jitsi.bouncycastle.asn1.ASN1EncodableVector;
import org.jitsi.bouncycastle.asn1.ASN1Integer;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.ASN1Set;
import org.jitsi.bouncycastle.asn1.BERSequenceGenerator;
import org.jitsi.bouncycastle.asn1.BERSet;
import org.jitsi.bouncycastle.asn1.DERSet;
import org.jitsi.bouncycastle.asn1.DERTaggedObject;
import org.jitsi.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.cms.EnvelopedData;
import org.jitsi.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.jitsi.bouncycastle.operator.GenericKey;
import org.jitsi.bouncycastle.operator.OutputEncryptor;

public class CMSEnvelopedDataStreamGenerator extends CMSEnvelopedGenerator {
    private boolean _berEncodeRecipientSet;
    private int _bufferSize;
    private ASN1Set _unprotectedAttributes = null;

    private class CmsEnvelopedDataOutputStream extends OutputStream {
        private BERSequenceGenerator _cGen;
        private BERSequenceGenerator _eiGen;
        private BERSequenceGenerator _envGen;
        private OutputStream _out;

        public CmsEnvelopedDataOutputStream(OutputStream outputStream, BERSequenceGenerator bERSequenceGenerator, BERSequenceGenerator bERSequenceGenerator2, BERSequenceGenerator bERSequenceGenerator3) {
            this._out = outputStream;
            this._cGen = bERSequenceGenerator;
            this._envGen = bERSequenceGenerator2;
            this._eiGen = bERSequenceGenerator3;
        }

        public void close() throws IOException {
            this._out.close();
            this._eiGen.close();
            if (CMSEnvelopedDataStreamGenerator.this.unprotectedAttributeGenerator != null) {
                this._envGen.addObject(new DERTaggedObject(false, 1, new BERSet(CMSEnvelopedDataStreamGenerator.this.unprotectedAttributeGenerator.getAttributes(new HashMap()).toASN1EncodableVector())));
            }
            this._envGen.close();
            this._cGen.close();
        }

        public void write(int i) throws IOException {
            this._out.write(i);
        }

        public void write(byte[] bArr) throws IOException {
            this._out.write(bArr);
        }

        public void write(byte[] bArr, int i, int i2) throws IOException {
            this._out.write(bArr, i, i2);
        }
    }

    public CMSEnvelopedDataStreamGenerator(SecureRandom secureRandom) {
        super(secureRandom);
    }

    private OutputStream doOpen(ASN1ObjectIdentifier aSN1ObjectIdentifier, OutputStream outputStream, OutputEncryptor outputEncryptor) throws IOException, CMSException {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        GenericKey key = outputEncryptor.getKey();
        for (RecipientInfoGenerator generate : this.recipientInfoGenerators) {
            aSN1EncodableVector.add(generate.generate(key));
        }
        return open(aSN1ObjectIdentifier, outputStream, aSN1EncodableVector, outputEncryptor);
    }

    private ASN1Integer getVersion() {
        return (this.originatorInfo == null && this._unprotectedAttributes == null) ? new ASN1Integer(0) : new ASN1Integer(2);
    }

    private OutputStream open(OutputStream outputStream, String str, int i, Provider provider, Provider provider2) throws NoSuchAlgorithmException, CMSException, IOException {
        convertOldRecipients(this.rand, provider2);
        JceCMSContentEncryptorBuilder jceCMSContentEncryptorBuilder = i != -1 ? new JceCMSContentEncryptorBuilder(new ASN1ObjectIdentifier(str), i) : new JceCMSContentEncryptorBuilder(new ASN1ObjectIdentifier(str));
        jceCMSContentEncryptorBuilder.setProvider(provider);
        jceCMSContentEncryptorBuilder.setSecureRandom(this.rand);
        return doOpen(CMSObjectIdentifiers.data, outputStream, jceCMSContentEncryptorBuilder.build());
    }

    public OutputStream open(OutputStream outputStream, String str, int i, String str2) throws NoSuchAlgorithmException, NoSuchProviderException, CMSException, IOException {
        return open(outputStream, str, i, CMSUtils.getProvider(str2));
    }

    public OutputStream open(OutputStream outputStream, String str, int i, Provider provider) throws NoSuchAlgorithmException, CMSException, IOException {
        KeyGenerator createSymmetricKeyGenerator = CMSEnvelopedHelper.INSTANCE.createSymmetricKeyGenerator(str, provider);
        createSymmetricKeyGenerator.init(i, this.rand);
        return open(outputStream, str, -1, createSymmetricKeyGenerator.getProvider(), provider);
    }

    public OutputStream open(OutputStream outputStream, String str, String str2) throws NoSuchAlgorithmException, NoSuchProviderException, CMSException, IOException {
        return open(outputStream, str, CMSUtils.getProvider(str2));
    }

    public OutputStream open(OutputStream outputStream, String str, Provider provider) throws NoSuchAlgorithmException, CMSException, IOException {
        KeyGenerator createSymmetricKeyGenerator = CMSEnvelopedHelper.INSTANCE.createSymmetricKeyGenerator(str, provider);
        createSymmetricKeyGenerator.init(this.rand);
        return open(outputStream, str, -1, createSymmetricKeyGenerator.getProvider(), provider);
    }

    /* access modifiers changed from: protected */
    public OutputStream open(OutputStream outputStream, ASN1EncodableVector aSN1EncodableVector, OutputEncryptor outputEncryptor) throws CMSException {
        try {
            BERSequenceGenerator bERSequenceGenerator = new BERSequenceGenerator(outputStream);
            bERSequenceGenerator.addObject(CMSObjectIdentifiers.envelopedData);
            BERSequenceGenerator bERSequenceGenerator2 = new BERSequenceGenerator(bERSequenceGenerator.getRawOutputStream(), 0, true);
            ASN1Set bERSet = this._berEncodeRecipientSet ? new BERSet(aSN1EncodableVector) : new DERSet(aSN1EncodableVector);
            bERSequenceGenerator2.addObject(new ASN1Integer((long) EnvelopedData.calculateVersion(this.originatorInfo, bERSet, this._unprotectedAttributes)));
            if (this.originatorInfo != null) {
                bERSequenceGenerator2.addObject(new DERTaggedObject(false, 0, this.originatorInfo));
            }
            bERSequenceGenerator2.getRawOutputStream().write(bERSet.getEncoded());
            BERSequenceGenerator bERSequenceGenerator3 = new BERSequenceGenerator(bERSequenceGenerator2.getRawOutputStream());
            bERSequenceGenerator3.addObject(CMSObjectIdentifiers.data);
            bERSequenceGenerator3.getRawOutputStream().write(outputEncryptor.getAlgorithmIdentifier().getEncoded());
            return new CmsEnvelopedDataOutputStream(outputEncryptor.getOutputStream(CMSUtils.createBEROctetOutputStream(bERSequenceGenerator3.getRawOutputStream(), 0, false, this._bufferSize)), bERSequenceGenerator, bERSequenceGenerator2, bERSequenceGenerator3);
        } catch (IOException e) {
            throw new CMSException("exception decoding algorithm parameters.", e);
        }
    }

    public OutputStream open(OutputStream outputStream, OutputEncryptor outputEncryptor) throws CMSException, IOException {
        return doOpen(new ASN1ObjectIdentifier(CMSObjectIdentifiers.data.getId()), outputStream, outputEncryptor);
    }

    /* access modifiers changed from: protected */
    public OutputStream open(ASN1ObjectIdentifier aSN1ObjectIdentifier, OutputStream outputStream, ASN1EncodableVector aSN1EncodableVector, OutputEncryptor outputEncryptor) throws IOException {
        BERSequenceGenerator bERSequenceGenerator = new BERSequenceGenerator(outputStream);
        bERSequenceGenerator.addObject(CMSObjectIdentifiers.envelopedData);
        BERSequenceGenerator bERSequenceGenerator2 = new BERSequenceGenerator(bERSequenceGenerator.getRawOutputStream(), 0, true);
        bERSequenceGenerator2.addObject(getVersion());
        if (this.originatorInfo != null) {
            bERSequenceGenerator2.addObject(new DERTaggedObject(false, 0, this.originatorInfo));
        }
        if (this._berEncodeRecipientSet) {
            bERSequenceGenerator2.getRawOutputStream().write(new BERSet(aSN1EncodableVector).getEncoded());
        } else {
            bERSequenceGenerator2.getRawOutputStream().write(new DERSet(aSN1EncodableVector).getEncoded());
        }
        BERSequenceGenerator bERSequenceGenerator3 = new BERSequenceGenerator(bERSequenceGenerator2.getRawOutputStream());
        bERSequenceGenerator3.addObject(aSN1ObjectIdentifier);
        bERSequenceGenerator3.getRawOutputStream().write(outputEncryptor.getAlgorithmIdentifier().getEncoded());
        return new CmsEnvelopedDataOutputStream(outputEncryptor.getOutputStream(CMSUtils.createBEROctetOutputStream(bERSequenceGenerator3.getRawOutputStream(), 0, false, this._bufferSize)), bERSequenceGenerator, bERSequenceGenerator2, bERSequenceGenerator3);
    }

    public OutputStream open(ASN1ObjectIdentifier aSN1ObjectIdentifier, OutputStream outputStream, OutputEncryptor outputEncryptor) throws CMSException, IOException {
        return doOpen(aSN1ObjectIdentifier, outputStream, outputEncryptor);
    }

    public void setBEREncodeRecipients(boolean z) {
        this._berEncodeRecipientSet = z;
    }

    public void setBufferSize(int i) {
        this._bufferSize = i;
    }
}
