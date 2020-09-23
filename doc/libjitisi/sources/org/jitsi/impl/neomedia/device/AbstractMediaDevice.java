package org.jitsi.impl.neomedia.device;

import java.io.IOException;
import java.util.List;
import javax.media.Manager;
import javax.media.Processor;
import javax.media.Renderer;
import javax.media.protocol.DataSource;
import org.jitsi.service.neomedia.RTPExtension;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.format.MediaFormat;

public abstract class AbstractMediaDevice implements MediaDevice {
    public abstract DataSource createOutputDataSource();

    public void connect(DataSource captureDevice) throws IOException {
        if (captureDevice == null) {
            throw new NullPointerException("captureDevice");
        }
        try {
            captureDevice.connect();
        } catch (NullPointerException npe) {
            IOException ioe = new IOException();
            ioe.initCause(npe);
            throw ioe;
        }
    }

    /* access modifiers changed from: protected */
    public Processor createPlayer(DataSource dataSource) throws Exception {
        Processor player = null;
        dataSource.connect();
        try {
            player = Manager.createProcessor(dataSource);
            return player;
        } finally {
            if (player == null) {
                dataSource.disconnect();
            }
        }
    }

    /* access modifiers changed from: protected */
    public Renderer createRenderer() {
        return null;
    }

    public MediaDeviceSession createSession() {
        switch (getMediaType()) {
            case VIDEO:
                return new VideoMediaDeviceSession(this);
            default:
                return new AudioMediaDeviceSession(this);
        }
    }

    public List<RTPExtension> getSupportedExtensions() {
        return null;
    }

    public List<MediaFormat> getSupportedFormats() {
        return getSupportedFormats(null, null);
    }
}
