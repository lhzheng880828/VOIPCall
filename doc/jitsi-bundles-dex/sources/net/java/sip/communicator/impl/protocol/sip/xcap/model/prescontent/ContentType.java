package net.java.sip.communicator.impl.protocol.sip.xcap.model.prescontent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

public class ContentType {
    private List<Element> any;
    private Map<QName, String> anyAttributes = new HashMap();
    private DataType data;
    private List<DescriptionType> description;
    private EncodingType encoding;
    private MimeType mimeType;

    public MimeType getMimeType() {
        return this.mimeType;
    }

    public void setMimeType(MimeType mimeType) {
        this.mimeType = mimeType;
    }

    public EncodingType getEncoding() {
        return this.encoding;
    }

    public void setEncoding(EncodingType encoding) {
        this.encoding = encoding;
    }

    public List<DescriptionType> getDescription() {
        if (this.description == null) {
            this.description = new ArrayList();
        }
        return this.description;
    }

    public DataType getData() {
        return this.data;
    }

    public void setData(DataType data) {
        this.data = data;
    }

    public List<Element> getAny() {
        if (this.any == null) {
            this.any = new ArrayList();
        }
        return this.any;
    }

    public void setAny(List<Element> any) {
        this.any = any;
    }

    public Map<QName, String> getAnyAttributes() {
        return this.anyAttributes;
    }

    public void setAnyAttributes(Map<QName, String> anyAttributes) {
        this.anyAttributes = anyAttributes;
    }
}
