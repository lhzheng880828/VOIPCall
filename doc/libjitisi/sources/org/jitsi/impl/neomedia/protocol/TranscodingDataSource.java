package org.jitsi.impl.neomedia.protocol;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import javax.media.Controller;
import javax.media.Format;
import javax.media.Manager;
import javax.media.NoProcessorException;
import javax.media.Processor;
import javax.media.Time;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushDataSource;
import javax.media.protocol.SourceStream;
import org.jitsi.impl.neomedia.ProcessorUtility;
import org.jitsi.impl.neomedia.control.ControlsAdapter;

public class TranscodingDataSource extends DataSource {
    private final DataSource inputDataSource;
    private DataSource outputDataSource;
    private final Format outputFormat;
    private Processor transcodingProcessor;

    public TranscodingDataSource(DataSource inputDataSource, Format outputFormat) {
        super(inputDataSource.getLocator());
        this.inputDataSource = inputDataSource;
        this.outputFormat = outputFormat;
    }

    public synchronized void connect() throws IOException {
        if (this.outputDataSource == null) {
            this.inputDataSource.connect();
            try {
                Processor processor = Manager.createProcessor(this.inputDataSource);
                ProcessorUtility processorUtility = new ProcessorUtility();
                if (processorUtility.waitForState(processor, Processor.Configured)) {
                    TrackControl[] trackControls = processor.getTrackControls();
                    if (trackControls != null) {
                        for (TrackControl trackControl : trackControls) {
                            Format trackFormat = trackControl.getFormat();
                            if ((trackFormat instanceof AudioFormat) && !trackFormat.matches(this.outputFormat)) {
                                Format[] supportedTrackFormats = trackControl.getSupportedFormats();
                                if (supportedTrackFormats != null) {
                                    for (Format supportedTrackFormat : supportedTrackFormats) {
                                        if (supportedTrackFormat.matches(this.outputFormat)) {
                                            Format intersectionFormat = supportedTrackFormat.intersects(this.outputFormat);
                                            if (intersectionFormat != null) {
                                                trackControl.setFormat(intersectionFormat);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (processorUtility.waitForState(processor, Controller.Realized)) {
                        DataSource outputDataSource = processor.getDataOutput();
                        outputDataSource.connect();
                        this.transcodingProcessor = processor;
                        this.outputDataSource = outputDataSource;
                    } else {
                        throw new IOException("Couldn't realize transcoding processor.");
                    }
                }
                throw new IOException("Couldn't configure transcoding processor.");
            } catch (NoProcessorException npex) {
                IOException ioex = new IOException();
                ioex.initCause(npex);
                throw ioex;
            }
        }
    }

    public synchronized void disconnect() {
        if (this.outputDataSource != null) {
            try {
                stop();
                this.outputDataSource.disconnect();
                this.transcodingProcessor.deallocate();
                this.transcodingProcessor.close();
                this.transcodingProcessor = null;
                this.outputDataSource = null;
            } catch (IOException ioex) {
                throw new UndeclaredThrowableException(ioex);
            }
        }
    }

    public synchronized String getContentType() {
        return this.outputDataSource == null ? null : this.outputDataSource.getContentType();
    }

    public synchronized Object getControl(String controlType) {
        return this.outputDataSource.getControl(controlType);
    }

    public synchronized Object[] getControls() {
        return this.outputDataSource == null ? ControlsAdapter.EMPTY_CONTROLS : this.outputDataSource.getControls();
    }

    public synchronized Time getDuration() {
        return this.outputDataSource == null ? DURATION_UNKNOWN : this.outputDataSource.getDuration();
    }

    public synchronized SourceStream[] getStreams() {
        SourceStream[] streams;
        if (this.outputDataSource instanceof PushBufferDataSource) {
            streams = ((PushBufferDataSource) this.outputDataSource).getStreams();
        } else if (this.outputDataSource instanceof PullBufferDataSource) {
            streams = ((PullBufferDataSource) this.outputDataSource).getStreams();
        } else if (this.outputDataSource instanceof PushDataSource) {
            streams = ((PushDataSource) this.outputDataSource).getStreams();
        } else if (this.outputDataSource instanceof PullDataSource) {
            streams = ((PullDataSource) this.outputDataSource).getStreams();
        } else {
            streams = new SourceStream[0];
        }
        return streams;
    }

    public synchronized void start() throws IOException {
        this.outputDataSource.start();
        this.transcodingProcessor.start();
    }

    public synchronized void stop() throws IOException {
        if (this.outputDataSource != null) {
            this.transcodingProcessor.stop();
            this.outputDataSource.stop();
        }
    }

    public Processor getTranscodingProcessor() {
        return this.transcodingProcessor;
    }
}
