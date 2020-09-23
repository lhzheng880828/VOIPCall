package org.jitsi.service.neomedia;

import java.net.URI;

public class RTPExtension {
    public static final String CSRC_AUDIO_LEVEL_URN = "urn:ietf:params:rtp-hdrext:csrc-audio-level";
    public static final String SSRC_AUDIO_LEVEL_URN = "urn:ietf:params:rtp-hdrext:ssrc-audio-level";
    private MediaDirection direction;
    private String extensionAttributes;
    private final URI extensionURI;

    public RTPExtension(URI extensionURI) {
        this(extensionURI, MediaDirection.SENDRECV);
    }

    public RTPExtension(URI extensionURI, MediaDirection direction) {
        this(extensionURI, direction, null);
    }

    public RTPExtension(URI extensionURI, String extensionAttributes) {
        this(extensionURI, MediaDirection.SENDRECV, extensionAttributes);
    }

    public RTPExtension(URI extensionURI, MediaDirection direction, String extensionAttributes) {
        this.direction = MediaDirection.SENDRECV;
        this.extensionAttributes = null;
        this.extensionURI = extensionURI;
        this.direction = direction;
        this.extensionAttributes = extensionAttributes;
    }

    public MediaDirection getDirection() {
        return this.direction;
    }

    public URI getURI() {
        return this.extensionURI;
    }

    public String getExtensionAttributes() {
        return this.extensionAttributes;
    }

    public String toString() {
        return this.extensionURI.toString() + ";" + getDirection();
    }

    public boolean equals(Object o) {
        return (o instanceof RTPExtension) && ((RTPExtension) o).getURI().equals(getURI());
    }

    public int hashCode() {
        return getURI().hashCode();
    }
}
