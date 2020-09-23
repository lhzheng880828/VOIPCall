package javax.media;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.fmj.utility.LoggerSingleton;

public class CaptureDeviceManager {
    private static Method addDeviceMethod;
    private static Method commitMethod;
    private static Method getDeviceListMethod;
    private static Method getDeviceMethod;
    private static Class<?> implClass;
    private static final Logger logger = LoggerSingleton.logger;
    private static Method removeDeviceMethod;

    public static boolean addDevice(CaptureDeviceInfo newDevice) {
        if (!init()) {
            return false;
        }
        return ((Boolean) callImpl(addDeviceMethod, new Object[]{newDevice})).booleanValue();
    }

    private static Object callImpl(Method method, Object[] args) {
        Object obj = null;
        try {
            return method.invoke(null, args);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "" + e, e);
            return obj;
        } catch (IllegalAccessException e2) {
            logger.log(Level.WARNING, "" + e2, e2);
            return obj;
        } catch (InvocationTargetException e3) {
            logger.log(Level.WARNING, "" + e3, e3);
            return obj;
        }
    }

    public static void commit() throws IOException {
        if (init()) {
            try {
                commitMethod.invoke(null, new Object[0]);
            } catch (IllegalArgumentException e) {
                logger.log(Level.WARNING, "" + e, e);
            } catch (IllegalAccessException e2) {
                logger.log(Level.WARNING, "" + e2, e2);
            } catch (InvocationTargetException e3) {
                if (e3.getCause() instanceof IOException) {
                    throw ((IOException) e3.getCause());
                }
                logger.log(Level.WARNING, "" + e3, e3);
            }
        }
    }

    public static CaptureDeviceInfo getDevice(String deviceName) {
        if (!init()) {
            return null;
        }
        return (CaptureDeviceInfo) callImpl(getDeviceMethod, new Object[]{deviceName});
    }

    public static Vector getDeviceList(Format format) {
        if (!init()) {
            return null;
        }
        return (Vector) callImpl(getDeviceListMethod, new Object[]{format});
    }

    private static Method getStaticMethodOnImplClass(String name, Class<?>[] args, Class<?> returnType) throws Exception {
        Method m = implClass.getMethod(name, args);
        if (m.getReturnType() != returnType) {
            throw new Exception("Expected return type of method " + name + " to be " + returnType + ", was " + m.getReturnType());
        } else if (Modifier.isStatic(m.getModifiers())) {
            return m;
        } else {
            throw new Exception("Expected method " + name + " to be static");
        }
    }

    private static synchronized boolean init() {
        boolean z = true;
        synchronized (CaptureDeviceManager.class) {
            if (implClass == null) {
                try {
                    implClass = Class.forName("javax.media.cdm.CaptureDeviceManager");
                    if (CaptureDeviceManager.class.isAssignableFrom(implClass)) {
                        getDeviceMethod = getStaticMethodOnImplClass("getDevice", new Class[]{String.class}, CaptureDeviceInfo.class);
                        getDeviceListMethod = getStaticMethodOnImplClass("getDeviceList", new Class[]{Format.class}, Vector.class);
                        addDeviceMethod = getStaticMethodOnImplClass("addDevice", new Class[]{CaptureDeviceInfo.class}, Boolean.TYPE);
                        removeDeviceMethod = getStaticMethodOnImplClass("removeDevice", new Class[]{CaptureDeviceInfo.class}, Boolean.TYPE);
                        commitMethod = getStaticMethodOnImplClass("commit", new Class[0], Void.TYPE);
                    } else {
                        throw new Exception("javax.media.cdm.CaptureDeviceManager not subclass of " + CaptureDeviceManager.class.getName());
                    }
                } catch (Exception e) {
                    implClass = null;
                    logger.log(Level.WARNING, "" + e, e);
                    z = false;
                }
            }
        }
        return z;
    }

    public static boolean removeDevice(CaptureDeviceInfo device) {
        if (!init()) {
            return false;
        }
        return ((Boolean) callImpl(removeDeviceMethod, new Object[]{device})).booleanValue();
    }
}
