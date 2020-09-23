package net.java.sip.communicator.impl.protocol.sip.xcap;

public class XCapResourceId {
    private static String DELIMETER = "/~~";
    private String document;
    private String node;

    public XCapResourceId(String document) {
        this(document, null);
    }

    public XCapResourceId(String document, String node) {
        if (document == null || document.length() == 0) {
            throw new IllegalArgumentException("XCAP resource document cannot be null or empty");
        }
        this.document = document;
        this.node = node;
    }

    public String getDocument() {
        return this.document;
    }

    public String getNode() {
        return this.node;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(this.document);
        if (!(this.node == null || this.node.length() == 0)) {
            builder.append(DELIMETER).append(this.node);
        }
        return builder.toString();
    }

    public static XCapResourceId create(String resourceId) {
        if (resourceId == null || resourceId.trim().length() == 0) {
            throw new IllegalArgumentException("Resource identifier cannot be null or empty");
        }
        int index = resourceId.indexOf(DELIMETER);
        if (index != -1) {
            return new XCapResourceId(resourceId.substring(0, index), resourceId.substring(DELIMETER.length() + index));
        }
        throw new IllegalArgumentException("Resource identifier has invalid format");
    }
}
