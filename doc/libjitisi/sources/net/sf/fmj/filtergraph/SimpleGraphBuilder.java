package net.sf.fmj.filtergraph;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.media.Codec;
import javax.media.Format;
import javax.media.Multiplexer;
import javax.media.PlugIn;
import javax.media.PlugInManager;
import javax.media.Renderer;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import net.sf.fmj.media.BasicPlugIn;
import net.sf.fmj.media.BasicTrackControl;
import net.sf.fmj.media.Log;

public class SimpleGraphBuilder {
    public static GraphInspector inspector;
    protected int STAGES = 5;
    protected int indent = 0;
    protected Hashtable plugIns = new Hashtable(40);
    protected Vector targetPluginNames = null;
    protected GraphNode[] targetPlugins = null;
    protected int targetType = -1;

    public static PlugIn createPlugIn(String name, int type) {
        try {
            Object obj = BasicPlugIn.getClassForName(name).newInstance();
            if (verifyClass(obj, type)) {
                return (PlugIn) obj;
            }
            return null;
        } catch (Exception e) {
            return null;
        } catch (Error e2) {
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:51:0x0055 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0060 A:{SYNTHETIC} */
    public static javax.media.Codec findCodec(javax.media.Format r11, javax.media.Format r12, javax.media.Format[] r13, javax.media.Format[] r14) {
        /*
        r10 = 2;
        r9 = 0;
        r8 = 0;
        r1 = javax.media.PlugInManager.getPlugInList(r11, r12, r10);
        if (r1 != 0) goto L_0x000b;
    L_0x0009:
        r0 = r8;
    L_0x000a:
        return r0;
    L_0x000b:
        r0 = 0;
        r3 = 0;
    L_0x000d:
        r7 = r1.size();
        if (r3 >= r7) goto L_0x006e;
    L_0x0013:
        r7 = r1.elementAt(r3);
        r7 = (java.lang.String) r7;
        r0 = createPlugIn(r7, r10);
        r0 = (javax.media.Codec) r0;
        if (r0 != 0) goto L_0x0024;
    L_0x0021:
        r3 = r3 + 1;
        goto L_0x000d;
    L_0x0024:
        r2 = r0.getSupportedInputFormats();
        r5 = matches(r11, r2, r8, r0);
        if (r5 == 0) goto L_0x0021;
    L_0x002e:
        if (r13 == 0) goto L_0x0035;
    L_0x0030:
        r7 = r13.length;
        if (r7 <= 0) goto L_0x0035;
    L_0x0033:
        r13[r9] = r5;
    L_0x0035:
        r2 = r0.getSupportedOutputFormats(r5);
        if (r2 == 0) goto L_0x0021;
    L_0x003b:
        r7 = r2.length;
        if (r7 == 0) goto L_0x0021;
    L_0x003e:
        r6 = 0;
        r4 = 0;
    L_0x0040:
        r7 = r2.length;
        if (r4 >= r7) goto L_0x0061;
    L_0x0043:
        if (r12 == 0) goto L_0x0058;
    L_0x0045:
        r7 = r2[r4];
        r7 = r12.matches(r7);
        if (r7 == 0) goto L_0x0055;
    L_0x004d:
        r7 = r2[r4];
        r5 = r12.intersects(r7);
        if (r5 != 0) goto L_0x005a;
    L_0x0055:
        r4 = r4 + 1;
        goto L_0x0040;
    L_0x0058:
        r5 = r2[r4];
    L_0x005a:
        r7 = r0.setOutputFormat(r5);
        if (r7 == 0) goto L_0x0055;
    L_0x0060:
        r6 = 1;
    L_0x0061:
        if (r6 == 0) goto L_0x0021;
    L_0x0063:
        r0.open();	 Catch:{ ResourceUnavailableException -> 0x0070 }
    L_0x0066:
        if (r14 == 0) goto L_0x000a;
    L_0x0068:
        r7 = r14.length;
        if (r7 <= 0) goto L_0x000a;
    L_0x006b:
        r14[r9] = r5;
        goto L_0x000a;
    L_0x006e:
        r0 = r8;
        goto L_0x000a;
    L_0x0070:
        r7 = move-exception;
        goto L_0x0066;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.sf.fmj.filtergraph.SimpleGraphBuilder.findCodec(javax.media.Format, javax.media.Format, javax.media.Format[], javax.media.Format[]):javax.media.Codec");
    }

    public static Renderer findRenderer(Format in) {
        Vector names = PlugInManager.getPlugInList(in, null, 4);
        if (names == null) {
            return null;
        }
        int i = 0;
        while (i < names.size()) {
            PlugIn r = (Renderer) createPlugIn((String) names.elementAt(i), 4);
            if (r == null || matches(in, r.getSupportedInputFormats(), null, r) == null) {
                i++;
            } else {
                try {
                    r.open();
                    return r;
                } catch (ResourceUnavailableException e) {
                    return r;
                }
            }
        }
        return null;
    }

    public static Vector findRenderingChain(Format in, Vector formats) {
        GraphNode n = new SimpleGraphBuilder().buildGraph(in);
        if (n == null) {
            return null;
        }
        Vector list = new Vector(10);
        while (n != null && n.plugin != null) {
            list.addElement(n.plugin);
            if (formats != null) {
                formats.addElement(n.input);
            }
            n = n.prev;
        }
        return list;
    }

    public static GraphNode getPlugInNode(String name, int type, Hashtable plugIns) {
        GraphNode gn;
        if (plugIns != null) {
            gn = (GraphNode) plugIns.get(name);
            if (gn != null) {
                GraphNode graphNode;
                if (gn.failed) {
                    graphNode = gn;
                    return null;
                } else if (verifyClass(gn.plugin, type)) {
                    graphNode = gn;
                    return gn;
                } else {
                    return null;
                }
            }
        }
        PlugIn p = createPlugIn(name, type);
        gn = new GraphNode(name, p, null, null, 0);
        if (plugIns != null) {
            plugIns.put(name, gn);
        }
        if (p == null) {
            gn.failed = true;
            return null;
        }
        return gn;
    }

    public static Format matches(Format[] outs, Format[] ins, PlugIn up, PlugIn down) {
        if (outs == null) {
            return null;
        }
        for (Format matches : outs) {
            Format fmt = matches(matches, ins, up, down);
            if (fmt != null) {
                return fmt;
            }
        }
        return null;
    }

    public static Format matches(Format out, Format[] ins, PlugIn up, PlugIn down) {
        if (out == null || ins == null) {
            return null;
        }
        int i = 0;
        while (i < ins.length) {
            if (ins[i] != null && ins[i].getClass().isAssignableFrom(out.getClass()) && out.matches(ins[i])) {
                Format fmt = out.intersects(ins[i]);
                if (fmt != null) {
                    if (down != null) {
                        fmt = verifyInput(down, fmt);
                        if (fmt == null) {
                            continue;
                        }
                    }
                    Format refined = fmt;
                    if (up != null) {
                        refined = verifyOutput(up, fmt);
                        if (refined == null) {
                            continue;
                        }
                    }
                    if (down == null || refined == fmt || verifyInput(down, refined) != null) {
                        return refined;
                    }
                } else {
                    continue;
                }
            }
            i++;
        }
        return null;
    }

    public static Format matches(Format[] outs, Format in, PlugIn up, PlugIn down) {
        return matches(outs, new Format[]{in}, up, down);
    }

    public static boolean verifyClass(Object obj, int type) {
        Class<?> cls;
        switch (type) {
            case 2:
                cls = Codec.class;
                break;
            case 4:
                cls = Renderer.class;
                break;
            case 5:
                cls = Multiplexer.class;
                break;
            default:
                cls = PlugIn.class;
                break;
        }
        if (cls.isInstance(obj)) {
            return true;
        }
        return false;
    }

    public static Format verifyInput(PlugIn p, Format in) {
        if (p instanceof Codec) {
            return ((Codec) p).setInputFormat(in);
        }
        if (p instanceof Renderer) {
            return ((Renderer) p).setInputFormat(in);
        }
        return null;
    }

    public static Format verifyOutput(PlugIn p, Format out) {
        if (p instanceof Codec) {
            return ((Codec) p).setOutputFormat(out);
        }
        return null;
    }

    public static void setGraphInspector(GraphInspector insp) {
        inspector = insp;
    }

    public boolean buildGraph(BasicTrackControl tc) {
        Log.comment("Input: " + tc.getOriginalFormat());
        Vector candidates = new Vector();
        GraphNode node = new GraphNode(null, (PlugIn) null, tc.getOriginalFormat(), null, 0);
        this.indent = 1;
        Log.setIndent(this.indent);
        if (!setDefaultTargets(tc.getOriginalFormat())) {
            return false;
        }
        candidates.addElement(node);
        while (true) {
            node = buildGraph(candidates);
            if (node != null) {
                GraphNode failed = buildTrackFromGraph(tc, node);
                if (failed == null) {
                    this.indent = 0;
                    Log.setIndent(this.indent);
                    return true;
                }
                removeFailure(candidates, failed, tc.getOriginalFormat());
            } else {
                this.indent = 0;
                Log.setIndent(this.indent);
                return false;
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public GraphNode buildGraph(Format input) {
        Log.comment("Input: " + input);
        Vector candidates = new Vector();
        GraphNode node = new GraphNode(null, (PlugIn) null, input, null, 0);
        this.indent = 1;
        Log.setIndent(this.indent);
        if (setDefaultTargets(input)) {
            candidates.addElement(node);
            while (true) {
                node = buildGraph(candidates);
                GraphNode graphNode;
                if (node != null) {
                    GraphNode failed = verifyGraph(node);
                    if (failed == null) {
                        this.indent = 0;
                        Log.setIndent(this.indent);
                        graphNode = node;
                        return node;
                    }
                    removeFailure(candidates, failed, input);
                } else {
                    this.indent = 0;
                    Log.setIndent(this.indent);
                    graphNode = node;
                    return node;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public GraphNode buildGraph(Vector candidates) {
        GraphNode node;
        do {
            node = doBuildGraph(candidates);
            if (node != null) {
                break;
            }
        } while (!candidates.isEmpty());
        return node;
    }

    /* access modifiers changed from: protected */
    public GraphNode buildTrackFromGraph(BasicTrackControl tc, GraphNode node) {
        return null;
    }

    /* access modifiers changed from: 0000 */
    public GraphNode doBuildGraph(Vector candidates) {
        if (candidates.isEmpty()) {
            return null;
        }
        GraphNode node = (GraphNode) candidates.firstElement();
        candidates.removeElementAt(0);
        if (node.input != null || (node.plugin != null && (node.plugin instanceof Codec))) {
            int oldIndent = this.indent;
            Log.setIndent(node.level + 1);
            if (node.plugin != null && verifyInput(node.plugin, node.input) == null) {
                return null;
            }
            GraphNode n = findTarget(node);
            if (n != null) {
                this.indent = oldIndent;
                Log.setIndent(this.indent);
                return n;
            }
            if (node.level >= this.STAGES) {
                this.indent = oldIndent;
                Log.setIndent(this.indent);
                return null;
            }
            Format[] outs;
            Format input;
            if (node.plugin != null) {
                if (node.output != null) {
                    outs = new Format[]{node.output};
                } else {
                    outs = node.getSupportedOutputs(node.input);
                    if (outs == null || outs.length == 0) {
                        this.indent = oldIndent;
                        Log.setIndent(this.indent);
                        return null;
                    }
                }
                input = node.input;
            } else {
                outs = new Format[]{node.input};
                input = null;
            }
            int i = 0;
            while (i < outs.length) {
                if (node.custom || input == null || !input.equals(outs[i])) {
                    if (node.plugin != null) {
                        if (verifyOutput(node.plugin, outs[i]) == null) {
                            if (inspector != null && inspector.detailMode()) {
                                inspector.verifyOutputFailed(node.plugin, outs[i]);
                            }
                        } else if (inspector != null) {
                            if (!inspector.verify((Codec) node.plugin, node.input, outs[i])) {
                            }
                        }
                    }
                    Vector cnames = PlugInManager.getPlugInList(outs[i], null, 2);
                    if (!(cnames == null || cnames.size() == 0)) {
                        for (int j = 0; j < cnames.size(); j++) {
                            GraphNode gn = getPlugInNode((String) cnames.elementAt(j), 2, this.plugIns);
                            if (!(gn == null || gn.checkAttempted(outs[i]))) {
                                Format[] ins = gn.getSupportedInputs();
                                Format fmt = matches(outs[i], ins, null, gn.plugin);
                                if (fmt != null) {
                                    if (inspector != null && inspector.detailMode()) {
                                        if (!inspector.verify((Codec) gn.plugin, fmt, null)) {
                                        }
                                    }
                                    candidates.addElement(new GraphNode(gn, fmt, node, node.level + 1));
                                } else if (inspector != null && inspector.detailMode()) {
                                    inspector.verifyInputFailed(gn.plugin, outs[i]);
                                }
                            }
                        }
                    }
                }
                i++;
            }
            this.indent = oldIndent;
            Log.setIndent(this.indent);
            return null;
        }
        Log.error("Internal error: doBuildGraph");
        return null;
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
        if (this.targetPlugins != null) {
            GraphNode n = verifyTargetPlugins(node, outs);
            if (n != null) {
                return n;
            }
        }
        return null;
    }

    /* access modifiers changed from: 0000 */
    public void removeFailure(Vector candidates, GraphNode failed, Format input) {
        if (failed.plugin != null) {
            Log.comment("Failed to open plugin " + failed.plugin + ". Will re-build the graph allover again");
            candidates.removeAllElements();
            GraphNode hsyn = new GraphNode(null, (PlugIn) null, input, null, 0);
            this.indent = 1;
            Log.setIndent(this.indent);
            candidates.addElement(hsyn);
            failed.failed = true;
            this.plugIns.put(failed.plugin.getClass().getName(), failed);
            Enumeration e = this.plugIns.keys();
            while (e.hasMoreElements()) {
                String ss = (String) e.nextElement();
                if (!((GraphNode) this.plugIns.get(ss)).failed) {
                    this.plugIns.remove(ss);
                }
            }
        }
    }

    public void reset() {
        Enumeration enum1 = this.plugIns.elements();
        while (enum1.hasMoreElements()) {
            ((GraphNode) enum1.nextElement()).resetAttempted();
        }
    }

    /* access modifiers changed from: protected */
    public boolean setDefaultTargetRenderer(Format in) {
        if (in instanceof AudioFormat) {
            this.targetPluginNames = PlugInManager.getPlugInList(new AudioFormat(null, -1.0d, -1, -1, -1, -1, -1, -1.0d, null), null, 4);
        } else if (in instanceof VideoFormat) {
            this.targetPluginNames = PlugInManager.getPlugInList(new VideoFormat(null, null, -1, null, -1.0f), null, 4);
        } else {
            this.targetPluginNames = PlugInManager.getPlugInList(null, null, 4);
        }
        if (this.targetPluginNames == null || this.targetPluginNames.size() == 0) {
            return false;
        }
        this.targetPlugins = new GraphNode[this.targetPluginNames.size()];
        this.targetType = 4;
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean setDefaultTargets(Format in) {
        return setDefaultTargetRenderer(in);
    }

    /* access modifiers changed from: protected */
    public GraphNode verifyGraph(GraphNode node) {
        Format prevFormat = null;
        Vector used = new Vector(5);
        if (node.plugin == null) {
            return null;
        }
        int i = this.indent;
        this.indent = i + 1;
        Log.setIndent(i);
        GraphNode node2 = node;
        while (node2 != null && node2.plugin != null) {
            if (used.contains(node2.plugin)) {
                if (node2.cname != null) {
                    PlugIn p = createPlugIn(node2.cname, -1);
                    if (p != null) {
                        node2.plugin = p;
                    }
                }
                Log.write("Failed to instantiate " + node2.cname);
                node = node2;
                return node2;
            }
            used.addElement(node2.plugin);
            if ((node2.type == -1 || node2.type == 4) && (node2.plugin instanceof Renderer)) {
                ((Renderer) node2.plugin).setInputFormat(node2.input);
            } else if ((node2.type == -1 || node2.type == 2) && (node2.plugin instanceof Codec)) {
                ((Codec) node2.plugin).setInputFormat(node2.input);
                if (prevFormat != null) {
                    ((Codec) node2.plugin).setOutputFormat(prevFormat);
                } else if (node2.output != null) {
                    ((Codec) node2.plugin).setOutputFormat(node2.output);
                }
            }
            if ((node2.type != -1 && node2.type != 4) || !(node2.plugin instanceof Renderer)) {
                try {
                    node2.plugin.open();
                } catch (Exception e) {
                    Log.warning("Failed to open: " + node2.plugin);
                    node2.failed = true;
                    node = node2;
                    return node2;
                }
            }
            prevFormat = node2.input;
            node2 = node2.prev;
        }
        i = this.indent;
        this.indent = i - 1;
        Log.setIndent(i);
        node = node2;
        return null;
    }

    /* access modifiers changed from: protected */
    public GraphNode verifyTargetPlugins(GraphNode node, Format[] outs) {
        for (int i = 0; i < this.targetPlugins.length; i++) {
            GraphNode gn = this.targetPlugins[i];
            if (gn == null) {
                String name = (String) this.targetPluginNames.elementAt(i);
                if (!(name == null || matches(outs, PlugInManager.getSupportedInputFormats(name, this.targetType), null, null) == null)) {
                    gn = getPlugInNode(name, this.targetType, this.plugIns);
                    if (gn == null) {
                        this.targetPluginNames.setElementAt(null, i);
                    } else {
                        this.targetPlugins[i] = gn;
                    }
                }
            }
            Format fmt = matches(outs, gn.getSupportedInputs(), node.plugin, gn.plugin);
            if (fmt == null) {
                continue;
            } else {
                if (inspector != null) {
                    if (node.plugin == null || inspector.verify((Codec) node.plugin, node.input, fmt)) {
                        if ((gn.type == -1 || gn.type == 2) && (gn.plugin instanceof Codec)) {
                            if (inspector.verify((Codec) gn.plugin, fmt, null)) {
                            }
                        } else if ((gn.type == -1 || gn.type == 4) && (gn.plugin instanceof Renderer) && !inspector.verify((Renderer) gn.plugin, fmt)) {
                        }
                    }
                }
                return new GraphNode(gn, fmt, node, node.level + 1);
            }
        }
        return null;
    }
}
