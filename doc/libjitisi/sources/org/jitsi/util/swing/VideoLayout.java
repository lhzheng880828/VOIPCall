package org.jitsi.util.swing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.Container;
import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Rectangle;
import org.jitsi.android.util.javax.swing.JLabel;

public class VideoLayout extends FitLayout {
    public static final String CANVAS = "CANVAS";
    public static final String CENTER_REMOTE = "CENTER_REMOTE";
    public static final String CLOSE_LOCAL_BUTTON = "CLOSE_LOCAL_BUTTON";
    public static final String EAST_REMOTE = "EAST_REMOTE";
    private static final int HGAP = 10;
    public static final String LOCAL = "LOCAL";
    private static final float LOCAL_TO_REMOTE_RATIO = 0.3f;
    private Component canvas;
    private Component closeButton;
    private final boolean conference;
    private final Map<Component, Object> constraints = new HashMap();
    private Component local;
    private float remoteAlignmentX = 0.5f;
    private final List<Component> remotes = new LinkedList();

    public VideoLayout(boolean conference) {
        this.conference = conference;
    }

    public void addLayoutComponent(String name, Component comp) {
        super.addLayoutComponent(name, comp);
        synchronized (this.constraints) {
            this.constraints.put(comp, name);
        }
        if (name == null || name.equals(CENTER_REMOTE)) {
            if (!this.remotes.contains(comp)) {
                this.remotes.add(comp);
            }
            this.remoteAlignmentX = 0.5f;
        } else if (name.equals(EAST_REMOTE)) {
            if (!this.remotes.contains(comp)) {
                this.remotes.add(comp);
            }
            this.remoteAlignmentX = 1.0f;
        } else if (name.equals(LOCAL)) {
            this.local = comp;
        } else if (name.equals(CLOSE_LOCAL_BUTTON)) {
            this.closeButton = comp;
        } else if (name.equals(CANVAS)) {
            this.canvas = comp;
        }
    }

    public static boolean areAspectRatiosEqual(Dimension size, int width, int height) {
        if (size.height == 0 || height == 0) {
            return false;
        }
        double diff = (((double) size.width) / ((double) size.height)) - (((double) width) / ((double) height));
        if (-0.01d >= diff || diff >= 0.01d) {
            return false;
        }
        return true;
    }

    private int calculateColumnCount(List<Component> remotes) {
        int remoteCount = remotes.size();
        if (remoteCount == 1) {
            return 1;
        }
        if (remoteCount == 2 || remoteCount == 4) {
            return 2;
        }
        return 3;
    }

    /* access modifiers changed from: protected */
    public Component getComponent(Container parent) {
        return this.remotes.size() == 1 ? (Component) this.remotes.get(0) : null;
    }

    public Object getComponentConstraints(Component c) {
        Object obj;
        synchronized (this.constraints) {
            obj = this.constraints.get(c);
        }
        return obj;
    }

    public Component getLocal() {
        return this.local;
    }

    public Component getLocalCloseButton() {
        return this.closeButton;
    }

    public void layoutContainer(Container parent) {
        int i;
        List<Component> remotes;
        List<Component> visibleRemotes = new ArrayList();
        Component local = getLocal();
        for (i = 0; i < this.remotes.size(); i++) {
            if (((Component) this.remotes.get(i)).isVisible()) {
                visibleRemotes.add(this.remotes.get(i));
            }
        }
        if (this.conference || (visibleRemotes.size() > 1 && local != null)) {
            remotes = new ArrayList();
            remotes.addAll(visibleRemotes);
            if (local != null) {
                remotes.add(local);
            }
        } else {
            remotes = visibleRemotes;
        }
        int remoteCount = remotes.size();
        Dimension parentSize = parent.getSize();
        if (!this.conference && remoteCount == 1) {
            float f;
            if (local == null) {
                f = 0.5f;
            } else {
                f = this.remoteAlignmentX;
            }
            super.layoutContainer(parent, f);
        } else if (remoteCount > 0) {
            int columns = calculateColumnCount(remotes);
            int columnsMinus1 = columns - 1;
            int rows = (remoteCount + columnsMinus1) / columns;
            int rowsMinus1 = rows - 1;
            Rectangle bounds = new Rectangle(0, 0, (parentSize.width - (columnsMinus1 * 10)) / columns, parentSize.height / rows);
            for (i = 0; i < remoteCount; i++) {
                int row = i / columns;
                if (i % columns == 0) {
                    bounds.x = 0;
                    if (row == rowsMinus1) {
                        int available = remoteCount - i;
                        if (available < columns) {
                            bounds.x = ((parentSize.width - (bounds.width * available)) - ((available - 1) * 10)) / 2;
                        }
                    }
                } else {
                    bounds.x += bounds.width + 10;
                }
                bounds.y = bounds.height * row;
                super.layoutComponent((Component) remotes.get(i), bounds, 0.5f, 0.5f);
            }
        }
        if (local != null) {
            if (!remotes.contains(local)) {
                Component remote0;
                int localX;
                int localY;
                float alignmentX;
                if (remotes.isEmpty()) {
                    remote0 = null;
                } else {
                    remote0 = (Component) remotes.get(0);
                }
                int height = Math.round(((float) parentSize.height) * LOCAL_TO_REMOTE_RATIO);
                int width = Math.round(((float) parentSize.width) * LOCAL_TO_REMOTE_RATIO);
                if (remoteCount == 1 && (remote0 instanceof JLabel)) {
                    localX = (parentSize.width - width) / 2;
                    localY = parentSize.height - height;
                    alignmentX = 0.5f;
                } else {
                    localX = (remote0 == null ? 0 : remote0.getX()) + 5;
                    localY = (parentSize.height - height) - 5;
                    alignmentX = 0.0f;
                }
                super.layoutComponent(local, new Rectangle(localX, localY, width, height), alignmentX, 1.0f);
            }
            if (this.closeButton != null) {
                this.closeButton.setVisible(local.isVisible());
                super.layoutComponent(this.closeButton, new Rectangle((local.getX() + local.getWidth()) - this.closeButton.getWidth(), local.getY(), this.closeButton.getWidth(), this.closeButton.getHeight()), 0.5f, 0.5f);
            }
        } else if (this.closeButton != null) {
            this.closeButton.setVisible(false);
        }
        if (this.canvas != null) {
            this.canvas.setBounds(0, 0, parentSize.width, parentSize.height);
        }
    }

    public Dimension preferredLayoutSize(Container parent) {
        int i;
        List<Component> remotes;
        Dimension prefLayoutSize;
        Dimension prefSize;
        List<Component> visibleRemotes = new ArrayList();
        Component local = getLocal();
        for (i = 0; i < this.remotes.size(); i++) {
            if (((Component) this.remotes.get(i)).isVisible()) {
                visibleRemotes.add(this.remotes.get(i));
            }
        }
        if (this.conference || (visibleRemotes.size() > 1 && local != null)) {
            remotes = new ArrayList();
            remotes.addAll(visibleRemotes);
            if (local != null) {
                remotes.add(local);
            }
        } else {
            remotes = visibleRemotes;
        }
        int remoteCount = remotes.size();
        if (!this.conference && remoteCount == 1) {
            prefLayoutSize = super.preferredLayoutSize(parent);
        } else if (remoteCount > 0) {
            int column;
            int row;
            int columns = calculateColumnCount(remotes);
            int columnsMinus1 = columns - 1;
            int rows = (remoteCount + columnsMinus1) / columns;
            i = 0;
            Dimension[] prefSizes = new Dimension[(columns * rows)];
            for (Component remote : remotes) {
                prefSizes[((i / columns) * columns) + (columnsMinus1 - (i % columns))] = remote.getPreferredSize();
                i++;
                if (i >= remoteCount) {
                    break;
                }
            }
            int prefLayoutWidth = 0;
            for (column = 0; column < columns; column++) {
                int prefColumnWidth = 0;
                for (row = 0; row < rows; row++) {
                    prefSize = prefSizes[(row * columns) + column];
                    if (prefSize != null) {
                        prefColumnWidth += prefSize.width;
                    }
                }
                prefLayoutWidth += prefColumnWidth / rows;
            }
            int prefLayoutHeight = 0;
            for (row = 0; row < rows; row++) {
                int prefRowHeight = 0;
                for (column = 0; column < columns; column++) {
                    prefSize = prefSizes[(row * columns) + column];
                    if (prefSize != null) {
                        prefRowHeight = prefSize.height;
                    }
                }
                prefLayoutHeight += prefRowHeight / columns;
            }
            prefLayoutSize = new Dimension((columnsMinus1 * 10) + prefLayoutWidth, prefLayoutHeight);
        } else {
            prefLayoutSize = null;
        }
        if (!(local == null || remotes.contains(local) || prefLayoutSize != null)) {
            prefSize = local.getPreferredSize();
            if (prefSize != null) {
                prefLayoutSize = new Dimension(Math.round(((float) prefSize.width) * LOCAL_TO_REMOTE_RATIO), Math.round(((float) prefSize.height) * LOCAL_TO_REMOTE_RATIO));
            }
        }
        if (prefLayoutSize == null) {
            return super.preferredLayoutSize(parent);
        }
        if (prefLayoutSize.height >= 1 && prefLayoutSize.width >= 1) {
            return prefLayoutSize;
        }
        prefLayoutSize.height = 16;
        prefLayoutSize.width = 16;
        return prefLayoutSize;
    }

    public void removeLayoutComponent(Component comp) {
        super.removeLayoutComponent(comp);
        synchronized (this.constraints) {
            this.constraints.remove(comp);
        }
        if (this.local == comp) {
            this.local = null;
        } else if (this.closeButton == comp) {
            this.closeButton = null;
        } else if (this.canvas == comp) {
            this.canvas = null;
        } else {
            this.remotes.remove(comp);
        }
    }
}
