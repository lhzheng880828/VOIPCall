package org.jivesoftware.smackx.commands;

public class AdHocCommandNote {
    private Type type;
    private String value;

    public enum Type {
        info,
        warn,
        error
    }

    public AdHocCommandNote(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public Type getType() {
        return this.type;
    }
}
