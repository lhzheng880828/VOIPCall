package org.jitsi.impl.neomedia.codec.audio.alaw;

import com.ibm.media.codec.audio.AudioPacketizer;
import javax.media.Codec;
import javax.media.Control;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;

public class Packetizer extends AudioPacketizer {

    private static class PacketSizeAdapter extends com.sun.media.controls.PacketSizeAdapter {
        public PacketSizeAdapter(Codec owner, int packetSize, boolean settable) {
            super(owner, packetSize, settable);
        }

        public int setPacketSize(int numBytes) {
            if (numBytes < 10) {
                numBytes = 10;
            }
            if (numBytes > 8000) {
                numBytes = 8000;
            }
            this.packetSize = numBytes;
            ((Packetizer) this.owner).setPacketSize(this.packetSize);
            return this.packetSize;
        }
    }

    public Packetizer() {
        AudioFormat[] audioFormatArr = new AudioFormat[1];
        audioFormatArr[0] = new AudioFormat("ALAW/rtp", -1.0d, 8, 1, -1, -1, 8, -1.0d, Format.byteArray);
        this.defaultOutputFormats = audioFormatArr;
        this.packetSize = 160;
        this.PLUGIN_NAME = "A-law Packetizer";
        audioFormatArr = new AudioFormat[1];
        audioFormatArr[0] = new AudioFormat(AudioFormat.ALAW, -1.0d, 8, 1, -1, -1, 8, -1.0d, Format.byteArray);
        this.supportedInputFormats = audioFormatArr;
    }

    public Object[] getControls() {
        if (this.controls == null) {
            this.controls = new Control[]{new PacketSizeAdapter(this, this.packetSize, true)};
        }
        return this.controls;
    }

    /* access modifiers changed from: protected */
    public Format[] getMatchingOutputFormats(Format in) {
        double sampleRate = ((AudioFormat) in).getSampleRate();
        AudioFormat[] audioFormatArr = new AudioFormat[1];
        audioFormatArr[0] = new AudioFormat("ALAW/rtp", sampleRate, 8, 1, -1, -1, 8, sampleRate, Format.byteArray);
        this.supportedOutputFormats = audioFormatArr;
        return this.supportedOutputFormats;
    }

    public void open() throws ResourceUnavailableException {
        setPacketSize(this.packetSize);
        reset();
    }

    /* access modifiers changed from: private|declared_synchronized */
    public synchronized void setPacketSize(int newPacketSize) {
        this.packetSize = newPacketSize;
        this.sample_count = this.packetSize;
        if (this.history == null) {
            this.history = new byte[this.packetSize];
        } else if (this.packetSize > this.history.length) {
            byte[] newHistory = new byte[this.packetSize];
            System.arraycopy(this.history, 0, newHistory, 0, this.historyLength);
            this.history = newHistory;
        }
    }
}
