package org.jitsi.bouncycastle.pkcs;

import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.ASN1OctetString;
import org.jitsi.bouncycastle.asn1.ASN1Set;
import org.jitsi.bouncycastle.asn1.pkcs.Attribute;
import org.jitsi.bouncycastle.asn1.pkcs.CRLBag;
import org.jitsi.bouncycastle.asn1.pkcs.CertBag;
import org.jitsi.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import org.jitsi.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.jitsi.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.jitsi.bouncycastle.asn1.pkcs.SafeBag;
import org.jitsi.bouncycastle.asn1.x509.Certificate;
import org.jitsi.bouncycastle.asn1.x509.CertificateList;
import org.jitsi.bouncycastle.cert.X509CRLHolder;
import org.jitsi.bouncycastle.cert.X509CertificateHolder;

public class PKCS12SafeBag {
    public static final ASN1ObjectIdentifier friendlyNameAttribute = PKCSObjectIdentifiers.pkcs_9_at_friendlyName;
    public static final ASN1ObjectIdentifier localKeyIdAttribute = PKCSObjectIdentifiers.pkcs_9_at_localKeyId;
    private SafeBag safeBag;

    public PKCS12SafeBag(SafeBag safeBag) {
        this.safeBag = safeBag;
    }

    public Attribute[] getAttributes() {
        ASN1Set bagAttributes = this.safeBag.getBagAttributes();
        if (bagAttributes == null) {
            return null;
        }
        Attribute[] attributeArr = new Attribute[bagAttributes.size()];
        for (int i = 0; i != bagAttributes.size(); i++) {
            attributeArr[i] = Attribute.getInstance(bagAttributes.getObjectAt(i));
        }
        return attributeArr;
    }

    public Object getBagValue() {
        return getType().equals(PKCSObjectIdentifiers.pkcs8ShroudedKeyBag) ? new PKCS8EncryptedPrivateKeyInfo(EncryptedPrivateKeyInfo.getInstance(this.safeBag.getBagValue())) : getType().equals(PKCSObjectIdentifiers.certBag) ? new X509CertificateHolder(Certificate.getInstance(ASN1OctetString.getInstance(CertBag.getInstance(this.safeBag.getBagValue()).getCertValue()).getOctets())) : getType().equals(PKCSObjectIdentifiers.keyBag) ? PrivateKeyInfo.getInstance(this.safeBag.getBagValue()) : getType().equals(PKCSObjectIdentifiers.crlBag) ? new X509CRLHolder(CertificateList.getInstance(ASN1OctetString.getInstance(CRLBag.getInstance(this.safeBag.getBagValue()).getCRLValue()).getOctets())) : this.safeBag.getBagValue();
    }

    public ASN1ObjectIdentifier getType() {
        return this.safeBag.getBagId();
    }

    public SafeBag toASN1Structure() {
        return this.safeBag;
    }
}
