package net.sf.fmj.ejmf.toolkit.controls;

import javax.media.Controller;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.RateChangeEvent;
import org.jitsi.android.util.java.awt.BorderLayout;
import org.jitsi.android.util.java.awt.event.ActionEvent;
import org.jitsi.android.util.java.awt.event.ActionListener;
import org.jitsi.android.util.javax.swing.JLabel;
import org.jitsi.android.util.javax.swing.JPanel;
import org.jitsi.android.util.javax.swing.JTextField;
import org.jitsi.android.util.javax.swing.SwingUtilities;
import org.jitsi.android.util.javax.swing.border.CompoundBorder;
import org.jitsi.android.util.javax.swing.border.EmptyBorder;
import org.jitsi.android.util.javax.swing.border.EtchedBorder;
import org.jitsi.android.util.javax.swing.border.TitledBorder;

public class RateControlComponent extends JPanel implements ActionListener, ControllerListener {
    private Controller controller;
    private JTextField rateField = new JTextField(6);

    class LoadRateThread implements Runnable {
        LoadRateThread() {
        }

        public void run() {
            RateControlComponent.this.loadRate();
        }
    }

    public RateControlComponent(Controller controller) {
        this.controller = controller;
        setUpControlComponent();
        SwingUtilities.invokeLater(new LoadRateThread());
        this.rateField.addActionListener(this);
        controller.addControllerListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.rateField) {
            try {
                this.controller.setRate(Float.valueOf(this.rateField.getText()).floatValue());
            } catch (NumberFormatException e2) {
            }
            loadRate();
        }
    }

    public void controllerUpdate(ControllerEvent e) {
        if (e.getSourceController() == this.controller && (e instanceof RateChangeEvent)) {
            SwingUtilities.invokeLater(new LoadRateThread());
        }
    }

    /* access modifiers changed from: private */
    public void loadRate() {
        this.rateField.setText(Float.toString(this.controller.getRate()));
    }

    private void setUpControlComponent() {
        JLabel rateLabel = new JLabel("Rate:", 4);
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new TitledBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(10, 10, 10, 10)), "Rate Control"));
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.add(rateLabel, "Center");
        mainPanel.add(this.rateField, "East");
        add(mainPanel);
    }
}
