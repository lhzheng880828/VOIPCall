package org.jitsi.util.xml;

public class XMLException extends Exception {
    private static final long serialVersionUID = 0;

    public XMLException(String message, Throwable cause) {
        super(message, cause);
    }

    public XMLException(String message) {
        super(message);
    }
}
