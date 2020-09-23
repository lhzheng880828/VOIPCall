package net.sf.fmj.media.control;

import javax.media.Format;
import javax.media.Player;
import javax.media.Time;
import javax.media.Track;
import javax.media.control.FramePositioningControl;
import javax.media.format.VideoFormat;
import net.sf.fmj.media.Reparentable;
import org.jitsi.android.util.java.awt.Component;

public class FramePositioningAdapter implements FramePositioningControl, Reparentable {
    long frameStep = -1;
    Track master = null;
    Object owner;
    Player player;

    public static Track getMasterTrack(Track[] tracks) {
        Track master = null;
        for (int i = 0; i < tracks.length; i++) {
            if (tracks[i] != null) {
                Format f = tracks[i].getFormat();
                if (f != null && (f instanceof VideoFormat)) {
                    master = tracks[i];
                    float rate = ((VideoFormat) f).getFrameRate();
                    if (!(rate == -1.0f || rate == 0.0f)) {
                        return master;
                    }
                }
            }
        }
        Track track;
        if (master == null || master.mapTimeToFrame(new Time(0)) == Integer.MAX_VALUE) {
            track = master;
            return null;
        }
        track = master;
        return master;
    }

    public FramePositioningAdapter(Player p, Track track) {
        this.player = p;
        this.master = track;
        Format f = track.getFormat();
        if (f instanceof VideoFormat) {
            float rate = ((VideoFormat) f).getFrameRate();
            if (rate != -1.0f && rate != 0.0f) {
                this.frameStep = (long) (1.0E9f / rate);
            }
        }
    }

    public Component getControlComponent() {
        return null;
    }

    public Object getOwner() {
        return this.owner == null ? this : this.owner;
    }

    public Time mapFrameToTime(int frameNumber) {
        return this.master.mapFrameToTime(frameNumber);
    }

    public int mapTimeToFrame(Time mediaTime) {
        return this.master.mapTimeToFrame(mediaTime);
    }

    public int seek(int frameNumber) {
        Time seekTo = this.master.mapFrameToTime(frameNumber);
        if (seekTo == null || seekTo == FramePositioningControl.TIME_UNKNOWN) {
            return Integer.MAX_VALUE;
        }
        this.player.setMediaTime(seekTo);
        return this.master.mapTimeToFrame(seekTo);
    }

    public void setOwner(Object newOwner) {
        this.owner = newOwner;
    }

    public int skip(int framesToSkip) {
        if (this.frameStep != -1) {
            this.player.setMediaTime(new Time(this.player.getMediaNanoseconds() + (((long) framesToSkip) * this.frameStep)));
            return framesToSkip;
        }
        int currentFrame = this.master.mapTimeToFrame(this.player.getMediaTime());
        return (currentFrame == 0 || currentFrame == Integer.MAX_VALUE) ? Integer.MAX_VALUE : seek(currentFrame + framesToSkip) - currentFrame;
    }
}
