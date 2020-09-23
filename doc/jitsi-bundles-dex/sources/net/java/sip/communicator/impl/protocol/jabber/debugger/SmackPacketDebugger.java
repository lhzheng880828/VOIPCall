package net.java.sip.communicator.impl.protocol.jabber.debugger;

import net.java.sip.communicator.impl.protocol.jabber.JabberActivator;
import org.jitsi.service.packetlogging.PacketLoggingService;
import org.jitsi.service.packetlogging.PacketLoggingService.ProtocolName;
import org.jitsi.service.packetlogging.PacketLoggingService.TransportName;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Body;
import org.jivesoftware.smack.packet.Message.Subject;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;

public class SmackPacketDebugger implements PacketListener, PacketInterceptor {
    private XMPPConnection connection;
    private byte[] localAddress;
    private PacketLoggingService packetLogging;
    private byte[] remoteAddress;

    public SmackPacketDebugger() {
        this.connection = null;
        this.packetLogging = null;
        this.packetLogging = JabberActivator.getPacketLogging();
    }

    public void setConnection(XMPPConnection connection) {
        this.connection = connection;
    }

    public void interceptPacket(Packet packet) {
        try {
            if (this.packetLogging.isLoggingEnabled(ProtocolName.JABBER) && packet != null && this.connection.getSocket() != null) {
                byte[] packetBytes;
                if (this.remoteAddress == null) {
                    this.remoteAddress = this.connection.getSocket().getInetAddress().getAddress();
                    this.localAddress = this.connection.getSocket().getLocalAddress().getAddress();
                }
                if (packet instanceof Message) {
                    packetBytes = cloneAnonyMessage(packet).toXML().getBytes("UTF-8");
                } else {
                    packetBytes = packet.toXML().getBytes("UTF-8");
                }
                this.packetLogging.logPacket(ProtocolName.JABBER, this.localAddress, this.connection.getSocket().getLocalPort(), this.remoteAddress, this.connection.getPort(), TransportName.TCP, true, packetBytes);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private Message cloneAnonyMessage(Packet packet) {
        Message oldMsg = (Message) packet;
        if (oldMsg.getBody() == null && (oldMsg.getBodies() == null || oldMsg.getBodies().size() == 0)) {
            return oldMsg;
        }
        Message newMsg = new Message();
        newMsg.setPacketID(packet.getPacketID());
        newMsg.setTo(packet.getTo());
        newMsg.setFrom(packet.getFrom());
        for (PacketExtension pex : packet.getExtensions()) {
            newMsg.addExtension(pex);
        }
        for (String propName : packet.getPropertyNames()) {
            newMsg.setProperty(propName, packet.getProperty(propName));
        }
        newMsg.setError(packet.getError());
        newMsg.setType(oldMsg.getType());
        newMsg.setThread(oldMsg.getThread());
        newMsg.setLanguage(oldMsg.getLanguage());
        for (Subject sub : oldMsg.getSubjects()) {
            if (sub.getSubject() != null) {
                newMsg.addSubject(sub.getLanguage(), new String(new char[sub.getSubject().length()]).replace(0, '.'));
            } else {
                newMsg.addSubject(sub.getLanguage(), sub.getSubject());
            }
        }
        for (Body b : oldMsg.getBodies()) {
            if (b.getMessage() != null) {
                newMsg.addBody(b.getLanguage(), new String(new char[b.getMessage().length()]).replace(0, '.'));
            } else {
                newMsg.addSubject(b.getLanguage(), b.getMessage());
            }
        }
        return newMsg;
    }

    public void processPacket(Packet packet) {
        try {
            if (this.packetLogging.isLoggingEnabled(ProtocolName.JABBER) && packet != null && this.connection.getSocket() != null) {
                byte[] packetBytes;
                if (packet instanceof Message) {
                    packetBytes = cloneAnonyMessage(packet).toXML().getBytes("UTF-8");
                } else {
                    packetBytes = packet.toXML().getBytes("UTF-8");
                }
                this.packetLogging.logPacket(ProtocolName.JABBER, this.remoteAddress, this.connection.getPort(), this.localAddress, this.connection.getSocket().getLocalPort(), TransportName.TCP, false, packetBytes);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
