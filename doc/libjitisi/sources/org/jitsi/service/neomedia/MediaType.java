package org.jitsi.service.neomedia;

import net.sf.fmj.media.datasink.rtp.ParsedRTPUrlElement;

public enum MediaType {
    AUDIO(ParsedRTPUrlElement.AUDIO),
    VIDEO(ParsedRTPUrlElement.VIDEO);
    
    private final String mediaTypeName;

    private MediaType(String mediaTypeName) {
        this.mediaTypeName = mediaTypeName;
    }

    public String toString() {
        return this.mediaTypeName;
    }

    public static MediaType parseString(String mediaTypeName) throws IllegalArgumentException {
        if (AUDIO.toString().equals(mediaTypeName)) {
            return AUDIO;
        }
        if (VIDEO.toString().equals(mediaTypeName)) {
            return VIDEO;
        }
        throw new IllegalArgumentException(mediaTypeName + " is not a currently supported MediaType");
    }
}
