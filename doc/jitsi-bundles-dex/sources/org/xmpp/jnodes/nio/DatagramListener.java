package org.xmpp.jnodes.nio;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public interface DatagramListener {
    void datagramReceived(SelDatagramChannel selDatagramChannel, ByteBuffer byteBuffer, SocketAddress socketAddress);
}
