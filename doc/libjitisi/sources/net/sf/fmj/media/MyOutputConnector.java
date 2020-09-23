package net.sf.fmj.media;

import javax.media.Track;

/* compiled from: BasicSourceModule */
class MyOutputConnector extends BasicOutputConnector {
    protected Track track;

    public MyOutputConnector(Track track) {
        this.track = track;
        this.format = track.getFormat();
    }

    public String toString() {
        return super.toString() + ": " + getFormat();
    }
}
