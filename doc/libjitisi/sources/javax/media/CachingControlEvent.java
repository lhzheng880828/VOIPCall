package javax.media;

public class CachingControlEvent extends ControllerEvent {
    CachingControl cachingControl;
    long progress;

    public CachingControlEvent(Controller from, CachingControl cachingControl, long progress) {
        super(from);
        this.cachingControl = cachingControl;
        this.progress = progress;
    }

    public CachingControl getCachingControl() {
        return this.cachingControl;
    }

    public long getContentProgress() {
        return this.progress;
    }

    public String toString() {
        return getClass().getName() + "[source=" + getSource() + ",cachingControl=" + this.cachingControl + ",progress=" + this.progress + "]";
    }
}
