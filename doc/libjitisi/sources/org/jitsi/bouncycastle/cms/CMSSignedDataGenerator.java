package org.jitsi.bouncycastle.cms;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.cms.AttributeTable;
import org.jitsi.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.jitsi.bouncycastle.operator.ContentSigner;
import org.jitsi.bouncycastle.operator.OperatorCreationException;
import org.jitsi.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.jitsi.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class CMSSignedDataGenerator extends CMSSignedGenerator {
    private List signerInfs = new ArrayList();

    private class SignerInf {
        final AttributeTable baseSignedTable;
        final String digestOID;
        final String encOID;
        final PrivateKey key;
        final CMSAttributeTableGenerator sAttr;
        final Object signerIdentifier;
        final CMSAttributeTableGenerator unsAttr;

        SignerInf(PrivateKey privateKey, Object obj, String str, String str2, CMSAttributeTableGenerator cMSAttributeTableGenerator, CMSAttributeTableGenerator cMSAttributeTableGenerator2, AttributeTable attributeTable) {
            this.key = privateKey;
            this.signerIdentifier = obj;
            this.digestOID = str;
            this.encOID = str2;
            this.sAttr = cMSAttributeTableGenerator;
            this.unsAttr = cMSAttributeTableGenerator2;
            this.baseSignedTable = attributeTable;
        }

        /* access modifiers changed from: 0000 */
        public SignerInfoGenerator toSignerInfoGenerator(SecureRandom secureRandom, Provider provider, boolean z) throws IOException, CertificateEncodingException, CMSException, OperatorCreationException, NoSuchAlgorithmException {
            String str = CMSSignedHelper.INSTANCE.getDigestAlgName(this.digestOID) + "with" + CMSSignedHelper.INSTANCE.getEncryptionAlgName(this.encOID);
            JcaSignerInfoGeneratorBuilder jcaSignerInfoGeneratorBuilder = new JcaSignerInfoGeneratorBuilder(new BcDigestCalculatorProvider());
            if (z) {
                jcaSignerInfoGeneratorBuilder.setSignedAttributeGenerator(this.sAttr);
            }
            jcaSignerInfoGeneratorBuilder.setDirectSignature(!z);
            jcaSignerInfoGeneratorBuilder.setUnsignedAttributeGenerator(this.unsAttr);
            try {
                JcaContentSignerBuilder secureRandom2 = new JcaContentSignerBuilder(str).setSecureRandom(secureRandom);
                if (provider != null) {
                    secureRandom2.setProvider(provider);
                }
                ContentSigner build = secureRandom2.build(this.key);
                return this.signerIdentifier instanceof X509Certificate ? jcaSignerInfoGeneratorBuilder.build(build, (X509Certificate) this.signerIdentifier) : jcaSignerInfoGeneratorBuilder.build(build, (byte[]) this.signerIdentifier);
            } catch (IllegalArgumentException e) {
                throw new NoSuchAlgorithmException(e.getMessage());
            }
        }
    }

    public CMSSignedDataGenerator(SecureRandom secureRandom) {
        super(secureRandom);
    }

    private void doAddSigner(PrivateKey privateKey, Object obj, String str, String str2, CMSAttributeTableGenerator cMSAttributeTableGenerator, CMSAttributeTableGenerator cMSAttributeTableGenerator2, AttributeTable attributeTable) throws IllegalArgumentException {
        this.signerInfs.add(new SignerInf(privateKey, obj, str2, str, cMSAttributeTableGenerator, cMSAttributeTableGenerator2, attributeTable));
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str) throws IllegalArgumentException {
        addSigner(privateKey, x509Certificate, getEncOID(privateKey, str), str);
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str, String str2) throws IllegalArgumentException {
        doAddSigner(privateKey, x509Certificate, str, str2, new DefaultSignedAttributeTableGenerator(), null, null);
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str, String str2, AttributeTable attributeTable, AttributeTable attributeTable2) throws IllegalArgumentException {
        doAddSigner(privateKey, x509Certificate, str, str2, new DefaultSignedAttributeTableGenerator(attributeTable), new SimpleAttributeTableGenerator(attributeTable2), attributeTable);
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str, String str2, CMSAttributeTableGenerator cMSAttributeTableGenerator, CMSAttributeTableGenerator cMSAttributeTableGenerator2) throws IllegalArgumentException {
        doAddSigner(privateKey, x509Certificate, str, str2, cMSAttributeTableGenerator, cMSAttributeTableGenerator2, null);
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str, AttributeTable attributeTable, AttributeTable attributeTable2) throws IllegalArgumentException {
        addSigner(privateKey, x509Certificate, getEncOID(privateKey, str), str, attributeTable, attributeTable2);
    }

    public void addSigner(PrivateKey privateKey, X509Certificate x509Certificate, String str, CMSAttributeTableGenerator cMSAttributeTableGenerator, CMSAttributeTableGenerator cMSAttributeTableGenerator2) throws IllegalArgumentException {
        addSigner(privateKey, x509Certificate, getEncOID(privateKey, str), str, cMSAttributeTableGenerator, cMSAttributeTableGenerator2);
    }

    public void addSigner(PrivateKey privateKey, byte[] bArr, String str) throws IllegalArgumentException {
        addSigner(privateKey, bArr, getEncOID(privateKey, str), str);
    }

    public void addSigner(PrivateKey privateKey, byte[] bArr, String str, String str2) throws IllegalArgumentException {
        doAddSigner(privateKey, bArr, str, str2, new DefaultSignedAttributeTableGenerator(), null, null);
    }

    public void addSigner(PrivateKey privateKey, byte[] bArr, String str, String str2, AttributeTable attributeTable, AttributeTable attributeTable2) throws IllegalArgumentException {
        doAddSigner(privateKey, bArr, str, str2, new DefaultSignedAttributeTableGenerator(attributeTable), new SimpleAttributeTableGenerator(attributeTable2), attributeTable);
    }

    public void addSigner(PrivateKey privateKey, byte[] bArr, String str, String str2, CMSAttributeTableGenerator cMSAttributeTableGenerator, CMSAttributeTableGenerator cMSAttributeTableGenerator2) throws IllegalArgumentException {
        doAddSigner(privateKey, bArr, str, str2, cMSAttributeTableGenerator, cMSAttributeTableGenerator2, null);
    }

    public void addSigner(PrivateKey privateKey, byte[] bArr, String str, AttributeTable attributeTable, AttributeTable attributeTable2) throws IllegalArgumentException {
        addSigner(privateKey, bArr, getEncOID(privateKey, str), str, attributeTable, attributeTable2);
    }

    public void addSigner(PrivateKey privateKey, byte[] bArr, String str, CMSAttributeTableGenerator cMSAttributeTableGenerator, CMSAttributeTableGenerator cMSAttributeTableGenerator2) throws IllegalArgumentException {
        addSigner(privateKey, bArr, getEncOID(privateKey, str), str, cMSAttributeTableGenerator, cMSAttributeTableGenerator2);
    }

    public CMSSignedData generate(String str, CMSProcessable cMSProcessable, boolean z, String str2) throws NoSuchAlgorithmException, NoSuchProviderException, CMSException {
        return generate(str, cMSProcessable, z, CMSUtils.getProvider(str2), true);
    }

    public CMSSignedData generate(String str, CMSProcessable cMSProcessable, boolean z, String str2, boolean z2) throws NoSuchAlgorithmException, NoSuchProviderException, CMSException {
        return generate(str, cMSProcessable, z, CMSUtils.getProvider(str2), z2);
    }

    public CMSSignedData generate(String str, CMSProcessable cMSProcessable, boolean z, Provider provider) throws NoSuchAlgorithmException, CMSException {
        return generate(str, cMSProcessable, z, provider, true);
    }

    public CMSSignedData generate(String str, final CMSProcessable cMSProcessable, boolean z, Provider provider, boolean z2) throws NoSuchAlgorithmException, CMSException {
        final ASN1ObjectIdentifier aSN1ObjectIdentifier = (str == null ? 1 : null) != null ? null : new ASN1ObjectIdentifier(str);
        for (SignerInf toSignerInfoGenerator : this.signerInfs) {
            try {
                this.signerGens.add(toSignerInfoGenerator.toSignerInfoGenerator(this.rand, provider, z2));
            } catch (OperatorCreationException e) {
                throw new CMSException("exception creating signerInf", e);
            } catch (IOException e2) {
                throw new CMSException("exception encoding attributes", e2);
            } catch (CertificateEncodingException e3) {
                throw new CMSException("error creating sid.", e3);
            }
        }
        this.signerInfs.clear();
        return cMSProcessable != null ? generate(new CMSTypedData() {
            public Object getContent() {
                return cMSProcessable.getContent();
            }

            public ASN1ObjectIdentifier getContentType() {
                return aSN1ObjectIdentifier;
            }

            public void write(OutputStream outputStream) throws IOException, CMSException {
                cMSProcessable.write(outputStream);
            }
        }, z) : generate(new CMSAbsentContent(aSN1ObjectIdentifier), z);
    }

    public CMSSignedData generate(CMSProcessable cMSProcessable, String str) throws NoSuchAlgorithmException, NoSuchProviderException, CMSException {
        return generate(cMSProcessable, CMSUtils.getProvider(str));
    }

    public CMSSignedData generate(CMSProcessable cMSProcessable, Provider provider) throws NoSuchAlgorithmException, CMSException {
        return generate(cMSProcessable, false, provider);
    }

    public CMSSignedData generate(CMSProcessable cMSProcessable, boolean z, String str) throws NoSuchAlgorithmException, NoSuchProviderException, CMSException {
        return cMSProcessable instanceof CMSTypedData ? generate(((CMSTypedData) cMSProcessable).getContentType().getId(), cMSProcessable, z, str) : generate(DATA, cMSProcessable, z, str);
    }

    public CMSSignedData generate(CMSProcessable cMSProcessable, boolean z, Provider provider) throws NoSuchAlgorithmException, CMSException {
        return cMSProcessable instanceof CMSTypedData ? generate(((CMSTypedData) cMSProcessable).getContentType().getId(), cMSProcessable, z, provider) : generate(DATA, cMSProcessable, z, provider);
    }

    public CMSSignedData generate(CMSTypedData cMSTypedData) throws CMSException {
        return generate(cMSTypedData, false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x007c  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0105  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00ce  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0103  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00dc  */
    public org.jitsi.bouncycastle.cms.CMSSignedData generate(org.jitsi.bouncycastle.cms.CMSTypedData r10, boolean r11) throws org.jitsi.bouncycastle.cms.CMSException {
        /*
        r9 = this;
        r1 = 0;
        r0 = r9.signerInfs;
        r0 = r0.isEmpty();
        if (r0 != 0) goto L_0x0011;
    L_0x0009:
        r0 = new java.lang.IllegalStateException;
        r1 = "this method can only be used with SignerInfoGenerator";
        r0.<init>(r1);
        throw r0;
    L_0x0011:
        r6 = new org.jitsi.bouncycastle.asn1.ASN1EncodableVector;
        r6.<init>();
        r7 = new org.jitsi.bouncycastle.asn1.ASN1EncodableVector;
        r7.<init>();
        r0 = r9.digests;
        r0.clear();
        r0 = r9._signers;
        r2 = r0.iterator();
    L_0x0026:
        r0 = r2.hasNext();
        if (r0 == 0) goto L_0x0047;
    L_0x002c:
        r0 = r2.next();
        r0 = (org.jitsi.bouncycastle.cms.SignerInformation) r0;
        r3 = org.jitsi.bouncycastle.cms.CMSSignedHelper.INSTANCE;
        r4 = r0.getDigestAlgorithmID();
        r3 = r3.fixAlgID(r4);
        r6.add(r3);
        r0 = r0.toASN1Structure();
        r7.add(r0);
        goto L_0x0026;
    L_0x0047:
        r8 = r10.getContentType();
        if (r10 == 0) goto L_0x0107;
    L_0x004d:
        if (r11 == 0) goto L_0x010a;
    L_0x004f:
        r0 = new java.io.ByteArrayOutputStream;
        r0.<init>();
    L_0x0054:
        r2 = r9.signerGens;
        r2 = org.jitsi.bouncycastle.cms.CMSUtils.attachSignersToOutputStream(r2, r0);
        r2 = org.jitsi.bouncycastle.cms.CMSUtils.getSafeOutputStream(r2);
        r10.write(r2);	 Catch:{ IOException -> 0x00a8 }
        r2.close();	 Catch:{ IOException -> 0x00a8 }
        if (r11 == 0) goto L_0x0107;
    L_0x0066:
        r2 = new org.jitsi.bouncycastle.asn1.BEROctetString;
        r0 = r0.toByteArray();
        r2.<init>(r0);
        r5 = r2;
    L_0x0070:
        r0 = r9.signerGens;
        r2 = r0.iterator();
    L_0x0076:
        r0 = r2.hasNext();
        if (r0 == 0) goto L_0x00c6;
    L_0x007c:
        r0 = r2.next();
        r0 = (org.jitsi.bouncycastle.cms.SignerInfoGenerator) r0;
        r3 = r0.generate(r8);
        r4 = r3.getDigestAlgorithm();
        r6.add(r4);
        r7.add(r3);
        r0 = r0.getCalculatedDigest();
        if (r0 == 0) goto L_0x0076;
    L_0x0096:
        r4 = r9.digests;
        r3 = r3.getDigestAlgorithm();
        r3 = r3.getAlgorithm();
        r3 = r3.getId();
        r4.put(r3, r0);
        goto L_0x0076;
    L_0x00a8:
        r0 = move-exception;
        r1 = new org.jitsi.bouncycastle.cms.CMSException;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "data processing exception: ";
        r2 = r2.append(r3);
        r3 = r0.getMessage();
        r2 = r2.append(r3);
        r2 = r2.toString();
        r1.m1197init(r2, r0);
        throw r1;
    L_0x00c6:
        r0 = r9.certs;
        r0 = r0.size();
        if (r0 == 0) goto L_0x0105;
    L_0x00ce:
        r0 = r9.certs;
        r3 = org.jitsi.bouncycastle.cms.CMSUtils.createBerSetFromList(r0);
    L_0x00d4:
        r0 = r9.crls;
        r0 = r0.size();
        if (r0 == 0) goto L_0x0103;
    L_0x00dc:
        r0 = r9.crls;
        r4 = org.jitsi.bouncycastle.cms.CMSUtils.createBerSetFromList(r0);
    L_0x00e2:
        r2 = new org.jitsi.bouncycastle.asn1.cms.ContentInfo;
        r2.<init>(r8, r5);
        r0 = new org.jitsi.bouncycastle.asn1.cms.SignedData;
        r1 = new org.jitsi.bouncycastle.asn1.DERSet;
        r1.<init>(r6);
        r5 = new org.jitsi.bouncycastle.asn1.DERSet;
        r5.<init>(r7);
        r0.<init>(r1, r2, r3, r4, r5);
        r1 = new org.jitsi.bouncycastle.asn1.cms.ContentInfo;
        r2 = org.jitsi.bouncycastle.asn1.cms.CMSObjectIdentifiers.signedData;
        r1.<init>(r2, r0);
        r0 = new org.jitsi.bouncycastle.cms.CMSSignedData;
        r0.m1213init(r10, r1);
        return r0;
    L_0x0103:
        r4 = r1;
        goto L_0x00e2;
    L_0x0105:
        r3 = r1;
        goto L_0x00d4;
    L_0x0107:
        r5 = r1;
        goto L_0x0070;
    L_0x010a:
        r0 = r1;
        goto L_0x0054;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.bouncycastle.cms.CMSSignedDataGenerator.generate(org.jitsi.bouncycastle.cms.CMSTypedData, boolean):org.jitsi.bouncycastle.cms.CMSSignedData");
    }

    public SignerInformationStore generateCounterSigners(SignerInformation signerInformation) throws CMSException {
        return generate(new CMSProcessableByteArray(null, signerInformation.getSignature()), false).getSignerInfos();
    }

    public SignerInformationStore generateCounterSigners(SignerInformation signerInformation, String str) throws NoSuchAlgorithmException, NoSuchProviderException, CMSException {
        return generate(null, new CMSProcessableByteArray(signerInformation.getSignature()), false, CMSUtils.getProvider(str)).getSignerInfos();
    }

    public SignerInformationStore generateCounterSigners(SignerInformation signerInformation, Provider provider) throws NoSuchAlgorithmException, CMSException {
        return generate(null, new CMSProcessableByteArray(signerInformation.getSignature()), false, provider).getSignerInfos();
    }
}
