package net.java.sip.communicator.impl.replacement.smiley;

import java.util.Hashtable;
import net.java.sip.communicator.service.replacement.ReplacementService;
import net.java.sip.communicator.service.replacement.smilies.SmiliesReplacementService;
import net.java.sip.communicator.util.Logger;
import org.jitsi.service.resources.ResourceManagementService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class SmileyActivator implements BundleActivator {
    private static BundleContext bundleContext = null;
    private static final Logger logger = Logger.getLogger(SmileyActivator.class);
    private static ResourceManagementService resourcesService;
    private static ReplacementService smileySource = null;
    private ServiceRegistration smileyServReg = null;

    public void start(BundleContext context) throws Exception {
        bundleContext = context;
        Hashtable<String, String> hashtable = new Hashtable();
        hashtable.put("SOURCE", ReplacementServiceSmileyImpl.SMILEY_SOURCE);
        smileySource = new ReplacementServiceSmileyImpl();
        this.smileyServReg = context.registerService(SmiliesReplacementService.class.getName(), smileySource, hashtable);
        this.smileyServReg = context.registerService(ReplacementService.class.getName(), smileySource, hashtable);
        logger.info("Smiley source implementation [STARTED].");
    }

    public void stop(BundleContext context) throws Exception {
        this.smileyServReg.unregister();
        logger.info("Smiley source implementation [STOPPED].");
    }

    public static ResourceManagementService getResources() {
        if (resourcesService == null) {
            ServiceReference serviceReference = bundleContext.getServiceReference(ResourceManagementService.class.getName());
            if (serviceReference == null) {
                return null;
            }
            resourcesService = (ResourceManagementService) bundleContext.getService(serviceReference);
        }
        return resourcesService;
    }
}
