package org.jitsi.impl.neomedia.control;

import javax.media.Control;

public interface DiagnosticsControl extends Control {
    public static final long NEVER = 0;

    long getMalfunctioningSince();

    String toString();
}
