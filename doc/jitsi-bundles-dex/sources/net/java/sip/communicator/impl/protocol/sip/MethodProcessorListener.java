package net.java.sip.communicator.impl.protocol.sip;

import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public interface MethodProcessorListener {
    void requestProcessed(CallPeerSipImpl callPeerSipImpl, Request request, Response response);

    void responseProcessed(CallPeerSipImpl callPeerSipImpl, Response response, Request request);
}
