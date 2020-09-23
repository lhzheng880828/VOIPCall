package net.sf.fmj.media.renderer.audio.device;

import javax.media.format.AudioFormat;

public interface AudioOutput {
    int bufferAvailable();

    void dispose();

    void drain();

    void flush();

    double getGain();

    long getMediaNanoseconds();

    boolean getMute();

    float getRate();

    boolean initialize(AudioFormat audioFormat, int i);

    void pause();

    void resume();

    void setGain(double d);

    void setMute(boolean z);

    float setRate(float f);

    int write(byte[] bArr, int i, int i2);
}
