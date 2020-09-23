package net.sf.fmj.media.rtp;

import java.util.Vector;
import javax.media.Format;
import javax.media.control.BufferControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import org.jitsi.android.util.java.awt.BorderLayout;
import org.jitsi.android.util.java.awt.Button;
import org.jitsi.android.util.java.awt.Choice;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.FlowLayout;
import org.jitsi.android.util.java.awt.Label;
import org.jitsi.android.util.java.awt.Panel;
import org.jitsi.android.util.java.awt.TextField;
import org.jitsi.android.util.java.awt.event.ActionEvent;
import org.jitsi.android.util.java.awt.event.ActionListener;
import org.jitsi.android.util.java.awt.event.ItemEvent;
import org.jitsi.android.util.java.awt.event.ItemListener;

public class BufferControlImpl implements BufferControl {
    private static final int AUDIO_DEFAULT_BUFFER = 250;
    private static final int AUDIO_DEFAULT_THRESHOLD = 125;
    private static final int AUDIO_MAX_BUFFER = 4000;
    private static final int AUDIO_MAX_THRESHOLD = 2000;
    private static final int NOT_SPECIFIED = Integer.MAX_VALUE;
    private static final int VIDEO_DEFAULT_BUFFER = 135;
    private static final int VIDEO_DEFAULT_THRESHOLD = 0;
    private static final int VIDEO_MAX_BUFFER = 4000;
    private static final int VIDEO_MAX_THRESHOLD = 0;
    BufferControlPanel controlComp = null;
    private long currBuffer = 2147483647L;
    private long currThreshold = 2147483647L;
    private long defBuffer = 2147483647L;
    private long defThreshold = 2147483647L;
    private boolean inited = false;
    private long maxBuffer = 2147483647L;
    private long maxThreshold = 2147483647L;
    private Vector<RTPSourceStream> sourcestreamlist = new Vector(1);
    boolean threshold_enabled = true;

    private class BufferControlPanel extends Panel {
        Button bb;
        Choice bchoice;
        TextField bsize;
        TextField btext;
        Panel buffersize;
        Button tb;
        Choice tchoice;
        Panel threshold;
        TextField tsize;
        TextField ttext;

        public BufferControlPanel() {
            super(new BorderLayout());
            this.buffersize = null;
            this.threshold = null;
            this.btext = null;
            this.bchoice = null;
            this.tchoice = null;
            this.ttext = null;
            this.tb = null;
            this.buffersize = new Panel(new FlowLayout());
            this.buffersize.add(new Label("BufferSize"));
            this.bsize = new TextField(15);
            updateBuffer(BufferControlImpl.this.getBufferLength());
            this.bsize.setEnabled(false);
            this.buffersize.add(this.bsize);
            this.buffersize.add(new Label("Update"));
            Panel panel = this.buffersize;
            Choice choice = new Choice();
            this.bchoice = choice;
            panel.add(choice);
            this.bchoice.add("DEFAULT");
            this.bchoice.add("MAX");
            this.bchoice.add("User Defined");
            this.bchoice.addItemListener(new ItemListener(BufferControlImpl.this) {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getItem().equals("User Defined")) {
                        BufferControlPanel.this.btext.setEnabled(true);
                    } else {
                        BufferControlPanel.this.btext.setEnabled(false);
                    }
                }
            });
            this.buffersize.add(new Label("If User Defined, Enter here:"));
            panel = this.buffersize;
            TextField textField = new TextField(10);
            this.btext = textField;
            panel.add(textField);
            this.btext.setEnabled(false);
            panel = this.buffersize;
            Button button = new Button("Commit");
            this.bb = button;
            panel.add(button);
            this.bb.addActionListener(new ActionListener(BufferControlImpl.this) {
                public void actionPerformed(ActionEvent e) {
                    BufferControlPanel.this.buffersizeUpdate();
                }
            });
            this.threshold = new Panel(new FlowLayout());
            this.threshold.add(new Label("Threshold"));
            this.tsize = new TextField(15);
            updateThreshold(BufferControlImpl.this.getMinimumThreshold());
            this.tsize.setEnabled(false);
            this.threshold.add(this.tsize);
            this.threshold.add(new Label("Update"));
            panel = this.threshold;
            choice = new Choice();
            this.tchoice = choice;
            panel.add(choice);
            this.tchoice.add("DEFAULT");
            this.tchoice.add("MAX");
            this.tchoice.add("User Defined");
            this.tchoice.addItemListener(new ItemListener(BufferControlImpl.this) {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getItem().equals("User Defined")) {
                        BufferControlPanel.this.ttext.setEnabled(true);
                    } else {
                        BufferControlPanel.this.ttext.setEnabled(false);
                    }
                }
            });
            this.threshold.add(new Label("If User Defined, Enter here:"));
            panel = this.threshold;
            textField = new TextField(10);
            this.ttext = textField;
            panel.add(textField);
            this.ttext.setEnabled(false);
            panel = this.threshold;
            button = new Button("Commit");
            this.tb = button;
            panel.add(button);
            this.tb.addActionListener(new ActionListener(BufferControlImpl.this) {
                public void actionPerformed(ActionEvent e) {
                    BufferControlPanel.this.thresholdUpdate();
                }
            });
            add(this.buffersize, "North");
            add(new Label("Actual buffer & threshold sizes (in millisec) not displayed until media type is determined"), "Center");
            add(this.threshold, "South");
            setVisible(true);
        }

        /* access modifiers changed from: private */
        public void buffersizeUpdate() {
            long b;
            String s = this.bchoice.getSelectedItem();
            if (s.equals("MAX")) {
                b = -2;
            } else if (s.equals("DEFAULT")) {
                b = -1;
            } else {
                b = new Long(this.btext.getText()).longValue();
            }
            updateBuffer(BufferControlImpl.this.setBufferLength(b));
        }

        /* access modifiers changed from: private */
        public void thresholdUpdate() {
            long t;
            String s = this.tchoice.getSelectedItem();
            if (s.equals("DEFAULT")) {
                t = -1;
            } else if (s.equals("MAX")) {
                t = -2;
            } else {
                t = new Long(this.ttext.getText()).longValue();
            }
            updateThreshold(BufferControlImpl.this.setMinimumThreshold(t));
        }

        public void updateBuffer(long b) {
            if (b != 2147483647L && b != -2 && b != -1) {
                this.bsize.setText(new Long(b).toString());
            }
        }

        public void updateThreshold(long d) {
            if (d != 2147483647L && d != -2 && d != -1) {
                this.tsize.setText(new Long(d).toString());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void addSourceStream(RTPSourceStream s) {
        this.sourcestreamlist.addElement(s);
        s.setBufferControl(this);
    }

    public long getBufferLength() {
        return this.currBuffer;
    }

    public Component getControlComponent() {
        if (this.controlComp == null) {
            this.controlComp = new BufferControlPanel();
        }
        return this.controlComp;
    }

    public boolean getEnabledThreshold() {
        return this.threshold_enabled;
    }

    public long getMinimumThreshold() {
        return this.currThreshold;
    }

    /* access modifiers changed from: protected */
    public void initBufferControl(Format f) {
        long j = 4000;
        long j2 = 0;
        long j3;
        if (f instanceof AudioFormat) {
            this.defBuffer = this.defBuffer != 2147483647L ? this.currBuffer : 250;
            this.defThreshold = this.defThreshold != 2147483647L ? this.currThreshold : 125;
            if (this.maxBuffer != 2147483647L) {
                j3 = this.maxBuffer;
            } else {
                j3 = 4000;
            }
            this.maxBuffer = j3;
            this.maxThreshold = this.maxThreshold != 2147483647L ? this.maxThreshold : 2000;
            this.currBuffer = this.currBuffer != 2147483647L ? this.currBuffer : this.defBuffer;
            if (this.currThreshold != 2147483647L) {
                j3 = this.currThreshold;
            } else {
                j3 = this.defThreshold;
            }
            this.currThreshold = j3;
        } else if (f instanceof VideoFormat) {
            this.defBuffer = this.defBuffer != 2147483647L ? this.currBuffer : 135;
            if (this.defThreshold != 2147483647L) {
                j3 = this.currThreshold;
            } else {
                j3 = 0;
            }
            this.defThreshold = j3;
            if (this.maxBuffer != 2147483647L) {
                j = this.maxBuffer;
            }
            this.maxBuffer = j;
            if (this.maxThreshold != 2147483647L) {
                j2 = this.maxThreshold;
            }
            this.maxThreshold = j2;
            this.currBuffer = this.currBuffer != 2147483647L ? this.currBuffer : this.defBuffer;
            if (this.currThreshold != 2147483647L) {
                j3 = this.currThreshold;
            } else {
                j3 = this.defThreshold;
            }
            this.currThreshold = j3;
        }
        if (this.currBuffer == -2) {
            this.currBuffer = this.maxBuffer;
        }
        if (this.currBuffer == -1) {
            this.currBuffer = this.defBuffer;
        }
        if (this.currThreshold == -2) {
            this.currThreshold = this.maxThreshold;
        }
        if (this.currThreshold == -1) {
            this.currThreshold = this.defThreshold;
        }
        if (this.controlComp != null) {
            this.controlComp.updateBuffer(this.currBuffer);
            this.controlComp.updateThreshold(this.currThreshold);
        }
        this.inited = true;
    }

    /* access modifiers changed from: protected */
    public void removeSourceStream(RTPSourceStream s) {
        this.sourcestreamlist.removeElement(s);
    }

    public long setBufferLength(long time) {
        if (this.inited) {
            if (time == -1) {
                time = this.defBuffer;
            }
            if (time == -2) {
                time = this.maxBuffer;
            }
            long j;
            if (time < this.currThreshold) {
                j = time;
                return this.currBuffer;
            }
            if (time >= this.maxBuffer) {
                this.currBuffer = this.maxBuffer;
            } else if (time <= 0 || time == this.defBuffer) {
                this.currBuffer = this.defBuffer;
            } else {
                this.currBuffer = time;
            }
            for (int i = 0; i < this.sourcestreamlist.size(); i++) {
                ((RTPSourceStream) this.sourcestreamlist.elementAt(i)).updateBuffer(this.currBuffer);
            }
            if (this.controlComp != null) {
                this.controlComp.updateBuffer(this.currBuffer);
            }
            j = time;
            return this.currBuffer;
        }
        this.currBuffer = time;
        return time;
    }

    public void setEnabledThreshold(boolean b) {
        this.threshold_enabled = b;
    }

    public long setMinimumThreshold(long t) {
        if (this.inited) {
            if (t == -1) {
                t = this.defThreshold;
            }
            if (t == -2) {
                t = this.maxThreshold;
            }
            long j;
            if (t > this.currBuffer) {
                j = t;
                return this.currThreshold;
            }
            if (t >= this.maxThreshold) {
                this.currThreshold = this.maxThreshold;
            } else if (t == this.defThreshold) {
                this.currThreshold = this.defThreshold;
            } else {
                this.currThreshold = t;
            }
            if (t < 0) {
                this.currThreshold = 0;
            }
            for (int i = 0; i < this.sourcestreamlist.size(); i++) {
                ((RTPSourceStream) this.sourcestreamlist.elementAt(i)).updateThreshold(this.currThreshold);
            }
            if (this.controlComp != null) {
                this.controlComp.updateThreshold(this.currThreshold);
            }
            j = t;
            return this.currThreshold;
        }
        this.currThreshold = t;
        return t;
    }
}
