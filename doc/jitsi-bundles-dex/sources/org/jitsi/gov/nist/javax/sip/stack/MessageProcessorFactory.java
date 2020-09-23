package org.jitsi.gov.nist.javax.sip.stack;

import java.io.IOException;
import java.net.InetAddress;

public interface MessageProcessorFactory {
    MessageProcessor createMessageProcessor(SIPTransactionStack sIPTransactionStack, InetAddress inetAddress, int i, String str) throws IOException;
}
