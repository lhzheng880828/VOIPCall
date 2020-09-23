package net.java.sip.communicator.impl.protocol.jabber;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketException;
import org.ice4j.TransportAddress;
import org.ice4j.ice.CandidateExtendedType;
import org.ice4j.ice.CandidateType;
import org.ice4j.ice.Component;
import org.ice4j.ice.LocalCandidate;
import org.ice4j.socket.IceSocketWrapper;
import org.ice4j.socket.IceUdpSocketWrapper;
import org.ice4j.socket.MultiplexingDatagramSocket;

public class JingleNodesCandidate extends LocalCandidate {
    private JingleNodesCandidateDatagramSocket jingleNodesCandidateDatagramSocket = null;
    private TransportAddress localEndPoint = null;
    private IceSocketWrapper socket = null;

    public JingleNodesCandidate(TransportAddress transportAddress, Component parentComponent, TransportAddress localEndPoint) {
        super(transportAddress, parentComponent, CandidateType.RELAYED_CANDIDATE, CandidateExtendedType.JINGLE_NODE_CANDIDATE, null);
        setBase(this);
        setRelayServerAddress(localEndPoint);
        this.localEndPoint = localEndPoint;
    }

    public synchronized JingleNodesCandidateDatagramSocket getRelayedCandidateDatagramSocket() {
        if (this.jingleNodesCandidateDatagramSocket == null) {
            try {
                this.jingleNodesCandidateDatagramSocket = new JingleNodesCandidateDatagramSocket(this, this.localEndPoint);
            } catch (SocketException sex) {
                throw new UndeclaredThrowableException(sex);
            }
        }
        return this.jingleNodesCandidateDatagramSocket;
    }

    public IceSocketWrapper getIceSocketWrapper() {
        if (this.socket == null) {
            try {
                this.socket = new IceUdpSocketWrapper(new MultiplexingDatagramSocket(getRelayedCandidateDatagramSocket()));
            } catch (SocketException sex) {
                throw new UndeclaredThrowableException(sex);
            }
        }
        return this.socket;
    }
}
