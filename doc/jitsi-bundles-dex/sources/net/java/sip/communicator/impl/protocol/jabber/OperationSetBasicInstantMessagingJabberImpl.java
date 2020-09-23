package net.java.sip.communicator.impl.protocol.jabber;

import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.java.sip.communicator.impl.protocol.jabber.extensions.carbon.CarbonPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.carbon.CarbonPacketExtension.PrivateExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.carbon.ForwardedPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.mailnotification.MailThreadInfo;
import net.java.sip.communicator.impl.protocol.jabber.extensions.mailnotification.MailboxIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.mailnotification.MailboxIQProvider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.mailnotification.MailboxQueryIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.mailnotification.NewMailNotificationIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.mailnotification.NewMailNotificationProvider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.messagecorrection.MessageCorrectionExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.messagecorrection.MessageCorrectionExtensionProvider;
import net.java.sip.communicator.impl.protocol.sip.SipStatusEnum;
import net.java.sip.communicator.service.protocol.AbstractOperationSetBasicInstantMessaging;
import net.java.sip.communicator.service.protocol.ChatRoom;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ContactResource;
import net.java.sip.communicator.service.protocol.OperationSetMessageCorrection;
import net.java.sip.communicator.service.protocol.OperationSetMultiUserChat;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.MessageDeliveredEvent;
import net.java.sip.communicator.service.protocol.event.MessageDeliveryFailedEvent;
import net.java.sip.communicator.service.protocol.event.MessageReceivedEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.util.Html2Text;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.MessageEventManager;
import org.jivesoftware.smackx.XHTMLManager;
import org.jivesoftware.smackx.packet.DelayInfo;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.jivesoftware.smackx.packet.MessageEvent;
import org.jivesoftware.smackx.packet.XHTMLExtension;

public class OperationSetBasicInstantMessagingJabberImpl extends AbstractOperationSetBasicInstantMessaging implements OperationSetMessageCorrection {
    private static final String CLOSE_BODY_TAG = "</body>";
    private static final String HTML_NAMESPACE = "http://jabber.org/protocol/xhtml-im";
    private static final long JID_INACTIVITY_TIMEOUT = 600000;
    private static final String OPEN_BODY_TAG = "<body>";
    private static final String PNAME_MAX_GMAIL_THREADS_PER_NOTIFICATION = "net.java.sip.communicator.impl.protocol.jabber.MAX_GMAIL_THREADS_PER_NOTIFICATION";
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(OperationSetBasicInstantMessagingJabberImpl.class);
    /* access modifiers changed from: private */
    public boolean isCarbonEnabled = false;
    /* access modifiers changed from: private|final */
    public final ProtocolProviderServiceJabberImpl jabberProvider;
    private Map<String, TargetAddress> jids = new Hashtable();
    /* access modifiers changed from: private */
    public long lastReceivedMailboxResultTime = -1;
    /* access modifiers changed from: private */
    public OperationSetPersistentPresenceJabberImpl opSetPersPresence = null;
    /* access modifiers changed from: private */
    public List<PacketFilter> packetFilters = new ArrayList();
    /* access modifiers changed from: private */
    public SmackMessageListener smackMessageListener = null;

    private static class GroupMessagePacketFilter implements PacketFilter {
        private GroupMessagePacketFilter() {
        }

        public boolean accept(Packet packet) {
            if ((packet instanceof Message) && !((Message) packet).getType().equals(Type.groupchat)) {
                return true;
            }
            return false;
        }
    }

    private class MailboxIQListener implements PacketListener {
        private MailboxIQListener() {
        }

        public void processPacket(Packet packet) {
            if (packet == null || (packet instanceof MailboxIQ)) {
                MailboxIQ mailboxIQ = (MailboxIQ) packet;
                if (mailboxIQ.getTotalMatched() >= 1) {
                    Contact sourceContact = OperationSetBasicInstantMessagingJabberImpl.this.opSetPersPresence.findContactByID(OperationSetBasicInstantMessagingJabberImpl.this.jabberProvider.getAccountID().getService());
                    if (sourceContact == null) {
                        sourceContact = OperationSetBasicInstantMessagingJabberImpl.this.opSetPersPresence.createVolatileContact(OperationSetBasicInstantMessagingJabberImpl.this.jabberProvider.getAccountID().getService());
                    }
                    OperationSetBasicInstantMessagingJabberImpl.this.lastReceivedMailboxResultTime = mailboxIQ.getResultTime();
                    OperationSetBasicInstantMessagingJabberImpl.this.fireMessageEvent(new MessageReceivedEvent(new MessageJabberImpl(OperationSetBasicInstantMessagingJabberImpl.this.createMailboxDescription(mailboxIQ), "text/html", "UTF-8", null), sourceContact, new Date(), 2));
                }
            }
        }
    }

    private class NewMailNotificationListener implements PacketListener {
        private NewMailNotificationListener() {
        }

        public void processPacket(Packet packet) {
            if ((packet == null || (packet instanceof NewMailNotificationIQ)) && OperationSetBasicInstantMessagingJabberImpl.this.jabberProvider.getAccountID().getAccountPropertyBoolean("GMAIL_NOTIFICATIONS_ENABLED", false) && !OperationSetBasicInstantMessagingJabberImpl.this.opSetPersPresence.getCurrentStatusMessage().equals(SipStatusEnum.OFFLINE)) {
                MailboxQueryIQ mailboxQueryIQ = new MailboxQueryIQ();
                if (OperationSetBasicInstantMessagingJabberImpl.this.lastReceivedMailboxResultTime != -1) {
                    mailboxQueryIQ.setNewerThanTime(OperationSetBasicInstantMessagingJabberImpl.this.lastReceivedMailboxResultTime);
                }
                if (OperationSetBasicInstantMessagingJabberImpl.logger.isTraceEnabled()) {
                    OperationSetBasicInstantMessagingJabberImpl.logger.trace("send mailNotification for acc: " + OperationSetBasicInstantMessagingJabberImpl.this.jabberProvider.getAccountID().getAccountUniqueID());
                }
                OperationSetBasicInstantMessagingJabberImpl.this.jabberProvider.getConnection().sendPacket(mailboxQueryIQ);
            }
        }
    }

    private class RegistrationStateListener implements RegistrationStateChangeListener {
        private RegistrationStateListener() {
        }

        public void registrationStateChanged(RegistrationStateChangeEvent evt) {
            if (OperationSetBasicInstantMessagingJabberImpl.logger.isDebugEnabled()) {
                OperationSetBasicInstantMessagingJabberImpl.logger.debug("The provider changed state from: " + evt.getOldState() + " to: " + evt.getNewState());
            }
            if (evt.getNewState() == RegistrationState.REGISTERING) {
                OperationSetBasicInstantMessagingJabberImpl.this.opSetPersPresence = (OperationSetPersistentPresenceJabberImpl) OperationSetBasicInstantMessagingJabberImpl.this.jabberProvider.getOperationSet(OperationSetPersistentPresence.class);
                if (OperationSetBasicInstantMessagingJabberImpl.this.smackMessageListener == null) {
                    OperationSetBasicInstantMessagingJabberImpl.this.smackMessageListener = new SmackMessageListener();
                } else {
                    OperationSetBasicInstantMessagingJabberImpl.this.jabberProvider.getConnection().removePacketListener(OperationSetBasicInstantMessagingJabberImpl.this.smackMessageListener);
                }
                OperationSetBasicInstantMessagingJabberImpl.this.jabberProvider.getConnection().addPacketListener(OperationSetBasicInstantMessagingJabberImpl.this.smackMessageListener, new AndFilter((PacketFilter[]) OperationSetBasicInstantMessagingJabberImpl.this.packetFilters.toArray(new PacketFilter[OperationSetBasicInstantMessagingJabberImpl.this.packetFilters.size()])));
            } else if (evt.getNewState() == RegistrationState.REGISTERED) {
                boolean enableCarbon;
                if (OperationSetBasicInstantMessagingJabberImpl.this.jabberProvider.getAccountID().getAccountPropertyBoolean("GMAIL_NOTIFICATIONS_ENABLED", false)) {
                    OperationSetBasicInstantMessagingJabberImpl.this.subscribeForGmailNotifications();
                }
                if (!OperationSetBasicInstantMessagingJabberImpl.this.isCarbonSupported() || OperationSetBasicInstantMessagingJabberImpl.this.jabberProvider.getAccountID().getAccountPropertyBoolean("CARBON_DISABLED", false)) {
                    enableCarbon = false;
                } else {
                    enableCarbon = true;
                }
                if (enableCarbon) {
                    OperationSetBasicInstantMessagingJabberImpl.this.enableDisableCarbon(true);
                } else {
                    OperationSetBasicInstantMessagingJabberImpl.this.isCarbonEnabled = false;
                }
            } else if (evt.getNewState() == RegistrationState.UNREGISTERED || evt.getNewState() == RegistrationState.CONNECTION_FAILED || evt.getNewState() == RegistrationState.AUTHENTICATION_FAILED) {
                if (!(OperationSetBasicInstantMessagingJabberImpl.this.jabberProvider.getConnection() == null || OperationSetBasicInstantMessagingJabberImpl.this.smackMessageListener == null)) {
                    OperationSetBasicInstantMessagingJabberImpl.this.jabberProvider.getConnection().removePacketListener(OperationSetBasicInstantMessagingJabberImpl.this.smackMessageListener);
                }
                OperationSetBasicInstantMessagingJabberImpl.this.smackMessageListener = null;
            }
        }
    }

    private class SmackMessageListener implements PacketListener {
        private SmackMessageListener() {
        }

        public void processPacket(Packet packet) {
            if (packet instanceof Message) {
                Message msg = (Message) packet;
                boolean isForwardedSentMessage = false;
                if (msg.getBody() == null) {
                    CarbonPacketExtension carbonExt = (CarbonPacketExtension) msg.getExtension(CarbonPacketExtension.NAMESPACE);
                    if (carbonExt != null) {
                        isForwardedSentMessage = carbonExt.getElementName() == CarbonPacketExtension.SENT_ELEMENT_NAME;
                        List<ForwardedPacketExtension> extensions = carbonExt.getChildExtensionsOfType(ForwardedPacketExtension.class);
                        if (!extensions.isEmpty()) {
                            msg = ((ForwardedPacketExtension) extensions.get(0)).getMessage();
                            if (msg == null || msg.getBody() == null) {
                                return;
                            }
                        }
                        return;
                    }
                    return;
                }
                if (msg.getExtension("x", "http://jabber.org/protocol/muc#user") == null) {
                    String str;
                    String userFullId = isForwardedSentMessage ? msg.getTo() : msg.getFrom();
                    String userBareID = StringUtils.parseBareAddress(userFullId);
                    boolean isPrivateMessaging = false;
                    ChatRoom privateContactRoom = ((OperationSetMultiUserChatJabberImpl) OperationSetBasicInstantMessagingJabberImpl.this.jabberProvider.getOperationSet(OperationSetMultiUserChat.class)).getChatRoom(userBareID);
                    if (privateContactRoom != null) {
                        isPrivateMessaging = true;
                    }
                    if (OperationSetBasicInstantMessagingJabberImpl.logger.isDebugEnabled() && OperationSetBasicInstantMessagingJabberImpl.logger.isDebugEnabled()) {
                        OperationSetBasicInstantMessagingJabberImpl.logger.debug("Received from " + userBareID + " the message " + msg.toXML());
                    }
                    net.java.sip.communicator.service.protocol.Message newMessage = OperationSetBasicInstantMessagingJabberImpl.this.createMessage(msg.getBody(), "text/plain", msg.getPacketID());
                    PacketExtension ext = msg.getExtension(OperationSetBasicInstantMessagingJabberImpl.HTML_NAMESPACE);
                    if (ext != null) {
                        Iterator<String> bodies = ((XHTMLExtension) ext).getBodies();
                        StringBuffer messageBuff = new StringBuffer();
                        while (bodies.hasNext()) {
                            messageBuff.append((String) bodies.next());
                        }
                        if (messageBuff.length() > 0) {
                            newMessage = OperationSetBasicInstantMessagingJabberImpl.this.createMessage(messageBuff.toString().replaceAll("\\<[bB][oO][dD][yY].*?>", "").replaceAll("\\</[bB][oO][dD][yY].*?>", "").replaceAll("&apos;", "&#39;"), "text/html", msg.getPacketID());
                        }
                    }
                    PacketExtension correctionExtension = msg.getExtension(MessageCorrectionExtension.NAMESPACE);
                    String correctedMessageUID = null;
                    if (correctionExtension != null) {
                        correctedMessageUID = ((MessageCorrectionExtension) correctionExtension).getCorrectedMessageUID();
                    }
                    OperationSetPersistentPresenceJabberImpl access$400 = OperationSetBasicInstantMessagingJabberImpl.this.opSetPersPresence;
                    if (isPrivateMessaging) {
                        str = userFullId;
                    } else {
                        str = userBareID;
                    }
                    Contact sourceContact = access$400.findContactByID(str);
                    if (msg.getType() == Type.error) {
                        if (OperationSetBasicInstantMessagingJabberImpl.logger.isInfoEnabled()) {
                            OperationSetBasicInstantMessagingJabberImpl.logger.info("Message error received from " + userBareID);
                        }
                        int errorResultCode = 1;
                        if (packet.getError().getCode() == 503) {
                            MessageEvent msgEvent = (MessageEvent) packet.getExtension("x", "jabber:x:event");
                            if (msgEvent != null && msgEvent.isOffline()) {
                                errorResultCode = 5;
                            }
                        }
                        MessageDeliveryFailedEvent messageDeliveryFailedEvent = new MessageDeliveryFailedEvent(newMessage, sourceContact, correctedMessageUID, errorResultCode);
                        if (messageDeliveryFailedEvent != null) {
                            OperationSetBasicInstantMessagingJabberImpl.this.fireMessageEvent(messageDeliveryFailedEvent);
                            return;
                        }
                        return;
                    }
                    EventObject msgEvt;
                    String address = userBareID;
                    if (isPrivateMessaging) {
                        address = JabberActivator.getResources().getI18NString("service.gui.FROM", new String[]{StringUtils.parseResource(msg.getFrom()), userBareID});
                    }
                    OperationSetBasicInstantMessagingJabberImpl.this.putJidForAddress(address, userFullId);
                    if (OperationSetBasicInstantMessagingJabberImpl.logger.isTraceEnabled()) {
                        OperationSetBasicInstantMessagingJabberImpl.logger.trace("just mapped: " + userBareID + " to " + msg.getFrom());
                    }
                    if (sourceContact == null) {
                        if (OperationSetBasicInstantMessagingJabberImpl.logger.isDebugEnabled()) {
                            OperationSetBasicInstantMessagingJabberImpl.logger.debug("received a message from an unknown contact: " + userBareID);
                        }
                        sourceContact = OperationSetBasicInstantMessagingJabberImpl.this.opSetPersPresence.createVolatileContact(userFullId, isPrivateMessaging);
                    }
                    Date timestamp = new Date();
                    PacketExtension delay = msg.getExtension("x", "jabber:x:delay");
                    if (delay != null && (delay instanceof DelayInformation)) {
                        timestamp = ((DelayInformation) delay).getStamp();
                    }
                    delay = msg.getExtension("delay", "urn:xmpp:delay");
                    if (delay != null && (delay instanceof DelayInfo)) {
                        timestamp = ((DelayInfo) delay).getStamp();
                    }
                    ContactResource resource = ((ContactJabberImpl) sourceContact).getResourceFromJid(userFullId);
                    if (isForwardedSentMessage) {
                        msgEvt = new MessageDeliveredEvent(newMessage, sourceContact, timestamp);
                    } else {
                        msgEvt = new MessageReceivedEvent(newMessage, sourceContact, resource, timestamp, correctedMessageUID, isPrivateMessaging, privateContactRoom);
                    }
                    if (msgEvt != null) {
                        OperationSetBasicInstantMessagingJabberImpl.this.fireMessageEvent(msgEvt);
                    }
                }
            }
        }
    }

    private class TargetAddress {
        String jid;
        long lastUpdatedTime;

        private TargetAddress() {
        }
    }

    OperationSetBasicInstantMessagingJabberImpl(ProtocolProviderServiceJabberImpl provider) {
        this.jabberProvider = provider;
        this.packetFilters.add(new GroupMessagePacketFilter());
        this.packetFilters.add(new PacketTypeFilter(Message.class));
        provider.addRegistrationStateChangeListener(new RegistrationStateListener());
        ProviderManager.getInstance().addExtensionProvider(MessageCorrectionExtension.ELEMENT_NAME, MessageCorrectionExtension.NAMESPACE, new MessageCorrectionExtensionProvider());
    }

    public net.java.sip.communicator.service.protocol.Message createMessageWithUID(String messageText, String contentType, String messageUID) {
        return new MessageJabberImpl(messageText, contentType, "UTF-8", null, messageUID);
    }

    public net.java.sip.communicator.service.protocol.Message createMessage(String content, String contentType) {
        return createMessage(content, contentType, "UTF-8", null);
    }

    public net.java.sip.communicator.service.protocol.Message createMessage(String content, String contentType, String encoding, String subject) {
        return new MessageJabberImpl(content, contentType, encoding, subject);
    }

    /* access modifiers changed from: 0000 */
    public net.java.sip.communicator.service.protocol.Message createMessage(String content, String contentType, String messageUID) {
        return new MessageJabberImpl(content, contentType, "UTF-8", null, messageUID);
    }

    public boolean isOfflineMessagingSupported() {
        return true;
    }

    public boolean isContentTypeSupported(String contentType) {
        return contentType.equals("text/plain") || contentType.equals("text/html");
    }

    public boolean isContentTypeSupported(String contentType, Contact contact) {
        if (contentType.equals("text/plain")) {
            return true;
        }
        if (!contentType.equals("text/html")) {
            return false;
        }
        String toJID = getJidForAddress(contact.getAddress());
        if (toJID == null) {
            toJID = contact.getAddress();
        }
        return this.jabberProvider.isFeatureListSupported(toJID, HTML_NAMESPACE);
    }

    public Chat obtainChatInstance(String jid) {
        XMPPConnection jabberConnection = this.jabberProvider.getConnection();
        Chat chat = jabberConnection.getChatManager().getThreadChat(jid);
        if (chat != null) {
            return chat;
        }
        return jabberConnection.getChatManager().createChat(jid, new MessageListener() {
            public void processMessage(Chat chat, Message message) {
            }
        });
    }

    private void purgeOldJids() {
        long currentTime = System.currentTimeMillis();
        Iterator<Entry<String, TargetAddress>> entries = this.jids.entrySet().iterator();
        while (entries.hasNext()) {
            if (currentTime - ((TargetAddress) ((Entry) entries.next()).getValue()).lastUpdatedTime > JID_INACTIVITY_TIMEOUT) {
                entries.remove();
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public String getJidForAddress(String address) {
        String str;
        synchronized (this.jids) {
            purgeOldJids();
            TargetAddress ta = (TargetAddress) this.jids.get(address);
            if (ta == null) {
                str = null;
            } else {
                ta.lastUpdatedTime = System.currentTimeMillis();
                str = ta.jid;
            }
        }
        return str;
    }

    /* access modifiers changed from: private */
    public void putJidForAddress(String address, String jid) {
        synchronized (this.jids) {
            purgeOldJids();
            TargetAddress ta = (TargetAddress) this.jids.get(address);
            if (ta == null) {
                ta = new TargetAddress();
                this.jids.put(address, ta);
            }
            ta.jid = jid;
            ta.lastUpdatedTime = System.currentTimeMillis();
        }
    }

    private MessageDeliveredEvent sendMessage(Contact to, ContactResource toResource, net.java.sip.communicator.service.protocol.Message message, PacketExtension[] extensions) {
        if (to instanceof ContactJabberImpl) {
            try {
                assertConnected();
                Message msg = new Message();
                String toJID = null;
                boolean sendToBaseResource = false;
                if (toResource != null) {
                    if (toResource.equals(ContactResource.BASE_RESOURCE)) {
                        toJID = to.getAddress();
                        sendToBaseResource = true;
                    } else {
                        toJID = ((ContactResourceJabberImpl) toResource).getFullJid();
                    }
                }
                if (toJID == null) {
                    toJID = getJidForAddress(to.getAddress());
                }
                if (toJID == null) {
                    sendToBaseResource = true;
                    toJID = to.getAddress();
                }
                Chat chat = obtainChatInstance(toJID);
                msg.setPacketID(message.getMessageUID());
                msg.setTo(toJID);
                for (PacketExtension ext : extensions) {
                    msg.addExtension(ext);
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Will send a message to:" + toJID + " chat.jid=" + chat.getParticipant() + " chat.tid=" + chat.getThreadID());
                }
                MessageDeliveredEvent msgDeliveryPendingEvt = messageDeliveryPendingTransform(new MessageDeliveredEvent(message, to, toResource));
                if (msgDeliveryPendingEvt == null) {
                    return null;
                }
                String content = msgDeliveryPendingEvt.getSourceMessage().getContent();
                if (message.getContentType().equals("text/html")) {
                    msg.setBody(Html2Text.extractText(content));
                    if (this.jabberProvider.isFeatureListSupported(chat.getParticipant(), HTML_NAMESPACE)) {
                        XHTMLManager.addBody(msg, OPEN_BODY_TAG + content + CLOSE_BODY_TAG);
                    }
                } else {
                    msg.setBody(content);
                }
                if (msgDeliveryPendingEvt.isMessageEncrypted()) {
                    msg.addExtension(new PrivateExtension());
                }
                MessageEventManager.addNotificationsRequests(msg, true, false, false, true);
                chat.sendMessage(msg);
                if (sendToBaseResource) {
                    putJidForAddress(to.getAddress(), to.getAddress());
                }
                return new MessageDeliveredEvent(message, to, toResource);
            } catch (XMPPException ex) {
                logger.error("message not sent", ex);
                return null;
            }
        }
        throw new IllegalArgumentException("The specified contact is not a Jabber contact." + to);
    }

    public void sendInstantMessage(Contact to, net.java.sip.communicator.service.protocol.Message message) throws IllegalStateException, IllegalArgumentException {
        sendInstantMessage(to, null, message);
    }

    public void sendInstantMessage(Contact to, ContactResource toResource, net.java.sip.communicator.service.protocol.Message message) throws IllegalStateException, IllegalArgumentException {
        fireMessageEvent(sendMessage(to, toResource, message, new PacketExtension[0]));
    }

    public void correctMessage(Contact to, ContactResource resource, net.java.sip.communicator.service.protocol.Message message, String correctedMessageUID) {
        MessageDeliveredEvent msgDelivered = sendMessage(to, resource, message, new PacketExtension[]{new MessageCorrectionExtension(correctedMessageUID)});
        msgDelivered.setCorrectedMessageUID(correctedMessageUID);
        fireMessageEvent(msgDelivered);
    }

    private void assertConnected() throws IllegalStateException {
        if (this.opSetPersPresence == null) {
            throw new IllegalStateException("The provider must be signed on the service before being able to communicate.");
        }
        this.opSetPersPresence.assertConnected();
    }

    /* access modifiers changed from: private */
    public void enableDisableCarbon(final boolean enable) {
        IQ iq = new IQ() {
            public String getChildElementXML() {
                return Separators.LESS_THAN + (enable ? "enable" : "disable") + " xmlns='urn:xmpp:carbons:2' />";
            }
        };
        Packet response = null;
        try {
            PacketCollector packetCollector = this.jabberProvider.getConnection().createPacketCollector(new PacketIDFilter(iq.getPacketID()));
            iq.setFrom(this.jabberProvider.getOurJID());
            iq.setType(IQ.Type.SET);
            this.jabberProvider.getConnection().sendPacket(iq);
            response = packetCollector.nextResult((long) SmackConfiguration.getPacketReplyTimeout());
            packetCollector.cancel();
        } catch (Exception e) {
            logger.error("Failed to enable carbon.", e);
        }
        this.isCarbonEnabled = false;
        if (response == null) {
            logger.error("Failed to enable carbon. No response is received.");
        } else if (response.getError() != null) {
            logger.error("Failed to enable carbon: " + response.getError());
        } else if ((response instanceof IQ) && ((IQ) response).getType().equals(IQ.Type.RESULT)) {
            this.isCarbonEnabled = true;
        } else {
            logger.error("Failed to enable carbon. The response is not correct.");
        }
    }

    /* access modifiers changed from: private */
    public boolean isCarbonSupported() {
        try {
            return this.jabberProvider.getDiscoveryManager().discoverInfo(this.jabberProvider.getAccountID().getService()).containsFeature(CarbonPacketExtension.NAMESPACE);
        } catch (XMPPException e) {
            logger.error("Failed to retrieve carbon support.", e);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void subscribeForGmailNotifications() {
        String accountIDService = this.jabberProvider.getAccountID().getService();
        if (this.jabberProvider.isFeatureSupported(accountIDService, "google:mail:notify")) {
            if (logger.isDebugEnabled()) {
                logger.debug(accountIDService + " seems to provide a Gmail notification " + " service so we will try to subscribe for it");
            }
            ProviderManager providerManager = ProviderManager.getInstance();
            providerManager.addIQProvider(MailboxIQ.ELEMENT_NAME, "google:mail:notify", new MailboxIQProvider());
            providerManager.addIQProvider(NewMailNotificationIQ.ELEMENT_NAME, "google:mail:notify", new NewMailNotificationProvider());
            XMPPConnection connection = this.jabberProvider.getConnection();
            connection.addPacketListener(new MailboxIQListener(), new PacketTypeFilter(MailboxIQ.class));
            connection.addPacketListener(new NewMailNotificationListener(), new PacketTypeFilter(NewMailNotificationIQ.class));
            if (!this.opSetPersPresence.getCurrentStatusMessage().equals(SipStatusEnum.OFFLINE)) {
                MailboxQueryIQ mailboxQuery = new MailboxQueryIQ();
                if (logger.isTraceEnabled()) {
                    logger.trace("sending mailNotification for acc: " + this.jabberProvider.getAccountID().getAccountUniqueID());
                }
                this.jabberProvider.getConnection().sendPacket(mailboxQuery);
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug(accountIDService + " does not seem to provide a Gmail notification " + " service so we won't be trying to subscribe for it");
        }
    }

    /* access modifiers changed from: private */
    public String createMailboxDescription(MailboxIQ mailboxIQ) {
        int threadCount = mailboxIQ.getThreadCount();
        String resourceHeaderKey = threadCount > 1 ? "service.gui.NEW_GMAIL_MANY_HEADER" : "service.gui.NEW_GMAIL_HEADER";
        String resourceFooterKey = threadCount > 1 ? "service.gui.NEW_GMAIL_MANY_FOOTER" : "service.gui.NEW_GMAIL_FOOTER";
        StringBuffer message = new StringBuffer(JabberActivator.getResources().getI18NString(resourceHeaderKey, new String[]{this.jabberProvider.getAccountID().getService(), mailboxIQ.getUrl(), Integer.toString(threadCount)}));
        message.append("<table width=100% cellpadding=2 cellspacing=0 ");
        message.append("border=0 bgcolor=#e8eef7>");
        Iterator<MailThreadInfo> threads = mailboxIQ.threads();
        String maxThreadsStr = (String) JabberActivator.getConfigurationService().getProperty(PNAME_MAX_GMAIL_THREADS_PER_NOTIFICATION);
        int maxThreads = 5;
        if (maxThreadsStr != null) {
            try {
                maxThreads = Integer.parseInt(maxThreadsStr);
            } catch (NumberFormatException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to parse max threads count: " + maxThreads + ". Going for default.");
                }
            }
        }
        for (int i = 0; i < maxThreads && threads.hasNext(); i++) {
            message.append(((MailThreadInfo) threads.next()).createHtmlDescription());
        }
        message.append("</table><br/>");
        if (threadCount > maxThreads) {
            message.append(JabberActivator.getResources().getI18NString(resourceFooterKey, new String[]{mailboxIQ.getUrl(), Integer.toString(threadCount - maxThreads)}));
        }
        return message.toString();
    }

    public long getInactivityTimeout() {
        return JID_INACTIVITY_TIMEOUT;
    }

    public void addMessageFilters(PacketFilter filter) {
        this.packetFilters.add(filter);
    }
}
