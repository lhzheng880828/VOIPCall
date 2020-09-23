package net.java.sip.communicator.impl.protocol.jabber;

import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.CallConference;
import net.java.sip.communicator.service.protocol.CallPeer;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.ContactResource;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetResourceAwareTelephony;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.util.StringUtils;

public class OperationSetResAwareTelephonyJabberImpl implements OperationSetResourceAwareTelephony {
    private final OperationSetBasicTelephonyJabberImpl jabberTelephony;

    public OperationSetResAwareTelephonyJabberImpl(OperationSetBasicTelephonyJabberImpl basicTelephony) {
        this.jabberTelephony = basicTelephony;
    }

    public Call createCall(Contact callee, ContactResource calleeResource) throws OperationFailedException {
        return createCall(callee, calleeResource, null);
    }

    public Call createCall(String callee, String calleeResource) throws OperationFailedException {
        return createCall(callee, calleeResource, null);
    }

    public Call createCall(Contact callee, ContactResource calleeResource, CallConference conference) throws OperationFailedException {
        return createCall(callee.getAddress(), calleeResource.getResourceName(), conference);
    }

    public Call createCall(String uri, String calleeResource, CallConference conference) throws OperationFailedException {
        String fullCalleeUri;
        CallJabberImpl call = new CallJabberImpl(this.jabberTelephony);
        if (conference != null) {
            call.setConference(conference);
        }
        if (StringUtils.isNullOrEmpty(calleeResource)) {
            fullCalleeUri = uri;
        } else {
            fullCalleeUri = uri + Separators.SLASH + calleeResource;
        }
        CallPeer callPeer = this.jabberTelephony.createOutgoingCall(call, uri, fullCalleeUri, null);
        if (callPeer == null) {
            throw new OperationFailedException("Failed to create outgoing call because no peer was created", 4);
        }
        Call callOfCallPeer = callPeer.getCall();
        if (!(callOfCallPeer == call || conference == null)) {
            callOfCallPeer.setConference(conference);
        }
        return callOfCallPeer;
    }
}
