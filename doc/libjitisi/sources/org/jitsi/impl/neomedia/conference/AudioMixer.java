package org.jitsi.impl.neomedia.conference;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.media.Buffer;
import javax.media.CaptureDeviceInfo;
import javax.media.Controls;
import javax.media.Format;
import javax.media.Time;
import javax.media.control.BufferControl;
import javax.media.control.FormatControl;
import javax.media.format.AudioFormat;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.PushBufferStream;
import javax.media.protocol.PushDataSource;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.SourceStream;
import org.jitsi.impl.neomedia.MediaStreamImpl;
import org.jitsi.impl.neomedia.control.ReadOnlyBufferControlDelegate;
import org.jitsi.impl.neomedia.control.ReadOnlyFormatControlDelegate;
import org.jitsi.impl.neomedia.device.MediaDeviceImpl;
import org.jitsi.impl.neomedia.device.ReceiveStreamPushBufferDataSource;
import org.jitsi.impl.neomedia.protocol.BufferStreamAdapter;
import org.jitsi.impl.neomedia.protocol.CachingPushBufferStream;
import org.jitsi.impl.neomedia.protocol.PullBufferStreamAdapter;
import org.jitsi.impl.neomedia.protocol.PushBufferDataSourceAdapter;
import org.jitsi.impl.neomedia.protocol.PushBufferStreamAdapter;
import org.jitsi.impl.neomedia.protocol.TranscodingDataSource;
import org.jitsi.util.Logger;
import org.jitsi.util.OSUtils;

public class AudioMixer {
    private static final AudioFormat DEFAULT_OUTPUT_FORMAT = new AudioFormat(AudioFormat.LINEAR, 8000.0d, 16, 1, 0, 1);
    private static final Logger logger = Logger.getLogger(AudioMixer.class);
    private BufferControl bufferControl;
    protected final CaptureDevice captureDevice;
    private int connected;
    private final List<InDataSourceDesc> inDataSources = new ArrayList();
    private final AudioMixingPushBufferDataSource localOutDataSource;
    private AudioMixerPushBufferStream outStream;
    private int started;
    private long startedGeneration;

    private static Format getFormat(DataSource dataSource) {
        FormatControl formatControl = (FormatControl) dataSource.getControl(FormatControl.class.getName());
        return formatControl == null ? null : formatControl.getFormat();
    }

    private static Format getFormat(SourceStream stream) {
        if (stream instanceof PushBufferStream) {
            return ((PushBufferStream) stream).getFormat();
        }
        if (stream instanceof PullBufferStream) {
            return ((PullBufferStream) stream).getFormat();
        }
        return null;
    }

    public AudioMixer(CaptureDevice captureDevice) {
        if (captureDevice instanceof PullBufferDataSource) {
            captureDevice = new PushBufferDataSourceAdapter((PullBufferDataSource) captureDevice);
        }
        if (logger.isTraceEnabled()) {
            captureDevice = MediaDeviceImpl.createTracingCaptureDevice(captureDevice, logger);
        }
        this.captureDevice = captureDevice;
        this.localOutDataSource = createOutDataSource();
        addInDataSource((DataSource) this.captureDevice, this.localOutDataSource);
    }

    public void addInDataSource(DataSource inDataSource) {
        addInDataSource(inDataSource, null);
    }

    /* access modifiers changed from: 0000 */
    public void addInDataSource(DataSource inDataSource, AudioMixingPushBufferDataSource outDataSource) {
        if (inDataSource == null) {
            throw new NullPointerException("inDataSource");
        }
        synchronized (this.inDataSources) {
            for (InDataSourceDesc inDataSourceDesc : this.inDataSources) {
                if (inDataSource.equals(inDataSourceDesc.inDataSource)) {
                    throw new IllegalArgumentException("inDataSource");
                }
            }
            InDataSourceDesc inDataSourceDesc2 = new InDataSourceDesc(inDataSource, outDataSource);
            if (this.inDataSources.add(inDataSourceDesc2)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Added input DataSource with hashCode " + inDataSource.hashCode());
                }
                if (this.connected > 0) {
                    try {
                        inDataSourceDesc2.connect(this);
                    } catch (IOException ioe) {
                        throw new UndeclaredThrowableException(ioe);
                    } catch (IOException ioex) {
                        throw new UndeclaredThrowableException(ioex);
                    }
                }
                if (this.outStream != null) {
                    getOutStream();
                }
                if (this.started > 0) {
                    inDataSourceDesc2.start();
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void connect() throws IOException {
        synchronized (this.inDataSources) {
            if (this.connected == 0) {
                for (InDataSourceDesc inDataSourceDesc : this.inDataSources) {
                    try {
                        inDataSourceDesc.connect(this);
                    } catch (IOException ioe) {
                        logger.error("Failed to connect to inDataSource " + MediaStreamImpl.toString(inDataSourceDesc.inDataSource), ioe);
                        throw ioe;
                    }
                }
                if (this.outStream != null) {
                    this.outStream.equalizeInStreamBufferLength();
                }
            }
            this.connected++;
        }
    }

    /* access modifiers changed from: protected */
    public void connect(DataSource dataSource, DataSource inDataSource) throws IOException {
        dataSource.connect();
    }

    /* access modifiers changed from: 0000 */
    public void connected(InDataSourceDesc inDataSource) throws IOException {
        synchronized (this.inDataSources) {
            if (this.inDataSources.contains(inDataSource) && this.connected > 0) {
                if (this.started > 0) {
                    inDataSource.start();
                }
                if (this.outStream != null) {
                    getOutStream();
                }
            }
        }
    }

    private InStreamDesc createInStreamDesc(SourceStream inStream, InDataSourceDesc inDataSourceDesc) {
        return new InStreamDesc(inStream, inDataSourceDesc);
    }

    public AudioMixingPushBufferDataSource createOutDataSource() {
        return new AudioMixingPushBufferDataSource(this);
    }

    private boolean createTranscodingDataSource(InDataSourceDesc inDataSourceDesc, Format outFormat) throws IOException {
        if (!inDataSourceDesc.createTranscodingDataSource(outFormat)) {
            return false;
        }
        if (this.connected > 0) {
            inDataSourceDesc.connect(this);
        }
        if (this.started > 0) {
            inDataSourceDesc.start();
        }
        return true;
    }

    /* access modifiers changed from: 0000 */
    /* JADX WARNING: Missing block: B:19:?, code skipped:
            return;
     */
    public void disconnect() {
        /*
        r6 = this;
        r3 = r6.inDataSources;
        monitor-enter(r3);
        r2 = r6.connected;	 Catch:{ all -> 0x0029 }
        if (r2 > 0) goto L_0x0009;
    L_0x0007:
        monitor-exit(r3);	 Catch:{ all -> 0x0029 }
    L_0x0008:
        return;
    L_0x0009:
        r2 = r6.connected;	 Catch:{ all -> 0x0029 }
        r2 = r2 + -1;
        r6.connected = r2;	 Catch:{ all -> 0x0029 }
        r2 = r6.connected;	 Catch:{ all -> 0x0029 }
        if (r2 != 0) goto L_0x0039;
    L_0x0013:
        r2 = r6.inDataSources;	 Catch:{ all -> 0x0029 }
        r0 = r2.iterator();	 Catch:{ all -> 0x0029 }
    L_0x0019:
        r2 = r0.hasNext();	 Catch:{ all -> 0x0029 }
        if (r2 == 0) goto L_0x002c;
    L_0x001f:
        r1 = r0.next();	 Catch:{ all -> 0x0029 }
        r1 = (org.jitsi.impl.neomedia.conference.InDataSourceDesc) r1;	 Catch:{ all -> 0x0029 }
        r1.disconnect();	 Catch:{ all -> 0x0029 }
        goto L_0x0019;
    L_0x0029:
        r2 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x0029 }
        throw r2;
    L_0x002c:
        r2 = r6.outStream;	 Catch:{ all -> 0x0029 }
        r4 = 0;
        r2.setInStreams(r4);	 Catch:{ all -> 0x0029 }
        r2 = 0;
        r6.outStream = r2;	 Catch:{ all -> 0x0029 }
        r4 = 0;
        r6.startedGeneration = r4;	 Catch:{ all -> 0x0029 }
    L_0x0039:
        monitor-exit(r3);	 Catch:{ all -> 0x0029 }
        goto L_0x0008;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.conference.AudioMixer.disconnect():void");
    }

    /* access modifiers changed from: 0000 */
    public BufferControl getBufferControl() {
        if (this.bufferControl == null && (this.captureDevice instanceof Controls)) {
            BufferControl captureDeviceBufferControl = (BufferControl) ((Controls) this.captureDevice).getControl(BufferControl.class.getName());
            if (captureDeviceBufferControl != null) {
                this.bufferControl = new ReadOnlyBufferControlDelegate(captureDeviceBufferControl);
            }
        }
        return this.bufferControl;
    }

    /* access modifiers changed from: 0000 */
    public CaptureDeviceInfo getCaptureDeviceInfo() {
        return this.captureDevice.getCaptureDeviceInfo();
    }

    /* access modifiers changed from: 0000 */
    public String getContentType() {
        return ContentDescriptor.RAW;
    }

    /* access modifiers changed from: 0000 */
    public Time getDuration() {
        return ((DataSource) this.captureDevice).getDuration();
    }

    private InStreamDesc getExistingInStreamDesc(SourceStream inStream, InStreamDesc[] existingInStreamDescs) {
        if (existingInStreamDescs == null) {
            return null;
        }
        for (InStreamDesc existingInStreamDesc : existingInStreamDescs) {
            SourceStream existingInStream = existingInStreamDesc.getInStream();
            if (existingInStream == inStream) {
                return existingInStreamDesc;
            }
            if ((existingInStream instanceof BufferStreamAdapter) && ((BufferStreamAdapter) existingInStream).getStream() == inStream) {
                return existingInStreamDesc;
            }
            if ((existingInStream instanceof CachingPushBufferStream) && ((CachingPushBufferStream) existingInStream).getStream() == inStream) {
                return existingInStreamDesc;
            }
        }
        return null;
    }

    /* access modifiers changed from: 0000 */
    public FormatControl[] getFormatControls() {
        FormatControl[] formatControls = this.captureDevice.getFormatControls();
        if (!(OSUtils.IS_ANDROID || formatControls == null)) {
            for (int i = 0; i < formatControls.length; i++) {
                formatControls[i] = new ReadOnlyFormatControlDelegate(formatControls[i]);
            }
        }
        return formatControls;
    }

    private boolean getInStreamsFromInDataSource(InDataSourceDesc inDataSourceDesc, AudioFormat outFormat, InStreamDesc[] existingInStreams, List<InStreamDesc> inStreams) {
        SourceStream[] inDataSourceStreams = inDataSourceDesc.getStreams();
        Format inFormat;
        InStreamDesc inStreamDesc;
        if (inDataSourceStreams != null) {
            boolean added = false;
            for (SourceStream inStream : inDataSourceStreams) {
                inFormat = getFormat(inStream);
                if (inFormat != null && matches(inFormat, outFormat)) {
                    inStreamDesc = getExistingInStreamDesc(inStream, existingInStreams);
                    if (inStreamDesc == null) {
                        inStreamDesc = createInStreamDesc(inStream, inDataSourceDesc);
                    }
                    if (inStreams.add(inStreamDesc)) {
                        added = true;
                    }
                }
            }
            return added;
        }
        DataSource inDataSource = inDataSourceDesc.getEffectiveInDataSource();
        if (inDataSource == null) {
            return false;
        }
        inFormat = getFormat(inDataSource);
        if (inFormat == null || matches(inFormat, outFormat)) {
            return false;
        }
        if (inDataSource instanceof PushDataSource) {
            for (PushSourceStream inStream2 : ((PushDataSource) inDataSource).getStreams()) {
                inStreamDesc = getExistingInStreamDesc(inStream2, existingInStreams);
                if (inStreamDesc == null) {
                    inStreamDesc = createInStreamDesc(new PushBufferStreamAdapter(inStream2, inFormat), inDataSourceDesc);
                }
                inStreams.add(inStreamDesc);
            }
            return true;
        } else if (!(inDataSource instanceof PullDataSource)) {
            return false;
        } else {
            for (PullSourceStream inStream3 : ((PullDataSource) inDataSource).getStreams()) {
                inStreamDesc = getExistingInStreamDesc(inStream3, existingInStreams);
                if (inStreamDesc == null) {
                    inStreamDesc = createInStreamDesc(new PullBufferStreamAdapter(inStream3, inFormat), inDataSourceDesc);
                }
                inStreams.add(inStreamDesc);
            }
            return true;
        }
    }

    private Collection<InStreamDesc> getInStreamsFromInDataSources(AudioFormat outFormat, InStreamDesc[] existingInStreams) throws IOException {
        List<InStreamDesc> inStreams = new ArrayList();
        synchronized (this.inDataSources) {
            for (InDataSourceDesc inDataSourceDesc : this.inDataSources) {
                if (!getInStreamsFromInDataSource(inDataSourceDesc, outFormat, existingInStreams, inStreams) && createTranscodingDataSource(inDataSourceDesc, outFormat)) {
                    getInStreamsFromInDataSource(inDataSourceDesc, outFormat, existingInStreams, inStreams);
                }
            }
        }
        return inStreams;
    }

    public AudioMixingPushBufferDataSource getLocalOutDataSource() {
        return this.localOutDataSource;
    }

    private AudioFormat getOutFormatFromInDataSources() {
        String formatControlType = FormatControl.class.getName();
        AudioFormat outFormat = null;
        synchronized (this.inDataSources) {
            for (InDataSourceDesc inDataSource : this.inDataSources) {
                DataSource effectiveInDataSource = inDataSource.getEffectiveInDataSource();
                if (effectiveInDataSource != null) {
                    FormatControl formatControl = (FormatControl) effectiveInDataSource.getControl(formatControlType);
                    if (formatControl != null) {
                        AudioFormat format = (AudioFormat) formatControl.getFormat();
                        if (format != null) {
                            int signed = format.getSigned();
                            if (1 == signed || -1 == signed) {
                                int endian = format.getEndian();
                                if (endian == 0 || -1 == endian) {
                                    outFormat = format;
                                    break;
                                }
                            }
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
        if (outFormat == null) {
            outFormat = DEFAULT_OUTPUT_FORMAT;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Determined outFormat of AudioMixer from inDataSources to be " + outFormat);
        }
        return outFormat;
    }

    /* access modifiers changed from: 0000 */
    public AudioMixerPushBufferStream getOutStream() {
        AudioMixerPushBufferStream audioMixerPushBufferStream;
        synchronized (this.inDataSources) {
            AudioFormat outFormat;
            if (this.outStream == null) {
                outFormat = getOutFormatFromInDataSources();
            } else {
                outFormat = this.outStream.getFormat();
            }
            setOutFormatToInDataSources(outFormat);
            try {
                Collection<InStreamDesc> inStreams = getInStreamsFromInDataSources(outFormat, this.outStream == null ? null : this.outStream.getInStreams());
                if (this.outStream == null) {
                    this.outStream = new AudioMixerPushBufferStream(this, outFormat);
                    this.startedGeneration = 0;
                }
                this.outStream.setInStreams(inStreams);
                audioMixerPushBufferStream = this.outStream;
            } catch (IOException ioex) {
                throw new UndeclaredThrowableException(ioex);
            }
        }
        return audioMixerPushBufferStream;
    }

    public TranscodingDataSource getTranscodingDataSource(DataSource inDataSource) {
        for (InDataSourceDesc inDataSourceDesc : this.inDataSources) {
            DataSource ourDataSource = inDataSourceDesc.getInDataSource();
            if (ourDataSource == inDataSource) {
                return inDataSourceDesc.getTranscodingDataSource();
            }
            if ((ourDataSource instanceof ReceiveStreamPushBufferDataSource) && ((ReceiveStreamPushBufferDataSource) ourDataSource).getDataSource() == inDataSource) {
                return inDataSourceDesc.getTranscodingDataSource();
            }
        }
        return null;
    }

    private boolean matches(Format input, AudioFormat pattern) {
        return (input instanceof AudioFormat) && input.isSameEncoding((Format) pattern);
    }

    /* access modifiers changed from: protected */
    public void read(PushBufferStream stream, Buffer buffer, DataSource dataSource) throws IOException {
        stream.read(buffer);
    }

    public void removeInDataSources(DataSourceFilter dataSourceFilter) {
        synchronized (this.inDataSources) {
            Iterator<InDataSourceDesc> inDataSourceIter = this.inDataSources.iterator();
            boolean removed = false;
            while (inDataSourceIter.hasNext()) {
                if (dataSourceFilter.accept(((InDataSourceDesc) inDataSourceIter.next()).inDataSource)) {
                    inDataSourceIter.remove();
                    removed = true;
                }
            }
            if (removed && this.outStream != null) {
                getOutStream();
            }
        }
    }

    private void setOutFormatToInDataSources(AudioFormat outFormat) {
        String formatControlType = FormatControl.class.getName();
        synchronized (this.inDataSources) {
            for (InDataSourceDesc inDataSourceDesc : this.inDataSources) {
                FormatControl formatControl = (FormatControl) inDataSourceDesc.getControl(formatControlType);
                if (formatControl != null) {
                    Format inFormat = formatControl.getFormat();
                    if (inFormat == null || !matches(inFormat, outFormat)) {
                        Format setFormat = formatControl.setFormat(outFormat);
                        if (setFormat == null) {
                            logger.error("Failed to set format of inDataSource to " + outFormat);
                        } else if (setFormat != outFormat) {
                            logger.warn("Failed to change format of inDataSource from " + setFormat + " to " + outFormat);
                        } else if (logger.isTraceEnabled()) {
                            logger.trace("Set format of inDataSource to " + setFormat);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void start(AudioMixerPushBufferStream outStream, long generation) throws IOException {
        synchronized (this.inDataSources) {
            if (this.outStream != outStream) {
            } else if (this.startedGeneration < generation) {
                this.startedGeneration = generation;
                if (this.started == 0) {
                    for (InDataSourceDesc inDataSourceDesc : this.inDataSources) {
                        inDataSourceDesc.start();
                    }
                }
                this.started++;
            }
        }
    }

    /* access modifiers changed from: 0000 */
    /* JADX WARNING: Missing block: B:29:?, code skipped:
            return;
     */
    public void stop(org.jitsi.impl.neomedia.conference.AudioMixerPushBufferStream r7, long r8) throws java.io.IOException {
        /*
        r6 = this;
        r3 = r6.inDataSources;
        monitor-enter(r3);
        r2 = r6.outStream;	 Catch:{ all -> 0x0017 }
        if (r2 == r7) goto L_0x0009;
    L_0x0007:
        monitor-exit(r3);	 Catch:{ all -> 0x0017 }
    L_0x0008:
        return;
    L_0x0009:
        r4 = r6.startedGeneration;	 Catch:{ all -> 0x0017 }
        r2 = (r4 > r8 ? 1 : (r4 == r8 ? 0 : -1));
        if (r2 >= 0) goto L_0x001a;
    L_0x000f:
        r6.startedGeneration = r8;	 Catch:{ all -> 0x0017 }
        r2 = r6.started;	 Catch:{ all -> 0x0017 }
        if (r2 > 0) goto L_0x001c;
    L_0x0015:
        monitor-exit(r3);	 Catch:{ all -> 0x0017 }
        goto L_0x0008;
    L_0x0017:
        r2 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x0017 }
        throw r2;
    L_0x001a:
        monitor-exit(r3);	 Catch:{ all -> 0x0017 }
        goto L_0x0008;
    L_0x001c:
        r2 = r6.started;	 Catch:{ all -> 0x0017 }
        r2 = r2 + -1;
        r6.started = r2;	 Catch:{ all -> 0x0017 }
        r2 = r6.started;	 Catch:{ all -> 0x0017 }
        if (r2 != 0) goto L_0x003c;
    L_0x0026:
        r2 = r6.inDataSources;	 Catch:{ all -> 0x0017 }
        r0 = r2.iterator();	 Catch:{ all -> 0x0017 }
    L_0x002c:
        r2 = r0.hasNext();	 Catch:{ all -> 0x0017 }
        if (r2 == 0) goto L_0x003c;
    L_0x0032:
        r1 = r0.next();	 Catch:{ all -> 0x0017 }
        r1 = (org.jitsi.impl.neomedia.conference.InDataSourceDesc) r1;	 Catch:{ all -> 0x0017 }
        r1.stop();	 Catch:{ all -> 0x0017 }
        goto L_0x002c;
    L_0x003c:
        monitor-exit(r3);	 Catch:{ all -> 0x0017 }
        goto L_0x0008;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.impl.neomedia.conference.AudioMixer.stop(org.jitsi.impl.neomedia.conference.AudioMixerPushBufferStream, long):void");
    }
}
