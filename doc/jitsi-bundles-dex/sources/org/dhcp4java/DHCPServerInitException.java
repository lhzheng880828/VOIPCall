package org.dhcp4java;

public class DHCPServerInitException extends Exception {
    private static final long serialVersionUID = 1;

    public DHCPServerInitException(String message) {
        super(message);
    }

    public DHCPServerInitException(Throwable cause) {
        super(cause);
    }

    public DHCPServerInitException(String message, Throwable cause) {
        super(message, cause);
    }
}
