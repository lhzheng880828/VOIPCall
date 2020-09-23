package net.sf.fmj.media.codec;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import net.sf.fmj.media.AbstractCodec;
import net.sf.fmj.media.BufferQueueInputStream;
import net.sf.fmj.media.renderer.audio.JavaSoundUtils;
import net.sf.fmj.utility.FormatUtils;
import net.sf.fmj.utility.LoggerSingleton;
import net.sf.fmj.utility.LoggingStringUtils;
import org.jitsi.android.util.javax.sound.sampled.AudioFormat;
import org.jitsi.android.util.javax.sound.sampled.AudioFormat.Encoding;
import org.jitsi.android.util.javax.sound.sampled.AudioInputStream;
import org.jitsi.android.util.javax.sound.sampled.AudioSystem;
import org.jitsi.android.util.javax.sound.sampled.UnsupportedAudioFileException;

public class JavaSoundCodec extends AbstractCodec {
    private static final int BITS_PER_BYTE = 8;
    private static final int MAX_BYTE = 255;
    private static final int MAX_BYTE_PLUS1 = 256;
    private static final int MAX_SIGNED_BYTE = 127;
    private static final int SIZEOF_INT = 4;
    private static final int SIZEOF_LONG = 8;
    private static final int SIZEOF_SHORT = 2;
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = LoggerSingleton.logger;
    /* access modifiers changed from: private|volatile */
    public volatile AudioInputStream audioInputStream;
    /* access modifiers changed from: private|volatile */
    public volatile AudioInputStream audioInputStreamConverted;
    private AudioInputStreamThread audioInputStreamThread;
    private BufferQueueInputStream bufferQueueInputStream;
    private int totalIn;
    private int totalOut;
    private boolean trace;

    private class AudioInputStreamThread extends Thread {
        private final BufferQueueInputStream bufferQueueInputStream;

        public AudioInputStreamThread(BufferQueueInputStream bufferQueueInputStream) {
            this.bufferQueueInputStream = bufferQueueInputStream;
        }

        public void run() {
            try {
                JavaSoundCodec.this.audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(this.bufferQueueInputStream));
                AudioFormat javaSoundAudioFormat = JavaSoundUtils.convertFormat((javax.media.format.AudioFormat) JavaSoundCodec.this.outputFormat);
                JavaSoundCodec.logger.fine("javaSoundAudioFormat converted (out)=" + javaSoundAudioFormat);
                JavaSoundCodec.this.audioInputStreamConverted = AudioSystem.getAudioInputStream(javaSoundAudioFormat, JavaSoundCodec.this.audioInputStream);
            } catch (UnsupportedAudioFileException e) {
                JavaSoundCodec.logger.log(Level.WARNING, "" + e, e);
            } catch (IOException e2) {
                JavaSoundCodec.logger.log(Level.WARNING, "" + e2, e2);
            }
        }
    }

    public static byte[] createAuHeader(AudioFormat f) {
        int encoding;
        byte[] result = new byte[24];
        encodeIntBE(779316836, result, 0);
        encodeIntBE(result.length, result, 4);
        encodeIntBE(-1, result, 8);
        if (f.getEncoding() == Encoding.ALAW) {
            if (f.getSampleSizeInBits() != 8) {
                return null;
            }
            encoding = 27;
        } else if (f.getEncoding() == Encoding.ULAW) {
            if (f.getSampleSizeInBits() != 8) {
                return null;
            }
            encoding = 1;
        } else if (f.getEncoding() == Encoding.PCM_SIGNED) {
            if (f.getSampleSizeInBits() == 8) {
                encoding = 2;
            } else if (f.getSampleSizeInBits() == 16) {
                encoding = 3;
            } else if (f.getSampleSizeInBits() == 24) {
                encoding = 4;
            } else if (f.getSampleSizeInBits() != 32) {
                return null;
            } else {
                encoding = 5;
            }
            if (f.getSampleSizeInBits() > 8 && !f.isBigEndian()) {
                return null;
            }
        } else if (f.getEncoding() == Encoding.PCM_UNSIGNED) {
            return null;
        } else {
            return null;
        }
        encodeIntBE(encoding, result, 12);
        if (f.getSampleRate() < 0.0f) {
            return null;
        }
        encodeIntBE((int) f.getSampleRate(), result, 16);
        if (f.getChannels() < 0) {
            return null;
        }
        encodeIntBE(f.getChannels(), result, 20);
        return result;
    }

    public static byte[] createWavHeader(AudioFormat f) {
        byte[] result = null;
        if ((f.getEncoding() == Encoding.PCM_SIGNED || f.getEncoding() == Encoding.PCM_UNSIGNED) && ((f.getSampleSizeInBits() != 8 || f.getEncoding() == Encoding.PCM_UNSIGNED) && (f.getSampleSizeInBits() != 16 || f.getEncoding() == Encoding.PCM_SIGNED))) {
            result = new byte[44];
            if (f.getSampleSizeInBits() <= 8 || !f.isBigEndian()) {
                encodeIntBE(1380533830, result, 0);
            } else {
                encodeIntBE(1380533848, result, 0);
            }
            encodeIntLE((result.length + Integer.MAX_VALUE) - 8, result, 4);
            encodeIntBE(1463899717, result, 8);
            encodeIntBE(1718449184, result, 12);
            encodeIntLE(16, result, 16);
            encodeShortLE((short) 1, result, 20);
            encodeShortLE((short) f.getChannels(), result, 22);
            encodeIntLE((int) f.getSampleRate(), result, 24);
            encodeIntLE(((((int) f.getSampleRate()) * f.getChannels()) * f.getSampleSizeInBits()) / 8, result, 28);
            encodeShortLE((short) ((f.getChannels() * f.getSampleSizeInBits()) / 8), result, 32);
            encodeShortLE((short) f.getSampleSizeInBits(), result, 34);
            encodeIntBE(1684108385, result, 36);
            encodeIntLE(Integer.MAX_VALUE, result, 40);
        }
        return result;
    }

    private static void encodeIntBE(int value, byte[] ba, int offset) {
        for (int i = 0; i < 4; i++) {
            int byteValue = value & 255;
            if (byteValue > 127) {
                byteValue -= 256;
            }
            ba[((4 - i) - 1) + offset] = (byte) byteValue;
            value >>= 8;
        }
    }

    private static void encodeIntLE(int value, byte[] ba, int offset) {
        for (int i = 0; i < 4; i++) {
            int byteValue = value & 255;
            if (byteValue > 127) {
                byteValue -= 256;
            }
            ba[offset + i] = (byte) byteValue;
            value >>= 8;
        }
    }

    public static void encodeShortBE(short value, byte[] ba, int offset) {
        for (int i = 0; i < 2; i++) {
            int byteValue = value & 255;
            if (byteValue > 127) {
                byteValue -= 256;
            }
            ba[((2 - i) - 1) + offset] = (byte) byteValue;
            value = (short) (value >> 8);
        }
    }

    public static void encodeShortLE(short value, byte[] ba, int offset) {
        for (int i = 0; i < 2; i++) {
            int byteValue = value & 255;
            if (byteValue > 127) {
                byteValue -= 256;
            }
            ba[offset + i] = (byte) byteValue;
            value = (short) (value >> 8);
        }
    }

    private static byte[] fakeHeader(AudioFormat f) {
        Class<?> classVorbisAudioFormat = null;
        Class<?> classMpegAudioFormatt = null;
        if (!JavaSoundUtils.onlyStandardFormats) {
            try {
                classMpegAudioFormatt = Class.forName("javazoom.spi.mpeg.sampled.file.MpegAudioFormat");
                classVorbisAudioFormat = Class.forName("javazoom.spi.vorbis.sampled.file.VorbisAudioFormat");
            } catch (Exception e) {
            }
        }
        if (classMpegAudioFormatt != null && classMpegAudioFormatt.isInstance(f)) {
            return new byte[0];
        }
        if (classVorbisAudioFormat != null && classVorbisAudioFormat.isInstance(f)) {
            return new byte[0];
        }
        byte[] result = createAuHeader(f);
        if (result != null) {
            return result;
        }
        result = createWavHeader(f);
        return result == null ? null : result;
    }

    public JavaSoundCodec() {
        Vector<Format> formats = new Vector();
        formats.add(new javax.media.format.AudioFormat(javax.media.format.AudioFormat.ULAW));
        formats.add(new javax.media.format.AudioFormat(javax.media.format.AudioFormat.ALAW));
        formats.add(new javax.media.format.AudioFormat(javax.media.format.AudioFormat.LINEAR));
        if (!JavaSoundUtils.onlyStandardFormats) {
            try {
                Class<?> classMpegEncoding = Class.forName("javazoom.spi.mpeg.sampled.file.MpegEncoding");
                String[] mpegEncodingStrings = new String[]{"MPEG1L1", "MPEG1L2", "MPEG1L3", "MPEG2DOT5L1", "MPEG2DOT5L2", "MPEG2DOT5L3", "MPEG2L1", "MPEG2L2", "MPEG2L3"};
                for (String audioFormat : mpegEncodingStrings) {
                    formats.add(new javax.media.format.AudioFormat(audioFormat));
                }
            } catch (Exception e) {
            }
            try {
                Class<?> classVorbisEncoding = Class.forName("javazoom.spi.vorbis.sampled.file.VorbisEncoding");
                String[] vorbisEncodingStrings = new String[]{"VORBISENC"};
                for (String audioFormat2 : vorbisEncodingStrings) {
                    formats.add(new javax.media.format.AudioFormat(audioFormat2));
                }
            } catch (Exception e2) {
            }
        }
        this.inputFormats = new Format[formats.size()];
        formats.toArray(this.inputFormats);
    }

    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return new Format[]{new javax.media.format.AudioFormat(javax.media.format.AudioFormat.LINEAR)};
        }
        AudioFormat[] targetsSpecial;
        int i;
        AudioFormat javaSoundFormat = JavaSoundUtils.convertFormat((javax.media.format.AudioFormat) input);
        AudioFormat[] targets1 = AudioSystem.getTargetFormats(Encoding.PCM_UNSIGNED, javaSoundFormat);
        AudioFormat[] targets2 = AudioSystem.getTargetFormats(Encoding.PCM_SIGNED, javaSoundFormat);
        Class<?> classVorbisAudioFormat = null;
        Class<?> classMpegAudioFormatt = null;
        if (!JavaSoundUtils.onlyStandardFormats) {
            try {
                classMpegAudioFormatt = Class.forName("javazoom.spi.mpeg.sampled.file.MpegAudioFormat");
                classVorbisAudioFormat = Class.forName("javazoom.spi.vorbis.sampled.file.VorbisAudioFormat");
            } catch (Exception e) {
            }
        }
        if (classMpegAudioFormatt == null || !classMpegAudioFormatt.isInstance(javaSoundFormat)) {
            targetsSpecial = (classVorbisAudioFormat == null || !classVorbisAudioFormat.isInstance(javaSoundFormat)) ? new AudioFormat[0] : new AudioFormat[]{new AudioFormat(Encoding.PCM_SIGNED, javaSoundFormat.getSampleRate(), 16, javaSoundFormat.getChannels(), javaSoundFormat.getChannels() * 2, javaSoundFormat.getSampleRate(), false)};
        } else {
            targetsSpecial = new AudioFormat[]{new AudioFormat(Encoding.PCM_SIGNED, javaSoundFormat.getSampleRate(), 16, javaSoundFormat.getChannels(), javaSoundFormat.getChannels() * 2, javaSoundFormat.getSampleRate(), false)};
        }
        Format[] result = new Format[((targets1.length + targets2.length) + targetsSpecial.length)];
        for (i = 0; i < targets1.length; i++) {
            result[i] = JavaSoundUtils.convertFormat(targets1[i]);
            logger.finer("getSupportedOutputFormats: " + result[i]);
        }
        for (i = 0; i < targets2.length; i++) {
            result[targets1.length + i] = JavaSoundUtils.convertFormat(targets2[i]);
            logger.finer("getSupportedOutputFormats: " + result[targets1.length + i]);
        }
        for (i = 0; i < targetsSpecial.length; i++) {
            result[(targets1.length + targets2.length) + i] = JavaSoundUtils.convertFormat(targetsSpecial[i]);
            logger.finer("getSupportedOutputFormats: " + result[(targets1.length + targets2.length) + i]);
        }
        for (i = 0; i < result.length; i++) {
            javax.media.format.AudioFormat a = result[i];
            if (FormatUtils.specified(((javax.media.format.AudioFormat) input).getSampleRate()) && !FormatUtils.specified(a.getSampleRate())) {
                result[i] = null;
            }
        }
        return result;
    }

    public void open() throws ResourceUnavailableException {
        super.open();
        this.bufferQueueInputStream = new BufferQueueInputStream();
        AudioFormat javaSoundAudioFormat = JavaSoundUtils.convertFormat((javax.media.format.AudioFormat) this.inputFormat);
        logger.fine("javaSoundAudioFormat converted (in)=" + javaSoundAudioFormat);
        byte[] header = fakeHeader(javaSoundAudioFormat);
        if (header == null) {
            throw new ResourceUnavailableException("Unable to reconstruct header for format: " + this.inputFormat);
        }
        if (header.length > 0) {
            Buffer headerBuffer = new Buffer();
            headerBuffer.setData(header);
            headerBuffer.setLength(header.length);
            this.bufferQueueInputStream.put(headerBuffer);
        }
        this.audioInputStreamThread = new AudioInputStreamThread(this.bufferQueueInputStream);
        this.audioInputStreamThread.start();
    }

    public int process(Buffer input, Buffer output) {
        if (!checkInputBuffer(input)) {
            return 1;
        }
        try {
            boolean noRoomInBufferQueue;
            int lenToRead;
            if (this.trace) {
                logger.fine("process: " + LoggingStringUtils.bufferToStr(input));
            }
            this.totalIn += input.getLength();
            if (this.bufferQueueInputStream.put(input)) {
                noRoomInBufferQueue = false;
            } else {
                noRoomInBufferQueue = true;
            }
            if (this.audioInputStreamConverted == null) {
                if (noRoomInBufferQueue) {
                    logger.fine("JavaSoundCodec: audioInputStreamConverted == null, blocking until not null");
                    while (this.audioInputStreamConverted == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            return 1;
                        }
                    }
                }
                logger.fine("JavaSoundCodec: audioInputStreamConverted == null, returning OUTPUT_BUFFER_NOT_FILLED");
                output.setLength(0);
                return 4;
            }
            int avail = this.audioInputStreamConverted.available();
            if (this.trace) {
                logger.fine("audioInputStreamConverted.available() == " + avail + ", bufferQueueInputStream.available() = " + this.bufferQueueInputStream.available());
            }
            if (output.getData() == null) {
                output.setData(new byte[10000]);
            }
            output.setFormat(getOutputFormat());
            byte[] data = (byte[]) output.getData();
            if (noRoomInBufferQueue || input.isEOM()) {
                lenToRead = data.length;
            } else {
                lenToRead = avail > data.length ? data.length : avail;
            }
            if (lenToRead == 0) {
                logger.finer("JavaSoundCodec: lenToRead == 0, returning OUTPUT_BUFFER_NOT_FILLED.  input.isEOM()=" + input.isEOM());
                output.setLength(0);
                return 4;
            }
            int lenRead = this.audioInputStreamConverted.read(data, 0, lenToRead);
            logger.finer("JavaSoundCodec: Read from audioInputStreamConverted: " + lenRead);
            if (lenRead == -1) {
                logger.fine("total in: " + this.totalIn + " total out: " + this.totalOut);
                output.setEOM(true);
                output.setLength(0);
                return 0;
            }
            output.setLength(lenRead);
            this.totalOut += lenRead;
            int i = (noRoomInBufferQueue || input.isEOM()) ? 2 : 0;
            return i;
        } catch (IOException e2) {
            output.setLength(0);
            return 1;
        }
    }

    /* access modifiers changed from: 0000 */
    public void setTrace(boolean value) {
        this.trace = value;
    }
}
