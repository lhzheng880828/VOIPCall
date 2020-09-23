package net.sf.fmj.ejmf.toolkit.util;

import org.jitsi.android.util.javax.swing.border.CompoundBorder;
import org.jitsi.android.util.javax.swing.border.EmptyBorder;
import org.jitsi.android.util.javax.swing.border.EtchedBorder;

public class BorderConstants {
    public static final int GAP = 10;
    public static final EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
    public static final CompoundBorder etchedBorder = new CompoundBorder(new EtchedBorder(), emptyBorder);
}
