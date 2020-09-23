package net.sf.fmj.media;

import java.util.Vector;
import javax.media.Codec;
import javax.media.Controller;
import javax.media.Format;
import javax.media.Multiplexer;
import javax.media.NotConfiguredError;
import javax.media.NotRealizedError;
import javax.media.Owned;
import javax.media.PlugIn;
import javax.media.PlugInManager;
import javax.media.Processor;
import javax.media.Renderer;
import javax.media.Track;
import javax.media.UnsupportedPlugInException;
import javax.media.control.FrameRateControl;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import net.sf.fmj.filtergraph.GraphNode;
import net.sf.fmj.filtergraph.SimpleGraphBuilder;
import net.sf.fmj.media.codec.video.colorspace.RGBScaler;
import net.sf.fmj.media.control.ProgressControl;
import net.sf.fmj.media.util.Resource;
import org.jitsi.android.util.java.awt.Dimension;

public class ProcessEngine extends PlaybackEngine {
    protected BasicMuxModule muxModule;
    protected ContentDescriptor outputContentDes = null;
    String prefetchError = ("Failed to prefetch: " + this);
    protected GraphNode targetMux = null;
    protected Format[] targetMuxFormats = null;
    protected Vector targetMuxNames = null;
    protected GraphNode[] targetMuxes = null;

    class ProcGraphBuilder extends SimpleGraphBuilder {
        Codec[] codecs = null;
        protected ProcessEngine engine;
        Format format = null;
        protected int nodesVisited = 0;
        protected int numTracks = 1;
        Renderer rend = null;
        protected Format targetFormat;
        protected int trackID = 0;

        ProcGraphBuilder(ProcessEngine engine) {
            this.engine = engine;
        }

        /* access modifiers changed from: 0000 */
        public GraphNode buildCustomGraph(Format in) {
            GraphNode graphNode;
            Vector candidates = new Vector();
            GraphNode node = new GraphNode(null, (PlugIn) null, in, null, 0);
            candidates.addElement(node);
            Log.comment("Custom options specified.");
            this.indent = 1;
            Log.setIndent(this.indent);
            if (this.codecs != null) {
                resetTargets();
                for (int i = 0; i < this.codecs.length; i++) {
                    if (this.codecs[i] != null) {
                        Log.comment("A custom codec is specified: " + this.codecs[i]);
                        setTargetPlugin(this.codecs[i], 2);
                        node = buildGraph(candidates);
                        if (node == null) {
                            Log.error("The input format is not compatible with the given codec plugin: " + this.codecs[i]);
                            this.indent = 0;
                            Log.setIndent(this.indent);
                            return null;
                        }
                        node.level = 0;
                        candidates = new Vector();
                        candidates.addElement(node);
                    }
                }
            }
            if (ProcessEngine.this.outputContentDes != null) {
                resetTargets();
                if (this.format != null) {
                    this.targetFormat = this.format;
                    Log.comment("An output format is specified: " + this.format);
                }
                if (setDefaultTargetMux()) {
                    node = buildGraph(candidates);
                    if (node == null) {
                        Log.error("Failed to build a graph for the given custom options.");
                        this.indent = 0;
                        Log.setIndent(this.indent);
                        graphNode = node;
                        return null;
                    }
                }
                graphNode = node;
                return null;
            }
            if (this.format != null) {
                resetTargets();
                this.targetFormat = this.format;
                Log.comment("An output format is specified: " + this.format);
                node = buildGraph(candidates);
                if (node == null) {
                    Log.error("The input format cannot be transcoded to the specified target format.");
                    this.indent = 0;
                    Log.setIndent(this.indent);
                    graphNode = node;
                    return null;
                }
                node.level = 0;
                candidates = new Vector();
                candidates.addElement(node);
                this.targetFormat = null;
            }
            if (this.rend != null) {
                Log.comment("A custom renderer is specified: " + this.rend);
                setTargetPlugin(this.rend, 4);
                node = buildGraph(candidates);
                if (node == null) {
                    if (this.format != null) {
                        Log.error("The customed transocoded format is not compatible with the given renderer plugin: " + this.rend);
                    } else {
                        Log.error("The input format is not compatible with the given renderer plugin: " + this.rend);
                    }
                    this.indent = 0;
                    Log.setIndent(this.indent);
                    graphNode = node;
                    return null;
                }
            }
            if (this.format != null) {
                in = this.format;
            }
            if (setDefaultTargetRenderer(in)) {
                node = buildGraph(candidates);
                if (node == null) {
                    if (this.format != null) {
                        Log.error("Failed to find a renderer that supports the customed transcoded format.");
                    } else {
                        Log.error("Failed to build a graph to render the input format with the given custom options.");
                    }
                    this.indent = 0;
                    Log.setIndent(this.indent);
                    graphNode = node;
                    return null;
                }
            }
            graphNode = node;
            return null;
            this.indent = 0;
            Log.setIndent(this.indent);
            graphNode = node;
            return node;
        }

        /* access modifiers changed from: 0000 */
        public boolean buildCustomGraph(ProcTControl tc) {
            this.codecs = tc.codecChainWanted;
            this.rend = tc.rendererWanted;
            this.format = tc.formatWanted;
            if ((this.format instanceof VideoFormat) && (tc.getOriginalFormat() instanceof VideoFormat)) {
                Dimension s1 = ((VideoFormat) tc.getOriginalFormat()).getSize();
                Dimension s2 = ((VideoFormat) this.format).getSize();
                if (!(s1 == null || s2 == null || s1.equals(s2))) {
                    RGBScaler scaler = new RGBScaler(s2);
                    if (this.codecs == null || this.codecs.length == 0) {
                        this.codecs = new Codec[1];
                        this.codecs[0] = scaler;
                    } else {
                        int i;
                        this.codecs = new Codec[(tc.codecChainWanted.length + 1)];
                        if (PlaybackEngine.isRawVideo(this.format)) {
                            this.codecs[tc.codecChainWanted.length] = scaler;
                            i = 0;
                        } else {
                            this.codecs[0] = scaler;
                            i = 1;
                        }
                        int j = 0;
                        while (j < tc.codecChainWanted.length) {
                            int i2 = i + 1;
                            this.codecs[i] = tc.codecChainWanted[j];
                            j++;
                            i = i2;
                        }
                    }
                }
            }
            GraphNode node = buildCustomGraph(tc.getOriginalFormat());
            if (node == null || buildTrackFromGraph(tc, node) != null) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: 0000 */
        public boolean buildGraph(BasicTrackControl tc, int trackID, int numTracks) {
            this.trackID = trackID;
            this.numTracks = numTracks;
            if (!tc.isCustomized()) {
                return super.buildGraph(tc);
            }
            Log.comment("Input: " + tc.getOriginalFormat());
            return buildCustomGraph((ProcTControl) tc);
        }

        /* access modifiers changed from: protected */
        public GraphNode buildTrackFromGraph(BasicTrackControl tc, GraphNode node) {
            return this.engine.buildTrackFromGraph(tc, node);
        }

        /* access modifiers changed from: 0000 */
        public void doGetSupportedOutputFormats(Vector candidates, Vector collected) {
            GraphNode node = (GraphNode) candidates.firstElement();
            candidates.removeElementAt(0);
            if (node.input == null && (node.plugin == null || !(node.plugin instanceof Codec))) {
                Log.error("Internal error: doGetSupportedOutputFormats");
            } else if (node.plugin == null || SimpleGraphBuilder.verifyInput(node.plugin, node.input) != null) {
                Format[] outs;
                int j;
                Format input;
                if (node.plugin != null) {
                    outs = node.getSupportedOutputs(node.input);
                    if (outs != null && outs.length != 0) {
                        j = 0;
                        while (j < outs.length) {
                            int size = collected.size();
                            boolean found = false;
                            for (int k = 0; k < size; k++) {
                                Format other = (Format) collected.elementAt(k);
                                if (other == outs[j] || other.equals(outs[j])) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                collected.addElement(outs[j]);
                            }
                            j++;
                        }
                        input = node.input;
                    } else {
                        return;
                    }
                }
                outs = new Format[]{node.input};
                input = null;
                if (node.level < this.STAGES) {
                    int i = 0;
                    while (i < outs.length) {
                        if ((input == null || !input.equals(outs[i])) && (node.plugin == null || SimpleGraphBuilder.verifyOutput(node.plugin, outs[i]) != null)) {
                            Vector cnames = PlugInManager.getPlugInList(outs[i], null, 2);
                            if (!(cnames == null || cnames.size() == 0)) {
                                for (j = 0; j < cnames.size(); j++) {
                                    GraphNode gn = SimpleGraphBuilder.getPlugInNode((String) cnames.elementAt(j), 2, this.plugIns);
                                    if (!(gn == null || gn.checkAttempted(outs[i]))) {
                                        Format[] ins = gn.getSupportedInputs();
                                        Format fmt = SimpleGraphBuilder.matches(outs[i], ins, null, gn.plugin);
                                        if (fmt != null) {
                                            candidates.addElement(new GraphNode(gn, fmt, node, node.level + 1));
                                            this.nodesVisited++;
                                        }
                                    }
                                }
                            }
                        }
                        i++;
                    }
                }
            }
        }

        /* access modifiers changed from: protected */
        public GraphNode findTarget(GraphNode node) {
            Format[] outs;
            if (node.plugin == null) {
                outs = new Format[]{node.input};
            } else if (node.output != null) {
                outs = new Format[]{node.output};
            } else {
                outs = node.getSupportedOutputs(node.input);
                if (outs == null || outs.length == 0) {
                    return null;
                }
            }
            if (this.targetFormat != null) {
                Format matched = SimpleGraphBuilder.matches(outs, this.targetFormat, node.plugin, null);
                if (matched == null) {
                    return null;
                }
                if (inspector != null && !inspector.verify((Codec) node.plugin, node.input, matched)) {
                    return null;
                }
                if (this.targetPlugins == null && ProcessEngine.this.targetMuxes == null) {
                    node.output = matched;
                    return node;
                }
                outs = new Format[]{matched};
            }
            GraphNode n;
            if (this.targetPlugins != null) {
                n = verifyTargetPlugins(node, outs);
                if (n == null) {
                    return null;
                }
                return n;
            }
            if (ProcessEngine.this.targetMuxes != null) {
                n = verifyTargetMuxes(node, outs);
                if (n != null) {
                    return n;
                }
            }
            return null;
        }

        public Format[] getSupportedOutputFormats(Format input) {
            long formatsTime = System.currentTimeMillis();
            Vector collected = new Vector();
            Vector candidates = new Vector();
            candidates.addElement(new GraphNode(null, (PlugIn) null, input, null, 0));
            collected.addElement(input);
            this.nodesVisited++;
            while (!candidates.isEmpty()) {
                doGetSupportedOutputFormats(candidates, collected);
            }
            Format[] all = new Format[collected.size()];
            int front = 0;
            int back = all.length - 1;
            Format audioFormat = new AudioFormat(AudioFormat.MPEG_RTP);
            boolean mpegInput = new AudioFormat(AudioFormat.MPEG).matches(input) || new AudioFormat(AudioFormat.MPEGLAYER3).matches(input) || new VideoFormat(VideoFormat.MPEG).matches(input);
            for (int i = 0; i < all.length; i++) {
                Object obj = collected.elementAt(i);
                if (!mpegInput) {
                    if (audioFormat.matches((Format) obj)) {
                        int back2 = back - 1;
                        all[back] = (Format) obj;
                        back = back2;
                    }
                }
                int front2 = front + 1;
                all[front] = (Format) obj;
                front = front2;
            }
            Log.comment("Getting the supported output formats for:");
            Log.comment("  " + input);
            Log.comment("  # of nodes visited: " + this.nodesVisited);
            Log.comment("  # of formats supported: " + all.length + "\n");
            PlaybackEngine.profile("getSupportedOutputFormats", formatsTime);
            return all;
        }

        public void reset() {
            super.reset();
            resetTargets();
        }

        /* access modifiers changed from: 0000 */
        public void resetTargets() {
            this.targetFormat = null;
            this.targetPlugins = null;
        }

        /* access modifiers changed from: 0000 */
        public boolean setDefaultTargetMux() {
            if (ProcessEngine.this.targetMuxes != null) {
                return true;
            }
            Log.comment("An output content type is specified: " + ProcessEngine.this.outputContentDes);
            ProcessEngine.this.targetMuxNames = PlugInManager.getPlugInList(null, ProcessEngine.this.outputContentDes, 5);
            if (ProcessEngine.this.targetMuxNames == null || ProcessEngine.this.targetMuxNames.size() == 0) {
                Log.error("No multiplexer is found for that content type: " + ProcessEngine.this.outputContentDes);
                return false;
            }
            ProcessEngine.this.targetMuxes = new GraphNode[ProcessEngine.this.targetMuxNames.size()];
            ProcessEngine.this.targetMux = null;
            ProcessEngine.this.targetMuxFormats = new Format[this.numTracks];
            this.targetPluginNames = null;
            this.targetPlugins = null;
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean setDefaultTargetRenderer(Format in) {
            if (!super.setDefaultTargetRenderer(in)) {
                return false;
            }
            ProcessEngine.this.targetMuxes = null;
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean setDefaultTargets(Format in) {
            if (ProcessEngine.this.outputContentDes != null) {
                return setDefaultTargetMux();
            }
            return setDefaultTargetRenderer(in);
        }

        /* access modifiers changed from: 0000 */
        public void setTargetPlugin(PlugIn p, int type) {
            this.targetPlugins = new GraphNode[1];
            this.targetPlugins[0] = new GraphNode(p, null, null, 0);
            this.targetPlugins[0].custom = true;
            this.targetPlugins[0].type = type;
        }

        /* access modifiers changed from: 0000 */
        public GraphNode verifyTargetMuxes(GraphNode node, Format[] outs) {
            int i = 0;
            while (i < ProcessEngine.this.targetMuxes.length) {
                GraphNode gn = ProcessEngine.this.targetMuxes[i];
                if (gn == null) {
                    String name = (String) ProcessEngine.this.targetMuxNames.elementAt(i);
                    if (name == null) {
                        continue;
                    } else {
                        gn = SimpleGraphBuilder.getPlugInNode(name, 5, this.plugIns);
                        if (gn == null) {
                            ProcessEngine.this.targetMuxNames.setElementAt(null, i);
                        } else {
                            Multiplexer mux = gn.plugin;
                            if (mux.setContentDescriptor(ProcessEngine.this.outputContentDes) == null) {
                                ProcessEngine.this.targetMuxNames.setElementAt(null, i);
                            } else if (mux.setNumTracks(this.numTracks) != this.numTracks) {
                                ProcessEngine.this.targetMuxNames.setElementAt(null, i);
                            } else {
                                ProcessEngine.this.targetMuxes[i] = gn;
                            }
                        }
                    }
                    i++;
                }
                if (ProcessEngine.this.targetMux == null || gn == ProcessEngine.this.targetMux) {
                    for (Format inputFormat : outs) {
                        Format fmt = ((Multiplexer) gn.plugin).setInputFormat(inputFormat, this.trackID);
                        if (fmt != null && (inspector == null || node.plugin == null || inspector.verify((Codec) node.plugin, node.input, fmt))) {
                            ProcessEngine.this.targetMux = gn;
                            ProcessEngine.this.targetMuxFormats[this.trackID] = fmt;
                            node.output = fmt;
                            return node;
                        }
                    }
                    continue;
                    i++;
                } else {
                    i++;
                }
            }
            return null;
        }
    }

    class ProcTControl extends BasicTrackControl implements Owned {
        protected Codec[] codecChainWanted = null;
        protected Format formatWanted = null;
        protected ProcGraphBuilder gb;
        protected Renderer rendererWanted = null;
        protected Format[] supportedFormats = null;

        public ProcTControl(ProcessEngine engine, Track track, OutputConnector oc) {
            super(engine, track, oc);
        }

        public boolean buildTrack(int trackID, int numTracks) {
            if (this.gb == null) {
                this.gb = new ProcGraphBuilder((ProcessEngine) this.engine);
            } else {
                this.gb.reset();
            }
            boolean rtn = this.gb.buildGraph(this, trackID, numTracks);
            this.gb = null;
            return rtn;
        }

        /* JADX WARNING: Missing block: B:8:0x0020, code skipped:
            if (r10 == null) goto L_0x0022;
     */
        private javax.media.Format checkSize(javax.media.Format r14) {
            /*
            r13 = this;
            r1 = 0;
            r0 = r14 instanceof javax.media.format.VideoFormat;
            if (r0 != 0) goto L_0x0008;
        L_0x0005:
            r6 = r14;
            r7 = r14;
        L_0x0007:
            return r7;
        L_0x0008:
            r11 = r14;
            r11 = (javax.media.format.VideoFormat) r11;
            r0 = r14;
            r0 = (javax.media.format.VideoFormat) r0;
            r10 = r0.getSize();
            if (r10 != 0) goto L_0x0025;
        L_0x0014:
            r9 = r13.getOriginalFormat();
            if (r9 == 0) goto L_0x0022;
        L_0x001a:
            r9 = (javax.media.format.VideoFormat) r9;
            r10 = r9.getSize();
            if (r10 != 0) goto L_0x0025;
        L_0x0022:
            r6 = r14;
            r7 = r14;
            goto L_0x0007;
        L_0x0025:
            r12 = r10.width;
            r8 = r10.height;
            r0 = new javax.media.format.VideoFormat;
            r2 = "jpeg/rtp";
            r0.m73init(r2);
            r0 = r14.matches(r0);
            if (r0 != 0) goto L_0x0043;
        L_0x0036:
            r0 = new javax.media.format.VideoFormat;
            r2 = "jpeg";
            r0.m73init(r2);
            r0 = r14.matches(r0);
            if (r0 == 0) goto L_0x00df;
        L_0x0043:
            r0 = r10.width;
            r0 = r0 % 8;
            if (r0 == 0) goto L_0x004f;
        L_0x0049:
            r0 = r10.width;
            r0 = r0 / 8;
            r12 = r0 * 8;
        L_0x004f:
            r0 = r10.height;
            r0 = r0 % 8;
            if (r0 == 0) goto L_0x005b;
        L_0x0055:
            r0 = r10.height;
            r0 = r0 / 8;
            r8 = r0 * 8;
        L_0x005b:
            if (r12 == 0) goto L_0x005f;
        L_0x005d:
            if (r8 != 0) goto L_0x0063;
        L_0x005f:
            r12 = r10.width;
            r8 = r10.height;
        L_0x0063:
            r0 = r10.width;
            if (r12 != r0) goto L_0x006b;
        L_0x0067:
            r0 = r10.height;
            if (r8 == r0) goto L_0x00db;
        L_0x006b:
            r0 = new java.lang.StringBuilder;
            r0.<init>();
            r2 = "setFormat: ";
            r0 = r0.append(r2);
            r2 = r14.getEncoding();
            r0 = r0.append(r2);
            r2 = ": video aspect ratio mismatched.";
            r0 = r0.append(r2);
            r0 = r0.toString();
            net.sf.fmj.media.Log.comment(r0);
            r0 = new java.lang.StringBuilder;
            r0.<init>();
            r2 = "  Scaled from ";
            r0 = r0.append(r2);
            r2 = r10.width;
            r0 = r0.append(r2);
            r2 = "x";
            r0 = r0.append(r2);
            r2 = r10.height;
            r0 = r0.append(r2);
            r2 = " to ";
            r0 = r0.append(r2);
            r0 = r0.append(r12);
            r2 = "x";
            r0 = r0.append(r2);
            r0 = r0.append(r8);
            r2 = ".\n";
            r0 = r0.append(r2);
            r0 = r0.toString();
            net.sf.fmj.media.Log.comment(r0);
            r0 = new javax.media.format.VideoFormat;
            r2 = new org.jitsi.android.util.java.awt.Dimension;
            r2.<init>(r12, r8);
            r3 = -1;
            r5 = -1082130432; // 0xffffffffbf800000 float:-1.0 double:NaN;
            r4 = r1;
            r0.m74init(r1, r2, r3, r4, r5);
            r14 = r0.intersects(r14);
        L_0x00db:
            r6 = r14;
            r7 = r14;
            goto L_0x0007;
        L_0x00df:
            r0 = new javax.media.format.VideoFormat;
            r2 = "h263/rtp";
            r0.m73init(r2);
            r0 = r14.matches(r0);
            if (r0 != 0) goto L_0x0106;
        L_0x00ec:
            r0 = new javax.media.format.VideoFormat;
            r2 = "h263-1998/rtp";
            r0.m73init(r2);
            r0 = r14.matches(r0);
            if (r0 != 0) goto L_0x0106;
        L_0x00f9:
            r0 = new javax.media.format.VideoFormat;
            r2 = "h263";
            r0.m73init(r2);
            r0 = r14.matches(r0);
            if (r0 == 0) goto L_0x0063;
        L_0x0106:
            r0 = r10.width;
            r2 = 352; // 0x160 float:4.93E-43 double:1.74E-321;
            if (r0 < r2) goto L_0x0112;
        L_0x010c:
            r12 = 352; // 0x160 float:4.93E-43 double:1.74E-321;
            r8 = 288; // 0x120 float:4.04E-43 double:1.423E-321;
            goto L_0x0063;
        L_0x0112:
            r0 = r10.width;
            r2 = 160; // 0xa0 float:2.24E-43 double:7.9E-322;
            if (r0 < r2) goto L_0x011e;
        L_0x0118:
            r12 = 176; // 0xb0 float:2.47E-43 double:8.7E-322;
            r8 = 144; // 0x90 float:2.02E-43 double:7.1E-322;
            goto L_0x0063;
        L_0x011e:
            r12 = 128; // 0x80 float:1.794E-43 double:6.32E-322;
            r8 = 96;
            goto L_0x0063;
            */
            throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.media.ProcessEngine$ProcTControl.checkSize(javax.media.Format):javax.media.Format");
        }

        /* access modifiers changed from: protected */
        public FrameRateControl frameRateControl() {
            this.muxModule = ProcessEngine.this.getMuxModule();
            return ProcessEngine.this.frameRateControl;
        }

        public Format getFormat() {
            return this.formatWanted == null ? this.track.getFormat() : this.formatWanted;
        }

        public Object getOwner() {
            return ProcessEngine.this.player;
        }

        public Format[] getSupportedFormats() {
            if (this.supportedFormats == null) {
                Format[] db = Resource.getDB(this.track.getFormat());
                this.supportedFormats = db;
                if (db == null) {
                    if (this.gb == null) {
                        this.gb = new ProcGraphBuilder((ProcessEngine) this.engine);
                    } else {
                        this.gb.reset();
                    }
                    this.supportedFormats = this.gb.getSupportedOutputFormats(this.track.getFormat());
                    this.supportedFormats = Resource.putDB(this.track.getFormat(), this.supportedFormats);
                    PlaybackEngine.needSavingDB = true;
                }
            }
            if (ProcessEngine.this.outputContentDes != null) {
                return verifyMuxInputs(ProcessEngine.this.outputContentDes, this.supportedFormats);
            }
            return this.supportedFormats;
        }

        public boolean isCustomized() {
            return (this.formatWanted == null && this.codecChainWanted == null && this.rendererWanted == null) ? false : true;
        }

        public boolean isTimeBase() {
            for (int j = 0; j < this.modules.size(); j++) {
                if (this.modules.elementAt(j) == ProcessEngine.this.masterSink) {
                    return true;
                }
            }
            return false;
        }

        public void prError() {
            if (isCustomized()) {
                Log.error("  Cannot build a flow graph with the customized options:");
                if (this.formatWanted != null) {
                    Log.error("    Unable to transcode format: " + getOriginalFormat());
                    Log.error("      to: " + getFormat());
                    if (ProcessEngine.this.outputContentDes != null) {
                        Log.error("      outputting to: " + ProcessEngine.this.outputContentDes);
                    }
                }
                if (this.codecChainWanted != null) {
                    Log.error("    Unable to add customed codecs: ");
                    for (Object obj : this.codecChainWanted) {
                        Log.error("      " + obj);
                    }
                }
                if (this.rendererWanted != null) {
                    Log.error("    Unable to add customed renderer: " + this.rendererWanted);
                }
                Log.write("\n");
                return;
            }
            super.prError();
        }

        /* access modifiers changed from: protected */
        public ProgressControl progressControl() {
            return ProcessEngine.this.progressControl;
        }

        public void setCodecChain(Codec[] codec) throws NotConfiguredError, UnsupportedPlugInException {
            if (this.engine.getState() > Processor.Configured) {
                ProcessEngine.this.throwError(new NotConfiguredError("Cannot set a PlugIn before reaching the configured state."));
            }
            if (codec.length < 1) {
                throw new UnsupportedPlugInException("No codec specified in the array.");
            }
            this.codecChainWanted = new Codec[codec.length];
            for (int i = 0; i < codec.length; i++) {
                this.codecChainWanted[i] = codec[i];
            }
        }

        public Format setFormat(Format format) {
            if (this.engine.getState() > Processor.Configured) {
                return getFormat();
            }
            if (format == null || format.matches(this.track.getFormat())) {
                return format;
            }
            this.formatWanted = checkSize(format);
            return this.formatWanted;
        }

        public void setRenderer(Renderer renderer) throws NotConfiguredError {
            if (this.engine.getState() > Processor.Configured) {
                ProcessEngine.this.throwError(new NotConfiguredError("Cannot set a PlugIn before reaching the configured state."));
            }
            this.rendererWanted = renderer;
            if (renderer instanceof SlowPlugIn) {
                ((SlowPlugIn) renderer).forceToUse();
            }
        }

        /* access modifiers changed from: 0000 */
        public Format[] verifyMuxInputs(ContentDescriptor cd, Format[] inputs) {
            if (cd == null || cd.getEncoding() == ContentDescriptor.RAW) {
                return inputs;
            }
            Vector cnames = PlugInManager.getPlugInList(null, cd, 5);
            if (cnames == null || cnames.size() == 0) {
                return new Format[0];
            }
            int i;
            Multiplexer[] mux = new Multiplexer[cnames.size()];
            int total = 0;
            for (i = 0; i < cnames.size(); i++) {
                Multiplexer m = (Multiplexer) SimpleGraphBuilder.createPlugIn((String) cnames.elementAt(i), 5);
                if (m != null) {
                    try {
                        m.setContentDescriptor(ProcessEngine.this.outputContentDes);
                        if (m.setNumTracks(1) >= 1) {
                            int total2 = total + 1;
                            mux[total] = m;
                            total = total2;
                        }
                    } catch (Exception e) {
                    }
                }
            }
            Format[] tmp = new Format[inputs.length];
            int vtotal = 0;
            for (i = 0; i < inputs.length; i++) {
                Format fmt;
                int vtotal2;
                if (total == 1) {
                    fmt = mux[0].setInputFormat(inputs[i], 0);
                    if (fmt != null) {
                        vtotal2 = vtotal + 1;
                        tmp[vtotal] = fmt;
                        vtotal = vtotal2;
                    }
                } else {
                    for (int j = 0; j < total; j++) {
                        fmt = mux[j].setInputFormat(inputs[i], 0);
                        if (fmt != null) {
                            vtotal2 = vtotal + 1;
                            tmp[vtotal] = fmt;
                            vtotal = vtotal2;
                            break;
                        }
                    }
                }
            }
            Format[] verified = new Format[vtotal];
            System.arraycopy(tmp, 0, verified, 0, vtotal);
            return verified;
        }
    }

    public ProcessEngine(BasicProcessor p) {
        super(p);
    }

    /* access modifiers changed from: 0000 */
    public boolean connectMux() {
        BasicTrackControl[] tcs = new BasicTrackControl[this.trackControls.length];
        int total = 0;
        Multiplexer mux = this.targetMux.plugin;
        for (int i = 0; i < this.trackControls.length; i++) {
            if (this.trackControls[i].isEnabled()) {
                int total2 = total + 1;
                tcs[total] = this.trackControls[i];
                total = total2;
            }
        }
        try {
            mux.setContentDescriptor(this.outputContentDes);
            boolean failed = false;
            if (mux.setNumTracks(this.targetMuxFormats.length) != this.targetMuxFormats.length) {
                Log.comment("Failed  to set number of tracks on the multiplexer.");
                return false;
            }
            int mf = 0;
            while (mf < this.targetMuxFormats.length) {
                if (this.targetMuxFormats[mf] == null || mux.setInputFormat(this.targetMuxFormats[mf], mf) == null) {
                    Log.comment("Failed to set input format on the multiplexer.");
                    failed = true;
                    break;
                }
                mf++;
            }
            if (failed) {
                return false;
            }
            if (SimpleGraphBuilder.inspector != null && !SimpleGraphBuilder.inspector.verify(mux, this.targetMuxFormats)) {
                return false;
            }
            BasicMuxModule bmm = new BasicMuxModule(mux, this.targetMuxFormats);
            for (int j = 0; j < this.targetMuxFormats.length; j++) {
                InputConnector ic = bmm.getInputConnector(BasicMuxModule.ConnectorNamePrefix + j);
                if (ic == null) {
                    Log.comment("BasicMuxModule: connector mismatched.");
                    return false;
                }
                ic.setFormat(this.targetMuxFormats[j]);
                tcs[j].lastOC.setProtocol(ic.getProtocol());
                tcs[j].lastOC.connectTo(ic, this.targetMuxFormats[j]);
            }
            if (!bmm.doRealize()) {
                return false;
            }
            bmm.setModuleListener(this);
            bmm.setController(this);
            this.modules.addElement(bmm);
            this.sinks.addElement(bmm);
            this.muxModule = bmm;
            return true;
        } catch (Exception e) {
            Log.comment("Failed to set the output content descriptor on the multiplexer.");
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean doConfigure() {
        if (!doConfigure1()) {
            return false;
        }
        String[] names = this.source.getOutputConnectorNames();
        this.trackControls = new BasicTrackControl[this.tracks.length];
        for (int i = 0; i < this.tracks.length; i++) {
            this.trackControls[i] = new ProcTControl(this, this.tracks[i], this.source.getOutputConnector(names[i]));
        }
        if (!doConfigure2()) {
            return false;
        }
        this.outputContentDes = new ContentDescriptor(ContentDescriptor.RAW);
        reenableHintTracks();
        return true;
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized boolean doPrefetch() {
        boolean z = false;
        synchronized (this) {
            if (this.prefetched) {
                z = true;
            } else if (doPrefetch1()) {
                if (this.muxModule == null || this.muxModule.doPrefetch()) {
                    z = doPrefetch2();
                } else {
                    Log.error(this.prefetchError);
                    Log.error("  Cannot prefetch the multiplexer: " + this.muxModule.getMultiplexer() + "\n");
                }
            }
        }
        return z;
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized boolean doRealize() {
        boolean z = false;
        synchronized (this) {
            this.targetMuxes = null;
            if (super.doRealize1()) {
                if (this.targetMux != null && !connectMux()) {
                    Log.error(this.realizeError);
                    Log.error("  Cannot connect the multiplexer\n");
                    this.player.processError = this.genericProcessorError;
                } else if (super.doRealize2()) {
                    z = true;
                }
            }
        }
        return z;
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void doStart() {
        if (!this.started) {
            doStart1();
            if (this.muxModule != null) {
                this.muxModule.doStart();
            }
            doStart2();
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void doStop() {
        if (this.started) {
            doStop1();
            if (this.muxModule != null) {
                this.muxModule.doStop();
            }
            doStop2();
        }
    }

    /* access modifiers changed from: protected */
    public BasicSinkModule findMasterSink() {
        if (this.muxModule == null || this.muxModule.getClock() == null) {
            return super.findMasterSink();
        }
        return this.muxModule;
    }

    /* access modifiers changed from: protected */
    public long getBitRate() {
        if (this.muxModule != null) {
            return this.muxModule.getBitsWritten();
        }
        return this.source.getBitsRead();
    }

    public ContentDescriptor getContentDescriptor() throws NotConfiguredError {
        if (getState() < Processor.Configured) {
            throwError(new NotConfiguredError("getContentDescriptor " + NOT_CONFIGURED_ERROR));
        }
        return this.outputContentDes;
    }

    public DataSource getDataOutput() throws NotRealizedError {
        if (getState() < Controller.Realized) {
            throwError(new NotRealizedError("getDataOutput " + NOT_REALIZED_ERROR));
        }
        if (this.muxModule != null) {
            return this.muxModule.getDataOutput();
        }
        return null;
    }

    /* access modifiers changed from: 0000 */
    public BasicMuxModule getMuxModule() {
        return this.muxModule;
    }

    /* access modifiers changed from: protected */
    public PlugIn getPlugIn(BasicModule m) {
        if (m instanceof BasicMuxModule) {
            return ((BasicMuxModule) m).getMultiplexer();
        }
        return super.getPlugIn(m);
    }

    public ContentDescriptor[] getSupportedContentDescriptors() throws NotConfiguredError {
        int i;
        if (getState() < Processor.Configured) {
            throwError(new NotConfiguredError("getSupportedContentDescriptors " + NOT_CONFIGURED_ERROR));
        }
        Vector names = PlugInManager.getPlugInList(null, null, 5);
        Vector fmts = new Vector();
        for (i = 0; i < names.size(); i++) {
            Format[] fs = PlugInManager.getSupportedOutputFormats((String) names.elementAt(i), 5);
            if (fs != null) {
                for (int j = 0; j < fs.length; j++) {
                    if (fs[j] instanceof ContentDescriptor) {
                        boolean duplicate = false;
                        for (int k = 0; k < fmts.size(); k++) {
                            if (fmts.elementAt(k).equals(fs[j])) {
                                duplicate = true;
                                break;
                            }
                        }
                        if (!duplicate) {
                            fmts.addElement(fs[j]);
                        }
                    }
                }
            }
        }
        ContentDescriptor[] cds = new ContentDescriptor[fmts.size()];
        for (i = 0; i < fmts.size(); i++) {
            cds[i] = (ContentDescriptor) fmts.elementAt(i);
        }
        return cds;
    }

    public TrackControl[] getTrackControls() throws NotConfiguredError {
        if (getState() < Processor.Configured) {
            throwError(new NotConfiguredError("getTrackControls " + NOT_CONFIGURED_ERROR));
        }
        return this.trackControls;
    }

    /* access modifiers changed from: 0000 */
    public boolean isRTPFormat(Format fmt) {
        return !(fmt == null || fmt.getEncoding() == null || !fmt.getEncoding().endsWith("rtp")) || fmt.getEncoding().endsWith("RTP");
    }

    /* access modifiers changed from: 0000 */
    public void reenableHintTracks() {
        for (int i = 0; i < this.trackControls.length; i++) {
            if (isRTPFormat(this.trackControls[i].getOriginalFormat())) {
                this.trackControls[i].setEnabled(true);
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void resetBitRate() {
        if (this.muxModule != null) {
            this.muxModule.resetBitsWritten();
        } else {
            this.source.resetBitsRead();
        }
    }

    public ContentDescriptor setContentDescriptor(ContentDescriptor ocd) throws NotConfiguredError {
        if (getState() < Processor.Configured) {
            throwError(new NotConfiguredError("setContentDescriptor " + NOT_CONFIGURED_ERROR));
        }
        if (getState() > Processor.Configured) {
            return null;
        }
        if (ocd != null) {
            Vector cnames = PlugInManager.getPlugInList(null, ocd, 5);
            if (cnames == null || cnames.size() == 0) {
                return null;
            }
        }
        this.outputContentDes = ocd;
        return this.outputContentDes;
    }
}
