package org.jitsi.bouncycastle.cms;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import org.jitsi.bouncycastle.asn1.ASN1EncodableVector;
import org.jitsi.bouncycastle.asn1.ASN1Integer;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.ASN1TaggedObject;
import org.jitsi.bouncycastle.asn1.BERSequenceGenerator;
import org.jitsi.bouncycastle.asn1.BERTaggedObject;
import org.jitsi.bouncycastle.asn1.DERSet;
import org.jitsi.bouncycastle.asn1.cms.AttributeTable;
import org.jitsi.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.cms.SignerInfo;

public class CMSSignedDataStreamGenerator extends CMSSignedGenerator {
    private int _bufferSize;

    private class CmsSignedDataOutputStream extends OutputStream {
        private ASN1ObjectIdentifier _contentOID;
        private BERSequenceGenerator _eiGen;
        private OutputStream _out;
        private BERSequenceGenerator _sGen;
        private BERSequenceGenerator _sigGen;

        public CmsSignedDataOutputStream(OutputStream outputStream, ASN1ObjectIdentifier aSN1ObjectIdentifier, BERSequenceGenerator bERSequenceGenerator, BERSequenceGenerator bERSequenceGenerator2, BERSequenceGenerator bERSequenceGenerator3) {
            this._out = outputStream;
            this._contentOID = aSN1ObjectIdentifier;
            this._sGen = bERSequenceGenerator;
            this._sigGen = bERSequenceGenerator2;
            this._eiGen = bERSequenceGenerator3;
        }

        public void close() throws IOException {
            this._out.close();
            this._eiGen.close();
            CMSSignedDataStreamGenerator.this.digests.clear();
            if (CMSSignedDataStreamGenerator.this.certs.size() != 0) {
                this._sigGen.getRawOutputStream().write(new BERTaggedObject(false, 0, CMSUtils.createBerSetFromList(CMSSignedDataStreamGenerator.this.certs)).getEncoded());
            }
            if (CMSSignedDataStreamGenerator.this.crls.size() != 0) {
                this._sigGen.getRawOutputStream().write(new BERTaggedObject(false, 1, CMSUtils.createBerSetFromList(CMSSignedDataStreamGenerator.this.crls)).getEncoded());
            }
            ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
            for (SignerInfoGenerator signerInfoGenerator : CMSSignedDataStreamGenerator.this.signerGens) {
                try {
                    aSN1EncodableVector.add(signerInfoGenerator.generate(this._contentOID));
                    CMSSignedDataStreamGenerator.this.digests.put(signerInfoGenerator.getDigestAlgorithm().getAlgorithm().getId(), signerInfoGenerator.getCalculatedDigest());
                } catch (CMSException e) {
                    throw new CMSStreamException("exception generating signers: " + e.getMessage(), e);
                }
            }
            for (SignerInformation toASN1Structure : CMSSignedDataStreamGenerator.this._signers) {
                aSN1EncodableVector.add(toASN1Structure.toASN1Structure());
            }
            this._sigGen.getRawOutputStream().write(new DERSet(aSN1EncodableVector).getEncoded());
            this._sigGen.close();
            this._sGen.close();
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

    public CMSSignedDataStreamGenerator(SecureRandom secureRandom) {
        super(secureRandom);
    }

    private ASN1Integer calculateVersion(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        Object obj;
        Object obj2;
        Object obj3;
        Object obj4 = null;
        if (this.certs != null) {
            obj = null;
            obj2 = null;
            obj3 = null;
            for (Object next : this.certs) {
                Object next2;
                if (next2 instanceof ASN1TaggedObject) {
                    ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject) next2;
                    if (aSN1TaggedObject.getTagNo() == 1) {
                        next2 = obj;
                        obj2 = 1;
                    } else if (aSN1TaggedObject.getTagNo() == 2) {
                        int i = 1;
                    } else if (aSN1TaggedObject.getTagNo() == 3) {
                        next2 = obj;
                        int obj32 = 1;
                    }
                    obj = next2;
                }
                next2 = obj;
                obj = next2;
            }
        } else {
            obj = null;
            obj2 = null;
            obj32 = null;
        }
        if (obj32 != null) {
            return new ASN1Integer(5);
        }
        if (this.crls != null) {
            for (Object obj322 : this.crls) {
                if (obj322 instanceof ASN1TaggedObject) {
                    obj4 = 1;
                }
            }
        }
        return obj4 != null ? new ASN1Integer(5) : obj != null ? new ASN1Integer(4) : obj2 != null ? new ASN1Integer(3) : checkForVersion3(this._signers, this.signerGens) ? new ASN1Integer(3) : !CMSObjectIdentifiers.data.equals(aSN1ObjectIdentifier) ? new ASN1Integer(3) : new ASN1Integer(1);
    }

    private boolean checkForVersion3(List list, List list2) {
        for (SignerInformation toASN1Structure : list) {
            if (SignerInfo.getInstance(toASN1Structure.toASN1Structure()).getVersion().getValue().intValue() == 3) {
                return true;
            }
        }
        for (SignerInfoGenerator generatedVersion : list2) {
            if (generatedVersion.getGeneratedVersion().getValue().intValue() == 3) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x00be A:{ExcHandler: CertificateEncodingException (e java.security.cert.CertificateEncodingException), Splitter:B:5:0x0033} */
    /* JADX WARNING: Missing block: B:37:0x00c6, code skipped:
            throw new java.lang.IllegalStateException("unable to encode certificate");
     */
    private void doAddSigner(java.security.PrivateKey r5, java.lang.Object r6, java.lang.String r7, java.lang.String r8, org.jitsi.bouncycastle.cms.CMSAttributeTableGenerator r9, org.jitsi.bouncycastle.cms.CMSAttributeTableGenerator r10, java.security.Provider r11, java.security.Provider r12) throws java.security.NoSuchAlgorithmException, java.security.InvalidKeyException {
        /*
        r4 = this;
        r0 = org.jitsi.bouncycastle.cms.CMSSignedHelper.INSTANCE;
        r0 = r0.getDigestAlgName(r8);
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r0 = r1.append(r0);
        r1 = "with";
        r0 = r0.append(r1);
        r1 = org.jitsi.bouncycastle.cms.CMSSignedHelper.INSTANCE;
        r1 = r1.getEncryptionAlgName(r7);
        r0 = r0.append(r1);
        r0 = r0.toString();
        r1 = new org.jitsi.bouncycastle.operator.jcajce.JcaContentSignerBuilder;	 Catch:{ IllegalArgumentException -> 0x006a }
        r1.m1605init(r0);	 Catch:{ IllegalArgumentException -> 0x006a }
        r0 = r4.rand;	 Catch:{ IllegalArgumentException -> 0x006a }
        r0 = r1.setSecureRandom(r0);	 Catch:{ IllegalArgumentException -> 0x006a }
        if (r11 == 0) goto L_0x0033;
    L_0x0030:
        r0.setProvider(r11);
    L_0x0033:
        r1 = new org.jitsi.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
        r1.m1615init();	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
        if (r12 == 0) goto L_0x0049;
    L_0x003a:
        r2 = r12.getName();	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
        r3 = "SunRsaSign";
        r2 = r2.equalsIgnoreCase(r3);	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
        if (r2 != 0) goto L_0x0049;
    L_0x0046:
        r1.setProvider(r12);	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
    L_0x0049:
        r2 = new org.jitsi.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
        r1 = r1.build();	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
        r2.m1342init(r1);	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
        r2.setSignedAttributeGenerator(r9);	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
        r2.setUnsignedAttributeGenerator(r10);	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
        r0 = r0.build(r5);	 Catch:{ OperatorCreationException -> 0x0081, CertificateEncodingException -> 0x00be }
        r1 = r6 instanceof java.security.cert.X509Certificate;	 Catch:{ OperatorCreationException -> 0x0081, CertificateEncodingException -> 0x00be }
        if (r1 == 0) goto L_0x0075;
    L_0x0060:
        r6 = (java.security.cert.X509Certificate) r6;	 Catch:{ OperatorCreationException -> 0x0081, CertificateEncodingException -> 0x00be }
        r0 = r2.build(r0, r6);	 Catch:{ OperatorCreationException -> 0x0081, CertificateEncodingException -> 0x00be }
        r4.addSignerInfoGenerator(r0);	 Catch:{ OperatorCreationException -> 0x0081, CertificateEncodingException -> 0x00be }
    L_0x0069:
        return;
    L_0x006a:
        r0 = move-exception;
        r1 = new java.security.NoSuchAlgorithmException;
        r0 = r0.getMessage();
        r1.<init>(r0);
        throw r1;
    L_0x0075:
        r6 = (byte[]) r6;	 Catch:{ OperatorCreationException -> 0x0081, CertificateEncodingException -> 0x00be }
        r6 = (byte[]) r6;	 Catch:{ OperatorCreationException -> 0x0081, CertificateEncodingException -> 0x00be }
        r0 = r2.build(r0, r6);	 Catch:{ OperatorCreationException -> 0x0081, CertificateEncodingException -> 0x00be }
        r4.addSignerInfoGenerator(r0);	 Catch:{ OperatorCreationException -> 0x0081, CertificateEncodingException -> 0x00be }
        goto L_0x0069;
    L_0x0081:
        r0 = move-exception;
        r1 = r0.getCause();	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
        r1 = r1 instanceof java.security.NoSuchAlgorithmException;	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
        if (r1 == 0) goto L_0x00af;
    L_0x008a:
        r0 = r0.getCause();	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
        r0 = (java.security.NoSuchAlgorithmException) r0;	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
        throw r0;	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
    L_0x0091:
        r0 = move-exception;
        r1 = new java.security.NoSuchAlgorithmException;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "unable to create operators: ";
        r2 = r2.append(r3);
        r0 = r0.getMessage();
        r0 = r2.append(r0);
        r0 = r0.toString();
        r1.<init>(r0);
        throw r1;
    L_0x00af:
        r1 = r0.getCause();	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
        r1 = r1 instanceof java.security.InvalidKeyException;	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
        if (r1 == 0) goto L_0x0069;
    L_0x00b7:
        r0 = r0.getCause();	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
        r0 = (java.security.InvalidKeyException) r0;	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
        throw r0;	 Catch:{ OperatorCreationException -> 0x0091, CertificateEncodingException -> 0x00be }
    L_0x00be:
        r0 = move-exception;
        r0 = new java.lang.IllegalStateException;
        r1 = "unable to encode certificate";
        r0.<init>(r1);
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.bouncycastle.cms.CMSSignedDataStreamGenerator.doAddSigner(java.security.PrivateKey, java.lang.Object, java.lang.String, java.lang.String, org.jitsi.bouncycastle.cms.CMSAttributeTableGenerator, org.jitsi.bouncycastle.cms.CMSAttributeTableGenerator, java.security.Provider, java.security.Provider):void");
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str, String str2) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        addSigner(privateKey, x509Certificate, str, CMSUtils.getProvider(str2));
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str, String str2, String str3) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        addSigner(privateKey, x509Certificate, str, str2, CMSUtils.getProvider(str3));
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str, String str2, Provider provider) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        addSigner(privateKey, x509Certificate, str, str2, new DefaultSignedAttributeTableGenerator(), (CMSAttributeTableGenerator) null, provider);
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str, String str2, AttributeTable attributeTable, AttributeTable attributeTable2, String str3) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        addSigner(privateKey, x509Certificate, str, str2, attributeTable, attributeTable2, CMSUtils.getProvider(str3));
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str, String str2, AttributeTable attributeTable, AttributeTable attributeTable2, Provider provider) throws NoSuchAlgorithmException, InvalidKeyException {
        addSigner(privateKey, x509Certificate, str, str2, new DefaultSignedAttributeTableGenerator(attributeTable), new SimpleAttributeTableGenerator(attributeTable2), provider);
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str, String str2, CMSAttributeTableGenerator cMSAttributeTableGenerator, CMSAttributeTableGenerator cMSAttributeTableGenerator2, String str3) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        addSigner(privateKey, x509Certificate, str, str2, cMSAttributeTableGenerator, cMSAttributeTableGenerator2, CMSUtils.getProvider(str3));
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str, String str2, CMSAttributeTableGenerator cMSAttributeTableGenerator, CMSAttributeTableGenerator cMSAttributeTableGenerator2, Provider provider) throws NoSuchAlgorithmException, InvalidKeyException {
        addSigner(privateKey, x509Certificate, str, str2, cMSAttributeTableGenerator, cMSAttributeTableGenerator2, provider, provider);
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str, String str2, CMSAttributeTableGenerator cMSAttributeTableGenerator, CMSAttributeTableGenerator cMSAttributeTableGenerator2, Provider provider, Provider provider2) throws NoSuchAlgorithmException, InvalidKeyException {
        doAddSigner(privateKey, x509Certificate, str, str2, cMSAttributeTableGenerator, cMSAttributeTableGenerator2, provider, provider2);
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str, Provider provider) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        addSigner(privateKey, x509Certificate, str, new DefaultSignedAttributeTableGenerator(), (CMSAttributeTableGenerator) null, provider);
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str, AttributeTable attributeTable, AttributeTable attributeTable2, String str2) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        addSigner(privateKey, x509Certificate, str, attributeTable, attributeTable2, CMSUtils.getProvider(str2));
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str, AttributeTable attributeTable, AttributeTable attributeTable2, Provider provider) throws NoSuchAlgorithmException, InvalidKeyException {
        addSigner(privateKey, x509Certificate, str, new DefaultSignedAttributeTableGenerator(attributeTable), new SimpleAttributeTableGenerator(attributeTable2), provider);
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str, CMSAttributeTableGenerator cMSAttributeTableGenerator, CMSAttributeTableGenerator cMSAttributeTableGenerator2, String str2) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        addSigner(privateKey, x509Certificate, str, cMSAttributeTableGenerator, cMSAttributeTableGenerator2, CMSUtils.getProvider(str2));
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str, CMSAttributeTableGenerator cMSAttributeTableGenerator, CMSAttributeTableGenerator cMSAttributeTableGenerator2, Provider provider) throws NoSuchAlgorithmException, InvalidKeyException {
        addSigner(privateKey, x509Certificate, getEncOID(privateKey, str), str, cMSAttributeTableGenerator, cMSAttributeTableGenerator2, provider);
    }

    public void addSigner(PrivateKey privateKey, byte[] bArr, String str, String str2) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        addSigner(privateKey, bArr, str, CMSUtils.getProvider(str2));
    }

    public void addSigner(PrivateKey privateKey, byte[] bArr, String str, String str2, String str3) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        addSigner(privateKey, bArr, str, str2, CMSUtils.getProvider(str3));
    }

    public void addSigner(PrivateKey privateKey, byte[] bArr, String str, String str2, Provider provider) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        addSigner(privateKey, bArr, str, str2, new DefaultSignedAttributeTableGenerator(), (CMSAttributeTableGenerator) null, provider);
    }

    public void addSigner(PrivateKey privateKey, byte[] bArr, String str, String str2, CMSAttributeTableGenerator cMSAttributeTableGenerator, CMSAttributeTableGenerator cMSAttributeTableGenerator2, String str3) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        addSigner(privateKey, bArr, str, str2, cMSAttributeTableGenerator, cMSAttributeTableGenerator2, CMSUtils.getProvider(str3));
    }

    public void addSigner(PrivateKey privateKey, byte[] bArr, String str, String str2, CMSAttributeTableGenerator cMSAttributeTableGenerator, CMSAttributeTableGenerator cMSAttributeTableGenerator2, Provider provider) throws NoSuchAlgorithmException, InvalidKeyException {
        addSigner(privateKey, bArr, str, str2, cMSAttributeTableGenerator, cMSAttributeTableGenerator2, provider, provider);
    }

    public void addSigner(PrivateKey privateKey, byte[] bArr, String str, String str2, CMSAttributeTableGenerator cMSAttributeTableGenerator, CMSAttributeTableGenerator cMSAttributeTableGenerator2, Provider provider, Provider provider2) throws NoSuchAlgorithmException, InvalidKeyException {
        doAddSigner(privateKey, bArr, str, str2, cMSAttributeTableGenerator, cMSAttributeTableGenerator2, provider, provider2);
    }

    public void addSigner(PrivateKey privateKey, byte[] bArr, String str, Provider provider) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        addSigner(privateKey, bArr, str, new DefaultSignedAttributeTableGenerator(), (CMSAttributeTableGenerator) null, provider);
    }

    public void addSigner(PrivateKey privateKey, byte[] bArr, String str, AttributeTable attributeTable, AttributeTable attributeTable2, String str2) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        addSigner(privateKey, bArr, str, attributeTable, attributeTable2, CMSUtils.getProvider(str2));
    }

    public void addSigner(PrivateKey privateKey, byte[] bArr, String str, AttributeTable attributeTable, AttributeTable attributeTable2, Provider provider) throws NoSuchAlgorithmException, InvalidKeyException {
        addSigner(privateKey, bArr, str, new DefaultSignedAttributeTableGenerator(attributeTable), new SimpleAttributeTableGenerator(attributeTable2), provider);
    }

    public void addSigner(PrivateKey privateKey, byte[] bArr, String str, CMSAttributeTableGenerator cMSAttributeTableGenerator, CMSAttributeTableGenerator cMSAttributeTableGenerator2, String str2) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        addSigner(privateKey, bArr, str, cMSAttributeTableGenerator, cMSAttributeTableGenerator2, CMSUtils.getProvider(str2));
    }

    public void addSigner(PrivateKey privateKey, byte[] bArr, String str, CMSAttributeTableGenerator cMSAttributeTableGenerator, CMSAttributeTableGenerator cMSAttributeTableGenerator2, Provider provider) throws NoSuchAlgorithmException, InvalidKeyException {
        addSigner(privateKey, bArr, getEncOID(privateKey, str), str, cMSAttributeTableGenerator, cMSAttributeTableGenerator2, provider);
    }

    /* access modifiers changed from: 0000 */
    public void generate(OutputStream outputStream, String str, boolean z, OutputStream outputStream2, CMSProcessable cMSProcessable) throws CMSException, IOException {
        OutputStream open = open(outputStream, str, z, outputStream2);
        if (cMSProcessable != null) {
            cMSProcessable.write(open);
        }
        open.close();
    }

    public OutputStream open(OutputStream outputStream) throws IOException {
        return open(outputStream, false);
    }

    public OutputStream open(OutputStream outputStream, String str, boolean z) throws IOException {
        return open(outputStream, str, z, null);
    }

    public OutputStream open(OutputStream outputStream, String str, boolean z, OutputStream outputStream2) throws IOException {
        return open(new ASN1ObjectIdentifier(str), outputStream, z, outputStream2);
    }

    public OutputStream open(OutputStream outputStream, boolean z) throws IOException {
        return open(CMSObjectIdentifiers.data, outputStream, z);
    }

    public OutputStream open(OutputStream outputStream, boolean z, OutputStream outputStream2) throws IOException {
        return open(CMSObjectIdentifiers.data, outputStream, z, outputStream2);
    }

    public OutputStream open(ASN1ObjectIdentifier aSN1ObjectIdentifier, OutputStream outputStream, boolean z) throws IOException {
        return open(aSN1ObjectIdentifier, outputStream, z, null);
    }

    public OutputStream open(ASN1ObjectIdentifier aSN1ObjectIdentifier, OutputStream outputStream, boolean z, OutputStream outputStream2) throws IOException {
        BERSequenceGenerator bERSequenceGenerator = new BERSequenceGenerator(outputStream);
        bERSequenceGenerator.addObject(CMSObjectIdentifiers.signedData);
        BERSequenceGenerator bERSequenceGenerator2 = new BERSequenceGenerator(bERSequenceGenerator.getRawOutputStream(), 0, true);
        bERSequenceGenerator2.addObject(calculateVersion(aSN1ObjectIdentifier));
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        for (SignerInformation digestAlgorithmID : this._signers) {
            aSN1EncodableVector.add(CMSSignedHelper.INSTANCE.fixAlgID(digestAlgorithmID.getDigestAlgorithmID()));
        }
        for (SignerInfoGenerator digestAlgorithm : this.signerGens) {
            aSN1EncodableVector.add(digestAlgorithm.getDigestAlgorithm());
        }
        bERSequenceGenerator2.getRawOutputStream().write(new DERSet(aSN1EncodableVector).getEncoded());
        BERSequenceGenerator bERSequenceGenerator3 = new BERSequenceGenerator(bERSequenceGenerator2.getRawOutputStream());
        bERSequenceGenerator3.addObject(aSN1ObjectIdentifier);
        return new CmsSignedDataOutputStream(CMSUtils.attachSignersToOutputStream(this.signerGens, CMSUtils.getSafeTeeOutputStream(outputStream2, z ? CMSUtils.createBEROctetOutputStream(bERSequenceGenerator3.getRawOutputStream(), 0, true, this._bufferSize) : null)), aSN1ObjectIdentifier, bERSequenceGenerator, bERSequenceGenerator2, bERSequenceGenerator3);
    }

    public void setBufferSize(int i) {
        this._bufferSize = i;
    }
}
