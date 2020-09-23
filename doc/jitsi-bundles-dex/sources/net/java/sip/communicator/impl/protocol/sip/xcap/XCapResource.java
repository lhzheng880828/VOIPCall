package net.java.sip.communicator.impl.protocol.sip.xcap;

public class XCapResource {
    private String content;
    private String contentType;
    private XCapResourceId id;

    public XCapResource(XCapResourceId id, String content, String contentType) {
        this.id = id;
        this.content = content;
        this.contentType = contentType;
    }

    public XCapResourceId getId() {
        return this.id;
    }

    public String getContent() {
        return this.content;
    }

    public String getContentType() {
        return this.contentType;
    }
}
