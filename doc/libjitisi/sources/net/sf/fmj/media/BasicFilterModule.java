package net.sf.fmj.media;

import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.control.FrameProcessingControl;
import net.sf.fmj.filtergraph.SimpleGraphBuilder;
import org.jitsi.android.util.java.awt.Frame;

public class BasicFilterModule extends BasicModule {
    protected final boolean VERBOSE_CONTROL = false;
    protected Codec codec;
    protected Frame controlFrame;
    protected float curFramesBehind = 0.0f;
    private boolean failed = false;
    protected FrameProcessingControl frameControl = null;
    protected InputConnector ic = new BasicInputConnector();
    private Object lastHdr = null;
    private boolean markerSet = false;
    protected OutputConnector oc;
    protected float prevFramesBehind = 0.0f;
    protected boolean readPendingFlag = false;
    protected Buffer storedInputBuffer;
    protected Buffer storedOutputBuffer;
    protected boolean writePendingFlag = false;

    public BasicFilterModule(Codec c) {
        registerInputConnector("input", this.ic);
        this.oc = new BasicOutputConnector();
        registerOutputConnector("output", this.oc);
        setCodec(c);
        this.protocol = 0;
        Object control = c.getControl(FrameProcessingControl.class.getName());
        if (control instanceof FrameProcessingControl) {
            this.frameControl = (FrameProcessingControl) control;
        }
    }

    public void doClose() {
        if (this.codec != null) {
            this.codec.close();
        }
        if (this.controlFrame != null) {
            this.controlFrame.dispose();
            this.controlFrame = null;
        }
    }

    public boolean doPrefetch() {
        return super.doPrefetch();
    }

    public boolean doRealize() {
        if (this.codec != null) {
            try {
                this.codec.open();
            } catch (ResourceUnavailableException e) {
                return false;
            }
        }
        return true;
    }

    public Codec getCodec() {
        return this.codec;
    }

    public Object getControl(String s) {
        return this.codec.getControl(s);
    }

    public Object[] getControls() {
        return this.codec.getControls();
    }

    public boolean isThreaded() {
        if (getProtocol() == 1) {
            return true;
        }
        return false;
    }

    public void process() {
        do {
            Buffer inputBuffer;
            Buffer outputBuffer;
            if (this.readPendingFlag) {
                inputBuffer = this.storedInputBuffer;
            } else {
                inputBuffer = this.ic.getValidBuffer();
                Format incomingFormat = inputBuffer.getFormat();
                if (incomingFormat == null) {
                    incomingFormat = this.ic.getFormat();
                    inputBuffer.setFormat(incomingFormat);
                }
                if (!(incomingFormat == this.ic.getFormat() || incomingFormat == null || incomingFormat.equals(this.ic.getFormat()) || inputBuffer.isDiscard())) {
                    if (this.writePendingFlag) {
                        this.storedOutputBuffer.setDiscard(true);
                        this.oc.writeReport();
                        this.writePendingFlag = false;
                    }
                    if (reinitCodec(inputBuffer.getFormat())) {
                        Format oldFormat = this.ic.getFormat();
                        this.ic.setFormat(inputBuffer.getFormat());
                        if (this.moduleListener != null) {
                            this.moduleListener.formatChanged(this, oldFormat, inputBuffer.getFormat());
                        }
                    } else {
                        inputBuffer.setDiscard(true);
                        this.ic.readReport();
                        this.failed = true;
                        if (this.moduleListener != null) {
                            this.moduleListener.formatChangedFailure(this, this.ic.getFormat(), inputBuffer.getFormat());
                            return;
                        }
                        return;
                    }
                }
                if ((inputBuffer.getFlags() & 1024) != 0) {
                    this.markerSet = true;
                }
            }
            if (this.writePendingFlag) {
                outputBuffer = this.storedOutputBuffer;
            } else {
                outputBuffer = this.oc.getEmptyBuffer();
                if (outputBuffer != null) {
                    outputBuffer.setLength(0);
                    outputBuffer.setOffset(0);
                    this.lastHdr = outputBuffer.getHeader();
                }
            }
            outputBuffer.setTimeStamp(inputBuffer.getTimeStamp());
            outputBuffer.setDuration(inputBuffer.getDuration());
            outputBuffer.setSequenceNumber(inputBuffer.getSequenceNumber());
            outputBuffer.setFlags(inputBuffer.getFlags());
            outputBuffer.setHeader(inputBuffer.getHeader());
            if (this.resetted) {
                if ((inputBuffer.getFlags() & 512) != 0) {
                    this.codec.reset();
                    this.resetted = false;
                }
                this.writePendingFlag = false;
                this.readPendingFlag = false;
                this.ic.readReport();
                this.oc.writeReport();
                return;
            } else if (this.failed || inputBuffer.isDiscard()) {
                if (this.markerSet) {
                    outputBuffer.setFlags(outputBuffer.getFlags() & -1025);
                    this.markerSet = false;
                }
                this.curFramesBehind = 0.0f;
                this.ic.readReport();
                if (!this.writePendingFlag) {
                    this.oc.writeReport();
                    return;
                }
                return;
            } else {
                if (!(this.frameControl == null || this.curFramesBehind == this.prevFramesBehind || (inputBuffer.getFlags() & 32) != 0)) {
                    this.frameControl.setFramesBehind(this.curFramesBehind);
                    this.prevFramesBehind = this.curFramesBehind;
                }
                int rc = 0;
                try {
                    rc = this.codec.process(inputBuffer, outputBuffer);
                } catch (Throwable e) {
                    Log.dumpStack(e);
                    if (this.moduleListener != null) {
                        this.moduleListener.internalErrorOccurred(this);
                    }
                }
                if (PlaybackEngine.TRACE_ON && !verifyBuffer(outputBuffer)) {
                    System.err.println("verify buffer failed: " + this.codec);
                    Thread.dumpStack();
                    if (this.moduleListener != null) {
                        this.moduleListener.internalErrorOccurred(this);
                    }
                }
                if ((rc & 8) != 0) {
                    this.failed = true;
                    if (this.moduleListener != null) {
                        this.moduleListener.pluginTerminated(this);
                    }
                    this.writePendingFlag = false;
                    this.readPendingFlag = false;
                    this.ic.readReport();
                    this.oc.writeReport();
                    return;
                }
                if (this.curFramesBehind > 0.0f && outputBuffer.isDiscard()) {
                    this.curFramesBehind -= 1.0f;
                    if (this.curFramesBehind < 0.0f) {
                        this.curFramesBehind = 0.0f;
                    }
                    rc &= -5;
                }
                if ((rc & 1) != 0) {
                    outputBuffer.setDiscard(true);
                    if (this.markerSet) {
                        outputBuffer.setFlags(outputBuffer.getFlags() & -1025);
                        this.markerSet = false;
                    }
                    this.ic.readReport();
                    this.oc.writeReport();
                    this.writePendingFlag = false;
                    this.readPendingFlag = false;
                    return;
                }
                if (outputBuffer.isEOM() && !((rc & 2) == 0 && (rc & 4) == 0)) {
                    outputBuffer.setEOM(false);
                }
                if ((rc & 4) != 0) {
                    this.writePendingFlag = true;
                    this.storedOutputBuffer = outputBuffer;
                } else {
                    if (this.markerSet) {
                        outputBuffer.setFlags(outputBuffer.getFlags() | 1024);
                        this.markerSet = false;
                    }
                    this.oc.writeReport();
                    this.writePendingFlag = false;
                }
                if ((rc & 2) != 0 || (inputBuffer.isEOM() && !outputBuffer.isEOM())) {
                    this.readPendingFlag = true;
                    this.storedInputBuffer = inputBuffer;
                } else {
                    inputBuffer.setHeader(this.lastHdr);
                    this.ic.readReport();
                    this.readPendingFlag = false;
                }
            }
        } while (this.readPendingFlag);
    }

    /* access modifiers changed from: protected */
    public boolean reinitCodec(Format input) {
        if (this.codec != null) {
            if (this.codec.setInputFormat(input) != null) {
                return true;
            }
            this.codec.close();
            this.codec = null;
        }
        Codec c = SimpleGraphBuilder.findCodec(input, null, null, null);
        if (c == null) {
            return false;
        }
        setCodec(c);
        return true;
    }

    public boolean setCodec(Codec codec) {
        this.codec = codec;
        return true;
    }

    public boolean setCodec(String codec) {
        return true;
    }

    public void setFormat(Connector c, Format f) {
        if (c == this.ic) {
            if (this.codec != null) {
                this.codec.setInputFormat(f);
            }
        } else if (c == this.oc && this.codec != null) {
            this.codec.setOutputFormat(f);
        }
    }

    /* access modifiers changed from: protected */
    public void setFramesBehind(float framesBehind) {
        this.curFramesBehind = framesBehind;
    }
}
