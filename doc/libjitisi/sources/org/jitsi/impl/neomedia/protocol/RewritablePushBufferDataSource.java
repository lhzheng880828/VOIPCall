package org.jitsi.impl.neomedia.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedList;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import org.jitsi.impl.neomedia.control.AbstractControls;
import org.jitsi.service.neomedia.DTMFInbandTone;

public class RewritablePushBufferDataSource extends PushBufferDataSourceDelegate<PushBufferDataSource> implements MuteDataSource, InbandDTMFDataSource {
    private boolean mute;
    /* access modifiers changed from: private|final */
    public final LinkedList<DTMFInbandTone> tones = new LinkedList();

    private class MutePushBufferStream extends SourceStreamDelegate<PushBufferStream> implements PushBufferStream {
        public MutePushBufferStream(PushBufferStream stream) {
            super(stream);
        }

        public Format getFormat() {
            return ((PushBufferStream) this.stream).getFormat();
        }

        public void read(Buffer buffer) throws IOException {
            ((PushBufferStream) this.stream).read(buffer);
            if (RewritablePushBufferDataSource.this.isSendingDTMF()) {
                RewritablePushBufferDataSource.sendDTMF(buffer, (DTMFInbandTone) RewritablePushBufferDataSource.this.tones.poll());
            } else if (RewritablePushBufferDataSource.this.isMute()) {
                RewritablePushBufferDataSource.mute(buffer);
            }
        }

        public void setTransferHandler(BufferTransferHandler transferHandler) {
            BufferTransferHandler bufferTransferHandler;
            PushBufferStream pushBufferStream = (PushBufferStream) this.stream;
            if (transferHandler == null) {
                bufferTransferHandler = null;
            } else {
                Object bufferTransferHandler2 = new StreamSubstituteBufferTransferHandler(transferHandler, (PushBufferStream) this.stream, this);
            }
            pushBufferStream.setTransferHandler(bufferTransferHandler2);
        }
    }

    public RewritablePushBufferDataSource(PushBufferDataSource dataSource) {
        super(dataSource);
    }

    public Object getControl(String controlType) {
        return (InbandDTMFDataSource.class.getName().equals(controlType) || MuteDataSource.class.getName().equals(controlType)) ? this : AbstractControls.queryInterface(this.dataSource, controlType);
    }

    public PushBufferStream[] getStreams() {
        PushBufferStream[] streams = ((PushBufferDataSource) this.dataSource).getStreams();
        if (streams != null) {
            for (int streamIndex = 0; streamIndex < streams.length; streamIndex++) {
                PushBufferStream stream = streams[streamIndex];
                if (stream != null) {
                    streams[streamIndex] = new MutePushBufferStream(stream);
                }
            }
        }
        return streams;
    }

    public synchronized boolean isMute() {
        return this.mute;
    }

    public static void mute(Buffer buffer) {
        Object data = buffer.getData();
        if (data != null) {
            Class<?> dataClass = data.getClass();
            int fromIndex = buffer.getOffset();
            int toIndex = fromIndex + buffer.getLength();
            if (Format.byteArray.equals(dataClass)) {
                Arrays.fill((byte[]) data, fromIndex, toIndex, (byte) 0);
            } else if (Format.intArray.equals(dataClass)) {
                Arrays.fill((int[]) data, fromIndex, toIndex, 0);
            } else if (Format.shortArray.equals(dataClass)) {
                Arrays.fill((short[]) data, fromIndex, toIndex, (short) 0);
            }
            buffer.setData(data);
        }
    }

    public synchronized void setMute(boolean mute) {
        this.mute = mute;
    }

    public void addDTMF(DTMFInbandTone tone) {
        this.tones.add(tone);
    }

    public boolean isSendingDTMF() {
        return !this.tones.isEmpty();
    }

    public static void sendDTMF(Buffer buffer, DTMFInbandTone tone) {
        Object data = buffer.getData();
        if (data != null) {
            Format format = buffer.getFormat();
            if (format instanceof AudioFormat) {
                int i;
                AudioFormat audioFormat = (AudioFormat) format;
                int sampleSizeInBits = audioFormat.getSampleSizeInBits();
                short[] samples = tone.getAudioSamples(audioFormat.getSampleRate(), sampleSizeInBits);
                int fromIndex = buffer.getOffset();
                int toIndex = fromIndex + (samples.length * (sampleSizeInBits / 8));
                ByteBuffer newData = ByteBuffer.allocate(toIndex);
                newData.order(audioFormat.getEndian() == 1 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
                Class<?> dataType = data.getClass();
                if (Format.byteArray.equals(dataType)) {
                    newData.put((byte[]) data, 0, fromIndex);
                } else if (Format.shortArray.equals(dataType)) {
                    short[] shortData = (short[]) data;
                    for (i = 0; i < fromIndex; i++) {
                        newData.putShort(shortData[i]);
                    }
                } else if (Format.intArray.equals(dataType)) {
                    int[] intData = (int[]) data;
                    for (i = 0; i < fromIndex; i++) {
                        newData.putInt(intData[i]);
                    }
                }
                switch (sampleSizeInBits) {
                    case 8:
                        for (short s : samples) {
                            newData.put((byte) s);
                        }
                        break;
                    case 16:
                        for (short s2 : samples) {
                            newData.putShort(s2);
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("buffer.format.sampleSizeInBits must be either 8 or 16, not " + sampleSizeInBits);
                }
                if (Format.byteArray.equals(dataType)) {
                    buffer.setData(newData.array());
                } else if (Format.shortArray.equals(dataType)) {
                    buffer.setData(newData.asShortBuffer().array());
                } else if (Format.intArray.equals(dataType)) {
                    buffer.setData(newData.asIntBuffer().array());
                }
                buffer.setLength(toIndex - fromIndex);
            }
        }
    }
}
