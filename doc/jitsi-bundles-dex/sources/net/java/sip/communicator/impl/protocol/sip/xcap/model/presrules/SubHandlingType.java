package net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules;

public enum SubHandlingType {
    Block("block"),
    Confirm("confirm"),
    PoliteBlock("polite-block"),
    Allow("allow");
    
    private final String value;

    private SubHandlingType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public static SubHandlingType fromString(String value) {
        if (value == null) {
            return null;
        }
        for (SubHandlingType subHandling : values()) {
            if (value.equalsIgnoreCase(subHandling.value())) {
                return subHandling;
            }
        }
        return null;
    }
}
