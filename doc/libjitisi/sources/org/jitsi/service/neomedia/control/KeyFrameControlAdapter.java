package org.jitsi.service.neomedia.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jitsi.service.neomedia.control.KeyFrameControl.KeyFrameRequestee;
import org.jitsi.service.neomedia.control.KeyFrameControl.KeyFrameRequester;

public class KeyFrameControlAdapter implements KeyFrameControl {
    private List<KeyFrameRequestee> keyFrameRequestees = new ArrayList(0);
    private List<KeyFrameRequester> keyFrameRequesters = new ArrayList(0);
    private List<KeyFrameRequestee> unmodifiableKeyFrameRequestees;
    private List<KeyFrameRequester> unmodifiableKeyFrameRequesters;

    public void addKeyFrameRequestee(int index, KeyFrameRequestee keyFrameRequestee) {
        if (keyFrameRequestee == null) {
            throw new NullPointerException("keyFrameRequestee");
        }
        synchronized (this) {
            if (!this.keyFrameRequestees.contains(keyFrameRequestee)) {
                List<KeyFrameRequestee> newKeyFrameRequestees = new ArrayList(this.keyFrameRequestees.size() + 1);
                newKeyFrameRequestees.addAll(this.keyFrameRequestees);
                if (-1 == index) {
                    if (keyFrameRequestee.getClass().getName().contains(".neomedia.")) {
                        index = newKeyFrameRequestees.size();
                    } else {
                        index = 0;
                    }
                }
                newKeyFrameRequestees.add(index, keyFrameRequestee);
                this.keyFrameRequestees = newKeyFrameRequestees;
                this.unmodifiableKeyFrameRequestees = null;
            }
        }
    }

    public void addKeyFrameRequester(int index, KeyFrameRequester keyFrameRequester) {
        if (keyFrameRequester == null) {
            throw new NullPointerException("keyFrameRequester");
        }
        synchronized (this) {
            if (!this.keyFrameRequesters.contains(keyFrameRequester)) {
                List<KeyFrameRequester> newKeyFrameRequesters = new ArrayList(this.keyFrameRequesters.size() + 1);
                newKeyFrameRequesters.addAll(this.keyFrameRequesters);
                if (-1 == index) {
                    if (keyFrameRequester.getClass().getName().contains(".neomedia.")) {
                        index = newKeyFrameRequesters.size();
                    } else {
                        index = 0;
                    }
                }
                newKeyFrameRequesters.add(index, keyFrameRequester);
                this.keyFrameRequesters = newKeyFrameRequesters;
                this.unmodifiableKeyFrameRequesters = null;
            }
        }
    }

    public List<KeyFrameRequestee> getKeyFrameRequestees() {
        List list;
        synchronized (this) {
            if (this.unmodifiableKeyFrameRequestees == null) {
                this.unmodifiableKeyFrameRequestees = Collections.unmodifiableList(this.keyFrameRequestees);
            }
            list = this.unmodifiableKeyFrameRequestees;
        }
        return list;
    }

    public List<KeyFrameRequester> getKeyFrameRequesters() {
        List list;
        synchronized (this) {
            if (this.unmodifiableKeyFrameRequesters == null) {
                this.unmodifiableKeyFrameRequesters = Collections.unmodifiableList(this.keyFrameRequesters);
            }
            list = this.unmodifiableKeyFrameRequesters;
        }
        return list;
    }

    public boolean keyFrameRequest() {
        for (KeyFrameRequestee keyFrameRequestee : getKeyFrameRequestees()) {
            try {
                if (keyFrameRequestee.keyFrameRequest()) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    public boolean removeKeyFrameRequestee(KeyFrameRequestee keyFrameRequestee) {
        boolean z;
        synchronized (this) {
            int index = this.keyFrameRequestees.indexOf(keyFrameRequestee);
            if (-1 != index) {
                List<KeyFrameRequestee> newKeyFrameRequestees = new ArrayList(this.keyFrameRequestees);
                newKeyFrameRequestees.remove(index);
                this.keyFrameRequestees = newKeyFrameRequestees;
                this.unmodifiableKeyFrameRequestees = null;
                z = true;
            } else {
                z = false;
            }
        }
        return z;
    }

    public boolean removeKeyFrameRequester(KeyFrameRequester keyFrameRequester) {
        boolean z;
        synchronized (this) {
            int index = this.keyFrameRequesters.indexOf(keyFrameRequester);
            if (-1 != index) {
                List<KeyFrameRequester> newKeyFrameRequesters = new ArrayList(this.keyFrameRequesters);
                newKeyFrameRequesters.remove(index);
                this.keyFrameRequesters = newKeyFrameRequesters;
                this.unmodifiableKeyFrameRequesters = null;
                z = true;
            } else {
                z = false;
            }
        }
        return z;
    }

    public boolean requestKeyFrame(boolean urgent) {
        for (KeyFrameRequester keyFrameRequester : getKeyFrameRequesters()) {
            try {
                if (keyFrameRequester.requestKeyFrame()) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }
}
