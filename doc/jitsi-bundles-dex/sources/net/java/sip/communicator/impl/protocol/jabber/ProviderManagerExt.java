package net.java.sip.communicator.impl.protocol.jabber;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingleinfo.JingleInfoQueryIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.thumbnail.FileElement;
import net.java.sip.communicator.util.Logger;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;
import org.jivesoftware.smackx.bytestreams.ibb.provider.CloseIQProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.DataPacketProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.OpenIQProvider;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5BytestreamManager;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.packet.ChatStateExtension.Provider;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.MessageEvent;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.Version;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInfoProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;

public class ProviderManagerExt extends ProviderManager {
    private static final Logger logger = Logger.getLogger(ProviderManagerExt.class);

    ProviderManagerExt() {
        load();
    }

    public void load() {
        addExtProvider("x", "jabber:x:roster", RosterExchangeProvider.class);
        addExtProvider("x", "jabber:x:event", MessageEventProvider.class);
        addExtProvider("active", "http://jabber.org/protocol/chatstates", Provider.class);
        addExtProvider(MessageEvent.COMPOSING, "http://jabber.org/protocol/chatstates", Provider.class);
        addExtProvider("paused", "http://jabber.org/protocol/chatstates", Provider.class);
        addExtProvider("inactive", "http://jabber.org/protocol/chatstates", Provider.class);
        addExtProvider("gone", "http://jabber.org/protocol/chatstates", Provider.class);
        addExtProvider("html", "http://jabber.org/protocol/xhtml-im", XHTMLExtensionProvider.class);
        addExtProvider("x", GroupChatInvitation.NAMESPACE, GroupChatInvitation.Provider.class);
        addProvider(JingleInfoQueryIQ.ELEMENT_NAME, "http://jabber.org/protocol/disco#items", DiscoverItemsProvider.class);
        addProvider(JingleInfoQueryIQ.ELEMENT_NAME, "http://jabber.org/protocol/disco#info", DiscoverInfoProvider.class);
        addExtProvider("x", "jabber:x:data", DataFormProvider.class);
        addExtProvider("x", "http://jabber.org/protocol/muc#user", MUCUserProvider.class);
        addProvider(JingleInfoQueryIQ.ELEMENT_NAME, "http://jabber.org/protocol/muc#admin", MUCAdminProvider.class);
        addProvider(JingleInfoQueryIQ.ELEMENT_NAME, "http://jabber.org/protocol/muc#owner", MUCOwnerProvider.class);
        addExtProvider("x", "jabber:x:delay", DelayInformationProvider.class);
        addExtProvider("delay", "urn:xmpp:delay", DelayInfoProvider.class);
        addProvider(JingleInfoQueryIQ.ELEMENT_NAME, "jabber:iq:version", Version.class);
        addProvider("vCard", "vcard-temp", VCardProvider.class);
        addProvider(MessageEvent.OFFLINE, "http://jabber.org/protocol/offline", OfflineMessageRequest.Provider.class);
        addExtProvider(MessageEvent.OFFLINE, "http://jabber.org/protocol/offline", OfflineMessageInfo.Provider.class);
        addProvider(JingleInfoQueryIQ.ELEMENT_NAME, "jabber:iq:last", LastActivity.class);
        addProvider(FileElement.ELEMENT_NAME, FileElement.NAMESPACE, StreamInitiationProvider.class);
        addProvider(JingleInfoQueryIQ.ELEMENT_NAME, Socks5BytestreamManager.NAMESPACE, BytestreamsProvider.class);
        addProvider("open", InBandBytestreamManager.NAMESPACE, OpenIQProvider.class);
        addProvider("data", InBandBytestreamManager.NAMESPACE, DataPacketProvider.class);
        addProvider("close", InBandBytestreamManager.NAMESPACE, CloseIQProvider.class);
        addExtProvider("data", InBandBytestreamManager.NAMESPACE, DataPacketProvider.class);
    }

    private void addProvider(String elementName, String namespace, Class<?> provider) {
        try {
            if (IQProvider.class.isAssignableFrom(provider)) {
                addIQProvider(elementName, namespace, provider.newInstance());
            } else if (IQ.class.isAssignableFrom(provider)) {
                addIQProvider(elementName, namespace, provider);
            }
        } catch (Throwable t) {
            logger.error("Error adding iq provider.", t);
        }
    }

    public void addExtProvider(String elementName, String namespace, Class<?> provider) {
        try {
            if (PacketExtensionProvider.class.isAssignableFrom(provider)) {
                addExtensionProvider(elementName, namespace, provider.newInstance());
            } else if (PacketExtension.class.isAssignableFrom(provider)) {
                addExtensionProvider(elementName, namespace, provider);
            }
        } catch (Throwable t) {
            logger.error("Error adding extension provider.", t);
        }
    }
}
