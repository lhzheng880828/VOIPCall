package net.sf.fmj.utility;

public class FPSCounter {
    private int frames;
    private long start;

    public double getFPS() {
        return (1000.0d * ((double) this.frames)) / ((double) (System.currentTimeMillis() - this.start));
    }

    public int getNumFrames() {
        return this.frames;
    }

    public void nextFrame() {
        if (this.start == 0) {
            this.start = System.currentTimeMillis();
        }
        this.frames++;
    }

    public void reset() {
        this.start = 0;
        this.frames = 0;
    }

    public String toString() {
        return "FPS: " + getFPS();
    }
}
