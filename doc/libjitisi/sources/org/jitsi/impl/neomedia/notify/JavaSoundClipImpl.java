package org.jitsi.impl.neomedia.notify;

import java.applet.AudioClip;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import org.jitsi.service.audionotifier.AbstractSCAudioClip;
import org.jitsi.service.audionotifier.AudioNotifierService;

public class JavaSoundClipImpl extends AbstractSCAudioClip {
    private static Constructor<AudioClip> acConstructor = null;
    private final AudioClip audioClip;

    /* access modifiers changed from: private|static */
    public static Constructor<AudioClip> createAcConstructor() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Class<?> class1;
        try {
            class1 = Class.forName("com.sun.media.sound.JavaSoundAudioClip", true, ClassLoader.getSystemClassLoader());
        } catch (ClassNotFoundException e) {
            class1 = Class.forName("sun.audio.SunAudioClip", true, null);
        }
        return class1.getConstructor(new Class[]{InputStream.class});
    }

    private static AudioClip createAppletAudioClip(InputStream inputstream) throws IOException {
        if (acConstructor == null) {
            try {
                acConstructor = (Constructor) AccessController.doPrivileged(new PrivilegedExceptionAction<Constructor<AudioClip>>() {
                    public Constructor<AudioClip> run() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
                        return JavaSoundClipImpl.createAcConstructor();
                    }
                });
            } catch (PrivilegedActionException paex) {
                throw new IOException("Failed to get AudioClip constructor: " + paex.getException());
            }
        }
        try {
            return (AudioClip) acConstructor.newInstance(new Object[]{inputstream});
        } catch (Exception ex) {
            throw new IOException("Failed to construct the AudioClip: " + ex);
        }
    }

    public JavaSoundClipImpl(String uri, AudioNotifierService audioNotifier) throws IOException {
        super(uri, audioNotifier);
        this.audioClip = createAppletAudioClip(new URL(uri).openStream());
    }

    /* access modifiers changed from: protected */
    public void internalStop() {
        try {
            if (this.audioClip != null) {
                this.audioClip.stop();
            }
            super.internalStop();
        } catch (Throwable th) {
            super.internalStop();
        }
    }

    /* access modifiers changed from: protected */
    public boolean runOnceInPlayThread() {
        if (this.audioClip == null) {
            return false;
        }
        this.audioClip.play();
        return true;
    }
}
