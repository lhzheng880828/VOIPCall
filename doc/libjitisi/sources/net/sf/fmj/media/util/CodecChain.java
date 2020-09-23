package net.sf.fmj.media.util;

import java.util.Vector;
import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Format;
import javax.media.Renderer;
import javax.media.ResourceUnavailableException;
import net.sf.fmj.filtergraph.SimpleGraphBuilder;
import net.sf.fmj.media.Log;
import org.jitsi.android.util.java.awt.Component;

public class CodecChain {
    static final int STAGES = 5;
    protected Buffer[] buffers = null;
    protected Codec[] codecs = null;
    private boolean deallocated = true;
    protected boolean firstBuffer = true;
    protected Format[] formats = null;
    protected Renderer renderer = null;
    private boolean rtpFormat = false;

    /* access modifiers changed from: protected */
    public boolean buildChain(Format input) {
        Vector formatList = new Vector(10);
        Vector pluginList = SimpleGraphBuilder.findRenderingChain(input, formatList);
        if (pluginList == null) {
            return false;
        }
        int len = pluginList.size();
        this.codecs = new Codec[(len - 1)];
        this.buffers = new Buffer[(len - 1)];
        this.formats = new Format[len];
        this.formats[0] = input;
        Log.comment("Monitor codec chain:");
        for (int j = 0; j < this.codecs.length; j++) {
            this.codecs[j] = (Codec) pluginList.elementAt((len - j) - 1);
            this.formats[j + 1] = (Format) formatList.elementAt((len - j) - 2);
            this.buffers[j] = new Buffer();
            this.buffers[j].setFormat(this.formats[j + 1]);
            Log.write("    codec: " + this.codecs[j]);
            Log.write("      format: " + this.formats[j]);
        }
        this.renderer = (Renderer) pluginList.elementAt(0);
        Log.write("    renderer: " + this.renderer);
        Log.write("      format: " + this.formats[this.codecs.length] + "\n");
        if (input.getEncoding() != null && input.getEncoding().toUpperCase().endsWith("RTP")) {
            this.rtpFormat = true;
        }
        return true;
    }

    public void close() {
        for (Codec close : this.codecs) {
            close.close();
        }
        if (this.renderer != null) {
            this.renderer.close();
        }
    }

    public void deallocate() {
        if (!this.deallocated) {
            if (this.renderer != null) {
                this.renderer.close();
            }
            this.deallocated = true;
        }
    }

    private int doProcess(int codecNo, Buffer input, boolean render) {
        Format format = input.getFormat();
        if (codecNo != this.codecs.length) {
            int returnVal;
            if (isRawFormat(format)) {
                if (!render) {
                    return 0;
                }
            } else if (!this.rtpFormat && this.firstBuffer) {
                if ((input.getFlags() & 16) == 0) {
                    return 0;
                }
                this.firstBuffer = false;
            }
            Codec codec = this.codecs[codecNo];
            if (!(codec == null || this.formats[codecNo] == null || this.formats[codecNo] == format || this.formats[codecNo].equals(format) || input.isDiscard())) {
                if (codec.setInputFormat(format) == null) {
                    Log.error("Monitor failed to handle mid-stream format change:");
                    Log.error("  old: " + this.formats[codecNo]);
                    Log.error("  new: " + format);
                    return 1;
                }
                this.formats[codecNo] = format;
            }
            do {
                try {
                    returnVal = codec.process(input, this.buffers[codecNo]);
                    if (returnVal == 1) {
                        return 1;
                    }
                    if ((returnVal & 4) == 0) {
                        if (!(this.buffers[codecNo].isDiscard() || this.buffers[codecNo].isEOM())) {
                            doProcess(codecNo + 1, this.buffers[codecNo], render);
                        }
                        this.buffers[codecNo].setOffset(0);
                        this.buffers[codecNo].setLength(0);
                        this.buffers[codecNo].setFlags(0);
                    }
                } catch (Exception e) {
                    Log.dumpStack(e);
                    return 1;
                } catch (Error err) {
                    Log.dumpStack(err);
                    return 1;
                }
            } while ((returnVal & 2) != 0);
            return returnVal;
        } else if (!render) {
            return 0;
        } else {
            if (!(this.renderer == null || this.formats[codecNo] == null || this.formats[codecNo] == format || this.formats[codecNo].equals(format) || input.isDiscard())) {
                if (this.renderer.setInputFormat(format) == null) {
                    Log.error("Monitor failed to handle mid-stream format change:");
                    Log.error("  old: " + this.formats[codecNo]);
                    Log.error("  new: " + format);
                    return 1;
                }
                this.formats[codecNo] = format;
            }
            try {
                return this.renderer.process(input);
            } catch (Exception e2) {
                Log.dumpStack(e2);
                return 1;
            } catch (Error err2) {
                Log.dumpStack(err2);
                return 1;
            }
        }
    }

    public Component getControlComponent() {
        return null;
    }

    /* access modifiers changed from: 0000 */
    public boolean isRawFormat(Format format) {
        return false;
    }

    public boolean prefetch() {
        if (!this.deallocated) {
            return true;
        }
        try {
            this.renderer.open();
            this.renderer.start();
            this.deallocated = false;
            return true;
        } catch (ResourceUnavailableException e) {
            return false;
        }
    }

    public int process(Buffer buffer, boolean render) {
        return doProcess(0, buffer, render);
    }

    public void reset() {
        this.firstBuffer = true;
        for (int i = 0; i < this.codecs.length; i++) {
            if (this.codecs[i] != null) {
                this.codecs[i].reset();
            }
        }
    }
}
