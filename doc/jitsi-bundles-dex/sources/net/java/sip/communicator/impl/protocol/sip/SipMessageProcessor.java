package net.java.sip.communicator.impl.protocol.sip;

import java.util.Map;
import net.java.sip.communicator.service.protocol.Message;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.TimeoutEvent;

public interface SipMessageProcessor {
    boolean processMessage(RequestEvent requestEvent);

    boolean processResponse(ResponseEvent responseEvent, Map<String, Message> map);

    boolean processTimeout(TimeoutEvent timeoutEvent, Map<String, Message> map);
}
