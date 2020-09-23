package net.java.sip.communicator.impl.protocol.sip;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import net.java.sip.communicator.service.protocol.AbstractOperationSetTypingNotifications;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.Message;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.MessageDeliveredEvent;
import net.java.sip.communicator.service.protocol.event.MessageDeliveryFailedEvent;
import net.java.sip.communicator.service.protocol.event.MessageListener;
import net.java.sip.communicator.service.protocol.event.MessageReceivedEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.util.Logger;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.TimeoutEvent;
import org.jitsi.javax.sip.TransactionUnavailableException;
import org.jitsi.javax.sip.header.CallIdHeader;
import org.jitsi.javax.sip.header.ContentTypeHeader;
import org.jitsi.javax.sip.header.FromHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;
import org.jitsi.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OperationSetTypingNotificationsSipImpl extends AbstractOperationSetTypingNotifications<ProtocolProviderServiceSipImpl> implements SipMessageProcessor, MessageListener {
    private static final String COMPOSING_STATE_ACTIVE = "active";
    private static final String COMPOSING_STATE_IDLE = "idle";
    private static final String CONTENT_SUBTYPE = "im-iscomposing+xml";
    private static final String CONTENT_TYPE = "application/im-iscomposing+xml";
    private static final String NS_VALUE = "urn:ietf:params:xml:ns:im-iscomposing";
    private static final int REFRESH_DEFAULT_TIME = 120;
    private static final String REFRESH_ELEMENT = "refresh";
    private static final int REFRESH_TIME = 60;
    private static final String STATE_ELEMENT = "state";
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(OperationSetTypingNotificationsSipImpl.class);
    private OperationSetBasicInstantMessagingSipImpl opSetBasicIm = null;
    /* access modifiers changed from: private */
    public OperationSetPresenceSipImpl opSetPersPresence = null;
    private final RegistrationStateListener registrationListener = new RegistrationStateListener();
    private Timer timer = new Timer();
    /* access modifiers changed from: private|final */
    public final List<TypingTask> typingTasks = new Vector();

    private class RegistrationStateListener implements RegistrationStateChangeListener {
        private RegistrationStateListener() {
        }

        public void registrationStateChanged(RegistrationStateChangeEvent evt) {
            if (OperationSetTypingNotificationsSipImpl.logger.isDebugEnabled()) {
                OperationSetTypingNotificationsSipImpl.logger.debug("The provider changed state from: " + evt.getOldState() + " to: " + evt.getNewState());
            }
            if (evt.getNewState() == RegistrationState.REGISTERED) {
                OperationSetTypingNotificationsSipImpl.this.opSetPersPresence = (OperationSetPresenceSipImpl) ((ProtocolProviderServiceSipImpl) OperationSetTypingNotificationsSipImpl.this.parentProvider).getOperationSet(OperationSetPersistentPresence.class);
            }
        }
    }

    private class TypingTask extends TimerTask {
        private final Contact contact;

        TypingTask(Contact contact, boolean receiving) {
            this.contact = contact;
        }

        public void run() {
            OperationSetTypingNotificationsSipImpl.this.typingTasks.remove(this);
            OperationSetTypingNotificationsSipImpl.this.fireTypingNotificationsEvent(this.contact, 4);
        }

        public Contact getContact() {
            return this.contact;
        }
    }

    OperationSetTypingNotificationsSipImpl(ProtocolProviderServiceSipImpl provider, OperationSetBasicInstantMessagingSipImpl opSetBasicIm) {
        super(provider);
        provider.addRegistrationStateChangeListener(this.registrationListener);
        this.opSetBasicIm = opSetBasicIm;
        opSetBasicIm.addMessageProcessor(this);
    }

    public boolean processMessage(RequestEvent requestEvent) {
        Request req = requestEvent.getRequest();
        ContentTypeHeader ctheader = (ContentTypeHeader) req.getHeader("Content-Type");
        if (ctheader == null || !ctheader.getContentSubType().equalsIgnoreCase(CONTENT_SUBTYPE)) {
            return true;
        }
        String content = new String(req.getRawContent());
        if (content == null || content.length() == 0) {
            sendResponse(requestEvent, Response.BAD_REQUEST);
            return false;
        }
        FromHeader fromHeader = (FromHeader) requestEvent.getRequest().getHeader("From");
        if (fromHeader == null) {
            logger.error("received a request without a from header");
            return true;
        }
        Contact from = this.opSetPersPresence.resolveContactID(fromHeader.getAddress().getURI().toString());
        if (from == null) {
            if (fromHeader.getAddress().getDisplayName() != null) {
                from = this.opSetPersPresence.createVolatileContact(fromHeader.getAddress().getURI().toString(), fromHeader.getAddress().getDisplayName().toString());
            } else {
                from = this.opSetPersPresence.createVolatileContact(fromHeader.getAddress().getURI().toString());
            }
        }
        Document doc = null;
        try {
            doc = this.opSetPersPresence.convertDocument(content);
        } catch (Exception e) {
        }
        if (doc == null) {
            sendResponse(requestEvent, Response.BAD_REQUEST);
            return false;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("parsing:\n" + content);
        }
        NodeList stateList = doc.getElementsByTagNameNS(NS_VALUE, "state");
        if (stateList.getLength() == 0) {
            logger.error("no state element in this document");
            sendResponse(requestEvent, Response.BAD_REQUEST);
            return false;
        }
        Node stateNode = stateList.item(0);
        if (stateNode.getNodeType() != (short) 1) {
            logger.error("the state node is not an element");
            sendResponse(requestEvent, Response.BAD_REQUEST);
            return false;
        }
        String state = XMLUtils.getText((Element) stateNode);
        if (state == null || state.length() == 0) {
            logger.error("the state element without value");
            sendResponse(requestEvent, Response.BAD_REQUEST);
            return false;
        }
        NodeList refreshList = doc.getElementsByTagNameNS(NS_VALUE, REFRESH_ELEMENT);
        int refresh = REFRESH_DEFAULT_TIME;
        if (refreshList.getLength() != 0) {
            Node refreshNode = refreshList.item(0);
            if (refreshNode.getNodeType() == (short) 1) {
                try {
                    refresh = Integer.parseInt(XMLUtils.getText((Element) refreshNode));
                } catch (Exception e2) {
                    logger.error("Wrong content for refresh", e2);
                }
            }
        }
        if (state.equals("active")) {
            TypingTask task = findTypingTask(from);
            if (task != null) {
                this.typingTasks.remove(task);
                task.cancel();
            }
            TimerTask typingTask = new TypingTask(from, true);
            this.typingTasks.add(typingTask);
            this.timer.schedule(typingTask, (long) (refresh * 1000));
            fireTypingNotificationsEvent(from, 1);
        } else if (state.equals(COMPOSING_STATE_IDLE)) {
            fireTypingNotificationsEvent(from, 3);
        }
        sendResponse(requestEvent, Response.OK);
        return false;
    }

    public boolean processResponse(ResponseEvent responseEvent, Map<String, Message> sentMsg) {
        Request req = responseEvent.getClientTransaction().getRequest();
        ContentTypeHeader ctheader = (ContentTypeHeader) req.getHeader("Content-Type");
        if (ctheader == null || !ctheader.getContentSubType().equalsIgnoreCase(CONTENT_SUBTYPE)) {
            return true;
        }
        int status = responseEvent.getResponse().getStatusCode();
        String key = ((CallIdHeader) req.getHeader("Call-ID")).getCallId();
        if (status >= Response.OK && status < 300) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ack received from the network : " + responseEvent.getResponse().getReasonPhrase());
            }
            sentMsg.remove(key);
            return false;
        } else if (status < Response.BAD_REQUEST || status == Response.UNAUTHORIZED || status == Response.PROXY_AUTHENTICATION_REQUIRED) {
            return true;
        } else {
            logger.warn("Error received : " + responseEvent.getResponse().getReasonPhrase());
            sentMsg.remove(key);
            return false;
        }
    }

    public boolean processTimeout(TimeoutEvent timeoutEvent, Map<String, Message> map) {
        ContentTypeHeader ctheader = (ContentTypeHeader) timeoutEvent.getClientTransaction().getRequest().getHeader("Content-Type");
        return ctheader == null || !CONTENT_SUBTYPE.equalsIgnoreCase(ctheader.getContentSubType());
    }

    private TypingTask findTypingTask(Contact contact) {
        for (TypingTask typingTask : this.typingTasks) {
            if (typingTask.getContact().equals(contact)) {
                return typingTask;
            }
        }
        return null;
    }

    public void sendTypingNotification(Contact to, int typingState) throws IllegalStateException, IllegalArgumentException {
        assertConnected();
        if (to instanceof ContactSipImpl) {
            Document doc = this.opSetPersPresence.createDocument();
            Element rootEl = doc.createElementNS(NS_VALUE, "isComposing");
            rootEl.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            doc.appendChild(rootEl);
            Element state;
            if (typingState == 1) {
                state = doc.createElement("state");
                state.appendChild(doc.createTextNode("active"));
                rootEl.appendChild(state);
                Element refresh = doc.createElement(REFRESH_ELEMENT);
                refresh.appendChild(doc.createTextNode(String.valueOf(60)));
                rootEl.appendChild(refresh);
            } else if (typingState == 4) {
                state = doc.createElement("state");
                state.appendChild(doc.createTextNode(COMPOSING_STATE_IDLE));
                rootEl.appendChild(state);
            } else {
                return;
            }
            Message message = this.opSetBasicIm.createMessage(this.opSetPersPresence.convertDocument(doc), CONTENT_TYPE, "UTF-8", null);
            try {
                try {
                    this.opSetBasicIm.sendMessageRequest(this.opSetBasicIm.createMessageRequest(to, message), to, message);
                    return;
                } catch (TransactionUnavailableException ex) {
                    logger.error("Failed to create messageTransaction.\nThis is most probably a network connection error.", ex);
                    return;
                } catch (SipException ex2) {
                    logger.error("Failed to send the message.", ex2);
                    return;
                }
            } catch (OperationFailedException ex3) {
                logger.error("Failed to create the message.", ex3);
                return;
            }
        }
        throw new IllegalArgumentException("The specified contact is not a Sip contact." + to);
    }

    private void sendResponse(RequestEvent requestEvent, int response) {
        try {
            SipStackSharing.getOrCreateServerTransaction(requestEvent).sendResponse(((ProtocolProviderServiceSipImpl) this.parentProvider).getMessageFactory().createResponse(response, requestEvent.getRequest()));
        } catch (ParseException exc) {
            logger.error("failed to build the response", exc);
        } catch (SipException exc2) {
            logger.error("failed to send the response : " + exc2.getMessage(), exc2);
        } catch (InvalidArgumentException exc3) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invalid argument for createResponse : " + exc3.getMessage(), exc3);
            }
        }
    }

    public void messageReceived(MessageReceivedEvent evt) {
        Contact from = evt.getSourceContact();
        TypingTask task = findTypingTask(from);
        if (task != null) {
            task.cancel();
            fireTypingNotificationsEvent(from, 4);
        }
    }

    public void messageDelivered(MessageDeliveredEvent evt) {
    }

    public void messageDeliveryFailed(MessageDeliveryFailedEvent evt) {
    }

    /* access modifiers changed from: 0000 */
    public void shutdown() {
        ((ProtocolProviderServiceSipImpl) this.parentProvider).removeRegistrationStateChangeListener(this.registrationListener);
    }
}
