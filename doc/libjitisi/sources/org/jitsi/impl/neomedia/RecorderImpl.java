package org.jitsi.impl.neomedia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.media.DataSink;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSinkException;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;
import org.jitsi.impl.neomedia.device.AudioMixerMediaDevice;
import org.jitsi.impl.neomedia.device.MediaDeviceSession;
import org.jitsi.service.neomedia.MediaDirection;
import org.jitsi.service.neomedia.MediaException;
import org.jitsi.service.neomedia.Recorder;
import org.jitsi.service.neomedia.Recorder.Listener;
import org.jitsi.util.SoundFileUtils;

public class RecorderImpl implements Recorder {
    public static final String[] SUPPORTED_FORMATS = new String[]{"mp3"};
    private final AudioMixerMediaDevice device;
    private MediaDeviceSession deviceSession;
    private String filename = null;
    private final List<Listener> listeners = new ArrayList();
    private boolean mute = false;
    private DataSink sink;

    public RecorderImpl(AudioMixerMediaDevice device) {
        if (device == null) {
            throw new NullPointerException("device");
        }
        this.device = device;
    }

    public void addListener(Listener listener) {
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        synchronized (this.listeners) {
            if (!this.listeners.contains(listener)) {
                this.listeners.add(listener);
            }
        }
    }

    private ContentDescriptor getContentDescriptor(String format) throws IllegalArgumentException {
        String type;
        if (SoundFileUtils.wav.equalsIgnoreCase(format)) {
            type = FileTypeDescriptor.WAVE;
        } else if ("mp3".equalsIgnoreCase(format)) {
            type = FileTypeDescriptor.MPEG_AUDIO;
        } else if ("gsm".equalsIgnoreCase(format)) {
            type = FileTypeDescriptor.GSM;
        } else if (SoundFileUtils.au.equalsIgnoreCase(format)) {
            type = FileTypeDescriptor.BASIC_AUDIO;
        } else if (SoundFileUtils.aif.equalsIgnoreCase(format)) {
            type = FileTypeDescriptor.AIFF;
        } else {
            throw new IllegalArgumentException(format + " is not a supported recording format.");
        }
        return new ContentDescriptor(type);
    }

    public List<String> getSupportedFormats() {
        return Arrays.asList(SUPPORTED_FORMATS);
    }

    public void removeListener(Listener listener) {
        if (listener != null) {
            synchronized (this.listeners) {
                this.listeners.remove(listener);
            }
        }
    }

    public void start(String format, String filename) throws IOException, MediaException {
        MediaException mediaException;
        if (this.sink != null) {
            return;
        }
        if (format == null) {
            throw new NullPointerException("format");
        } else if (filename == null) {
            throw new NullPointerException("filename");
        } else {
            this.filename = filename;
            int extensionBeginIndex = filename.lastIndexOf(46);
            if (extensionBeginIndex < 0) {
                filename = filename + '.' + format;
            } else if (extensionBeginIndex == filename.length() - 1) {
                filename = filename + format;
            }
            MediaDeviceSession deviceSession = this.device.createSession();
            try {
                deviceSession.setContentDescriptor(getContentDescriptor(format));
                deviceSession.setMute(this.mute);
                deviceSession.start(MediaDirection.SENDONLY);
                this.deviceSession = deviceSession;
                if (this.deviceSession == null) {
                    throw new MediaException("Failed to create MediaDeviceSession from AudioMixerMediaDevice for the purposes of recording");
                }
                try {
                    DataSink sink = Manager.createDataSink(deviceSession.getOutputDataSource(), new MediaLocator("file:" + filename));
                    sink.open();
                    sink.start();
                    this.sink = sink;
                    if (this.sink == null || null != null) {
                        stop();
                        throw new MediaException("Failed to start recording into file " + filename, null);
                    }
                } catch (NoDataSinkException ndsex) {
                    Throwable exception = ndsex;
                    if (this.sink == null || exception != null) {
                        stop();
                        throw new MediaException("Failed to start recording into file " + filename, exception);
                    }
                } catch (Throwable th) {
                    if (this.sink == null || null != null) {
                        stop();
                        mediaException = new MediaException("Failed to start recording into file " + filename, null);
                    }
                }
            } catch (Throwable th2) {
                if (this.deviceSession == null) {
                    mediaException = new MediaException("Failed to create MediaDeviceSession from AudioMixerMediaDevice for the purposes of recording");
                }
            }
        }
    }

    public void stop() {
        if (this.deviceSession != null) {
            this.deviceSession.close();
            this.deviceSession = null;
        }
        if (this.sink != null) {
            this.sink.close();
            this.sink = null;
            synchronized (this.listeners) {
            }
            for (Listener listener : (Listener[]) this.listeners.toArray(new Listener[this.listeners.size()])) {
                listener.recorderStopped(this);
            }
        }
    }

    public void setMute(boolean mute) {
        this.mute = mute;
        if (this.deviceSession != null) {
            this.deviceSession.setMute(mute);
        }
    }

    public String getFilename() {
        return this.filename;
    }
}
