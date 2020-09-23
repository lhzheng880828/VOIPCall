package org.jitsi.impl.neomedia;

import java.io.IOException;
import java.util.Vector;
import javax.media.Format;
import javax.media.format.UnsupportedFormatException;
import javax.media.protocol.DataSource;
import javax.media.rtp.GlobalReceptionStats;
import javax.media.rtp.GlobalTransmissionStats;
import javax.media.rtp.RTPConnector;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.RemoteListener;
import javax.media.rtp.SendStream;
import javax.media.rtp.SendStreamListener;
import javax.media.rtp.SessionListener;
import net.sf.fmj.media.rtp.RTPSessionMgr;
import org.jitsi.service.neomedia.MediaStream;
import org.jitsi.service.neomedia.RTPTranslator;
import org.jitsi.service.neomedia.SSRCFactory;

public class StreamRTPManager {
    private final RTPManager manager;
    private final MediaStream stream;
    private final RTPTranslatorImpl translator;

    public StreamRTPManager(MediaStream stream, RTPTranslator translator) {
        this.stream = stream;
        this.translator = (RTPTranslatorImpl) translator;
        this.manager = this.translator == null ? RTPManager.newInstance() : null;
    }

    public void addFormat(Format format, int payloadType) {
        if (this.translator == null) {
            this.manager.addFormat(format, payloadType);
        } else {
            this.translator.addFormat(this, format, payloadType);
        }
    }

    public void addReceiveStreamListener(ReceiveStreamListener listener) {
        if (this.translator == null) {
            this.manager.addReceiveStreamListener(listener);
        } else {
            this.translator.addReceiveStreamListener(this, listener);
        }
    }

    public void addRemoteListener(RemoteListener listener) {
        if (this.translator == null) {
            this.manager.addRemoteListener(listener);
        } else {
            this.translator.addRemoteListener(this, listener);
        }
    }

    public void addSendStreamListener(SendStreamListener listener) {
        if (this.translator == null) {
            this.manager.addSendStreamListener(listener);
        } else {
            this.translator.addSendStreamListener(this, listener);
        }
    }

    public void addSessionListener(SessionListener listener) {
        if (this.translator == null) {
            this.manager.addSessionListener(listener);
        } else {
            this.translator.addSessionListener(this, listener);
        }
    }

    public SendStream createSendStream(DataSource dataSource, int streamIndex) throws IOException, UnsupportedFormatException {
        if (this.translator == null) {
            return this.manager.createSendStream(dataSource, streamIndex);
        }
        return this.translator.createSendStream(this, dataSource, streamIndex);
    }

    public void dispose() {
        if (this.translator == null) {
            this.manager.dispose();
        } else {
            this.translator.dispose(this);
        }
    }

    public <T> T getControl(Class<T> controlType) {
        return getControl(controlType.getName());
    }

    public Object getControl(String controlType) {
        if (this.translator == null) {
            return this.manager.getControl(controlType);
        }
        return this.translator.getControl(this, controlType);
    }

    public GlobalReceptionStats getGlobalReceptionStats() {
        if (this.translator == null) {
            return this.manager.getGlobalReceptionStats();
        }
        return this.translator.getGlobalReceptionStats(this);
    }

    public GlobalTransmissionStats getGlobalTransmissionStats() {
        if (this.translator == null) {
            return this.manager.getGlobalTransmissionStats();
        }
        return this.translator.getGlobalTransmissionStats(this);
    }

    public long getLocalSSRC() {
        if (this.translator == null) {
            return ((RTPSessionMgr) this.manager).getLocalSSRC();
        }
        return this.translator.getLocalSSRC(this);
    }

    public MediaStream getMediaStream() {
        return this.stream;
    }

    public Vector getReceiveStreams() {
        if (this.translator == null) {
            return this.manager.getReceiveStreams();
        }
        return this.translator.getReceiveStreams(this);
    }

    public Vector getSendStreams() {
        if (this.translator == null) {
            return this.manager.getSendStreams();
        }
        return this.translator.getSendStreams(this);
    }

    public void initialize(RTPConnector connector) {
        if (this.translator == null) {
            this.manager.initialize(connector);
        } else {
            this.translator.initialize(this, connector);
        }
    }

    public void removeReceiveStreamListener(ReceiveStreamListener listener) {
        if (this.translator == null) {
            this.manager.removeReceiveStreamListener(listener);
        } else {
            this.translator.removeReceiveStreamListener(this, listener);
        }
    }

    public void removeRemoteListener(RemoteListener listener) {
        if (this.translator == null) {
            this.manager.removeRemoteListener(listener);
        } else {
            this.translator.removeRemoteListener(this, listener);
        }
    }

    public void removeSendStreamListener(SendStreamListener listener) {
        if (this.translator == null) {
            this.manager.removeSendStreamListener(listener);
        } else {
            this.translator.removeSendStreamListener(this, listener);
        }
    }

    public void removeSessionListener(SessionListener listener) {
        if (this.translator == null) {
            this.manager.removeSessionListener(listener);
        } else {
            this.translator.removeSessionListener(this, listener);
        }
    }

    public void setSSRCFactory(SSRCFactory ssrcFactory) {
        RTPManager m = this.manager;
        if (m instanceof org.jitsi.impl.neomedia.jmfext.media.rtp.RTPSessionMgr) {
            ((org.jitsi.impl.neomedia.jmfext.media.rtp.RTPSessionMgr) m).setSSRCFactory(ssrcFactory);
        }
    }
}
