package java.awt;

public class Canvas extends Component {

    public void addNotify() {
        /*synchronized(this.getTreeLock()) {
            if (this.peer == null) {
                this.peer = this.getToolkit().createCanvas(this);
            }

            super.addNotify();
        }*/
    }
}
