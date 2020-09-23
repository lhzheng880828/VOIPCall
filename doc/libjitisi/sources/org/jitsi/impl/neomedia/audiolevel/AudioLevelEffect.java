package org.jitsi.impl.neomedia.audiolevel;

import javax.media.Buffer;
import javax.media.Effect;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import org.jitsi.impl.neomedia.control.ControlsAdapter;
import org.jitsi.service.neomedia.event.SimpleAudioLevelListener;

public class AudioLevelEffect extends ControlsAdapter implements Effect {
    private static final boolean COPY_DATA_FROM_INPUT_TO_OUTPUT = true;
    private SimpleAudioLevelListener audioLevelListener = null;
    private final AudioLevelEventDispatcher eventDispatcher = new AudioLevelEventDispatcher("AudioLevelEffect Dispatcher");
    private boolean open = false;
    private Format[] supportedAudioFormats;

    public AudioLevelEffect() {
        Format[] formatArr = new Format[1];
        formatArr[0] = new AudioFormat(AudioFormat.LINEAR, -1.0d, 16, 1, 0, 1, 16, -1.0d, Format.byteArray);
        this.supportedAudioFormats = formatArr;
    }

    public void setAudioLevelListener(SimpleAudioLevelListener listener) {
        synchronized (this.eventDispatcher) {
            this.audioLevelListener = listener;
            if (this.open) {
                this.eventDispatcher.setAudioLevelListener(this.audioLevelListener);
            }
        }
    }

    public SimpleAudioLevelListener getAudioLevelListener() {
        SimpleAudioLevelListener simpleAudioLevelListener;
        synchronized (this.eventDispatcher) {
            simpleAudioLevelListener = this.audioLevelListener;
        }
        return simpleAudioLevelListener;
    }

    public Format[] getSupportedInputFormats() {
        return this.supportedAudioFormats;
    }

    public Format[] getSupportedOutputFormats(Format input) {
        Format[] formatArr = new Format[1];
        formatArr[0] = new AudioFormat(AudioFormat.LINEAR, ((AudioFormat) input).getSampleRate(), 16, 1, 0, 1, 16, -1.0d, Format.byteArray);
        return formatArr;
    }

    public Format setInputFormat(Format format) {
        return format instanceof AudioFormat ? (AudioFormat) format : null;
    }

    public Format setOutputFormat(Format format) {
        return format instanceof AudioFormat ? (AudioFormat) format : null;
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer) {
        byte[] bufferData;
        Object data = outputBuffer.getData();
        int inputBufferLength = inputBuffer.getLength();
        if (!(data instanceof byte[]) || ((byte[]) data).length < inputBufferLength) {
            bufferData = new byte[inputBufferLength];
            outputBuffer.setData(bufferData);
        } else {
            bufferData = (byte[]) data;
        }
        outputBuffer.setLength(inputBufferLength);
        outputBuffer.setOffset(0);
        System.arraycopy(inputBuffer.getData(), inputBuffer.getOffset(), bufferData, 0, inputBufferLength);
        outputBuffer.setFormat(inputBuffer.getFormat());
        outputBuffer.setHeader(inputBuffer.getHeader());
        outputBuffer.setSequenceNumber(inputBuffer.getSequenceNumber());
        outputBuffer.setTimeStamp(inputBuffer.getTimeStamp());
        outputBuffer.setFlags(inputBuffer.getFlags());
        outputBuffer.setDiscard(inputBuffer.isDiscard());
        outputBuffer.setEOM(inputBuffer.isEOM());
        outputBuffer.setDuration(inputBuffer.getDuration());
        this.eventDispatcher.addData(outputBuffer);
        return 0;
    }

    public String getName() {
        return "Audio Level Effect";
    }

    public void open() throws ResourceUnavailableException {
        synchronized (this.eventDispatcher) {
            if (!this.open) {
                this.open = true;
                this.eventDispatcher.setAudioLevelListener(this.audioLevelListener);
            }
        }
    }

    public void close() {
        synchronized (this.eventDispatcher) {
            if (this.open) {
                this.open = false;
                this.eventDispatcher.setAudioLevelListener(null);
            }
        }
    }

    public void reset() {
    }
}
