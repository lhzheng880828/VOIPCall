package javax.media;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.fmj.utility.LoggerSingleton;

public class PlugInManager {
    public static final int CODEC = 2;
    public static final int DEMULTIPLEXER = 1;
    public static final int EFFECT = 3;
    public static final int MULTIPLEXER = 5;
    public static final int RENDERER = 4;
    private static Method addPlugInMethod;
    private static Method commitMethod;
    private static Method getPlugInListMethod;
    private static Method getSupportedInputFormatsMethod;
    private static Method getSupportedOutputFormatsMethod;
    private static Class<?> implClass;
    private static final Logger logger = LoggerSingleton.logger;
    private static Method removePlugInMethod;
    private static Method setPlugInListMethod;

    public static boolean addPlugIn(String classname, Format[] in, Format[] out, int type) {
        if (!init()) {
            return false;
        }
        return ((Boolean) callImpl(addPlugInMethod, new Object[]{classname, in, out, Integer.valueOf(type)})).booleanValue();
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
            callImpl(commitMethod, new Object[0]);
        }
    }

    public static Vector getPlugInList(Format input, Format output, int type) {
        if (!init()) {
            return null;
        }
        return (Vector) callImpl(getPlugInListMethod, new Object[]{input, output, Integer.valueOf(type)});
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

    public static Format[] getSupportedInputFormats(String className, int type) {
        if (!init()) {
            return null;
        }
        return (Format[]) callImpl(getSupportedInputFormatsMethod, new Object[]{className, Integer.valueOf(type)});
    }

    public static Format[] getSupportedOutputFormats(String className, int type) {
        if (!init()) {
            return null;
        }
        return (Format[]) callImpl(getSupportedOutputFormatsMethod, new Object[]{className, Integer.valueOf(type)});
    }

    private static synchronized boolean init() {
        boolean z = true;
        synchronized (PlugInManager.class) {
            if (implClass == null) {
                try {
                    implClass = Class.forName("javax.media.pim.PlugInManager");
                    if (PlugInManager.class.isAssignableFrom(implClass)) {
                        getPlugInListMethod = getStaticMethodOnImplClass("getPlugInList", new Class[]{Format.class, Format.class, Integer.TYPE}, Vector.class);
                        setPlugInListMethod = getStaticMethodOnImplClass("setPlugInList", new Class[]{Vector.class, Integer.TYPE}, Void.TYPE);
                        commitMethod = getStaticMethodOnImplClass("commit", new Class[0], Void.TYPE);
                        addPlugInMethod = getStaticMethodOnImplClass("addPlugIn", new Class[]{String.class, Format[].class, Format[].class, Integer.TYPE}, Boolean.TYPE);
                        removePlugInMethod = getStaticMethodOnImplClass("removePlugIn", new Class[]{String.class, Integer.TYPE}, Boolean.TYPE);
                        getSupportedInputFormatsMethod = getStaticMethodOnImplClass("getSupportedInputFormats", new Class[]{String.class, Integer.TYPE}, Format[].class);
                        getSupportedOutputFormatsMethod = getStaticMethodOnImplClass("getSupportedOutputFormats", new Class[]{String.class, Integer.TYPE}, Format[].class);
                    } else {
                        throw new Exception("javax.media.pim.PlugInManager not subclass of " + PlugInManager.class.getName());
                    }
                } catch (Throwable e) {
                    implClass = null;
                    logger.log(Level.SEVERE, "Unable to initialize javax.media.pim.PlugInManager: " + e, e);
                    z = false;
                }
            }
        }
        return z;
    }

    public static boolean removePlugIn(String classname, int type) {
        if (!init()) {
            return false;
        }
        return ((Boolean) callImpl(removePlugInMethod, new Object[]{classname, Integer.valueOf(type)})).booleanValue();
    }

    public static void setPlugInList(Vector plugins, int type) {
        if (init()) {
            callImpl(setPlugInListMethod, new Object[]{plugins, Integer.valueOf(type)});
        }
    }
}
