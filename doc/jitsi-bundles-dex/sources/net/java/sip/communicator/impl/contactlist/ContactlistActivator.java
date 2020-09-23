package net.java.sip.communicator.impl.contactlist;

import net.java.sip.communicator.service.contactlist.MetaContactListService;
import net.java.sip.communicator.service.protocol.AccountManager;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.ServiceUtils;
import org.jitsi.service.fileaccess.FileAccessService;
import org.jitsi.service.resources.ResourceManagementService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ContactlistActivator implements BundleActivator {
    private static AccountManager accountManager;
    private static BundleContext bundleContext;
    private static FileAccessService fileAccessService;
    private static final Logger logger = Logger.getLogger(ContactlistActivator.class);
    private static ResourceManagementService resourcesService;
    private MetaContactListServiceImpl mclServiceImpl = null;

    public void start(BundleContext context) throws Exception {
        bundleContext = context;
        if (logger.isDebugEnabled()) {
            logger.debug("Service Impl: " + getClass().getName() + " [  STARTED ]");
        }
        this.mclServiceImpl = new MetaContactListServiceImpl();
        context.registerService(MetaContactListService.class.getName(), this.mclServiceImpl, null);
        this.mclServiceImpl.start(context);
        if (logger.isDebugEnabled()) {
            logger.debug("Service Impl: " + getClass().getName() + " [REGISTERED]");
        }
    }

    public void stop(BundleContext context) throws Exception {
        if (logger.isTraceEnabled()) {
            logger.trace("Stopping the contact list.");
        }
        if (this.mclServiceImpl != null) {
            this.mclServiceImpl.stop(context);
        }
    }

    public static FileAccessService getFileAccessService() {
        if (fileAccessService == null) {
            fileAccessService = (FileAccessService) ServiceUtils.getService(bundleContext, FileAccessService.class);
        }
        return fileAccessService;
    }

    public static ResourceManagementService getResources() {
        if (resourcesService == null) {
            resourcesService = (ResourceManagementService) ServiceUtils.getService(bundleContext, ResourceManagementService.class);
        }
        return resourcesService;
    }

    public static AccountManager getAccountManager() {
        if (accountManager == null) {
            accountManager = (AccountManager) ServiceUtils.getService(bundleContext, AccountManager.class);
        }
        return accountManager;
    }
}
