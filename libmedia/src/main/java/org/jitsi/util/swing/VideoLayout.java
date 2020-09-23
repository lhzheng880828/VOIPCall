//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.jitsi.util.swing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JLabel;

public class VideoLayout extends FitLayout {
    public static final String CANVAS = "CANVAS";
    public static final String CENTER_REMOTE = "CENTER_REMOTE";
    public static final String CLOSE_LOCAL_BUTTON = "CLOSE_LOCAL_BUTTON";
    public static final String EAST_REMOTE = "EAST_REMOTE";
    private static final int HGAP = 10;
    public static final String LOCAL = "LOCAL";
    private static final float LOCAL_TO_REMOTE_RATIO = 0.3F;
    private Component canvas;
    private Component closeButton;
    private final boolean conference;
    private final Map<Component, Object> constraints = new HashMap();
    private Component local;
    private float remoteAlignmentX = 0.5F;
    private final List<Component> remotes = new LinkedList();

    public VideoLayout(boolean conference) {
        this.conference = conference;
    }

    public void addLayoutComponent(String name, Component comp) {
        super.addLayoutComponent(name, comp);
        synchronized(this.constraints) {
            this.constraints.put(comp, name);
        }

        if (name != null && !name.equals("CENTER_REMOTE")) {
            if (name.equals("EAST_REMOTE")) {
                if (!this.remotes.contains(comp)) {
                    this.remotes.add(comp);
                }

                this.remoteAlignmentX = 1.0F;
            } else if (name.equals("LOCAL")) {
                this.local = comp;
            } else if (name.equals("CLOSE_LOCAL_BUTTON")) {
                this.closeButton = comp;
            } else if (name.equals("CANVAS")) {
                this.canvas = comp;
            }
        } else {
            if (!this.remotes.contains(comp)) {
                this.remotes.add(comp);
            }

            this.remoteAlignmentX = 0.5F;
        }

    }

    public static boolean areAspectRatiosEqual(Dimension size, int width, int height) {
        if (size.height != 0 && height != 0) {
            double a = (double)size.width / (double)size.height;
            double b = (double)width / (double)height;
            double diff = a - b;
            return -0.01D < diff && diff < 0.01D;
        } else {
            return false;
        }
    }

    private int calculateColumnCount(List<Component> remotes) {
        int remoteCount = remotes.size();
        if (remoteCount == 1) {
            return 1;
        } else {
            return remoteCount != 2 && remoteCount != 4 ? 3 : 2;
        }
    }

    protected Component getComponent(Container parent) {
        return this.remotes.size() == 1 ? (Component)this.remotes.get(0) : null;
    }

    public Object getComponentConstraints(Component c) {
        synchronized(this.constraints) {
            return this.constraints.get(c);
        }
    }

    public Component getLocal() {
        return this.local;
    }

    public Component getLocalCloseButton() {
        return this.closeButton;
    }

    public void layoutContainer(Container parent) {
        List<Component> visibleRemotes = new ArrayList();
        Component local = this.getLocal();

        int remoteCount;
        for(remoteCount = 0; remoteCount < this.remotes.size(); ++remoteCount) {
            if (((Component)this.remotes.get(remoteCount)).isVisible()) {
                visibleRemotes.add(this.remotes.get(remoteCount));
            }
        }

        List<Component> remotes;
        if (!this.conference && (visibleRemotes.size() <= 1 || local == null)) {
            remotes = visibleRemotes;
        } else {
            remotes = new ArrayList();
            remotes.addAll(visibleRemotes);
            if (local != null) {
                remotes.add(local);
            }
        }

        remoteCount = remotes.size();
        Dimension parentSize = parent.getSize();
        int localX;
        int localY;
        int rowsMinus1;
        if (!this.conference && remoteCount == 1) {
            super.layoutContainer(parent, local == null ? 0.5F : this.remoteAlignmentX);
        } else if (remoteCount > 0) {
            int columns = this.calculateColumnCount(remotes);
            localX = columns - 1;
            localY = (remoteCount + localX) / columns;
            rowsMinus1 = localY - 1;
            Rectangle bounds = new Rectangle(0, 0, (parentSize.width - localX * 10) / columns, parentSize.height / localY);

            for(int i = 0; i < remoteCount; ++i) {
                int column = i % columns;
                int row = i / columns;
                if (column == 0) {
                    bounds.x = 0;
                    if (row == rowsMinus1) {
                        int available = remoteCount - i;
                        if (available < columns) {
                            bounds.x = (parentSize.width - available * bounds.width - (available - 1) * 10) / 2;
                        }
                    }
                } else {
                    bounds.x += bounds.width + 10;
                }

                bounds.y = row * bounds.height;
                super.layoutComponent((Component)remotes.get(i), bounds, 0.5F, 0.5F);
            }
        }

        if (local == null) {
            if (this.closeButton != null) {
                this.closeButton.setVisible(false);
            }
        } else {
            if (!remotes.contains(local)) {
                Component remote0 = remotes.isEmpty() ? null : (Component)remotes.get(0);
                rowsMinus1 = Math.round((float)parentSize.height * 0.3F);
                int width = Math.round((float)parentSize.width * 0.3F);
                float alignmentX;
                if (remoteCount == 1 && remote0 instanceof JLabel) {
                    localX = (parentSize.width - width) / 2;
                    localY = parentSize.height - rowsMinus1;
                    alignmentX = 0.5F;
                } else {
                    localX = (remote0 == null ? 0 : remote0.getX()) + 5;
                    localY = parentSize.height - rowsMinus1 - 5;
                    alignmentX = 0.0F;
                }

                super.layoutComponent(local, new Rectangle(localX, localY, width, rowsMinus1), alignmentX, 1.0F);
            }

            if (this.closeButton != null) {
                this.closeButton.setVisible(local.isVisible());
                super.layoutComponent(this.closeButton, new Rectangle(local.getX() + local.getWidth() - this.closeButton.getWidth(), local.getY(), this.closeButton.getWidth(), this.closeButton.getHeight()), 0.5F, 0.5F);
            }
        }

        if (this.canvas != null) {
            this.canvas.setBounds(0, 0, parentSize.width, parentSize.height);
        }

    }

    public Dimension preferredLayoutSize(Container parent) {
        List<Component> visibleRemotes = new ArrayList();
        Component local = this.getLocal();

        int remoteCount;
        for(remoteCount = 0; remoteCount < this.remotes.size(); ++remoteCount) {
            if (((Component)this.remotes.get(remoteCount)).isVisible()) {
                visibleRemotes.add(this.remotes.get(remoteCount));
            }
        }

        List remotes;
        if (this.conference || visibleRemotes.size() > 1 && local != null) {
            remotes = new ArrayList();
            remotes.addAll(visibleRemotes);
            if (local != null) {
                remotes.add(local);
            }
        } else {
            remotes = visibleRemotes;
        }

        remoteCount = remotes.size();
        Dimension prefLayoutSize;
        int columnsMinus1;
        int rows;
        if (!this.conference && remoteCount == 1) {
            prefLayoutSize = super.preferredLayoutSize(parent);
        } else if (remoteCount > 0) {
            int columns = this.calculateColumnCount(remotes);
            columnsMinus1 = columns - 1;
            rows = (remoteCount + columnsMinus1) / columns;
            int i = 0;
            Dimension[] prefSizes = new Dimension[columns * rows];
            Iterator i$ = remotes.iterator();

            int row;
            int prefRowHeight;
            while(i$.hasNext()) {
                Component remote = (Component)i$.next();
                row = columnsMinus1 - i % columns;
                prefRowHeight = i / columns;
                prefSizes[row + prefRowHeight * columns] = remote.getPreferredSize();
                ++i;
                if (i >= remoteCount) {
                    break;
                }
            }

            int prefLayoutWidth = 0;

            int prefLayoutHeight;
            for(prefLayoutHeight = 0; prefLayoutHeight < columns; ++prefLayoutHeight) {
                row = 0;

                for(prefRowHeight = 0; prefRowHeight < rows; ++prefRowHeight) {
                    Dimension prefSize = prefSizes[prefLayoutHeight + prefRowHeight * columns];
                    if (prefSize != null) {
                        row += prefSize.width;
                    }
                }

                row /= rows;
                prefLayoutWidth += row;
            }

            prefLayoutHeight = 0;

            for(row = 0; row < rows; ++row) {
                prefRowHeight = 0;

                for(int column = 0; column < columns; ++column) {
                    Dimension prefSize = prefSizes[column + row * columns];
                    if (prefSize != null) {
                        prefRowHeight = prefSize.height;
                    }
                }

                prefRowHeight /= columns;
                prefLayoutHeight += prefRowHeight;
            }

            prefLayoutSize = new Dimension(prefLayoutWidth + columnsMinus1 * 10, prefLayoutHeight);
        } else {
            prefLayoutSize = null;
        }

        if (local != null && !remotes.contains(local) && prefLayoutSize == null) {
            Dimension prefSize = local.getPreferredSize();
            if (prefSize != null) {
                columnsMinus1 = Math.round((float)prefSize.height * 0.3F);
                rows = Math.round((float)prefSize.width * 0.3F);
                prefLayoutSize = new Dimension(rows, columnsMinus1);
            }
        }

        if (prefLayoutSize == null) {
            prefLayoutSize = super.preferredLayoutSize(parent);
        } else if (prefLayoutSize.height < 1 || prefLayoutSize.width < 1) {
            prefLayoutSize.height = 16;
            prefLayoutSize.width = 16;
        }

        return prefLayoutSize;
    }

    public void removeLayoutComponent(Component comp) {
        super.removeLayoutComponent(comp);
        synchronized(this.constraints) {
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
