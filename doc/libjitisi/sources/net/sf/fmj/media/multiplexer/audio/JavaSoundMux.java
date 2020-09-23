package net.sf.fmj.media.multiplexer.audio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;
import javax.media.Format;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;
import net.sf.fmj.media.multiplexer.AbstractStreamCopyMux;
import net.sf.fmj.media.multiplexer.StreamCopyPushDataSource;
import net.sf.fmj.media.renderer.audio.JavaSoundUtils;
import net.sf.fmj.utility.LoggerSingleton;
import org.jitsi.android.util.javax.sound.sampled.AudioFileFormat.Type;
import org.jitsi.android.util.javax.sound.sampled.AudioFormat;
import org.jitsi.android.util.javax.sound.sampled.AudioInputStream;
import org.jitsi.android.util.javax.sound.sampled.AudioSystem;

public abstract class JavaSoundMux extends AbstractStreamCopyMux {
    private static final int MAX_TRACKS = 1;
    private static final Logger logger = LoggerSingleton.logger;
    private final Type audioFileFormatType;

    private class MyPushDataSource extends StreamCopyPushDataSource {
        final AudioFormat[] javaSoundFormats;

        public MyPushDataSource(ContentDescriptor outputContentDescriptor, int numTracks, InputStream[] inputStreams, Format[] inputFormats) {
            super(outputContentDescriptor, numTracks, inputStreams, inputFormats);
            this.javaSoundFormats = new AudioFormat[numTracks];
            for (int track = 0; track < numTracks; track++) {
                this.javaSoundFormats[track] = JavaSoundUtils.convertFormat((javax.media.format.AudioFormat) inputFormats[track]);
            }
        }

        /* access modifiers changed from: protected */
        public void write(InputStream in, OutputStream out, int track) throws IOException {
            JavaSoundMux.this.write(in, out, this.javaSoundFormats[track]);
        }
    }

    public JavaSoundMux(FileTypeDescriptor fileTypeDescriptor, Type audioFileFormatType) {
        super(fileTypeDescriptor);
        this.audioFileFormatType = audioFileFormatType;
    }

    /* access modifiers changed from: protected */
    public StreamCopyPushDataSource createInputStreamPushDataSource(ContentDescriptor outputContentDescriptor, int numTracks, InputStream[] inputStreams, Format[] inputFormats) {
        return new MyPushDataSource(outputContentDescriptor, numTracks, inputStreams, inputFormats);
    }

    public Format[] getSupportedInputFormats() {
        return new Format[]{new javax.media.format.AudioFormat(javax.media.format.AudioFormat.LINEAR)};
    }

    public int setNumTracks(int numTracks) {
        if (numTracks > 1) {
            numTracks = 1;
        }
        return super.setNumTracks(numTracks);
    }

    /* access modifiers changed from: protected */
    public void write(InputStream in, OutputStream out, AudioFormat javaSoundFormat) throws IOException {
        logger.fine("Audio OutputStream bytes written: " + AudioSystem.write(new AudioInputStream(in, javaSoundFormat, 2147483647L), this.audioFileFormatType, out));
    }
}
