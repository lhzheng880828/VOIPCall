package org.jitsi.impl.neomedia.device;

public class NoneAudioSystem extends AudioSystem {
    public static final String LOCATOR_PROTOCOL = "none";

    public NoneAudioSystem() throws Exception {
        super(LOCATOR_PROTOCOL);
    }

    /* access modifiers changed from: protected */
    public void doInitialize() throws Exception {
    }

    public String toString() {
        return "None";
    }
}
