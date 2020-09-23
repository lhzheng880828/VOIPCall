package javax.media;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.fmj.utility.LoggerSingleton;

public class PackageManager {
    private static Method commitContentPrefixListMethod;
    private static Method commitProtocolPrefixListMethod;
    private static Method getContentPrefixListMethod;
    private static Method getProtocolPrefixListMethod;
    private static Class<?> implClass;
    private static final Logger logger = LoggerSingleton.logger;
    private static Method setContentPrefixListMethod;
    private static Method setProtocolPrefixListMethod;

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

    public static void commitContentPrefixList() {
        if (init()) {
            callImpl(commitContentPrefixListMethod, new Object[0]);
        }
    }

    public static void commitProtocolPrefixList() {
        if (init()) {
            callImpl(commitProtocolPrefixListMethod, new Object[0]);
        }
    }

    public static Vector getContentPrefixList() {
        if (init()) {
            return (Vector) callImpl(getContentPrefixListMethod, new Object[0]);
        }
        return null;
    }

    public static Vector getProtocolPrefixList() {
        if (init()) {
            return (Vector) callImpl(getProtocolPrefixListMethod, new Object[0]);
        }
        return null;
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
        synchronized (PackageManager.class) {
            if (implClass == null) {
                try {
                    implClass = Class.forName("javax.media.pm.PackageManager");
                    if (PackageManager.class.isAssignableFrom(implClass)) {
                        getProtocolPrefixListMethod = getStaticMethodOnImplClass("getProtocolPrefixList", new Class[0], Vector.class);
                        setProtocolPrefixListMethod = getStaticMethodOnImplClass("setProtocolPrefixList", new Class[]{Vector.class}, Void.TYPE);
                        commitProtocolPrefixListMethod = getStaticMethodOnImplClass("commitProtocolPrefixList", new Class[0], Void.TYPE);
                        getContentPrefixListMethod = getStaticMethodOnImplClass("getContentPrefixList", new Class[0], Vector.class);
                        setContentPrefixListMethod = getStaticMethodOnImplClass("setContentPrefixList", new Class[]{Vector.class}, Void.TYPE);
                        commitContentPrefixListMethod = getStaticMethodOnImplClass("commitContentPrefixList", new Class[0], Void.TYPE);
                    } else {
                        throw new Exception("javax.media.pm.PackageManager not subclass of " + PackageManager.class.getName());
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

    public static void setContentPrefixList(Vector list) {
        if (init()) {
            callImpl(setContentPrefixListMethod, new Object[]{list});
        }
    }

    public static void setProtocolPrefixList(Vector list) {
        if (init()) {
            callImpl(setProtocolPrefixListMethod, new Object[]{list});
        }
    }
}
