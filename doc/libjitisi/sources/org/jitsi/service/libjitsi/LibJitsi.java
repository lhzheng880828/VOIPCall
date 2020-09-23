package org.jitsi.service.libjitsi;

import java.lang.reflect.Constructor;
import org.jitsi.service.audionotifier.AudioNotifierService;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.fileaccess.FileAccessService;
import org.jitsi.service.neomedia.MediaService;
import org.jitsi.service.packetlogging.PacketLoggingService;
import org.jitsi.service.resources.ResourceManagementService;
import org.jitsi.util.Logger;

public abstract class LibJitsi {
    private static LibJitsi impl;
    private static final Logger logger = Logger.getLogger(LibJitsi.class);

    public abstract <T> T getService(Class<T> cls);

    public static AudioNotifierService getAudioNotifierService() {
        return (AudioNotifierService) invokeGetServiceOnImpl(AudioNotifierService.class);
    }

    public static ConfigurationService getConfigurationService() {
        return (ConfigurationService) invokeGetServiceOnImpl(ConfigurationService.class);
    }

    public static FileAccessService getFileAccessService() {
        return (FileAccessService) invokeGetServiceOnImpl(FileAccessService.class);
    }

    public static MediaService getMediaService() {
        return (MediaService) invokeGetServiceOnImpl(MediaService.class);
    }

    public static PacketLoggingService getPacketLoggingService() {
        return (PacketLoggingService) invokeGetServiceOnImpl(PacketLoggingService.class);
    }

    public static ResourceManagementService getResourceManagementService() {
        return (ResourceManagementService) invokeGetServiceOnImpl(ResourceManagementService.class);
    }

    private static <T> T invokeGetServiceOnImpl(Class<T> serviceClass) {
        LibJitsi impl = impl;
        if (impl != null) {
            return impl.getService(serviceClass);
        }
        throw new IllegalStateException("impl");
    }

    public static void start() {
        start(null);
    }

    private static void start(Object context) {
        String implBaseClassName = LibJitsi.class.getName().replace(".service.", ".impl.");
        String[] implClassNameExtensions = new String[]{"OSGi", ""};
        LibJitsi impl = null;
        for (int i = 0; i < implClassNameExtensions.length; i++) {
            Class<?> implClass = null;
            String implClassName = implBaseClassName + implClassNameExtensions[i] + "Impl";
            Throwable exception = null;
            try {
                implClass = Class.forName(implClassName);
            } catch (ClassNotFoundException cnfe) {
                exception = cnfe;
            } catch (ExceptionInInitializerError eiie) {
                exception = eiie;
            } catch (LinkageError le) {
                exception = le;
            }
            if (implClass != null && LibJitsi.class.isAssignableFrom(implClass)) {
                if (context == null) {
                    try {
                        impl = (LibJitsi) implClass.newInstance();
                    } catch (Throwable t) {
                        if (t instanceof ThreadDeath) {
                            ThreadDeath t2 = (ThreadDeath) t;
                        } else {
                            exception = t;
                        }
                    }
                } else {
                    Constructor<?> constructor = null;
                    for (Constructor<?> aConstructor : implClass.getConstructors()) {
                        Class<?>[] parameterTypes = aConstructor.getParameterTypes();
                        if (parameterTypes.length == 1 && parameterTypes[0].isInstance(context)) {
                            constructor = aConstructor;
                            break;
                        }
                    }
                    impl = (LibJitsi) constructor.newInstance(new Object[]{context});
                }
                if (impl != null) {
                    break;
                }
            }
            if (exception != null && logger.isInfoEnabled()) {
                StringBuilder message = new StringBuilder();
                message.append("Failed to initialize LibJitsi backend ");
                message.append(implClassName);
                message.append(". (Exception stack trace follows.)");
                if (i < implClassNameExtensions.length - 1) {
                    message.append(" Will try an alternative.");
                }
                logger.info(message, exception);
            }
        }
        if (impl == null) {
            throw new IllegalStateException("impl");
        }
        impl = impl;
        if (logger.isInfoEnabled()) {
            logger.info("Successfully started LibJitsi using as implementation: " + impl.getClass().getCanonicalName());
        }
    }

    public static void stop() {
        impl = null;
    }

    protected LibJitsi() {
    }
}
