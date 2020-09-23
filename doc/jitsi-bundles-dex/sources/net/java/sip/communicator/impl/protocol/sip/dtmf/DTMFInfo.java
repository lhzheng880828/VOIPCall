package net.java.sip.communicator.impl.protocol.sip.dtmf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import net.java.sip.communicator.impl.protocol.sip.CallPeerSipImpl;
import net.java.sip.communicator.impl.protocol.sip.MethodProcessorAdapter;
import net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl;
import net.java.sip.communicator.impl.protocol.sip.SipStackSharing;
import net.java.sip.communicator.service.protocol.CallPeer;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.event.DTMFListener;
import net.java.sip.communicator.service.protocol.event.DTMFReceivedEvent;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.header.ContentLength;
import org.jitsi.gov.nist.javax.sip.header.ContentType;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.DialogState;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.TransactionUnavailableException;
import org.jitsi.javax.sip.header.ContentTypeHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;
import org.jitsi.service.protocol.DTMFTone;

public class DTMFInfo extends MethodProcessorAdapter {
    private static final String CONTENT_SUB_TYPE = "dtmf-relay";
    private static final String CONTENT_TYPE = "application";
    private static final Logger logger = Logger.getLogger(DTMFInfo.class);
    private Hashtable<CallPeer, Object[]> currentlyTransmittingTones = new Hashtable();
    private final List<DTMFListener> dtmfListeners = new LinkedList();
    private final ProtocolProviderServiceSipImpl pps;

    public DTMFInfo(ProtocolProviderServiceSipImpl pps) {
        this.pps = pps;
        this.pps.registerMethodProcessor(Request.INFO, this);
    }

    public void startSendingDTMF(CallPeerSipImpl callPeer, DTMFTone tone) throws OperationFailedException, NullPointerException, IllegalArgumentException {
        if (this.currentlyTransmittingTones.contains(callPeer)) {
            throw new IllegalStateException("Error starting dtmf tone, already started");
        }
        this.currentlyTransmittingTones.put(callPeer, new Object[]{tone, Long.valueOf(System.currentTimeMillis())});
    }

    public void stopSendingDTMF(CallPeerSipImpl callPeer) {
        Object[] toneInfo = (Object[]) this.currentlyTransmittingTones.remove(callPeer);
        if (toneInfo != null) {
            try {
                sayInfo(callPeer, (DTMFTone) toneInfo[0], System.currentTimeMillis() - ((Long) toneInfo[1]).longValue());
            } catch (OperationFailedException e) {
                logger.error("Error stoping dtmf ");
            }
        }
    }

    private void sayInfo(CallPeerSipImpl callPeer, DTMFTone dtmftone, long duration) throws OperationFailedException {
        Request info = this.pps.getMessageFactory().createRequest(callPeer.getDialog(), Request.INFO);
        ContentType ct = new ContentType("application", CONTENT_SUB_TYPE);
        String content = "Signal=" + dtmftone.getValue() + "\r\nDuration=" + duration + Separators.NEWLINE;
        info.setContentLength(new ContentLength(content.length()));
        try {
            info.setContent(content.getBytes(), ct);
            try {
                ClientTransaction clientTransaction = callPeer.getJainSipProvider().getNewClientTransaction(info);
                try {
                    if (callPeer.getDialog().getState() == DialogState.TERMINATED) {
                        logger.warn("Trying to send a dtmf tone inside a TERMINATED dialog.");
                        return;
                    }
                    callPeer.getDialog().sendRequest(clientTransaction);
                    if (logger.isDebugEnabled()) {
                        logger.debug("sent request:\n" + info);
                    }
                } catch (SipException ex) {
                    throw new OperationFailedException("Failed to send the INFO request", 2, ex);
                }
            } catch (TransactionUnavailableException ex2) {
                logger.error("Failed to construct a client transaction from the INFO request", ex2);
                throw new OperationFailedException("Failed to construct a client transaction from the INFO request", 4, ex2);
            }
        } catch (ParseException ex3) {
            logger.error("Failed to construct the INFO request", ex3);
            throw new OperationFailedException("Failed to construct a client the INFO request", 4, ex3);
        }
    }

    public boolean processResponse(ResponseEvent responseEvent) {
        boolean processed = false;
        if (responseEvent != null) {
            Response response = responseEvent.getResponse();
            if (response != null) {
                ClientTransaction clientTransaction = responseEvent.getClientTransaction();
                if (clientTransaction != null) {
                    Request request = clientTransaction.getRequest();
                    if (request != null) {
                        ContentTypeHeader contentTypeHeader = (ContentTypeHeader) request.getHeader("Content-Type");
                        if (contentTypeHeader != null && "application".equalsIgnoreCase(contentTypeHeader.getContentType()) && CONTENT_SUB_TYPE.equalsIgnoreCase(contentTypeHeader.getContentSubType())) {
                            processed = true;
                            int statusCode = response.getStatusCode();
                            if (statusCode != Response.OK) {
                                logger.error("DTMF send failed: " + statusCode);
                            } else if (logger.isDebugEnabled()) {
                                logger.debug("DTMF send succeeded: " + statusCode);
                            }
                        }
                    } else if (logger.isDebugEnabled()) {
                        logger.debug("null request");
                    }
                } else if (logger.isDebugEnabled()) {
                    logger.debug("null clientTransaction");
                }
            } else if (logger.isDebugEnabled()) {
                logger.debug("null response");
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("null responseEvent");
        }
        return processed;
    }

    public boolean processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ContentTypeHeader contentTypeHeader = (ContentTypeHeader) request.getHeader("Content-Type");
        if (contentTypeHeader == null || !"application".equalsIgnoreCase(contentTypeHeader.getContentType()) || !CONTENT_SUB_TYPE.equalsIgnoreCase(contentTypeHeader.getContentSubType())) {
            return false;
        }
        try {
            byte[] value;
            Object valueObj = request.getContent();
            if (valueObj instanceof String) {
                value = ((String) valueObj).getBytes("UTF-8");
            } else if (valueObj instanceof byte[]) {
                value = (byte[]) valueObj;
            } else {
                logger.error("Unknown content type");
                return false;
            }
            Properties prop = new Properties();
            prop.load(new ByteArrayInputStream(value));
            String signal = prop.getProperty("Signal");
            String durationStr = prop.getProperty("Duration");
            DTMFTone tone = DTMFTone.getDTMFTone(signal);
            if (tone == null) {
                logger.warn("Unknown tone received: " + tone);
                return false;
            }
            long duration = 0;
            try {
                duration = Long.parseLong(durationStr);
            } catch (NumberFormatException ex) {
                logger.warn("Error parsing duration:" + durationStr, ex);
            }
            fireToneEvent(tone, duration);
            try {
                try {
                    SipStackSharing.getOrCreateServerTransaction(requestEvent).sendResponse(this.pps.getMessageFactory().createResponse(Response.OK, requestEvent.getRequest()));
                    return true;
                } catch (TransactionUnavailableException ex2) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Failed to respond to an incoming transactionless INFO request");
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("Exception was:", ex2);
                    }
                    return false;
                } catch (InvalidArgumentException ex3) {
                    logger.warn("Failed to send OK for incoming INFO request", ex3);
                    return false;
                } catch (SipException ex4) {
                    logger.warn("Failed to send OK for incoming INFO request", ex4);
                    return false;
                }
            } catch (ParseException ex5) {
                logger.warn("Failed to create OK for incoming INFO request", ex5);
                return false;
            }
        } catch (IOException e) {
        }
    }

    private void fireToneEvent(DTMFTone tone, long duration) {
        Collection<DTMFListener> listeners;
        synchronized (this.dtmfListeners) {
            listeners = new ArrayList(this.dtmfListeners);
        }
        DTMFReceivedEvent evt = new DTMFReceivedEvent(this.pps, tone, duration);
        if (logger.isDebugEnabled()) {
            logger.debug("Dispatching DTMFTone Listeners=" + listeners.size() + " evt=" + evt);
        }
        try {
            for (DTMFListener listener : listeners) {
                listener.toneReceived(evt);
            }
        } catch (Throwable e) {
            logger.error("Error delivering dtmf tone", e);
        }
    }

    public void addDTMFListener(DTMFListener listener) {
        synchronized (this.dtmfListeners) {
            if (!this.dtmfListeners.contains(listener)) {
                this.dtmfListeners.add(listener);
            }
        }
    }

    public void removeDTMFListener(DTMFListener listener) {
        synchronized (this.dtmfListeners) {
            this.dtmfListeners.remove(listener);
        }
    }
}
