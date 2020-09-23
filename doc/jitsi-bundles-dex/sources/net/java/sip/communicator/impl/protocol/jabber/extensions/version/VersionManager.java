package net.java.sip.communicator.impl.protocol.jabber.extensions.version;

import net.java.sip.communicator.impl.protocol.jabber.JabberActivator;
import net.java.sip.communicator.impl.protocol.jabber.ProtocolProviderServiceJabberImpl;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.packet.Version;

public class VersionManager implements RegistrationStateChangeListener, PacketListener {
    private ProtocolProviderServiceJabberImpl parentProvider = null;

    public VersionManager(ProtocolProviderServiceJabberImpl parentProvider) {
        this.parentProvider = parentProvider;
        this.parentProvider.addRegistrationStateChangeListener(this);
    }

    public void registrationStateChanged(RegistrationStateChangeEvent evt) {
        if (evt.getNewState() == RegistrationState.REGISTERED) {
            this.parentProvider.getConnection().removePacketListener(this);
            this.parentProvider.getConnection().addPacketListener(this, new AndFilter(new IQTypeFilter(Type.GET), new PacketTypeFilter(Version.class)));
        } else if ((evt.getNewState() == RegistrationState.UNREGISTERED || evt.getNewState() == RegistrationState.CONNECTION_FAILED || evt.getNewState() == RegistrationState.AUTHENTICATION_FAILED) && this.parentProvider.getConnection() != null) {
            this.parentProvider.getConnection().removePacketListener(this);
        }
    }

    public void processPacket(Packet packet) {
        Version versionIQ = new Version();
        versionIQ.setType(Type.RESULT);
        versionIQ.setTo(packet.getFrom());
        versionIQ.setFrom(packet.getTo());
        versionIQ.setPacketID(packet.getPacketID());
        org.jitsi.service.version.Version ver = JabberActivator.getVersionService().getCurrentVersion();
        String appName = ver.getApplicationName();
        if (!appName.toLowerCase().contains("jitsi")) {
            appName = appName + "-Jitsi";
        }
        versionIQ.setName(appName);
        versionIQ.setVersion(ver.toString());
        versionIQ.setOs(System.getProperty("os.name"));
        this.parentProvider.getConnection().sendPacket(versionIQ);
    }
}
