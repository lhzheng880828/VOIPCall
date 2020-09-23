package net.java.sip.communicator.impl.libjitsi;

import org.jitsi.service.libjitsi.LibJitsi;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class LibJitsiActivator implements BundleActivator {
    public void start(BundleContext bundleContext) throws Exception {
        Method start;
        try {
            start = LibJitsi.class.getDeclaredMethod("start", new Class[]{Object.class});
            if (Modifier.isStatic(start.getModifiers())) {
                start.setAccessible(true);
                if (!start.isAccessible()) {
                    start = null;
                }
            } else {
                start = null;
            }
        } catch (NoSuchMethodException e) {
            start = null;
        } catch (SecurityException e2) {
            start = null;
        }
        if (start == null) {
            LibJitsi.start();
            return;
        }
        start.invoke(null, new Object[]{bundleContext});
    }

    public void stop(BundleContext bundleContext) throws Exception {
        LibJitsi.stop();
    }
}
