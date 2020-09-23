package org.jitsi.bouncycastle.cms;

import java.util.Hashtable;
import java.util.Map;
import org.jitsi.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.jitsi.bouncycastle.asn1.DEROctetString;
import org.jitsi.bouncycastle.asn1.DERSet;
import org.jitsi.bouncycastle.asn1.cms.Attribute;
import org.jitsi.bouncycastle.asn1.cms.AttributeTable;
import org.jitsi.bouncycastle.asn1.cms.CMSAttributes;

public class DefaultAuthenticatedAttributeTableGenerator implements CMSAttributeTableGenerator {
    private final Hashtable table;

    public DefaultAuthenticatedAttributeTableGenerator() {
        this.table = new Hashtable();
    }

    public DefaultAuthenticatedAttributeTableGenerator(AttributeTable attributeTable) {
        if (attributeTable != null) {
            this.table = attributeTable.toHashtable();
        } else {
            this.table = new Hashtable();
        }
    }

    /* access modifiers changed from: protected */
    public Hashtable createStandardAttributeTable(Map map) {
        Attribute attribute;
        Hashtable hashtable = (Hashtable) this.table.clone();
        if (!hashtable.containsKey(CMSAttributes.contentType)) {
            attribute = new Attribute(CMSAttributes.contentType, new DERSet(ASN1ObjectIdentifier.getInstance(map.get(CMSAttributeTableGenerator.CONTENT_TYPE))));
            hashtable.put(attribute.getAttrType(), attribute);
        }
        if (!hashtable.containsKey(CMSAttributes.messageDigest)) {
            attribute = new Attribute(CMSAttributes.messageDigest, new DERSet(new DEROctetString((byte[]) map.get(CMSAttributeTableGenerator.DIGEST))));
            hashtable.put(attribute.getAttrType(), attribute);
        }
        return hashtable;
    }

    public AttributeTable getAttributes(Map map) {
        return new AttributeTable(createStandardAttributeTable(map));
    }
}
