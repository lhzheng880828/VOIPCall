package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import java.util.ArrayList;
import java.util.List;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import org.jivesoftware.smack.packet.PacketExtension;

public class IceUdpTransportPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "transport";
    public static final String NAMESPACE = "urn:xmpp:jingle:transports:ice-udp:1";
    public static final String PWD_ATTR_NAME = "pwd";
    public static final String UFRAG_ATTR_NAME = "ufrag";
    private final List<CandidatePacketExtension> candidateList = new ArrayList();
    private RemoteCandidatePacketExtension remoteCandidate;

    public IceUdpTransportPacketExtension() {
        super("urn:xmpp:jingle:transports:ice-udp:1", "transport");
    }

    protected IceUdpTransportPacketExtension(String namespace, String elementName) {
        super(namespace, elementName);
    }

    public void setPassword(String pwd) {
        super.setAttribute(PWD_ATTR_NAME, pwd);
    }

    public String getPassword() {
        return super.getAttributeAsString(PWD_ATTR_NAME);
    }

    public void setUfrag(String ufrag) {
        super.setAttribute(UFRAG_ATTR_NAME, ufrag);
    }

    public String getUfrag() {
        return super.getAttributeAsString(UFRAG_ATTR_NAME);
    }

    public List<? extends PacketExtension> getChildExtensions() {
        List<PacketExtension> childExtensions = new ArrayList();
        childExtensions.addAll(super.getChildExtensions());
        synchronized (this.candidateList) {
            if (this.candidateList.size() > 0) {
                childExtensions.addAll(this.candidateList);
            } else if (this.remoteCandidate != null) {
                childExtensions.add(this.remoteCandidate);
            }
        }
        return childExtensions;
    }

    public void addCandidate(CandidatePacketExtension candidate) {
        synchronized (this.candidateList) {
            this.candidateList.add(candidate);
        }
    }

    public boolean removeCandidate(CandidatePacketExtension candidate) {
        boolean remove;
        synchronized (this.candidateList) {
            remove = this.candidateList.remove(candidate);
        }
        return remove;
    }

    public List<CandidatePacketExtension> getCandidateList() {
        ArrayList arrayList;
        synchronized (this.candidateList) {
            arrayList = new ArrayList(this.candidateList);
        }
        return arrayList;
    }

    public void setRemoteCandidate(RemoteCandidatePacketExtension candidate) {
        this.remoteCandidate = candidate;
    }

    public RemoteCandidatePacketExtension getRemoteCandidate() {
        return this.remoteCandidate;
    }

    public void addChildExtension(PacketExtension childExtension) {
        if (childExtension instanceof RemoteCandidatePacketExtension) {
            setRemoteCandidate((RemoteCandidatePacketExtension) childExtension);
        } else if (childExtension instanceof CandidatePacketExtension) {
            addCandidate((CandidatePacketExtension) childExtension);
        } else if (childExtension instanceof DtlsFingerprintPacketExtension) {
            super.addChildExtension(childExtension);
        }
    }
}
