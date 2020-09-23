package org.jitsi.util.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.media.Controller;
import org.jitsi.android.util.java.awt.Color;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.event.ContainerEvent;
import org.jitsi.android.util.java.awt.event.ContainerListener;
import org.jitsi.android.util.javax.swing.SwingUtilities;

public class VideoContainer extends TransparentPanel {
    public static final Color DEFAULT_BACKGROUND_COLOR = Color.BLACK;
    private static final String PREFERRED_SIZE_PROPERTY_NAME = "preferredSize";
    private static final long serialVersionUID = 0;
    private int inAddOrRemove;
    private final Component noVideoComponent;
    private final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
            VideoContainer.this.propertyChange(ev);
        }
    };
    private final Object syncRoot = new Object();
    private boolean validateAndRepaint;

    public VideoContainer(Component noVideoComponent, boolean conference) {
        setLayout(new VideoLayout(conference));
        this.noVideoComponent = noVideoComponent;
        if (DEFAULT_BACKGROUND_COLOR != null) {
            setBackground(DEFAULT_BACKGROUND_COLOR);
        }
        addContainerListener(new ContainerListener() {
            public void componentAdded(ContainerEvent ev) {
                VideoContainer.this.onContainerEvent(ev);
            }

            public void componentRemoved(ContainerEvent ev) {
                VideoContainer.this.onContainerEvent(ev);
            }
        });
        if (this.noVideoComponent != null) {
            add(this.noVideoComponent, VideoLayout.CENTER_REMOTE, -1);
        }
    }

    public Component add(Component comp) {
        add(comp, VideoLayout.CENTER_REMOTE);
        return comp;
    }

    public Component add(Component comp, int index) {
        add(comp, null, index);
        return comp;
    }

    public void add(Component comp, Object constraints) {
        add(comp, constraints, -1);
    }

    public void add(Component comp, Object constraints, int index) {
        enterAddOrRemove();
        try {
            if (!(!VideoLayout.CENTER_REMOTE.equals(constraints) || this.noVideoComponent == null || this.noVideoComponent.equals(comp)) || (comp.equals(this.noVideoComponent) && this.noVideoComponent.getParent() != null)) {
                remove(this.noVideoComponent);
            }
            super.add(comp, constraints, index);
        } finally {
            exitAddOrRemove();
        }
    }

    private void enterAddOrRemove() {
        synchronized (this.syncRoot) {
            if (this.inAddOrRemove == 0) {
                this.validateAndRepaint = false;
            }
            this.inAddOrRemove++;
        }
    }

    private void exitAddOrRemove() {
        synchronized (this.syncRoot) {
            this.inAddOrRemove--;
            if (this.inAddOrRemove < 1) {
                this.inAddOrRemove = 0;
                if (this.validateAndRepaint) {
                    this.validateAndRepaint = false;
                    if (isDisplayable()) {
                        if (isValid()) {
                            doLayout();
                        } else {
                            validate();
                        }
                        repaint();
                    } else {
                        doLayout();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void onContainerEvent(ContainerEvent ev) {
        boolean z = true;
        try {
            Component component = ev.getChild();
            switch (ev.getID()) {
                case Controller.Realized /*300*/:
                    component.addPropertyChangeListener(PREFERRED_SIZE_PROPERTY_NAME, this.propertyChangeListener);
                    break;
                case 301:
                    component.removePropertyChangeListener(PREFERRED_SIZE_PROPERTY_NAME, this.propertyChangeListener);
                    break;
            }
            if (DEFAULT_BACKGROUND_COLOR != null) {
                int componentCount = getComponentCount();
                if (componentCount == 1 && getComponent(0) == this.noVideoComponent) {
                    componentCount = 0;
                }
                if (componentCount <= 0) {
                    z = false;
                }
                setOpaque(z);
            }
            synchronized (this.syncRoot) {
                if (this.inAddOrRemove != 0) {
                    this.validateAndRepaint = true;
                }
            }
        } catch (Throwable th) {
            synchronized (this.syncRoot) {
                if (this.inAddOrRemove != 0) {
                    this.validateAndRepaint = true;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void propertyChange(PropertyChangeEvent ev) {
        if (PREFERRED_SIZE_PROPERTY_NAME.equals(ev.getPropertyName()) && SwingUtilities.isEventDispatchThread()) {
            synchronized (this.syncRoot) {
                enterAddOrRemove();
                this.validateAndRepaint = true;
                exitAddOrRemove();
            }
        }
    }

    public void remove(Component comp) {
        enterAddOrRemove();
        try {
            super.remove(comp);
            VideoLayout videoLayout = (VideoLayout) getLayout();
            boolean hasComponentsAtCenterRemote = false;
            for (Component c : getComponents()) {
                if (!c.equals(this.noVideoComponent) && VideoLayout.CENTER_REMOTE.equals(videoLayout.getComponentConstraints(c))) {
                    hasComponentsAtCenterRemote = true;
                    break;
                }
            }
            if (!(hasComponentsAtCenterRemote || this.noVideoComponent == null || this.noVideoComponent.equals(comp))) {
                add(this.noVideoComponent, VideoLayout.CENTER_REMOTE);
            }
            exitAddOrRemove();
        } catch (Throwable th) {
            exitAddOrRemove();
        }
    }

    public void removeAll() {
        enterAddOrRemove();
        try {
            super.removeAll();
            if (this.noVideoComponent != null) {
                add(this.noVideoComponent, VideoLayout.CENTER_REMOTE);
            }
            exitAddOrRemove();
        } catch (Throwable th) {
            exitAddOrRemove();
        }
    }
}
