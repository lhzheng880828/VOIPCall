package java.awt;

import java.awt.event.ContainerListener;

public class Container extends Component {
    public Component add(Component component) {
        return null;
    }

    public LayoutManager getLayout() {
        return null;
    }

    public void setLayout(LayoutManager layout) {
    }

    public void removeAll() {
    }

    public void remove(Component comp) {
    }

    public synchronized void addContainerListener(ContainerListener l) {
    }

    public Component add(Component comp, int index) {
        return null;
    }

    public void add(Component comp, Object constraints) {
    }

    public void add(Component comp, Object constraints, int index) {
    }

    public void validate() {
    }

    public void doLayout() {
    }

    public int getComponentCount() {
        return 0;
    }

    public Component getComponent(int n) {
        return null;
    }

    public Component[] getComponents() {
        return new Component[0];
    }

    public int getComponentZOrder(Component var1) {
        /*if (var1 == null) {
            return -1;
        } else {
            synchronized(this.getTreeLock()) {
                return var1.parent != this ? -1 : this.component.indexOf(var1);
            }
        }*/
        return -1;
    }
}
