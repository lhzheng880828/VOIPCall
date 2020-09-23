package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import java.util.ArrayList;
import java.util.List;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import org.jivesoftware.smack.packet.PacketExtension;

public class RtpDescriptionPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "description";
    public static final String MEDIA_ATTR_NAME = "media";
    public static final String NAMESPACE = "urn:xmpp:jingle:apps:rtp:1";
    public static final String SSRC_ATTR_NAME = "ssrc";
    private BandwidthPacketExtension bandwidth;
    private List<PacketExtension> children;
    private EncryptionPacketExtension encryption;
    private List<RTPHdrExtPacketExtension> extmapList = new ArrayList();
    private final List<PayloadTypePacketExtension> payloadTypes = new ArrayList();

    public RtpDescriptionPacketExtension() {
        super("urn:xmpp:jingle:apps:rtp:1", "description");
    }

    public RtpDescriptionPacketExtension(String namespace) {
        super(namespace, "description");
    }

    public void setMedia(String media) {
        super.setAttribute("media", media);
    }

    public String getMedia() {
        return getAttributeAsString("media");
    }

    public void setSsrc(String ssrc) {
        super.setAttribute("ssrc", ssrc);
    }

    public String getSsrc() {
        return getAttributeAsString("ssrc");
    }

    public void addPayloadType(PayloadTypePacketExtension payloadType) {
        this.payloadTypes.add(payloadType);
    }

    public List<PayloadTypePacketExtension> getPayloadTypes() {
        return this.payloadTypes;
    }

    public List<? extends PacketExtension> getChildExtensions() {
        if (this.children == null) {
            this.children = new ArrayList();
        } else {
            this.children.clear();
        }
        this.children.addAll(this.payloadTypes);
        if (this.encryption != null) {
            this.children.add(this.encryption);
        }
        if (this.bandwidth != null) {
            this.children.add(this.bandwidth);
        }
        if (this.extmapList != null) {
            this.children.addAll(this.extmapList);
        }
        this.children.addAll(super.getChildExtensions());
        return this.children;
    }

    public void addChildExtension(PacketExtension childExtension) {
        if (childExtension instanceof PayloadTypePacketExtension) {
            addPayloadType((PayloadTypePacketExtension) childExtension);
        } else if (childExtension instanceof EncryptionPacketExtension) {
            setEncryption((EncryptionPacketExtension) childExtension);
        } else if (childExtension instanceof BandwidthPacketExtension) {
            setBandwidth((BandwidthPacketExtension) childExtension);
        } else if (childExtension instanceof RTPHdrExtPacketExtension) {
            addExtmap((RTPHdrExtPacketExtension) childExtension);
        } else {
            super.addChildExtension(childExtension);
        }
    }

    public void setEncryption(EncryptionPacketExtension encryption) {
        this.encryption = encryption;
    }

    public EncryptionPacketExtension getEncryption() {
        return this.encryption;
    }

    public void setBandwidth(BandwidthPacketExtension bandwidth) {
        this.bandwidth = bandwidth;
    }

    public BandwidthPacketExtension getBandwidth() {
        return this.bandwidth;
    }

    public void addExtmap(RTPHdrExtPacketExtension extmap) {
        this.extmapList.add(extmap);
    }

    public List<RTPHdrExtPacketExtension> getExtmapList() {
        return this.extmapList;
    }
}
