package org.jitsi.util.swing;

import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.Container;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.LayoutManager;
import org.jitsi.android.util.java.awt.Rectangle;

public class FitLayout implements LayoutManager {
    protected static final int DEFAULT_HEIGHT_OR_WIDTH = 16;

    public void addLayoutComponent(String name, Component comp) {
    }

    /* access modifiers changed from: protected */
    public Component getComponent(Container parent) {
        Component[] components = parent.getComponents();
        return components.length > 0 ? components[0] : null;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x002d  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0052  */
    public void layoutComponent(org.jitsi.android.util.java.awt.Component r17, org.jitsi.android.util.java.awt.Rectangle r18, float r19, float r20) {
        /*
        r16 = this;
        r0 = r17;
        r9 = r0 instanceof org.jitsi.android.util.javax.swing.JPanel;
        if (r9 == 0) goto L_0x0017;
    L_0x0006:
        r9 = r17.isOpaque();
        if (r9 != 0) goto L_0x0017;
    L_0x000c:
        r9 = r17;
        r9 = (org.jitsi.android.util.java.awt.Container) r9;
        r9 = r9.getComponentCount();
        r12 = 1;
        if (r9 > r12) goto L_0x0023;
    L_0x0017:
        r0 = r17;
        r9 = r0 instanceof org.jitsi.util.swing.VideoContainer;
        if (r9 != 0) goto L_0x0023;
    L_0x001d:
        r8 = r17.getPreferredSize();
        if (r8 != 0) goto L_0x0085;
    L_0x0023:
        r8 = r18.getSize();
    L_0x0027:
        r9 = r17.isMaximumSizeSet();
        if (r9 == 0) goto L_0x0045;
    L_0x002d:
        r4 = r17.getMaximumSize();
        r9 = r8.width;
        r12 = r4.width;
        if (r9 <= r12) goto L_0x003b;
    L_0x0037:
        r9 = r4.width;
        r8.width = r9;
    L_0x003b:
        r9 = r8.height;
        r12 = r4.height;
        if (r9 <= r12) goto L_0x0045;
    L_0x0041:
        r9 = r4.height;
        r8.height = r9;
    L_0x0045:
        r9 = r8.height;
        r12 = 1;
        if (r9 >= r12) goto L_0x004d;
    L_0x004a:
        r9 = 1;
        r8.height = r9;
    L_0x004d:
        r9 = r8.width;
        r12 = 1;
        if (r9 >= r12) goto L_0x0055;
    L_0x0052:
        r9 = 1;
        r8.width = r9;
    L_0x0055:
        r0 = r18;
        r9 = r0.x;
        r0 = r18;
        r12 = r0.width;
        r13 = r8.width;
        r12 = r12 - r13;
        r12 = (float) r12;
        r12 = r12 * r19;
        r12 = java.lang.Math.round(r12);
        r9 = r9 + r12;
        r0 = r18;
        r12 = r0.y;
        r0 = r18;
        r13 = r0.height;
        r14 = r8.height;
        r13 = r13 - r14;
        r13 = (float) r13;
        r13 = r13 * r20;
        r13 = java.lang.Math.round(r13);
        r12 = r12 + r13;
        r13 = r8.width;
        r14 = r8.height;
        r0 = r17;
        r0.setBounds(r9, r12, r13, r14);
        return;
    L_0x0085:
        r5 = 0;
        r9 = r8.width;
        r0 = r18;
        r12 = r0.width;
        if (r9 == r12) goto L_0x00ca;
    L_0x008e:
        r9 = r8.width;
        if (r9 <= 0) goto L_0x00ca;
    L_0x0092:
        r5 = 1;
        r0 = r18;
        r9 = r0.width;
        r12 = (double) r9;
        r9 = r8.width;
        r14 = (double) r9;
        r10 = r12 / r14;
    L_0x009d:
        r9 = r8.height;
        r0 = r18;
        r12 = r0.height;
        if (r9 == r12) goto L_0x00cd;
    L_0x00a5:
        r9 = r8.height;
        if (r9 <= 0) goto L_0x00cd;
    L_0x00a9:
        r5 = 1;
        r0 = r18;
        r9 = r0.height;
        r12 = (double) r9;
        r9 = r8.height;
        r14 = (double) r9;
        r2 = r12 / r14;
    L_0x00b4:
        if (r5 == 0) goto L_0x0027;
    L_0x00b6:
        r6 = java.lang.Math.min(r10, r2);
        r9 = r8.width;
        r12 = (double) r9;
        r12 = r12 * r6;
        r9 = (int) r12;
        r8.width = r9;
        r9 = r8.height;
        r12 = (double) r9;
        r12 = r12 * r6;
        r9 = (int) r12;
        r8.height = r9;
        goto L_0x0027;
    L_0x00ca:
        r10 = 4607182418800017408; // 0x3ff0000000000000 float:0.0 double:1.0;
        goto L_0x009d;
    L_0x00cd:
        r2 = 4607182418800017408; // 0x3ff0000000000000 float:0.0 double:1.0;
        goto L_0x00b4;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.util.swing.FitLayout.layoutComponent(org.jitsi.android.util.java.awt.Component, org.jitsi.android.util.java.awt.Rectangle, float, float):void");
    }

    public void layoutContainer(Container parent) {
        layoutContainer(parent, 0.5f);
    }

    /* access modifiers changed from: protected */
    public void layoutContainer(Container parent, float componentAlignmentX) {
        Component component = getComponent(parent);
        if (component != null) {
            layoutComponent(component, new Rectangle(parent.getSize()), componentAlignmentX, 0.5f);
        }
    }

    public Dimension minimumLayoutSize(Container parent) {
        Component component = getComponent(parent);
        return component != null ? component.getMinimumSize() : new Dimension(16, 16);
    }

    public Dimension preferredLayoutSize(Container parent) {
        Component component = getComponent(parent);
        return component != null ? component.getPreferredSize() : new Dimension(16, 16);
    }

    public void removeLayoutComponent(Component comp) {
    }
}
