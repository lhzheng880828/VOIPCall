package net.sf.fmj.filtergraph;

import javax.media.Codec;
import javax.media.Format;
import javax.media.Multiplexer;
import javax.media.PlugIn;
import javax.media.Renderer;

public class GraphNode {
    static int ARRAY_INC = 30;
    Format[] attempted;
    int attemptedIdx;
    Class<?> clz;
    public String cname;
    public boolean custom;
    public boolean failed;
    public Format input;
    public int level;
    public Format output;
    public PlugIn plugin;
    public GraphNode prev;
    Format[] supportedIns;
    Format[] supportedOuts;
    public int type;

    public GraphNode(GraphNode gn, Format input, GraphNode prev, int level) {
        this.type = -1;
        this.output = null;
        this.failed = false;
        this.custom = false;
        this.attemptedIdx = 0;
        this.attempted = null;
        this.cname = gn.cname;
        this.plugin = gn.plugin;
        this.type = gn.type;
        this.custom = gn.custom;
        this.input = input;
        this.prev = prev;
        this.level = level;
        this.supportedIns = gn.supportedIns;
        if (gn.input == input) {
            this.supportedOuts = gn.supportedOuts;
        }
    }

    public GraphNode(PlugIn plugin, Format input, GraphNode prev, int level) {
        this(plugin == null ? null : plugin.getClass().getName(), plugin, input, prev, level);
    }

    public GraphNode(String cname, PlugIn plugin, Format input, GraphNode prev, int level) {
        this.type = -1;
        this.output = null;
        this.failed = false;
        this.custom = false;
        this.attemptedIdx = 0;
        this.attempted = null;
        this.cname = cname;
        this.plugin = plugin;
        this.input = input;
        this.prev = prev;
        this.level = level;
    }

    public boolean checkAttempted(Format input) {
        Format[] formatArr;
        int i;
        if (this.attempted == null) {
            this.attempted = new Format[ARRAY_INC];
            formatArr = this.attempted;
            i = this.attemptedIdx;
            this.attemptedIdx = i + 1;
            formatArr[i] = input;
            return false;
        }
        for (int j = 0; j < this.attemptedIdx; j++) {
            if (input.equals(this.attempted[j])) {
                return true;
            }
        }
        if (this.attemptedIdx >= this.attempted.length) {
            Format[] newarray = new Format[(this.attempted.length + ARRAY_INC)];
            System.arraycopy(this.attempted, 0, newarray, 0, this.attempted.length);
            this.attempted = newarray;
        }
        formatArr = this.attempted;
        i = this.attemptedIdx;
        this.attemptedIdx = i + 1;
        formatArr[i] = input;
        return false;
    }

    public Format[] getSupportedInputs() {
        if (this.supportedIns != null) {
            return this.supportedIns;
        }
        if (this.plugin == null) {
            return null;
        }
        if ((this.type == -1 || this.type == 2) && (this.plugin instanceof Codec)) {
            this.supportedIns = ((Codec) this.plugin).getSupportedInputFormats();
        } else if ((this.type == -1 || this.type == 4) && (this.plugin instanceof Renderer)) {
            this.supportedIns = ((Renderer) this.plugin).getSupportedInputFormats();
        } else if (this.plugin instanceof Multiplexer) {
            this.supportedIns = ((Multiplexer) this.plugin).getSupportedInputFormats();
        }
        return this.supportedIns;
    }

    public Format[] getSupportedOutputs(Format in) {
        if (in == this.input && this.supportedOuts != null) {
            return this.supportedOuts;
        }
        if (this.plugin == null) {
            return null;
        }
        if ((this.type == -1 || this.type == 4) && (this.plugin instanceof Renderer)) {
            return null;
        }
        if ((this.type != -1 && this.type != 2) || !(this.plugin instanceof Codec)) {
            return null;
        }
        Format[] outs = ((Codec) this.plugin).getSupportedOutputFormats(in);
        if (this.input != in) {
            return outs;
        }
        this.supportedOuts = outs;
        return outs;
    }

    public void resetAttempted() {
        this.attemptedIdx = 0;
        this.attempted = null;
    }
}
