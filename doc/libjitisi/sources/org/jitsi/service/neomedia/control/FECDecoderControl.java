package org.jitsi.service.neomedia.control;

import javax.media.Control;

public interface FECDecoderControl extends Control {
    int fecPacketsDecoded();
}
