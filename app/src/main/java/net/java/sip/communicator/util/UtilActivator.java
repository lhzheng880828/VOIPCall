/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.java.sip.communicator.util;

import android.util.Log;

import net.java.sip.communicator.impl.globaldisplaydetails.GlobalStatusServiceImpl;
import net.java.sip.communicator.service.gui.AlertUIService;
import net.java.sip.communicator.service.gui.UIService;
import net.java.sip.communicator.service.protocol.AccountManager;
import net.java.sip.communicator.service.protocol.ProtocolProviderFactory;
import net.java.sip.communicator.service.protocol.globalstatus.GlobalStatusService;
import net.java.sip.communicator.service.resources.ResourceManagementServiceUtils;
import net.java.sip.communicator.service.systray.SystrayService;

import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.fileaccess.FileAccessService;
import org.jitsi.service.neomedia.MediaConfigurationService;
import org.jitsi.service.neomedia.MediaService;
import org.jitsi.service.resources.ResourceManagementService;
import org.jitsi.util.OSUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * The only raison d'etre for this Activator is so that it would set a global
 * exception handler. It doesn't export any services and neither it runs any
 * initialization - all it does is call
 * <tt>Thread.setUncaughtExceptionHandler()</tt>
 *
 * @author Emil Ivov
 */
public class UtilActivator
    implements BundleActivator,
               Thread.UncaughtExceptionHandler
{
    /**
     * The <tt>Logger</tt> used by the <tt>UtilActivator</tt> class and its
     * instances for logging output.
     */
    private static final Logger logger
        = Logger.getLogger(UtilActivator.class);

    private static ConfigurationService configurationService;

    private static ResourceManagementService resourceService;

    private static UIService uiService;

    private static FileAccessService fileAccessService;

    private static MediaService mediaService;

    public static BundleContext bundleContext;

    private static AccountManager accountManager;

    private static AlertUIService alertUIService;

    private static SystrayService systrayService;

    /**
     * Calls <tt>Thread.setUncaughtExceptionHandler()</tt>
     *
     * @param context The execution context of the bundle being started
     * (unused).
     * @throws Exception If this method throws an exception, this bundle is
     *   marked as stopped and the Framework will remove this bundle's
     *   listeners, unregister all services registered by this bundle, and
     *   release all services used by this bundle.
     */
    public void start(BundleContext context)
        throws Exception
    {
        bundleContext = context;

        Log.d("GlobalStatusReg", "register");
        String name = GlobalStatusService.class.getName();
        GlobalStatusService instance = new GlobalStatusServiceImpl();
        Log.d("GlobalStatusReg", "name = "+name+", instance = "+instance);
        context.registerService(name, instance,null);

        if(OSUtils.IS_ANDROID)
            loadLoggingConfig();

        if (logger.isTraceEnabled())
            logger.trace("Setting default uncaught exception handler.");
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * Loads logging config if any. Need to be loaded in order to activate
     * logging and need to be activated after bundle context is initialized.
     */
    private void loadLoggingConfig()
    {
        try
        {
            Class.forName(
                    "net.java.sip.communicator.util.JavaUtilLoggingConfig")
                .newInstance();
        }
        catch(Throwable t){}
    }

    /**
     * Method invoked when a thread would terminate due to the given uncaught
     * exception. All we do here is simply log the exception using the system
     * logger.
     *
     * <p>Any exception thrown by this method will be ignored by the
     * Java Virtual Machine and thus won't screw our application.
     *
     * @param thread the thread
     * @param exc the exception
     */
    public void uncaughtException(Thread thread, Throwable exc)
    {
        logger.error("An uncaught exception occurred in thread="
                     + thread
                     + " and message was: "
                     + exc.getMessage()
                     , exc);
    }

    /**
     * Doesn't do anything.
     *
     * @param context The execution context of the bundle being stopped.
     * @throws Exception If this method throws an exception, the bundle is
     *   still marked as stopped, and the Framework will remove the bundle's
     *   listeners, unregister all services registered by the bundle, and
     *   release all services used by the bundle.
     */
    public void stop(BundleContext context)
        throws Exception
    {
    }

    /**
     * Returns the <tt>ConfigurationService</tt> currently registered.
     *
     * @return the <tt>ConfigurationService</tt>
     */
    public static ConfigurationService getConfigurationService()
    {
        if (configurationService == null)
        {
            configurationService
                = ServiceUtils.getService(
                        bundleContext,
                        ConfigurationService.class);
        }
        return configurationService;
    }

    /**
     * Returns the service giving access to all application resources.
     *
     * @return the service giving access to all application resources.
     */
    public static ResourceManagementService getResources()
    {
        if (resourceService == null)
        {
            resourceService
                = ResourceManagementServiceUtils.getService(bundleContext);
        }
        return resourceService;
    }

    /**
     * Gets the <tt>UIService</tt> instance registered in the
     * <tt>BundleContext</tt> of the <tt>UtilActivator</tt>.
     *
     * @return the <tt>UIService</tt> instance registered in the
     * <tt>BundleContext</tt> of the <tt>UtilActivator</tt>
     */
    public static UIService getUIService()
    {
        if (uiService == null)
            uiService = ServiceUtils.getService(bundleContext, UIService.class);
        return uiService;
    }

    /**
     * Gets the <tt>SystrayService</tt> instance registered in the
     * <tt>BundleContext</tt> of the <tt>UtilActivator</tt>.
     *
     * @return the <tt>SystrayService</tt> instance registered in the
     * <tt>BundleContext</tt> of the <tt>UtilActivator</tt>
     */
    public static SystrayService getSystrayService()
    {
        if (systrayService == null)
            systrayService =
                ServiceUtils.getService(bundleContext, SystrayService.class);
        return systrayService;
    }

    /**
     * Returns the <tt>FileAccessService</tt> obtained from the bundle context.
     *
     * @return the <tt>FileAccessService</tt> obtained from the bundle context
     */
    public static FileAccessService getFileAccessService()
    {
        if (fileAccessService == null)
        {
            fileAccessService
                = ServiceUtils.getService(
                        bundleContext,
                        FileAccessService.class);
        }
        return fileAccessService;
    }

    /**
     * Returns an instance of the <tt>MediaService</tt> obtained from the
     * bundle context.
     * @return an instance of the <tt>MediaService</tt> obtained from the
     * bundle context
     */
    public static MediaService getMediaService()
    {
        if (mediaService == null)
        {
            mediaService
                = ServiceUtils.getService(bundleContext, MediaService.class);
        }
        return mediaService;
    }

    /**
     * Returns the {@link MediaConfigurationService} instance registered in the
     * <tt>BundleContext</tt> of the <tt>UtilActivator</tt>.
     *
     * @return the <tt>UIService</tt> instance registered in the
     * <tt>BundleContext</tt> of the <tt>UtilActivator</tt>
     */
    public static MediaConfigurationService getMediaConfiguration()
    {
        return ServiceUtils.getService(bundleContext,
                MediaConfigurationService.class);
    }

    /**
     * Returns all <tt>ProtocolProviderFactory</tt>s obtained from the bundle
     * context.
     *
     * @return all <tt>ProtocolProviderFactory</tt>s obtained from the bundle
     *         context
     */
    public static Map<Object, ProtocolProviderFactory>
        getProtocolProviderFactories()
    {
        Collection<ServiceReference<ProtocolProviderFactory>> serRefs;
        Map<Object, ProtocolProviderFactory> providerFactoriesMap
            = new Hashtable<Object, ProtocolProviderFactory>();

        try
        {
            // get all registered provider factories
            serRefs
                = bundleContext.getServiceReferences(
                        ProtocolProviderFactory.class,
                        null);
        }
        catch (InvalidSyntaxException ex)
        {
            serRefs = null;
            logger.error("LoginManager : " + ex);
        }

        if ((serRefs != null) && !serRefs.isEmpty())
        {
            for (ServiceReference<ProtocolProviderFactory> serRef : serRefs)
            {
                ProtocolProviderFactory providerFactory
                    = bundleContext.getService(serRef);

                providerFactoriesMap.put(
                        serRef.getProperty(ProtocolProviderFactory.PROTOCOL),
                        providerFactory);
            }
        }
        return providerFactoriesMap;
    }

    /**
     * Returns the <tt>AccountManager</tt> obtained from the bundle context.
     * @return the <tt>AccountManager</tt> obtained from the bundle context
     */
    public static AccountManager getAccountManager()
    {
        if(accountManager == null)
        {
            accountManager
                = ServiceUtils.getService(bundleContext, AccountManager.class);
        }
        return accountManager;
    }

    /**
     * Returns the <tt>MetaContactListService</tt> obtained from the bundle
     * context.
     * @return the <tt>MetaContactListService</tt> obtained from the bundle
     * context
     */
    public static AlertUIService getAlertUIService()
    {
        if (alertUIService == null)
        {
            alertUIService
                = ServiceUtils.getService(
                        bundleContext,
                        AlertUIService.class);
        }
        return alertUIService;
    }
}
