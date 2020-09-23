package net.sf.fmj.utility;

import javax.media.Format;

public class PlugInInfo {
    public final String className;
    public final Format[] in;
    public final Format[] out;
    public final int type;

    public PlugInInfo(String className, Format[] in, Format[] out, int type) {
        this.className = className;
        this.type = type;
        this.in = in;
        this.out = out;
    }
}
