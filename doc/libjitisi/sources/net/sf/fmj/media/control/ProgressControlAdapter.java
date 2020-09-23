package net.sf.fmj.media.control;

import javax.media.Control;

public class ProgressControlAdapter extends AtomicControlAdapter implements ProgressControl {
    StringControl ac = null;
    StringControl apc = null;
    StringControl brc = null;
    Control[] controls = null;
    StringControl frc = null;
    StringControl vc = null;
    StringControl vpc = null;

    public ProgressControlAdapter(StringControl frameRate, StringControl bitRate, StringControl videoProps, StringControl audioProps, StringControl videoCodec, StringControl audioCodec) {
        super(null, true, null);
        this.frc = frameRate;
        this.brc = bitRate;
        this.vpc = videoProps;
        this.apc = audioProps;
        this.vc = videoCodec;
        this.ac = audioCodec;
    }

    public StringControl getAudioCodec() {
        return this.ac;
    }

    public StringControl getAudioProperties() {
        return this.apc;
    }

    public StringControl getBitRate() {
        return this.brc;
    }

    public Control[] getControls() {
        if (this.controls == null) {
            this.controls = new Control[6];
            this.controls[0] = this.frc;
            this.controls[1] = this.brc;
            this.controls[2] = this.vpc;
            this.controls[3] = this.apc;
            this.controls[4] = this.ac;
            this.controls[5] = this.vc;
        }
        return this.controls;
    }

    public StringControl getFrameRate() {
        return this.frc;
    }

    public StringControl getVideoCodec() {
        return this.vc;
    }

    public StringControl getVideoProperties() {
        return this.vpc;
    }
}
