package net.java.sip.communicator.impl.fileaccess;

import org.jitsi.service.fileaccess.FileAccessService;
import org.jitsi.service.libjitsi.LibJitsi;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class FileAccessActivator implements BundleActivator {
    public void start(BundleContext bundleContext) throws Exception {
        FileAccessService fileAccessService = LibJitsi.getFileAccessService();
        if (fileAccessService != null) {
            bundleContext.registerService(FileAccessService.class.getName(), fileAccessService, null);
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
}
