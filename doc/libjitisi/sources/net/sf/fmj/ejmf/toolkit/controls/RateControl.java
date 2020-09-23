package net.sf.fmj.ejmf.toolkit.controls;

import javax.media.Control;
import javax.media.Controller;
import org.jitsi.android.util.java.awt.Component;

public class RateControl implements Control {
    private Component controlComponent;
    private Controller controller;

    public RateControl(Controller controller) {
        this.controller = controller;
    }

    public Component getControlComponent() {
        if (this.controlComponent == null) {
            this.controlComponent = new RateControlComponent(this.controller);
        }
        return this.controlComponent;
    }
}
