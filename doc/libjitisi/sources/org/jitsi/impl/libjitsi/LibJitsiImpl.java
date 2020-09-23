package org.jitsi.impl.libjitsi;

import java.util.HashMap;
import java.util.Map;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.util.Logger;

public class LibJitsiImpl extends LibJitsi {
    private static final Logger logger = Logger.getLogger(LibJitsiImpl.class);
    private final Map<String, Object> services = new HashMap();

    public LibJitsiImpl() {
        String key = "org.jitsi.service.audionotifier.AudioNotifierService";
        String value = System.getProperty(key);
        if (value == null || value.length() == 0) {
            System.setProperty(key, "org.jitsi.impl.neomedia.notify.AudioNotifierServiceImpl");
        }
    }

    /* access modifiers changed from: protected */
    public <T> T getService(Class<T> serviceClass) {
        T service;
        String serviceClassName = serviceClass.getName();
        synchronized (this.services) {
            if (this.services.containsKey(serviceClassName)) {
                service = this.services.get(serviceClassName);
            } else {
                this.services.put(serviceClassName, null);
                String serviceImplClassName = System.getProperty(serviceClassName);
                if (serviceImplClassName == null || serviceImplClassName.length() == 0) {
                    serviceImplClassName = serviceClassName.replace(".service.", ".impl.").concat("Impl");
                }
                Class<?> serviceImplClass = null;
                Throwable exception = null;
                try {
                    serviceImplClass = Class.forName(serviceImplClassName);
                } catch (ClassNotFoundException cnfe) {
                    exception = cnfe;
                } catch (ExceptionInInitializerError eiie) {
                    exception = eiie;
                } catch (LinkageError le) {
                    exception = le;
                }
                service = null;
                if (serviceImplClass != null && serviceClass.isAssignableFrom(serviceImplClass)) {
                    try {
                        service = serviceImplClass.newInstance();
                    } catch (Throwable t) {
                        if (t instanceof ThreadDeath) {
                            ThreadDeath t2 = (ThreadDeath) t;
                        } else {
                            exception = t;
                        }
                    }
                }
                if (exception == null) {
                    synchronized (this.services) {
                        if (service != null) {
                            this.services.put(serviceClassName, service);
                        }
                    }
                } else if (logger.isInfoEnabled()) {
                    logger.info("Failed to initialize service implementation " + serviceImplClassName + ". Will continue without it.", exception);
                }
            }
        }
        return service;
    }
}
