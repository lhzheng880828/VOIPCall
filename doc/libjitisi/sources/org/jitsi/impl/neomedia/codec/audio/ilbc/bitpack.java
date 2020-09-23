package org.jitsi.impl.neomedia.codec.audio.ilbc;

class bitpack {
    int firstpart;
    int rest;

    public bitpack() {
        this.firstpart = 0;
        this.rest = 0;
    }

    public bitpack(int fp, int r) {
        this.firstpart = fp;
        this.rest = r;
    }

    public int get_firstpart() {
        return this.firstpart;
    }

    public void set_firstpart(int fp) {
        this.firstpart = fp;
    }

    public int get_rest() {
        return this.rest;
    }

    public void set_rest(int r) {
        this.rest = r;
    }
}
