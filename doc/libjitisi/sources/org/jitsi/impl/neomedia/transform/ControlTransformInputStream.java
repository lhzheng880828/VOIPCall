package org.jitsi.impl.neomedia.transform;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.List;
import org.jitsi.impl.neomedia.RTCPConnectorInputStream;
import org.jitsi.service.neomedia.event.RTCPFeedbackListener;

public class ControlTransformInputStream extends TransformUDPInputStream {
    private final List<RTCPFeedbackListener> listeners = new LinkedList();

    public ControlTransformInputStream(DatagramSocket socket) {
        super(socket);
    }

    public void addRTCPFeedbackListener(RTCPFeedbackListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        synchronized (this.listeners) {
            if (!this.listeners.contains(listener)) {
                this.listeners.add(listener);
            }
        }
    }

    public void removeRTCPFeedbackListener(RTCPFeedbackListener listener) {
        if (listener != null) {
            synchronized (this.listeners) {
                this.listeners.remove(listener);
            }
        }
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        int pktLength = super.read(buffer, offset, length);
        RTCPConnectorInputStream.fireRTCPFeedbackReceived(this, buffer, offset, pktLength, this.listeners);
        return pktLength;
    }
}
