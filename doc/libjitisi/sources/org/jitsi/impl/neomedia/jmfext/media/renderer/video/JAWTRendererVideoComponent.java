package org.jitsi.impl.neomedia.jmfext.media.renderer.video;

import org.jitsi.android.util.java.awt.Canvas;
import org.jitsi.android.util.java.awt.Container;
import org.jitsi.android.util.java.awt.Graphics;

public class JAWTRendererVideoComponent extends Canvas {
    private static final long serialVersionUID = 0;
    protected final JAWTRenderer renderer;
    private boolean wantsPaint = true;

    public JAWTRendererVideoComponent(JAWTRenderer renderer) {
        this.renderer = renderer;
    }

    public void addNotify() {
        JAWTRendererVideoComponent.super.addNotify();
        this.wantsPaint = true;
    }

    /* access modifiers changed from: protected */
    public long getHandle() {
        return this.renderer.getHandle();
    }

    /* access modifiers changed from: protected */
    public Object getHandleLock() {
        return this.renderer.getHandleLock();
    }

    public void paint(Graphics g) {
        if (this.wantsPaint && getWidth() >= 4 && getHeight() >= 4) {
            synchronized (getHandleLock()) {
                long handle = getHandle();
                if (handle != 0) {
                    Container parent = getParent();
                    this.wantsPaint = JAWTRenderer.paint(handle, this, g, parent == null ? -1 : parent.getComponentZOrder(this));
                }
            }
        }
    }

    public void removeNotify() {
        this.wantsPaint = true;
        JAWTRendererVideoComponent.super.removeNotify();
    }

    public void update(Graphics g) {
        synchronized (getHandleLock()) {
            if (!this.wantsPaint || getHandle() == 0) {
                JAWTRendererVideoComponent.super.update(g);
                return;
            }
            paint(g);
        }
    }
}
