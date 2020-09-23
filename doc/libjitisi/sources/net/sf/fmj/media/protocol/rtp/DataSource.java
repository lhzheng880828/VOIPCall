package net.sf.fmj.media.protocol.rtp;

import java.io.IOException;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.RTPControl;
import net.sf.fmj.media.protocol.BasicPushBufferDataSource;
import net.sf.fmj.media.protocol.BufferListener;
import net.sf.fmj.media.protocol.RTPSource;
import net.sf.fmj.media.protocol.Streamable;
import net.sf.fmj.media.rtp.RTPControlImpl;
import net.sf.fmj.media.rtp.RTPSessionMgr;
import net.sf.fmj.media.rtp.RTPSourceStream;
import net.sf.fmj.media.rtp.SSRCInfo;

public class DataSource extends BasicPushBufferDataSource implements Streamable, RTPSource {
    static int SSRC_UNDEFINED = 0;
    DataSource childsrc;
    RTPSessionMgr mgr;
    RTPControl rtpcontrol;
    private RTPSourceStream[] srcStreams;
    int ssrc;
    private boolean stopped;
    Player streamplayer;

    class MyRTPControl extends RTPControlImpl {
        MyRTPControl() {
        }

        public String getCNAME() {
            if (DataSource.this.mgr == null) {
                return null;
            }
            SSRCInfo info = DataSource.this.mgr.getSSRCInfo(DataSource.this.ssrc);
            if (info != null) {
                return info.getCNAME();
            }
            return null;
        }

        public int getSSRC() {
            return DataSource.this.ssrc;
        }
    }

    public DataSource() {
        this.srcStreams = null;
        this.stopped = true;
        this.streamplayer = null;
        this.mgr = null;
        this.rtpcontrol = null;
        this.childsrc = null;
        this.ssrc = SSRC_UNDEFINED;
        this.srcStreams = new RTPSourceStream[1];
        this.rtpcontrol = new MyRTPControl();
        setContentType("rtp");
    }

    public void connect() throws IOException {
        if (this.srcStreams != null) {
            for (int i = 0; i < this.srcStreams.length; i++) {
                if (this.srcStreams[i] != null) {
                    this.srcStreams[i].connect();
                }
            }
        }
        this.connected = true;
    }

    public void disconnect() {
        if (this.srcStreams != null) {
            for (RTPSourceStream close : this.srcStreams) {
                close.close();
            }
        }
    }

    public void flush() {
        this.srcStreams[0].reset();
    }

    public String getCNAME() {
        if (this.mgr == null) {
            return null;
        }
        SSRCInfo info = this.mgr.getSSRCInfo(this.ssrc);
        if (info != null) {
            return info.getCNAME();
        }
        return null;
    }

    public Object getControl(String type) {
        try {
            Class<?> cls = Class.forName(type);
            Object[] cs = getControls();
            for (int i = 0; i < cs.length; i++) {
                if (cls.isInstance(cs[i])) {
                    return cs[i];
                }
            }
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public Object[] getControls() {
        return new RTPControl[]{this.rtpcontrol};
    }

    public RTPSessionMgr getMgr() {
        return this.mgr;
    }

    public Player getPlayer() {
        return this.streamplayer;
    }

    public int getSSRC() {
        return this.ssrc;
    }

    public PushBufferStream[] getStreams() {
        if (this.connected) {
            return this.srcStreams;
        }
        return null;
    }

    public boolean isPrefetchable() {
        return false;
    }

    public boolean isStarted() {
        return this.started;
    }

    public void prebuffer() {
        this.started = true;
        this.srcStreams[0].prebuffer();
    }

    public void setBufferListener(BufferListener listener) {
        this.srcStreams[0].setBufferListener(listener);
    }

    public void setBufferWhenStopped(boolean flag) {
        this.srcStreams[0].setBufferWhenStopped(flag);
    }

    public void setChild(DataSource source) {
        this.childsrc = source;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setControl(Object control) {
        this.rtpcontrol = (RTPControl) control;
    }

    public void setLocator(MediaLocator mrl) {
        super.setLocator(mrl);
    }

    public void setMgr(RTPSessionMgr mgr) {
        this.mgr = mgr;
    }

    public void setPlayer(Player player) {
        this.streamplayer = player;
    }

    public void setSourceStream(RTPSourceStream stream) {
        if (this.srcStreams != null) {
            this.srcStreams[0] = stream;
        }
    }

    public void setSSRC(int ssrc) {
        this.ssrc = ssrc;
    }

    public void start() throws IOException {
        super.start();
        if (this.childsrc != null) {
            this.childsrc.start();
        }
        if (this.srcStreams != null) {
            for (RTPSourceStream start : this.srcStreams) {
                start.start();
            }
        }
    }

    public void stop() throws IOException {
        super.stop();
        if (this.childsrc != null) {
            this.childsrc.stop();
        }
        if (this.srcStreams != null) {
            for (RTPSourceStream stop : this.srcStreams) {
                stop.stop();
            }
        }
    }
}
