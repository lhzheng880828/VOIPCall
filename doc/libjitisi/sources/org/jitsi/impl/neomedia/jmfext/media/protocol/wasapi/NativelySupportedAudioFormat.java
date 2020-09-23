package org.jitsi.impl.neomedia.jmfext.media.protocol.wasapi;

import javax.media.format.AudioFormat;

public class NativelySupportedAudioFormat extends AudioFormat {
    public NativelySupportedAudioFormat(String encoding, double sampleRate, int sampleSizeInBits, int channels, int endian, int signed, int frameSizeInBits, double frameRate, Class<?> dataType) {
        super(encoding, sampleRate, sampleSizeInBits, channels, endian, signed, frameSizeInBits, frameRate, dataType);
        this.clz = AudioFormat.class;
    }
}
