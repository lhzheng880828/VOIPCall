package net.java.sip.communicator.impl.protocol.sip;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.java.sip.communicator.impl.protocol.sip.EventPackageSubscriber.Subscription;
import net.java.sip.communicator.service.protocol.OperationSetMessageWaiting;
import net.java.sip.communicator.service.protocol.OperationSetMessageWaiting.MessageType;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.MessageWaitingListener;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.header.ims.AuthorizationHeaderIms;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.header.EventHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.util.StringUtils;

public class OperationSetMessageWaitingSipImpl implements OperationSetMessageWaiting, RegistrationStateChangeListener {
    private static final String CONTENT_SUB_TYPE = "simple-message-summary";
    static final String EVENT_PACKAGE = "message-summary";
    private static final int REFRESH_MARGIN = 60;
    private static final int SUBSCRIPTION_DURATION = 3600;
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(OperationSetMessageWaitingSipImpl.class);
    private final Map<MessageType, List<MessageWaitingListener>> messageWaitingNotificationListeners = new HashMap();
    private EventPackageSubscriber messageWaitingSubscriber = null;
    /* access modifiers changed from: private|final */
    public final ProtocolProviderServiceSipImpl provider;
    private int readMessages = 0;
    private int readUrgentMessages = 0;
    private final TimerScheduler timer = new TimerScheduler();
    private int unreadMessages = 0;
    private int unreadUrgentMessages = 0;

    private class MessageSummarySubscriber extends Subscription {
        private Pattern messageWaitingCountPattern = Pattern.compile("(\\d+)/(\\d+)( \\((\\d+)/(\\d+)\\))*");

        public MessageSummarySubscriber(Address toAddress) {
            super(toAddress);
        }

        /* access modifiers changed from: protected */
        public void processActiveRequest(RequestEvent requestEvent, byte[] rawContent) {
            if (rawContent != null && rawContent.length > 0) {
                try {
                    String messageAccount = OperationSetMessageWaitingSipImpl.this.provider.getAccountID().getAccountPropertyString("VOICEMAIL_CHECK_URI");
                    BufferedReader input = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(rawContent)));
                    boolean messageWaiting = false;
                    boolean eventFired = false;
                    while (true) {
                        String line = input.readLine();
                        if (line == null) {
                            break;
                        }
                        String lcaseLine = line.toLowerCase();
                        if (lcaseLine.startsWith("messages-waiting")) {
                            if (line.substring(line.indexOf(Separators.COLON) + 1).trim().equalsIgnoreCase(AuthorizationHeaderIms.YES)) {
                                messageWaiting = true;
                            }
                        } else if (lcaseLine.startsWith("message-account")) {
                            messageAccount = line.substring(line.indexOf(Separators.COLON) + 1).trim();
                        } else if (lcaseLine.startsWith(MessageType.VOICE.toString()) || lcaseLine.startsWith(MessageType.FAX.toString()) || lcaseLine.startsWith(MessageType.MULTIMEDIA.toString()) || lcaseLine.startsWith(MessageType.PAGER.toString()) || lcaseLine.startsWith(MessageType.TEXT.toString()) || lcaseLine.startsWith(MessageType.NONE.toString())) {
                            String msgType = lcaseLine.substring(0, line.indexOf(Separators.COLON)).trim();
                            Matcher matcher = this.messageWaitingCountPattern.matcher(line.substring(line.indexOf(Separators.COLON) + 1).trim());
                            if (matcher.find()) {
                                int i;
                                int i2;
                                String newM = matcher.group(1);
                                String oldM = matcher.group(2);
                                String urgentNew = matcher.group(4);
                                String urgentOld = matcher.group(5);
                                OperationSetMessageWaitingSipImpl operationSetMessageWaitingSipImpl = OperationSetMessageWaitingSipImpl.this;
                                int intValue = Integer.valueOf(newM).intValue();
                                int intValue2 = Integer.valueOf(oldM).intValue();
                                if (urgentNew == null) {
                                    i = 0;
                                } else {
                                    i = Integer.valueOf(urgentNew).intValue();
                                }
                                if (urgentOld == null) {
                                    i2 = 0;
                                } else {
                                    i2 = Integer.valueOf(urgentOld).intValue();
                                }
                                operationSetMessageWaitingSipImpl.fireVoicemailNotificationEvent(msgType, messageAccount, intValue, intValue2, i, i2);
                                eventFired = true;
                            }
                        }
                    }
                    if (messageWaiting && !eventFired) {
                        OperationSetMessageWaitingSipImpl.this.fireVoicemailNotificationEvent(MessageType.VOICE.toString(), messageAccount, 1, 0, 0, 0);
                    }
                } catch (IOException e) {
                    OperationSetMessageWaitingSipImpl.logger.error("Error processing message waiting info");
                }
            }
        }

        /* access modifiers changed from: protected */
        public void processFailureResponse(ResponseEvent responseEvent, int statusCode) {
            if (OperationSetMessageWaitingSipImpl.logger.isDebugEnabled()) {
                OperationSetMessageWaitingSipImpl.logger.debug("Processing failed: " + statusCode);
            }
        }

        /* access modifiers changed from: protected */
        public void processSuccessResponse(ResponseEvent responseEvent, int statusCode) {
            if (OperationSetMessageWaitingSipImpl.logger.isDebugEnabled()) {
                OperationSetMessageWaitingSipImpl.logger.debug("Cannot subscripe to presence watcher info!");
            }
        }

        /* access modifiers changed from: protected */
        public void processTerminatedRequest(RequestEvent requestEvent, String reasonCode) {
            if (OperationSetMessageWaitingSipImpl.logger.isDebugEnabled()) {
                OperationSetMessageWaitingSipImpl.logger.debug("Processing terminated: " + reasonCode);
            }
        }
    }

    OperationSetMessageWaitingSipImpl(ProtocolProviderServiceSipImpl provider) {
        this.provider = provider;
        this.provider.addRegistrationStateChangeListener(this);
        this.provider.registerMethodProcessor("SUBSCRIBE", new MethodProcessorAdapter() {
            public boolean processRequest(RequestEvent requestEvent) {
                return OperationSetMessageWaitingSipImpl.this.processRequest(requestEvent);
            }
        });
    }

    public void addMessageWaitingNotificationListener(MessageType type, MessageWaitingListener listener) {
        synchronized (this.messageWaitingNotificationListeners) {
            List<MessageWaitingListener> l = (List) this.messageWaitingNotificationListeners.get(type);
            if (l == null) {
                l = new ArrayList();
                this.messageWaitingNotificationListeners.put(type, l);
            }
            if (!l.contains(listener)) {
                l.add(listener);
            }
        }
    }

    public void removeMessageWaitingNotificationListener(MessageType type, MessageWaitingListener listener) {
        synchronized (this.messageWaitingNotificationListeners) {
            if (((List) this.messageWaitingNotificationListeners.get(type)) != null) {
                this.messageWaitingNotificationListeners.remove(listener);
            }
        }
    }

    public void registrationStateChanged(RegistrationStateChangeEvent evt) {
        if (evt.getNewState().equals(RegistrationState.REGISTERED)) {
            this.messageWaitingSubscriber = new EventPackageSubscriber(this.provider, EVENT_PACKAGE, 3600, CONTENT_SUB_TYPE, this.timer, 60) {
                /* access modifiers changed from: protected */
                public Subscription getSubscription(String callId) {
                    Subscription resultSub = super.getSubscription(callId);
                    if (resultSub != null) {
                        return resultSub;
                    }
                    for (Object s : getSubscriptions()) {
                        if (s instanceof MessageSummarySubscriber) {
                            return (MessageSummarySubscriber) s;
                        }
                    }
                    return null;
                }
            };
            try {
                Address subscribeAddress = getSubscribeAddress();
                if (subscribeAddress != null) {
                    this.messageWaitingSubscriber.subscribe(new MessageSummarySubscriber(subscribeAddress));
                }
            } catch (Throwable e) {
                logger.error("Error subscribing for mailbox", e);
            }
        } else if (evt.getNewState().equals(RegistrationState.UNREGISTERING) && this.messageWaitingSubscriber != null) {
            try {
                this.messageWaitingSubscriber.unsubscribe(getSubscribeAddress(), false);
            } catch (Throwable t) {
                logger.error("Error unsubscribing mailbox", t);
            }
        }
    }

    private Address getSubscribeAddress() throws ParseException {
        String vmAddressURI = this.provider.getAccountID().getAccountPropertyString("VOICEMAIL_URI");
        if (StringUtils.isNullOrEmpty(vmAddressURI)) {
            return this.provider.getRegistrarConnection().getAddressOfRecord();
        }
        return this.provider.parseAddressString(vmAddressURI);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Missing block: B:13:0x002c, code skipped:
            r3 = net.java.sip.communicator.service.protocol.OperationSetMessageWaiting.MessageType.valueOfByType(r14);
            r1 = new net.java.sip.communicator.service.protocol.event.MessageWaitingEvent(r13.provider, r3, r15, r16, r17, r18, r19);
            r4 = r13.messageWaitingNotificationListeners;
     */
    /* JADX WARNING: Missing block: B:14:0x0042, code skipped:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:16:?, code skipped:
            r12 = (java.util.List) r13.messageWaitingNotificationListeners.get(r3);
     */
    /* JADX WARNING: Missing block: B:17:0x004b, code skipped:
            if (r12 != null) goto L_0x0055;
     */
    /* JADX WARNING: Missing block: B:18:0x004d, code skipped:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:28:?, code skipped:
            r11 = new java.util.ArrayList(r12);
     */
    /* JADX WARNING: Missing block: B:29:0x005a, code skipped:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:30:0x005b, code skipped:
            r9 = r11.iterator();
     */
    /* JADX WARNING: Missing block: B:32:0x0063, code skipped:
            if (r9.hasNext() == false) goto L_0x001a;
     */
    /* JADX WARNING: Missing block: B:33:0x0065, code skipped:
            ((net.java.sip.communicator.service.protocol.event.MessageWaitingListener) r9.next()).messageWaitingNotify(r1);
     */
    /* JADX WARNING: Missing block: B:39:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:40:?, code skipped:
            return;
     */
    public void fireVoicemailNotificationEvent(java.lang.String r14, java.lang.String r15, int r16, int r17, int r18, int r19) {
        /*
        r13 = this;
        monitor-enter(r13);
        r2 = r13.unreadMessages;	 Catch:{ all -> 0x0052 }
        r0 = r16;
        if (r2 != r0) goto L_0x001b;
    L_0x0007:
        r2 = r13.readMessages;	 Catch:{ all -> 0x0052 }
        r0 = r17;
        if (r2 != r0) goto L_0x001b;
    L_0x000d:
        r2 = r13.unreadUrgentMessages;	 Catch:{ all -> 0x0052 }
        r0 = r18;
        if (r2 != r0) goto L_0x001b;
    L_0x0013:
        r2 = r13.readUrgentMessages;	 Catch:{ all -> 0x0052 }
        r0 = r19;
        if (r2 != r0) goto L_0x001b;
    L_0x0019:
        monitor-exit(r13);	 Catch:{ all -> 0x0052 }
    L_0x001a:
        return;
    L_0x001b:
        r0 = r16;
        r13.unreadMessages = r0;	 Catch:{ all -> 0x0052 }
        r0 = r17;
        r13.readMessages = r0;	 Catch:{ all -> 0x0052 }
        r0 = r18;
        r13.unreadUrgentMessages = r0;	 Catch:{ all -> 0x0052 }
        r0 = r19;
        r13.readUrgentMessages = r0;	 Catch:{ all -> 0x0052 }
        monitor-exit(r13);	 Catch:{ all -> 0x0052 }
        r3 = net.java.sip.communicator.service.protocol.OperationSetMessageWaiting.MessageType.valueOfByType(r14);
        r1 = new net.java.sip.communicator.service.protocol.event.MessageWaitingEvent;
        r2 = r13.provider;
        r4 = r15;
        r5 = r16;
        r6 = r17;
        r7 = r18;
        r8 = r19;
        r1.<init>(r2, r3, r4, r5, r6, r7, r8);
        r4 = r13.messageWaitingNotificationListeners;
        monitor-enter(r4);
        r2 = r13.messageWaitingNotificationListeners;	 Catch:{ all -> 0x004f }
        r12 = r2.get(r3);	 Catch:{ all -> 0x004f }
        r12 = (java.util.List) r12;	 Catch:{ all -> 0x004f }
        if (r12 != 0) goto L_0x0055;
    L_0x004d:
        monitor-exit(r4);	 Catch:{ all -> 0x004f }
        goto L_0x001a;
    L_0x004f:
        r2 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x004f }
        throw r2;
    L_0x0052:
        r2 = move-exception;
        monitor-exit(r13);	 Catch:{ all -> 0x0052 }
        throw r2;
    L_0x0055:
        r11 = new java.util.ArrayList;	 Catch:{ all -> 0x004f }
        r11.<init>(r12);	 Catch:{ all -> 0x004f }
        monitor-exit(r4);	 Catch:{ all -> 0x004f }
        r9 = r11.iterator();
    L_0x005f:
        r2 = r9.hasNext();
        if (r2 == 0) goto L_0x001a;
    L_0x0065:
        r10 = r9.next();
        r10 = (net.java.sip.communicator.service.protocol.event.MessageWaitingListener) r10;
        r10.messageWaitingNotify(r1);
        goto L_0x005f;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.sip.OperationSetMessageWaitingSipImpl.fireVoicemailNotificationEvent(java.lang.String, java.lang.String, int, int, int, int):void");
    }

    /* access modifiers changed from: private */
    public boolean processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        EventHeader eventHeader = (EventHeader) request.getHeader("Event");
        if (eventHeader == null) {
            return false;
        }
        if (EVENT_PACKAGE.equalsIgnoreCase(eventHeader.getEventType()) && "SUBSCRIBE".equals(request.getMethod())) {
            return EventPackageSupport.sendNotImplementedResponse(this.provider, requestEvent);
        }
        return false;
    }

    /* access modifiers changed from: 0000 */
    public void shutdown() {
        this.provider.removeRegistrationStateChangeListener(this);
    }
}
