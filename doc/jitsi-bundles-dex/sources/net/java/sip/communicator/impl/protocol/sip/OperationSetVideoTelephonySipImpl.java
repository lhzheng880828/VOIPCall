package net.java.sip.communicator.impl.protocol.sip;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.CallPeer;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.media.AbstractOperationSetVideoTelephony;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.javax.sip.stack.SIPServerTransaction;
import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.ServerTransaction;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.header.ContentTypeHeader;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;
import org.jitsi.service.neomedia.QualityControl;
import org.jitsi.service.neomedia.QualityPreset;

public class OperationSetVideoTelephonySipImpl extends AbstractOperationSetVideoTelephony<OperationSetBasicTelephonySipImpl, ProtocolProviderServiceSipImpl, CallSipImpl, CallPeerSipImpl> {
    private static final Logger logger = Logger.getLogger(OperationSetVideoTelephonySipImpl.class);

    private class PictureFastUpdateMethodProcessor extends MethodProcessorAdapter {
        private PictureFastUpdateMethodProcessor() {
        }

        private boolean isPictureFastUpdate(Request request) {
            ContentTypeHeader contentTypeHeader = (ContentTypeHeader) request.getHeader("Content-Type");
            if (contentTypeHeader == null || !SIPServerTransaction.CONTENT_TYPE_APPLICATION.equalsIgnoreCase(contentTypeHeader.getContentType()) || !"media_control+xml".equalsIgnoreCase(contentTypeHeader.getContentSubType())) {
                return false;
            }
            Object content = request.getContent();
            if (content == null) {
                return false;
            }
            String xml;
            if (content instanceof String) {
                xml = content.toString();
            } else if (!(content instanceof byte[])) {
                return false;
            } else {
                byte[] bytes = (byte[]) content;
                try {
                    xml = new String(bytes, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    xml = new String(bytes);
                }
            }
            if (xml != null) {
                return xml.contains("picture_fast_update");
            }
            return false;
        }

        public boolean processRequest(RequestEvent requestEvent) {
            if (requestEvent == null) {
                return false;
            }
            Request request = requestEvent.getRequest();
            if (request == null) {
                return false;
            }
            if (!isPictureFastUpdate(request)) {
                return false;
            }
            try {
                ServerTransaction serverTransaction = SipStackSharing.getOrCreateServerTransaction(requestEvent);
                if (serverTransaction == null) {
                    return false;
                }
                CallPeerSipImpl callPeer = ((OperationSetBasicTelephonySipImpl) OperationSetVideoTelephonySipImpl.this.basicTelephony).getActiveCallsRepository().findCallPeer(serverTransaction.getDialog());
                if (callPeer == null) {
                    return false;
                }
                try {
                    return callPeer.processPictureFastUpdate(serverTransaction, request);
                } catch (OperationFailedException e) {
                    return false;
                }
            } catch (Exception e2) {
                e2.printStackTrace(System.err);
                return false;
            }
        }

        public boolean processResponse(ResponseEvent responseEvent) {
            if (responseEvent == null) {
                return false;
            }
            Response response = responseEvent.getResponse();
            if (response == null) {
                return false;
            }
            ClientTransaction clientTransaction = responseEvent.getClientTransaction();
            if (clientTransaction == null) {
                return false;
            }
            Request request = clientTransaction.getRequest();
            if (request == null) {
                return false;
            }
            if (!isPictureFastUpdate(request)) {
                return false;
            }
            CallPeerSipImpl callPeer = ((OperationSetBasicTelephonySipImpl) OperationSetVideoTelephonySipImpl.this.basicTelephony).getActiveCallsRepository().findCallPeer(clientTransaction.getDialog());
            if (callPeer == null) {
                return false;
            }
            callPeer.processPictureFastUpdate(clientTransaction, response);
            return true;
        }
    }

    public OperationSetVideoTelephonySipImpl(OperationSetBasicTelephonySipImpl basicTelephony) {
        super(basicTelephony);
        ((ProtocolProviderServiceSipImpl) this.parentProvider).registerMethodProcessor(Request.INFO, new PictureFastUpdateMethodProcessor());
    }

    public void setLocalVideoAllowed(Call call, boolean allowed) throws OperationFailedException {
        OperationSetVideoTelephonySipImpl.super.setLocalVideoAllowed(call, allowed);
        ((CallSipImpl) call).reInvite();
    }

    public Call createVideoCall(String uri) throws OperationFailedException, ParseException {
        return createVideoCall(uri, null);
    }

    public Call createVideoCall(Contact callee) throws OperationFailedException {
        return createVideoCall(callee, null);
    }

    public Call createVideoCall(String uri, QualityPreset qualityPreferences) throws OperationFailedException, ParseException {
        Address toAddress = ((ProtocolProviderServiceSipImpl) this.parentProvider).parseAddressString(uri);
        CallSipImpl call = ((OperationSetBasicTelephonySipImpl) this.basicTelephony).createOutgoingCall();
        call.setLocalVideoAllowed(true, getMediaUseCase());
        call.setInitialQualityPreferences(qualityPreferences);
        call.invite(toAddress, null);
        return call;
    }

    public Call createVideoCall(Contact callee, QualityPreset qualityPreferences) throws OperationFailedException {
        try {
            Address toAddress = ((ProtocolProviderServiceSipImpl) this.parentProvider).parseAddressString(callee.getAddress());
            CallSipImpl call = ((OperationSetBasicTelephonySipImpl) this.basicTelephony).createOutgoingCall();
            call.setLocalVideoAllowed(true, getMediaUseCase());
            call.setInitialQualityPreferences(qualityPreferences);
            call.invite(toAddress, null);
            return call;
        } catch (ParseException ex) {
            logger.error(ex.getMessage(), ex);
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public void answerVideoCallPeer(CallPeer peer) throws OperationFailedException {
        CallPeerSipImpl callPeer = (CallPeerSipImpl) peer;
        ((CallSipImpl) callPeer.getCall()).setLocalVideoAllowed(true, getMediaUseCase());
        callPeer.answer();
    }

    public QualityControl getQualityControl(CallPeer peer) {
        return ((CallPeerMediaHandlerSipImpl) ((CallPeerSipImpl) peer).getMediaHandler()).getQualityControl();
    }
}
