package net.java.sip.communicator.impl.protocol.sip;

import java.text.ParseException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import net.java.sip.communicator.service.protocol.AbstractOperationSetBasicInstantMessaging;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.Message;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetBasicInstantMessaging;
import net.java.sip.communicator.service.protocol.OperationSetPersistentPresence;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.MessageDeliveredEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.util.Logger;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.SipProvider;
import org.jitsi.javax.sip.TransactionUnavailableException;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.header.CSeqHeader;
import org.jitsi.javax.sip.header.CallIdHeader;
import org.jitsi.javax.sip.header.ContentLengthHeader;
import org.jitsi.javax.sip.header.ContentTypeHeader;
import org.jitsi.javax.sip.header.FromHeader;
import org.jitsi.javax.sip.header.Header;
import org.jitsi.javax.sip.header.HeaderFactory;
import org.jitsi.javax.sip.header.MaxForwardsHeader;
import org.jitsi.javax.sip.header.ToHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public class OperationSetBasicInstantMessagingSipImpl extends AbstractOperationSetBasicInstantMessaging {
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(OperationSetBasicInstantMessagingSipImpl.class);
    /* access modifiers changed from: private|final */
    public final List<SipMessageProcessor> messageProcessors = new Vector();
    private final boolean offlineMessageSupported;
    /* access modifiers changed from: private */
    public OperationSetPresenceSipImpl opSetPersPresence = null;
    private final RegistrationStateListener registrationListener;
    /* access modifiers changed from: private|final */
    public final Map<String, Message> sentMsg = new Hashtable(3);
    /* access modifiers changed from: private */
    public long seqN = ((long) hashCode());
    /* access modifiers changed from: private|final */
    public final ProtocolProviderServiceSipImpl sipProvider;
    private final SipStatusEnum sipStatusEnum;

    private class BasicInstantMessagingMethodProcessor extends MethodProcessorAdapter {
        private BasicInstantMessagingMethodProcessor() {
        }

        /* JADX WARNING: Missing block: B:11:0x002f, code skipped:
            net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.access$200().error("Timeout event thrown : " + r15.toString());
     */
        /* JADX WARNING: Missing block: B:12:0x0051, code skipped:
            if (r15.isServerTransaction() == false) goto L_0x0060;
     */
        /* JADX WARNING: Missing block: B:13:0x0053, code skipped:
            net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.access$200().warn("The sender has probably not received our OK");
     */
        /* JADX WARNING: Missing block: B:18:0x0060, code skipped:
            r6 = r15.getClientTransaction().getRequest();
     */
        /* JADX WARNING: Missing block: B:20:?, code skipped:
            r0 = new java.lang.String(r6.getRawContent(), getCharset(r6));
     */
        /* JADX WARNING: Missing block: B:24:0x008a, code skipped:
            r1 = move-exception;
     */
        /* JADX WARNING: Missing block: B:25:0x008b, code skipped:
            net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.access$200().warn("failed to convert the message charset", r1);
            r0 = new java.lang.String(r6.getRawContent());
     */
        /* JADX WARNING: Missing block: B:39:?, code skipped:
            return false;
     */
        public boolean processTimeout(org.jitsi.javax.sip.TimeoutEvent r15) {
            /*
            r14 = this;
            r10 = 1;
            r9 = 0;
            r11 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r11 = r11.messageProcessors;
            monitor-enter(r11);
            r12 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;	 Catch:{ all -> 0x005d }
            r12 = r12.messageProcessors;	 Catch:{ all -> 0x005d }
            r3 = r12.iterator();	 Catch:{ all -> 0x005d }
        L_0x0013:
            r12 = r3.hasNext();	 Catch:{ all -> 0x005d }
            if (r12 == 0) goto L_0x002e;
        L_0x0019:
            r5 = r3.next();	 Catch:{ all -> 0x005d }
            r5 = (net.java.sip.communicator.impl.protocol.sip.SipMessageProcessor) r5;	 Catch:{ all -> 0x005d }
            r12 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;	 Catch:{ all -> 0x005d }
            r12 = r12.sentMsg;	 Catch:{ all -> 0x005d }
            r12 = r5.processTimeout(r15, r12);	 Catch:{ all -> 0x005d }
            if (r12 != 0) goto L_0x0013;
        L_0x002b:
            monitor-exit(r11);	 Catch:{ all -> 0x005d }
            r9 = r10;
        L_0x002d:
            return r9;
        L_0x002e:
            monitor-exit(r11);	 Catch:{ all -> 0x005d }
            r11 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r12 = new java.lang.StringBuilder;
            r12.<init>();
            r13 = "Timeout event thrown : ";
            r12 = r12.append(r13);
            r13 = r15.toString();
            r12 = r12.append(r13);
            r12 = r12.toString();
            r11.error(r12);
            r11 = r15.isServerTransaction();
            if (r11 == 0) goto L_0x0060;
        L_0x0053:
            r10 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r11 = "The sender has probably not received our OK";
            r10.warn(r11);
            goto L_0x002d;
        L_0x005d:
            r9 = move-exception;
            monitor-exit(r11);	 Catch:{ all -> 0x005d }
            throw r9;
        L_0x0060:
            r11 = r15.getClientTransaction();
            r6 = r11.getRequest();
            r0 = 0;
            r0 = new java.lang.String;	 Catch:{ UnsupportedEncodingException -> 0x008a }
            r11 = r6.getRawContent();	 Catch:{ UnsupportedEncodingException -> 0x008a }
            r12 = r14.getCharset(r6);	 Catch:{ UnsupportedEncodingException -> 0x008a }
            r0.<init>(r11, r12);	 Catch:{ UnsupportedEncodingException -> 0x008a }
        L_0x0076:
            r11 = "To";
            r8 = r6.getHeader(r11);
            r8 = (org.jitsi.javax.sip.header.ToHeader) r8;
            if (r8 != 0) goto L_0x009e;
        L_0x0080:
            r10 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r11 = "received a request without a to header";
            r10.error(r11);
            goto L_0x002d;
        L_0x008a:
            r1 = move-exception;
            r11 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r12 = "failed to convert the message charset";
            r11.warn(r12, r1);
            r0 = new java.lang.String;
            r11 = r6.getRawContent();
            r0.<init>(r11);
            goto L_0x0076;
        L_0x009e:
            r9 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r9 = r9.opSetPersPresence;
            r11 = r8.getAddress();
            r11 = r11.getURI();
            r11 = r11.toString();
            r7 = r9.resolveContactID(r11);
            r2 = 0;
            if (r7 != 0) goto L_0x00ec;
        L_0x00b7:
            r9 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r11 = new java.lang.StringBuilder;
            r11.<init>();
            r12 = "timeout on a message sent to an unknown contact : ";
            r11 = r11.append(r12);
            r12 = r8.getAddress();
            r12 = r12.getURI();
            r12 = r12.toString();
            r11 = r11.append(r12);
            r11 = r11.toString();
            r9.error(r11);
            r9 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r2 = r9.createMessage(r0);
        L_0x00e3:
            r9 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r11 = 4;
            r9.fireMessageDeliveryFailed(r2, r7, r11);
            r9 = r10;
            goto L_0x002d;
        L_0x00ec:
            r9 = "Call-ID";
            r9 = r6.getHeader(r9);
            r9 = (org.jitsi.javax.sip.header.CallIdHeader) r9;
            r4 = r9.getCallId();
            r9 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r9 = r9.sentMsg;
            r2 = r9.get(r4);
            r2 = (net.java.sip.communicator.service.protocol.Message) r2;
            if (r2 != 0) goto L_0x00e3;
        L_0x0106:
            r9 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r11 = "Couldn't find the sent message.";
            r9.error(r11);
            r9 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r2 = r9.createMessage(r0);
            goto L_0x00e3;
            */
            throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl$BasicInstantMessagingMethodProcessor.processTimeout(org.jitsi.javax.sip.TimeoutEvent):boolean");
        }

        /* JADX WARNING: Missing block: B:12:0x002d, code skipped:
            r14 = r19.getRequest();
     */
        /* JADX WARNING: Missing block: B:14:?, code skipped:
            r2 = new java.lang.String(r14.getRawContent(), getCharset(r14));
     */
        /* JADX WARNING: Missing block: B:24:0x0068, code skipped:
            if (net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.access$200().isDebugEnabled() != false) goto L_0x006a;
     */
        /* JADX WARNING: Missing block: B:25:0x006a, code skipped:
            net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.access$200().debug("failed to convert the message charset");
     */
        /* JADX WARNING: Missing block: B:26:0x0073, code skipped:
            r2 = new java.lang.String(r19.getRequest().getRawContent());
     */
        public boolean processRequest(org.jitsi.javax.sip.RequestEvent r19) {
            /*
            r18 = this;
            r0 = r18;
            r15 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r16 = r15.messageProcessors;
            monitor-enter(r16);
            r0 = r18;
            r15 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;	 Catch:{ all -> 0x005c }
            r15 = r15.messageProcessors;	 Catch:{ all -> 0x005c }
            r9 = r15.iterator();	 Catch:{ all -> 0x005c }
        L_0x0015:
            r15 = r9.hasNext();	 Catch:{ all -> 0x005c }
            if (r15 == 0) goto L_0x002c;
        L_0x001b:
            r10 = r9.next();	 Catch:{ all -> 0x005c }
            r10 = (net.java.sip.communicator.impl.protocol.sip.SipMessageProcessor) r10;	 Catch:{ all -> 0x005c }
            r0 = r19;
            r15 = r10.processMessage(r0);	 Catch:{ all -> 0x005c }
            if (r15 != 0) goto L_0x0015;
        L_0x0029:
            r15 = 1;
            monitor-exit(r16);	 Catch:{ all -> 0x005c }
        L_0x002b:
            return r15;
        L_0x002c:
            monitor-exit(r16);	 Catch:{ all -> 0x005c }
            r2 = 0;
            r14 = r19.getRequest();
            r2 = new java.lang.String;	 Catch:{ UnsupportedEncodingException -> 0x005f }
            r15 = r14.getRawContent();	 Catch:{ UnsupportedEncodingException -> 0x005f }
            r0 = r18;
            r16 = r0.getCharset(r14);	 Catch:{ UnsupportedEncodingException -> 0x005f }
            r0 = r16;
            r2.<init>(r15, r0);	 Catch:{ UnsupportedEncodingException -> 0x005f }
        L_0x0043:
            r15 = r19.getRequest();
            r16 = "From";
            r8 = r15.getHeader(r16);
            r8 = (org.jitsi.javax.sip.header.FromHeader) r8;
            if (r8 != 0) goto L_0x0081;
        L_0x0051:
            r15 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r16 = "received a request without a from header";
            r15.error(r16);
            r15 = 0;
            goto L_0x002b;
        L_0x005c:
            r15 = move-exception;
            monitor-exit(r16);	 Catch:{ all -> 0x005c }
            throw r15;
        L_0x005f:
            r5 = move-exception;
            r15 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r15 = r15.isDebugEnabled();
            if (r15 == 0) goto L_0x0073;
        L_0x006a:
            r15 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r16 = "failed to convert the message charset";
            r15.debug(r16);
        L_0x0073:
            r2 = new java.lang.String;
            r15 = r19.getRequest();
            r15 = r15.getRawContent();
            r2.<init>(r15);
            goto L_0x0043;
        L_0x0081:
            r0 = r18;
            r15 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r15 = r15.opSetPersPresence;
            r16 = r8.getAddress();
            r16 = r16.getURI();
            r16 = r16.toString();
            r7 = r15.resolveContactID(r16);
            r15 = "Content-Type";
            r3 = r14.getHeader(r15);
            r3 = (org.jitsi.javax.sip.header.ContentTypeHeader) r3;
            r4 = 0;
            r1 = 0;
            if (r3 != 0) goto L_0x0148;
        L_0x00a5:
            r4 = "text/plain";
        L_0x00a7:
            if (r1 != 0) goto L_0x00ab;
        L_0x00a9:
            r1 = "UTF-8";
        L_0x00ab:
            r0 = r18;
            r15 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r16 = 0;
            r0 = r16;
            r12 = r15.createMessage(r2, r4, r1, r0);
            if (r7 != 0) goto L_0x0117;
        L_0x00b9:
            r15 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r15 = r15.isDebugEnabled();
            if (r15 == 0) goto L_0x00e9;
        L_0x00c3:
            r15 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r16 = new java.lang.StringBuilder;
            r16.<init>();
            r17 = "received a message from an unknown contact: ";
            r16 = r16.append(r17);
            r17 = r8.getAddress();
            r17 = r17.getURI();
            r17 = r17.toString();
            r16 = r16.append(r17);
            r16 = r16.toString();
            r15.debug(r16);
        L_0x00e9:
            r15 = r8.getAddress();
            r15 = r15.getDisplayName();
            if (r15 == 0) goto L_0x016f;
        L_0x00f3:
            r0 = r18;
            r15 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r15 = r15.opSetPersPresence;
            r16 = r8.getAddress();
            r16 = r16.getURI();
            r16 = r16.toString();
            r17 = r8.getAddress();
            r17 = r17.getDisplayName();
            r17 = r17.toString();
            r7 = r15.createVolatileContact(r16, r17);
        L_0x0117:
            r0 = r18;
            r15 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;	 Catch:{ ParseException -> 0x0188, SipException -> 0x0195, InvalidArgumentException -> 0x01b8 }
            r15 = r15.sipProvider;	 Catch:{ ParseException -> 0x0188, SipException -> 0x0195, InvalidArgumentException -> 0x01b8 }
            r15 = r15.getMessageFactory();	 Catch:{ ParseException -> 0x0188, SipException -> 0x0195, InvalidArgumentException -> 0x01b8 }
            r16 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
            r17 = r19.getRequest();	 Catch:{ ParseException -> 0x0188, SipException -> 0x0195, InvalidArgumentException -> 0x01b8 }
            r13 = r15.createResponse(r16, r17);	 Catch:{ ParseException -> 0x0188, SipException -> 0x0195, InvalidArgumentException -> 0x01b8 }
            r15 = net.java.sip.communicator.impl.protocol.sip.SipStackSharing.getOrCreateServerTransaction(r19);	 Catch:{ ParseException -> 0x0188, SipException -> 0x0195, InvalidArgumentException -> 0x01b8 }
            r15.sendResponse(r13);	 Catch:{ ParseException -> 0x0188, SipException -> 0x0195, InvalidArgumentException -> 0x01b8 }
        L_0x0134:
            r11 = new net.java.sip.communicator.service.protocol.event.MessageReceivedEvent;
            r15 = new java.util.Date;
            r15.<init>();
            r11.<init>(r12, r7, r15);
            r0 = r18;
            r15 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r15.fireMessageEvent(r11);
            r15 = 1;
            goto L_0x002b;
        L_0x0148:
            r15 = new java.lang.StringBuilder;
            r15.<init>();
            r16 = r3.getContentType();
            r15 = r15.append(r16);
            r16 = "/";
            r15 = r15.append(r16);
            r16 = r3.getContentSubType();
            r15 = r15.append(r16);
            r4 = r15.toString();
            r15 = "charset";
            r1 = r3.getParameter(r15);
            goto L_0x00a7;
        L_0x016f:
            r0 = r18;
            r15 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r15 = r15.opSetPersPresence;
            r16 = r8.getAddress();
            r16 = r16.getURI();
            r16 = r16.toString();
            r7 = r15.createVolatileContact(r16);
            goto L_0x0117;
        L_0x0188:
            r6 = move-exception;
            r15 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r16 = "failed to build the response";
            r0 = r16;
            r15.error(r0, r6);
            goto L_0x0134;
        L_0x0195:
            r6 = move-exception;
            r15 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r16 = new java.lang.StringBuilder;
            r16.<init>();
            r17 = "failed to send the response : ";
            r16 = r16.append(r17);
            r17 = r6.getMessage();
            r16 = r16.append(r17);
            r16 = r16.toString();
            r0 = r16;
            r15.error(r0, r6);
            goto L_0x0134;
        L_0x01b8:
            r6 = move-exception;
            r15 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r15 = r15.isDebugEnabled();
            if (r15 == 0) goto L_0x0134;
        L_0x01c3:
            r15 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r16 = new java.lang.StringBuilder;
            r16.<init>();
            r17 = "Invalid argument for createResponse : ";
            r16 = r16.append(r17);
            r17 = r6.getMessage();
            r16 = r16.append(r17);
            r16 = r16.toString();
            r0 = r16;
            r15.debug(r0, r6);
            goto L_0x0134;
            */
            throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl$BasicInstantMessagingMethodProcessor.processRequest(org.jitsi.javax.sip.RequestEvent):boolean");
        }

        /* JADX WARNING: Missing block: B:12:0x0035, code skipped:
            r17 = r23.getClientTransaction().getRequest();
            r19 = r23.getResponse().getStatusCode();
     */
        /* JADX WARNING: Missing block: B:14:?, code skipped:
            r10 = new java.lang.String(r17.getRawContent(), getCharset(r17));
     */
        /* JADX WARNING: Missing block: B:22:0x0071, code skipped:
            r12 = move-exception;
     */
        /* JADX WARNING: Missing block: B:24:0x007a, code skipped:
            if (net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.access$200().isDebugEnabled() != false) goto L_0x007c;
     */
        /* JADX WARNING: Missing block: B:25:0x007c, code skipped:
            net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.access$200().debug("failed to convert the message charset", r12);
     */
        /* JADX WARNING: Missing block: B:26:0x0085, code skipped:
            r10 = new java.lang.String(r17.getRawContent());
     */
        public boolean processResponse(org.jitsi.javax.sip.ResponseEvent r23) {
            /*
            r22 = this;
            r0 = r22;
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r6 = r5.messageProcessors;
            monitor-enter(r6);
            r0 = r22;
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;	 Catch:{ all -> 0x006e }
            r5 = r5.messageProcessors;	 Catch:{ all -> 0x006e }
            r13 = r5.iterator();	 Catch:{ all -> 0x006e }
        L_0x0015:
            r5 = r13.hasNext();	 Catch:{ all -> 0x006e }
            if (r5 == 0) goto L_0x0034;
        L_0x001b:
            r15 = r13.next();	 Catch:{ all -> 0x006e }
            r15 = (net.java.sip.communicator.impl.protocol.sip.SipMessageProcessor) r15;	 Catch:{ all -> 0x006e }
            r0 = r22;
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;	 Catch:{ all -> 0x006e }
            r5 = r5.sentMsg;	 Catch:{ all -> 0x006e }
            r0 = r23;
            r5 = r15.processResponse(r0, r5);	 Catch:{ all -> 0x006e }
            if (r5 != 0) goto L_0x0015;
        L_0x0031:
            r5 = 1;
            monitor-exit(r6);	 Catch:{ all -> 0x006e }
        L_0x0033:
            return r5;
        L_0x0034:
            monitor-exit(r6);	 Catch:{ all -> 0x006e }
            r5 = r23.getClientTransaction();
            r17 = r5.getRequest();
            r5 = r23.getResponse();
            r19 = r5.getStatusCode();
            r10 = 0;
            r10 = new java.lang.String;	 Catch:{ UnsupportedEncodingException -> 0x0071 }
            r5 = r17.getRawContent();	 Catch:{ UnsupportedEncodingException -> 0x0071 }
            r0 = r22;
            r1 = r17;
            r6 = r0.getCharset(r1);	 Catch:{ UnsupportedEncodingException -> 0x0071 }
            r10.<init>(r5, r6);	 Catch:{ UnsupportedEncodingException -> 0x0071 }
        L_0x0057:
            r5 = "To";
            r0 = r17;
            r20 = r0.getHeader(r5);
            r20 = (org.jitsi.javax.sip.header.ToHeader) r20;
            if (r20 != 0) goto L_0x008f;
        L_0x0063:
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r6 = "send a request without a to header";
            r5.error(r6);
            r5 = 0;
            goto L_0x0033;
        L_0x006e:
            r5 = move-exception;
            monitor-exit(r6);	 Catch:{ all -> 0x006e }
            throw r5;
        L_0x0071:
            r12 = move-exception;
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r5 = r5.isDebugEnabled();
            if (r5 == 0) goto L_0x0085;
        L_0x007c:
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r6 = "failed to convert the message charset";
            r5.debug(r6, r12);
        L_0x0085:
            r10 = new java.lang.String;
            r5 = r17.getRawContent();
            r10.<init>(r5);
            goto L_0x0057;
        L_0x008f:
            r0 = r22;
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r5 = r5.opSetPersPresence;
            r6 = r20.getAddress();
            r6 = r6.getURI();
            r6 = r6.toString();
            r4 = r5.resolveContactID(r6);
            if (r4 != 0) goto L_0x0106;
        L_0x00a9:
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r6 = new java.lang.StringBuilder;
            r6.<init>();
            r7 = "Error received a response from an unknown contact : ";
            r6 = r6.append(r7);
            r7 = r20.getAddress();
            r7 = r7.getURI();
            r7 = r7.toString();
            r6 = r6.append(r7);
            r7 = " : ";
            r6 = r6.append(r7);
            r7 = r23.getResponse();
            r7 = r7.getStatusCode();
            r6 = r6.append(r7);
            r7 = " ";
            r6 = r6.append(r7);
            r7 = r23.getResponse();
            r7 = r7.getReasonPhrase();
            r6 = r6.append(r7);
            r6 = r6.toString();
            r5.error(r6);
            r0 = r22;
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r0 = r22;
            r6 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r6 = r6.createMessage(r10);
            r7 = 4;
            r5.fireMessageDeliveryFailed(r6, r4, r7);
            r5 = 0;
            goto L_0x0033;
        L_0x0106:
            r5 = "Call-ID";
            r0 = r17;
            r5 = r0.getHeader(r5);
            r5 = (org.jitsi.javax.sip.header.CallIdHeader) r5;
            r14 = r5.getCallId();
            r0 = r22;
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r5 = r5.sentMsg;
            r3 = r5.get(r14);
            r3 = (net.java.sip.communicator.service.protocol.Message) r3;
            if (r3 != 0) goto L_0x0140;
        L_0x0124:
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r6 = "Couldn't find the message sent";
            r5.error(r6);
            r0 = r22;
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r0 = r22;
            r6 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r6 = r6.createMessage(r10);
            r7 = 4;
            r5.fireMessageDeliveryFailed(r6, r4, r7);
            r5 = 1;
            goto L_0x0033;
        L_0x0140:
            r5 = 400; // 0x190 float:5.6E-43 double:1.976E-321;
            r0 = r19;
            if (r0 < r5) goto L_0x01d6;
        L_0x0146:
            r5 = 401; // 0x191 float:5.62E-43 double:1.98E-321;
            r0 = r19;
            if (r0 == r5) goto L_0x01d6;
        L_0x014c:
            r5 = 407; // 0x197 float:5.7E-43 double:2.01E-321;
            r0 = r19;
            if (r0 == r5) goto L_0x01d6;
        L_0x0152:
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r5 = r5.isInfoEnabled();
            if (r5 == 0) goto L_0x018a;
        L_0x015c:
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r6 = new java.lang.StringBuilder;
            r6.<init>();
            r7 = r23.getResponse();
            r7 = r7.getStatusCode();
            r6 = r6.append(r7);
            r7 = " ";
            r6 = r6.append(r7);
            r7 = r23.getResponse();
            r7 = r7.getReasonPhrase();
            r6 = r6.append(r7);
            r6 = r6.toString();
            r5.info(r6);
        L_0x018a:
            r2 = new net.java.sip.communicator.service.protocol.event.MessageDeliveryFailedEvent;
            r5 = 2;
            r6 = java.lang.System.currentTimeMillis();
            r8 = new java.lang.StringBuilder;
            r8.<init>();
            r21 = r23.getResponse();
            r21 = r21.getStatusCode();
            r0 = r21;
            r8 = r8.append(r0);
            r21 = " ";
            r0 = r21;
            r8 = r8.append(r0);
            r21 = r23.getResponse();
            r21 = r21.getReasonPhrase();
            r0 = r21;
            r8 = r8.append(r0);
            r8 = r8.toString();
            r2.<init>(r3, r4, r5, r6, r8);
            r0 = r22;
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r5.fireMessageEvent(r2);
            r0 = r22;
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r5 = r5.sentMsg;
            r5.remove(r14);
        L_0x01d3:
            r5 = 1;
            goto L_0x0033;
        L_0x01d6:
            r5 = 401; // 0x191 float:5.62E-43 double:1.98E-321;
            r0 = r19;
            if (r0 == r5) goto L_0x01e2;
        L_0x01dc:
            r5 = 407; // 0x197 float:5.7E-43 double:2.01E-321;
            r0 = r19;
            if (r0 != r5) goto L_0x0262;
        L_0x01e2:
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r5 = r5.isDebugEnabled();
            if (r5 == 0) goto L_0x0220;
        L_0x01ec:
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r6 = new java.lang.StringBuilder;
            r6.<init>();
            r7 = "proxy asks authentication : ";
            r6 = r6.append(r7);
            r7 = r23.getResponse();
            r7 = r7.getStatusCode();
            r6 = r6.append(r7);
            r7 = " ";
            r6 = r6.append(r7);
            r7 = r23.getResponse();
            r7 = r7.getReasonPhrase();
            r6 = r6.append(r7);
            r6 = r6.toString();
            r5.debug(r6);
        L_0x0220:
            r9 = r23.getClientTransaction();
            r18 = r23.getSource();
            r18 = (org.jitsi.javax.sip.SipProvider) r18;
            r5 = r23.getResponse();	 Catch:{ OperationFailedException -> 0x0236 }
            r0 = r22;
            r1 = r18;
            r0.processAuthenticationChallenge(r9, r5, r1);	 Catch:{ OperationFailedException -> 0x0236 }
            goto L_0x01d3;
        L_0x0236:
            r11 = move-exception;
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r6 = "can't solve the challenge";
            r5.error(r6, r11);
            r2 = new net.java.sip.communicator.service.protocol.event.MessageDeliveryFailedEvent;
            r5 = 2;
            r6 = java.lang.System.currentTimeMillis();
            r8 = r11.getMessage();
            r2.<init>(r3, r4, r5, r6, r8);
            r0 = r22;
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r5.fireMessageEvent(r2);
            r0 = r22;
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r5 = r5.sentMsg;
            r5.remove(r14);
            goto L_0x01d3;
        L_0x0262:
            r5 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
            r0 = r19;
            if (r0 < r5) goto L_0x01d3;
        L_0x0268:
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r5 = r5.isDebugEnabled();
            if (r5 == 0) goto L_0x02a6;
        L_0x0272:
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.logger;
            r6 = new java.lang.StringBuilder;
            r6.<init>();
            r7 = "Ack received from the network : ";
            r6 = r6.append(r7);
            r7 = r23.getResponse();
            r7 = r7.getStatusCode();
            r6 = r6.append(r7);
            r7 = " ";
            r6 = r6.append(r7);
            r7 = r23.getResponse();
            r7 = r7.getReasonPhrase();
            r6 = r6.append(r7);
            r6 = r6.toString();
            r5.debug(r6);
        L_0x02a6:
            r16 = new net.java.sip.communicator.service.protocol.event.MessageDeliveredEvent;
            r5 = new java.util.Date;
            r5.<init>();
            r0 = r16;
            r0.<init>(r3, r4, r5);
            r0 = r22;
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r0 = r16;
            r5.fireMessageEvent(r0);
            r0 = r22;
            r5 = net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl.this;
            r5 = r5.sentMsg;
            r5.remove(r14);
            goto L_0x01d3;
            */
            throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.impl.protocol.sip.OperationSetBasicInstantMessagingSipImpl$BasicInstantMessagingMethodProcessor.processResponse(org.jitsi.javax.sip.ResponseEvent):boolean");
        }

        private String getCharset(Request req) {
            String charset = null;
            Header contentTypeHeader = req.getHeader("Content-Type");
            if (contentTypeHeader instanceof ContentTypeHeader) {
                charset = ((ContentTypeHeader) contentTypeHeader).getParameter("charset");
            }
            if (charset == null) {
                return "UTF-8";
            }
            return charset;
        }

        private void processAuthenticationChallenge(ClientTransaction clientTransaction, Response response, SipProvider jainSipProvider) throws OperationFailedException {
            try {
                ClientTransaction retryTran;
                if (OperationSetBasicInstantMessagingSipImpl.logger.isDebugEnabled()) {
                    OperationSetBasicInstantMessagingSipImpl.logger.debug("Authenticating a message request.");
                }
                synchronized (this) {
                    retryTran = OperationSetBasicInstantMessagingSipImpl.this.sipProvider.getSipSecurityManager().handleChallenge(response, clientTransaction, jainSipProvider, OperationSetBasicInstantMessagingSipImpl.this.seqN = 1 + OperationSetBasicInstantMessagingSipImpl.this.seqN);
                }
                if (retryTran != null) {
                    retryTran.sendRequest();
                } else if (OperationSetBasicInstantMessagingSipImpl.logger.isTraceEnabled()) {
                    OperationSetBasicInstantMessagingSipImpl.logger.trace("No password supplied or error occured!");
                }
            } catch (Exception exc) {
                OperationSetBasicInstantMessagingSipImpl.logger.error("We failed to authenticate a message request.", exc);
                throw new OperationFailedException("Failed to authenticatea message request", 4, exc);
            }
        }
    }

    private class RegistrationStateListener implements RegistrationStateChangeListener {
        private RegistrationStateListener() {
        }

        public void registrationStateChanged(RegistrationStateChangeEvent evt) {
            if (OperationSetBasicInstantMessagingSipImpl.logger.isDebugEnabled()) {
                OperationSetBasicInstantMessagingSipImpl.logger.debug("The provider changed state from: " + evt.getOldState() + " to: " + evt.getNewState());
            }
            if (evt.getNewState() == RegistrationState.REGISTERED) {
                OperationSetBasicInstantMessagingSipImpl.this.opSetPersPresence = (OperationSetPresenceSipImpl) OperationSetBasicInstantMessagingSipImpl.this.sipProvider.getOperationSet(OperationSetPersistentPresence.class);
            }
        }
    }

    OperationSetBasicInstantMessagingSipImpl(ProtocolProviderServiceSipImpl provider) {
        this.sipProvider = provider;
        this.registrationListener = new RegistrationStateListener();
        provider.addRegistrationStateChangeListener(this.registrationListener);
        this.offlineMessageSupported = provider.getAccountID().getAccountPropertyBoolean("OFFLINE_MSG_SUPPORTED", false);
        this.sipProvider.registerMethodProcessor("MESSAGE", new BasicInstantMessagingMethodProcessor());
        this.sipStatusEnum = this.sipProvider.getSipStatusEnum();
    }

    /* access modifiers changed from: 0000 */
    public void addMessageProcessor(SipMessageProcessor processor) {
        synchronized (this.messageProcessors) {
            if (!this.messageProcessors.contains(processor)) {
                this.messageProcessors.add(processor);
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void removeMessageProcessor(SipMessageProcessor processor) {
        synchronized (this.messageProcessors) {
            this.messageProcessors.remove(processor);
        }
    }

    public Message createMessage(String content, String contentType, String encoding, String subject) {
        return new MessageSipImpl(content, contentType, encoding, subject);
    }

    public boolean isOfflineMessagingSupported() {
        return this.offlineMessageSupported;
    }

    public boolean isContentTypeSupported(String contentType) {
        if (contentType.equals("text/plain") || contentType.equals("text/html")) {
            return true;
        }
        return false;
    }

    public void sendInstantMessage(Contact to, Message message) throws IllegalStateException, IllegalArgumentException {
        if (to instanceof ContactSipImpl) {
            assertConnected();
            if (!to.getPresenceStatus().equals(this.sipStatusEnum.getStatus(SipStatusEnum.OFFLINE)) || this.offlineMessageSupported) {
                try {
                    try {
                        sendMessageRequest(createMessageRequest(to, transformSIPMessage(to, message)), to, message);
                        return;
                    } catch (TransactionUnavailableException ex) {
                        logger.error("Failed to create messageTransaction.\nThis is most probably a network connection error.", ex);
                        fireMessageDeliveryFailed(message, to, 2);
                        return;
                    } catch (SipException ex2) {
                        logger.error("Failed to send the message.", ex2);
                        fireMessageDeliveryFailed(message, to, 4);
                        return;
                    }
                } catch (OperationFailedException ex3) {
                    logger.error("Failed to create the message.", ex3);
                    fireMessageDeliveryFailed(message, to, 4);
                    return;
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("trying to send a message to an offline contact");
            }
            fireMessageDeliveryFailed(message, to, 5);
            return;
        }
        throw new IllegalArgumentException("The specified contact is not a Sip contact." + to);
    }

    /* access modifiers changed from: 0000 */
    public void sendMessageRequest(Request messageRequest, Contact to, Message messageContent) throws TransactionUnavailableException, SipException {
        this.sipProvider.getDefaultJainSipProvider().getNewClientTransaction(messageRequest).sendRequest();
        this.sentMsg.put(((CallIdHeader) messageRequest.getHeader("Call-ID")).getCallId(), messageContent);
    }

    /* access modifiers changed from: 0000 */
    public Request createMessageRequest(Contact to, Message message) throws OperationFailedException {
        try {
            Address toAddress = this.sipProvider.parseAddressString(to.getAddress());
            CallIdHeader callIdHeader = this.sipProvider.getDefaultJainSipProvider().getNewCallId();
            try {
                CSeqHeader cSeqHeader;
                synchronized (this) {
                    HeaderFactory headerFactory = this.sipProvider.getHeaderFactory();
                    long j = this.seqN;
                    this.seqN = 1 + j;
                    cSeqHeader = headerFactory.createCSeqHeader(j, "MESSAGE");
                }
                try {
                    FromHeader fromHeader = this.sipProvider.getHeaderFactory().createFromHeader(this.sipProvider.getOurSipAddress(toAddress), SipMessageFactory.generateLocalTag());
                    ToHeader toHeader = this.sipProvider.getHeaderFactory().createToHeader(toAddress, null);
                    List viaHeaders = this.sipProvider.getLocalViaHeaders(toAddress);
                    MaxForwardsHeader maxForwards = this.sipProvider.getMaxForwardsHeader();
                    try {
                        ContentTypeHeader contTypeHeader = this.sipProvider.getHeaderFactory().createContentTypeHeader(getType(message), getSubType(message));
                        if (!"UTF-8".equalsIgnoreCase(message.getEncoding())) {
                            contTypeHeader.setParameter("charset", message.getEncoding());
                        }
                        ContentLengthHeader contLengthHeader = this.sipProvider.getHeaderFactory().createContentLengthHeader(message.getSize());
                        try {
                            Request req = this.sipProvider.getMessageFactory().createRequest(toHeader.getAddress().getURI(), "MESSAGE", callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards, contTypeHeader, message.getRawData());
                            req.addHeader(contLengthHeader);
                            return req;
                        } catch (ParseException ex) {
                            logger.error("Failed to create message Request!", ex);
                            throw new OperationFailedException("Failed to create message Request!", 4, ex);
                        }
                    } catch (ParseException ex2) {
                        logger.error("An unexpected error occurred whileconstructing the content headers", ex2);
                        throw new OperationFailedException("An unexpected error occurred whileconstructing the content headers", 4, ex2);
                    } catch (InvalidArgumentException exc) {
                        logger.error("An unexpected error occurred whileconstructing the content length header", exc);
                        throw new OperationFailedException("An unexpected error occurred whileconstructing the content length header", 4, exc);
                    }
                } catch (ParseException ex22) {
                    logger.error("An unexpected error occurred whileconstructing the FromHeader or ToHeader", ex22);
                    throw new OperationFailedException("An unexpected error occurred whileconstructing the FromHeader or ToHeader", 4, ex22);
                }
            } catch (InvalidArgumentException ex222) {
                logger.error("An unexpected error occurred whileconstructing the CSeqHeadder", ex222);
                throw new OperationFailedException("An unexpected error occurred whileconstructing the CSeqHeadder", 4, ex222);
            } catch (ParseException exc2) {
                logger.error("An unexpected error occurred whileconstructing the CSeqHeadder", exc2);
                throw new OperationFailedException("An unexpected error occurred whileconstructing the CSeqHeadder", 4, exc2);
            }
        } catch (ParseException exc22) {
            logger.error("An unexpected error occurred whileconstructing the address", exc22);
            throw new OperationFailedException("An unexpected error occurred whileconstructing the address", 4, exc22);
        }
    }

    private Message transformSIPMessage(Contact to, Message message) {
        MessageDeliveredEvent msgDeliveryPendingEvt = messageDeliveryPendingTransform(new MessageDeliveredEvent(message, to));
        if (msgDeliveryPendingEvt == null) {
            return null;
        }
        return ((OperationSetBasicInstantMessaging) this.sipProvider.getSupportedOperationSets().get(OperationSetBasicInstantMessaging.class.getName())).createMessage(msgDeliveryPendingEvt.getSourceMessage().getContent(), message.getContentType(), message.getEncoding(), message.getSubject());
    }

    private String getType(Message msg) {
        String type = msg.getContentType();
        return type.substring(0, type.indexOf(47));
    }

    private String getSubType(Message msg) {
        String subtype = msg.getContentType();
        return subtype.substring(subtype.indexOf(47) + 1);
    }

    private void assertConnected() throws IllegalStateException {
        if (this.sipProvider == null) {
            throw new IllegalStateException("The provider must be non-null and signed on the service before being able to communicate.");
        } else if (!this.sipProvider.isRegistered()) {
            throw new IllegalStateException("The provider must be signed on the service before being able to communicate.");
        }
    }

    /* access modifiers changed from: 0000 */
    public void shutdown() {
        this.sipProvider.removeRegistrationStateChangeListener(this.registrationListener);
    }
}
