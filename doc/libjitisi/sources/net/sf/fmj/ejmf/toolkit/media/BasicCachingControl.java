package net.sf.fmj.ejmf.toolkit.media;

import javax.media.CachingControl;
import javax.media.CachingControlEvent;
import org.jitsi.android.util.java.awt.Component;
import org.jitsi.android.util.java.awt.event.ActionEvent;
import org.jitsi.android.util.java.awt.event.ActionListener;
import org.jitsi.android.util.javax.swing.JButton;
import org.jitsi.android.util.javax.swing.JProgressBar;

public class BasicCachingControl implements CachingControl {
    private static final String PAUSEMESSAGE = "Pause";
    private static final String RESUMEMESSAGE = "Resume";
    private AbstractController controller;
    private boolean isDownloading;
    private boolean isPaused;
    private long length;
    /* access modifiers changed from: private */
    public JButton pauseButton;
    private long progress;
    private JProgressBar progressBar = new JProgressBar();

    public BasicCachingControl(AbstractController c, long length) {
        this.controller = c;
        this.progressBar.setMinimum(0);
        this.pauseButton = new JButton(PAUSEMESSAGE);
        this.pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String label = BasicCachingControl.this.pauseButton.getText();
                if (BasicCachingControl.this.isPaused()) {
                    BasicCachingControl.this.pauseButton.setText(BasicCachingControl.PAUSEMESSAGE);
                    BasicCachingControl.this.setPaused(false);
                } else {
                    BasicCachingControl.this.pauseButton.setText(BasicCachingControl.RESUMEMESSAGE);
                    BasicCachingControl.this.setPaused(true);
                }
                BasicCachingControl.this.pauseButton.getParent().validate();
            }
        });
        reset(length);
        this.controller.addControl(this);
    }

    public void addToProgress(long toAdd) {
        setContentProgress(this.progress + toAdd);
    }

    public synchronized void blockWhilePaused() {
        while (this.isPaused) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public long getContentLength() {
        return this.length;
    }

    public long getContentProgress() {
        return this.progress;
    }

    public Component getControlComponent() {
        return this.pauseButton;
    }

    public Component getProgressBarComponent() {
        return this.progressBar;
    }

    public boolean isDownloading() {
        return this.isDownloading;
    }

    public boolean isPaused() {
        return this.isPaused;
    }

    public synchronized void reset(long length) {
        this.length = length;
        this.progress = 0;
        this.progressBar.setValue(0);
        setContentLength(length);
        setDownLoading(false);
        setPaused(false);
        this.controller.postEvent(new CachingControlEvent(this.controller, this, this.progress));
    }

    public synchronized void setContentLength(long length) {
        this.length = length;
        if (length == CachingControl.LENGTH_UNKNOWN) {
            this.progressBar.setMaximum(0);
        } else {
            this.progressBar.setMaximum((int) length);
        }
    }

    public synchronized void setContentProgress(long progress) {
        blockWhilePaused();
        this.progress = progress;
        setDownLoading(progress < this.length);
        this.progressBar.setValue((int) progress);
        this.controller.postEvent(new CachingControlEvent(this.controller, this, progress));
    }

    public void setDone() {
        setContentProgress(this.length);
    }

    public void setDownLoading(boolean isDownloading) {
        this.isDownloading = isDownloading;
        if (!isDownloading) {
            setPaused(false);
        }
    }

    /* access modifiers changed from: protected|declared_synchronized */
    public synchronized void setPaused(boolean isPaused) {
        this.isPaused = isPaused;
        notifyAll();
    }
}
