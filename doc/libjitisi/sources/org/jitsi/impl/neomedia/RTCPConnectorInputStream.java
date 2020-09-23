package org.jitsi.impl.neomedia;

import com.lti.utils.UnsignedUtils;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import org.jitsi.service.neomedia.event.RTCPFeedbackEvent;
import org.jitsi.service.neomedia.event.RTCPFeedbackListener;

public class RTCPConnectorInputStream extends RTPConnectorUDPInputStream {
    private final List<RTCPFeedbackListener> listeners = new ArrayList();

    public RTCPConnectorInputStream(DatagramSocket socket) {
        super(socket);
    }

    public void addRTCPFeedbackListener(RTCPFeedbackListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener");
        } else if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public static void fireRTCPFeedbackReceived(Object source, byte[] buffer, int offset, int length, List<RTCPFeedbackListener> listeners) {
        if (length >= 12 && !listeners.isEmpty()) {
            int pt = buffer[offset + 1] & UnsignedUtils.MAX_UBYTE;
            if (pt == RTCPFeedbackEvent.PT_PS || pt == RTCPFeedbackEvent.PT_TL) {
                RTCPFeedbackEvent ev = new RTCPFeedbackEvent(source, buffer[offset] & 31, pt);
                for (RTCPFeedbackListener l : listeners) {
                    l.rtcpFeedbackReceived(ev);
                }
            }
        }
    }

    public void removeRTCPFeedbackListener(RTCPFeedbackListener listener) {
        this.listeners.remove(listener);
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        int pktLength = super.read(buffer, offset, length);
        fireRTCPFeedbackReceived(this, buffer, offset, pktLength, this.listeners);
        return pktLength;
    }
}
