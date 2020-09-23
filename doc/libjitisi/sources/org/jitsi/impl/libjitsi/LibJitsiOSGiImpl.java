package org.jitsi.impl.libjitsi;

import org.jitsi.service.libjitsi.LibJitsi;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class LibJitsiOSGiImpl extends LibJitsiImpl {
    private final BundleContext bundleContext;

    public LibJitsiOSGiImpl() {
        Bundle bundle = FrameworkUtil.getBundle(LibJitsi.class);
        if (bundle == null) {
            throw new IllegalStateException("FrameworkUtil.getBundle");
        }
        BundleContext bundleContext = bundle.getBundleContext();
        if (bundleContext == null) {
            throw new IllegalStateException("Bundle.getBundleContext");
        }
        this.bundleContext = bundleContext;
    }

    public LibJitsiOSGiImpl(BundleContext bundleContext) {
        if (bundleContext == null) {
            throw new NullPointerException("bundleContext");
        }
        this.bundleContext = bundleContext;
    }

    /* access modifiers changed from: protected */
    public <T> T getService(Class<T> serviceClass) {
        ServiceReference serviceReference = this.bundleContext.getServiceReference(serviceClass.getName());
        T service = serviceReference == null ? null : this.bundleContext.getService(serviceReference);
        if (service == null) {
            return super.getService(serviceClass);
        }
        return service;
    }
}
