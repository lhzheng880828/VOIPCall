package org.jivesoftware.smack.proxy;

import java.io.IOException;
import org.jivesoftware.smack.proxy.ProxyInfo.ProxyType;

public class ProxyException extends IOException {
    public ProxyException(ProxyType type, String ex, Throwable cause) {
        super("Proxy Exception " + type.toString() + " : " + ex + ", " + cause);
    }

    public ProxyException(ProxyType type, String ex) {
        super("Proxy Exception " + type.toString() + " : " + ex);
    }

    public ProxyException(ProxyType type) {
        super("Proxy Exception " + type.toString() + " : " + "Unknown Error");
    }
}
