package com.sun.media.format;

import java.util.Hashtable;
import javax.media.Format;
import javax.media.format.AudioFormat;
import net.sf.fmj.utility.FormatUtils;

public class WavAudioFormat extends AudioFormat {
    public static final int WAVE_FORMAT_ADPCM = 2;
    public static final int WAVE_FORMAT_ALAW = 6;
    public static final int WAVE_FORMAT_DIGIFIX = 22;
    public static final int WAVE_FORMAT_DIGISTD = 21;
    public static final int WAVE_FORMAT_DSPGROUP_TRUESPEECH = 34;
    public static final int WAVE_FORMAT_DVI_ADPCM = 17;
    public static final int WAVE_FORMAT_GSM610 = 49;
    public static final int WAVE_FORMAT_MPEG_LAYER3 = 85;
    public static final int WAVE_FORMAT_MSG723 = 66;
    public static final int WAVE_FORMAT_MSNAUDIO = 50;
    public static final int WAVE_FORMAT_MSRT24 = 130;
    public static final int WAVE_FORMAT_MULAW = 7;
    public static final int WAVE_FORMAT_OKI_ADPCM = 16;
    public static final int WAVE_FORMAT_PCM = 1;
    public static final int WAVE_FORMAT_SX7383 = 7175;
    public static final int WAVE_FORMAT_VOXWARE_AC10 = 113;
    public static final int WAVE_FORMAT_VOXWARE_AC16 = 114;
    public static final int WAVE_FORMAT_VOXWARE_AC20 = 115;
    public static final int WAVE_FORMAT_VOXWARE_AC8 = 112;
    public static final int WAVE_FORMAT_VOXWARE_METASOUND = 117;
    public static final int WAVE_FORMAT_VOXWARE_METAVOICE = 116;
    public static final int WAVE_FORMAT_VOXWARE_RT29H = 118;
    public static final int WAVE_FORMAT_VOXWARE_TQ40 = 121;
    public static final int WAVE_FORMAT_VOXWARE_TQ60 = 129;
    public static final int WAVE_FORMAT_VOXWARE_VR12 = 119;
    public static final int WAVE_FORMAT_VOXWARE_VR18 = 120;
    public static final int WAVE_IBM_FORMAT_ADPCM = 259;
    public static final int WAVE_IBM_FORMAT_ALAW = 258;
    public static final int WAVE_IBM_FORMAT_MULAW = 257;
    public static final Hashtable<Integer, String> formatMapper = new Hashtable();
    public static final Hashtable<String, Integer> reverseFormatMapper = new Hashtable();
    private int averageBytesPerSecond = -1;
    protected byte[] codecSpecificHeader;

    static {
        formatMapper.put(new Integer(1), AudioFormat.LINEAR);
        formatMapper.put(new Integer(2), AudioFormat.MSADPCM);
        formatMapper.put(new Integer(6), AudioFormat.ALAW);
        formatMapper.put(new Integer(7), AudioFormat.ULAW);
        formatMapper.put(new Integer(17), AudioFormat.IMA4_MS);
        formatMapper.put(new Integer(34), AudioFormat.TRUESPEECH);
        formatMapper.put(new Integer(49), AudioFormat.GSM_MS);
        formatMapper.put(new Integer(50), AudioFormat.MSNAUDIO);
        formatMapper.put(new Integer(85), AudioFormat.MPEGLAYER3);
        formatMapper.put(new Integer(WAVE_FORMAT_VOXWARE_AC8), AudioFormat.VOXWAREAC8);
        formatMapper.put(new Integer(WAVE_FORMAT_VOXWARE_AC10), AudioFormat.VOXWAREAC10);
        formatMapper.put(new Integer(WAVE_FORMAT_VOXWARE_AC16), AudioFormat.VOXWAREAC16);
        formatMapper.put(new Integer(WAVE_FORMAT_VOXWARE_AC20), AudioFormat.VOXWAREAC20);
        formatMapper.put(new Integer(WAVE_FORMAT_VOXWARE_METAVOICE), AudioFormat.VOXWAREMETAVOICE);
        formatMapper.put(new Integer(WAVE_FORMAT_VOXWARE_METASOUND), AudioFormat.VOXWAREMETASOUND);
        formatMapper.put(new Integer(WAVE_FORMAT_VOXWARE_RT29H), AudioFormat.VOXWARERT29H);
        formatMapper.put(new Integer(WAVE_FORMAT_VOXWARE_VR12), AudioFormat.VOXWAREVR12);
        formatMapper.put(new Integer(WAVE_FORMAT_VOXWARE_VR18), AudioFormat.VOXWAREVR18);
        formatMapper.put(new Integer(WAVE_FORMAT_VOXWARE_TQ40), AudioFormat.VOXWARETQ40);
        formatMapper.put(new Integer(WAVE_FORMAT_VOXWARE_TQ60), AudioFormat.VOXWARETQ60);
        formatMapper.put(new Integer(WAVE_FORMAT_MSRT24), AudioFormat.MSRT24);
        reverseFormatMapper.put(AudioFormat.ALAW, new Integer(6));
        reverseFormatMapper.put(AudioFormat.GSM_MS, new Integer(49));
        reverseFormatMapper.put(AudioFormat.IMA4_MS, new Integer(17));
        reverseFormatMapper.put("linear", new Integer(1));
        reverseFormatMapper.put(AudioFormat.MPEGLAYER3, new Integer(85));
        reverseFormatMapper.put(AudioFormat.MSADPCM, new Integer(2));
        reverseFormatMapper.put(AudioFormat.MSNAUDIO, new Integer(50));
        reverseFormatMapper.put(AudioFormat.MSRT24, new Integer(WAVE_FORMAT_MSRT24));
        reverseFormatMapper.put(AudioFormat.TRUESPEECH, new Integer(34));
        reverseFormatMapper.put("ulaw", new Integer(7));
        reverseFormatMapper.put(AudioFormat.VOXWAREAC10, new Integer(WAVE_FORMAT_VOXWARE_AC10));
        reverseFormatMapper.put(AudioFormat.VOXWAREAC16, new Integer(WAVE_FORMAT_VOXWARE_AC16));
        reverseFormatMapper.put(AudioFormat.VOXWAREAC20, new Integer(WAVE_FORMAT_VOXWARE_AC20));
        reverseFormatMapper.put(AudioFormat.VOXWAREAC8, new Integer(WAVE_FORMAT_VOXWARE_AC8));
        reverseFormatMapper.put(AudioFormat.VOXWAREMETASOUND, new Integer(WAVE_FORMAT_VOXWARE_METASOUND));
        reverseFormatMapper.put(AudioFormat.VOXWAREMETAVOICE, new Integer(WAVE_FORMAT_VOXWARE_METAVOICE));
        reverseFormatMapper.put(AudioFormat.VOXWARERT29H, new Integer(WAVE_FORMAT_VOXWARE_RT29H));
        reverseFormatMapper.put(AudioFormat.VOXWARETQ40, new Integer(WAVE_FORMAT_VOXWARE_TQ40));
        reverseFormatMapper.put(AudioFormat.VOXWARETQ60, new Integer(WAVE_FORMAT_VOXWARE_TQ60));
        reverseFormatMapper.put(AudioFormat.VOXWAREVR12, new Integer(WAVE_FORMAT_VOXWARE_VR12));
        reverseFormatMapper.put(AudioFormat.VOXWAREVR18, new Integer(WAVE_FORMAT_VOXWARE_VR18));
    }

    public WavAudioFormat(String encoding) {
        super(encoding);
    }

    public WavAudioFormat(String encoding, double sampleRate, int sampleSizeInBits, int channels, int frameSizeInBits, int averageBytesPerSecond, byte[] codecSpecificHeader) {
        super(encoding, sampleRate, sampleSizeInBits, channels, -1, -1, frameSizeInBits, (double) averageBytesPerSecond, byteArray);
        this.averageBytesPerSecond = averageBytesPerSecond;
        this.codecSpecificHeader = codecSpecificHeader;
    }

    public WavAudioFormat(String encoding, double sampleRate, int sampleSizeInBits, int channels, int frameSizeInBits, int averageBytesPerSecond, int endian, int signed, float frameRate, Class<?> dataType, byte[] codecSpecificHeader) {
        super(encoding, sampleRate, sampleSizeInBits, channels, endian, signed, frameSizeInBits, (double) averageBytesPerSecond, dataType);
        this.averageBytesPerSecond = averageBytesPerSecond;
        this.codecSpecificHeader = codecSpecificHeader;
    }

    public Object clone() {
        return new WavAudioFormat(this.encoding, this.sampleRate, this.sampleSizeInBits, this.channels, this.frameSizeInBits, this.averageBytesPerSecond, this.endian, this.signed, (float) this.frameRate, this.dataType, this.codecSpecificHeader);
    }

    /* access modifiers changed from: protected */
    public void copy(Format f) {
        super.copy(f);
        WavAudioFormat oCast = (WavAudioFormat) f;
        this.averageBytesPerSecond = oCast.averageBytesPerSecond;
        this.codecSpecificHeader = oCast.codecSpecificHeader;
    }

    public boolean equals(Object format) {
        if (!super.equals(format) || !(format instanceof WavAudioFormat)) {
            return false;
        }
        WavAudioFormat oCast = (WavAudioFormat) format;
        if (this.averageBytesPerSecond == oCast.averageBytesPerSecond && this.codecSpecificHeader == oCast.codecSpecificHeader) {
            return true;
        }
        return false;
    }

    public int getAverageBytesPerSecond() {
        return this.averageBytesPerSecond;
    }

    public byte[] getCodecSpecificHeader() {
        return this.codecSpecificHeader;
    }

    public Format intersects(Format other) {
        Format result = super.intersects(other);
        if (other instanceof WavAudioFormat) {
            WavAudioFormat resultCast = (WavAudioFormat) result;
            WavAudioFormat oCast = (WavAudioFormat) other;
            if (getClass().isAssignableFrom(other.getClass())) {
                if (FormatUtils.specified(this.averageBytesPerSecond)) {
                    resultCast.averageBytesPerSecond = this.averageBytesPerSecond;
                }
                if (FormatUtils.specified(this.codecSpecificHeader)) {
                    resultCast.codecSpecificHeader = this.codecSpecificHeader;
                }
            } else if (other.getClass().isAssignableFrom(getClass())) {
                if (!FormatUtils.specified(resultCast.averageBytesPerSecond)) {
                    resultCast.averageBytesPerSecond = oCast.averageBytesPerSecond;
                }
                if (!FormatUtils.specified(resultCast.codecSpecificHeader)) {
                    resultCast.codecSpecificHeader = oCast.codecSpecificHeader;
                }
            }
        }
        return result;
    }

    public boolean matches(Format format) {
        if (!super.matches(format)) {
            return false;
        }
        if (!(format instanceof WavAudioFormat)) {
            return true;
        }
        WavAudioFormat oCast = (WavAudioFormat) format;
        if (FormatUtils.matches(this.averageBytesPerSecond, oCast.averageBytesPerSecond) && FormatUtils.matches(this.codecSpecificHeader, oCast.codecSpecificHeader)) {
            return true;
        }
        return false;
    }
}
