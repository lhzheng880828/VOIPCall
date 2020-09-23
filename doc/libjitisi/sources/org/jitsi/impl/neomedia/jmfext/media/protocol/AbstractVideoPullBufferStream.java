package org.jitsi.impl.neomedia.jmfext.media.protocol;

import java.io.IOException;
import javax.media.Buffer;
import javax.media.control.FormatControl;
import javax.media.control.FrameRateControl;
import javax.media.protocol.PullBufferDataSource;

public abstract class AbstractVideoPullBufferStream<T extends PullBufferDataSource> extends AbstractPullBufferStream<T> {
    private float frameRate;
    private FrameRateControl frameRateControl;
    private long minimumVideoFrameInterval;

    public abstract void doRead(Buffer buffer) throws IOException;

    protected AbstractVideoPullBufferStream(T dataSource, FormatControl formatControl) {
        super(dataSource, formatControl);
    }

    public void read(Buffer buffer) throws IOException {
        FrameRateControl frameRateControl = this.frameRateControl;
        if (frameRateControl != null) {
            float frameRate = frameRateControl.getFrameRate();
            if (frameRate > 0.0f) {
                if (this.frameRate != frameRate) {
                    this.minimumVideoFrameInterval = (long) (1000.0f / frameRate);
                    this.frameRate = frameRate;
                }
                if (this.minimumVideoFrameInterval > 0) {
                    long startTime = System.currentTimeMillis();
                    doRead(buffer);
                    if (!buffer.isDiscard()) {
                        boolean interrupted = false;
                        while (true) {
                            long sleep = this.minimumVideoFrameInterval - (System.currentTimeMillis() - startTime);
                            if (sleep <= 0) {
                                break;
                            }
                            try {
                                Thread.sleep(sleep);
                            } catch (InterruptedException e) {
                                interrupted = true;
                            }
                        }
                        Thread.yield();
                        if (interrupted) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                        return;
                    }
                    return;
                }
            }
        }
        doRead(buffer);
    }

    public void start() throws IOException {
        super.start();
        this.frameRateControl = (FrameRateControl) ((PullBufferDataSource) this.dataSource).getControl(FrameRateControl.class.getName());
    }

    public void stop() throws IOException {
        super.stop();
        this.frameRateControl = null;
    }
}
