package org.jitsi.impl.neomedia.conference;

import java.io.IOException;
import javax.media.Format;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.SourceStream;
import org.jitsi.impl.neomedia.MediaStreamImpl;
import org.jitsi.impl.neomedia.protocol.TranscodingDataSource;
import org.jitsi.util.Logger;

class InDataSourceDesc {
    private static final SourceStream[] EMPTY_STREAMS = new SourceStream[0];
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(InDataSourceDesc.class);
    /* access modifiers changed from: private */
    public Thread connectThread;
    /* access modifiers changed from: private */
    public boolean connected;
    public final DataSource inDataSource;
    public final AudioMixingPushBufferDataSource outDataSource;
    private DataSource transcodingDataSource;

    public InDataSourceDesc(DataSource inDataSource, AudioMixingPushBufferDataSource outDataSource) {
        this.inDataSource = inDataSource;
        this.outDataSource = outDataSource;
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized void connect(final AudioMixer audioMixer) throws IOException {
        final DataSource effectiveInDataSource = this.transcodingDataSource == null ? this.inDataSource : this.transcodingDataSource;
        if (!(effectiveInDataSource instanceof TranscodingDataSource)) {
            audioMixer.connect(effectiveInDataSource, this.inDataSource);
            this.connected = true;
        } else if (this.connectThread == null) {
            this.connectThread = new Thread() {
                public void run() {
                    try {
                        audioMixer.connect(effectiveInDataSource, InDataSourceDesc.this.inDataSource);
                        synchronized (InDataSourceDesc.this) {
                            InDataSourceDesc.this.connected = true;
                        }
                        audioMixer.connected(InDataSourceDesc.this);
                        synchronized (InDataSourceDesc.this) {
                            if (InDataSourceDesc.this.connectThread == Thread.currentThread()) {
                                InDataSourceDesc.this.connectThread = null;
                            }
                        }
                    } catch (IOException ioex) {
                        try {
                            InDataSourceDesc.logger.error("Failed to connect to inDataSource " + MediaStreamImpl.toString(InDataSourceDesc.this.inDataSource), ioex);
                            synchronized (InDataSourceDesc.this) {
                                if (InDataSourceDesc.this.connectThread == Thread.currentThread()) {
                                    InDataSourceDesc.this.connectThread = null;
                                }
                            }
                        } catch (Throwable th) {
                            synchronized (InDataSourceDesc.this) {
                                if (InDataSourceDesc.this.connectThread == Thread.currentThread()) {
                                    InDataSourceDesc.this.connectThread = null;
                                }
                            }
                        }
                    }
                }
            };
            this.connectThread.setDaemon(true);
            this.connectThread.start();
        }
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized boolean createTranscodingDataSource(Format outFormat) {
        boolean z;
        if (this.transcodingDataSource == null) {
            setTranscodingDataSource(new TranscodingDataSource(this.inDataSource, outFormat));
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized void disconnect() {
        if (this.connected) {
            getEffectiveInDataSource().disconnect();
            this.connected = false;
        }
    }

    public synchronized Object getControl(String controlType) {
        DataSource effectiveInDataSource;
        effectiveInDataSource = getEffectiveInDataSource();
        return effectiveInDataSource == null ? null : effectiveInDataSource.getControl(controlType);
    }

    public synchronized DataSource getEffectiveInDataSource() {
        DataSource dataSource;
        dataSource = this.transcodingDataSource == null ? this.inDataSource : this.connected ? this.transcodingDataSource : null;
        return dataSource;
    }

    public DataSource getInDataSource() {
        return this.inDataSource;
    }

    public synchronized SourceStream[] getStreams() {
        SourceStream[] streams;
        if (this.connected) {
            DataSource inDataSource = getEffectiveInDataSource();
            if (inDataSource instanceof PushBufferDataSource) {
                streams = ((PushBufferDataSource) inDataSource).getStreams();
            } else if (inDataSource instanceof PullBufferDataSource) {
                streams = ((PullBufferDataSource) inDataSource).getStreams();
            } else if (inDataSource instanceof TranscodingDataSource) {
                streams = ((TranscodingDataSource) inDataSource).getStreams();
            } else {
                streams = null;
            }
        } else {
            streams = EMPTY_STREAMS;
        }
        return streams;
    }

    public TranscodingDataSource getTranscodingDataSource() {
        return this.transcodingDataSource == null ? null : (TranscodingDataSource) this.transcodingDataSource;
    }

    private synchronized void setTranscodingDataSource(DataSource transcodingDataSource) {
        this.transcodingDataSource = transcodingDataSource;
        this.connected = false;
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized void start() throws IOException {
        if (this.connected) {
            getEffectiveInDataSource().start();
        }
    }

    /* access modifiers changed from: declared_synchronized */
    public synchronized void stop() throws IOException {
        if (this.connected) {
            getEffectiveInDataSource().stop();
        }
    }
}
