package org.jitsi.gov.nist.javax.sip.stack;

import java.io.IOException;
import java.net.InetAddress;
import org.jitsi.javax.sip.ListeningPoint;

public class OIOMessageProcessorFactory implements MessageProcessorFactory {
    public MessageProcessor createMessageProcessor(SIPTransactionStack sipStack, InetAddress ipAddress, int port, String transport) throws IOException {
        if (transport.equalsIgnoreCase(ListeningPoint.UDP)) {
            UDPMessageProcessor udpMessageProcessor = new UDPMessageProcessor(ipAddress, sipStack, port);
            sipStack.udpFlag = true;
            return udpMessageProcessor;
        } else if (transport.equalsIgnoreCase(ListeningPoint.TCP)) {
            return new TCPMessageProcessor(ipAddress, sipStack, port);
        } else {
            if (transport.equalsIgnoreCase(ListeningPoint.TLS)) {
                return new TLSMessageProcessor(ipAddress, sipStack, port);
            }
            if (transport.equalsIgnoreCase(ListeningPoint.SCTP)) {
                try {
                    MessageProcessor mp = (MessageProcessor) ClassLoader.getSystemClassLoader().loadClass("org.jitsi.gov.nist.javax.sip.stack.sctp.SCTPMessageProcessor").newInstance();
                    mp.initialize(ipAddress, port, sipStack);
                    return mp;
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("SCTP not supported (needs Java 7 and SCTP jar in classpath)");
                } catch (InstantiationException ie) {
                    throw new IllegalArgumentException("Error initializing SCTP", ie);
                } catch (IllegalAccessException ie2) {
                    throw new IllegalArgumentException("Error initializing SCTP", ie2);
                }
            }
            throw new IllegalArgumentException("bad transport");
        }
    }
}
