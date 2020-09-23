package net.java.sip.communicator.impl.protocol.sip.xcap;

public class XCapHttpResponse {
    private byte[] content;
    private String contentType;
    private String eTag;
    private int httpCode;

    public int getHttpCode() {
        return this.httpCode;
    }

    /* access modifiers changed from: 0000 */
    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public String getContentType() {
        return this.contentType;
    }

    /* access modifiers changed from: 0000 */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getContent() {
        return this.content;
    }

    /* access modifiers changed from: 0000 */
    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getETag() {
        return this.eTag;
    }

    /* access modifiers changed from: 0000 */
    public void setETag(String eTag) {
        this.eTag = eTag;
    }
}
